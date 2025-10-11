package com.team.codejam.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.team.codejam.entity.User;
import com.team.codejam.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.WebApplicationContext;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class AuthIntegrationTest {

    private MockMvc mockMvc;

    @Autowired
    private WebApplicationContext webApplicationContext;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ObjectMapper objectMapper;

    private MockHttpSession session;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).build();
        userRepository.deleteAll();
        session = new MockHttpSession();
    }

    @Test
    void fullAuthFlow_ShouldWork_WhenValidCredentials() throws Exception {
        String email = "integration@example.com";
        String password = "integrationPass123";

        // 1. Sign up
        Map<String, String> signupRequest = Map.of(
            "email", email,
            "password", password
        );

        mockMvc.perform(post("/api/auth/signup")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(signupRequest))
                .session(session))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.message").value("Account created and signed in"));

        // Verify user was created in database
        User createdUser = userRepository.findByEmail(email).orElse(null);
        assertNotNull(createdUser);
        assertEquals(email, createdUser.getEmail());
        assertNotNull(createdUser.getPasswordHash());

        // Verify session was set
        assertEquals(createdUser.getId(), session.getAttribute("userId"));
        assertNotNull(session.getAttribute("SPRING_SECURITY_CONTEXT"));

        // 2. Sign out
        mockMvc.perform(post("/api/auth/signout")
                .session(session))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.message").value("Signed out"));

        // Verify session was invalidated
        assertTrue(session.isInvalid());

        // 3. Sign in again with same credentials
        MockHttpSession newSession = new MockHttpSession();
        Map<String, String> signinRequest = Map.of(
            "email", email,
            "password", password
        );

        mockMvc.perform(post("/api/auth/signin")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(signinRequest))
                .session(newSession))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.message").value("Signed in"));

        // Verify session was set again
        assertEquals(createdUser.getId(), newSession.getAttribute("userId"));
        assertNotNull(newSession.getAttribute("SPRING_SECURITY_CONTEXT"));
    }

    @Test
    void signup_ShouldPersistUserToDatabase() throws Exception {
        // Given
        String email = "persist@example.com";
        String password = "persistPass123";
        Map<String, String> signupRequest = Map.of(
            "email", email,
            "password", password
        );

        // When
        mockMvc.perform(post("/api/auth/signup")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(signupRequest))
                .session(session))
            .andExpect(status().isOk());

        // Then
        User user = userRepository.findByEmail(email).orElse(null);
        assertNotNull(user);
        assertEquals(email, user.getEmail());
        assertNotNull(user.getPasswordHash());
        assertNotEquals(password, user.getPasswordHash()); // Should be hashed
    }

    @Test
    void signup_ShouldFailForDuplicateEmail() throws Exception {
        // Given - Create first user
        String email = "duplicate@example.com";
        String password1 = "password123";
        String password2 = "differentPassword456";

        Map<String, String> firstSignup = Map.of(
            "email", email,
            "password", password1
        );

        mockMvc.perform(post("/api/auth/signup")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(firstSignup))
                .session(session))
            .andExpect(status().isOk());

        // When - Try to create second user with same email
        Map<String, String> secondSignup = Map.of(
            "email", email,
            "password", password2
        );

        // Then
        mockMvc.perform(post("/api/auth/signup")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(secondSignup))
                .session(new MockHttpSession()))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.error").value("Email already in use"));

        // Verify only one user exists
        assertEquals(1, userRepository.count());
    }

    @Test
    void signin_ShouldFailWithWrongPassword() throws Exception {
        // Given - Create user first
        String email = "wrongpass@example.com";
        String correctPassword = "correctPass123";
        String wrongPassword = "wrongPass456";

        // Create signup request using proper DTO structure
        String signupJson = String.format("{\"email\":\"%s\",\"password\":\"%s\"}", email, correctPassword);

        mockMvc.perform(post("/api/auth/signup")
                .contentType(MediaType.APPLICATION_JSON)
                .content(signupJson)
                .session(session))
            .andExpect(status().isOk());

        // When - Try to sign in with wrong password using proper DTO structure
        String signinJson = String.format("{\"email\":\"%s\",\"password\":\"%s\"}", email, wrongPassword);

        // Then
        mockMvc.perform(post("/api/auth/signin")
                .contentType(MediaType.APPLICATION_JSON)
                .content(signinJson)
                .session(new MockHttpSession()))
            .andExpect(status().isUnauthorized())
            .andExpect(jsonPath("$.error").value("Invalid credentials"));
    }

    @Test
    void passwordPolicy_ShouldBeEnforced() throws Exception {
        String baseEmail = "policy@example.com";

        String[] invalidPasswords = {
            "short",
            "12345678",
            "abcdefgh",
            "",
            "1234567"
        };

        java.util.List<String> invalidEmails = new java.util.ArrayList<>();
        int attempt = 0;
        for (String invalidPassword : invalidPasswords) {
            String email = baseEmail.replace("@", "+" + attempt + "@");
            invalidEmails.add(email);
            attempt++;
            String signupJson = String.format("{\"email\":\"%s\",\"password\":\"%s\"}", email, invalidPassword);

            if (invalidPassword.isEmpty()) {
                mockMvc.perform(post("/api/auth/signup")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(signupJson)
                        .session(new MockHttpSession()))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.error").value("Validation failed"))
                    .andExpect(jsonPath("$.details.password").value("Password is required"));
            } else {
                mockMvc.perform(post("/api/auth/signup")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(signupJson)
                        .session(new MockHttpSession()))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.error").value("Password does not meet policy"));
            }
        }

        // Ensure none of the invalid emails were persisted
        for (String invalidEmail : invalidEmails) {
            assertTrue(userRepository.findByEmail(invalidEmail).isEmpty(), "Invalid password attempt should not persist user: " + invalidEmail);
        }

        // Perform valid signup
        String validSignupJson = String.format("{\"email\":\"%s\",\"password\":\"%s\"}", baseEmail, "validPass123");

        mockMvc.perform(post("/api/auth/signup")
                .contentType(MediaType.APPLICATION_JSON)
                .content(validSignupJson)
                .session(session))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.message").value("Account created and signed in"));

        // Verify valid email persisted and invalid ones still absent
        assertTrue(userRepository.findByEmail(baseEmail).isPresent(), "Valid signup email should be persisted");
        for (String invalidEmail : invalidEmails) {
            assertTrue(userRepository.findByEmail(invalidEmail).isEmpty(), "Previously invalid email should remain absent: " + invalidEmail);
        }
    }

    @Test
    void sessionManagement_ShouldWork() throws Exception {
        // Given
        String email = "session@example.com";
        String password = "sessionPass123";

        Map<String, String> signupRequest = Map.of(
            "email", email,
            "password", password
        );

        // When - Sign up
        mockMvc.perform(post("/api/auth/signup")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(signupRequest))
                .session(session))
            .andExpect(status().isOk());

        // Then - Session should contain user ID
        User user = userRepository.findByEmail(email).orElse(null);
        assertNotNull(user);
        assertEquals(user.getId(), session.getAttribute("userId"));

        // When - Sign out
        mockMvc.perform(post("/api/auth/signout")
                .session(session))
            .andExpect(status().isOk());

        // Then - Session should be invalidated
        assertTrue(session.isInvalid());
    }
}
