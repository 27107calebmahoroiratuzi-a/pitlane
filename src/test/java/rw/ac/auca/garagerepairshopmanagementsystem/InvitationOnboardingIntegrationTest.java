package rw.ac.auca.garagerepairshopmanagementsystem;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import rw.ac.auca.garagerepairshopmanagementsystem.messaging.EventPublisher;
import rw.ac.auca.garagerepairshopmanagementsystem.messaging.NotificationEvent;
import rw.ac.auca.garagerepairshopmanagementsystem.security.AppUser;
import rw.ac.auca.garagerepairshopmanagementsystem.security.AppUserRepository;
import rw.ac.auca.garagerepairshopmanagementsystem.security.Role;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.clearInvocations;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * System admin invites a garage admin; the invitee only learns the link from the email event,
 * previews the invitation on the welcome page, completes their profile and is signed in.
 */
@SpringBootTest(properties = {
        "app.bootstrap.enabled=true",
        "app.bootstrap.root-password=RootSeedTestPass123!"
})
@AutoConfigureMockMvc
class InvitationOnboardingIntegrationTest {

    private static final Pattern TOKEN = Pattern.compile("token=([A-Za-z0-9_-]+)");

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private AppUserRepository appUserRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @MockitoBean
    private EventPublisher eventPublisher;

    @BeforeEach
    void setRootCredentials() {
        AppUser root = appUserRepository.findByUsername("root").orElseGet(() ->
                new AppUser("root", "root@garage.local", "", Set.of(Role.SYSTEM_ADMIN)));
        root.setPassword(passwordEncoder.encode("RootSeedTestPass123!"));
        root.setRoles(Set.of(Role.SYSTEM_ADMIN));
        root.setGarage(null);
        appUserRepository.save(root);
        clearInvocations(eventPublisher);
    }

    @Test
    void systemAdminInvitesGarageAdmin_whoCompletesProfileFromEmailLink() throws Exception {
        String rootToken = login("root", "RootSeedTestPass123!");

        mockMvc.perform(post("/api/system/garages")
                        .header("Authorization", "Bearer " + rootToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(Map.of("name", "Kigali Auto Care", "adminEmail", "Ada.Owner@Example.com"))))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.adminInvitation.email").value("ada.owner@example.com"))
                .andExpect(jsonPath("$.adminInvitation.role").value("GARAGE_ADMIN"))
                .andExpect(jsonPath("$.adminInvitation.acceptanceUrl").doesNotExist());

        NotificationEvent email = lastEvent("USER_INVITED");
        assertThat(email.channel()).isEqualTo("EMAIL");
        assertThat(email.recipient()).isEqualTo("ada.owner@example.com");
        assertThat(email.subject()).contains("Kigali Auto Care");
        assertThat(email.message()).contains("GARAGE ADMIN", "http://localhost:5173/accept-invitation?token=");
        String token = tokenFrom(email);

        mockMvc.perform(post("/api/auth/invitations/preview")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(Map.of("token", token))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value("ada.owner@example.com"))
                .andExpect(jsonPath("$.role").value("GARAGE_ADMIN"))
                .andExpect(jsonPath("$.garageName").value("Kigali Auto Care"))
                .andExpect(jsonPath("$.invitedBy").value("root"))
                .andExpect(jsonPath("$.expiresAt").exists());

        mockMvc.perform(post("/api/auth/invitations/accept")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(Map.of("token", token, "username", "ada", "password", "AdaGaragePass123!"))))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors.fullName").exists());

