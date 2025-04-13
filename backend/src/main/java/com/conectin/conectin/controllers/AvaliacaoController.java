package com.conectin.conectin.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.conectin.conectin.dto.AvaliacaoDto;
import com.conectin.conectin.entities.Avaliacao;
import com.conectin.conectin.services.AvaliacaoService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/avaliacoes")
public class AvaliacaoController {

    @Autowired
    private AvaliacaoService avaliacaoService;

    @PostMapping("/criar")
    public ResponseEntity<Avaliacao> criarAvaliacao(@Valid @RequestBody AvaliacaoDto dto) {
        try {
            Avaliacao avaliacao = avaliacaoService.criarAvaliacao(dto);
            return ResponseEntity.status(HttpStatus.CREATED).body(avaliacao);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(null);
        }
    }
}