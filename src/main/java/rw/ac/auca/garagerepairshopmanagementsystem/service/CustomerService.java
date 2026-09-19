package rw.ac.auca.garagerepairshopmanagementsystem.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import rw.ac.auca.garagerepairshopmanagementsystem.dto.CustomerRequest;
import rw.ac.auca.garagerepairshopmanagementsystem.dto.CustomerResponse;
import rw.ac.auca.garagerepairshopmanagementsystem.exception.BusinessException;
import rw.ac.auca.garagerepairshopmanagementsystem.exception.ResourceNotFoundException;
import rw.ac.auca.garagerepairshopmanagementsystem.model.Customer;
import rw.ac.auca.garagerepairshopmanagementsystem.repository.CustomerRepository;

import java.util.List;

@Service
public class CustomerService {

    private final CustomerRepository customerRepository;

    public CustomerService(CustomerRepository customerRepository) {
        this.customerRepository = customerRepository;
    }

    @Transactional
    public CustomerResponse create(CustomerRequest request) {

        if (customerRepository.existsByEmail(request.getEmail())) {
            throw new BusinessException("Email already exists");
        }

        if (customerRepository.existsByPhone(request.getPhone())) {
            throw new BusinessException("Phone number already exists");
        }

        Customer customer = new Customer();

        customer.setFullName(request.getFullName());
        customer.setPhone(request.getPhone());
        customer.setEmail(request.getEmail());
        customer.setAddress(request.getAddress());

        return toResponse(customerRepository.save(customer));
    }

    public List<CustomerResponse> findAll() {

        return customerRepository.findAll()
                .stream()
                .map(this::toResponse)
                .toList();
    }

    public CustomerResponse findById(Long id) {

        Customer customer = customerRepository.findById(id)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Customer not found with ID: " + id
                        ));

        return toResponse(customer);
    }

    @Transactional
    public CustomerResponse update(Long id, CustomerRequest request) {

        Customer customer = customerRepository.findById(id)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Customer not found with ID: " + id
                        ));

        if (customerRepository.existsByEmailAndIdNot(
                request.getEmail(), id)) {
            throw new BusinessException("Email already exists");
        }

        if (customerRepository.existsByPhoneAndIdNot(
                request.getPhone(), id)) {
            throw new BusinessException("Phone number already exists");
        }

        customer.setFullName(request.getFullName());
        customer.setPhone(request.getPhone());
        customer.setEmail(request.getEmail());
        customer.setAddress(request.getAddress());

        return toResponse(customerRepository.save(customer));
    }

    @Transactional
    public void delete(Long id) {

        Customer customer = customerRepository.findById(id)
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