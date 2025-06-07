package com.conectin.conectin.services;

import com.conectin.conectin.dto.SolicitacaoServicoDto;
import com.conectin.conectin.entities.Categoria;
import com.conectin.conectin.entities.SolicitacaoServico;
import com.conectin.conectin.entities.StatusSolicitacao;
import com.conectin.conectin.entities.Usuario;
import com.conectin.conectin.repository.AvaliacaoRepository;
import com.conectin.conectin.repository.CategoriaRepository;
import com.conectin.conectin.repository.SolicitacaoServicoRepository;
import com.conectin.conectin.repository.UsuarioRepository;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class SolicitacaoServicoService {

        @Autowired
        private SolicitacaoServicoRepository solicitacaoServicoRepository;

        @Autowired
        private UsuarioRepository usuarioRepository;

        @Autowired
        private CategoriaRepository categoriaRepository;

        @Autowired
        private AvaliacaoRepository avaliacaoRepository;

        public SolicitacaoServico criarSolicitacao(SolicitacaoServicoDto dto) {
                Usuario cliente = usuarioRepository.findById(dto.getClienteId())
                                .orElseThrow(() -> new IllegalArgumentException("Cliente não encontrado"));
                Usuario prestador = usuarioRepository.findById(dto.getPrestadorId())
                                .orElseThrow(() -> new IllegalArgumentException("Prestador não encontrado"));
                Categoria categoria = categoriaRepository.findById(dto.getCategoriaId())
                                .orElseThrow(() -> new IllegalArgumentException("Categoria não encontrada"));

                SolicitacaoServico solicitacao = new SolicitacaoServico();
                solicitacao.setCliente(cliente);
                solicitacao.setPrestador(prestador);
                solicitacao.setCategoria(categoria);
                solicitacao.setDataSolicitacao(LocalDateTime.now());
                solicitacao.setStatus(StatusSolicitacao.PENDENTE);
                solicitacao.setDetalhes(dto.getDetalhes());

                return solicitacaoServicoRepository.save(solicitacao);
        }

        public SolicitacaoServico atualizarStatus(Integer solicitacaoId, StatusSolicitacao novoStatus) {
                SolicitacaoServico solicitacao = solicitacaoServicoRepository.findById(solicitacaoId)
                                .orElseThrow(() -> new IllegalArgumentException(
                                                "Solicitação não encontrada com ID: " + solicitacaoId));

                // Adicionar lógica de validação de transição de status se necessário
                // Ex: Não pode ir de PENDENTE para CONCLUIDA diretamente
                validarTransicaoStatus(solicitacao.getStatus(), novoStatus);

                solicitacao.setStatus(novoStatus);
                if (novoStatus == StatusSolicitacao.CONCLUIDA) {
                        solicitacao.setDataConclusao(LocalDateTime.now());
                }
                return solicitacaoServicoRepository.save(solicitacao);
        }

        private void validarTransicaoStatus(StatusSolicitacao atual, StatusSolicitacao novo) {
                // Exemplo simples de validação
                if (atual == StatusSolicitacao.PENDENTE && !(novo == StatusSolicitacao.EM_ANDAMENTO
                                || novo == StatusSolicitacao.CANCELADA || novo == StatusSolicitacao.RECUSADA)) {
                        throw new IllegalStateException("Transição de PENDENTE para " + novo + " inválida.");
                }
                if (atual == StatusSolicitacao.EM_ANDAMENTO
                                && !(novo == StatusSolicitacao.AVALIACAO || novo == StatusSolicitacao.CANCELADA)) { // Adicionar
                                                                                                                    // outras
                                                                                                                    // se
                                                                                                                    // necessário
                        throw new IllegalStateException("Transição de EM_ANDAMENTO para " + novo + " inválida.");
                }
                if (atual == StatusSolicitacao.AVALIACAO
                                && !(novo == StatusSolicitacao.CONCLUIDA || novo == StatusSolicitacao.CANCELADA)) {
                        throw new IllegalStateException("Transição de AVALIACAO para " + novo + " inválida.");
                }
        }

        public List<SolicitacaoServico> findSolicitacoesAtivasPorUsuario(Long usuarioId, String tipoUsuario) {
                Usuario usuario = usuarioRepository.findById(usuarioId)
                                .orElseThrow(() -> new IllegalArgumentException(
                                                "Usuário não encontrado: " + usuarioId));

                List<StatusSolicitacao> statusParaBuscar = List.of(
                                StatusSolicitacao.PENDENTE,
                                StatusSolicitacao.EM_ANDAMENTO,
                                StatusSolicitacao.AVALIACAO);

                List<SolicitacaoServico> solicitacoesCandidatas;

                if ("cliente".equalsIgnoreCase(tipoUsuario)) {
                        solicitacoesCandidatas = solicitacaoServicoRepository.findByClienteAndStatusIn(usuario,
                                        statusParaBuscar);
                } else if ("prestador".equalsIgnoreCase(tipoUsuario)) {
                        solicitacoesCandidatas = solicitacaoServicoRepository.findByPrestadorAndStatusIn(usuario,
                                        statusParaBuscar);
                } else {
                        return List.of();
                }

                // Filtra a lista para remover as solicitações que o usuário já avaliou
                return solicitacoesCandidatas.stream()
                                .filter(solicitacao -> {
                                        // Se o status NÃO é AVALIACAO, mantém a solicitação na lista.
                                        if (solicitacao.getStatus() != StatusSolicitacao.AVALIACAO) {
                                                return true;
                                        }
                                        // Se o status É AVALIACAO, verifica se o usuário já avaliou.
                                        // Mantém na lista APENAS se ele AINDA NÃO avaliou.
                                        boolean jaAvaliou = avaliacaoRepository.existsBySolicitacaoIdAndAvaliadorId(
                                                        solicitacao.getId(), usuarioId);
                                        return !jaAvaliou; // Retorna true (mantém) se jaAvaliou for false.
                                })
                                .collect(Collectors.toList());
        }
}