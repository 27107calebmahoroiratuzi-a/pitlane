package rw.ac.auca.garagerepairshopmanagementsystem.model;

import jakarta.persistence.*;

import java.math.BigDecimal;

@Entity
@Table(name = "repair_job_parts", uniqueConstraints = @UniqueConstraint(
        name = "uk_repair_job_part", columnNames = {"repair_job_id", "spare_part_id"}
))
public class RepairJobPart {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "repair_job_id", nullable = false)
    private RepairJob repairJob;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "spare_part_id", nullable = false)
    private SparePart sparePart;

    @Column(nullable = false)
    private Integer quantity;

    @Column(name = "unit_price", nullable = false, precision = 12, scale = 2)
    private BigDecimal unitPrice;

    public RepairJobPart() {
    }

    public Long getId() { return id; }
    public RepairJob getRepairJob() { return repairJob; }
    public SparePart getSparePart() { return sparePart; }
    public Integer getQuantity() { return quantity; }
    public BigDecimal getUnitPrice() { return unitPrice; }
    public void setRepairJob(RepairJob repairJob) { this.repairJob = repairJob; }
    public void setSparePart(SparePart sparePart) { this.sparePart = sparePart; }
    public void setQuantity(Integer quantity) { this.quantity = quantity; }
    public void setUnitPrice(BigDecimal unitPrice) { this.unitPrice = unitPrice; }
}