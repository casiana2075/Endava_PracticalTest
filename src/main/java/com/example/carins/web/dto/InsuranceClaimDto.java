package com.example.carins.web.dto;

import com.example.carins.model.InsuranceClaim;
import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;
import java.time.LocalDate;

public record InsuranceClaimDto(
        @NotNull Long carId,
        Long id, // Null for create, populated for responses
        @NotNull @JsonFormat(pattern = "yyyy-MM-dd") LocalDate claimDate,
        @NotBlank String description,
        @NotNull @Positive BigDecimal amount
) {
    public static InsuranceClaimDto fromEntity(InsuranceClaim claim) {
        return new InsuranceClaimDto(
                claim.getCar().getId(),
                claim.getId(),
                claim.getClaimDate(),
                claim.getDescription(),
                claim.getAmount()
        );
    }
}