        mockMvc.perform(post("/api/auth/invitations/accept")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(acceptBody(token, "Ada Uwase", "+250 788 000 111", "ada", "AdaGaragePass123!"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").isNotEmpty())
                .andExpect(jsonPath("$.username").value("ada"))
                .andExpect(jsonPath("$.roles[0]").value("GARAGE_ADMIN"))
                .andExpect(jsonPath("$.garageName").value("Kigali Auto Care"));

        NotificationEvent accepted = lastEvent("INVITATION_ACCEPTED");
        assertThat(accepted.recipient()).isEqualTo("root@garage.local");
        assertThat(accepted.message()).contains("Ada Uwase", "Kigali Auto Care");

        mockMvc.perform(post("/api/auth/invitations/preview")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(Map.of("token", token))))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message").value("This invitation has already been accepted. Sign in instead."));
        mockMvc.perform(post("/api/auth/invitations/accept")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(acceptBody(token, "Someone Else", "", "ada-two", "AnotherPass1234!"))))
                .andExpect(status().isConflict());

        String adaToken = login("ada", "AdaGaragePass123!");
        mockMvc.perform(get("/api/garages/me/users").header("Authorization", "Bearer " + adaToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].username").value("ada"))
                .andExpect(jsonPath("$[0].fullName").value("Ada Uwase"))
                .andExpect(jsonPath("$[0].role").value("GARAGE_ADMIN"));
    }

    @Test
    void reinvitingTheSameEmailReplacesThePendingInvitation() throws Exception {
        String rootToken = login("root", "RootSeedTestPass123!");
        Map<String, String> garage = Map.of("name", "Musanze Motors", "adminEmail", "owner@musanze.example");

        mockMvc.perform(post("/api/system/garages")
                        .header("Authorization", "Bearer " + rootToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(garage)))
                .andExpect(status().isCreated());
        String firstToken = tokenFrom(lastEvent("USER_INVITED"));
        String adminToken = acceptAndGetToken(firstToken, "musanze-admin");

        mockMvc.perform(post("/api/garages/me/invitations")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(Map.of("email", "staff@musanze.example", "role", "STAFF"))))
                .andExpect(status().isCreated());
        String lostToken = tokenFrom(lastEvent("USER_INVITED"));

        mockMvc.perform(post("/api/garages/me/invitations")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(Map.of("email", "staff@musanze.example", "role", "STAFF"))))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.message").value(
                        "Previous invitation revoked; a new invitation email was queued for staff@musanze.example"));
        String resentToken = tokenFrom(lastEvent("USER_INVITED"));

        assertThat(resentToken).isNotEqualTo(lostToken);
        mockMvc.perform(post("/api/auth/invitations/preview")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(Map.of("token", lostToken))))
                .andExpect(status().isConflict());
        mockMvc.perform(post("/api/auth/invitations/preview")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(Map.of("token", resentToken))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.role").value("STAFF"))
                .andExpect(jsonPath("$.invitedBy").value("musanze-admin Example"));
    }

    @Test
    void unknownTokenIsRejectedOnTheWelcomePage() throws Exception {
        mockMvc.perform(post("/api/auth/invitations/preview")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(Map.of("token", "x".repeat(43)))))
                .andExpect(status().isNotFound());
    }

    private String acceptAndGetToken(String invitationToken, String username) throws Exception {
        MvcResult result = mockMvc.perform(post("/api/auth/invitations/accept")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(acceptBody(invitationToken, username + " Example", "", username, "GaragePass12345!"))))
                .andExpect(status().isOk())
                .andReturn();
        return objectMapper.readTree(result.getResponse().getContentAsString()).path("token").asText();
    }

    private NotificationEvent lastEvent(String eventType) {
        ArgumentCaptor<NotificationEvent> captor = ArgumentCaptor.forClass(NotificationEvent.class);
        verify(eventPublisher, atLeastOnce()).publish(captor.capture());
        List<NotificationEvent> matching = captor.getAllValues().stream()
                .filter(event -> event.eventType().equals(eventType))
                .toList();
        assertThat(matching).as("published %s events", eventType).isNotEmpty();
        return matching.getLast();
    }

    private static String tokenFrom(NotificationEvent email) {
        Matcher matcher = TOKEN.matcher(email.message());
        assertThat(matcher.find()).as("invitation link in email body").isTrue();
        return matcher.group(1);
    }

    private static Map<String, String> acceptBody(String token, String fullName, String phone,
                                                  String username, String password) {
        Map<String, String> body = new HashMap<>();
        body.put("token", token);
        body.put("fullName", fullName);
        body.put("phone", phone);
        body.put("username", username);
        body.put("password", password);
        return body;
    }

    private String login(String username, String password) throws Exception {
        MvcResult result = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(Map.of("username", username, "password", password))))
                .andExpect(status().isOk())
                .andReturn();
        JsonNode body = objectMapper.readTree(result.getResponse().getContentAsString());
        return body.path("token").asText();
    }

    private String json(Object value) throws Exception {
        return objectMapper.writeValueAsString(value);
    }
}
