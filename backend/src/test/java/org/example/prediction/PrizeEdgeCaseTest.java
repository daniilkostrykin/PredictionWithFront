package org.example.prediction;

import org.example.prediction.dto.form.AddPrizeDto;
import org.example.prediction.models.entities.*;
import org.example.prediction.models.enums.PrizeStatus;
import org.example.prediction.repositories.PrizeRepository;
import org.example.prediction.repositories.TicketRepository;
import org.example.prediction.repositories.UserRepository;
import org.example.prediction.services.PrizeServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PrizeEdgeCaseTest {

    @Mock
    private PrizeRepository prizeRepository;

    @Mock
    private TicketRepository ticketRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private PrizeServiceImpl prizeService;

    private User user;
    private Prize prize;

    @BeforeEach
    void setUp() {
        user = new User();
        user.setId(1L);
        user.setUsername("testUser");
        user.setBalance(10);

        prize = new Prize();
        prize.setId(1L);
        prize.setTitle("Test Prize");
        prize.setTicketPrice(5);
        prize.setDrawDate(Instant.now().plusSeconds(3600)); // 1 hour from now
        prize.setStatus(PrizeStatus.OPEN);
    }

    @Test
    void testCreatePrize_WithEmptyTitle() {
        AddPrizeDto form = new AddPrizeDto();
        form.setTitle("");
        form.setTicketPrice(3);
        form.setDrawDate(LocalDateTime.now().plusDays(1));

        // Should not throw exception during creation, validation happens in controller
        assertDoesNotThrow(() -> prizeService.createPrize(form));
    }

    @Test
    void testCreatePrize_WithPastDate() {
        AddPrizeDto form = new AddPrizeDto();
        form.setTitle("Test Prize");
        form.setTicketPrice(3);
        form.setDrawDate(LocalDateTime.now().minusDays(1)); // Past date

        // Should not throw exception during creation, validation happens in controller
        assertDoesNotThrow(() -> prizeService.createPrize(form));
    }

    @Test
    void testBuyTicket_WithExactBalance() {
        // User has exactly the required amount
        user.setBalance(5); // Same as ticket price
        when(prizeRepository.findById(1L)).thenReturn(Optional.of(prize));
        when(userRepository.findByUsername("testUser")).thenReturn(Optional.of(user));

        assertDoesNotThrow(() -> prizeService.buyTicket(1L, "testUser"));
        assertEquals(0, user.getBalance()); // Should be 0 after purchase
        verify(userRepository, times(1)).save(user);
        verify(ticketRepository, times(1)).save(any(Ticket.class));
    }

    @Test
    void testBuyTicket_WithInsufficientBalanceByOne() {
        // User has 1 less than required
        user.setBalance(4); // 1 less than ticket price (5)
        when(prizeRepository.findById(1L)).thenReturn(Optional.of(prize));
        when(userRepository.findByUsername("testUser")).thenReturn(Optional.of(user));

        IllegalStateException exception = assertThrows(IllegalStateException.class, 
            () -> prizeService.buyTicket(1L, "testUser"));
        assertEquals("Недостаточно побед для покупки", exception.getMessage());
    }

    @Test
    void testBuyTicket_WithZeroBalance() {
        user.setBalance(0);
        when(prizeRepository.findById(1L)).thenReturn(Optional.of(prize));
        when(userRepository.findByUsername("testUser")).thenReturn(Optional.of(user));

        IllegalStateException exception = assertThrows(IllegalStateException.class, 
            () -> prizeService.buyTicket(1L, "testUser"));
        assertEquals("Недостаточно побед для покупки", exception.getMessage());
    }

    @Test
    void testBuyTicket_WithExpiredPrize() {
        prize.setDrawDate(Instant.now().minusSeconds(3600)); // 1 hour ago
        when(prizeRepository.findById(1L)).thenReturn(Optional.of(prize));
        when(userRepository.findByUsername("testUser")).thenReturn(Optional.of(user));

        IllegalStateException exception = assertThrows(IllegalStateException.class, 
            () -> prizeService.buyTicket(1L, "testUser"));
        assertEquals("Время участия истекло! Ждите результатов.", exception.getMessage());
    }

    @Test
    void testBuyTicket_WithClosedPrize() {
        prize.setStatus(PrizeStatus.CLOSED);
        when(prizeRepository.findById(1L)).thenReturn(Optional.of(prize));
        when(userRepository.findByUsername("testUser")).thenReturn(Optional.of(user));

        IllegalStateException exception = assertThrows(IllegalStateException.class, 
            () -> prizeService.buyTicket(1L, "testUser"));
        assertEquals("Розыгрыш закрыт", exception.getMessage());
    }

    @Test
    void testPerformDraw_WithMultipleTickets() {
        // Create multiple tickets to test random selection
        User user1 = new User();
        user1.setId(1L);
        user1.setUsername("user1");

        User user2 = new User();
        user2.setId(2L);
        user2.setUsername("user2");

        Ticket ticket1 = new Ticket();
        ticket1.setUser(user1);
        ticket1.setPrize(prize);

        Ticket ticket2 = new Ticket();
        ticket2.setUser(user2);
        ticket2.setPrize(prize);

        when(prizeRepository.findById(1L)).thenReturn(Optional.of(prize));
        when(ticketRepository.findAllByPrize(prize)).thenReturn(List.of(ticket1, ticket2));

        // Perform draw once to ensure it works
        prizeService.performDraw(1L);
        assertEquals(PrizeStatus.CLOSED, prize.getStatus());
        assertNotNull(prize.getWinner());
        verify(prizeRepository, times(1)).save(prize);
    }

    @Test
    void testPerformDraw_WithSingleTicket() {
        User winnerUser = new User();
        winnerUser.setId(2L);
        winnerUser.setUsername("winner");

        Ticket winningTicket = new Ticket();
        winningTicket.setUser(winnerUser);
        winningTicket.setPrize(prize);

        when(prizeRepository.findById(1L)).thenReturn(Optional.of(prize));
        when(ticketRepository.findAllByPrize(prize)).thenReturn(List.of(winningTicket));

        prizeService.performDraw(1L);

        assertEquals(PrizeStatus.CLOSED, prize.getStatus());
        assertEquals(winnerUser, prize.getWinner());
        verify(prizeRepository, times(1)).save(prize);
    }

    @Test
    void testPerformDraw_WithNullWinner() {
        when(prizeRepository.findById(1L)).thenReturn(Optional.of(prize));
        when(ticketRepository.findAllByPrize(prize)).thenReturn(List.of());

        prizeService.performDraw(1L);

        assertEquals(PrizeStatus.CLOSED, prize.getStatus());
        assertNull(prize.getWinner());
        verify(prizeRepository, times(1)).save(prize);
    }

    @Test
    void testSearchPrizes_WithSpecialCharacters() {
        String searchTerm = "!@#$%^&*()_+";
        when(prizeRepository.findAllByTitleContainingIgnoreCase(eq(searchTerm), any())).thenReturn(
            new org.springframework.data.domain.PageImpl<>(List.of()));

        var result = prizeService.searchPrizes(searchTerm, 
            org.springframework.data.domain.PageRequest.of(0, 5));

        assertTrue(result.getContent().isEmpty());
        verify(prizeRepository, times(1)).findAllByTitleContainingIgnoreCase(eq(searchTerm), any());
    }

    @Test
    void testSearchPrizes_WithWhitespaceOnly() {
        String searchTerm = "   ";
        when(prizeRepository.findAll((org.springframework.data.domain.Pageable) any())).thenReturn(
            new org.springframework.data.domain.PageImpl<>(List.of()));

        var result = prizeService.searchPrizes(searchTerm, 
            org.springframework.data.domain.PageRequest.of(0, 5));

        assertTrue(result.getContent().isEmpty());
        verify(prizeRepository, times(1)).findAll((org.springframework.data.domain.Pageable) any());
    }

    @Test
    void testSearchPrizes_WithVeryLongSearchTerm() {
        String searchTerm = "a".repeat(100); // Very long search term
        when(prizeRepository.findAllByTitleContainingIgnoreCase(eq(searchTerm), any())).thenReturn(
            new org.springframework.data.domain.PageImpl<>(List.of()));

        var result = prizeService.searchPrizes(searchTerm, 
            org.springframework.data.domain.PageRequest.of(0, 5));

        assertTrue(result.getContent().isEmpty());
        verify(prizeRepository, times(1)).findAllByTitleContainingIgnoreCase(eq(searchTerm), any());
    }
}