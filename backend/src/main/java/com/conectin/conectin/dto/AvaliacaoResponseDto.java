package com.conectin.conectin.dto;

import java.time.LocalDateTime;
import lombok.Data;

@Data
public class AvaliacaoResponseDto {
    private String nomeAvaliador;
    private String fotoAvaliador;
    private String comentario;
    private Float nota;
    private LocalDateTime data;
}