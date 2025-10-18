package com.kidcode.core.evaluator;

import com.kidcode.core.ast.Statement;
import com.kidcode.core.event.ExecutionEvent;
import com.kidcode.core.lexer.Lexer;
import com.kidcode.core.parser.Parser;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class EvaluatorTest {

    private Evaluator makeEvaluator() {
        return new Evaluator(() -> false);
    }

    @Test
    public void testHomeCommandResetsPositionAndLiftsPen() {
        String input = "home";
        Lexer lexer = new Lexer(input);
        Parser parser = new Parser(lexer);
        List<Statement> program = parser.parseProgram();

        Evaluator evaluator = makeEvaluator();
        Environment env = new Environment();
        // Move Cody away from the center first
        env.setPosition(100, 150);

        List<ExecutionEvent> events = evaluator.evaluate(program, env);

        // Check the environment state
        assertEquals(250, env.getX(), "Environment X should be reset to 250");
        assertEquals(250, env.getY(), "Environment Y should be reset to 250");

        // Check the generated event (the last one, after initial setup events)
        ExecutionEvent.MoveEvent lastMoveEvent = (ExecutionEvent.MoveEvent) events.get(events.size() - 1);
        assertEquals(100, lastMoveEvent.fromX());
        assertEquals(150, lastMoveEvent.fromY());
        assertEquals(250, lastMoveEvent.toX());
        assertEquals(250, lastMoveEvent.toY());
        assertFalse(lastMoveEvent.isPenDown(), "Pen should be up for the home command move");
    }
}