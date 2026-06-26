<script setup>
import { ref, provide, onMounted } from 'vue'
import LoginForm from './components/auth/LoginForm.vue'
import EventList from './components/events/EventList.vue'
import PredictionForm from './components/events/PredictionForm.vue'
import UserProfile from './components/auth/UserProfile.vue'
import AppToast from './components/ui/AppToast.vue'
import BaseButton from './components/ui/BaseButton.vue'
import { useToast } from './composables/useToast.js'

import RegisterForm from './components/auth/RegisterForm.vue'
import AdminPanel from './components/admin/AdminPanel.vue'
import Leaderboard from './components/users/Leaderboard.vue'
import { api } from './api' 

const authMode = ref('login') 
const isAuthenticated = ref(false)
const currentUser = ref('')
const selectedEventId = ref(null)
const currentTab = ref('events')

const { toasts, showToast, removeToast } = useToast()

provide('showToast', showToast)

onMounted(async () => {
  try {
    
    
    const response = await api.get('/auth/me') 
    if (response.data) {
      isAuthenticated.value = true
      currentUser.value = response.data.username 
    }
  } catch (e) {
    
    isAuthenticated.value = false
  }
})
function onLoginSuccess(username) {
  isAuthenticated.value = true
  currentUser.value = username
}

function onLogout() {
  isAuthenticated.value = false
  currentUser.value = ''
  selectedEventId.value = null
  currentTab.value = 'events'
  showToast('Вы вышли из системы', 'info')
}


function onSelectEvent(id) {
  selectedEventId.value = id
}
</script>

<template>
  <div class="app">
    <template v-if="!isAuthenticated">
      <LoginForm 
        v-if="authMode === 'login'"
        @login-success="onLoginSuccess" 
        @switch-to-register="authMode = 'register'"
      />
      <RegisterForm 
        v-if="authMode === 'register'"
        @switch-to-login="authMode = 'login'"
        @register-success="authMode = 'login'"
      />
    </template>

    <main v-else class="dashboard">
      <header class="dashboard__header">
        <div class="header-left">
          <h2>PredictionApp</h2>
          <nav class="nav-links">
            <a 
              href="#" 
              @click.prevent="currentTab = 'events'; selectedEventId = null"
              :class="{ active: currentTab === 'events' }"
            >События</a>
            <a 
              href="#" 
              @click.prevent="currentTab = 'leaderboard'; selectedEventId = null"
              :class="{ active: currentTab === 'leaderboard' }"
            >Рейтинг</a>
            <a 
              href="#" 
              @click.prevent="currentTab = 'profile'; selectedEventId = null"
              :class="{ active: currentTab === 'profile' }"
            >Профиль</a>
            <a 
              href="#" 
              @click.prevent="currentTab = 'admin'; selectedEventId = null"
              :class="{ active: currentTab === 'admin' }"
              v-if="currentUser === 'admin'"
            >Админка</a>
          </nav>
        </div>
        <div class="header-right">
          <span style="margin-right: 1rem;">{{ currentUser }}</span>
          <BaseButton variant="danger" @click="onLogout">Выйти</BaseButton>
        </div>
      </header>
      
      <section class="dashboard__content">
        <template v-if="currentTab === 'events'">
          <EventList 
            v-if="!selectedEventId" 
            @select-event="onSelectEvent" 
          />
          <PredictionForm 
            v-else 
            :eventId="selectedEventId" 
            @cancel="selectedEventId = null"
            @prediction-success="selectedEventId = null; currentTab = 'profile'"
          />
          
        </template>
        <AdminPanel v-else-if="currentTab === 'admin'" />
        <UserProfile v-else-if="currentTab === 'profile'" />
        <Leaderboard v-else-if="currentTab === 'leaderboard'" />
      </section>
    </main>

    <AppToast :toasts="toasts" @dismiss="removeToast" />
  </div>
</template>

<style scoped>
.app {
  min-height: 100vh;
  padding: 1.5rem;
}

.dashboard {
  max-width: 900px;
  margin: 0 auto;
}

.dashboard__header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 2rem;
  padding-bottom: 1rem;
  border-bottom: 1px solid var(--color-border);
}

.header-left {
  display: flex;
  align-items: center;
  gap: 2rem;
}

.nav-links {
  display: flex;
  gap: 1rem;
}

.nav-links a {
  text-decoration: none;
  color: var(--color-text-muted);
  font-weight: 500;
}

.nav-links a.active {
  color: var(--color-primary);
  border-bottom: 2px solid var(--color-primary);
}

.dashboard__content {
  background: var(--color-surface);
  border: 1px solid var(--color-border);
  border-radius: var(--radius);
  padding: 2rem;
}

@media (max-width: 768px) {

  .app {
    padding: 1rem 0.5rem;
  }

  .dashboard__header {
    flex-direction: column;
    align-items: flex-start;
    gap: 1rem;
  }
  
  .header-left {
    display: flex;
    flex-direction: column;
    align-items: center; 
    gap: 1rem;
  }

  .header-right {
    width: 100%;
    display: flex;
    justify-content: space-between;
    align-items: center;
    padding-top: 1rem;
    border-top: 1px dashed var(--color-border); 
  }
}

</style>