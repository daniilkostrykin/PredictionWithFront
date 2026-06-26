package org.example.prediction.services;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.prediction.dto.form.AddPrizeDto;
import org.example.prediction.models.entities.*;
import org.example.prediction.models.enums.PrizeStatus;
import org.example.prediction.repositories.*;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.ZoneId;
import java.util.List;
import java.util.Random;

@Slf4j
@Service
@RequiredArgsConstructor
public class PrizeServiceImpl implements PrizeService {

    private final PrizeRepository prizeRepository;
    private final TicketRepository ticketRepository;
    private final UserRepository userRepository;

    @Override
    public List<Prize> getAllPrizes() {
        return prizeRepository.findAllByOrderByStatusAscDrawDateAsc();
    }

    @Transactional
    @Override
    public void createPrize(AddPrizeDto form) {
        Prize prize = new Prize();
        prize.setTitle(form.getTitle());
        prize.setTicketPrice(form.getTicketPrice());
        prize.setDrawDate(form.getDrawDate().atZone(ZoneId.systemDefault()).toInstant());
        prize.setStatus(PrizeStatus.OPEN);
        prizeRepository.save(prize);
    }

    @Transactional
    @Override
    public void buyTicket(Long prizeId, String username) {
        Prize prize = prizeRepository.findById(prizeId)
                .orElseThrow(() -> new RuntimeException("Приз не найден"));
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("Пользователь не найден"));

        if (prize.getStatus() != PrizeStatus.OPEN) {
            throw new IllegalStateException("Розыгрыш закрыт");
        }
        if (prize.getDrawDate().isBefore(java.time.Instant.now())) {
            throw new IllegalStateException("Время участия истекло! Ждите результатов.");
        }
        if (user.getBalance() < prize.getTicketPrice()) {
            throw new IllegalStateException("Недостаточно побед для покупки");
        }

        user.setBalance(user.getBalance() - prize.getTicketPrice());
        userRepository.save(user);

        Ticket ticket = new Ticket();
        ticket.setUser(user);
        ticket.setPrize(prize);
        ticketRepository.save(ticket);
    }

    @Transactional
    @Override
    public void performDraw(Long prizeId) {
        Prize prize = prizeRepository.findById(prizeId).orElseThrow();

        List<Ticket> tickets = ticketRepository.findAllByPrize(prize);

        if (tickets.isEmpty()) {
            prize.setStatus(PrizeStatus.CLOSED);
        } else {
            Random rand = new Random();
            Ticket winningTicket = tickets.get(rand.nextInt(tickets.size()));

            prize.setWinner(winningTicket.getUser());
            prize.setStatus(PrizeStatus.CLOSED);
        }
        prizeRepository.save(prize);
    }

    public Page<Prize> searchPrizes(String search, Pageable pageable) {
        if (search == null || search.isBlank()) {
            return prizeRepository.findAll(pageable);
        } else {
            return prizeRepository.findAllByTitleContainingIgnoreCase(search, pageable);
        }
    }

    @org.springframework.scheduling.annotation.Scheduled(fixedRate = 60000)
    @Transactional
    public void drawExpiredPrizes() {
        List<Prize> expiredPrizes = prizeRepository.findAllByOrderByStatusAscDrawDateAsc().stream()
                .filter(p -> p.getStatus() == PrizeStatus.OPEN)
                .filter(p -> p.getDrawDate().isBefore(java.time.Instant.now()))
                .toList();

        for (Prize prize : expiredPrizes) {
            log.info("Автоматический розыгрыш приза: {}", prize.getTitle());
            performDraw(prize.getId());
        }
    }
}