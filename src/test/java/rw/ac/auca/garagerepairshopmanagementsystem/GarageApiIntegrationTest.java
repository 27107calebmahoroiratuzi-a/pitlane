package rw.ac.auca.garagerepairshopmanagementsystem;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;

import rw.ac.auca.garagerepairshopmanagementsystem.dto.CustomerRequest;
import rw.ac.auca.garagerepairshopmanagementsystem.dto.MechanicRequest;
import rw.ac.auca.garagerepairshopmanagementsystem.dto.PaymentRequest;
import rw.ac.auca.garagerepairshopmanagementsystem.dto.RepairJobRequest;
import rw.ac.auca.garagerepairshopmanagementsystem.dto.RepairJobPartRequest;
import rw.ac.auca.garagerepairshopmanagementsystem.dto.SparePartRequest;
import rw.ac.auca.garagerepairshopmanagementsystem.dto.VehicleRequest;
import rw.ac.auca.garagerepairshopmanagementsystem.model.Garage;
import rw.ac.auca.garagerepairshopmanagementsystem.model.RepairJobStatus;
import rw.ac.auca.garagerepairshopmanagementsystem.security.AppUser;
import rw.ac.auca.garagerepairshopmanagementsystem.security.AppUserRepository;
import rw.ac.auca.garagerepairshopmanagementsystem.security.AuthRequest;
import rw.ac.auca.garagerepairshopmanagementsystem.security.Role;
import rw.ac.auca.garagerepairshopmanagementsystem.repository.GarageRepository;

import java.util.Set;

