package rw.ac.auca.garagerepairshopmanagementsystem.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import rw.ac.auca.garagerepairshopmanagementsystem.model.RepairJobStatus;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class RepairJobRequest {

    @NotBlank(message = "Complaint is required")
    @Size(min = 5, max = 500, message = "Complaint must be between 5 and 500 characters")
    private String complaint;

    @Size(max = 1000)
    private String diagnosis;

    @Size(max = 1000)
    private String repairDescription;

    private LocalDateTime expectedCompletionDate;

    private LocalDateTime actualCompletionDate;

    @NotNull(message = "Cost is required")
    @DecimalMin(value = "0.0", message = "Cost cannot be negative")
    private BigDecimal cost;

    private RepairJobStatus status;

    @NotNull(message = "Vehicle ID is required")
    private Long vehicleId;

    public String getComplaint() {
        return complaint;
    }

    public String getDiagnosis() {
        return diagnosis;
    }

    public String getRepairDescription() {
        return repairDescription;
    }

    public LocalDateTime getExpectedCompletionDate() {
        return expectedCompletionDate;
    }

    public LocalDateTime getActualCompletionDate() {
        return actualCompletionDate;
    }

    public BigDecimal getCost() {
        return cost;
    }

    public RepairJobStatus getStatus() {
        return status;
    }

    public Long getVehicleId() {
        return vehicleId;
    }

    public void setComplaint(String complaint) {
        this.complaint = complaint;
    }

    public void setDiagnosis(String diagnosis) {
        this.diagnosis = diagnosis;
    }

    public void setRepairDescription(String repairDescription) {
        this.repairDescription = repairDescription;
    }

    public void setExpectedCompletionDate(LocalDateTime expectedCompletionDate) {
        this.expectedCompletionDate = expectedCompletionDate;
    }

    public void setActualCompletionDate(LocalDateTime actualCompletionDate) {
        this.actualCompletionDate = actualCompletionDate;
    }

    public void setCost(BigDecimal cost) {
        this.cost = cost;
    }

    public void setStatus(RepairJobStatus status) {
        this.status = status;
    }

    public void setVehicleId(Long vehicleId) {
        this.vehicleId = vehicleId;
    }
}