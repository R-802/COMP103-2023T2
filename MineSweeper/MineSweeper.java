// This program is copyright VUW.
// You are granted permission to use it to construct your answer to a COMP103 assignment.
// You may not distribute it in any other way without permission.

/* Code for COMP103 - 2023T2, Assignment 3
 * Name: Shemaiah Rangitaawa
 * Username: rangitshem
 * ID: 300601546
 */

import ecs100.UI;

import javax.swing.*;
import java.awt.*;

/**
 * Simple 'Minesweeper' program.
 * There is a grid of squares, some of which contain a mine.
 * <p>
 * The user can click on a square to either expose it or to
 * mark/unmark it.
 * <p>
 * If the user exposes a square with a mine, they lose.
 * Otherwise, it is uncovered, and shows a number which represents the
 * number of mines in the eight squares surrounding that one.
 * If there are no mines adjacent to it, then all the unexposed squares
 * immediately adjacent to it are exposed (and so on)
 * <p>
 * If the user marks a square, then they cannot expose the square,
 * (unless they unmark it first)
 * When all the squares without mines are exposed, the user has won.
 */
public class MineSweeper {

    public static final int ROWS = 15;
    public static final int COLS = 15;
    public static final double LEFT = 10;
    public static final double TOP = 10;
    public static final double SQUARE_SIZE = 20;
    Color defaultColor;

    // Fields
    private boolean marking;
    private Square[][] squares;
    private int[][] aiDecisions;
    private JButton mrkButton;
    private JButton expButton;
    private Square square;

    /**
     * Construct a new MineSweeper object
     * and set up the GUI
     */
    public static void main(String[] arguments) {
        MineSweeper ms = new MineSweeper();
        ms.setupGUI();
        ms.setMarking(false);
        ms.makeGrid();
    }

    /**
     * Set up the GUI: buttons and mouse to play the game
     */
    public void setupGUI() {
        UI.setMouseListener(this::doMouse);
        UI.addButton("New Game", this::makeGrid);
        this.expButton = UI.addButton("Expose", () -> setMarking(false));
        this.mrkButton = UI.addButton("Mark", () -> setMarking(true));
        UI.addButton("Mark Potential Bombs", this::performAIAction);
        UI.addButton("Quit", UI::quit);
        UI.setDivider(0.0);
    }

    /**
     * Respond to mouse events
     */
    public void doMouse(String action, double x, double y) {
        if (action.equals("released")) {
            int row = (int) ((y - TOP) / SQUARE_SIZE);
            int col = (int) ((x - LEFT) / SQUARE_SIZE);
            if (row >= 0 && row < ROWS && col >= 0 && col < COLS) {
                if (marking) {
                    mark(row, col);
                } else {
                    tryExpose(row, col);
                }
            }
        }
    }

    //-----------------------------------------//
    //           CORE AND COMPLETION           //
    //-----------------------------------------//

    /**
     * Mark (or unmark) the square.
     * If the square is exposed, don't do anything,
     * If it is marked, unmark it and redraw,
     * otherwise mark it and redraw.
     */
    public void mark(int row, int col) {
        square = squares[row][col];

        // If square is exposed, do nothing
        if (square.isExposed()) {
            return;
        }

        // Toggle the marking state of the square and redraw
        if (!square.isMarked()) {
            square.mark();
        } else if (square.isMarked()) {
            square.unMark();
        }

        square.draw(row, col);
    }

    /**
     * Respond to the player clicking on a square to expose it
     * - if it is already exposed or marked, do nothing.
     * - if it's a mine: lose (call drawLose())
     * - otherwise expose it (call exposeSquareAt)
     * then check to see if the player has won and call drawWon() if they have.
     * (This method is not recursive)
     */
    public void tryExpose(int row, int col) {
        square = squares[row][col];

        // If square is exposed or marked do nothing
        if (square.isExposed() || square.isMarked()) {
            return;

            // If square has a mine
        } else if (square.hasMine()) {
            drawLose();
            return; // Stop the game

            // Otherwise expose the square
        } else if (!square.hasMine()) {
            exposeSquareAt(row, col);
        }

        if (hasWon()) {
            drawWin();
        }
    }

