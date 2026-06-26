# PredictionApp

Веб-платформа прогнозов на исход событий. Администратор публикует событие с вариантами исхода и дедлайном, зарегистрированные пользователи делают по одному прогнозу; после закрытия приёма итоги подводит администратор, а угадавшие зарабатывают «победы» — внутреннюю валюту для покупки билетов в розыгрышах призов. Готовый движок прогнозного конкурса с лидербордом и розыгрышами: REST API на Spring Boot 3.5 (Java 21) + SPA на Vue 3.

![Java 21](https://img.shields.io/badge/Java-21-007396?logo=openjdk&logoColor=white) ![Spring Boot 3.5](https://img.shields.io/badge/Spring%20Boot-3.5-6DB33F?logo=spring&logoColor=white) ![Vue 3](https://img.shields.io/badge/Vue-3-4FC08D?logo=vuedotjs&logoColor=white)

- Приём прогнозов закрывается автоматически по дедлайну (`@Scheduled`, каждую минуту)
- Подведение итогов события администратором с автозачислением «побед»
- Розыгрыши призов: билет за N «побед», победитель — случайный; авто- и ручной розыгрыш
- Лидерборд, личный кабинет с историей прогнозов, админ-панель со статистикой
- Сессии в Redis, кэш списков событий с TTL 10 минут

## Quick Look

Данные движутся так:

```text
┌──────────────────────┐  JSON + cookie SESSION   ┌───────────────────────────┐
│      Vue 3 SPA       │ ◀──────────────────────▶ │   Spring Boot API :8080   │
│    localhost:3000    │  (Axios, withCredentials)│   /api/**  +  /prizes     │
└──────────────────────┘                          └─────────────┬─────────────┘
                                                                │
                             ┌──────────────────────────────────┼──────────────────────────┐
                             ▼                                  ▼                          ▼
                  ┌─────────────────────┐          ┌─────────────────────┐   ┌────────────────────────┐
                  │  PostgreSQL :5433   │          │     Redis :6379     │   │ @Scheduled — раз/мин   │
                  │ события, прогнозы,  │          │ сессии + кэш списков│   │ закрытие приёма        │
                  │ призы, билеты       │          │   TTL 10 минут      │   │ прогнозов, розыгрыши   │
                  └─────────────────────┘          └─────────────────────┘   └────────────────────────┘
```

Полный цикл события через REST API (живой сценарий, все учётные данные — тестовые):

```bash
API=http://localhost:8080/api

# 1. Вход администратора — получаем cookie-сессию
curl -c admin.jar -d "username=admin&password=12345" $API/auth/login
# {"message":"Успешный вход"}

# 2. Создание события: ≥ 2 вариантов исхода, дедлайн в будущем (формат yyyy-MM-dd'T'HH:mm)
curl -b admin.jar -H "Content-Type: application/json" -d '{
  "title": "Матч Спартак — Динамо",
  "description": "Исход центрального матча тура",
  "options": ["Победа Спартака", "Ничья", "Победа Динамо"],
  "closesAt": "2026-12-01T19:00"
}' $API/events/create
# {"message":"Событие успешно создано"}

# 3. Пользователь входит и смотрит детали — id вариантов в optionsWithStats
curl -c user.jar -d "username=user1&password=12345" $API/auth/login
curl -b user.jar $API/events/details/1
# {"event":{"id":1,"title":"Матч Спартак — Динамо","status":"ACTIVE",
#  "closesAt":"2026-12-01T16:00:00Z",
#  "optionsWithStats":[{"optionId":1,"text":"Победа Спартака","percentage":0},
#                      {"optionId":2,"text":"Ничья","percentage":0},
#                      {"optionId":3,"text":"Победа Динамо","percentage":0}]},
#  "hasVoted":false}

# 4. Прогноз на «Ничью»
curl -b user.jar -H "Content-Type: application/json" \
     -d '{"eventId":1,"chosenOptionId":2}' $API/predictions/make
# {"message":"Предсказание успешно сделано!"}

# 5. Итоги подводит админ — победила опция 2
curl -b admin.jar -X POST "$API/events/1/resolve?winningOptionId=2"
# {"message":"Победитель выбран, итоги подведены"}

# 6. Лидерборд: user1 угадал → +1 «победа» (+1 к successfulPredictions и balance)
curl -b user.jar "$API/dashboard"
# {"userStats":[{"username":"user1","successfulPredictions":1,"totalPredictionsCount":1,
#  "successRate":100.0,"balance":1}],"currentPage":0,"totalPages":1}
```

## Модель «побед как валюты»

Успешный прогноз засчитывается дважды: `successfulPredictions +1` — трофейный счёт для лидерборда, и `balance +1` — расходуемая валюта. Билет в розыгрыш стоит N «побед» и списывает `balance`; победитель выбирается случайно среди владельцев билетов, при их отсутствии розыгрыш закрывается без победителя.

Техническая основа: аутентификация сессионная — [Spring Session](https://docs.spring.io/spring-session/reference/) хранит сессии в Redis, клиент работает с HttpOnly-cookie `SESSION`; пароли хешируются [BCrypt](https://ru.wikipedia.org/wiki/Bcrypt); списки и детали событий кэшируются в Redis через [Spring Cache](https://docs.spring.io/spring-framework/reference/integration/cache/annotations.html) с TTL 10 минут и инвалидацией при мутациях.

## REST API

| Метод | Путь | Доступ | Тело / query | Успешный ответ |
|---|---|---|---|---|
| `POST` | `/api/auth/register` | публичный | `{"username","email","password","confirmPassword"}` | `{"message":"Регистрация успешна"}` |
| `POST` | `/api/auth/login` | публичный | form-urlencoded: `username`, `password` | `{"message":"Успешный вход"}` + cookie `SESSION` |
| `POST` | `/api/auth/logout` | авторизован | — | `{"message":"Вышли из системы"}` |
| `GET` | `/api/auth/me` | любой¹ | — | `{"username"}` или `401` |
| `GET` | `/api/events/all` | публичный | `page=0&size=10&search=` | `{"events":[],"currentPage","totalPages"}` |
| `GET` | `/api/events/details/{id}` | авторизован | — | `{"event":{…},"hasVoted"}` |
| `POST` | `/api/events/create` | `ADMIN` | событие² | `{"message":"Событие успешно создано"}` |
| `POST` | `/api/events/{id}/resolve` | `ADMIN` | `?winningOptionId=` | `{"message":"Победитель выбран, итоги подведены"}` |
| `POST` | `/api/predictions/make` | авторизован | `{"eventId","chosenOptionId"}` | `{"message":"Предсказание успешно сделано!"}` |
| `GET` | `/api/dashboard` | авторизован | `page&size&search` | `{"userStats":[…],"currentPage","totalPages"}` |
| `GET` | `/api/users/profile` | авторизован | — | `{"username","totalPredictions","wonPredictions","predictions":[…]}` |
| `GET` | `/api/admin/dashboard` | `ADMIN` | — | `{"users":[…],"pendingEvents":[…],"dashboardStats":{…}}` |

¹ Путь публичный, но без валидной сессии вернёт `401`.
² `title` 5–100 символов, `description` ≤ 500, `options` — не менее 2 непустых строк, `closesAt` (`yyyy-MM-dd'T'HH:mm`) строго в будущем.

Структуры ответов: `userStats[]` = `{username, successfulPredictions, totalPredictionsCount, successRate, balance}`; `predictions[]` = `{id, status, event:{title}, chosenOption:{text}}`; `dashboardStats` = `{totalUsers, activeEvents, predictionsMadeToday}`.

### Крайние случаи

- Один прогноз от пользователя на событие; повторный — ошибка `400`.
- Предсказание принимается только пока событие `ACTIVE` и дедлайн не наступил.
- Ошибки валидации и домена возвращаются как `400 {"error":"…"}`; запрос без сессии — `401`.
- Кэш Redis живёт 10 минут; сбрасывается при создании/удалении события, подведении итогов и ежеминутной проверке дедлайнов.
- Розыгрыши призов — не JSON API, а серверные [Thymeleaf](https://www.thymeleaf.org/)-страницы: `GET /prizes` (HTML), `POST /prizes/buy/{id}` — билет, `POST /prizes/add` (`ADMIN`, форма) — создание розыгрыша, `POST /prizes/draw/{id}` (`ADMIN`) — ручной розыгрыш.
- `spring.jpa.hibernate.ddl-auto=create`: при каждом старте бэкенда схема БД пересоздаётся и накопленные данные теряются. Для продакшена переключите на `validate`/`update` в `backend/src/main/resources/application.properties`.
- CSRF отключён (REST + SPA); CORS разрешён только для `http://localhost:3000` и `http://127.0.0.1:3000` с `allowCredentials=true`.

## Установка и запуск

Понадобятся: **JDK 21**, **Docker**, **Node.js** `^20.19.0 || >=22.12.0`.

```bash
# 1. Инфраструктура: PostgreSQL 18 (:5433) + Redis (:6379)
cd backend && docker compose up -d

# 2. Бэкенд (:8080) — первый запуск: создайте локальный конфиг из шаблона
cd backend
cp src/main/resources/application.properties.example src/main/resources/application.properties
./mvnw spring-boot:run              # Linux/macOS
# mvnw.cmd spring-boot:run          # Windows

# 3. Фронтенд (:3000)
cd frontend
npm install
npm run dev
```

Приложение: <http://localhost:3000> · REST API: <http://localhost:8080/api>

Тестовые учётные данные (создаются при первом старте `DataInitializer`):

| Логин | Пароль | Роль |
|---|---|---|
| `admin` | `12345` | `ADMIN` |
| `user1` | `12345` | `USER` |

### Тесты

```bash
cd backend && ./mvnw test
```

JUnit 5 + Mockito на in-memory H2; Redis и PostgreSQL для тестов не нужны (`spring.cache.type=none`). Покрыты админ-сценарии, жизненный цикл розыгрышей и граничные случаи, конкурентные предсказания, пользовательские сценарии.

Подробнее: [`backend/README.md`](backend/README.md) — слои, доменная модель, безопасность, конфигурация · [`frontend/README.md`](frontend/README.md) — SPA-клиент.