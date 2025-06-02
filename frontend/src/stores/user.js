import { defineStore } from 'pinia';
import api from '@/services/api';
import { useToast } from 'vue-toastification';
import router from '@/router'; // Certifique-se que este é o caminho correto para seu arquivo router/index.js

const BACKEND_BASE_URL = 'http://localhost:8080';

export const useUserStore = defineStore('user', {
    state: () => ({
        user: null,
    }),
    actions: {

        async loadUser() {
            const token = localStorage.getItem('token');
            if (token) {
                try {
                    const response = await api.get('/usuarios/perfil', {
                        headers: { Authorization: `Bearer ${token}` },
                    });
                    this.user = {
                        id: response.data.id,
                        nome: response.data.nome,
                        email: response.data.email,
                        endereco: response.data.endereco,
                        telefone: response.data.telefone,
                        foto: response.data.foto
                            ? `${BACKEND_BASE_URL}${response.data.foto}` // Prepend backend base URL
                            : 'https://www.gravatar.com/avatar/?d=mp',
                        prestador: response.data.tipos.includes('PRESTADOR'),
                        cliente: response.data.tipos.includes('CLIENTE'),
                    };
                    // Após carregar o usuário, verificar solicitações
                    this.verificarSolicitacoesPendentes();
                } catch (error) {
                    console.error('Erro ao carregar usuário:', error);
                    this.logout();
                }
            }
        },
        setUser(userData) {
            this.user = {
                ...this.user,
                ...userData,
                foto: userData.foto
                    ? userData.foto.startsWith('http')
                        ? userData.foto // Already an absolute URL
                        : `${BACKEND_BASE_URL}${userData.foto}` // Prepend backend base URL
                    : this.user?.foto || 'https://www.gravatar.com/avatar/?d=mp',
            };
            // Após definir/atualizar o usuário (ex: após login ou edição de perfil), verificar solicitações
            this.verificarSolicitacoesPendentes();
        },
        logout() {
            this.user = null;
            localStorage.removeItem('token');
            // Não precisa remover 'usuarioLogado' se você não o está usando mais no authStore para popular userStore
        },

        async verificarSolicitacoesPendentes() {
            if (!this.user || !this.user.id || !localStorage.getItem('token') || this.mostrarModalNotificacao) {
                // Não verifica se o modal já está ativo ou se não há usuário
                return;
            }

            let tipoUsuarioParaConsulta = this.user.cliente ? 'cliente' : null;
            if (!tipoUsuarioParaConsulta) return;

            try {
                const response = await api.get(`/solicitacoes/usuario/${this.user.id}/ativas?tipoUsuario=${tipoUsuarioParaConsulta}`, {
                    headers: { Authorization: `Bearer ${localStorage.getItem('token')}` }
                });
                const solicitacoesAtivas = response.data;

                if (solicitacoesAtivas && solicitacoesAtivas.length > 0) {
                    for (const solicitacao of solicitacoesAtivas) {
                        const jaProcessadaNestaSessao = sessionStorage.getItem(`solicitacao_notif_fechada_${solicitacao.id}`);
                        if (!jaProcessadaNestaSessao) {
                            this.solicitacaoParaNotificar = solicitacao;
                            this.mostrarModalNotificacao = true;
                            // Não marcar como processada aqui, marcar quando o usuário interagir com o modal
                            break;
                        }
                    }
                }
            } catch (error) {
                console.error("Erro ao verificar solicitações pendentes:", error.response?.data || error.message);
            }
        },

        // Nova action para lidar com a resposta do modal
        async lidarComAcaoNotificacao(acao) {
            const toast = useToast();
            const { type, solicitacao } = acao;

            // Marcar como "vista" para não reaparecer na mesma sessão se o usuário apenas fechar/adiar
            sessionStorage.setItem(`solicitacao_notif_fechada_${solicitacao.id}`, 'true');
            this.mostrarModalNotificacao = false; // Fecha o modal
            this.solicitacaoParaNotificar = null;

            try {
                switch (type) {
                    case 'CONTRATOU':
                        await api.put(`/solicitacoes/${solicitacao.id}/status?status=EM_ANDAMENTO`, {}, { /* headers */ });
                        toast.success(`Serviço com ${solicitacao.prestador.nome} marcado como EM ANDAMENTO.`);
                        break;
                    case 'NAO_CONTRATOU':
                        // Poderia ter um passo intermediário aqui para perguntar se quer cancelar
                        await api.put(`/solicitacoes/${solicitacao.id}/status?status=CANCELADA`, {}, { /* headers */ });
                        toast.info(`Solicitação para ${solicitacao.prestador.nome} foi CANCELADA.`);
                        break;
                    case 'CONCLUIU':
                        await api.put(`/solicitacoes/${solicitacao.id}/status?status=AVALIACAO`, {}, { /* headers */ });
                        toast.info(`Serviço com ${solicitacao.prestador.nome} aguardando sua avaliação.`);
                        router.push({ name: 'PaginaDeAvaliacao', params: { solicitacaoId: solicitacao.id } });
                        break;
                    case 'AINDA_NAO_CONCLUIU':
                        toast.info(`Serviço com ${solicitacao.prestador.nome} permanece EM ANDAMENTO.`);
                        sessionStorage.removeItem(`solicitacao_notif_fechada_${solicitacao.id}`); // Permite reaparecer
                        break;
                    case 'AVALIAR_AGORA':
                        router.push({ name: 'PaginaDeAvaliacao', params: { solicitacaoId: solicitacao.id } });
                        break;
                    case 'AVALIAR_DEPOIS':
                        toast.info(`Avaliação do serviço com ${solicitacao.prestador.nome} adiada.`);
                        sessionStorage.removeItem(`solicitacao_notif_fechada_${solicitacao.id}`); // Permite reaparecer
                        break;
                    default:
                        console.warn("Tipo de ação desconhecida:", type);
                }
            } catch (err) {
                console.error(`Erro ao processar ação ${type}:`, err.response?.data || err.message);
                toast.error('Ocorreu um erro ao processar sua resposta.');
                sessionStorage.removeItem(`solicitacao_notif_fechada_${solicitacao.id}`); // Permite reaparecer se falhar
            }

            // Após uma interação, verificar se há mais alguma pendência (mas não imediatamente para evitar loop)
            setTimeout(() => {
                this.verificarSolicitacoesPendentes();
            }, 1000); // Pequeno delay
        },

        fecharModalNotificacao() {
            if (this.solicitacaoParaNotificar) {
                // Marca como "vista" para não reaparecer imediatamente se o usuário fechar pelo 'Esc' ou 'X'
                sessionStorage.setItem(`solicitacao_notif_fechada_${this.solicitacaoParaNotificar.id}`, 'true');
            }
            this.mostrarModalNotificacao = false;
            this.solicitacaoParaNotificar = null;
            // Verificar se há outra após um pequeno delay
            setTimeout(() => this.verificarSolicitacoesPendentes(), 500);
        }
    },
});