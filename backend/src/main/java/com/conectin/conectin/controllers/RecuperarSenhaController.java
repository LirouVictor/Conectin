package com.conectin.conectin.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.conectin.conectin.services.UsuarioService;

@RestController
@RequestMapping("/api/recuperar-senha")
public class RecuperarSenhaController {

@Autowired
UsuarioService usuarioService;


    
}
