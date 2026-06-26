<script setup>
import { ref, onMounted, inject } from 'vue'
import { api } from '../../api'
import BaseCard from '../ui/BaseCard.vue'
import BaseButton from '../ui/BaseButton.vue'

const showToast = inject('showToast')
const userStats = ref([])
const searchQuery = ref('')
const currentPage = ref(0)
const totalPages = ref(1)
const isLoading = ref(false)

const loadLeaderboard = async () => {
  isLoading.value = true
  try {
    
    const response = await api.get('/dashboard', {
      params: {
        page: currentPage.value,
        size: 10,
        search: searchQuery.value
      }
    })
    userStats.value = response.data.userStats || []
    currentPage.value = response.data.currentPage || 0
    totalPages.value = response.data.totalPages || 1
  } catch (error) {
    showToast?.('Ошибка при загрузке рейтинга', 'danger')
  } finally {
    isLoading.value = false
  }
}

const handleSearch = () => {
  currentPage.value = 0 
  loadLeaderboard()
}

onMounted(() => {
  loadLeaderboard()
})
</script>

<template>
  <div class="leaderboard-container">
    <hgroup class="page-header">
      <h2>Рейтинг игроков</h2>
      <p class="text-muted">У кого сейчас больше всего побед и самый высокий винрейт?</p>
    </hgroup>

    <div class="search-box">
      <input 
        v-model="searchQuery" 
        type="search" 
        placeholder="Найти игрока..." 
        class="custom-search-input"
        @keyup.enter="handleSearch"
      />
      <BaseButton variant="primary" @click="handleSearch">Искать</BaseButton>
    </div>

    <BaseCard>
      <div v-if="isLoading" class="text-center-padding">Загрузка рейтинга...</div>
      
      <div v-else-if="userStats.length === 0" class="text-center-padding">
        Игроки не найдены
      </div>
      
      <div v-else class="table-responsive">
        <table class="leaderboard-table">
          <thead>
            <tr>
              <th>Имя пользователя</th>
              <th>Баланс</th>
              <th>Всего прогнозов</th>
              <th>Процент побед</th>
            </tr>
          </thead>
          <tbody>
            <tr v-for="user in userStats" :key="user.username">
              <td class="username-td"><strong>{{ user.username }}</strong></td>
              <td class="stat-weight">{{ user.wonPredictions }} 💰</td>
              <td>{{ user.totalPredictions }}</td>
              <td class="win-rate-td">{{ user.winRate ? user.winRate.toFixed(0) : 0 }}%</td>
            </tr>
          </tbody>
        </table>
      </div>

      <div v-if="totalPages > 1" class="pagination-wrapper">
        <BaseButton 
          variant="secondary" 
          :disabled="currentPage === 0" 
          @click="currentPage--; loadLeaderboard()"
        >Назад</BaseButton>
        
        <span class="page-info">Страница {{ currentPage + 1 }} из {{ totalPages }}</span>
        
        <BaseButton 
          variant="secondary" 
          :disabled="currentPage >= totalPages - 1" 
          @click="currentPage++; loadLeaderboard()"
        >Вперед</BaseButton>
      </div>
    </BaseCard>
  </div>
</template>

<style scoped>
.leaderboard-container {
  max-width: 700px;
  margin: 0 auto;
}

.page-header {
  text-align: center;
  margin-bottom: 2rem;
}

.search-box {
  display: flex;
  gap: 0.5rem;
  margin-bottom: 1.5rem;
}

.custom-search-input {
  flex: 1;
  padding: 0.75rem 1rem;
  border: 1px solid var(--color-border);
  border-radius: 8px;
  background: var(--color-surface);
  color: var(--color-text-main);
  font-family: inherit;
}

.custom-search-input:focus {
  outline: none;
  border-color: var(--color-primary);
}

.table-responsive {
  overflow-x: auto;
}

.leaderboard-table {
  width: 100%;
  border-collapse: collapse;
  text-align: center;
}

.leaderboard-table th, .leaderboard-table td {
  padding: 1rem;
  border-bottom: 1px solid var(--color-border);
}

.leaderboard-table th {
  font-weight: 600;
  color: var(--color-text-muted);
  font-size: 0.9rem;
}

.username-td {
  text-align: left;
  padding-left: 1.5rem;
}

.stat-weight {
  font-weight: 700;
  color: #f59e0b;
}

.win-rate-td {
  font-weight: 700;
  color: var(--color-success);
}

.text-center-padding {
  text-align: center;
  padding: 2rem 0;
  color: var(--color-text-muted);
}

.pagination-wrapper {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-top: 1.5rem;
  padding-top: 1rem;
  border-top: 1px dashed var(--color-border);
}

.page-info {
  font-size: 0.9rem;
  color: var(--color-text-muted);
  font-weight: 500;
}
</style>