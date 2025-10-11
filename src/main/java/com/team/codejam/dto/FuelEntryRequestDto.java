package com.team.codejam.dto;

import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;

public class FuelEntryRequestDto {
    @NotNull
    private Long vehicleId;
    @NotNull
    private LocalDate date;
    @NotNull
    private Integer odometer;
    @NotNull
    private String stationName;
    private String fuelBrand;
    private String fuelGrade;
    @NotNull
    private Double liters;
    @NotNull
    private Double totalAmount;
    private String notes;

    public Long getVehicleId() { return vehicleId; }
    public void setVehicleId(Long vehicleId) { this.vehicleId = vehicleId; }
    public LocalDate getDate() { return date; }
    public void setDate(LocalDate date) { this.date = date; }
    public Integer getOdometer() { return odometer; }
    public void setOdometer(Integer odometer) { this.odometer = odometer; }
    public String getStationName() { return stationName; }
    public void setStationName(String stationName) { this.stationName = stationName; }
    public String getFuelBrand() { return fuelBrand; }
    public void setFuelBrand(String fuelBrand) { this.fuelBrand = fuelBrand; }
    public String getFuelGrade() { return fuelGrade; }
    public void setFuelGrade(String fuelGrade) { this.fuelGrade = fuelGrade; }
    public Double getLiters() { return liters; }
    public void setLiters(Double liters) { this.liters = liters; }
    public Double getTotalAmount() { return totalAmount; }
    public void setTotalAmount(Double totalAmount) { this.totalAmount = totalAmount; }
    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }
}
