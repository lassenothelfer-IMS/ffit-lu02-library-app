package ch.bzz;

import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.function.Consumer;

public class LibraryAppMain {

    private static final BookRepository bookRepository = new BookRepository();

    public static void main(String[] args) {
        var running = new boolean[]{true};
        Map<String, Command> commands = buildCommands(running);

        Scanner scanner = new Scanner(System.in);
        while (running[0]) {
            System.out.print("> ");
            String input = scanner.nextLine().trim();
            String[] parts = input.split("\\s+", 2);
            String commandName = parts[0];
            String argument = parts.length > 1 ? parts[1] : null;

            Command command = commands.get(commandName);
            if (command != null) {
                command.execute(argument);
            } else {
                System.out.println("Unknown command: '" + input + "'. Type 'help' to list all available commands.");
            }
        }
    }

    private static Map<String, Command> buildCommands(boolean[] running) {
        Map<String, Command> commands = new LinkedHashMap<>();
        commands.put("help", new Command("help", "Lists all available commands", arg -> printHelp(commands)));
        commands.put("listBooks", new Command("listBooks", "Lists all available books", arg -> listBooks()));
        commands.put("importBooks", new Command("importBooks <FILE_PATH>",
                "Imports books from a TSV file into the database", LibraryAppMain::importBooks));
        commands.put("quit", new Command("quit", "Ends the program", arg -> running[0] = false));
        return commands;
    }

    private static void printHelp(Map<String, Command> commands) {
        System.out.println("Available commands:");
        commands.values().forEach(command ->
                System.out.println("  " + command.name() + " - " + command.description()));
    }

    private static void listBooks() {
        bookRepository.findAll().forEach(book ->
                System.out.println(book.title() + " (" + book.author() + ", " + book.publicationYear() + ")"));
    }

    private static void importBooks(String filePath) {
        if (filePath == null || filePath.isBlank()) {
            System.out.println("Usage: importBooks <FILE_PATH>");
            return;
        }

        try {
            List<Book> books = TsvBookReader.read(filePath);
            bookRepository.saveAll(books);
            System.out.println("Imported " + books.size() + " book(s) from " + filePath);
        } catch (IOException e) {
            System.out.println("Could not import books from '" + filePath + "': " + e.getMessage());
        }
    }

    private record Command(String name, String description, Consumer<String> action) {
        void execute(String argument) {
            action.accept(argument);
        }
    }
}
