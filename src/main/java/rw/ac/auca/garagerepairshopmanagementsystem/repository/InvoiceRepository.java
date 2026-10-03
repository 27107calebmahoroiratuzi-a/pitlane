package rw.ac.auca.garagerepairshopmanagementsystem.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import rw.ac.auca.garagerepairshopmanagementsystem.model.Invoice;

import jakarta.persistence.LockModeType;
import java.util.List;
import java.util.Optional;

public interface InvoiceRepository extends JpaRepository<Invoice, Long> {
    List<Invoice> findAllByRepairJobVehicleGarageId(Long garageId);
    Optional<Invoice> findByIdAndRepairJobVehicleGarageId(Long id, Long garageId);
    Optional<Invoice> findByRepairJobIdAndRepairJobVehicleGarageId(Long repairJobId, Long garageId);
    boolean existsByRepairJobId(Long repairJobId);
    boolean existsByRepairJobIdAndRepairJobVehicleGarageId(Long repairJobId, Long garageId);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select invoice from Invoice invoice where invoice.id = :id and invoice.repairJob.vehicle.garage.id = :garageId")
    Optional<Invoice> findByIdForUpdateAndGarageId(@Param("id") Long id, @Param("garageId") Long garageId);
}