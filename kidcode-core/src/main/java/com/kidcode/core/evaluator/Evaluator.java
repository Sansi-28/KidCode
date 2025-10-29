package com.kidcode.core.evaluator;

import com.kidcode.core.ast.*;
import com.kidcode.core.builtins.Builtins;
import com.kidcode.core.event.ExecutionEvent;

import java.util.List;
import java.util.ArrayList;
import java.util.function.Supplier;

public class Evaluator {
    private static final int INSTRUCTION_LIMIT = 1_000_000;
    private int instructionCount = 0;
    private final Supplier<Boolean> stopSignal;
    private final List<ExecutionEvent> events = new ArrayList<>();

    public Evaluator(Supplier<Boolean> stopSignal) {
        this.stopSignal = stopSignal;
    }

    public List<ExecutionEvent> evaluate(List<Statement> program, Environment env) {
        instructionCount = 0;
        events.clear();
        events.add(new ExecutionEvent.ClearEvent());
        events.add(new ExecutionEvent.MoveEvent(
            env.getX(), env.getY(), env.getX(), env.getY(),
            env.getDirection(), env.isPenDown(), env.getPenColor()));
        for (Statement statement : program) {
            if (stopSignal.get()) break;
            evaluateStatement(statement, env);
        }
        return events;
    }

    public List<ExecutionEvent> getEvents() {
        return List.copyOf(events);
    }

