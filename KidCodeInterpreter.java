import java.util.*;

public class KidCodeInterpreter {
    private Map<String, Integer> variables = new HashMap<>(); // Stores variables
    private Map<String, List<String>> functions = new HashMap<>(); // Stores functions
    private int x = 0, y = 0, direction = 0; // Cody's position and direction: 0: North, 1: East, 2: South, 3: West

    // Method to interpret the code
    public void interpret(List<String> code) {
        for (int i = 0; i < code.size(); i++) {
            String line = code.get(i).trim();

            if (line.startsWith("move forward")) {
                String[] parts = line.split(" ");
                if (parts.length == 3) {
                    int steps = Integer.parseInt(parts[2]);
                    moveForward(steps);
                } else {
                    System.out.println("Invalid syntax for 'move forward'");
                }
            }
            else if (line.equals("turn right")) {
                turnRight();
            }
            else if (line.equals("turn left")) {
                turnLeft();
            }
            else if (line.startsWith("say")) {
                String message = line.substring(4).trim().replaceAll("^\"|\"$", "");
                say(message);
            }
            else if (line.startsWith("repeat")) {
                String[] parts = line.split(" ");
                if (parts.length == 2) {
                    int times = Integer.parseInt(parts[1]);
                    int endIndex = findEndIndex(code, i + 1, "repeat", "end repeat");
                    List<String> loopBody = code.subList(i + 1, endIndex);
                    for (int j = 0; j < times; j++) {
                        interpret(loopBody);
                    }
                    i = endIndex;
                } else {
                    System.out.println("Invalid syntax for 'repeat'");
                }
            }
            else if (line.startsWith("if")) {
                String[] parts = line.split(" ");
                if (parts.length >= 2) {
                    boolean condition = evaluateCondition(line.substring(3).trim());
                    int endIndex = findEndIndex(code, i + 1, "if", "end if");
                    if (condition) {
                        interpret(code.subList(i + 1, endIndex));
                    }
                    i = endIndex;
                } else {
                    System.out.println("Invalid syntax for 'if'");
                }
            }
            else if (line.startsWith("set")) {
                String[] parts = line.split(" ");
                if (parts.length == 4 && parts[2].equals("=")) {
                    String varName = parts[1];
                    int value = Integer.parseInt(parts[3]);
                    variables.put(varName, value);
                } else {
                    System.out.println("Invalid syntax for 'set'");
                }
            }
            else if (line.startsWith("add") || line.startsWith("subtract") || line.startsWith("multiply") || line.startsWith("divide")) {
                performArithmetic(line);
            }
            else if (line.startsWith("define")) {
                String[] parts = line.split(" ");
                if (parts.length == 2) {
                    String funcName = parts[1];
                    int endIndex = findEndIndex(code, i + 1, "define", "end define");
                    functions.put(funcName, code.subList(i + 1, endIndex));
                    i = endIndex;
                } else {
                    System.out.println("Invalid syntax for 'define'");
                }
            }
            else if (functions.containsKey(line)) {
                interpret(functions.get(line));
            } else {
                System.out.println("Unknown command: " + line);
            }
        }
    }

    // Method to move forward
    private void moveForward(int steps) {
        switch (direction) {
            case 0: y += steps; break; // North
            case 1: x += steps; break; // East
            case 2: y -= steps; break; // South
            case 3: x -= steps; break; // West
        }
        System.out.println("Moved to (" + x + ", " + y + ")");
    }

    // Method to turn right
    private void turnRight() {
        direction = (direction + 1) % 4;
        System.out.println("Turned right, now facing " + getDirectionName());
    }

    // Method to turn left
    private void turnLeft() {
        direction = (direction + 3) % 4;
        System.out.println("Turned left, now facing " + getDirectionName());
    }

    // Method to say (print) messages
    private void say(String message) {
        System.out.println("Cody says: " + message);
    }

    // Helper method to get the direction's name
    private String getDirectionName() {
        switch (direction) {
            case 0: return "North";
            case 1: return "East";
            case 2: return "South";
            case 3: return "West";
            default: return "Unknown";
        }
    }

    // Method to find the index where a block ends
    private int findEndIndex(List<String> code, int startIndex, String startKeyword, String endKeyword) {
        int nestingLevel = 1;
        for (int i = startIndex; i < code.size(); i++) {
            if (code.get(i).trim().startsWith(startKeyword)) nestingLevel++;
            if (code.get(i).trim().equals(endKeyword)) nestingLevel--;
            if (nestingLevel == 0) return i;
        }
        throw new RuntimeException("Matching " + endKeyword + " not found");
    }

    // Method to evaluate conditions (currently simplified to check for "true")
    private boolean evaluateCondition(String condition) {
        return condition.equals("true");
    }

    // Helper method to handle arithmetic operations
    private void performArithmetic(String line) {
        String[] parts = line.split(" ");
        if (parts.length == 3) {
            String operation = parts[0];
            String var1 = parts[1];
            String var2 = parts[2];

            if (!variables.containsKey(var1) || !variables.containsKey(var2)) {
                System.out.println("Error: Undefined variable(s).");
                return;
            }

            int result;
            switch (operation) {
                case "add":
                    result = variables.get(var1) + variables.get(var2);
                    break;
                case "subtract":
                    result = variables.get(var1) - variables.get(var2);
                    break;
                case "multiply":
                    result = variables.get(var1) * variables.get(var2);
                    break;
                case "divide":
                    if (variables.get(var2) == 0) {
                        System.out.println("Error: Division by zero.");
                        return;
                    }
                    result = variables.get(var1) / variables.get(var2);
                    break;
                default:
                    throw new IllegalArgumentException("Invalid operation: " + operation);
            }
            variables.put(var1, result);
            System.out.println("Performed " + operation + ": " + var1 + " = " + result);
        } else {
            System.out.println("Invalid syntax for arithmetic operation");
        }
    }

    public static void main(String[] args) {
        KidCodeInterpreter interpreter = new KidCodeInterpreter();

        // Example program
        List<String> sampleProgram = Arrays.asList(
                "set score = 10",
                "set bonus = 5",
                "add score bonus",
                "say \"Score after bonus: \" + score",
                "move forward 10",
                "turn right",
                "repeat 2",
                "    move forward 5",
                "    turn left",
                "end repeat",
                "if true",
                "    say \"Condition is true!\"",
                "end if",
                "define draw_triangle",
                "    repeat 3",
                "        move forward 10",
                "        turn right",
                "    end repeat",
                "end define",
                "draw_triangle"
        );

        interpreter.interpret(sampleProgram);
    }
}
