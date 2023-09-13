// This program is copyright VUW.
// You are granted permission to use it to construct your answer to a COMP103 assignment.
// You may not distribute it in any other way without permission.

/* Code for COMP103 - 2023T2, Assignment 4
 * Name: Shemaiah Rangitaawa
 * Username: rangitshem
 * ID: 300601546
 */

import ecs100.UI;
import ecs100.UIFileChooser;

import java.io.IOException;
import java.io.PrintStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayDeque;
import java.util.Objects;
import java.util.Queue;
import java.util.Scanner;

public class DecisionTree {
    /**
     * The root of the tree.
     */
    public DTNode theTree;

    /**
     * Set up the GUI and initialize a sample tree
     *
     * @param args the input arguments
     */
    public static void main(String[] args) {
        DecisionTree dt = new DecisionTree();
        dt.setupGUI();
        dt.loadTree("extended animal tree.txt");
    }

    /**
     * Set up the interface
     */
    public void setupGUI() {
        UI.addButton("Load Tree", () -> loadTree(UIFileChooser.open("File with a Decision Tree")));
        UI.addButton("Print Tree Core", this::printTreeCore);
        UI.addButton("Print Tree Completion", this::printTree);
        UI.addButton("Run Tree", this::runTree);
        UI.addButton("Grow Tree", this::growTree);
        UI.addButton("Save Tree", this::saveTree); // Completion
        UI.addButton("Draw Tree", this::drawTree); // Challenge
        UI.addButton("Reset", () -> loadTree("extended animal tree.txt"));
        UI.addButton("Quit", UI::quit);
        UI.setDivider(0.5);
        UI.setWindowSize(1280, 720);
    }

    /**
     * Print out the contents of the decision tree in the text pane.
     * The root node should be at the top, followed by its "yes" subtree,
     * and then its "no" subtree.
     * Needs a recursive "helper method" which is passed a node.
     */
    public void printTreeCore() {
        UI.clearText();
        // Print from the root node, initializing the prefix as an empty string
        _printTreeCore(this.theTree, "");
    }

    /**
     * Helper method for printTreeCore
     *
     * @param node   The node to print
     * @param prefix The prefix to print before the node
     */
    private void _printTreeCore(DTNode node, String prefix) {
        // Base case: stop recursion at the end of the tree
        if (node == null) return;
        UI.sleep(15); // Small delay to make visualization nicer
        String affix = (!node.isAnswer()) ? "?" : ""; // Add a question mark if the node is not an answer
        UI.println(prefix + node.getText() + affix);

        // Recursively print the yes and no nodes
        _printTreeCore(node.getYes(), "yes:");
        _printTreeCore(node.getNo(), "no:");
    }

    /**
     * Run the tree by starting at the top (of theTree), and working
     * down the tree until it gets to a leaf node (a node with no children)
     * If the node is a leaf it prints the answer in the node
     * If the node is not a leaf node, then it asks in the node,
     * and depending on the answer, goes to the "yes" child or the "no" child.
     */
    public void runTree() {
        UI.clearText();
        _runTree(this.theTree); // Run the tree from the root node
    }

    /**
     * Helper method for runTree
     *
     * @param node The node to run
     */
    private void _runTree(DTNode node) {
        // If the node is a leaf, print the answer
        if (node.isAnswer()) {
            UI.println("The answer is: " + node.getText());
            return; // Stop recursion
        }

        // Small delay to make visualization nicer
        UI.sleep(15);

        String askString = UI.askString("Is it true: " + node.getText() + " (yes/no)");
        String answer = askString.toLowerCase(); // Convert to lower case to make it easier to check
        if (answer.equals("yes") || answer.equals("y")) {
            _runTree(node.getYes());
        } else if (answer.equals("no") || answer.equals("n")) {
            _runTree(node.getNo());
        } else { // If the user doesn't enter a valid answer, ask again
            blinkBlink(3);
            _runTree(node);
        }
    }

