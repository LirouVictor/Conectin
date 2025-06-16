<template>
  <div class="perfil-container">
    <div class="perfil-box">
      <h1>Perfil do Prestador</h1>
      <div v-if="prestador" class="perfil-content">
        <div class="perfil-header">
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

        <div class="contact-section">
          <!-- O botão continua o mesmo, a mágica acontece no método 'handleContact' -->
          <button @click="handleContact" class="contact-btn">
            <i class="bi bi-whatsapp"></i>
            <span>Entrar em Contato via WhatsApp</span>
          </button>
        </div>
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

    // ===== NOVO MÉTODO PARA FORMATAR URL DA FOTO =====
    getFotoUrl(fotoPath) {
      if (!fotoPath) {
        // Retorna uma imagem padrão caso o usuário não tenha foto
        return 'https://www.gravatar.com/avatar/?d=mp';
      }
      // Verifica se o caminho já é uma URL completa
      if (fotoPath.startsWith('http')) {
        return fotoPath;
      }
      // Adiciona a URL base do backend ao caminho relativo da imagem
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
        this.toast.info('Você precisa estar cadastrado para entrar em contato. Redirecionando para cadastro...');
        this.router.push({ name: 'Cadastro' });
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
/* Seu CSS permanece o mesmo, sem necessidade de alteração */
:root {
  --conectin-blue: #1e7ac5;
  --conectin-blue-dark: #156cb2;
  --conectin-yellow: #f8b617;
  --conectin-yellow-light: #ffd266;
  --conectin-white: #ffffff;
  --conectin-light-gray: #f5f7fa;
  --conectin-gray: #e1e5eb;
}

.perfil-container {
  max-width: 900px;
  margin: 30px auto;
  font-family: 'Roboto', Arial, sans-serif;
}

.perfil-box {
  padding: 30px;
  border-radius: 12px;
  background-color: var(--conectin-white);
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

h1 {
  color: var(--conectin-blue);
  font-size: 28px;
  margin-bottom: 30px;
  padding-bottom: 15px;
  border-bottom: 1px solid var(--conectin-gray);
  position: relative;
}

h1::after {
  content: '';
  position: absolute;
  bottom: -1px;
  left: 0;
  width: 80px;
  height: 3px;
  background-color: var(--conectin-yellow);
}

.perfil-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
  flex-wrap: wrap;
}

.perfil-header h2 {
  color: var(--conectin-blue-dark);
  font-size: 24px;
  margin: 0;
}

.avaliacao {
  background-color: var(--conectin-light-gray);
  padding: 6px 12px;
  border-radius: 20px;
  display: inline-flex;
  align-items: center;
  font-weight: 500;
  color: #555;
  border: 1px solid var(--conectin-gray);
}

.perfil-info {
  background-color: var(--conectin-light-gray);
  padding: 20px;
  border-radius: 8px;
  margin-bottom: 25px;
  border-left: 4px solid var(--conectin-blue);
}

.perfil-info p {
  margin: 10px 0;
  line-height: 1.6;
}

.perfil-info strong {
  color: var(--conectin-blue-dark);
  margin-right: 5px;
}

.contact-section {
  text-align: center;
  margin-top: 30px;
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

.loading {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 40px 0;
}

.loading-spinner {
  width: 50px;
  height: 50px;
  border: 5px solid var(--conectin-gray);
  border-radius: 50%;
  border-top-color: var(--conectin-blue);
  border-right-color: var(--conectin-yellow);
  animation: spin 1s linear infinite;
  margin-bottom: 15px;
}

.avaliacoes-section {
  margin-top: 40px;
  padding-top: 25px;
  border-top: 1px solid var(--conectin-gray);
}

.avaliacoes-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
}

.avaliacoes-header h3 {
  color: var(--conectin-blue);
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
  border: 1px solid var(--conectin-gray);
  border-radius: 6px;
  background-color: var(--conectin-white);
  font-size: 14px;
}

.avaliacao-card {
  background-color: var(--conectin-light-gray);
  border-radius: 8px;
  padding: 20px;
  margin-bottom: 15px;
  border-left: 4px solid var(--conectin-yellow);
  display: flex;
  flex-direction: column;
  gap: 15px;
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
  border: 2px solid var(--conectin-white);
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);
}

