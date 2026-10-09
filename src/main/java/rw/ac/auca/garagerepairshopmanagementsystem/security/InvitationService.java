package rw.ac.auca.garagerepairshopmanagementsystem.security;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import rw.ac.auca.garagerepairshopmanagementsystem.dto.AcceptInvitationRequest;
import rw.ac.auca.garagerepairshopmanagementsystem.dto.InvitationPreviewResponse;
import rw.ac.auca.garagerepairshopmanagementsystem.dto.InvitationResponse;
import rw.ac.auca.garagerepairshopmanagementsystem.exception.BusinessException;
import rw.ac.auca.garagerepairshopmanagementsystem.exception.ResourceNotFoundException;
import rw.ac.auca.garagerepairshopmanagementsystem.messaging.NotificationService;
import rw.ac.auca.garagerepairshopmanagementsystem.model.Garage;
import rw.ac.auca.garagerepairshopmanagementsystem.model.UserInvitation;
import rw.ac.auca.garagerepairshopmanagementsystem.repository.UserInvitationRepository;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Base64;
import java.util.HexFormat;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class InvitationService {

    private static final int VALIDITY_HOURS = 72;
    private static final DateTimeFormatter EXPIRY_FORMAT = DateTimeFormatter.ofPattern("dd MMM yyyy 'at' HH:mm");

    private final UserInvitationRepository invitationRepository;
    private final AppUserRepository appUserRepository;
    private final GarageContext garageContext;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final NotificationService notificationService;
    private final String acceptanceUrl;
    private final boolean exposeLink;
    private final SecureRandom secureRandom = new SecureRandom();

    public InvitationService(UserInvitationRepository invitationRepository,
                             AppUserRepository appUserRepository,
                             GarageContext garageContext,
                             PasswordEncoder passwordEncoder,
                             JwtService jwtService,
                             NotificationService notificationService,
                             @Value("${app.security.invitation.accept-url:http://localhost:5173/accept-invitation}") String acceptanceUrl,
                             @Value("${app.security.invitation.expose-link:false}") boolean exposeLink) {
        this.invitationRepository = invitationRepository;
        this.appUserRepository = appUserRepository;
        this.garageContext = garageContext;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
        this.notificationService = notificationService;
        this.acceptanceUrl = acceptanceUrl;
        this.exposeLink = exposeLink;
    }

    @Transactional
    public InvitationResponse invite(Garage garage, String rawEmail, Role invitedRole) {
        UserPrincipal principal = garageContext.principal();
        Role role = invitedRole.effectiveRole();
        if (role == Role.SYSTEM_ADMIN || !canInvite(principal, role)) {
            throw new AccessDeniedException("You cannot invite this role");
        }
        if (!isSystemAdmin(principal) && !garage.getId().equals(principal.getGarageId())) {
            throw new AccessDeniedException("You can invite users only to your garage");
        }
        String email = rawEmail.trim().toLowerCase();
        if (appUserRepository.existsByEmail(email)) {
            throw new BusinessException("An account already exists for this email");
        }

        // Re-inviting the same address supersedes the pending invitation, so a lost email can be resent.
        LocalDateTime now = LocalDateTime.now();
        List<UserInvitation> pending = invitationRepository
                .findAllByEmailIgnoreCaseAndGarageIdAndAcceptedAtIsNullAndExpiresAtAfter(email, garage.getId(), now);
        for (UserInvitation previous : pending) {
            if (!canInvite(principal, previous.getRole().effectiveRole())) {
                throw new BusinessException("An active invitation with a higher role already exists for this email");
            }
            previous.setExpiresAt(now);
        }

        String token = createToken();
        AppUser inviter = appUserRepository.findById(principal.getId()).orElseThrow(() ->
                new AccessDeniedException("Inviting account no longer exists"));
        UserInvitation invitation = new UserInvitation();
        invitation.setGarage(garage);
        invitation.setInvitedBy(inviter);
        invitation.setEmail(email);
        invitation.setRole(role);
        invitation.setTokenHash(hashToken(token));
        invitation.setExpiresAt(now.plusHours(VALIDITY_HOURS));
        invitationRepository.save(invitation);

        String link = acceptanceUrl + (acceptanceUrl.contains("?") ? "&" : "?") + "token=" + token;
        notificationService.notifyEmail("USER_INVITED", email,
                "You're invited to join " + garage.getName() + " on Pitlane",
                invitationEmail(garage, role, displayName(inviter), link, invitation.getExpiresAt()));

        String message = pending.isEmpty()
                ? "Invitation email queued for " + email
                : "Previous invitation revoked; a new invitation email was queued for " + email;
        return new InvitationResponse(email, role.name(), exposeLink ? link : null, invitation.getExpiresAt(), message);
    }

    @Transactional(readOnly = true)
    public InvitationPreviewResponse preview(String rawToken) {
        UserInvitation invitation = findUsableInvitation(rawToken);
        return new InvitationPreviewResponse(
                invitation.getEmail(),
                invitation.getRole().effectiveRole().name(),
                invitation.getGarage().getName(),
                displayName(invitation.getInvitedBy()),
                invitation.getExpiresAt());
    }

    @Transactional
    public AuthResponse accept(AcceptInvitationRequest request) {
        UserInvitation invitation = findUsableInvitation(request.token());
        String username = request.username().trim();
        if (appUserRepository.existsByUsername(username)) {
            throw new BusinessException("Username already exists");
        }
        if (appUserRepository.existsByEmail(invitation.getEmail())) {
            throw new BusinessException("An account already exists for this email");
        }
        if (request.password().getBytes(StandardCharsets.UTF_8).length > 72) {
            throw new BusinessException("Password must be no more than 72 UTF-8 bytes");
        }

        Role role = invitation.getRole().effectiveRole();
        Garage garage = invitation.getGarage();
        AppUser user = new AppUser(username, invitation.getEmail(), passwordEncoder.encode(request.password()),
                Set.of(role), garage);
        user.setFullName(request.fullName().trim());
        user.setPhone(request.phone() == null || request.phone().isBlank() ? null : request.phone().trim());
        user = appUserRepository.save(user);
        invitation.setAcceptedAt(LocalDateTime.now());
        invitationRepository.save(invitation);

        AppUser inviter = invitation.getInvitedBy();
        notificationService.notifyEmail("INVITATION_ACCEPTED", inviter.getEmail(),
                user.getFullName() + " joined " + garage.getName(),
                "Hello " + displayName(inviter) + ",\n\n"
                        + user.getFullName() + " (" + user.getEmail() + ") accepted your invitation and joined "
                        + garage.getName() + " as " + role.name() + " with the username \"" + user.getUsername() + "\".\n\n"
                        + "— Pitlane");

        UserPrincipal principal = UserPrincipal.from(user);
        String token = jwtService.generateToken(principal);
        Set<String> roles = principal.getRoles().stream()
                .map(Role::effectiveRole)
                .map(Enum::name)
                .collect(Collectors.toSet());
        return new AuthResponse(token, user.getUsername(), roles, "Invitation accepted",
                garage.getId(), garage.getName());
    }

    private UserInvitation findUsableInvitation(String rawToken) {
        UserInvitation invitation = invitationRepository.findByTokenHash(hashToken(rawToken.trim()))
                .orElseThrow(() -> new ResourceNotFoundException("Invitation is invalid or has expired"));
        if (invitation.getAcceptedAt() != null) {
            throw new BusinessException("This invitation has already been accepted. Sign in instead.");
        }
        if (!invitation.getExpiresAt().isAfter(LocalDateTime.now())) {
            throw new BusinessException("This invitation has expired or was replaced. Ask your administrator for a new one.");
        }
        return invitation;
    }

    private static String invitationEmail(Garage garage, Role role, String inviter, String link, LocalDateTime expiresAt) {
        return "Hello,\n\n"
                + inviter + " invited you to join " + garage.getName() + " on Pitlane as "
                + role.name().replace('_', ' ') + ".\n\n"
                + "Open the link below to set up your account. You will choose your username and password "
                + "and add your contact details:\n\n"
                + link + "\n\n"
                + "This link can be used once and expires on " + EXPIRY_FORMAT.format(expiresAt) + ".\n"
                + "If you were not expecting this invitation, you can ignore this email.\n\n"
                + "— Pitlane";
    }

    private static String displayName(AppUser user) {
        return user.getFullName() == null || user.getFullName().isBlank() ? user.getUsername() : user.getFullName();
    }

    private static boolean canInvite(UserPrincipal principal, Role role) {
        return principal.getRoles().stream().anyMatch(actor -> actor.canInvite(role));
    }

    private static boolean isSystemAdmin(UserPrincipal principal) {
        return principal.getRoles().stream().map(Role::effectiveRole).anyMatch(actor -> actor == Role.SYSTEM_ADMIN);
    }

    private String createToken() {
        byte[] random = new byte[32];
        secureRandom.nextBytes(random);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(random);
    }

    private String hashToken(String token) {
        try {
            byte[] digest = MessageDigest.getInstance("SHA-256").digest(token.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(digest);
        } catch (java.security.NoSuchAlgorithmException exception) {
            throw new IllegalStateException("SHA-256 is unavailable", exception);
        }
    }
}
