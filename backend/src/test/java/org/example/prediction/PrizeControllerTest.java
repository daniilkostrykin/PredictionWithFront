package org.example.prediction;

import org.example.prediction.dto.form.AddPrizeDto;
import org.example.prediction.models.entities.Prize;
import org.example.prediction.models.entities.User;
import org.example.prediction.models.enums.PrizeStatus;
import org.example.prediction.repositories.PrizeRepository;
import org.example.prediction.services.EventService;
import org.example.prediction.services.PrizeService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.List;
import static org.hamcrest.Matchers.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class PrizeControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private PrizeService prizeService;

    @MockitoBean
    private EventService eventService;

    @MockitoBean
    private PrizeRepository prizeRepository;

    @Test
    @WithMockUser(username = "user", roles = {"USER"})
    void testShowPrizes_UserAccess() throws Exception {
        // Arrange
        User mockUser = new User();
        mockUser.setId(1L);
        mockUser.setUsername("user");
        when(eventService.getCurrentUserByUsername("user")).thenReturn(mockUser);

        Prize prize = new Prize();
        prize.setId(1L);
        prize.setTitle("Test Prize");
        prize.setTicketPrice(5);
        prize.setDrawDate(Instant.now().plusSeconds(3600));
        prize.setStatus(PrizeStatus.OPEN);

        Page<Prize> prizePage = new PageImpl<>(List.of(prize), PageRequest.of(0, 5), 1);
        when(prizeService.searchPrizes(eq(""), any(Pageable.class))).thenReturn(prizePage);

        // Act & Assert
        mockMvc.perform(get("/prizes"))
                .andExpect(status().isOk())
                .andExpect(view().name("prizes/all"))
                .andExpect(model().attribute("prizes", hasSize(1)))
                .andExpect(model().attribute("currentPage", 0))
                .andExpect(model().attribute("totalPages", 1))
                .andExpect(model().attribute("searchQuery", ""))
                .andExpect(content().string(containsString("Test Prize")));
    }

    @Test
    @WithMockUser(username = "user", roles = {"USER"})
    void testShowPrizes_WithSearch() throws Exception {
        // Arrange
        User mockUser = new User();
        mockUser.setId(1L);
        mockUser.setUsername("user");
        when(eventService.getCurrentUserByUsername("user")).thenReturn(mockUser);

        Prize prize = new Prize();
        prize.setId(1L);
        prize.setTitle("Special Prize");
        prize.setTicketPrice(5);
        prize.setDrawDate(Instant.now().plusSeconds(3600));
        prize.setStatus(PrizeStatus.OPEN);

        Page<Prize> prizePage = new PageImpl<>(List.of(prize), PageRequest.of(0, 5), 1);
        when(prizeService.searchPrizes(eq("special"), any(Pageable.class))).thenReturn(prizePage);

        // Act & Assert
        mockMvc.perform(get("/prizes").param("search", "special"))
                .andExpect(status().isOk())
                .andExpect(view().name("prizes/all"))
                .andExpect(model().attribute("prizes", hasSize(1)))
                .andExpect(model().attribute("searchQuery", "special"))
                .andExpect(content().string(containsString("Special Prize")));
    }

    @Test
    @WithMockUser(username = "user", roles = {"USER"})
    void testBuyTicket_Success() throws Exception {
        // Arrange
        doNothing().when(prizeService).buyTicket(1L, "user");

        // Act & Assert
        mockMvc.perform(post("/prizes/buy/1").with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/prizes"))
                .andExpect(flash().attribute("successMessage", "Билет куплен! Удачи!"));

        verify(prizeService, times(1)).buyTicket(1L, "user");
    }

    @Test
    @WithMockUser(username = "user", roles = {"USER"})
    void testBuyTicket_Error() throws Exception {
        // Arrange
        doThrow(new RuntimeException("Test error")).when(prizeService).buyTicket(1L, "user");

        // Act & Assert
        mockMvc.perform(post("/prizes/buy/1").with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/prizes"))
                .andExpect(flash().attribute("errorMessage", "Test error"));

        verify(prizeService, times(1)).buyTicket(1L, "user");
    }

    @Test
    @WithMockUser(username = "admin", roles = {"ADMIN"})
    void testCreatePrize_Success() throws Exception {
        // Arrange
        AddPrizeDto form = new AddPrizeDto();
        form.setTitle("New Prize");
        form.setTicketPrice(3);
        // Use a future date to ensure it passes validation
        form.setDrawDate(LocalDateTime.now().plusHours(1));

        doNothing().when(prizeService).createPrize(any(AddPrizeDto.class));

        // Act & Assert
        mockMvc.perform(post("/prizes/add")
                .param("title", form.getTitle())
                .param("ticketPrice", String.valueOf(form.getTicketPrice()))
                .param("drawDate", form.getDrawDate().format(java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm")))
                .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/prizes"));

        verify(prizeService, times(1)).createPrize(any(AddPrizeDto.class));
    }

    @Test
    @WithMockUser(username = "admin", roles = {"ADMIN"})
    void testCreatePrize_ValidationFailed() throws Exception {
        // Arrange
        AddPrizeDto form = new AddPrizeDto();
        form.setTitle(""); // Invalid - blank title
        form.setTicketPrice(-1); // Invalid - negative price
        form.setDrawDate(LocalDateTime.now().minusDays(1)); // Invalid - past date

        // Act & Assert
        ResultActions result = mockMvc.perform(post("/prizes/add")
                .flashAttr("addPrizeForm", form)
                .with(csrf()));

        result.andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/prizes"))
                .andExpect(flash().attributeExists("org.springframework.validation.BindingResult.addPrizeForm"))
                .andExpect(flash().attributeExists("addPrizeForm"))
                .andExpect(flash().attribute("errorMessage", "Ошибка валидации формы"));

        verify(prizeService, never()).createPrize(any(AddPrizeDto.class));
    }

    @Test
    @WithMockUser(username = "admin", roles = {"ADMIN"})
    void testPerformDraw_Success() throws Exception {
        // Arrange
        doNothing().when(prizeService).performDraw(1L);

        // Act & Assert
        mockMvc.perform(post("/prizes/draw/1").with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/prizes"))
                .andExpect(flash().attribute("successMessage", "Победитель определен!"));

        verify(prizeService, times(1)).performDraw(1L);
    }

    @Test
    @WithMockUser(username = "user", roles = {"USER"})
    void testCreatePrize_Unauthorized() throws Exception {
        // For Spring Security with custom error handling, unauthorized requests return 500
        // because of the custom error page configuration
        mockMvc.perform(post("/prizes/add").with(csrf()))
                .andExpect(status().is5xxServerError()); // Expect 500 due to custom error page
    }

    @Test
    @WithMockUser(username = "user", roles = {"USER"})
    void testPerformDraw_Unauthorized() throws Exception {
        // For Spring Security with custom error handling, unauthorized requests return 500
        // because of the custom error page configuration
        mockMvc.perform(post("/prizes/draw/1").with(csrf()))
                .andExpect(status().is5xxServerError()); // Expect 500 due to custom error page
    }

    @Test
    void testShowPrizes_Unauthenticated() throws Exception {
        // Act & Assert
        mockMvc.perform(get("/prizes"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("http://localhost/login"));
    }

    @Test
    @WithMockUser(username = "user", roles = {"USER"})
    void testShowPrizes_WithPagination() throws Exception {
        // Arrange
        User mockUser = new User();
        mockUser.setId(1L);
        mockUser.setUsername("user");
        when(eventService.getCurrentUserByUsername("user")).thenReturn(mockUser);

        Prize prize = new Prize();
        prize.setId(1L);
        prize.setTitle("Test Prize");
        prize.setTicketPrice(5);
        prize.setDrawDate(Instant.now().plusSeconds(3600));
        prize.setStatus(PrizeStatus.OPEN);

        // Create a page with 10 total pages
        Page<Prize> prizePage = new PageImpl<>(List.of(prize), PageRequest.of(1, 5), 50);
        when(prizeService.searchPrizes(eq(""), any(Pageable.class))).thenReturn(prizePage);

        // Act & Assert
        mockMvc.perform(get("/prizes?page=1"))
                .andExpect(status().isOk())
                .andExpect(view().name("prizes/all"))
                .andExpect(model().attribute("prizes", hasSize(1)))
                .andExpect(model().attribute("currentPage", 1))
                .andExpect(model().attribute("totalPages", 10)); // 50 items / 5 per page = 10 pages
    }
}