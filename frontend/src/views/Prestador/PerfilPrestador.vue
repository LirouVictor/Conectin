<template>
    <div class="perfil-container">
      <div class="perfil-box">
        <h1>Perfil do Prestador</h1>
        <div v-if="prestador" class="perfil-content">
          <!-- Cabeçalho do Perfil -->
          <div class="perfil-header">
            <h2>{{ prestador.nome }}</h2>
            <p class="avaliacao">
              Avaliação: {{ prestador.avaliacaoMedia ? prestador.avaliacaoMedia.toFixed(1) : 'Sem avaliações' }} ⭐
            </p>
          </div>
  
          <!-- Informações Principais -->
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
  
          <!-- Portfólio -->
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
  
          <!-- Botão de Contato -->
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
  import axios from 'axios';
  
  export default {
    name: 'PerfilPrestador',
    data() {
      return {
        prestador: null
      };
    },
    computed: {
      whatsappLink() {
        // Assumindo que o número de telefone vem do backend ou é fixo
        const numero = '+5511999999999'; // Substitua pelo número real ou adicione ao PrestadorDto
        const mensagem = encodeURIComponent(`Olá, vi seu perfil no Conectin e gostaria de contratar seus serviços!`);
        return `https://wa.me/${numero}?text=${mensagem}`;
      }
    },
    mounted() {
      this.fetchPrestador();
    },
    methods: {
      async fetchPrestador() {
        try {
          const id = this.$route.params.id;
          const response = await axios.get(`http://localhost:8080/api/prestadores/${id}`);
          this.prestador = response.data;
        } catch (error) {
          console.error('Erro ao carregar prestador:', error);
        }
      }
    }
  };
  </script>
  
  <style scoped>
  .perfil-container {
    display: flex;
    justify-content: center;
    align-items: flex-start;
    min-height: calc(100vh - 80px);
    background-color: #f5f5f5;
    padding: 20px 0;
  }
  
  .perfil-box {
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
  
  .perfil-header {
    text-align: center;
    margin-bottom: 20px;
  }
  
  .perfil-header h2 {
    font-size: 1.5rem;
    color: #257BB8;
    margin-bottom: 10px;
  }
  
  .perfil-header .avaliacao {
    font-size: 1rem;
    color: #F4B400;
    font-weight: bold;
  }
  
  .perfil-info p {
    font-size: 1rem;
    color: #666;
    margin-bottom: 10px;
  }
  
  .perfil-info strong {
    color: #333;
  }
  
  .portfolio-section {
    margin-top: 20px;
  }
  
  .portfolio-section h3 {
    font-size: 1.2rem;
    color: #257BB8;
    margin-bottom: 10px;
  }
  
  .portfolio-grid {
    display: grid;
    grid-template-columns: repeat(auto-fit, minmax(200px, 1fr));
    gap: 20px;
  }
  
  .portfolio-item {
    background-color: #f9f9f9;
    padding: 10px;
    border-radius: 8px;
    border: 1px solid #257BB8;
    text-align: center;
  }
  
  .portfolio-image {
    max-width: 100%;
    height: auto;
    border-radius: 5px;
    margin-bottom: 10px;
  }
  
  .contact-section {
    margin-top: 30px;
    text-align: center;
  }
  
  .contact-btn {
    background-color: #257BB8;
    color: #FFFFFF;
    padding: 10px 20px;
    border-radius: 5px;
    text-decoration: none;
    font-size: 1rem;
    transition: background-color 0.2s;
  }
  
  .contact-btn:hover {
    background-color: #1e5f8a;
  }
  
  .loading {
    text-align: center;
    font-size: 1rem;
    color: #666;
  }
  </style>