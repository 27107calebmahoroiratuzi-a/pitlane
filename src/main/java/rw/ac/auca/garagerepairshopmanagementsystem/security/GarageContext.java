package rw.ac.auca.garagerepairshopmanagementsystem.security;

import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import rw.ac.auca.garagerepairshopmanagementsystem.model.Garage;
import rw.ac.auca.garagerepairshopmanagementsystem.repository.GarageRepository;

@Component("garageContext")
public class GarageContext {

    private final GarageRepository garageRepository;

    public GarageContext(GarageRepository garageRepository) {
        this.garageRepository = garageRepository;
    }

    public UserPrincipal principal() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !(authentication.getPrincipal() instanceof UserPrincipal principal)) {
            throw new AccessDeniedException("A garage account is required");
        }
        return principal;
    }

    public Long requireGarageId() {
        Long garageId = principal().getGarageId();
        if (garageId == null) {
            throw new AccessDeniedException("This account is not assigned to a garage");
        }
        return garageId;
    }

    public Garage requireGarage() {
        return garageRepository.findById(requireGarageId())
                .orElseThrow(() -> new AccessDeniedException("The assigned garage is unavailable"));
    }
}