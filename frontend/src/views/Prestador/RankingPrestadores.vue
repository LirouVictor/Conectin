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
  import axios from 'axios';
  
  export default {
    name: 'RankingPrestadores',
    data() {
      return {
        prestadores: []
      };
    },
    mounted() {
      this.fetchPrestadores();
    },
    methods: {
      async fetchPrestadores() {
        try {
          const response = await axios.get('http://localhost:8080/api/prestadores/ranking');
          // Para cada prestador, buscamos suas categorias
          this.prestadores = await Promise.all(response.data.map(async (prestador) => {
            const categoriasResponse = await axios.get(`http://localhost:8080/api/prestadores/${prestador.id}/categorias`);
            return { ...prestador, categorias: categoriasResponse.data };
          }));
        } catch (error) {
          console.error('Erro ao carregar prestadores:', error);
        }
      }
    }
  };
  </script>
  
  <style scoped>
  .ranking-container {
    display: flex;
    justify-content: center;
    align-items: flex-start;
    min-height: calc(100vh - 80px);
    background-color: #f5f5f5;
    padding: 20px 0;
  }
  
  .ranking-box {
    background-color: #FFFFFF;
    padding: 40px;
    border-radius: 10px;
    box-shadow: 0 4px 15px rgba(0, 0, 0, 0.1);
    width: 100%;
    max-width: 900px;
    text-align: left;
    border: 2px solid #257BB8;
  }
  
  h1 {
    font-size: 2rem;
    color: #257BB8;
    text-align: center;
    margin-bottom: 20px;
  }
  
  .intro-text {
    font-size: 1.1rem;
    color: #333;
    margin-bottom: 30px;
    text-align: center;
  }
  
  .prestadores-container {
    display: flex;
    flex-direction: column;
    gap: 20px;
  }
  
  .prestador-card {
    display: flex;
    justify-content: space-between;
    align-items: center;
    background-color: #FFFFFF;
    padding: 20px;
    border-radius: 8px;
    border: 1px solid #257BB8;
    transition: transform 0.2s, box-shadow 0.2s;
  }
  
  .prestador-card:hover {
    transform: translateY(-5px);
    box-shadow: 0 4px 15px rgba(0, 0, 0, 0.1);
  }
  
  .prestador-info h3 {
    font-size: 1.2rem;
    color: #257BB8;
    margin-bottom: 10px;
  }
  
  .prestador-info .categorias,
  .prestador-info .disponibilidade {
    font-size: 1rem;
    color: #666;
    margin-bottom: 5px;
  }
  
  .prestador-info .avaliacao {
    font-size: 1rem;
    color: #F4B400;
    font-weight: bold;
  }
  
  .ver-perfil-btn {
    background-color: #257BB8;
    color: #FFFFFF;
    padding: 10px 20px;
    border-radius: 5px;
    text-decoration: none;
    font-size: 1rem;
    transition: background-color 0.2s;
  }
  
  .ver-perfil-btn:hover {
    background-color: #1e5f8a;
  }
  </style>