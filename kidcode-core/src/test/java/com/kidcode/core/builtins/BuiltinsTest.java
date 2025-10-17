package com.kidcode.core.builtins;

import org.junit.jupiter.api.Test;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;

public class BuiltinsTest {

    @Test
    void testCountFunction() {
        // Test with a string
        Object resultString = Builtins.apply("count", List.of("hello"));
        assertEquals(5, resultString);

        // Test with a list
        Object resultList = Builtins.apply("count", List.of(List.of(1, "two", true)));
        assertEquals(3, resultList);

        // Test with an empty string
        Object resultEmptyString = Builtins.apply("count", List.of(""));
        assertEquals(0, resultEmptyString);

        // Test with an empty list
        Object resultEmptyList = Builtins.apply("count", List.of(List.of()));
        assertEquals(0, resultEmptyList);
    }

    @Test
    void testCountFunctionErrors() {
        // Test with wrong number of arguments
        Object resultWrongArgCount = Builtins.apply("count", List.of("a", "b"));
        assertTrue(resultWrongArgCount instanceof String && ((String) resultWrongArgCount).startsWith("Error:"));

        // Test with wrong type
        Object resultWrongType = Builtins.apply("count", List.of(123));
        assertTrue(resultWrongType instanceof String && ((String) resultWrongType).startsWith("Error:"));
    }

    @Test
    void testWhisperFunction() {
        // Test with a number
        Object resultNumber = Builtins.apply("whisper", List.of(123.45));
        assertEquals("123.45", resultNumber);

        // Test with a boolean
        Object resultBoolean = Builtins.apply("whisper", List.of(true));
        assertEquals("true", resultBoolean);

        // Test with a string (should be unchanged)
        Object resultString = Builtins.apply("whisper", List.of("hello"));
        assertEquals("hello", resultString);
    }

    @Test
    void testWhisperFunctionErrors() {
        // Test with wrong number of arguments
        Object resultWrongArgCount = Builtins.apply("whisper", List.of());
        assertTrue(resultWrongArgCount instanceof String && ((String) resultWrongArgCount).startsWith("Error:"));
    }
}