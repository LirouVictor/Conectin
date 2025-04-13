package com.conectin.conectin.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.conectin.conectin.entities.Usuario;

public interface UsuarioRepository extends JpaRepository<Usuario, Integer> {
    Optional<Usuario> findByEmail(String email);
    boolean existsByEmail(String email);
}