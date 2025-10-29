package com.kidcode.core.parser;

import com.kidcode.core.ast.*;
import com.kidcode.core.lexer.Lexer;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Comprehensive tests for Parser edge cases and error handling
 */
public class ParserEdgeCaseTest {

    @Test
    public void testEmptyProgram() {
        String input = "";
        Lexer lexer = new Lexer(input);
        Parser parser = new Parser(lexer);
        List<Statement> program = parser.parseProgram();

        assertTrue(parser.getErrors().isEmpty(), "Empty program should have no errors");
        assertNotNull(program, "parseProgram() should not return null");
        assertEquals(0, program.size(), "Empty program should contain 0 statements");
    }

    @Test
    public void testWhitespaceOnlyProgram() {
        String input = "   \n\n  \t  \n  ";
        Lexer lexer = new Lexer(input);
        Parser parser = new Parser(lexer);
        List<Statement> program = parser.parseProgram();

        assertTrue(parser.getErrors().isEmpty(), "Whitespace-only program should have no errors");
        assertEquals(0, program.size(), "Whitespace-only program should contain 0 statements");
    }

    @Test
    public void testCommentOnlyProgram() {
        String input = "# This is a comment\n# Another comment";
        Lexer lexer = new Lexer(input);
        Parser parser = new Parser(lexer);
        List<Statement> program = parser.parseProgram();

        assertTrue(parser.getErrors().isEmpty(), "Comment-only program should have no errors");
        assertEquals(0, program.size(), "Comment-only program should contain 0 statements");
    }

    @Test
    public void testInvalidTokenAtStart() {
        String input = "@ invalid token";
        Lexer lexer = new Lexer(input);
        Parser parser = new Parser(lexer);
        List<Statement> program = parser.parseProgram();

        assertFalse(parser.getErrors().isEmpty(), "Invalid token should generate errors");
        assertTrue(parser.getErrors().get(0).contains("Invalid start of a statement"),
                "Error message should mention invalid start");
    }

    @Test
    public void testMissingExpressionInMove() {
        String input = "move forward";
        Lexer lexer = new Lexer(input);
        Parser parser = new Parser(lexer);
        List<Statement> program = parser.parseProgram();

        assertFalse(parser.getErrors().isEmpty(), "Missing expression should generate error");
    }

    @Test
    public void testMissingDirectionInTurn() {
        String input = "turn 90";
        Lexer lexer = new Lexer(input);
        Parser parser = new Parser(lexer);
        List<Statement> program = parser.parseProgram();

        assertFalse(parser.getErrors().isEmpty(), "Missing direction should generate error");
    }

    @Test
    public void testInvalidDirectionInTurn() {
        String input = "turn forward 90";
        Lexer lexer = new Lexer(input);
        Parser parser = new Parser(lexer);
        List<Statement> program = parser.parseProgram();

        assertFalse(parser.getErrors().isEmpty(), "Invalid direction should generate error");
    }

    @Test
    public void testMissingPenDirection() {
        String input = "pen";
        Lexer lexer = new Lexer(input);
        Parser parser = new Parser(lexer);
        List<Statement> program = parser.parseProgram();

        assertFalse(parser.getErrors().isEmpty(), "Missing pen direction should generate error");
    }

    @Test
    public void testInvalidPenDirection() {
        String input = "pen left";
        Lexer lexer = new Lexer(input);
        Parser parser = new Parser(lexer);
        List<Statement> program = parser.parseProgram();

        assertFalse(parser.getErrors().isEmpty(), "Invalid pen direction should generate error");
    }

    @Test
    public void testUnterminatedString() {
        String input = "say \"hello";
        Lexer lexer = new Lexer(input);
        Parser parser = new Parser(lexer);
        List<Statement> program = parser.parseProgram();

        // This should parse but the string might be incomplete
        assertNotNull(program);
    }

    @Test
    public void testNestedRepeatStatements() {
        String input = """
            repeat 3
                repeat 2
                    move forward 10
                end
            end
            """;
        Lexer lexer = new Lexer(input);
        Parser parser = new Parser(lexer);
        List<Statement> program = parser.parseProgram();

        assertTrue(parser.getErrors().isEmpty(), "Nested repeat should be valid");
        assertEquals(1, program.size());
        assertInstanceOf(RepeatStatement.class, program.get(0));
        
        RepeatStatement outer = (RepeatStatement) program.get(0);
        assertEquals(1, outer.body().size());
        assertInstanceOf(RepeatStatement.class, outer.body().get(0));
    }

    @Test
    public void testRepeatWithoutEnd() {
        String input = "repeat 3\n    move forward 10";
        Lexer lexer = new Lexer(input);
        Parser parser = new Parser(lexer);
        List<Statement> program = parser.parseProgram();

        assertFalse(parser.getErrors().isEmpty(), "Repeat without 'end' should generate error");
    }

    @Test
    public void testIfWithoutEnd() {
        String input = "if x == 5\n    move forward 10";
        Lexer lexer = new Lexer(input);
        Parser parser = new Parser(lexer);
        List<Statement> program = parser.parseProgram();

        assertFalse(parser.getErrors().isEmpty(), "If without 'end' should generate error");
    }

    @Test
    public void testMismatchedParentheses() {
        String input = "set x = (5 + 3";
        Lexer lexer = new Lexer(input);
        Parser parser = new Parser(lexer);
        List<Statement> program = parser.parseProgram();

        assertFalse(parser.getErrors().isEmpty(), "Mismatched parentheses should generate error");
    }

