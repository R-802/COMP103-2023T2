// This program is copyright VUW.
// You are granted permission to use it to construct your answer to a COMP103 assignment.
// You may not distribute it in any other way without permission.

/* Code for COMP103 - 2023T2, Assignment 6
 * Name: Shemaiah Rangitaawa
 * Username: rangitshem
 * ID: 300601546
 */

import ecs100.UI;
import ecs100.UIFileChooser;

import java.awt.*;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.*;

public class BusNetworks {
    /**
     * Map of towns, indexed by their names
     */
    private final Map<String, Town> busNetwork = new HashMap<>();
    private final int animationSpeed = 20; // Speed of the animation
    private final int circleSize = 13; // Size of the drawn vertices

    public static void main(String[] arguments) {
        BusNetworks bnw = new BusNetworks();
        bnw.setupGUI();
    }

    /**
     * Core: Loads a network of towns from a specified file and constructs a set of {@code Town} objects
     * in the {@code busNetwork} field.
     * <p>
     * The method expects a file where:
     * <ul>
     *     <li>The first line contains the names of all the towns, separated by whitespace.</li>
     *     <li>Each subsequent line contains pairs of town names that are connected, separated by whitespace.</li>
     * </ul>
     * The {@code busNetwork} is populated with {@code Town} objects, each having a name and a set
     * of neighbouring towns based on the file content. The UI text is cleared at the start of the
     * method, and a message is printed to the UI upon successful loading of the towns.
     *
     * @param filename the name of the file containing the town network data
     * @throws RuntimeException if loading the file fails, with the cause being the original {@code IOException}
     */
    public void loadNetwork(String filename) {
        try {
            UI.clearText();
            busNetwork.clear();

            // Read file and get town names from first line of file
            List<String> lines = Files.readAllLines(Path.of(filename));
            String firstLine = lines.remove(0);
            String[] townNames = firstLine.split("\\s+");

            // Add all towns to the busNetwork field
            for (String town : townNames) busNetwork.put(town, new Town(town));
            constructGraph(lines); // Construct the graph
            UI.println("Loaded " + busNetwork.size() + " towns!");
        } catch (IOException e) {
            throw new RuntimeException("Loading " + filename + " failed", e);
        }
    }

    /**
     * Helper method for loadNetwork and loadChallenge, constructs the graph of towns based on the provided lines.
     *
     * @param lines List of strings containing the town names and their connections
     */
    private void constructGraph(List<String> lines) {
        for (String line : lines) {
            String[] townNames = line.split("\\s+");
            Town firstTown = busNetwork.get(townNames[0]);
            Town secondTown = busNetwork.get(townNames[1]);
            if (firstTown != null && secondTown != null) {
                firstTown.addNeighbour(secondTown);
                secondTown.addNeighbour(firstTown);
            }
        }
    }

    /**
     * Core: Prints the names of all towns in the network along with their immediate neighbours to the UI.
     * <p>
     * The method iterates through each {@code Town} object in the {@code busNetwork} and prints
     * a line to the UI for each town. Each line starts with the name of the town, followed by
     * an arrow (" -> ") and the names of all its immediate neighbours, separated by commas.
     * The text is cleared at the start of the method, and a header line is printed before
     * the town information to provide context to the user.
     * <p>
     * Example Output:
     * <pre>
     * The current network:
     * ====================
     * TownA -> TownB, TownC
     * TownB -> TownA, TownD
     * TownC -> TownA
     * TownD -> TownB
     */
    public void printNetwork() {
        UI.clearText();
        printWithDivider("The current network:");

        // Iterate through the entry set to avoid multiple lookups for each town
        for (Map.Entry<String, Town> entry : busNetwork.entrySet()) {
            Town town = entry.getValue();
            // Ensure town is not null
            if (town != null) {
                // Get name of the town and its set of neighbors
                String townName = town.getName();
                String neighborNames = getTownName(town.getNeighbours());

                // Print each town and its set of neighbors
                UI.println(townName + " -> " + neighborNames);
                UI.sleep(animationSpeed);
            }
        }
    }

    /**
     * Retrieves a comma-separated string of neighbor town names for the provided town. Used in
     * printNetwork to clean up the output of the string. I didn't want to modify the to string
     * method of the Town class.
     *
     * @return A string containing all neighbor names of the given town, separated by commas.
     */
    private String getTownName(Set<Town> neighbors) {
        StringBuilder neighborNamesBuilder = new StringBuilder();

        // Iterate through each neighbor in the set.
        for (Town neighbor : neighbors) {
            // If the StringBuilder is not empty, append a comma and space as a separator.
            if (!neighborNamesBuilder.isEmpty()) {
                neighborNamesBuilder.append(", ");
            }
            // Append the name of the current neighbor to the StringBuilder.
            neighborNamesBuilder.append(neighbor.getName());
        }

        // Convert and return the StringBuilder contents to a String.
        return neighborNamesBuilder.toString();
    }

