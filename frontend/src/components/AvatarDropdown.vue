<template>
    <div 
        class="avatar-dropdown" 
        @mouseenter="showDropdown" 
        @mouseleave="hideDropdown"
    >
        <img 
            :src="user && user.foto ? user.foto : defaultAvatar" 
            class="avatar" 
            alt="Avatar" 
            @error="handleImageError"
        />
        
        <Transition name="dropdown">
            <div v-if="dropdownVisible" class="dropdown-content">
                <template v-if="user">
                    <router-link :to="{ name: 'EditarPerfilUsuario', params: { id: user.id } }">Meu Perfil</router-link>
                    <a href="#" @click.prevent="logout">Logout</a>
                </template>
                <template v-else>
                    <router-link to="/login">Login</router-link>
                    <router-link to="/cadastro">Cadastre-se</router-link>
                </template>
            </div>
        </Transition>

    </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue';
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
const user = computed(() => userStore.user);

// Funções simples para mostrar/esconder no hover
const showDropdown = () => {
    dropdownVisible.value = true;
};

const hideDropdown = () => {
    dropdownVisible.value = false;
};

const logout = () => {
    userStore.logout();
    dropdownVisible.value = false;
    router.push({ name: 'Home' });
};

const handleImageError = (event) => {
    console.error('Erro ao carregar imagem:', event.target.src);
    event.target.src = 'https://www.gravatar.com/avatar/?d=mp';
};

onMounted(() => {
    userStore.loadUser();
});
</script>

<style scoped>
@property --angle {
  syntax: '<angle>';
  inherits: false;
  initial-value: 0deg;
}

.avatar-dropdown {
  position: relative;
  display: flex;
  align-items: center;
  cursor: pointer;
  /* AJUSTE CRÍTICO: Aumentando o padding para acomodar o avatar 
    escalado para 1.2 E a espessura do anel. 
  */
  padding: 10px; 
}

.avatar {
  width: 60px;
  height: 60px;
  border-radius: 50%;
  object-fit: cover;
  /* AJUSTE: Duração e timing-function sincronizados com a animação do círculo */
  transition: transform 0.2s linear; 
  position: relative;
  z-index: 2;
}

.avatar-dropdown:hover .avatar {
  /* AJUSTE: Nível do zoom aumentado para 1.2 */
  transform: scale(1.2);
}

.avatar-dropdown::before {
  content: '';
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  border-radius: 50%;
  background: conic-gradient(
    #f4b400 var(--angle), 
    transparent var(--angle)
  );
  animation: none;
}

.avatar-dropdown:hover::before {
  /* AJUSTE: Animação mais rápida e sincronizada */
  animation: draw-circle 0.2s linear forwards;
}

@keyframes draw-circle {
  to {
    --angle: 360deg;
  }
}

/* Estilos do dropdown (sem alterações) */
.dropdown-content {
  position: absolute;
  top: 82px; 
  right: 0;
  background-color: #ffffff;
  border: 1px solid #ccc;
  border-radius: 8px; /* Vamos arredondar o container também */
  min-width: 150px;
  z-index: 999;
  box-shadow: 0 2px 10px rgba(0, 0, 0, 0.1);
  
  /* MUDANÇAS AQUI */
  display: flex;
  flex-direction: column;
  gap: 8px; /* Espaço de 8px entre cada link/botão */
  padding: 8px; /* Um padding interno para o container */
  overflow: hidden; /* Garante que os cantos dos botões fiquem arredondados */
}

.dropdown-content a {
  /* Propriedades do botão que queremos */
  background-color: #F8B617;
  color: #FFFFFF;
  font-weight: bold;
  font-size: 16px;
  border-radius: 8px;
  transition: all 0.3s ease;
  
  /* Propriedades de layout adaptadas */
  display: block;
  padding: 15px; /* O padding maior que você queria */
  text-decoration: none;
  text-align: center; /* Centraliza o texto no botão */
  
  /* Propriedades que NÃO pegamos: width, border, cursor, margin-top */
}

.dropdown-content a:hover {
  background-color: #1E7AC5; /* **MUDANÇA:** Hover state agora é o azul principal */
  transform: translateY(-3px);
  box-shadow: 0 4px 15px rgba(30, 122, 197, 0.3);
  color: #fff;
  text-decoration: none;
  transform: translateY(-2px);
}
/* Animação de entrada/saída do Dropdown */

/* Estado inicial da entrada (antes de aparecer) */
.dropdown-enter-from,
.dropdown-leave-to {
  opacity: 0;
  transform: translateY(-10px); /* Começa 10px para cima */
}

/* Estado final da entrada (quando já apareceu) */
.dropdown-enter-to,
.dropdown-leave-from {
  opacity: 1;
  transform: translateY(0); /* Termina na posição normal */
}

/* Duração e suavização da transição */
.dropdown-enter-active,
.dropdown-leave-active {
  transition: all 0.2s ease-out;
}
</style>