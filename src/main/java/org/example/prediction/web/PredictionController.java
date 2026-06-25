package org.example.prediction.web;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.prediction.dto.form.AddPredictionDto;
import org.example.prediction.models.entities.User;
import org.example.prediction.repositories.UserRepository;
import org.example.prediction.services.PredictionService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.Map;

@Slf4j
@RestController 
@RequestMapping("/api/predictions") 
@RequiredArgsConstructor
public class PredictionController {

    private final PredictionService predictionService;
    private final UserRepository userRepository;

    @PreAuthorize("isAuthenticated()")
    @PostMapping("/make")
    public ResponseEntity<?> makePrediction(
            @Valid @RequestBody AddPredictionDto form, 
            Principal principal
    ) {
        try {
            User user = userRepository.findByUsername(principal.getName())
                    .orElseThrow(() -> new RuntimeException("Пользователь не найден"));
            
            predictionService.makePrediction(user.getId(), form);
            
            return ResponseEntity.ok(Map.of("message", "Предсказание успешно сделано!"));
        } catch (IllegalStateException e) {
            log.warn("Логическая ошибка при предсказании: {}", e.getMessage());
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            log.error("Ошибка при предсказании", e);
            return ResponseEntity.internalServerError().body(Map.of("error", "Произошла системная ошибка"));
        }
    }
}