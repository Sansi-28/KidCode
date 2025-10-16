package com.kidcode.core.lexer;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class LexerTest {
    @Test
    void testSimpleInput() {
        Lexer lexer = new Lexer("move forward 10");
        List<Token> tokens = lexer.allTokens();

        TokenType[] expectedTypes = {TokenType.MOVE, TokenType.FORWARD, TokenType.NUMBER, TokenType.EOF};
        String[] expectedLiterals = {"move", "forward", "10", ""};

        for (int i = 0; i < expectedTypes.length; i++) {
            assertEquals(expectedTypes[i], tokens.get(i).type(), "Type mismatch at index " + i);
            assertEquals(expectedLiterals[i], tokens.get(i).literal(), "Literal mismatch at index " + i);
        }
    }
}
