# PredictionApp

Приложение `PredictionApp` — это веб-сервис на Spring Boot для организации прогнозов, ставок и розыгрышей призов.

## Обзор

Система реализует следующие ключевые сценарии:

- регистрация и аутентификация пользователей;
- просмотр списка событий и детальной информации о каждом событии;
- создание событий и вариантов прогнозов администраторами;
- оформление предсказания для зарегистрированных пользователей;
- управление розыгрышами призов, покупка билетов и автоматический/ручной выбор победителя;
- панель администратора с базовой статистикой и списком пользователей.

## Архитектура приложения

Проект разделён на следующие слои:

- `org.example.prediction.web` — контроллеры MVC;
- `org.example.prediction.services` — бизнес-логика;
- `org.example.prediction.repositories` — JPA-репозитории для доступа к БД;
- `org.example.prediction.models.entities` — сущности доменной модели;
- `org.example.prediction.models.enums` — перечисления статусов и ролей;
- `org.example.prediction.dto` — DTO и формы для представления данных;
- `org.example.prediction.config` — конфигурация Spring, безопасность, кэширование, инициализация данных.

## Основные модули

### `PredictionApplication`

Точка входа приложения. Класс помечен `@SpringBootApplication` и включает `@EnableScheduling` для выполнения запланированных задач.

### Конфигурация

#### `SecurityConfig`

- без авторизации доступны `/api/auth/**`, `/api/events/all` и страница ошибок `/error`;
- административные операции (`/api/events/create`, `/api/events/{id}/resolve`, `/api/admin/**`) — только роль `ADMIN` (`@PreAuthorize`);
- аутентификация сессионная: Spring Session хранит сессии в Redis; вход/выход обрабатывает Spring Security по `/api/auth/login` и `/api/auth/logout` (JSON-ответы), неавторизованные запросы к API получают `401 Unauthorized`;
- CSRF отключён (REST API + SPA); настроен CORS для origins `http://localhost:3000` и `http://127.0.0.1:3000` с `allowCredentials(true)`;
- использует `BCryptPasswordEncoder`.

#### `BeanConfiguration`

- включает кэширование (`@EnableCaching`);
- включает аудит JPA (`@EnableJpaAuditing`);
- создаёт `ModelMapper` для конвертации DTO/сущностей.

#### `RedisConfig`

- настраивает Redis в качестве механизма кэша, если `spring.cache.type=redis`;
- задаёт сериализацию ключей/значений и TTL 10 минут.

#### `DataInitializer`

- инициализирует начальные данные при старте приложения:
  - администратор `admin / 12345`;
  - пользователь `user1 / 12345`;
  - набор тестовых событий и вариантов;
- используется при отсутствии данных в базе.

## Доменные сущности

### `User`

Поля:

- `username`, `email`, `password`;
- `role` (`USER` или `ADMIN`);
- `successfulPredictions` — число выигранных прогнозов;
- `balance` — счёт пользователя для покупки билетов.

### `Event`

Поля:

- `title`, `description`;
- `status` (`ACTIVE`, `CLOSED`, `FINISHED`);
- `closesAt` — время закрытия приёма ставок;
- `options` — набор вариантов прогноза.

### `EventOption`

- содержит текст варианта прогноза;
- поле `isCorrectOutcome` для пометки победной опции после завершения события.

### `Prediction`

- связь с пользователем, событием и выбранной опцией;
- статус `PLACED`, `WON`, `LOST`.

### `Prize`

- заголовок, цена билета, дата розыгрыша;
- статус `OPEN` / `CLOSED`;
- победитель `winner` после проведения розыгрыша.

### `Ticket`

- связь между пользователем и призом;
- один билет = одна запись.

### `BaseEntity`

- содержит `id`, `createdAt`, `updatedAt`;
- аудит создаётся с помощью Spring Data JPA.

## Репозитории

- `UserRepository` — поиск по логину и email;
- `EventRepository` — поиск событий, выборка с опциями, выбор событий для закрытия;
- `EventOptionRepository` — CRUD для опций события;
- `PredictionRepository` — проверка существующих прогнозов, поиск по событию и пользователю;
- `PrizeRepository` — поиск призов и поиск по названию;
- `TicketRepository` — выборка билетов для конкретного приза.

