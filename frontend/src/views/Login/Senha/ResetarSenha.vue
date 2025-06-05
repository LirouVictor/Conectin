<template>
  <div class="reset-container">
    <div class="reset-box">
      <h2>Redefinir sua Senha</h2>
      <p v-if="!successMessage" class="reset-instructions">
        Por favor, crie uma nova senha para sua conta.
      </p>
      
      <form v-if="!successMessage" @submit.prevent="handleResetPassword">
        <div class="input-group">
          <label for="novaSenha">Nova Senha</label>
          <input id="novaSenha" v-model="novaSenha" type="password" placeholder="Digite sua nova senha" required class="input-field" />
        </div>
        <div class="input-group">
          <label for="confirmarNovaSenha">Confirmar Nova Senha</label>
          <input id="confirmarNovaSenha" v-model="confirmarNovaSenha" type="password" placeholder="Confirme sua nova senha" required class="input-field" />
        </div>
        
        <div v-if="errorMessage" class="error-message">
          {{ errorMessage }}
        </div>

        <button type="submit" class="reset-btn" :disabled="isLoading">
          <span v-if="isLoading">Salvando...</span>
          <span v-else>Redefinir Senha</span>
        </button>
      </form>

      <div v-if="successMessage" class="success-message">
        <p>{{ successMessage }}</p>
        <router-link to="/login" class="login-link">Ir para o Login</router-link>
      </div>
    </div>
  </div>
</template>

<script>
import { useAuthStore } from '@/stores/auth';
import { useToast } from 'vue-toastification';
import { useRouter } from 'vue-router';

export default {
  name: 'ResetarSenha',
  props: {
    token: {
      type: String,
      required: false, // Opcional para não quebrar antes da verificação
      default: null,
    },
  },
  data() {
    return {
      novaSenha: '',
      confirmarNovaSenha: '',
      isLoading: false,
      errorMessage: '',
      successMessage: ''
    };
  },
  setup() {
    const toast = useToast();
    const router = useRouter();
    return { toast, router };
  },
  mounted() {
    // Se não houver token na URL, redireciona o usuário
    if (!this.token) {
      this.toast.error("Token de redefinição inválido ou ausente. Por favor, solicite um novo link.");
      this.router.push('/recuperar-senha');
    }
  },
  methods: {
    async handleResetPassword() {
  this.errorMessage = '';
  
  // --- Início da Nova Validação ---
  const erros = [];
  const senha = this.novaSenha;

  if (senha.length < 8) {
    erros.push('A senha deve ter pelo menos 8 caracteres.');
  }
  if (!/[A-Z]/.test(senha)) {
    erros.push('A senha deve conter pelo menos uma letra maiúscula.');
  }
  if (!/[a-z]/.test(senha)) {
    erros.push('A senha deve conter pelo menos uma letra minúscula.');
  }
  if (!/\d/.test(senha)) {
    erros.push('A senha deve conter pelo menos um número.');
  }
  if (!/[!@#$%^&*(),.?":{}|<>]/.test(senha)) {
    erros.push('A senha deve conter pelo menos um caractere especial (ex: !@#$).');
  }
  if (senha !== this.confirmarNovaSenha) {
    erros.push('As senhas não coincidem.');
  }

  if (erros.length > 0) {
    // Junta todos os erros em uma única mensagem com quebras de linha
    this.errorMessage = erros.join('\n');
    // Mostra o primeiro erro no toast para feedback rápido
    this.toast.error(erros[0]);
    return; // Interrompe a execução se houver erros
  }
  // --- Fim da Nova Validação ---

  this.isLoading = true;
  const authStore = useAuthStore();

  try {
    const payload = {
      token: this.token,
      novaSenha: this.novaSenha,
      confirmarNovaSenha: this.confirmarNovaSenha,
    };
    const response = await authStore.resetPassword(payload);
    
    this.toast.success(response.message || 'Senha redefinida com sucesso!');
    this.successMessage = 'Senha redefinida com sucesso! Você já pode fazer login com sua nova senha.';

  } catch (error) {
    console.error('Erro ao redefinir senha:', error);
    const backendErrorMessage = error.response?.data?.message || error.message || 'Ocorreu um erro ao redefinir a senha.';
    this.toast.error(backendErrorMessage);
    this.errorMessage = backendErrorMessage;
  } finally {
    this.isLoading = false;
  }
},
}
};
</script>

<style scoped>
/* Estilos similares aos outros componentes de autenticação */
.reset-container {
  max-width: 400px;
  margin: 50px auto;
}

.reset-box {
  padding: 25px;
  border: 1px solid #e0e0e0;
  border-radius: 8px;
  box-shadow: 0 4px 8px rgba(0, 0, 0, 0.1);
}

.reset-instructions {
  margin-bottom: 20px;
  color: #555;
}

.input-group {
  margin-bottom: 20px;
  text-align: left;
}

.input-field {
  width: calc(100% - 20px);
  padding: 10px;
  border: 1px solid #ccc;
  border-radius: 4px;
}

.reset-btn {
  width: 100%;
  padding: 12px;
  background-color: #ffc107;
  color: white;
  font-size: 1em;
  border: none;
  border-radius: 4px;
  cursor: pointer;
  transition: background-color 0.3s;
}

.reset-btn:hover {
  background-color: #257bb8;
}

.error-message {
  color: #dc3545;
  margin-bottom: 15px;
  font-size: 0.9em;
}

.success-message {
  color: #28a745;
  font-weight: bold;
}

.login-link {
  display: inline-block;
  margin-top: 20px;
  padding: 10px 20px;
  background-color: #007bff;
  color: white;
  text-decoration: none;
  border-radius: 4px;
}

.error-message {
  color: #dc3545;
  margin-bottom: 15px;
  font-size: 0.9em;
  white-space: pre-wrap; /* Essencial para respeitar as quebras de linha (\n) */
  text-align: left;
  background-color: #f8d7da;
  border: 1px solid #f5c6cb;
  border-radius: 4px;
  padding: 10px;
}
</style>