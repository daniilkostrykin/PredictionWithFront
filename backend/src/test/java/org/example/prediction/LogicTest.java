package org.example.prediction;

import org.example.prediction.models.entities.*;
import org.example.prediction.models.enums.EventStatus;
import org.example.prediction.models.enums.PredictionStatus;
import org.example.prediction.repositories.EventOptionRepository;
import org.example.prediction.repositories.EventRepository;
import org.example.prediction.repositories.PredictionRepository;
import org.example.prediction.repositories.UserRepository;
import org.example.prediction.services.EventServiceImpl;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.modelmapper.ModelMapper;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
@ExtendWith(MockitoExtension.class)
class LogicTest {

    @Mock private EventRepository eventRepository;
    @Mock private EventOptionRepository eventOptionRepository;
    @Mock private PredictionRepository predictionRepository;
    @Mock private UserRepository userRepository;
    @Mock private ModelMapper mapper;

    @InjectMocks
    private EventServiceImpl eventService;

    @Test
    void testFinishEventLogic() {
        // 1. Подготовка данных
        Long eventId = 100L;
        Long winnerOptionId = 5L;

        Event event = new Event();
        event.setId(eventId);
        event.setStatus(EventStatus.ACTIVE);

        EventOption winningOption = new EventOption();
        winningOption.setId(winnerOptionId);
        winningOption.setEvent(event);
        winningOption.setIsCorrectOutcome(false);

        // Создаем пользователя и его прогноз
        User user = new User();
        user.setUsername("luckyUser");
        user.setSuccessfulPredictions(0);

        Prediction prediction = new Prediction();
        prediction.setUser(user);
        prediction.setChosenOption(winningOption);
        prediction.setStatus(PredictionStatus.PLACED);

        // 2. Настройка моков
        when(eventRepository.findById(eventId)).thenReturn(Optional.of(event));
        when(eventOptionRepository.findById(winnerOptionId)).thenReturn(Optional.of(winningOption));
        when(predictionRepository.findAllByEvent(event)).thenReturn(List.of(prediction));

        // 3. Вызов метода
        eventService.finishEvent(eventId, winnerOptionId);

        // 4. Проверки (Asserts)

        // Статус события должен стать FINISHED
        assertEquals(EventStatus.FINISHED, event.getStatus());

        // Опция должна стать победной
        assertTrue(winningOption.getIsCorrectOutcome());

        // Статус предсказания должен стать WON
        assertEquals(PredictionStatus.WON, prediction.getStatus());

        // У пользователя должен увеличиться счетчик побед
        assertEquals(1, user.getSuccessfulPredictions());

        // Проверяем, что всё сохранилось в БД
        verify(eventRepository).saveAndFlush(event);
        verify(eventOptionRepository).saveAndFlush(winningOption);
        verify(userRepository).save(user);
    }

    @Test
    void testFinishEvent_ThrowsException_WhenEventNotFound() {
        // Настройка мока: событие не найдено
        when(eventRepository.findById(999L)).thenReturn(java.util.Optional.empty());
        
        // Проверяем, что метод выбрасывает исключение
        org.junit.jupiter.api.Assertions.assertThrows(org.example.prediction.models.exceptions.EventNotFoundException.class, () ->
            eventService.finishEvent(999L, 1L)
        );
    }

    @Test
    void testFinishEvent_ThrowsException_WhenOptionNotBelongsToEvent() {
        // Подготовка данных
        Long eventId = 100L;
        Long wrongOptionId = 999L;

        Event event = new Event();
        event.setId(eventId);
        event.setStatus(EventStatus.ACTIVE);

        EventOption wrongOption = new EventOption();
        wrongOption.setId(wrongOptionId);
        // У этой опции другое событие, не то, которое мы передаем

        // Настройка моков
        when(eventRepository.findById(eventId)).thenReturn(java.util.Optional.of(event));
        when(eventOptionRepository.findById(wrongOptionId)).thenReturn(java.util.Optional.of(wrongOption));

        // Проверяем, что метод выбрасывает исключение из-за несоответствия события и опции
        org.junit.jupiter.api.Assertions.assertThrows(IllegalArgumentException.class, () ->
            eventService.finishEvent(eventId, wrongOptionId)
        );
    }
}