package rw.ac.auca.garagerepairshopmanagementsystem.security;

import java.nio.charset.StandardCharsets;
import java.util.Set;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.core.annotation.Order;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@Order(1)
@ConditionalOnProperty(prefix = "app.bootstrap", name = "enabled", havingValue = "true")
public class BootstrapUserInitializer implements ApplicationRunner {

    private final AppUserRepository appUserRepository;
    private final PasswordEncoder passwordEncoder;
    private final String rootPassword;

    public BootstrapUserInitializer(
            AppUserRepository appUserRepository,
            PasswordEncoder passwordEncoder,
            @Value("${app.bootstrap.root-password:}") String rootPassword) {
        this.appUserRepository = appUserRepository;
        this.passwordEncoder = passwordEncoder;
        this.rootPassword = rootPassword;
    }

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        AppUser root = appUserRepository.findByUsername("root").orElse(null);
        if (root != null) {
            root.setRoles(Set.of(Role.SYSTEM_ADMIN));
            root.setGarage(null);
            appUserRepository.save(root);
            return;
        }

        validateRootPassword();
        if (appUserRepository.existsByEmail("root@garage.local")) {
            throw new IllegalStateException("Bootstrap email is already assigned: root@garage.local");
        }
        appUserRepository.save(new AppUser(
                "root",
                "root@garage.local",
                passwordEncoder.encode(rootPassword),
                Set.of(Role.SYSTEM_ADMIN)
        ));
    }

    private void validateRootPassword() {
        int passwordLength = rootPassword.getBytes(StandardCharsets.UTF_8).length;
        if (passwordLength < 12 || passwordLength > 72) {
            throw new IllegalStateException("Bootstrap root password must be 12 to 72 UTF-8 bytes");
        }
    }
}