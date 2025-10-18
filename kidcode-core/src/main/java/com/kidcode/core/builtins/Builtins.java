package com.kidcode.core.builtins;

import java.util.ArrayList;
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
        BUILTINS = Map.ofEntries(
            Map.entry("count", new Builtin(Builtins::countFunction)),
            Map.entry("whisper", new Builtin(Builtins::whisperFunction)),
            Map.entry("pack", new Builtin(Builtins::packFunction)),
            Map.entry("front", new Builtin(Builtins::frontFunction)),
            Map.entry("back", new Builtin(Builtins::backFunction)),
            Map.entry("solve", new Builtin(Builtins::solveFunction))
        );
    }

    /**
     * Checks if a function name corresponds to a built-in function.
     * @param name The name of the function.
     * @return true if it's a built-in, false otherwise.
     */
    public static boolean isBuiltin(String name) {
        return BUILTINS.containsKey(name);
    }

    /**
     * Applies a built-in function by name to a list of arguments.
     * @param name The name of the built-in function.
     * @param args The list of evaluated arguments.
     * @return The result of the function call, or an error string.
     */
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

    // 🧳 Backpack Magic Words

    private static Object packFunction(List<Object> args) {
        if (args.size() != 2) {
            return "Error: pack() expects 2 arguments: a list and an item.";
        }
        Object backpack = args.get(0);
        Object item = args.get(1);

        if (backpack == null) {
            backpack = new ArrayList<>();
        }

        if (!(backpack instanceof List<?> list)) {
            return "Error: pack() expects the first argument to be a list.";
        }

        List<Object> newList = new ArrayList<>(list);
        newList.add(item);
        return newList;
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
            return "Error: front() cannot be used on an empty list.";
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
            return "Error: back() cannot be used on an empty list.";
        }
        return list.get(list.size() - 1);
    }

    // 🧮 Solve Function

    private static Object solveFunction(List<Object> args) {
        if (args.size() != 1) {
            return "Error: solve() expects exactly 1 argument (a string).";
        }
        Object arg = args.get(0);
        if (!(arg instanceof String s)) {
            return "Error: solve() expects a string input.";
        }

        s = s.trim();
        try {
            return Double.parseDouble(s);
        } catch (NumberFormatException e) {
            return "Error: solve() cannot convert input to a number.";
        }
    }
}
