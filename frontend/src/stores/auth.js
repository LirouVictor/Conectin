import { defineStore } from 'pinia';
import api from '@/services/api'; // usa a instância com baseURL definida

export const useAuthStore = defineStore('auth', {
    state: () => ({
        user: null,
        token: null,
    }),
    actions: {
        async login(email, senha) {
            const response = await api.post('usuarios/login', { email, senha });
            this.user = response.data.user || { email };
            this.token = response.data.token || null;
            return response.data;
        },
        logout() {
            this.user = null;
            this.token = null;
        },
    },
});
