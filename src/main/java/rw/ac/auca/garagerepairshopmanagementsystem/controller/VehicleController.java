package rw.ac.auca.garagerepairshopmanagementsystem.controller;

import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import rw.ac.auca.garagerepairshopmanagementsystem.dto.VehicleRequest;
import rw.ac.auca.garagerepairshopmanagementsystem.dto.VehicleResponse;
import rw.ac.auca.garagerepairshopmanagementsystem.service.VehicleService;

import java.util.List;

@RestController
@RequestMapping("/api/vehicles")
public class VehicleController {

    private final VehicleService vehicleService;

    public VehicleController(VehicleService vehicleService) {
        this.vehicleService = vehicleService;
    }

    @PostMapping
    public ResponseEntity<VehicleResponse> create(
            @Valid @RequestBody VehicleRequest request
    ) {

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(vehicleService.create(request));
    }

    @GetMapping
    public ResponseEntity<List<VehicleResponse>> findAll() {

        return ResponseEntity.ok(
                vehicleService.findAll()
        );
    }

    @GetMapping("/{id}")
    public ResponseEntity<VehicleResponse> findById(
            @PathVariable Long id
    ) {

        return ResponseEntity.ok(
                vehicleService.findById(id)
        );
    }

    @PutMapping("/{id}")
    public ResponseEntity<VehicleResponse> update(
            @PathVariable Long id,
            @Valid @RequestBody VehicleRequest request
    ) {

        return ResponseEntity.ok(
                vehicleService.update(id, request)
        );
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(
            @PathVariable Long id
    ) {

        vehicleService.delete(id);

        return ResponseEntity.noContent().build();
    }
}