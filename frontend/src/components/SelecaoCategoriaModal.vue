// src/components/SelecaoCategoriaModal.vue
<template>
    <BaseModal :visible="visible" @update:visible="handleVisibilityChange" title="Selecionar Categoria do Serviço"
        max-width="400px" @close="cancelar">
        <div v-if="
            prestador && prestador.categorias && prestador.categorias.length > 0
        ">
            <p>
                Para qual dos serviços de <strong>{{ prestador.nome }}</strong> você
                gostaria de solicitar um orçamento?
            </p>
            <div class="categorias-list">
                <button v-for="categoria in prestador.categorias" :key="categoria.id"
                    @click="selecionarCategoria(categoria)" class="categoria-btn">
                    {{ categoria.nome }}
                </button>
            </div>
        </div>
        <div v-else>
            <p>Este prestador não possui categorias de serviço cadastradas.</p>
        </div>

        <template #footer>
            <button @click="cancelar" class="btn-secondary">Cancelar</button>
        </template>
    </BaseModal>
</template>

<script setup>
import { defineProps, defineEmits } from "vue";
import BaseModal from "./BaseModal.vue";

defineProps({
    visible: Boolean,
    prestador: {
        type: Object,
        default: null,
    },
});

const emit = defineEmits([
    "update:visible",
    "categoria-selecionada",
    "cancelado",
]);

const selecionarCategoria = (categoria) => {
    emit("categoria-selecionada", categoria);
    emit("update:visible", false);
};

const cancelar = () => {
    emit("cancelado");
    emit("update:visible", false);
};

const handleVisibilityChange = (value) => {
    emit("update:visible", value);
    if (!value) {
        // Se estiver fechando o modal (pelo X ou ESC)
        cancelar();
    }
};
</script>

<style scoped>
.categorias-list {
    display: flex;
    flex-direction: column;
    gap: 10px;
    margin-top: 15px;
}

.categoria-btn {
    padding: 12px 15px;
    border: 1px solid var(--conectin-gray, #e1e5eb);
    background-color: #fff;
    color: var(--conectin-blue-dark, #156cb2);
    border-radius: 6px;
    cursor: pointer;
    text-align: left;
    font-size: 1em;
    transition:
        background-color 0.2s ease,
        color 0.2s ease;
}

.categoria-btn:hover {
    background-color: var(--conectin-blue-light, #e0f0ff);
    /* Cor de hover mais suave */
    border-color: var(--conectin-blue, #1e7ac5);
}

p {
    text-align: center;
    margin-bottom: 10px;
    font-size: 1.1em;
}

.btn-secondary {
    /* Estilo para o botão de cancelar no footer */
    background-color: #6c757d;
    color: white;
}

.btn-secondary:hover {
    background-color: #5a6268;
}
</style>
