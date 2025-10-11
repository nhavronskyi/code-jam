package com.team.codejam.controller;

import com.team.codejam.service.StatisticsService;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/statistics")
public class StatisticsController {
    private final StatisticsService statisticsService;

    public StatisticsController(StatisticsService statisticsService) {
        this.statisticsService = statisticsService;
    }

    private Long getCurrentUserId() {
        org.springframework.security.core.Authentication authentication = org.springframework.security.core.context.SecurityContextHolder.getContext().getAuthentication();
        // Assuming principal is a UserDetails with getId(), otherwise adapt as needed
        Object principal = authentication.getPrincipal();
        if (principal instanceof com.team.codejam.entity.User) {
            return ((com.team.codejam.entity.User) principal).getId();
        }
        // If using a custom UserDetails, adapt here
        throw new IllegalStateException("User not authenticated or principal type unknown");
    }

    @GetMapping("/per-fill")
    public List<Map<String, Object>> getPerFillMetrics(@RequestParam Long vehicleId) {
        Long userId = getCurrentUserId();
        return statisticsService.getPerFillMetrics(userId, vehicleId);
    }

    @GetMapping("/aggregates")
    public Map<String, Object> getAggregates(
            @RequestParam Long vehicleId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to) {
        Long userId = getCurrentUserId();
        return statisticsService.getAggregates(userId, vehicleId, from, to);
    }

    @GetMapping("/brand-grade")
    public List<Map<String, Object>> getBrandGradeStats(@RequestParam Long vehicleId) {
        Long userId = getCurrentUserId();
        return statisticsService.getBrandGradeStats(userId, vehicleId);
    }

    @GetMapping("/user-aggregates")
    public Map<String, Object> getUserAggregates(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to) {
        Long userId = getCurrentUserId();
        return statisticsService.getUserAggregates(userId, from, to);
    }

    @GetMapping("/monthly")
    public Map<String, Map<String, Object>> getMonthlyStats(
            @RequestParam Long vehicleId,
            @RequestParam int year) {
        return statisticsService.getMonthlyStats(getCurrentUserId(), vehicleId, year);
    }

    @GetMapping("/grade")
    public List<Map<String, Object>> getGradeStats(@RequestParam Long userId, @RequestParam Long vehicleId) {
        return statisticsService.getGradeStats(userId, vehicleId);
    }

    @GetMapping("/best-worst")
    public Map<String, Object> getBestWorstFillUps(@RequestParam Long userId, @RequestParam Long vehicleId) {
        return statisticsService.getBestWorstFillUps(userId, vehicleId);
    }

    @GetMapping("/efficiency")
    public Map<String, Object> getMostLeastEfficientVehicles(@RequestParam Long userId) {
        return statisticsService.getMostLeastEfficientVehicles(userId);
    }

    @GetMapping("/dashboard")
    public Map<String, Object> getDashboardSummary(@RequestParam Long userId) {
        return statisticsService.getDashboardSummary(userId);
    }
}

