package com.team.codejam.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.team.codejam.entity.User;
import com.team.codejam.repository.UserRepository;
import com.team.codejam.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.context.annotation.Import;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
@ActiveProfiles("test")
@Import({UserService.class, BCryptPasswordEncoder.class})
class AuthDataTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private UserService userService;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @BeforeEach
    void setUp() {
        // Clean up before each test
        userRepository.deleteAll();
        entityManager.flush();
        entityManager.clear();
    }

    @Test
    void userRepository_ShouldSaveAndFindUser() {
        // Given
        User user = new User();
        user.setEmail("repository@example.com");
        user.setPasswordHash("hashedPassword123");

        // When
        User savedUser = userRepository.save(user);
        entityManager.flush();
        entityManager.clear();

        // Then
        assertNotNull(savedUser.getId());
        Optional<User> foundUser = userRepository.findByEmail("repository@example.com");
        assertTrue(foundUser.isPresent());
        assertEquals("repository@example.com", foundUser.get().getEmail());
        assertEquals("hashedPassword123", foundUser.get().getPasswordHash());
    }

    @Test
    void userRepository_ShouldCheckEmailExists() {
        // Given
        User user = new User();
        user.setEmail("exists@example.com");
        user.setPasswordHash("hashedPassword");
        userRepository.save(user);
        entityManager.flush();

        // When & Then
        assertTrue(userRepository.existsByEmail("exists@example.com"));
        assertFalse(userRepository.existsByEmail("notexists@example.com"));
    }

    @Test
    void userService_ShouldRegisterUserWithHashedPassword() {
        // Given
        String email = "service@example.com";
        String plainPassword = "myPassword123";

        // When
        User registeredUser = userService.registerUser(email, plainPassword);

        // Then
        assertNotNull(registeredUser);
        assertNotNull(registeredUser.getId());
        assertEquals(email, registeredUser.getEmail());
        assertNotEquals(plainPassword, registeredUser.getPasswordHash());
        assertTrue(passwordEncoder.matches(plainPassword, registeredUser.getPasswordHash()));
    }

    @Test
    void userService_ShouldPreventDuplicateEmails() {
        // Given
        String email = "duplicate@example.com";
        userService.registerUser(email, "password123");

        // When & Then
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> userService.registerUser(email, "differentPassword456")
        );
        assertEquals("Email already in use", exception.getMessage());
    }

    @Test
    void userService_ShouldEnforcePasswordPolicy() {
        // Given
        String email = "policy@example.com";

        // Test various invalid passwords
        String[] invalidPasswords = {
            "short",       // Too short
            "12345678",    // No letters
            "abcdefgh",    // No digits
            null           // Null password
        };

        for (String invalidPassword : invalidPasswords) {
            IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> userService.registerUser(email, invalidPassword)
            );
            assertEquals("Password does not meet policy", exception.getMessage());
        }

        // Test valid password
        assertDoesNotThrow(() -> userService.registerUser(email, "validPass123"));
    }

    @Test
    void userService_ShouldFindUserByEmail() {
        // Given
        String email = "findme@example.com";
        User savedUser = userService.registerUser(email, "password123");

        // When
        Optional<User> foundUser = userService.findByEmail(email);

        // Then
        assertTrue(foundUser.isPresent());
        assertEquals(savedUser.getId(), foundUser.get().getId());
        assertEquals(email, foundUser.get().getEmail());
    }

    @Test
    void passwordEncoder_ShouldHashAndVerifyPasswords() {
        // Given
        String plainPassword = "testPassword123";

        // When
        String hashedPassword = passwordEncoder.encode(plainPassword);

        // Then
        assertNotEquals(plainPassword, hashedPassword);
        assertTrue(passwordEncoder.matches(plainPassword, hashedPassword));
        assertFalse(passwordEncoder.matches("wrongPassword", hashedPassword));
    }
}
