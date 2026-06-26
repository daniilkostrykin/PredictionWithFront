<script setup>
import { ref, onMounted, inject } from 'vue'
import { api } from '../../api'
import BaseCard from '../ui/BaseCard.vue'
import BaseButton from '../ui/BaseButton.vue'

const showToast = inject('showToast')
const events = ref([])
const isLoading = ref(true)

const loadEvents = async () => {
  isLoading.value = true
  try {
    const response = await api.get('/events/all')
    events.value = response.data.events || []
  } catch (error) {
    showToast?.('Ошибка при загрузке событий', 'warning')
    events.value = []
  } finally {
    isLoading.value = false
  }
}

onMounted(() => {
  loadEvents()
})
</script>

<template>
  <div class="events-container">
    <h2 class="page-title">Активные события</h2>
    
    <div v-if="isLoading" class="loading">Загрузка событий...</div>
    
    <div v-else-if="events.length === 0" class="empty-list">
      Нет доступных событий
    </div>
    
    <div v-else class="events-grid">
      <BaseCard v-for="event in events" :key="event.id">
        <div class="card-header">
          <h3 class="event-title">{{ event.title }}</h3>
          <span class="status" :class="event.status?.toLowerCase()">
            {{ event.status }}
          </span>
        </div>
        
        <div class="card-footer">
          <span class="date-text">
            До: {{ event.closesAt ? new Date(event.closesAt).toLocaleString() : 'не указано' }}
          </span>
          <BaseButton variant="primary" @click="$emit('select-event', event.id)">
            Сделать прогноз
          </BaseButton>
        </div>
      </BaseCard>
    </div>
  </div>
</template>

<style scoped>
.page-title {
  margin-bottom: 1.5rem;
  font-size: 1.5rem;
  font-weight: 700;
}


.events-grid {
  display: flex;
  flex-direction: column;
  gap: 1.5rem;
  max-width: 650px; 
  margin: 0 auto;
}


.card-header {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  gap: 1rem;
}

.event-title {
  font-size: 1.25rem;
  font-weight: 600;
  margin: 0;
  line-height: 1.4;
}

.card-footer {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-top: 1.2rem;
  padding-top: 1rem;
  border-top: 1px solid var(--color-border); 
}

.date-text {
  font-size: 0.875rem;
  color: var(--color-text-muted);
}


.status {
  padding: 4px 8px;
  border-radius: 6px;
  font-size: 0.75rem;
  font-weight: 700;
  text-transform: uppercase;
  letter-spacing: 0.05em;
}

.active {
  background-color: #e6f4ea;
  color: #137333;
}

.finished {
  background-color: #fce8e6;
  color: #c5221f;
}

.loading, .empty-list {
  text-align: center;
  color: var(--color-text-muted);
  padding: 2rem 0;
}
</style>