    private void evaluateStatement(Statement stmt, Environment env) {
        if (stopSignal.get() || ++instructionCount > INSTRUCTION_LIMIT) {
            if(instructionCount > INSTRUCTION_LIMIT) {
                events.add(new ExecutionEvent.ErrorEvent("Execution timed out! Possible infinite loop."));
            }
            return;
        }
        if (stmt instanceof SetStatement setStmt) {
            Object value = evaluateExpression(setStmt.value(), env);
            if (isError(value)) {
                events.add(new ExecutionEvent.ErrorEvent((String) value));
            } else {
                env.set(setStmt.name().value(), value);
            }
        } else if (stmt instanceof MoveStatement moveStmt) {
            Object stepsVal = evaluateExpression(moveStmt.steps(), env);
            if (!(stepsVal instanceof Integer steps)) {
                events.add(new ExecutionEvent.SayEvent("Error: 'move forward' requires a number. Got: " + stepsVal));
                return;
            }
            int oldX = env.getX();
            int oldY = env.getY();
            int newX = oldX + (int) (steps * Math.sin(Math.toRadians(env.getDirection())));
            int newY = oldY - (int) (steps * Math.cos(Math.toRadians(env.getDirection())));
            env.setPosition(newX, newY);
            events.add(new ExecutionEvent.MoveEvent(oldX, oldY, newX, newY, env.getDirection(), env.isPenDown(), env.getPenColor()));
        } else if (stmt instanceof TurnStatement turnStmt) {
            Object degreesVal = evaluateExpression(turnStmt.degrees(), env);
            if (!(degreesVal instanceof Integer degrees)) {
                events.add(new ExecutionEvent.SayEvent("Error: 'turn' requires a number. Got: " + degreesVal));
                return;
            }
            if (turnStmt.direction().equalsIgnoreCase("right")) {
                env.setDirection((env.getDirection() + degrees) % 360);
            } else {
                env.setDirection((env.getDirection() - degrees + 360) % 360);
            }
            events.add(new ExecutionEvent.MoveEvent(env.getX(), env.getY(), env.getX(), env.getY(), env.getDirection(), env.isPenDown(), env.getPenColor()));
        } else if (stmt instanceof PenStatement penStmt) {
            env.setPenDown(penStmt.state().equalsIgnoreCase("down"));
            events.add(new ExecutionEvent.MoveEvent(env.getX(), env.getY(), env.getX(), env.getY(), env.getDirection(), env.isPenDown(), env.getPenColor()));
        } else if (stmt instanceof SetColorStatement colorStmt) {
            Object colorVal = evaluateExpression(colorStmt.colorName(), env);
            if (!(colorVal instanceof String colorName)) {
                events.add(new ExecutionEvent.SayEvent("Error: 'color' requires a string color name."));
                return;
            }
            if (!isSupportedColor(colorName)) {
                events.add(new ExecutionEvent.SayEvent("Error: Unknown color '" + colorName + "'."));
                return;
            }
            env.setPenColor(colorName.toLowerCase());
            events.add(new ExecutionEvent.MoveEvent(env.getX(), env.getY(), env.getX(), env.getY(), env.getDirection(), env.isPenDown(), env.getPenColor()));
        } else if (stmt instanceof SayStatement sayStmt) {
            Object messageObj = evaluateExpression(sayStmt.message(), env);
            if (isError(messageObj)) {
                events.add(new ExecutionEvent.ErrorEvent((String) messageObj));  // Use ErrorEvent for consistency
            } else {
                events.add(new ExecutionEvent.SayEvent(String.valueOf(messageObj)));
            }
        } else if (stmt instanceof RepeatStatement repeatStmt) {
            Object timesVal = evaluateExpression(repeatStmt.times(), env);
            if (!(timesVal instanceof Integer times)) {
                events.add(new ExecutionEvent.SayEvent("Error: 'repeat' requires a number."));
                return;
            }
            for (int i = 0; i < times; i++) {
                if (stopSignal.get()) return;
                for (Statement bodyStatement : repeatStmt.body()) {
                    evaluateStatement(bodyStatement, env);
                }
            }
        } else if (stmt instanceof IfStatement ifStmt) {
            Object cond = evaluateExpression(ifStmt.condition(), env);
            if (isError(cond)) {
                events.add(new ExecutionEvent.SayEvent((String) cond));
                return;
            }
            boolean condVal = (cond instanceof Boolean b && b) || (cond instanceof Integer i && i != 0);
            if (condVal) {
                for (Statement bodyStatement : ifStmt.consequence()) {
                    evaluateStatement(bodyStatement, env);
                }
            } else if (ifStmt.alternative() != null) {
                for (Statement bodyStatement : ifStmt.alternative()) {
                    evaluateStatement(bodyStatement, env);
                }
            }
        } else if (stmt instanceof FunctionDefinitionStatement funcDefStmt) {
            env.defineFunction(funcDefStmt.name().value(), funcDefStmt);
        } else if (stmt instanceof FunctionCallStatement funcCallStmt) {
            evaluateFunctionCall(funcCallStmt, env);
        } else if (stmt instanceof ExpressionStatement exprStmt) {
            evaluateExpression(exprStmt.expression(), env);
        } else if (stmt instanceof HomeStatement) {
            int oldX = env.getX();
            int oldY = env.getY();
            int newX = 250;
            int newY = 250;
            env.setPosition(newX, newY);
            // Add a move event, but with the pen up so it doesn't draw a line.
            events.add(new ExecutionEvent.MoveEvent(oldX, oldY, newX, newY, env.getDirection(), false, env.getPenColor()));
        } else if (stmt instanceof ClearStatement) {
            // Emit a clear event to clear the canvas
            events.add(new ExecutionEvent.ClearEvent());
        }
    }

    /**
     * Checks if the given color name is supported by KidCode.
     * 
     * @param colorName the color name to check (case-insensitive)
     * @return true if the color is supported, false otherwise
     */
    private boolean isSupportedColor(String colorName) {
        if (colorName == null) {
            return false;
        }
        return switch (colorName.toLowerCase()) {
            case "red", "green", "blue", "yellow", "orange", "purple", "black", "white",
                 "cyan", "magenta", "pink", "brown" -> true;
            default -> false;
        };
    }

