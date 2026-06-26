package org.example.prediction;

import org.example.prediction.dto.form.AddPredictionDto;
import org.example.prediction.models.entities.User;
import org.example.prediction.repositories.UserRepository;
import org.example.prediction.services.EventService;
import org.example.prediction.services.PredictionService;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.autoconfigure.ImportAutoConfiguration;
import org.example.prediction.config.RedisConfig;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Optional;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
@SpringBootTest
@AutoConfigureMockMvc
@ImportAutoConfiguration(exclude = {RedisConfig.class})
class UserScenarioTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private PredictionService predictionService;

    @MockitoBean
    private UserRepository userRepository;

    @MockitoBean
    private EventService eventService; // Мокаем, чтобы не грузить реальную БД

    @Test
    @WithMockUser(username = "simpleUser", roles = "USER")
    void testUserCanViewEvents() throws Exception {
        // Проверяем, что обычный юзер может открыть страницу событий
        mockMvc.perform(get("/events/all"))
                .andExpect(status().isOk())
                .andExpect(view().name("events/all"));
    }

    @Test
    @WithMockUser(username = "simpleUser", roles = "USER")
    void testUserCanMakePrediction() throws Exception {
        // Настраиваем поведение мока: когда ищем юзера, возвращаем объект User
        User mockUser = new User();
        mockUser.setId(1L);
        mockUser.setUsername("simpleUser");
        Mockito.when(userRepository.findByUsername("simpleUser")).thenReturn(Optional.of(mockUser));

        // Эмулируем отправку формы предсказания
        mockMvc.perform(post("/predictions/make")
                        .param("eventId", "10")
                        .param("chosenOptionId", "55")
                        .with(csrf())) // Обязательно добавляем CSRF токен
                .andExpect(status().is3xxRedirection()) // Должен быть редирект
                .andExpect(redirectedUrl("/events/details/10"))
                .andExpect(flash().attributeExists("successMessage")); // Проверяем, что есть сообщение об успехе

        // Проверяем, что сервис предсказаний действительно был вызван
        Mockito.verify(predictionService, Mockito.times(1))
                .makePrediction(Mockito.eq(1L), Mockito.any(AddPredictionDto.class));
    }

    @Test
    @WithMockUser(username = "simpleUser", roles = "USER")
    void testUserCannotAccessAdminPanel() throws Exception {
        // Пытаемся зайти в админку под юзером
        mockMvc.perform(get("/admin"))
                .andExpect(status().isForbidden()); // Ожидаем ошибку 403 (Доступ запрещен)
    }

    @Test
    @WithMockUser(username = "simpleUser", roles = "USER")
    void testUserCannotMakeDuplicatePrediction() throws Exception {
        // Настраиваем моки
        User mockUser = new User();
        mockUser.setId(1L);
        mockUser.setUsername("simpleUser");
        Mockito.when(userRepository.findByUsername("simpleUser")).thenReturn(Optional.of(mockUser));

        // Настраиваем сервис, чтобы он выбросил исключение при попытке дублирования
        Mockito.doThrow(new IllegalStateException("Вы уже сделали предсказание на это событие! Повторная ставка запрещена."))
                .when(predictionService).makePrediction(Mockito.eq(1L), Mockito.any());

        // Эмулируем отправку формы предсказания
        mockMvc.perform(post("/predictions/make")
                        .param("eventId", "10")
                        .param("chosenOptionId", "55")
                        .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/events/details/10"))
                .andExpect(flash().attributeExists("errorMessage")); // Проверяем, что есть сообщение об ошибке

        // Проверяем, что сервис предсказаний действительно был вызван
        Mockito.verify(predictionService, Mockito.times(1))
                .makePrediction(Mockito.eq(1L), Mockito.any(AddPredictionDto.class));
    }
}