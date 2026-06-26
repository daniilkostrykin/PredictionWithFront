/**
 * Точка входа в приложение.
 *
 * 1. Подключаем глобальные стили
 * 2. Создаём экземпляр Vue (createApp)
 * 3. Монтируем App.vue в элемент #app из index.html
 */
import './assets/main.css'

import { createApp } from 'vue'
import App from './App.vue'

createApp(App).mount('#app')
