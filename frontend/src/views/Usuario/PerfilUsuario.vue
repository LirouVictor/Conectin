<template>
    <div class="perfil-container">
        <div v-if="loading" class="loading">
            <div class="loading-spinner"></div>
            <p>Carregando perfil...</p>
        </div>
        <div v-else-if="usuario" class="perfil-box">
            <!-- Seção de Informações do Usuário -->
            <div class="usuario-header">
                <img :src="getFotoUrl(usuario.fotoPerfil)" :alt="'Foto de ' + usuario.nome" class="usuario-foto-grande">
                <div class="usuario-info">
                    <h1>{{ usuario.nome }}</h1>
                    <!-- Link para o perfil de prestador, se aplicável -->
                    <router-link v-if="isPrestador" :to="{ name: 'PerfilPrestador', params: { id: prestadorId } }"
                        class="link-prestador">
                        Ver perfil de prestador
                    </router-link>
                </div>
            </div>

            <!-- Seção de Avaliações Recebidas -->
            <div class="avaliacoes-section">
                <div class="avaliacoes-header">
                    <h3>Avaliações Recebidas</h3>
                    <div class="filtro-ordenacao" v-if="avaliacoes.length > 1">
                        <label for="filtro">Ordenar por:</label>
                        <select id="filtro" v-model="ordemFiltro">
                            <option value="maior">Maior Nota</option>
                            <option value="menor">Menor Nota</option>
                            <option value="recente">Mais Recente</option>
                        </select>
                    </div>
                </div>

                <div v-if="loadingAvaliacoes" class="loading-avaliacoes">
                    <div class="loading-spinner-small"></div>
                    Carregando avaliações...
                </div>
                <div v-else-if="avaliacoesOrdenadas.length > 0">
                    <div v-for="avaliacao in avaliacoesOrdenadas" :key="avaliacao.id" class="avaliacao-card">
                        <!-- Informações de quem avaliou este usuário -->
                        <div class="avaliador-info">
                            <img :src="getFotoUrl(avaliacao.avaliador.fotoPerfil)" alt="Foto do Avaliador"
                                class="avaliador-foto" />
                            <div class="avaliador-detalhes">
                                <span class="avaliador-nome">{{ avaliacao.avaliador.nome }}</span>
                                <span class="avaliacao-data">{{ formatarData(avaliacao.data) }}</span>
                            </div>
                        </div>
                        <!-- Conteúdo da avaliação -->
                        <div class="avaliacao-conteudo">
                            <div class="nota-estrelas">
                                <span v-for="n in 5" :key="n" class="estrela"
                                    :class="{ 'preenchida': n <= avaliacao.nota }">★</span>
                            </div>
                            <p class="comentario-texto">{{ avaliacao.comentario || 'Nenhum comentário deixado.' }}</p>
                        </div>
                    </div>
                </div>
                <div v-else class="sem-avaliacoes">
                    <p>Este usuário ainda não recebeu avaliações.</p>
                </div>
            </div>
        </div>
        <div v-else class="error-message">
            <p>Usuário não encontrado.</p>
        </div>
    </div>
</template>

<script>
import api from '@/services/api';
import { useToast } from 'vue-toastification';

export default {
    name: 'PerfilUsuario',
    props: {
        id: {
            type: [String, Number],
            required: true,
        },
    },
    data() {
        return {
            usuario: null,
            avaliacoes: [],
            prestadorId: null, // Para armazenar o ID do perfil de prestador
            loading: true,
            loadingAvaliacoes: true,
            ordemFiltro: 'maior',
            backendUrl: process.env.VUE_APP_API_BASE_URL || 'http://localhost:8080',
        };
    },
    computed: {
        isPrestador() {
            return this.usuario && this.usuario.tipos.includes('PRESTADOR');
        },
        avaliacoesOrdenadas() {
            const sorted = [...this.avaliacoes];
            if (this.ordemFiltro === 'maior') return sorted.sort((a, b) => b.nota - a.nota);
            if (this.ordemFiltro === 'menor') return sorted.sort((a, b) => a.nota - b.nota);
            if (this.ordemFiltro === 'recente') return sorted.sort((a, b) => new Date(b.data) - new Date(a.data));
            return sorted;
        },
    },
    methods: {
        async fetchData() {
            this.loading = true;
            this.loadingAvaliacoes = true;
            const toast = useToast();

            try {
                // Usamos Promise.all para fazer as chamadas em paralelo
                const [userResponse, avResponse] = await Promise.all([
                    api.get(`/usuarios/publico/${this.id}`),
                    api.get(`/avaliacoes/usuario/${this.id}`)
                ]);

                this.usuario = userResponse.data;
                this.avaliacoes = avResponse.data;

                // Se o usuário for um prestador, busca o ID do seu perfil de prestador
                if (this.isPrestador) {
                    // O endpoint /perfil de prestador tem o id do prestador
                    // Precisamos de um endpoint que relacione usuarioId -> prestadorId
                    // Por enquanto, vamos assumir que o ID do usuário e do prestador são diferentes.
                    // O ideal seria o endpoint /publico/{id} já retornar o prestadorId se houver.
                    // Vamos fazer uma chamada extra por enquanto:
                    const prestadorResponse = await api.get(`/prestadores/usuario/${this.id}`); // Assumindo que este endpoint existe
                    this.prestadorId = prestadorResponse.data.id;
                }

            } catch (error) {
                console.error("Erro ao buscar dados do usuário:", error);
                toast.error("Não foi possível carregar o perfil do usuário.");
                this.usuario = null; // Garante que a mensagem de erro seja exibida
            } finally {
                this.loading = false;
                this.loadingAvaliacoes = false;
            }
        },
        getFotoUrl(fotoPath) {
            if (!fotoPath) {
                // Se fotoPath for null, undefined ou "", retorna a imagem padrão
                return 'https://www.gravatar.com/avatar/?d=mp';
            }
            // Se já for uma URL completa, retorna ela mesma
            if (fotoPath.startsWith('http')) {
                return fotoPath;
            }
            // Caso contrário, constrói a URL completa com a base do backend
            return `${this.backendUrl}${fotoPath}`;
        },
        formatarData(dataString) {
            if (!dataString) return '';
            return new Date(dataString).toLocaleDateString('pt-BR');
        },
    },
    created() {
        this.fetchData();
    },
    watch: {
        // Se o usuário navegar de um perfil para outro, recarrega os dados
        id() {
            this.fetchData();
        },
    },
};
</script>

