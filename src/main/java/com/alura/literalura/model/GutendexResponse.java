package com.alura.literalura.model;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public class GutendexResponse {

    @JsonAlias("results") // Asegura el mapeo del JSON a esta lista
    private List<Book> results;

    // Getters y Setters
    public List<Book> getResults() {
        return results;
    }

    public void setResults(List<Book> results) {
        this.results = results;
    }

    @Override
    public String toString() {
        return "GutendexResponse {" +
                "results=" + results +
                '}';
    }
}
