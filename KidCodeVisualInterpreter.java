import javax.swing.*;
import java.awt.*;
import java.util.*;
import java.util.List;

public class KidCodeVisualInterpreter extends JFrame {
    private Map<String, Integer> variables = new HashMap<>();
    private Map<String, List<String>> functions = new HashMap<>();
    private int x = 250, y = 250;
    private double direction = 0;
    private DrawingPanel drawingPanel;
    private JTextArea codeArea;
    private JTextArea outputArea;
    private JComboBox<String> commandSelector;
    private JButton addCommandButton;
    private JButton clearCodeButton;
    private JSlider speedSlider;

    public KidCodeVisualInterpreter() {
        setTitle("KidCode Visual Interpreter");
        setSize(1000, 700);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout());

        drawingPanel = new DrawingPanel();
        add(drawingPanel, BorderLayout.CENTER);

        JPanel controlPanel = new JPanel(new BorderLayout());
        codeArea = new JTextArea(20, 30);
        codeArea.setFont(new Font("Monospaced", Font.PLAIN, 14));
        JScrollPane codeScrollPane = new JScrollPane(codeArea);
        controlPanel.add(codeScrollPane, BorderLayout.CENTER);

        JPanel buttonPanel = new JPanel(new FlowLayout());
        JButton runButton = new JButton("Run Code");
        runButton.addActionListener(e -> new Thread(this::runCode).start());
        buttonPanel.add(runButton);

        clearCodeButton = new JButton("Clear Code");
        clearCodeButton.addActionListener(e -> codeArea.setText(""));
        buttonPanel.add(clearCodeButton);

        controlPanel.add(buttonPanel, BorderLayout.SOUTH);

        add(controlPanel, BorderLayout.WEST);

        outputArea = new JTextArea(5, 30);
        outputArea.setEditable(false);
        JScrollPane outputScrollPane = new JScrollPane(outputArea);
        add(outputScrollPane, BorderLayout.SOUTH);

        JPanel interactivePanel = new JPanel(new BorderLayout());
        String[] commands = {"move forward", "turn right", "turn left", "say", "repeat", "if", "set", "define", "add", "subtract", "multiply", "divide"};
        commandSelector = new JComboBox<>(commands);
        interactivePanel.add(commandSelector, BorderLayout.NORTH);

        addCommandButton = new JButton("Add Command");
        addCommandButton.addActionListener(e -> addSelectedCommand());
        interactivePanel.add(addCommandButton, BorderLayout.CENTER);

        speedSlider = new JSlider(JSlider.HORIZONTAL, 0, 1000, 100);
        speedSlider.setMajorTickSpacing(250);
        speedSlider.setPaintTicks(true);
        speedSlider.setPaintLabels(true);
        interactivePanel.add(speedSlider, BorderLayout.SOUTH);

        add(interactivePanel, BorderLayout.EAST);

