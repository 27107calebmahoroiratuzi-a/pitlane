package rw.ac.auca.garagerepairshopmanagementsystem.model;

import jakarta.persistence.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "payments", uniqueConstraints = @UniqueConstraint(
        name = "uk_payment_uuid", columnNames = "uuid"
))
public class Payment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, updatable = false)
    private UUID uuid;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "invoice_id", nullable = false)
    private Invoice invoice;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal amount;

    @Column(name = "payment_method", nullable = false, length = 40)
    private String paymentMethod;

    @Column(length = 100)
    private String reference;

    @Column(name = "paid_at", nullable = false)
    private LocalDateTime paidAt;

    @PrePersist
    public void preparePayment() {
        if (uuid == null) {
            uuid = UUID.randomUUID();
        }
        if (paidAt == null) {
            paidAt = LocalDateTime.now();
        }
    }

    public Payment() {
    }

    public Long getId() { return id; }
    public UUID getUuid() { return uuid; }
    public Invoice getInvoice() { return invoice; }
    public BigDecimal getAmount() { return amount; }
    public String getPaymentMethod() { return paymentMethod; }
    public String getReference() { return reference; }
    public LocalDateTime getPaidAt() { return paidAt; }
    public void setInvoice(Invoice invoice) { this.invoice = invoice; }
    public void setAmount(BigDecimal amount) { this.amount = amount; }
    public void setPaymentMethod(String paymentMethod) { this.paymentMethod = paymentMethod; }
    public void setReference(String reference) { this.reference = reference; }
}