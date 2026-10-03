package rw.ac.auca.garagerepairshopmanagementsystem.security;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import rw.ac.auca.garagerepairshopmanagementsystem.dto.AcceptInvitationRequest;
import rw.ac.auca.garagerepairshopmanagementsystem.dto.InvitationResponse;
import rw.ac.auca.garagerepairshopmanagementsystem.exception.BusinessException;
import rw.ac.auca.garagerepairshopmanagementsystem.exception.ResourceNotFoundException;
import rw.ac.auca.garagerepairshopmanagementsystem.messaging.NotificationService;
import rw.ac.auca.garagerepairshopmanagementsystem.model.Garage;
import rw.ac.auca.garagerepairshopmanagementsystem.model.UserInvitation;
import rw.ac.auca.garagerepairshopmanagementsystem.repository.UserInvitationRepository;
import rw.ac.auca.garagerepairshopmanagementsystem.security.AppUserRepository;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.Base64;
import java.util.HexFormat;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class InvitationService {

    private final UserInvitationRepository invitationRepository;
    private final AppUserRepository appUserRepository;
    private final GarageContext garageContext;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final NotificationService notificationService;
    private final String acceptanceUrl;
    private final SecureRandom secureRandom = new SecureRandom();

    public InvitationService(UserInvitationRepository invitationRepository,
                             AppUserRepository appUserRepository,
                             GarageContext garageContext,
                             PasswordEncoder passwordEncoder,
                             JwtService jwtService,
                             NotificationService notificationService,
                             @Value("${app.security.invitation.accept-url:http://localhost:5173/accept-invitation}") String acceptanceUrl) {
        this.invitationRepository = invitationRepository;
        this.appUserRepository = appUserRepository;
        this.garageContext = garageContext;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
        this.notificationService = notificationService;
        this.acceptanceUrl = acceptanceUrl;
    }

    @Transactional
    public InvitationResponse invite(Garage garage, String email, Role invitedRole) {
        UserPrincipal principal = garageContext.principal();
        Role role = invitedRole.effectiveRole();
        if (role == Role.SYSTEM_ADMIN || principal.getRoles().stream().noneMatch(actor -> actor.canInvite(role))) {
            throw new AccessDeniedException("You cannot invite this role");
        }
        if (!principal.getRoles().stream().map(Role::effectiveRole).anyMatch(actor -> actor == Role.SYSTEM_ADMIN)
                && !garage.getId().equals(principal.getGarageId())) {
            throw new AccessDeniedException("You can invite users only to your garage");
        }
        if (appUserRepository.existsByEmail(email)) {
            throw new BusinessException("An account already exists for this email");
        }
        LocalDateTime now = LocalDateTime.now();
        if (invitationRepository.existsByEmailIgnoreCaseAndGarageIdAndAcceptedAtIsNullAndExpiresAtAfter(
                email, garage.getId(), now)) {
            throw new BusinessException("An active invitation already exists for this email");
        }

        String token = createToken();
        UserInvitation invitation = new UserInvitation();
        invitation.setGarage(garage);
        invitation.setInvitedBy(appUserRepository.findById(principal.getId()).orElseThrow(() ->
                new AccessDeniedException("Inviting account no longer exists")));
        invitation.setEmail(email.trim().toLowerCase());
        invitation.setRole(role);
        invitation.setTokenHash(hashToken(token));
        invitation.setExpiresAt(now.plusHours(72));
        invitationRepository.save(invitation);

        String link = acceptanceUrl + (acceptanceUrl.contains("?") ? "&" : "?") + "token=" + token;
        notificationService.notifyEmail(invitation.getEmail(), "Garage invitation: " + garage.getName(),
                "You have been invited to " + garage.getName() + " as " + role.name()
                        + ". Accept your invitation within 72 hours: " + link);
        return new InvitationResponse(invitation.getEmail(), role.name(), link, invitation.getExpiresAt(),
            "Invitation created; email delivery depends on the configured notification publisher");
    }

    @Transactional
    public AuthResponse accept(AcceptInvitationRequest request) {
        String rawToken = request.token().trim();
        UserInvitation invitation = invitationRepository.findByTokenHash(hashToken(rawToken))
                .orElseThrow(() -> new ResourceNotFoundException("Invitation is invalid or has expired"));
        if (invitation.getAcceptedAt() != null || !invitation.getExpiresAt().isAfter(LocalDateTime.now())) {
            throw new BusinessException("Invitation is invalid, expired, or already accepted");
        }
        if (appUserRepository.existsByUsername(request.username())) {
            throw new BusinessException("Username already exists");
        }
        if (appUserRepository.existsByEmail(invitation.getEmail())) {
            throw new BusinessException("An account already exists for this email");
        }
        if (request.password().getBytes(StandardCharsets.UTF_8).length > 72) {
            throw new BusinessException("Password must be no more than 72 UTF-8 bytes");
        }

        Role role = invitation.getRole().effectiveRole();
        AppUser user = appUserRepository.save(new AppUser(
                request.username().trim(),
                invitation.getEmail(),
                passwordEncoder.encode(request.password()),
                Set.of(role),
                invitation.getGarage()
        ));
        invitation.setAcceptedAt(LocalDateTime.now());
        invitationRepository.save(invitation);

        UserPrincipal principal = UserPrincipal.from(user);
        String token = jwtService.generateToken(principal);
        Set<String> roles = principal.getRoles().stream()
                .map(Role::effectiveRole)
                .map(Enum::name)
                .collect(Collectors.toSet());
        return new AuthResponse(token, user.getUsername(), roles, "Invitation accepted",
                invitation.getGarage().getId(), invitation.getGarage().getName());
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