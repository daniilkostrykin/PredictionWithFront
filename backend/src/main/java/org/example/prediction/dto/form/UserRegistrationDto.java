package org.example.prediction.dto.form;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.example.prediction.utils.validation.UniqueEmail;
import org.example.prediction.utils.validation.PasswordMatches;
import org.example.prediction.utils.validation.UniqueUsername;

@PasswordMatches(message = "Пароли не совпадают")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserRegistrationDto implements java.io.Serializable {
    @UniqueUsername
    @NotBlank(message = "Напишите хоть что-нибудь")
    @Size(min = 3, max = 20, message = "Username слишком маленький(")
    private String username;

    @UniqueEmail
    @NotBlank(message = "Без email никак")
    @Email(message = "Кажется, это не email")
    private String email;

    @NotBlank(message = "Пароль обязателен")
    @Size(min = 6, message = "Слишком маленький, увеличь")
    private String password;

    private String confirmPassword;
}