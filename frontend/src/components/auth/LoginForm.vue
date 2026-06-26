<script setup>
import { ref, inject } from 'vue'
import { api } from '../../api' 
import BaseCard from '../ui/BaseCard.vue'
import BaseInput from '../ui/BaseInput.vue'
import BaseButton from '../ui/BaseButton.vue'

const emit = defineEmits(['login-success', 'switch-to-register'])


const showToast = inject('showToast')

const username = ref('')
const password = ref('')
const isLoading = ref(false)

const handleLogin = async () => {
  if (!username.value || !password.value) {
    showToast?.('Введите логин и пароль', 'warning')
    return
  }

  isLoading.value = true

  try {
    const response = await api.post('/auth/login', {
      username: username.value,
      password: password.value
    }, {
      headers: {
        'Content-Type': 'application/x-www-form-urlencoded'
      }
    });

    if (response.status === 200) {
      showToast?.('Успешный вход!', 'success')
      emit('login-success', username.value)
    }
  } catch (error) {
    const errorMsg = error.response?.data?.error || 'Ошибка авторизации'
    
    showToast?.(errorMsg, 'warning') 
  } finally {
    isLoading.value = false
  }
};
</script>

<template>
  <div class="login-wrapper">
    <BaseCard title="Вход в PredictionApp">
      <form class="login-form" @submit.prevent="handleLogin">
        
        <BaseInput 
          v-model="username" 
          label="Логин" 
          placeholder="Введите имя пользователя" 
        />
        
        <BaseInput 
          v-model="password" 
          type="password" 
          label="Пароль" 
          placeholder="Введите пароль" 
        />
        
        <BaseButton 
          type="submit" 
          variant="primary" 
          :disabled="isLoading"
        >
          {{ isLoading ? 'Загрузка...' : 'Войти' }}
        </BaseButton>

      <p class="switch-text">
          Нет аккаунта? 
          <a href="#" @click.prevent="$emit('switch-to-register')">Зарегистрироваться</a>
        </p>
      </form>
    </BaseCard>
  </div>
</template>

<style scoped>
.login-wrapper {
  max-width: 400px;
  margin: 4rem auto;
}

.login-form {
  display: flex;
  flex-direction: column;
  gap: 1.25rem;
  margin-top: 1rem;
}

.switch-text {
  text-align: center;
  margin-top: 1rem;
  font-size: 0.9rem;
  color: var(--color-text-muted);
}
</style>