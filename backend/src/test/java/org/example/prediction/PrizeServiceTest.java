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
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PrizeServiceTest {

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
    private AddPrizeDto addPrizeDto;

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

        addPrizeDto = new AddPrizeDto();
        addPrizeDto.setTitle("New Prize");
        addPrizeDto.setTicketPrice(3);
        addPrizeDto.setDrawDate(LocalDateTime.now().plusDays(1));
    }

    @Test
    void testCreatePrize_Success() {
        // Act
        prizeService.createPrize(addPrizeDto);

        // Verify
        ArgumentCaptor<Prize> prizeCaptor = ArgumentCaptor.forClass(Prize.class);
        verify(prizeRepository, times(1)).save(prizeCaptor.capture());

        Prize savedPrize = prizeCaptor.getValue();
        assertEquals(addPrizeDto.getTitle(), savedPrize.getTitle());
        assertEquals(addPrizeDto.getTicketPrice(), savedPrize.getTicketPrice());
        assertEquals(PrizeStatus.OPEN, savedPrize.getStatus());
        assertEquals(addPrizeDto.getDrawDate().atZone(ZoneId.systemDefault()).toInstant(), savedPrize.getDrawDate());
    }

    @Test
    void testGetAllPrizes() {
        // Arrange
        List<Prize> prizes = List.of(prize);
        when(prizeRepository.findAllByOrderByStatusAscDrawDateAsc()).thenReturn(prizes);

        // Act
        List<Prize> result = prizeService.getAllPrizes();

        // Assert
        assertEquals(1, result.size());
        assertEquals(prize, result.get(0));
        verify(prizeRepository, times(1)).findAllByOrderByStatusAscDrawDateAsc();
    }

    @Test
    void testBuyTicket_Success() {
        // Arrange
        when(prizeRepository.findById(1L)).thenReturn(Optional.of(prize));
        when(userRepository.findByUsername("testUser")).thenReturn(Optional.of(user));

        // Act
        prizeService.buyTicket(1L, "testUser");

        // Assert
        assertEquals(5, user.getBalance()); // 10 - 5 = 5
        verify(userRepository, times(1)).save(user);
        verify(ticketRepository, times(1)).save(any(Ticket.class));
    }

    @Test
    void testBuyTicket_ThrowsException_WhenPrizeNotFound() {
        // Arrange
        when(prizeRepository.findById(999L)).thenReturn(Optional.empty());

        // Assert
        assertThrows(RuntimeException.class, () -> prizeService.buyTicket(999L, "testUser"));
    }

    @Test
    void testBuyTicket_ThrowsException_WhenUserNotFound() {
        // Arrange
        when(prizeRepository.findById(1L)).thenReturn(Optional.of(prize));
        when(userRepository.findByUsername("nonexistentUser")).thenReturn(Optional.empty());

        // Assert
        assertThrows(RuntimeException.class, () -> prizeService.buyTicket(1L, "nonexistentUser"));
    }

    @Test
    void testBuyTicket_ThrowsException_WhenPrizeClosed() {
        // Arrange
        prize.setStatus(PrizeStatus.CLOSED);
        when(prizeRepository.findById(1L)).thenReturn(Optional.of(prize));
        when(userRepository.findByUsername("testUser")).thenReturn(Optional.of(user));

        // Assert
        assertThrows(IllegalStateException.class, () -> prizeService.buyTicket(1L, "testUser"));
    }

    @Test
    void testBuyTicket_ThrowsException_WhenTimeExpired() {
        // Arrange
        prize.setDrawDate(Instant.now().minusSeconds(3600)); // 1 hour ago
        when(prizeRepository.findById(1L)).thenReturn(Optional.of(prize));
        when(userRepository.findByUsername("testUser")).thenReturn(Optional.of(user));

        // Assert
        assertThrows(IllegalStateException.class, () -> prizeService.buyTicket(1L, "testUser"));
    }

    @Test
    void testBuyTicket_ThrowsException_WhenInsufficientBalance() {
        // Arrange
        user.setBalance(2); // Less than ticket price (5)
        when(prizeRepository.findById(1L)).thenReturn(Optional.of(prize));
        when(userRepository.findByUsername("testUser")).thenReturn(Optional.of(user));

        // Assert
        assertThrows(IllegalStateException.class, () -> prizeService.buyTicket(1L, "testUser"));
    }

    @Test
    void testPerformDraw_NoWinner() {
        // Arrange
        when(prizeRepository.findById(1L)).thenReturn(Optional.of(prize));
        when(ticketRepository.findAllByPrize(prize)).thenReturn(List.of());

        // Act
        prizeService.performDraw(1L);

        // Assert
        assertEquals(PrizeStatus.CLOSED, prize.getStatus());
        assertNull(prize.getWinner());
        verify(prizeRepository, times(1)).save(prize);
    }

    @Test
    void testPerformDraw_WithWinner() {
        // Arrange
        User winnerUser = new User();
        winnerUser.setId(2L);
        winnerUser.setUsername("winner");

        Ticket winningTicket = new Ticket();
        winningTicket.setUser(winnerUser);
        winningTicket.setPrize(prize);

        when(prizeRepository.findById(1L)).thenReturn(Optional.of(prize));
        when(ticketRepository.findAllByPrize(prize)).thenReturn(List.of(winningTicket));

        // Act
        prizeService.performDraw(1L);

        // Assert
        assertEquals(PrizeStatus.CLOSED, prize.getStatus());
        assertEquals(winnerUser, prize.getWinner());
        verify(prizeRepository, times(1)).save(prize);
    }

    @Test
    void testSearchPrizes_WithSearchTerm() {
        // Arrange
        String searchTerm = "test";
        Pageable pageable = PageRequest.of(0, 5);
        List<Prize> prizes = List.of(prize);
        Page<Prize> expectedPage = new PageImpl<>(prizes, pageable, 1);
        
        when(prizeRepository.findAllByTitleContainingIgnoreCase(searchTerm, pageable)).thenReturn(expectedPage);

        // Act
        Page<Prize> result = prizeService.searchPrizes(searchTerm, pageable);

        // Assert
        assertEquals(1, result.getContent().size());
        assertEquals(prize, result.getContent().get(0));
        verify(prizeRepository, times(1)).findAllByTitleContainingIgnoreCase(searchTerm, pageable);
    }

    @Test
    void testSearchPrizes_WithoutSearchTerm() {
        // Arrange
        String searchTerm = "";
        Pageable pageable = PageRequest.of(0, 5);
        List<Prize> prizes = List.of(prize);
        Page<Prize> expectedPage = new PageImpl<>(prizes, pageable, 1);
        
        when(prizeRepository.findAll(pageable)).thenReturn(expectedPage);

        // Act
        Page<Prize> result = prizeService.searchPrizes(searchTerm, pageable);

        // Assert
        assertEquals(1, result.getContent().size());
        assertEquals(prize, result.getContent().get(0));
        verify(prizeRepository, times(1)).findAll(pageable);
    }

    @Test
    void testSearchPrizes_WithNullSearchTerm() {
        // Arrange
        String searchTerm = null;
        Pageable pageable = PageRequest.of(0, 5);
        List<Prize> prizes = List.of(prize);
        Page<Prize> expectedPage = new PageImpl<>(prizes, pageable, 1);
        
        when(prizeRepository.findAll(pageable)).thenReturn(expectedPage);

        // Act
        Page<Prize> result = prizeService.searchPrizes(searchTerm, pageable);

        // Assert
        assertEquals(1, result.getContent().size());
        assertEquals(prize, result.getContent().get(0));
        verify(prizeRepository, times(1)).findAll(pageable);
    }
}