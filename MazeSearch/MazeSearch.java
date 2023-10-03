// This program is copyright VUW.
// You are granted permission to use it to construct your answer to a COMP103 assignment.
// You may not distribute it in any other way without permission.

/* Code for COMP103 - 2023T2, Assignment 6
 * Name:
 * Username:
 * ID:
 */

import ecs100.UI;

import java.awt.*;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Queue;

/**
 * Search for a path to the goal in a maze.
 * The maze consists of a graph of MazeCells:
 * Each cell has a collection of neighbouring cells.
 * Each cell can be "visited" and it will remember that it has been visited
 * A MazeCell is Iterable, so that you can iterate through its neighbour cells with
 * for(MazeCell neighbour : cell){....
 * <p>
 * The maze has a goal cell (shown in green, two thirds towards the bottom right corner)
 * The maze.getGoal() method returns the goal cell of the maze.
 * The user can click on a cell, and the program will search for a path
 * from that cell to the goal.
 * <p>
 * Every cell that is looked at during the search is coloured  yellow, and then,
 * if the cell turns out to be on a dead end, it is coloured red.
 */

public class MazeSearch {

    private Maze maze;
    private String search = "first";   // "first", "all", or "shortest"
    private int pathCount = 0;
    private boolean stopNow = false;
    // fields for gui.
    private int delay = 20;
    private int size = 10;

    public static void main(String[] args) {
        MazeSearch ms = new MazeSearch();
        ms.setupGui();
        ms.makeMaze();
    }

    //=================================================

    /**
     * CORE
     * Search for a path from a cell to the goal.
     * Return true if we got to the goal via this cell (and don't
     * search for any more paths).
     * Return false if there is not a path via this cell.
     * <p>
     * If the cell is the goal, then we have found a path - return true.
     * If the cell is already visited, then abandon this path - return false.
     * Otherwise,
     * Mark the cell as visited, and colour it yellow [and pause: UI.sleep(delay);]
     * Recursively try exploring from the cell's neighbouring cells, returning true
     * if a neighbour leads to the goal
     * If no neighbour leads to a goal,
     * colour the cell red (to signal failure)
     * abandon the path - return false.
     */
    public boolean exploreFromCell(MazeCell cell) {
        if (cell == maze.getGoal()) { // We've found the path
            cell.draw(Color.blue);   // Indicate finding the goal
            return true; // Terminate method
        } else {
            UI.printMessage("Searching...");
            cell.visit(); // Visit the cell
            cell.draw(Color.yellow); // Color it yellow
            UI.sleep(delay);
            // Look at the cell's neighbors
            for (MazeCell neighbor : cell) {
                // Ensure the cells neighbor has not  been seen before
                if (!neighbor.isVisited()) {
                    if (exploreFromCell(neighbor)) return true;
                }
            }
            cell.draw(Color.red); // Draw incorrect paths red
            return false;
        }
    }

    /**
     * COMPLETION
     * Search for all paths from a cell,
     * If we reach the goal, then we have found a complete path,
     * so pause for 1000 milliseconds
     * Otherwise,
     * visit the cell, and colour it yellow [and pause: UI.sleep(delay);]
     * Recursively explore from the cell's neighbours,
     * un visit the cell and colour it white.
     */
    public void exploreFromCellAll(MazeCell cell) {
        if (stopNow) return; // Exit if user clicked the stop now button
        // If cell is the goal cell
        if (cell == maze.getGoal()) {
            pathCount++; // We've found a new path, so increment path count
            cell.draw(Color.blue); // Color blue before pausing
            UI.printMessage("Found " + pathCount + " paths!");
            UI.sleep(1000); // Pause for 1 second
            cell.draw(Color.green); // Colour green after pause
        } else {
            UI.printMessage("Searching...");
            cell.visit();
            cell.draw(Color.yellow); // Color visited cell yellow
            UI.sleep(delay); // Pause briefly
            for (MazeCell neighbors : cell) { // Traverse cells neighbors
                if (!neighbors.isVisited()) {
                    exploreFromCellAll(neighbors);
                }
            }
            cell.draw(Color.white); // Once cell is visited, colour cell white
            cell.unvisit(); // Un visit so we can visit again
        }
    }

    /**
     * CHALLENGE
     * Search for shortest path from a cell,
     * Use Breadth first search.
     */
    public void exploreFromCellShortest(MazeCell start) {
        boolean found = false;
        Queue<ArrayList<MazeCell>> paths = new ArrayDeque<>();
        ArrayList<MazeCell> routes = new ArrayList<>();
        routes.add(start);
        paths.offer(routes);
        while (!found || paths.isEmpty()) {
            ArrayList<MazeCell> route = paths.poll();
            if (route == null) return;
            MazeCell cell = route.get(route.size() - 1);
            if (cell.equals(maze.getGoal())) {
                found = true;
                for (MazeCell step : route) {
                    step.draw(Color.yellow);
                }
                cell.draw(Color.red);
                UI.sleep(delay);
                cell.draw(Color.green);
            } else {
                cell.visit();
                for (MazeCell neighbors : cell) {
                    if (!neighbors.isVisited()) {
                        ArrayList<MazeCell> temp = new ArrayList<>(route);
                        temp.add(neighbors);
                        paths.offer(temp);
                    }
                }
            }
        }
    }

    /**
     * Set up the interface
     */
    public void setupGui() {
        UI.addButton("New Maze", this::makeMaze);
        UI.addSlider("Maze Size", 4, 40, 10, (double v) -> size = (int) v);
        UI.setMouseListener(this::doMouse);
        UI.addButton("First path", () -> search = "first");
        UI.addButton("All paths", () -> search = "all");
        UI.addButton("Shortest path", () -> search = "shortest");
        UI.addButton("Stop", () -> stopNow = true);
        UI.addSlider("Speed", 1, 101, 80, (double v) -> delay = (int) (100 - v));
        UI.addButton("Quit", UI::quit);
        UI.setDivider(0.);
    }

    /**
     * Creates a new maze and draws it .
     */
    public void makeMaze() {
        maze = new Maze(size);
        maze.draw();
    }

    /**
     * Clicking the mouse on a cell should make the program
     * search for a path from the clicked cell to the goal.
     */
    public void doMouse(String action, double x, double y) {
        if (action.equals("released")) {
            maze.reset();
            maze.draw();
            pathCount = 0;
            MazeCell start = maze.getCellAt(x, y);
            switch (search) {
                case "first" -> exploreFromCell(start);
                case "all" -> {
                    stopNow = false;
                    exploreFromCellAll(start);
                }
                case "shortest" -> exploreFromCellShortest(start);
            }
        }
    }
}
