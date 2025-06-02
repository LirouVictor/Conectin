package com.conectin.conectin.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.conectin.conectin.entities.SolicitacaoServico;
import com.conectin.conectin.entities.StatusSolicitacao;
import com.conectin.conectin.entities.Usuario;

public interface SolicitacaoServicoRepository extends JpaRepository<SolicitacaoServico, Integer> {
    List<SolicitacaoServico> findByClienteAndStatusIn(Usuario cliente, List<StatusSolicitacao> statuses);

    List<SolicitacaoServico> findByPrestadorAndStatusIn(Usuario prestador, List<StatusSolicitacao> statuses); // Se
                                                                                                              // necessário
}