@SpringBootTest
@AutoConfigureMockMvc
class GarageApiIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

        @Autowired
        private AppUserRepository appUserRepository;

        @Autowired
        private GarageRepository garageRepository;

        @Autowired
        private PasswordEncoder passwordEncoder;

    @Test
    void garageWorkflow_shouldCreateCustomerVehicleAndRepairJob() throws Exception {
                String bearerToken = registerAdminToken();

        CustomerRequest customerRequest = new CustomerRequest();
        customerRequest.setFullName("Alice Niyonsenga");
        customerRequest.setPhone("+250788123456");
        customerRequest.setEmail("alice@example.com");
        customerRequest.setAddress("Kigali, Rwanda");

                MvcResult customerResult = mockMvc.perform(authorized(post("/api/customers"), bearerToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(customerRequest)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.fullName").value("Alice Niyonsenga"))
                .andReturn();

        Long customerId = objectMapper.readTree(customerResult.getResponse().getContentAsString())
                .get("id")
                .asLong();

        VehicleRequest vehicleRequest = new VehicleRequest();
        vehicleRequest.setPlateNumber("RAA-203-A");
        vehicleRequest.setMake("Toyota");
        vehicleRequest.setModel("Corolla");
        vehicleRequest.setYear(2021);
        vehicleRequest.setColor("Silver");
        vehicleRequest.setCustomerId(customerId);

        MvcResult vehicleResult = mockMvc.perform(authorized(post("/api/vehicles"), bearerToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(vehicleRequest)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.plateNumber").value("RAA-203-A"))
                .andExpect(jsonPath("$.customerId").value(customerId))
                .andReturn();

        Long vehicleId = objectMapper.readTree(vehicleResult.getResponse().getContentAsString())
                .get("id")
                .asLong();

        RepairJobRequest repairRequest = new RepairJobRequest();
        repairRequest.setComplaint("Engine warning light keeps flashing");
        repairRequest.setDiagnosis("Loose ignition coil connection");
        repairRequest.setRepairDescription("Inspect and replace coil connector");
        repairRequest.setExpectedCompletionDate(LocalDateTime.now().plusDays(2));
        repairRequest.setCost(new BigDecimal("180.50"));
        repairRequest.setStatus(RepairJobStatus.PENDING);
        repairRequest.setVehicleId(vehicleId);

        MvcResult repairJobResult = mockMvc.perform(authorized(post("/api/repair-jobs"), bearerToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(repairRequest)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.complaint").value("Engine warning light keeps flashing"))
                .andExpect(jsonPath("$.status").value("PENDING"))
                .andExpect(jsonPath("$.vehicleId").value(vehicleId))
                .andReturn();
        Long repairJobId = objectMapper.readTree(repairJobResult.getResponse().getContentAsString())
                .get("id").asLong();

        mockMvc.perform(authorized(get("/api/customers/" + customerId), bearerToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value("alice@example.com"));

        mockMvc.perform(authorized(get("/api/vehicles/" + vehicleId), bearerToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.model").value("Corolla"));

        MechanicRequest mechanicRequest = new MechanicRequest();
        mechanicRequest.setFullName("Jean Mugisha");
        mechanicRequest.setPhone("+250788555111");
        mechanicRequest.setEmail("jean.mechanic@example.com");
        mechanicRequest.setSpecialization("Engine repair");
        MvcResult mechanicResult = mockMvc.perform(authorized(post("/api/mechanics"), bearerToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(mechanicRequest)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.active").value(true))
                .andReturn();
        Long mechanicId = objectMapper.readTree(mechanicResult.getResponse().getContentAsString())
                .get("id").asLong();

        mockMvc.perform(authorized(post("/api/repair-jobs/" + repairJobId + "/mechanics/" + mechanicId), bearerToken))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.mechanicId").value(mechanicId));

        SparePartRequest partRequest = new SparePartRequest();
        partRequest.setSku("OIL-FILTER-001");
        partRequest.setName("Oil filter");
        partRequest.setUnitPrice(new BigDecimal("25.00"));
        partRequest.setStockQuantity(8);
        partRequest.setReorderLevel(2);
        MvcResult partResult = mockMvc.perform(authorized(post("/api/spare-parts"), bearerToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(partRequest)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.lowStock").value(false))
                .andReturn();
        Long partId = objectMapper.readTree(partResult.getResponse().getContentAsString())
                .get("id").asLong();

        RepairJobPartRequest usageRequest = new RepairJobPartRequest();
        usageRequest.setSparePartId(partId);
        usageRequest.setQuantity(2);
        mockMvc.perform(authorized(post("/api/repair-jobs/" + repairJobId + "/parts"), bearerToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(usageRequest)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.lineTotal").value(50.0));
        mockMvc.perform(authorized(get("/api/spare-parts/" + partId), bearerToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.stockQuantity").value(6));

        repairRequest.setStatus(RepairJobStatus.COMPLETED);
        mockMvc.perform(authorized(org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put(
                                "/api/repair-jobs/" + repairJobId), bearerToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(repairRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("COMPLETED"));

        MvcResult invoiceResult = mockMvc.perform(authorized(post("/api/repair-jobs/" + repairJobId + "/invoice"), bearerToken))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.laborAmount").value(180.5))
                .andExpect(jsonPath("$.partsAmount").value(50.0))
                .andExpect(jsonPath("$.totalAmount").value(230.5))
                .andReturn();
        Long invoiceId = objectMapper.readTree(invoiceResult.getResponse().getContentAsString())
                .get("id").asLong();

        PaymentRequest paymentRequest = new PaymentRequest();
        paymentRequest.setAmount(new BigDecimal("100.00"));
        paymentRequest.setPaymentMethod("CARD");
        paymentRequest.setReference("TXN-100");
        mockMvc.perform(authorized(post("/api/invoices/" + invoiceId + "/payments"), bearerToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(paymentRequest)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.status").value("PARTIALLY_PAID"))
                .andExpect(jsonPath("$.balanceDue").value(130.5));

        paymentRequest.setAmount(new BigDecimal("130.50"));
        paymentRequest.setReference("TXN-230");
        mockMvc.perform(authorized(post("/api/invoices/" + invoiceId + "/payments"), bearerToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(paymentRequest)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.status").value("PAID"))
                .andExpect(jsonPath("$.balanceDue").value(0.0))
                .andExpect(jsonPath("$.payments.length()").value(2));

        mockMvc.perform(authorized(org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put(
                                "/api/repair-jobs/" + repairJobId), bearerToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(repairRequest)))
                .andExpect(status().isConflict());
    }

    private String registerAdminToken() throws Exception {
        String password = "TestAdminPassword123!";
        Garage garage = garageRepository.save(new Garage("Test Garage"));
        appUserRepository.save(new AppUser(
                "garageadmin",
                "garageadmin@example.com",
                passwordEncoder.encode(password),
                Set.of(Role.GARAGE_ADMIN),
                garage
        ));

        MvcResult result = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new AuthRequest("garageadmin", password))))
                .andExpect(status().isOk())
                .andReturn();

        return objectMapper.readTree(result.getResponse().getContentAsString())
                .get("token")
                .asText();
    }

    private MockHttpServletRequestBuilder authorized(MockHttpServletRequestBuilder builder, String bearerToken) {
        return builder.header("Authorization", "Bearer " + bearerToken);
    }
}
