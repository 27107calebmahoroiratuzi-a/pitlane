package rw.ac.auca.garagerepairshopmanagementsystem.dto;

import java.util.UUID;

public class CustomerResponse {

    private Long id;
    private UUID uuid;
    private String fullName;
    private String phone;
    private String email;
    private String address;

    public CustomerResponse() {
    }

    public CustomerResponse(
            Long id,
            UUID uuid,
            String fullName,
            String phone,
            String email,
            String address
    ) {
        this.id = id;
        this.uuid = uuid;
        this.fullName = fullName;
        this.phone = phone;
        this.email = email;
        this.address = address;
    }

    public Long getId() {
        return id;
    }

    public UUID getUuid() {
        return uuid;
    }

    public String getFullName() {
        return fullName;
    }

    public String getPhone() {
        return phone;
    }

    public String getEmail() {
        return email;
    }

    public String getAddress() {
        return address;
    }
}