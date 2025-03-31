<template>
  <div class="home-container">

    <!-- Seção Principal -->
    <div class="header-section">
      <div class="header-text">
        <h1>Encontre o Profissional Ideal</h1>
        <p>Conecte-se com trabalhadores qualificados na sua região!</p>
      </div>
    </div>

    <!-- Seção de Categorias -->
    <div class="categories-section">
      <input
        v-model="searchCategory"
        type="text"
        placeholder="Busque por categoria (ex.: Jardineiro, Pedreiro...)"
        class="category-search"
      />
      <div class="categories-grid">
        <div
          v-for="category in filteredCategories"
          :key="category.id"
          class="category-card"
          @click="openRegionModal(category)"
        >
          <img :src="category.image" :alt="category.name" class="category-image" />
          <h3>{{ category.name }}</h3>
        </div>
      </div>
    </div>

    <!-- Modal para inserção da região -->
    <div v-if="showRegionModal" class="modal-overlay">
      <div class="modal-content">
        <h2>Qual é a sua região?</h2>
        <input
          v-model="selectedRegion"
          type="text"
          placeholder="Digite sua cidade ou região"
          class="region-input"
        />
        <div class="modal-buttons">
          <button @click="confirmRegion" class="confirm-btn">Confirmar</button>
          <button @click="closeRegionModal" class="cancel-btn">Cancelar</button>
        </div>
      </div>
    </div>
  </div>
</template>

<script>
export default {
  name: 'HomePage',
  data() {
    return {
      searchCategory: '',
      categories: [
        { id: 1, name: 'Jardineiro', image: '/images/jardineiro.jpg' },
        { id: 2, name: 'Ar Condicionado', image: '/images/ar-condicionado.jpg' },
        { id: 3, name: 'Pedreiro', image: '/images/pedreiro.jpg' },
        { id: 4, name: 'Encanador', image: '/images/encanador.jpg' },
        { id: 5, name: 'Eletricista', image: '/images/eletricista.jpg' },
      ],
      showRegionModal: false,
      selectedCategory: null,
      selectedRegion: '',
    };
  },
  computed: {
    filteredCategories() {
      return this.categories.filter((category) =>
        category.name.toLowerCase().includes(this.searchCategory.toLowerCase())
      );
    },
  },
  methods: {
    openRegionModal(category) {
      this.selectedCategory = category;
      this.showRegionModal = true;
    },
    closeRegionModal() {
      this.showRegionModal = false;
      this.selectedRegion = '';
      this.selectedCategory = null;
    },
    confirmRegion() {
      if (this.selectedRegion.trim() === '') {
        alert('Por favor, insira uma cidade ou região.');
        return;
      }
      this.$router.push({
        path: '/workers',
        query: { category: this.selectedCategory.name, region: this.selectedRegion },
      });
      this.closeRegionModal();
    },
  },
};
</script>

<style scoped>
.home-container {
  padding: 0;
  background-color: #f5f5f5; /* Fundo cinza claro para contraste */
}

/* Seção Principal */
.header-section {
  display: flex;
  align-items: center;
  justify-content: center;
  padding-bottom: 40px;
  background-color: #257BB8; /* Fundo azul do logo */
  color: #FFFFFF; /* Texto branco */
}

.header-text {
  max-width: 50%;
}

h1 {
  font-size: 2.5rem;
  margin-bottom: 10px;
}

p {
  font-size: 1.2rem;
}

.header-image {
  width: 300px;
  height: auto;
  border-radius: 10px;
  margin-left: 20px;
  border: 3px solid #F4B400; /* Borda amarela do logo */
}

/* Seção de Categorias */
.categories-section {
  max-width: 1200px;
  margin: 40px auto;
  padding: 0 20px;
}

.category-search {
  padding: 12px;
  width: 100%;
  max-width: 500px;
  font-size: 1rem;
  border: 2px solid #257BB8; /* Borda azul */
  border-radius: 5px;
  margin-bottom: 20px;
  transition: border-color 0.3s;
}

.category-search:focus {
  border-color: #F4B400; /* Amarelo ao focar */
  outline: none;
}

.categories-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(200px, 1fr));
  gap: 20px;
}

.category-card {
  background-color: #FFFFFF;
  border-radius: 10px;
  padding: 15px;
  cursor: pointer;
  transition: transform 0.2s, box-shadow 0.2s;
  border: 1px solid #257BB8; /* Borda azul */
}

.category-card:hover {
  transform: scale(1.05);
  box-shadow: 0 4px 15px rgba(0, 0, 0, 0.1);
}

.category-image {
  width: 100%;
  height: 150px;
  object-fit: cover;
  border-radius: 5px;
  margin-bottom: 10px;
}

h3 {
  font-size: 1.2rem;
  color: #333;
  margin: 0;
}

/* Estilos do Modal */
.modal-overlay {
  position: fixed;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  background: rgba(0, 0, 0, 0.5);
  display: flex;
  justify-content: center;
  align-items: center;
}

.modal-content {
  background: #FFFFFF;
  padding: 20px;
  border-radius: 10px;
  width: 400px;
  text-align: center;
  border: 2px solid #257BB8; /* Borda azul */
}

h2 {
  font-size: 1.5rem;
  color: #257BB8; /* Título azul */
}

.region-input {
  padding: 10px;
  width: 92%;
  font-size: 1rem;
  border: 2px solid #257BB8; /* Borda azul */
  border-radius: 5px;
  margin: 20px 0;
  transition: border-color 0.3s;
}

.region-input:focus {
  border-color: #F4B400; /* Amarelo ao focar */
  outline: none;
}

.modal-buttons {
  display: flex;
  justify-content: space-around;
}

.confirm-btn,
.cancel-btn {
  padding: 10px 20px;
  font-size: 1rem;
  border: none;
  border-radius: 5px;
  cursor: pointer;
  transition: background-color 0.3s;
}

.confirm-btn {
  background-color: #F4B400; /* Amarelo do logo */
  color: #FFFFFF;
}

.confirm-btn:hover {
  background-color: #d9a300; /* Amarelo mais escuro */
}

.cancel-btn {
  background-color: #257BB8; /* Azul do logo */
  color: #FFFFFF;
}

.cancel-btn:hover {
  background-color: #257BB8; /* Azul mais escuro */
}
</style>