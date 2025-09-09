package com.example.carins.web;

import com.example.carins.model.Car;
import com.example.carins.model.InsurancePolicy;
import com.example.carins.repo.CarRepository;
import com.example.carins.repo.InsurancePolicyRepository;
import com.example.carins.web.dto.InsurancePolicyDto;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

@RestController
@RequestMapping("/api")
public class InsurancePolicyController {

    private final InsurancePolicyRepository policyRepository;
    private final CarRepository carRepository;

    public InsurancePolicyController(InsurancePolicyRepository policyRepository, CarRepository carRepository) {
        this.policyRepository = policyRepository;
        this.carRepository = carRepository;
    }

    @PostMapping("/policies")
    public ResponseEntity<InsurancePolicyDto> createPolicy(@Valid @RequestBody InsurancePolicyDto policyDto) {
        Optional<Car> carOpt = carRepository.findById(policyDto.carId());
        if (carOpt.isEmpty()) {
            return ResponseEntity.badRequest().build();
        }
        Car car = carOpt.get();

        InsurancePolicy policy = new InsurancePolicy(car, policyDto.provider(), policyDto.startDate(), policyDto.endDate());
        InsurancePolicy saved = policyRepository.save(policy);
        // return DTO to avoid serialization of entity graph
        return ResponseEntity.ok(InsurancePolicyDto.fromEntity(saved));
    }

    @PutMapping("/policies/{id}")
    public ResponseEntity<InsurancePolicyDto> updatePolicy(@PathVariable Long id, @Valid @RequestBody InsurancePolicyDto policyDto) {
        Optional<InsurancePolicy> existingOpt = policyRepository.findById(id);
        if (existingOpt.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        Optional<Car> carOpt = carRepository.findById(policyDto.carId());
        if (carOpt.isEmpty()) {
            return ResponseEntity.badRequest().build();
        }
        Car car = carOpt.get();

        InsurancePolicy existing = existingOpt.get();
        existing.setCar(car);
        existing.setProvider(policyDto.provider());
        existing.setStartDate(policyDto.startDate());
        existing.setEndDate(policyDto.endDate());
        InsurancePolicy saved = policyRepository.save(existing);
        return ResponseEntity.ok(InsurancePolicyDto.fromEntity(saved));
    }
}