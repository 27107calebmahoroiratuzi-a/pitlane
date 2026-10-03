package rw.ac.auca.garagerepairshopmanagementsystem.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import rw.ac.auca.garagerepairshopmanagementsystem.model.Mechanic;

import java.util.List;
import java.util.Optional;

public interface MechanicRepository extends JpaRepository<Mechanic, Long> {
    List<Mechanic> findAllByGarageId(Long garageId);
    Optional<Mechanic> findByIdAndGarageId(Long id, Long garageId);
    boolean existsByEmailAndGarageId(String email, Long garageId);
    boolean existsByPhoneAndGarageId(String phone, Long garageId);
    boolean existsByEmailAndIdNotAndGarageId(String email, Long id, Long garageId);
    boolean existsByPhoneAndIdNotAndGarageId(String phone, Long id, Long garageId);
}