    /**
     * Ensures that the square at row and col is exposed.
     * If it is already exposed, do nothing.
     * Otherwise, expose it and redraw it.
     * If the number of adjacent mines of this square is 0, then none of
     * its neighbours have mines, so expose all its eight neighbours
     * (and if they have no adjacent mines, expose their neighbours, and ....)
     * (be careful not to go over the edges of the map)
     */
    public void exposeSquareAt(int row, int col) {
        // Error catching
        if (row < 0 || row >= ROWS || col < 0 || col >= COLS) {
            return;
        }

        square = squares[row][col];

        // If square is exposed do nothing
        if (square.isExposed()) {
            return;
        } else {

            // Expose the square and redraw
            square.setExposed();
            square.draw(row, col);
        }

        // If the square has no adjacent mines
        if (square.getAdjacentMines() == 0) {
            // Expose its eight neighbors
            UI.sleep(2);
            exposeSquareAt(row - 1, col - 1);
            exposeSquareAt(row - 1, col);
            exposeSquareAt(row - 1, col + 1);
            exposeSquareAt(row, col - 1);
            exposeSquareAt(row, col + 1);
            exposeSquareAt(row + 1, col - 1);
            exposeSquareAt(row + 1, col);
            exposeSquareAt(row + 1, col + 1);
        }
    }

    /**
     * Returns true if and only if the player has won:
     * If any square without a mine is not exposed, then the player has not won yet.
     * If all the squares without a mine have been exposed, then the player has won.
     * (It doesn't matter if the squares with a mine have been marked or not).
     *
     * @return true if the player has won
     */
    public boolean hasWon() {
        // Iterate through rows
        for (int row = 0; row < ROWS; row++) {
            // Iterate through columns
            for (int col = 0; col < COLS; col++) {
                // If square does not have mine and is not exposed
                if (!squares[row][col].hasMine() && !squares[row][col].isExposed()) {
                    return false;
                }
            }
        }
        return true;
    }

    //-----------------------------------------//
    //               CHALLANGE                 //
    //-----------------------------------------//

    /**
     * Perform the AIs action, called by the "Mark Potential Bombs" button
     */
    public void performAIAction() {
        int[][] previousAIDecisions = aiDecisions; // Store current AI decisions
        int[][] visibleState = getVisibleState(); // Get the visible state of the game board
        aiDecisions = new int[ROWS][COLS]; // Initialize aiDecisions array
        findPotentialBomb(visibleState); // Call AI logic to make recommendations
        updateUIAfterAIMove(); // Update UI to reflect AI's choices
        aiDecisions = previousAIDecisions; // Restore AI decisions
    }

    /**
     * Logic to make potential bomb recommendations
     *
     * @param visibleState The visible state of the game board.
     */
    public void findPotentialBomb(int[][] visibleState) {
        // Iterate through the visible state of the game
        for (int row = 0; row < ROWS; row++) {
            for (int col = 0; col < COLS; col++) {
                int squareValue = visibleState[row][col];

                // Skip already exposed or marked squares
                if (squareValue >= 0) {
                    continue;
                }

                // Calculate the number of hidden squares adjacent to a given square.
                int adjacentHiddenSquares = countAdjacentHiddenSquares(row, col, visibleState);

                // Check if the number of hidden squares matches the number on the square
                if (adjacentHiddenSquares > 0 && adjacentHiddenSquares == -squareValue) {
                    aiDecisions[row][col] = 1; // Mark this square in AI decisions
                }
            }
        }
    }

    /**
     * This method counts the number of hidden squares (-1) adjacent to
     * a given square in the visible state of the Minesweeper board.
     *
     * @param row          Row index of the square.
     * @param col          Column index of the square.
     * @param visibleState The visible state of the game board.
     * @return The count of hidden squares adjacent to the given square.
     */
    private int countAdjacentHiddenSquares(int row, int col, int[][] visibleState) {
        int count = 0;

        for (int rOffset = -1; rOffset <= 1; rOffset++) {
            for (int cOffset = -1; cOffset <= 1; cOffset++) {
                int r = row + rOffset;
                int c = col + cOffset;

                if (isValidCoordinate(r, c) && visibleState[r][c] == -1) {
                    count++;
                }
            }
        }
        return count;
    }

