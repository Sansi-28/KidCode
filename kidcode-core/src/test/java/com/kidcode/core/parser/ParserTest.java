package com.kidcode.core.parser;

import com.kidcode.core.ast.HomeStatement;
import com.kidcode.core.ast.Statement;
import com.kidcode.core.lexer.Lexer;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class ParserTest {

    @Test
    public void testHomeStatement() {
        String input = "home";
        Lexer lexer = new Lexer(input);
        Parser parser = new Parser(lexer);
        List<Statement> program = parser.parseProgram();

        assertTrue(parser.getErrors().isEmpty(), "Parser should have no errors");
        assertNotNull(program, "parseProgram() should not return null");
        assertEquals(1, program.size(), "Program should contain 1 statement");

        Statement stmt = program.get(0);
        assertInstanceOf(HomeStatement.class, stmt, "Statement should be a HomeStatement");
    }
}