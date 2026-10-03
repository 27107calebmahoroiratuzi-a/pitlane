package rw.ac.auca.garagerepairshopmanagementsystem.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import rw.ac.auca.garagerepairshopmanagementsystem.model.SparePart;

import jakarta.persistence.LockModeType;
import java.util.Optional;

public interface SparePartRepository extends JpaRepository<SparePart, Long> {
    boolean existsBySku(String sku);
    boolean existsBySkuAndIdNot(String sku, Long id);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select part from SparePart part where part.id = :id")
    Optional<SparePart> findByIdForUpdate(@Param("id") Long id);
}