package com.conectin.conectin.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
public class EmailServiceImpl implements EmailService {

    @Autowired
    private JavaMailSender mailSender;

    @Override
    public void enviarSenhaRecuperarEmail(String to, String userName, String resetLink){
        if (mailSender == null) {
            System.err.println("JavaMailSender não está configurado. O e-mail NÃO será enviado.");
            // Ainda imprime no console para fins de desenvolvimento se o mailSender não for injetado
            System.out.println("----------------------------------------------------");
            System.out.println("SIMULANDO ENVIO DE E-MAIL DE REDEFINIÇÃO DE SENHA (mailSender nulo):");
            System.out.println("Para: " + to);
            System.out.println("Link de Redefinição: " + resetLink);
            System.out.println("----------------------------------------------------");
            return;
        }
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setTo(to);
            // Você deve configurar um e-mail 'from' aqui ou no application.properties:
            // spring.mail.from=seu_email_remetente@example.com
            // Se não, pode precisar de message.setFrom("seu_email_remetente@example.com");
            message.setSubject("Conectin - Redefinição de Senha");
            message.setText("Olá " + userName + ",\n\n"
                    + "Você solicitou a redefinição da sua senha.\n"
                    + "Clique no link abaixo para criar uma nova senha:\n"
                    + resetLink + "\n\n"
                    + "Se você não solicitou esta alteração, por favor, ignore este e-mail.\n\n"
                    + "Atenciosamente,\nEquipe Conectin");
            mailSender.send(message);
            System.out.println("E-mail de redefinição de senha enviado para: " + to);
        } catch (Exception e) {
            System.err.println("Erro ao tentar enviar e-mail de redefinição de senha: " + e.getMessage());
            // Em produção, você deve logar esta exceção de forma mais robusta
            // e talvez lançar uma exceção customizada para ser tratada mais acima.
        }
    }
}
        



        // Para fins de desenvolvimento, podemos apenas imprimir no console:
        
        
        
        
        // System.out.println("----------------------------------------------------");
        // System.out.println("Enviando e-mail de redefinição de senha para: " + to);
        // System.out.println("Usuário: " + userName);
        // System.out.println("Link de Redefinição: " + resetLink);
        // System.out.println("Assunto: Conectin - Redefinição de Senha");
        // System.out.println("Corpo: Olá " + userName + ",\\n\\n"
        //         + "Você solicitou a redefinição da sua senha.\\n"
        //         + "Clique no link abaixo para criar uma nova senha:\\n"
        //         + resetLink + "\\n\\n"
        //         + "Se você não solicitou esta alteração, por favor, ignore este e-mail.\\n\\n"
        //         + "Atenciosamente,\\nEquipe Conectin");
        // System.out.println("----------------------------------------------------");


