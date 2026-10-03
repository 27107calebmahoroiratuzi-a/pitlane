package rw.ac.auca.garagerepairshopmanagementsystem.dto;

import java.time.LocalDateTime;

public record InvitationResponse(
        String email,
        String role,
        String acceptanceUrl,
        LocalDateTime expiresAt,
        String message
) {
}