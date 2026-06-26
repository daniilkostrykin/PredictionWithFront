<script setup>
/**
 * Стек уведомлений. TransitionGroup анимирует появление и исчезновение.
 */
defineProps({
  toasts: {
    type: Array,
    required: true,
  },
})

const emit = defineEmits(['dismiss'])

const typeIcons = {
  info: 'ℹ️',
  success: '✅',
  warning: '⚠️',
}
</script>

<template>
  <Teleport to="body">
    <TransitionGroup name="toast" tag="ul" class="toast-stack" aria-live="polite">
      <li
        v-for="toast in toasts"
        :key="toast.id"
        :class="['toast', `toast--${toast.type}`]"
        @click="emit('dismiss', toast.id)"
      >
        <span>{{ typeIcons[toast.type] || typeIcons.info }}</span>
        {{ toast.message }}
      </li>
    </TransitionGroup>
  </Teleport>
</template>

<style scoped>
.toast-stack {
  position: fixed;
  top: 1rem;
  right: 1rem;
  z-index: 1100;
  list-style: none;
  display: flex;
  flex-direction: column;
  gap: 0.5rem;
  max-width: 320px;
  padding: 0;
  margin: 0;
}

.toast {
  display: flex;
  align-items: center;
  gap: 0.5rem;
  padding: 0.75rem 1rem;
  background: var(--color-surface);
  border: 1px solid var(--color-border);
  border-radius: 8px;
  box-shadow: var(--shadow);
  cursor: pointer;
  font-size: 0.9rem;
}

.toast--success {
  border-left: 4px solid #22c55e;
}

.toast--warning {
  border-left: 4px solid #f59e0b;
}

.toast--info {
  border-left: 4px solid var(--color-primary);
}

.toast-enter-active,
.toast-leave-active {
  transition: all 0.25s ease;
}

.toast-enter-from,
.toast-leave-to {
  opacity: 0;
  transform: translateX(1rem);
}

.toast-move {
  transition: transform 0.25s ease;
}
</style>
