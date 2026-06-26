package org.example.prediction.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserStatsDto implements java.io.Serializable {
    private String username;
    private int successfulPredictions;
    private long totalPredictionsCount;
    private double successRate;
    private int balance;
}