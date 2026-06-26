<script setup>
/**
 * Прогресс-бар: :style привязывает ширину к проценту (реактивный inline-style).
 */
defineProps({
  percent: {
    type: Number,
    default: 0,
    validator: (v) => v >= 0 && v <= 100,
  },
  label: { type: String, default: '' },
})
</script>

<template>
  <div class="progress">
    <div v-if="label" class="progress__label">
      <span>{{ label }}</span>
      <span>{{ percent }}%</span>
    </div>
    <div class="progress__track" role="progressbar" :aria-valuenow="percent" aria-valuemin="0" aria-valuemax="100">
      <div class="progress__fill" :style="{ width: `${percent}%` }" />
    </div>
  </div>
</template>

<style scoped>
.progress__label {
  display: flex;
  justify-content: space-between;
  font-size: 0.85rem;
  margin-bottom: 0.35rem;
  color: var(--color-text-muted);
}

.progress__track {
  height: 8px;
  background: var(--color-border);
  border-radius: 999px;
  overflow: hidden;
}

.progress__fill {
  height: 100%;
  background: linear-gradient(90deg, var(--color-primary), #818cf8);
  border-radius: 999px;
  transition: width 0.35s ease;
}
</style>
