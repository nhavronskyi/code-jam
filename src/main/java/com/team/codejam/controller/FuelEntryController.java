package com.team.codejam.controller;

import com.team.codejam.entity.FuelEntry;
import com.team.codejam.entity.User;
import com.team.codejam.service.FuelEntryService;
import com.team.codejam.dto.FuelEntryRequestDto;
import com.team.codejam.dto.FuelEntryResponseDto;
import com.team.codejam.entity.Vehicle;
import com.team.codejam.repository.VehicleRepository;
import com.team.codejam.repository.UserRepository;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.data.domain.Page;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import com.team.codejam.security.AppUserDetails;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/fuel-entries")
@RequiredArgsConstructor
public class FuelEntryController {

    private final FuelEntryService fuelEntryService;
    private final VehicleRepository vehicleRepository;
    @Autowired
    private UserRepository userRepository;

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
    public ResponseEntity<FuelEntryResponseDto> addEntry(@Valid @RequestBody FuelEntryRequestDto entryDto) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            return ResponseEntity.status(401).build();
        }
        AppUserDetails userDetails = (AppUserDetails) authentication.getPrincipal();
        Vehicle vehicle = vehicleRepository.findById(entryDto.getVehicleId()).orElse(null);
        if (vehicle == null) return ResponseEntity.badRequest().build();
        FuelEntry entry = toEntity(entryDto, vehicle);
        User user = userRepository.findById(userDetails.getId()).orElse(null);
        if (user == null) return ResponseEntity.status(401).build();
        entry.setUser(user); // Set the user field
        FuelEntry saved = fuelEntryService.addFuelEntry(entry);
        return ResponseEntity.ok(toDto(saved));
    }

    @GetMapping("/history")
    public ResponseEntity<?> getHistory(
            @RequestParam(required = false) Long vehicleId,
            @RequestParam(required = false) String brand,
            @RequestParam(required = false) String grade,
            @RequestParam(required = false) String station,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) java.time.LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) java.time.LocalDate endDate,
            @RequestParam(defaultValue = "0") int page) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            return ResponseEntity.status(401).build();
        }
        AppUserDetails userDetails = (AppUserDetails) authentication.getPrincipal();
        Long userId = userDetails.getId();
        Page<FuelEntry> entries = fuelEntryService.getFilteredEntries(userId, vehicleId, brand, grade, station, startDate, endDate, page);
        List<FuelEntryResponseDto> dtos = entries.getContent().stream().map(this::toDto).collect(Collectors.toList());
        return ResponseEntity.ok(new PaginatedResponse<>(dtos, entries.getTotalPages(), entries.getTotalElements()));
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

    // Helper class for paginated response
    class PaginatedResponse<T> {
        public List<T> content;
        public int totalPages;
        public long totalElements;
        public PaginatedResponse(List<T> content, int totalPages, long totalElements) {
            this.content = content;
            this.totalPages = totalPages;
            this.totalElements = totalElements;
        }
    }
}
