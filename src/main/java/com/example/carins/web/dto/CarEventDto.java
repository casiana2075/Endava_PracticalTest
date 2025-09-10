package com.example.carins.web.dto;

import com.fasterxml.jackson.annotation.JsonFormat;

import java.time.LocalDate;

public record CarEventDto(
        String eventType, // "POLICY" or "CLAIM"
        @JsonFormat(pattern = "yyyy-MM-dd") LocalDate eventDate,
        String description
) {}