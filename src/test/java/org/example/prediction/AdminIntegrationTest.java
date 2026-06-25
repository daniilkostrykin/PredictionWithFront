package org.example.prediction;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.example.prediction.models.entities.Event;
import org.example.prediction.models.entities.EventOption;
import org.example.prediction.models.entities.Prediction;
import org.example.prediction.models.entities.User;
import org.example.prediction.models.enums.EventStatus;
import org.example.prediction.models.enums.PredictionStatus;
import org.example.prediction.models.enums.UserRole;
import org.example.prediction.repositories.EventOptionRepository;
import org.example.prediction.repositories.EventRepository;
import org.example.prediction.repositories.PredictionRepository;
import org.example.prediction.repositories.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional // Откат БД после каждого теста
public class AdminIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private EventRepository eventRepository;

    @Autowired
    private EventOptionRepository eventOptionRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PredictionRepository predictionRepository;

    private Event testEvent;
    private EventOption option1;
    private EventOption option2;
    private User testUser;

    @BeforeEach
    void setup() {
        // Подготовка данных для тестов
        testEvent = new Event();
        testEvent.setTitle("Test T1/T6 Event");
        testEvent.setStatus(EventStatus.CLOSED); // Имитируем работу планировщика
        testEvent.setClosesAt(Instant.now().minus(1, ChronoUnit.HOURS));
        eventRepository.save(testEvent);

        option1 = new EventOption();
        option1.setText("Option 1");
        option1.setEvent(testEvent);
        option1.setIsCorrectOutcome(false);

        option2 = new EventOption();
        option2.setText("Option 2");
        option2.setEvent(testEvent);
        option2.setIsCorrectOutcome(false);

        eventOptionRepository.saveAll(List.of(option1, option2));
        testEvent.setOptions(List.of(option1, option2));
        eventRepository.save(testEvent);

        testUser = new User();
        testUser.setUsername("player1");
        testUser.setPassword("password");
        testUser.setEmail("player1@mail.com");
        testUser.setRole(UserRole.USER);
        testUser.setBalance(0);
        testUser.setSuccessfulPredictions(0);
        userRepository.save(testUser);
    }

    // T1: Появление в админке просроченного события
    // T6: Целостность JSON (options не пустое)
    @Test
    @WithMockUser(roles = "ADMIN")
    void t1_t6_shouldFindPendingEventsWithValidJsonOptions() throws Exception {
        mockMvc.perform(get("/api/admin/dashboard"))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                // Проверяем, что событие в списке
                .andExpect(jsonPath("$.pendingEvents[0].title").value("Test T1/T6 Event"))
                // Проверяем, что подтянулись options (целостность JSON, FetchType сработал)
                .andExpect(jsonPath("$.pendingEvents[0].options[0].text").value("Option 1"))
                .andExpect(jsonPath("$.pendingEvents[0].options[1].text").value("Option 2"));
    }

    // T2: Валидация выбора (отправка без winningOptionId)
    @Test
    @WithMockUser(roles = "ADMIN")
    void t2_shouldReturn400IfResolveWithoutWinningOption() throws Exception {
        mockMvc.perform(post("/api/events/" + testEvent.getId() + "/resolve")) // Не передаем param "winningOptionId"
                .andExpect(status().isBadRequest());
    }

    // T3: Корректность начисления выигрыша
    @Test
    @WithMockUser(roles = "ADMIN")
    void t3_shouldCorrectlyCalculateRewardsWhenEventFinished() throws Exception {
        // Пользователь ставит на Option 2
        Prediction prediction = new Prediction();
        prediction.setUser(testUser);
        prediction.setEvent(testEvent);
        prediction.setChosenOption(option2);
        prediction.setStatus(PredictionStatus.PLACED);
        predictionRepository.save(prediction);

        // Админ завершает событие в пользу Option 2
        mockMvc.perform(post("/api/events/" + testEvent.getId() + "/resolve")
                        .param("winningOptionId", option2.getId().toString()))
                .andExpect(status().isOk());

        // Проверяем изменения в БД
        User updatedUser = userRepository.findById(testUser.getId()).orElseThrow();
        Prediction updatedPrediction = predictionRepository.findById(prediction.getId()).orElseThrow();
        Event updatedEvent = eventRepository.findById(testEvent.getId()).orElseThrow();

        assertEquals(EventStatus.FINISHED, updatedEvent.getStatus(), "Статус события должен стать FINISHED");
        assertEquals(PredictionStatus.WON, updatedPrediction.getStatus(), "Статус ставки должен стать WON");
        assertEquals(1, updatedUser.getSuccessfulPredictions(), "Успешные ставки должны увеличиться");
        assertEquals(1, updatedUser.getBalance(), "Баланс должен пополниться");
    }

    // T4: Доступность API (Security 403 Forbidden)
    @Test
    @WithMockUser(roles = "USER") // Заходим под обычным юзером
    void t4_shouldDenyAccessToAdminApiForNormalUsers() throws Exception {
        mockMvc.perform(get("/api/admin/dashboard"))
                .andExpect(status().isForbidden());

        mockMvc.perform(post("/api/events/" + testEvent.getId() + "/resolve")
                        .param("winningOptionId", option1.getId().toString()))
                .andExpect(status().isForbidden());
    }
}