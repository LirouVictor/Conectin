<template>
    <div>
        <div v-if="user" class="avatar-dropdown" @click="toggleDropdown">
            <img 
                :src="user.foto ? user.foto : defaultAvatar" 
                class="avatar" 
                alt="Avatar" 
                @error="handleImageError"
            />
            <div v-if="dropdownVisible" class="dropdown-content">
                <router-link :to="{ name: 'EditarPerfilUsuario', params: { id: user.id } }">Meu Perfil</router-link>
                <a href="#" @click.prevent="logout">Logout</a>
            </div>
        </div>

        <div v-else class="logged-out-container">
            <div class="auth-links">
                <router-link to="/login" class="auth-link">Entrar</router-link>
                <router-link to="/cadastro" class="auth-link">Cadastre-se</router-link>
            </div>
            <img 
                :src="defaultAvatar" 
                class="avatar" 
                alt="Avatar Padrão" 
                @error="handleImageError"
            />

        </div>
    </div>
</template>

<script setup>
import { ref, computed, onMounted, onUnmounted } from 'vue';
import { useRouter } from 'vue-router';
import { useUserStore } from '../stores/user';

defineProps({
    defaultAvatar: {
        type: String,
        default: 'https://www.gravatar.com/avatar/?d=mp',
    },
});

const userStore = useUserStore();
const router = useRouter();

const dropdownVisible = ref(false);
// O 'user' computado continua sendo a chave para tudo funcionar
const user = computed(() => userStore.user);

// Esta função agora só será relevante quando o usuário estiver logado
const toggleDropdown = (event) => {
    event.stopPropagation();
    dropdownVisible.value = !dropdownVisible.value;
};

// Esta função de fechar o dropdown também só agirá quando ele estiver visível
const closeDropdown = (event) => {
    // A verificação impede que ele tente fechar algo que não está aberto
    if (dropdownVisible.value && !event.target.closest('.avatar-dropdown')) {
        dropdownVisible.value = false;
    }
};

const logout = () => {
    userStore.logout();
    dropdownVisible.value = false;
    // Redireciona para a página inicial após o logout
    router.push({ name: 'Home' }); 
};

const handleImageError = (event) => {
    console.error('Erro ao carregar imagem:', event.target.src);
    event.target.src = 'https://www.gravatar.com/avatar/?d=mp';
};

onMounted(() => {
    // Carrega o usuário para definir o estado inicial (logado ou deslogado)
    userStore.loadUser(); 
    document.addEventListener('click', closeDropdown);
});

onUnmounted(() => {
    document.removeEventListener('click', closeDropdown);
});
</script>

<style scoped>
/* Container para o estado logado */
.avatar-dropdown {
  position: relative;
  display: flex;
  align-items: center;
  cursor: pointer;
}

/* Container para o estado deslogado */
.logged-out-container {
  display: flex;
  align-items: center;
}

.auth-links {
  display: flex;
  align-items: center;
}

/* Estilo geral para os links/botões de autenticação */
.auth-link {
  background-color: transparent;
  border: none;;
  padding: 0;
  border-radius: 0; 
  font-weight: bold;
  font-size: 16px;  
  text-decoration: none;
  color: #257bb8; /* Cor azul solicitada */
  margin: 0 8px;
  transition: color 0.2s ease, transform 0.2s ease;
  text-align: center;
  border: 1px solid transparent;
}

/* Botão "Entrar" */


/* EFEITO HOVER AJUSTADO PARA AMBOS OS BOTÕES */
.auth-link:hover{
  background-color: transparent; /* Cor amarela solicitada */
  color: #f4b400; /* Garante que o texto fique branco */
  transform: translateY(-1px);
}


/* --- O RESTANTE DO CSS CONTINUA O MESMO --- */

/* Estilos do avatar */
.avatar {
  width: 60px;
  height: 60px;
  border-radius: 50%;
  object-fit: cover;
  border: 2px solid #ddd;
}

/* Estilo para o ícone de fallback quando não há foto */
.fallback-avatar {
  display: flex;
  align-items: center;
  justify-content: center;
  background-color: #f0f0f0;
  color: #a0a0a0;
}

.fallback-avatar svg {
    width: 22px;
    height: 22px;
}

/* Estilos do dropdown */
.dropdown-content {
  position: absolute;
  top: 55px;
  right: 0;
  background-color: #ffffff;
  border: 1px solid #e0e0e0;
  border-radius: 8px;
  min-width: 160px;
  z-index: 1000;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
  overflow: hidden;
}

.dropdown-content a {
  display: block;
  padding: 12px 15px;
  color: #333;
  text-decoration: none;
  font-weight: 500;
  transition: background-color 0.2s, color 0.2s;
}

.dropdown-content a:hover {
  background-color: #f4b400;
  color: #fff;
}
</style>