package com.conectin.conectin.services;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.io.IOException; 
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.conectin.conectin.dto.PortfolioDto;
import com.conectin.conectin.entities.Portfolio;
import com.conectin.conectin.entities.Prestador;
import com.conectin.conectin.exception.CustomException;
import com.conectin.conectin.repository.PortfolioRepository;
import com.conectin.conectin.repository.PrestadorRepository;

@Service
public class PortfolioService {

    @Autowired
    private PortfolioRepository portfolioRepository;

    @Autowired
    private PrestadorRepository prestadorRepository;

    @Autowired
    private FileStorageService fileStorageService;

    @Transactional
    public Portfolio adicionarPortfolio(PortfolioDto dto) {
        Prestador prestador = prestadorRepository.findById(dto.getPrestadorId())
                .orElseThrow(() -> new IllegalArgumentException("Prestador não encontrado"));

        Portfolio portfolio = new Portfolio();
        portfolio.setPrestador(prestador);
        portfolio.setTitulo(dto.getTitulo());
        portfolio.setDescricao(dto.getDescricao());
        portfolio.setData(LocalDateTime.now());

        // Salva as imagens que vieram como Base64 e atualiza a lista de fotos com as
        // URLs relativas
        List<String> urlsSalvas = new ArrayList<>();
        if (dto.getFotos() != null) {
            for (String fotoBase64 : dto.getFotos()) {
                if (fotoBase64 != null && fotoBase64.startsWith("data:image")) {
                    try {
                        String url = fileStorageService.saveFile(fotoBase64);
                        urlsSalvas.add(url);
                    } catch (IOException e) {
                        throw new CustomException("Erro ao salvar uma das imagens do portfólio.", "FILE_ERROR_002");
                    }
                } else if (fotoBase64 != null && !fotoBase64.isBlank()) {
                    // Se já for uma URL, apenas adiciona
                    urlsSalvas.add(fotoBase64);
                }
            }
        }
        portfolio.setFotos(urlsSalvas);

        return portfolioRepository.save(portfolio);
    }

    // MÉTODO NOVO: Atualizar um item do portfólio
    @Transactional
    public Portfolio atualizarPortfolio(Integer portfolioId, PortfolioDto dto) {
        Portfolio portfolio = portfolioRepository.findById(portfolioId)
                .orElseThrow(() -> new CustomException("Item de portfólio não encontrado", "PORTFOLIO_NOT_FOUND"));

        portfolio.setTitulo(dto.getTitulo());
        portfolio.setDescricao(dto.getDescricao());

        // A lista de fotos no DTO deve conter todas as URLs finais desejadas (antigas e
        // novas)
        // O frontend será responsável por fazer o upload de novas imagens e enviar a
        // lista completa de URLs.
        portfolio.setFotos(dto.getFotos());

        return portfolioRepository.save(portfolio);
    }

    // MÉTODO NOVO: Deletar um item do portfólio
    @Transactional
    public void deletarPortfolio(Integer portfolioId) {
        if (!portfolioRepository.existsById(portfolioId)) {
            throw new CustomException("Item de portfólio não encontrado", "PORTFOLIO_NOT_FOUND");
        }
        // TODO: Futuramente, deletar as imagens do disco também.
        portfolioRepository.deleteById(portfolioId);
    }

    // MÉTODO NOVO: Buscar todos os portfólios de um prestador
    public List<PortfolioDto> buscarPorPrestador(Integer prestadorId) {
        if (!prestadorRepository.existsById(prestadorId)) {
            throw new CustomException("Prestador não encontrado", "PRESTADOR_NOT_FOUND");
        }
        return portfolioRepository.findByPrestadorId(prestadorId).stream()
                .map(PortfolioDto::fromEntity)
                .collect(Collectors.toList());
    }
}