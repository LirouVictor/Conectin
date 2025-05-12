<template>
  <div class="editar-perfil-container">
    <div class="editar-perfil-box">
      <h1>Editar Perfil</h1>
      <form @submit.prevent="salvarPerfil">
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

          <div class="input-group">
            <label>Categorias (Serviços):</label>
            <div class="categorias-selecionadas">
              <div v-if="usuario.categoriasSelecionadas.length === 0" class="sem-categorias">
                Nenhuma categoria selecionada.
              </div>
              <div v-else class="categoria-item" v-for="categoriaId in usuario.categoriasSelecionadas" :key="categoriaId">
                <span>{{ getCategoriaNome(categoriaId) }}</span>
                <button type="button" @click="removerCategoria(categoriaId)" class="remove-btn">Remover</button>
              </div>
            </div>
            <div class="adicionar-categoria">
              <select v-model="novaCategoria" class="input-field">
                <option value="" disabled>Selecione uma categoria</option>
                <option v-for="categoria in categoriasDisponiveis" :key="categoria.id" :value="categoria.id">
                  {{ categoria.nome }}
                </option>
              </select>
              <button type="button" @click="adicionarCategoria" :disabled="!novaCategoria" class="add-btn">Adicionar
                Categoria</button>
            </div>
          </div>

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
              <button type="button" @click="adicionarCidade" :disabled="!novaCidade" class="add-btn">Adicionar
                Cidade</button>
            </div>
          </div>

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
import { useUserStore } from '@/stores/user'; // Seu store de usuário
import { useRouter } from 'vue-router'; // Se precisar de navegação

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
        fotoPerfil: null,
        prestador: false, // Booleano que indica se é prestador
        cliente: false,   // Booleano que indica se é cliente
        // Campos específicos do prestador
        descricao: '',
        disponibilidade: '',
        portfolios: [], // Lista de objetos { id?, urlImagem: string, descricao: string }
        categoriasSelecionadas: [], // Lista de IDs de categorias
        cidadesSelecionadas: [],    // Lista de IDs de cidades
        // Campos para alteração de senha
        senhaAtual: '',
        senha: '',
        confirmarSenha: '',
      },
      categoriasDisponiveis: [], // Para popular o <select> de categorias
      cidadesDisponiveis: [],    // Para popular o <select> de cidades
      novaCidade: '',            // v-model para adicionar nova cidade
      novaCategoria: '',         // v-model para adicionar nova categoria
      alterarSenhaVisivel: false,
      fotoPerfilFile: null,      // Armazena o File object da foto de perfil para upload
      portfolioFiles: {},        // Armazena File objects do portfólio { index: File }
    };
  },
  mounted() {
    this.carregarPerfil();
    this.carregarCategorias(); // Carrega opções para o select de categorias
    this.carregarCidades();    // Carrega opções para o select de cidades
  },
  methods: {
    async carregarPerfil() {
      try {
        const response = await api.get('/usuarios/perfil', {
          headers: { Authorization: `Bearer ${localStorage.getItem('token')}` },
        });
        const data = response.data;

        this.usuario.nome = data.nome || '';
        this.usuario.endereco = data.endereco || '';
        this.usuario.email = data.email || '';
        this.usuario.fotoPerfil = data.foto || null;

        const isPrestador = data.tipos?.includes('PRESTADOR') || (data.prestador && typeof data.prestador === 'object');
        const isCliente = data.tipos?.includes('CLIENTE') || false;

        this.usuario.prestador = isPrestador;
        this.usuario.cliente = isCliente;

        if (this.usuario.prestador && data.prestador && typeof data.prestador === 'object') {
          const prestadorData = data.prestador;

          this.usuario.descricao = prestadorData.descricao || '';
          this.usuario.disponibilidade = prestadorData.disponibilidade || '';
          this.usuario.portfolios = (prestadorData.portfolios || []).map(p => ({
            id: p.id || null,
            urlImagem: p.urlImagem || p.imagemUrl || '',
            descricao: p.descricao || ''
          }));
          this.usuario.categoriasSelecionadas = (prestadorData.categorias || []).map(cat => cat.id);
          this.usuario.cidadesSelecionadas = (prestadorData.cidades || []).map(cid => cid.id);
        } else {
          this.usuario.descricao = '';
          this.usuario.disponibilidade = '';
          this.usuario.portfolios = [];
          this.usuario.categoriasSelecionadas = [];
          this.usuario.cidadesSelecionadas = [];
        }

        this.usuario.senhaAtual = '';
        this.usuario.senha = '';
        this.usuario.confirmarSenha = '';
        this.alterarSenhaVisivel = false;

      } catch (error) {
        console.error('Erro ao carregar perfil:', error.response || error);
        this.toast.error(error.response?.data?.message || 'Não foi possível carregar os dados do perfil.');
      }
    },

    async carregarCategorias() {
      try {
        const response = await api.get('/categorias');
        this.categoriasDisponiveis = response.data;
      } catch (error) {
        console.error('Erro ao carregar categorias:', error.response || error);
        this.toast.error('Erro ao carregar lista de categorias.');
      }
    },

    async carregarCidades() {
      try {
        const response = await api.get('/cidades');
        this.cidadesDisponiveis = response.data;
      } catch (error) {
        console.error('Erro ao carregar cidades:', error.response || error);
        this.toast.error('Erro ao carregar lista de cidades.');
      }
    },

    handleFotoUpload(event) {
      const file = event.target.files[0];
      if (file) {
        this.fotoPerfilFile = file;
        const reader = new FileReader();
        reader.onload = (e) => {
          this.usuario.fotoPerfil = e.target.result;
        };
        reader.readAsDataURL(file);
      } else {
        this.fotoPerfilFile = null;
      }
    },

    handlePortfolioUpload(event, index) {
      const file = event.target.files[0];
      if (file) {
        this.portfolioFiles[index] = file;
        const reader = new FileReader();
        reader.onload = (e) => {
          if (!this.usuario.portfolios[index]) {
            this.usuario.portfolios[index] = { id: null, urlImagem: '', descricao: '' };
          }
          this.usuario.portfolios[index].urlImagem = e.target.result;
        };
        reader.readAsDataURL(file);
      } else {
        delete this.portfolioFiles[index];
      }
    },

    adicionarPortfolio() {
      this.usuario.portfolios.push({ id: null, urlImagem: '', descricao: '' });
    },

    removerPortfolio(index) {
      this.usuario.portfolios.splice(index, 1);
      delete this.portfolioFiles[index];
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
      return cidade ? cidade.nome : 'ID: ' + cidadeId;
    },

    // Métodos para Categorias
    adicionarCategoria() {
      if (this.novaCategoria && !this.usuario.categoriasSelecionadas.includes(this.novaCategoria)) {
        this.usuario.categoriasSelecionadas.push(this.novaCategoria); // novaCategoria é o ID
        this.novaCategoria = ''; // Limpa o select de adicionar categoria
      } else if (this.novaCategoria) {
        this.toast.info('Esta categoria já foi adicionada.');
      } else {
        this.toast.error('Selecione uma categoria para adicionar.');
      }
    },

    removerCategoria(categoriaId) {
      this.usuario.categoriasSelecionadas = this.usuario.categoriasSelecionadas.filter(id => id !== categoriaId);
    },

    getCategoriaNome(categoriaId) {
      const categoria = this.categoriasDisponiveis.find(c => c.id === categoriaId);
      return categoria ? categoria.nome : 'ID: ' + categoriaId; // Fallback se a categoria não for encontrada
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
        if (this.usuario.senha.length < 6) {
          this.toast.error('A nova senha deve ter pelo menos 6 caracteres.');
          return;
        }
        if (this.usuario.senha !== this.usuario.confirmarSenha) {
          this.toast.error('A nova senha e a confirmação de senha não coincidem.');
          return;
        }
      }

      const dadosParaEnviar = {
        nome: this.usuario.nome,
        endereco: this.usuario.endereco,
        email: this.usuario.email,
        fotoPerfil: this.usuario.fotoPerfil, // Assumindo que o backend aceita base64 ou URL
        prestador: this.usuario.prestador,
        cliente: this.usuario.cliente,
        descricao: this.usuario.prestador ? this.usuario.descricao : null,
        disponibilidade: this.usuario.prestador ? this.usuario.disponibilidade : null,
        categorias: this.usuario.prestador ? this.usuario.categoriasSelecionadas.map(id => ({ id })) : [],
        cidades: this.usuario.prestador ? this.usuario.cidadesSelecionadas.map(id => ({ id })) : [],
        portfolios: this.usuario.prestador ? this.usuario.portfolios.map(p => ({
          id: p.id || null,
          urlImagem: p.urlImagem,
          descricao: p.descricao
        })) : [],
      };

      if (this.alterarSenhaVisivel) {
        dadosParaEnviar.senhaAtual = this.usuario.senhaAtual;
        dadosParaEnviar.senha = this.usuario.senha;
      }

      console.log("Dados que seriam enviados para o backend:", JSON.stringify(dadosParaEnviar, null, 2));

      try {
        if (!this.userStore.user || !this.userStore.user.id) {
          this.toast.error("ID do usuário não encontrado. Por favor, faça login novamente.");
          return;
        }
        const userId = this.userStore.user.id;

        // Lembre-se da lógica de FormData se estiver enviando arquivos reais
        const response = await api.put(`/usuarios/editar/${userId}`, dadosParaEnviar, {
          headers: {
            Authorization: `Bearer ${localStorage.getItem('token')}`,
            'Content-Type': 'application/json',
          },
        });

        this.userStore.setUser({
          ...this.userStore.user,
          nome: dadosParaEnviar.nome,
          email: dadosParaEnviar.email,
          endereco: dadosParaEnviar.endereco,
          fotoPerfil: response.data.fotoPerfilUrl || dadosParaEnviar.fotoPerfil,
          prestador: dadosParaEnviar.prestador,
          cliente: dadosParaEnviar.cliente,
        });

        this.toast.success(response.data.message || 'Perfil atualizado com sucesso!');
        // this.router.push({ name: 'PerfilUsuario' });

      } catch (error) {
        console.error("Erro ao salvar perfil:", error.response || error);
        const errorMessage = error.response?.data?.message ||
          (error.response?.data?.errors ? Object.values(error.response.data.errors).join(', ') : null) ||
          error.response?.data?.error ||
          'Falha ao atualizar o perfil. Verifique os dados e tente novamente.';
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
  font-family: 'Inter', sans-serif; /* Adicionando uma fonte mais moderna */
}

.editar-perfil-box {
  padding: 30px; /* Aumentando o padding */
  border: 1px solid #e0e0e0; /* Borda mais suave */
  border-radius: 8px; /* Bordas mais arredondadas */
  background-color: #fff; /* Fundo branco */
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.08); /* Sombra mais suave */
}

