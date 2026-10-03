package rw.ac.auca.garagerepairshopmanagementsystem.dto;

import java.time.LocalDateTime;

public record GarageResponse(Long id, String name, LocalDateTime createdAt) {
}