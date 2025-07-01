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
/* ==========================================================================
   PALETA DE CORES CONECTIN - APLICADA AO MODAL
   ========================================================================== */
:root {
    --azul-principal: #1E7AC5;
    --azul-escuro: #155e8f;
    --amarelo-destaque: #F8B617;
    --amarelo-escuro: #e0a800;
    --fundo-secundario: #F7F9FC;
    --texto-principal: #2c3e50;
    --texto-secundario: #5A6A7B;
    --borda-neutra: #E0E6ED;
}

/* ==========================================================================
   ESTILO DO CONTEÚDO DO MODAL
   ========================================================================== */
p {
    color: var(--texto-secundario);
    font-size: 1.1rem;
    line-height: 1.6;
    text-align: center;
    margin: 15px 0 25px 0;
    /* Mais espaço para respiro */
}

/* 
  Estiliza o conteúdo injetado por v-html.
  :deep() é um seletor do Vue que permite estilizar
  elementos filhos de um componente com escopo.
*/
:deep(b) {
    color: var(--azul-escuro);
    font-weight: 600;
}

:deep(i) {
    color: var(--azul-principal);
    font-style: normal;
    /* Remove itálico para um visual mais limpo */
}

/* ==========================================================================
   ESTILO DOS BOTÕES DE AÇÃO DO FOOTER
   ========================================================================== */

.login-btn {
    width: 100%;
    padding: 15px;
    background-color: #F8B617;
    /* **MUDANÇA PRINCIPAL:** Botão de login é amarelo */
    color: #FFFFFF;
    font-size: 16px;
    font-weight: bold;
    border: none;
    border-radius: 8px;
    cursor: pointer;
    transition: all 0.3s ease;
    margin-top: 10px;
}

.login-btn:hover {}

.login-btn:before {
    content: '';
    position: absolute;
    top: 0;
    left: -100%;
    width: 100%;
    height: 100%;
    background: linear-gradient(90deg, transparent, rgba(255, 255, 255, 0.2), transparent);
    transition: left 0.7s;
}

.login-btn:hover:before {
    left: 100%;
}

.login-btn::active {
    transform: translateY(0);
}

.btn-primary {
    width: 100%;
    padding: 15px;
    background-color: #F8B617;
    color: #FFFFFF;
    font-size: 16px;
    font-weight: bold;
    border: none;
    border-radius: 8px;
    cursor: pointer;
    transition: all 0.3s ease;
    margin-top: 10px;
}

.btn-primary:hover {
    background-color: #1E7AC5;
    /* **MUDANÇA:** Hover state agora é o azul principal */
    transform: translateY(-3px);
    box-shadow: 0 4px 15px rgba(30, 122, 197, 0.3);
    color: #fff;
    text-decoration: none;
    transform: translateY(-2px);
}

/* .btn-primary:before {
    content: '';
    position: absolute;
    top: 0;
    left: -100%;
    width: 100%;
    height: 100%;
    background: linear-gradient(90deg, transparent, rgba(255, 255, 255, 0.2), transparent);
    transition: left 0.7s;
} */

/* .btn-primary:hover:before {
    left: 100%;
}

.btn-primary::active {
    transform: translateY(0);
} */

.btn-secondary {
    width: 100%;
    padding: 15px;
    background-color: #F8B617;
    color: #FFFFFF;
    font-size: 16px;
    font-weight: bold;
    border: none;
    border-radius: 8px;
    cursor: pointer;
    transition: all 0.3s ease;
    margin-top: 10px;
}

.btn-secondary:hover {
    background-color: #1E7AC5;
    /* **MUDANÇA:** Hover state agora é o azul principal */
    transform: translateY(-3px);
    box-shadow: 0 4px 15px rgba(30, 122, 197, 0.3);
    color: #fff;
    text-decoration: none;
    transform: translateY(-2px);
}

/* .btn-secondary:before {
    content: '';
    position: absolute;
    top: 0;
    left: -100%;
    width: 100%;
    height: 100%;
    background: linear-gradient(90deg, transparent, rgba(255, 255, 255, 0.2), transparent);
    transition: left 0.7s;
} */

/* .btn-secondary:hover:before {
    left: 10%;
}

.btn-secondary::active {
    transform: translateY(0);
} */
</style>