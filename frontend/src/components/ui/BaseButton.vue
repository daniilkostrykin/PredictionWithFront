<script setup>
/**
 * Переиспользуемая кнопка.
 *
 * variant — визуальный стиль (primary для активной вкладки, ghost — обычная).
 * emit('click') — событие клика, которое слушает родитель через @click.
 */
defineProps({
  variant: {
    type: String,
    default: 'ghost',
    validator: (value) => ['primary', 'ghost', 'danger', 'successApp'].includes(value),
  },
  /** Тип HTML-кнопки: button | submit */
  type: {
    type: String,
    default: 'button',
  },
})

defineEmits(['click'])
</script>

<template>
  <button :type="type" :class="['btn', `btn--${variant}`]" @click="$emit('click')">
    <slot />
  </button>
</template>

<style scoped>
.btn {
  padding: 0.5rem 1rem;
  border-radius: 8px;
  border: 1px solid var(--color-border);
  background: var(--color-surface);
  color: var(--color-text);
  font-size: 0.9rem;
  cursor: pointer;
  transition: background 0.15s, border-color 0.15s;
}

.btn:hover {
  border-color: var(--color-primary);
}

.btn--primary {
  background: var(--color-primary);
  border-color: var(--color-primary);
  color: white;
}

.btn--primary:hover {
  background: var(--color-primary-hover);
  border-color: var(--color-primary-hover);
}

.btn--danger {
  background: #fef2f2;
  border-color: #fecaca;
  color: #b91c1c;
}

.btn--danger:hover {
  background: #fee2e2;
  border-color: #f87171;
}
</style>
