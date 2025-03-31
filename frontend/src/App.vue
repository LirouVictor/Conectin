<template>
  <div id="app">
    <!-- Menu de navegação com logo -->
    <header class="navbar">
      <div class="navbar-container">
        <img src="../public/images/logoConectin-removebg.png" alt="Conectin Logo" class="logo" />
        <nav>
          <router-link to="/">Home</router-link> |
          <router-link to="/login">Login</router-link> |
          <router-link to="/cadastro">Register</router-link> |
          <router-link to="/comofunciona">Como funciona</router-link>
        </nav>
      </div>
    </header>

    <!-- Renderiza as rotas aqui -->
    <router-view />
  </div>
</template>

<script>
export default {
  name: 'App',
};
</script>

<style>
/* Estilos gerais */
body {
  padding: 0;
  margin: 0;
  font-family: Arial, sans-serif;
  background-color: #f5f5f5;
}

/* Estilos da Navbar */
.navbar {
  background-color: #257BB8; /* Cor de fundo solicitada */
  padding: 15px 0;
  box-shadow: 0 2px 5px rgba(0, 0, 0, 0.1); /* Sombra para profundidade */
}

.navbar-container {
  /* max-width: 1200px; */
  /* margin: 0 auto; */
  display: flex;
  align-items: center;
  /*justify-content: space-between;  Alinha logo à esquerda e links à direita */
  padding: 0 20px;
}

.logo {
  width: 100px; /* Ajuste conforme o tamanho do logo */
  height: auto;
  margin-right: 20px;
}

nav {
  display: flex;
  align-items: center;
}

nav a {
  font-size: 20px;
  font-weight: bold;
  color: #FFFFFF; /* Links em branco para contraste com o fundo azul */
  margin: 0 15px;
  text-decoration: none;
  transition: color 0.3s;
}

nav a:hover {
  color: #F4B400; /* Amarelo do logo ao passar o mouse */
}

nav a.router-link-exact-active {
  color: #F4B400; /* Amarelo do logo para o link ativo */
}
</style>