package com.conectin.conectin.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.conectin.conectin.entities.Cliente;

public interface ClienteRepository extends JpaRepository<Cliente, Integer> {
}