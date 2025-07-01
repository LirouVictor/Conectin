<template>
  <div class="perfil-container">
    <div class="perfil-box">
      <h1>Perfil do Prestador</h1>
      <div v-if="prestador" class="perfil-content">
        <div class="perfil-header">
          <!-- Foto do Perfil -->
          <img :src="getFotoUrl(prestador.fotoPerfil)" alt="Foto de perfil" class="perfil-foto">
          <h2>{{ prestador.nome }}</h2>
          <p class="avaliacao">
            Avaliação: {{ prestador.avaliacaoMedia ? prestador.avaliacaoMedia.toFixed(1) : 'Sem avaliações' }} ⭐
          </p>
        </div>
        <div class="perfil-info">
          <p class="descricao"><strong>Descrição:</strong> {{ prestador.descricao || 'Não informado' }}</p>
          <p class="disponibilidade">
            <strong>Disponibilidade:</strong> {{ prestador.disponibilidade || 'Não informado' }}
          </p>
          <p class="categorias" v-if="prestador.categorias && prestador.categorias.length">
            <strong>Categorias:</strong> {{prestador.categorias.map(c => c.nome).join(', ')}}
          </p>
          <p class="cidades" v-if="prestador.cidades && prestador.cidades.length">
            <strong>Cidades Atendidas:</strong> {{prestador.cidades.map(c => c.nome).join(', ')}}
          </p>
        </div>

        <div class="contact-section">
          <!-- O botão continua o mesmo, a mágica acontece no método 'handleContact' -->
          <button @click="handleContact" class="contact-btn">
            <i class="bi bi-whatsapp"></i>
            <span>Entrar em Contato via WhatsApp</span>
          </button>
        </div>

        <h3>Portfólio</h3>
        <div class="portfolio-section" v-if="prestador.portfolios && prestador.portfolios.length">
          <div class="portfolio-item-container" v-for="item in prestador.portfolios" :key="item.id">
            <!-- <h4 class="portfolio-item-title">{{ item.titulo }}</h4>
            <p class="portfolio-item-description">{{ item.descricao }}</p> -->

            <div class="portfolio-fotos-horizontal">
              <div v-for="fotoUrl in item.fotos" :key="fotoUrl" class="thumbnail-wrapper"
                @click="abrirGaleria(item.fotos, item.titulo)">
                <img :src="getAbsoluteUrl(fotoUrl)" :alt="item.titulo" class="portfolio-thumbnail" />
              </div>
            </div>
          </div>
        </div>
        <div class="portfolio-section" v-else>
          <p>Nenhum item no portfólio.</p>
        </div>

        <!-- ======================= NOVA SEÇÃO DE AVALIAÇÕES ====================== -->
        <div class="avaliacoes-section">
          <div class="avaliacoes-header">
            <h3>Comentários e Avaliações</h3>
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
            <div v-for="avaliacao in avaliacoesOrdenadas" :key="avaliacao.data" class="avaliacao-card">
              <router-link :to="{ name: 'PerfilUsuario', params: { id: avaliacao.avaliadorId } }"
                class="avaliador-info-link">
                <div class="avaliador-info">
                  <img :src="getFotoUrl(avaliacao.fotoAvaliador)" alt="Foto do Avaliador" class="avaliador-foto" />
                  <div class="avaliador-detalhes">
                    <span class="avaliador-nome">{{ avaliacao.nomeAvaliador }}</span>
                    <span class="avaliacao-data">{{ formatarData(avaliacao.data) }}</span>
                  </div>
                </div>
              </router-link>
              <div class="avaliacao-conteudo">
                <div class="nota-estrelas">
                  <span v-for="n in 5" :key="n" class="estrela" :class="{ 'preenchida': n <= avaliacao.nota }">★</span>
                </div>
                <p class="comentario-texto">{{ avaliacao.comentario || 'Nenhum comentário deixado.' }}</p>
              </div>
            </div>
          </div>

          <div v-else class="sem-avaliacoes">
            <p>Este prestador ainda não possui avaliações.</p>
          </div>
        </div>
        <!-- ===================== FIM DA NOVA SEÇÃO DE AVALIAÇÕES =================== -->

      </div>
      <div v-else class="loading">
        <div class="loading-spinner"></div>
        <p>Carregando perfil...</p>
      </div>
    </div>

    <div v-if="galeriaVisivel" class="galeria-overlay" @click="fecharGaleria">
      <div class="galeria-content" @click.stop>
        <button class="galeria-close-btn" @click="fecharGaleria">×</button>
        <h3 class="galeria-title">{{ galeriaTitulo }}</h3>
        <div class="galeria-grid">
          <div v-for="fotoUrl in fotosDaGaleria" :key="fotoUrl" class="galeria-image-wrapper">
            <img :src="getAbsoluteUrl(fotoUrl)" :alt="galeriaTitulo" class="galeria-image" />
          </div>
        </div>
      </div>
    </div>

  </div>
