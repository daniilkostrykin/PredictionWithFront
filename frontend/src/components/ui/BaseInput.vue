<script setup>
/**
 * Поле ввода с поддержкой v-model через defineModel (Vue 3.4+).
 * Родитель пишет: <BaseInput v-model="searchQuery" label="Поиск" />
 */
defineProps({
  label: { type: String, default: '' },
  type: { type: String, default: 'text' },
  placeholder: { type: String, default: '' },
  hint: { type: String, default: '' },
})

const model = defineModel({ type: [String, Number], default: '' })
</script>

<template>
  <label class="input-wrap">
    <span v-if="label" class="input-wrap__label">{{ label }}</span>
    <input
      v-model="model"
      class="input-wrap__field"
      :type="type"
      :placeholder="placeholder"
    />
    <span v-if="hint" class="input-wrap__hint">{{ hint }}</span>
  </label>
</template>

<style scoped>
.input-wrap {
  display: flex;
  flex-direction: column;
  gap: 0.35rem;
}

.input-wrap__label {
  font-size: 0.85rem;
  font-weight: 500;
  color: var(--color-text-muted);
}

.input-wrap__field {
  padding: 0.55rem 0.75rem;
  border: 1px solid var(--color-border);
  border-radius: 8px;
  font-size: 0.95rem;
  background: var(--color-surface);
  color: var(--color-text);
}

.input-wrap__field:focus {
  outline: 2px solid var(--color-primary);
  outline-offset: 1px;
}

.input-wrap__hint {
  font-size: 0.75rem;
  color: var(--color-text-muted);
}
</style>
