package ru.job4j;

import java.util.HashMap;
import java.util.Map;

public class ArgsName {
    private final Map<String, String> values = new HashMap<>();

    public String get(String key) {
        if (!values.containsKey(key)) {
            throw new IllegalArgumentException("This key: '" + key + "' is missing");
        }
        return values.get(key);
    }

    private void parse(String[] args) {
        for (String arg : args) {
            if (!arg.startsWith("-")) {
                throw new IllegalArgumentException(
                        "Error: This argument '" + arg + "' does not start with a '-' character");
            }
            if (!arg.contains("=")) {
                throw new IllegalArgumentException(
                        "Error: This argument '" + arg + "' does not contain an equal sign");
            }
            int equals = arg.indexOf("=");
            if (equals < 2) {
                throw new IllegalArgumentException(
                        "Error: This argument '" + arg + "' does not contain a key");
            }
            String value = arg.substring(equals + 1);
            if (value.isEmpty()) {
                throw new IllegalArgumentException(
                        "Error: This argument '" + arg + "' does not contain a value");
            }
            values.put(arg.substring(1, equals), value);
        }
    }

    public static ArgsName of(String[] args) {
        if (args.length == 0) {
            throw new IllegalArgumentException("Arguments not passed to program");
        }
        ArgsName names = new ArgsName();
        names.parse(args);
        return names;
    }
}
