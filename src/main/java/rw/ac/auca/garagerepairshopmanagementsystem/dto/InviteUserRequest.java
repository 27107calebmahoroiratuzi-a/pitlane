package rw.ac.auca.garagerepairshopmanagementsystem.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import rw.ac.auca.garagerepairshopmanagementsystem.security.Role;

public record InviteUserRequest(
        @NotBlank @Email @Size(max = 150) String email,
        @NotNull Role role
) {
}