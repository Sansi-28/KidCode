package com.kidcode.gui;

import com.kidcode.core.KidCodeEngine;
import com.kidcode.core.evaluator.Evaluator;
import com.kidcode.core.event.ExecutionEvent;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import org.fife.ui.rsyntaxtextarea.*;
import org.fife.ui.rtextarea.RTextScrollPane;

import java.io.File;

public class KidCodeVisualInterpreter extends JFrame {
    private final DrawingPanel drawingPanel;
    private final RSyntaxTextArea codeArea;
    private final JTextArea outputArea;
    private final KidCodeEngine engine;

    public KidCodeVisualInterpreter() {
        this.engine = new KidCodeEngine();
        
        setTitle("KidCode Visual Interpreter");
        setSize(1000, 700);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout());

        drawingPanel = new DrawingPanel();
        add(drawingPanel, BorderLayout.CENTER);

        JPanel controlPanel = new JPanel(new BorderLayout());
        codeArea = new RSyntaxTextArea(20, 40);
        codeArea.setSyntaxEditingStyle(SyntaxConstants.SYNTAX_STYLE_PROPERTIES_FILE);
        codeArea.setCodeFoldingEnabled(true);
        codeArea.setFont(new Font("Monospaced", Font.PLAIN, 14));
        // Add some default code for testing
         codeArea.setText("""
                  # Welcome to KidCode!
                  # Run this code to see a rainbow spiral, then try changing it!
                  
                  set colors = ["red", "orange", "yellow", "green", "blue", "purple"]
                  set length = 5
                  set color_index = 0
                  
                  # Repeat many times to make a large spiral
                  repeat 75
                      # Set the color from the list
                      color colors[color_index]
                      
                      move forward length
                      turn right 60
                      
                      # Get ready for the next line
                      set length = length + 2
                      set color_index = color_index + 1
                      
                      # Reset color index to loop through the rainbow
                      if color_index == 6
                          set color_index = 0
                      end if
                  end repeat
                  """);
        RTextScrollPane codeScrollPane = new RTextScrollPane(codeArea);
        controlPanel.add(codeScrollPane, BorderLayout.CENTER);

        JPanel buttonPanel = new JPanel(new FlowLayout());
        JButton runButton = new JButton("Run Code");
        runButton.addActionListener(e -> new Thread(this::runCode).start());
        buttonPanel.add(runButton);
        JButton stopButton = new JButton("Stop");
        stopButton.addActionListener(e -> engine.stopExecution());
        buttonPanel.add(stopButton);
        controlPanel.add(buttonPanel, BorderLayout.SOUTH);

        add(controlPanel, BorderLayout.WEST);

        outputArea = new JTextArea(5, 30);
        outputArea.setEditable(false);
        JScrollPane outputScrollPane = new JScrollPane(outputArea);
        add(outputScrollPane, BorderLayout.SOUTH);

        // Add menu bar
        JMenuBar menuBar = new JMenuBar();
        JMenu fileMenu = new JMenu("File");
        
        JMenuItem openItem = new JMenuItem("Open...");
        openItem.addActionListener(e -> openFile());
        fileMenu.add(openItem);
        
        JMenuItem saveItem = new JMenuItem("Save As...");
        saveItem.addActionListener(e -> saveFile());
        fileMenu.add(saveItem);
        
