<script setup>
import { ref, onMounted, inject } from 'vue'
import { api } from '../../api'
import BaseCard from '../ui/BaseCard.vue'
import BaseButton from '../ui/BaseButton.vue'

const props = defineProps({
  eventId: {
    type: Number,
    required: true
  }
})

const emit = defineEmits(['prediction-success', 'cancel'])

const showToast = inject('showToast')

const eventData = ref(null)
const selectedOption = ref(null)
const isLoading = ref(true)
const isSubmitting = ref(false)

const loadEventDetails = async () => {
  try {
    const response = await api.get(`/events/details/${props.eventId}`)
    eventData.value = response.data.event
  } catch (error) {
    showToast?.('Ошибка загрузки события', 'warning')
  } finally {
    isLoading.value = false
  }
}

const makePrediction = async () => {
  if (!selectedOption.value) {
    showToast?.('Выберите исход', 'warning')
    return
  }

  isSubmitting.value = true
  try {
    await api.post('/predictions/make',
      {
        eventId: props.eventId,
        chosenOptionId: selectedOption.value 
      }
    )
    showToast?.('Прогноз успешно сделан!', 'success')
    emit('prediction-success')
  } catch (error) {
    showToast?.(error.response?.data?.error || 'Ошибка при отправке', 'warning')
  } finally {
    isSubmitting.value = false
  }
}

onMounted(() => {
  loadEventDetails()
})
</script>

<template>
  <div class="prediction-wrapper">
    <BaseCard v-if="isLoading">
      <p style="text-align: center;">Загрузка данных...</p>
    </BaseCard>

    <BaseCard v-else-if="eventData" :title="eventData.title">
      <p class="description">{{ eventData.description }}</p>

      <form class="prediction-form" @submit.prevent="makePrediction">
        <div class="options-list">
          <label 
            v-for="option in eventData.optionsWithStats" 
            :key="option.optionId" 
            class="option-label"
            :class="{ 'selected': selectedOption === option.optionId }"
          >
            <input 
              type="radio" 
              :value="option.optionId" 
              v-model="selectedOption"
              name="prediction_option"
            />
            <span>{{ option.text }}</span>
          </label>
        </div>

        <div class="form-actions">
          <BaseButton type="submit" variant="primary" :disabled="isSubmitting || !selectedOption">
            {{ isSubmitting ? 'Отправка...' : 'Подтвердить выбор' }}
          </BaseButton>
          <BaseButton variant="ghost" type="button" @click="emit('cancel')">
            Отмена
          </BaseButton>
        </div>
      </form>
    </BaseCard>
  </div>
</template>

<style scoped>
.prediction-wrapper {
  max-width: 600px;
  margin: 0 auto;
}

.description {
  margin-bottom: 1.5rem;
  color: var(--color-text-muted);
}

.options-list {
  display: flex;
  flex-direction: column;
  gap: 0.75rem;
  margin-bottom: 1.5rem;
}

.option-label {
  display: flex;
  align-items: center;
  gap: 0.75rem;
  padding: 1rem;
  border: 1px solid var(--color-border);
  border-radius: 8px;
  cursor: pointer;
  transition: all 0.2s ease;
}

.option-label:hover {
  border-color: var(--color-primary);
}

.option-label.selected {
  border-color: var(--color-primary);
  background: rgba(79, 70, 229, 0.05);
}

.form-actions {
  display: flex;
  gap: 1rem;
}
</style>