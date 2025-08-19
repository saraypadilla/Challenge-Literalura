package com.alura.literalura.service;

import com.alura.literalura.client.GutendexClient;
import com.alura.literalura.model.Author;
import com.alura.literalura.model.Book;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class BookService {

    private final GutendexClient client = new GutendexClient();

    // “BD” temporal en memoria
    private final List<Book> biblioteca = new ArrayList<>();
    private List<Book> ultimosResultados = new ArrayList<>();

    // Buscar libros en la API
    public List<Book> buscarLibros(String termino) throws IOException, InterruptedException {
        ultimosResultados = client.searchBooks(termino);
        return ultimosResultados;
    }

    public List<Book> getUltimosResultados() {
        return ultimosResultados;
    }

    public boolean guardarLibro(int index) {
        if (ultimosResultados == null || ultimosResultados.isEmpty()) return false;
        if (index < 1 || index > ultimosResultados.size()) return false;

        Book seleccionado = ultimosResultados.get(index - 1);

        // Evitar duplicados por título + nombre de autor (si existe)
        String tituloSel = seleccionado.getTitle() != null ? seleccionado.getTitle() : "";
        String autorSel = (seleccionado.getAuthor() != null && seleccionado.getAuthor().getName() != null)
                ? seleccionado.getAuthor().getName()
                : "";

        boolean yaExiste = biblioteca.stream().anyMatch(b -> {
            String t = b.getTitle() != null ? b.getTitle() : "";
            String a = (b.getAuthor() != null && b.getAuthor().getName() != null) ? b.getAuthor().getName() : "";
            return t.equalsIgnoreCase(tituloSel) && a.equalsIgnoreCase(autorSel);
        });
        if (yaExiste) return false;

        // Crear una copia ligera para evitar referencias compartidas (opcional, pero recomendable)
        Book copia = new Book();
        copia.setTitle(seleccionado.getTitle());
        copia.setLanguages(seleccionado.getLanguages() == null ? null : new ArrayList<>(seleccionado.getLanguages()));
        copia.setDownloadCount(seleccionado.getDownloadCount());
        if (seleccionado.getAuthor() != null) {
            Author a = new Author();
            a.setName(seleccionado.getAuthor().getName());
            a.setBirthYear(seleccionado.getAuthor().getBirthYear());
            a.setDeathYear(seleccionado.getAuthor().getDeathYear());
            copia.setAuthor(a);
        }

        biblioteca.add(copia);
        return true;
    }

    public List<Book> getBiblioteca() {
        return Collections.unmodifiableList(biblioteca);
    }

    public List<Book> filtrarPorIdioma(String lang) {
        String target = lang == null ? "" : lang.toLowerCase(Locale.ROOT);
        return biblioteca.stream()
                .filter(b -> b.getLanguages() != null &&
                        b.getLanguages().stream().anyMatch(l -> l != null && l.equalsIgnoreCase(target)))
                .collect(Collectors.toList());
    }

    // ===== Paso 8: funcionalidades de autores =====

    /** Lista de autores únicos (por nombre) presentes en la biblioteca */
    public List<Author> listarAutores() {
        return biblioteca.stream()
                .map(Book::getAuthor)
                .filter(Objects::nonNull)
                .collect(Collectors.collectingAndThen(
                        // Distinct por nombre (case-insensitive), conservando el orden de aparición
                        Collectors.toMap(a -> a.getName() == null ? "" : a.getName().toLowerCase(Locale.ROOT),
                                a -> a,
                                (a1, a2) -> a1,
                                LinkedHashMap::new),
                        m -> new ArrayList<>(m.values())
                ));
    }

    /** Autores vivos en un año dado (nombre del método coincide con lo que usa el menú) */
    public List<Author> listarAutoresVivosEn(int year) {
        return listarAutores().stream()
                .filter(a -> {
                    Integer n = a.getBirthYear();
                    Integer m = a.getDeathYear();
                    boolean nacido = (n == null) || n <= year;
                    boolean noHaMuerto = (m == null) || m >= year;
                    return nacido && noHaMuerto;
                })
                .collect(Collectors.toList());
    }
}
