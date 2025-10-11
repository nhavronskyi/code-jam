package com.team.codejam.controller;

import com.team.codejam.entity.FuelEntry;
import com.team.codejam.service.FuelEntryService;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/fuel-entries")
public class FuelEntryController {
    @Autowired
    private FuelEntryService fuelEntryService;

    @GetMapping("/vehicle/{vehicleId}")
    public ResponseEntity<List<FuelEntry>> getEntries(@PathVariable Long vehicleId, HttpSession session) {
        Long userId = (Long) session.getAttribute("userId");
        if (userId == null) return ResponseEntity.status(401).build();
        // Optionally check vehicle ownership
        return ResponseEntity.ok(fuelEntryService.getEntriesForVehicle(vehicleId));
    }

    @PostMapping
    public ResponseEntity<FuelEntry> addEntry(@RequestBody FuelEntry entry, HttpSession session) {
        Long userId = (Long) session.getAttribute("userId");
        if (userId == null) return ResponseEntity.status(401).build();
        // Optionally check vehicle ownership
        return ResponseEntity.ok(fuelEntryService.addFuelEntry(entry));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteEntry(@PathVariable Long id, HttpSession session) {
        Long userId = (Long) session.getAttribute("userId");
        if (userId == null) return ResponseEntity.status(401).build();
        // Optionally check entry ownership
        fuelEntryService.deleteEntry(id);
        return ResponseEntity.ok().build();
    }
}
