package com.conectin.conectin.services;

import com.conectin.conectin.dto.CategoriaDto;
import com.conectin.conectin.dto.CidadeDto;
import com.conectin.conectin.dto.PortfolioDto;
import com.conectin.conectin.dto.UsuarioDto;
import com.conectin.conectin.entities.*; // Import all entities
import com.conectin.conectin.repository.*; // Import all repositories
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCrypt; // Mantenha seu BCrypt ou use PasswordEncoder do Spring Security
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional; // IMPORTANTE
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet; // Para as coleções em Prestador
import java.util.List;
import java.util.Optional;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Service
public class UsuarioService {

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private PrestadorRepository prestadorRepository;

    @Autowired
    private ClienteRepository clienteRepository;

    @Autowired
    private CidadeRepository cidadeRepository; // Já estava

    @Autowired
    private CategoriaRepository categoriaRepository; // ADICIONADO

    @Autowired
    private PrestadorCategoriaRepository prestadorCategoriaRepository; // ADICIONADO

    @Autowired
    private CidadePrestadorRepository cidadePrestadorRepository; // Já estava

    // Se for gerenciar Portfolios de forma mais complexa:
    // @Autowired
    // private PortfolioRepository portfolioRepository;


    private static final Pattern EMAIL_PATTERN = Pattern.compile("^[A-Za-z0-9+_.-]+@(.+)$");

   @Transactional
public Usuario cadastrarUsuario(UsuarioDto usuarioDto) {
    if (!EMAIL_PATTERN.matcher(usuarioDto.getEmail()).matches()) {
        throw new IllegalArgumentException("Formato de e-mail inválido");
    }

    if (usuarioRepository.findByEmail(usuarioDto.getEmail()).isPresent()) {
        throw new IllegalArgumentException("Este e-mail já existe");
    }

    if (usuarioDto.getSenha() == null || usuarioDto.getSenha().isEmpty()) {
        throw new IllegalArgumentException("A senha é obrigatória para cadastro.");
    }
    if (!usuarioDto.isSenhasCoincidem()) {
        throw new IllegalArgumentException("As senhas não coincidem");
    }

    Usuario usuario = new Usuario();
    usuario.setNome(usuarioDto.getNome());
    usuario.setEndereco(usuarioDto.getEndereco());
    usuario.setEmail(usuarioDto.getEmail());
    usuario.setTelefone(usuarioDto.getTelefone()); // Novo campo
    usuario.setSenha(BCrypt.hashpw(usuarioDto.getSenha(), BCrypt.gensalt()));
    usuario.setFotoPerfil(usuarioDto.getFotoPerfil());

    if (usuarioDto.isPrestador()) {
        usuario.addTipo(TipoUsuario.PRESTADOR);
    }
    if (usuarioDto.isCliente()) {
        usuario.addTipo(TipoUsuario.CLIENTE);
    }

    if (usuario.getTipos().isEmpty()) {
        throw new IllegalArgumentException("Pelo menos um tipo de usuário deve ser selecionado");
    }

    if (usuario.isPrestador()) {
        Prestador prestador = new Prestador();
        prestador.setUsuario(usuario);
        usuario.setPrestador(prestador);
        prestador.setDescricao(usuarioDto.getDescricao() != null ? usuarioDto.getDescricao() : "");
        prestador.setDisponibilidade(usuarioDto.getDisponibilidade() != null ? usuarioDto.getDisponibilidade() : "");
        prestador.setAvaliacaoMedia(0.0f);

        if (usuarioDto.getCategorias() != null) {
            for (CategoriaDto catDto : usuarioDto.getCategorias()) {
                Categoria categoria = categoriaRepository.findById(catDto.getId().intValue())
                        .orElseThrow(() -> new IllegalArgumentException("Categoria não encontrada no cadastro: ID " + catDto.getId()));
                PrestadorCategoria pc = new PrestadorCategoria();
                pc.setPrestador(prestador);
                pc.setCategoria(categoria);
                prestador.getPrestadorCategorias().add(pc);
            }
        }
        if (usuarioDto.getCidades() != null) {
            for (CidadeDto cidDto : usuarioDto.getCidades()) {
                Cidade cidade = cidadeRepository.findById(cidDto.getId().intValue())
                        .orElseThrow(() -> new IllegalArgumentException("Cidade não encontrada no cadastro: ID " + cidDto.getId()));
                CidadePrestador cp = new CidadePrestador();
                cp.setPrestador(prestador);
                cp.setCidade(cidade);
                prestador.getCidadePrestadores().add(cp);
            }
        }
        if (usuarioDto.getPortfolios() != null && !usuarioDto.getPortfolios().isEmpty()) {
            for (PortfolioDto portfolioDto : usuarioDto.getPortfolios()) {
                Portfolio portfolio = new Portfolio();
                portfolio.setDescricao(portfolioDto.getDescricao());
                portfolio.setPrestador(prestador);
                prestador.getPortfolios().add(portfolio);
            }
        }
    }

    if (usuario.isCliente()) {
        Cliente cliente = new Cliente();
        cliente.setUsuario(usuario);
        usuario.setCliente(cliente);
        cliente.setHistoricoContratacoes("");
    }

    return usuarioRepository.save(usuario);
}

