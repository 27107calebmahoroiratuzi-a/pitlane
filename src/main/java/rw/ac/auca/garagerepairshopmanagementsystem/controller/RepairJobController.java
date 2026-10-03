package rw.ac.auca.garagerepairshopmanagementsystem.controller;

import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import rw.ac.auca.garagerepairshopmanagementsystem.dto.RepairJobRequest;
import rw.ac.auca.garagerepairshopmanagementsystem.dto.RepairJobResponse;
import rw.ac.auca.garagerepairshopmanagementsystem.service.RepairJobService;

import java.util.List;

@RestController
@RequestMapping("/api/repair-jobs")
public class RepairJobController {

    private final RepairJobService repairJobService;

    public RepairJobController(
            RepairJobService repairJobService
    ) {
        this.repairJobService = repairJobService;
    }

    @PostMapping
        @PreAuthorize("hasAnyRole('GARAGE_ADMIN','ADMIN','MANAGER','STAFF')")
    public ResponseEntity<RepairJobResponse> create(
            @Valid @RequestBody RepairJobRequest request
    ) {

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(repairJobService.create(request));
    }

    @GetMapping
        @PreAuthorize("hasAnyRole('GARAGE_ADMIN','ADMIN','MANAGER','STAFF','USER')")
    public ResponseEntity<List<RepairJobResponse>> findAll() {

        return ResponseEntity.ok(
                repairJobService.findAll()
        );
    }

    @GetMapping("/{id}")
        @PreAuthorize("hasAnyRole('GARAGE_ADMIN','ADMIN','MANAGER','STAFF','USER')")
    public ResponseEntity<RepairJobResponse> findById(
            @PathVariable Long id
    ) {

        return ResponseEntity.ok(
                repairJobService.findById(id)
        );
    }

    @PutMapping("/{id}")
        @PreAuthorize("hasAnyRole('GARAGE_ADMIN','ADMIN','MANAGER','STAFF')")
    public ResponseEntity<RepairJobResponse> update(
            @PathVariable Long id,
            @Valid @RequestBody RepairJobRequest request
    ) {

        return ResponseEntity.ok(
                repairJobService.update(id, request)
        );
    }

    @DeleteMapping("/{id}")
        @PreAuthorize("hasAnyRole('GARAGE_ADMIN','ADMIN')")
    public ResponseEntity<Void> delete(
            @PathVariable Long id
    ) {

        repairJobService.delete(id);

        return ResponseEntity.noContent().build();
    }
}