    /**
     * Grow the tree by allowing the user to extend the tree.
     * Like runTree, it starts at the top (of theTree), and works its way down the tree
     * until it finally gets to a leaf node.
     * If the current node has a question, then it asks in the node,
     * and depending on the answer, goes to the "yes" child or the "no" child.
     * If the current node is a leaf it prints the decision, and asks if it is right.
     * If it was wrong, it
     * - asks the user what the decision should have been,
     * - asks for a question to distinguish the right decision from the wrong one
     * - changes the text in the node to be the question
     * - adds two new children (leaf nodes) to the node with the two decisions.
     */
    public void growTree() {
        UI.clearText();
        _growTree(this.theTree); // Run the tree from the root node
    }

    /**
     * Helper method for growTree
     *
     * @param node The node to grow
     */
    private void _growTree(DTNode node) {
        // Base case: stop recursion at the end of the tree
        if (node == null) return;
        UI.sleep(15); // Small delay to make visualization nicer
        if (node.isAnswer()) { // If the node is a leaf, ask if it's correct
            String userAnswer = UI.askString("Is it a " + node.getText() + "? (yes/no) ");
            String lowerCaseAnswer = userAnswer.toLowerCase(); // Convert to lower case to make it easier to check
            if (lowerCaseAnswer.equals("yes") || lowerCaseAnswer.equals("y")) {
                _growTree(node.getYes());
            } else if (lowerCaseAnswer.equals("no") || lowerCaseAnswer.equals("n")) {
                String correctAnswer = UI.askString("Alright, what is the correct answer? ");
                UI.println("Tell me something that's true for a " + correctAnswer + " but not for a " + node.getText() + ". ");
                String newNodeProperty = UI.askString("Property: "); // Ask for a new property

                // Create the new nodes and update the tree
                DTNode yesNode = new DTNode(correctAnswer);
                DTNode noNode = new DTNode(node.getText());
                node.setText(newNodeProperty);
                node.setChildren(yesNode, noNode); // Set the children of the node
                UI.println("Thank you! The decision tree has been updated.");
                drawTree(); // Draw the tree to show the users the changes
                return; // Terminate the method
            } else { // If the user doesn't enter a valid answer, ask again
                blinkBlink(3);
                _growTree(node);
            }
        }

        // If the node is a question, ask and go to the yes or no node
        String currentQuestion = node.getText();
        String userResponse = UI.askString("Is it true: " + currentQuestion + " (yes/no)"); // Ask the question
        String lowerCaseResponse = userResponse.toLowerCase(); // Convert to lower case to make it easier to check
        if (lowerCaseResponse.equals("yes") || lowerCaseResponse.equals("y")) { // Go to the yes or no node
            _growTree(node.getYes());
        } else if (lowerCaseResponse.equals("no") || lowerCaseResponse.equals("n")) {
            _growTree(node.getNo());
        } else {
            blinkBlink(3);
            _growTree(node);
        }
    }

    //---------------------------------//
    //           COMPLETION            //
    //---------------------------------//

    /**
     * COMPLETION:
     * Each node should be indented by how deep it is in the tree.
     * The recursive "helper method" is passed a node and an indentation string.
     */
    public void printTree() {
        UI.clearText();
        // Start printing the tree from the root node
        _printTree(this.theTree, "", "");
    }

    /**
     * COMPLETION:
     * Helper method for printTree
     *
     * @param node   The node to print
     * @param prefix The prefix to print before the node
     * @param indent The indentation to print before the node
     */
    private void _printTree(DTNode node, String prefix, String indent) {
        // Base case: If the current node is null, stop recursion
        if (node == null) return;

        // Small delay to make visualization nicer
        UI.sleep(15);
        String affix = "";

        // Check if the current node is a question node
        affix = (!node.isAnswer()) ? "?" : affix;
        indent += (!node.isAnswer()) ? "   " : "";
        UI.println(prefix + node.getText() + affix);

        // Recursively print the "yes" and "no" branches, passing updated indentation and prefix
        _printTree(node.getYes(), indent + "y:", indent);
        _printTree(node.getNo(), indent + "n:", indent);
    }