</template>

<script>
import api from '@/services/api';
import { useToast } from 'vue-toastification';
import { useUserStore } from '@/stores/user';
import { useRouter } from 'vue-router';
import 'bootstrap-icons/font/bootstrap-icons.css';

export default {
  name: 'PerfilPrestador',
  data() {
    return {
      prestador: null,
      avaliacoes: [],
      loadingAvaliacoes: true,
      ordemFiltro: 'maior', // 'maior', 'menor', ou 'recente'
      backendUrl: process.env.VUE_APP_API_BASE_URL || 'http://localhost:8080',
      galeriaVisivel: false,
      fotosDaGaleria: [],
      galeriaTitulo: '',
    };
  },
  setup() {
    const toast = useToast();
    const userStore = useUserStore();
    const router = useRouter();
    return { toast, userStore, router };
  },

  computed: {
    avaliacoesOrdenadas() {
      // Cria uma cópia do array para não modificar o original
      const sorted = [...this.avaliacoes];

      if (this.ordemFiltro === 'maior') {
        return sorted.sort((a, b) => b.nota - a.nota);
      }
      if (this.ordemFiltro === 'menor') {
        return sorted.sort((a, b) => a.nota - b.nota);
      }
      if (this.ordemFiltro === 'recente') {
        // Compara as datas, convertendo para objetos Date
        return sorted.sort((a, b) => new Date(b.data) - new Date(a.data));
      }
      return this.avaliacoes;
    },
  },

  mounted() {
    this.fetchPrestador();
    this.fetchAvaliacoes();
  },
  methods: {
    getAbsoluteUrl(relativeUrl) {
      if (!relativeUrl || relativeUrl.startsWith('http')) {
        return relativeUrl;
      }
      return `${this.backendUrl}${relativeUrl}`;
    },

    async fetchPrestador() {
      try {
        const id = this.$route.params.id;
        const response = await api.get(`/prestadores/${id}`);
        this.prestador = response.data;
      } catch (error) {
        if (error.response && error.response.data) {
          this.toast.error(error.response.data.message || 'Erro ao carregar dados do prestador.');
        } else {
          this.toast.error('Erro ao carregar perfil do prestador.');
        }
        console.error("Erro ao buscar prestador:", error);
      }
    },

    async fetchAvaliacoes() {
      this.loadingAvaliacoes = true;
      try {
        const prestadorId = this.$route.params.id;
        const response = await api.get(`/avaliacoes/prestador/${prestadorId}`);
        this.avaliacoes = response.data;
      } catch (error) {
        this.toast.error('Não foi possível carregar as avaliações.');
        console.error("Erro ao buscar avaliações:", error);
      } finally {
        this.loadingAvaliacoes = false;
      }
    },

    abrirGaleria(fotos, titulo) {
      this.fotosDaGaleria = fotos;
      this.galeriaTitulo = titulo;
      this.galeriaVisivel = true;
      document.body.style.overflow = 'hidden'; // Impede o scroll da página de fundo
    },
    fecharGaleria() {
      this.galeriaVisivel = false;
      this.fotosDaGaleria = [];
      this.galeriaTitulo = '';
      document.body.style.overflow = ''; // Restaura o scroll da página
    },

    // ===== MÉTODO ADICIONADO PARA FOTO DE PERFIL (CONSISTÊNCIA) =====
    getFotoUrl(fotoPath) {
      if (!fotoPath) {
        return 'https://www.gravatar.com/avatar/?d=mp';
      }
      if (fotoPath.startsWith('http')) {
        return fotoPath;
      }
      return `${this.backendUrl}${fotoPath}`;
    },

    // ===== NOVO MÉTODO PARA FORMATAR A DATA =====
    formatarData(dataString) {
      if (!dataString) return '';
      const data = new Date(dataString);
      return data.toLocaleDateString('pt-BR', {
        day: '2-digit',
        month: '2-digit',
        year: 'numeric'
      });
    },

    // AQUI ESTÁ A LÓGICA ATUALIZADA
    async handleContact() {
      if (!this.prestador) {
        this.toast.error('Dados do prestador ainda não carregados. Tente novamente em instantes.');
        return;
      }

      const currentUser = this.userStore.user;
      if (!currentUser) {
        this.toast.info('Você precisa estar logado para entrar em contato. Redirecionando para login...');
        this.router.push({ name: 'Login' });
        return;
      }

      if (!currentUser.cliente) {
        this.toast.error('Apenas usuários do tipo CLIENTE podem solicitar serviços.');
        return;
      }

      // ---- NOVO BLOCO DE LÓGICA PARA SELEÇÃO DE CATEGORIA ----
      if (!this.prestador.categorias || this.prestador.categorias.length === 0) {
        this.toast.error("Este prestador não possui categorias de serviço definidas.");
        return;
      }

      let categoriaSelecionada;
      if (this.prestador.categorias.length === 1) {
        // Se houver apenas uma, seleciona-a automaticamente
        categoriaSelecionada = this.prestador.categorias[0];
        this.toast.info(`Iniciando contato para o serviço: ${categoriaSelecionada.nome}`);
      } else {
        // Se houver mais de uma, abre o modal para o usuário escolher
        // A action da store abre o modal e retorna uma Promise que resolve com a categoria ou null
        categoriaSelecionada = await this.userStore.solicitarSelecaoCategoria(this.prestador);
      }

      // Se o usuário fechou o modal ou não selecionou uma categoria, a Promise resolve para null
      if (!categoriaSelecionada) {
        this.toast.info('Seleção de categoria cancelada.');
        return; // Interrompe a execução
      }
      // ---- FIM DO NOVO BLOCO ----


      // ---- LÓGICA DE CRIAÇÃO DE SOLICITAÇÃO E CONTATO (AGORA USANDO A CATEGORIA SELECIONADA) ----
      const prestadorTelefone = this.prestador.telefone;

      try {
        // O DTO agora é montado com a 'categoriaSelecionada' que veio do bloco anterior
        const solicitacaoDto = {
          clienteId: parseInt(currentUser.id),
          prestadorId: parseInt(this.prestador.usuarioId),
          categoriaId: parseInt(categoriaSelecionada.id),
          detalhes: `Contato iniciado para ${this.prestador.nome} referente à categoria ${categoriaSelecionada.nome}.`
        };

        const responseSolicitacao = await api.post('/solicitacoes/criar', solicitacaoDto, {
          headers: { Authorization: `Bearer ${localStorage.getItem('token')}` }
        });

        if (responseSolicitacao.status === 201) {
          this.toast.success('Solicitação de serviço registrada com sucesso!');

          if (prestadorTelefone && prestadorTelefone.trim() !== '') {
            let numeroLimpo = prestadorTelefone.replace(/\D/g, '');
            if (numeroLimpo.length <= 11 && !numeroLimpo.startsWith('55')) {
              numeroLimpo = '55' + numeroLimpo;
            }
            const nomePrestador = this.prestador.nome || 'Prestador';
            const nomeCliente = currentUser.nome || 'um cliente'; // Pega o nome do usuário logado

            // 2. Montamos a nova mensagem personalizada
            const mensagem = encodeURIComponent(`Olá ${nomePrestador}, sou ${nomeCliente}, vi seu perfil no Conectin e gostaria de mais informações sobre seus serviços de "${categoriaSelecionada.nome}"`);

            // A linha abaixo permanece a mesma
            const whatsappUrl = `https://wa.me/${numeroLimpo}?text=${mensagem}`;
            window.open(whatsappUrl, '_blank');
          } else {
            this.toast.info("A solicitação foi registrada. O prestador não informou WhatsApp para contato direto. Aguarde o contato.");
          }

        } else {
          this.toast.error(`Não foi possível registrar a solicitação. Status: ${responseSolicitacao.status}`);
        }
      } catch (error) {
        console.error("Erro ao criar solicitação ou contatar:", error.response || error);
        let errorMsg = 'Falha ao registrar a solicitação de serviço.';
        if (error.response && error.response.data && error.response.data.message) {
          errorMsg = error.response.data.message;
        }
        this.toast.error(`Erro: ${errorMsg}`);
      }
    },
  },
};
</script>

