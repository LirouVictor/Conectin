package com.conectin.conectin.services;

import java.time.LocalDateTime;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.conectin.conectin.dto.AvaliacaoDto;
import com.conectin.conectin.entities.Avaliacao;
import com.conectin.conectin.entities.SolicitacaoServico;
import com.conectin.conectin.entities.Usuario;
import com.conectin.conectin.repository.AvaliacaoRepository;
import com.conectin.conectin.repository.SolicitacaoServicoRepository;
import com.conectin.conectin.repository.UsuarioRepository;

@Service
public class AvaliacaoService {

    @Autowired
    private AvaliacaoRepository avaliacaoRepository;

    @Autowired
    private SolicitacaoServicoRepository solicitacaoServicoRepository;

    @Autowired
    private UsuarioRepository usuarioRepository;

    public Avaliacao criarAvaliacao(AvaliacaoDto dto) {
        SolicitacaoServico solicitacao = solicitacaoServicoRepository.findById(dto.getSolicitacaoId())
                .orElseThrow(() -> new IllegalArgumentException("Solicitação não encontrada"));
        Usuario avaliador = usuarioRepository.findById(dto.getAvaliadorId())
                .orElseThrow(() -> new IllegalArgumentException("Avaliador não encontrado"));
        Usuario avaliado = usuarioRepository.findById(dto.getAvaliadoId())
                .orElseThrow(() -> new IllegalArgumentException("Avaliado não encontrado"));

        Avaliacao avaliacao = new Avaliacao();
        avaliacao.setSolicitacao(solicitacao);
        avaliacao.setAvaliador(avaliador);
        avaliacao.setAvaliado(avaliado);
        avaliacao.setNota(dto.getNota());
        avaliacao.setComentario(dto.getComentario());
        avaliacao.setData(LocalDateTime.now());
        avaliacao.setFotos(dto.getFotos());

        return avaliacaoRepository.save(avaliacao);
    }
}