package rw.ac.auca.garagerepairshopmanagementsystem.controller;

import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import rw.ac.auca.garagerepairshopmanagementsystem.dto.MechanicRequest;
import rw.ac.auca.garagerepairshopmanagementsystem.dto.MechanicResponse;
import rw.ac.auca.garagerepairshopmanagementsystem.service.MechanicService;

import java.util.List;

@RestController
@RequestMapping("/api/mechanics")
public class MechanicController {
    private final MechanicService mechanicService;

    public MechanicController(MechanicService mechanicService) {
        this.mechanicService = mechanicService;
    }

    @PostMapping
    public ResponseEntity<MechanicResponse> create(@Valid @RequestBody MechanicRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(mechanicService.create(request));
    }

    @GetMapping
    public List<MechanicResponse> findAll() { return mechanicService.findAll(); }

    @GetMapping("/{id}")
    public MechanicResponse findById(@PathVariable Long id) { return mechanicService.findById(id); }

    @PutMapping("/{id}")
    public MechanicResponse update(@PathVariable Long id, @Valid @RequestBody MechanicRequest request) {
        return mechanicService.update(id, request);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        mechanicService.delete(id);
        return ResponseEntity.noContent().build();
    }
}