package com.brendanavarro.bookcatalog.console;

import com.brendanavarro.bookcatalog.domain.Author;
import com.brendanavarro.bookcatalog.domain.Book;
import com.brendanavarro.bookcatalog.gutendex.GutendexClient;
import com.brendanavarro.bookcatalog.gutendex.dto.BookDto;
import com.brendanavarro.bookcatalog.gutendex.mapper.GutendexMapper;
import org.springframework.stereotype.Component;

import java.io.PrintStream;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

/**
 * Menú de interacción por consola (FASE 6).
 * - Usa Scanner para leer la entrada de usuario.
 * - Mantiene un catálogo EN MEMORIA (temporal) con los libros registrados.
 *   => En la siguiente fase se reemplaza por JPA/PostgreSQL.
 */
@Component
public class ConsoleMenu {

    private final GutendexClient client;
    private final GutendexMapper mapper;
    private final Scanner scanner;          // Un único scanner para toda la app
    private final PrintStream out = System.out;

    // Catálogo temporal en memoria: clave = gutendexId
    private final Map<Integer, Book> catalog = new LinkedHashMap<>();

    public ConsoleMenu(GutendexClient client, GutendexMapper mapper) {
        this.client = client;
        this.mapper = mapper;
        this.scanner = new Scanner(System.in);
    }

    /**
     * Punto de entrada del menú. Es un bucle que se repite hasta que el usuario elige "Salir".
     */
    public void start() {
        int option;
        do {
            printHeader();
            printOptions();
            option = readInt("Selecciona una opción: ");
            handleOption(option);
            if (option != 6) {
                out.println();
                out.println("Presiona ENTER para continuar...");
                scanner.nextLine(); // espera confirmación
            }
        } while (option != 6);

        out.println("👋 ¡Gracias por usar LiterAlura! Hasta pronto.");
        // NO cerrar scanner (cerraría System.in y puede afectar otros componentes)
    }

    // ===================== DIBUJO DE MENÚ =====================

    private void printHeader() {
        out.println("==============================================");
        out.println("         LiterAlura - Catálogo de Libros      ");
        out.println("==============================================");
    }

    private void printOptions() {
        out.println("1) Buscar libro por TÍTULO (API) y REGISTRAR (evita duplicados)");
        out.println("2) Listar LIBROS registrados");
        out.println("3) Listar AUTORES registrados");
        out.println("4) Listar AUTORES vivos en un AÑO");
        out.println("5) Listar LIBROS por IDIOMA");
        out.println("6) Salir");
        out.println("----------------------------------------------");
    }

    private void handleOption(int option) {
        switch (option) {
            case 1 -> optionSearchAndRegister();
            case 2 -> optionListBooks();
            case 3 -> optionListAuthors();
            case 4 -> optionListAuthorsAliveInYear();
            case 5 -> optionListBooksByLanguage();
            case 6 -> { /* salir */ }
            default -> out.println("❌ Opción inválida. Intenta nuevamente.");
        }
    }

    // ===================== OPCIÓN 1 =====================

    /**
     * Opción 1: Buscar en Gutendex por título (y, opcionalmente, por idiomas)
     * y registrar en el catálogo en memoria evitando duplicados por gutendexId.
     */
    private void optionSearchAndRegister() {
        out.println("🔎 Buscar y registrar libro");
        String title = readNonEmpty("Ingresa el TÍTULO del libro a buscar: ");
        String languagesCsv = readOptional("Filtrar por IDIOMAS (códigos ISO separados por coma, ej. en,es) o deja vacío: ");

        try {
            Optional<BookDto> maybe = client.searchFirstByTitle(title, languagesCsv);
            if (maybe.isEmpty()) {
                out.printf("❌ No se encontró ningún libro con título que contenga \"%s\"%s%n",
                        title,
                        languagesCsv == null || languagesCsv.isBlank() ? "" : (" (idiomas=" + languagesCsv + ")"));
                return;
            }

            Book found = mapper.toDomain(maybe.get());
            if (found == null || found.getGutendexId() == null) {
                out.println("⚠️ Se obtuvo una respuesta inesperada al mapear el libro.");
                return;
            }

            if (catalog.containsKey(found.getGutendexId())) {
                out.printf("ℹ️ El libro \"%s\" (ID=%d) ya estaba registrado. No se duplicará.%n",
                        found.getTitle(), found.getGutendexId());
            } else {
                catalog.put(found.getGutendexId(), found);
                out.printf("✅ Registrado: \"%s\" (ID=%d)%n", found.getTitle(), found.getGutendexId());
            }

            printBookSummary(found);
        } catch (Exception e) {
            out.printf("❌ Error al consultar/registrar: %s%n", e.getMessage());
        }
    }

    // ===================== OPCIÓN 2 =====================

    private void optionListBooks() {
        out.println("📚 Libros registrados:");
        if (catalog.isEmpty()) {
            out.println("  (aún no hay libros; usa la opción 1 para registrar alguno)");
            return;
        }

        AtomicInteger i = new AtomicInteger(1);
        catalog.values().forEach(book -> {
            out.printf("%d) %s%n", i.getAndIncrement(), book.getTitle());
            out.printf("   ID: %d | Idioma: %s | Descargas: %s%n",
                    book.getGutendexId(),
                    nullTo(book.getPrimaryLanguage(), "N/D"),
                    book.getDownloadCount() == null ? "N/D" : book.getDownloadCount().toString());
            out.printf("   Autor(es): %s%n", book.getAuthors().isEmpty()
                    ? "Autor desconocido"
                    : book.getAuthors().stream().map(Author::toString).collect(Collectors.joining(" | ")));
        });
    }