## Сервисы

### `AuthServiceImpl`

- регистрирует пользователя;
- проверяет уникальность имени;
- шифрует пароль `BCrypt`;
- сохраняет роль `USER`.

### `EventServiceImpl`

- создаёт событие с опциями;
- кэширует список `events` и детали события по id;
- удаляет событие вместе с предсказаниями;
- закрывает событие (`FINISHED`) и отмечает выигравшую опцию;
- пересчитывает статусы прогнозов и увеличивает баланс победителя;
- проверяет, сделал ли пользователь ставку на событие;
- запускает задачу `closeExpiredEvents()` каждую минуту для автоматического перехода просроченных событий в статус `CLOSED`.

Ключевая логика:

- событие должно иметь минимум 2 варианта;
- ставку нельзя сделать, если событие `CLOSED` или `FINISHED`;
- при завершении события статус предсказания меняется на `WON` или `LOST`.

### `PredictionServiceImpl`

- оформляет ставку пользователя на конкретную опцию события;
- проверяет наличие пользователя, события и выбранной опции;
- запрещает множественные ставки на одно событие;
- делает ставку только если событие ещё активно и время ещё не истекло.

### `PrizeServiceImpl`

- создаёт призы с датой розыгрыша и статусом `OPEN`;
- продаёт билет, списывая баланс пользователя;
- при розыгрыше выбирает победителя случайным образом из купленных билетов;
- закрывает приз, если билетов нет;
- автоматический розыгрыш просроченных призов выполняется каждую минуту.

### `AdminService`

- возвращает список всех пользователей;
- собирает события, требующие ручного завершения;
- формирует статистику для панели администратора:
  - общее число пользователей;
  - число активных событий;
  - число предсказаний за текущий день.

## Контроллеры и маршруты

### `AuthController`

- `GET /login` — страница входа;
- `GET /register` — страница регистрации;
- `POST /register` — регистрация нового пользователя.

### `EventController`

- `GET /events/all` — список событий с поиском и пагинацией;
- `GET /events/details/{id}` — подробности события;
- `GET /events/add` — форма создания события (только `ADMIN`);
- `POST /events/add` — создание события (только `ADMIN`);
- `DELETE /events/delete/{id}` — удаление события (только `ADMIN`);
- `POST /events/{id}/finish` — завершение события и выбор выигравшей опции (только `ADMIN`).

### `PredictionController`

- `POST /predictions/make` — оформление прогноза зарегистрированным пользователем.

### `PrizeController`

- `GET /prizes` — просмотр всех призов;
- `POST /prizes/buy/{id}` — покупка билета;
- `POST /prizes/add` — создание приза (только `ADMIN`);
- `POST /prizes/draw/{id}` — ручной розыгрыш приза (только `ADMIN`).

### `AdminController`

- `GET /admin` — административная панель с базовой статистикой.

## Представление (Thymeleaf)

Основные шаблоны доступны в `src/main/resources/templates`:

- `index.html` — главная страница;
- `auth/login.html`, `auth/register.html` — формы авторизации;
- `events/all.html`, `events/details.html`, `events/add.html` — управление событиями;
- `prizes/all.html` — просмотр и покупка призов;
- `admin.html` — панель администратора;
- `users/dashboard.html`, `users/profile.html` — профиль и дашборд пользователя;
- `error/custom-error.html` — пользовательская страница ошибки;
- `fragments/head.html`, `fragments/navbar.html`, `fragments/footer.html` — общие фрагменты.

## Конфигурация базы и окружения

`src/main/resources/application.properties` содержит настройки:

- `server.port=8080`;
- PostgreSQL на `jdbc:postgresql://localhost:5433/prediction_db?currentSchema=prediction_schema`;
- Redis на `localhost:6379`;
- `spring.jpa.hibernate.ddl-auto=create` — создание схемы на старте;
- `spring.sql.init.mode=never` — SQL-файлы `db/schema.sql` и `db/data.sql` не выполняются автоматически;
- зато данные инициализируются через `DataInitializer`.

## Запуск приложения

1. Поднимите PostgreSQL и Redis через Docker Compose (PostgreSQL — порт **5433**, Redis — **6379**):

