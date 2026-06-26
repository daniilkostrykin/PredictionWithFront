<script setup>
import { ref, onMounted, inject } from 'vue'
import { api } from '../../api'
import BaseCard from '../ui/BaseCard.vue'
import BaseInput from '../ui/BaseInput.vue'
import BaseButton from '../ui/BaseButton.vue'

const showToast = inject('showToast')


const activeTab = ref('create')


const newEvent = ref({
  title: '',
  description: '',
  options: ['', ''], 
  closesAt: ''
})
const isSubmitting = ref(false)

const addOption = () => {
  newEvent.value.options.push('')
}

const removeOption = (index) => {
  if (newEvent.value.options.length > 2) {
    newEvent.value.options.splice(index, 1)
  } else {
    showToast?.('Событие должно иметь минимум 2 исхода', 'warning')
  }
}

const handleCreateEvent = async () => {
  if (!newEvent.value.title || !newEvent.value.closesAt || newEvent.value.options.some(opt => !opt)) {
    showToast?.('Заполните все поля и варианты исходов', 'warning')
    return
  }

  isSubmitting.value = true
  try {
    
    await api.post('/events/create', newEvent.value)
    showToast?.('Событие успешно создано!', 'success')
    
    newEvent.value = { title: '', description: '', options: ['', ''], closesAt: '' }
  } catch (error) {
    showToast?.('Ошибка при создании события', 'danger')
  } finally {
    isSubmitting.value = false
  }
}


const pendingEvents = ref([])
const selectedWinners = ref({}) 
const isLoadingStats = ref(false)

const loadAdminData = async () => {
  isLoadingStats.value = true
  try {
    const response = await api.get('/admin/dashboard')
    pendingEvents.value = response.data.pendingEvents || []
  } catch (error) {
    showToast?.('Не удалось загрузить данные админ-панели', 'danger')
  } finally {
    isLoadingStats.value = false
  }
}

const handleResolveEvent = async (eventId) => {
  const winningOptionId = selectedWinners.value[eventId]
  if (!winningOptionId) {
    showToast?.('Выберите победивший исход', 'warning')
    return
  }

  try {
    await api.post(`/events/${eventId}/resolve?winningOptionId=${winningOptionId}`)
    showToast?.('Итоги успешно подведены!', 'success')
    loadAdminData() 
  } catch (error) {
    showToast?.('Ошибка при завершении события', 'danger')
  }
}

onMounted(() => {
  loadAdminData()
})
</script>

<template>
  <div class="admin-panel">
    <div class="admin-tabs">
      <button 
        :class="['tab-btn', { active: activeTab === 'create' }]" 
        @click="activeTab = 'create'"
      >Создать событие</button>
      <button 
        :class="['tab-btn', { active: activeTab === 'resolve' }]" 
        @click="activeTab = 'resolve'"
      >Ожидают завершения 
        <span class="badge" v-if="pendingEvents.length">{{ pendingEvents.length }}</span>
      </button>
    </div>

    <BaseCard v-if="activeTab === 'create'" title="Новое событие">
      <form @submit.prevent="handleCreateEvent" class="create-form">
        <BaseInput v-model="newEvent.title" label="Название" placeholder="Например: Кто выиграет Оскар 2027?" required />
        
        <div class="input-group">
          <label class="input-label">Описание</label>
          <textarea v-model="newEvent.description" class="custom-textarea" rows="3" placeholder="Дополнительная информация..."></textarea>
        </div>

        <div class="options-section">
          <label class="input-label">Варианты исходов</label>
          <div v-for="(opt, index) in newEvent.options" :key="index" class="option-row">
            <input v-model="newEvent.options[index]" class="custom-input" placeholder="Введите вариант..." required />
            <button type="button" class="btn-remove" @click="removeOption(index)" title="Удалить">✕</button>
          </div>
          <button type="button" class="btn-add" @click="addOption">+ Добавить вариант</button>
        </div>

        <BaseInput v-model="newEvent.closesAt" type="datetime-local" label="Дата окончания приёма ставок" required />

        <BaseButton type="submit" variant="primary" :disabled="isSubmitting" style="margin-top: 1rem;">
          {{ isSubmitting ? 'Создаем...' : 'Запустить событие' }}
        </BaseButton>
      </form>
    </BaseCard>

    <div v-if="activeTab === 'resolve'" class="resolve-section">
      <div v-if="pendingEvents.length === 0" class="empty-state">
        Нет событий, ожидающих ручного завершения.
      </div>

      <BaseCard v-for="event in pendingEvents" :key="event.id" class="pending-card">
        <h3>{{ event.title }}</h3>
        <p class="text-muted">Время вышло: {{ new Date(event.closesAt).toLocaleString() }}</p>
        
        <div class="resolve-actions">
          <select v-model="selectedWinners[event.id]" class="custom-select">
            <option disabled value="undefined">-- Выберите победителя --</option>
            <option v-for="opt in event.options" :key="opt.id" :value="opt.id">
              {{ opt.text }}
            </option>
          </select>
          
          <BaseButton variant="success" @click="handleResolveEvent(event.id)">
            Подтвердить результат
          </BaseButton>
        </div>
      </BaseCard>
    </div>
  </div>
