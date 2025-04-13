package com.conectin.conectin.dto;

import lombok.Data;

@Data
public class PrestadorDto {

    private Integer id;
    private Integer usuarioId;
    private String nome;
    private String descricao;
    private String disponibilidade;
    private Float avaliacaoMedia;
}