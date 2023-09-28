// This program is copyright VUW.
// You are granted permission to use it to construct your answer to a COMP103 assignment.
// You may not distribute it in any other way without permission.

/* Code for COMP103 - 2023T2, Assignment 5
 * Name: Shemaiah Rangitaawa
 * Username: rangitshem
 * ID: 300601546
 */

import ecs100.UI;
import ecs100.UIFileChooser;

import java.awt.*;
import java.io.IOException;
import java.io.PrintStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Scanner;

/**
 * <description of class OrganisationChart>
 */

public class OrganisationChart {

    // constants for the layout
    public static final double NEW_LEFT = 10;  // left of the new position Icon
    public static final double NEW_TOP = 10;   // top of the new position Icon
    public static final double ICON_X = 40;    // location and size of the remove icon
    public static final double ICON_Y = 100;   //  the user entered
    public static final double ICON_RAD = 20;

    // Fields
    private Position organisation;             // the root of the current organisational chart
    private Position pressedPosition = null;   // the position on which the mouse was pressed
    private Position selectedPosition = null;  // the selected position on which we can modify

    //  the attributes.
    private boolean newPosition = false;       // adding a new Position to tree
    private Position position = null;

    // Main
    public static void main(String[] arguments) {
        OrganisationChart oc = new OrganisationChart();
        oc.setupGUI();
        oc.initialiseChart();
    }

    /**
     * Set up the GUI (buttons and mouse)
     */
    public void setupGUI() {
        UI.setMouseListener(this::doMouse);
        UI.setMouseMotionListener(this::doMouse); // To detect drag action
        UI.addTextField("Change Role", this::setRole);
        UI.addButton("Load test tree", this::makeTestTree);
        UI.addButton("Save Tree", this::save);
        UI.addButton("Load Tree", () -> load(UIFileChooser.open("Select File")));
        UI.addButton("Quit", UI::quit);
        UI.setWindowSize(1100, 500);
        UI.setDivider(0);
    }

    /**
     * initialise the root of the organisation
     */
    public void initialiseChart() {
        organisation = new Position("CEO");   // Set the root node of the organisation
        redraw();
    }

    /**
     * If a Position has been selected, update the name of the role of this position
     */
    public void setRole(String v) {
        if (selectedPosition != null) {
            selectedPosition.setRole(v);
        }
        redraw();
    }

    /**
     * Most of the work is initiated by the mouse.
     * <p>
     * The action depends on where the mouse is pressed:
     * on the new icon,
     * a Position in the tree, or
     * and where it is released:
     * on the same Position,
     * another Position in the tree,
     * on the delete Icon, or
     * empty space
     * <p>
     * See the table in the assignment description.
     * The method follows the structure of the table.
     */
    public void doMouse(String action, double x, double y) {
        switch (action) {
            case "pressed" -> {
                //initialise
                newPosition = false;
                pressedPosition = null;
                if (onNewIcon(x, y)) {// adding a new vacant Position to tree
                    newPosition = true;
                } else { // acting on an existing Position
                    pressedPosition = findPosition(x, y, organisation);
                    if (pressedPosition != null) {
                        pressedPosition.draw(false, true);
                    }
                }
            }
            case "dragged" -> {
                if (pressedPosition != null) {
                    // Constants keep the cursor in the center of a node
                    pressedPosition.moveOffset(x + 5);
                    pressedPosition.moveOffsetY(y - 55);
                    this.redraw();
                }
            }
            case "released" -> {
                Position targetPosition = findPosition(x, y, organisation);

                // pressed on "new" icon, released on a target
                if (newPosition && targetPosition != null) {
                    Position newP = new Position();
                    addNewPosition(newP, targetPosition);     // Method to complete!
                }
                // pressed and released on a Position
                else if (pressedPosition != null && targetPosition == pressedPosition) {
                    // selecting a position
                    selectedPosition = targetPosition;
                }
                // pressed on a Position and released on empty space
                else if (pressedPosition != null && targetPosition == null && !onRemoveIcon(x, y)) {
                    // move the Position to left or right
                    pressedPosition.moveOffset(x);
                }
                // pressed on a Position
                else if (pressedPosition != null) {
                    if (targetPosition != null) {
                        // moving pressed position to target
                        movePosition(pressedPosition, targetPosition);  // Method to complete!
                    } else if (onRemoveIcon(x, y)) {
                        // removing Position from tree
                        removePosition(pressedPosition);                // Method to complete!
                    }
                }
                this.redraw();
            }
        }
    }

