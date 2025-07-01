<template>
    <div class="evaluation-page-container">
        <div v-if="loading" class="loading">Carregando...</div>
        <div v-if="error" class="error-message">{{ error }}</div>

        <div v-if="solicitacao && usuarioAvaliado" class="evaluation-form-card">
            <h1 class="title">Avaliar Serviço</h1>
            <div class="service-details">
                <p><strong>Serviço:</strong> {{ solicitacao.categoria.nome }}</p>
                <p>Você está avaliando <strong>{{ usuarioAvaliado.nome }}</strong>.</p>
            </div>

            <form @submit.prevent="submeterAvaliacao">
                <div class="form-group">
                    <label for="nota">Nota (1 a 5):</label>
                    <div class="star-rating">
                        <span v-for="star in 5" :key="star" class="star" :class="{ 'filled': star <= form.nota }"
                            @click="form.nota = star">
                            ★
                        </span>
                    </div>
                    <input type="number" v-model.number="form.nota" min="1" max="5" required hidden>
                </div>

                <div class="form-group">
                    <label for="comentario">Comentário:</label>
                    <textarea id="comentario" v-model="form.comentario" rows="4"></textarea>
                </div>

                <button type="submit" class="submit-btn" :disabled="submitting">
                    {{ submitting ? 'Enviando...' : 'Enviar Avaliação' }}
                </button>
            </form>
        </div>
    </div>
</template>

<script setup>
import { ref, onMounted, computed } from 'vue';
import { useUserStore } from '@/stores/user';
import api from '@/services/api';
import { useToast } from 'vue-toastification';

const props = defineProps({
    solicitacaoId: {
        type: String,
        required: true,
    },
});

const userStore = useUserStore();
const toast = useToast();

const solicitacao = ref(null);
const loading = ref(true);
const submitting = ref(false);
const error = ref('');

const form = ref({
    nota: 0,
    comentario: '',
});

onMounted(async () => {
    if (!userStore.user) {
        // Caso o usuário abra a aba diretamente sem estar logado na outra
        await userStore.loadUser();
    }

    if (!userStore.user) {
        error.value = 'Você precisa estar logado para avaliar.';
        loading.value = false;
        return;
    }

    try {
        const response = await api.get(`/solicitacoes/${props.solicitacaoId}`);
        solicitacao.value = response.data;
    } catch (err) {
        error.value = 'Não foi possível carregar os dados da solicitação. Verifique se ela é válida.';
        console.error(err);
    } finally {
        loading.value = false;
    }
});

const usuarioAvaliado = computed(() => {
    if (!solicitacao.value || !userStore.user) return null;

    // Compara o ID do usuário logado com o ID do cliente da solicitação
    if (userStore.user.id === solicitacao.value.cliente.id) {
        return solicitacao.value.prestador; // Cliente está avaliando o Prestador
    } else {
        return solicitacao.value.cliente; // Prestador está avaliando o Cliente
    }
});

const submeterAvaliacao = async () => {
    if (form.value.nota === 0) {
        toast.error('Por favor, selecione uma nota.');
        return;
    }
    submitting.value = true;
    try {
        const payload = {
            solicitacaoId: solicitacao.value.id,
            avaliadorId: userStore.user.id,
            nota: parseFloat(form.value.nota),
            comentario: form.value.comentario,
        };

        await api.post('/avaliacoes/criar', payload);

        toast.success('Avaliação enviada com sucesso! Esta aba pode ser fechada.');
        // Desabilitar o botão para evitar envio duplo
        // Opcional: fechar a aba automaticamente após um delay
        setTimeout(() => {
            window.close();
        }, 2000);

    } catch (err) {
        toast.error(err.response?.data?.message || 'Erro ao enviar avaliação.');
        console.error(err);
        submitting.value = false;
    }
};
</script>

<style scoped>
.evaluation-page-container {
    display: flex;
    justify-content: center;
    align-items: center;
    min-height: 80vh;
    padding: 20px;
    background-color: #FFFFFF;
}

