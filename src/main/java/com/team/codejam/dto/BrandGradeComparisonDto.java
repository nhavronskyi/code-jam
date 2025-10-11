package com.team.codejam.dto;

import lombok.Builder;
import lombok.Data;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class BrandGradeComparisonDto {
    private String brand;
    private String grade;
    private Double avgCostPerLiter;
    private Double avgConsumption;
    private int fillUpCount;
}

