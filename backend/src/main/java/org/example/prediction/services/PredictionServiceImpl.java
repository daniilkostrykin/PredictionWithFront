package org.example.prediction.services;

import lombok.RequiredArgsConstructor;
import org.example.prediction.dto.form.AddPredictionDto;
import org.example.prediction.models.entities.Event;
import org.example.prediction.models.entities.EventOption;
import org.example.prediction.models.entities.Prediction;
import org.example.prediction.models.entities.User;
import org.example.prediction.models.enums.EventStatus;
import org.example.prediction.models.enums.PredictionStatus;
import org.example.prediction.repositories.EventOptionRepository;
import org.example.prediction.repositories.EventRepository;
import org.example.prediction.repositories.PredictionRepository;
import org.example.prediction.repositories.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;

@Transactional(readOnly = true)
@Service
@RequiredArgsConstructor
public class PredictionServiceImpl implements PredictionService {

    private final PredictionRepository predictionRepository;
    private final UserRepository userRepository;
    private final EventRepository eventRepository;
    private final EventOptionRepository eventOptionRepository;
    @Transactional
    public void makePrediction(Long userId, AddPredictionDto form) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("Пользователь не найден"));

        Event event = eventRepository.findById(form.getEventId())
                .orElseThrow(() -> new IllegalArgumentException("Событие не найдено"));
        
                if (predictionRepository.existsByUserAndEvent(user, event)) {
            throw new IllegalStateException("Вы уже сделали предсказание на это событие! Повторная ставка запрещена.");
        }
        if (event.getStatus() == EventStatus.CLOSED || event.getStatus() == EventStatus.FINISHED) {
            throw new IllegalStateException("Событие уже закрыто");
        }

        if (event.getClosesAt().isBefore(Instant.now())) {
            throw new IllegalStateException("Время для предсказаний истекло");
        }

        EventOption option = eventOptionRepository.findById(form.getChosenOptionId())
                .orElseThrow(() -> new IllegalArgumentException("Опция не найдена"));

        if (!option.getEvent().getId().equals(event.getId())) {
            throw new IllegalArgumentException("Неверная опция для этого события");
        }

        Prediction prediction = new Prediction();
        prediction.setUser(user);
        prediction.setEvent(event);
        prediction.setChosenOption(option);
        prediction.setStatus(PredictionStatus.PLACED);
        
        predictionRepository.save(prediction);
    }
}