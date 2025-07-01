<template>
  <div class="login-container">
    <div class="login-box">
      <h2>Login</h2>
      <form @submit.prevent="handleLogin">
        <div class="input-group">
          <input v-model="email" type="email" placeholder="Email" required class="input-field" />
        </div>
        <div class="input-group">
          <input v-model="senha" type="password" placeholder="Senha" required class="input-field" />
        </div>
        <button type="submit" class="login-btn">Entrar</button>
      </form>
      <div class="forgot-password">
        <p>Você esqueceu sua senha? <router-link to="/recuperar-senha">Clique aqui!</router-link></p>
      </div>
      
      <div class="signup-link">
        <p>Não tem cadastro? <router-link to="/cadastro">Cadastre-se AQUI!</router-link></p>
      </div>

    </div>
  </div>
</template>

<script>
import { useAuthStore } from '@/stores/auth';
import { useUserStore } from '@/stores/user';
import { useToast } from 'vue-toastification';

export default {
  data() {
    return {
      email: '',
      senha: '',
      showForgotPassword: false,
    };
  },
  setup() {
    const toast = useToast();
    return { toast };
  },
  methods: {
    async handleLogin() {
      const authStore = useAuthStore();
      const userStore = useUserStore();
      this.showForgotPassword = false;
      try {
        const response = await authStore.login(this.email, this.senha);
        this.toast.success(response.message);
        await userStore.loadUser();
        this.$router.push('/');
      } catch (error) {
        console.log('Erro completo:', error);
        if (error.response && error.response.data && error.response.data.message) {
          const errorMessage = error.response.data.message;
          this.toast.error(errorMessage);
          if (
            errorMessage.includes('Usuário não encontrado') ||
            errorMessage.includes('Senha incorreta')
          ) {
            this.showForgotPassword = true;
          }
        } else {
          this.toast.error('Erro ao fazer login: ' + (error.message || 'Servidor não retornou detalhes.'));
        }
      }
    },
  },
};
</script>

<style scoped>
/*
  --- PALETA DE CORES REVISADA - CONECTIN ---
  Azul Estrutural: #1E7AC5
  Amarelo Ação/Destaque: #F8B617
  Fundo: #F7F9FC
*/

.login-container {
  display: flex;
  align-items: center;
  justify-content: center;
  min-height: calc(100vh - 120px);
  background-color: #f7f9fc;
  padding: 20px;
  font-family: 'Roboto', Arial, sans-serif;
}

.login-box {
  width: 100%;
  max-width: 420px;
  padding: 40px;
  background-color: #ffffff;
  border-radius: 16px;
  box-shadow: 0 10px 30px rgba(30, 122, 197, 0.1);
  text-align: center;
}

.login-box h2 {
  color: #2c3e50;
  margin-bottom: 30px;
  font-size: 28px;
  font-weight: 700;
}

.input-group {
  margin-bottom: 20px;
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

.login-btn {
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
}

.login-btn:hover {
  background-color: #1E7AC5;
  transform: translateY(-3px);
  box-shadow: 0 4px 15px rgba(30, 122, 197, 0.3);
  color: #fff;
  text-decoration: none;
  transform: translateY(-2px);
}

.login-btn:before {
  content: '';
  position: absolute;
  top: 0;
  left: -100%;
  width: 100%;
  height: 100%;
  background: linear-gradient(90deg, transparent, rgba(255,255,255,0.2), transparent);
  transition: left 0.7s;
}

.login-btn:hover:before {
  left: 100%;
}

.login-btn::active {
  transform: translateY(0);
}

/* ESTILOS ATUALIZADOS AQUI */
.forgot-password,
.signup-link {
  font-size: 14px;
  color: #5A6A7B;
}
.forgot-password {
  margin-top: 25px;
}
.signup-link {
  margin-top: 15px;
}

.forgot-password a,
.signup-link a {
  color: #1E7AC5;
  text-decoration: none;
  font-weight: 500;
}

.forgot-password a:hover,
.signup-link a:hover {
  text-decoration: underline;
}
</style>