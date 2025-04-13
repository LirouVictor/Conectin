package com.conectin.conectin.services;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.conectin.conectin.dto.UsuarioDto;
import com.conectin.conectin.entities.Cidade;
import com.conectin.conectin.entities.CidadePrestador;
import com.conectin.conectin.entities.Cliente;
import com.conectin.conectin.entities.TipoUsuario;
import com.conectin.conectin.entities.Prestador;
import com.conectin.conectin.entities.Usuario;
import com.conectin.conectin.repository.CidadeRepository;
import com.conectin.conectin.repository.CidadePrestadorRepository;
import com.conectin.conectin.repository.UsuarioRepository;

@Service
public class UsuarioService {

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private CidadeRepository cidadeRepository;

    @Autowired
    private CidadePrestadorRepository cidadePrestadorRepository;

    public Usuario cadastrarUsuario(UsuarioDto usuarioDto) {
        if (!usuarioDto.isSenhasCoincidem()) {
            throw new IllegalArgumentException("As senhas não coincidem");
        }
        if (usuarioRepository.existsByEmail(usuarioDto.getEmail())) {
            throw new IllegalArgumentException("Email já cadastrado");
        }

        Usuario usuario = new Usuario();
        usuario.setNome(usuarioDto.getNome());
        usuario.setEndereco(usuarioDto.getEndereco());
        usuario.setEmail(usuarioDto.getEmail());
        usuario.setSenha(usuarioDto.getSenha());

        if (usuarioDto.isPrestador()) {
            usuario.addTipo(TipoUsuario.PRESTADOR);
        }
        if (usuarioDto.isCliente()) {
            usuario.addTipo(TipoUsuario.CLIENTE);
        }

        usuario = usuarioRepository.save(usuario);

        if (usuario.isPrestador()) {
            Prestador prestador = new Prestador();
            prestador.setUsuario(usuario);
            prestador.setDescricao("Descrição inicial");
            prestador.setDisponibilidade("Disponibilidade inicial");
            prestador.setAvaliacaoMedia(0.0f);
            usuario.setPrestador(prestador);
            usuarioRepository.save(usuario);

            List<Integer> cidadesIds = usuarioDto.getCidadesAtuacao();
            if (cidadesIds != null) {
                for (Integer cidadeId : cidadesIds) {
                    Cidade cidade = cidadeRepository.findById(cidadeId)
                            .orElseThrow(() -> new IllegalArgumentException("Cidade não encontrada"));
                    CidadePrestador cidadePrestador = new CidadePrestador();
                    cidadePrestador.setPrestador(prestador);
                    cidadePrestador.setCidade(cidade);
                    cidadePrestadorRepository.save(cidadePrestador);
                }
            }
        }

        if (usuario.isCliente()) {
            Cliente cliente = new Cliente();
            cliente.setUsuario(usuario);
            cliente.setHistoricoContratacoes("Histórico inicial");
            usuario.setCliente(cliente);
            usuarioRepository.save(usuario);
        }

        return usuario;
    }
}