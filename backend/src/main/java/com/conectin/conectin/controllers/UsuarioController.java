package com.conectin.conectin.controllers;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.bcrypt.BCrypt;
import org.springframework.web.bind.annotation.*;
import java.io.IOException;

import com.conectin.conectin.config.JwtUtil;
import com.conectin.conectin.dto.EsqueceuSenhaRequestDto;
import com.conectin.conectin.dto.ResetarSenhaRequestDto;
import com.conectin.conectin.dto.UsuarioDto;
import com.conectin.conectin.entities.Prestador;
import com.conectin.conectin.entities.Usuario;
import com.conectin.conectin.exception.CustomException;
import com.conectin.conectin.exception.ErrorMessages;
import com.conectin.conectin.payload.SuccessMessage;
import com.conectin.conectin.repository.ClienteRepository;
import com.conectin.conectin.repository.PrestadorRepository;
import com.conectin.conectin.repository.UsuarioRepository;
import com.conectin.conectin.services.EmailService;
import com.conectin.conectin.services.FileStorageService;
import com.conectin.conectin.services.ResetarSenhaTokenService;
import com.conectin.conectin.services.UsuarioService;
import com.conectin.conectin.dto.UsuarioPublicoDto;
import com.conectin.conectin.entities.TipoUsuario;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/usuarios")
public class UsuarioController {

    @Value("${app.frontend.url}")
    private String frontendUrl;

    @Autowired
    private UsuarioService usuarioService;

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private PrestadorRepository prestadorRepository;

    @Autowired
    private FileStorageService fileStorageService;

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    EmailService emailService;

    @Autowired
    ResetarSenhaTokenService resetarSenhaTokenService;

