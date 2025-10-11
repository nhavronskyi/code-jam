package com.team.codejam.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;

public class VehicleRequestDto {
    @NotBlank
    private String name;
    @NotBlank
    private String make;
    @NotBlank
    private String model;
    @NotNull
    private Integer year;
    @NotBlank
    @Pattern(regexp = "(?i)lpg|diesel|petrol", message = "fuelType must be one of: lpg, diesel, petrol")
    private String fuelType;

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getMake() { return make; }
    public void setMake(String make) { this.make = make; }
    public String getModel() { return model; }
    public void setModel(String model) { this.model = model; }
    public Integer getYear() { return year; }
    public void setYear(Integer year) { this.year = year; }
    public String getFuelType() { return fuelType; }
    public void setFuelType(String fuelType) { this.fuelType = fuelType; }
}
