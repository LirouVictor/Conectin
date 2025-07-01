<template>
  <div class="ranking-container">
    <div class="ranking-box">
      <!-- Header com logo e título -->
      <div class="ranking-header">
        <div class="logo-container">
          <!-- Se você tiver um logo SVG ou imagem, coloque aqui -->
          <!-- <span class="conectin-logo">Conectin</span> -->
        </div>
        <h1>Ranking de Prestadores</h1>
      </div>

      <p class="intro-text">
        Conheça os melhores prestadores da plataforma.
        <span v-if="!categoria && !cidade && !searchTerm">
          Use a busca abaixo para encontrar um prestador específico ou selecione uma categoria e cidade na página
          inicial para refinar.
        </span>
        <span v-else>
          Use a busca abaixo para refinar ainda mais os resultados.
        </span>
        Clique em um prestador para ver mais detalhes!
      </p>

      <!-- Seção de Filtros e Busca -->
      <div class="filtros-e-busca-container">
        <!-- Filtros de Categoria e Cidade (existentes) -->
        <div class="filtros" v-if="categoria || cidade">
          <div class="filtro-info">
            <span class="filtro-label">Filtrando por:</span>
            <span v-if="categoria" class="filtro-valor">Categoria: {{ categoria }}</span>
            <span v-if="cidade" class="filtro-valor">Cidade: {{ cidade }}</span>
          </div>
        </div>
        <button v-if="categoria || cidade || searchTerm" class="limpar-filtro-geral" @click="limparFiltros">
          Limpar Filtros e Busca
        </button>
        <p v-else-if="!searchTerm && !categoria && !cidade" class="intro-text filtro-status">
          Nenhum filtro ou busca ativa. Mostrando ranking geral.
        </p>

        <!-- Barra de Busca Local -->
        <div class="busca-local-container">
          <input type="text" v-model="searchTerm" placeholder="Buscar por nome, categoria ou cidade nos resultados..."
            class="busca-input" />
          <button v-if="searchTerm" @click="limparBusca" class="limpar-busca-btn icon-btn" title="Limpar busca">
            ×
          </button>
        </div>
      </div>

      <!-- Indicador de Carregamento -->
      <div v-if="carregando" class="carregando">
        <div class="spinner"></div>
        <p>Carregando prestadores...</p>
      </div>

      <!-- Mensagem de Sem Resultados -->
      <div v-else-if="prestadoresFiltrados.length === 0" class="sem-resultados">
        <div class="empty-state-icon">🔍</div>
        <p v-if="categoria || cidade || searchTerm">
          Nenhum prestador encontrado com os filtros e/ou busca aplicados.
        </p>
        <p v-else>
          Ainda não há prestadores cadastrados no ranking.
        </p>
      </div>

      <!-- Container de Prestadores -->
      <div v-else class="prestadores-container">
        <div class="prestador-card" v-for="(prestador, index) in prestadoresFiltrados" :key="prestador.id">
          <div class="prestador-ranking-badge">{{ index + 1 }}</div>
          <div class="prestador-info">
            <h3>{{ prestador.nome }}</h3>
            <div class="prestador-details">
              <p class="categorias" v-if="prestador.categorias && prestador.categorias.length">
                <span class="detail-icon">📋</span>
                <span class="detail-label">Categorias:</span>
                {{prestador.categorias.map(c => c.nome).join(', ')}}
              </p>
              <p class="cidade">
                <span class="detail-icon">📍</span>
                <span class="detail-label">Cidade:</span>
                {{ prestador.cidade || 'Não informada' }}
              </p>
              <p class="avaliacao">
                <span class="detail-icon">⭐</span>
                <span class="detail-label">Avaliação:</span>
                <span class="rating-value">
                  {{ prestador.avaliacaoMedia ? prestador.avaliacaoMedia.toFixed(1) : 'Sem avaliações' }}
                </span>
              </p>
              <p class="disponibilidade">
                <span class="detail-icon">🕒</span>
                <span class="detail-label">Disponibilidade:</span>
                {{ prestador.disponibilidade || 'Não informado' }}
              </p>
            </div>
          </div>
          <router-link :to="{ name: 'PerfilPrestador', params: { id: prestador.id } }" class="ver-perfil-btn">
            Ver Perfil
          </router-link>
        </div>
      </div>
    </div>
  </div>
</template>


<script>
import api from '@/services/api';
import { useToast } from 'vue-toastification';

