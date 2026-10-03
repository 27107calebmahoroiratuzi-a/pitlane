package rw.ac.auca.garagerepairshopmanagementsystem.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public class RepairJobPartRequest {
    @NotNull
    private Long sparePartId;

    @NotNull
    @Positive
    private Integer quantity;

    public Long getSparePartId() { return sparePartId; }
    public Integer getQuantity() { return quantity; }
    public void setSparePartId(Long sparePartId) { this.sparePartId = sparePartId; }
    public void setQuantity(Integer quantity) { this.quantity = quantity; }
}