import { createRouter, createWebHistory } from 'vue-router';
import HomePage from '../views/HomePage.vue';
import LoginPage from '../views/Login/LoginPage.vue';
import CadastroPage from '../views/Login/CadastroPage.vue';
import ComoFuncionaPage from '../views/ComoFunciona.vue';
import RankingPrestadores from '../views/Prestador/RankingPrestadores.vue'; // Ajustado
import PerfilPrestador from '../views/Prestador/PerfilPrestador.vue'; // A ser criado

const routes = [
    { path: '/', component: HomePage },
    { path: '/login', component: LoginPage },
    { path: '/cadastro', component: CadastroPage },
    { path: '/comofunciona', component: ComoFuncionaPage },
    { path: '/ranking-prestadores', component: RankingPrestadores, name: 'RankingPrestadores' },
    { path: '/prestador/:id', component: PerfilPrestador, name: 'PerfilPrestador' }
];

const router = createRouter({
    history: createWebHistory(),
    routes,
});

export default router;