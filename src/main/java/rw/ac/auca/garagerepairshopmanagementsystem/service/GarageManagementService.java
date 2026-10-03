package rw.ac.auca.garagerepairshopmanagementsystem.service;

import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import rw.ac.auca.garagerepairshopmanagementsystem.dto.CreateGarageRequest;
import rw.ac.auca.garagerepairshopmanagementsystem.dto.GarageCreationResponse;
import rw.ac.auca.garagerepairshopmanagementsystem.dto.GarageResponse;
import rw.ac.auca.garagerepairshopmanagementsystem.dto.InvitationResponse;
import rw.ac.auca.garagerepairshopmanagementsystem.dto.InviteUserRequest;
import rw.ac.auca.garagerepairshopmanagementsystem.dto.TeamMemberResponse;
import rw.ac.auca.garagerepairshopmanagementsystem.model.Garage;
import rw.ac.auca.garagerepairshopmanagementsystem.repository.GarageRepository;
import rw.ac.auca.garagerepairshopmanagementsystem.security.AppUserRepository;
import rw.ac.auca.garagerepairshopmanagementsystem.security.GarageContext;
import rw.ac.auca.garagerepairshopmanagementsystem.security.InvitationService;
import rw.ac.auca.garagerepairshopmanagementsystem.security.Role;
import rw.ac.auca.garagerepairshopmanagementsystem.security.UserPrincipal;

import java.util.List;

@Service
public class GarageManagementService {

    private final GarageRepository garageRepository;
    private final AppUserRepository appUserRepository;
    private final GarageContext garageContext;
    private final InvitationService invitationService;

    public GarageManagementService(GarageRepository garageRepository,
                                   AppUserRepository appUserRepository,
                                   GarageContext garageContext,
                                   InvitationService invitationService) {
        this.garageRepository = garageRepository;
        this.appUserRepository = appUserRepository;
        this.garageContext = garageContext;
        this.invitationService = invitationService;
    }

    @Transactional
    public GarageCreationResponse createGarage(CreateGarageRequest request) {
        requireSystemAdmin();
        Garage garage = garageRepository.save(new Garage(request.name().trim()));
        InvitationResponse invitation = invitationService.invite(garage, request.adminEmail(), Role.GARAGE_ADMIN);
        return new GarageCreationResponse(toResponse(garage), invitation);
    }

    @Transactional(readOnly = true)
    public List<GarageResponse> findAllGarages() {
        requireSystemAdmin();
        return garageRepository.findAll().stream().map(this::toResponse).toList();
    }

    @Transactional(readOnly = true)
    public GarageResponse currentGarage() {
        return toResponse(garageContext.requireGarage());
    }

    @Transactional(readOnly = true)
    public List<TeamMemberResponse> currentTeam() {
        Long garageId = garageContext.requireGarageId();
        return appUserRepository.findAllByGarageId(garageId).stream()
                .map(user -> new TeamMemberResponse(user.getId(), user.getUsername(), user.getEmail(),
                        user.getRoles().stream().findFirst().map(Role::effectiveRole).map(Enum::name).orElse("USER")))
                .toList();
    }

    @Transactional
    public InvitationResponse inviteCurrentGarageUser(InviteUserRequest request) {
        return invitationService.invite(garageContext.requireGarage(), request.email(), request.role());
    }

    private void requireSystemAdmin() {
        UserPrincipal principal = garageContext.principal();
        if (principal.getRoles().stream().map(Role::effectiveRole).noneMatch(role -> role == Role.SYSTEM_ADMIN)) {
            throw new AccessDeniedException("Only a system administrator can manage garages");
        }
    }

    private GarageResponse toResponse(Garage garage) {
        return new GarageResponse(garage.getId(), garage.getName(), garage.getCreatedAt());
    }
}