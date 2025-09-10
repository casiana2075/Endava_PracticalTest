package com.example.carins.web;

import com.example.carins.model.Car;
import com.example.carins.model.InsuranceClaim;
import com.example.carins.model.InsurancePolicy;
import com.example.carins.repo.CarRepository;
import com.example.carins.repo.InsuranceClaimRepository;
import com.example.carins.repo.InsurancePolicyRepository;
import com.example.carins.service.CarService;
import com.example.carins.web.dto.CarDto;
import com.example.carins.web.dto.InsuranceClaimDto;
import com.example.carins.web.dto.InsurancePolicyDto;
import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.Valid;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api")
public class CarController {

    private final CarService service;
    private final CarRepository carRepository;
    private final InsurancePolicyRepository policyRepository;
    private final InsuranceClaimRepository claimRepository;

    public CarController(CarService service, CarRepository carRepository,
                         InsurancePolicyRepository policyRepository, InsuranceClaimRepository claimRepository) {
        this.service = service;
        this.carRepository = carRepository;
        this.policyRepository = policyRepository;
        this.claimRepository = claimRepository;
    }

    @GetMapping("/cars")
    public ResponseEntity<List<CarDto>> getCars() {
        return ResponseEntity.ok(service.listCars().stream().map(this::toDto).toList());
    }

    @GetMapping("/cars/{carId}/insurance-valid")
    public ResponseEntity<?> isInsuranceValid(
            @PathVariable Long carId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        Optional<Car> carOpt = carRepository.findById(carId);
        if (carOpt.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        boolean valid = service.isInsuranceValid(carId, date);
        return ResponseEntity.ok(new InsuranceValidityResponse(carId, date.toString(), valid));
    }

    @PostMapping("/cars/{carId}/claims")
    public ResponseEntity<InsuranceClaimDto> registerClaim(
            @PathVariable Long carId,
            @Valid @RequestBody InsuranceClaimDto claimDto) {
        if (!carId.equals(claimDto.carId())) {
            return ResponseEntity.badRequest().body(null); // 400: Mismatched carId
        }
        Optional<Car> carOpt = carRepository.findById(carId);
        if (carOpt.isEmpty()) {
            return ResponseEntity.notFound().build(); // 404: Car not found
        }
        Car car = carOpt.get();
        InsuranceClaim claim = new InsuranceClaim(car, claimDto.claimDate(), claimDto.description(), claimDto.amount());
        InsuranceClaim saved = claimRepository.save(claim);
        return ResponseEntity.created(URI.create("/api/cars/" + carId + "/claims/" + saved.getId()))
                .body(InsuranceClaimDto.fromEntity(saved));
    }

    @GetMapping("/cars/{carId}/history")
    public ResponseEntity<List<CarEventDto>> getCarHistory(@PathVariable Long carId) {
        Optional<Car> carOpt = carRepository.findById(carId);
        if (carOpt.isEmpty()) {
            return ResponseEntity.notFound().build(); // 404: Car not found
        }
        List<CarEventDto> events = new ArrayList<>();
        // Fetch policies
        List<InsurancePolicy> policies = policyRepository.findByCarId(carId);
        policies.forEach(policy -> events.add(new CarEventDto(
                "POLICY",
                policy.getStartDate(),
                "Policy " + policy.getId() + " (" + policy.getProvider() + ") from " + policy.getStartDate() + " to " + policy.getEndDate()
        )));
        // Fetch claims
        List<InsuranceClaim> claims = claimRepository.findByCarIdOrderByClaimDateAsc(carId);
        claims.forEach(claim -> events.add(new CarEventDto(
                "CLAIM",
                claim.getClaimDate(),
                "Claim " + claim.getId() + ": " + claim.getDescription() + " ($" + claim.getAmount() + ")"
        )));
        // Sort by event date
        events.sort((e1, e2) -> e1.eventDate().compareTo(e2.eventDate()));
        return ResponseEntity.ok(events);
    }

    private CarDto toDto(Car c) {
        var o = c.getOwner();
        return new CarDto(c.getId(), c.getVin(), c.getMake(), c.getModel(), c.getYearOfManufacture(),
                o != null ? o.getId() : null,
                o != null ? o.getName() : null,
                o != null ? o.getEmail() : null);
    }

    public record InsuranceValidityResponse(Long carId, String date, boolean valid) {}
    public record CarEventDto(String eventType, @JsonFormat(pattern = "yyyy-MM-dd") LocalDate eventDate, String description) {}
}