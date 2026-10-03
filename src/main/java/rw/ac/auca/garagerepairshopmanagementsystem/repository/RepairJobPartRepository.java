package rw.ac.auca.garagerepairshopmanagementsystem.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import rw.ac.auca.garagerepairshopmanagementsystem.model.RepairJobPart;

import java.util.List;
import java.util.Optional;

public interface RepairJobPartRepository extends JpaRepository<RepairJobPart, Long> {
    List<RepairJobPart> findByRepairJobId(Long repairJobId);
    Optional<RepairJobPart> findByRepairJobIdAndSparePartId(Long repairJobId, Long sparePartId);
    boolean existsBySparePartId(Long sparePartId);
}