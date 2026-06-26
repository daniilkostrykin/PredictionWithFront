package org.example.prediction.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class OptionDto implements java.io.Serializable {
    private Long optionId;
    private String text;
    private int percentage;
}
