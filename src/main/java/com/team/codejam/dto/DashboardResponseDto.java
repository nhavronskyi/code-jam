package com.team.codejam.dto;

import lombok.Builder;
import lombok.Data;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class DashboardResponseDto {
    private double totalDistance;
    private double totalLiters;
    private double totalSpend;
    private Double avgCostPerLiter;
    private Double avgConsumption;
    private Double avgCostPerKm;
    private Double avgDistancePerDay;
    private List<ChartPointDto> costPerLiterData;
    private List<ChartPointDto> consumptionData;
    private Double avgConsumptionImperial;
}