    @PostMapping("/cadastrar")
    public ResponseEntity<?> cadastrarUsuario(@Valid @RequestBody UsuarioDto usuarioDto) {
        try {
            Usuario usuario = usuarioService.cadastrarUsuario(usuarioDto);
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(new SuccessMessage("Usuário cadastrado com sucesso: " + usuario.getNome(),
                            "USER_SUCCESS_002"));
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
            String email = usuarioDto.getEmail();
            String senha = usuarioDto.getSenha();
            if (email == null || senha == null) {
                Map<String, String> errorResponse = new HashMap<>();
                errorResponse.put("message", "Email e senha são obrigatórios");
                errorResponse.put("code", "USER_ERROR_001");
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
            }

            Usuario usuario = usuarioService.findByEmail(email);
            if (usuario == null) {
                Map<String, String> errorResponse = new HashMap<>();
                errorResponse.put("message", "Usuário não encontrado");
                errorResponse.put("code", "USER_ERROR_002");
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorResponse);
            }

            boolean isPasswordValid = BCrypt.checkpw(senha, usuario.getSenha());
            if (!isPasswordValid) {
                Map<String, String> errorResponse = new HashMap<>();
                errorResponse.put("message", "Usuário ou Senha incorreta");
                errorResponse.put("code", "USER_ERROR_003");
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(errorResponse);
            }

            String token = jwtUtil.generateToken(usuario.getEmail());
            Map<String, String> successResponse = new HashMap<>();
            successResponse.put("message", "Login bem-sucedido!");
            successResponse.put("token", token);
            successResponse.put("code", "AUTH_SUCCESS_001");
            return ResponseEntity.ok(successResponse);

        } catch (Exception e) {
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("message", "Erro no servidor: " + e.getMessage());
            errorResponse.put("code", "SERVER_ERROR_001");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    @GetMapping("/perfil")
    public ResponseEntity<?> perfilUsuario(@RequestHeader("Authorization") String token) {
        if (token == null || !token.startsWith("Bearer ")) {
            throw new CustomException(ErrorMessages.INVALID_TOKEN, ErrorMessages.INVALID_TOKEN_CODE);
        }
        String jwtToken = token.substring(7);
        String username = jwtUtil.extractUsername(jwtToken);

        if (!jwtUtil.validateToken(jwtToken, username)) {
            throw new CustomException(ErrorMessages.EXPIRED_TOKEN, ErrorMessages.EXPIRED_TOKEN_CODE);
        }

        Optional<Usuario> usuarioOpt = usuarioRepository.findByEmail(username);
        if (usuarioOpt.isEmpty()) {
            throw new CustomException("Usuário não encontrado", "USER_NOT_FOUND");
        }
        Usuario usuario = usuarioOpt.get();

        Map<String, Object> response = new HashMap<>();
        response.put("id", usuario.getId());
        response.put("nome", usuario.getNome());
        response.put("endereco", usuario.getEndereco());
        response.put("email", usuario.getEmail());
        response.put("telefone", usuario.getTelefone()); // Novo campo
        response.put("foto", usuario.getFotoPerfil());
        response.put("tipos", usuario.getTipos());

        if (usuario.isPrestador()) {
            Optional<Prestador> prestadorOpt = prestadorRepository.findByUsuarioId(usuario.getId());
            if (prestadorOpt.isPresent()) {
                Prestador prestador = prestadorOpt.get();
                Map<String, Object> prestadorData = new HashMap<>();
                prestadorData.put("id", prestador.getId());
                prestadorData.put("descricao", prestador.getDescricao());
                prestadorData.put("disponibilidade", prestador.getDisponibilidade());
                prestadorData.put("avaliacaoMedia", prestador.getAvaliacaoMedia());

                List<Map<String, Object>> categoriasList = prestador.getPrestadorCategorias().stream()
                        .map(pc -> {
                            Map<String, Object> catMap = new HashMap<>();
                            catMap.put("id", pc.getCategoria().getId().longValue());
                            catMap.put("nome", pc.getCategoria().getNome());
                            return catMap;
                        })
                        .collect(Collectors.toList());
                prestadorData.put("categorias", categoriasList);

                List<Map<String, Object>> cidadesList = prestador.getCidadePrestadores().stream()
                        .map(cp -> {
                            Map<String, Object> cidMap = new HashMap<>();
                            cidMap.put("id", cp.getCidade().getId().longValue());
                            cidMap.put("nome", cp.getCidade().getNome());
                            return cidMap;
                        })
                        .collect(Collectors.toList());
                prestadorData.put("cidades", cidadesList);

                List<Map<String, Object>> portfoliosList = prestador.getPortfolios().stream()
                        .map(p -> {
                            Map<String, Object> portMap = new HashMap<>();
                            portMap.put("id", p.getId());
                            portMap.put("descricao", p.getDescricao());
                            String urlImagem = (p.getFotos() != null && !p.getFotos().isEmpty()) ? p.getFotos().get(0)
                                    : null;
                            portMap.put("urlImagem", urlImagem);
                            return portMap;
                        })
                        .collect(Collectors.toList());
                prestadorData.put("portfolios", portfoliosList);

                response.put("prestador", prestadorData);
            } else {
                response.put("prestador", null);
            }
        } else {
            response.put("prestador", null);
        }

        return ResponseEntity.ok(response);
    }

    @PutMapping("/editar/{id}")
    public ResponseEntity<?> editarUsuario(@PathVariable Long id, @Valid @RequestBody UsuarioDto usuarioDto,
            @RequestHeader("Authorization") String token) {
        if (token == null || !token.startsWith("Bearer ")) {
            throw new CustomException(ErrorMessages.INVALID_TOKEN, ErrorMessages.INVALID_TOKEN_CODE);
        }

        String jwtToken = token.substring(7);
        String username = jwtUtil.extractUsername(jwtToken);

        if (!jwtUtil.validateToken(jwtToken, username)) {
            throw new CustomException(ErrorMessages.EXPIRED_TOKEN, ErrorMessages.EXPIRED_TOKEN_CODE);
        }

        try {
            if (usuarioDto.getFotoPerfil() != null && usuarioDto.getFotoPerfil().startsWith("data:image")) {
                String fotoUrl = fileStorageService.saveFile(usuarioDto.getFotoPerfil());
                usuarioDto.setFotoPerfil(fotoUrl);
            }

            Optional<Usuario> usuarioEditado = usuarioService.editarUsuario(id, usuarioDto);
            if (usuarioEditado.isEmpty()) {
                throw new CustomException(ErrorMessages.USER_NOT_FOUND, ErrorMessages.USER_NOT_FOUND_CODE);
            }

            Map<String, Object> response = new HashMap<>();
            response.put("message", "Usuário editado com sucesso: " + usuarioEditado.get().getNome());
            response.put("code", "USER_SUCCESS_003");
            response.put("fotoPerfilUrl", usuarioEditado.get().getFotoPerfil()); // Relative path
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            if (e.getMessage().contains("e-mail já existe")) {
                throw new CustomException(ErrorMessages.EMAIL_ALREADY_EXISTS, ErrorMessages.EMAIL_ALREADY_EXISTS_CODE);
            } else if (e.getMessage().contains("e-mail inválido")) {
                throw new CustomException(ErrorMessages.INVALID_EMAIL, ErrorMessages.INVALID_EMAIL_CODE);
            }
            throw new CustomException("Erro ao editar usuário: " + e.getMessage(), "USER_ERROR_007");
        } catch (IOException e) {
            throw new CustomException("Erro ao salvar a foto de perfil: " + e.getMessage(), "FILE_ERROR_001");
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

        java.util.List<Usuario> usuarios = usuarioService.listarUsuarios();
        return ResponseEntity.ok(usuarios);
    }

    @PostMapping("/password/forgot")
    public ResponseEntity<?> forgotPassword(@Valid @RequestBody EsqueceuSenhaRequestDto requestDto) {
        try {
            Usuario usuario = usuarioService.findByEmail(requestDto.getEmail());

            if (usuario != null) {
                String token = resetarSenhaTokenService.criarTokerParaUsuario(usuario);
                // Adapte a URL base para o seu ambiente de frontend
                String resetLink = frontendUrl + "/resetar-senha?token=" + token;
                // Exemplo de URL do frontend
                emailService.enviarSenhaRecuperarEmail(usuario.getEmail(), usuario.getNome(), resetLink);
            }

            // Resposta genérica por segurança
            return ResponseEntity.ok(new SuccessMessage(
                    "Se o seu endereço de e-mail existir em nosso banco de dados, você receberá um link de recuperação de senha em breve.",
                    "PASSWORD_RESET_LINK_SENT"));

        } catch (Exception e) {
            // Log e.getMessage() ou use um logger
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new CustomException("Erro ao processar solicitação de recuperação de senha.",
                            "SERVER_ERROR_002"));
        }
    }

    @PostMapping("/password/reset")
    public ResponseEntity<?> resetPassword(@Valid @RequestBody ResetarSenhaRequestDto requestDto) {
        try {
            if (!requestDto.isSenhasCoincidem()) {
                throw new CustomException("As novas senhas não coincidem.", "VALIDATION_ERROR_002");
            }

            Optional<Usuario> usuarioOpt = resetarSenhaTokenService.validarResetarSenhaToken(requestDto.getToken());
            if (usuarioOpt.isEmpty()) {
                throw new CustomException("Token de redefinição de senha inválido ou expirado.",
                        "TOKEN_INVALID_OR_EXPIRED");
            }

            Usuario usuario = usuarioOpt.get();
            resetarSenhaTokenService.trocarSenhaUsuario(usuario, requestDto.getNovaSenha());

            return ResponseEntity
                    .ok(new SuccessMessage("Sua senha foi redefinida com sucesso.", "PASSWORD_RESET_SUCCESS"));

        } catch (CustomException e) {
            throw e; // Re-lança para o ExceptionHandler global, se houver
        } catch (Exception e) {
            // Log e.getMessage() ou use um logger
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new CustomException("Erro ao redefinir a senha.", "SERVER_ERROR_003"));
        }
    }

    @GetMapping("/publico/{id}")
    public ResponseEntity<UsuarioPublicoDto> getUsuarioPublico(@PathVariable Long id) {
        Usuario usuario = usuarioRepository.findById(id)
                .orElseThrow(() -> new CustomException("Usuário não encontrado.", "USER_NOT_FOUND"));

        UsuarioPublicoDto dto = new UsuarioPublicoDto();
        dto.setId(usuario.getId());
        dto.setNome(usuario.getNome());
        dto.setFotoPerfil(usuario.getFotoPerfil());
        dto.setTipos(usuario.getTipos());

        return ResponseEntity.ok(dto);
    }
}