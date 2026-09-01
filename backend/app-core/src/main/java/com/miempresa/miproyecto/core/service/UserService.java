package com.miempresa.miproyecto.core.service;

import com.miempresa.miproyecto.common.exception.BadRequestException;
import com.miempresa.miproyecto.common.exception.ResourceNotFoundException;
import com.miempresa.miproyecto.core.domain.Role;
import com.miempresa.miproyecto.core.domain.User;
import com.miempresa.miproyecto.core.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Transactional
    public User register(String fullName, String email, String rawPassword) {
        if (userRepository.existsByEmail(email)) {
            throw new BadRequestException("Ya existe un usuario con ese email");
        }
        User user = User.builder()
                .fullName(fullName)
                .email(email)
                .passwordHash(passwordEncoder.encode(rawPassword))
                .role(Role.USER)
                .enabled(true)
                .build();
        return userRepository.save(user);
    }

    @Transactional(readOnly = true)
    public User findByEmail(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado: " + email));
    }

    @Transactional(readOnly = true)
    public User findById(UUID id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado: " + id));
    }
}
