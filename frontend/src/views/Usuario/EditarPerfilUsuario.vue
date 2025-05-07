<template>
  <div class="editar-perfil-container">
    <div class="editar-perfil-box">
      <h1>Editar Perfil</h1>
      <form @submit.prevent="salvarPerfil">
        <!-- Dados Gerais -->
        <div class="input-group">
          <label for="nome">Nome:</label>
          <input v-model="usuario.nome" type="text" id="nome" required class="input-field" />
        </div>
        <div class="input-group">
          <label for="endereco">Endereço:</label>
          <input v-model="usuario.endereco" type="text" id="endereco" required class="input-field" />
        </div>
        <div class="input-group">
          <label for="email">Email:</label>
          <input v-model="usuario.email" type="email" id="email" required class="input-field" />
        </div>
        <div class="input-group">
          <label for="fotoPerfil">Foto de Perfil:</label>
          <input type="file" id="fotoPerfil" accept="image/*" @change="handleFotoUpload" class="input-field" />
          <img v-if="usuario.fotoPerfil" :src="usuario.fotoPerfil" alt="Foto de Perfil" class="foto-preview" />
        </div>

        <!-- Dados do Prestador -->
        <div v-if="usuario.prestador" class="prestador-section">
          <h3>Dados do Prestador</h3>
          <div class="input-group">
            <label for="descricao">Descrição:</label>
            <textarea v-model="usuario.descricao" id="descricao" class="input-field"></textarea>
          </div>
          <div class="input-group">
            <label for="disponibilidade">Disponibilidade:</label>
            <input v-model="usuario.disponibilidade" type="text" id="disponibilidade" class="input-field" />
          </div>

          <!-- Categorias (Serviços) -->
          <div class="input-group">
            <label>Categorias (Serviços):</label>
            <select multiple v-model="usuario.categoriasSelecionadas" class="input-field">
              <option v-for="categoria in categoriasDisponiveis" :key="categoria.id" :value="categoria.id">
                {{ categoria.nome }}
              </option>
            </select>
          </div>

          <!-- Cidades Atendidas -->
          <div class="input-group">
            <label>Cidades Atendidas:</label>
            <div class="cidades-selecionadas">
              <div v-if="usuario.cidadesSelecionadas.length === 0" class="sem-cidades">
                Nenhuma cidade selecionada.
              </div>
              <div v-else class="cidade-item" v-for="cidadeId in usuario.cidadesSelecionadas" :key="cidadeId">
                <span>{{ getCidadeNome(cidadeId) }}</span>
                <button type="button" @click="removerCidade(cidadeId)" class="remove-btn">Remover</button>
              </div>
            </div>
            <div class="adicionar-cidade">
              <select v-model="novaCidade" class="input-field">
                <option value="" disabled>Selecione uma cidade</option>
                <option v-for="cidade in cidadesDisponiveis" :key="cidade.id" :value="cidade.id">
                  {{ cidade.nome }}
                </option>
              </select>
              <button type="button" @click="adicionarCidade" :disabled="!novaCidade" class="add-btn">Adicionar Cidade</button>
            </div>
          </div>

          <!-- Portfólio -->
          <div class="portfolio-section">
            <h4>Portfólio</h4>
            <div v-for="(item, index) in usuario.portfolios" :key="index" class="portfolio-item">
              <input type="file" accept="image/*" @change="handlePortfolioUpload($event, index)" class="input-field" />
              <input v-model="item.descricao" type="text" placeholder="Descrição" class="input-field" />
              <button type="button" @click="removerPortfolio(index)" class="remove-btn">Remover</button>
              <img v-if="item.urlImagem" :src="item.urlImagem" alt="Portfolio" class="portfolio-preview" />
            </div>
            <button type="button" @click="adicionarPortfolio" class="add-btn">Adicionar Item ao Portfólio</button>
          </div>
        </div>

        <!-- Tipos de Usuário -->
        <div class="input-group">
          <label>Tipo de Usuário:</label>
          <div class="checkbox-group">
            <div>
              <input type="checkbox" id="prestador" v-model="usuario.prestador" />
              <label for="prestador">Prestador</label>
            </div>
            <div>
              <input type="checkbox" id="cliente" v-model="usuario.cliente" />
              <label for="cliente">Cliente</label>
            </div>
          </div>
        </div>

        <!-- Alterar Senha -->
        <div class="input-group">
          <button type="button" @click="toggleAlterarSenha" class="alterar-senha-btn">
            {{ alterarSenhaVisivel ? 'Cancelar Alteração de Senha' : 'Alterar Senha' }}
          </button>
        </div>
        <div v-if="alterarSenhaVisivel" class="senha-section">
          <div class="input-group">
            <label for="senhaAtual">Senha Atual:</label>
            <input v-model="usuario.senhaAtual" type="password" id="senhaAtual" required class="input-field" />
          </div>
          <div class="input-group">
            <label for="senha">Nova Senha:</label>
            <input v-model="usuario.senha" type="password" id="senha" required class="input-field" />
          </div>
          <div class="input-group">
            <label for="confirmarSenha">Confirmar Nova Senha:</label>
            <input v-model="usuario.confirmarSenha" type="password" id="confirmarSenha" required class="input-field" />
          </div>
        </div>

        <button type="submit" class="save-btn">Salvar Perfil</button>
      </form>
    </div>
  </div>
