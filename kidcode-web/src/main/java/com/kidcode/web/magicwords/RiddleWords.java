package com.kidcode.web.magicwords;

public class RiddleWords {

    // Converts a word that looks like a number into a real number
    public static Number solve(String word) {
        if (word == null) {
            throw new IllegalArgumentException("Input cannot be null or empty");
        }

        String s = word.trim();
        if (s.isEmpty()) {
            throw new IllegalArgumentException("Input cannot be null or empty");
        }

        try {
            // Try parsing as integer
            return Integer.parseInt(s);
        } catch (NumberFormatException e1) {
            try {
                // Try parsing as double
                return Double.parseDouble(s);
            } catch (NumberFormatException e2) {
                throw new IllegalArgumentException("Input is not a valid number: " + s);
            }
        }
    }
}
