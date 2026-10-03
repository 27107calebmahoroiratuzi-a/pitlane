package rw.ac.auca.garagerepairshopmanagementsystem.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import rw.ac.auca.garagerepairshopmanagementsystem.model.RepairJob;
import rw.ac.auca.garagerepairshopmanagementsystem.model.RepairJobStatus;

import java.util.List;
import java.util.Optional;

public interface RepairJobRepository extends JpaRepository<RepairJob, Long> {

    List<RepairJob> findAllByVehicleGarageId(Long garageId);

    Optional<RepairJob> findByIdAndVehicleGarageId(Long id, Long garageId);

    boolean existsByVehicleIdAndStatusIn(
            Long vehicleId,
            Iterable<RepairJobStatus> statuses
    );

    boolean existsByVehicleId(Long vehicleId);
}