<template>
    <div class="editar-usuario">
      <h1>Editar Perfil</h1>
      <form @submit.prevent="saveProfile">
        <label>
          Nome:
          <input v-model="user.nome" type="text" required />
        </label>
        <label>
          Foto (URL):
          <input v-model="user.foto" type="url" />
        </label>
        <button type="submit">Salvar</button>
      </form>
    </div>
  </template>
  
  <script setup>
  import { useUserStore } from '../stores/user';
  import { useRouter } from 'vue-router';
  
  const userStore = useUserStore();
  const router = useRouter();
  
  const user = userStore.user || { nome: '', foto: '' };
  
  const saveProfile = () => {
    localStorage.setItem('usuarioLogado', JSON.stringify(user));
    router.push({ name: 'Home' });
  };
  </script>
  
  <style scoped>
  .editar-usuario {
    max-width: 600px;
    margin: 20px auto;
    padding: 20px;
  }
  
  form {
    display: flex;
    flex-direction: column;
    gap: 15px;
  }
  
  label {
    display: flex;
    flex-direction: column;
    font-weight: bold;
  }
  
  input {
    padding: 8px;
    border: 1px solid #ccc;
    border-radius: 4px;
  }
  
  button {
    padding: 10px;
    background-color: #257bb8;
    color: #fff;
    border: none;
    border-radius: 4px;
    cursor: pointer;
  }
  
  button:hover {
    background-color: #f4b400;
  }
  </style>