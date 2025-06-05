package com.conectin.conectin.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class ResetarSenhaRequestDto {

    @NotBlank(message = "O token é obrigatório")
    private String token;

    @NotBlank(message = "A nova senha é obrigatória")
    @Size(min = 6, message = "A nova senha deve ter pelo menos 6 caracteres")
    private String novaSenha;

    @NotBlank(message = "A confirmação da nova senha é obrigatória")
    private String confirmarNovaSenha;

    public boolean isSenhasCoincidem() {
        return novaSenha != null && novaSenha.equals(confirmarNovaSenha);
    }
    
}
