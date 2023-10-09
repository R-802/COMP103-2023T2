// This program is copyright VUW.
// You are granted permission to use it to construct your answer to a COMP103 assignment.
// You may not distribute it in any other way without permission.

/* Code for COMP103 - 2023T2, Assignment 6
 * Name: Shemaiah Rangitaawa
 * Username: rangitshem
 * ID: 300601546
 */

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

public class Town {
    private final String name;
    private final Set<Town> neighbours = new HashSet<>();
    private double x, y;

    public Town(String name) {
        this.name = name;
    }

    public Town(String name, double x, double y) {
        this.name = name;
        this.x = x;
        this.y = y;
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

    public double getX() {
        return x;
    }

    public double getY() {
        return y;
    }

    public void setX(double x) {
        this.x = x;
    }

    public void setY(double y) {
        this.y = y;
    }
}
