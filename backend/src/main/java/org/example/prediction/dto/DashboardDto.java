package org.example.prediction.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class DashboardDto {
    private String username;
    private int totalPredictions;
    private int wonPredictions;
    private double winRate;

}
