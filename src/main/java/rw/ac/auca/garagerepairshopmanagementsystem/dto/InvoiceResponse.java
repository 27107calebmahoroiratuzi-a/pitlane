package rw.ac.auca.garagerepairshopmanagementsystem.dto;

import rw.ac.auca.garagerepairshopmanagementsystem.model.InvoiceStatus;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public record InvoiceResponse(Long id, UUID uuid, String invoiceNumber, Long repairJobId,
                              String plateNumber, String customerName, String customerEmail,
                              BigDecimal laborAmount, BigDecimal partsAmount,
                              BigDecimal totalAmount, BigDecimal amountPaid,
                              BigDecimal balanceDue, InvoiceStatus status,
                              LocalDateTime issuedAt, LocalDateTime dueAt,
                              List<PaymentResponse> payments) {
}