        menuBar.add(fileMenu);
        setJMenuBar(menuBar);
    }

    /**
     * Converts a KidCode color name to a Java AWT Color object.
     * 
     * @param colorName the color name (case-insensitive)
     * @return the corresponding AWT Color, defaults to BLACK for unknown colors
     */
    private Color parseAwtColor(String colorName) {
        if (colorName == null) {
            return Color.BLACK;
        }
        return switch (colorName.toLowerCase()) {
            case "red" -> Color.RED;
            case "green" -> Color.GREEN;
            case "blue" -> Color.BLUE;
            case "yellow" -> Color.YELLOW;
            case "orange" -> Color.ORANGE;
            case "purple" -> new Color(128, 0, 128); // Purple color
            case "black" -> Color.BLACK;
            case "white" -> Color.WHITE;
            case "cyan" -> Color.CYAN;
            case "magenta" -> Color.MAGENTA;
            case "pink" -> Color.PINK;
            case "brown" -> new Color(139, 69, 19); // Saddle brown color
            default -> Color.BLACK;
        };
    }

    private void runCode() {
        String code = codeArea.getText();
        List<ExecutionEvent> events = engine.execute(code);
        SwingUtilities.invokeLater(() -> {
            for (ExecutionEvent event : events) {
                if (event instanceof ExecutionEvent.ClearEvent) {
            drawingPanel.clear();
            outputArea.setText("");
                } else if (event instanceof ExecutionEvent.MoveEvent e) {
                    if (e.isPenDown() && (e.fromX() != e.toX() || e.fromY() != e.toY())) {
                        drawingPanel.drawLine(e.fromX(), e.fromY(), e.toX(), e.toY(), parseAwtColor(e.color()));
                    }
                    drawingPanel.updateCodyState(e.toX(), e.toY(), e.newDirection());
                } else if (event instanceof ExecutionEvent.SayEvent e) {
                    outputArea.setForeground(Color.BLACK);
                    outputArea.append("Cody says: " + e.message() + "\n");
                } else if (event instanceof ExecutionEvent.ErrorEvent e) {
                    outputArea.setForeground(Color.RED);
                    outputArea.append("ERROR: " + e.errorMessage() + "\n");
                }
            }
        });
    }
    
    private void openFile() {
        JFileChooser chooser = new JFileChooser();
        if (chooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
            try {
                codeArea.setText(new String(java.nio.file.Files.readAllBytes(chooser.getSelectedFile().toPath())));
            } catch (Exception e) {
                JOptionPane.showMessageDialog(this, "Error opening file: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void saveFile() {
        JFileChooser chooser = new JFileChooser();
        if (chooser.showSaveDialog(this) == JFileChooser.APPROVE_OPTION) {
            try {
                java.nio.file.Files.write(chooser.getSelectedFile().toPath(), codeArea.getText().getBytes());
            } catch (Exception e) {
                JOptionPane.showMessageDialog(this, "Error saving file: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }
    
    // --- Inner classes from your original file ---
    static class DrawingPanel extends JPanel {
        private final List<Line> lines = new ArrayList<>();
        // Add state for Cody's visual representation
        private int codyX = 250;
        private int codyY = 250;
        private double codyDirection = 0; // In degrees

        public DrawingPanel() {
            setPreferredSize(new Dimension(500, 500));
            setBackground(Color.WHITE);
        }

        public void drawLine(int x1, int y1, int x2, int y2, Color color) {
            lines.add(new Line(x1, y1, x2, y2, color));
            // We don't need to call repaint() here, updateCodyState will do it.
        }

        public void updateCodyState(int x, int y, double direction) {
            this.codyX = x;
            this.codyY = y;
            this.codyDirection = direction;
            repaint(); // Trigger a repaint to show Cody's new position/rotation
        }

        public void clear() {
            lines.clear();
            // Reset Cody to the initial state when clearing
            this.codyX = 250;
            this.codyY = 250;
            this.codyDirection = 0;
            repaint();
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2d = (Graphics2D) g;

            // 1. Draw all the lines from previous moves
            for (Line line : lines) {
                g2d.setColor(line.color);
                g2d.setStroke(new BasicStroke(2));
                g2d.drawLine(line.x1, line.y1, line.x2, line.y2);
            }

            // 2. Draw Cody
            drawCody(g2d);
        }

        private void drawCody(Graphics2D g2d) {
            Graphics2D g2dCopy = (Graphics2D) g2d.create();

            // Move and rotate the canvas to the pointer's position and direction.
            g2dCopy.translate(codyX, codyY);
            g2dCopy.rotate(Math.toRadians(codyDirection));

            // --- Define the Classic Pointer shape using a Polygon ---
            Polygon pointerShape = new Polygon();
            pointerShape.addPoint(0, -18);   // The very tip (hotspot)
            pointerShape.addPoint(10, 7);    // The bottom-right corner
            pointerShape.addPoint(0, 0);     // The indented base center
            pointerShape.addPoint(-4, 7);    // The bottom-left corner (closer to center for asymmetry)

            // Fill the shape with a dynamic color (for now, keep orange as before)
            g2dCopy.setColor(new Color(255, 100, 0)); // You can replace with this.codyColor if you add color support
            g2dCopy.fill(pointerShape);

            // Draw a crisp black outline
            g2dCopy.setColor(Color.BLACK);
            g2dCopy.setStroke(new BasicStroke(1.5f));
            g2dCopy.draw(pointerShape);

            g2dCopy.dispose();
        }

        static class Line {
            int x1, y1, x2, y2;
            Color color;
            Line(int x1, int y1, int x2, int y2, Color color) {
                this.x1 = x1; this.y1 = y1; this.x2 = x2; this.y2 = y2; this.color = color;
            }
        }
    }
    
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new KidCodeVisualInterpreter().setVisible(true));
    }
} 