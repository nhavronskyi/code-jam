package com.team.codejam.controller;

import com.team.codejam.dto.VehicleRequestDto;
import com.team.codejam.dto.VehicleResponseDto;
import com.team.codejam.entity.User;
import com.team.codejam.entity.Vehicle;
import com.team.codejam.repository.UserRepository;
import com.team.codejam.service.VehicleService;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/vehicles")
public class VehicleController {
    @Autowired
    private VehicleService vehicleService;

    @Autowired
    private UserRepository userRepository;

    @GetMapping
    public ResponseEntity<List<VehicleResponseDto>> getVehicles(HttpSession session) {
        Long userId = (Long) session.getAttribute("userId");
        if (userId == null) return ResponseEntity.status(401).build();
        List<VehicleResponseDto> dtos = vehicleService.getVehiclesForUser(userId)
            .stream().map(this::toDto).collect(Collectors.toList());
        return ResponseEntity.ok(dtos);
    }

    @PostMapping
    public ResponseEntity<VehicleResponseDto> addVehicle(@Valid @RequestBody VehicleRequestDto vehicleDto, HttpSession session) {
        Long userId = (Long) session.getAttribute("userId");
        if (userId == null) return ResponseEntity.status(401).build();
        User user = userRepository.findById(userId).orElse(null);
        if (user == null) return ResponseEntity.status(401).build();
        Vehicle vehicle = toEntity(vehicleDto);
        vehicle.setUser(user);
        Vehicle saved = vehicleService.addVehicle(vehicle);
        return ResponseEntity.ok(toDto(saved));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteVehicle(@PathVariable Long id, HttpSession session) {
        Long userId = (Long) session.getAttribute("userId");
        if (userId == null) return ResponseEntity.status(401).build();
        // Optionally check ownership before delete
        vehicleService.deleteVehicle(id);
        return ResponseEntity.ok().build();
    }

    private VehicleResponseDto toDto(Vehicle vehicle) {
        VehicleResponseDto dto = new VehicleResponseDto();
        dto.setId(vehicle.getId());
        dto.setName(vehicle.getName());
        dto.setMake(vehicle.getMake());
        dto.setModel(vehicle.getModel());
        dto.setYear(vehicle.getYear());
        dto.setFuelType(vehicle.getFuelType());
        return dto;
    }

    private Vehicle toEntity(VehicleRequestDto dto) {
        Vehicle vehicle = new Vehicle();
        vehicle.setName(dto.getName());
        vehicle.setMake(dto.getMake());
        vehicle.setModel(dto.getModel());
        vehicle.setYear(dto.getYear());
        vehicle.setFuelType(dto.getFuelType());
        return vehicle;
    }
}
