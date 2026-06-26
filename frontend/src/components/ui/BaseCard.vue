<script setup>
/**
 * Базовая карточка — обёртка с рамкой и тенью.
 * Используется во многих разделах, чтобы не дублировать стили.
 *
 * Слот <slot /> — место, куда родитель вставляет содержимое.
 */
defineProps({
  /** Заголовок карточки (необязательный) */
  title: {
    type: String,
    default: '',
  },
})
</script>

<template>
  <article class="card">
    <h3 v-if="title" class="card__title">{{ title }}</h3>
    <div class="card__body">
      <slot />
    </div>
  </article>
</template>

<style scoped>
.card {
  background: var(--color-surface);
  border: 1px solid var(--color-border);
  border-radius: var(--radius);
  box-shadow: var(--shadow);
  padding: 1.25rem;
}

.card__title {
  font-size: 1rem;
  font-weight: 600;
  margin-bottom: 0.75rem;
  color: var(--color-text);
}

.card__body {
  color: var(--color-text);
}
</style>
