package rw.ac.auca.garagerepairshopmanagementsystem.security;

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Set;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@ConditionalOnProperty(prefix = "app.bootstrap", name = "enabled", havingValue = "true")
public class BootstrapUserInitializer implements ApplicationRunner {

    private final AppUserRepository appUserRepository;
    private final PasswordEncoder passwordEncoder;
    private final List<SeedAccount> accounts;

    public BootstrapUserInitializer(
            AppUserRepository appUserRepository,
            PasswordEncoder passwordEncoder,
            @Value("${app.bootstrap.root-password:}") String rootPassword,
            @Value("${app.bootstrap.admin-password:}") String adminPassword,
            @Value("${app.bootstrap.manager-password:}") String managerPassword,
            @Value("${app.bootstrap.staff-password:}") String staffPassword,
            @Value("${app.bootstrap.user-password:}") String userPassword) {
        this.appUserRepository = appUserRepository;
        this.passwordEncoder = passwordEncoder;
        this.accounts = List.of(
                new SeedAccount("root", "root@garage.local", Role.ADMIN, rootPassword),
                new SeedAccount("admin", "admin@garage.local", Role.ADMIN, adminPassword),
                new SeedAccount("manager", "manager@garage.local", Role.MANAGER, managerPassword),
                new SeedAccount("staff", "staff@garage.local", Role.STAFF, staffPassword),
                new SeedAccount("user", "user@garage.local", Role.USER, userPassword)
        );
    }

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        validatePasswords();
        for (SeedAccount account : accounts) {
            if (appUserRepository.existsByUsername(account.username())) {
                continue;
            }
            if (appUserRepository.existsByEmail(account.email())) {
                throw new IllegalStateException("Bootstrap email is already assigned: " + account.email());
            }
            appUserRepository.save(new AppUser(
                    account.username(),
                    account.email(),
                    passwordEncoder.encode(account.password()),
                    Set.of(account.role())
            ));
        }
    }

    private void validatePasswords() {
        for (SeedAccount account : accounts) {
            int passwordLength = account.password().getBytes(StandardCharsets.UTF_8).length;
            if (passwordLength < 12 || passwordLength > 72) {
                throw new IllegalStateException(
                        "Bootstrap password for " + account.username() + " must be 12 to 72 UTF-8 bytes");
            }
        }
    }

    private record SeedAccount(String username, String email, Role role, String password) {
    }
}