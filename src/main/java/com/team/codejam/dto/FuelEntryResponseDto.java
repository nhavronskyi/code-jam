package com.team.codejam.dto;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

@Setter
@Getter
public class FuelEntryResponseDto {
    private Long id;
    private Long vehicleId;
    private LocalDate date;
    private Integer odometer;
    private String stationName;
    private String fuelBrand;
    private String fuelGrade;
    private Double liters;
    private Double totalAmount;
    private String notes;
    private Double distanceSinceLast;
    private Double unitPrice;
    private Double costPerKm;
    private Double efficiencyMetric; // L/100km
    private Double efficiencyImperial; // MPG
}
