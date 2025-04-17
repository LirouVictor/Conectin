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
export default {
    data() {
        return {
            email: '',
            senha: '',
        };
    },
    methods: {
        async handleLogin() {
            try {
                const response = await this.$axios.post('/api/auth/login', {
                    email: this.email,
                    senha: this.senha,
                });
                console.log(response.data);
            } catch (error) {
                console.error(error);
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
    /* Ajusta para o espaço do header */
    background-color: #f5f5f5;
    /* Fundo cinza claro, consistente com o site */
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
    /* Borda azul do logo */
}

h2 {
    font-size: 2rem;
    color: #257BB8;
    /* Título azul */
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
    /* Borda azul */
    border-radius: 5px;
    transition: border-color 0.3s;
}

.input-field:focus {
    border-color: #F4B400;
    /* Amarelo ao focar */
    outline: none;
}

.login-btn {
    width: 100%;
    padding: 12px;
    font-size: 1rem;
    background-color: #F4B400;
    /* Amarelo do logo */
    color: #FFFFFF;
    border: none;
    border-radius: 5px;
    cursor: pointer;
    transition: background-color 0.3s;
}

.login-btn:hover {
    background-color: #d9a300;
    /* Amarelo mais escuro ao passar o mouse */
}
</style>