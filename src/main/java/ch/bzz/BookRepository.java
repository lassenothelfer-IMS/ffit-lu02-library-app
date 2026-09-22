package ch.bzz;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

public class BookRepository {

    private static final String CONFIG_FILE = "config.properties";
    private static final String SELECT_ALL_BOOKS =
            "SELECT id, isbn, title, author, publication_year FROM books ORDER BY id";
    private static final String UPSERT_BOOK =
            "INSERT INTO books (id, isbn, title, author, publication_year) VALUES (?, ?, ?, ?, ?) "
                    + "ON CONFLICT (id) DO UPDATE SET "
                    + "isbn = EXCLUDED.isbn, title = EXCLUDED.title, "
                    + "author = EXCLUDED.author, publication_year = EXCLUDED.publication_year";

    public List<Book> findAll() {
        List<Book> books = new ArrayList<>();

        try (Connection connection = openConnection();
             Statement statement = connection.createStatement();
             ResultSet resultSet = statement.executeQuery(SELECT_ALL_BOOKS)) {

            while (resultSet.next()) {
                books.add(new Book(
                        resultSet.getInt("id"),
                        resultSet.getString("isbn"),
                        resultSet.getString("title"),
                        resultSet.getString("author"),
                        resultSet.getInt("publication_year")));
            }
        } catch (SQLException e) {
            throw new RuntimeException("Could not read books from database", e);
        }

        return books;
    }

    public void saveAll(List<Book> books) {
        try (Connection connection = openConnection();
             PreparedStatement statement = connection.prepareStatement(UPSERT_BOOK)) {

            for (Book book : books) {
                statement.setInt(1, book.id());
                statement.setString(2, book.isbn());
                statement.setString(3, book.title());
                statement.setString(4, book.author());
                statement.setInt(5, book.publicationYear());
                statement.addBatch();
            }

            statement.executeBatch();
        } catch (SQLException e) {
            throw new RuntimeException("Could not save books to database", e);
        }
    }

    private Connection openConnection() throws SQLException {
        Properties config = loadConfig();
        return DriverManager.getConnection(
                config.getProperty("DB_URL"),
                config.getProperty("DB_USER"),
                config.getProperty("DB_PASSWORD"));
    }

    private Properties loadConfig() {
        Properties properties = new Properties();
        try (InputStream input = Files.newInputStream(Path.of(CONFIG_FILE))) {
            properties.load(input);
        } catch (IOException e) {
            throw new RuntimeException(
                    "Could not read " + CONFIG_FILE + ". Copy config.properties.template to "
                            + CONFIG_FILE + " and set the database connection details.", e);
        }
        return properties;
    }
}