    /**
     * [STEP 1:]
     * Recursive method to draw all nodes in a subtree, given the root node.
     * (The provided code just draws the root node;
     * you need to make it draw all the nodes.)
     */
    private void drawTree(Position pos) {
        if (pos == selectedPosition) pos.drawHighlighted();
        else pos.draw();
        for (Position teamMember : pos.getTeam()) {
            drawTree(teamMember);
        }
    }

    /**
     * Find and return a Position that is currently placed over the point (x,y).
     * Must do a recursive search of the subtree whose root is the given Position.
     * [STEP 2:]
     * Returns a Position if it finds one,
     * Returns null if it doesn't.
     * [Completion:] If (x,y) is under two Positions, it should return the top one.
     */
    private Position findPosition(double x, double y, Position pos) {
        if (pos == null) return null;
        if (pos.on(x, y)) return pos;
        for (Position teamMember : pos.getTeam()) {
            Position ans = findPosition(x, y, teamMember);
            if (ans != null) return ans;
        }
        return null;
    }

    /**
     * [STEP 2:]
     * Add the new position to the target's team.d
     * Check the arguments are valid first.
     */
    public void addNewPosition(Position newPos, Position target) {
        if (newPos == null || target == null) return;
        target.addToTeam(newPos);
    }

    /**
     * [STEP 2:]
     * Move a current position (pos) to another position (target)
     * by adding the position to the team of the target,
     * (and bringing the whole subtree of the position with them)
     * Check the arguments are valid first.
     * <p>
     * [COMPLETION:]
     * Moving any position to a target that is in the
     * position's subtree is a problem and should not be allowed. (Why?)
     * (one consequence is that the CEO position can't be moved at all)
     */
    private void movePosition(Position pos, Position target) {
        if (pos == null || target == null) return;
        if (pos != organisation && !inSubtree(pos, target)) {
            Position manager = pos.getManager();
            manager.removeFromTeam(pos);
            target.addToTeam(pos);
        }
    }

    /**
     * [STEP 2:]
     * Remove a position by removing it from the tree completely.
     * The position cannot be a manager of another position.
     * If this removes the current selected position, then there
     * should now be no selected position
     */
    public void removePosition(Position pos) {
        if (!pos.isManager()) pos.getManager().removeFromTeam(pos);
    }

    //----------------------//
    //      CHALLENGE       //
    //----------------------//

    /**
     * [COMPLETION:]
     * Return true if position is in the subtree, and false otherwise
     * Uses == to determine node equality
     * Check if position is the same as the root of subTree
     * if not, check if in any of the subtrees of the team members of the root
     * (recursive call, which must return true if it finds the position)
     */
    private boolean inSubtree(Position pos, Position subtree) {
        if (subtree == null || pos == null) return false;
        else if (subtree == pos) return true;
        else {
            for (Position position : pos.getTeam()) { //get child of pos
                boolean ans = inSubtree(position, subtree);
                if (ans) return true;
            }
        }
        return false;
    }

    public void save() {
        String fileName = UIFileChooser.save("Save As");
        if (fileName == null) return; // Ensure Validity
        else if (!fileName.endsWith(".TXT") && !fileName.endsWith(".txt")) fileName += ".txt";
        List<String> team = new ArrayList<>();
        for (Position member : organisation.getTeam()) team.add(member.toStringFull());
        try {
            PrintStream stream = new PrintStream(fileName);
            stream.println(organisation.getRole() + " " + team);
            _save(organisation, stream);
        } catch (Exception e) {
            UI.println("Save Failed");
        }
    }

    private void _save(Position pos, PrintStream stream) {
        // For each member in the team
        for (Position member : pos.getTeam()) {
            if (!member.isManager()) return;
            List<String> team = new ArrayList<>();
            for (Position info : member.getTeam()) team.add(info.toStringFull());
            stream.println(member.getRole() + " " + team);
            _save(member, stream);
        }
    }

    /**
     * Loads organization tree from selected file.
     *
     * @param fileName name of the file selected in the file chooser
     */
    public void load(String fileName) {
        // Ensure file name and path exist
        if (fileName == null) return;
        Path path = Path.of(fileName);
        if (!Files.exists(path)) return;
        try {
            List<String> lines = Files.readAllLines(path); // Lines contains the file contents
            organisation = _load(lines, null); // Initialize organization to loaded tree
            redraw();
        } catch (IOException e) {
            UI.println("Failed to read file");
        }
    }


