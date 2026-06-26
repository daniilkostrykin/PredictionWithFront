# PredictionApp — платформа прогнозов и призов

Курсовой проект: веб-сервис для прогнозов на события, ведения таблицы лидеров и розыгрыша призов.

**Монорепозиторий:** REST API на Spring Boot (Java 21) + SPA на Vue 3.

## Стек

| Часть | Технологии |
|---|---|
| Бэкенд | Java 21, Spring Boot 3.5 (Web, Security, Data JPA, Validation, Cache), Spring Session Data Redis, Lombok, ModelMapper, Maven Wrapper |
| БД / кэш | PostgreSQL 18, Redis (сессии + кэш, TTL 10 минут) |
| Фронтенд | Vue 3, Vite, Axios, ESLint + Prettier |
| Тесты | JUnit 5, Mockito, H2 in-memory (тесты работают без Docker) |

## Структура

```
backend/    Spring Boot: REST API `/api/**`, безопасность, JPA, сервисы, планировщики
frontend/   Vue 3 SPA: события, прогнозы, профиль, лидерборд, админ-панель
```

## Быстрый старт

Понадобятся: **JDK 21**, **Docker**, **Node.js 20.19+**.

### 1. Инфраструктура (PostgreSQL + Redis)

```bash
cd backend
docker compose up -d
```

PostgreSQL поднимется на порту **5433** (БД `prediction_db`, схема `prediction_schema`, данные инициализируются из `db/schema.sql` и `db/data.sql`), Redis — на порту **6379**.

### 2. Бэкенд — порт 8080

```bash
cd backend
# первый запуск: создайте локальный конфиг из шаблона
cp src/main/resources/application.properties.example src/main/resources/application.properties

./mvnw spring-boot:run          # Linux/macOS
# или на Windows: mvnw.cmd spring-boot:run
```

### 3. Фронтенд — порт 3000

```bash
cd frontend
npm install
npm run dev
```

Приложение: **http://localhost:3000** · REST API: **http://localhost:8080/api**

## Тестовые пользователи

Создаются автоматически при первом старте (`DataInitializer`):

| Логин | Пароль | Роль |
|---|---|---|
| `admin` | `12345` | ADMIN |
| `user1` | `12345` | USER |

## Как связаны фронтенд и бэкенд

- Axios-клиент настроен на `baseURL: http://localhost:8080/api` и `withCredentials: true`;
- аутентификация **сессионная**: Spring Session хранит сессии в Redis, браузер получает куку `SESSION` (SameSite=Lax, HttpOnly);
- CORS разрешён для `http://localhost:3000` и `http://127.0.0.1:3000` (`allowCredentials=true`), CSRF отключён;
- неавторизованные запросы к API получают `401`; админ-эндпоинты защищены `@PreAuthorize("hasRole('ADMIN')")`;
- списки событий кэшируются в Redis; `@Scheduled`-задачи автоматически закрывают приём ставок и проводят розыгрыши.

## Основные эндпоинты

| Метод и путь | Доступ | Назначение |
|---|---|---|
| `POST /api/auth/register` | публичный | регистрация |
| `POST /api/auth/login` | публичный | вход (обрабатывает Spring Security, JSON-ответ) |
| `POST /api/auth/logout` | авторизован | выход |
| `GET /api/auth/me` | любой¹ | текущий пользователь (`401` без сессии) |
| `GET /api/events/all` | публичный | список событий (пагинация, поиск) |
| `GET /api/events/details/{id}` | авторизован | детали события и варианты прогноза |
| `POST /api/events/create` | ADMIN | создать событие |
| `POST /api/events/{id}/resolve` | ADMIN | подвести итоги события |
| `POST /api/predictions/make` | авторизован | сделать прогноз |
| `GET /api/dashboard` | авторизован | личная статистика, билеты, призы |
| `GET /api/users/profile` | авторизован | профиль пользователя |
| `GET /api/admin/dashboard` | ADMIN | админ-панель: пользователи и статистика |

¹ путь публичный, но без сессии вернёт `401`.

Функционал розыгрышей призов (покупка билетов, `/prizes/**`) также доступен через серверные Thymeleaf-страницы.

Подробное описание доменной модели и слоёв приложения — в [`backend/README.md`](backend/README.md).