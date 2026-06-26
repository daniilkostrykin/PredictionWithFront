import { ref } from 'vue'

let toastId = 0

/**
 * Composable для всплывающих уведомлений (тостов).
 * showToast() можно вызывать из любого компонента через inject.
 */
export function useToast() {
  const toasts = ref([])

  function showToast(message, type = 'info') {
    const id = ++toastId
    toasts.value.push({ id, message, type })
    setTimeout(() => removeToast(id), 3000)
  }

  function removeToast(id) {
    toasts.value = toasts.value.filter((t) => t.id !== id)
  }

  return { toasts, showToast, removeToast }
}
