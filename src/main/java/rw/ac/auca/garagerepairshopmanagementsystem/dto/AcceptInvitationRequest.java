package rw.ac.auca.garagerepairshopmanagementsystem.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record AcceptInvitationRequest(
        @NotBlank @Size(min = 40, max = 100) String token,
        @NotBlank @Size(min = 3, max = 50) String username,
        @NotBlank @Size(min = 12, max = 72) String password
) {
}