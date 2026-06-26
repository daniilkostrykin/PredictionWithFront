# PredictionApp · backend

Сервис платформы PredictionApp: REST API `/api/**` для SPA-клиента и серверные Thymeleaf-страницы розыгрышей `/prizes`. Стек: Java 21, Spring Boot 3.5 (Web, Security, Data JPA, Validation, Cache), Spring Session Data Redis, PostgreSQL 18, Redis, Lombok, ModelMapper, Maven Wrapper.

![Java 21](https://img.shields.io/badge/Java-21-007396?logo=openjdk&logoColor=white) ![Spring Boot 3.5](https://img.shields.io/badge/Spring%20Boot-3.5-6DB33F?logo=spring&logoColor=white) ![PostgreSQL 18](https://img.shields.io/badge/PostgreSQL-18-4169E1?logo=postgresql&logoColor=white) ![Redis](https://img.shields.io/badge/Redis-DC382D?logo=redis&logoColor=white)

Обзор проекта, сценарии и полный контракт API — в корневом [`README.md`](../README.md).

## Слои

| Пакет | Назначение |
|---|---|
| `web` | REST-контроллеры, обработчик ошибок |
| `services` | Бизнес-логика, планировщики |
| `repositories` | Spring Data JPA |
| `models.entities` · `models.enums` | Доменная модель и статусы |
| `dto` (+ `dto.form`, `dto.admin`) | DTO запросов/ответов и формы |
| `config` | `SecurityConfig` (доступ, сессии, CORS), `RedisConfig` (кэш, TTL), `BeanConfiguration` (`@EnableCaching`, `@EnableJpaAuditing`, `ModelMapper`), `DataInitializer` (демо-данные) |
| `utils.validation` | Валидаторы: уникальность username/email, совпадение паролей |

Точка входа — `PredictionApplication` (`@SpringBootApplication`, `@EnableScheduling`).

## Доменная модель

| Сущность | Ключевые поля | Статусы |
|---|---|---|
| `User` | username, email, password (BCrypt), role, successfulPredictions, balance | `USER`, `ADMIN` |
| `Event` | title, description, closesAt (Instant), options | `ACTIVE` → `CLOSED` → `FINISHED` |
| `EventOption` | text, isCorrectOutcome | — |
| `Prediction` | user, event, chosenOption | `PLACED`, `WON`, `LOST` |
| `Prize` | title, ticketPrice, drawDate, winner | `OPEN`, `CLOSED` |
| `Ticket` | user, prize, createdAt | — |

`BaseEntity` добавляет `id`, `createdAt`, `updatedAt` (JPA-аудит, `@EnableJpaAuditing`).

Репозитории Spring Data: `UserRepository` (поиск по логину/email), `EventRepository` (выборка с опциями, события к закрытию), `EventOptionRepository`, `PredictionRepository` (проверки дубликатов, выборки по событию/пользователю), `PrizeRepository` (поиск по названию), `TicketRepository` (билеты приза).

## Бизнес-правила

### События и прогнозы (`EventServiceImpl`, `PredictionServiceImpl`)

- Создание события: ≥ 2 непустых опций, `closesAt` в будущем; статус `ACTIVE`; кэш списка сбрасывается.
- Прогноз: пользователь, событие и опция должны существовать; один прогноз на событие; только для `ACTIVE` и до `closesAt` — иначе `IllegalStateException` → `400`.
- `finishEvent` (итоги): событие не `FINISHED`, опция принадлежит событию; статус `FINISHED`, опция помечается `isCorrectOutcome`; угадавшие прогнозы → `WON` c `successfulPredictions +1` и `balance +1`, остальные → `LOST`.
- `closeExpiredEvents` — `@Scheduled(fixedRate = 60_000)`: просроченные `ACTIVE` → `CLOSED` (приём предсказаний закрыт, итоги ещё не подведены).
- `deleteEvent` удаляет событие вместе с его прогнозами.

### Розыгрыши (`PrizeServiceImpl`)

- Создание: `ticketPrice ≥ 1`, `drawDate` в будущем, статус `OPEN`.
- `buyTicket`: розыгрыш `OPEN`, дата не прошла, `balance ≥ ticketPrice`; списывает `balance` и создаёт `Ticket`. Транзакционный метод.
- `performDraw`: нет билетов → `CLOSED` без победителя; иначе случайный билет → победитель, `CLOSED`.
- `drawExpiredPrizes` — `@Scheduled(fixedRate = 60_000)`: автоматический розыгрыш просроченных `OPEN`.

### Кэширование

`@Cacheable` — список событий (`events`) и детали по id (`event`); `@CacheEvict` — при создании, удалении, подведении итогов и ежеминутной проверке дедлайнов. Хранилище — Redis, TTL 10 минут (`spring.cache.redis.time-to-live`, сериализация JSON в `RedisConfig`).

### Аутентификация (`AuthServiceImpl`, `AppUserDetailsService`)

- регистрация: проверка уникальности username/email, хеширование BCrypt, роль `USER`;
- вход/выход обрабатывает Spring Security (см. «Безопасность»); текущий пользователь загружается `AppUserDetailsService` по username.

## Безопасность (`SecurityConfig`)

| Правило | Значение |
|---|---|
| `permitAll` | `/api/auth/**`, `/api/events/all`, `/error` |
| Остальные запросы | `authenticated`; без сессии → `401` (`HttpStatusEntryPoint`) |
| Админ-операции | `@PreAuthorize("hasRole('ADMIN')")` на эндпоинтах |
| Логин | formLogin на `/api/auth/login` (form-urlencoded), JSON-обработчики успеха/неудачи |
| Логаут | `/api/auth/logout`, JSON-ответ |
| CSRF | отключён (REST + SPA) |
| CORS | `http://localhost:3000`, `http://127.0.0.1:3000`, `allowCredentials=true` |
| Пароли | `BCryptPasswordEncoder` |

Сессии хранятся в Redis (Spring Session); cookie `SESSION` — HttpOnly, SameSite=lax, secure=false (dev-настройка в `application.properties`).

## Конфигурация

```bash
cp src/main/resources/application.properties.example src/main/resources/application.properties
```

Файл добавлен в `.gitignore` — секреты не попадают в репозиторий. Ключевые параметры:

| Свойство | По умолчанию | Комментарий |
|---|---|---|
| `server.port` | `8080` | порт REST API и Thymeleaf-страниц |
| `spring.datasource.url` | `jdbc:postgresql://localhost:5433/prediction_db?currentSchema=prediction_schema` | совпадает с docker-compose |
| `spring.datasource.username` / `password` | `user` / `pass` | учётка контейнера PostgreSQL |
| `spring.data.redis.host` / `port` | `localhost` / `6379` | сессии + кэш |
| `spring.cache.type` / `…redis.time-to-live` | `redis` / `600000` | TTL 10 минут |
| `spring.jpa.hibernate.ddl-auto` | `create` | ⚠ схема пересоздаётся при каждом старте, данные теряются; для продакшена — `validate`/`update` |
| `spring.sql.init.mode` | `never` | `db/schema.sql` и `db/data.sql` применяет только контейнер PostgreSQL при первом запуске |
| `server.servlet.session.cookie.*` | `SESSION`, lax, http-only, secure=false | dev-режим |

### docker-compose.yml

- `postgres:18` — `5433:5432`, БД `prediction_db`, учётка `user/pass`, том `prediction-db-data`; скрипты `db/schema.sql` и `db/data.sql` монтируются в `docker-entrypoint-initdb.d` и выполняются только при первом создании тома;
- `redis:alpine` — `6379:6379`.

## Запуск и тесты

```bash
docker compose up -d        # инфраструктура: PostgreSQL :5433, Redis :6379
./mvnw spring-boot:run      # Linux/macOS
mvnw.cmd spring-boot:run    # Windows
```

REST API — `http://localhost:8080/api/**`; фронтенд запускается из `frontend/` (см. корневой [`README.md`](../README.md)).

Тестовые учётные данные (`DataInitializer`, создаются при отсутствии в БД): админ `admin / 12345`, пользователь `user1 / 12345`, набор тестовых событий.

Тесты — JUnit 5 + Mockito, профиль `test`: H2 in-memory (`create-drop`) и `spring.cache.type=none` (Redis-автоконфигурация исключена) — Docker не нужен:

```bash
./mvnw test
```

Покрытие: `AdminIntegrationTest`, `AdminScenarioTest`, `ConcurrencyTest`, `LogicTest`, `PrizeControllerTest`, `PrizeEdgeCaseTest`, `PrizeServiceTest`, `UserScenarioTest`.

## Структура

```text
src/main/java/org/example/prediction
├── PredictionApplication.java
├── config/             SecurityConfig, RedisConfig, BeanConfiguration, DataInitializer
├── web/                Auth, Event, Prediction, Dashboard, User, Admin, Prize, Home,
│                       CustomError контроллеры + GlobalExceptionHandler
├── services/           EventServiceImpl, PredictionServiceImpl, PrizeServiceImpl,
│                       AuthServiceImpl, DashboardServiceImpl, AdminService,
│                       AppUserDetailsService
├── repositories/       User, Event, EventOption, Prediction, Prize, Ticket
├── dto/                ShowEventInfoDto, ShowDetailedEventInfoDto, Option, Prediction,
│                       UserStats, Dashboard + form/ + admin/
├── models/
│   ├── entities/       BaseEntity, User, Event, EventOption, Prediction, Prize, Ticket
│   ├── enums/          EventStatus, PredictionStatus, PrizeStatus, UserRole
│   └── exceptions/     EventNotFoundException
└── utils/validation/   UniqueUsername, UniqueEmail, PasswordMatches

src/main/resources
├── application.properties.example    шаблон локальной конфигурации
├── db/schema.sql · db/data.sql       инициализация контейнера PostgreSQL
├── templates/                        Thymeleaf-страницы: prizes/, events/, auth/,
│                                     admin.html, users/, error/, fragments/
└── static/                           favicon, css
```

## См. также

- корневой [`README.md`](../README.md) — сценарии, REST API, быстрый старт;
- [`frontend/README.md`](../frontend/README.md) — SPA-клиент.
