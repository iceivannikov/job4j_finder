package ru.job4j;

import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.List;

public class Finder {
    public static void main(String[] args) {
        if (args.length < 4) {
            throw new IllegalArgumentException("Usage: java Finder -d=<directory> -n=<name> -t=<type> -o=<output>");
        }

        String directory = null;
        String name = null;
        String type = null;
        String output = null;

        for (String arg : args) {
            if (arg.startsWith("-d=")) {
                directory = arg.substring(3);
            } else if (arg.startsWith("-n=")) {
                name = arg.substring(3);
            } else if (arg.startsWith("-t=")) {
                type = arg.substring(3);
            } else if (arg.startsWith("-o=")) {
                output = arg.substring(3);
            }
        }

        if (directory == null || name == null || type == null || output == null) {
            throw new IllegalArgumentException("All parameters -d, -n, -t, and -o are required.");
        }

        try {
            // Поиск файлов в указанной директории
            List<String> result = searchFiles(directory, name, type);
            writeToFile(result, output);
        } catch (IOException e) {
            throw new RuntimeException("Error during file operation: " + e.getMessage(), e);
        }
    }

    private static List<String> searchFiles(String directory, String name, String type) throws IOException {
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
        try (FileWriter writer = new FileWriter(output, false)) {
            for (String line : result) {
                writer.write(line + System.lineSeparator());
            }
        }
    }
}