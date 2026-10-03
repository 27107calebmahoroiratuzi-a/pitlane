package rw.ac.auca.garagerepairshopmanagementsystem;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import rw.ac.auca.garagerepairshopmanagementsystem.security.AppUserRepository;
import rw.ac.auca.garagerepairshopmanagementsystem.security.Role;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Map;
import static org.assertj.core.api.Assertions.assertThat;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(properties = {
        "app.bootstrap.enabled=true",
        "app.bootstrap.root-password=RootSeedTestPass123!"
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
            void bootstrap_shouldCreateSystemAdminWithoutGarage() {
                assertThat(appUserRepository.findByUsername("root"))
                                .get()
                        .extracting(user -> user.getRoles())
                        .isEqualTo(java.util.Set.of(Role.SYSTEM_ADMIN));
                assertThat(appUserRepository.findByUsername("root").orElseThrow().getGarage()).isNull();
        }

    @Test
            void registrationIsInvitationOnly_andSystemAdminCanLogin() throws Exception {
                mockMvc.perform(post("/api/auth/register"))
                        .andExpect(status().isGone());

                Map<String, String> loginRequest = Map.of("username", "root", "password", "RootSeedTestPass123!");
        mockMvc.perform(post("/api/auth/login")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isOk())
                        .andExpect(jsonPath("$.token").exists())
                        .andExpect(jsonPath("$.roles[0]").value("SYSTEM_ADMIN"))
                        .andExpect(jsonPath("$.garageId").doesNotExist());
    }

    @Test
    void protectedCustomerEndpoint_shouldRequireAuthentication() throws Exception {
        mockMvc.perform(get("/api/customers"))
                .andExpect(status().isUnauthorized());
    }

}
