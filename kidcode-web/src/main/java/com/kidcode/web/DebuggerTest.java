package com.kidcode.web;

import com.kidcode.core.ast.*;
import com.kidcode.core.evaluator.Evaluator;
import com.kidcode.core.evaluator.Environment;
import com.kidcode.core.evaluator.ExecutionContext;  // <-- Use core ExecutionContext here

import java.util.List;
import java.util.Set;
import java.util.HashSet;
import java.util.function.Supplier;

public class DebuggerTest {

    public static void main(String[] args) throws InterruptedException {
        ExecutionContext context = new ExecutionContext();

        Set<Integer> breakpoints = new HashSet<>();
        breakpoints.add(2); // breakpoint at line 2

        List<Statement> program = List.of(
            new SetStatement(new Identifier("x"), new IntegerLiteral(5)),
            new MoveStatement(new IntegerLiteral(10)),
            new TurnStatement("right", new IntegerLiteral(90)),
            new SayStatement(new StringLiteral("Done!"))
        );

        Environment env = new Environment();

        Supplier<Boolean> stopSignal = () -> context.isTerminated();

        Evaluator evaluator = new Evaluator(stopSignal, context, breakpoints);

        Thread t = new Thread(() -> {
            List<?> events = evaluator.evaluate(program, env);
            System.out.println("Program finished. Events: " + events);
        });
        t.start();

        System.out.println("Program paused at breakpoint. Waiting 2 seconds...");
        Thread.sleep(2000);

        System.out.println("Resuming execution...");
        context.resume();

        t.join();
        System.out.println("Test finished.");
    }
}
