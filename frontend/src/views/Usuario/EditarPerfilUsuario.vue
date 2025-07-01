<template>
  <div class="editar-perfil-container">
    <div class="editar-perfil-box">
      <h1>Editar Perfil</h1>
      <!-- CORREÇÃO: O <form> agora envolve todos os campos relevantes -->
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
          <label for="telefone">Telefone:</label>
          <input v-model="usuario.telefone" type="tel" id="telefone" required class="input-field"
            placeholder="Ex: 11987654321" />
        </div>
        <div class="input-group">
          <label for="fotoPerfil">Foto de Perfil:</label>
          <input type="file" id="fotoPerfil" accept="image/*" @change="handleFotoUpload" class="input-field" />
          <img v-if="usuario.fotoPerfil || userStore.user?.foto" :src="usuario.fotoPerfil || userStore.user.foto"
            alt="Foto de Perfil" class="foto-preview" @error="handleImageError" />
        </div>

        <!-- Seção do Prestador -->
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

          <!-- Categorias -->
          <div class="input-group">
            <label>Categorias (Serviços):</label>
            <div class="categorias-selecionadas">
              <div v-if="usuario.categoriasSelecionadas.length === 0" class="sem-categorias">
                Nenhuma categoria selecionada.
              </div>
              <div v-else class="tag-container">
                <span v-for="categoriaId in usuario.categoriasSelecionadas" :key="categoriaId" class="tag-item">
                  <span class="tag-name">{{ getCategoriaNome(categoriaId) }}</span>
                  <span class="trash-icon" @click="removerCategoria(categoriaId)" title="Remover">
                    <svg xmlns="http://www.w3.org/2000/svg" width="14" height="14" fill="#e74c3c" viewBox="0 0 16 16">
                      <path d="M5.5 5.5A.5.5 0 0 1 6 6v6a.5.5 0 0 1-1 0V6a.5.5 0 0 1 .5-.5zm2.5 0a.5.5 0 0 1 .5.5v6a.5.5 0 0 1-1 0V6a.5.5 0 0 1 .5-.5zm3 .5a.5.5 0 0 0-1 0v6a.5.5 0 0 0 1 0V6z" />
                      <path fill-rule="evenodd" d="M14.5 3a1 1 0 0 1-1 1H13v9a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2V4h-.5a1 1 0 0 1-1-1V2a1 1 0 0 1 1-1H6a1 1 0 0 1 1-1h2a1 1 0 0 1 1 1h3.5a1 1 0 0 1 1 1v1zM4.118 4 4 4.059V13a1 1 0 0 0 1 1h6a1 1 0 0 0 1-1V4.059L11.882 4H4.118zM2.5 3V2h11v1h-11z" />
                    </svg>
                  </span>
                </span>
              </div>
            </div>
            <div class="adicionar-categoria">
              <select v-model="novaCategoria" class="input-field">
                <option value="" disabled>Selecione uma categoria</option>
                <option v-for="categoria in categoriasDisponiveis" :key="categoria.id" :value="categoria.id">
                  {{ categoria.nome }}
                </option>
              </select>
              <button type="button" @click="adicionarCategoria" :disabled="!novaCategoria" class="add-btn">Adicionar</button>
            </div>
          </div>

          <!-- Cidades -->
          <div class="input-group">
            <label>Cidades Atendidas:</label>
            <div class="cidades-selecionadas">
              <div v-if="usuario.cidadesSelecionadas.length === 0" class="sem-cidades">
                Nenhuma cidade selecionada.
              </div>
              <div v-else class="tag-container">
                <span v-for="cidadeId in usuario.cidadesSelecionadas" :key="cidadeId" class="tag-item">
                  <span class="tag-name">{{ getCidadeNome(cidadeId) }}</span>
                  <span class="trash-icon" @click="removerCidade(cidadeId)" title="Remover">
                    <svg xmlns="http://www.w3.org/2000/svg" width="14" height="14" fill="#e74c3c" viewBox="0 0 16 16">
                       <path d="M5.5 5.5A.5.5 0 0 1 6 6v6a.5.5 0 0 1-1 0V6a.5.5 0 0 1 .5-.5zm2.5 0a.5.5 0 0 1 .5.5v6a.5.5 0 0 1-1 0V6a.5.5 0 0 1 .5-.5zm3 .5a.5.5 0 0 0-1 0v6a.5.5 0 0 0 1 0V6z" />
                      <path fill-rule="evenodd" d="M14.5 3a1 1 0 0 1-1 1H13v9a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2V4h-.5a1 1 0 0 1-1-1V2a1 1 0 0 1 1-1H6a1 1 0 0 1 1-1h2a1 1 0 0 1 1 1h3.5a1 1 0 0 1 1 1v1zM4.118 4 4 4.059V13a1 1 0 0 0 1 1h6a1 1 0 0 0 1-1V4.059L11.882 4H4.118zM2.5 3V2h11v1h-11z" />
                    </svg>
                  </span>
                </span>
              </div>
            </div>
            <div class="adicionar-cidade">
              <select v-model="novaCidade" class="input-field">
                <option value="" disabled>Selecione uma cidade</option>
                <option v-for="cidade in cidadesDisponiveis" :key="cidade.id" :value="cidade.id">
                  {{ cidade.nome }}
                </option>
              </select>
              <button type="button" @click="adicionarCidade" :disabled="!novaCidade" class="add-btn">Adicionar</button>
            </div>
          </div>
        </div>
        
        <!-- Seção de Tipo de Usuário e Senha -->
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
            <input v-model="usuario.senhaAtual" type="password" id="senhaAtual" class="input-field" />
          </div>
          <div class="input-group">
            <label for="senha">Nova Senha:</label>
            <input v-model="usuario.senha" type="password" id="senha" class="input-field" />
          </div>
          <div class="input-group">
            <label for="confirmarSenha">Confirmar Nova Senha:</label>
            <input v-model="usuario.confirmarSenha" type="password" id="confirmarSenha" class="input-field" />
          </div>
        </div>

        <button type="submit" class="save-btn">Salvar Perfil</button>
      </form>

      <!-- CORREÇÃO: A seção de portfólio agora fica FORA do form principal, mas dentro da área do prestador -->
       <div v-if="usuario.prestador" class="portfolio-section">
          <h4>Portfólio de Trabalhos</h4>
          <div v-if="loadingPortfolio" class="loading-text">Carregando portfólio...</div>

          <div v-for="(item, index) in portfolios" :key="item.id || `new-${index}`" class="portfolio-item">
            <div class="portfolio-item-header">
              <h5>Item de Portfólio {{ index + 1 }}</h5>
              <button type="button" @click="removerPortfolio(item, index)" class="remove-btn-portfolio"
                title="Remover este item do portfólio">
                ×
              </button>
            </div>

            <div class="input-group">
              <label :for="`portfolio-titulo-${index}`">Título do Trabalho:</label>
              <input v-model="item.titulo" type="text" :id="`portfolio-titulo-${index}`"
                placeholder="Ex: Reforma de Cozinha" class="input-field" />
            </div>

            <div class="input-group">
              <label :for="`portfolio-desc-${index}`">Descrição:</label>
              <textarea v-model="item.descricao" :id="`portfolio-desc-${index}`"
                placeholder="Descreva o trabalho realizado" class="input-field"></textarea>
            </div>

            <div class="input-group">
              <label>Fotos:</label>
              <div class="portfolio-fotos-container">
                <div v-for="(fotoUrl, fotoIndex) in item.fotos" :key="fotoUrl" class="thumbnail-wrapper">
                  <img :src="getAbsoluteUrl(fotoUrl)" alt="Miniatura" class="thumbnail"
                    @click="ampliarImagem(fotoUrl)" />
                  <button @click="removerFoto(item, fotoIndex)" class="remove-btn-foto" title="Remover foto">×</button>
                </div>
                <div class="thumbnail-wrapper add-foto-wrapper">
                  <input type="file" :id="`portfolio-upload-${index}`" multiple accept="image/*"
                    @change="handlePortfolioUpload($event, item)" class="add-foto-input" :disabled="item.uploading" />
                  <label :for="`portfolio-upload-${index}`" class="add-foto-label">
                    <span v-if="!item.uploading">+</span>
                    <div v-if="item.uploading" class="spinner"></div>
                  </label>
                </div>
              </div>
            </div>
            <button type="button" @click="salvarItemPortfolio(item)" class="save-item-btn" :disabled="item.uploading">
              {{ item.id ? 'Atualizar Item' : 'Salvar Novo Item' }}
            </button>
          </div>

          <button type="button" @click="adicionarPortfolio" class="add-btn">
            + Adicionar Item ao Portfólio
          </button>
        </div>

    </div> <!-- Fim de .editar-perfil-box -->
  </div> <!-- Fim de .editar-perfil-container -->

  <!-- MODAL DE VISUALIZAÇÃO DE IMAGEM -->
  <div v-if="imagemAmpliadaUrl" class="modal-overlay" @click="fecharAmpliacao">
    <div class="modal-content" @click.stop>
      <span class="modal-close" @click="fecharAmpliacao">×</span>
      <img :src="getAbsoluteUrl(imagemAmpliadaUrl)" alt="Imagem Ampliada" class="imagem-ampliada" />
    </div>
  </div>
