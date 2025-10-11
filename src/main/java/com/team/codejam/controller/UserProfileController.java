package com.team.codejam.controller;

import com.team.codejam.entity.User;
import com.team.codejam.repository.UserRepository;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/user")
public class UserProfileController {
    @Autowired
    private UserRepository userRepository;

    @GetMapping("/profile")
    public ResponseEntity<?> getProfile(HttpSession session) {
        Long userId = (Long) session.getAttribute("userId");
        if (userId == null) return ResponseEntity.status(401).build();
        Optional<User> user = userRepository.findById(userId);
        return user.map(ResponseEntity::ok).orElseGet(() -> ResponseEntity.notFound().build());
    }

    @PutMapping("/settings")
    public ResponseEntity<?> updateSettings(@RequestBody Map<String, String> payload, HttpSession session) {
        Long userId = (Long) session.getAttribute("userId");
        if (userId == null) return ResponseEntity.status(401).build();
        Optional<User> userOpt = userRepository.findById(userId);
        if (userOpt.isEmpty()) return ResponseEntity.notFound().build();
        User user = userOpt.get();
        user.setDisplayName(payload.getOrDefault("displayName", user.getDisplayName()));
        user.setCurrency(payload.getOrDefault("currency", user.getCurrency()));
        user.setDistanceUnit(payload.getOrDefault("distanceUnit", user.getDistanceUnit()));
        user.setVolumeUnit(payload.getOrDefault("volumeUnit", user.getVolumeUnit()));
        user.setTimeZone(payload.getOrDefault("timeZone", user.getTimeZone()));
        userRepository.save(user);
        return ResponseEntity.ok(user);
    }
}
