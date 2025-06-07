package com.conectin.conectin.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.conectin.conectin.entities.Avaliacao;
import com.conectin.conectin.entities.Usuario;

@Repository
public interface AvaliacaoRepository extends JpaRepository<Avaliacao, Integer> {

    List<Avaliacao> findByAvaliadoId(Long avaliadoId);

    @Query("SELECT AVG(a.nota) FROM Avaliacao a WHERE a.avaliado.id = :avaliadoId")
    Float calcularMediaAvaliacoes(Long avaliadoId);

    // NOVO MÉTODO
    long countBySolicitacaoId(Integer solicitacaoId);

    /**
     * Verifica se já existe uma avaliação para uma dada solicitação feita por um
     * específico avaliador.
     * 
     * @param solicitacaoId O ID da SolicitacaoServico.
     * @param avaliadorId   O ID do Usuario que seria o avaliador.
     * @return true se a avaliação já existe, false caso contrário.
     */
    boolean existsBySolicitacaoIdAndAvaliadorId(Integer solicitacaoId, Long avaliadorId);
}