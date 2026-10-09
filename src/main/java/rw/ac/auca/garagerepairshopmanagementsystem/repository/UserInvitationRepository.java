package rw.ac.auca.garagerepairshopmanagementsystem.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import rw.ac.auca.garagerepairshopmanagementsystem.model.UserInvitation;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface UserInvitationRepository extends JpaRepository<UserInvitation, Long> {
    Optional<UserInvitation> findByTokenHash(String tokenHash);

    List<UserInvitation> findAllByEmailIgnoreCaseAndGarageIdAndAcceptedAtIsNullAndExpiresAtAfter(
            String email, Long garageId, LocalDateTime now);
}
