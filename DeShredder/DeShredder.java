// This program is copyright VUW.
// You are granted permission to use it to construct your answer to a COMP103 assignment.
// You may not distribute it in any other way without permission.

/* Code for COMP103 - 2023T2, Assignment 1
 * Name: Shemaiah Rangitaawa
 * Username: Rangitshem
 * ID: 300601546
 */

import ecs100.*;

import java.awt.Color;
import java.util.*;
import java.io.*;
import java.nio.file.*;
import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;

/**
 * DeShredder allows a user to sort fragments of a shredded document ("shreds") into strips, and
 * then sort the strips into the original document.
 * The program shows
 * - a list of all the shreds along the top of the window,
 * - the working strip (which the user is constructing) just below it.
 * - the list of completed strips below the working strip.
 * The "rotate" button moves the first shred on the list to the end of the list to let the
 * user see the shreds that have disappeared over the edge of the window.
 * The "shuffle" button reorders the shreds in the list randomly.
 * The user can use the mouse to drag shreds between the list at the top and the working strip,
 * and move shreds around in the working strip to get them in order.
 * When the user has the working strip complete, they can move
 * the working strip down into the list of completed strips, and reorder the completed strips.
 */
public class DeShredder {

    // Fields to store the lists of Shreds and strips.  These should never be null.
    private final List<Shred> allShreds = new ArrayList<Shred>();    //  List of all shreds
    private final List<Shred> workingStrip = new ArrayList<Shred>(); // Current strip of shreds
    private final List<List<Shred>> completedStrips = new ArrayList<List<Shred>>();

    // Constants for the display and the mouse
    public static final double LEFT = 20;       // left side of the display
    public static final double TOP_ALL = 20;    // top of list of all shreds 
    public static final double GAP = 5;         // gap between strips
    public static final double SIZE = Shred.SIZE; // size of the shreds

    public static final double TOP_WORKING = TOP_ALL + SIZE + GAP;
    public static final double TOP_STRIPS = TOP_WORKING + (SIZE + GAP);

    //Fields for recording where the mouse was pressed  (which list/strip and position in list)
    // note, the position may be past the end of the list!
    private List<Shred> fromStrip;   // The strip (List of Shreds) that the user pressed on
    private int fromPosition = -1;   // index of shred in the strip
    public boolean neighborState = false;

    /**
     * Initialises the UI window, and sets up the buttons.
     */
    public void setupGUI() {
        UI.addButton("Load library", this::loadLibrary);
        UI.addButton("Rotate", this::rotateList);
        UI.addButton("Shuffle", this::shuffleList);
        UI.addButton("Complete Strip", this::completeStrip);
        UI.addButton("Show Neighbors", this::showNeighbors);
        UI.addButton("Quit", UI::quit);

        UI.setMouseListener(this::doMouse);
        UI.setWindowSize(1000, 800);
        UI.setDivider(0);

    }

    /**
     * Asks user for a library of shreds, loads it, and redisplays.
     * Uses UIFileChooser to let user select library
     * and finds out how many images are in the library
     * Calls load(...) to construct the List of all the Shreds
     */
    public void loadLibrary() {
        Path filePath = Path.of(UIFileChooser.open("Choose first shred in directory"));
        Path directory = filePath.getParent(); //subPath(0, filePath.getNameCount()-1);
        int count = 1;
        while (Files.exists(directory.resolve(count + ".png"))) {
            count++;
        }
        //loop stops when count.png doesn't exist
        count = count - 1;
        load(directory, count);
        display();
    }

    /**
     * Empties out all the current lists (the list of all shreds,
     * the working strip, and the completed strips).
     * Loads the library of shreds into the allShreds list.
     * Parameters are the directory containing the shred images and the number of shreds.
     * Each new Shred needs the directory and the number/id of the shred.
     */
    public void load(Path dir, int count) {
        // Clear lists
        allShreds.clear();
        workingStrip.clear();
        completedStrips.clear();

        // Iterate through each image id and directory
        for (int i = 1; i <= count; i++) {
            // Add new shred to allShreds list
            allShreds.add(new Shred(dir, i));
        }
    }

    /**
     * Rotate the list of all shreds by one step to the left
     * and redisplay;
     * Should not have an error if the list is empty
     * (Called by the "Rotate" button)
     */
    public void rotateList() {
        if (allShreds.isEmpty()) {
            return;
        }
        Shred firstShred = allShreds.remove(0);
        allShreds.add(firstShred);
        display();
    }

