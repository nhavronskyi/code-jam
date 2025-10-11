package com.team.codejam.controller;

import jakarta.servlet.http.HttpSession;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/stats")
public class StatisticsController {
    // @Autowired
    // private StatisticsService statisticsService;

    @GetMapping("/overview")
    public ResponseEntity<Map<String, Object>> getOverview(@RequestParam Long vehicleId, @RequestParam String period, HttpSession session) {
        Long userId = (Long) session.getAttribute("userId");
        if (userId == null) return ResponseEntity.status(401).build();
        // Map<String, Object> stats = statisticsService.getOverviewStats(userId, vehicleId, period);
        // return ResponseEntity.ok(stats);
        return ResponseEntity.ok(Map.of()); // Placeholder for actual stats
    }

    @GetMapping("/brand-grade")
    public ResponseEntity<Map<String, Object>> getBrandGradeStats(@RequestParam Long vehicleId, HttpSession session) {
        Long userId = (Long) session.getAttribute("userId");
        if (userId == null) return ResponseEntity.status(401).build();
        // Map<String, Object> stats = statisticsService.getBrandGradeStats(userId, vehicleId);
        // return ResponseEntity.ok(stats);
        return ResponseEntity.ok(Map.of()); // Placeholder for actual stats
    }
}
