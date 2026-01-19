package org.ostech.gtdcardsbackend.service;

import lombok.extern.slf4j.Slf4j;
import org.ostech.gtdcardsbackend.dto.AuthResponseDTO;
import org.ostech.gtdcardsbackend.dto.LoginRequestDTO;
import org.ostech.gtdcardsbackend.dto.RegisterRequestDTO;
import org.ostech.gtdcardsbackend.dto.UserResponseDTO;
import org.ostech.gtdcardsbackend.model.User;
import org.ostech.gtdcardsbackend.repository.UserRepository;
import org.ostech.gtdcardsbackend.util.JwtUtil;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@Slf4j
@Transactional
public class AuthService {

    private final UserRepository userRepository;
    private final JwtUtil jwtUtil;
    private final PasswordEncoder passwordEncoder;

    public AuthService(UserRepository userRepository, JwtUtil jwtUtil, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.jwtUtil = jwtUtil;
        this.passwordEncoder = passwordEncoder;
    }

    public AuthResponseDTO login(LoginRequestDTO loginRequest) {
        log.info("Login attempt for user: {}", loginRequest.getEmail());

        User user = userRepository.findByEmail(loginRequest.getEmail())
            .orElseThrow(() -> new RuntimeException("Invalid email or password"));

        if (!user.getActive()) {
            throw new RuntimeException("User account is inactive");
        }

        if (!passwordEncoder.matches(loginRequest.getPassword(), user.getPassword())) {
            throw new RuntimeException("Invalid email or password");
        }

        user.setLastLoginAt(LocalDateTime.now());
        userRepository.save(user);

        String token = jwtUtil.generateToken(user);

        log.info("User logged in successfully: {}", user.getEmail());

        return AuthResponseDTO.builder()
            .token(token)
            .tokenType("Bearer")
            .expiresIn(jwtUtil.getTokenExpirationTime())
            .user(mapToUserResponseDTO(user))
            .build();
    }

    public AuthResponseDTO register(RegisterRequestDTO registerRequest) {
        log.info("Registration attempt for user: {}", registerRequest.getEmail());

        if (userRepository.existsByEmail(registerRequest.getEmail())) {
            throw new RuntimeException("Email already registered");
        }

        User newUser = User.builder()
            .name(registerRequest.getName())
            .email(registerRequest.getEmail())
            .password(passwordEncoder.encode(registerRequest.getPassword()))
            .role("USER")
            .active(true)
            .createdAt(LocalDateTime.now())
            .build();

        User savedUser = userRepository.save(newUser);

        String token = jwtUtil.generateToken(savedUser);

        log.info("User registered successfully: {}", savedUser.getEmail());

        return AuthResponseDTO.builder()
            .token(token)
            .tokenType("Bearer")
            .expiresIn(jwtUtil.getTokenExpirationTime())
            .user(mapToUserResponseDTO(savedUser))
            .build();
    }

    public void logout(String email) {
        log.info("Logout for user: {}", email);
        // In a production app, you might want to invalidate tokens in a blacklist
        // For now, we'll just log the action
    }

    public UserResponseDTO validateToken(String token) {
        if (!jwtUtil.isTokenValid(token)) {
            throw new RuntimeException("Invalid or expired token");
        }

        String email = jwtUtil.extractEmail(token);
        User user = userRepository.findByEmail(email)
            .orElseThrow(() -> new RuntimeException("User not found"));

        if (!user.getActive()) {
            throw new RuntimeException("User account is inactive");
        }

        return mapToUserResponseDTO(user);
    }

    private UserResponseDTO mapToUserResponseDTO(User user) {
        return UserResponseDTO.builder()
            .id(user.getId())
            .name(user.getName())
            .email(user.getEmail())
            .role(user.getRole())
            .build();
    }
}
