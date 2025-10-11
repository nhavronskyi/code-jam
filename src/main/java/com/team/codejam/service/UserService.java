package com.team.codejam.service;

import com.team.codejam.entity.User;
import com.team.codejam.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class UserService {
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private PasswordEncoder passwordEncoder;

    public User registerUser(String email, String rawPassword) {
        if (userRepository.existsByEmail(email)) {
            throw new IllegalArgumentException("Email already in use");
        }
        if (!isValidPassword(rawPassword)) {
            throw new IllegalArgumentException("Password does not meet policy");
        }
        User user = new User();
        user.setEmail(email);
        user.setPasswordHash(passwordEncoder.encode(rawPassword));
        return userRepository.save(user);
    }

    public Optional<User> findByEmail(String email) {
        return userRepository.findByEmail(email);
    }

    private boolean isValidPassword(String password) {
        return password != null && password.length() >= 8 && password.matches(".*[A-Za-z].*") && password.matches(".*\\d.*");
    }
}
