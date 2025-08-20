package com.brendanavarro.bookcatalog.gutendex.mapper;

import com.brendanavarro.bookcatalog.domain.Author;
import com.brendanavarro.bookcatalog.domain.Book;
import com.brendanavarro.bookcatalog.gutendex.dto.BookDto;
import com.brendanavarro.bookcatalog.gutendex.dto.PersonDto;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

/**
 * Convierte objetos de la API (DTOs) a objetos de dominio internos de la app.
 * Mantiene la lógica de normalización (p. ej., "Apellido, Nombre").
 */
@Component
public class GutendexMapper {

    /** Convierte un BookDto (API) a Book (dominio). */
    public Book toDomain(BookDto dto) {
        if (dto == null) return null;

        Book b = new Book();
        b.setGutendexId(dto.getId());
        b.setTitle(dto.getTitle());
        b.setDownloadCount(dto.getDownloadCount());

        // Tomamos el primer idioma como "principal" (si hay)
        String lang = (dto.getLanguages() != null && !dto.getLanguages().isEmpty())
                ? dto.getLanguages().get(0).toUpperCase(Locale.ROOT)
                : "N/D";
        b.setPrimaryLanguage(lang);

        // Autores normalizados
        List<Author> authors = new ArrayList<>();
        if (dto.getAuthors() != null) {
            for (PersonDto p : dto.getAuthors()) {
                Author a = toDomain(p);
                if (a != null) authors.add(a);
            }
        }
        b.setAuthors(authors);

        return b;
    }

    /** Convierte un PersonDto (API) a Author (dominio) con nombre "Apellido, Nombre". */
    public Author toDomain(PersonDto p) {
        if (p == null || p.getName() == null || p.getName().isBlank()) return null;

        String normalized = normalizeToLastNameFirst(p.getName());
        return new Author(normalized, p.getBirthYear(), p.getDeathYear());
    }

    /**
     * Normaliza un nombre a "Apellido, Nombre(s)".
     * Reglas simples:
     *  - Si ya contiene coma, se devuelve tal cual (asumimos ya está en "Apellido, Nombre").
     *  - Si no, el último token es el apellido; el resto son nombre(s).
     *  - Casos límite (un solo token): se devuelve tal cual.
     */
    public static String normalizeToLastNameFirst(String raw) {
        String name = raw.trim().replaceAll("\\s+", " ");
        if (name.contains(",")) return name; // ya viene como "Austen, Jane"

        String[] parts = name.split(" ");
        if (parts.length <= 1) return name; // "Plato", "Voltaire", etc.

        String last = parts[parts.length - 1];
        String first = String.join(" ", java.util.Arrays.copyOf(parts, parts.length - 1));
        return last + ", " + first;
    }
}