</template>

<script>
import api from '@/services/api';
import { useToast } from 'vue-toastification';
import { useUserStore } from '@/stores/user';
import { useRouter } from 'vue-router';

const BACKEND_URL = 'http://localhost:8080';

export default {
  name: 'EditarPerfilUsuario',
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
        telefone: '',
        fotoPerfil: null,
        prestador: false,
        cliente: false,
        descricao: '',
        disponibilidade: '',
        categoriasSelecionadas: [],
        cidadesSelecionadas: [],
        senhaAtual: '',
        senha: '',
        confirmarSenha: '',
      },
      // CORREÇÃO: Removidos dados duplicados daqui
      categoriasDisponiveis: [],
      cidadesDisponiveis: [],
      novaCidade: '',
      novaCategoria: '',
      alterarSenhaVisivel: false,
      fotoPerfilFile: null,

      // Dados para a nova lógica de portfólio
      prestadorId: null,
      portfolios: [],
      loadingPortfolio: false,
      imagemAmpliadaUrl: null,
    };
  },
  async mounted() {
    await this.carregarPerfil();
    this.carregarCategorias();
    this.carregarCidades();
    // A chamada para carregarPortfolio agora é feita dentro de carregarPerfil
  },
  methods: {
    getAbsoluteUrl(relativeUrl) {
      if (!relativeUrl || relativeUrl.startsWith('http') || relativeUrl.startsWith('data:image')) {
        return relativeUrl;
      }
      return `${BACKEND_URL}${relativeUrl}`;
    },

    async carregarPerfil() {
      try {
        const response = await api.get('/usuarios/perfil');
        const data = response.data;

        this.usuario.nome = data.nome || '';
        this.usuario.endereco = data.endereco || '';
        this.usuario.email = data.email || '';
        this.usuario.telefone = data.telefone || '';
        this.usuario.fotoPerfil = null;

        this.usuario.prestador = data.tipos?.includes('PRESTADOR');
        this.usuario.cliente = data.tipos?.includes('CLIENTE');

        if (this.usuario.prestador && data.prestador) {
          this.prestadorId = data.prestador.id; // Define o ID do prestador
          const prestadorData = data.prestador;
          this.usuario.descricao = prestadorData.descricao || '';
          this.usuario.disponibilidade = prestadorData.disponibilidade || '';
          this.usuario.categoriasSelecionadas = (prestadorData.categorias || []).map(cat => cat.id);
          this.usuario.cidadesSelecionadas = (prestadorData.cidades || []).map(cid => cid.id);
          
          this.carregarPortfolio(); // Chama o carregamento do portfólio aqui
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
        this.toast.error('Erro ao carregar lista de categorias.');
      }
    },

    async carregarCidades() {
      try {
        const response = await api.get('/cidades');
        this.cidadesDisponiveis = response.data;
      } catch (error) {
        this.toast.error('Erro ao carregar lista de cidades.');
      }
    },

    async carregarPortfolio() {
      if (!this.prestadorId) return;
      this.loadingPortfolio = true;
      try {
        const response = await api.get(`/portfolios/prestador/${this.prestadorId}`);
        this.portfolios = response.data.map(p => ({ ...p, uploading: false }));
      } catch (error) {
        this.toast.error("Erro ao carregar o portfólio.");
      } finally {
        this.loadingPortfolio = false;
      }
    },

    ampliarImagem(url) {
      this.imagemAmpliadaUrl = url;
    },
    fecharAmpliacao() {
      this.imagemAmpliadaUrl = null;
    },

    // CORREÇÃO: Mantendo apenas a versão correta de adicionarPortfolio
    adicionarPortfolio() {
      this.portfolios.push({
        id: null,
        titulo: '',
        descricao: '',
        fotos: [],
        prestadorId: this.prestadorId,
        uploading: false,
      });
    },

    // CORREÇÃO: Mantendo apenas a versão correta de removerPortfolio
    async removerPortfolio(item, index) {
      if (!confirm('Tem certeza que deseja remover este item do portfólio?')) return;
      if (item.id) {
        try {
          await api.delete(`/portfolios/${item.id}`);
          this.toast.success("Item do portfólio removido.");
        } catch (error) {
          this.toast.error("Erro ao remover o item do portfólio.");
          return;
        }
      }
      this.portfolios.splice(index, 1);
    },

    removerFoto(item, fotoIndex) {
      if (!confirm('Remover esta foto?')) return;
      item.fotos.splice(fotoIndex, 1);
      if (item.id) {
        this.salvarItemPortfolio(item, false);
      }
    },

    async handlePortfolioUpload(event, item) {
      const files = event.target.files;
      if (!files.length) return;
      item.uploading = true;
      try {
        for (const file of files) {
          const formData = new FormData();
          formData.append('file', file);
          const response = await api.post('/upload/foto', formData, {
            headers: { 'Content-Type': 'multipart/form-data' }
          });
          item.fotos.push(response.data);
        }
      } catch (error) {
        this.toast.error("Erro no upload de uma ou mais fotos.");
      } finally {
        item.uploading = false;
        event.target.value = '';
      }
    },

    async salvarItemPortfolio(item, showSuccessToast = true) {
      if (!item.titulo || !this.prestadorId) {
        this.toast.error("O título do trabalho é obrigatório.");
        return;
      }
      const dto = {
        id: item.id,
        titulo: item.titulo,
        descricao: item.descricao,
        fotos: item.fotos,
        prestadorId: this.prestadorId,
      };
      try {
        if (item.id) {
          const response = await api.put(`/portfolios/${item.id}`, dto);
          Object.assign(item, response.data);
          if (showSuccessToast) this.toast.success("Item do portfólio atualizado!");
        } else {
          const response = await api.post('/portfolios', dto);
          Object.assign(item, response.data);
          if (showSuccessToast) this.toast.success("Novo item do portfólio salvo!");
        }
      } catch (error) {
        this.toast.error("Falha ao salvar o item do portfólio.");
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
        this.usuario.fotoPerfil = null;
      }
    },
    handleImageError(event) {
      event.target.src = 'https://www.gravatar.com/avatar/?d=mp';
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

    adicionarCategoria() {
      if (this.novaCategoria && !this.usuario.categoriasSelecionadas.includes(this.novaCategoria)) {
        this.usuario.categoriasSelecionadas.push(this.novaCategoria);
        this.novaCategoria = '';
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
      return categoria ? categoria.nome : 'ID: ' + categoriaId;
    },

    async salvarPerfil() {
      if (!this.usuario.prestador && !this.usuario.cliente) {
        this.toast.error('Selecione pelo menos um tipo de usuário (Prestador ou Cliente).');
        return;
      }
      if (this.alterarSenhaVisivel) {
        if (!this.usuario.senhaAtual || !this.usuario.senha) {
          this.toast.error('Para alterar a senha, a senha atual e a nova senha são obrigatórias.');
          return;
        }
        if (this.usuario.senha !== this.usuario.confirmarSenha) {
           this.toast.error('A nova senha e a confirmação não coincidem.');
           return;
        }
      }

      const dadosParaEnviar = {
        nome: this.usuario.nome,
        endereco: this.usuario.endereco,
        email: this.usuario.email,
        telefone: this.usuario.telefone,
        fotoPerfil: this.usuario.fotoPerfil,
        prestador: this.usuario.prestador,
        cliente: this.usuario.cliente,
        descricao: this.usuario.prestador ? this.usuario.descricao : null,
        disponibilidade: this.usuario.prestador ? this.usuario.disponibilidade : null,
        categorias: this.usuario.prestador ? this.usuario.categoriasSelecionadas.map(id => ({ id })) : [],
        cidades: this.usuario.prestador ? this.usuario.cidadesSelecionadas.map(id => ({ id })) : [],
      };

      if (this.alterarSenhaVisivel) {
        dadosParaEnviar.senhaAtual = this.usuario.senhaAtual;
        dadosParaEnviar.senha = this.usuario.senha;
      }

      try {
        const userId = this.userStore.user.id;
        const response = await api.put(`/usuarios/editar/${userId}`, dadosParaEnviar);

        this.userStore.setUser({
          nome: dadosParaEnviar.nome,
          email: dadosParaEnviar.email,
          endereco: dadosParaEnviar.endereco,
          telefone: dadosParaEnviar.telefone,
          foto: response.data.fotoPerfilUrl,
          prestador: dadosParaEnviar.prestador,
          cliente: dadosParaEnviar.cliente,
        });

        this.toast.success(response.data.message || 'Perfil atualizado com sucesso!');
        this.router.push({ name: 'EditarPerfilUsuario', params: { id: userId } });
      } catch (error) {
        this.toast.error(error.response?.data?.message || 'Falha ao atualizar o perfil.');
      }
    },
  },
};
</script>

<style scoped>

.editar-perfil-container {
  background-color: #f7f9fc;
  display: flex;
  justify-content: center;
  align-items: flex-start;
  max-width: 100%;
  min-height: calc(100vh - 80px);
  padding: 40px 20px;
  font-family: 'Roboto', 'Arial', sans-serif;
}

.editar-perfil-box {
  width: 50%;
  padding: 30px;
  background-color: #FFFFFF;
  border-radius: 12px;
  box-shadow: 0 4px 20px rgba(26, 119, 181, 0.15);
  position: relative;
  overflow: hidden;
}

.editar-perfil-box h1 {
  text-align: center;
  margin-bottom: 25px;
  color: #333;
  font-size: 1.8em;
}

.input-group {
  margin-bottom: 20px;
}

.input-group label {
  display: block;
  margin-bottom: 8px;
  font-weight: 500;
  color: #555;
}

.input-field {
  width: 100%;
  padding: 14px 16px;
  border: 1px solid #E0E6ED;
  border-radius: 8px;
  font-size: 16px;
  box-sizing: border-box;
  transition: border-color 0.3s, box-shadow 0.3s;
}

.input-field::placeholder {
  color: #aab5c0;
}

.input-field:focus {
  outline: none;
  border-color: #F8B617; /* **MUDANÇA:** Borda de foco amarela */
  box-shadow: 0 0 0 4px rgba(248, 182, 23, 0.2); /* Sombra de foco amarela */
}


textarea.input-field {
  min-height: 100px;
  resize: vertical;
}

.checkbox-group {
  display: flex;
  gap: 25px;
  align-items: center;
}

.checkbox-group div {
  display: flex;
  align-items: center;
}

.checkbox-group input[type="checkbox"] {
  margin-right: 8px;
  width: 18px;
  height: 18px;
  accent-color: #007bff;
}

.checkbox-group label {
  margin-bottom: 0;
  font-weight: normal;
}

.foto-preview {
  max-width: 120px;
  height: auto;
  margin-top: 12px;
  border-radius: 6px;
  border: 1px solid #ddd;
  padding: 4px;
  background-color: #f9f9f9;
}

.prestador-section,
.senha-section {
  margin-top: 30px;
  padding-top: 20px;
  border-top: 1px solid #eee;
}

.prestador-section h3 {
  margin-bottom: 20px;
  color: #333;
}

.cidades-selecionadas,
.categorias-selecionadas {
  margin-bottom: 15px;
  padding: 10px;
  border: 1px solid #eee;
  border-radius: 6px;
  background-color: #f9f9f9;
  min-height: 40px;
  display: flex;
  flex-wrap: wrap;
  align-items: center;
  gap: 8px;
}

.sem-cidades,
.sem-categorias {
  color: #777;
  font-style: italic;
  padding: 8px 0;
}

.tag-container {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
}

.tag-item {
  display: inline-flex;
  align-items: center;
  background-color: #fff;
  border: 1px solid #e0e0e0;
  border-radius: 16px;
  padding: 6px 12px;
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.05);
  font-size: 0.9em;
  color: #333;
}

.tag-name {
  margin-right: 8px;
}

.trash-icon {
  cursor: pointer;
  display: inline-flex;
  align-items: center;
  justify-content: center;
}

.trash-icon:hover svg {
  opacity: 0.8;
}

.adicionar-cidade,
.adicionar-categoria {
  display: flex;
  gap: 12px;
  align-items: center;
  margin-top: 10px;
}

.adicionar-cidade .input-field,
.adicionar-categoria .input-field {
  flex-grow: 1;
}

.add-btn {
  width: 100%;
  padding: 15px;
  background-color: #1E7AC5;
  color: #FFFFFF;
  font-size: 16px;
  font-weight: bold;
  border: none;
  border-radius: 8px;
  cursor: pointer;
  transition: all 0.3s ease;
  margin-top: 10px;
}

.add-btn:hover {
  background-color: #F8B617; 
  transform: translateY(-3px);
  box-shadow: 0 4px 15px rgba(30, 122, 197, 0.3);
  color: #fff;
  text-decoration: none;
  transform: translateY(-2px);
}

.alterar-senha-btn {
  width: 20%;
  padding: 15px;
  background-color: #F8B617; /* **MUDANÇA PRINCIPAL:** Botão de login é amarelo */
  color: #FFFFFF;
  font-size: 16px;
  font-weight: bold;
  border: none;
  border-radius: 8px;
  cursor: pointer;
  transition: all 0.3s ease;
  margin-top: 10px;
}

.alterar-senha-btn:hover {
  background-color: #1E7AC5; /* **MUDANÇA:** Hover state agora é o azul principal */
  transform: translateY(-3px);
  box-shadow: 0 4px 15px rgba(30, 122, 197, 0.3);
  color: #fff;
  text-decoration: none;
  transform: translateY(-2px);
}

.save-btn {
  width: 100%;
  padding: 15px;
  background-color: #1E7AC5; 
  color: #FFFFFF;
  font-size: 16px;
  font-weight: bold;
  border: none;
  border-radius: 8px;
  cursor: pointer;
  transition: all 0.3s ease;
  margin-top: 10px;
}

.save-btn:hover {
  background-color: #F8B617;
  transform: translateY(-3px);
  box-shadow: 0 4px 15px rgba(30, 122, 197, 0.3);
  color: #fff;
  text-decoration: none;
  transform: translateY(-2px);
}

/* CSS para a nova Seção de Portfólio */
.portfolio-section {
  margin-top: 30px;
  padding-top: 20px;
  border-top: 1px solid #eee;
}
.portfolio-section h4 {
  margin-bottom: 20px;
  color: #333;
}

.portfolio-item {
  margin-bottom: 25px;
  padding: 20px;
  border: 1px solid #e7e7e7;
  border-radius: 8px;
  background-color: #fdfdfd;
  position: relative;
}

.portfolio-item-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    border-bottom: 1px solid #eee;
    padding-bottom: 10px;
    margin-bottom: 15px;
}

