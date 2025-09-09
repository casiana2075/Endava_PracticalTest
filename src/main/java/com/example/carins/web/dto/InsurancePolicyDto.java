// In InsurancePolicyDto.java (add for response projection)
package com.example.carins.web.dto;

import com.example.carins.model.InsurancePolicy;
import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;

public record InsurancePolicyDto(
        @NotNull Long carId,  // for requests
        Long id,              // for responses (null on create)
        @NotBlank String provider,
        @NotNull LocalDate startDate,
        @NotNull LocalDate endDate
) {
    // Factory method to create from entity
    public static InsurancePolicyDto fromEntity(InsurancePolicy policy) {
        return new InsurancePolicyDto(
                policy.getCar().getId(),
                policy.getId(),
                policy.getProvider(),
                policy.getStartDate(),
                policy.getEndDate()
        );
    }
}