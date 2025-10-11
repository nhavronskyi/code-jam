package com.team.codejam.controller;

import com.team.codejam.dto.UserProfileResponseDto;
import com.team.codejam.dto.UserSettingsUpdateRequestDto;
import com.team.codejam.entity.User;
import com.team.codejam.repository.UserRepository;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

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
        return userRepository.findById(userId)
                .map(this::toUserProfileResponseDto)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @PutMapping("/settings")
    public ResponseEntity<?> updateSettings(@Valid @RequestBody UserSettingsUpdateRequestDto payload, HttpSession session) {
        Long userId = (Long) session.getAttribute("userId");
        if (userId == null) return ResponseEntity.status(401).build();
        Optional<User> userOpt = userRepository.findById(userId);
        if (userOpt.isEmpty()) return ResponseEntity.notFound().build();
        User user = userOpt.get();
        if (payload.getDisplayName() != null) user.setDisplayName(payload.getDisplayName());
        if (payload.getCurrency() != null) user.setCurrency(payload.getCurrency());
        if (payload.getDistanceUnit() != null) user.setDistanceUnit(payload.getDistanceUnit());
        if (payload.getVolumeUnit() != null) user.setVolumeUnit(payload.getVolumeUnit());
        if (payload.getTimeZone() != null) user.setTimeZone(payload.getTimeZone());
        userRepository.save(user);
        return ResponseEntity.ok(toUserProfileResponseDto(user));
    }

    private UserProfileResponseDto toUserProfileResponseDto(User user) {
        UserProfileResponseDto dto = new UserProfileResponseDto();
        dto.setId(user.getId());
        dto.setEmail(user.getEmail());
        dto.setDisplayName(user.getDisplayName());
        dto.setCurrency(user.getCurrency());
        dto.setDistanceUnit(user.getDistanceUnit());
        dto.setVolumeUnit(user.getVolumeUnit());
        dto.setTimeZone(user.getTimeZone());
        return dto;
    }
}