    /**
     * Implementation of the Fisher-Yates shuffle algorithm.
     * In each iteration, the algorithm selects a random index from the
     * remaining unshuffled portion of the sequence and swaps
     * the current element with the element at that random index.
     * This process is repeated until all elements have been shuffled.
     *
     * @param list
     */
    public static void shuffle(List<Shred> list) {
        Random random = new Random();

        for (int i = list.size() - 1; i > 0; i--) {
            int j = random.nextInt(i + 1);
            Shred temp = list.get(i);
            list.set(i, list.get(j));
            list.set(j, temp);
        }
    }

    /**
     * Shuffle the list of all shreds into a random order
     * and redisplay;
     */
    public void shuffleList() {
        shuffle(allShreds);
        display();
    }

    /**
     * Move the current working strip to the end of the list of completed strips.
     * (Called by the "Complete Strip" button)
     */
    public void completeStrip() {
        if (workingStrip.isEmpty()) {
            return;
        }
        completedStrips.add(new ArrayList<>(workingStrip));
        workingStrip.clear();
        display();
    }

    /**
     * Simple Mouse actions to move shreds and strips
     * User can
     * - move a Shred from allShreds to a position in the working strip
     * - move a Shred from the working strip back into allShreds
     * - move a Shred around within the working strip.
     * - move a completed Strip around within the list of completed strips
     * - move a completed Strip back to become the working strip
     * (but only if the working strip is currently empty)
     * Moving a shred to a position past the end of a List should put it at the end.
     * You should create additional methods to do the different actions - do not attempt
     * to put all the code inside the doMouse method - you will lose style points for this.
     * Attempting an invalid action should have no effect.
     * Note: doMouse uses getStrip and getColumn, which are written for you (at the end).
     * You should not change them.
     */
    public void doMouse(String action, double x, double y) {
        if (action.equals("pressed")) {
            fromStrip = getStrip(y);      // the List of shreds to move from (possibly null)
            fromPosition = getColumn(x);  // the index of the shred to move (may be off the end)
        }

        if (action.equals("released")) {
            List<Shred> toStrip = getStrip(y); // the List of shreds to move to (possibly null)
            int toPosition = getColumn(x);     // the index to move the shred to (may be off the end)

            // try catch block to avoid any errors due to invalid movement
            try {
                if (neighborState == true) {
                    showNeighbors();
                } else if (neighborState == false) {
                    hideNeighbors();
                }
                // perform the correct action, depending on the from/to strips/positions
                performAction(fromStrip, fromPosition, toStrip, toPosition);
                display();
            } catch (Exception e) {
                return;
            }
        }
    }

    // Additional methods to perform the different actions, called by doMouse

    /**
     * performAction() contains the logic and method calls required for moving:
     * - a Shred around within the working and allShreds strips
     * - a Shred between allShreds and the working strips
     * - a completed Strip around within the list of completed strips
     * - a completed Strip back to become the working strip (only if working strip is empty)
     *
     * @param fromStrip
     * @param fromPosition
     * @param toStrip
     * @param toPosition
     */
    private void performAction(List<Shred> fromStrip, int fromPosition, List<Shred> toStrip, int toPosition) {
        if (fromStrip == toStrip) {
            moveShredWithinStrip(fromStrip, fromPosition, toPosition);
        } else if (toStrip == workingStrip && fromStrip == allShreds) {
            moveShredBetweenStrips(fromStrip, fromPosition, toStrip, toPosition);
        } else if (fromStrip == workingStrip && toStrip == allShreds) {
            moveShredBetweenStrips(fromStrip, fromPosition, toStrip, toPosition);
        } else if (toStrip == workingStrip && workingStrip.isEmpty()) {
            moveStripToWorkingStrip(fromStrip);
        } else if (completedStrips.contains(toStrip)) {
            int fromIndex = completedStrips.indexOf(fromStrip);
            int toIndex = completedStrips.indexOf(toStrip);
            moveStripBetweenCompletedStrips(fromIndex, toIndex);
        }
    }

    /**
     * Method for moving shreds within a strip
     *
     * @param strip
     * @param fromPosition
     * @param toPosition
     */
    private void moveShredWithinStrip(List<Shred> strip, int fromPosition, int toPosition) {
        Shred shred = strip.remove(fromPosition);
        // If moving shred outside the boundary of the strip
        if (toPosition >= strip.size()) {
            strip.add(shred);
        } else {
            strip.add(toPosition, shred); // Add the shred the to relevant index
        }
    }

