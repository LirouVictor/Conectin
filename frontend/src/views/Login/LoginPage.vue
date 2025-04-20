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
import { useAuthStore } from '@/stores/auth'; // Ajuste o caminho conforme necessário

export default {
    data() {
        return {
            email: '',
            senha: '',
        };
    },
    methods: {
        async handleLogin() {
            const authStore = useAuthStore();
            try {
                const response = await authStore.login(this.email, this.senha);
                console.log('Login bem-sucedido:', response);
                // Redirecionar para outra página, se necessário
                this.$router.push('/');
            } catch (error){
                if (error.response && error.response.data) {
                    this.mensagemErro = error.response.data; // Exibe mensagem do backend
                } else {
                    this.mensagemErro = 'Erro ao fazer login. Tente novamente.';
                }
                console.error('Erro no login:', error);
            }
        },
    },
};
</script>

<style scoped>
.login-container {
    display: flex;
    justify-content: center;
    align-items: center;
    min-height: calc(100vh - 400px);
    background-color: #f5f5f5;
}

.login-box {
    background-color: #FFFFFF;
    padding: 40px;
    border-radius: 10px;
    box-shadow: 0 4px 15px rgba(0, 0, 0, 0.1);
    width: 100%;
    max-width: 400px;
    text-align: center;
    border: 2px solid #257BB8;
}

h2 {
    font-size: 2rem;
    color: #257BB8;
    margin-bottom: 30px;
}

.input-group {
    margin-bottom: 20px;
}

.input-field {
    width: 92%;
    padding: 12px;
    font-size: 1rem;
    border: 2px solid #257BB8;
    border-radius: 5px;
    transition: border-color 0.3s;
}

.input-field:focus {
    border-color: #F4B400;
    outline: none;
}

.login-btn {
    width: 100%;
    padding: 12px;
    font-size: 1rem;
    background-color: #F4B400;
    color: #FFFFFF;
    border: none;
    border-radius: 5px;
    cursor: pointer;
    transition: background-color 0.3s;
}

.login-btn:hover {
    background-color: #d9a300;
}
</style>