</template>

<style scoped>
.admin-panel {
  max-width: 700px;
  margin: 0 auto;
}

.admin-tabs {
  display: flex;
  gap: 1rem;
  margin-bottom: 2rem;
  border-bottom: 1px solid var(--color-border);
  padding-bottom: 1rem;
}

.tab-btn {
  background: none;
  border: none;
  font-size: 1rem;
  font-weight: 600;
  color: var(--color-text-muted);
  cursor: pointer;
  padding: 0.5rem 1rem;
  border-radius: 6px;
  transition: all 0.2s;
  display: flex;
  align-items: center;
  gap: 0.5rem;
}

.tab-btn:hover {
  background: var(--color-border);
}

.tab-btn.active {
  color: var(--color-primary);
  background: rgba(79, 70, 229, 0.1);
}

.badge {
  background: var(--color-danger);
  color: white;
  font-size: 0.75rem;
  padding: 2px 6px;
  border-radius: 10px;
}

.create-form {
  display: flex;
  flex-direction: column;
  gap: 1.25rem;
}

.input-label {
  display: block;
  font-size: 0.875rem;
  font-weight: 500;
  margin-bottom: 0.5rem;
  color: var(--color-text-main);
}

.custom-input, .custom-textarea, .custom-select {
  width: 100%;
  padding: 0.75rem 1rem;
  border: 1px solid var(--color-border);
  border-radius: 8px;
  background: var(--color-surface);
  color: var(--color-text-main);
  font-family: inherit;
}

.options-section {
  background: var(--color-bg);
  padding: 1rem;
  border-radius: 8px;
}

.option-row {
  display: flex;
  gap: 0.5rem;
  margin-bottom: 0.75rem;
}

.btn-remove {
  background: var(--color-danger);
  color: white;
  border: none;
  border-radius: 8px;
  padding: 0 1rem;
  cursor: pointer;
}

.btn-add {
  background: none;
  border: 1px dashed var(--color-primary);
  color: var(--color-primary);
  width: 100%;
  padding: 0.75rem;
  border-radius: 8px;
  cursor: pointer;
  font-weight: 600;
}

.pending-card {
  margin-bottom: 1.5rem;
  border-left: 4px solid var(--color-danger);
}

.resolve-actions {
  display: flex;
  gap: 1rem;
  margin-top: 1rem;
}

.empty-state {
  text-align: center;
  padding: 3rem;
  color: var(--color-text-muted);
  background: var(--color-surface);
  border-radius: 8px;
  border: 1px dashed var(--color-border);
}

@media (max-width: 600px) {
  .resolve-actions {
    flex-direction: column;
  }
}
</style>