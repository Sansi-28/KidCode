package com.kidcode.web.magicwords;

public class RiddleWords {

    // Converts a word that looks like a number into a real number
    public static Number solve(String word) {
        if (word == null || word.isEmpty()) {
            throw new IllegalArgumentException("Input cannot be null or empty");
        }

        try {
            // Try parsing as integer
            return Integer.parseInt(word);
        } catch (NumberFormatException e1) {
            try {
                // Try parsing as double
                return Double.parseDouble(word);
            } catch (NumberFormatException e2) {
                throw new IllegalArgumentException("Input is not a valid number: " + word);
            }
        }
    }
}
