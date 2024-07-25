package ru.job4j;

import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.List;

public class Finder {
    private static List<String> searchFiles(String directory,
                                            String name,
                                            String type) throws IOException {
        List<String> result = new ArrayList<>();
        Path startPath = Paths.get(directory);
        Files.walkFileTree(startPath, new SimpleFileVisitor<>() {
            @Override
            public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) {
                String fileName = file.getFileName().toString();
                if (matches(fileName, name, type)) {
                    result.add(file.toString());
                }
                return FileVisitResult.CONTINUE;
            }
            @Override
            public FileVisitResult visitFileFailed(Path file, IOException exc) {
                System.err.println("Failed to access file: " + file + " (" + exc.getMessage() + ")");
                return FileVisitResult.CONTINUE;
            }
        });
        return result;
    }

    private static boolean matches(String fileName, String pattern, String type) {
        return switch (type) {
            case "mask" -> matchesMask(fileName, pattern);
            case "name" -> fileName.equals(pattern);
            case "regex" -> fileName.matches(pattern);
            default -> throw new IllegalArgumentException("Unknown search type: " + type);
        };
    }

    private static boolean matchesMask(String fileName, String mask) {
        String regex = mask.replace(".", "\\.")
                .replace("*", ".*")
                .replace("?", ".");
        return fileName.matches(regex);
    }

    private static void writeToFile(List<String> result, String output) throws IOException {
        try (FileWriter writer = new FileWriter(output)) {
            for (String line : result) {
                writer.write(line + System.lineSeparator());
            }
        }
    }

    public static void main(String[] args) {
        if (args.length < 4) {
            throw new IllegalArgumentException(
                    "Usage: java Finder -d=<directory> -n=<name> -t=<type> -o=<output>");
        }
        ArgsName argsName = ArgsName.of(args);
        validateArgs(argsName);
        String directory = argsName.get("d");
        String name = argsName.get("n");
        String type = argsName.get("t");
        String output = argsName.get("o");
        try {
            List<String> result = searchFiles(directory, name, type);
            writeToFile(result, output);
        } catch (IOException e) {
            throw new RuntimeException("Error during file operation: " + e.getMessage(), e);
        }
    }

    private static void validateArgs(ArgsName argsName) {
        if (argsName.get("d") == null || argsName.get("d").isEmpty()) {
            throw new IllegalArgumentException("Directory parameter '-d' is required.");
        }
        if (argsName.get("n") == null || argsName.get("n").isEmpty()) {
            throw new IllegalArgumentException("Name parameter '-n' is required.");
        }
        if (argsName.get("t") == null || argsName.get("t").isEmpty()) {
            throw new IllegalArgumentException("Type parameter '-t' is required.");
        }
        if (argsName.get("o") == null || argsName.get("o").isEmpty()) {
            throw new IllegalArgumentException("Output parameter '-o' is required.");
        }
        if (!argsName.get("t").equals("mask")
                && !argsName.get("t").equals("name")
                && !argsName.get("t").equals("regex")) {
            throw new IllegalArgumentException(
                    "Type parameter '-t' must be one of the following: mask, name, regex.");
        }
    }
}