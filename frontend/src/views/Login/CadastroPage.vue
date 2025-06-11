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
            placeholder="Ex: 11987654321" />
        </div>
        <div class="input-group">
          <label for="senha">Senha:</label>
          <input v-model="usuario.senha" type="password" id="senha" required class="input-field" />
        </div>
        <div class="input-group">
          <label for="confirmarSenha">Confirmar Senha:</label>
          <input v-model="usuario.confirmarSenha" type="password" id="confirmarSenha" required class="input-field" />
        </div>
        <button type="submit" class="register-btn">Cadastrar</button>
      </form>
    </div>
  </div>
</template>

<script>
import api from '@/services/api';
import { useToast } from 'vue-toastification';

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
    };
  },
  setup() {
    const toast = useToast();
    return { toast };
  },
  watch: {
    'usuario.telefone'(novoValor) {
      // Esta função será chamada sempre que o telefone for alterado
      if (!novoValor) return '';
      
      // 1. Remove tudo que não for dígito
      let numeros = novoValor.replace(/\D/g, '');

      // 2. Limita a 11 dígitos para não exceder o tamanho do celular com DDD
      if (numeros.length > 11) {
        numeros = numeros.slice(0, 11);
      }

      // 3. Aplica a máscara dinamicamente
      if (numeros.length > 10) {
        // Formato (XX) XXXXX-XXXX
        numeros = numeros.replace(/(\d{2})(\d{5})(\d{4})/, '($1) $2-$3');
      } else if (numeros.length > 6) {
        // Formato (XX) XXXX-XXXX
        numeros = numeros.replace(/(\d{2})(\d{4})(\d{0,4})/, '($1) $2-$3');
      } else if (numeros.length > 2) {
        // Formato (XX) XXXXX
        numeros = numeros.replace(/(\d{2})(\d{0,5})/, '($1) $2');
      } else {
        // Formato (XX
        if (numeros.length > 0) {
            numeros = numeros.replace(/(\d{0,2})/, '($1');
        }
      }

      // 4. Atualiza o valor no v-model sem causar um loop infinito
      if (novoValor !== numeros) {
        this.usuario.telefone = numeros;
      }
    }
  },
  methods: {
    async cadastrarUsuario() {
  // --- Início da Nova Validação de Senha ---
    const erros = [];
    const senha = this.usuario.senha;

    if (this.usuario.nome.length > 50) {
      erros.push('O nome deve ter no máximo 50 caracteres.');
    }

    // 2. Validação de Endereço (máx 50 caracteres)
    if (this.usuario.endereco.length > 50) {
      erros.push('O endereço deve ter no máximo 50 caracteres.');
    }

    // 3. Validação de Email (formato)
    const emailRegex = /^[^@\s]+@[^@\s]+\.[^@\s]+$/;
    if (!emailRegex.test(this.usuario.email)) {
      erros.push('Por favor, insira um formato de email válido.');
    }

    // 4. Validação de Telefone (10 ou 11 dígitos)
    const telefoneLimpo = this.usuario.telefone.replace(/\D/g, ''); // Remove a máscara
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
      const response = await api.post('/usuarios/cadastrar', {
        nome: this.usuario.nome,
        endereco: this.usuario.endereco,
        prestador: this.usuario.prestador,
        cliente: this.usuario.cliente,
        email: this.usuario.email,
        telefone: this.usuario.telefone,
        senha: this.usuario.senha,
        confirmarSenha: this.usuario.confirmarSenha,
      });
    
      this.toast.success(response.data.message);
    
      // Limpar formulário
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

.input-field {
  width: 95.5%;
  padding: 8px;
  border: 1px solid #ccc;
  border-radius: 4px;
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