export default {
  name: 'RankingPrestadores',
  data() {
    return {
      prestadores: [],
      categoria: '',
      cidade: '',
      searchTerm: '',
      carregando: true,
    };
  },
  setup() {
    const toast = useToast();
    return { toast };
  },
  computed: {
    prestadoresFiltrados() {
      let filtered = this.prestadores;

      // 1. Filtro por categoria e cidade (lógica existente)
      if (this.categoria || this.cidade) {
        filtered = filtered.filter(prestador => {
          const matchCategoria = !this.categoria ||
            (prestador.categorias && prestador.categorias.some(
              c => c.nome.toLowerCase() === this.categoria.toLowerCase()
            ));

          const matchCidade = !this.cidade ||
            (prestador._listaDeCidadesDoPrestador && prestador._listaDeCidadesDoPrestador.some(
              cidadeDoPrestadorObj => cidadeDoPrestadorObj.nome.toLowerCase() === this.cidade.toLowerCase()
            ));
          return matchCategoria && matchCidade;
        });
      }

      // 2. Filtro por termo de busca (aplicado sobre os resultados já filtrados por categoria/cidade)
      if (this.searchTerm.trim() !== '') {
        const lowerSearchTerm = this.searchTerm.toLowerCase().trim();
        filtered = filtered.filter(prestador => {
          const matchNome = prestador.nome && prestador.nome.toLowerCase().includes(lowerSearchTerm);
          const matchCategoriasPrestador = prestador.categorias && prestador.categorias.some(
            cat => cat.nome && cat.nome.toLowerCase().includes(lowerSearchTerm)
          );
          // NOVO: Adiciona busca pela string de cidades do prestador
          const matchCidadeDisplay = prestador.cidade && prestador.cidade.toLowerCase().includes(lowerSearchTerm);

          return matchNome || matchCategoriasPrestador || matchCidadeDisplay;
        });
      }

      return filtered;
    }
  },
  created() {
    this.atualizarFiltrosDaRota();
    this.fetchPrestadores();
  },
  watch: {
    '$route.query': {
      handler() {
        this.atualizarFiltrosDaRota();
      },
      deep: true
    }
  },
  methods: {
    atualizarFiltrosDaRota() {
      this.categoria = this.$route.query.categoria || localStorage.getItem('selectedCategory') || '';
      this.cidade = this.$route.query.cidade || localStorage.getItem('selectedCity') || '';
    },

    async fetchPrestadores() {
      this.carregando = true;
      try {
        const response = await api.get('/prestadores/ranking');
        if (!response.data) {
          this.toast.error('Ranking de prestadores não retornou dados.');
          this.prestadores = [];
          this.carregando = false;
          return;
        }

        this.prestadores = response.data.map(prestadorDto => {
          let cidadeDisplay = 'Não informada';
          let cidadesDoPrestadorParaFiltro = [];

          if (prestadorDto.cidades && prestadorDto.cidades.length > 0) {
            cidadeDisplay = prestadorDto.cidades.map(c => c.nome).join(', ');
            cidadesDoPrestadorParaFiltro = prestadorDto.cidades;
          }

          return {
            ...prestadorDto,
            categorias: prestadorDto.categorias || [],
            cidade: cidadeDisplay, // Esta é a string usada para busca e exibição
            _listaDeCidadesDoPrestador: cidadesDoPrestadorParaFiltro
          };
        });

      } catch (error) {
        console.error("Erro detalhado em fetchPrestadores:", error.response || error);
        const errorMessage = error.response?.data?.message || error.message || 'Erro ao carregar prestadores.';
        this.toast.error(errorMessage);
        this.prestadores = [];
      } finally {
        this.carregando = false;
      }
    },

    limparFiltros() {
      localStorage.removeItem('selectedCategory');
      localStorage.removeItem('selectedCity');
      this.categoria = '';
      this.cidade = '';
      this.searchTerm = '';

      if (Object.keys(this.$route.query).length > 0) {
        this.$router.push({ path: this.$route.path });
      }
    },

    limparBusca() {
      this.searchTerm = '';
    }
  }
};
</script>

<style scoped>
/* Variáveis de cores baseadas na logo Conectin */
:root {
  --conectin-blue: #1A77B5;
  --conectin-blue-light: #2089cf;
  --conectin-blue-dark: #155e8f;
  --conectin-yellow: #F4B400;
  --conectin-yellow-dark: #e1a300;
  --conectin-text: #333333;
  --conectin-white: #FFFFFF;
  --conectin-gray-light: #f5f7fa;
  --conectin-gray: #e0e0e0;
  --conectin-red: #e74c3c;
}

