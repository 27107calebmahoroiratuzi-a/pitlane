package rw.ac.auca.garagerepairshopmanagementsystem.dto;

import java.time.LocalDateTime;

public record RepairJobAssignmentResponse(Long mechanicId, String mechanicName,
                                          String specialization, LocalDateTime assignedAt) {
}