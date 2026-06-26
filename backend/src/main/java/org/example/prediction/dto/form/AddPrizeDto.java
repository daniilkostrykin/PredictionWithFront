package org.example.prediction.dto.form;

import jakarta.validation.constraints.*;
import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import java.io.Serializable;
import java.time.LocalDateTime;

@Data
public class AddPrizeDto implements Serializable {
    @NotBlank(message = "Название не может быть пустым")
    @Size(min = 3, max = 100, message = "Название должно быть от 3 до 100 символов")
    private String title;

    @Min(value = 1, message = "Цена должна быть минимум 1 победа")
    private int ticketPrice;

    @NotNull(message = "Укажите дату розыгрыша")
    @Future(message = "Дата должна быть в будущем")
    @DateTimeFormat(pattern = "yyyy-MM-dd'T'HH:mm")
    private LocalDateTime drawDate;
}