package rw.ac.auca.garagerepairshopmanagementsystem.model;

import jakarta.persistence.*;

import java.util.UUID;

@Entity
@Table(name = "mechanics", uniqueConstraints = {
        @UniqueConstraint(name = "uk_mechanic_uuid", columnNames = "uuid"),
        @UniqueConstraint(name = "uk_mechanic_email", columnNames = "email"),
        @UniqueConstraint(name = "uk_mechanic_phone", columnNames = "phone")
})
public class Mechanic {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, updatable = false)
    private UUID uuid;

    @Column(name = "full_name", nullable = false, length = 100)
    private String fullName;

    @Column(nullable = false, unique = true, length = 30)
    private String phone;

    @Column(nullable = false, unique = true, length = 150)
    private String email;

    @Column(length = 100)
    private String specialization;

    @Column(nullable = false)
    private boolean active = true;

    @PrePersist
    public void generateUuid() {
        if (uuid == null) {
            uuid = UUID.randomUUID();
        }
    }

    public Mechanic() {
    }

    public Long getId() { return id; }
    public UUID getUuid() { return uuid; }
    public String getFullName() { return fullName; }
    public String getPhone() { return phone; }
    public String getEmail() { return email; }
    public String getSpecialization() { return specialization; }
    public boolean isActive() { return active; }
    public void setFullName(String fullName) { this.fullName = fullName; }
    public void setPhone(String phone) { this.phone = phone; }
    public void setEmail(String email) { this.email = email; }
    public void setSpecialization(String specialization) { this.specialization = specialization; }
    public void setActive(boolean active) { this.active = active; }
}