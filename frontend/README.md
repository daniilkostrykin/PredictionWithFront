# PredictionApp · frontend

SPA-клиент платформы прогнозов PredictionApp: Vue 3 + Vite. Требует запущенный бэкенд PredictionApp на `http://localhost:8080` — общий обзор и быстрый старт в корневом [`README.md`](../README.md).

![Vue 3](https://img.shields.io/badge/Vue-3-4FC08D?logo=vuedotjs&logoColor=white) ![Vite](https://img.shields.io/badge/Vite-8-646CFF?logo=vite&logoColor=white) ![Axios](https://img.shields.io/badge/Axios-1.18-5A29E4?logo=axios&logoColor=white)

## Требования

- Node.js `^20.19.0 || >=22.12.0` (поле `engines` в `package.json`)
- Работающий бэкенд PredictionApp на порту `8080`

## Запуск

```bash
npm install
npm run dev        # http://localhost:3000
```

`strictPort: false`: если порт 3000 занят, Vite возьмёт 3001 и далее. CORS на бэкенде разрешён только для `http://localhost:3000` и `http://127.0.0.1:3000` — при смене порта добавьте новый origin в `SecurityConfig` бэкенда.

## Скрипты

| Команда | Действие |
|---|---|
| `npm run dev` | dev-сервер с HMR и [Vue DevTools](https://devtools.vuejs.org/) |
| `npm run build` | продакшен-сборка в `dist/` |
| `npm run preview` | локальный просмотр сборки |
| `npm run lint` | `oxlint` + `eslint` (с автоисправлениями) |
| `npm run format` | Prettier по `src/` |

## Слой API

`src/api/index.js` — единственная точка доступа к бэкенду:

- Axios-инстанс: `baseURL: http://localhost:8080/api`, `withCredentials: true` (cookie-сессия `SESSION`), `Content-Type: application/json`;
- интерцептор ответа `cleanJavaTypes` вычищает артефакты Java-сериализации (элементы вида `["java.util.ArrayList", …]`) из JSON-ответов;
- логин отправляется как `application/x-www-form-urlencoded` (`username`, `password`) — контракт Spring Security formLogin; все остальные запросы — JSON;
- сессия проверяется при загрузке приложения запросом `GET /api/auth/me`.

## Структура

```text
src/
├── main.js             точка входа приложения
├── api/index.js        HTTP-клиент (Axios + интерцепторы)
├── App.vue             корневой компонент: вкладки (события, лидерборд, админ),
│                       состояние сессии, тосты
├── components/
│   ├── auth/           LoginForm, RegisterForm, UserProfile
│   ├── events/         EventList, PredictionForm
│   ├── admin/          AdminPanel
│   ├── users/          Leaderboard
│   └── ui/             BaseButton, BaseCard, BaseInput, BaseModal,
│                       ProgressBar, AppToast
├── composables/        useToast, useTheme
└── assets/             base.css, main.css, logo.svg
```

## Конфигурация

`vite.config.js`: плагины `@vitejs/plugin-vue` и `vite-plugin-vue-devtools`; алиас `@` → `src/`; dev-сервер — `127.0.0.1:3000`, `strictPort: false`.
