package org.example.prediction.dto.admin;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AdminDashboardViewModel implements java.io.Serializable {
    private long totalUsers;
    private long activeEvents;
    private long predictionsMadeToday;
}