package com.brendanavarro.bookcatalog.gutendex.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.List;

/**
 * Respuesta de /books:
 * { "count": ..., "next": "...", "previous": "...", "results": [ ... ] }
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class GutendexResponseDto {

    private Integer count;
    private String next;
    private String previous;
    private List<BookDto> results;

    public Integer getCount() { return count; }
    public void setCount(Integer count) { this.count = count; }

    public String getNext() { return next; }
    public void setNext(String next) { this.next = next; }

    public String getPrevious() { return previous; }
    public void setPrevious(String previous) { this.previous = previous; }

    public List<BookDto> getResults() { return results; }
    public void setResults(List<BookDto> results) { this.results = results; }

    @Override
    public String toString() {
        return "GutendexResponseDto{" +
                "count=" + count +
                ", next='" + next + '\'' +
                ", previous='" + previous + '\'' +
                ", results=" + (results == null ? 0 : results.size()) +
                '}';
    }
}
