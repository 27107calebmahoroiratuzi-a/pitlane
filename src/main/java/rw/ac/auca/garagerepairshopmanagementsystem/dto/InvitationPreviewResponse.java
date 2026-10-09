package rw.ac.auca.garagerepairshopmanagementsystem.dto;

import java.time.LocalDateTime;

public record InvitationPreviewResponse(
        String email,
        String role,
        String garageName,
        String invitedBy,
        LocalDateTime expiresAt
) {
}
