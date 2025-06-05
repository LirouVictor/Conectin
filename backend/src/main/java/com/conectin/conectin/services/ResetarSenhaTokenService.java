package com.conectin.conectin.services;

import java.util.Optional;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCrypt;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.conectin.conectin.entities.ResetarSenhaToken;
import com.conectin.conectin.entities.Usuario;
import com.conectin.conectin.repository.ResetarSenhaTokenRepository;
import com.conectin.conectin.repository.UsuarioRepository;

@Service
public class ResetarSenhaTokenService {

    @Autowired
    private ResetarSenhaTokenRepository resetarSenhaTokenRepository;

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Transactional
    public String criarTokerParaUsuario(Usuario usuario) {
        // MUDANÇA PRINCIPAL: Usando o método de exclusão direta e forçando o flush.
        resetarSenhaTokenRepository.deleteByUsuario(usuario);
        resetarSenhaTokenRepository.flush(); // Força a execução do comando DELETE no banco de dados imediatamente.

        String tokenString = UUID.randomUUID().toString();
        ResetarSenhaToken meuToken = new ResetarSenhaToken(tokenString, usuario);
        resetarSenhaTokenRepository.save(meuToken);
        
        return tokenString;
    }

    // Seus outros métodos (validarResetarSenhaToken e trocarSenhaUsuario) continuam como estão
    public Optional<Usuario> validarResetarSenhaToken(String token) {
        Optional<ResetarSenhaToken> passTokenOpt = resetarSenhaTokenRepository.findByToken(token);
        if (passTokenOpt.isEmpty() || passTokenOpt.get().isExpired()) {
            return Optional.empty();
        }
        return Optional.of(passTokenOpt.get().getUsuario());
    }

    @Transactional
    public void trocarSenhaUsuario(Usuario usuario, String novaSenha) {
        usuario.setSenha(BCrypt.hashpw(novaSenha, BCrypt.gensalt()));
        usuarioRepository.save(usuario);
        resetarSenhaTokenRepository.deleteByUsuario(usuario); // Também usando o método direto aqui
    }
}