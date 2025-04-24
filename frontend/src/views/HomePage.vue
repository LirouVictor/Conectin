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
          <!-- <img :src="category.image" :alt="category.name" class="category-image" /> -->
          <h3>{{ category.name }}</h3>
        </div>
      </div>
    </div>

    <!-- Modal para escolha da cidade -->
    <div v-if="showRegionModal" class="modal-overlay">
      <div class="modal-content">
        <h2>Selecione sua cidade</h2>
        <input
          v-model="searchCity"
          type="text"
          placeholder="Digite para filtrar cidades..."
          class="region-input"
        />
        <div class="cities-list">
          <div
            v-for="city in filteredCities"
            :key="city"
            class="city-option"
            @click="selectCity(city)"
          >
            {{ city }}
          </div>
        </div>
        <div class="modal-buttons">
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
        { id: 1, name: 'Jardineiro'},
        { id: 2, name: 'Ar Condicionado'},
        { id: 3, name: 'Pedreiro'},
        { id: 4, name: 'Encanador'},
        { id: 5, name: 'Eletricista'},
      ],
      cities: [
        'Armazém',
        'Braço do Norte',
        'Capivari de Baixo',
        'Grão-Pará',
        'Gravatal',
        'Imaruí',
        'Imbituba',
        'Jaguaruna',
        'Laguna',
        'Paulo Lopes',
        'Rio Fortuna',
        'São Bonifácio',
        'São Martinho',
        'Santa Rosa de Lima',
        'Tubarão',
      ],
      showRegionModal: false,
      selectedCategory: null,
      selectedRegion: '',
      searchCity: '',
    };
  },
  computed: {
    filteredCategories() {
      return this.categories.filter((category) =>
        category.name.toLowerCase().includes(this.searchCategory.toLowerCase())
      );
    },
    filteredCities() {
      return this.cities.filter((city) =>
        city.toLowerCase().includes(this.searchCity.toLowerCase())
      );
    },
  },
  methods: {
    openRegionModal(category) {
      this.selectedCategory = category;
      this.showRegionModal = true;
      this.searchCity = '';
    },
    closeRegionModal() {
      this.showRegionModal = false;
      this.selectedRegion = '';
      this.selectedCategory = null;
      this.searchCity = '';
    },
    selectCity(city) {
      this.selectedRegion = city;
      // Salvar a cidade no localStorage
      localStorage.setItem('selectedCity', city);
      // Redirecionar para a página de trabalhadores
      // this.$router.push({
      //   path: '/trabalhadores',
      //   query: { category: this.selectedCategory.name, region: this.selectedRegion },
      // });
      this.closeRegionModal();
    },
  },
};
</script>

<style scoped>
.home-container {
  padding: 0;
  background-color: #f5f5f5;
}

.header-logo {
  background-color: #257BB8;
  padding: 20px;
  text-align: center;
}

.logo {
  width: 200px;
  height: auto;
}

.header-section {
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 40px 20px;
  background-color: #257BB8;
  color: #FFFFFF;
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
  border: 3px solid #F4B400;
}

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
  border: 2px solid #257BB8;
  border-radius: 5px;
  margin-bottom: 20px;
  transition: border-color 0.3s;
}

.category-search:focus {
  border-color: #F4B400;
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
  border: 1px solid #257BB8;
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
  border: 2px solid #257BB8;
}

h2 {
  font-size: 1.5rem;
  color: #257BB8;
}

.region-input {
  padding: 10px;
  width: 94%;
  font-size: 1rem;
  border: 2px solid #257BB8;
  border-radius: 5px;
  margin: 20px 0;
  transition: border-color 0.3s;
}

.region-input:focus {
  border-color: #F4B400;
  outline: none;
}

.cities-list {
  max-height: 200px;
  overflow-y: auto;
  margin-bottom: 20px;
  border: 1px solid #257BB8;
  border-radius: 5px;
}

.city-option {
  padding: 10px;
  cursor: pointer;
  transition: background-color 0.3s;
}

.city-option:hover {
  background-color: #F4B400;
  color: #FFFFFF;
}

.modal-buttons {
  display: flex;
  justify-content: center;
}

.cancel-btn {
  padding: 10px 20px;
  font-size: 1rem;
  border: none;
  border-radius: 5px;
  cursor: pointer;
  background-color: #257BB8;
  color: #FFFFFF;
  transition: background-color 0.3s;
}

.cancel-btn:hover {
  background-color: #1557b0;
}
</style>