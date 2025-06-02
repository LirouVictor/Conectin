// src/components/BaseModal.vue
<template>
    <transition name="modal-fade">
        <div v-if="visible" class="modal-overlay" @click.self="closeModal">
            <div class="modal-container" :style="{ maxWidth: maxWidth }">
                <header class="modal-header">
                    <slot name="header">
                        <h3>{{ title }}</h3>
                    </slot>
                    <button v-if="showCloseButton" class="modal-close-btn" @click="closeModal">×</button>
                </header>
                <section class="modal-body">
                    <slot></slot> <!-- Conteúdo principal do modal -->
                </section>
                <footer class="modal-footer">
                    <slot name="footer">
                        <!-- Botões padrão podem ir aqui se não sobrescritos -->
                    </slot>
                </footer>
            </div>
        </div>
    </transition>
</template>

<script setup>
import { defineProps, defineEmits, watch } from 'vue';

const props = defineProps({
    visible: {
        type: Boolean,
        required: true,
    },
    title: {
        type: String,
        default: 'Notificação',
    },
    maxWidth: {
        type: String,
        default: '500px',
    },
    showCloseButton: {
        type: Boolean,
        default: true,
    }
});

const emit = defineEmits(['close', 'update:visible']);

const closeModal = () => {
    emit('update:visible', false);
    emit('close');
};

// Opcional: permitir fechar com a tecla Esc
watch(() => props.visible, (isVisible) => {
    if (isVisible) {
        document.addEventListener('keydown', onKeydown);
    } else {
        document.removeEventListener('keydown', onKeydown);
    }
});

const onKeydown = (event) => {
    if (event.key === 'Escape' && props.visible) {
        closeModal();
    }
};
</script>

<style scoped>
.modal-fade-enter-active,
.modal-fade-leave-active {
    transition: opacity 0.3s ease;
}

.modal-fade-enter-from,
.modal-fade-leave-to {
    opacity: 0;
}

.modal-overlay {
    position: fixed;
    top: 0;
    left: 0;
    width: 100%;
    height: 100%;
    background-color: rgba(0, 0, 0, 0.6);
    display: flex;
    justify-content: center;
    align-items: center;
    z-index: 1000;
    /* Alto z-index */
}

.modal-container {
    background-color: #fff;
    border-radius: 8px;
    box-shadow: 0 10px 25px rgba(0, 0, 0, 0.15);
    padding: 25px;
    width: 90%;
    display: flex;
    flex-direction: column;
}

.modal-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    border-bottom: 1px solid #eee;
    padding-bottom: 15px;
    margin-bottom: 20px;
}

.modal-header h3 {
    margin: 0;
    font-size: 1.4em;
    color: #333;
}

.modal-close-btn {
    background: none;
    border: none;
    font-size: 1.8em;
    cursor: pointer;
    color: #888;
    padding: 0 5px;
}

.modal-close-btn:hover {
    color: #555;
}

.modal-body {
    margin-bottom: 20px;
    line-height: 1.6;
    color: #555;
    max-height: 60vh;
    overflow-y: auto;
}

.modal-footer {
    display: flex;
    justify-content: flex-end;
    gap: 10px;
    padding-top: 15px;
    border-top: 1px solid #eee;
}

/* Estilos de botões para o footer (exemplo) */
.modal-footer button {
    padding: 10px 20px;
    border-radius: 5px;
    border: none;
    cursor: pointer;
    font-weight: 500;
    transition: background-color 0.2s ease;
}

.btn-primary {
    background-color: var(--conectin-blue, #1e7ac5);
    /* Use suas variáveis CSS */
    color: white;
}

.btn-primary:hover {
    background-color: var(--conectin-blue-dark, #156cb2);
}

.btn-secondary {
    background-color: #f0f0f0;
    color: #333;
    border: 1px solid #ddd;
}

.btn-secondary:hover {
    background-color: #e0e0e0;
}
</style>