/* Estilos globais */
.ranking-container {
  background-color: #f7f9fc;
  display: flex;
  justify-content: center;
  align-items: flex-start;
  max-width: 100%;
  min-height: calc(100vh - 80px);
  padding: 40px 20px;
  font-family: 'Roboto', 'Arial', sans-serif;
}

.ranking-box {
  width: 60%;
  padding: 30px;
  background-color: #FFFFFF;
  border-radius: 12px;
  box-shadow: 0 4px 20px rgba(26, 119, 181, 0.15);
  position: relative;
  overflow: hidden;
}

/* Estilos do cabeçalho */
.ranking-header {
  display: flex;
  flex-direction: column;
  align-items: center;
  margin-bottom: 25px;
  position: relative;
}

.logo-container {
  margin-bottom: 15px;
}

h1 {
  font-size: 2.2rem;
  color: var(--conectin-blue);
  margin: 10px 0;
  text-align: center;
  font-weight: 600;
  position: relative;
}

h1:after {
  content: '';
  position: absolute;
  bottom: -10px;
  left: 50%;
  transform: translateX(-50%);
  width: 80px;
  height: 4px;
  background-color: var(--conectin-yellow);
  border-radius: 2px;
}

.intro-text {
  text-align: center;
  margin-bottom: 20px;
  color: var(--conectin-text);
  line-height: 1.6;
  font-size: 1.05rem;
  max-width: 700px;
  margin-left: auto;
  margin-right: auto;
}

.filtro-status {
  color: #666;
  font-style: italic;
  margin-bottom: 15px;
  text-align: center;
  /* Adicionado para centralizar */
}

/* Container para filtros e busca */
.filtros-e-busca-container {
  margin-bottom: 30px;
}

/* Estilos dos Filtros de Categoria/Cidade */
.filtros {
  display: flex;
  flex-wrap: wrap;
  justify-content: center;
  /* Centraliza os filtros de categoria/cidade */
  align-items: center;
  margin-bottom: 15px;
  padding: 15px 20px;
  background-color: rgba(26, 119, 181, 0.05);
  border-radius: 10px;
  border-left: 4px solid var(--conectin-blue);
  gap: 15px;
}

.filtro-info {
  display: flex;
  flex-wrap: wrap;
  gap: 12px;
  align-items: center;
}

.filtro-label {
  font-weight: bold;
  color: var(--conectin-blue);
}

.filtro-valor {
  background-color: var(--conectin-white);
  padding: 6px 14px;
  border-radius: 20px;
  font-size: 0.9rem;
  border: 1px solid var(--conectin-blue-light);
  color: var(--conectin-blue);
  box-shadow: 0 1px 3px rgba(26, 119, 181, 0.15);
}

.limpar-filtro-geral {
  background-color: #e74c3c;
  color: white;
  border: none;
  padding: 10px 20px;
  border-radius: 6px;
  cursor: pointer;
  transition: all 0.3s ease;
  font-weight: 500;
  box-shadow: 0 2px 5px rgba(231, 76, 60, 0.2);
  width: 100%;
  max-width: 300px;
  /* Limita a largura máxima */
  display: block;
  /* Para que margin auto funcione */
  margin: 15px auto 20px auto;
  /* Centraliza e adiciona margens */
}

.limpar-filtro-geral:hover {
  background-color: #c0392b;
  transform: translateY(-2px);
  box-shadow: 0 4px 8px rgba(231, 76, 60, 0.3);
}

.limpar-filtro-geral:active {
  transform: translateY(0);
}

.busca-local-container {
  display: flex;
  gap: 10px;
  align-items: center;
}

.busca-input {
  flex-grow: 1;
  padding: 10px 15px;
  font-size: 1rem;
  border: 0.5px solid #1A77B5;
  box-shadow: #1A77B5;
  border-radius: 6px;
  transition: border-color 0.3s ease, box-shadow 0.3s ease;
}

.busca-input:focus {
  outline: none;
  border-color: var(--conectin-blue);
  box-shadow: 0 0 0 3px rgba(26, 119, 181, 0.15);
}

.limpar-busca-btn.icon-btn {
  background-color: transparent;
  color: #888;
  border: none;
  padding: 0;
  width: 38px;
  height: 38px;
  font-size: 1.8rem;
  line-height: 38px;
  text-align: center;
  border-radius: 50%;
  cursor: pointer;
  transition: background-color 0.2s ease, color 0.2s ease;
}

