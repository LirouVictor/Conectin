package com.conectin.conectin.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.conectin.conectin.entities.ResetarSenhaToken;
import com.conectin.conectin.entities.Usuario;

@Repository
public interface ResetarSenhaTokenRepository extends JpaRepository<ResetarSenhaToken, Long>{

    void deleteByUsuario(Usuario usuario); // Para deletar tokens antigos ao criar um novo

    Optional<ResetarSenhaToken> findByUsuario(Usuario usuario);

    Optional<ResetarSenhaToken> findByToken(String token);
 
} 