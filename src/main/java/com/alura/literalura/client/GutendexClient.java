package com.alura.literalura.client;

import com.alura.literalura.model.Author;
import com.alura.literalura.model.Book;
import com.alura.literalura.model.GutendexResponse;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

public class GutendexClient {

    private static final String BASE_URL = "https://gutendex.com/books/";
    private final HttpClient client;
    private final ObjectMapper mapper;

    public GutendexClient() {
        this.client = HttpClient.newHttpClient();
        this.mapper = new ObjectMapper();
    }

    public List<Book> getAllBooks() throws IOException, InterruptedException {
        return fetchBooks(BASE_URL);
    }

    public List<Book> searchBooks(String keyword) throws IOException, InterruptedException {
        String encodedKeyword = URLEncoder.encode(keyword, StandardCharsets.UTF_8);
        String url = BASE_URL + "?search=" + encodedKeyword;
        return fetchBooks(url);
    }

    // ====================
    // Método común para obtener libros + asignar primer autor
    // ====================
    private List<Book> fetchBooks(String url) throws IOException, InterruptedException {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .GET()
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        // Leemos toda la respuesta
        JsonNode root = mapper.readTree(response.body());
        JsonNode results = root.get("results");

        List<Book> books = new ArrayList<>();

        if (results != null && results.isArray()) {
            for (JsonNode bookNode : results) {
                // Parseamos Book normalmente
                Book book = mapper.treeToValue(bookNode, Book.class);

                // Si hay autores, tomamos solo el primero
                JsonNode authorsNode = bookNode.get("authors");
                if (authorsNode != null && authorsNode.isArray() && authorsNode.size() > 0) {
                    JsonNode firstAuthor = authorsNode.get(0);
                    Author author = new Author();

                    if (firstAuthor.has("name")) {
                        author.setName(firstAuthor.get("name").asText());
                    }
                    if (firstAuthor.has("birth_year") && !firstAuthor.get("birth_year").isNull()) {
                        author.setBirthYear(firstAuthor.get("birth_year").asInt());
                    }
                    if (firstAuthor.has("death_year") && !firstAuthor.get("death_year").isNull()) {
                        author.setDeathYear(firstAuthor.get("death_year").asInt());
                    }

                    book.setAuthor(author);
                }

                books.add(book);
            }
        }

        return books;
    }
}
