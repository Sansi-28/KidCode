package com.kidcode.core.evaluator;

import com.kidcode.core.ast.*;
import com.kidcode.core.event.ExecutionEvent;
import java.util.List;
import java.util.function.Supplier;
import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.Test;

public class EvaluatorStringOpTest {

    private Evaluator makeEvaluator() {
        return new Evaluator(() -> false);
    }

    private Environment makeEnv() {
        return new Environment();
    }

    @Test
    public void plusConcatenatesStrings() {
        Evaluator ev = makeEvaluator();
        Environment env = makeEnv();
        Expression expr = new InfixExpression(new StringLiteral("A"), "+", new StringLiteral("B"));
        Object res = ev.evaluateExpression(expr, env);
        assertEquals("AB", res);
    }

    @Test
    public void minusWithStringIsError() {
        Evaluator ev = makeEvaluator();
        Environment env = makeEnv();
        Expression expr = new InfixExpression(new StringLiteral("A"), "-", new StringLiteral("B"));
        Object res = ev.evaluateExpression(expr, env);
        assertTrue(res instanceof String);
        assertTrue(((String) res).startsWith("Error:"));
    }

    @Test
    public void multiplyStringWithNumberIsError() {
        Evaluator ev = makeEvaluator();
        Environment env = makeEnv();
        Expression expr = new InfixExpression(new StringLiteral("Hello"), "*", new IntegerLiteral(2));
        Object res = ev.evaluateExpression(expr, env);
        assertTrue(res instanceof String);
        assertTrue(((String) res).startsWith("Error:"));
    }
}
