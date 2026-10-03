package rw.ac.auca.garagerepairshopmanagementsystem.security;

import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Set;
import java.util.stream.Collectors;

@Service
public class AuthenticationService {

    private final AppUserRepository appUserRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;

    public AuthenticationService(AppUserRepository appUserRepository,
                                 PasswordEncoder passwordEncoder,
                                 AuthenticationManager authenticationManager,
                                 JwtService jwtService) {
        this.appUserRepository = appUserRepository;
        this.passwordEncoder = passwordEncoder;
        this.authenticationManager = authenticationManager;
        this.jwtService = jwtService;
    }

    public AuthResponse register(RegisterRequest request) {
        if (appUserRepository.existsByUsername(request.username())) {
            throw new IllegalArgumentException("Username already exists");
        }
        if (appUserRepository.existsByEmail(request.email())) {
            throw new IllegalArgumentException("Email already exists");
        }

        AppUser appUser = new AppUser(
                request.username(),
                request.email(),
                passwordEncoder.encode(request.password()),
            Set.of(Role.USER)
        );

        appUserRepository.save(appUser);
        return buildResponse(appUser, "User registered successfully");
    }

    public AuthResponse login(AuthRequest request) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.username(), request.password())
        );

        UserPrincipal principal = (UserPrincipal) authentication.getPrincipal();
        return buildResponse(principal, "Login successful");
    }

    private AuthResponse buildResponse(AppUser user, String message) {
        String token = jwtService.generateToken(UserPrincipal.from(user));
        Set<String> roles = user.getRoles().stream()
                .map(Enum::name)
                .collect(Collectors.toSet());

        return new AuthResponse(token, user.getUsername(), roles, message);
    }

    private AuthResponse buildResponse(UserPrincipal principal, String message) {
        String token = jwtService.generateToken(principal);
        Set<String> roles = principal.getRoles().stream()
                .map(Enum::name)
                .collect(Collectors.toSet());

        return new AuthResponse(token, principal.getUsername(), roles, message);
    }
}