.editar-perfil-box h1 {
  text-align: center;
  margin-bottom: 25px;
  color: #333;
  font-size: 1.8em;
}

.input-group {
  margin-bottom: 20px; /* Aumentando o espaçamento */
}

.input-group label {
  display: block;
  margin-bottom: 8px; /* Espaçamento entre label e input */
  font-weight: 500; /* Peso da fonte */
  color: #555;
}

.input-field {
  width: 100%;
  padding: 12px; /* Padding maior para melhor toque */
  border: 1px solid #ccc;
  border-radius: 6px; /* Bordas arredondadas */
  box-sizing: border-box; /* Para incluir padding e borda na largura total */
  transition: border-color 0.3s ease; /* Transição suave */
}

.input-field:focus {
  border-color: #007bff; /* Cor da borda ao focar */
  outline: none; /* Remove outline padrão */
  box-shadow: 0 0 0 0.2rem rgba(0, 123, 255, 0.25); /* Sombra ao focar */
}

textarea.input-field {
  min-height: 100px; /* Altura mínima para textareas */
  resize: vertical; /* Permite redimensionamento vertical */
}

.checkbox-group {
  display: flex;
  gap: 25px; /* Espaçamento maior */
  align-items: center;
}
.checkbox-group div {
  display: flex;
  align-items: center;
}
.checkbox-group input[type="checkbox"] {
  margin-right: 8px;
  width: 18px; /* Tamanho do checkbox */
  height: 18px; /* Tamanho do checkbox */
  accent-color: #007bff; /* Cor do checkbox */
}
.checkbox-group label {
  margin-bottom: 0; /* Remove margem inferior da label do checkbox */
  font-weight: normal;
}


