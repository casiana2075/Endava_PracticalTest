package com.example.carins;

import com.example.carins.model.Car;
import com.example.carins.model.InsuranceClaim;
import com.example.carins.model.InsurancePolicy;
import com.example.carins.model.Owner;
import com.example.carins.repo.CarRepository;
import com.example.carins.repo.InsuranceClaimRepository;
import com.example.carins.repo.InsurancePolicyRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Collections;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
public class CarControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private CarRepository carRepository;

    @MockitoBean
    private InsurancePolicyRepository policyRepository;

    @MockitoBean
    private InsuranceClaimRepository claimRepository;

    @Test
    void registerClaim_withValidData_succeeds() throws Exception {
        // Setup Car with a mock ID using reflection
        Car car = new Car("VIN12345", "Dacia", "Logan", 2018, new Owner("Ana", "ana@example.com"));
        Field carIdField = Car.class.getDeclaredField("id");
        carIdField.setAccessible(true);
        carIdField.set(car, 1L);

        when(carRepository.findById(1L)).thenReturn(Optional.of(car));

        // Create input and saved claim instances
        InsuranceClaim claimInput = new InsuranceClaim(car, LocalDate.parse("2025-09-10"), "Collision", BigDecimal.valueOf(1000));
        InsuranceClaim claimSaved = new InsuranceClaim(car, LocalDate.parse("2025-09-10"), "Collision", BigDecimal.valueOf(1000));
        // Set ID for claimSaved using reflection to mimic DB generation
        Field claimIdField = InsuranceClaim.class.getDeclaredField("id");
        claimIdField.setAccessible(true);
        claimIdField.set(claimSaved, 1L);

        when(claimRepository.save(any(InsuranceClaim.class))).thenReturn(claimSaved);

        String json = """
                {
                    "carId": 1,
                    "claimDate": "2025-09-10",
                    "description": "Collision",
                    "amount": 1000.00
                }
                """;

        mockMvc.perform(post("/api/cars/1/claims")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.carId").value(1))
                .andExpect(jsonPath("$.claimDate").value("2025-09-10"))
                .andExpect(jsonPath("$.description").value("Collision"))
                .andExpect(jsonPath("$.amount").value(1000.00));
    }

    @Test
    void registerClaim_withInvalidCarId_returnsNotFound() throws Exception {
        when(carRepository.findById(999L)).thenReturn(Optional.empty());

        String json = """
                {
                    "carId": 999,
                    "claimDate": "2025-09-10",
                    "description": "Collision",
                    "amount": 1000.00
                }
                """;

        mockMvc.perform(post("/api/cars/999/claims")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isNotFound());
    }

    @Test
    void registerClaim_withMissingFields_returnsBadRequest() throws Exception {
        String json = """
                {
                    "carId": 1,
                    "claimDate": null,
                    "description": "",
                    "amount": -100
                }
                """;

        mockMvc.perform(post("/api/cars/1/claims")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.claimDate").value("must not be null"))
                .andExpect(jsonPath("$.description").value("must not be blank"))
                .andExpect(jsonPath("$.amount").value("must be greater than 0"));
    }

    @Test
    void getCarHistory_withValidCarId_succeeds() throws Exception {
        // Setup Car with a mock ID using reflection
        Car car = new Car("VIN12345", "Dacia", "Logan", 2018, new Owner("Ana", "ana@example.com"));
        Field carIdField = Car.class.getDeclaredField("id");
        carIdField.setAccessible(true);
        carIdField.set(car, 1L);

        when(carRepository.findById(1L)).thenReturn(Optional.of(car));

        // Setup Policy and Claim with simulated IDs using reflection
        InsurancePolicy policy = new InsurancePolicy(car, "Allianz", LocalDate.parse("2024-01-01"), LocalDate.parse("2024-12-31"));
        Field policyIdField = InsurancePolicy.class.getDeclaredField("id");
        policyIdField.setAccessible(true);
        policyIdField.set(policy, 1L);

        InsuranceClaim claim = new InsuranceClaim(car, LocalDate.parse("2024-06-15"), "Collision", BigDecimal.valueOf(1500));
        Field claimIdField = InsuranceClaim.class.getDeclaredField("id");
        claimIdField.setAccessible(true);
        claimIdField.set(claim, 1L);

        when(policyRepository.findByCarId(1L)).thenReturn(Collections.singletonList(policy));
        when(claimRepository.findByCarIdOrderByClaimDateAsc(1L)).thenReturn(Collections.singletonList(claim));

        mockMvc.perform(get("/api/cars/1/history"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].eventType").value("POLICY"))
                .andExpect(jsonPath("$[0].eventDate").value("2024-01-01"))
                .andExpect(jsonPath("$[0].description").value("Policy 1 (Allianz) from 2024-01-01 to 2024-12-31"))
                .andExpect(jsonPath("$[1].eventType").value("CLAIM"))
                .andExpect(jsonPath("$[1].eventDate").value("2024-06-15"))
                .andExpect(jsonPath("$[1].description").value("Claim 1: Collision ($1500)"));
    }

    @Test
    void getCarHistory_withInvalidCarId_returnsNotFound() throws Exception {
        when(carRepository.findById(999L)).thenReturn(Optional.empty());

        mockMvc.perform(get("/api/cars/999/history"))
                .andExpect(status().isNotFound());
    }

    //task 3 tests
    @Test
    void isInsuranceValid_withValidData_succeeds() throws Exception {
        Car car = new Car("VIN12345", "Dacia", "Logan", 2018, new Owner("Ana", "ana@example.com"));
        Field carIdField = Car.class.getDeclaredField("id");
        carIdField.setAccessible(true);
        carIdField.set(car, 1L);

        when(carRepository.findById(1L)).thenReturn(Optional.of(car));
        when(policyRepository.existsActiveOnDate(1L, LocalDate.parse("2025-09-10"))).thenReturn(true);

        mockMvc.perform(get("/api/cars/1/insurance-valid?date=2025-09-10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.carId").value(1))
                .andExpect(jsonPath("$.date").value("2025-09-10"))
                .andExpect(jsonPath("$.valid").value(true));
    }

    @Test
    void isInsuranceValid_withInvalidCarId_returnsNotFound() throws Exception {
        when(carRepository.findById(999L)).thenReturn(Optional.empty());

        mockMvc.perform(get("/api/cars/999/insurance-valid?date=2025-09-10"))
                .andExpect(status().isNotFound());
    }

    @Test
    void isInsuranceValid_withInvalidDateFormat_returnsBadRequest() throws Exception {
        mockMvc.perform(get("/api/cars/1/insurance-valid?date=09-10-2025"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.date").value("Invalid date format. Must be YYYY-MM-DD."));
    }

    @Test
    void isInsuranceValid_withOutOfRangeDate_returnsBadRequest() throws Exception {
        Car car = new Car("VIN12345", "Dacia", "Logan", 2018, new Owner("Ana", "ana@example.com"));
        Field carIdField = Car.class.getDeclaredField("id");
        carIdField.setAccessible(true);
        carIdField.set(car, 1L);

        when(carRepository.findById(1L)).thenReturn(Optional.of(car));

        mockMvc.perform(get("/api/cars/1/insurance-valid?date=2125-09-10"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.date").value("must be between 1900-01-01 and 2100-12-31"));
    }
}