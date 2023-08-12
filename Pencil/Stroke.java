/* Code for COMP103 - 2023T2, Assignment 2
 * Name: Shemaiah Rangitaawa
 * Username:rangitshem
 * ID: 300601645
 */

import ecs100.UI;

import java.awt.*;
import java.util.*;


/**
 * A stroke is defined as an array of points
 */
public class Stroke {
    private final ArrayList<Point> points;
    private Color color;
    private Float width;

    public Stroke() {
        points = new ArrayList<>();
    }

    public void addPoint(double x, double y) {
        points.add(new Point((int) x, (int) y));
    }

    public void draw() {
        for (int i = 1; i < points.size(); i++) {
            Point p1 = points.get(i - 1);
            Point p2 = points.get(i);
            UI.setColor(color);
            UI.drawLine(p1.x, p1.y, p2.x, p2.y);
        }
    }
}