    /**
     * Saves the decision tree structure to a text file.
     * The user is prompted to choose a file location using a file dialog.
     * The decision tree is traversed and written to the file using the _saveTree method.
     * If the saving process encounters any errors, an appropriate message is displayed.
     */
    public void saveTree() {
        UI.clearText();
        String filename = UIFileChooser.save("Enter a file name to save your decision tree");
        if (filename == null) {
            UI.println("No file name specified");
            return; // Terminate the method
        }
        try {
            // Create a PrintStream to write to the specified file
            PrintStream out = new PrintStream(filename);

            // Start the traversal from the root node of the decision tree
            DTNode node = this.theTree;

            // Call the recursive method to save the tree's structure to the file
            _saveTree(node, out);

            // Close the output stream to ensure data is written to the file
            out.close();
        } catch (IOException e) {
            // Display an error message if saving the file fails
            UI.println("File saving failed: " + e);
        }
    }

    /**
     * Recursively saves the decision tree structure to a text file.
     * This method traverses the tree in a depth-first manner.
     *
     * @param node The current node being processed.
     * @param out  The PrintStream used for writing to the output file.
     */
    private void _saveTree(DTNode node, PrintStream out) {
        if (node == null) return; // Base case: Stop recursion at the end of the tree
        out.println((node.isAnswer() ? "Answer: " : "Question: ") + node.getText());
        _saveTree(node.getYes(), out); // Save yes child's subtree
        _saveTree(node.getNo(), out);  // Save no child's subtree
    }

    //---------------------------------//
    //           CHALLENGE             //
    //---------------------------------//

    /**
     * Recursively draws the decision tree.
     */
    public void drawTree() {
        UI.clearGraphics();

        // Starting position for drawing
        double x = 60;
        double y = 350;

        // Draw the root node
        drawNode(x, y, x, y, theTree.getText());

        // Start drawing the tree recursively
        if (!theTree.isAnswer()) {
            drawSubtree(theTree.getYes(), true, x, y, y / 2);
            drawSubtree(theTree.getNo(), false, x, y, y / 2);
        }
    }

    /**
     * Helper method for drawTree. Method draws the yes nodes first.
     *
     * @param node      The current node being processed.
     * @param isYesNode Whether the current node is a "yes" node or "no" node.
     * @param x         The x-coordinate of the parent node.
     * @param y         The y-coordinate of the parent node.
     * @param yOffset   The y-offset for the current node.
     */
    private void drawSubtree(DTNode node, boolean isYesNode, double x, double y, double yOffset) {
        UI.sleep(15); // Delay for visualization
        double xDirection = isYesNode ? -1 : 1;  // Offset is  based on whether node is yes or no
        double newX = x + 115; // 115 is the new horizontal distance between nodes
        double newY = y + (yOffset * xDirection);

        // Draw the node. Ternary statement ensures line is drawn to the correct side of the node
        drawNode(newX, newY, x, (!isYesNode) ? y + 11 : y - 11, node.getText());
        yOffset = (yOffset < 100) ? (yOffset + 25) : yOffset; // Ensure nodes don't overlap

        // Base case: Stop recursion at the end of the tree
        if (!node.isAnswer()) {
            drawSubtree(node.getYes(), true, newX, newY, yOffset / 2);
            drawSubtree(node.getNo(), false, newX, newY, yOffset / 2);
        }
    }

