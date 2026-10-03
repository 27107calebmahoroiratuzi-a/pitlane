package rw.ac.auca.garagerepairshopmanagementsystem.controller;

import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import rw.ac.auca.garagerepairshopmanagementsystem.dto.CreateGarageRequest;
import rw.ac.auca.garagerepairshopmanagementsystem.dto.GarageCreationResponse;
import rw.ac.auca.garagerepairshopmanagementsystem.dto.GarageResponse;
import rw.ac.auca.garagerepairshopmanagementsystem.dto.InvitationResponse;
import rw.ac.auca.garagerepairshopmanagementsystem.dto.InviteUserRequest;
import rw.ac.auca.garagerepairshopmanagementsystem.dto.TeamMemberResponse;
import rw.ac.auca.garagerepairshopmanagementsystem.service.GarageManagementService;

import java.util.List;

@RestController
@RequestMapping("/api")
public class GarageManagementController {

    private final GarageManagementService garageManagementService;

    public GarageManagementController(GarageManagementService garageManagementService) {
        this.garageManagementService = garageManagementService;
    }

    @PostMapping("/system/garages")
    @PreAuthorize("hasRole('SYSTEM_ADMIN')")
    public ResponseEntity<GarageCreationResponse> createGarage(@Valid @RequestBody CreateGarageRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(garageManagementService.createGarage(request));
    }

    @GetMapping("/system/garages")
    @PreAuthorize("hasRole('SYSTEM_ADMIN')")
    public List<GarageResponse> findAllGarages() {
        return garageManagementService.findAllGarages();
    }

    @GetMapping("/garages/me")
    @PreAuthorize("hasAnyRole('GARAGE_ADMIN','ADMIN','MANAGER','STAFF','USER')")
    public GarageResponse currentGarage() {
        return garageManagementService.currentGarage();
    }

    @GetMapping("/garages/me/users")
    @PreAuthorize("hasAnyRole('GARAGE_ADMIN','ADMIN','MANAGER','STAFF','USER')")
    public List<TeamMemberResponse> currentTeam() {
        return garageManagementService.currentTeam();
    }

    @PostMapping("/garages/me/invitations")
    @PreAuthorize("hasAnyRole('GARAGE_ADMIN','ADMIN','MANAGER','STAFF')")
    public ResponseEntity<InvitationResponse> inviteUser(@Valid @RequestBody InviteUserRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(garageManagementService.inviteCurrentGarageUser(request));
    }
}