<style scoped>

/* ==========================================================================
   PALETA DE CORES E VARIÁVEIS - CONECTIN (VERSÃO PREMIUM)
   ========================================================================== */
:root {
  --azul-principal: #1E7AC5;
  --azul-escuro: #155e8f;
  --amarelo-destaque: #F8B617;
  --amarelo-escuro: #e0a800;
  --fundo-primario: #FFFFFF;
  --fundo-secundario: #F7F9FC;
  --texto-principal: #2c3e50;
  --texto-secundario: #5A6A7B;
  --borda-neutra: #E0E6ED;
  --sombra-cor: rgba(30, 122, 197, 0.1);
}

/* ==========================================================================
   ESTRUTURA BASE E CONTAINER
   ========================================================================== */

.perfil-container {  
  background-color: #f7f9fc;
  display: flex;
  justify-content: center;
  align-items: flex-start;
  max-width: 100%;
  min-height: calc(100vh - 80px);
  padding: 40px 20px;
  font-family: 'Roboto', 'Arial', sans-serif;
  /* max-width: 900px;
  margin: 50px auto;
  padding: 0 15px;
  font-family: 'Roboto', Arial, sans-serif; */
}

.perfil-box {
  width: 60%;
  padding: 30px;
  border-radius: 12px;
  background-color: #FFFFFF;
  box-shadow: 0 8px 24px rgba(30, 122, 197, 0.12);
  position: relative;
  overflow: hidden;
}