    Object evaluateExpression(Expression expr, Environment env) {
        if (expr instanceof IntegerLiteral i) {
            return i.value();
        }
        if (expr instanceof StringLiteral s) {
            return s.value();
        }
        if (expr instanceof FunctionCallExpression funcCall) {
            if (Builtins.isBuiltin(funcCall.function().value())) {
                return applyBuiltinFunction(funcCall, env);
            }
            // User-defined functions that return values are not yet supported
            return "Error: function '" + funcCall.function().value() + "' is not defined.";
        }
        if (expr instanceof Identifier id) {
            Object value = env.get(id.value());
            if (value == null) {
                return "Error: variable '" + id.value() + "' not found.";
            }
            return value;
        }
        if (expr instanceof InfixExpression infix) {
            Object left = evaluateExpression(infix.left(), env);
            if (isError(left)) return left;
            Object right = evaluateExpression(infix.right(), env);
            if (isError(right)) return right;
            // If either side is a string, only allow + for concatenation. Other
            // operators should be reported as errors (e.g., "Hello" * 2 is invalid).
            if (left instanceof String || right instanceof String) {
                if ("+".equals(infix.operator())) {
                    return String.valueOf(left) + String.valueOf(right);
                }
                return "Error: Cannot use '" + infix.operator() + "' with a string.";
            }
            if (left instanceof Integer l && right instanceof Integer r) {
                return switch (infix.operator()) {
                    case "+" -> l + r;
                    case "-" -> l - r;
                    case "*" -> l * r;
                    case "/" -> (r == 0) ? "Error: Division by zero" : l / r;
                    case "==" -> l.equals(r);
                    case "!=" -> !l.equals(r);
                    case ">" -> l > r;
                    case "<" -> l < r;
                    default -> "Error: Unknown operator '" + infix.operator() + "' for numbers.";
                };
            }
            return "Error: Cannot perform operation '" + infix.operator() + "' on these types.";
        }
        if (expr instanceof ListLiteral listLiteral) {
            List<Object> elements = new ArrayList<>();
            for (Expression el : listLiteral.elements()) {
                Object evaluated = evaluateExpression(el, env);
                if (isError(evaluated)) return evaluated;
                elements.add(evaluated);
            }
            return elements;
        }
        if (expr instanceof IndexExpression indexExpr) {
            Object left = evaluateExpression(indexExpr.left(), env);
            if (isError(left)) return left;
            Object index = evaluateExpression(indexExpr.index(), env);
            if (isError(index)) return index;
            if (!(left instanceof List)) {
                return "Error: index operator [] cannot be used on non-list type.";
            }
            if (!(index instanceof Integer)) {
                return "Error: index must be a number.";
            }
            List<Object> list = (List<Object>) left;
            int idx = (Integer) index;
            if (idx < 0 || idx >= list.size()) {
                return "Error: index " + idx + " out of bounds for list of size " + list.size() + ".";
            }
            return list.get(idx);
        }
        return "Error: Cannot evaluate expression";
    }
    
    private boolean isError(Object obj) {
        return obj instanceof String s && s.startsWith("Error:");
    }

    private Object applyBuiltinFunction(FunctionCallExpression call, Environment env) {
        String funcName = call.function().value();

        List<Object> args = new ArrayList<>();
        for (Expression argExpr : call.arguments()) {
            Object evaluated = evaluateExpression(argExpr, env);
            if (isError(evaluated)) {
                return evaluated; // Propagate error
            }
            args.add(evaluated);
        }

        return Builtins.apply(funcName, args);
    }

    private void evaluateFunctionCall(FunctionCallStatement call, Environment env) {
        FunctionDefinitionStatement func = env.getFunction(call.function().value());
        if (func == null) {
            events.add(new ExecutionEvent.SayEvent("Error: function '" + call.function().value() + "' not defined."));
            return;
        }
        List<String> paramNames = func.parameters().stream().map(Identifier::value).toList();
        List<Expression> argExprs = call.arguments();
        if (paramNames.size() != argExprs.size()) {
            events.add(new ExecutionEvent.SayEvent("Error: function '" + call.function().value() + "' expects " + paramNames.size() + " arguments, got " + argExprs.size() + "."));
            return;
        }
        Environment localEnv = new Environment(env);
        for (int i = 0; i < paramNames.size(); i++) {
            Object argVal = evaluateExpression(argExprs.get(i), env);
            if (isError(argVal)) {
                events.add(new ExecutionEvent.SayEvent((String) argVal));
                return;
            }
            localEnv.set(paramNames.get(i), argVal);
        }
        for (Statement bodyStmt : func.body()) {
            evaluateStatement(bodyStmt, localEnv);
        }
    }
}