package rw.ac.auca.garagerepairshopmanagementsystem.model;

import jakarta.persistence.*;

import java.math.BigDecimal;
import java.util.UUID;

@Entity
@Table(name = "spare_parts", uniqueConstraints = {
        @UniqueConstraint(name = "uk_spare_part_uuid", columnNames = "uuid"),
    @UniqueConstraint(name = "uk_spare_part_garage_sku", columnNames = {"garage_id", "sku"})
})
public class SparePart {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, updatable = false)
    private UUID uuid;

    @Column(nullable = false, length = 50)
    private String sku;

    @Column(nullable = false, length = 150)
    private String name;

    @Column(length = 500)
    private String description;

    @Column(name = "unit_price", nullable = false, precision = 12, scale = 2)
    private BigDecimal unitPrice;

    @Column(name = "stock_quantity", nullable = false)
    private Integer stockQuantity;

    @Column(name = "reorder_level", nullable = false)
    private Integer reorderLevel = 0;

    @ManyToOne
    @JoinColumn(name = "garage_id", foreignKey = @ForeignKey(name = "fk_spare_part_garage"))
    private Garage garage;

    @PrePersist
    public void generateUuid() {
        if (uuid == null) {
            uuid = UUID.randomUUID();
        }
        if (reorderLevel == null) {
            reorderLevel = 0;
        }
    }

    public SparePart() {
    }

    public Long getId() { return id; }
    public UUID getUuid() { return uuid; }
    public String getSku() { return sku; }
    public String getName() { return name; }
    public String getDescription() { return description; }
    public BigDecimal getUnitPrice() { return unitPrice; }
    public Integer getStockQuantity() { return stockQuantity; }
    public Integer getReorderLevel() { return reorderLevel; }
    public Garage getGarage() { return garage; }
    public void setSku(String sku) { this.sku = sku; }
    public void setName(String name) { this.name = name; }
    public void setDescription(String description) { this.description = description; }
    public void setUnitPrice(BigDecimal unitPrice) { this.unitPrice = unitPrice; }
    public void setStockQuantity(Integer stockQuantity) { this.stockQuantity = stockQuantity; }
    public void setReorderLevel(Integer reorderLevel) { this.reorderLevel = reorderLevel; }
    public void setGarage(Garage garage) { this.garage = garage; }
}