.perfil-box::before {
  content: '';
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  height: 6px;
  background: linear-gradient(90deg, var(--conectin-blue) 70%, var(--conectin-yellow) 100%);
}




/* ==========================================================================
   CABEÇALHO DO PERFIL (PONTO FOCAL)
   ========================================================================== */
.perfil-header {
  display: flex;
  flex-direction: column;
  align-items: center;
  text-align: center;
  padding: 40px 30px;
  background: linear-gradient(180deg, var(--fundo-secundario) 0%, #FFFFFF 100%);
}

.perfil-foto {
  width: 250px;
  height: 250px;
  border-radius: 50%;
  object-fit: cover;
  border: 6px solid #FFFFFF;
  box-shadow: 0 0 0 4px #1E7AC5, 0 10px 30px rgba(0, 0, 0, 0.1);
  margin-bottom: 20px;
}

.perfil-header h2 {
  font-size: 2rem;
  font-weight: 700;
  color: #2c3e50;
  margin: 0 0 10px 0;
}

.avaliacao {
  display: inline-flex;
  align-items: center;
  gap: 8px;
  color: #2c3e50;
  /* TEXTO ESCURO NO FUNDO AMARELO */
  font-weight: bold;
  padding: 8px 18px;
  border-radius: 50px;
  font-size: 1.4rem;
  border: none;
}

/* ==========================================================================
   CONTEÚDO PRINCIPAL (LAYOUT INTERNO)
   ========================================================================== */
.perfil-content {
  padding: 30px;
}

h1 {
  display: none;
}

/* O título principal já está claro pelo contexto */

/* Bloco de informações */
.perfil-info {
  padding: 50px;
  background-color: #F7F9FC;
  border-radius: 12px;
  margin-bottom: 30px;
}

.perfil-info p {
  margin: 0 0 12px 0;
  line-height: 1.7;
  color: #5A6A7B;
}

.perfil-info p:last-child {
  margin-bottom: 0;
}

.perfil-info strong {
  color: #155e8f;
  font-weight: 500;
}

/* Botão de Contato - Call to Action Principal */
.contact-section {
  text-align: center;
  margin: 0 0 40px 0;
  /* Espaçamento antes das seções */
}

.contact-btn {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  gap: 10px;
  padding: 12px 28px;
  background-color: #1ee23f;
  /* Verde do WhatsApp */
  color: white;
  text-decoration: none;
  border-radius: 30px;
  font-weight: 500;
  transition: all 0.3s ease;
  box-shadow: 0 4px 12px rgba(30, 122, 197, 0.3);
  position: relative;
  overflow: hidden;
  border: none;
  cursor: pointer;
  font-size: 1em;

}

.contact-btn::after {
  content: '';
  position: absolute;
  top: 0;
  right: -30%;
  width: 30%;
  height: 100%;
  background-color: rgba(255, 255, 255, 0.2);
  transform: skewX(-30deg);
  transition: all 0.5s ease;
}

.contact-btn:hover {
  background-color: #F4B400;
  /* Conectin Yellow */
  transform: translateY(-2px);
  box-shadow: 0 6px 16px rgba(248, 182, 23, 0.4);
  /* Shadow color to match hover */
}

.contact-btn:hover::after {
  right: 120%;
}

.contact-btn i.bi-whatsapp {
  font-size: 1.5em;
}

/* ==========================================================================
   TÍTULOS DE SEÇÃO E PORTFÓLIO
   ========================================================================== */
h3 {
  font-size: 1.5rem;
  font-weight: 700;
  color: #1E7AC5;
  margin: 50px 0 25px 0;
  text-align: center;
  position: relative;
}

h3::after {
  content: '';
  display: block;
  width: 50px;
  height: 3px;
  background-color: #F8B617;
  border-radius: 2px;
  margin: 10px auto 0;
}

.portfolio-section {
  display: flex;
  overflow-x: auto;
  gap: 20px;
  padding: 10px 5px 20px 5px;
  /* Padding para a sombra aparecer */
  scrollbar-width: thin;
  scrollbar-color: transparent;
}

.thumbnail-wrapper {
  flex-shrink: 0;
  width: 180px;
  height: 180px;
  border-radius: 16px;
  overflow: hidden;
  cursor: pointer;
  transition: transform 0.3s ease, box-shadow 0.3s ease;
}

.thumbnail-wrapper:hover {
  transform: translateY(-8px);
  box-shadow: 0 10px 25px rgba(30, 122, 197, 0.1);
}

.portfolio-thumbnail {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

/* ==========================================================================
  SEÇÃO DE AVALIAÇÕES
   ========================================================================== */
.avaliacoes-header {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 15px;
  margin-bottom: 25px;
}

.filtro-ordenacao select {
  padding: 8px 12px;
  border: 1px solid var(--borda-neutra);
  border-radius: 8px;
  background-color: #FFFFFF;
  font-size: 14px;
  cursor: pointer;
}

.filtro-ordenacao select:focus {
  outline: none;
  border-color: #F8B617;
}

.avaliacao-card {
  background-color: #FFFFFF;
  border-radius: 16px;
  padding: 25px;
  margin-bottom: 20px;
  transition: all 0.3s ease;
}

.avaliacao-card:hover {
  transform: translateY(-5px);
  border-color: #1E7AC5;
  box-shadow: 0 8px 25px rgba(30, 122, 197, 0.1);
}

.avaliador-info {
  display: flex;
  align-items: center;
  gap: 15px;
}

.avaliador-foto {
  width: 50px;
  height: 50px;
  border-radius: 50%;
  object-fit: cover;
}

.avaliador-detalhes {
  display: flex;
  flex-direction: column;
}

.avaliador-nome {
  font-weight: bold;
  color: #2c3e50;
}

.avaliacao-data {
  font-size: 0.8rem;
  color: #5A6A7B;
}

.avaliacao-conteudo {
  margin-top: 15px;
  padding-top: 15px;
}

.nota-estrelas {
  font-size: 1.2rem;
}

.nota-estrelas .estrela {
  color: #ccc;
}

.nota-estrelas .estrela.preenchida {
  color: #F8B617;
}

.comentario-texto {
  margin: 10px 0 0 0;
  line-height: 1.6;
  color: #5A6A7B;
}

.avaliador-info-link {
  text-decoration: none;
  /* Remove o sublinhado do link */
  color: inherit;
  /* Faz o texto herdar a cor do pai */
  transition: transform 0.2s ease-in-out;
  display: block;
}

.avaliador-info-link:hover {
  transform: scale(1.02);
  /* Efeito sutil de zoom no hover */
}

/* ==========================================================================
   ESTADOS (LOADING, SEM RESULTADOS) E GALERIA
   ========================================================================== */
.loading,
.sem-avaliacoes,
.loading-avaliacoes,
.portfolio-section p {
  text-align: center;
  padding: 40px;
  color: #5A6A7B;
  font-style: italic;
}

.loading-spinner,
.loading-spinner-small {
  border: 4px solid rgba(30, 122, 197, 0.1);
  border-radius: 50%;
  border-top-color: #F8B617;
  animation: spin 1s linear infinite;
  display: inline-block;
  margin: 0 auto;
}

.loading-spinner {
  width: 50px;
  height: 50px;
  margin-bottom: 15px;
}

.loading-spinner-small {
  width: 25px;
  height: 25px;
  margin: 0 10px 0 0;
  vertical-align: middle;
}

/* ======================= ESTILOS PARA A GALERIA EXPANDIDA ====================== */
.galeria-overlay {
  position: fixed;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  background-color: rgba(0, 0, 0, 0.9);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 1001;
  /* Z-index maior que o resto do conteúdo */
  padding: 20px;
  box-sizing: border-box;
}

.galeria-content {
  background-color: #fff;
  padding: 30px;
  border-radius: 12px;
  max-width: 95vw;
  max-height: 95vh;
  overflow-y: auto;
  /* Permite scroll se as fotos não couberem */
  position: relative;
  width: 100%;
}

.galeria-close-btn {
  position: absolute;
  top: 10px;
  right: 15px;
  background: none;
  border: none;
  color: #333;
  font-size: 2.5em;
  cursor: pointer;
  line-height: 1;
}

.galeria-title {
  margin-top: 0;
  margin-bottom: 25px;
  color: var(--conectin-blue, #1e7ac5);
  padding-bottom: 10px;
  border-bottom: 1px solid #eee;
}

.galeria-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(250px, 1fr));
  gap: 20px;
}

.galeria-image-wrapper {
  width: 100%;
  height: 250px;
  border-radius: 8px;
  overflow: hidden;
}

.galeria-image {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

@keyframes spin {
  to {
    transform: rotate(360deg);
  }
}

/* Responsividade */
@media (max-width: 768px) {
  .perfil-box {
    padding: 20px;
  }

  .perfil-header {
    flex-direction: column;
    align-items: flex-start;
  }

  .perfil-header h2 {
    margin-bottom: 10px;
  }
}

@keyframes spin {
  to {
    transform: rotate(360deg);
  }
}

/* ==========================================================================
   RESPONSIVIDADE
   ========================================================================== */
@media (max-width: 768px) {
  .perfil-box {
    padding: 0;
    border-radius: 0;
  }

  .perfil-content {
    padding: 25px;
  }

  .perfil-foto {
    width: 120px;
    height: 120px;
  }

  .perfil-header h2 {
    font-size: 1.8rem;
  }
}
</style>