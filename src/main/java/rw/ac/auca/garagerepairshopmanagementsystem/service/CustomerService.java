package rw.ac.auca.garagerepairshopmanagementsystem.service;

import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import rw.ac.auca.garagerepairshopmanagementsystem.dto.CustomerRequest;
import rw.ac.auca.garagerepairshopmanagementsystem.dto.CustomerResponse;
import rw.ac.auca.garagerepairshopmanagementsystem.exception.BusinessException;
import rw.ac.auca.garagerepairshopmanagementsystem.exception.ResourceNotFoundException;
import rw.ac.auca.garagerepairshopmanagementsystem.model.Customer;
import rw.ac.auca.garagerepairshopmanagementsystem.repository.CustomerRepository;
import rw.ac.auca.garagerepairshopmanagementsystem.security.GarageContext;

import java.util.List;

@Service
public class CustomerService {

    private final CustomerRepository customerRepository;
    private final GarageContext garageContext;

    public CustomerService(CustomerRepository customerRepository, GarageContext garageContext) {
        this.customerRepository = customerRepository;
        this.garageContext = garageContext;
    }

    @Transactional
    @CacheEvict(value = "customers", allEntries = true)
    public CustomerResponse create(CustomerRequest request) {
        Long garageId = garageContext.requireGarageId();

        if (customerRepository.existsByEmailAndGarageId(request.getEmail(), garageId)) {
            throw new BusinessException("Email already exists");
        }

        if (customerRepository.existsByPhoneAndGarageId(request.getPhone(), garageId)) {
            throw new BusinessException("Phone number already exists");
        }

        Customer customer = new Customer();

        customer.setFullName(request.getFullName());
        customer.setPhone(request.getPhone());
        customer.setEmail(request.getEmail());
        customer.setAddress(request.getAddress());
        customer.setGarage(garageContext.requireGarage());

        return toResponse(customerRepository.save(customer));
    }

    @Cacheable(value = "customers", key = "@garageContext.requireGarageId()")
    public List<CustomerResponse> findAll() {

        return customerRepository.findAllByGarageId(garageContext.requireGarageId())
                .stream()
                .map(this::toResponse)
                .toList();
    }

    @Cacheable(value = "customers", key = "#id + ':' + @garageContext.requireGarageId()")
    public CustomerResponse findById(Long id) {

        Customer customer = customerRepository.findByIdAndGarageId(id, garageContext.requireGarageId())
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Customer not found with ID: " + id
                        ));

        return toResponse(customer);
    }

    @Transactional
    @CacheEvict(value = "customers", allEntries = true)
    public CustomerResponse update(Long id, CustomerRequest request) {
        Long garageId = garageContext.requireGarageId();

        Customer customer = customerRepository.findByIdAndGarageId(id, garageId)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Customer not found with ID: " + id
                        ));

        if (customerRepository.existsByEmailAndIdNotAndGarageId(
                request.getEmail(), id, garageId)) {
            throw new BusinessException("Email already exists");
        }

        if (customerRepository.existsByPhoneAndIdNotAndGarageId(
                request.getPhone(), id, garageId)) {
            throw new BusinessException("Phone number already exists");
        }

        customer.setFullName(request.getFullName());
        customer.setPhone(request.getPhone());
        customer.setEmail(request.getEmail());
        customer.setAddress(request.getAddress());

        return toResponse(customerRepository.save(customer));
    }

    @Transactional
    @CacheEvict(value = "customers", allEntries = true)
    public void delete(Long id) {
        Long garageId = garageContext.requireGarageId();

        Customer customer = customerRepository.findByIdAndGarageId(id, garageId)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Customer not found with ID: " + id
                        ));

        if (customer.getId() == null) {
            throw new BusinessException("Invalid customer");
        }

        customerRepository.delete(customer);
    }

    private CustomerResponse toResponse(Customer customer) {

        return new CustomerResponse(
                customer.getId(),
                customer.getUuid(),
                customer.getFullName(),
                customer.getPhone(),
                customer.getEmail(),
                customer.getAddress()
        );
    }
}