    /**
     * Method for moving shreds between strips.
     *
     * @param fromStrip
     * @param fromPosition
     * @param toStrip
     * @param toPosition
     */
    private void moveShredBetweenStrips(List<Shred> fromStrip, int fromPosition, List<Shred> toStrip, int toPosition) {
        Shred shred = fromStrip.remove(fromPosition);
        if (toPosition > 0 && toPosition >= toStrip.size() && !fromStrip.equals(completedStrips)) {
            toStrip.add(shred);
        } else {
            toStrip.add(toPosition, shred);
        }
    }

    /**
     * Method for moving strips from completeStrip back to workingStrip.
     *
     * @param fromStrip
     */
    private void moveStripToWorkingStrip(List<Shred> fromStrip) {
        workingStrip.addAll(fromStrip);
        completedStrips.remove(fromStrip);
    }

    /**
     * Method for moving completed strips between completed strips.
     *
     * @param fromIndex
     * @param toIndex
     */
    private void moveStripBetweenCompletedStrips(int fromIndex, int toIndex) {
        List<Shred> stripToMove = completedStrips.remove(fromIndex);
        completedStrips.add(toIndex, stripToMove);
    }

    //=============================================================================
    //  Challange part 2, did not attempt part 1.
    //=============================================================================

    /**
     * Check if two colors are a close match based on a threshold value. Works decently for
     * shreds0, not so much for shreds1 and shreds2.
     *
     * @param color1    The first color.
     * @param color2    The second color.
     * @param threshold The threshold value to determine a close match (smaller values indicate a stricter match).
     * @return True if the colors are a close match, false otherwise.
     */
    private boolean isColorCloseMatch(Color color1, Color color2, int threshold) {
        int redDiff = Math.abs(color1.getRed() - color2.getRed());
        int greenDiff = Math.abs(color1.getGreen() - color2.getGreen());
        int blueDiff = Math.abs(color1.getBlue() - color2.getBlue());

        // Calculate the total difference as the sum of individual color differences
        int totalDiff = redDiff + greenDiff + blueDiff;

        // Check if the total difference is within the specified threshold
        return totalDiff <= threshold;
    }

    /**
     * Find potential neighbors for the last shred in the working strip.
     * Compares the colors of pixels on one side of each shred with the complementary side
     * of the last shred in the working strip. If the colors are a close match,
     * add the shred to a list of potential neighbors.
     * The method assumes that all shreds have the same dimensions.
     */
    public List<Integer> findPotentialNeighbors() {
        if (workingStrip.isEmpty() || allShreds.isEmpty()) {
            return new ArrayList<>();
        }

        List<Integer> potentialNeighbors = new ArrayList<>();

        Shred lastShred = workingStrip.get(workingStrip.size() - 1);

        // Get the complementary side of the last shred
        Color[] complementarySide = lastShred.getComplementarySide();

        // Threshold value to determine a close match for the colors
        int threshold = 84; // Adjust this value based on the image characteristics

        // Iterate through all the shreds and their indices
        for (int i = 0; i < allShreds.size(); i++) {
            Shred shred = allShreds.get(i);

            // Skip the last shred in the working strip (it cannot be its own neighbor)
            if (shred.equals(lastShred)) {
                continue;
            }

            // Get the side of the shred that will be compared with the complementary side of the last shred
            Color[] currentSide = shred.getSide(Shred.Side.LEFT); // Adjust the side according to the layout of shreds

            // Check if the colors of corresponding pixels are a close match
            boolean isPotentialNeighbor = true;
            for (int j = 0; j < currentSide.length; j++) {
                if (!isColorCloseMatch(currentSide[j], complementarySide[j], threshold)) {
                    isPotentialNeighbor = false;
                    break;
                }
            }

            // If the colors are a close match, add the index of the shred to the list
            if (isPotentialNeighbor) {
                potentialNeighbors.add(i);
            }
        }
        return potentialNeighbors;
    }

    /**
     * Show potential neighboring shreds with a green border,
     * uses highLight() method defined in the Shred class
     */
    public void showNeighbors() {
        for (int index : findPotentialNeighbors()) {
            if (index >= 0 && index < allShreds.size()) {
                Shred shred = allShreds.get(index);
                double left = LEFT + index * SIZE; // Adjust LEFT and SIZE according to your constants
                double top = TOP_ALL;
                shred.highLight(left, top);
            } else {
                return;
            }
        }
    }

