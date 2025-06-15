package com.conectin.conectin.dto;

import java.util.Set;
import com.conectin.conectin.entities.TipoUsuario;
import lombok.Data;

@Data
public class UsuarioPublicoDto {
    private Long id;
    private String nome;
    private String fotoPerfil;
    private Set<TipoUsuario> tipos;
}