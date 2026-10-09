package rw.ac.auca.garagerepairshopmanagementsystem.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record InvitationPreviewRequest(@NotBlank @Size(min = 40, max = 100) String token) {
}
