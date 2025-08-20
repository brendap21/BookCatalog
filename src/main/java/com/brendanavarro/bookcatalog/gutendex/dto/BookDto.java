package com.brendanavarro.bookcatalog.gutendex.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.List;
import java.util.Map;

/**
 * Libro de Gutendex.
 * Campos principales: id, title, authors, languages, downloadCount.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class BookDto {

    private Integer id;
    private String title;
    private List<PersonDto> authors;
    private List<String> languages;

    // Con SNAKE_CASE global, "download_count" -> downloadCount
    private Integer downloadCount;

    // Opcionales para futuras mejoras (se mapean si vienen)
    private List<String> subjects;
    private List<String> bookshelves;
    private Map<String, String> formats; // MIME -> URL

    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public List<PersonDto> getAuthors() { return authors; }
    public void setAuthors(List<PersonDto> authors) { this.authors = authors; }

    public List<String> getLanguages() { return languages; }
    public void setLanguages(List<String> languages) { this.languages = languages; }

    public Integer getDownloadCount() { return downloadCount; }
    public void setDownloadCount(Integer downloadCount) { this.downloadCount = downloadCount; }

    public List<String> getSubjects() { return subjects; }
    public void setSubjects(List<String> subjects) { this.subjects = subjects; }

    public List<String> getBookshelves() { return bookshelves; }
    public void setBookshelves(List<String> bookshelves) { this.bookshelves = bookshelves; }

    public Map<String, String> getFormats() { return formats; }
    public void setFormats(Map<String, String> formats) { this.formats = formats; }

    @Override
    public String toString() {
        return "BookDto{" +
                "id=" + id +
                ", title='" + title + '\'' +
                ", authors=" + authors +
                ", languages=" + languages +
                ", downloadCount=" + downloadCount +
                ", subjects=" + subjects +
                ", bookshelves=" + bookshelves +
                ", formats=" + formats +
                '}';
    }
}