.foto-preview,
.portfolio-preview {
  max-width: 120px; /* Tamanho maior para preview */
  height: auto;
  margin-top: 12px;
  border-radius: 6px; /* Bordas arredondadas para preview */
  border: 1px solid #ddd;
  padding: 4px;
  background-color: #f9f9f9;
}

.prestador-section,
.senha-section,
.portfolio-section {
  margin-top: 30px; /* Espaçamento maior entre seções */
  padding-top: 20px;
  border-top: 1px solid #eee; /* Linha divisória */
}

.prestador-section h3,
.portfolio-section h4 {
  margin-bottom: 20px;
  color: #333;
}

.portfolio-item {
  margin-bottom: 20px;
  display: flex;
  flex-direction: column;
  gap: 12px;
  padding: 15px;
  border: 1px solid #e7e7e7;
  border-radius: 6px;
  background-color: #fdfdfd;
}

.cidades-selecionadas,
.categorias-selecionadas { /* Aplicando estilo também para categorias */
  margin-bottom: 15px; /* Aumentando margem inferior */
  padding: 10px;
  border: 1px solid #eee;
  border-radius: 6px;
  background-color: #f9f9f9;
  min-height: 40px; /* Altura mínima para indicar que é uma área */
}

.sem-cidades,
.sem-categorias { /* Aplicando estilo também para categorias */
  color: #777; /* Cor mais suave */
  font-style: italic;
  padding: 8px 0;
}

