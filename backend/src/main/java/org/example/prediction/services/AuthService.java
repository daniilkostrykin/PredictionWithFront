package org.example.prediction.services;

import org.example.prediction.dto.form.UserRegistrationDto;

public interface AuthService {
    void register(UserRegistrationDto form);
}