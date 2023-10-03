// This program is copyright VUW.
// You are granted permission to use it to construct your answer to a COMP103 assignment.
// You may not distribute it in any other way without permission.

/* Code for COMP103 - 2023T2, Assignment 6
 * Name:
 * Username:
 * ID:
 */

import ecs100.UI;
import ecs100.UIFileChooser;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;

public class BusNetworks {

    /**
     * Map of towns, indexed by their names
     */
    private final Map<String, Town> busNetwork = new HashMap<>();

    // Main
    public static void main(String[] arguments) {
        BusNetworks bnw = new BusNetworks();
        bnw.setupGUI();
    }

    /**
     * CORE
     * Loads a network of towns from a file.
     * Constructs a Set of Town objects in the busNetwork field
     * Each town has a name and a set of neighbouring towns
     * First line of file contains the names of all the towns.
     * Remaining lines have pairs of names of towns that are connected.
     */
    public void loadNetwork(String filename) {
        try {
            busNetwork.clear();
            UI.clearText();
            List<String> lines = Files.readAllLines(Path.of(filename));
            String firstLine = lines.remove(0);

            // Add all town names to new array
            List<String> townNames = new ArrayList<>();
            Scanner scan = new Scanner(firstLine);
            while (scan.hasNext()) townNames.add(scan.next());

            // Add all towns to the busNetwork field
            for (String town : townNames) {
                if (town == null) return; // Ensure no null values
                busNetwork.put(town, new Town(town)); // Add town name and new town to the field
            }

            // Add neighbors to graph
            for (String line : lines) {
                Scanner scanLine = new Scanner(line);

                // Get names of each town then get each town's object
                String townAName = scanLine.next();
                String townBName = scanLine.next();
                Town townA = busNetwork.get(townAName);
                Town townB = busNetwork.get(townBName);

                // Ensure no null values
                if (townA == null || townB == null) {
                    return;
                } else {
                    // Add neighbors to each town
                    townA.addNeighbour(townB);
                    townB.addNeighbour(townA);
                }
            }

            UI.println("Loaded " + busNetwork.size() + " towns:");
        } catch (IOException e) {
            throw new RuntimeException("Loading data.txt failed" + e);
        }
    }

    /**
     * CORE
     * Print all the towns and their neighbours:
     * Each line starts with the name of the town, followed by
     * the names of all its immediate neighbours,
     */
    public void printNetwork() {
        UI.clearText();
        UI.println("The current network: \n====================");
        for (String town : busNetwork.keySet()) {
            if (town != null) { // Ensure town is not null
                // Get names of each town and each towns set of neighbors
                String townName = busNetwork.get(town).getName();
                Set<Town> neighbors = busNetwork.get(town).getNeighbours();

                // Print each town and its set of neighbors with no brackets
                UI.println(townName + " -> " + neighbors.toString().replace("[", "").replace("]", ""));
            }
        }
    }

    /**
     * COMPLETION
     * Return a set of all the nodes that are connected to the given node.
     * Traverse the network from this node in the standard way, using a
     * visited set, and then return the visited set
     */
    public Set<Town> findAllConnected(Town town) {
        return _findAllConnected(town, new HashSet<>());
    }

    /**
     * Helper for findAllConnected, traverses the graph keeping track
     * of all visited nodes using a set.
     *
     * @param town    Method with return the neighbors of this town
     * @param visited Used to keep track of the towns we've visited
     * @return The set of towns neighbouring towns
     */
    private Set<Town> _findAllConnected(Town town, Set<Town> visited) {
        visited.add(town); // Add town to a set, so we don't visit it again
        for (Town neighbor : town.getNeighbours()) { // Iterate through the towns neighbors
            if (!visited.contains(neighbor)) { // If the visited set doesn't contain the neighboring town
                _findAllConnected(neighbor, visited); // Continue traversing
            }
        }
        return visited; // Return the visited set
    }

    /**
     * COMPLETION
     * Print all the towns that are reachable through the network from
     * the town with the given name.
     * Note, do not include the town itself in the list.
     */
    public void printReachable(String name) {
        UI.clearText();
        Town town = busNetwork.get(name);
        if (town == null) {
            UI.println(name + " is not a recognised town!");
        } else {
            UI.println("From " + town.getName() + " you can get to:");
            if (findAllConnected(town) == null) return;
            for (Town neighbor : findAllConnected(town)) {
                // Ensure we don't print the town itself
                UI.println(neighbor.getName());
            }
        }
    }

    /**
     * COMPLETION
     * Print all the connected sets of towns in the busNetwork
     * Each line of the output should be the names of the towns in a connected set
     * Works through busNetwork, using findAllConnected on each town that hasn't
     * yet been printed out.
     */
    public void printConnectedGroups() {
        UI.clearText();
        UI.println("Groups of Connected Towns: \n================");


    }

    /**
     * Set up the GUI (buttons and mouse)
     */
    public void setupGUI() {
        UI.addButton("Load", () -> loadNetwork(UIFileChooser.open()));
        UI.addButton("Print Network", this::printNetwork);
        UI.addTextField("Reachable from", this::printReachable);
        UI.addButton("All Connected Groups", this::printConnectedGroups);
        UI.addButton("Clear", UI::clearText);
        UI.addButton("Quit", UI::quit);
        UI.setWindowSize(1100, 500);
        UI.setDivider(1.0);
        loadNetwork("data-small.txt");
    }
}
