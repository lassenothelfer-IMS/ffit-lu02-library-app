package ch.bzz;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

public class TsvBookReader {

    private TsvBookReader() {
    }

    public static List<Book> read(String filePath) throws IOException {
        List<Book> books = new ArrayList<>();

        try (BufferedReader reader = Files.newBufferedReader(Path.of(filePath), StandardCharsets.UTF_8)) {
            String line = reader.readLine(); // skip header

            while ((line = reader.readLine()) != null) {
                if (line.isBlank()) {
                    continue;
                }

                String[] fields = line.split("\t", -1);
                int id = Integer.parseInt(fields[0].trim());
                String isbn = fields[1].trim();
                String title = fields[2].trim();
                String author = fields[3].trim();
                int publicationYear = Integer.parseInt(fields[4].trim());

                books.add(new Book(id, isbn, title, author, publicationYear));
            }
        }

        return books;
    }
}
