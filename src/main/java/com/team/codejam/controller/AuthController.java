package com.team.codejam.controller;

import com.team.codejam.entity.User;
import com.team.codejam.service.UserService;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.team.codejam.dto.SignUpRequestDto;
import com.team.codejam.dto.SignInRequestDto;
import com.team.codejam.dto.AuthResponseDto;
import com.team.codejam.dto.ErrorResponseDto;

import jakarta.validation.Valid;
import java.util.Collections;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/auth")
public class AuthController {
    @Autowired
    private UserService userService;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @PostMapping("/signup")
    public ResponseEntity<?> signup(@Valid @RequestBody SignUpRequestDto payload, HttpSession session) {
        String email = payload.getEmail();
        String password = payload.getPassword();
        try {
            User user = userService.registerUser(email, password);
            session.setAttribute("userId", user.getId());
            // Authenticate user in Spring Security
            Authentication auth = new UsernamePasswordAuthenticationToken(email, null, Collections.emptyList());
            SecurityContextHolder.getContext().setAuthentication(auth);
            session.setAttribute("SPRING_SECURITY_CONTEXT", SecurityContextHolder.getContext());
            return ResponseEntity.ok().body(new AuthResponseDto("Account created and signed in"));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(new ErrorResponseDto(e.getMessage()));
        }
    }

    @PostMapping("/signin")
    public ResponseEntity<?> signin(@Valid @RequestBody SignInRequestDto payload, HttpSession session) {
        String email = payload.getEmail();
        String password = payload.getPassword();
        Optional<User> userOpt = userService.findByEmail(email);

        if (userOpt.isPresent() && passwordEncoder.matches(password, userOpt.get().getPasswordHash())) {
            User user = userOpt.get();
            session.setAttribute("userId", user.getId());
            // Authenticate user in Spring Security
            Authentication auth = new UsernamePasswordAuthenticationToken(email, null, Collections.emptyList());
            SecurityContextHolder.getContext().setAuthentication(auth);
            session.setAttribute("SPRING_SECURITY_CONTEXT", SecurityContextHolder.getContext());
            return ResponseEntity.ok().body(new AuthResponseDto("Signed in"));
        } else {
            return ResponseEntity.status(401).body(new ErrorResponseDto("Invalid credentials"));
        }
    }

    @PostMapping("/signout")
    public ResponseEntity<?> signout(HttpSession session) {
        session.invalidate();
        return ResponseEntity.ok().body(new AuthResponseDto("Signed out"));
    }
}
