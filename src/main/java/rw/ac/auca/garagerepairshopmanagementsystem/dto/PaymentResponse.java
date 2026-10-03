package rw.ac.auca.garagerepairshopmanagementsystem.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

public record PaymentResponse(Long id, UUID uuid, BigDecimal amount, String paymentMethod,
                              String reference, LocalDateTime paidAt) {
}