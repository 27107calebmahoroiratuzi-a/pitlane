package rw.ac.auca.garagerepairshopmanagementsystem;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import rw.ac.auca.garagerepairshopmanagementsystem.security.AppUser;
import rw.ac.auca.garagerepairshopmanagementsystem.security.AppUserRepository;
import rw.ac.auca.garagerepairshopmanagementsystem.security.Role;

import java.net.URI;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(properties = {
        "app.bootstrap.enabled=true",
        "app.bootstrap.root-password=RootSeedTestPass123!"
})
@AutoConfigureMockMvc
class GarageTenantIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

        @Autowired
        private AppUserRepository appUserRepository;

        @Autowired
        private PasswordEncoder passwordEncoder;

        @BeforeEach
        void setRootCredentials() {
                AppUser root = appUserRepository.findByUsername("root").orElseGet(() ->
                        new AppUser("root", "root@garage.local", "", java.util.Set.of(Role.SYSTEM_ADMIN)));
                root.setPassword(passwordEncoder.encode("RootSeedTestPass123!"));
                root.setRoles(java.util.Set.of(Role.SYSTEM_ADMIN));
                root.setGarage(null);
                appUserRepository.save(root);
        }

    @Test
    void garagesAreIsolatedAndInvitesRespectRoleHierarchy() throws Exception {
        String systemAdminToken = login("root", "RootSeedTestPass123!");
        JsonNode garageOne = createGarage(systemAdminToken, "Northside Service", "owner-one@example.com");
        String garageOneAdminToken = acceptInvitation(garageOne, "north-admin", "GarageAdminOnePass123!");
        Long garageOneCustomerId = createCustomer(garageOneAdminToken, "Shared Customer", "shared@example.com", "+250788123001");

        JsonNode garageTwo = createGarage(systemAdminToken, "Lakeside Motors", "owner-two@example.com");
        String garageTwoAdminToken = acceptInvitation(garageTwo, "lake-admin", "GarageAdminTwoPass123!");
        createCustomer(garageTwoAdminToken, "Shared Customer", "shared@example.com", "+250788123001");

        mockMvc.perform(get("/api/customers/{id}", garageOneCustomerId)
                        .header("Authorization", "Bearer " + garageTwoAdminToken))
                .andExpect(status().isNotFound());
        mockMvc.perform(get("/api/customers/{id}", garageOneCustomerId)
                        .header("Authorization", "Bearer " + garageOneAdminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value("shared@example.com"));

        MvcResult managerInviteResult = mockMvc.perform(post("/api/garages/me/invitations")
                        .header("Authorization", "Bearer " + garageOneAdminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of(
                                "email", "manager-one@example.com", "role", "MANAGER"))))
                .andExpect(status().isCreated())
                .andReturn();
        String managerToken = acceptInvitation(
                objectMapper.readTree(managerInviteResult.getResponse().getContentAsString()),
                "north-manager",
                "GarageManagerOnePass123!");

        mockMvc.perform(post("/api/garages/me/invitations")
                        .header("Authorization", "Bearer " + managerToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of(
                                "email", "bad-promotion@example.com", "role", "GARAGE_ADMIN"))))
                .andExpect(status().isForbidden());

        mockMvc.perform(post("/api/system/garages")
                        .header("Authorization", "Bearer " + managerToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of(
                                "name", "Unauthorized Garage", "adminEmail", "owner-bad@example.com"))))
                .andExpect(status().isForbidden());

        assertThat(garageOne.path("garage").path("name").asText()).isEqualTo("Northside Service");
        assertThat(garageTwo.path("garage").path("name").asText()).isEqualTo("Lakeside Motors");
    }

    private JsonNode createGarage(String systemAdminToken, String name, String adminEmail) throws Exception {
        MvcResult result = mockMvc.perform(post("/api/system/garages")
                        .header("Authorization", "Bearer " + systemAdminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of("name", name, "adminEmail", adminEmail))))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.garage.id").exists())
                .andExpect(jsonPath("$.adminInvitation.role").value("GARAGE_ADMIN"))
                .andReturn();
        return objectMapper.readTree(result.getResponse().getContentAsString());
    }

    private String acceptInvitation(JsonNode invitation, String username, String password) throws Exception {
        String acceptanceUrl = invitation.path("adminInvitation").path("acceptanceUrl").asText();
        if (acceptanceUrl.isBlank()) {
            acceptanceUrl = invitation.path("acceptanceUrl").asText();
        }
        String token = URI.create(acceptanceUrl).getRawQuery().substring("token=".length());
        MvcResult result = mockMvc.perform(post("/api/auth/invitations/accept")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of(
                                "token", token, "username", username, "password", password))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.roles[0]").value(invitation.path("adminInvitation").path("role").asText().isBlank()
                        ? "MANAGER" : "GARAGE_ADMIN"))
                .andReturn();
        return objectMapper.readTree(result.getResponse().getContentAsString()).path("token").asText();
    }

    private String login(String username, String password) throws Exception {
        MvcResult result = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of("username", username, "password", password))))
                .andExpect(status().isOk())
                .andReturn();
        return objectMapper.readTree(result.getResponse().getContentAsString()).path("token").asText();
    }

    private Long createCustomer(String token, String name, String email, String phone) throws Exception {
        MvcResult result = mockMvc.perform(post("/api/customers")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of(
                                "fullName", name,
                                "phone", phone,
                                "email", email,
                                "address", "Rwanda"))))
                .andExpect(status().isCreated())
                .andReturn();
        return objectMapper.readTree(result.getResponse().getContentAsString()).path("id").asLong();
    }
}