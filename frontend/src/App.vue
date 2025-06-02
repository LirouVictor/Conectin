<template>
  <div id="app-container">
    <Navbar />
    <router-view />

    <!-- Modal de Notificação de Solicitação -->
    <SolicitacaoNotificationModal
      v-if="userStore.solicitacaoParaNotificar"
      v-model="userStore.mostrarModalNotificacao"
      :solicitacao="userStore.solicitacaoParaNotificar"
      @acao="userStore.lidarComAcaoNotificacao"
      @update:modelValue="handleModalVisibilityChange"
    />
  </div>
</template>

<script setup>
import { onMounted, onUnmounted } from 'vue';
import Navbar from './components/NavBar.vue';
import SolicitacaoNotificationModal from './components/SolicitacaoNotificationModal.vue'; // Importe o novo modal
import { useUserStore } from './stores/user';

const userStore = useUserStore();

// Os listeners para 'visibilitychange' e 'focus' permanecem os mesmos
const verificarPendencias = () => {
  userStore.verificarSolicitacoesPendentes();
};

const handleVisibilityChange = () => {
  if (document.visibilityState === 'visible') {
    verificarPendencias();
  }
};

onMounted(() => {
  // loadUser em main.js já deve ter chamado a verificação inicial
  window.addEventListener('visibilitychange', handleVisibilityChange);
  window.addEventListener('focus', verificarPendencias);
});

onUnmounted(() => {
  window.removeEventListener('visibilitychange', handleVisibilityChange);
  window.removeEventListener('focus', verificarPendencias);
});

// Função para lidar com o fechamento do modal (pelo 'Esc' ou clique fora, via BaseModal)
const handleModalVisibilityChange = (isVisible) => {
    if (!isVisible && userStore.mostrarModalNotificacao) { // Se foi fechado por fora dos botões de ação
        userStore.fecharModalNotificacao();
    }
    // Se isVisible for true, a store já deve ter setado mostrarModalNotificacao = true
};
</script>

<style>
/* Global styles can go here if needed */
body {
    margin: 0;
    padding: 0;
  }
#app-container {
  font-family: Avenir, Helvetica, Arial, sans-serif;
  -webkit-font-smoothing: antialiased;
  -moz-osx-font-smoothing: grayscale;
  color: #2c3e50;
}
</style>