    // ===================== OPCIÓN 3 =====================

    private void optionListAuthors() {
        out.println("👤 Autores registrados (únicos):");
        if (catalog.isEmpty()) {
            out.println("  (aún no hay autores; primero registra algún libro en la opción 1)");
            return;
        }

        // Conjunto de autores únicos por "displayName|birth|death"
        Map<String, Author> unique = new TreeMap<>();
        catalog.values().forEach(book ->
                book.getAuthors().forEach(a -> unique.put(keyOf(a), a))
        );

        if (unique.isEmpty()) {
            out.println("  (no hay autores asociados a los libros registrados)");
            return;
        }

        AtomicInteger i = new AtomicInteger(1);
        unique.values().forEach(a -> {
            out.printf("%d) %s%n", i.getAndIncrement(), a);
        });
    }

    // ===================== OPCIÓN 4 =====================

    private void optionListAuthorsAliveInYear() {
        out.println("📅 Autores vivos en un año específico");
        int year = readInt("Ingresa el año (ej. 1600): ");

        if (catalog.isEmpty()) {
            out.println("  (aún no hay datos; registra libros en la opción 1)");
            return;
        }

        // Distintos por clave
        Map<String, Author> unique = new TreeMap<>();
        catalog.values().forEach(book ->
                book.getAuthors().forEach(a -> unique.put(keyOf(a), a))
        );

        List<Author> alive = unique.values().stream()
                .filter(a -> a.isAliveIn(year))
                .collect(Collectors.toList());

        if (alive.isEmpty()) {
            out.printf("  Ningún autor registrado estaba vivo en el año %d%n", year);
            return;
        }

        out.printf("  Autores vivos en %d:%n", year);
        AtomicInteger i = new AtomicInteger(1);
        alive.forEach(a -> out.printf("%d) %s%n", i.getAndIncrement(), a));
    }

    // ===================== OPCIÓN 5 =====================

    private void optionListBooksByLanguage() {
        out.println("🌐 Filtrar libros por idioma");
        String code = readNonEmpty("Ingresa el código ISO del idioma (ej. ES, EN, FR, PT): ").toUpperCase(Locale.ROOT);

        List<Book> filtered = catalog.values().stream()
                .filter(b -> code.equalsIgnoreCase(nullTo(b.getPrimaryLanguage(), "")))
                .collect(Collectors.toList());

        if (filtered.isEmpty()) {
            out.printf("  No hay libros registrados en idioma %s%n", code);
            return;
        }

        AtomicInteger i = new AtomicInteger(1);
        filtered.forEach(book -> {
            out.printf("%d) %s (ID=%d) | Autor(es): %s | Descargas: %s%n",
                    i.getAndIncrement(),
                    book.getTitle(),
                    book.getGutendexId(),
                    book.getAuthors().isEmpty()
                            ? "Autor desconocido"
                            : book.getAuthors().stream().map(Author::toString).collect(Collectors.joining(" | ")),
                    book.getDownloadCount() == null ? "N/D" : book.getDownloadCount().toString());
        });
    }

    // ===================== UTILIDADES =====================

    private void printBookSummary(Book book) {
        out.println("----------------------------------------------");
        out.println("Resumen del libro registrado:");
        out.printf("ID: %d%n", book.getGutendexId());
        out.printf("Título: %s%n", book.getTitle());
        out.printf("Idioma: %s%n", nullTo(book.getPrimaryLanguage(), "N/D"));
        out.printf("Descargas: %s%n", book.getDownloadCount() == null ? "N/D" : book.getDownloadCount().toString());
        out.printf("Autor(es): %s%n", book.getAuthors().isEmpty()
                ? "Autor desconocido"
                : book.getAuthors().stream().map(Author::toString).collect(Collectors.joining(" | ")));
        out.println("----------------------------------------------");
    }

    private int readInt(String prompt) {
        while (true) {
            out.print(prompt);
            String line = scanner.nextLine();
            try {
                return Integer.parseInt(line.trim());
            } catch (NumberFormatException ex) {
                out.println("  ⚠️ Ingresa un número válido.");
            }
        }
    }

    private String readNonEmpty(String prompt) {
        while (true) {
            out.print(prompt);
            String line = scanner.nextLine();
            if (line != null && !line.trim().isEmpty()) return line.trim();
            out.println("  ⚠️ No puede estar vacío.");
        }
    }

    private String readOptional(String prompt) {
        out.print(prompt);
        String line = scanner.nextLine();
        return line == null ? "" : line.trim();
    }

    private String nullTo(String value, String fallback) {
        return value == null ? fallback : value;
    }

    private String keyOf(Author a) {
        // clave compuesta para deduplicar autores en memoria
        return (a.getDisplayName() == null ? "?" : a.getDisplayName()) + "|" +
                (a.getBirthYear() == null ? "?" : a.getBirthYear()) + "|" +
                (a.getDeathYear() == null ? "?" : a.getDeathYear());
    }
}