    public Usuario findByEmail(String email) {
        return usuarioRepository.findByEmail(email).orElse(null);
    }

    public Usuario findById(Long id) { // Mantive o tipo Long como no seu método
        return usuarioRepository.findById(id).orElse(null);
    }

@Transactional
public Optional<Usuario> editarUsuario(Long id, UsuarioDto usuarioDto) {
    Optional<Usuario> usuarioOpt = usuarioRepository.findById(id);
    if (usuarioOpt.isEmpty()) {
        return Optional.empty();
    }

    Usuario usuario = usuarioOpt.get();

    if (!EMAIL_PATTERN.matcher(usuarioDto.getEmail()).matches()) {
        throw new IllegalArgumentException("Formato de e-mail inválido");
    }

   Optional<Usuario> usuarioByEmail = usuarioRepository.findByEmail(usuarioDto.getEmail());
    if (usuarioByEmail.isPresent() && usuarioByEmail.get().getId() != id) {
        throw new IllegalArgumentException("Este e-mail já está em uso por outra conta.");
    }

    usuario.setNome(usuarioDto.getNome());
    usuario.setEndereco(usuarioDto.getEndereco());
    usuario.setEmail(usuarioDto.getEmail());
    usuario.setTelefone(usuarioDto.getTelefone());
    if (usuarioDto.getFotoPerfil() != null) {
        usuario.setFotoPerfil(usuarioDto.getFotoPerfil());
    }

    if (usuarioDto.getSenhaAtual() != null && !usuarioDto.getSenhaAtual().isEmpty()) {
        if (!BCrypt.checkpw(usuarioDto.getSenhaAtual(), usuario.getSenha())) {
            throw new IllegalArgumentException("Senha atual incorreta.");
        }
        if (usuarioDto.getSenha() == null || usuarioDto.getSenha().isEmpty()) {
            throw new IllegalArgumentException("Nova senha não pode ser vazia.");
        }
        if (!usuarioDto.isSenhasCoincidem()) {
            throw new IllegalArgumentException("A nova senha e a confirmação não coincidem.");
        }
        usuario.setSenha(BCrypt.hashpw(usuarioDto.getSenha(), BCrypt.gensalt()));
    }

    boolean eraPrestador = usuario.isPrestador();
    boolean eraCliente = usuario.isCliente();

    usuario.getTipos().clear();
    if (usuarioDto.isPrestador()) {
        usuario.addTipo(TipoUsuario.PRESTADOR);
    }
    if (usuarioDto.isCliente()) {
        usuario.addTipo(TipoUsuario.CLIENTE);
    }

    if (usuario.getTipos().isEmpty()) {
        throw new IllegalArgumentException("Pelo menos um tipo de usuário deve ser selecionado.");
    }

    if (usuario.isPrestador()) {
        Prestador prestador = prestadorRepository.findByUsuarioId(usuario.getId())
                .orElseGet(() -> {
                    Prestador novoPrestador = new Prestador();
                    novoPrestador.setUsuario(usuario);
                    usuario.setPrestador(novoPrestador);
                    return novoPrestador;
                });

        prestador.setDescricao(usuarioDto.getDescricao());
        prestador.setDisponibilidade(usuarioDto.getDisponibilidade());

        prestador.getPrestadorCategorias().clear();
        if (usuarioDto.getCategorias() != null) {
            for (CategoriaDto catDto : usuarioDto.getCategorias()) {
                Categoria categoria = categoriaRepository.findById(catDto.getId().intValue())
                        .orElseThrow(() -> new IllegalArgumentException("Categoria não encontrada: ID " + catDto.getId()));
                PrestadorCategoria prestadorCategoria = new PrestadorCategoria();
                prestadorCategoria.setPrestador(prestador);
                prestadorCategoria.setCategoria(categoria);
                prestador.getPrestadorCategorias().add(prestadorCategoria);
            }
        }

        prestador.getCidadePrestadores().clear();
        if (usuarioDto.getCidades() != null) {
            for (CidadeDto cidDto : usuarioDto.getCidades()) {
                Cidade cidade = cidadeRepository.findById(cidDto.getId().intValue())
                        .orElseThrow(() -> new IllegalArgumentException("Cidade não encontrada: ID " + cidDto.getId()));
                CidadePrestador cidadePrestador = new CidadePrestador();
                cidadePrestador.setPrestador(prestador);
                cidadePrestador.setCidade(cidade);
                prestador.getCidadePrestadores().add(cidadePrestador);
            }
        }

        // prestador.getPortfolios().clear();
        // if (usuarioDto.getPortfolios() != null && !usuarioDto.getPortfolios().isEmpty()) {
        //     for (PortfolioDto portfolioDto : usuarioDto.getPortfolios()) {
        //         Portfolio portfolio = new Portfolio();
        //         portfolio.setDescricao(portfolioDto.getDescricao());
        //         portfolio.setPrestador(prestador);
        //         prestador.getPortfolios().add(portfolio);
        //     }
        // }
    } else if (eraPrestador) {
        prestadorRepository.findByUsuarioId(usuario.getId()).ifPresent(prestadorParaDeletar -> {
            usuario.setPrestador(null);
            prestadorRepository.delete(prestadorParaDeletar);
        });
    }

    if (usuario.isCliente()) {
        if (!eraCliente) {
            Cliente cliente = new Cliente();
            cliente.setUsuario(usuario);
            usuario.setCliente(cliente);
        }
    } else if (eraCliente) {
        clienteRepository.findByUsuarioId(usuario.getId()).ifPresent(clienteParaDeletar -> {
            usuario.setCliente(null);
            clienteRepository.delete(clienteParaDeletar);
        });
    }

    return Optional.of(usuarioRepository.save(usuario));
}


