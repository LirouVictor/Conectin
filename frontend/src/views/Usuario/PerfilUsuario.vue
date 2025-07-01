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
/*
  --- PALETA DE CORES REVISADA - CONECTIN ---
  Azul Estrutural: #1E7AC5 (azul forte e profissional da logo)
  Amarelo Ação/Destaque: #F8B617 (amarelo-ouro vibrante da logo)
  Fundo Principal: #FFFFFF (branco)
  Fundo Secundário: #F7F9FC (um cinza ainda mais claro e neutro)
  Texto Principal: #2c3e50
  Texto Secundário: #5A6A7B
  Bordas: #E0E6ED
*/


.perfil-container {
    background-color: #f7f9fc;
    display: flex;
    justify-content: center;
    align-items: flex-start;
    max-width: 100%;
    min-height: calc(100vh - 80px);
    padding: 40px 20px;
    font-family: 'Roboto', 'Arial', sans-serif;
}

.perfil-box {
    width: 60%;
    padding: 30px;
    background-color: #FFFFFF;
    border-radius: 12px;
    box-shadow: 0 4px 20px rgba(26, 119, 181, 0.15);
    position: relative;
    overflow: hidden;
}


.usuario-header {
    display: flex;
    align-items: center;
    gap: 25px;
    margin-bottom: 30px;
    padding-bottom: 30px;
}

.usuario-foto-grande {
    width: 110px;
    height: 110px;
    border-radius: 50%;
    object-fit: cover;
    border: 4px solid #1E7AC5;
    /* Azul como cor de base do perfil */
}

.usuario-info h1 {
    margin: 0 0 8px 0;
    color: #2c3e50;
    font-size: 28px;
    font-weight: 700;
}

.link-prestador {
    display: inline-block;
    padding: 9px 18px;
    background-color: #F8B617;
    /* **MUDANÇA:** Amarelo como ação principal */
    color: #FFFFFF;
    /* Texto branco para melhor contraste no amarelo */
    border-radius: 8px;
    text-decoration: none;
    font-weight: bold;
    transition: all 0.3s ease;
}

.link-prestador:hover {
    background-color: #e0a800;
    /* Amarelo mais escuro no hover */
    transform: translateY(-2px);
    box-shadow: 0 4px 10px rgba(0, 0, 0, 0.15);
}

.avaliacoes-section {
    margin-top: 30px;
}

.avaliacoes-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-bottom: 25px;
}

.avaliacoes-header h3 {
    color: #1E7AC5;
    /* Título da seção permanece azul para estrutura */
    font-size: 22px;
    margin: 0;
    font-weight: 700;
}

.filtro-ordenacao label {
    margin-right: 8px;
    font-size: 14px;
    color: #555;
}

.filtro-ordenacao select {
    padding: 6px 10px;
    border: 1px solid var(--conectin-gray);
    border-radius: 6px;
    background-color: var(--conectin-white);
    font-size: 14px;
}

.avaliacao-card {
    border-radius: 12px;
    padding: 20px;
    margin-bottom: 15px;
    /* border-left: 4px solid #1E7AC5;
    border-bottom: 3px solid #1E7AC5; */
    transition: box-shadow 0.3s ease, transform 0.3s ease;
}

.avaliacao-card:hover {
    transform: translateY(-3px);
    box-shadow: 0 8px 20px rgba(26, 119, 181, 0.15);
    border-color: var(--conectin-blue-light);
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
}

.avaliador-detalhes .avaliador-nome {
    font-weight: bold;
    color: #2c3e50;
}

.avaliacao-data {
    font-size: 13px;
    color: #5A6A7B;
}

.nota-estrelas {
    margin-bottom: 10px;
    color: #F8B617;
    /* Estrelas mantêm o amarelo */
    font-size: 20px;
}

.nota-estrelas .estrela {
    color: #E0E6ED;
}

.nota-estrelas .estrela.preenchida {
    color: #F8B617;
}

.comentario-texto {
    margin: 0;
    line-height: 1.6;
    color: #34495e;
}

.loading-spinner,
.loading-spinner-small {
    border: 4px solid rgba(30, 122, 197, 0.2);
    /* Fundo do spinner azul claro */
    border-radius: 50%;
    border-top-color: #F8B617;
    /* **MUDANÇA:** Spinner agora é amarelo */
    animation: spin 1s linear infinite;
    display: inline-block;
    vertical-align: middle;
}

.loading-spinner {
    width: 50px;
    height: 50px;
    margin: 0 auto 20px;
}

.loading-spinner-small {
    width: 25px;
    height: 25px;
    margin-right: 10px;
}

/* Demais estilos (sem-avaliacoes, loading, error-message) podem ser mantidos ou ajustados conforme acima */
.sem-avaliacoes,
.loading-avaliacoes,
.loading,
.error-message {
    text-align: center;
    padding: 40px;
    color: #5A6A7B;
}

@keyframes spin {
    to {
        transform: rotate(360deg);
    }
}
</style>