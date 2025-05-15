import { defineStore } from 'pinia';
import api from '@/services/api';

const BACKEND_BASE_URL = 'http://localhost:8080';

export const useUserStore = defineStore('user', {
    state: () => ({
        user: null,
    }),
actions: {

        async loadUser() {
            const token = localStorage.getItem('token');
            if (token) {
                try {
                    const response = await api.get('/usuarios/perfil', {
                        headers: { Authorization: `Bearer ${token}` },
                    });
                    this.user = {
                        id: response.data.id,
                        nome: response.data.nome,
                        email: response.data.email,
                        endereco: response.data.endereco,
                        telefone: response.data.telefone,
                        foto: response.data.foto
                            ? `${BACKEND_BASE_URL}${response.data.foto}` // Prepend backend base URL
                            : 'https://www.gravatar.com/avatar/?d=mp',
                        prestador: response.data.tipos.includes('PRESTADOR'),
                        cliente: response.data.tipos.includes('CLIENTE'),
                    };
                } catch (error) {
                    console.error('Erro ao carregar usuário:', error);
                    this.logout();
                }
            }
        },
        setUser(userData) {
                    this.user = {
                        ...this.user,
                        ...userData,
                        foto: userData.foto
                            ? userData.foto.startsWith('http')
                                ? userData.foto // Already an absolute URL
                                : `${BACKEND_BASE_URL}${userData.foto}` // Prepend backend base URL
                            : this.user?.foto || 'https://www.gravatar.com/avatar/?d=mp',
                    };
                },
        logout() {
            this.user = null;
            localStorage.removeItem('token');
        },
    },
});