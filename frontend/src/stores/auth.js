import { defineStore } from 'pinia';
import api from '@/services/api';

export const useAuthStore = defineStore('auth', {
  state: () => ({
    user: null,
    token: null,
  }),
  actions: {
    async login(email, senha) {
      const response = await api.post('usuarios/login', { email, senha });
      const message = response.data.message;
      const token = message.split('Token: ')[1];
      this.token = token;
      this.user = { email }; // Ajuste conforme os dados retornados pelo backend
      localStorage.setItem('token', token);
      localStorage.setItem('usuarioLogado', JSON.stringify(this.user));
      return response.data;
    },
    logout() {
      this.user = null;
      this.token = null;
      localStorage.removeItem('token');
      localStorage.removeItem('usuarioLogado');
    },
  },
});