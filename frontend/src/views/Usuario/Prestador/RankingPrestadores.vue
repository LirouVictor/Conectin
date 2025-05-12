<template>
  <div class="ranking-container">
    <div class="ranking-box">
      <!-- Header com logo e título -->
      <div class="ranking-header">
        <div class="logo-container">
        </div>
        <h1>Ranking de Prestadores</h1>
      </div>
      
      <p class="intro-text">
        Conheça os melhores prestadores da plataforma. Use os filtros para refinar por categoria e cidade.
        Clique em um prestador para ver mais detalhes!
      </p>

      <!-- Seção de Filtros -->
      <div class="filtros" v-if="categoria || cidade">
        <div class="filtro-info">
          <span class="filtro-label">Filtrando por:</span>
          <span v-if="categoria" class="filtro-valor">Categoria: {{ categoria }}</span>
          <span v-if="cidade" class="filtro-valor">Cidade: {{ cidade }}</span>
        </div>
        <button class="limpar-filtro" @click="limparFiltros">Limpar Filtros</button>
      </div>
      <p v-else class="intro-text filtro-status">Nenhum filtro aplicado. Mostrando ranking geral.</p>

      <!-- Indicador de Carregamento -->
      <div v-if="carregando" class="carregando">
        <div class="spinner"></div>
        <p>Carregando prestadores...</p>
      </div>

      <!-- Mensagem de Sem Resultados -->
      <div v-else-if="prestadoresFiltrados.length === 0" class="sem-resultados">
        <div class="empty-state-icon">🔍</div>
        <p>Nenhum prestador encontrado com os filtros selecionados.</p>
        <!-- <button v-if="categoria || cidade" class="limpar-filtro" @click="limparFiltros">Limpar Filtros</button> -->
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
                {{ prestador.categorias.map(c => c.nome).join(', ') }}
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
          <router-link :to="{ name: 'PerfilPrestador', params: { id: prestador.id }}" class="ver-perfil-btn">
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
      prestadores: [], // Lista original da API, agora com dados mais completos
      categoria: '',   // Filtro de categoria (string)
      cidade: '',      // Filtro de cidade (string)
      carregando: true, 
    };
  },
  setup() {
    const toast = useToast();
    return { toast };
  },
  computed: {
    prestadoresFiltrados() {
      if (!this.categoria && !this.cidade) {
        return this.prestadores;
      }

      return this.prestadores.filter(prestador => {
        const matchCategoria = !this.categoria ||
          (prestador.categorias && prestador.categorias.some(
            c => c.nome.toLowerCase() === this.categoria.toLowerCase()
          ));

        // AJUSTADO: Lógica para filtrar pela lista de cidades do prestador
        const matchCidade = !this.cidade ||
          (prestador._listaDeCidadesDoPrestador && prestador._listaDeCidadesDoPrestador.some(
            cidadeDoPrestadorObj => cidadeDoPrestadorObj.nome.toLowerCase() === this.cidade.toLowerCase()
          ));

        return matchCategoria && matchCidade;
      });
    }
  },
  created() {
    this.atualizarFiltrosDaRota();
    this.fetchPrestadores(); // Este método foi significativamente alterado
  },
  watch: {
    '$route.query': {
      handler() {
        this.atualizarFiltrosDaRota();
        // AJUSTADO: Se os filtros da rota mudam, é bom recarregar os prestadores
        // caso o backend já os filtre. No seu caso atual, a filtragem é no frontend,
        // então apenas atualizar os filtros `this.categoria` e `this.cidade`
        // já dispara a recomputação de `prestadoresFiltrados`.
        // Se no futuro você passar os filtros para o backend em `fetchPrestadores`,
        // esta linha abaixo seria necessária:
        // this.fetchPrestadores();
      },
      deep: true
    }
  },
  methods: {
    atualizarFiltrosDaRota() {
        this.categoria = this.$route.query.categoria || localStorage.getItem('selectedCategory') || '';
        this.cidade = this.$route.query.cidade || localStorage.getItem('selectedCity') || '';
    },

    // AJUSTADO: Método fetchPrestadores completamente reescrito
    async fetchPrestadores() {
      this.carregando = true;
      try {
        // O endpoint /prestadores/ranking já retorna PrestadorDto que inclui
        // prestador.categorias (List<Categoria>) e prestador.cidades (List<Cidade>)
        // graças ao convertToDto no backend.
        const response = await api.get('/prestadores/ranking');

        // Verificação se response.data existe
        if (!response.data) {
          this.toast.error('Ranking de prestadores não retornou dados.');
          this.prestadores = [];
          this.carregando = false; // Finaliza o carregamento aqui também
          return; 
        }

        // Mapeia os dados do DTO para o formato que o template e os filtros esperam.
        this.prestadores = response.data.map(prestadorDto => {
          let cidadeDisplay = 'Não informada';
          let cidadesDoPrestadorParaFiltro = []; // Lista de objetos Cidade {id, nome}

          // O DTO do backend envia `prestadorDto.cidades` como uma lista de objetos Cidade.
          if (prestadorDto.cidades && prestadorDto.cidades.length > 0) {
            // Para exibição no card, podemos mostrar a primeira cidade ou concatenar.
            // Aqui estou concatenando, mas você pode escolher a primeira: prestadorDto.cidades[0].nome
            cidadeDisplay = prestadorDto.cidades.map(c => c.nome).join(', '); 

            // Para o filtro `matchCidade` em `prestadoresFiltrados`, usamos a lista completa.
            cidadesDoPrestadorParaFiltro = prestadorDto.cidades;
          }

          return {
            ...prestadorDto, // Inclui id, nome (do usuario), descricao, disponibilidade, avaliacaoMedia, etc.
            categorias: prestadorDto.categorias || [], // Garante que 'categorias' seja sempre uma lista
            cidade: cidadeDisplay,                     // String para exibição em {{ prestador.cidade }}
            // NOVO: Propriedade auxiliar para o filtro `matchCidade` usar a lista de cidades.
            // O _ no início é uma convenção para indicar uma propriedade "privada" ou auxiliar.
            _listaDeCidadesDoPrestador: cidadesDoPrestadorParaFiltro
          };
        });

      } catch (error) {
        console.error("Erro detalhado em fetchPrestadores:", error.response || error);
        const errorMessage = error.response?.data?.message || error.message || 'Erro ao carregar prestadores.';
        this.toast.error(errorMessage);
        this.prestadores = []; // Limpa em caso de erro para evitar mostrar dados antigos
      } finally {
        this.carregando = false; // Finaliza carregamento
      }
    },

    limparFiltros() {
      localStorage.removeItem('selectedCategory');
      localStorage.removeItem('selectedCity');
      this.categoria = '';
      this.cidade = '';

      // Navegar para a mesma página sem parâmetros de query
      // O watcher $route.query vai pegar essa mudança e chamar atualizarFiltrosDaRota.
      if (Object.keys(this.$route.query).length > 0) {
        this.$router.push({ path: this.$route.path });
      }
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
  max-width: 900px;
  margin: 20px auto;
  padding: 0 15px;
  font-family: 'Segoe UI', Tahoma, Geneva, Verdana, sans-serif;
}

.ranking-box {
  padding: 30px;
  background-color: var(--conectin-white);
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

/* Logo estilizada */
.logo-container {
  position: relative;
  width: 100%;
  display: flex;
  justify-content: center;
  margin-bottom: 15px;
}

.conectin-logo {
  font-size: 2.5rem;
  font-weight: bold;
  color: var(--conectin-blue);
  text-shadow: 1px 1px 2px rgba(0,0,0,0.1);
  position: relative;
  z-index: 2;
}

.conectin-nodes {
  position: absolute;
  width: 100%;
  height: 100%;
  top: 0;
  left: 0;
  z-index: 1;
}

.node {
  position: absolute;
  width: 10px;
  height: 10px;
  background-color: var(--conectin-blue-light);
  border-radius: 50%;
  border: 2px solid white;
}

.n1 { top: 15%; left: 35%; }
.n2 { top: 45%; left: 25%; }
.n3 { top: 65%; left: 40%; }
.n4 { top: 25%; left: 65%; }
.n5 { top: 55%; left: 70%; }

.user-node {
  width: 16px;
  height: 16px;
  background-color: var(--conectin-yellow);
  border: 3px solid white;
  box-shadow: 0 0 5px rgba(0,0,0,0.2);
  top: 10%; 
  left: 50%;
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
  margin-bottom: 30px;
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
  margin-bottom: 20px;
}

/* Estilos dos Filtros */
.filtros {
  display: flex;
  flex-wrap: wrap;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 30px;
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

.limpar-filtro {
  background-color: #257BB8;
  color: white;
  border: none;
  padding: 8px 18px;
  border-radius: 6px;
  cursor: pointer;
  transition: all 0.3s ease;
  font-weight: 500;
  box-shadow: 0 2px 5px rgba(231, 76, 60, 0.2);
}

.limpar-filtro:hover {
  background-color: #c0392b;
  transform: translateY(-2px);
  box-shadow: 0 4px 8px rgba(231, 76, 60, 0.3);
}

.limpar-filtro:active {
  transform: translateY(0);
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
  0% { transform: rotate(0deg); }
  100% { transform: rotate(360deg); }
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

/* Estilos para os detalhes do prestador */
.categorias, .cidade, .avaliacao, .disponibilidade {
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

/* Estilo do botão Ver Perfil */
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
  /* box-shadow: 0 3px 6px #257BB8; */
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
  background: linear-gradient(90deg, transparent, rgba(255,255,255,0.2), transparent);
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
    align-items: flex-start;
  }
  
  .limpar-filtro {
    width: 100%;
  }
}
</style>