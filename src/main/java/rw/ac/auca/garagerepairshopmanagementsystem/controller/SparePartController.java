package rw.ac.auca.garagerepairshopmanagementsystem.controller;

import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import rw.ac.auca.garagerepairshopmanagementsystem.dto.SparePartRequest;
import rw.ac.auca.garagerepairshopmanagementsystem.dto.SparePartResponse;
import rw.ac.auca.garagerepairshopmanagementsystem.service.SparePartService;

import java.util.List;

@RestController
@RequestMapping("/api/spare-parts")
public class SparePartController {
    private final SparePartService sparePartService;

    public SparePartController(SparePartService sparePartService) {
        this.sparePartService = sparePartService;
    }

    @PostMapping
    public ResponseEntity<SparePartResponse> create(@Valid @RequestBody SparePartRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(sparePartService.create(request));
    }

    @GetMapping
    public List<SparePartResponse> findAll() { return sparePartService.findAll(); }

    @GetMapping("/{id}")
    public SparePartResponse findById(@PathVariable Long id) { return sparePartService.findById(id); }

    @PutMapping("/{id}")
    public SparePartResponse update(@PathVariable Long id, @Valid @RequestBody SparePartRequest request) {
        return sparePartService.update(id, request);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        sparePartService.delete(id);
        return ResponseEntity.noContent().build();
    }
}