        addHelpButton();
    }

    private void addSelectedCommand() {
        String selectedCommand = (String) commandSelector.getSelectedItem();
        switch (selectedCommand) {
            case "move forward":
                codeArea.append("move forward 50\n");
                break;
            case "turn right":
                codeArea.append("turn right 90\n");
                break;
            case "turn left":
                codeArea.append("turn left 90\n");
                break;
            case "say":
                codeArea.append("say \"Hello, World!\"\n");
                break;
            case "repeat":
                codeArea.append("repeat 3\n    // Your code here\nend repeat\n");
                break;
            case "if":
                codeArea.append("if true\n    // Your code here\nend if\n");
                break;
            case "set":
                codeArea.append("set variable 0\n");
                break;
            case "define":
                codeArea.append("define functionName\n    // Your code here\nend define\n");
                break;
            case "add":
                codeArea.append("add var1 var2\n");
                break;
            case "subtract":
                codeArea.append("subtract var1 var2\n");
                break;
            case "multiply":
                codeArea.append("multiply var1 var2\n");
                break;
            case "divide":
                codeArea.append("divide var1 var2\n");
                break;
        }
    }

    private void addHelpButton() {
        JButton helpButton = new JButton("Help");
        helpButton.addActionListener(e -> showHelp());
        add(helpButton, BorderLayout.NORTH);
    }

    private void showHelp() {
        String helpMessage = "Welcome to KidCode!\n\n" +
                "Here are some commands you can use:\n" +
                "- move forward [steps]: Move Cody forward\n" +
                "- turn right [degrees]: Turn Cody right\n" +
                "- turn left [degrees]: Turn Cody left\n" +
                "- say [message]: Make Cody say something\n" +
                "- repeat [times]: Repeat commands\n" +
                "- if [condition]: Do something if condition is true\n" +
                "- set [variable] [value]: Set a variable\n" +
                "- define [function]: Define a new function\n" +
                "- add [var1] [var2]: Add two variables\n" +
                "- subtract [var1] [var2]: Subtract two variables\n" +
                "- multiply [var1] [var2]: Multiply two variables\n" +
                "- divide [var1] [var2]: Divide two variables\n\n" +
                "Use the dropdown menu to add commands easily!";
        JOptionPane.showMessageDialog(this, helpMessage, "KidCode Help", JOptionPane.INFORMATION_MESSAGE);
    }

    private void runCode() {
        x = 250;
        y = 250;
        direction = 0;
        drawingPanel.clear();
        outputArea.setText("");
        variables.clear();
        functions.clear();

        String[] lines = codeArea.getText().split("\n");
        interpret(Arrays.asList(lines));
    }

    private void interpret(List<String> code) {
        for (int i = 0; i < code.size(); i++) {
            String line = code.get(i).trim();

            if (line.isEmpty() || line.startsWith("//")) {
                continue;
            }

            if (line.startsWith("move forward")) {
                int steps = Integer.parseInt(line.split(" ")[2]);
                moveForward(steps);
            } else if (line.startsWith("turn")) {
                String[] parts = line.split(" ");
                if (parts.length == 3) {
                    double degrees = Double.parseDouble(parts[2]);
                    if (parts[1].equals("right")) {
                        turn(degrees);
                    } else if (parts[1].equals("left")) {
                        turn(-degrees);
                    }
                }
            } else if (line.startsWith("say")) {
                String message = line.substring(4).trim().replaceAll("^\"|\"$", "");
                say(message);
            } else if (line.startsWith("repeat")) {
                int times = Integer.parseInt(line.split(" ")[1]);
                int endIndex = findEndIndex(code, i + 1, "repeat", "end repeat");
                List<String> loopBody = code.subList(i + 1, endIndex);
                for (int j = 0; j < times; j++) {
                    interpret(loopBody);
                }
                i = endIndex;
            } else if (line.startsWith("if")) {
                boolean condition = evaluateCondition(line.substring(3).trim());
                int endIndex = findEndIndex(code, i + 1, "if", "end if");
                if (condition) {
                    interpret(code.subList(i + 1, endIndex));
                }
                i = endIndex;
            } else if (line.startsWith("set")) {
                String[] parts = line.split(" ");
                String varName = parts[1];
                int value = evaluateExpression(parts[2]);
                variables.put(varName, value);
            } else if (line.startsWith("add")) {
                performArithmetic(line, "+");
            } else if (line.startsWith("subtract")) {
                performArithmetic(line, "-");
            } else if (line.startsWith("multiply")) {
                performArithmetic(line, "*");
            } else if (line.startsWith("divide")) {
                performArithmetic(line, "/");
            } else if (line.startsWith("define")) {
                String funcName = line.split(" ")[1];
                int endIndex = findEndIndex(code, i + 1, "define", "end define");
                functions.put(funcName, code.subList(i + 1, endIndex));
                i = endIndex;
            } else if (functions.containsKey(line)) {
                interpret(functions.get(line));
            }

            try {
                Thread.sleep(speedSlider.getValue());
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    private void moveForward(int steps) {
        int oldX = x, oldY = y;
        x += (int)(steps * Math.sin(Math.toRadians(direction)));
        y -= (int)(steps * Math.cos(Math.toRadians(direction)));
        drawingPanel.drawLine(oldX, oldY, x, y);
        outputArea.append("Moved forward by " + steps + " steps.\n");
    }

    private void turn(double degrees) {
        direction += degrees;
        outputArea.append("Turned " + degrees + " degrees.\n");
    }

    private void say(String message) {
        outputArea.append("Cody says: " + message + "\n");
        JOptionPane.showMessageDialog(this, "Cody says: " + message, "Message", JOptionPane.INFORMATION_MESSAGE);
    }

    private boolean evaluateCondition(String condition) {
        // Basic condition handling (true/false or variable comparison)
        return condition.equals("true");
    }

    private int evaluateExpression(String expression) {
        if (variables.containsKey(expression)) {
            return variables.get(expression);
        } else {
            return Integer.parseInt(expression);
        }
    }

    private void performArithmetic(String line, String operator) {
        String[] parts = line.split(" ");
        String var1 = parts[1];
        String var2 = parts[2];
        int value1 = evaluateExpression(var1);
        int value2 = evaluateExpression(var2);
        int result = 0;
        switch (operator) {
            case "+":
                result = value1 + value2;
                break;
            case "-":
                result = value1 - value2;
                break;
            case "*":
                result = value1 * value2;
                break;
            case "/":
                if (value2 != 0) {
                    result = value1 / value2;
                } else {
                    outputArea.append("Error: Division by zero!\n");
                    return;
                }
                break;
        }
        variables.put(var1, result);
        outputArea.append("Performed arithmetic: " + var1 + " = " + result + "\n");
    }

    private int findEndIndex(List<String> code, int startIndex, String open, String close) {
        int nestedCount = 0;
        for (int i = startIndex; i < code.size(); i++) {
            String line = code.get(i).trim();
            if (line.startsWith(open)) {
                nestedCount++;
            } else if (line.startsWith(close)) {
                if (nestedCount == 0) {
                    return i;
                } else {
                    nestedCount--;
                }
            }
        }
        return code.size();
    }

    class DrawingPanel extends JPanel {
        private List<Line> lines = new ArrayList<>();

        public DrawingPanel() {
            setPreferredSize(new Dimension(500, 500));
            setBackground(Color.WHITE);
        }

        public void drawLine(int x1, int y1, int x2, int y2) {
            lines.add(new Line(x1, y1, x2, y2));
            repaint();
        }

        public void clear() {
            lines.clear();
            repaint();
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            for (Line line : lines) {
                g.drawLine(line.x1, line.y1, line.x2, line.y2);
            }
        }

        class Line {
            int x1, y1, x2, y2;

            Line(int x1, int y1, int x2, int y2) {
                this.x1 = x1;
                this.y1 = y1;
                this.x2 = x2;
                this.y2 = y2;
            }
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new KidCodeVisualInterpreter().setVisible(true));
    }
}
