package com.example.carins;

import com.example.carins.model.Car;
import com.example.carins.model.InsurancePolicy;
import com.example.carins.model.Owner;
import com.example.carins.repo.CarRepository;
import com.example.carins.repo.InsurancePolicyRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.lang.reflect.Field;
import java.time.LocalDate;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
public class InsurancePolicyControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private InsurancePolicyRepository policyRepository;

    @MockitoBean
    private CarRepository carRepository;

    //task 1 tests
    @Test
    void createPolicy_withoutEndDate_returnsBadRequest() throws Exception {
        when(carRepository.findById(1L)).thenReturn(Optional.of(new Car()));

        String json = """
                {
                    "carId": 1,
                    "provider": "Allianz",
                    "startDate": "2025-01-01",
                    "endDate": null
                }
                """;

        mockMvc.perform(post("/api/policies")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.endDate").value("must not be null"));
    }

    @Test
    void createPolicy_withValidData_succeeds() throws Exception {
        Car car = new Car("VIN12345", "Dacia", "Logan", 2018, new Owner("Ana", "ana@example.com"));
        // Set car ID using reflection to mimic DB-generated ID
        Field carIdField = Car.class.getDeclaredField("id");
        carIdField.setAccessible(true);
        carIdField.set(car, 1L);

        InsurancePolicy policy = new InsurancePolicy(car, "Allianz", LocalDate.parse("2025-01-01"), LocalDate.parse("2026-01-01"));
        // Set policy ID using reflection
        Field policyIdField = InsurancePolicy.class.getDeclaredField("id");
        policyIdField.setAccessible(true);
        policyIdField.set(policy, 1L);

        when(carRepository.findById(1L)).thenReturn(Optional.of(car));
        when(policyRepository.save(any(InsurancePolicy.class))).thenReturn(policy);

        String json = """
                {
                    "carId": 1,
                    "provider": "Allianz",
                    "startDate": "2025-01-01",
                    "endDate": "2026-01-01"
                }
                """;

        mockMvc.perform(post("/api/policies")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.carId").value(1))
                .andExpect(jsonPath("$.provider").value("Allianz"))
                .andExpect(jsonPath("$.startDate").value("2025-01-01"))
                .andExpect(jsonPath("$.endDate").value("2026-01-01"));
    }

    @Test
    void updatePolicy_withoutEndDate_returnsBadRequest() throws Exception {
        Car car = new Car("VIN12345", "Dacia", "Logan", 2018, new Owner("Ana", "ana@example.com"));
        Field carIdField = Car.class.getDeclaredField("id");
        carIdField.setAccessible(true);
        carIdField.set(car, 1L);

        InsurancePolicy existing = new InsurancePolicy(car, "Allianz", LocalDate.parse("2025-01-01"), LocalDate.parse("2026-01-01"));
        Field policyIdField = InsurancePolicy.class.getDeclaredField("id");
        policyIdField.setAccessible(true);
        policyIdField.set(existing, 1L);

        when(policyRepository.findById(1L)).thenReturn(Optional.of(existing));
        when(carRepository.findById(1L)).thenReturn(Optional.of(car));

        String json = """
                {
                    "carId": 1,
                    "provider": "Allianz",
                    "startDate": "2025-01-01",
                    "endDate": null
                }
                """;

        mockMvc.perform(put("/api/policies/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.endDate").value("must not be null"));
    }

    @Test
    void updatePolicy_withValidData_succeeds() throws Exception {
        Car car = new Car("VIN12345", "Dacia", "Logan", 2018, new Owner("Ana", "ana@example.com"));
        Field carIdField = Car.class.getDeclaredField("id");
        carIdField.setAccessible(true);
        carIdField.set(car, 1L);

        InsurancePolicy existing = new InsurancePolicy(car, "Allianz", LocalDate.parse("2025-01-01"), LocalDate.parse("2026-01-01"));
        Field policyIdField = InsurancePolicy.class.getDeclaredField("id");
        policyIdField.setAccessible(true);
        policyIdField.set(existing, 1L);

        InsurancePolicy updated = new InsurancePolicy(car, "Groupama", LocalDate.parse("2025-02-01"), LocalDate.parse("2026-02-01"));
        policyIdField.set(updated, 1L); // Same ID as existing

        when(policyRepository.findById(1L)).thenReturn(Optional.of(existing));
        when(carRepository.findById(1L)).thenReturn(Optional.of(car));
        when(policyRepository.save(any(InsurancePolicy.class))).thenReturn(updated);

        String json = """
                {
                    "carId": 1,
                    "provider": "Groupama",
                    "startDate": "2025-02-01",
                    "endDate": "2026-02-01"
                }
                """;

        mockMvc.perform(put("/api/policies/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.carId").value(1))
                .andExpect(jsonPath("$.provider").value("Groupama"))
                .andExpect(jsonPath("$.startDate").value("2025-02-01"))
                .andExpect(jsonPath("$.endDate").value("2026-02-01"));
    }
}