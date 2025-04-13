package com.conectin.conectin.dto;

import java.util.List;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class PortfolioDto {

    @NotNull(message = "O ID do prestador é obrigatório")
    private Integer prestadorId;

    @NotBlank(message = "O título é obrigatório")
    private String titulo;

    private String descricao;

    private List<String> fotos;
}