.evaluation-form-card {
    padding: 30px;
    background-color: #FFFFFF;
    border-radius: 12px;
    box-shadow: 0 4px 20px rgba(26, 119, 181, 0.15);
    position: relative;
    overflow: hidden;
    width: 100%;
    max-width: 500px;
}

.title {
    text-align: center;
    margin-bottom: 20px;
    color: #333;
}

.service-details {
    margin-bottom: 25px;
    padding: 15px;
    /* background-color: #eef; */
    /* border-left: 4px solid #6f42c1; */
    border-radius: 4px;
}

.form-group {
    margin-bottom: 20px;
}

label {
    display: block;
    margin-bottom: 8px;
    font-weight: bold;
}

textarea {
  width: 100%;
  padding: 14px 16px;
  border: 1px solid #E0E6ED;
  border-radius: 8px;
  font-size: 16px;
  box-sizing: border-box;
  transition: border-color 0.3s, box-shadow 0.3s;
}

textarea::placeholder {
  color: #aab5c0;
}

textarea:focus {
  outline: none;
  border-color: #F8B617; /* **MUDANÇA:** Borda de foco amarela */
  box-shadow: 0 0 0 4px rgba(248, 182, 23, 0.2); /* Sombra de foco amarela */
}

.star-rating {
    display: flex;
    justify-content: center;
    gap: 15px;
    font-size: 3.5rem;
    /* Estrelas bem grandes e clicáveis */
    cursor: pointer;
    line-height: 1;
    /* Garante que não haja espaçamento extra */
}

.star {
    color: var(--borda-neutra);
    transition: all 0.2s cubic-bezier(0.25, 0.46, 0.45, 0.94);
    /* Transição suave */
    text-shadow: 1px 1px 2px rgba(0, 0, 0, 0.1);
    /* Sombra sutil para profundidade */
}

/* EFEITO DE HOVER: a estrela atual e as anteriores se iluminam */
.star-rating:hover .star {
    color: #ffc107;
    transform: scale(1.2);
}

/* As estrelas seguintes à que está em hover voltam ao estado normal */
.star-rating .star:hover~.star {
    color: var(--borda-neutra);
    transform: scale(1.0);
}

/* Estrela SELECIONADA (clicada) */
.star.filled {
    color: #ffc107;
    transform: scale(1.1);
    text-shadow: 0 0 15px rgba(248, 182, 23, 0.5);
    /* Brilho amarelo ao redor */
}



.submit-btn {
  width: 100%;
  padding: 15px;
  background-color: #F8B617; /* **MUDANÇA PRINCIPAL:** Botão de login é amarelo */
  color: #FFFFFF;
  font-size: 16px;
  font-weight: bold;
  border: none;
  border-radius: 8px;
  cursor: pointer;
  transition: all 0.3s ease;
  margin-top: 10px;
}

.submit-btn:hover {
  background-color: #1E7AC5; /* **MUDANÇA:** Hover state agora é o azul principal */
  transform: translateY(-3px);
  box-shadow: 0 4px 15px rgba(30, 122, 197, 0.3);
  color: #fff;
  text-decoration: none;
  transform: translateY(-2px);
}

.submit-btn:before {
  content: '';
  position: absolute;
  top: 0;
  left: -100%;
  width: 100%;
  height: 100%;
  background: linear-gradient(90deg, transparent, rgba(255,255,255,0.2), transparent);
  transition: left 0.7s;
}

.submit-btn:hover:before {
  left: 100%;
}

.submit-btn::active {
  transform: translateY(0);
}


/* 

.submit-btn {
    width: 100%;
    padding: 12px;
    border: none;
    background-color: #6f42c1;
    color: white;
    border-radius: 4px;
    font-size: 1rem;
    font-weight: bold;
    cursor: pointer;
    transition: background-color 0.3s ease;
}

.submit-btn:hover {
    background-color: #5a349c;
}

.submit-btn:disabled {
    background-color: #ccc;
    cursor: not-allowed;
} */

.error-message {
    color: red;
    text-align: center;
}
</style>