    /**
     * This method checks if a given row and column are valid coordinates
     * on the Minesweeper board.
     *
     * @param row Row index of the square.
     * @param col Column index of the square.
     * @return True if the row and column are valid coordinates, false otherwise.
     */
    private boolean isValidCoordinate(int row, int col) {
        return row >= 0 && row < ROWS && col >= 0 && col < COLS;
    }

    /**
     * Update the UI based on the AI's decisions. The method
     * iterates through each square in the grid and checks
     * the AI's decision for that square.
     */
    public void updateUIAfterAIMove() {
        for (int row = 0; row < MineSweeper.ROWS; row++) {
            for (int col = 0; col < MineSweeper.COLS; col++) {
                int aiDecision = aiDecisions[row][col]; // Get AI's decision for this square

                // If AI has decided to mark this square
                if (aiDecision == 1) {
                    // Mark the square on the UI
                    mark(row, col);
                } else {
                    // Redraw the square based on its actual state
                    square = squares[row][col];
                    square.draw(row, col);
                }
            }
        }
    }


    // completed methods

    /**
     * Respond to the Mark and Expose buttons:
     * Remember whether the user is currently "Marking" or "Exposing"
     * Change the colour of the "Mark", "Expose" buttons
     */
    public void setMarking(boolean v) {
        marking = v;
        if (marking) {
            mrkButton.setBackground(Color.red);
            expButton.setBackground(null);
        } else {
            expButton.setBackground(Color.red);
            mrkButton.setBackground(null);
        }
    }

    /**
     * Construct and draw a grid with random mines.
     * Compute the number of adjacent mines in each Square
     */
    public void makeGrid() {
        UI.clearGraphics();
        this.squares = new Square[ROWS][COLS];
        for (int row = 0; row < ROWS; row++) {
            for (int col = 0; col < COLS; col++) {
                boolean isMine = Math.random() < 0.1;     // approx 1 in 10 squares is a mine
                this.squares[row][col] = new Square(isMine);
                this.squares[row][col].draw(row, col);
                UI.sleep(2);
            }
        }

        // now compute the number of adjacent mines for each square
        for (int row = 0; row < ROWS; row++) {
            for (int col = 0; col < COLS; col++) {
                int count = 0;
                //look at each square in the neighbourhood.
                for (int r = Math.max(row - 1, 0); r < Math.min(row + 2, ROWS); r++) {
                    for (int c = Math.max(col - 1, 0); c < Math.min(col + 2, COLS); c++) {
                        if (squares[r][c].hasMine()) count++;
                    }
                }
                if (this.squares[row][col].hasMine())
                    count--;  // we weren't suppose to count this square, just the adjacent ones.

                this.squares[row][col].setAdjacentMines(count);
            }
        }
    }

    /**
     * Draw a message telling the player they have won
     */
    public void drawWin() {
        UI.setFontSize(28);
        UI.drawString("You Win!", LEFT + COLS * SQUARE_SIZE + 20, TOP + ROWS * SQUARE_SIZE / 2);
        UI.setFontSize(12);
    }

    /**
     * Draw a message telling the player they have lost
     * and expose all the squares and redraw them
     */
    public void drawLose() {
        for (int row = 0; row < ROWS; row++) {
            for (int col = 0; col < COLS; col++) {
                squares[row][col].setExposed();
                squares[row][col].draw(row, col);
                UI.sleep(2);
            }
        }
        UI.setFontSize(28);
        UI.drawString("You Lose!", LEFT + COLS * SQUARE_SIZE + 20, TOP + ROWS * SQUARE_SIZE / 2);
        UI.setFontSize(12);
    }

    /**
     * Return a grid of integers, showing the visible state of the board:
     * -1 for any square that is not exposed
     * 0 - 8 for any exposed square, saying how many mines are adjacent to it.
     */
    public int[][] getVisibleState() {
        int[][] ans = new int[ROWS][COLS];
        for (int r = 0; r < ROWS; r++) {
            for (int c = 0; c < COLS; c++) {
                ans[r][c] = squares[r][c].isExposed() ? (squares[r][c].getAdjacentMines()) : -1;
            }
        }
        return ans;
    }

}