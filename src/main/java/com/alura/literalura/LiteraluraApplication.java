package com.alura.literalura;

import com.alura.literalura.model.Author;
import com.alura.literalura.model.Book;
import com.alura.literalura.service.BookService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.io.IOException;
import java.util.List;
import java.util.Locale;
import java.util.Scanner;

@SpringBootApplication
public class LiteraluraApplication implements CommandLineRunner {

	private final Scanner scanner = new Scanner(System.in);

	@Autowired
	private BookService bookService; // Inyección en lugar de "new"

	public static void main(String[] args) {
		SpringApplication.run(LiteraluraApplication.class, args);
	}

	@Override
	public void run(String... args) {
		mostrarMenu();
	}

	private void mostrarMenu() {
		while (true) {
			System.out.println("\n================= 📚 LITERALURA =================");
			System.out.println("1️⃣ Buscar libros por título (API Gutendex)");
			System.out.println("2️⃣ Guardar un libro de los últimos resultados");
			System.out.println("3️⃣ Ver libros guardados (biblioteca local)");
			System.out.println("4️⃣ Filtrar guardados por idioma (ej: es, en, pt)");
			System.out.println("5️⃣ Ver detalles de un resultado reciente");
			System.out.println("6️⃣ Listar todos los autores");
			System.out.println("7️⃣ Listar autores vivos en un año");
			System.out.println("0️⃣ Salir");
			System.out.print("👉 Selecciona una opción: ");

			int opcion = leerEnteroSeguro();
			switch (opcion) {
				case 1 -> buscarLibros();
				case 2 -> guardarLibroDeResultados();
				case 3 -> verBiblioteca();
				case 4 -> filtrarBibliotecaPorIdioma();
				case 5 -> verDetallesResultadoReciente();
				case 6 -> listarAutores();
				case 7 -> listarAutoresVivosEnUnAnio();
				case 0 -> {
					System.out.println("👋 ¡Hasta luego, lector!");
					return;
				}
				default -> System.out.println("⚠️ Opción inválida, intenta de nuevo.");
			}
		}
	}

	private void buscarLibros() {
		System.out.print("🔎 Escribe una palabra para buscar (título/autor): ");
		String termino = scanner.nextLine().trim();
		if (termino.isEmpty()) {
			System.out.println("⚠️ El término no puede estar vacío.");
			return;
		}
		try {
			List<Book> resultados = bookService.buscarLibros(termino);
			if (resultados == null || resultados.isEmpty()) {
				System.out.println("❌ No se encontraron resultados para: " + termino);
				return;
			}
			System.out.println("\n✅ Resultados encontrados (" + resultados.size() + "):");
			imprimirListado(resultados);
			System.out.println("💡 Usa la opción 2 para guardar alguno.");
		} catch (IOException | InterruptedException e) {
			System.out.println("❌ Error consultando la API: " + e.getMessage());
		}
	}

	private void guardarLibroDeResultados() {
		List<Book> ultimos = bookService.getUltimosResultados();
		if (ultimos == null || ultimos.isEmpty()) {
			System.out.println("⚠️ No hay resultados recientes. Primero usa la opción 1 (buscar).");
			return;
		}
		imprimirListado(ultimos);
		System.out.print("👉 Ingresa el número del libro a guardar: ");
		int idx = leerEnteroSeguro();
		if (bookService.guardarLibro(idx)) {
			System.out.println("✅ Libro guardado en la biblioteca.");
		} else {
			System.out.println("⚠️ No se pudo guardar (índice inválido o libro duplicado).");
		}
	}

	private void verBiblioteca() {
		List<Book> biblioteca = bookService.getBiblioteca();
		if (biblioteca.isEmpty()) {
			System.out.println("📭 Tu biblioteca está vacía.");
			return;
		}
		System.out.println("\n📚 Tus libros guardados (" + biblioteca.size() + "):");
		imprimirListado(biblioteca);
	}

	private void filtrarBibliotecaPorIdioma() {
		List<Book> biblioteca = bookService.getBiblioteca();
		if (biblioteca.isEmpty()) {
			System.out.println("📭 Tu biblioteca está vacía.");
			return;
		}
		System.out.print("🌐 Código de idioma (ej: es, en, pt): ");
		String lang = scanner.nextLine().trim().toLowerCase(Locale.ROOT);
		if (lang.isEmpty()) {
			System.out.println("⚠️ Debes ingresar un idioma.");
			return;
		}
		List<Book> filtrados = bookService.filtrarPorIdioma(lang);
		if (filtrados.isEmpty()) {
			System.out.println("❌ No hay libros guardados en idioma '" + lang + "'.");
			return;
		}
		System.out.println("\n📘 Libros en idioma '" + lang + "':");
		imprimirListado(filtrados);
	}

	private void verDetallesResultadoReciente() {
		List<Book> ultimos = bookService.getUltimosResultados();
		if (ultimos == null || ultimos.isEmpty()) {
			System.out.println("⚠️ No hay resultados recientes. Primero busca (opción 1).");
			return;
		}
		imprimirListado(ultimos);
		System.out.print("👉 Número del libro para ver detalles: ");
		int idx = leerEnteroSeguro();
		if (idx < 1 || idx > ultimos.size()) {
			System.out.println("⚠️ Índice fuera de rango.");
			return;
		}
		Book b = ultimos.get(idx - 1);
		System.out.println("\n=========== 📖 DETALLES ===========");
		System.out.println("Título: " + b.getTitle());
		System.out.println("Autor: " + (b.getAuthor() != null ? b.getAuthor() : "Desconocido"));
		System.out.println("Idiomas: " + b.getLanguages());
		System.out.println("Descargas: " + b.getDownloadCount());
		System.out.println("===================================");
	}

	private void listarAutores() {
		List<Author> autores = bookService.listarAutores();
		if (autores.isEmpty()) {
			System.out.println("📭 No hay autores en la biblioteca.");
			return;
		}
		System.out.println("\n👥 Autores guardados:");
		autores.forEach(System.out::println);
	}

	private void listarAutoresVivosEnUnAnio() {
		System.out.print("👉 Ingresa el año: ");
		int anio = leerEnteroSeguro();
		List<Author> vivos = bookService.listarAutoresVivosEn(anio);
		if (vivos.isEmpty()) {
			System.out.println("❌ No se encontraron autores vivos en " + anio);
			return;
		}
		System.out.println("\n👥 Autores vivos en " + anio + ":");
		vivos.forEach(System.out::println);
	}

	private void imprimirListado(List<Book> libros) {
		int i = 1;
		for (Book b : libros) {
			System.out.printf("%d) %s%n", i++, b.toString());
		}
	}

	private int leerEnteroSeguro() {
		while (true) {
			String line = scanner.nextLine().trim();
			try {
				return Integer.parseInt(line);
			} catch (NumberFormatException e) {
				System.out.print("⚠️ Ingresa un número válido: ");
			}
		}
	}
}
