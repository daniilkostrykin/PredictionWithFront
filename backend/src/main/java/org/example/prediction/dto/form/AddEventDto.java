package org.example.prediction.dto.form;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.format.annotation.DateTimeFormat;

import jakarta.validation.constraints.Size;
import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AddEventDto implements java.io.Serializable {
    @NotBlank(message = "Название события не может быть пустым")
    @Size(min = 5, max = 100, message = "Название должно быть от 5 до 100 символов")
    private String title;

    @Size(max = 500, message = "Описание должно быть до 500 символов")
    private String description;

    @NotEmpty(message = "Варианты выбора должны быть")
    @Size(min = 2, message = "Не менее 2-х вариантов выбора")
    private List<@NotBlank(message = "Вариант ответа не может быть пустым") String> options;

    @NotNull(message = "Укажите дату и время окончания события")
    @Future(message = "Дата и время окончания должны быть в будущем")
    @DateTimeFormat(pattern = "yyyy-MM-dd'T'HH:mm")
    private LocalDateTime closesAt;
}