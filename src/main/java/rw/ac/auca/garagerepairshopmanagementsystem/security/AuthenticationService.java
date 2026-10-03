package rw.ac.auca.garagerepairshopmanagementsystem.security;

import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

import java.util.Set;
import java.util.stream.Collectors;

@Service
public class AuthenticationService {

    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;

    public AuthenticationService(AuthenticationManager authenticationManager,
                                 JwtService jwtService) {
        this.authenticationManager = authenticationManager;
        this.jwtService = jwtService;
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
            .map(Role::effectiveRole)
            .map(Enum::name)
                .collect(Collectors.toSet());

        return new AuthResponse(token, user.getUsername(), roles, message,
            user.getGarage() == null ? null : user.getGarage().getId(),
            user.getGarage() == null ? null : user.getGarage().getName());
    }

    private AuthResponse buildResponse(UserPrincipal principal, String message) {
        String token = jwtService.generateToken(principal);
        Set<String> roles = principal.getRoles().stream()
            .map(Role::effectiveRole)
            .map(Enum::name)
                .collect(Collectors.toSet());

        return new AuthResponse(token, principal.getUsername(), roles, message,
            principal.getGarageId(), principal.getGarageName());
    }
}
