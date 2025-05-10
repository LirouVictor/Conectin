package com.conectin.conectin.repository;

import com.conectin.conectin.entities.Prestador; // Importar Prestador
import org.springframework.data.jpa.repository.JpaRepository;
import com.conectin.conectin.entities.PrestadorCategoria;
import java.util.List; // Importar List

public interface PrestadorCategoriaRepository extends JpaRepository<PrestadorCategoria, Integer> {
    List<PrestadorCategoria> findByPrestadorId(Integer prestadorId); // Adicionar este se precisar buscar por ID
    void deleteByPrestador(Prestador prestador); // Método crucial para limpar associações
}