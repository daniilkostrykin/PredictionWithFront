<script setup>
import { ref, inject } from 'vue'
import { api } from '../../api'
import BaseCard from '../ui/BaseCard.vue'
import BaseInput from '../ui/BaseInput.vue'
import BaseButton from '../ui/BaseButton.vue'

const emit = defineEmits(['switch-to-login', 'register-success'])
const showToast = inject('showToast')

const username = ref('')
const password = ref('')
const isLoading = ref(false)

const handleRegister = async () => {
  if (!username.value || !password.value) {
    showToast?.('Введите имя пользователя и пароль', 'warning')
    return
  }

  isLoading.value = true
  try {
    
    await api.post('/auth/register', {
      username: username.value,
      password: password.value
    })
    
    showToast?.('Регистрация успешна! Теперь войдите.', 'success')
    emit('register-success') 
  } catch (error) {
    showToast?.(error.response?.data?.error || 'Ошибка при регистрации', 'danger')
  } finally {
    isLoading.value = false
  }
}
</script>

<template>
  <div class="auth-container">
    <BaseCard title="Регистрация">
      <form @submit.prevent="handleRegister" class="auth-form">
        <BaseInput 
          v-model="username" 
          label="Имя пользователя" 
          placeholder="Придумайте логин"
          required 
        />
        <BaseInput 
          v-model="password" 
          label="Пароль" 
          type="password" 
          placeholder="Придумайте пароль"
          required 
        />
        
        <BaseButton type="submit" variant="primary" :disabled="isLoading">
          {{ isLoading ? 'Регистрируем...' : 'Зарегистрироваться' }}
        </BaseButton>

        <p class="switch-text">
          Уже есть аккаунт? 
          <a href="#" @click.prevent="$emit('switch-to-login')">Войти</a>
        </p>
      </form>
    </BaseCard>
  </div>
</template>

<style scoped>
.auth-container {
  display: flex;
  justify-content: center;
  align-items: center;
  min-height: 60vh;
}
.auth-form {
  display: flex;
  flex-direction: column;
  gap: 1.5rem;
  width: 100%;
  min-width: 320px;
}
.switch-text {
  text-align: center;
  margin-top: 1rem;
  font-size: 0.9rem;
  color: var(--color-text-muted);
}
</style>