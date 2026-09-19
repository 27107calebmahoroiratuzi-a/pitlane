package rw.ac.auca.garagerepairshopmanagementsystem.dto;

import rw.ac.auca.garagerepairshopmanagementsystem.model.RepairJobStatus;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

public class RepairJobResponse {

    private Long id;
    private UUID uuid;
    private String complaint;
    private String diagnosis;
    private String repairDescription;
    private LocalDateTime dateReceived;
    private LocalDateTime expectedCompletionDate;
    private LocalDateTime actualCompletionDate;
    private BigDecimal cost;
    private RepairJobStatus status;

    private Long vehicleId;
    private String plateNumber;

    public RepairJobResponse() {
    }

    public RepairJobResponse(
            Long id,
            UUID uuid,
            String complaint,
            String diagnosis,
            String repairDescription,
            LocalDateTime dateReceived,
            LocalDateTime expectedCompletionDate,
            LocalDateTime actualCompletionDate,
            BigDecimal cost,
            RepairJobStatus status,
            Long vehicleId,
            String plateNumber
    ) {
        this.id = id;
        this.uuid = uuid;
        this.complaint = complaint;
        this.diagnosis = diagnosis;
        this.repairDescription = repairDescription;
        this.dateReceived = dateReceived;
        this.expectedCompletionDate = expectedCompletionDate;
        this.actualCompletionDate = actualCompletionDate;
        this.cost = cost;
        this.status = status;
        this.vehicleId = vehicleId;
        this.plateNumber = plateNumber;
    }

    public Long getId() {
        return id;
    }

    public UUID getUuid() {
        return uuid;
    }

    public String getComplaint() {
        return complaint;
    }

    public String getDiagnosis() {
        return diagnosis;
    }

    public String getRepairDescription() {
        return repairDescription;
    }

    public LocalDateTime getDateReceived() {
        return dateReceived;
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

    public String getPlateNumber() {
        return plateNumber;
    }
}