package rw.ac.auca.garagerepairshopmanagementsystem.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import rw.ac.auca.garagerepairshopmanagementsystem.model.Vehicle;

import java.util.List;
import java.util.Optional;

public interface VehicleRepository extends JpaRepository<Vehicle, Long> {

    List<Vehicle> findAllByGarageId(Long garageId);

    Optional<Vehicle> findByIdAndGarageId(Long id, Long garageId);

    boolean existsByPlateNumberAndGarageId(String plateNumber, Long garageId);

    boolean existsByPlateNumberAndIdNotAndGarageId(String plateNumber, Long id, Long garageId);

    boolean existsByCustomerId(Long customerId);
}