package com.conectin.conectin.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.conectin.conectin.dto.PortfolioDto;
import com.conectin.conectin.entities.Portfolio;
import com.conectin.conectin.exception.CustomException;
import com.conectin.conectin.services.PortfolioService;

import jakarta.validation.Valid;
import java.util.List;

@RestController
@RequestMapping("/api/portfolios")
public class PortfolioController {

    @Autowired
    private PortfolioService portfolioService;

    // Adicionar um novo item de portfólio para um prestador
    @PostMapping
    public ResponseEntity<PortfolioDto> adicionarPortfolio(@Valid @RequestBody PortfolioDto dto) {
        try {
            Portfolio portfolioSalvo = portfolioService.adicionarPortfolio(dto);
            return ResponseEntity.status(HttpStatus.CREATED).body(PortfolioDto.fromEntity(portfolioSalvo));
        } catch (IllegalArgumentException | CustomException e) {
            // Lançar exceção para ser capturada pelo handler global seria o ideal
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(null);
        }
    }

    // Buscar todos os itens de portfólio de um prestador
    @GetMapping("/prestador/{prestadorId}")
    public ResponseEntity<List<PortfolioDto>> buscarPorPrestador(@PathVariable Integer prestadorId) {
        List<PortfolioDto> portfolios = portfolioService.buscarPorPrestador(prestadorId);
        return ResponseEntity.ok(portfolios);
    }

    // Atualizar um item de portfólio existente
    @PutMapping("/{id}")
    public ResponseEntity<PortfolioDto> atualizarPortfolio(@PathVariable Integer id,
            @Valid @RequestBody PortfolioDto dto) {
        try {
            Portfolio portfolioAtualizado = portfolioService.atualizarPortfolio(id, dto);
            return ResponseEntity.ok(PortfolioDto.fromEntity(portfolioAtualizado));
        } catch (CustomException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
        }
    }

    // Deletar um item de portfólio
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletarPortfolio(@PathVariable Integer id) {
        try {
            portfolioService.deletarPortfolio(id);
            return ResponseEntity.noContent().build();
        } catch (CustomException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
    }
}