    /**
     * Hides the highlighted shreds
     */
    public void hideNeighbors() {
        neighborState = false;
        display();
    }

    //=============================================================================
    //  End of challange section
    //=============================================================================

    // Completed for you. Do not change.
    // loadImage and saveImage may be useful for the challenge.

    /**
     * Displays the remaining Shreds, the working strip, and all completed strips
     */
    public void display() {
        UI.clearGraphics();

        // list of all the remaining shreds that haven't been added to a strip
        double x = LEFT;
        for (Shred shred : allShreds) {
            shred.drawWithBorder(x, TOP_ALL);
            x += SIZE;
        }

        //working strip (the one the user is workingly working on)
        x = LEFT;
        for (Shred shred : workingStrip) {
            shred.draw(x, TOP_WORKING);
            x += SIZE;
        }
        UI.setColor(Color.red);
        UI.drawRect(LEFT - 1, TOP_WORKING - 1, SIZE * workingStrip.size() + 2, SIZE + 2);
        UI.setColor(Color.black);

        //completed strips
        double y = TOP_STRIPS;
        for (List<Shred> strip : completedStrips) {
            x = LEFT;
            for (Shred shred : strip) {
                shred.draw(x, y);
                x += SIZE;
            }
            UI.drawRect(LEFT - 1, y - 1, SIZE * strip.size() + 2, SIZE + 2);
            y += SIZE + GAP;
        }
    }

    /**
     * Returns which column the mouse position is on.
     * This will be the index in the list of the shred that the mouse is on,
     * (or the index of the shred that the mouse would be on if the list were long enough)
     */
    public int getColumn(double x) {
        return (int) ((x - LEFT) / (SIZE));
    }

    /**
     * Returns the strip that the mouse position is on.
     * This may be the list of all remaining shreds, the working strip, or
     * one of the completed strips.
     * If it is not on any strip, then it returns null.
     */
    public List<Shred> getStrip(double y) {
        int row = (int) ((y - TOP_ALL) / (SIZE + GAP));
        if (row <= 0) {
            return allShreds;
        } else if (row == 1) {
            return workingStrip;
        } else if (row - 2 < completedStrips.size()) {
            return completedStrips.get(row - 2);
        } else {
            return null;
        }
    }


    /**
     * Load an image from a file and return as a two-dimensional array of Color.
     * From COMP 102 assignment 8&9.
     * Maybe useful for the challenge. Not required for the core or completion.
     */
    public Color[][] loadImage(String imageFileName) {
        if (imageFileName == null || !Files.exists(Path.of(imageFileName))) {
            return null;
        }
        try {
            BufferedImage img = ImageIO.read(Files.newInputStream(Path.of(imageFileName)));
            int rows = img.getHeight();
            int cols = img.getWidth();
            Color[][] ans = new Color[rows][cols];
            for (int row = 0; row < rows; row++) {
                for (int col = 0; col < cols; col++) {
                    Color c = new Color(img.getRGB(col, row));
                    ans[row][col] = c;
                }
            }
            return ans;
        } catch (IOException e) {
            UI.println("Reading Image from " + imageFileName + " failed: " + e);
        }
        return null;
    }

    /**
     * Save a 2D array of Color as an image file
     * From COMP 102 assignment 8&9.
     * Maybe useful for the challenge. Not required for the core or completion.
     */
    public void saveImage(Color[][] imageArray, String imageFileName) {
        int rows = imageArray.length;
        int cols = imageArray[0].length;
        BufferedImage img = new BufferedImage(cols, rows, BufferedImage.TYPE_INT_RGB);
        for (int row = 0; row < rows; row++) {
            for (int col = 0; col < cols; col++) {
                Color c = imageArray[row][col];
                img.setRGB(col, row, c.getRGB());
            }
        }
        try {
            if (imageFileName == null) {
                return;
            }
            ImageIO.write(img, "png", Files.newOutputStream(Path.of(imageFileName)));
        } catch (IOException e) {
            UI.println("Image reading failed: " + e);
        }

    }

    /**
     * Creates an object and set up the user interface
     */
    public static void main(String[] args) {
        DeShredder ds = new DeShredder();
        ds.setupGUI();
    }
}
