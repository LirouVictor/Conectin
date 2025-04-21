package com.conectin.conectin.controllers;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.bcrypt.BCrypt;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.conectin.conectin.config.JwtUtil;
import com.conectin.conectin.dto.UsuarioDto;
import com.conectin.conectin.entities.Usuario;
import com.conectin.conectin.exception.CustomException;
import com.conectin.conectin.exception.ErrorMessages;
import com.conectin.conectin.payload.SuccessMessage;
import com.conectin.conectin.services.UsuarioService;

import jakarta.validation.Valid;

import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.security.crypto.bcrypt.BCrypt;

@RestController
@RequestMapping("/api/usuarios")
public class UsuarioController {

    @Autowired
    private UsuarioService usuarioService;

    @Autowired
    private JwtUtil jwtUtil;

    @PostMapping("/cadastrar")
    public ResponseEntity<?> cadastrarUsuario(@Valid @RequestBody UsuarioDto usuarioDto) {
        try {
            Usuario usuario = usuarioService.cadastrarUsuario(usuarioDto);
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(new SuccessMessage("Usuário cadastrado com sucesso: " + usuario.getNome(), "USER_SUCCESS_002"));
        } catch (IllegalArgumentException e) {
            if (e.getMessage().contains("e-mail já existe")) {
                throw new CustomException(ErrorMessages.EMAIL_ALREADY_EXISTS, ErrorMessages.EMAIL_ALREADY_EXISTS_CODE);
            } else if (e.getMessage().contains("e-mail inválido")) {
                throw new CustomException(ErrorMessages.INVALID_EMAIL, ErrorMessages.INVALID_EMAIL_CODE);
            } else if (e.getMessage().contains("usuário já existe")) {
                throw new CustomException(ErrorMessages.USER_ALREADY_EXISTS, ErrorMessages.USER_ALREADY_EXISTS_CODE);
            }
            throw new CustomException("Erro ao cadastrar usuário", "USER_ERROR_006");
        }
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody UsuarioDto usuarioDto) {
        try {
            // Validação básica
            String email = usuarioDto.getEmail();
            String senha = usuarioDto.getSenha();
            if (email == null || senha == null) {
                throw new CustomException("Email e senha são obrigatórios", "USER_ERROR_001");
            }
    
            // Buscar usuário pelo email
            Usuario usuario = usuarioService.findByEmail(email);
            if (usuario == null) {
                System.out.println("Usuário não encontrado para o email: " + email);
                throw new CustomException("Usuário não encontrado", "USER_ERROR_002");
            }
    
            // Comparar a senha com o hash
            boolean isPasswordValid = BCrypt.checkpw(senha, usuario.getSenha());
            if (!isPasswordValid) {
                throw new CustomException("Senha incorreta", "USER_ERROR_003");
            }
    
            // Gerar token
            String token = jwtUtil.generateToken(usuario.getEmail());
            return ResponseEntity.ok(new SuccessMessage("Login bem-sucedido! Token: " + token, "AUTH_SUCCESS_001"));
        } catch (CustomException e) {
            throw e; // Propaga a CustomException para ser tratada pelo handler global
        } catch (Exception e) {
            throw new CustomException("Erro no servidor: " + e.getMessage(), "SERVER_ERROR_001");
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getUsuarioById(@PathVariable Long id, @RequestHeader("Authorization") String token) {
        if (token == null || !token.startsWith("Bearer ")) {
            throw new CustomException(ErrorMessages.INVALID_TOKEN, ErrorMessages.INVALID_TOKEN_CODE);
        }

        String jwtToken = token.substring(7);
        String username = jwtUtil.extractUsername(jwtToken);

        if (!jwtUtil.validateToken(jwtToken, username)) {
            throw new CustomException(ErrorMessages.EXPIRED_TOKEN, ErrorMessages.EXPIRED_TOKEN_CODE);
        }

        Usuario usuario = usuarioService.findById(id);
        if (usuario == null) {
            throw new CustomException(ErrorMessages.USER_NOT_FOUND, ErrorMessages.USER_NOT_FOUND_CODE);
        }
        return ResponseEntity.ok(new SuccessMessage("Usuário encontrado: " + usuario.getNome(), "USER_SUCCESS_001"));
    }

    @PutMapping("/editar/{id}")
    public ResponseEntity<?> editarUsuario(@PathVariable Long id, @Valid @RequestBody UsuarioDto usuarioDto, @RequestHeader("Authorization") String token) {
        if (token == null || !token.startsWith("Bearer ")) {
            throw new CustomException(ErrorMessages.INVALID_TOKEN, ErrorMessages.INVALID_TOKEN_CODE);
        }

        String jwtToken = token.substring(7);
        String username = jwtUtil.extractUsername(jwtToken);

        if (!jwtUtil.validateToken(jwtToken, username)) {
            throw new CustomException(ErrorMessages.EXPIRED_TOKEN, ErrorMessages.EXPIRED_TOKEN_CODE);
        }

        try {
            Optional<Usuario> usuarioEditado = usuarioService.editarUsuario(id, usuarioDto);
            if (usuarioEditado.isEmpty()) {
                throw new CustomException(ErrorMessages.USER_NOT_FOUND, ErrorMessages.USER_NOT_FOUND_CODE);
            }
            return ResponseEntity.ok(new SuccessMessage("Usuário editado com sucesso: " + usuarioEditado.get().getNome(), "USER_SUCCESS_003"));
        } catch (IllegalArgumentException e) {
            if (e.getMessage().contains("e-mail já existe")) {
                throw new CustomException(ErrorMessages.EMAIL_ALREADY_EXISTS, ErrorMessages.EMAIL_ALREADY_EXISTS_CODE);
            } else if (e.getMessage().contains("e-mail inválido")) {
                throw new CustomException(ErrorMessages.INVALID_EMAIL, ErrorMessages.INVALID_EMAIL_CODE);
            }
            throw new CustomException("Erro ao editar usuário", "USER_ERROR_007");
        }
    }

    @DeleteMapping("/deletar/{id}")
    public ResponseEntity<?> deletarUsuario(@PathVariable Long id, @RequestHeader("Authorization") String token) {
        if (token == null || !token.startsWith("Bearer ")) {
            throw new CustomException(ErrorMessages.INVALID_TOKEN, ErrorMessages.INVALID_TOKEN_CODE);
        }

        String jwtToken = token.substring(7);
        String username = jwtUtil.extractUsername(jwtToken);

        if (!jwtUtil.validateToken(jwtToken, username)) {
            throw new CustomException(ErrorMessages.EXPIRED_TOKEN, ErrorMessages.EXPIRED_TOKEN_CODE);
        }

        boolean deletado = usuarioService.deletarUsuario(id);
        if (!deletado) {
            throw new CustomException(ErrorMessages.USER_NOT_FOUND, ErrorMessages.USER_NOT_FOUND_CODE);
        }
        return ResponseEntity.ok(new SuccessMessage("Usuário deletado com sucesso", "USER_SUCCESS_004"));
    }

    @GetMapping("/listar")
    public ResponseEntity<?> listarUsuarios(@RequestHeader("Authorization") String token) {
        if (token == null || !token.startsWith("Bearer ")) {
            throw new CustomException(ErrorMessages.INVALID_TOKEN, ErrorMessages.INVALID_TOKEN_CODE);
        }

        String jwtToken = token.substring(7);
        String username = jwtUtil.extractUsername(jwtToken);

        if (!jwtUtil.validateToken(jwtToken, username)) {
            throw new CustomException(ErrorMessages.EXPIRED_TOKEN, ErrorMessages.EXPIRED_TOKEN_CODE);
        }

        List<Usuario> usuarios = usuarioService.listarUsuarios();
        return ResponseEntity.ok(usuarios);
    }
}