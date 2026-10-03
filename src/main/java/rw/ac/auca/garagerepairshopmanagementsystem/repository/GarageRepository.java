package rw.ac.auca.garagerepairshopmanagementsystem.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import rw.ac.auca.garagerepairshopmanagementsystem.model.Garage;

import java.util.Optional;

public interface GarageRepository extends JpaRepository<Garage, Long> {
	Optional<Garage> findFirstByName(String name);
}