package rw.ac.auca.garagerepairshopmanagementsystem.dto;

import java.util.UUID;

public record MechanicResponse(Long id, UUID uuid, String fullName, String phone,
                               String email, String specialization, boolean active) {
}