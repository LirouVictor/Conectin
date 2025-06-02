import { createApp } from 'vue';
import { createPinia } from 'pinia';
import App from './App.vue';
import router from './router';
import Toast from 'vue-toastification';
import 'vue-toastification/dist/index.css';
import { useUserStore } from './stores/user'; // 1. Importe o userStore

const app = createApp(App);
const pinia = createPinia();

app.use(pinia); // 2. Use Pinia primeiro para que as stores estejam disponíveis

// 3. Chame loadUser ANTES de router.isReady() e app.mount()
const userStore = useUserStore();

// loadUser agora é async, então precisamos esperar por ele.
userStore.loadUser().then(() => {
    // É importante esperar o router estar pronto, especialmente se você tiver
    // guardas de navegação que dependem do estado do usuário.
    router.isReady().then(() => {
        app.mount('#app');
    });
}).catch(error => {
    console.error("Falha ao inicializar o userStore ou router:", error);
    // Mesmo se houver um erro (ex: token inválido), monte o app
    // para que o usuário possa, por exemplo, ir para a página de login.
    router.isReady().then(() => {
        app.mount('#app');
    });
});

// Configurar plugins depois de criar o app e usar Pinia, mas antes de montar
app.use(router); // O router já é usado acima dentro do .then(), pode ser redundante aqui, mas não prejudica.
app.use(Toast, {
    position: 'top-right',
    timeout: 3000,
    closeOnClick: true,
    pauseOnHover: true,
    // Adicione outras opções globais do toast se desejar
});