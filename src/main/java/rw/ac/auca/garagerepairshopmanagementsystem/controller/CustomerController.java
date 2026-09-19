package rw.ac.auca.garagerepairshopmanagementsystem.controller;

import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import rw.ac.auca.garagerepairshopmanagementsystem.dto.CustomerRequest;
import rw.ac.auca.garagerepairshopmanagementsystem.dto.CustomerResponse;
import rw.ac.auca.garagerepairshopmanagementsystem.service.CustomerService;

import java.util.List;

@RestController
@RequestMapping("/api/customers")
public class CustomerController {

    private final CustomerService customerService;

    public CustomerController(CustomerService customerService) {
        this.customerService = customerService;
    }

    @PostMapping
    public ResponseEntity<CustomerResponse> create(
            @Valid @RequestBody CustomerRequest request
    ) {
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(customerService.create(request));
    }

    @GetMapping
    public ResponseEntity<List<CustomerResponse>> findAll() {

        return ResponseEntity.ok(
                customerService.findAll()
        );
    }

    @GetMapping("/{id}")
    public ResponseEntity<CustomerResponse> findById(
            @PathVariable Long id
    ) {

        return ResponseEntity.ok(
                customerService.findById(id)
        );
    }

    @PutMapping("/{id}")
    public ResponseEntity<CustomerResponse> update(
            @PathVariable Long id,
            @Valid @RequestBody CustomerRequest request
    ) {

        return ResponseEntity.ok(
                customerService.update(id, request)
        );
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(
            @PathVariable Long id
    ) {

        customerService.delete(id);

        return ResponseEntity.noContent().build();
    }
}