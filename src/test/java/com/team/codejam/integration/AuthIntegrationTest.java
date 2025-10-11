package com.team.codejam.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.team.codejam.entity.User;
import com.team.codejam.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureWebMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.WebApplicationContext;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

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

        Map<String, String> signupRequest = Map.of(
            "email", email,
            "password", correctPassword
        );

        mockMvc.perform(post("/api/auth/signup")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(signupRequest))
                .session(session))
                .andExpect(status().isOk());

        // When - Try to sign in with wrong password
        Map<String, String> signinRequest = Map.of(
            "email", email,
            "password", wrongPassword
        );

        // Then
        mockMvc.perform(post("/api/auth/signin")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(signinRequest))
                .session(new MockHttpSession()))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.error").value("Invalid credentials"));
    }

    @Test
    void passwordPolicy_ShouldBeEnforced() throws Exception {
        String email = "policy@example.com";

        // Test various invalid passwords
        String[] invalidPasswords = {
            "short",           // Too short
            "12345678",        // No letters
            "abcdefgh",        // No digits
            "",                // Empty
            "1234567"          // Too short and no letters
        };

        for (String invalidPassword : invalidPasswords) {
            Map<String, String> signupRequest = Map.of(
                "email", email,
                "password", invalidPassword
            );

            mockMvc.perform(post("/api/auth/signup")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(signupRequest))
                    .session(new MockHttpSession()))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.error").value("Password does not meet policy"));
        }

        // Verify no user was created
        assertEquals(0, userRepository.count());

        // Test valid password
        Map<String, String> validSignup = Map.of(
            "email", email,
            "password", "validPass123"
        );

        mockMvc.perform(post("/api/auth/signup")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(validSignup))
                .session(session))
                .andExpect(status().isOk());

        // Verify user was created
        assertEquals(1, userRepository.count());
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
