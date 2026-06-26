package org.example.prediction.dto.form;

import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AddPredictionDto implements java.io.Serializable {

    @NotNull(message = "ID события обязательно")
    private Long eventId;

    @NotNull(message = "Выберите вариант исхода")
    private Long chosenOptionId;
}