package rw.ac.auca.garagerepairshopmanagementsystem.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import rw.ac.auca.garagerepairshopmanagementsystem.dto.VehicleRequest;
import rw.ac.auca.garagerepairshopmanagementsystem.dto.VehicleResponse;
import rw.ac.auca.garagerepairshopmanagementsystem.exception.BusinessException;
import rw.ac.auca.garagerepairshopmanagementsystem.exception.ResourceNotFoundException;
import rw.ac.auca.garagerepairshopmanagementsystem.model.Customer;
import rw.ac.auca.garagerepairshopmanagementsystem.model.Vehicle;
import rw.ac.auca.garagerepairshopmanagementsystem.repository.CustomerRepository;
import rw.ac.auca.garagerepairshopmanagementsystem.repository.RepairJobRepository;
import rw.ac.auca.garagerepairshopmanagementsystem.repository.VehicleRepository;

import java.time.Year;
import java.util.List;

@Service
public class VehicleService {

    private final VehicleRepository vehicleRepository;
    private final CustomerRepository customerRepository;
    private final RepairJobRepository repairJobRepository;

    public VehicleService(
            VehicleRepository vehicleRepository,
            CustomerRepository customerRepository,
            RepairJobRepository repairJobRepository
    ) {
        this.vehicleRepository = vehicleRepository;
        this.customerRepository = customerRepository;
        this.repairJobRepository = repairJobRepository;
    }

    @Transactional
    public VehicleResponse create(VehicleRequest request) {

        validateYear(request.getYear());

        if (vehicleRepository.existsByPlateNumber(
                request.getPlateNumber())) {
            throw new BusinessException(
                    "Vehicle plate number already exists"
            );
        }

        Customer customer = customerRepository.findById(
                request.getCustomerId()
        ).orElseThrow(() ->
                new ResourceNotFoundException(
                        "Customer not found with ID: "
                                + request.getCustomerId()
                ));

        Vehicle vehicle = new Vehicle();

        vehicle.setPlateNumber(request.getPlateNumber());
        vehicle.setMake(request.getMake());
        vehicle.setModel(request.getModel());
        vehicle.setYear(request.getYear());
        vehicle.setColor(request.getColor());
        vehicle.setCustomer(customer);

        return toResponse(vehicleRepository.save(vehicle));
    }

    public List<VehicleResponse> findAll() {

        return vehicleRepository.findAll()
                .stream()
                .map(this::toResponse)
                .toList();
    }

    public VehicleResponse findById(Long id) {

        Vehicle vehicle = vehicleRepository.findById(id)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Vehicle not found with ID: " + id
                        ));

        return toResponse(vehicle);
    }

    @Transactional
    public VehicleResponse update(
            Long id,
            VehicleRequest request
    ) {

        validateYear(request.getYear());

        Vehicle vehicle = vehicleRepository.findById(id)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Vehicle not found with ID: " + id
                        ));

        if (vehicleRepository.existsByPlateNumberAndIdNot(
                request.getPlateNumber(), id)) {
            throw new BusinessException(
                    "Vehicle plate number already exists"
            );
        }

        Customer customer = customerRepository.findById(
                request.getCustomerId()
        ).orElseThrow(() ->
                new ResourceNotFoundException(
                        "Customer not found with ID: "
                                + request.getCustomerId()
                ));

        vehicle.setPlateNumber(request.getPlateNumber());
        vehicle.setMake(request.getMake());
        vehicle.setModel(request.getModel());
        vehicle.setYear(request.getYear());
        vehicle.setColor(request.getColor());
        vehicle.setCustomer(customer);

        return toResponse(vehicleRepository.save(vehicle));
    }

    @Transactional
    public void delete(Long id) {

        Vehicle vehicle = vehicleRepository.findById(id)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Vehicle not found with ID: " + id
                        ));

        if (repairJobRepository.existsByVehicleId(id)) {
            throw new BusinessException(
                    "Vehicle cannot be deleted because it has repair jobs"
            );
        }

        vehicleRepository.delete(vehicle);
    }

    private void validateYear(Integer year) {

        int currentYear = Year.now().getValue();

        if (year > currentYear) {
            throw new BusinessException(
                    "Vehicle year cannot be in the future"
            );
        }
    }

    private VehicleResponse toResponse(Vehicle vehicle) {

        return new VehicleResponse(
                vehicle.getId(),
                vehicle.getUuid(),
                vehicle.getPlateNumber(),
                vehicle.getMake(),
                vehicle.getModel(),
                vehicle.getYear(),
                vehicle.getColor(),
                vehicle.getStatus(),
                vehicle.getCustomer().getId(),
                vehicle.getCustomer().getFullName()
        );
    }
}