</template>

<script>
import api from '@/services/api';
import { useToast } from 'vue-toastification';
import { useUserStore } from '@/stores/user';
import { useRouter } from 'vue-router';

export default {
  name: 'EditarPerfil',
  setup() {
    const toast = useToast();
    const userStore = useUserStore();
    const router = useRouter();
    return { toast, userStore, router };
  },
  data() {
    return {
      usuario: {
        nome: '',
        endereco: '',
        email: '',
        prestador: false, // Booleano
        cliente: false,   // Booleano
        fotoPerfil: null, // Pode ser string (URL/base64) ou File object temporariamente
        descricao: '',
        disponibilidade: '',
        portfolios: [], // Lista de objetos { id?, urlImagem: string, descricao: string }
        categoriasSelecionadas: [], // Lista de IDs
        cidadesSelecionadas: [],    // Lista de IDs
        senhaAtual: '',
        senha: '',
        confirmarSenha: '',
      },
      categoriasDisponiveis: [], // Lista de objetos {id, nome, ...}
      cidadesDisponiveis: [],    // Lista de objetos {id, nome, ...}
      novaCidade: '',
      alterarSenhaVisivel: false,
      fotoPerfilFile: null, // Para armazenar o objeto File da foto de perfil
      portfolioFiles: {},   // Para armazenar os objetos File do portfólio { index: File }
    };
  },
  mounted() {
    this.carregarPerfil();
    this.carregarCategorias();
    this.carregarCidades();
  },
  methods: {
    async carregarPerfil() {
      try {
        const response = await api.get('/usuarios/perfil', { // Ajuste o endpoint se necessário
          headers: { Authorization: `Bearer ${localStorage.getItem('token')}` },
        });
        const data = response.data;

        // Popula o estado local 'usuario' com os dados do backend
        this.usuario.nome = data.nome;
        this.usuario.endereco = data.endereco;
        this.usuario.email = data.email;
        this.usuario.prestador = data.prestador; // Assume que o backend retorna 'prestador' como booleano
        this.usuario.cliente = data.cliente;   // Assume que o backend retorna 'cliente' como booleano
        this.usuario.fotoPerfil = data.fotoPerfil; // Assume que o backend retorna a URL da foto

        // Campos específicos do prestador, que no DTO estão na raiz
        this.usuario.descricao = data.descricao || '';
        this.usuario.disponibilidade = data.disponibilidade || '';
        this.usuario.portfolios = data.portfolios?.map(p => ({ ...p, urlImagem: p.urlImagem || p.imagemUrl || '' })) || []; // Ajuste nome do campo se necessário
        this.usuario.categoriasSelecionadas = data.categorias?.map(c => c.id) || [];
        this.usuario.cidadesSelecionadas = data.cidades?.map(c => c.id) || [];

        // Limpa campos de senha
        this.usuario.senhaAtual = '';
        this.usuario.senha = '';
        this.usuario.confirmarSenha = '';

      } catch (error) {
        console.error('Erro ao carregar perfil:', error);
        this.toast.error('Erro ao carregar perfil.');
      }
    },
    async carregarCategorias() {
      try {
        const response = await api.get('/categorias');
        this.categoriasDisponiveis = response.data;
      } catch (error) {
        this.toast.error('Erro ao carregar categorias.');
      }
    },
    async carregarCidades() {
      try {
        const response = await api.get('/cidades');
        this.cidadesDisponiveis = response.data;
      } catch (error) {
        this.toast.error('Erro ao carregar cidades.');
      }
    },
    handleFotoUpload(event) {
      const file = event.target.files[0];
      if (file) {
        this.fotoPerfilFile = file; // Armazena o File object
        const reader = new FileReader();
        reader.onload = (e) => {
          this.usuario.fotoPerfil = e.target.result; // Preview como base64
        };
        reader.readAsDataURL(file);
      } else {
        this.fotoPerfilFile = null;
        this.usuario.fotoPerfil = null; // Ou a URL anterior se quiser manter
      }
    },
    handlePortfolioUpload(event, index) {
      const file = event.target.files[0];
      if (file) {
        this.portfolioFiles[index] = file; // Armazena o File object
        const reader = new FileReader();
        reader.onload = (e) => {
          // Garante que o objeto portfolio no índice exista
          if (!this.usuario.portfolios[index]) {
            this.usuario.portfolios[index] = { id: null, urlImagem: '', descricao: '' };
          }
          this.usuario.portfolios[index].urlImagem = e.target.result; // Preview como base64
        };
        reader.readAsDataURL(file);
      } else {
        delete this.portfolioFiles[index];
         if (this.usuario.portfolios[index]) {
            // Poderia resetar a urlImagem ou manter a anterior se a edição for cancelada
            // this.usuario.portfolios[index].urlImagem = ''; // Ou valor original
         }
      }
    },
    adicionarPortfolio() {
      this.usuario.portfolios.push({ id: null, urlImagem: '', descricao: '' });
    },
    removerPortfolio(index) {
      this.usuario.portfolios.splice(index, 1);
      delete this.portfolioFiles[index]; // Remove o arquivo associado se houver
    },
    toggleAlterarSenha() {
      this.alterarSenhaVisivel = !this.alterarSenhaVisivel;
      if (!this.alterarSenhaVisivel) {
        this.usuario.senhaAtual = '';
        this.usuario.senha = '';
        this.usuario.confirmarSenha = '';
      }
    },
    adicionarCidade() {
      if (this.novaCidade && !this.usuario.cidadesSelecionadas.includes(this.novaCidade)) {
        this.usuario.cidadesSelecionadas.push(this.novaCidade);
        this.novaCidade = '';
      } else if (this.novaCidade) {
        this.toast.info('Esta cidade já foi adicionada.');
      } else {
         this.toast.error('Selecione uma cidade para adicionar.');
      }
    },
    removerCidade(cidadeId) {
      this.usuario.cidadesSelecionadas = this.usuario.cidadesSelecionadas.filter(id => id !== cidadeId);
    },
    getCidadeNome(cidadeId) {
      const cidade = this.cidadesDisponiveis.find(c => c.id === cidadeId);
      return cidade ? cidade.nome : 'Carregando...';
    },

    async salvarPerfil() {
      if (!this.usuario.prestador && !this.usuario.cliente) {
        this.toast.error('Selecione pelo menos um tipo de usuário (Prestador ou Cliente).');
        return;
      }

      if (this.alterarSenhaVisivel) {
        if (!this.usuario.senhaAtual) {
          this.toast.error('A senha atual é obrigatória para alterar a senha.');
          return;
        }
        if (!this.usuario.senha) {
          this.toast.error('A nova senha é obrigatória.');
          return;
        }
        if (this.usuario.senha.length < 6) { // Exemplo de validação
            this.toast.error('A nova senha deve ter pelo menos 6 caracteres.');
            return;
        }
        if (this.usuario.senha !== this.usuario.confirmarSenha) {
          this.toast.error('A nova senha e a confirmação de senha não coincidem.');
          return;
        }
      }

      // O backend espera um JSON, não FormData diretamente para este DTO.
      // Se você fosse enviar arquivos como File objects, precisaria usar FormData.
      // Como fotoPerfil e portfolio.urlImagem são base64 strings (do FileReader),
      // eles podem ir no JSON. Se o backend espera File, a lógica aqui precisaria mudar para FormData.

      const dadosParaEnviar = {
        nome: this.usuario.nome,
        endereco: this.usuario.endereco,
        email: this.usuario.email,
        fotoPerfil: this.usuario.fotoPerfil, // Envia a string base64 ou URL existente

        prestador: this.usuario.prestador, // Booleano
        cliente: this.usuario.cliente,     // Booleano

        // Campos que, no DTO, estão na raiz, mas são relevantes para o prestador
        descricao: this.usuario.prestador ? this.usuario.descricao : null,
        disponibilidade: this.usuario.prestador ? this.usuario.disponibilidade : null,
        
        // Mapeia IDs para objetos {id: valor} como esperado pelo backend para List<CategoriaDto>, etc.
        categorias: this.usuario.prestador ? this.usuario.categoriasSelecionadas.map(id => ({ id: id })) : [],
        cidades: this.usuario.prestador ? this.usuario.cidadesSelecionadas.map(id => ({ id: id })) : [],
        
        // Portfolios: envia a lista de objetos com urlImagem (base64 ou URL) e descricao
        // Se o portfolio item tiver um 'id' (vindo do carregarPerfil), mantenha-o para o backend saber se é update ou new.
        portfolios: this.usuario.prestador ? this.usuario.portfolios.map(p => ({
            id: p.id || null, // Envia o ID se existir (para update)
            urlImagem: p.urlImagem, // String base64 ou URL
            descricao: p.descricao
        })) : [],
      };

      if (this.alterarSenhaVisivel) {
        dadosParaEnviar.senhaAtual = this.usuario.senhaAtual;
        dadosParaEnviar.senha = this.usuario.senha;
        dadosParaEnviar.confirmarSenha = this.usuario.confirmarSenha;
      } else {
        // Não envie campos de senha se não estiver alterando,
        // para evitar validações desnecessárias no backend.
        // O UsuarioDto os tem, mas o backend pode tratá-los como opcionais
        // se não forem para alteração de senha.
        // Se o backend exigir que sejam nulos, envie:
        // dadosParaEnviar.senhaAtual = null;
        // dadosParaEnviar.senha = null;
        // dadosParaEnviar.confirmarSenha = null;
      }

      console.log("Dados a enviar para o backend:", JSON.stringify(dadosParaEnviar, null, 2));

      try {
        // Use o ID do usuário logado do store para o endpoint
        if (!this.userStore.user || !this.userStore.user.id) {
            this.toast.error("ID do usuário não encontrado. Faça login novamente.");
            return;
        }
        const userId = this.userStore.user.id;

        const response = await api.put(`/usuarios/editar/${userId}`, dadosParaEnviar, {
          headers: {
            Authorization: `Bearer ${localStorage.getItem('token')}`,
            'Content-Type': 'application/json', // Importante para payload JSON
          },
        });

        // Atualizar o userStore e localStorage com os dados que FORAM enviados e confirmados
        // (ou com a resposta do backend se ela retornar o objeto atualizado)
        const perfilAtualizadoParaStore = {
            nome: dadosParaEnviar.nome,
            email: dadosParaEnviar.email,
            endereco: dadosParaEnviar.endereco,
            fotoPerfil: dadosParaEnviar.fotoPerfil, // pode ser base64, idealmente o backend retornaria a URL final
            prestador: dadosParaEnviar.prestador,
            cliente: dadosParaEnviar.cliente,
            // ... outros campos que o userStore precise e que foram atualizados
        };
        this.userStore.setUser({ ...this.userStore.user, ...perfilAtualizadoParaStore }); // Assumindo que setUser existe no store

        this.toast.success(response.data.message || 'Perfil salvo com sucesso!');
        this.router.push({ name: 'Perfil' }); // Ou para onde for apropriado

      } catch (error) {
        console.error("Erro ao salvar perfil:", error.response || error);
        const errorMessage = error.response?.data?.message ||
                             (error.response?.data?.errors ? Object.values(error.response.data.errors).join(', ') : null) ||
                             error.response?.data?.error ||
                             'Erro ao salvar perfil. Verifique os dados e tente novamente.';
        this.toast.error(errorMessage);
      }
    },
  },
};
</script>

