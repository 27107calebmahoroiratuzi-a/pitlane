package rw.ac.auca.garagerepairshopmanagementsystem.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import rw.ac.auca.garagerepairshopmanagementsystem.model.RepairJobAssignment;

import java.util.List;
import java.util.Optional;

public interface RepairJobAssignmentRepository extends JpaRepository<RepairJobAssignment, Long> {
    List<RepairJobAssignment> findByRepairJobId(Long repairJobId);
    boolean existsByMechanicId(Long mechanicId);
    Optional<RepairJobAssignment> findByRepairJobIdAndMechanicId(Long repairJobId, Long mechanicId);
}