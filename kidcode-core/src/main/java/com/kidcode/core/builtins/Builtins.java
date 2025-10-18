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
            "whisper", new Builtin(Builtins::whisperFunction)
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
            // This case should ideally not be reached if isBuiltin is checked first
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
}