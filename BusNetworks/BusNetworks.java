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

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.stream.Collectors;

public class BusNetworks {
    /**
     * Map of towns, indexed by their names
     */
    private final Map<String, Town> busNetwork = new HashMap<>();

    public static void main(String[] arguments) {
        BusNetworks bnw = new BusNetworks();
        bnw.setupGUI();
    }

    /**
     * Core: Load a network of towns from a file.
     * Constructs a Set of Town objects in the busNetwork field.
     * Each town has a name and a set of neighbouring towns
     * first line of file contains the names of all the towns.
     * Remaining lines have pairs of names of towns that are connected.
     */
    public void loadNetwork(String filename) {
        try {
            busNetwork.clear();
            UI.clearText();

            // Use Files.lines to lazily load lines from the file
            List<String> lines = Files.lines(Path.of(filename)).collect(Collectors.toList());
            String firstLine = lines.remove(0);
            String[] townNames = firstLine.split("\\s+");

            // Add all towns to the busNetwork field
            for (String town : townNames) {
                if (town == null || town.isEmpty()) {
                    throw new IllegalArgumentException("Town name is null or empty");
                }
                busNetwork.put(town, new Town(town));
            }

            // Construct the graph
            for (String line : lines) {
                String[] towns = line.split("\\s+");
                Town townA = busNetwork.get(towns[0]);
                Town townB = busNetwork.get(towns[1]);
                // Add neighbors to each town
                townA.addNeighbour(townB);
                townB.addNeighbour(townA);
            }

            UI.println("Loaded " + busNetwork.size() + " towns:");
        } catch (IOException e) {
            throw new RuntimeException("Loading " + filename + " failed", e);
        }
    }

    /**
     * Core: Print all the towns and their neighbours.
     * Each line starts with the name of the town, followed by
     * the names of all its immediate neighbours.
     */
    public void printNetwork() {
        UI.clearText();
        UI.println("The current network: \n====================");
        // Iterate through the entry set to avoid multiple lookups for each town
        for (Map.Entry<String, Town> entry : busNetwork.entrySet()) {
            Town town = entry.getValue();
            // Ensure town is not null
            if (town != null) {
                // Get name of the town and its set of neighbors
                String townName = town.getName();
                String neighborNames = getString(town);

                // Print each town and its set of neighbors
                UI.println(townName + " -> " + neighborNames);
            }
        }
    }

    /**
     * Retrieves a comma-separated string of neighbor town names for the provided town. Used in
     * printNetwork to clean up the output of the string. I didn't want to modify the to string
     * method of the Town class.
     *
     * @param town The town for which to retrieve neighbor names.
     * @return A string containing all neighbor names of the given town, separated by commas.
     */
    private String getString(Town town) {
        // Retrieve the set of neighboring towns.
        Set<Town> neighbors = town.getNeighbours();
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
        // Add the current town to the visited set.
        visitedTowns.add(currentTown);
        // Use a for-each loop to iterate through all neighbor towns.
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
            UI.println("From " + startingTown.getName() + " you can get to:");
            for (Town neighborTown : findAllConnected(startingTown)) {
                // Ensure the starting town itself is not printed
                if (!neighborTown.getName().equals(townName)) {
                    UI.println(neighborTown.getName());
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
        UI.println("Groups of Connected Towns: \n==========================");
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
     * Displays the graph of towns and their connections, based on data read from data-with-lat-long.txt
     * Towns are displayed as circles and connections as lines between them.
     */
    public void displayGraph() {
        try {
            busNetwork.clear();
            UI.clearPanes();

            // Constants
            double latitudeOffset = 35, longitudeOffset = -166;
            int margin = 50, circleSize = 7;

            List<String> lines = Files.readAllLines(Path.of("BusNetworks\\data-with-lat-long.txt"));
            int numberOfTowns = Integer.parseInt(lines.remove(0).trim());

            for (int i = 0; i < numberOfTowns; i++) {
                String[] townData = lines.remove(0).split(" ");
                String townName = townData[0];
                double latitude = Double.parseDouble(townData[1]);
                double longitude = Double.parseDouble(townData[2]);

                if (latitude > 0) latitude *= -1;
                latitude += latitudeOffset;
                longitude += longitudeOffset;

                double xCoordinate = longitude * 45;
                double yCoordinate = (latitude * -45) + margin;

                // Drawing "town", the vertex with its name overlapping
                UI.drawOval(xCoordinate - ((double) circleSize / 2), yCoordinate - ((double) circleSize / 2), circleSize, circleSize);
                UI.setFontSize(12);
                UI.drawString(townName, xCoordinate, yCoordinate);
                busNetwork.put(townName, new Town(townName, xCoordinate, yCoordinate));
            }

            for (String connection : lines) {
                String[] townNames = connection.split(" ");
                Town firstTown = busNetwork.get(townNames[0]);
                Town secondTown = busNetwork.get(townNames[1]);
                if (firstTown != null && secondTown != null) {
                    firstTown.addNeighbour(secondTown);
                    secondTown.addNeighbour(firstTown);
                }
            }

            // Drawing connections
            for (Town town : busNetwork.values()) {
                for (Town neighbour : town.getNeighbours()) {
                    UI.setLineWidth(1);
                    UI.drawLine(town.x, town.y, neighbour.x, neighbour.y);
                }
            }

        } catch (Exception e) {
            throw new RuntimeException("Failed to load data!", e);
        }
    }

    /**
     * Set up the GUI (buttons and mouse)
     */
    public void setupGUI() {
        UI.addButton("Load", () -> loadNetwork(UIFileChooser.open()));
        UI.addButton("Print Network", this::printNetwork);
        UI.addTextField("Reachable from", this::printReachable);
        UI.addButton("All Connected Groups", this::printConnectedGroups);
        UI.addButton("Draw Graph", this::displayGraph);
        UI.addButton("Clear", UI::clearText);
        UI.addButton("Quit", UI::quit);
        UI.setWindowSize(1280, 720);
        UI.setDivider(1);
        loadNetwork("BusNetworks\\data-small.txt");
    }
}
