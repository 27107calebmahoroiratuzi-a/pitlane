package rw.ac.auca.garagerepairshopmanagementsystem.security;

import java.util.Set;

public record AuthResponse(
        String token,
        String username,
        Set<String> roles,
        String message,
        Long garageId,
        String garageName
) {
}
