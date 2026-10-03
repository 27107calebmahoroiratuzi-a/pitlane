package rw.ac.auca.garagerepairshopmanagementsystem.dto;

import java.math.BigDecimal;
import java.util.UUID;

public record SparePartResponse(Long id, UUID uuid, String sku, String name, String description,
                                BigDecimal unitPrice, Integer stockQuantity,
                                Integer reorderLevel, boolean lowStock) {
}