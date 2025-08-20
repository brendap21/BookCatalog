package com.brendanavarro.bookcatalog.domain;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * Libro en el dominio (lo que la app entiende y mostrará/guardará).
 * NOTA: En una fase posterior lo convertiremos a @Entity y añadiremos @Id, etc.
 */
public class Book {
    /** ID de Gutendex/Gutenberg (no es UUID propio de BD todavía) */
    private Integer gutendexId;
    private String title;
    /** Código de idioma principal (EN, ES, FR...) */
    private String primaryLanguage;
    private Integer downloadCount;
    private List<Author> authors = new ArrayList<>();

    public Integer getGutendexId() { return gutendexId; }
    public void setGutendexId(Integer gutendexId) { this.gutendexId = gutendexId; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getPrimaryLanguage() { return primaryLanguage; }
    public void setPrimaryLanguage(String primaryLanguage) { this.primaryLanguage = primaryLanguage; }

    public Integer getDownloadCount() { return downloadCount; }
    public void setDownloadCount(Integer downloadCount) { this.downloadCount = downloadCount; }

    public List<Author> getAuthors() { return authors; }
    public void setAuthors(List<Author> authors) { this.authors = authors; }

    @Override public String toString() {
        String authorsStr = authors.isEmpty()
                ? "Autor desconocido"
                : authors.stream().map(Author::toString).collect(Collectors.joining(" | "));
        return "Book{" +
                "gutendexId=" + gutendexId +
                ", title='" + title + '\'' +
                ", primaryLanguage='" + primaryLanguage + '\'' +
                ", downloadCount=" + downloadCount +
                ", authors=[" + authorsStr + "]" +
                '}';
    }

    @Override public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Book)) return false;
        Book book = (Book) o;
        return Objects.equals(gutendexId, book.gutendexId);
    }
    @Override public int hashCode() { return Objects.hash(gutendexId); }
}