    @Transactional
    public boolean deletarUsuario(Long id) {
        Optional<Usuario> usuarioOpt = usuarioRepository.findById(id);
        if (usuarioOpt.isEmpty()) {
            return false;
        }
        // A deleção do usuário deve, por cascata, deletar Prestador, Cliente e suas sub-entidades
        // como PrestadorCategoria, CidadePrestador, Portfolio se configurado corretamente.
        usuarioRepository.deleteById(id);
        return true;
    }

    public List<Usuario> listarUsuarios() {
        return usuarioRepository.findAll();
    }

    // Método para buscar perfil (você já tinha, mas revisado para consistência)
    // Este método DEVE ser usado pelo seu UsuarioController no endpoint /perfil
    public UsuarioDto buscarPerfilCompletoDto(Long usuarioId) {
        Usuario usuario = usuarioRepository.findById(usuarioId)
                .orElseThrow(() -> new IllegalArgumentException("Usuário não encontrado com ID: " + usuarioId));

        UsuarioDto dto = new UsuarioDto();
        dto.setNome(usuario.getNome());
        dto.setEndereco(usuario.getEndereco());
        dto.setEmail(usuario.getEmail());
        dto.setFotoPerfil(usuario.getFotoPerfil());
        dto.setPrestador(usuario.isPrestador());
        dto.setCliente(usuario.isCliente());

        if (usuario.isPrestador() && usuario.getPrestador() != null) { // Verifica se a entidade Prestador existe
            Prestador prestador = usuario.getPrestador();
            dto.setDescricao(prestador.getDescricao());
            dto.setDisponibilidade(prestador.getDisponibilidade());
            
            // Portfólios
            if (prestador.getPortfolios() != null) {
                dto.setPortfolios(
                    prestador.getPortfolios().stream()
                        .map(p -> convertToPortfolioDto(p, prestador.getId())) // Renomeei seu método
                        .collect(Collectors.toList())
                );
            } else {
                dto.setPortfolios(Collections.emptyList());
            }
        
            // Cidades
            if (prestador.getCidadePrestadores() != null) {
                dto.setCidades(prestador.getCidadePrestadores().stream()
                    .map(this::convertCidadePrestadorToCidadeDto) // Renomeei seu método
                    .collect(Collectors.toList()));
            } else {
                dto.setCidades(Collections.emptyList());
            }

            // Categorias
            if (prestador.getPrestadorCategorias() != null) {
                dto.setCategorias(prestador.getPrestadorCategorias().stream()
                    .map(this::convertPrestadorCategoriaToCategoriaDto) // Renomeei seu método
                    .collect(Collectors.toList()));
            } else {
                dto.setCategorias(Collections.emptyList());
            }
        } else { // Se não for prestador, ou se a entidade prestador for nula, inicializa listas vazias
            dto.setPortfolios(Collections.emptyList());
            dto.setCidades(Collections.emptyList());
            dto.setCategorias(Collections.emptyList());

            
        }

        return dto;
    }

