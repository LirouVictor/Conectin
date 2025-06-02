package com.conectin.conectin.controllers;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.conectin.conectin.dto.SolicitacaoServicoDto;
import com.conectin.conectin.entities.SolicitacaoServico;
import com.conectin.conectin.entities.StatusSolicitacao;
import com.conectin.conectin.services.SolicitacaoServicoService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/solicitacoes")
public class SolicitacaoServicoController {

    @Autowired
    private SolicitacaoServicoService solicitacaoServicoService;

    @PostMapping("/criar")
    public ResponseEntity<SolicitacaoServico> criarSolicitacao(@Valid @RequestBody SolicitacaoServicoDto dto) {
        try {
            SolicitacaoServico solicitacao = solicitacaoServicoService.criarSolicitacao(dto);
            return ResponseEntity.status(HttpStatus.CREATED).body(solicitacao);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(null);
        }
    }

    @PutMapping("/{id}/status")
    public ResponseEntity<SolicitacaoServico> atualizarStatusSolicitacao(
            @PathVariable Integer id,
            @RequestParam("status") StatusSolicitacao novoStatus) {
        try {
            SolicitacaoServico solicitacaoAtualizada = solicitacaoServicoService.atualizarStatus(id, novoStatus);
            return ResponseEntity.ok(solicitacaoAtualizada);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null); // Ou BAD_REQUEST dependendo do erro
        } catch (IllegalStateException e) { // Para transições de status inválidas
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(null);
        }
    }

    @GetMapping("/usuario/{usuarioId}/ativas")
    public ResponseEntity<List<SolicitacaoServico>> getSolicitacoesAtivasPorUsuario(
            @PathVariable Long usuarioId,
            @RequestParam(value = "tipoUsuario", required = true) String tipoUsuario // "cliente" ou "prestador"
    ) {
        // Validação básica do tipoUsuario
        if (!"cliente".equalsIgnoreCase(tipoUsuario) && !"prestador".equalsIgnoreCase(tipoUsuario)) {
            return ResponseEntity.badRequest().build();
        }

        List<SolicitacaoServico> solicitacoes = solicitacaoServicoService.findSolicitacoesAtivasPorUsuario(usuarioId,
                tipoUsuario);
        return ResponseEntity.ok(solicitacoes);
    }

}