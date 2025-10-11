package com.team.codejam.dto;

import lombok.Builder;
import lombok.Data;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class FuelEntryPerFillDto {
    private String date;
    private Double consumptionLPer100km;
}

