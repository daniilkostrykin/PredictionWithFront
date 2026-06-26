<script setup>
/**
 * Модальное окно через <Teleport to="body"> — рендер вне #app.
 * v-if на overlay: окно появляется только когда open === true.
 */
defineProps({
  open: { type: Boolean, default: false },
  title: { type: String, default: '' },
})

const emit = defineEmits(['close'])

function onBackdropClick() {
  emit('close')
}
</script>

<template>
  <Teleport to="body">
    <Transition name="modal">
      <div v-if="open" class="modal-backdrop" @click.self="onBackdropClick">
        <div class="modal" role="dialog" aria-modal="true">
          <header class="modal__header">
            <h3 v-if="title">{{ title }}</h3>
            <button type="button" class="modal__close" aria-label="Закрыть" @click="emit('close')">
              ×
            </button>
          </header>
          <div class="modal__body">
            <slot />
          </div>
          <footer v-if="$slots.footer" class="modal__footer">
            <slot name="footer" />
          </footer>
        </div>
      </div>
    </Transition>
  </Teleport>
</template>

<style scoped>
.modal-backdrop {
  position: fixed;
  inset: 0;
  z-index: 1000;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 1rem;
  background: rgba(15, 23, 42, 0.5);
}

.modal {
  width: 100%;
  max-width: 420px;
  background: var(--color-surface);
  border-radius: var(--radius);
  box-shadow: 0 20px 40px rgba(0, 0, 0, 0.15);
  border: 1px solid var(--color-border);
}

.modal__header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 1rem 1.25rem;
  border-bottom: 1px solid var(--color-border);
}

.modal__header h3 {
  font-size: 1.1rem;
}

.modal__close {
  width: 2rem;
  height: 2rem;
  border: none;
  background: var(--color-bg);
  border-radius: 6px;
  font-size: 1.25rem;
  cursor: pointer;
  line-height: 1;
}

.modal__body {
  padding: 1.25rem;
}

.modal__footer {
  display: flex;
  gap: 0.5rem;
  justify-content: flex-end;
  padding: 1rem 1.25rem;
  border-top: 1px solid var(--color-border);
}

.modal-enter-active,
.modal-leave-active {
  transition: opacity 0.2s ease;
}

.modal-enter-active .modal,
.modal-leave-active .modal {
  transition: transform 0.2s ease;
}

.modal-enter-from,
.modal-leave-to {
  opacity: 0;
}

.modal-enter-from .modal,
.modal-leave-to .modal {
  transform: scale(0.95) translateY(8px);
}
</style>
