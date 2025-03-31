import { createRouter, createWebHistory } from 'vue-router';
import HomePage from '../views/HomePage.vue';
import LoginPage from '../views/login/LoginPage.vue';
import CadastroPage from '../views/login/CadastroPage.vue';
import ComoFuncionaPage from '../views/ComoFunciona.vue';

const routes = [
    { path: '/', component: HomePage },
    { path: '/login', component: LoginPage },
    { path: '/cadastro', component: CadastroPage },
    { path: '/comofunciona', component: ComoFuncionaPage },
];

const router = createRouter({
    history: createWebHistory(),
    routes,
});

export default router;