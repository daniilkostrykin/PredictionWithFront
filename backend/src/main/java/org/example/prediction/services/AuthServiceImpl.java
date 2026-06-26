package org.example.prediction.services;

import org.example.prediction.models.entities.User;
import org.example.prediction.dto.form.UserRegistrationDto;
import org.example.prediction.models.enums.UserRole;
import org.example.prediction.repositories.UserRepository;
//import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Transactional
    public void register(UserRegistrationDto form){
        log.debug("Регистрация пользователя: {}", form.getUsername());

        if (userRepository.findByUsername(form.getUsername()).isPresent()) {
            log.warn("Попытка регистрации пользователя с уже существующим именем: {}", form.getUsername());
            throw new RuntimeException("Username уже занят");
        }
        User user = new User();
        user.setUsername(form.getUsername());
        user.setEmail(form.getEmail());
        user.setPassword(passwordEncoder.encode(form.getPassword()));
        user.setRole(UserRole.USER);

        userRepository.save(user);
        log.debug("Пользователь успешно зарегистрирован: {}", user.getUsername());
    }
}
