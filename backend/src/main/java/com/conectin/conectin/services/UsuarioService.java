package com.conectin.conectin.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.conectin.conectin.dto.UsuarioDto;
import com.conectin.conectin.entities.Cliente;
import com.conectin.conectin.entities.Prestador;
import com.conectin.conectin.entities.TipoUsuario;
import com.conectin.conectin.entities.Usuario;
import com.conectin.conectin.repository.UsuarioRepository;

@Service
public class UsuarioService {

    @Autowired
    private UsuarioRepository usuarioRepository;

    public Usuario cadastrarUsuario(UsuarioDto usuarioDto) {
        // Validação de senhas
        if (!usuarioDto.isSenhasCoincidem()) {
            throw new IllegalArgumentException("As senhas não coincidem");
        }

        // Verifica se o email já está cadastrado
        if (usuarioRepository.existsByEmail(usuarioDto.getEmail())) {
            throw new IllegalArgumentException("Email já cadastrado");
        }

        // Cria um novo usuário
        Usuario usuario = new Usuario();
        usuario.setNome(usuarioDto.getNome());
        usuario.setEndereco(usuarioDto.getEndereco());
        usuario.setEmail(usuarioDto.getEmail());
        usuario.setSenha(usuarioDto.getSenha());

        // Define os tipos de usuário
        if (usuarioDto.isPrestador()) {
            usuario.addTipo(TipoUsuario.PRESTADOR);
        }
        if (usuarioDto.isCliente()) {
            usuario.addTipo(TipoUsuario.CLIENTE);
        }

        // Relacionamentos com Prestador e Cliente
        if (usuario.isPrestador()) {
            Prestador prestador = new Prestador();
            prestador.setUsuario(usuario);
            prestador.setServicosOferecidos("Serviços iniciais"); // Preencha conforme necessário
            prestador.setPortfolio("Portfólio inicial");
            usuario.setPrestador(prestador);
        }

        if (usuario.isCliente()) {
            Cliente cliente = new Cliente();
            cliente.setUsuario(usuario);
            cliente.setHistoricoContratacoes("Histórico inicial"); // Preencha conforme necessário
            usuario.setCliente(cliente);
        }

        // Salva o usuário no banco de dados
        return usuarioRepository.save(usuario);
    }
}