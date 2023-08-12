// This program is copyright VUW.
// You are granted permission to use it to construct your answer to a COMP103 assignment.
// You may not distribute it in any other way without permission.

/* Code for COMP103 - 2023T2, Assignment 2
 * Name: Shemaiah Rangitaawa
 * Username:rangitshem
 * ID: 300601645
 */

import ecs100.*;
import java.awt.*;
import java.util.*;

/**
 * Pencil
 */
public class Pencil {
    private Stroke currentStroke;
    private final Stack<Stroke> strokes = new Stack<>();
    private final Stack<Stroke> undoStack = new Stack<>();

    /**
     * Set up the GUI
     */
    public void setupGUI() {
        UI.setMouseMotionListener(this::doMouse);
        UI.addButton("Quit", UI::quit);
        UI.addButton("Undo", this::undo);
        UI.addButton("Redo", this::redo);
        UI.addButton("Clear", this::clearCanvas);
        UI.setLineWidth(3);
        UI.setDivider(0.0);
    }

    /**
     * Respond to mouse events
     */
    public void doMouse(String action, double x, double y) {
        switch (action) {
            case "pressed" -> {
                // Create a new stroke and add the current points
                currentStroke = new Stroke();
                currentStroke.addPoint(x, y);
            }
            case "dragged" -> {
                currentStroke.addPoint(x, y);

                // Clear and redraw last stroke
                UI.clearGraphics();
                for (Stroke stroke : strokes) {
                    stroke.draw();
                }

                // Redraw the current stroke
                currentStroke.draw();
            }
            case "released" -> {
                strokes.push(currentStroke);
                currentStroke = null;
                undoStack.clear();
            }
        }
    }

    /**
     * undo method
     */
    public void undo() {
        // Ensure stack is occupied
        if (!strokes.isEmpty()) {
            // Find the last drawn stroke
            Stroke strokeToUndo = strokes.pop();

            // Push the last drawn stroke into the undo stack
            undoStack.push(strokeToUndo);

            // Clear and redraw any previous strokes
            redrawStroke();
        }
    }

    /**
     * redo method
     */
    public void redo() {
        if (!undoStack.isEmpty()) {
            Stroke strokeToRedo = undoStack.pop();
            strokes.push(strokeToRedo);
            redrawStroke();
        }
    }

    /**
     * redrawStroke(), clears the graphics pane
     * and redraws each stroke in the stroke stack
     */
    private void redrawStroke() {
        UI.clearGraphics();
        for (Stroke stroke : strokes) {
            stroke.draw();
        }
    }

    /**
     * clearCanvas(), empties the undo and stroke
     * stacks and clears the graphics pane
     */
    public void clearCanvas() {
        strokes.clear();
        undoStack.clear();
        UI.clearGraphics();
    }

    public static void main(String[] arguments) {
        new Pencil().setupGUI();
    }
}