.cidade-item,
.categoria-item { /* Aplicando estilo também para categorias */
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 8px 10px; /* Padding ajustado */
  border-bottom: 1px solid #e0e0e0;
  background-color: #fff;
  border-radius: 4px;
  margin-bottom: 6px; /* Espaço entre itens */
}
.cidade-item:last-child,
.categoria-item:last-child {
  border-bottom: none;
}
.cidade-item span,
.categoria-item span {
  color: #333;
}


.adicionar-cidade,
.adicionar-categoria { /* Aplicando estilo também para categorias */
  display: flex;
  gap: 12px;
  align-items: center;
  margin-top: 10px; /* Espaço acima do grupo de adicionar */
}
.adicionar-cidade .input-field,
.adicionar-categoria .input-field {
  flex-grow: 1; /* Faz o select ocupar o espaço disponível */
}


.remove-btn {
  background-color: #e74c3c; /* Cor mais viva */
  color: white;
  border: none;
  padding: 6px 12px; /* Padding ajustado */
  border-radius: 5px;
  cursor: pointer;
  font-size: 0.9em;
  transition: background-color 0.2s ease;
}

.remove-btn:hover {
  background-color: #c0392b; /* Cor mais escura no hover */
}

.add-btn {
  background-color: #2ecc71; /* Cor mais viva */
  color: white;
  border: none;
  padding: 12px 18px; /* Padding ajustado para input-field */
  border-radius: 6px;
  cursor: pointer;
  font-weight: 500;
  transition: background-color 0.2s ease, opacity 0.2s ease;
}

.add-btn:hover {
  background-color: #27ae60; /* Cor mais escura no hover */
}

.add-btn:disabled {
  background-color: #bdc3c7; /* Cinza mais claro para desabilitado */
  color: #7f8c8d;
  cursor: not-allowed;
  opacity: 0.7;
}

.alterar-senha-btn {
  background-color: #5bc0de; /* Azul informativo */
  color: white;
  border: none;
  padding: 10px 18px;
  border-radius: 6px;
  cursor: pointer;
  font-weight: 500;
  transition: background-color 0.2s ease;
  display: inline-block; /* Para que não ocupe a largura toda */
  margin-bottom: 10px; /* Espaço se a seção de senha aparecer */
}

.alterar-senha-btn:hover {
  background-color: #31b0d5;
}

.save-btn {
  width: 100%;
  padding: 14px; /* Padding maior */
  background-color: #007bff;
  color: white;
  border: none;
  border-radius: 6px;
  cursor: pointer;
  font-size: 1.1em; /* Fonte maior */
  font-weight: 500;
  transition: background-color 0.2s ease;
  margin-top: 20px; /* Espaço antes do botão de salvar */
}

.save-btn:hover {
  background-color: #0056b3;
}
</style>
