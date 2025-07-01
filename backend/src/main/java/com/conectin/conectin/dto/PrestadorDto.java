package com.conectin.conectin.dto;

import java.util.List;

import com.conectin.conectin.entities.Categoria;
import com.conectin.conectin.entities.Cidade;
import com.conectin.conectin.entities.Portfolio;

import lombok.Data;

@Data
public class PrestadorDto {

    private long id;
    private long usuarioId;
    private String nome;
    private String descricao;
    private String disponibilidade;
    private Float avaliacaoMedia;
    private String fotoPerfil;
    private List<Categoria> categorias;
    private List<Portfolio> portfolios;
    private List<Cidade> cidades; 
    private String telefone; 
}