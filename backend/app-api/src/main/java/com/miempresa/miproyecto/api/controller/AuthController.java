package com.miempresa.miproyecto.api.controller;

import com.miempresa.miproyecto.api.dto.AuthResponse;
import com.miempresa.miproyecto.api.dto.LoginRequest;
import com.miempresa.miproyecto.api.dto.RegisterRequest;
import com.miempresa.miproyecto.api.dto.UserResponse;
import com.miempresa.miproyecto.core.domain.User;
import com.miempresa.miproyecto.core.service.UserService;
import com.miempresa.miproyecto.security.jwt.JwtService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthController {

    private final UserService userService;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;
    private final UserDetailsService userDetailsService;

    @PostMapping("/register")
    public ResponseEntity<AuthResponse> register(@Valid @RequestBody RegisterRequest request) {
        User user = userService.register(request.fullName(), request.email(), request.password());
        String token = jwtService.generateToken(userDetailsService.loadUserByUsername(user.getEmail()));
        return ResponseEntity.status(HttpStatus.CREATED).body(AuthResponse.of(token, UserResponse.from(user)));
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest request) {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.email(), request.password()));

        User user = userService.findByEmail(request.email());
        String token = jwtService.generateToken(userDetailsService.loadUserByUsername(user.getEmail()));
        return ResponseEntity.ok(AuthResponse.of(token, UserResponse.from(user)));
    }
}
