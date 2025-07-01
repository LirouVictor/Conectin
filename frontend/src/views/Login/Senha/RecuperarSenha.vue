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
/*
  --- PALETA DE CORES E FONTES PADRÃO ---
  Azul Estrutural: #1E7AC5
  Amarelo Ação/Destaque: #F8B617
  Fundo: #F7F9FC
  Fonte Principal: 'Roboto', Arial, sans-serif
*/

/* Container e Box (Padrão da aplicação) */
.recovery-container {
  display: flex;
  align-items: center;
  justify-content: center;
  min-height: calc(100vh - 120px);
  background-color: #f7f9fc;
  padding: 20px;
  font-family: 'Roboto', Arial, sans-serif;
}

.recovery-box {
  width: 100%;
  max-width: 420px;
  padding: 40px;
  background-color: #ffffff;
  border-radius: 16px;
  box-shadow: 0 10px 30px rgba(30, 122, 197, 0.1);
  text-align: center;
}

/* Títulos e Textos Instrutivos */
.recovery-box h2 {
  color: #2c3e50;
  margin-bottom: 15px;
  font-size: 28px;
  font-weight: 700;
}

.recovery-instructions {
  margin-bottom: 30px;
  color: #5A6A7B;
  font-size: 16px;
  line-height: 1.5;
}

/* Inputs e Labels (Padrão da aplicação) */
.input-group {
  margin-bottom: 25px;
  text-align: left;
}

.input-group label {
  display: block;
  margin-bottom: 8px;
  color: #2c3e50;
  font-weight: 500;
  font-size: 14px;
}

.input-field {
  width: 100%;
  padding: 14px 16px;
  border: 1px solid #E0E6ED;
  border-radius: 8px;
  font-size: 16px;
  box-sizing: border-box;
  transition: border-color 0.3s, box-shadow 0.3s;
}

.input-field::placeholder {
  color: #aab5c0;
}

.input-field:focus {
  outline: none;
  border-color: #F8B617;
  box-shadow: 0 0 0 4px rgba(248, 182, 23, 0.2);
}

/* Botão de Ação Principal (Padrão da aplicação) */
.recovery-btn {
  width: 100%;
  padding: 15px;
  background-color: #F8B617;
  color: #FFFFFF;
  font-size: 16px;
  font-weight: bold;
  border: none;
  border-radius: 8px;
  cursor: pointer;
  transition: all 0.3s ease;
  margin-top: 10px;
  position: relative;
  overflow: hidden;
}

.recovery-btn:hover:not(:disabled) {
  background-color: #1E7AC5;
  transform: translateY(-2px);
  box-shadow: 0 4px 15px rgba(30, 122, 197, 0.3);
}

.recovery-btn:before {
  content: '';
  position: absolute;
  top: 0;
  left: -100%;
  width: 100%;
  height: 100%;
  background: linear-gradient(90deg, transparent, rgba(255,255,255,0.2), transparent);
  transition: left 0.7s;
}

.recovery-btn:hover:not(:disabled):before {
  left: 100%;
}

.recovery-btn:disabled {
  background-color: #f8b617;
  opacity: 0.6;
  cursor: not-allowed;
}

/* Link Secundário (Padrão da aplicação) */
.back-to-login {
  margin-top: 25px;
  font-size: 14px;
  color: #5A6A7B;
}

.back-to-login a {
  color: #1E7AC5;
  text-decoration: none;
  font-weight: 500;
}

.back-to-login a:hover {
  text-decoration: underline;
}
</style>