<style scoped>
.editar-perfil-container {
  max-width: 600px;
  margin: 50px auto;
}

.editar-perfil-box {
  padding: 20px;
  border: 1px solid #ccc;
  border-radius: 5px;
  box-shadow: 0 2px 5px rgba(0, 0, 0, 0.1);
}

.input-group {
  margin-bottom: 15px;
}

.input-field {
  width: 100%;
  padding: 8px;
  border: 1px solid #ccc;
  border-radius: 4px;
}

.checkbox-group {
  display: flex;
  gap: 20px;
}

.foto-preview, .portfolio-preview {
  max-width: 100px;
  margin-top: 10px;
}

.prestador-section {
  margin-top: 20px;
}

.senha-section {
  margin-top: 20px;
}

.portfolio-section {
  margin-top: 20px;
}

.portfolio-item {
  margin-bottom: 15px;
  display: flex;
  flex-direction: column;
  gap: 10px;
}

.cidades-selecionadas {
  margin-bottom: 10px;
}

.sem-cidades {
  color: #888;
}

.cidade-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 5px;
  border-bottom: 1px solid #eee;
}

.adicionar-cidade {
  display: flex;
  gap: 10px;
  align-items: center;
}

.remove-btn {
  background-color: #dc3545;
  color: white;
  border: none;
  padding: 5px 10px;
  border-radius: 4px;
  cursor: pointer;
}

.remove-btn:hover {
  background-color: #c82333;
}

.add-btn {
  background-color: #28a745;
  color: white;
  border: none;
  padding: 8px 15px;
  border-radius: 4px;
  cursor: pointer;
}

.add-btn:hover {
  background-color: #218838;
}

.add-btn:disabled {
  background-color: #6c757d;
  cursor: not-allowed;
}

.alterar-senha-btn {
  background-color: #6c757d;
  color: white;
  border: none;
  padding: 8px 15px;
  border-radius: 4px;
  cursor: pointer;
}

.alterar-senha-btn:hover {
  background-color: #5a6268;
}

.save-btn {
  width: 100%;
  padding: 10px;
  background-color: #007bff;
  color: white;
  border: none;
  border-radius: 4px;
  cursor: pointer;
}

.save-btn:hover {
  background-color: #0056b3;
}
</style>