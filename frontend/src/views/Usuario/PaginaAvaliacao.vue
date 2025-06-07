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
    background-color: #f4f4f9;
}

.evaluation-form-card {
    background: white;
    padding: 30px;
    border-radius: 8px;
    box-shadow: 0 4px 10px rgba(0, 0, 0, 0.1);
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
    background-color: #eef;
    border-left: 4px solid #6f42c1;
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
    padding: 10px;
    border-radius: 4px;
    border: 1px solid #ccc;
    font-family: inherit;
    resize: vertical;
}

.star-rating {
    font-size: 2.5rem;
    cursor: pointer;
}

.star {
    color: #ccc;
}

.star.filled {
    color: #ffc107;
}

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
}

.error-message {
    color: red;
    text-align: center;
}
</style>