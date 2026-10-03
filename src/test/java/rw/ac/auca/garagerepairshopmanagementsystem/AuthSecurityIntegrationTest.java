package rw.ac.auca.garagerepairshopmanagementsystem;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import rw.ac.auca.garagerepairshopmanagementsystem.security.AppUserRepository;
import rw.ac.auca.garagerepairshopmanagementsystem.security.Role;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Map;
import java.util.Set;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(properties = {
        "app.bootstrap.enabled=true",
        "app.bootstrap.root-password=RootSeedTestPass123!",
        "app.bootstrap.admin-password=AdminSeedTestPass123!",
        "app.bootstrap.manager-password=ManagerSeedTestPass123!",
        "app.bootstrap.staff-password=StaffSeedTestPass123!",
        "app.bootstrap.user-password=UserSeedTestPass123!"
})
@AutoConfigureMockMvc
class AuthSecurityIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

        @Autowired
        private AppUserRepository appUserRepository;

        @Test
        void bootstrap_shouldCreateRootAndOneAccountPerRole() {
                assertSeededRole("root", Role.ADMIN);
                assertSeededRole("admin", Role.ADMIN);
                assertSeededRole("manager", Role.MANAGER);
                assertSeededRole("staff", Role.STAFF);
                assertSeededRole("user", Role.USER);
        }

        private void assertSeededRole(String username, Role role) {
                org.assertj.core.api.Assertions.assertThat(appUserRepository.findByUsername(username))
                                .get()
                                .extracting(user -> user.getRoles())
                                .isEqualTo(Set.of(role));
        }

    @Test
    void registerAndLogin_shouldCreateUserAndIssueJwt() throws Exception {
        Map<String, Object> request = Map.of(
                "username", "adminuser",
                "email", "adminuser@example.com",
                "password", "P@ssword123",
                "roles", new String[]{"ADMIN"}
        );

        mockMvc.perform(post("/api/auth/register")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.username").value("adminuser"))
                .andExpect(jsonPath("$.roles.length()").value(1))
                .andExpect(jsonPath("$.roles[0]").value("USER"));

        Map<String, String> loginRequest = Map.of(
                "username", "adminuser",
                "password", "P@ssword123"
        );

        mockMvc.perform(post("/api/auth/login")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").exists());
    }

    @Test
    void protectedCustomerEndpoint_shouldRequireAuthentication() throws Exception {
        mockMvc.perform(get("/api/customers"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void protectedCustomerEndpoint_shouldAllowAdminAccess() throws Exception {
        mockMvc.perform(get("/api/customers"))
                .andExpect(status().isOk());
    }
}
