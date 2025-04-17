<template>
  <div class="register-container">
    <div class="register-box">
      <h1>Página de Cadastro</h1>
      <form @submit.prevent="cadastrarUsuario">
        <div class="input-group">
          <label for="nome">Nome:</label>
          <input v-model="usuario.nome" type="text" id="nome" required class="input-field" />
        </div>
        <div class="input-group">
          <label for="endereco">Endereço:</label>
          <input
            v-model="usuario.endereco"
            type="text"
            id="endereco"
            required
            class="input-field"
          />
        </div>
        <div class="input-group">
          <label>Tipo de Usuário:</label>
          <div class="checkbox-group">
            <div>
              <input type="checkbox" id="prestador" v-model="usuario.isPrestador" />
              <label for="prestador">Prestador</label>
            </div>
            <div>
              <input type="checkbox" id="cliente" v-model="usuario.isCliente" />
              <label for="cliente">Cliente</label>
            </div>
          </div>
        </div>
        <div class="input-group">
          <label for="email">Email:</label>
          <input v-model="usuario.email" type="email" id="email" required class="input-field" />
        </div>
        <div class="input-group">
          <label for="senha">Senha:</label>
          <input v-model="usuario.senha" type="password" id="senha" required class="input-field" />
        </div>
        <div class="input-group">
          <label for="confirmarSenha">Confirmar Senha:</label>
          <input
            v-model="usuario.confirmarSenha"
            type="password"
            id="confirmarSenha"
            required
            class="input-field"
          />
        </div>
        <button type="submit" class="register-btn">Cadastrar</button>
      </form>
      <p v-if="mensagemErro" class="error-message">{{ mensagemErro }}</p>
      <p v-if="mensagemSucesso" class="success-message">{{ mensagemSucesso }}</p>
    </div>
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
          tipo: tipos.join(','),
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

<style scoped>
.register-container {
  display: flex;
  justify-content: center;
  align-items: center;
  min-height: calc(100vh - 110px); /* Ajusta para o espaço do header */
  background-color: #f5f5f5; /* Fundo cinza claro */
}

.register-box {
  background-color: #FFFFFF;
  padding: 40px;
  border-radius: 10px;
  box-shadow: 0 4px 15px rgba(0, 0, 0, 0.1);
  width: 100%;
  max-width: 500px; /* Aumentado para acomodar os checkboxes */
  text-align: left;
  border: 2px solid #257BB8; /* Borda azul corrigida */
}

h1 {
  font-size: 2rem;
  color: #257BB8; /* Título azul */
  text-align: center;
  margin-bottom: 30px;
}

.input-group {
  margin-bottom: 20px;
}

.input-field {
  width: 94.5%;
  padding: 12px;
  font-size: 1rem;
  border: 2px solid #257BB8; /* Borda azul corrigida */
  border-radius: 5px;
  transition: border-color 0.3s;
}

.input-field:focus {
  border-color: #F4B400; /* Amarelo ao focar */
  outline: none;
}

.checkbox-group {
  display: flex;
  gap: 20px;
  margin-top: 10px;
}

.checkbox-group div {
  display: flex;
  align-items: center;
}

.checkbox-group input {
  margin-right: 5px;
}

.register-btn {
  width: 100%;
  padding: 12px;
  font-size: 1rem;
  background-color: #F4B400; /* Amarelo do logo */
  color: #FFFFFF;
  border: none;
  border-radius: 5px;
  cursor: pointer;
  transition: background-color 0.3s;
}

.register-btn:hover {
  background-color: #d9a300; /* Amarelo mais escuro ao passar o mouse */
}

.error-message {
  color: #dc3545; /* Vermelho para erro */
  text-align: center;
  margin-top: 10px;
}

.success-message {
  color: #28a745; /* Verde para sucesso */
  text-align: center;
  margin-top: 10px;
}
</style>