<script setup>
import { ref, onMounted, inject } from 'vue'
import { api } from '../../api'
import BaseCard from '../ui/BaseCard.vue'

const showToast = inject('showToast')
const profileData = ref(null)
const isLoading = ref(true)

const loadProfile = async () => {
  try {
    const response = await api.get('/users/profile')
    profileData.value = response.data
  } catch (error) {
    showToast?.('Ошибка загрузки профиля', 'warning')
  } finally {
    isLoading.value = false
  }
}

onMounted(() => {
  loadProfile()
})
</script>

<template>
  <div class="profile-wrapper">
    <BaseCard v-if="isLoading">
      <p style="text-align: center;">Загрузка...</p>
    </BaseCard>

    <div v-else-if="profileData">
      <BaseCard :title="'Профиль: ' + profileData.username">
        <div class="stats">
          <div class="stat-item">
            <span class="label">Всего предсказаний:</span>
            <span class="value">{{ profileData.totalPredictions }}</span>
          </div>
          <div class="stat-item">
            <span class="label">Побед:</span>
            <span class="value win-text">{{ profileData.wonPredictions }}</span>
          </div>
        </div>
      </BaseCard>

      <h3 class="predictions-title">Мои прогнозы</h3>
      <div v-if="!profileData.predictions || profileData.predictions.length === 0">
        <p>Вы еще не делали прогнозов.</p>
      </div>
      <div class="predictions-grid" v-else>
        <BaseCard v-for="pred in profileData.predictions" :key="pred.id">
          <p><strong>Событие:</strong> {{ pred.event.title }}</p>
          <p><strong>Ваш выбор:</strong> {{ pred.chosenOption.text }}</p>
          <p><strong>Статус:</strong> <span :class="pred.status.toLowerCase()">{{ pred.status }}</span></p>
        </BaseCard>
      </div>
    </div>
  </div>
</template>

<style scoped>
.profile-wrapper {
  max-width: 800px;
  margin: 0 auto;
}
.stats {
  display: grid;
  grid-template-columns: 1fr 1fr; 
  gap: 1rem; 
  margin-top: 1rem;
}

.stat-item {
  display: flex;
  flex-direction: column;
  background: var(--color-bg);
  padding: 1rem;
  border-radius: 8px;
  
}
.label {
  font-size: 0.85rem;
  color: var(--color-text-muted);
}
.value {
  font-size: 1.5rem;
  font-weight: bold;
  color: var(--color-primary);
}
.predictions-title {
  margin-top: 2rem;
  margin-bottom: 1rem;
}
.predictions-grid {
  display: grid;
  gap: 1rem;
}
.pending {
  color: #f59e0b;
  font-weight: bold;
}
.won {
  color: #10b981;
  font-weight: bold;
}
.lost {
  color: #ef4444;
  font-weight: bold;
}
.win-text {
  color: #10b981;
}
</style>