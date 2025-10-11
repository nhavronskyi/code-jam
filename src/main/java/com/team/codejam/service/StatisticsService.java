package com.team.codejam.service;

import com.team.codejam.entity.FuelEntry;
import com.team.codejam.entity.Vehicle;
import com.team.codejam.repository.FuelEntryRepository;
import com.team.codejam.repository.VehicleRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class StatisticsService {

    private final FuelEntryRepository fuelEntryRepository;
    private final VehicleRepository vehicleRepository;

    // --- Per-fill metrics ---
    public List<Map<String, Object>> getPerFillMetrics(Long userId, Long vehicleId) {
        List<FuelEntry> entries = fuelEntryRepository.findByVehicleUserIdAndVehicleIdOrderByDateDesc(userId, vehicleId);
        List<Map<String, Object>> metrics = new ArrayList<>();
        FuelEntry prev = null;
        for (FuelEntry entry : entries) {
            metrics.add(mapFuelEntryToMap(entry, prev));
            prev = entry;
        }
        return metrics;
    }

    // --- Rolling and all-time aggregates ---
    public Map<String, Object> getAggregates(Long userId, Long vehicleId, LocalDate from, LocalDate to) {
        List<FuelEntry> entries = fuelEntryRepository.findByVehicleUserIdAndVehicleIdAndDateBetweenOrderByDateDesc(userId, vehicleId, from, to);
        return calculateAggregates(entries);
    }

    // --- Per-brand and per-grade comparisons ---
    public List<Map<String, Object>> getBrandGradeStats(Long userId, Long vehicleId) {
        List<FuelEntry> entries = fuelEntryRepository.findByVehicleUserIdAndVehicleIdOrderByDateDesc(userId, vehicleId);
        Map<String, List<FuelEntry>> byBrand = entries.stream().collect(Collectors.groupingBy(FuelEntry::getFuelBrand));
        List<Map<String, Object>> stats = new ArrayList<>();
        for (String brand : byBrand.keySet()) {
            List<FuelEntry> brandEntries = byBrand.get(brand);
            double avgCostPerLiter = brandEntries.stream().mapToDouble(FuelEntry::getTotalAmount).sum() /
                    brandEntries.stream().mapToDouble(FuelEntry::getLiters).sum();
            double avgConsumption = calculateAvgConsumption(brandEntries);
            Map<String, Object> stat = mapFuelEntryToMap(brandEntries.get(0), null);
            stat.put("fuelBrand", brand);
            stat.put("avgCostPerLiter", avgCostPerLiter);
            stat.put("avgConsumptionLPer100km", avgConsumption);
            stat.put("numFillUps", brandEntries.size());
            stat.put("totalAmount", brandEntries.stream().mapToDouble(FuelEntry::getTotalAmount).sum());
            stats.add(stat);
        }
        return stats;
    }

    // --- Support for metric/imperial conversions ---
    public static double litersToGallons(double liters) {
        return liters / 3.78541;
    }
    public static double kmToMiles(double km) {
        return km / 1.60934;
    }
    public static double consumptionToMPG(double liters, double km) {
        double miles = kmToMiles(km);
        double gallons = litersToGallons(liters);
        return gallons > 0 ? miles / gallons : 0;
    }
    // --- Rounding helpers ---
    public static double round(double value, int places) {
        if (places < 0) throw new IllegalArgumentException();
        long factor = (long) Math.pow(10, places);
        value = value * factor;
        long tmp = Math.round(value);
        return (double) tmp / factor;
    }

    // --- Aggregates across all vehicles ---
    public Map<String, Object> getUserAggregates(Long userId, LocalDate from, LocalDate to) {
        List<FuelEntry> entries = fuelEntryRepository.findByVehicleUserIdAndDateBetween(userId, from, to);
        return calculateAggregates(entries);
    }

    // --- Time-based statistics (monthly/yearly) ---
    public Map<String, Map<String, Object>> getMonthlyStats(Long userId, Long vehicleId, int year) {
        List<FuelEntry> entries = fuelEntryRepository.findByVehicleUserIdAndVehicleIdOrderByDateDesc(userId, vehicleId);
        Map<String, List<FuelEntry>> byMonth = entries.stream().filter(e -> e.getDate().getYear() == year)
                .collect(Collectors.groupingBy(e -> String.format("%02d", e.getDate().getMonthValue())));
        Map<String, Map<String, Object>> stats = new TreeMap<>();
        for (String month : byMonth.keySet()) {
            List<FuelEntry> monthEntries = byMonth.get(month);
            stats.put(month, calculateAggregates(monthEntries));
        }
        return stats;
    }

    // --- Statistics by fuel grade/type ---
    public List<Map<String, Object>> getGradeStats(Long userId, Long vehicleId) {
        List<FuelEntry> entries = fuelEntryRepository.findByVehicleUserIdAndVehicleIdOrderByDateDesc(userId, vehicleId);
        Map<String, List<FuelEntry>> byGrade = entries.stream().collect(Collectors.groupingBy(FuelEntry::getFuelGrade));
        List<Map<String, Object>> stats = new ArrayList<>();
        for (String grade : byGrade.keySet()) {
            List<FuelEntry> gradeEntries = byGrade.get(grade);
            double avgCostPerLiter = gradeEntries.stream().mapToDouble(FuelEntry::getTotalAmount).sum() /
                    gradeEntries.stream().mapToDouble(FuelEntry::getLiters).sum();
            double avgConsumption = calculateAvgConsumption(gradeEntries);
            Map<String, Object> stat = mapFuelEntryToMap(gradeEntries.get(0), null);
            stat.put("fuelGrade", grade);
            stat.put("avgCostPerLiter", avgCostPerLiter);
            stat.put("avgConsumptionLPer100km", avgConsumption);
            stat.put("numFillUps", gradeEntries.size());
            stat.put("totalAmount", gradeEntries.stream().mapToDouble(FuelEntry::getTotalAmount).sum());
            stats.add(stat);
        }
        return stats;
    }

    // --- Best/worst fill-ups ---
    public Map<String, Object> getBestWorstFillUps(Long userId, Long vehicleId) {
        List<FuelEntry> entries = fuelEntryRepository.findByVehicleUserIdAndVehicleIdOrderByDateDesc(userId, vehicleId);
        FuelEntry best = null, worst = null;
        double bestEfficiency = Double.MAX_VALUE, worstEfficiency = Double.MIN_VALUE;
        FuelEntry prev = null;
        for (FuelEntry entry : entries) {
            if (prev != null) {
                int distance = entry.getOdometer() - prev.getOdometer();
                double efficiency = distance > 0 ? (entry.getLiters() / distance) * 100 : Double.MAX_VALUE;
                if (efficiency < bestEfficiency) {
                    bestEfficiency = efficiency;
                    best = entry;
                }
                if (efficiency > worstEfficiency) {
                    worstEfficiency = efficiency;
                    worst = entry;
                }
            }
            prev = entry;
        }
        Map<String, Object> result = new HashMap<>();
        result.put("bestFillUp", best);
        result.put("worstFillUp", worst);
        return result;
    }

    // --- Most/least efficient vehicles ---
    public Map<String, Object> getMostLeastEfficientVehicles(Long userId) {
        List<Vehicle> vehicles = vehicleRepository.findByUserId(userId);
        Vehicle mostEff = null, leastEff = null;
        double bestEff = Double.MAX_VALUE, worstEff = Double.MIN_VALUE;
        for (Vehicle v : vehicles) {
            List<FuelEntry> entries = fuelEntryRepository.findByVehicleUserIdAndVehicleIdOrderByDateDesc(userId, v.getId());
            FuelEntry prev = null;
            List<Double> effs = new ArrayList<>();
            for (FuelEntry entry : entries) {
                if (prev != null) {
                    int distance = entry.getOdometer() - prev.getOdometer();
                    if (distance > 0) {
                        effs.add((entry.getLiters() / distance) * 100);
                    }
                }
                prev = entry;
            }
            double avgEff = effs.stream().mapToDouble(Double::doubleValue).average().orElse(Double.MAX_VALUE);
            if (avgEff < bestEff) {
                bestEff = avgEff;
                mostEff = v;
            }
            if (avgEff > worstEff && avgEff < Double.MAX_VALUE) {
                worstEff = avgEff;
                leastEff = v;
            }
        }
        Map<String, Object> result = new HashMap<>();
        result.put("mostEfficientVehicle", mostEff);
        result.put("leastEfficientVehicle", leastEff);
        return result;
    }

    // --- Dashboard summary ---
    public Map<String, Object> getDashboardSummary(Long userId) {
        Map<String, Object> summary = new HashMap<>();
        summary.put("userAggregates", getUserAggregates(userId, LocalDate.now().minusYears(1), LocalDate.now()));
        summary.put("mostLeastEfficientVehicles", getMostLeastEfficientVehicles(userId));
        return summary;
    }

    // --- Helper: Map FuelEntry fields to Map ---
    private Map<String, Object> mapFuelEntryToMap(FuelEntry entry, FuelEntry prev) {
        Map<String, Object> m = new HashMap<>();
        m.put("entryId", entry.getId());
        m.put("date", entry.getDate());
        m.put("odometer", entry.getOdometer());
        m.put("liters", entry.getLiters());
        m.put("totalAmount", entry.getTotalAmount());
        m.put("stationName", entry.getStationName());
        m.put("fuelBrand", entry.getFuelBrand());
        m.put("fuelGrade", entry.getFuelGrade());
        m.put("notes", entry.getNotes());
        m.put("vehicleId", entry.getVehicle() != null ? entry.getVehicle().getId() : null);
        m.put("unitPrice", entry.getLiters() > 0 ? entry.getTotalAmount() / entry.getLiters() : null);
        if (prev != null) {
            int distance = entry.getOdometer() - prev.getOdometer();
            m.put("distanceSinceLast", distance);
            m.put("consumptionLPer100km", distance > 0 ? (entry.getLiters() / distance) * 100 : null);
            m.put("costPerKm", distance > 0 ? entry.getTotalAmount() / distance : null);
        }
        return m;
    }

    // --- Helper: Calculate aggregates for FuelEntry list ---
    private Map<String, Object> calculateAggregates(List<FuelEntry> entries) {
        double totalLiters = entries.stream().mapToDouble(FuelEntry::getLiters).sum();
        double totalSpend = entries.stream().mapToDouble(FuelEntry::getTotalAmount).sum();
        int totalDistance = 0;
        FuelEntry prev = null;
        List<Double> consumptions = new ArrayList<>();
        for (FuelEntry entry : entries) {
            if (prev != null) {
                int distance = entry.getOdometer() - prev.getOdometer();
                totalDistance += distance;
                if (distance > 0) {
                    consumptions.add((entry.getLiters() / distance) * 100);
                }
            }
            prev = entry;
        }
        double avgConsumption = consumptions.stream().mapToDouble(Double::doubleValue).average().orElse(0);
        double avgCostPerLiter = totalLiters > 0 ? totalSpend / totalLiters : 0;
        double avgCostPerKm = totalDistance > 0 ? totalSpend / totalDistance : 0;
        Map<String, Object> agg = new HashMap<>();
        agg.put("totalLiters", totalLiters);
        agg.put("totalSpend", totalSpend);
        agg.put("totalDistance", totalDistance);
        agg.put("avgConsumptionLPer100km", avgConsumption);
        agg.put("avgCostPerLiter", avgCostPerLiter);
        agg.put("avgCostPerKm", avgCostPerKm);
        return agg;
    }

    // --- Helper: Calculate average consumption for grouped entries ---
    private double calculateAvgConsumption(List<FuelEntry> entries) {
        if (entries.size() <= 1) return 0;
        double sum = 0;
        int count = 0;
        for (int i = 1; i < entries.size(); i++) {
            FuelEntry prev = entries.get(i - 1);
            FuelEntry curr = entries.get(i);
            int distance = curr.getOdometer() - prev.getOdometer();
            if (distance > 0) {
                sum += (curr.getLiters() / distance) * 100;
                count++;
            }
        }
        return count > 0 ? sum / count : 0;
    }
}
