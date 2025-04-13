package com.conectin.conectin.controllers;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.conectin.conectin.dto.PrestadorDto;
import com.conectin.conectin.entities.Categoria;
import com.conectin.conectin.entities.Prestador;
import com.conectin.conectin.repository.CategoriaRepository;
import com.conectin.conectin.repository.PrestadorRepository;
import com.conectin.conectin.services.PrestadorService;

@RestController
@RequestMapping("/api")
public class PrestadorController {

    @Autowired
    private PrestadorService prestadorService;

    @Autowired
    private PrestadorRepository prestadorRepository;

    @Autowired
    private CategoriaRepository categoriaRepository;

    // Lista todas as categorias disponíveis
    @GetMapping("/categorias")
    public ResponseEntity<List<Categoria>> getCategorias() {
        List<Categoria> categorias = categoriaRepository.findAll();
        return ResponseEntity.ok(categorias);
    }

    // Cria um novo prestador (usado para testes ou criação direta, mas normalmente o cadastro é via UsuarioController)
    @PostMapping("/prestadores")
    public ResponseEntity<Prestador> createPrestador(@RequestBody PrestadorDto prestadorDto) {
        try {
            Prestador prestador = prestadorService.criarPrestador(prestadorDto);
            return ResponseEntity.status(HttpStatus.CREATED).body(prestador);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(null);
        }
    }

    // Lista prestadores por categoria e cidade
    @GetMapping("/prestadores")
    public ResponseEntity<List<PrestadorDto>> getPrestadoresPorCategoriaECidade(
            @RequestParam("categoriaId") Integer categoriaId,
            @RequestParam("cidadeId") Integer cidadeId) {
        List<Prestador> prestadores = prestadorService.findPrestadoresPorCategoriaECidade(categoriaId, cidadeId);
        List<PrestadorDto> prestadoresDto = prestadores.stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
        return ResponseEntity.ok(prestadoresDto);
    }

    // Método auxiliar para converter Prestador em PrestadorDto
    private PrestadorDto convertToDto(Prestador prestador) {
        PrestadorDto dto = new PrestadorDto();
        dto.setId(prestador.getId());
        dto.setUsuarioId(prestador.getUsuario().getId());
        dto.setNome(prestador.getUsuario().getNome());
        dto.setDescricao(prestador.getDescricao());
        dto.setDisponibilidade(prestador.getDisponibilidade());
        dto.setAvaliacaoMedia(prestador.getAvaliacaoMedia());
        return dto;
    }
}