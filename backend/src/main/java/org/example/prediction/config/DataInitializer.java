package org.example.prediction.config;

import org.example.prediction.models.entities.Event;
import org.example.prediction.models.entities.EventOption;
import org.example.prediction.models.entities.Prize;
import org.example.prediction.models.entities.User;
import org.example.prediction.models.enums.EventStatus;
import org.example.prediction.models.enums.UserRole;
import org.example.prediction.repositories.EventOptionRepository;
import org.example.prediction.repositories.EventRepository;
import org.example.prediction.repositories.PrizeRepository;
import org.example.prediction.repositories.UserRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;
import java.time.LocalDateTime;
import java.util.Arrays;

@Configuration
public class DataInitializer {

    @Bean
    public CommandLineRunner initData(UserRepository userRepository, EventRepository eventRepository, EventOptionRepository eventOptionRepository, PrizeRepository prizeRepository, PasswordEncoder passwordEncoder) {
        return args -> {
            System.out.println("Запуск инициализации данных");

            // Создание пользователей
            if (userRepository.findByUsername("admin").isEmpty()) {
                System.out.println("Создание администратора...");
                User admin = new User();
                admin.setUsername("admin");
                String encodedPassword = passwordEncoder.encode("12345");
                admin.setPassword(encodedPassword);
                admin.setEmail("admin@example.com");
                admin.setRole(UserRole.ADMIN);
                admin.setSuccessfulPredictions(0);

                userRepository.save(admin);
                System.out.println("Администратор создан: admin / 12345");
            } else {
                System.out.println("Администратор уже существует");
            }

            if (userRepository.findByUsername("user1").isEmpty()) {
                System.out.println("Создание пользователя user1...");
                User user = new User();
                user.setUsername("user1");
                user.setPassword(passwordEncoder.encode("12345"));
                user.setEmail("user1@example.com");
                user.setRole(UserRole.USER);
                user.setSuccessfulPredictions(0);

                userRepository.save(user);
                System.out.println("Пользователь создан: user1 / 12345");
            } else {
                System.out.println("Пользователь user1 уже существует");
            }
/*
            // Создание дополнительных пользователей с обычными именами user2, user3 и т.д.
            for (int i = 2; i <= 10; i++) {
                String username = "user" + i;
                if (userRepository.findByUsername(username).isEmpty()) {
                    System.out.println("Создание пользователя " + username + "...");
                    User user = new User();
                    user.setUsername(username);
                    user.setPassword(passwordEncoder.encode("12345"));
                    user.setEmail(username + "@example.com");
                    user.setRole(UserRole.USER);
                    
                    // Установка случайного баланса от 10 до 1000
                    double balance = 10 + Math.random() * 990;
                    user.setBalance((int) balance);
                    
                    // Установка случайного количества успешных предсказаний от 0 до 20
                    int successfulPredictions = (int) (Math.random() * 21);
                    user.setSuccessfulPredictions(successfulPredictions);

                    userRepository.save(user);
                    System.out.println("Пользователь создан: " + username + " / 12345, баланс: " + (int) balance + ", успешных предсказаний: " + successfulPredictions);
                } else {
                    System.out.println("Пользователь " + username + " уже существует");
                }
            }
*/
            // Создание событий и опций
            if (eventRepository.count() == 0) {
                System.out.println("Создание событий и опций...");

                // Событие 1: The Game Awards 2025
                Event event1 = new Event();
                event1.setTitle("The Game Awards 2025");
                event1.setDescription("Кто станет Игрой Года (GOTY)?");
                event1.setStatus(EventStatus.ACTIVE);
                event1.setClosesAt(LocalDateTime.now().plusDays(1).toInstant(java.time.ZoneOffset.UTC));
                eventRepository.save(event1);

                EventOption[] options1 = {
                    createEventOption("GTA VI (если вышла)", event1),
                    createEventOption("Death Stranding 2", event1),
                    createEventOption("Metaphor: ReFantazio", event1),
                    createEventOption("Другая игра", event1)
                };
                Arrays.stream(options1).forEach(eventOptionRepository::save);

                // Событие 2: Курс Bitcoin: $150k до курантов
                Event event2 = new Event();
                event2.setTitle("Курс Bitcoin: $150k до курантов");
                event2.setDescription("Пробьет ли Биткоин отметку $150,000 до конца года?");
                event2.setStatus(EventStatus.ACTIVE);
                event2.setClosesAt(LocalDateTime.now().plusDays(2).toInstant(java.time.ZoneOffset.UTC));
                eventRepository.save(event2);

                EventOption[] options2 = {
                    createEventOption("Да, будет выше $150k", event2),
                    createEventOption("Нет, не дотянет", event2)
                };
                Arrays.stream(options2).forEach(eventOptionRepository::save);

                // Событие 3: Погода в Москве: Новый год
                Event event3 = new Event();
                event3.setTitle("Погода в Москве: Новый год");
                event3.setDescription("Будет ли снег и мороз в новогоднюю ночь?");
                event3.setStatus(EventStatus.ACTIVE);
                event3.setClosesAt(LocalDateTime.now().plusDays(3).toInstant(java.time.ZoneOffset.UTC));
                eventRepository.save(event3);

                EventOption[] options3 = {
                    createEventOption("Снег и мороз (< -5)", event3),
                    createEventOption("Слякоть / Оттепель", event3),
                    createEventOption("Сухой мороз", event3)
                };
                Arrays.stream(options3).forEach(eventOptionRepository::save);

                // Событие 4: Человек года TIME 2025
                Event event4 = new Event();
                event4.setTitle("Человек года TIME 2025");
                event4.setDescription("Кого журнал выберет человеком года?");
                event4.setStatus(EventStatus.CLOSED);
                event4.setClosesAt(LocalDateTime.now().minusDays(1).toInstant(java.time.ZoneOffset.UTC));
                eventRepository.save(event4);

                EventOption[] options4 = {
                    createEventOption("Сэм Альтман (OpenAI)", event4),
                    createEventOption("Илон Маск", event4),
                    createEventOption("Тейлор Свифт", event4),
                    createEventOption("Искусственный Интеллект", event4)
                };
                Arrays.stream(options4).forEach(eventOptionRepository::save);

                // Событие 5: Box Office: "Муфаса: Король Лев"
                Event event5 = new Event();
                event5.setTitle("Box Office: \"Муфаса: Король Лев\"");
                event5.setDescription("Соберет ли фильм 1 млрд долларов?");
                event5.setStatus(EventStatus.ACTIVE);
                event5.setClosesAt(LocalDateTime.now().plusDays(4).toInstant(java.time.ZoneOffset.UTC));
                eventRepository.save(event5);

                EventOption[] options5 = {
                    createEventOption("Да, > $1 млрд", event5),
                    createEventOption("Нет, провалится", event5)
                };
                Arrays.stream(options5).forEach(eventOptionRepository::save);

                // Событие 6: Apple Vision Pro 2
                Event event6 = new Event();
                event6.setTitle("Apple Vision Pro 2");
                event6.setDescription("Анонсируют ли бюджетную версию очков в 2026?");
                event6.setStatus(EventStatus.ACTIVE);
                event6.setClosesAt(LocalDateTime.now().plusDays(5).toInstant(java.time.ZoneOffset.UTC));
                eventRepository.save(event6);

                EventOption[] options6 = {
                    createEventOption("Да, покажут бюджетную", event6),
                    createEventOption("Нет, только Pro", event6)
                };
                Arrays.stream(options6).forEach(eventOptionRepository::save);

                // Событие 7: OpenAI: Релиз GPT-5
                Event event7 = new Event();
                event7.setTitle("OpenAI: Релиз GPT-5");
                event7.setDescription("Выйдет ли модель GPT-5 в первом квартале 2026?");
                event7.setStatus(EventStatus.ACTIVE);
                event7.setClosesAt(LocalDateTime.now().plusDays(6).toInstant(java.time.ZoneOffset.UTC));
                eventRepository.save(event7);

                EventOption[] options7 = {
                    createEventOption("Да, Q1 2026", event7),
                    createEventOption("Нет, перенесут", event7)
                };
                Arrays.stream(options7).forEach(eventOptionRepository::save);

                // Событие 8: Консоль Nintendo Switch 2
                Event event8 = new Event();
                event8.setTitle("Консоль Nintendo Switch 2");
                event8.setDescription("Будет ли цена на старте выше $400?");
                event8.setStatus(EventStatus.ACTIVE);
                event8.setClosesAt(LocalDateTime.now().plusDays(7).toInstant(java.time.ZoneOffset.UTC));
                eventRepository.save(event8);

                EventOption[] options8 = {
                    createEventOption("Дороже $400", event8),
                    createEventOption("$399 или дешевле", event8)
                };
                Arrays.stream(options8).forEach(eventOptionRepository::save);

                // Событие 9: Tesla Optimus Robot
                Event event9 = new Event();
                event9.setTitle("Tesla Optimus Robot");
                event9.setDescription("Начнутся ли массовые продажи робота?");
                event9.setStatus(EventStatus.ACTIVE);
                event9.setClosesAt(LocalDateTime.now().plusDays(8).toInstant(java.time.ZoneOffset.UTC));
                eventRepository.save(event9);

                EventOption[] options9 = {
                    createEventOption("Да, можно будет купить", event9),
                    createEventOption("Нет, только прототипы", event9)
                };
                Arrays.stream(options9).forEach(eventOptionRepository::save);

                // Событие 10: Windows 12 Release
                Event event10 = new Event();
                event10.setTitle("Windows 12 Release");
                event10.setDescription("Выпустит ли Microsoft новую ОС?");
                event10.setStatus(EventStatus.ACTIVE);
                event10.setClosesAt(LocalDateTime.now().plusDays(9).toInstant(java.time.ZoneOffset.UTC));
                eventRepository.save(event10);

                EventOption[] options10 = {
                    createEventOption("Да, релиз в 2026", event10),
                    createEventOption("Нет, только обновление Win 11", event10)
                };
                Arrays.stream(options10).forEach(eventOptionRepository::save);

                // Событие 11: Оскар 2026: Лучший фильм
                Event event11 = new Event();
                event11.setTitle("Оскар 2026: Лучший фильм");
                event11.setDescription("Кто получит главную кинонаграду?");
                event11.setStatus(EventStatus.ACTIVE);
                event11.setClosesAt(LocalDateTime.now().plusDays(10).toInstant(java.time.ZoneOffset.UTC));
                eventRepository.save(event11);

                EventOption[] options11 = {
                    createEventOption("Дюна: Часть 3", event11),
                    createEventOption("Фильм Нолана", event11),
                    createEventOption("Аватар 3", event11),
                    createEventOption("Другой фильм", event11)
                };
                Arrays.stream(options11).forEach(eventOptionRepository::save);

                // Событие 12: Шрек 5: Трейлер
                Event event12 = new Event();
                event12.setTitle("Шрек 5: Трейлер");
                event12.setDescription("Покажут ли первый трейлер до лета 2026?");
                event12.setStatus(EventStatus.ACTIVE);
                event12.setClosesAt(LocalDateTime.now().plusDays(11).toInstant(java.time.ZoneOffset.UTC));
                eventRepository.save(event12);

                EventOption[] options12 = {
                    createEventOption("Покажут трейлер", event12),
                    createEventOption("Тишина до 2027", event12)
                };
                Arrays.stream(options12).forEach(eventOptionRepository::save);

                // Событие 13: Новый Джеймс Бонд
                Event event13 = new Event();
                event13.setTitle("Новый Джеймс Бонд");
                event13.setDescription("Объявят ли имя нового актера официально?");
                event13.setStatus(EventStatus.ACTIVE);
                event13.setClosesAt(LocalDateTime.now().plusDays(12).toInstant(java.time.ZoneOffset.UTC));
                eventRepository.save(event13);

                EventOption[] options13 = {
                    createEventOption("Аарон Тейлор-Джонсон", event13),
                    createEventOption("Генри Кавилл", event13),
                    createEventOption("Неизвестный актер", event13)
                };
                Arrays.stream(options13).forEach(eventOptionRepository::save);

                // Событие 14: Сериал по Гарри Поттеру
                Event event14 = new Event();
                event14.setTitle("Сериал по Гарри Поттеру");
                event14.setDescription("Назовут ли дату премьеры первого сезона?");
                event14.setStatus(EventStatus.ACTIVE);
                event14.setClosesAt(LocalDateTime.now().plusDays(13).toInstant(java.time.ZoneOffset.UTC));
                eventRepository.save(event14);

                EventOption[] options14 = {
                    createEventOption("Да, назовут дату", event14),
                    createEventOption("Нет новостей", event14)
                };
                Arrays.stream(options14).forEach(eventOptionRepository::save);

                // Событие 15: Аватар 3 (Fire and Ash)
                Event event15 = new Event();
                event15.setTitle("Аватар 3 (Fire and Ash)");
                event15.setDescription("Станет ли самым кассовым фильмом в истории?");
                event15.setStatus(EventStatus.ACTIVE);
                event15.setClosesAt(LocalDateTime.now().plusDays(14).toInstant(java.time.ZoneOffset.UTC));
                eventRepository.save(event15);

                EventOption[] options15 = {
                    createEventOption("Да, побьет рекорд", event15),
                    createEventOption("Нет, соберет много, но не топ-1", event15)
                };
                Arrays.stream(options15).forEach(eventOptionRepository::save);

                // Событие 16: SpaceX Starship: Марс
                Event event16 = new Event();
                event16.setTitle("SpaceX Starship: Марс");
                event16.setDescription("Отправят ли беспилотный корабль к Марсу в 2026?");
                event16.setStatus(EventStatus.ACTIVE);
                event16.setClosesAt(LocalDateTime.now().plusDays(15).toInstant(java.time.ZoneOffset.UTC));
                eventRepository.save(event16);

                EventOption[] options16 = {
                    createEventOption("Полетит на Марс", event16),
                    createEventOption("Останется на Земле/Луне", event16)
                };
                Arrays.stream(options16).forEach(eventOptionRepository::save);

                // Событие 17: Высадка на Луну (Artemis III)
                Event event17 = new Event();
                event17.setTitle("Высадка на Луну (Artemis III)");
                event17.setDescription("Состоится ли высадка астронавтов в 2026 году?");
                event17.setStatus(EventStatus.ACTIVE);
                event17.setClosesAt(LocalDateTime.now().plusDays(16).toInstant(java.time.ZoneOffset.UTC));
                eventRepository.save(event17);

                EventOption[] options17 = {
                    createEventOption("Успешная высадка", event17),
                    createEventOption("Перенос на 2027+", event17)
                };
                Arrays.stream(options17).forEach(eventOptionRepository::save);

                // Событие 18: Первый контакт
                Event event18 = new Event();
                event18.setTitle("Первый контакт");
                event18.setDescription("Найдут ли признаки жизни на экзопланетах?");
                event18.setStatus(EventStatus.ACTIVE);
                event18.setClosesAt(LocalDateTime.now().plusDays(17).toInstant(java.time.ZoneOffset.UTC));
                eventRepository.save(event18);

                EventOption[] options18 = {
                    createEventOption("Да, найдут жизнь", event18),
                    createEventOption("Нет, мы одни", event18)
                };
                Arrays.stream(options18).forEach(eventOptionRepository::save);

                // Событие 19: Neuralink для всех
                Event event19 = new Event();
                event19.setTitle("Neuralink для всех");
                event19.setDescription("Станет ли чип доступен для здоровых людей?");
                event19.setStatus(EventStatus.ACTIVE);
                event19.setClosesAt(LocalDateTime.now().plusDays(18).toInstant(java.time.ZoneOffset.UTC));
                eventRepository.save(event19);

                EventOption[] options19 = {
                    createEventOption("Доступен всем", event19),
                    createEventOption("Только по мед. показаниям", event19)
                };
                Arrays.stream(options19).forEach(eventOptionRepository::save);

                // Событие 20: Ядерный синтез
                Event event20 = new Event();
                event20.setTitle("Ядерный синтез");
                event20.setDescription("Достигнут ли чистого прироста энергии (Q>10)?");
                event20.setStatus(EventStatus.ACTIVE);
                event20.setClosesAt(LocalDateTime.now().plusDays(19).toInstant(java.time.ZoneOffset.UTC));
                eventRepository.save(event20);

                EventOption[] options20 = {
                    createEventOption("Да, прорыв (Q>10)", event20),
                    createEventOption("Нет, рано", event20)
                };
                Arrays.stream(options20).forEach(eventOptionRepository::save);

                // Событие 21: Евровидение 2026
                Event event21 = new Event();
                event21.setTitle("Евровидение 2026");
                event21.setDescription("Какая страна победит?");
                event21.setStatus(EventStatus.ACTIVE);
                event21.setClosesAt(LocalDateTime.now().plusDays(20).toInstant(java.time.ZoneOffset.UTC));
                eventRepository.save(event21);

                EventOption[] options21 = {
                    createEventOption("Швеция", event21),
                    createEventOption("Италия", event21),
                    createEventOption("Франция", event21),
                    createEventOption("Украина", event21)
                };
                Arrays.stream(options21).forEach(eventOptionRepository::save);

                // Событие 22: MrBeast: 500 млн подписчиков
                Event event22 = new Event();
                event22.setTitle("MrBeast: 500 млн подписчиков");
                event22.setDescription("Наберет ли канал 500 млн сабов до конца 2026?");
                event22.setStatus(EventStatus.ACTIVE);
                event22.setClosesAt(LocalDateTime.now().plusDays(21).toInstant(java.time.ZoneOffset.UTC));
                eventRepository.save(event22);

                EventOption[] options22 = {
                    createEventOption("Да, 500 млн+", event22),
                    createEventOption("Нет, меньше", event22)
                };
                Arrays.stream(options22).forEach(eventOptionRepository::save);

                // Событие 23: Грэмми 2026: Альбом года
                Event event23 = new Event();
                event23.setTitle("Грэмми 2026: Альбом года");
                event23.setDescription("Кто заберет статуэтку?");
                event23.setStatus(EventStatus.ACTIVE);
                event23.setClosesAt(LocalDateTime.now().plusDays(2).toInstant(java.time.ZoneOffset.UTC));
                eventRepository.save(event23);

                EventOption[] options23 = {
                    createEventOption("Billie Eilish", event23),
                    createEventOption("Taylor Swift", event23),
                    createEventOption("Новый артист", event23)
                };
                Arrays.stream(options23).forEach(eventOptionRepository::save);

                // Событие 24: Half-Life 3
                Event event24 = new Event();
                event24.setTitle("Half-Life 3");
                event24.setDescription("Анонсирует ли Valve игру в 2026 году?");
                event24.setStatus(EventStatus.ACTIVE);
                event24.setClosesAt(LocalDateTime.now().plusDays(23).toInstant(java.time.ZoneOffset.UTC));
                eventRepository.save(event24);

                EventOption[] options24 = {
                    createEventOption("ДА! Анонс!", event24),
                    createEventOption("Нет, это миф", event24)
                };
                Arrays.stream(options24).forEach(eventOptionRepository::save);

                // Событие 25: Twitter (X) подписка
                Event event25 = new Event();
                event25.setTitle("Twitter (X) подписка");
                event25.setDescription("Сделают ли вход платным для всех?");
                event25.setStatus(EventStatus.ACTIVE);
                event25.setClosesAt(LocalDateTime.now().plusDays(24).toInstant(java.time.ZoneOffset.UTC));
                eventRepository.save(event25);

                EventOption[] options25 = {
                    createEventOption("Да, платно для всех", event25),
                    createEventOption("Нет, останется бесплатным", event25)
                };
                Arrays.stream(options25).forEach(eventOptionRepository::save);

                // Событие 26: Spotify Wrapped 2025
                Event event26 = new Event();
                event26.setTitle("Spotify Wrapped 2025");
                event26.setDescription("Дата релиза итогов года");
                event26.setStatus(EventStatus.CLOSED);
                event26.setClosesAt(LocalDateTime.now().minusDays(2).toInstant(java.time.ZoneOffset.UTC));
                eventRepository.save(event26);

                EventOption[] options26 = {
                    createEventOption("До 5 декабря", event26),
                    createEventOption("После 5 декабря", event26)
                };
                Arrays.stream(options26).forEach(eventOptionRepository::save);

                // Событие 27: Цвет года Pantone 2026
                Event event27 = new Event();
                event27.setTitle("Цвет года Pantone 2026");
                event27.setDescription("Каким будет главный цвет года?");
                event27.setStatus(EventStatus.ACTIVE);
                event27.setClosesAt(LocalDateTime.now().plusDays(25).toInstant(java.time.ZoneOffset.UTC));
                eventRepository.save(event27);

                EventOption[] options27 = {
                    createEventOption("Да, объявили", event27),
                    createEventOption("Нет, еще ждут", event27)
                };
                Arrays.stream(options27).forEach(eventOptionRepository::save);

                // Событие 28: Трейлер GTA VI (2)
                Event event28 = new Event();
                event28.setTitle("Трейлер GTA VI (2)");
                event28.setDescription("Выход второго трейлера");
                event28.setStatus(EventStatus.ACTIVE);
                event28.setClosesAt(LocalDateTime.now().plusDays(26).toInstant(java.time.ZoneOffset.UTC));
                eventRepository.save(event28);

                EventOption[] options28 = {
                    createEventOption("Вышел", event28),
                    createEventOption("Не вышел", event28)
                };
                Arrays.stream(options28).forEach(eventOptionRepository::save);

                // Событие 29: Распродажа "Черная Пятница"
                Event event29 = new Event();
                event29.setTitle("Распродажа \"Черная Пятница\"");
                event29.setDescription("Побит ли рекорд продаж?");
                event29.setStatus(EventStatus.FINISHED);
                event29.setClosesAt(LocalDateTime.now().minusDays(3).toInstant(java.time.ZoneOffset.UTC));
                eventRepository.save(event29);

                EventOption[] options29 = {
                    createEventOption("Да, новый рекорд", event29),
                    createEventOption("Нет, спад продаж", event29)
                };
                // Устанавливаем правильные исходы для завершенного события
                options29[0].setIsCorrectOutcome(true);
                options29[1].setIsCorrectOutcome(false);
                Arrays.stream(options29).forEach(eventOptionRepository::save);

                // Событие 30: Apple December Surprise
                Event event30 = new Event();
                event30.setTitle("Apple December Surprise");
                event30.setDescription("Был ли внезапный анонс гаджетов?");
                event30.setStatus(EventStatus.FINISHED);
                event30.setClosesAt(LocalDateTime.now().minusDays(4).toInstant(java.time.ZoneOffset.UTC));
                eventRepository.save(event30);

                EventOption[] options30 = {
                    createEventOption("Да, был анонс", event30),
                    createEventOption("Нет, тишина", event30)
                };
                // Устанавливаем правильные исходы для завершенного события
                options30[0].setIsCorrectOutcome(false);
                options30[1].setIsCorrectOutcome(true);
                Arrays.stream(options30).forEach(eventOptionRepository::save);

                System.out.println("События и опции созданы");
            } else {
                System.out.println("События уже существуют");
            }

            // Создание призов
            if (prizeRepository.count() == 0) {
                System.out.println("Создание призов...");
                Prize prize1 = new Prize();
                prize1.setTitle("Футболка Prediction App");
                prize1.setTicketPrice(1);
                prize1.setDrawDate(LocalDateTime.now().plusDays(30).toInstant(java.time.ZoneOffset.UTC));
                prize1.setStatus(org.example.prediction.models.enums.PrizeStatus.OPEN);
                prizeRepository.save(prize1);

                Prize prize2 = new Prize();
                prize2.setTitle("iPhone 15 Pro");
                prize2.setTicketPrice(50);
                prize2.setDrawDate(LocalDateTime.now().plusDays(60).toInstant(java.time.ZoneOffset.UTC));
                prize2.setStatus(org.example.prediction.models.enums.PrizeStatus.OPEN);
                prizeRepository.save(prize2);

                Prize prize3 = new Prize();
                prize3.setTitle("VIP Статус");
                prize3.setTicketPrice(10);
                prize3.setDrawDate(LocalDateTime.now().plusDays(45).toInstant(java.time.ZoneOffset.UTC));
                prize3.setStatus(org.example.prediction.models.enums.PrizeStatus.OPEN);
                prizeRepository.save(prize3);

                System.out.println("Призы созданы");
            } else {
                System.out.println("Призы уже существуют");
            }

            System.out.println("Инициализация данных завершена");
        };
    }

   private EventOption createEventOption(String text, Event event) {
       EventOption option = new EventOption();
       option.setText(text);
       option.setEvent(event);
       return option;
   }
}