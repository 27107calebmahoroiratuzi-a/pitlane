package rw.ac.auca.garagerepairshopmanagementsystem.model;

import jakarta.persistence.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "repair_jobs")
public class RepairJob {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, updatable = false)
    private UUID uuid;

    @Column(nullable = false, length = 500)
    private String complaint;

    @Column(length = 1000)
    private String diagnosis;

    @Column(name = "repair_description", length = 1000)
    private String repairDescription;

    @Column(name = "date_received", nullable = false)
    private LocalDateTime dateReceived;

    @Column(name = "expected_completion_date")
    private LocalDateTime expectedCompletionDate;

    @Column(name = "actual_completion_date")
    private LocalDateTime actualCompletionDate;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal cost = BigDecimal.ZERO;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private RepairJobStatus status = RepairJobStatus.PENDING;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(
            name = "vehicle_id",
            nullable = false,
            foreignKey = @ForeignKey(name = "fk_repair_job_vehicle")
    )
    private Vehicle vehicle;

    @PrePersist
    public void prepareEntity() {

        if (uuid == null) {
            uuid = UUID.randomUUID();
        }

        if (dateReceived == null) {
            dateReceived = LocalDateTime.now();
        }

        if (cost == null) {
            cost = BigDecimal.ZERO;
        }

        if (status == null) {
            status = RepairJobStatus.PENDING;
        }
    }

    public RepairJob() {
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

    public Vehicle getVehicle() {
        return vehicle;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public void setUuid(UUID uuid) {
        this.uuid = uuid;
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

    public void setDateReceived(LocalDateTime dateReceived) {
        this.dateReceived = dateReceived;
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

    public void setVehicle(Vehicle vehicle) {
        this.vehicle = vehicle;
    }
}