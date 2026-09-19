package rw.ac.auca.garagerepairshopmanagementsystem.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import rw.ac.auca.garagerepairshopmanagementsystem.model.Customer;

public interface CustomerRepository extends JpaRepository<Customer, Long> {

    boolean existsByEmail(String email);

    boolean existsByPhone(String phone);

    boolean existsByEmailAndIdNot(String email, Long id);

    boolean existsByPhoneAndIdNot(String phone, Long id);
}