.avaliador-detalhes {
  display: flex;
  flex-direction: column;
}

.avaliador-nome {
  font-weight: bold;
  color: var(--conectin-blue-dark);
}

.avaliacao-data {
  font-size: 12px;
  color: #777;
}

.avaliacao-conteudo .nota-estrelas {
  margin-bottom: 8px;
  color: var(--conectin-yellow);
  font-size: 18px;
}

.nota-estrelas .estrela {
  color: #ccc;
  /* Estrela vazia */
}

.nota-estrelas .estrela.preenchida {
  color: var(--conectin-yellow);
  /* Estrela cheia */
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
  background-color: var(--conectin-light-gray);
  border-radius: 8px;
  color: #777;
}

.loading-spinner-small {
  width: 25px;
  height: 25px;
  border: 3px solid var(--conectin-gray);
  border-radius: 50%;
  border-top-color: var(--conectin-blue);
  animation: spin 1s linear infinite;
  display: inline-block;
  vertical-align: middle;
  margin-right: 10px;
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

/* ================== NOVOS ESTILOS PARA O PORTFÓLIO HORIZONTAL ================== */

.portfolio-section {
  display: flex;
    display: flex;
  /* ESSENCIAL: Alinha os filhos (fotos) lado a lado */
  overflow-x: auto;
  /* ESSENCIAL: Adiciona a barra de rolagem horizontal */
  gap: 15px;
  /* Cria um espaço entre as fotos */
  scrollbar-width: thin;
  /* Barra de rolagem mais fina (Firefox) */
  scrollbar-color: var(--conectin-gray) transparent;
  padding: 5px 0 15px 0;
  margin-top: 40px;
  padding-top: 25px;
  border-top: 1px solid var(--conectin-gray);
}

.portfolio-section h3 {
  color: var(--conectin-blue);
  margin-bottom: 25px;
  font-size: 20px;
  position: relative;
  padding-left: 16px;
}

.portfolio-section h3::before {
  content: '';
  position: absolute;
  left: 0;
  top: 50%;
  transform: translateY(-50%);
  width: 8px;
  height: 20px;
  background-color: var(--conectin-yellow);
  border-radius: 4px;
}

.portfolio-item-container {
  margin-bottom: 35px;
  /* Espaço entre diferentes projetos do portfólio */
}

.portfolio-item-container:last-child {
  margin-bottom: 0;
}

.portfolio-item-title {
  font-size: 1.2em;
  font-weight: 500;
  color: var(--conectin-blue-dark);
  margin: 0 0 5px 0;
}

.portfolio-item-description {
  margin: 0 0 15px 0;
  /* Espaço entre a descrição e as fotos */
  color: #555;
  line-height: 1.5;
}

/* 2. O WRAPPER DE CADA FOTO */
.thumbnail-wrapper {
  flex-shrink: 0;
  /* CRÍTICO: Impede que as fotos encolham ou quebrem a linha */
  width: 150px;
  /* Largura fixa da miniatura */
  height: 150px;
  /* Altura fixa da miniatura */
  border-radius: 8px;
  overflow: hidden;
  /* Garante que a imagem respeite o border-radius */
  cursor: pointer;
  box-shadow: 0 4px 10px rgba(0, 0, 0, 0.08);
  transition: transform 0.2s ease-in-out, box-shadow 0.2s ease-in-out;
}

.thumbnail-wrapper:hover {
  transform: scale(1.05);
  box-shadow: 0 6px 15px rgba(30, 122, 197, 0.15);
}

/* 3. A IMAGEM DENTRO DO WRAPPER */
.portfolio-thumbnail {
  width: 100%;
  height: 100%;
  object-fit: cover;
  /* Garante que a imagem preencha o espaço sem distorcer */
}

/* ================== FIM DOS NOVOS ESTILOS ================== */


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
</style>