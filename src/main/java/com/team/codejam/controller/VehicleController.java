package com.team.codejam.controller;

import com.team.codejam.entity.User;
import com.team.codejam.entity.Vehicle;
import com.team.codejam.repository.UserRepository;
import com.team.codejam.service.VehicleService;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/vehicles")
public class VehicleController {
    @Autowired
    private VehicleService vehicleService;

    @Autowired
    private UserRepository userRepository;

    @GetMapping
    public ResponseEntity<List<Vehicle>> getVehicles(HttpSession session) {
        Long userId = (Long) session.getAttribute("userId");
        if (userId == null) return ResponseEntity.status(401).build();
        return ResponseEntity.ok(vehicleService.getVehiclesForUser(userId));
    }

    @PostMapping
    public ResponseEntity<Vehicle> addVehicle(@RequestBody Vehicle vehicle, HttpSession session) {
        Long userId = (Long) session.getAttribute("userId");
        if (userId == null) return ResponseEntity.status(401).build();
        User user = userRepository.findById(userId).orElse(null);
        if (user == null) return ResponseEntity.status(401).build();
        vehicle.setUser(user); // Set the User object
        return ResponseEntity.ok(vehicleService.addVehicle(vehicle));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteVehicle(@PathVariable Long id, HttpSession session) {
        Long userId = (Long) session.getAttribute("userId");
        if (userId == null) return ResponseEntity.status(401).build();
        // Optionally check ownership before delete
        vehicleService.deleteVehicle(id);
        return ResponseEntity.ok().build();
    }
}
