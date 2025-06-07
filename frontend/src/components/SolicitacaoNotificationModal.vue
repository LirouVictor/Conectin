// src/components/SolicitacaoNotificationModal.vue
<template>
    <BaseModal :visible="modelValue" @update:visible="$emit('update:modelValue', $event)" :title="modalTitle"
        max-width="450px" :show-close-button="false">
        <p v-html="mensagem"></p> <!-- Usar v-html para formatar texto se necessário -->

        <template #footer>
            <button v-if="acaoPrimaria" @click="handleAcaoPrimaria" class="btn-primary">
                {{ acaoPrimaria.label }}
            </button>
            <button v-if="acaoSecundaria" @click="handleAcaoSecundaria" class="btn-secondary">
                {{ acaoSecundaria.label }}
            </button>
            <button v-if="acaoTerciaria" @click="handleAcaoTerciaria" class="btn-secondary">
                {{ acaoTerciaria.label }}
            </button>
        </template>
    </BaseModal>
</template>

<script setup>
import { computed, defineProps, defineEmits } from 'vue';
import BaseModal from './BaseModal.vue';

const props = defineProps({
    modelValue: Boolean, // Controla a visibilidade (v-model)
    solicitacao: {
        type: Object,
        default: null,
    },
});

const emit = defineEmits(['update:modelValue', 'acao']); // 'acao' emitirá { type: '...', payload: ... }

const infoPrestador = computed(() => props.solicitacao?.prestador?.nome || 'Prestador');
const infoCliente = computed(() => props.solicitacao?.cliente?.nome || 'Cliente');
const infoCategoria = computed(() => props.solicitacao?.categoria?.nome || 'Serviço');

const modalTitle = computed(() => {
    if (!props.solicitacao) return 'Notificação';
    switch (props.solicitacao.status) {
        case 'PENDENTE':
            return 'Confirmar Contratação';
        case 'EM_ANDAMENTO':
            return 'Confirmar Conclusão do Serviço';
        case 'AVALIACAO':
            return 'Avaliação Pendente';
        default:
            return 'Notificação de Serviço';
    }
});

const mensagem = computed(() => {
    if (!props.solicitacao) return '';
    switch (props.solicitacao.status) {
        case 'PENDENTE':
            return `Por favor, confirme o acordo de serviço de <i>${infoCategoria.value}</i> entre <b>${infoCliente.value}</b> e <b>${infoPrestador.value}</b>.`;

        case 'EM_ANDAMENTO':
            return `O serviço de <i>${infoCategoria.value}</i> entre <b>${infoCliente.value}</b> e <b>${infoPrestador.value}</b> foi concluído?`;

        case 'AVALIACAO':
            return `Existe uma avaliação pendente para o serviço de <i>${infoCategoria.value}</i> realizado entre <b>${infoCliente.value}</b> e <b>${infoPrestador.value}</b>. Deseja avaliar agora?`;

        default:
            return 'Você tem uma notificação sobre um serviço.';
    }
});

// Ações dinâmicas baseadas no status
const acaoPrimaria = computed(() => {
    if (!props.solicitacao) return null;
    switch (props.solicitacao.status) {
        case 'PENDENTE':
            return { label: 'Serviço Contratado', type: 'CONTRATOU' };
        case 'EM_ANDAMENTO':
            return { label: 'Sim, Concluído', type: 'CONCLUIU' };
        case 'AVALIACAO':
            return { label: 'Avaliar Agora', type: 'AVALIAR_AGORA' };
        default: return null;
    }
});

const acaoSecundaria = computed(() => {
    if (!props.solicitacao) return null;
    switch (props.solicitacao.status) {
        case 'PENDENTE':
            return { label: 'Não Contratado', type: 'NAO_CONTRATOU' };
        case 'EM_ANDAMENTO':
            return { label: 'Ainda Não', type: 'AINDA_NAO_CONCLUIU' }; // Ou 'Cancelar' se apropriado
        case 'AVALIACAO':
            return { label: 'Avaliar Depois', type: 'AVALIAR_DEPOIS' };
        default: return null;
    }
});

const acaoTerciaria = computed(() => {
    // Exemplo para o caso de 'NAO_CONTRATOU' levar a um 'CANCELAR'
    // Isso pode ser tratado diretamente na store também
    return null; // Simplificando por agora
});


const handleAcaoPrimaria = () => {
    emit('acao', { type: acaoPrimaria.value.type, solicitacao: props.solicitacao });
    emit('update:modelValue', false);
};

const handleAcaoSecundaria = () => {
    emit('acao', { type: acaoSecundaria.value.type, solicitacao: props.solicitacao });
    emit('update:modelValue', false);
};

const handleAcaoTerciaria = () => {
    if (acaoTerciaria.value) {
        emit('acao', { type: acaoTerciaria.value.type, solicitacao: props.solicitacao });
        emit('update:modelValue', false);
    }
};

</script>

<style scoped>
/* Adicione estilos específicos se necessário, mas a maioria virá do BaseModal */
p {
    font-size: 1.1em;
    text-align: center;
    margin-bottom: 15px;
}

.btn-primary {
    /* Reutilizando classes do BaseModal ou definindo novas */
    background-color: #28a745;
    /* Verde para confirmação */
}

.btn-primary:hover {
    background-color: #218838;
}
</style>