```bash
docker compose up -d
```

2. Создайте локальный конфиг из шаблона и при необходимости поправьте параметры:

```bash
cp src/main/resources/application.properties.example src/main/resources/application.properties
```

3. Запустите приложение (порт **8080**):

```bash
./mvnw spring-boot:run          # Linux/macOS
mvnw.cmd spring-boot:run        # Windows
```

или через установленный Maven:

```bash
mvn spring-boot:run
```

4. Запустите фронтенд из папки `frontend/` (подробнее — в корневом `README.md` репозитория):

```bash
npm install
npm run dev
```

5. Откройте в браузере:

```text
http://localhost:3000
```

REST API будет доступно на `http://localhost:8080/api/**`.

## Учётные данные для тестирования

- Админ: `admin / 12345`
- Пользователь: `user1 / 12345`

## Особенности и важные моменты

- В приложении используется ролевой доступ через Spring Security;
- все CRUD-операции с событиями и призами выполняются через сервисный слой;
- `EventServiceImpl` и `PrizeServiceImpl` используют `@Scheduled` задачи для автоматизации закрытия ставок и проведения розыгрышей;
- `ModelMapper` используется для преобразования DTO в сущности и обратно;
- кэширование событий реализовано через Redis, что ускоряет чтение списка и деталей событий.

## Структура проекта

```
src/main/java/org/example/prediction
  ├─ config
  │    ├─ BeanConfiguration.java
  │    ├─ DataInitializer.java
  │    ├─ RedisConfig.java
  │    └─ SecurityConfig.java
  ├─ dto
  │    ├─ ShowEventInfoDto.java
  │    ├─ ShowDetailedEventInfoDto.java
  │    ├─ PredictionDto.java
  │    ├─ OptionDto.java
  │    ├─ UserStatsDto.java
  │    ├─ form
  │    │    ├─ AddEventDto.java
  │    │    ├─ AddPredictionDto.java
  │    │    ├─ AddPrizeDto.java
  │    │    ├─ LoginDto.java
  │    │    └─ UserRegistrationDto.java
  │    └─ admin
  │         └─ AdminDashboardViewModel.java
  ├─ models
  │    ├─ entities
  │    │    ├─ BaseEntity.java
  │    │    ├─ Event.java
  │    │    ├─ EventOption.java
  │    │    ├─ Prediction.java
  │    │    ├─ Prize.java
  │    │    ├─ Ticket.java
  │    │    └─ User.java
  │    ├─ enums
  │    │    ├─ EventStatus.java
  │    │    ├─ PredictionStatus.java
  │    │    ├─ PrizeStatus.java
  │    │    └─ UserRole.java
  │    └─ exceptions
  │         └─ EventNotFoundException.java
  ├─ repositories
  │    ├─ EventOptionRepository.java
  │    ├─ EventRepository.java
  │    ├─ PredictionRepository.java
  │    ├─ PrizeRepository.java
  │    ├─ TicketRepository.java
  │    └─ UserRepository.java
  ├─ services
  │    ├─ AdminService.java
  │    ├─ AppUserDetailsService.java
  │    ├─ AuthService.java
  │    ├─ AuthServiceImpl.java
  │    ├─ EventService.java
  │    ├─ EventServiceImpl.java
  │    ├─ PredictionService.java
  │    ├─ PredictionServiceImpl.java
  │    ├─ PrizeService.java
  │    ├─ PrizeServiceImpl.java
  │    └─ DashboardServiceImpl.java
  └─ web
       ├─ AdminController.java
       ├─ AuthController.java
       ├─ CustomErrorController.java
       ├─ DashboardController.java
       ├─ EventController.java
       ├─ GlobalExceptionHandler.java
       ├─ HomeController.java
       ├─ PredictionController.java
       ├─ PrizeController.java
       └─ UserController.java
```

## Рекомендации

- если нужно запускать без PostgreSQL, можно адаптировать `application.properties` под H2;
- при деплое на продакшн следует отключить `spring.jpa.hibernate.ddl-auto=create` и включить `spring.sql.init.mode=always`/`never` в зависимости от стратегии миграций;
- для расширения возможностей прогнозов можно добавить множители коэффициентов и учёт ставок пользователей.