    private Position _load(List<String> lines, Position root) {
        for (String line : lines) {
            Scanner scan = new Scanner(line);
            String name = scan.next();
            Position node = new Position(name);
            if (root != null) {
                this.position = null;
                find(root, name, false);
                if (this.position != null) node = this.position;
            }
            String contentsRAW = scan.nextLine().trim(); // Scan contents of file by line
            String contentsNOB = contentsRAW.replace("[", ""); // Remove opening brackets
            String contentsNCB = contentsNOB.replace("]", ""); // Remove closing brackets
            String[] content = contentsNCB.split(",");
            List<String> members = new ArrayList<>(Arrays.asList(content));
            for (String member : members) {
                Scanner scanner = new Scanner(member);
                String childName = scanner.next();
                double offSet = scanner.nextDouble();
                Position child = new Position(childName, offSet);
                node.addToTeam(child);
            }
            if (root == null) root = node;
        }
        return root;
    }

    private void find(Position pos, String name, boolean found) {
        if (found) return;
        if (pos.getRole().equals(name)) {
            this.position = pos;
            found = true;
        }
        for (Position p : pos.getTeam()) find(p, name, found);
    }


    // OTHER DRAWING METHODS =======================================

    /**
     * Redraw the entire organisation chart.
     */
    private void redraw() {
        UI.clearGraphics();
        drawTree(organisation);
        drawNewIcon();
        drawRetireIcon();
    }

    /**
     * Redraw the new Person box
     */
    private void drawNewIcon() {
        UI.setColor(Position.BACKGROUND_COL);
        UI.fillRect(NEW_LEFT, NEW_TOP, Position.WIDTH, Position.HEIGHT);
        UI.setColor(Color.black);
        UI.drawRect(NEW_LEFT, NEW_TOP, Position.WIDTH, Position.HEIGHT);
        UI.drawString("NEW", NEW_LEFT + 8, NEW_TOP + Position.HEIGHT / 2 - 5);
        UI.drawString("POSN", NEW_LEFT + 5, NEW_TOP + Position.HEIGHT / 2 + 10);
    }

    /**
     * Redraw the remove Icon
     */
    private void drawRetireIcon() {
        UI.setColor(Color.red);
        UI.setLineWidth(5);
        UI.drawOval(ICON_X - ICON_RAD, ICON_Y - ICON_RAD, ICON_RAD * 2, ICON_RAD * 2);
        double off = ICON_RAD * 0.68;
        UI.drawLine((ICON_X - off), (ICON_Y - off), (ICON_X + off), (ICON_Y + off));
        UI.setLineWidth(1);
        UI.setColor(Color.black);
    }

    /**
     * is the mouse position on the New Position box
     */
    private boolean onNewIcon(double x, double y) {
        return ((x >= NEW_LEFT) && (x <= NEW_LEFT + Position.WIDTH) && (y >= NEW_TOP) && (y <= NEW_TOP + Position.HEIGHT));
    }

    // Testing ==============================================

    /**
     * is the mouse position on the remove icon
     */
    private boolean onRemoveIcon(double x, double y) {
        return (Math.abs(x - ICON_X) < ICON_RAD) && (Math.abs(y - ICON_Y) < ICON_RAD);
    }

    /**
     * Makes an initial tree so you can test your program
     */
    private void makeTestTree() {
        organisation = new Position("CEO");
        Position aa = new Position("VP1");
        Position bb = new Position("VP2");
        Position cc = new Position("VP3");
        Position dd = new Position("VP4");
        Position a1 = new Position("AL1");
        Position a2 = new Position("AL2");
        Position b1 = new Position("AS");
        Position b2 = new Position("DPA");
        Position d1 = new Position("DBP");
        Position d2 = new Position("SEP");
        Position d3 = new Position("MSP");

        organisation.addToTeam(aa);
        aa.setOffset(-160);
        organisation.addToTeam(bb);
        bb.setOffset(-50);
        organisation.addToTeam(cc);
        cc.setOffset(15);
        organisation.addToTeam(dd);
        dd.setOffset(150);

        aa.addToTeam(a1);
        a1.setOffset(-35);
        aa.addToTeam(a2);
        a2.setOffset(25);
        bb.addToTeam(b1);
        b1.setOffset(-25);
        bb.addToTeam(b2);
        b2.setOffset(35);
        dd.addToTeam(d1);
        d2.setOffset(-60);
        dd.addToTeam(d2);
        dd.addToTeam(d3);
        d3.setOffset(60);
        organisation.addToTeam(aa);
        aa.setOffset(-160);

        selectedPosition = null;
        this.redraw();
    }

    //* Test for printing out the tree structure, indented text */
    private void printTree(Position posn, String indent) {
        UI.println(indent + posn + " " + (posn.getManager() == null ? "noM" : "hasM") + " " + posn.getTeam().size() + " reports");
        String subIndent = indent + "  ";
        for (Position tm : posn.getTeam()) {
            printTree(tm, subIndent);
        }
    }
}
