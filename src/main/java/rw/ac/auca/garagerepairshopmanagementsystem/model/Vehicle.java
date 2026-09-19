package rw.ac.auca.garagerepairshopmanagementsystem.model;

import jakarta.persistence.*;

import java.util.UUID;

@Entity
@Table(
        name = "vehicles",
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_vehicle_uuid", columnNames = "uuid"),
                @UniqueConstraint(name = "uk_vehicle_plate", columnNames = "plate_number")
        }
)
public class Vehicle {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, updatable = false)
    private UUID uuid;

    @Column(name = "plate_number", nullable = false, unique = true, length = 30)
    private String plateNumber;

    @Column(nullable = false, length = 50)
    private String make;

    @Column(nullable = false, length = 50)
    private String model;

    @Column(nullable = false)
    private Integer year;

    @Column(length = 50)
    private String color;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private VehicleStatus status = VehicleStatus.AVAILABLE;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(
            name = "customer_id",
            nullable = false,
            foreignKey = @ForeignKey(name = "fk_vehicle_customer")
    )
    private Customer customer;

    @PrePersist
    public void generateUuid() {
        if (uuid == null) {
            uuid = UUID.randomUUID();
        }
    }

    public Vehicle() {
    }

    public Long getId() {
        return id;
    }

    public UUID getUuid() {
        return uuid;
    }

    public String getPlateNumber() {
        return plateNumber;
    }

    public String getMake() {
        return make;
    }

    public String getModel() {
        return model;
    }

    public Integer getYear() {
        return year;
    }

    public String getColor() {
        return color;
    }

    public VehicleStatus getStatus() {
        return status;
    }

    public Customer getCustomer() {
        return customer;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public void setUuid(UUID uuid) {
        this.uuid = uuid;
    }

    public void setPlateNumber(String plateNumber) {
        this.plateNumber = plateNumber;
    }

    public void setMake(String make) {
        this.make = make;
    }

    public void setModel(String model) {
        this.model = model;
    }

    public void setYear(Integer year) {
        this.year = year;
    }

    public void setColor(String color) {
        this.color = color;
    }

    public void setStatus(VehicleStatus status) {
        this.status = status;
    }

    public void setCustomer(Customer customer) {
        this.customer = customer;
    }
}