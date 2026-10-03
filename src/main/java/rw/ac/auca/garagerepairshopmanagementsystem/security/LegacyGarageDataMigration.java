package rw.ac.auca.garagerepairshopmanagementsystem.security;

import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.annotation.Order;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import rw.ac.auca.garagerepairshopmanagementsystem.model.Customer;
import rw.ac.auca.garagerepairshopmanagementsystem.model.Garage;
import rw.ac.auca.garagerepairshopmanagementsystem.model.Mechanic;
import rw.ac.auca.garagerepairshopmanagementsystem.model.SparePart;
import rw.ac.auca.garagerepairshopmanagementsystem.model.Vehicle;
import rw.ac.auca.garagerepairshopmanagementsystem.repository.CustomerRepository;
import rw.ac.auca.garagerepairshopmanagementsystem.repository.GarageRepository;
import rw.ac.auca.garagerepairshopmanagementsystem.repository.MechanicRepository;
import rw.ac.auca.garagerepairshopmanagementsystem.repository.SparePartRepository;
import rw.ac.auca.garagerepairshopmanagementsystem.repository.VehicleRepository;

import java.util.Set;
import java.util.stream.Collectors;

@Component
@Order(0)
public class LegacyGarageDataMigration implements ApplicationRunner {

    private final GarageRepository garageRepository;
    private final AppUserRepository appUserRepository;
    private final CustomerRepository customerRepository;
    private final VehicleRepository vehicleRepository;
    private final MechanicRepository mechanicRepository;
    private final SparePartRepository sparePartRepository;
    private final JdbcTemplate jdbcTemplate;

    public LegacyGarageDataMigration(GarageRepository garageRepository,
                                     AppUserRepository appUserRepository,
                                     CustomerRepository customerRepository,
                                     VehicleRepository vehicleRepository,
                                     MechanicRepository mechanicRepository,
                                     SparePartRepository sparePartRepository,
                                     JdbcTemplate jdbcTemplate) {
        this.garageRepository = garageRepository;
        this.appUserRepository = appUserRepository;
        this.customerRepository = customerRepository;
        this.vehicleRepository = vehicleRepository;
        this.mechanicRepository = mechanicRepository;
        this.sparePartRepository = sparePartRepository;
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        dropLegacyGlobalUniqueness();

        boolean hasLegacyRows = appUserRepository.findAll().stream()
                .anyMatch(user -> user.getGarage() == null && !user.getUsername().equals("root")
                        && user.getRoles().stream().noneMatch(role -> role.effectiveRole() == Role.SYSTEM_ADMIN))
                || customerRepository.findAll().stream().anyMatch(customer -> customer.getGarage() == null)
                || vehicleRepository.findAll().stream().anyMatch(vehicle -> vehicle.getGarage() == null)
                || mechanicRepository.findAll().stream().anyMatch(mechanic -> mechanic.getGarage() == null)
                || sparePartRepository.findAll().stream().anyMatch(part -> part.getGarage() == null);
        if (!hasLegacyRows) {
            return;
        }

        Garage legacyGarage = garageRepository.findFirstByName("Legacy Garage")
                .orElseGet(() -> garageRepository.save(new Garage("Legacy Garage")));
        appUserRepository.findAll().forEach(user -> {
            if (!user.getUsername().equals("root")
                    && user.getRoles().stream().noneMatch(role -> role.effectiveRole() == Role.SYSTEM_ADMIN)) {
                if (user.getGarage() == null) user.setGarage(legacyGarage);
                Set<Role> roles = user.getRoles().stream().map(Role::effectiveRole).collect(Collectors.toSet());
                user.setRoles(roles);
                appUserRepository.save(user);
            }
        });
        customerRepository.findAll().forEach(customer -> {
            if (customer.getGarage() == null) customer.setGarage(legacyGarage);
        });
        vehicleRepository.findAll().forEach(vehicle -> {
            if (vehicle.getGarage() == null) {
                vehicle.setGarage(vehicle.getCustomer().getGarage() == null ? legacyGarage : vehicle.getCustomer().getGarage());
            }
        });
        mechanicRepository.findAll().forEach(mechanic -> {
            if (mechanic.getGarage() == null) mechanic.setGarage(legacyGarage);
        });
        sparePartRepository.findAll().forEach(part -> {
            if (part.getGarage() == null) part.setGarage(legacyGarage);
        });
        customerRepository.saveAll(customerRepository.findAll());
        vehicleRepository.saveAll(vehicleRepository.findAll());
        mechanicRepository.saveAll(mechanicRepository.findAll());
        sparePartRepository.saveAll(sparePartRepository.findAll());
    }

    private void dropLegacyGlobalUniqueness() {
        jdbcTemplate.execute("alter table customers drop constraint if exists uk_customer_email");
        jdbcTemplate.execute("alter table customers drop constraint if exists uk_customer_phone");
        jdbcTemplate.execute("alter table vehicles drop constraint if exists uk_vehicle_plate");
        jdbcTemplate.execute("alter table mechanics drop constraint if exists uk_mechanic_email");
        jdbcTemplate.execute("alter table mechanics drop constraint if exists uk_mechanic_phone");
        jdbcTemplate.execute("alter table spare_parts drop constraint if exists uk_spare_part_sku");
    }
}