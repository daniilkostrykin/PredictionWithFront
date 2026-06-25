package org.example.prediction.web;

import lombok.RequiredArgsConstructor;
import org.example.prediction.models.entities.Prediction;
import org.example.prediction.models.enums.PredictionStatus;
import org.example.prediction.models.entities.User;
import org.example.prediction.repositories.PredictionRepository;
import org.example.prediction.repositories.UserRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.security.Principal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController // 1. Теперь это REST контроллер
@RequestMapping("/api/users") // 2. Меняем путь под наш API
@RequiredArgsConstructor
public class UserController {

    private final PredictionRepository predictionRepository;
    private final UserRepository userRepository;

    @PreAuthorize("isAuthenticated()")
    @GetMapping("/profile")
    public ResponseEntity<Map<String, Object>> userProfile(Principal principal) {

        String username = principal.getName();

        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("Пользователь не найден"));

        List<Prediction> history = predictionRepository.findByUserOrderByCreatedAtDesc(user);

        int totalPredictions = history.size();
        long wonPredictions = history.stream()
                .filter(p -> p.getStatus() == PredictionStatus.WON)
                .count();

        // 3. Собираем чистый JSON-ответ
        Map<String, Object> response = new HashMap<>();
        response.put("username", user.getUsername());
        response.put("totalPredictions", totalPredictions);
        response.put("wonPredictions", wonPredictions);

        // 4. Безопасно извлекаем нужные данные из истории, чтобы не сломать Jackson
        List<Map<String, Object>> historyJson = history.stream().map(p -> {
            Map<String, Object> predMap = new HashMap<>();
            predMap.put("id", p.getId());
            predMap.put("status", p.getStatus().name());

            Map<String, Object> eventMap = new HashMap<>();
            eventMap.put("title", p.getEvent().getTitle());
            predMap.put("event", eventMap);

            Map<String, Object> optionMap = new HashMap<>();
            optionMap.put("text", p.getChosenOption().getText());
            predMap.put("chosenOption", optionMap);

            return predMap;
        }).collect(Collectors.toList());

        response.put("predictions", historyJson);

        return ResponseEntity.ok(response);
    }
}