package org.example.prediction;

import org.example.prediction.dto.form.AddPrizeDto;
import org.example.prediction.models.entities.User;
import org.example.prediction.models.enums.UserRole;
import org.example.prediction.repositories.PrizeRepository;
import org.example.prediction.repositories.TicketRepository;
import org.example.prediction.repositories.UserRepository;
import org.example.prediction.services.PrizeService;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.time.LocalDateTime;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

@SpringBootTest
public class ConcurrencyTest {

    @Autowired private PrizeService prizeService;
    @Autowired private UserRepository userRepository;
    @Autowired private PrizeRepository prizeRepository;
    @Autowired private TicketRepository ticketRepository;

    @Test
    void testDoubleSpending() throws InterruptedException {
        // 1. Подготовка: Создаем юзера с балансом 10
        User user = new User();
        user.setUsername("racer");
        user.setEmail("racer@test.com");
        user.setPassword("pass");
        user.setRole(UserRole.USER);
        user.setBalance(10); // Баланс 10
        userRepository.save(user);

        // 2. Создаем приз ценой 10 (хватает ровно на 1 билет)
        AddPrizeDto prizeDto = new AddPrizeDto();
        prizeDto.setTitle("Race Prize");
        prizeDto.setTicketPrice(10);
        prizeDto.setDrawDate(LocalDateTime.now().plusDays(1));
        prizeService.createPrize(prizeDto);

        Long prizeId = prizeRepository.findAll().stream()
                .filter(p -> p.getTitle().equals("Race Prize"))
                .findFirst()
                .orElseThrow()
                .getId();
        // 3. Запускаем 2 потока одновременно
        int numberOfThreads = 2;
        ExecutorService executorService = Executors.newFixedThreadPool(numberOfThreads);
        CountDownLatch latch = new CountDownLatch(numberOfThreads);

        // Считаем успешные и неуспешные попытки
        AtomicInteger successCount = new AtomicInteger(0);
        AtomicInteger failCount = new AtomicInteger(0);

        for (int i = 0; i < numberOfThreads; i++) {
            executorService.submit(() -> {
                try {
                    // Пытаемся купить билет
                    prizeService.buyTicket(prizeId, "racer");
                    successCount.incrementAndGet();
                } catch (Exception e) {
                    // Если упало (Optimistic Locking или нехватка средств)
                    System.out.println("Поймана ошибка: " + e.getClass().getName());
                    failCount.incrementAndGet();
                } finally {
                    latch.countDown();
                }
            });
        }

        latch.await(); // Ждем завершения обоих потоков

        // 4. ПРОВЕРКИ
        // Только одна покупка должна пройти
        Assertions.assertEquals(1, successCount.get(), "Должен купиться только 1 билет");
        Assertions.assertEquals(1, failCount.get(), "Второй поток должен упасть с ошибкой");

        // Баланс должен быть 0, а не -10
        User updatedUser = userRepository.findById(user.getId()).orElseThrow();
        Assertions.assertEquals(0, updatedUser.getBalance());

        // Билет должен быть 1
        Assertions.assertEquals(1, ticketRepository.count());
    }
}