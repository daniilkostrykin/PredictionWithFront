package org.example.prediction.services;

import org.example.prediction.dto.DashboardDto;
import org.springframework.data.domain.Page;

public interface DashboardService {
    Page<DashboardDto> getLeaderboard(String search, int page, int size);
}
