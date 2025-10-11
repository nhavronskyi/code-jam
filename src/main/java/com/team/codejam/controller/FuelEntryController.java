package com.team.codejam.controller;

import com.team.codejam.entity.FuelEntry;
import com.team.codejam.service.FuelEntryService;
import com.team.codejam.dto.FuelEntryRequestDto;
import com.team.codejam.dto.FuelEntryResponseDto;
import com.team.codejam.entity.Vehicle;
import com.team.codejam.repository.VehicleRepository;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/fuel-entries")
@RequiredArgsConstructor
public class FuelEntryController {

    private final FuelEntryService fuelEntryService;
    private final VehicleRepository vehicleRepository;

    @GetMapping("/vehicle/{vehicleId}")
    public ResponseEntity<List<FuelEntryResponseDto>> getEntries(@PathVariable Long vehicleId, HttpSession session) {
        Long userId = (Long) session.getAttribute("userId");
        if (userId == null) return ResponseEntity.status(401).build();
        // Optionally check vehicle ownership
        List<FuelEntryResponseDto> dtos = fuelEntryService.getEntriesForVehicle(vehicleId)
            .stream().map(this::toDto).collect(Collectors.toList());
        return ResponseEntity.ok(dtos);
    }

    @PostMapping
    public ResponseEntity<FuelEntryResponseDto> addEntry(@Valid @RequestBody FuelEntryRequestDto entryDto, HttpSession session) {
        Long userId = (Long) session.getAttribute("userId");
        if (userId == null) return ResponseEntity.status(401).build();
        Vehicle vehicle = vehicleRepository.findById(entryDto.getVehicleId()).orElse(null);
        if (vehicle == null) return ResponseEntity.badRequest().build();
        FuelEntry entry = toEntity(entryDto, vehicle);
        FuelEntry saved = fuelEntryService.addFuelEntry(entry);
        return ResponseEntity.ok(toDto(saved));
    }

    private FuelEntryResponseDto toDto(FuelEntry entry) {
        FuelEntryResponseDto dto = new FuelEntryResponseDto();
        dto.setId(entry.getId());
        dto.setVehicleId(entry.getVehicle().getId());
        dto.setDate(entry.getDate());
        dto.setOdometer(entry.getOdometer());
        dto.setStationName(entry.getStationName());
        dto.setFuelBrand(entry.getFuelBrand());
        dto.setFuelGrade(entry.getFuelGrade());
        dto.setLiters(entry.getLiters());
        dto.setTotalAmount(entry.getTotalAmount());
        dto.setNotes(entry.getNotes());
        return dto;
    }

    private FuelEntry toEntity(FuelEntryRequestDto dto, Vehicle vehicle) {
        FuelEntry entry = new FuelEntry();
        entry.setVehicle(vehicle);
        entry.setDate(dto.getDate());
        entry.setOdometer(dto.getOdometer());
        entry.setStationName(dto.getStationName());
        entry.setFuelBrand(dto.getFuelBrand());
        entry.setFuelGrade(dto.getFuelGrade());
        entry.setLiters(dto.getLiters());
        entry.setTotalAmount(dto.getTotalAmount());
        entry.setNotes(dto.getNotes());
        return entry;
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
