package ch.bzz;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Scanner;

public class LibraryAppMain {

    public static void main(String[] args) {
        var running = new boolean[]{true};
        Map<String, Command> commands = buildCommands(running);

        Scanner scanner = new Scanner(System.in);
        while (running[0]) {
            System.out.print("> ");
            String input = scanner.nextLine().trim();

            Command command = commands.get(input);
            if (command != null) {
                command.execute();
            } else {
                System.out.println("Unknown command: '" + input + "'. Type 'help' to list all available commands.");
            }
        }
    }

    private static Map<String, Command> buildCommands(boolean[] running) {
        Map<String, Command> commands = new LinkedHashMap<>();
        commands.put("help", new Command("help", "Lists all available commands", () -> printHelp(commands)));
        commands.put("quit", new Command("quit", "Ends the program", () -> running[0] = false));
        return commands;
    }

    private static void printHelp(Map<String, Command> commands) {
        System.out.println("Available commands:");
        commands.values().forEach(command ->
                System.out.println("  " + command.name() + " - " + command.description()));
    }

    private record Command(String name, String description, Runnable action) {
        void execute() {
            action.run();
        }
    }
}