    //======================//
    //      Completion      //
    //======================//

    /**
     * Completion: Return a set of all the nodes that are connected to the given node.
     * Traverse the network from this node in the standard way, using a
     * visited set, and then return the visited set
     */
    public Set<Town> findAllConnected(Town startingTown) {
        // Using LinkedHashSet to maintain insertion order
        Set<Town> visitedTowns = new LinkedHashSet<>();
        _findAllConnected(startingTown, visitedTowns);
        return visitedTowns;
    }

    /**
     * Helper for findAllConnected, traverses the graph depth first
     * keeping track of all visited towns using a set. Used in completion.
     *
     * @param currentTown  Method with return the neighbors of this town
     * @param visitedTowns Used to keep track of the towns we've visited
     */
    private void _findAllConnected(Town currentTown, Set<Town> visitedTowns) {
        visitedTowns.add(currentTown);
        for (Town neighborTown : currentTown.getNeighbours()) {
            // Check if the neighbor town has been visited before.
            if (!visitedTowns.contains(neighborTown)) {
                // If not, recursively search through its neighbors.
                _findAllConnected(neighborTown, visitedTowns);
            }
        }
    }

    /**
     * Completion: This method prints out all towns that are reachable
     * from the specified town within the network. The specified town
     * itself is not included in the printed list.
     *
     * @param townName The name of the town from which reachable towns are to be determined
     */
    public void printReachable(String townName) {
        UI.clearText();
        Town startingTown = busNetwork.get(townName);
        if (startingTown == null) {
            UI.println(townName + " is not a recognized town!");
        } else {
            printWithDivider("From " + startingTown.getName() + " you can get to:");
            for (Town neighborTown : findAllConnected(startingTown)) {
                // Ensure the starting town itself is not printed
                if (!neighborTown.getName().equals(townName)) {
                    UI.println(neighborTown.getName());
                    UI.sleep(animationSpeed);
                }
            }
        }
    }

    /**
     * Completion: This method prints all groups of connected towns within the network.
     * Every connected set of towns is printed on a new line, prefixed by a group number.
     */
    public void printConnectedGroups() {
        UI.clearText();
        printWithDivider("Groups of Connected Towns:");
        Set<Town> unvisitedTowns = new HashSet<>(busNetwork.values());
        int groupCounter = 1;

        // Continue the loop until all towns have been visited.
        while (!unvisitedTowns.isEmpty()) {
            // Retrieve a town from the unvisitedTowns set
            Town currentTown = unvisitedTowns.iterator().next();
            Set<Town> connectedTowns = findAllConnected(currentTown);
            UI.print("Group " + groupCounter + ": ");
            for (Town connectedTown : connectedTowns) {
                UI.print(connectedTown.getName() + ", ");
            }
            UI.println();
            unvisitedTowns.removeAll(connectedTowns);
            groupCounter++;
        }
    }

    //======================//
    //      Challenge       //
    //======================//

    /**
     * Loads a network of towns from a specified file, constructs a graph, and displays the vertices on the UI.
     * <p>
     * The method expects a file with the following format:
     * <ul>
     *     <li>The first line contains the total number of towns.</li>
     *     <li>Subsequent lines contain the name, latitude, and longitude of each town, separated by whitespace.</li>
     *     <li>After the town data, lines contain pairs of town names that are connected, separated by whitespace.</li>
     * </ul>
     * The method performs the following operations:
     * <ul>
     *     <li>Clears the UI panes and the {@code busNetwork} map.</li>
     *     <li>Reads the town data from the file and adds {@code Town} objects to the {@code busNetwork}.</li>
     *     <li>Constructs the graph of towns and their connections.</li>
     *     <li>Displays the vertices (towns) on the UI.</li>
     *     <li>Prints a message to the UI indicating the number of towns loaded.</li>
     *     <li>Displays a message on the UI instructing the user to click to draw connected towns.</li>
     *     <li>Sets a mouse listener to handle user clicks.</li>
     * </ul>
     *
     * @throws RuntimeException if loading the file fails, with the cause being the original exception
     */
    public void loadChallenge() {
        try {
            UI.clearPanes();
            busNetwork.clear();
            double latitudeOffset = 35, longitudeOffset = -165;
            int magnification = 55;

            // Read file and retrieve the number of towns
            List<String> lines = Files.readAllLines(Path.of("BusNetworks\\data-with-lat-long.txt"));
            int numberOfTowns = Integer.parseInt(lines.remove(0).trim()); // Retrieve and remove number of towns
            for (int i = 0; i < numberOfTowns; i++) {
                String[] townData = lines.remove(0).split("\\s+");
                String townName = townData[0];
                double latitude = Double.parseDouble(townData[1]);
                double longitude = Double.parseDouble(townData[2]);
                if (latitude > 0) latitude *= -1;
                latitude += latitudeOffset;
                longitude += longitudeOffset;
                double x = longitude * magnification;
                double y = (latitude * -magnification) + 50; // + 50 padding to keep inside boundaries
                busNetwork.put(townName, new Town(townName, x, y));
            }
            constructGraph(lines); // Construct the graph
            displayVertices(); // Display all vertices on the UI
            UI.println("Loaded " + numberOfTowns + " towns!");
            UI.drawString("Click to draw connected towns", 2, 12);
            UI.setMouseListener(this::doMouse); // Did not call in setupGUI as we're not using it for other methods
        } catch (Exception e) {
            throw new RuntimeException("Loading data-with-lat-long.txt failed", e);
        }
    }