    /**
     * Helper method for drawTree. Draws a node and the line connecting it to its parent.
     *
     * @param centerX The x-coordinate of the node.
     * @param centerY The y-coordinate of the node.
     * @param lineX   The x-coordinate of the parent node.
     * @param lineY   The y-coordinate of the parent node.
     * @param text    The text to display in the node.
     */
    private void drawNode(double centerX, double centerY, double lineX, double lineY, String text) {
        // Set font size
        int fontSize = 12;
        UI.setFontSize(fontSize);

        // Estimate text dimensions based on text length
        int estimatedTextWidth = text.length() * 7;

        // Calculate rectangle dimensions based on estimated text size
        double rectWidth = estimatedTextWidth + 10; // Adding padding
        double rectHeight = (double) fontSize + 10; // Adding padding

        // Calculate the coordinates for the top-left corner of the rectangle
        double rectX = centerX - (rectWidth / 2);
        double rectY = centerY - (rectHeight / 2);

        // Draw the line connecting the node to its parent
        drawLine(centerX, centerY, lineX, lineY, 0);

        // Draw the rectangle/node
        UI.setLineWidth(1);
        UI.eraseRect(rectX, rectY, rectWidth, rectHeight); // Just to hide all the mess...
        UI.drawRect(rectX, rectY, rectWidth, rectHeight);

        // Calculate coordinates for centering text within the rectangle
        double textX = centerX - ((double) estimatedTextWidth / 2);
        double textY = centerY + ((double) fontSize / 2);

        // Draw the centered text
        UI.drawString(text, textX, textY);
    }

    /**
     * Recursively draws a line between two points.
     *
     * @param x1            The x-coordinate of the start point.
     * @param y1            The y-coordinate of the start point.
     * @param x2            The x-coordinate of the end point.
     * @param y2            The y-coordinate of the end point.
     * @param currentLength The current length of the line.
     */
    private void drawLine(double x1, double y1, double x2, double y2, double currentLength) {
        // Calculate the Euclidian distance between two points. Used to ensure the line is fully drawn
        double pointDistance = Math.sqrt((x2 - x1) * (x2 - x1) + (y2 - y1) * (y2 - y1));

        // Base case: Stop recursion when the line is fully drawn
        if (currentLength >= pointDistance) return;

        // Calculate the direction and length of the small segment to be drawn
        double dx = (x2 - x1) / pointDistance;
        double dy = (y2 - y1) / pointDistance;
        double segmentLength = 2; // Drawing speed

        // Calculate the new start point of the segment
        double newStartX = x2 - dx * currentLength;
        double newStartY = y2 - dy * currentLength;

        UI.setLineWidth(0.05); // Draw the small line segment
        UI.drawLine(newStartX, newStartY, x2, y2);
        UI.sleep(1); // Delay for visualization

        // Recursive call with extended segment
        drawLine(x1, y1, x2, y2, currentLength + segmentLength);
    }

    //---------------------------------//
    //          CHALLENGE END          //
    //---------------------------------//

    /**
     * Loads a decision tree from a file.
     * Each line starts with either "Question:" or "Answer:" and is followed by the text
     * Calls a recursive method to load the tree and return the root node,
     * and assigns this node to theTree.
     *
     * @param filename the filename
     */
    public void loadTree(String filename) {
        Path path = Path.of(filename);
        if (!Files.exists(path)) {
            UI.println("No such file: " + filename);
            return;
        }
        try {
            theTree = loadSubTree(new ArrayDeque<>(Files.readAllLines(path)));
        } catch (IOException e) {
            UI.println("File reading failed: " + e);
        }
    }

    /**
     * Loads a tree (or subtree) from a Scanner and returns the root.
     * The first line has the text for the root node of the tree (or subtree)
     * It should make the node, and
     * if the first line starts with "Question:", it loads two subtrees (yes, and no)
     * from the scanner and add them as the  children of the node,
     * Finally, it should return the  node.
     *
     * @param lines the lines
     * @return the DT node
     */
    public DTNode loadSubTree(Queue<String> lines) {
        Scanner line = new Scanner(Objects.requireNonNull(lines.poll()));
        String type = line.next();
        String text = line.nextLine().trim();
        DTNode node = new DTNode(text);
        if (type.equals("Question:")) {
            DTNode yesCh = loadSubTree(lines);
            DTNode noCh = loadSubTree(lines);
            node.setChildren(yesCh, noCh);
        }
        return node;
    }

    //---------------------------------//

    /**
     * Easter Egg! Even more recursion!!!
     *
     * @param blinks The number of times to blink.
     */
    private void blinkBlink(int blinks) {
        if (blinks > 0) {
            UI.printMessage("Invalid answer!");
            UI.sleep(100);
            UI.printMessage("");
            UI.sleep(100);
            blinkBlink(blinks - 1);
        }
    }
}
