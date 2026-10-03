package rw.ac.auca.garagerepairshopmanagementsystem.model;

import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "repair_job_mechanics", uniqueConstraints = @UniqueConstraint(
        name = "uk_repair_job_mechanic", columnNames = {"repair_job_id", "mechanic_id"}
))
public class RepairJobAssignment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "repair_job_id", nullable = false)
    private RepairJob repairJob;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "mechanic_id", nullable = false)
    private Mechanic mechanic;

    @Column(name = "assigned_at", nullable = false)
    private LocalDateTime assignedAt;

    @PrePersist
    public void setAssignmentTime() {
        if (assignedAt == null) {
            assignedAt = LocalDateTime.now();
        }
    }

    public RepairJobAssignment() {
    }

    public Long getId() { return id; }
    public RepairJob getRepairJob() { return repairJob; }
    public Mechanic getMechanic() { return mechanic; }
    public LocalDateTime getAssignedAt() { return assignedAt; }
    public void setRepairJob(RepairJob repairJob) { this.repairJob = repairJob; }
    public void setMechanic(Mechanic mechanic) { this.mechanic = mechanic; }
}