.portfolio-item-header h5 {
    margin: 0;
    font-size: 1.1em;
    color: #444;
}

.remove-btn-portfolio {
    background: transparent;
    border: none;
    color: #e74c3c;
    font-size: 1.8em;
    font-weight: bold;
    cursor: pointer;
    line-height: 1;
    padding: 0 5px;
}
.remove-btn-portfolio:hover {
    color: #c0392b;
}

.portfolio-fotos-container {
  display: flex;
  flex-wrap: wrap;
  gap: 15px;
}
.thumbnail-wrapper {
  position: relative;
  width: 100px;
  height: 100px;
}
.thumbnail {
  width: 100%;
  height: 100%;
  object-fit: cover;
  border-radius: 8px;
  border: 1px solid #ddd;
  cursor: pointer;
  transition: transform 0.2s;
}
.thumbnail:hover {
  transform: scale(1.05);
}

.remove-btn-foto {
  position: absolute;
  top: -5px;
  right: -5px;
  background-color: #e74c3c;
  color: white;
  border: none;
  border-radius: 50%;
  width: 22px;
  height: 22px;
  font-size: 14px;
  font-weight: bold;
  line-height: 22px;
  text-align: center;
  cursor: pointer;
  box-shadow: 0 2px 4px rgba(0,0,0,0.2);
}

