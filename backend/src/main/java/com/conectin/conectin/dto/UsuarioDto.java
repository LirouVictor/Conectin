package com.conectin.conectin.dto;

import java.util.List;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class UsuarioDto {

    @NotBlank(message = "O nome é obrigatório")
    private String nome;

    @NotBlank(message = "O endereço é obrigatório")
    private String endereco;

    @NotBlank(message = "O email é obrigatório")
    private String email;

    @NotBlank(message = "A senha é obrigatória")
    private String senha;

    @NotBlank(message = "A confirmação de senha é obrigatória")
    private String confirmarSenha;

    private boolean prestador;
    private boolean cliente;

    private List<Integer> cidadesAtuacao;

    public boolean isSenhasCoincidem() {
        return senha != null && senha.equals(confirmarSenha);
    }
}           