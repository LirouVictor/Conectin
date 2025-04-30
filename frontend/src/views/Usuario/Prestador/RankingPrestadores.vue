<template>
  <div class="ranking-container">
    <div class="ranking-box">
      <h1>Ranking de Prestadores</h1>
      <p class="intro-text">
        Conheça os melhores prestadores da plataforma, ordenados por avaliações. Clique em um prestador para ver mais detalhes!
      </p>
      <div class="prestadores-container">
        <div class="prestador-card" v-for="prestador in prestadores" :key="prestador.id">
          <div class="prestador-info">
            <h3>{{ prestador.nome }}</h3>
            <p class="categorias" v-if="prestador.categorias && prestador.categorias.length">
              Categorias: {{ prestador.categorias.map(c => c.nome).join(', ') }}
            </p>
            <p class="avaliacao">
              Avaliação: {{ prestador.avaliacaoMedia ? prestador.avaliacaoMedia.toFixed(1) : 'Sem avaliações' }} ⭐
            </p>
            <p class="disponibilidade">Disponibilidade: {{ prestador.disponibilidade || 'Não informado' }}</p>
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
      prestadores: [],
    };
  },
  setup() {
    const toast = useToast();
    return { toast };
  },
  mounted() {
    this.fetchPrestadores();
  },
  methods: {
    async fetchPrestadores() {
      try {
        const response = await api.get('/prestadores/ranking');
        this.prestadores = await Promise.all(response.data.map(async (prestador) => {
          const categoriasResponse = await api.get(`/prestadores/${prestador.id}/categorias`);
          return { ...prestador, categorias: categoriasResponse.data };
        }));
      } catch (error) {
        if (error.response && error.response.data) {
          this.toast.error(error.response.data.message);
        } else {
          this.toast.error('Erro ao carregar prestadores.');
        }
      }
    },
  },
};
</script>

<style scoped>
.ranking-container {
  max-width: 800px;
  margin: 20px auto;
}

.ranking-box {
  padding: 20px;
}

.prestador-card {
  border: 1px solid #ccc;
  border-radius: 5px;
  padding: 10px;
  margin-bottom: 10px;
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.ver-perfil-btn {
  color: #007bff;
  text-decoration: none;
}

.ver-perfil-btn:hover {
  text-decoration: underline;
}
</style>