.add-foto-wrapper {
  border: 2px dashed #ccc;
  border-radius: 8px;
  display: flex;
  align-items: center;
  justify-content: center;
}
.add-foto-input {
  display: none;
}
.add-foto-label {
  width: 100%;
  height: 100%;
  display: flex;
  align-items: center;
  justify-content: center;
  cursor: pointer;
  color: #aaa;
  font-size: 2.5em;
  font-weight: 300;
}
.add-foto-label:hover {
  background-color: #f9f9f9;
  color: #888;
}

.spinner {
    border: 4px solid rgba(0, 0, 0, 0.1);
    width: 36px;
    height: 36px;
    border-radius: 50%;
    border-left-color: #007bff;
    animation: spin 1s ease infinite;
}
@keyframes spin {
  0% { transform: rotate(0deg); }
  100% { transform: rotate(360deg); }
}

.save-item-btn {
  width: 100%;
  padding: 15px;
  background-color: #F8B617; /* **MUDANÇA PRINCIPAL:** Botão de login é amarelo */
  color: #FFFFFF;
  font-size: 16px;
  font-weight: bold;
  border: none;
  border-radius: 8px;
  cursor: pointer;
  transition: all 0.3s ease;
  margin-top: 10px;
}

.save-item-btn:hover {
  background-color: #1E7AC5; /* **MUDANÇA:** Hover state agora é o azul principal */
  transform: translateY(-3px);
  box-shadow: 0 4px 15px rgba(30, 122, 197, 0.3);
  color: #fff;
  text-decoration: none;
  transform: translateY(-2px);
}

/* Estilos do Modal de Imagem */
.modal-overlay {
  position: fixed;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  background-color: rgba(0, 0, 0, 0.8);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 1000;
}
.modal-content {
  position: relative;
  max-width: 90vw;
  max-height: 90vh;
}
.imagem-ampliada {
  display: block;
  max-width: 100%;
  max-height: 90vh;
  border-radius: 5px;
}
.modal-close {
  position: absolute;
  top: -15px;
  right: -15px;
  color: #fff;
  font-size: 2em;
  cursor: pointer;
  background: rgba(0,0,0,0.5);
  border-radius: 50%;
  width: 30px;
  height: 30px;
  line-height: 30px;
  text-align: center;
}
</style>