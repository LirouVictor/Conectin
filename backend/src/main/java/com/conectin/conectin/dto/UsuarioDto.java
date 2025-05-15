package com.conectin.conectin.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

import java.util.List;

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
    private String senhaAtual;

    @NotBlank(message = "O telefone é obrigatório")
    @Pattern(regexp = "\\d{10,11}", message = "Telefone deve ter 10 ou 11 dígitos")
    private String telefone; // Novo campo

    private boolean prestador;
    private boolean cliente;

    private String fotoPerfil;
    private String descricao;
    private String disponibilidade;
    private List<CategoriaDto> categorias;
    private List<CidadeDto> cidades;
    private List<PortfolioDto> portfolios;

    public boolean isSenhasCoincidem() {
        if (senha == null || confirmarSenha == null) {
            return true;
        }
        return senha.equals(confirmarSenha);
    }
}