// This program is copyright VUW.
// You are granted permission to use it to construct your answer to a COMP103 assignment.
// You may not distribute it in any other way without permission.

/* Code for COMP103 - 2023T2, Assignment 1
 * Name:
 * Username:
 * ID:
 */

import ecs100.*;

import javax.imageio.ImageIO;
import java.awt.Color;
import java.awt.image.BufferedImage;
import java.util.*;
import java.io.*;
import java.nio.file.*;

/**
 * Shred stores information about a shred.
 * All shreds are 40x40 png images.
 */

public class Shred {

    // Fields to store
    // the name of the image
    // the id of the image
    public static final double SIZE = 40;
    private final String filename;
    private final int id;   // ID of the shred

    // Constructor
    /**
     * Construct a new Shred object.
     * Parameters are the name of the directory and the id of the image
     */
    public Shred(Path dir, int id) {
        filename = dir.resolve(id + ".png").toString();
        this.id = id;
    }

    /**
     * Draw the shred (no border) at the specified coordinates
     */
    public void draw(double left, double top) {
        UI.drawImage(filename, left, top, SIZE, SIZE);
    }

    /**
     * Draw the shred with a border at the specified coordinates
     */
    public void drawWithBorder(double left, double top) {
        UI.drawImage(filename, left, top, SIZE, SIZE);
        UI.drawRect(left, top, SIZE, SIZE);
    }

    /**
     * Return a string representation of a Shred
     */
    public String toString() {
        return "ID:" + id;
    }

    //=============================================================================

    /**
     * Enumeration that defines the four sides of the shred
     */
    public enum Side {
        LEFT,
        RIGHT,
        TOP,
        BOTTOM
    }

    /**
     * Get the colors of the specified side of the shred.
     *
     * @param side
     * @return colors
     */
    public Color[] getSide(Side side) {
        try {
            BufferedImage img = ImageIO.read(new File(filename));
            int width = img.getWidth();
            int height = img.getHeight();
            Color[] colors = new Color[height];

            switch (side) {
                case LEFT:
                    for (int y = 0; y < height; y++) {
                        colors[y] = new Color(img.getRGB(0, y));
                    }
                    break;

                case RIGHT:
                    for (int y = 0; y < height; y++) {
                        colors[y] = new Color(img.getRGB(width - 1, y));
                    }
                    break;

                case TOP:
                    for (int x = 0; x < width; x++) {
                        colors[x] = new Color(img.getRGB(x, 0));
                    }
                    break;

                case BOTTOM:
                    for (int x = 0; x < width; x++) {
                        colors[x] = new Color(img.getRGB(x, height - 1));
                    }
                    break;
            }

            return colors;
        } catch (IOException e) {
            UI.println("Error reading image: " + e.getMessage());
            return new Color[0];
        }
    }

    /**
     * Get the complementary side of the shred (the opposite side).
     * For example, if the provided side is LEFT, the complementary side will be RIGHT.
     * Parameters:
     * - side: The side for which to get the complementary side.
     * Returns:
     * - An array of Color representing the colors of the pixels on the complementary side.
     */
    public Color[] getComplementarySide() {
        Side complementarySide = Side.RIGHT; // Change this based on the layout of shreds
        return getSide(complementarySide);
    }

    public void highLight(double left, double top) {
        UI.setColor(Color.GREEN);
        UI.drawImage(filename, left, top, SIZE, SIZE);
        UI.drawRect(left, top, SIZE, SIZE);
    }
}
