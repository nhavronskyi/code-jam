package com.team.codejam.service;

import com.team.codejam.dto.ChartPointDto;
import com.team.codejam.dto.DashboardResponseDto;
import com.team.codejam.dto.BrandGradeComparisonDto;
import com.team.codejam.dto.FuelEntryPerFillDto;
import com.team.codejam.dto.FuelEntryResponseDto;
import com.team.codejam.entity.FuelEntry;
import com.team.codejam.repository.FuelEntryRepository;
import com.team.codejam.specification.FuelEntrySpecification;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class FuelEntryService {

    private final FuelEntryRepository fuelEntryRepository;

    public void deleteEntry(Long entryId) {
        fuelEntryRepository.deleteById(entryId);
    }

    public Page<FuelEntry> getFilteredEntries(Long userId, Long vehicleId, String brand, String grade, String station, LocalDate startDate, LocalDate endDate, int page) {
        Pageable pageable = PageRequest.of(page, 25, Sort.by("date").ascending());
        System.out.println("fetching filtered entries " + startDate + " | " + endDate + " | ");
        return fuelEntryRepository.findAll(
                FuelEntrySpecification.filter(vehicleId, brand, grade, station, startDate, endDate, userId),
                pageable
        );
    }

    public FuelEntry addFuelEntry(FuelEntry entry) {
        validateEntry(entry);
        // Odometer integrity: must be greater than previous entry for the same vehicle
        List<FuelEntry> entries = fuelEntryRepository.findByVehicleIdOrderByDateDesc(entry.getVehicle().getId());
        if (!entries.isEmpty()) {
            FuelEntry latest = entries.getFirst();
            if (entry.getDate().isAfter(latest.getDate()) || entry.getDate().isEqual(latest.getDate())) {
                if (entry.getOdometer() <= latest.getOdometer()) {
                    throw new IllegalArgumentException("Odometer must be greater than previous entry for this vehicle");
                }
            } else {
                // Inserted out of order: must check odometer against previous and next entries
                for (int i = 0; i < entries.size(); i++) {
                    FuelEntry e = entries.get(i);
                    if (e.getDate().isBefore(entry.getDate())) {
                        if (entry.getOdometer() <= e.getOdometer()) {
                            throw new IllegalArgumentException("Odometer must be greater than previous entry for this vehicle");
                        }
                        if (i > 0 && entry.getOdometer() >= entries.get(i - 1).getOdometer()) {
                            throw new IllegalArgumentException("Odometer must be less than next entry for this vehicle");
                        }
                        break;
                    }
                }
            }
        }
        return fuelEntryRepository.save(entry);
    }


    private void validateEntry(FuelEntry entry) {
        if (entry.getLiters() == null || entry.getLiters() <= 0) {
            throw new IllegalArgumentException("Liters must be positive");
        }
        if (entry.getTotalAmount() == null || entry.getTotalAmount() <= 0) {
            throw new IllegalArgumentException("Total amount must be positive");
        }
        if (entry.getOdometer() == null || entry.getOdometer() <= 0) {
            throw new IllegalArgumentException("Odometer must be positive");
        }
        if (entry.getDate() == null || entry.getDate().isAfter(LocalDate.now())) {
            throw new IllegalArgumentException("Date must not be in the future");
        }
    }

    public DashboardResponseDto getDashboardStats(Long userId, Long vehicleId, java.time.LocalDate startDate, java.time.LocalDate endDate, boolean imperialUnits) {
        List<FuelEntry> entries = fuelEntryRepository.findAll(
                FuelEntrySpecification.filter(vehicleId, null, null, null, startDate, endDate, userId),
                Sort.by("date").ascending()
        );
        if (entries.isEmpty()) {
            return DashboardResponseDto.builder()
                    .totalDistance(0)
                    .totalLiters(0)
                    .totalSpend(0)
                    .avgCostPerLiter(null)
                    .avgConsumption(null)
                    .avgConsumptionImperial(null)
                    .avgCostPerKm(null)
                    .avgDistancePerDay(null)
                    .costPerLiterData(List.of())
                    .consumptionData(List.of())
                    .build();
        }
        double totalLiters = calculateTotalLiters(entries);
        double totalSpend = calculateTotalSpend(entries);
        List<ChartPointDto> costPerLiterData = calculateCostPerLiterData(entries);
        ConsumptionResult consumptionResult = calculateConsumptionData(entries);
        Double avgCostPerLiter = totalLiters > 0 ? totalSpend / totalLiters : null;
        Double avgConsumption = consumptionResult.totalDistance > 0 ? (totalLiters / consumptionResult.totalDistance) * 100 : null;
        Double avgCostPerKm = consumptionResult.totalDistance > 0 ? totalSpend / consumptionResult.totalDistance : null;
        Double avgDistancePerDay = calculateAvgDistancePerDay(entries, consumptionResult.totalDistance);
        Double avgConsumptionImperial = null;
        if (imperialUnits && totalLiters > 0 && consumptionResult.totalDistance > 0) {
            double miles = consumptionResult.totalDistance * 0.621371;
            double gallons = totalLiters * 0.264172;
            avgConsumptionImperial = gallons > 0 ? miles / gallons : null;
            if (avgConsumptionImperial != null) avgConsumptionImperial = round(avgConsumptionImperial, 1);
        }
        return DashboardResponseDto.builder()
                .totalDistance(consumptionResult.totalDistance)
                .totalLiters(totalLiters)
                .totalSpend(totalSpend)
                .avgCostPerLiter(avgCostPerLiter)
                .avgConsumption(avgConsumption)
                .avgConsumptionImperial(avgConsumptionImperial)
                .avgCostPerKm(avgCostPerKm)
                .avgDistancePerDay(avgDistancePerDay)
                .costPerLiterData(costPerLiterData)
                .consumptionData(consumptionResult.consumptionData)
                .build();
    }

    private double calculateTotalLiters(List<FuelEntry> entries) {
        return entries.stream().mapToDouble(e -> e.getLiters() != null ? e.getLiters() : 0).sum();
    }

    private double calculateTotalSpend(List<FuelEntry> entries) {
        return entries.stream().mapToDouble(e -> e.getTotalAmount() != null ? e.getTotalAmount() : 0).sum();
    }

    private List<ChartPointDto> calculateCostPerLiterData(List<FuelEntry> entries) {
        return entries.stream()
                .map(e -> ChartPointDto.builder()
                        .date(e.getDate().toString())
                        .value((e.getLiters() != null && e.getLiters() > 0 && e.getTotalAmount() != null) ? e.getTotalAmount() / e.getLiters() : null)
                        .build())
                .toList();
    }

    private ConsumptionResult calculateConsumptionData(List<FuelEntry> entries) {
        double totalDistance = 0;
        List<ChartPointDto> consumptionData = new ArrayList<>();
        for (int i = 1; i < entries.size(); i++) {
            FuelEntry prev = entries.get(i - 1);
            FuelEntry curr = entries.get(i);
            double distance = curr.getOdometer() - prev.getOdometer();
            if (distance > 0) {
                totalDistance += distance;
                consumptionData.add(ChartPointDto.builder()
                        .date(curr.getDate().toString())
                        .value((curr.getLiters() != null) ? (curr.getLiters() / distance) * 100 : null)
                        .build());
            } else {
                consumptionData.add(ChartPointDto.builder().date(curr.getDate().toString()).value(null).build());
            }
        }
        return new ConsumptionResult(totalDistance, consumptionData);
    }

    private Double calculateAvgDistancePerDay(List<FuelEntry> entries, double totalDistance) {
        if (entries.size() > 1) {
            long days = ChronoUnit.DAYS.between(entries.getFirst().getDate(), entries.getLast().getDate());
            return days > 0 ? totalDistance / days : null;
        }
        return null;
    }

    public List<BrandGradeComparisonDto> getBrandGradeComparison(Long userId, Long vehicleId, java.time.LocalDate startDate, java.time.LocalDate endDate) {
        List<FuelEntry> entries = fuelEntryRepository.findAll(
            FuelEntrySpecification.filter(vehicleId, null, null, null, startDate, endDate, userId),
            Sort.by("date").ascending()
        );
        if (entries.isEmpty()) return List.of();
        // Group by brand and grade
        return entries.stream()
            .collect(java.util.stream.Collectors.groupingBy(e -> e.getFuelBrand() + "|" + e.getFuelGrade()))
            .entrySet().stream()
            .map(entry -> {
                List<FuelEntry> group = entry.getValue();
                String[] keys = entry.getKey().split("\\|");
                String brand = keys.length > 0 ? keys[0] : "";
                String grade = keys.length > 1 ? keys[1] : "";
                double totalLiters = group.stream().mapToDouble(e -> e.getLiters() != null ? e.getLiters() : 0).sum();
                double totalSpend = group.stream().mapToDouble(e -> e.getTotalAmount() != null ? e.getTotalAmount() : 0).sum();
                double totalDistance = 0;
                for (int i = 1; i < group.size(); i++) {
                    FuelEntry prev = group.get(i - 1);
                    FuelEntry curr = group.get(i);
                    double distance = curr.getOdometer() - prev.getOdometer();
                    if (distance > 0) totalDistance += distance;
                }
                Double avgCostPerLiter = totalLiters > 0 ? totalSpend / totalLiters : null;
                Double avgConsumption = totalDistance > 0 ? (totalLiters / totalDistance) * 100 : null;
                int fillUpCount = group.size();
                return BrandGradeComparisonDto.builder()
                    .brand(brand)
                    .grade(grade)
                    .avgCostPerLiter(avgCostPerLiter)
                    .avgConsumption(avgConsumption)
                    .fillUpCount(fillUpCount)
                    .build();
            })
            .toList();
    }

    public List<FuelEntryPerFillDto> getPerFillConsumption(Long userId, Long vehicleId, java.time.LocalDate startDate, java.time.LocalDate endDate) {
        List<FuelEntry> entries = fuelEntryRepository.findAll(
            FuelEntrySpecification.filter(vehicleId, null, null, null, startDate, endDate, userId),
            Sort.by("date").ascending()
        );
        List<FuelEntryPerFillDto> result = new ArrayList<>();
        for (int i = 1; i < entries.size(); i++) {
            FuelEntry prev = entries.get(i - 1);
            FuelEntry curr = entries.get(i);
            double distance = curr.getOdometer() - prev.getOdometer();
            Double consumption = (curr.getLiters() != null && distance > 0) ? (curr.getLiters() / distance) * 100 : null;
            result.add(FuelEntryPerFillDto.builder()
                .date(curr.getDate().toString())
                .consumptionLPer100km(consumption)
                .build());
        }
        return result;
    }

    public List<FuelEntryResponseDto> getPerFillMetricsForVehicle(Long userId, Long vehicleId, boolean imperialUnits) {
        List<FuelEntry> entries = fuelEntryRepository.findAll(
            FuelEntrySpecification.filter(vehicleId, null, null, null, null, null, userId),
            Sort.by("date").ascending()
        );
        List<FuelEntryResponseDto> result = new ArrayList<>();
        FuelEntry prev = null;
        for (FuelEntry curr : entries) {
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
            // Per-fill metrics
            Double distanceSinceLast = null;
            if (prev != null && curr.getOdometer() != null && prev.getOdometer() != null) {
                distanceSinceLast = (double) (curr.getOdometer() - prev.getOdometer());
            }
            dto.setDistanceSinceLast(distanceSinceLast != null ? Double.valueOf(Math.round(distanceSinceLast)) : null);
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
            result.add(dto);
            prev = curr;
        }
        return result;
    }
    private double round(double value, int places) {
        if (places < 0) throw new IllegalArgumentException();
        long factor = (long) Math.pow(10, places);
        value = value * factor;
        long tmp = Math.round(value);
        return (double) tmp / factor;
    }

    private static class ConsumptionResult {
        double totalDistance;
        List<ChartPointDto> consumptionData;

        ConsumptionResult(double totalDistance, List<ChartPointDto> consumptionData) {
            this.totalDistance = totalDistance;
            this.consumptionData = consumptionData;
        }
    }
}
