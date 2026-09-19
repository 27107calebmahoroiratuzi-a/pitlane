package rw.ac.auca.garagerepairshopmanagementsystem.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import rw.ac.auca.garagerepairshopmanagementsystem.model.Vehicle;

public interface VehicleRepository extends JpaRepository<Vehicle, Long> {

    boolean existsByPlateNumber(String plateNumber);

    boolean existsByPlateNumberAndIdNot(String plateNumber, Long id);

    boolean existsByCustomerId(Long customerId);
}