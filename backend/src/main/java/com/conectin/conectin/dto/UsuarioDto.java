package com.conectin.conectin.dto;

import java.util.List;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class UsuarioDto {

    @NotBlank(message = "O nome é obrigatório")
    private String nome;

    @NotBlank(message = "O endereço é obrigatório")
    private String endereco;

    @NotBlank(message = "O email é obrigatório")
    @Email(message = "Email inválido")
    private String email;

    private String senha;
    private String confirmarSenha;

    private boolean prestador;
    private boolean cliente;

    private String fotoPerfil; // URL ou caminho da foto de perfil
    private String descricao; // Descrição do prestador
    private String disponibilidade; // Disponibilidade do prestador
    private List<CategoriaDto> categorias; // Categorias de trabalho
    private List<CidadeDto> cidades; // Cidades atendidas
    private List<PortfolioDto> portfolios; // Itens do portfólio

    public boolean isSenhasCoincidem() {
        if (senha == null || confirmarSenha == null) {
            return true;
        }
        return senha.equals(confirmarSenha);
    }
}

@Data
class CategoriaDto {
    private Long id;
    private String nome;
}

@Data
class CidadeDto {
    private Long id;
    private String nome;
}

@Data
class PortfolioDto {
    private Long id;
    private String urlImagem;
    private String descricao;
}