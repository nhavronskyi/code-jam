package com.team.codejam.dto;

import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class VehicleDto {
    private Long id;
    private String name;
    private String make;
    private String model;
    private Integer year;
    private String fuelType;
}
