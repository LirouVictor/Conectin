package com.conectin.conectin.services;

import org.springframework.stereotype.Service;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;

@Service
public class FileStorageService {

    private final String uploadDir = "uploads/";

    public FileStorageService() {
        try {
            Files.createDirectories(Paths.get(uploadDir));
        } catch (IOException e) {
            throw new RuntimeException("Não foi possível criar o diretório de uploads: " + uploadDir, e);
        }
    }

public String saveFile(String base64Image) throws IOException {
        if (base64Image == null || base64Image.isEmpty()) {
            throw new IllegalArgumentException("Imagem base64 não pode ser nula ou vazia");
        }

        String imageData = base64Image;
        if (base64Image.contains(",")) {
            imageData = base64Image.split(",")[1];
        }

        byte[] decodedBytes;
        try {
            decodedBytes = java.util.Base64.getDecoder().decode(imageData);
        } catch (IllegalArgumentException e) {
            throw new IOException("Formato base64 inválido", e);
        }

        String fileName = UUID.randomUUID().toString() + ".jpg";
        Path filePath = Paths.get(uploadDir, fileName);
        Files.write(filePath, decodedBytes);

        if (!Files.exists(filePath)) {
            throw new IOException("Falha ao salvar o arquivo: " + filePath);
        }

        return "/uploads/" + fileName; // Return relative path
    }
}