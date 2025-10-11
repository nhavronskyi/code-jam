package com.team.codejam.mapper;

import com.team.codejam.dto.FuelEntryResponseDto;
import com.team.codejam.entity.FuelEntry;

public class FuelEntryMapper {
    public static FuelEntryResponseDto toResponseDto(FuelEntry curr, FuelEntry prev, boolean imperialUnits) {
        FuelEntryResponseDto dto = new FuelEntryResponseDto();
        dto.setId(curr.getId());
        dto.setVehicleId(curr.getVehicle().getId());
        dto.setDate(curr.getDate());
        dto.setOdometer(curr.getOdometer());
        dto.setStationName(curr.getStationName());
        dto.setFuelBrand(curr.getFuelBrand());
        dto.setFuelGrade(curr.getFuelGrade());
        dto.setLiters(curr.getLiters());
        dto.setTotalAmount(curr.getTotalAmount());
        dto.setNotes(curr.getNotes());
        Double distanceSinceLast = null;
        if (prev != null && curr.getOdometer() != null && prev.getOdometer() != null)
            distanceSinceLast = (double) (curr.getOdometer() - prev.getOdometer());
        dto.setDistanceSinceLast(distanceSinceLast != null ? (double) Math.round(distanceSinceLast) : null);
        Double unitPrice = (curr.getLiters() != null && curr.getLiters() > 0 && curr.getTotalAmount() != null) ? curr.getTotalAmount() / curr.getLiters() : null;
        dto.setUnitPrice(unitPrice != null ? round(unitPrice, 2) : null);
        Double costPerKm = (distanceSinceLast != null && distanceSinceLast > 0 && curr.getTotalAmount() != null) ? curr.getTotalAmount() / distanceSinceLast : null;
        dto.setCostPerKm(costPerKm != null ? round(costPerKm, 2) : null);
        Double efficiencyMetric = (curr.getLiters() != null && distanceSinceLast != null && distanceSinceLast > 0) ? (curr.getLiters() / distanceSinceLast) * 100 : null;
        dto.setEfficiencyMetric(efficiencyMetric != null ? round(efficiencyMetric, 1) : null);
        Double efficiencyImperial = null;
        if (imperialUnits && curr.getLiters() != null && distanceSinceLast != null && distanceSinceLast > 0) {
            double miles = distanceSinceLast * 0.621371;
            double gallons = curr.getLiters() * 0.264172;
            efficiencyImperial = gallons > 0 ? miles / gallons : null;
        }
        dto.setEfficiencyImperial(efficiencyImperial != null ? round(efficiencyImperial, 1) : null);
        return dto;
    }

    private static double round(double value, int places) {
        if (places < 0) throw new IllegalArgumentException();
        long factor = (long) Math.pow(10, places);
        value = value * factor;
        long tmp = Math.round(value);
        return (double) tmp / factor;
    }
}

