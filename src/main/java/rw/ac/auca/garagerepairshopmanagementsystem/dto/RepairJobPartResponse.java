package rw.ac.auca.garagerepairshopmanagementsystem.dto;

import java.math.BigDecimal;

public record RepairJobPartResponse(Long id, Long sparePartId, String sku, String partName,
                                    Integer quantity, BigDecimal unitPrice, BigDecimal lineTotal) {
}