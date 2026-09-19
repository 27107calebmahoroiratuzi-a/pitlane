package rw.ac.auca.garagerepairshopmanagementsystem.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import rw.ac.auca.garagerepairshopmanagementsystem.model.RepairJob;
import rw.ac.auca.garagerepairshopmanagementsystem.model.RepairJobStatus;

public interface RepairJobRepository extends JpaRepository<RepairJob, Long> {

    boolean existsByVehicleIdAndStatusIn(
            Long vehicleId,
            Iterable<RepairJobStatus> statuses
    );

    boolean existsByVehicleId(Long vehicleId);
}