    /**
     * Displays all vertices (towns) on the UI by drawing them on the graphics window.
     * <p>
     * The method iterates through all the {@code Town} objects in the {@code busNetwork}
     * and utilizes the {@code drawVertex} method to draw each vertex on the UI. The
     * vertices are drawn at their respective (x, y) coordinates and display the town's
     * name. The graphics window and text are cleared at the start to ensure a clean slate
     * for drawing.
     */
    private void displayVertices() {
        UI.clearPanes();
        for (Town town : busNetwork.values()) {
            drawVertex(town.getX(), town.getY(), town.getName(), false);
        }
    }

    /**
     * Handles mouse events on the UI.
     * The method uses {@code getTownByCoordinates} to retrieve the town at the
     * selected (x, y) position.
     * <p>
     * The method also performs the following operations:
     * <ul>
     *     <li>Retrieves the selected towns.</li>
     *     <li>Calls {@code drawVertices} to clear panes and redraw vertices.</li>
     *     <li>Displays the selected towns neighbors and its connections.</li>
     *     <li>Highlights the selected town vertex green.</li>
     *     <li>Prints the selected town and its connected neighbors.</li>
     * </ul>
     *
     * @param action Mouse action
     * @param x      Mouse x coordinate
     * @param y      Mouse y coordinate
     */
    private void doMouse(String action, double x, double y) {
        if (action.equals("pressed")) {
            Town clickedTown = getTownByCoordinates(x, y);
            if (clickedTown != null) {
                displayVertices(); // Clears the graphics and text window and redraws all vertices
                UI.drawString("Click to draw connected towns", 2, 12); // Redisplay the message

                // Draw the towns neighbors and its connections
                for (Town neighbour : clickedTown.getNeighbours()) {
                    drawLine(clickedTown.getX(), clickedTown.getY(), neighbour.getX(), neighbour.getY(), 0);
                    drawVertex(neighbour.getX(), neighbour.getY(), neighbour.getName(), true);
                }

                // Color the clicked town green
                UI.setColor(Color.green);
                drawVertex(clickedTown.getX(), clickedTown.getY(), clickedTown.getName(), true);
                UI.setColor(Color.black); // Reset color to black

                // Get name of the town and its set of neighbors
                String townName = clickedTown.getName();
                String neighborNames = getTownName(clickedTown.getNeighbours());

                // Print each town and its set of neighbors
                printWithDivider(townName + " Connections:");
                UI.println(townName + " -> " + neighborNames);
            }
        }
    }

    /**
     * Draws a singular vertex at the specified (x, y) coordinate and displays the provided text.
     * <p>
     * The method draws a circle (vertex) at the specified coordinates and displays
     * the provided text centered within the circle. The vertex can be optionally
     * highlighted by filling the circle with a color (initialized before the call), based on the {@code highlight}
     * parameter.
     *
     * @param x         the x-coordinate of the center of the vertex
     * @param y         the y-coordinate of the center of the vertex
     * @param text      the text to be displayed centered within the vertex
     * @param highlight a boolean flag indicating whether the vertex should be
     *                  highlighted (filled) or not
     */
    private void drawVertex(double x, double y, String text, boolean highlight) {
        int fontSize = 11;
        UI.setFontSize(fontSize);
        if (highlight) {
            UI.fillOval(x - ((double) circleSize / 2), y - ((double) circleSize / 2), circleSize, circleSize);
        } else {
            UI.drawOval(x - ((double) circleSize / 2), y - ((double) circleSize / 2), circleSize, circleSize);
        }

        // Estimate text dimensions based on text length
        int estimatedTextWidth = text.length() * 6;

        // Calculate coordinates
        double textX = x - ((double) estimatedTextWidth / 2);
        double textY = y + ((double) fontSize / 2) - ((double) 165 / fontSize); // prevent font overlapping the circle
        UI.drawString(text, textX, textY);
    }

