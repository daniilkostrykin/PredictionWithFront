package org.example.prediction.web;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.prediction.services.DashboardService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/dashboard") // Меняем базовый путь для API
@RequiredArgsConstructor
public class DashboardController {

    private final DashboardService dashboardService;

    @GetMapping
    public ResponseEntity<Map<String, Object>> getDashboard(
            @RequestParam(value = "page", defaultValue = "0") int page,
            @RequestParam(value = "size", defaultValue = "10") int size, // Увеличил дефолтный размер страницы до 10
            @RequestParam(defaultValue = "") String search) {

        // Получаем данные от сервиса (скорее всего возвращается Page<UserStatsDto>)
        var dashboardData = dashboardService.getLeaderboard(search, page, size);

        // Упаковываем данные в JSON-формат
        Map<String, Object> response = new HashMap<>();
        response.put("userStats", dashboardData.getContent());
        response.put("currentPage", dashboardData.getNumber());
        response.put("totalPages", dashboardData.getTotalPages());

        return ResponseEntity.ok(response);
    }
}