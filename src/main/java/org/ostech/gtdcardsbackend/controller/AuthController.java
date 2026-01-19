package org.ostech.gtdcardsbackend.controller;

import jakarta.validation.Valid;
import org.ostech.gtdcardsbackend.dto.*;
import org.ostech.gtdcardsbackend.service.AuthService;
import org.ostech.gtdcardsbackend.util.APIConstants;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping(APIConstants.AUTH_ENDPOINT)
@CrossOrigin(origins = "*", maxAge = 3600)
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping(APIConstants.AUTH_LOGIN_ENDPOINT)
    public ResponseEntity<ApiResponseDTO<AuthResponseDTO>> login(
        @Valid @RequestBody LoginRequestDTO loginRequest) {
        try {
            AuthResponseDTO authResponse = authService.login(loginRequest);
            ApiResponseDTO<AuthResponseDTO> response = ApiResponseDTO.success(
                authResponse,
                "Login successful"
            );
            return new ResponseEntity<>(response, HttpStatus.OK);
        } catch (Exception e) {
            ApiResponseDTO<AuthResponseDTO> errorResponse = ApiResponseDTO.error(e.getMessage());
            return new ResponseEntity<>(errorResponse, HttpStatus.UNAUTHORIZED);
        }
    }

    @PostMapping(APIConstants.AUTH_REGISTER_ENDPOINT)
    public ResponseEntity<ApiResponseDTO<AuthResponseDTO>> register(
        @Valid @RequestBody RegisterRequestDTO registerRequest) {
        try {
            AuthResponseDTO authResponse = authService.register(registerRequest);
            ApiResponseDTO<AuthResponseDTO> response = ApiResponseDTO.success(
                authResponse,
                "Registration successful"
            );
            return new ResponseEntity<>(response, HttpStatus.CREATED);
        } catch (Exception e) {
            ApiResponseDTO<AuthResponseDTO> errorResponse = ApiResponseDTO.error(e.getMessage());
            return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
        }
    }

    @PostMapping(APIConstants.AUTH_LOGOUT_ENDPOINT)
    public ResponseEntity<ApiResponseDTO<Void>> logout(
        @RequestHeader(value = "Authorization", required = false) String authHeader) {
        try {
            String email = extractEmailFromToken(authHeader);
            authService.logout(email);
            ApiResponseDTO<Void> response = ApiResponseDTO.success(
                null,
                "Logout successful"
            );
            return new ResponseEntity<>(response, HttpStatus.OK);
        } catch (Exception e) {
            ApiResponseDTO<Void> errorResponse = ApiResponseDTO.error(e.getMessage());
            return new ResponseEntity<>(errorResponse, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping(APIConstants.AUTH_VALIDATE_ENDPOINT)
    public ResponseEntity<ApiResponseDTO<UserResponseDTO>> validateToken(
        @RequestHeader("Authorization") String authHeader) {
        try {
            String token = extractTokenFromHeader(authHeader);
            UserResponseDTO userResponse = authService.validateToken(token);
            ApiResponseDTO<UserResponseDTO> response = ApiResponseDTO.success(
                userResponse,
                "Token is valid"
            );
            return new ResponseEntity<>(response, HttpStatus.OK);
        } catch (Exception e) {
            ApiResponseDTO<UserResponseDTO> errorResponse = ApiResponseDTO.error(e.getMessage());
            return new ResponseEntity<>(errorResponse, HttpStatus.UNAUTHORIZED);
        }
    }

    private String extractTokenFromHeader(String authHeader) {
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            return authHeader.substring(7);
        }
        throw new RuntimeException("Invalid authorization header");
    }

    private String extractEmailFromToken(String authHeader) {
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            // In a real scenario, extract the email from the token
            return "unknown";
        }
        return "unknown";
    }
}
