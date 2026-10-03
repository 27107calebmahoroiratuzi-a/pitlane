package rw.ac.auca.garagerepairshopmanagementsystem.service;

import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
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
import rw.ac.auca.garagerepairshopmanagementsystem.security.GarageContext;

import java.time.Year;
import java.util.List;

@Service
public class VehicleService {

    private final VehicleRepository vehicleRepository;
    private final CustomerRepository customerRepository;
    private final RepairJobRepository repairJobRepository;
        private final GarageContext garageContext;

    public VehicleService(
            VehicleRepository vehicleRepository,
            CustomerRepository customerRepository,
            RepairJobRepository repairJobRepository,
            GarageContext garageContext
    ) {
        this.vehicleRepository = vehicleRepository;
        this.customerRepository = customerRepository;
        this.repairJobRepository = repairJobRepository;
        this.garageContext = garageContext;
    }

    @Transactional
    @CacheEvict(value = "vehicles", allEntries = true)
    public VehicleResponse create(VehicleRequest request) {
                Long garageId = garageContext.requireGarageId();

        validateYear(request.getYear());

        if (vehicleRepository.existsByPlateNumberAndGarageId(request.getPlateNumber(), garageId)) {
            throw new BusinessException(
                    "Vehicle plate number already exists"
            );
        }

        Customer customer = customerRepository.findByIdAndGarageId(request.getCustomerId(), garageId).orElseThrow(() ->
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
        vehicle.setGarage(customer.getGarage());

        return toResponse(vehicleRepository.save(vehicle));
    }

        @Cacheable(value = "vehicles", key = "@garageContext.requireGarageId()")
    public List<VehicleResponse> findAll() {

                return vehicleRepository.findAllByGarageId(garageContext.requireGarageId())
                .stream()
                .map(this::toResponse)
                .toList();
    }

        @Cacheable(value = "vehicles", key = "#id + ':' + @garageContext.requireGarageId()")
    public VehicleResponse findById(Long id) {

                Vehicle vehicle = vehicleRepository.findByIdAndGarageId(id, garageContext.requireGarageId())
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Vehicle not found with ID: " + id
                        ));

        return toResponse(vehicle);
    }

    @Transactional
    @CacheEvict(value = "vehicles", allEntries = true)
    public VehicleResponse update(
            Long id,
            VehicleRequest request
    ) {
                Long garageId = garageContext.requireGarageId();

        validateYear(request.getYear());

        Vehicle vehicle = vehicleRepository.findByIdAndGarageId(id, garageId)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Vehicle not found with ID: " + id
                        ));

        if (vehicleRepository.existsByPlateNumberAndIdNotAndGarageId(request.getPlateNumber(), id, garageId)) {
            throw new BusinessException(
                    "Vehicle plate number already exists"
            );
        }

        Customer customer = customerRepository.findByIdAndGarageId(request.getCustomerId(), garageId).orElseThrow(() ->
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
        vehicle.setGarage(customer.getGarage());

        return toResponse(vehicleRepository.save(vehicle));
    }

    @Transactional
    @CacheEvict(value = "vehicles", allEntries = true)
    public void delete(Long id) {
                Long garageId = garageContext.requireGarageId();

                Vehicle vehicle = vehicleRepository.findByIdAndGarageId(id, garageId)
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