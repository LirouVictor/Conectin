package com.conectin.conectin.services;

import com.conectin.conectin.dto.UsuarioDto;
import com.conectin.conectin.entities.Cliente;
import com.conectin.conectin.entities.Prestador;
import com.conectin.conectin.entities.TipoUsuario;
import com.conectin.conectin.entities.Usuario;
import com.conectin.conectin.repository.ClienteRepository;
import com.conectin.conectin.repository.PrestadorRepository;
import com.conectin.conectin.repository.UsuarioRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCrypt;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.regex.Pattern;

@Service
public class UsuarioService {

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private PrestadorRepository prestadorRepository;

    @Autowired
    private ClienteRepository clienteRepository;

    private static final Pattern EMAIL_PATTERN = Pattern.compile("^[A-Za-z0-9+_.-]+@(.+)$");

    public Usuario cadastrarUsuario(UsuarioDto usuarioDto) {
        // Validar email
        if (!EMAIL_PATTERN.matcher(usuarioDto.getEmail()).matches()) {
            throw new IllegalArgumentException("Formato de e-mail inválido");
        }

        // Verificar se o email já existe
        if (usuarioRepository.findByEmail(usuarioDto.getEmail()).isPresent()) {
            throw new IllegalArgumentException("Este e-mail já existe");
        }

        // Validar senhas
        if (!usuarioDto.isSenhasCoincidem()) {
            throw new IllegalArgumentException("As senhas não coincidem");
        }

        // Criar usuário
        Usuario usuario = new Usuario();
        usuario.setNome(usuarioDto.getNome());
        usuario.setEndereco(usuarioDto.getEndereco());
        usuario.setEmail(usuarioDto.getEmail());
        usuario.setSenha(BCrypt.hashpw(usuarioDto.getSenha(), BCrypt.gensalt()));

        // Adicionar tipos
        if (usuarioDto.isPrestador()) {
            usuario.addTipo(TipoUsuario.PRESTADOR);
        }
        if (usuarioDto.isCliente()) {
            usuario.addTipo(TipoUsuario.CLIENTE);
        }

        if (usuario.getTipos().isEmpty()) {
            throw new IllegalArgumentException("Pelo menos um tipo de usuário deve ser selecionado");
        }

        // Salvar usuário
        usuario = usuarioRepository.save(usuario);

        // Criar Prestador, se aplicável
        if (usuario.isPrestador()) {
            Prestador prestador = new Prestador();
            prestador.setUsuario(usuario);
            prestador.setDescricao(""); // Pode ser preenchido posteriormente
            prestador.setDisponibilidade("");
            prestador.setAvaliacaoMedia(0.0f);
            prestadorRepository.save(prestador);
        }

        // Criar Cliente, se aplicável
        if (usuario.isCliente()) {
            Cliente cliente = new Cliente();
            cliente.setUsuario(usuario);
            cliente.setHistoricoContratacoes("");
            clienteRepository.save(cliente);
        }

        return usuario;
    }

    public Usuario findByEmail(String email) {
        return usuarioRepository.findByEmail(email).orElse(null);
    }

    public Usuario findById(Long id) {
        return usuarioRepository.findById(id).orElse(null);
    }

    public Optional<Usuario> editarUsuario(Long id, UsuarioDto usuarioDto) {
        Optional<Usuario> usuarioOpt = usuarioRepository.findById(id);
        if (usuarioOpt.isEmpty()) {
            return Optional.empty();
        }

        Usuario usuario = usuarioOpt.get();

        // Validar email
        if (!EMAIL_PATTERN.matcher(usuarioDto.getEmail()).matches()) {
            throw new IllegalArgumentException("Formato de e-mail inválido");
        }

        // Verificar se o novo email já existe (exceto para o próprio usuário)
        Optional<Usuario> usuarioByEmail = usuarioRepository.findByEmail(usuarioDto.getEmail());
        if (usuarioByEmail.isPresent() && usuarioByEmail.get().getId() != id) { // Correção: usar != para comparar long
            throw new IllegalArgumentException("Este e-mail já existe");
        }

        // Atualizar dados
        usuario.setNome(usuarioDto.getNome());
        usuario.setEndereco(usuarioDto.getEndereco());
        usuario.setEmail(usuarioDto.getEmail());

        // Atualizar tipos
        usuario.getTipos().clear();
        if (usuarioDto.isPrestador()) {
            usuario.addTipo(TipoUsuario.PRESTADOR);
        }
        if (usuarioDto.isCliente()) {
            usuario.addTipo(TipoUsuario.CLIENTE);
        }

        if (usuario.getTipos().isEmpty()) {
            throw new IllegalArgumentException("Pelo menos um tipo de usuário deve ser selecionado");
        }

        // Atualizar senha, se fornecida
        if (usuarioDto.getSenha() != null && !usuarioDto.getSenha().isEmpty()) {
            if (!usuarioDto.isSenhasCoincidem()) {
                throw new IllegalArgumentException("As senhas não coincidem");
            }
            usuario.setSenha(BCrypt.hashpw(usuarioDto.getSenha(), BCrypt.gensalt()));
        }

        // Atualizar Prestador e Cliente
        if (usuario.isPrestador()) {
            Optional<Prestador> prestadorOpt = prestadorRepository.findByUsuarioId(id);
            if (prestadorOpt.isEmpty()) {
                Prestador prestador = new Prestador();
                prestador.setUsuario(usuario);
                prestador.setDescricao("");
                prestador.setDisponibilidade("");
                prestador.setAvaliacaoMedia(0.0f);
                prestadorRepository.save(prestador);
            }
        } else {
            prestadorRepository.findByUsuarioId(id).ifPresent(prestadorRepository::delete);
        }

        if (usuario.isCliente()) {
            Optional<Cliente> clienteOpt = clienteRepository.findByUsuarioId(id);
            if (clienteOpt.isEmpty()) {
                Cliente cliente = new Cliente();
                cliente.setUsuario(usuario);
                cliente.setHistoricoContratacoes("");
                clienteRepository.save(cliente);
            }
        } else {
            clienteRepository.findByUsuarioId(id).ifPresent(clienteRepository::delete);
        }

        return Optional.of(usuarioRepository.save(usuario));
    }

    public boolean deletarUsuario(Long id) {
        Optional<Usuario> usuarioOpt = usuarioRepository.findById(id);
        if (usuarioOpt.isEmpty()) {
            return false;
        }
        usuarioRepository.deleteById(id);
        return true;
    }

    public List<Usuario> listarUsuarios() {
        return usuarioRepository.findAll();
    }
}