.limpar-busca-btn.icon-btn:hover {
  background-color: #f0f0f0;
  color: #333;
}


/* Estilos de Carregamento */
.carregando {
  text-align: center;
  padding: 50px 20px;
  color: var(--conectin-blue);
  font-size: 1.1rem;
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 15px;
}

.spinner {
  width: 40px;
  height: 40px;
  border: 4px solid rgba(26, 119, 181, 0.2);
  border-top: 4px solid var(--conectin-blue);
  border-radius: 50%;
  animation: spin 1s linear infinite;
}

@keyframes spin {
  0% {
    transform: rotate(0deg);
  }

  100% {
    transform: rotate(360deg);
  }
}

/* Sem resultados */
.sem-resultados {
  text-align: center;
  padding: 40px 20px;
  color: #666;
  font-size: 1.1rem;
  display: flex;
  flex-direction: column;
  align-items: center;
}

.empty-state-icon {
  font-size: 3rem;
  margin-bottom: 15px;
  opacity: 0.7;
}

.sem-resultados p {
  margin-bottom: 20px;
}

/* Estilos dos Cards de Prestadores */
.prestadores-container {
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.prestador-card {
  border: 1px solid var(--conectin-gray);
  border-radius: 10px;
  padding: 20px;
  display: flex;
  flex-wrap: wrap;
  justify-content: space-between;
  align-items: center;
  background-color: var(--conectin-white);
  transition: all 0.3s ease;
  gap: 15px;
  position: relative;
  overflow: hidden;
}

.prestador-card:hover {
  transform: translateY(-3px);
  box-shadow: 0 8px 20px rgba(26, 119, 181, 0.15);
  border-color: var(--conectin-blue-light);
}

.prestador-ranking-badge {
  position: absolute;
  top: 0;
  left: 0;
  background-color: var(--conectin-blue);
  color: white;
  width: 30px;
  height: 30px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-weight: bold;
  border-bottom-right-radius: 10px;
}

.prestador-info {
  flex: 1;
  min-width: 250px;
  padding-left: 10px;
}

.prestador-info h3 {
  margin: 0 0 15px 0;
  color: var(--conectin-blue);
  font-size: 1.3rem;
  border-bottom: 2px solid var(--conectin-yellow);
  padding-bottom: 8px;
  display: inline-block;
}

.prestador-details {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.categorias,
.cidade,
.avaliacao,
.disponibilidade {
  margin: 0;
  font-size: 0.95rem;
  color: var(--conectin-text);
  line-height: 1.5;
  display: flex;
  align-items: center;
  gap: 5px;
}

.detail-icon {
  font-size: 1.1rem;
  min-width: 20px;
  text-align: center;
}

.detail-label {
  font-weight: 600;
  color: #555;
  margin-right: 5px;
}

.rating-value {
  color: var(--conectin-blue);
  font-weight: 600;
}

.ver-perfil-btn {
  background-color: #257BB8;
  color: #FFFFFF;
  text-decoration: none;
  padding: 10px 24px;
  border-radius: 6px;
  transition: all 0.3s ease;
  font-weight: bold;
  text-align: center;
  white-space: nowrap;
  position: relative;
  overflow: hidden;
}

.ver-perfil-btn:before {
  content: '';
  position: absolute;
  top: 0;
  left: -100%;
  width: 100%;
  height: 100%;
  background: linear-gradient(90deg, transparent, rgba(255, 255, 255, 0.2), transparent);
  transition: left 0.7s;
}

.ver-perfil-btn:hover {
  background-color: #F4B400;
  color: #fff;
  text-decoration: none;
  transform: translateY(-2px);
  box-shadow: 0 5px 10px rgba(244, 180, 0, 0.4);
}

.ver-perfil-btn:hover:before {
  left: 100%;
}

.ver-perfil-btn:active {
  transform: translateY(0);
}

/* Media queries para responsividade */
@media (max-width: 768px) {
  .ranking-box {
    padding: 20px 15px;
  }

  .prestador-card {
    flex-direction: column;
    align-items: flex-start;
  }

  .ver-perfil-btn {
    width: 100%;
    margin-top: 10px;
  }

  .filtros {
    flex-direction: column;
    align-items: center;
    /* Centraliza os filtros em mobile */
  }

  .limpar-filtro-geral {
    width: 90%;
    /* Ajusta a largura em mobile */
    max-width: none;
    /* Remove a largura máxima para mobile */
  }
}
</style>