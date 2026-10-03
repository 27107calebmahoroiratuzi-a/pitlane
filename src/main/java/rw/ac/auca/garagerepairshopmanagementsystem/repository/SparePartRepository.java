package rw.ac.auca.garagerepairshopmanagementsystem.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import rw.ac.auca.garagerepairshopmanagementsystem.model.SparePart;

import jakarta.persistence.LockModeType;
import java.util.List;
import java.util.Optional;

public interface SparePartRepository extends JpaRepository<SparePart, Long> {
    List<SparePart> findAllByGarageId(Long garageId);
    Optional<SparePart> findByIdAndGarageId(Long id, Long garageId);
    boolean existsBySkuAndGarageId(String sku, Long garageId);
    boolean existsBySkuAndIdNotAndGarageId(String sku, Long id, Long garageId);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select part from SparePart part where part.id = :id and part.garage.id = :garageId")
    Optional<SparePart> findByIdForUpdateAndGarageId(@Param("id") Long id, @Param("garageId") Long garageId);
}