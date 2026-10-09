package rw.ac.auca.garagerepairshopmanagementsystem.security;

import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.bind.annotation.*;
import rw.ac.auca.garagerepairshopmanagementsystem.dto.AcceptInvitationRequest;
import rw.ac.auca.garagerepairshopmanagementsystem.dto.InvitationPreviewRequest;
import rw.ac.auca.garagerepairshopmanagementsystem.dto.InvitationPreviewResponse;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthenticationService authenticationService;
    private final InvitationService invitationService;

    public AuthController(AuthenticationService authenticationService, InvitationService invitationService) {
        this.authenticationService = authenticationService;
        this.invitationService = invitationService;
    }

    @PostMapping("/register")
    public ResponseEntity<Void> register() {
        throw new ResponseStatusException(HttpStatus.GONE, "Registration requires a garage invitation");
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody AuthRequest request) {
        return ResponseEntity.ok(authenticationService.login(request));
    }

    /** Token travels in the body rather than the URL so it does not end up in access logs. */
    @PostMapping("/invitations/preview")
    public ResponseEntity<InvitationPreviewResponse> previewInvitation(@Valid @RequestBody InvitationPreviewRequest request) {
        return ResponseEntity.ok(invitationService.preview(request.token()));
    }

    @PostMapping("/invitations/accept")
    public ResponseEntity<AuthResponse> acceptInvitation(@Valid @RequestBody AcceptInvitationRequest request) {
        return ResponseEntity.ok(invitationService.accept(request));
    }
}
