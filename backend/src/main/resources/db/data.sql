/*
-- События (10 карточек)
INSERT INTO prediction_schema.events (title, description, status, closes_at) VALUES
('Оскар 2026: Лучший фильм', 'Какая картина заберет главную статуэтку?', 'ACTIVE', '2026-03-10 03:00:00'),
('Курс Bitcoin к Новому году', 'Преодолеет ли Bitcoin отметку в $100,000 к 31 декабря?', 'ACTIVE', '2025-12-31 23:59:00'),
('Игра года (TGA 2025)', 'Кто получит титул Game of the Year?', 'ACTIVE', '2025-12-12 04:00:00'),
('Погода: Первый снег в Москве', 'Выпадет ли устойчивый снег до 1 ноября?', 'ACTIVE', '2025-11-01 00:00:00'),
('SpaceX: Полет на Марс', 'Состоится ли успешная беспилотная посадка Starship на Марс в 2026?', 'ACTIVE', '2026-12-31 23:59:00'),
('Релиз GTA VI', 'В каком квартале выйдет игра?', 'ACTIVE', '2026-11-19 00:00:00'),
('Евровидение 2025', 'Страна-победитель музыкального конкурса', 'CLOSED', '2025-05-15 22:00:00'),
('Box Office: Лидер лета', 'Какой фильм соберет больше всего денег в прокате?', 'ACTIVE', '2025-09-01 00:00:00'),
('Apple iPhone Fold', 'Представят ли складной iPhone (Fold) в сентябре 2026?', 'ACTIVE', '2026-09-30 20:00:00'),
('Человек года TIME', 'Кого журнал TIME назовет человеком года: Человек или ИИ?', 'ACTIVE', '2025-12-10 12:00:00');

-- Опции

-- 1. Оскар
INSERT INTO prediction_schema.event_options (event_id, text) VALUES
(1, '«Дюна: Часть 3»'),
(1, 'Новый фильм Тарантино'),
(1, '«Аватар 3»');

-- 2. Биткоин
INSERT INTO prediction_schema.event_options (event_id, text) VALUES
(2, 'Да, будет выше $100k'),
(2, 'Нет, будет ниже $100k');

-- 3. Игра года
INSERT INTO prediction_schema.event_options (event_id, text) VALUES
(3, 'Death Stranding 2'),
(3, 'GTA VI'),
(3, 'Hollow Knight: Silksong');

-- 4. Погода
INSERT INTO prediction_schema.event_options (event_id, text) VALUES
(4, 'Да, выпадет'),
(4, 'Нет, будет позже');

-- 5. SpaceX
INSERT INTO prediction_schema.event_options (event_id, text) VALUES
(5, 'Успешная посадка'),
(5, 'Авария / Перенос');

-- 6. GTA VI
INSERT INTO prediction_schema.event_options (event_id, text) VALUES
(6, 'Q4 2026'),
(6, 'Перенос на 2027');

-- 7. Евровидение (закрыто)
INSERT INTO prediction_schema.event_options (event_id, text, is_correct_outcome) VALUES
(7, 'Швейцария', TRUE),
(7, 'Хорватия', FALSE),
(7, 'Франция', FALSE);

-- 8. Box Office
INSERT INTO prediction_schema.event_options (event_id, text) VALUES
(8, 'Миссия невыполнима 8'),
(8, 'Супермен: Наследие'),
(8, 'Фантастическая четвёрка');

-- 9. iPhone Fold
INSERT INTO prediction_schema.event_options (event_id, text) VALUES
(9, 'Да, покажут iPhone Fold'),
(9, 'Нет, только обычный iPhone 17/18');

-- 10. TIME
INSERT INTO prediction_schema.event_options (event_id, text) VALUES
(10, 'Конкретная персона (человек)'),
(10, 'Искусственный интеллект (технология)'),
(10, 'Собирательный образ (группа людей)');



-- ==========================================
-- 1. ОЧИСТКА И СБРОС
-- ==========================================
TRUNCATE TABLE prediction_schema.predictions CASCADE;
TRUNCATE TABLE prediction_schema.event_options CASCADE;
TRUNCATE TABLE prediction_schema.events CASCADE;

ALTER SEQUENCE prediction_schema.events_id_seq RESTART WITH 1;
ALTER SEQUENCE prediction_schema.event_options_id_seq RESTART WITH 1;
ALTER SEQUENCE prediction_schema.predictions_id_seq RESTART WITH 1;


-- ==========================================
-- 2. ВСТАВКА СОБЫТИЙ (30 шт.)
-- Все даты > 05.12.2025
-- ==========================================

INSERT INTO prediction_schema.events (title, description, status, closes_at) VALUES

-- --- 1-5: ГОРЯЧИЕ / ДЕКАБРЬ 2025 (ACTIVE) ---
('The Game Awards 2025', 'Кто станет Игрой Года (GOTY)?', 'ACTIVE', '2025-12-12 18:00:00'),
('Курс Bitcoin: $150k до курантов', 'Пробьет ли Биткоин отметку $150,000 до конца года?', 'ACTIVE', '2025-12-31 23:59:00'),
('Погода в Москве: Новый год', 'Будет ли снег и мороз в новогоднюю ночь?', 'ACTIVE', '2025-12-31 18:00:00'),
('Человек года TIME 2025', 'Кого журнал выберет человеком года?', 'CLOSED', '2025-12-10 12:00:00'),
('Box Office: "Муфаса: Король Лев"', 'Соберет ли фильм 1 млрд долларов?', 'ACTIVE', '2025-12-31 23:59:00'),

-- --- 6-10: ТЕХНОЛОГИИ 2026 (ACTIVE) ---
('Apple Vision Pro 2', 'Анонсируют ли бюджетную версию очков в 2026?', 'ACTIVE', '2026-06-05 10:00:00'),
('OpenAI: Релиз GPT-5', 'Выйдет ли модель GPT-5 в первом квартале 2026?', 'ACTIVE', '2026-03-31 23:59:00'),
('Консоль Nintendo Switch 2', 'Будет ли цена на старте выше $400?', 'ACTIVE', '2026-02-01 00:00:00'),
('Tesla Optimus Robot', 'Начнутся ли массовые продажи робота?', 'ACTIVE', '2026-12-01 00:00:00'),
('Windows 12 Release', 'Выпустит ли Microsoft новую ОС?', 'ACTIVE', '2026-10-01 00:00:00'),

-- --- 11-15: КИНО И СЕРИАЛЫ (ACTIVE) ---
('Оскар 2026: Лучший фильм', 'Кто получит главную кинонаграду?', 'ACTIVE', '2026-03-02 03:00:00'),
('Шрек 5: Трейлер', 'Покажут ли первый трейлер до лета 2026?', 'ACTIVE', '2026-05-31 23:59:00'),
('Новый Джеймс Бонд', 'Объявят ли имя нового актера официально?', 'ACTIVE', '2026-08-01 00:00:00'),
('Сериал по Гарри Поттеру', 'Назовут ли дату премьеры первого сезона?', 'ACTIVE', '2026-12-31 23:59:00'),
('Аватар 3 (Fire and Ash)', 'Станет ли самым кассовым фильмом в истории?', 'ACTIVE', '2026-04-01 00:00:00'),

-- --- 16-20: КОСМОС И НАУКА (ACTIVE) ---
('SpaceX Starship: Марс', 'Отправят ли беспилотный корабль к Марсу в 2026?', 'ACTIVE', '2026-11-01 00:00:00'),
('Высадка на Луну (Artemis III)', 'Состоится ли высадка астронавтов в 2026 году?', 'ACTIVE', '2026-12-31 23:59:00'),
('Первый контакт', 'Найдут ли признаки жизни на экзопланетах?', 'ACTIVE', '2026-12-31 23:59:00'),
('Neuralink для всех', 'Станет ли чип доступен для здоровых людей?', 'ACTIVE', '2026-12-31 23:59:00'),
('Ядерный синтез', 'Достигнут ли чистого прироста энергии (Q>10)?', 'ACTIVE', '2026-12-31 23:59:00'),

-- --- 21-25: ШОУ-БИЗНЕС (ACTIVE) ---
('Евровидение 2026', 'Какая страна победит?', 'ACTIVE', '2026-05-16 22:00:00'),
('MrBeast: 500 млн подписчиков', 'Наберет ли канал 500 млн сабов до конца 2026?', 'ACTIVE', '2026-12-31 23:59:00'),
('Грэмми 2026: Альбом года', 'Кто заберет статуэтку?', 'ACTIVE', '2026-02-04 04:00:00'),
('Half-Life 3', 'Анонсирует ли Valve игру в 2026 году?', 'ACTIVE', '2026-12-31 23:59:00'),
('Twitter (X) подписка', 'Сделают ли вход платным для всех?', 'ACTIVE', '2026-06-01 00:00:00'),

-- --- 26-28: CLOSED (Прием ставок закрыт, но дата в будущем) ---
('Spotify Wrapped 2025', 'Дата релиза итогов года', 'CLOSED', '2025-12-10 10:00:00'),
('Цвет года Pantone 2026', 'Каким будет главный цвет года?', 'ACTIVE', '2025-12-12 12:00:00'),
('Трейлер GTA VI (2)', 'Выход второго трейлера', 'ACTIVE', '2025-12-15 18:00:00'),

-- --- 29-30: FINISHED (Завершены досрочно, дата в будущем) ---
('Распродажа "Черная Пятница"', 'Побит ли рекорд продаж?', 'FINISHED', '2025-12-06 00:00:00'),
('Apple December Surprise', 'Был ли внезапный анонс гаджетов?', 'FINISHED', '2025-12-07 10:00:00');


-- ==========================================
-- 3. ВСТАВКА ОПЦИЙ
-- ==========================================

-- 1. TGA 2025
INSERT INTO prediction_schema.event_options (event_id, text) VALUES
(1, 'GTA VI (если вышла)'), (1, 'Death Stranding 2'), (1, 'Metaphor: ReFantazio'), (1, 'Другая игра');

-- 2. Bitcoin 150k
INSERT INTO prediction_schema.event_options (event_id, text) VALUES
(2, 'Да, будет выше $150k'), (2, 'Нет, не дотянет');

-- 3. Погода НГ
INSERT INTO prediction_schema.event_options (event_id, text) VALUES
(3, 'Снег и мороз (< -5)'), (3, 'Слякоть / Оттепель'), (3, 'Сухой мороз');

-- 4. TIME Person
INSERT INTO prediction_schema.event_options (event_id, text) VALUES
(4, 'Сэм Альтман (OpenAI)'), (4, 'Илон Маск'), (4, 'Тейлор Свифт'), (4, 'Искусственный Интеллект');

-- 5. Король Лев Касса
INSERT INTO prediction_schema.event_options (event_id, text) VALUES
(5, 'Да, > $1 млрд'), (5, 'Нет, провалится');

-- 6. Vision Pro 2
INSERT INTO prediction_schema.event_options (event_id, text) VALUES
(6, 'Да, покажут бюджетную'), (6, 'Нет, только Pro');

-- 7. GPT-5
INSERT INTO prediction_schema.event_options (event_id, text) VALUES
(7, 'Да, Q1 2026'), (7, 'Нет, перенесут');

-- 8. Switch 2
INSERT INTO prediction_schema.event_options (event_id, text) VALUES
(8, 'Дороже $400'), (8, '$399 или дешевле');

-- 9. Tesla Bot
INSERT INTO prediction_schema.event_options (event_id, text) VALUES
(9, 'Да, можно будет купить'), (9, 'Нет, только прототипы');

-- 10. Windows 12
INSERT INTO prediction_schema.event_options (event_id, text) VALUES
(10, 'Да, релиз в 2026'), (10, 'Нет, только обновление Win 11');

-- 11. Оскар Фильм
INSERT INTO prediction_schema.event_options (event_id, text) VALUES
(11, 'Дюна: Часть 3'), (11, 'Фильм Нолана'), (11, 'Аватар 3'), (11, 'Другой фильм');

-- 12. Шрек 5
INSERT INTO prediction_schema.event_options (event_id, text) VALUES
(12, 'Покажут трейлер'), (12, 'Тишина до 2027');

-- 13. Бонд
INSERT INTO prediction_schema.event_options (event_id, text) VALUES
(13, 'Аарон Тейлор-Джонсон'), (13, 'Генри Кавилл'), (13, 'Неизвестный актер');

-- 14. Harry Potter TV
INSERT INTO prediction_schema.event_options (event_id, text) VALUES
(14, 'Да, назовут дату'), (14, 'Нет новостей');

-- 15. Аватар 3 рекорд
INSERT INTO prediction_schema.event_options (event_id, text) VALUES
(15, 'Да, побьет рекорд'), (15, 'Нет, соберет много, но не топ-1');

-- 16. Starship Mars
INSERT INTO prediction_schema.event_options (event_id, text) VALUES
(16, 'Полетит на Марс'), (16, 'Останется на Земле/Луне');

-- 17. Artemis III
INSERT INTO prediction_schema.event_options (event_id, text) VALUES
(17, 'Успешная высадка'), (17, 'Перенос на 2027+');

-- 18. Aliens
INSERT INTO prediction_schema.event_options (event_id, text) VALUES
(18, 'Да, найдут жизнь'), (18, 'Нет, мы одни');

-- 19. Neuralink
INSERT INTO prediction_schema.event_options (event_id, text) VALUES
(19, 'Доступен всем'), (19, 'Только по мед. показаниям');

-- 20. Fusion Energy
INSERT INTO prediction_schema.event_options (event_id, text) VALUES
(20, 'Да, прорыв (Q>10)'), (20, 'Нет, рано');

-- 21. Евровидение
INSERT INTO prediction_schema.event_options (event_id, text) VALUES
(21, 'Швеция'), (21, 'Италия'), (21, 'Франция'), (21, 'Украина');

-- 22. MrBeast
INSERT INTO prediction_schema.event_options (event_id, text) VALUES
(22, 'Да, 500 млн+'), (22, 'Нет, меньше');

-- 23. Грэмми
INSERT INTO prediction_schema.event_options (event_id, text) VALUES
(23, 'Billie Eilish'), (23, 'Taylor Swift'), (23, 'Новый артист');

-- 24. Half-Life 3
INSERT INTO prediction_schema.event_options (event_id, text) VALUES
(24, 'ДА! Анонс!'), (24, 'Нет, это миф');

-- 25. Twitter Paid
INSERT INTO prediction_schema.event_options (event_id, text) VALUES
(25, 'Да, платно для всех'), (25, 'Нет, останется бесплатным');

-- 26. Spotify Wrapped (CLOSED)
INSERT INTO prediction_schema.event_options (event_id, text) VALUES
(26, 'До 5 декабря'), (26, 'После 5 декабря');

-- 27. Pantone (CLOSED)
INSERT INTO prediction_schema.event_options (event_id, text) VALUES
(27, 'Да, объявили'), (27, 'Нет, еще ждут');

-- 28. GTA VI Trailer 2 (CLOSED)
INSERT INTO prediction_schema.event_options (event_id, text) VALUES
(28, 'Вышел'), (28, 'Не вышел');

-- 29. Sales (FINISHED)
INSERT INTO prediction_schema.event_options (event_id, text, is_correct_outcome) VALUES
(29, 'Да, новый рекорд', TRUE), (29, 'Нет, спад продаж', FALSE);

-- 30. Apple Surprise (FINISHED)
INSERT INTO prediction_schema.event_options (event_id, text, is_correct_outcome) VALUES
(30, 'Да, был анонс', FALSE), (30, 'Нет, тишина', TRUE);

-- ПРИЗЫ
INSERT INTO prediction_schema.prizes (title, ticket_price, draw_date, status) VALUES
('Футболка Prediction App', 1, '2026-01-01 12:00:00', 'OPEN'),
('iPhone 15 Pro', 50, '2026-03-01 12:00:00', 'OPEN'),
('VIP Статус', 10, '2025-12-31 23:59:00', 'OPEN');*/