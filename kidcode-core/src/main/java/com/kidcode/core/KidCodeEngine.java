package com.kidcode.core;

import com.kidcode.core.ast.Statement;
import com.kidcode.core.evaluator.Evaluator;
import com.kidcode.core.evaluator.ExecutionContext;
import com.kidcode.core.evaluator.Environment;
import com.kidcode.core.event.ExecutionEvent;
import com.kidcode.core.lexer.Lexer;
import com.kidcode.core.parser.Parser;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Supplier;

public class KidCodeEngine {

    private volatile boolean executionStopped = false;

    public void stopExecution() {
        this.executionStopped = true;
    }

    public List<ExecutionEvent> execute(String sourceCode) {
        this.executionStopped = false;

        Lexer lexer = new Lexer(sourceCode);
        Parser parser = new Parser(lexer);
        List<Statement> program = parser.parseProgram();

        List<String> errors = parser.getErrors();
        if (!errors.isEmpty()) {
            List<ExecutionEvent> errorEvents = new ArrayList<>();
            errors.forEach(err -> errorEvents.add(new ExecutionEvent.ErrorEvent(err)));
            return errorEvents;
        }

        Supplier<Boolean> stopSignal = () -> executionStopped;
     ExecutionContext context = new ExecutionContext();
      Set<Integer> breakpoints = new HashSet<>();  // empty set for now, add breakpoints as needed
        Supplier<Boolean> stopSignal = () -> executionStopped || context.isTerminated();

        Evaluator evaluator = new Evaluator(stopSignal, context, breakpoints);
        return evaluator.evaluate(program, environment);
    }
}
