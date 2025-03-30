<template>
    <div>
        <h2>Login</h2>
        <form @submit.prevent="handleLogin">
            <input v-model="email" type="email" placeholder="Email" required />
            <input v-model="senha" type="password" placeholder="Senha" required />
            <button type="submit">Entrar</button>
        </form>
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