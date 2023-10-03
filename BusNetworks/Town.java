// This program is copyright VUW.
// You are granted permission to use it to construct your answer to a COMP103 assignment.
// You may not distribute it in any other way without permission.

/* Code for COMP103 - 2023T2, Assignment 6
 * Name:
 * Username:
 * ID:
 */

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

public class Town {

    private final String name;
    private final Set<Town> neighbours = new HashSet<Town>();

    public Town(String name) {
        this.name = name;
    }

    public String getName() {
        return this.name;
    }

    public Set<Town> getNeighbours() {
        return Collections.unmodifiableSet(neighbours);
    }

    public void addNeighbour(Town node) {
        neighbours.add(node);
    }

    public String toString() {
        return name + " (" + neighbours.size() + " connections)";
    }
}
