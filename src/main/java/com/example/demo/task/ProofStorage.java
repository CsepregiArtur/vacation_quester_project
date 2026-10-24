package com.example.demo.task;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Locale;
import java.util.Set;
import java.util.UUID;

/**
 * Salveaza pozele doveditoare in folderul uploads/proofs si returneaza
 * adresa relativa sub care pot fi descarcate (/uploads/**).
 */
@Component
public class ProofStorage {

    /** Extensiile acceptate pentru dovezile foto. */
    private static final Set<String> ALLOWED = Set.of(".jpg", ".jpeg", ".png", ".webp");

    private final Path directory = Paths.get("uploads", "proofs").toAbsolutePath();

    public ProofStorage() {
        try {
            Files.createDirectories(directory);
        } catch (IOException ex) {
            throw new IllegalStateException("Could not create uploads/proofs", ex);
        }
    }

    /** Copiaza fisierul primit sub un nume unic si returneaza calea publica. */
    public String save(MultipartFile photo) {
        String extension = extensionOf(photo.getOriginalFilename());
        if (!ALLOWED.contains(extension)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Only JPG, JPEG, PNG or WEBP images are accepted");
        }
        String fileName = UUID.randomUUID() + extension;
        try (InputStream input = photo.getInputStream()) {
            Files.copy(input, directory.resolve(fileName), StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException ex) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Could not save the photo");
        }
        return "/uploads/proofs/" + fileName;
    }

    /** Extrage extensia (cu punct) din numele original al fisierului. */
    private String extensionOf(String originalFilename) {
        if (originalFilename == null) {
            return "";
        }
        int dot = originalFilename.lastIndexOf('.');
        return dot < 0 ? "" : originalFilename.substring(dot).toLowerCase(Locale.ROOT);
    }
}
