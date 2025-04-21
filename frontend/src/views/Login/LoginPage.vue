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
      </div>
    </div>
  </template>
  
  <script>
  import { useAuthStore } from '@/stores/auth';
  import { useToast } from 'vue-toastification';
  
  export default {
    data() {
      return {
        email: '',
        senha: '',
      };
    },
    setup() {
      const toast = useToast();
      return { toast };
    },
    methods: {
      async handleLogin() {
        const authStore = useAuthStore();
        try {
          const response = await authStore.login(this.email, this.senha);
          this.toast.success(response.message);
          this.$router.push('/');
        } catch (error) {
          if (error.response && error.response.data) {
            this.toast.error(error.response.data.message);
          } else {
            this.toast.error('Erro ao fazer login. Tente novamente.');
          }
        }
      },
    },
  };
  </script>
  
  <style scoped>
  .login-container {
    max-width: 400px;
    margin: 50px auto;
  }
  
  .login-box {
    padding: 20px;
    border: 1px solid #ccc;
    border-radius: 5px;
    box-shadow: 0 2px 5px rgba(0, 0, 0, 0.1);
  }
  
  .input-group {
    margin-bottom: 15px;
  }
  
  .input-field {
    width: 100%;
    padding: 8px;
    border: 1px solid #ccc;
    border-radius: 4px;
  }
  
  .login-btn {
    width: 100%;
    padding: 10px;
    background-color: #007bff;
    color: white;
    border: none;
    border-radius: 4px;
    cursor: pointer;
  }
  
  .login-btn:hover {
    background-color: #0056b3;
  }
  </style>