    @Test
    public void testMismatchedBrackets() {
        String input = "set list = [1, 2, 3";
        Lexer lexer = new Lexer(input);
        Parser parser = new Parser(lexer);
        List<Statement> program = parser.parseProgram();

        assertFalse(parser.getErrors().isEmpty(), "Mismatched brackets should generate error");
    }

    @Test
    public void testDivisionByZeroExpression() {
        String input = "set x = 10 / 0";
        Lexer lexer = new Lexer(input);
        Parser parser = new Parser(lexer);
        List<Statement> program = parser.parseProgram();

        // Parser should succeed - division by zero is a runtime error
        assertTrue(parser.getErrors().isEmpty(), "Division by zero is runtime error, not parse error");
        assertEquals(1, program.size());
        assertInstanceOf(SetStatement.class, program.get(0));
    }

    @Test
    public void testComplexExpression() {
        String input = "set result = (5 + 3) * 2 - 10 / 2";
        Lexer lexer = new Lexer(input);
        Parser parser = new Parser(lexer);
        List<Statement> program = parser.parseProgram();

        assertTrue(parser.getErrors().isEmpty(), "Complex expression should parse correctly");
        assertEquals(1, program.size());
        assertInstanceOf(SetStatement.class, program.get(0));
    }

    @Test
    public void testListWithMixedTypes() {
        String input = "set mixed = [1, \"hello\", 42]";
        Lexer lexer = new Lexer(input);
        Parser parser = new Parser(lexer);
        List<Statement> program = parser.parseProgram();

        assertTrue(parser.getErrors().isEmpty(), "List with mixed types should parse");
        assertEquals(1, program.size());
        assertInstanceOf(SetStatement.class, program.get(0));
    }

    @Test
    public void testEmptyList() {
        String input = "set empty = []";
        Lexer lexer = new Lexer(input);
        Parser parser = new Parser(lexer);
        List<Statement> program = parser.parseProgram();

        assertTrue(parser.getErrors().isEmpty(), "Empty list should parse correctly");
        assertEquals(1, program.size());
    }

    @Test
    public void testFunctionCallWithNoArguments() {
        String input = "my_function()";
        Lexer lexer = new Lexer(input);
        Parser parser = new Parser(lexer);
        List<Statement> program = parser.parseProgram();

        assertTrue(parser.getErrors().isEmpty(), "Function call with no args should parse");
        assertEquals(1, program.size());
        assertInstanceOf(FunctionCallStatement.class, program.get(0));
    }

    @Test
    public void testFunctionDefinitionEmpty() {
        String input = """
            define my_func()
            end
            """;
        Lexer lexer = new Lexer(input);
        Parser parser = new Parser(lexer);
        List<Statement> program = parser.parseProgram();

        assertTrue(parser.getErrors().isEmpty(), "Empty function should parse");
        assertEquals(1, program.size());
        assertInstanceOf(FunctionDefinitionStatement.class, program.get(0));
    }

    @Test
    public void testMultipleStatementsOnMultipleLines() {
        String input = """
            move forward 10
            turn left 90
            say "Hello"
            home
            """;
        Lexer lexer = new Lexer(input);
        Parser parser = new Parser(lexer);
        List<Statement> program = parser.parseProgram();

        assertTrue(parser.getErrors().isEmpty(), "Multiple statements should parse");
        assertEquals(4, program.size());
        assertInstanceOf(MoveStatement.class, program.get(0));
        assertInstanceOf(TurnStatement.class, program.get(1));
        assertInstanceOf(SayStatement.class, program.get(2));
        assertInstanceOf(HomeStatement.class, program.get(3));
    }

    @Test
    public void testComparisonOperators() {
        String input = """
            set a = 5 > 3
            set b = 10 < 20
            set c = 5 == 5
            set d = 3 != 4
            """;
        Lexer lexer = new Lexer(input);
        Parser parser = new Parser(lexer);
        List<Statement> program = parser.parseProgram();

        assertTrue(parser.getErrors().isEmpty(), "Comparison operators should parse");
        assertEquals(4, program.size());
    }

    @Test
    public void testStringConcatenation() {
        String input = "set greeting = \"Hello\" + \" \" + \"World\"";
        Lexer lexer = new Lexer(input);
        Parser parser = new Parser(lexer);
        List<Statement> program = parser.parseProgram();

        assertTrue(parser.getErrors().isEmpty(), "String concatenation should parse");
        assertEquals(1, program.size());
    }

    @Test
    public void testIndexExpression() {
        String input = "set item = my_list[0]";
        Lexer lexer = new Lexer(input);
        Parser parser = new Parser(lexer);
        List<Statement> program = parser.parseProgram();

        assertTrue(parser.getErrors().isEmpty(), "Index expression should parse");
        assertEquals(1, program.size());
    }

    @Test
    public void testColorStatement() {
        String input = "color \"red\"";
        Lexer lexer = new Lexer(input);
        Parser parser = new Parser(lexer);
        List<Statement> program = parser.parseProgram();

        assertTrue(parser.getErrors().isEmpty(), "Color statement should parse");
        assertEquals(1, program.size());
        assertInstanceOf(SetColorStatement.class, program.get(0));
    }
}
