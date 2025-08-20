package com.brendanavarro.bookcatalog.domain;

import java.util.Objects;

/**
 * Autor en el dominio de la app (independiente del formato de la API).
 * NOTA: Aún no usamos JPA. En una fase posterior añadiremos @Entity.
 */
public class Author {
    /** Nombre normalizado en formato "Apellido, Nombre(s)" */
    private String displayName;
    private Integer birthYear;   // puede ser null
    private Integer deathYear;   // puede ser null

    public Author() {}

    public Author(String displayName, Integer birthYear, Integer deathYear) {
        this.displayName = displayName;
        this.birthYear = birthYear;
        this.deathYear = deathYear;
    }

    public String getDisplayName() { return displayName; }
    public void setDisplayName(String displayName) { this.displayName = displayName; }

    public Integer getBirthYear() { return birthYear; }
    public void setBirthYear(Integer birthYear) { this.birthYear = birthYear; }

    public Integer getDeathYear() { return deathYear; }
    public void setDeathYear(Integer deathYear) { this.deathYear = deathYear; }

    /** Utilidad futura: ¿autor vivo en un año dado? */
    public boolean isAliveIn(int year) {
        boolean afterBirth = (birthYear == null) || (year >= birthYear);
        boolean beforeDeath = (deathYear == null) || (year <= deathYear);
        return afterBirth && beforeDeath;
    }

    @Override public String toString() {
        return displayName + (birthYear != null || deathYear != null
                ? " (" + (birthYear == null ? "?" : birthYear) + "–" + (deathYear == null ? "?" : deathYear) + ")"
                : "");
    }

    @Override public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Author)) return false;
        Author author = (Author) o;
        return Objects.equals(displayName, author.displayName)
                && Objects.equals(birthYear, author.birthYear)
                && Objects.equals(deathYear, author.deathYear);
    }
    @Override public int hashCode() { return Objects.hash(displayName, birthYear, deathYear); }
}
