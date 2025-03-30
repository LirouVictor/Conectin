<template>
  <div>
    <h1>Página de Cadastro</h1>
    <form @submit.prevent="cadastrarUsuario">
      <div>
        <label for="nome">Nome:</label>
        <input v-model="usuario.nome" type="text" id="nome" required />
      </div>
      <div>
        <label for="endereco">Endereço:</label>
        <input v-model="usuario.endereco" type="text" id="endereco" required />
      </div>
      <div>
        <label>Tipo de Usuário:</label>
        <div>
          <input type="checkbox" id="prestador" v-model="usuario.isPrestador" />
          <label for="prestador">Prestador</label>
        </div>
        <div>
          <input type="checkbox" id="cliente" v-model="usuario.isCliente" />
          <label for="cliente">Cliente</label>
        </div>
      </div>
      <div>
        <label for="email">Email:</label>
        <input v-model="usuario.email" type="email" id="email" required />
      </div>
      <div>
        <label for="senha">Senha:</label>
        <input v-model="usuario.senha" type="password" id="senha" required />
      </div>
      <div>
        <label for="confirmarSenha">Confirmar Senha:</label>
        <input v-model="usuario.confirmarSenha" type="password" id="confirmarSenha" required />
      </div>
      <button type="submit">Cadastrar</button>
    </form>
    <p v-if="mensagemErro">{{ mensagemErro }}</p>
    <p v-if="mensagemSucesso">{{ mensagemSucesso }}</p>
  </div>
</template>

<script>
import axios from 'axios';

export default {
  name: 'RegisterPage',
  data() {
    return {
      usuario: {
        nome: '',
        endereco: '',
        isPrestador: false,
        isCliente: false,
        email: '',
        senha: '',
        confirmarSenha: '',
      },
      mensagemErro: '',
      mensagemSucesso: '',
    };
  },
  methods: {
    async cadastrarUsuario() {
      this.mensagemErro = '';
      this.mensagemSucesso = '';

      // Verificar se as senhas coincidem
      if (this.usuario.senha !== this.usuario.confirmarSenha) {
        this.mensagemErro = 'As senhas não coincidem.';
        return;
      }

      // Determinar o tipo de usuário
      const tipos = [];
      if (this.usuario.isPrestador) tipos.push('PRESTADOR');
      if (this.usuario.isCliente) tipos.push('CLIENTE');

      if (tipos.length === 0) {
        this.mensagemErro = 'Selecione pelo menos um tipo de usuário.';
        return;
      }

      try {
        // Enviar os dados para o backend
        const response = await axios.post('http://localhost:8080/api/usuarios/cadastrar', {
          nome: this.usuario.nome,
          endereco: this.usuario.endereco,
          tipo: tipos.join(','), // Envia os tipos como uma string separada por vírgula
          email: this.usuario.email,
          senha: this.usuario.senha,
          confirmarSenha: this.usuario.confirmarSenha,
        });

        // Exibir mensagem de sucesso
        this.mensagemSucesso = 'Usuário cadastrado com sucesso!';
        console.log('Resposta do backend:', response.data);
      } catch (error) {
        // Exibir mensagem de erro
        this.mensagemErro = 'Erro ao cadastrar usuário. Tente novamente.';
        console.error('Erro ao cadastrar usuário:', error);
      }
    },
  },
};
</script>