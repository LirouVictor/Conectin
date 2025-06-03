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
          <p><strong>Telefone:</strong> {{ prestador.telefone || 'Não informado' }}</p>
          <p class="descricao"><strong>Descrição:</strong> {{ prestador.descricao || 'Não informado' }}</p>
          <p class="disponibilidade">
            <strong>Disponibilidade:</strong> {{ prestador.disponibilidade || 'Não informado' }}
          </p>
          <p class="categorias" v-if="prestador.categorias && prestador.categorias.length">
            <strong>Categorias:</strong> {{ prestador.categorias.map(c => c.nome).join(', ') }}
          </p>
          <p class="cidades" v-if="prestador.cidades && prestador.cidades.length">
            <strong>Cidades Atendidas:</strong> {{ prestador.cidades.map(c => c.nome).join(', ') }}
          </p>
        </div>
        <div class="portfolio-section" v-if="prestador.portfolios && prestador.portfolios.length">
          <h3>Portfólio</h3>
          <div class="portfolio-grid">
            <div class="portfolio-item" v-for="item in prestador.portfolios" :key="item.id">
              <img v-if="item.urlImagem" :src="item.urlImagem" alt="Portfolio" class="portfolio-image" />
              <p>{{ item.descricao || 'Sem descrição' }}</p>
            </div>
          </div>
        </div>
        <div class="portfolio-section" v-else>
          <p>Nenhum item no portfólio.</p>
        </div>
        <div class="contact-section">
          <!-- O botão continua o mesmo, a mágica acontece no método 'handleContact' -->
          <button @click="handleContact" class="contact-btn">Entrar em Contato via WhatsApp</button>
        </div>
      </div>
      <div v-else class="loading">
        <div class="loading-spinner"></div>
        <p>Carregando perfil...</p>
      </div>
    </div>
  </div>
</template>

<script>
import api from '@/services/api';
import { useToast } from 'vue-toastification';
import { useUserStore } from '@/stores/user';
import { useRouter } from 'vue-router';

export default {
  name: 'PerfilPrestador',
  data() {
    return {
      prestador: null,
    };
  },
  setup() {
    const toast = useToast();
    const userStore = useUserStore();
    const router = useRouter();
    return { toast, userStore, router };
  },
  mounted() {
    this.fetchPrestador();
  },
  methods: {
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
            
            // A mensagem do WhatsApp agora inclui a categoria específica selecionada pelo usuário
            const mensagem = encodeURIComponent(`Olá ${nomePrestador}, vi seu perfil no Conectin e gostaria de mais informações sobre seus serviços (${categoriaSelecionada.nome}). Minha solicitação (ID: ${responseSolicitacao.data.id}) foi registrada no sistema.`);
            
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

.portfolio-section h3 {
  color: var(--conectin-blue);
  margin-bottom: 15px;
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

.portfolio-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(200px, 1fr));
  gap: 20px;
  margin-bottom: 30px;
}

.portfolio-item {
  background: white;
  border-radius: 8px;
  overflow: hidden;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.05);
  transition: transform 0.3s ease, box-shadow 0.3s ease;
}

.portfolio-item:hover {
  transform: translateY(-5px);
  box-shadow: 0 8px 16px rgba(0, 0, 0, 0.1);
}

.portfolio-image {
  width: 100%;
  height: 150px;
  object-fit: cover;
  border-bottom: 3px solid var(--conectin-yellow);
}

.portfolio-item p {
  padding: 12px;
  margin: 0;
  font-size: 14px;
  color: #444;
}

.contact-section {
  text-align: center;
  margin-top: 30px;
}

.contact-btn {
  display: inline-block;
  padding: 12px 28px;
  background-color: #257BB8;
  /* Original Conectin Blue */
  color: white;
  text-decoration: none;
  border-radius: 30px;
  font-weight: 500;
  transition: all 0.3s ease;
  box-shadow: 0 4px 12px rgba(30, 122, 197, 0.3);
  position: relative;
  overflow: hidden;
  border: none;
  /* Ensure button style consistency */
  cursor: pointer;
  /* Standard cursor for buttons */
  font-size: 1em;
  /* Match typical button font size */
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

  .portfolio-grid {
    grid-template-columns: repeat(auto-fill, minmax(150px, 1fr));
  }
}
</style>