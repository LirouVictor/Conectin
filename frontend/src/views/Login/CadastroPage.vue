<template>
  <div class="register-container">
    <div class="register-box">
      <h1>Faça seu Cadastro</h1>
      <form @submit.prevent="cadastrarUsuario">
        <div class="input-group">
          <label for="nome">Nome:</label>
          <input v-model="usuario.nome" type="text" id="nome" required class="input-field" />
        </div>
        <div class="input-group">
          <label for="endereco">Endereço:</label>
          <input v-model="usuario.endereco" type="text" id="endereco" required class="input-field" />
        </div>
        <div class="input-group">
          <label>Tipo de Usuário:</label>
          <div class="checkbox-group">
            <div>
              <br>
              <input type="checkbox" id="prestador" v-model="usuario.prestador" />
              <label for="prestador">Prestador</label>
            </div>
            <div>
              <br>
              <input type="checkbox" id="cliente" v-model="usuario.cliente" />
              <label for="cliente">Cliente</label>
            </div>
          </div>
        </div>
        <div class="input-group">
          <label for="email">Email:</label>
          <input v-model="usuario.email" type="email" id="email" required class="input-field" />
        </div>
        <div class="input-group">
          <label for="telefone">Telefone:</label>
          <input v-model="usuario.telefone" type="tel" id="telefone" required class="input-field"
            placeholder="Ex: (11) 98765-4321" />
        </div>

        <div class="input-group">
          <label for="senha">Senha:</label>
          <div class="password-wrapper">
            <input v-model="usuario.senha" :type="senhaFieldType" id="senha" required class="input-field" />
            <i 
              class="password-toggle-icon bi"
              :class="senhaVisivel ? 'bi-eye' : 'bi-eye-slash'"
              @mousedown="toggleVisibilidade('senha', true)"
              @mouseup="toggleVisibilidade('senha', false)"
              @mouseleave="toggleVisibilidade('senha', false)"
              title="Segure para ver a senha"
            ></i>
          </div>
        </div>

        <div class="input-group">
          <label for="confirmarSenha">Confirmar Senha:</label>
          <div class="password-wrapper">
            <input v-model="usuario.confirmarSenha" :type="confirmarSenhaFieldType" id="confirmarSenha" required class="input-field" />
            <i
              class="password-toggle-icon bi"
              :class="confirmarSenhaVisivel ? 'bi-eye' : 'bi-eye-slash'"
              @mousedown="toggleVisibilidade('confirmar', true)"
              @mouseup="toggleVisibilidade('confirmar', false)"
              @mouseleave="toggleVisibilidade('confirmar', false)"
              title="Segure para ver a senha"
            ></i>
          </div>
        </div>
        
        <button type="submit" class="register-btn">Cadastrar</button>
      </form>
    </div>
  </div>
</template>

<script>
import api from '@/services/api';
import { useToast } from 'vue-toastification';
// Se o Bootstrap Icons não for importado globalmente no seu projeto (main.js),
// esta linha garante que os ícones de olho funcionarão neste componente.
import 'bootstrap-icons/font/bootstrap-icons.css';

