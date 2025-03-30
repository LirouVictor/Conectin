package com.conectin.conectin.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;


@Data // Lombok annotation para gerar getters, setters, equals, hashCode e toString
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

    // Método para verificar se as senhas coincidem
    public boolean isSenhasCoincidem() {
        return senha != null && senha.equals(confirmarSenha);
    }
}