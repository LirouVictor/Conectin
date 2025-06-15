package com.conectin.conectin.controllers;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.conectin.conectin.dto.AvaliacaoDto;
import com.conectin.conectin.dto.AvaliacaoResponseDto;
import com.conectin.conectin.entities.Avaliacao;
import com.conectin.conectin.entities.Prestador;
import com.conectin.conectin.entities.Usuario;
import com.conectin.conectin.repository.AvaliacaoRepository;
import com.conectin.conectin.repository.PrestadorRepository;
import com.conectin.conectin.services.AvaliacaoService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/avaliacoes")
public class AvaliacaoController {

    @Autowired
    private AvaliacaoService avaliacaoService;

    @Autowired
    private AvaliacaoRepository avaliacaoRepository;

    @Autowired
    private PrestadorRepository prestadorRepository;

    @PostMapping("/criar")
    public ResponseEntity<Avaliacao> criarAvaliacao(@Valid @RequestBody AvaliacaoDto dto) {
        try {
            Avaliacao avaliacao = avaliacaoService.criarAvaliacao(dto);
            return ResponseEntity.status(HttpStatus.CREATED).body(avaliacao);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(null);
        }
    }

    @GetMapping("/usuario/{usuarioId}")
    public ResponseEntity<List<Avaliacao>> listarAvaliacoesPorUsuario(@PathVariable Long usuarioId) {
        List<Avaliacao> avaliacoes = avaliacaoRepository.findByAvaliadoId(usuarioId);
        return ResponseEntity.ok(avaliacoes);
    }

    @GetMapping("/prestador/{prestadorId}")
    public ResponseEntity<List<AvaliacaoResponseDto>> getAvaliacoesPorPrestador(@PathVariable Integer prestadorId) {
        Prestador prestador = prestadorRepository.findById(prestadorId)
                .orElseThrow(() -> new IllegalArgumentException("Prestador não encontrado com o ID: " + prestadorId));

        Long usuarioAvaliadoId = prestador.getUsuario().getId();
        List<Avaliacao> avaliacoes = avaliacaoRepository.findByAvaliadoId(usuarioAvaliadoId);
        List<AvaliacaoResponseDto> dtos = avaliacoes.stream()
                .map(this::convertToResponseDto)
                .collect(Collectors.toList());
        return ResponseEntity.ok(dtos);
    }

    private AvaliacaoResponseDto convertToResponseDto(Avaliacao avaliacao) {
        AvaliacaoResponseDto dto = new AvaliacaoResponseDto();
        Usuario avaliador = avaliacao.getAvaliador();

        dto.setNomeAvaliador(avaliador.getNome());
        dto.setFotoAvaliador(avaliador.getFotoPerfil()); // O frontend cuidará de adicionar a URL base
        dto.setComentario(avaliacao.getComentario());
        dto.setNota(avaliacao.getNota());
        dto.setData(avaliacao.getData());

        return dto;
    }
}