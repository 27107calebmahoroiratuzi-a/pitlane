package rw.ac.auca.garagerepairshopmanagementsystem.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record AcceptInvitationRequest(
        @NotBlank @Size(min = 40, max = 100) String token,
        @NotBlank @Size(min = 2, max = 120) String fullName,
        @Size(max = 30) @Pattern(regexp = "^$|^\\+?[0-9 ()-]{7,30}$", message = "Phone number is invalid") String phone,
        @NotBlank @Size(min = 3, max = 50)
        @Pattern(regexp = "^[A-Za-z0-9._-]+$", message = "Username may contain letters, numbers, dots, dashes and underscores")
        String username,
        @NotBlank @Size(min = 12, max = 72) String password
) {
}