    // Métodos conversores (seus métodos com nomes ligeiramente ajustados para clareza)
    private PortfolioDto convertToPortfolioDto(Portfolio p, Integer prestadorId) {
        PortfolioDto portfolioDto = new PortfolioDto();
        if (p.getId() != null) { // Adicionar ID do portfolio se existir e o DTO suportar
           // portfolioDto.setId(p.getId());
        }
        portfolioDto.setPrestadorId(prestadorId); // Pode não ser necessário no DTO de resposta do perfil
        // portfolioDto.setTitulo(p.getTitulo()); // Se Portfolio tiver título
        portfolioDto.setDescricao(p.getDescricao());
        portfolioDto.setFotos(p.getFotos() != null ? new ArrayList<>(p.getFotos()) : Collections.emptyList());
        // Se o frontend espera 'urlImagem' para preview
        // if (p.getFotos() != null && !p.getFotos().isEmpty()){
        //    portfolioDto.setUrlImagem(p.getFotos().get(0)); // Pega a primeira foto como preview
        // }
        return portfolioDto;
    }

    private CidadeDto convertCidadePrestadorToCidadeDto(CidadePrestador cp) {
        CidadeDto cidadeDto = new CidadeDto();
        // Assumindo que Cidade.id é Integer e CidadeDto.id é Long
        cidadeDto.setId(cp.getCidade().getId().longValue()); 
        cidadeDto.setNome(cp.getCidade().getNome());
        return cidadeDto;
    }

    private CategoriaDto convertPrestadorCategoriaToCategoriaDto(PrestadorCategoria pc) {
        CategoriaDto categoriaDto = new CategoriaDto();
        // Assumindo que Categoria.id é Integer e CategoriaDto.id é Long
        categoriaDto.setId(pc.getCategoria().getId().longValue());
        categoriaDto.setNome(pc.getCategoria().getNome());
        return categoriaDto;
    }
}