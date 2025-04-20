import { defineStore } from 'pinia';

export const useUserStore = defineStore('user', {
  state: () => ({
    user: null,
  }),
  actions: {
    loadUser() {
      const userData = localStorage.getItem('usuarioLogado');
      if (userData) {
        this.user = JSON.parse(userData);
      }
    },
    logout() {
      this.user = null;
      localStorage.removeItem('usuarioLogado');
    },
  },
});