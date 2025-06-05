package com.conectin.conectin.services;

public interface EmailService {
    void enviarSenhaRecuperarEmail(String to, String userName, String resetLink);
}