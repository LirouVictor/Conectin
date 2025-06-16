package com.conectin.conectin.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.conectin.conectin.entities.Portfolio;

public interface PortfolioRepository extends JpaRepository<Portfolio, Integer> {
        List<Portfolio> findByPrestadorId(Integer prestadorId);
}