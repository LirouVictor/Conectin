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
          <a :href="whatsappLink" target="_blank" class="contact-btn">Entrar em Contato via WhatsApp</a>
        </div>
      </div>
      <div v-else class="loading">
        <p>Carregando perfil...</p>
      </div>
    </div>
  </div>
</template>

<script>
import api from '@/services/api';
import { useToast } from 'vue-toastification';

export default {
  name: 'PerfilPrestador',
  data() {
    return {
      prestador: null,
    };
  },
  setup() {
    const toast = useToast();
    return { toast };
  },
  computed: {
    whatsappLink() {
      const numero = '+5511999999999'; // Substitua pelo número real ou adicione ao PrestadorDto
      const mensagem = encodeURIComponent(`Olá, vi seu perfil no Conectin e gostaria de contratar seus serviços!`);
      return `https://wa.me/${numero}?text=${mensagem}`;
    },
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
          this.toast.error(error.response.data.message);
        } else {
          this.toast.error('Erro ao carregar perfil do prestador.');
        }
      }
    },
  },
};
</script>

<style scoped>
.perfil-container {
  max-width: 800px;
  margin: 20px auto;
}

.perfil-box {
  padding: 20px;
  border: 1px solid #ccc;
  border-radius: 5px;
}

.contact-btn {
  display: inline-block;
  padding: 10px 20px;
  background-color: #25d366;
  color: white;
  text-decoration: none;
  border-radius: 5px;
}

.contact-btn:hover {
  background-color: #1ebe57;
}
</style>