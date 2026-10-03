package rw.ac.auca.garagerepairshopmanagementsystem.model;

import jakarta.persistence.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "invoices", uniqueConstraints = {
        @UniqueConstraint(name = "uk_invoice_uuid", columnNames = "uuid"),
        @UniqueConstraint(name = "uk_invoice_number", columnNames = "invoice_number"),
        @UniqueConstraint(name = "uk_invoice_repair_job", columnNames = "repair_job_id")
})
public class Invoice {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, updatable = false)
    private UUID uuid;

    @Column(name = "invoice_number", nullable = false, unique = true, length = 40)
    private String invoiceNumber;

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "repair_job_id", nullable = false, unique = true)
    private RepairJob repairJob;

    @Column(name = "labor_amount", nullable = false, precision = 12, scale = 2)
    private BigDecimal laborAmount;

    @Column(name = "parts_amount", nullable = false, precision = 12, scale = 2)
    private BigDecimal partsAmount;

    @Column(name = "total_amount", nullable = false, precision = 12, scale = 2)
    private BigDecimal totalAmount;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private InvoiceStatus status = InvoiceStatus.ISSUED;

    @Column(name = "issued_at", nullable = false)
    private LocalDateTime issuedAt;

    @Column(name = "due_at")
    private LocalDateTime dueAt;

    @PrePersist
    public void prepareInvoice() {
        if (uuid == null) {
            uuid = UUID.randomUUID();
        }
        if (invoiceNumber == null) {
            invoiceNumber = "INV-" + uuid.toString().substring(0, 8).toUpperCase();
        }
        if (issuedAt == null) {
            issuedAt = LocalDateTime.now();
        }
        if (status == null) {
            status = InvoiceStatus.ISSUED;
        }
    }

    public Invoice() {
    }

    public Long getId() { return id; }
    public UUID getUuid() { return uuid; }
    public String getInvoiceNumber() { return invoiceNumber; }
    public RepairJob getRepairJob() { return repairJob; }
    public BigDecimal getLaborAmount() { return laborAmount; }
    public BigDecimal getPartsAmount() { return partsAmount; }
    public BigDecimal getTotalAmount() { return totalAmount; }
    public InvoiceStatus getStatus() { return status; }
    public LocalDateTime getIssuedAt() { return issuedAt; }
    public LocalDateTime getDueAt() { return dueAt; }
    public void setRepairJob(RepairJob repairJob) { this.repairJob = repairJob; }
    public void setLaborAmount(BigDecimal laborAmount) { this.laborAmount = laborAmount; }
    public void setPartsAmount(BigDecimal partsAmount) { this.partsAmount = partsAmount; }
    public void setTotalAmount(BigDecimal totalAmount) { this.totalAmount = totalAmount; }
    public void setStatus(InvoiceStatus status) { this.status = status; }
    public void setDueAt(LocalDateTime dueAt) { this.dueAt = dueAt; }
}