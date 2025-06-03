<template>
  <div id="app-container">
    <Navbar />
    <router-view />

    <!-- Modal para Notificações de Solicitação -->
    <SolicitacaoNotificationModal v-if="userStore.user && userStore.solicitacaoParaNotificar"
      v-model:visible="userStore.mostrarModalNotificacao" :solicitacao="userStore.solicitacaoParaNotificar"
      @acao="userStore.lidarComAcaoNotificacao" @update:modelValue="handleModalNotificacaoVisibility" />

    <!-- Modal para Seleção de Categoria (usado por PerfilPrestador) -->
    <SelecaoCategoriaModal v-if="userStore.prestadorParaSelecaoCategoria"
      v-model:visible="userStore.mostrarModalSelecaoCategoria" :prestador="userStore.prestadorParaSelecaoCategoria"
      @categoria-selecionada="userStore.responderSelecaoCategoria" @cancelado="userStore.cancelarSelecaoCategoria"
      @update:visible="handleModalSelecaoVisibility" />
  </div>
</template>

<script setup>
import { onMounted, onUnmounted } from 'vue';
import Navbar from './components/NavBar.vue';
import SolicitacaoNotificationModal from './components/SolicitacaoNotificationModal.vue'; // Importe o novo modal
import SelecaoCategoriaModal from './components/SelecaoCategoriaModal.vue'; // <--- IMPORTAR O NOVO MODAL
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

// Função para lidar com o fechamento do modal de notificação vindo do BaseModal (Esc ou X)
const handleModalNotificacaoVisibility = (isVisible) => {
  userStore.mostrarModalNotificacao = isVisible;
  if (!isVisible && userStore.solicitacaoParaNotificar) { // Se fechou e tinha uma solicitação
    userStore.fecharModalNotificacao(); // Chama a action para lógica de fechar
  }
};
// Função para lidar com o fechamento do modal de seleção de categoria (Esc ou X)
const handleModalSelecaoVisibility = (isVisible) => {
  userStore.mostrarModalSelecaoCategoria = isVisible;
  if (!isVisible && userStore.prestadorParaSelecaoCategoria && userStore.resolveSelecaoCategoriaPromise) {
    console.log("App.vue: Modal de seleção fechado por X/ESC (via @update:visible). Chamando userStore.cancelarSelecaoCategoria().");
    userStore.cancelarSelecaoCategoria();
  }
}


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
// const handleModalVisibilityChange = (isVisible) => {
//   if (!isVisible && userStore.mostrarModalNotificacao) { // Se foi fechado por fora dos botões de ação
//     userStore.fecharModalNotificacao();
//   }
//   // Se isVisible for true, a store já deve ter setado mostrarModalNotificacao = true
// };
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