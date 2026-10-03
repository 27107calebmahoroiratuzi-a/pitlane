package rw.ac.auca.garagerepairshopmanagementsystem.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CreateGarageRequest(
        @NotBlank @Size(max = 120) String name,
        @NotBlank @Email @Size(max = 150) String adminEmail
) {
}