package com.conectin.conectin.entities;

import java.time.LocalDateTime;

import lombok.Getter;
import lombok.Setter;


import jakarta.persistence.*;
import java.util.HashSet;
import java.util.Set;

@Entity
@Getter
@Setter
public class Usuario {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(nullable = false)
    private String nome;

    @Column(nullable = false)
    private String endereco;

    @Column(nullable = false, unique = true)
    private String email;

    @Column(nullable = false)
    private String senha;

    private String fotoPerfil;

    @Column(nullable = false, updatable = false)
    private LocalDateTime dataCadastro;

    // Mapeamento para tipos de usuário (enum)
    @ElementCollection(targetClass = TipoUsuario.class, fetch = FetchType.EAGER)
    @Enumerated(EnumType.STRING) // Armazena os valores como strings no banco de dados
    private Set<TipoUsuario> tipos = new HashSet<>();

    @OneToOne(mappedBy = "usuario", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private Prestador prestador;

    @OneToOne(mappedBy = "usuario", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private Cliente cliente;

    @PrePersist
    public void preencherDataCadastro() {
        this.dataCadastro = LocalDateTime.now();
    }


    // Método para verificar se o usuário é um PRESTADOR
    public boolean isPrestador() {
        return tipos.contains(TipoUsuario.PRESTADOR);
    }

    // Método para verificar se o usuário é um CLIENTE
    public boolean isCliente() {
        return tipos.contains(TipoUsuario.CLIENTE);
    }

    // Método para adicionar um tipo ao usuário
    public void addTipo(TipoUsuario tipo) {
        tipos.add(tipo);
    }
}
