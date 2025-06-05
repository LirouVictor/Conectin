<template>
  <div class="recovery-container">
    <div class="recovery-box">
      <h2>Recuperar Senha</h2>
      <p class="recovery-instructions">
        Digite seu e-mail abaixo. Se estiver cadastrado em nosso sistema, enviaremos um link para você redefinir sua senha.
      </p>
      <form @submit.prevent="handleRecovery">
        <div class="input-group">
          <label for="email-recovery">Email</label>
          <input id="email-recovery" v-model="email" type="email" placeholder="Seu Email" required class="input-field" />
        </div>
        <button type="submit" class="recovery-btn" :disabled="isLoading">
          <span v-if="isLoading">Enviando...</span>
          <span v-else>Enviar Link de Recuperação</span>
        </button>
      </form>
      <div class="back-to-login">
        <p>Lembrou sua senha? <router-link to="/login">Voltar para o Login</router-link></p>
      </div>
    </div>
  </div>
</template>

<script>
import { useAuthStore } from '@/stores/auth'; // Certifique-se que o caminho está correto
import { useToast } from 'vue-toastification';

export default {
  name: 'RecuperarSenha',
  data() {
    return {
      email: '',
      isLoading: false,
    };
  },
  setup() {
    const toast = useToast();
    return { toast };
  },
  methods: {
    async handleRecovery() {
      if (!this.email) {
        this.toast.error('Por favor, insira seu e-mail.');
        return;
      }
      this.isLoading = true;
      const authStore = useAuthStore();
      try {
        // Você precisará criar uma ação 'requestPasswordRecovery' no seu authStore
        // Esta ação deve fazer a chamada API para o seu backend
        const response = await authStore.requestPasswordRecovery(this.email);
        this.toast.success(response.message || 'Instruções para recuperação de senha enviadas para o seu e-mail, caso ele esteja cadastrado.');
        this.email = ''; // Limpar o campo após o envio
      } catch (error) {
        console.error('Erro na recuperação de senha:', error);
        const errorMessage = error.response?.data?.message || error.message || 'Ocorreu um erro ao tentar recuperar a senha.';
        this.toast.error(errorMessage);
      } finally {
        this.isLoading = false;
      }
    },
  },
};
</script>

<style scoped>
.recovery-container {
  max-width: 400px;
  margin: 50px auto;
  font-family: Arial, sans-serif;
}

.recovery-box {
  padding: 25px;
  border: 1px solid #e0e0e0;
  border-radius: 8px;
  background-color: #fff;
  box-shadow: 0 4px 8px rgba(0, 0, 0, 0.1);
  text-align: center;
}

.recovery-box h2 {
  margin-bottom: 15px;
  color: #333;
}

.recovery-instructions {
  margin-bottom: 25px;
  color: #555;
  font-size: 0.95em;
  line-height: 1.6;
}

.input-group {
  margin-bottom: 20px;
  text-align: left;
}

.input-group label {
  display: block;
  margin-bottom: 5px;
  font-weight: bold;
  color: #444;
}

.input-field {
  width: calc(100% - 20px); /* Considera padding */
  padding: 10px;
  border: 1px solid #ccc;
  border-radius: 4px;
  box-sizing: border-box;
}

.recovery-btn {
  width: 100%;
  padding: 12px;
  background-color: #28a745; /* Verde para ação positiva */
  color: white;
  font-size: 1em;
  font-weight: bold;
  border: none;
  border-radius: 4px;
  cursor: pointer;
  transition: background-color 0.3s ease;
}

.recovery-btn:hover {
  background-color: #218838;
}

.recovery-btn:disabled {
  background-color: #cccccc;
  cursor: not-allowed;
}

.back-to-login {
  margin-top: 25px;
  font-size: 0.9em;
}

.back-to-login a {
  color: #007bff;
  text-decoration: none;
}

.back-to-login a:hover {
  text-decoration: underline;
}
</style>