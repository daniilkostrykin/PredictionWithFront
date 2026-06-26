package org.example.prediction.dto;

import java.time.Instant;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PredictionDto implements java.io.Serializable {
    private String eventTitle;
    private String prediction;
    private String status;
    private Instant createdAt;
}
