package rw.ac.auca.garagerepairshopmanagementsystem.dto;

import rw.ac.auca.garagerepairshopmanagementsystem.model.VehicleStatus;

import java.util.UUID;

public class VehicleResponse {

    private Long id;
    private UUID uuid;
    private String plateNumber;
    private String make;
    private String model;
    private Integer year;
    private String color;
    private VehicleStatus status;

    private Long customerId;
    private String customerName;

    public VehicleResponse() {
    }

    public VehicleResponse(
            Long id,
            UUID uuid,
            String plateNumber,
            String make,
            String model,
            Integer year,
            String color,
            VehicleStatus status,
            Long customerId,
            String customerName
    ) {
        this.id = id;
        this.uuid = uuid;
        this.plateNumber = plateNumber;
        this.make = make;
        this.model = model;
        this.year = year;
        this.color = color;
        this.status = status;
        this.customerId = customerId;
        this.customerName = customerName;
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

    public Long getCustomerId() {
        return customerId;
    }

    public String getCustomerName() {
        return customerName;
    }
}