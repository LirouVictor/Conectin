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

.categoria-btn:hover {
    background-color: #1E7AC5;
    /* **MUDANÇA:** Hover state agora é o azul principal */
    transform: translateY(-3px);
    box-shadow: 0 4px 15px rgba(30, 122, 197, 0.3);
    color: #fff;
    text-decoration: none;
    transform: translateY(-2px);
}

p {
    text-align: center;
    margin-bottom: 10px;
    font-size: 1.1em;
}

.btn-secondary {
    width: 100%;
    padding: 15px;
    background-color: #1E7AC5;
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
    background-color: #F8B617;
    /* **MUDANÇA:** Hover state agora é o azul principal */
    transform: translateY(-3px);
    box-shadow: 0 4px 15px rgba(30, 122, 197, 0.3);
    color: #fff;
    text-decoration: none;
    transform: translateY(-2px);
}

</style>