<style scoped>
/* Estilos podem ser reutilizados de PerfilPrestador.vue para consistência */
.perfil-container {
    max-width: 900px;
    margin: 30px auto;
    font-family: 'Roboto', Arial, sans-serif;
}

.perfil-box {
    padding: 30px;
    border-radius: 12px;
    background-color: #ffffff;
    box-shadow: 0 8px 24px rgba(30, 122, 197, 0.12);
}

.usuario-header {
    display: flex;
    align-items: center;
    gap: 20px;
    margin-bottom: 30px;
    padding-bottom: 20px;
    border-bottom: 1px solid #e1e5eb;
}

.usuario-foto-grande {
    width: 100px;
    height: 100px;
    border-radius: 50%;
    object-fit: cover;
    border: 3px solid #1e7ac5;
}

.usuario-info h1 {
    margin: 0;
    color: #156cb2;
}

.link-prestador {
    display: inline-block;
    margin-top: 10px;
    padding: 5px 10px;
    background-color: #f8b617;
    color: #fff;
    border-radius: 5px;
    text-decoration: none;
    font-weight: 500;
    transition: background-color 0.3s;
}

.link-prestador:hover {
    background-color: #e0a800;
}

/* Reutilizando estilos de avaliações */
.avaliacoes-section,
.avaliacoes-header,
.filtro-ordenacao,
.avaliacao-card,
.avaliador-info,
.avaliador-foto,
.avaliador-detalhes,
.avaliador-nome,
.avaliacao-data,
.avaliacao-conteudo,
.nota-estrelas,
.estrela,
.estrela.preenchida,
.comentario-texto,
.sem-avaliacoes,
.loading-avaliacoes,
.loading-spinner-small {
    /* Cole os estilos correspondentes de PerfilPrestador.vue aqui para manter a consistência */
}

.avaliacoes-section {
    margin-top: 20px;
}

.avaliacoes-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-bottom: 20px;
}

.avaliacoes-header h3 {
    color: #1e7ac5;
    font-size: 20px;
    margin: 0;
}

.filtro-ordenacao label {
    margin-right: 8px;
    font-size: 14px;
    color: #555;
}

.filtro-ordenacao select {
    padding: 6px 10px;
    border: 1px solid #e1e5eb;
    border-radius: 6px;
    background-color: #ffffff;
    font-size: 14px;
}

.avaliacao-card {
    background-color: #f5f7fa;
    border-radius: 8px;
    padding: 20px;
    margin-bottom: 15px;
    border-left: 4px solid #f8b617;
}

.avaliador-info {
    display: flex;
    align-items: center;
    gap: 15px;
    margin-bottom: 15px;
}

.avaliador-foto {
    width: 50px;
    height: 50px;
    border-radius: 50%;
    object-fit: cover;
    border: 2px solid #ffffff;
    box-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);
}

.avaliador-detalhes {
    display: flex;
    flex-direction: column;
}

.avaliador-nome {
    font-weight: bold;
    color: #156cb2;
}

.avaliacao-data {
    font-size: 12px;
    color: #777;
}

.nota-estrelas {
    margin-bottom: 8px;
    color: #f8b617;
    font-size: 18px;
}

.nota-estrelas .estrela {
    color: #ccc;
}

.nota-estrelas .estrela.preenchida {
    color: #f8b617;
}

.comentario-texto {
    margin: 0;
    line-height: 1.6;
    color: #333;
}

.sem-avaliacoes,
.loading-avaliacoes {
    text-align: center;
    padding: 20px;
    background-color: #f5f7fa;
    border-radius: 8px;
    color: #777;
}

.loading-spinner-small {
    width: 25px;
    height: 25px;
    border: 3px solid #e1e5eb;
    border-radius: 50%;
    border-top-color: #1e7ac5;
    animation: spin 1s linear infinite;
    display: inline-block;
    vertical-align: middle;
    margin-right: 10px;
}

.loading,
.error-message {
    text-align: center;
    padding: 50px;
}

.loading-spinner {
    width: 50px;
    height: 50px;
    border: 5px solid #e1e5eb;
    border-radius: 50%;
    border-top-color: #1e7ac5;
    animation: spin 1s linear infinite;
    margin: 0 auto 15px;
}

@keyframes spin {
    to {
        transform: rotate(360deg);
    }
}
</style>