package com.brendanavarro.bookcatalog.gutendex.dto;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/**
 * Representa a una persona (autor/a) según Gutendex.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class PersonDto {

    // Con SNAKE_CASE global no son obligatorios los alias, pero los dejamos
    // por robustez (si algún día cambia el nombre del campo en la API).
    @JsonAlias("birth_year")
    private Integer birthYear;   // puede venir null

    @JsonAlias("death_year")
    private Integer deathYear;   // puede venir null

    private String name;

    public Integer getBirthYear() { return birthYear; }
    public void setBirthYear(Integer birthYear) { this.birthYear = birthYear; }

    public Integer getDeathYear() { return deathYear; }
    public void setDeathYear(Integer deathYear) { this.deathYear = deathYear; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    @Override
    public String toString() {
        return "PersonDto{" +
                "birthYear=" + birthYear +
                ", deathYear=" + deathYear +
                ", name='" + name + '\'' +
                '}';
    }
}