export default {
  name: 'CadastroPage',
  data() {
    return {
      usuario: {
        nome: '',
        endereco: '',
        prestador: false,
        cliente: false,
        email: '',
        telefone: '',
        senha: '',
        confirmarSenha: '',
      },
      // Novas propriedades para controlar a visibilidade da senha
      senhaVisivel: false,
      confirmarSenhaVisivel: false,
    };
  },
  setup() {
    const toast = useToast();
    return { toast };
  },
  computed: {
    // Propriedades computadas para mudar o tipo do input (text/password)
    senhaFieldType() {
      return this.senhaVisivel ? 'text' : 'password';
    },
    confirmarSenhaFieldType() {
      return this.confirmarSenhaVisivel ? 'text' : 'password';
    }
  },
  watch: {
    'usuario.telefone'(novoValor) {
      if (!novoValor) return '';
      let numeros = novoValor.replace(/\D/g, '');
      if (numeros.length > 11) {
        numeros = numeros.slice(0, 11);
      }
      if (numeros.length > 10) {
        numeros = numeros.replace(/(\d{2})(\d{5})(\d{4})/, '($1) $2-$3');
      } else if (numeros.length > 6) {
        numeros = numeros.replace(/(\d{2})(\d{4})(\d{0,4})/, '($1) $2-$3');
      } else if (numeros.length > 2) {
        numeros = numeros.replace(/(\d{2})(\d{0,5})/, '($1) $2');
      } else {
        if (numeros.length > 0) {
            numeros = numeros.replace(/(\d{0,2})/, '($1');
        }
      }
      if (novoValor !== numeros) {
        this.usuario.telefone = numeros;
      }
    }
  },
  methods: {
    // Novo método para alternar a visibilidade da senha
    toggleVisibilidade(campo, visivel) {
      if (campo === 'senha') {
        this.senhaVisivel = visivel;
      } else if (campo === 'confirmar') {
        this.confirmarSenhaVisivel = visivel;
      }
    },

    async cadastrarUsuario() {
      // --- Validações de frontend ---
      const erros = [];
      const senha = this.usuario.senha;
      if (this.usuario.nome.length > 50) {
        erros.push('O nome deve ter no máximo 50 caracteres.');
      }
      if (this.usuario.endereco.length > 50) {
        erros.push('O endereço deve ter no máximo 50 caracteres.');
      }
      const emailRegex = /^[^@\s]+@[^@\s]+\.[^@\s]+$/;
      if (!emailRegex.test(this.usuario.email)) {
        erros.push('Por favor, insira um formato de email válido.');
      }
      const telefoneLimpo = this.usuario.telefone.replace(/\D/g, '');
      if (telefoneLimpo.length < 10 || telefoneLimpo.length > 11) {
        erros.push('O telefone deve ter 10 ou 11 dígitos (incluindo DDD).');
      }
      if (senha.length < 8) {
        erros.push('A senha deve ter pelo menos 8 caracteres.');
      }
      if (!/[A-Z]/.test(senha)) {
        erros.push('A senha deve conter pelo menos uma letra maiúscula.');
      }
      if (!/[a-z]/.test(senha)) {
        erros.push('A senha deve conter pelo menos uma letra minúscula.');
      }
      if (!/\d/.test(senha)) {
        erros.push('A senha deve conter pelo menos um número.');
      }
      if (!/[!@#$%^&*(),.?":{}|<>]/.test(senha)) {
        erros.push('A senha deve conter pelo menos um caractere especial (ex: !@#$).');
      }
      if (this.usuario.senha !== this.usuario.confirmarSenha) {
        erros.push('As senhas não coincidem.');
      }
      if (!this.usuario.prestador && !this.usuario.cliente) {
        this.toast.error('Selecione pelo menos um tipo de usuário.');
        return;
      }
      if (erros.length > 0) {
        this.toast.error(erros[0]);
        return; 
      }
    
      try {
        const telefoneLimpoParaApi = this.usuario.telefone.replace(/\D/g, '');
        const payloadParaApi = {
          nome: this.usuario.nome,
          endereco: this.usuario.endereco,
          prestador: this.usuario.prestador,
          cliente: this.usuario.cliente,
          email: this.usuario.email,
          telefone: telefoneLimpoParaApi,
          senha: this.usuario.senha,
          confirmarSenha: this.usuario.confirmarSenha,
        };
        const response = await api.post('/usuarios/cadastrar', payloadParaApi);
        this.toast.success(response.data.message);
        this.usuario = {
          nome: '',
          endereco: '',
          prestador: false,
          cliente: false,
          email: '',
          telefone: '',
          senha: '',
          confirmarSenha: '',
        };
        setTimeout(() => {
          this.$router.push('/login');
        }, 2000);
      } catch (error) {
        if (error.response && error.response.data) {
          this.toast.error(error.response.data.message);
        } else {
          this.toast.error('Erro ao cadastrar usuário. Tente novamente.');
        }
      }
    }
  }
};
</script>

<style scoped>
.register-container {
  max-width: 500px;
  margin: 50px auto;
}

.register-box {
  padding: 20px;
  border: 1px solid #ccc;
  border-radius: 5px;
  box-shadow: 0 2px 5px rgba(0, 0, 0, 0.1);
}

.input-group {
  margin-bottom: 15px;
}

/* NOVO: Container para o input de senha e o ícone */
.password-wrapper {
  position: relative;
}

.input-field {
  width: 95.5%;
  padding: 8px;
  border: 1px solid #ccc;
  border-radius: 4px;
}

/* Adiciona um espaço à direita no input de senha para o ícone não sobrepor o texto */
.password-wrapper .input-field {
  width: 100%; /* Ocupa todo o espaço do wrapper */
  box-sizing: border-box; /* Garante que o padding não aumente a largura total */
  padding-right: 40px; /* Espaço para o ícone */
}

/* NOVO: Estilo e posicionamento do ícone de olho */
.password-toggle-icon {
  position: absolute;
  top: 50%;
  right: 10px; /* Distância da borda direita */
  transform: translateY(-50%); /* Alinhamento vertical perfeito */
  cursor: pointer;
  color: #888;
}

.password-toggle-icon:hover {
    color: #333;
}


.checkbox-group {
  display: flex;
  gap: 20px;
}

.register-btn {
  width: 100%;
  padding: 10px;
  background-color: #ffc107;
  color: white;
  font-size: large;
  border: none;
  border-radius: 4px;
  cursor: pointer;
}

.register-btn:hover {
  background-color: #257bb8;
}
</style>