    /**
     * Recursively draws a line between two points (x1, y1) and (x2, y2) in segments,
     * providing a visual effect of the line being drawn progressively.
     * <p>
     * The method utilizes the {@code calculateDistance} method to determine the
     * Euclidean distance between the two points and draws small line segments
     * from point (x2, y2) towards point (x1, y1) until the entire line is drawn.
     * The drawing speed is controlled by the {@code segmentLength} and the delay
     * introduced by {@code UI.sleep(1)}.
     *
     * @param x1            the x-coordinate of the starting point of the line
     * @param y1            the y-coordinate of the starting point of the line
     * @param x2            the x-coordinate of the ending point of the line
     * @param y2            the y-coordinate of the ending point of the line
     * @param currentLength the current length of the line. Initially, this should be 0.
     */
    private void drawLine(double x1, double y1, double x2, double y2, double currentLength) {
        // Calculate point distance. Used to ensure the line is fully drawn
        double pointDistance = calculateDistance(x1, y1, x2, y2);
        if (currentLength >= pointDistance) return; // Stop when the line is fully drawn

        // Calculate the direction and length of the small segment to be drawn
        double dx = (x2 - x1) / pointDistance;
        double dy = (y2 - y1) / pointDistance;
        double segmentLength = 2; // Drawing speed

        // Calculate the new start point of the segment
        double newStartX = x2 - dx * currentLength;
        double newStartY = y2 - dy * currentLength;
        UI.setLineWidth(1); // Draw the small line segment
        UI.drawLine(newStartX, newStartY, x2, y2);
        UI.sleep(segmentLength); // Delay for visualization

        // Recursive call with extended segment
        drawLine(x1, y1, x2, y2, currentLength + segmentLength);
    }

    /**
     * Retrieves a {@code Town} object based on the provided x and y coordinates.
     * <p>
     * This method iterates through all the towns in the {@code busNetwork} and calculates
     * the distance from the provided coordinates to each town using the
     * {@code calculateDistance} method. If the distance is less than or equal to
     * the threshold, which is defined as an inverse relationship to the {@code circleSize},
     * the town is returned. If no town is found within the threshold distance, {@code null} is returned.
     * <p>
     * The threshold is calculated as:
     * <br>
     * {@code threshold = scalingFactor / circleSize}
     * <p>
     * where {@code scalingFactor} is a constant that can be adjusted based on specific use cases
     * to get the desired sensitivity for the threshold.
     *
     * @param x the x-coordinate to search for a town
     * @param y the y-coordinate to search for a town
     * @return the {@code Town} object found within the threshold distance, or
     * {@code null} if no town is found
     */
    private Town getTownByCoordinates(double x, double y) {
        // Iterate through all towns in the busNetwork to find town within threshold distance
        for (Map.Entry<String, Town> entry : busNetwork.entrySet()) {
            Town town = entry.getValue();
            double distance = calculateDistance(x, y, town.getX(), town.getY());
            double scalingFactor = 165;

            // Inverse relationship between circle size and threshold
            double threshold = scalingFactor / circleSize;
            if (distance <= threshold) return town;
        }
        return null; // Return null if no town is found within the threshold distance
    }

    /**
     * Calculates the Euclidean distance between two points (x1, y1) and (x2, y2)
     * in a 2D plane.
     * <p>
     * The distance is calculated using the formula:
     * <br>
     * {@code distance = sqrt((x2 - x1)^2 + (y2 - y1)^2)}
     *
     * @param x1 the x-coordinate of the first point
     * @param y1 the y-coordinate of the first point
     * @param x2 the x-coordinate of the second point
     * @param y2 the y-coordinate of the second point
     * @return the Euclidean distance between the two points
     */
    private double calculateDistance(double x1, double y1, double x2, double y2) {
        return Math.sqrt((x2 - x1) * (x2 - x1) + (y2 - y1) * (y2 - y1));
    }

    /**
     * Iteratively prints a divider under an input string.
     * Very useless method, but I like it.
     *
     * @param s input text
     */
    private void printWithDivider(String s) {
        UI.println(s);
        for (int i = 0; i < s.length(); i++) {
            UI.print("=");
            UI.sleep((double) animationSpeed / 2); // Ensure the animation is faster than the text
        }
        UI.println(); // Print a new line
    }

    /**
     * Set up the GUI
     */
    public void setupGUI() {
        UI.addButton("Load", () -> loadNetwork(UIFileChooser.open()));
        UI.addButton("Print Network", this::printNetwork);
        UI.addTextField("Reachable from", this::printReachable);
        UI.addButton("All Connected Groups", this::printConnectedGroups);
        UI.addButton("Draw Graph", this::loadChallenge);
        UI.addButton("Clear", UI::clearPanes);
        UI.addButton("Quit", UI::quit);
        UI.setWindowSize(1280, 720);
        UI.setDivider(1);
        loadNetwork("BusNetworks\\data-small.txt");
    }
}
