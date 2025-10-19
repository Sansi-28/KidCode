package com.kidcode.core.builtins;

import java.util.List;
import java.util.Map;
import java.util.function.Function;

public final class Builtins {

    // Private constructor to prevent instantiation
    private Builtins() {}

    // Record to represent a built-in function
    private record Builtin(Function<List<Object>, Object> function) {}
    private static final Map<String, Builtin> BUILTINS;

    static {
        BUILTINS = Map.of(
            "count", new Builtin(Builtins::countFunction),
            "whisper", new Builtin(Builtins::whisperFunction),
            "pack", new Builtin(Builtins::packFunction),
            "front", new Builtin(Builtins::frontFunction),
            "back", new Builtin(Builtins::backFunction),
            "after", new Builtin(Builtins::afterFunction),
            "find", new Builtin(Builtins::findFunction),
            "solve", new Builtin(Builtins::solveFunction)
        );
    }

    public static boolean isBuiltin(String name) {
        return BUILTINS.containsKey(name);
    }

    public static Object apply(String name, List<Object> args) {
        Builtin builtin = BUILTINS.get(name);
        if (builtin == null) {
            return "Error: Unknown built-in function '" + name + "'.";
        }
        return builtin.function().apply(args);
    }

    // --- Built-in Function Implementations ---

    private static Object countFunction(List<Object> args) {
        if (args.size() != 1) {
            return "Error: count() expects exactly 1 argument, but got " + args.size();
        }
        Object target = args.get(0);
        if (target instanceof String s) { return s.length(); }
        if (target instanceof List<?> l) { return l.size(); }
        return "Error: count() can only be used on a list or a string.";
    }

    private static Object whisperFunction(List<Object> args) {
        if (args.size() != 1) {
            return "Error: whisper() expects exactly 1 argument, but got " + args.size();
        }
        return String.valueOf(args.get(0));
    }

    private static Object packFunction(List<Object> args) {
        if (args.size() != 2) {
            return "Error: pack() expects 2 arguments: a list and an item.";
        }
        Object listObj = args.get(0);
        Object item = args.get(1);
        if (!(listObj instanceof List<Object> list)) {
            return "Error: pack() expects a list as the first argument.";
        }
        if (item == null) {
            return "Error: cannot pack a null item.";
        }
        list.add(item);
        return list;
    }

    private static Object frontFunction(List<Object> args) {
        if (args.size() != 1) {
            return "Error: front() expects exactly 1 argument: a list.";
        }
        Object backpack = args.get(0);
        if (!(backpack instanceof List<?> list)) {
            return "Error: front() expects a list.";
        }
        if (list.isEmpty()) {
            return null; // Return null for empty list
        }
        return list.get(0);
    }

    private static Object backFunction(List<Object> args) {
        if (args.size() != 1) {
            return "Error: back() expects exactly 1 argument: a list.";
        }
        Object backpack = args.get(0);
        if (!(backpack instanceof List<?> list)) {
            return "Error: back() expects a list.";
        }
        if (list.isEmpty()) {
            return null; // Return null for empty list
        }
        return list.get(list.size() - 1);
    }

    private static Object afterFunction(List<Object> args) {
        if (args.size() != 1) {
            return "Error: after() expects exactly 1 argument: a list.";
        }
        Object backpack = args.get(0);
        if (!(backpack instanceof List<?> list)) {
            return "Error: after() expects a list.";
        }
        if (list.isEmpty()) {
            return List.of(); // Return empty list
        }
        return list.subList(1, list.size());
    }

    private static Object findFunction(List<Object> args) {
        if (args.size() != 2) {
            return "Error: find() expects 2 arguments: a list and an item to find.";
        }
        Object backpack = args.get(0);
        Object item = args.get(1);
        if (!(backpack instanceof List<?> list)) {
            return "Error: find() expects a list as the first argument.";
        }
        return list.contains(item);
    }

    private static Object solveFunction(List<Object> args) {
        if (args.size() != 1) {
            return "Error: solve() expects exactly 1 argument (a string).";
        }
        Object arg = args.get(0);
        if (!(arg instanceof String s)) {
            return "Error: solve() expects a string input.";
        }

        s = s.trim();
        if (s.isEmpty()) {
            return "Error: solve() cannot convert empty string to a number.";
        }

        try {
            if (!s.contains(".") && !s.toLowerCase().contains("e")) {
                return Integer.parseInt(s);
            }
            return Double.parseDouble(s);
        } catch (NumberFormatException e) {
            return "Error: solve() cannot convert input to a number.";
        }
    }
}
