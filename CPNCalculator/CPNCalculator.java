// This program is copyright VUW.
// You are granted permission to use it to construct your answer to a COMP103 assignment.
// You may not distribute it in any other way without permission.

/* Code for COMP103 - 2023T2, Assignment 5
 * Name: Shemaiah Rangitaawa
 * Username: rangitshem
 * ID: 300601546
 */

import ecs100.UI;

import java.util.Arrays;
import java.util.List;
import java.util.Scanner;

/**
 * Calculator for Cambridge-Polish Notation expressions
 * (see the description in the assignment page)
 * User can type in an expression (in CPN) and the program
 * will compute and print out the value of the expression.
 * The template provides the method to read an expression and turn it into a tree.
 * You have to write the method to evaluate an expression tree.
 * and also check and report certain kinds of invalid expressions
 */

public class CPNCalculator {
    /*
     * These fields hold all the available operations and functions.
     * Used in the print expression method to determine whether expression is operation of function
     */
    private static final List<String> operators = Arrays.asList("+", "-", "*", "/", "^", "dist", "sin", "cos", "tan", "log", "ln", "sqrt");
    private static final List<String> functions = Arrays.asList("avg", "dist", "ln", "sin", "cos", "sqrt", "tan", "log");

    /**
     * Setup GUI then run the calculator
     */
    public static void main(String[] args) {
        CPNCalculator calc = new CPNCalculator();
        calc.setupGUI();
        calc.runCalculator();
    }

    /**
     * Prints the expression represented by the given Generic Tree (GT) node,
     * formatting it according to operator precedence and function notation.
     *
     * @param expression The root node of the expression tree to be printed
     */
    public static void printExpr(GTNode<ExpElem> expression) {
        UI.print(" -> ");
        printExpr(expression, "");
        UI.println();
    }

    /**
     * Recursively prints the expression represented by the given GT node, considering operator precedence and function notation.
     *
     * @param node             The current node in the expression tree
     * @param previousOperator The operator of the parent node (empty for the root node)
     */
    private static void printExpr(GTNode<ExpElem> node, String previousOperator) {
        if (node.numberOfChildren() == 0) UI.print(node.getItem()); // Base case: If node is a number, print it.
        // Determine if brackets or function notation should be used.
        boolean needsBracket = shouldPrintBrackets(node, previousOperator);
        boolean isFunction = isFunction(node);
        if (isFunction) UI.print(node.getItem().operator);
        if (needsBracket || isFunction) UI.print("(");
        for (int i = 0; i < node.numberOfChildren(); ++i) {
            printExpr(node.getChild(i), node.getItem().operator);
            if (i < node.numberOfChildren() - 1) {
                // Print a comma between function parameters if there are more than one,
                // otherwise, print the operator between sub-equations/values.
                if (isFunction) UI.print(",");
                else UI.print(node.getItem().operator);
            }
        }
        if (needsBracket || isFunction) UI.print(")");
    }

    /**
     * Check if brackets should be printed based on the order of operations and function rules.
     *
     * @param node             The expression
     * @param previousOperator The operator of the parent node (empty for the root node)
     * @return Whether brackets should be printed around the expression
     */
    private static boolean shouldPrintBrackets(GTNode<ExpElem> node, String previousOperator) {
        return (operators.indexOf(node.getItem().operator) > operators.indexOf(previousOperator) && !previousOperator.isEmpty() && !functions.contains(previousOperator));
    }

    /**
     * Check if the current node represents a function.
     *
     * @param node The current node
     * @return Whether the expression is a function
     */
    private static boolean isFunction(GTNode<ExpElem> node) {
        return functions.contains(node.getItem().operator);
    }

    /**
     * Set up the gui
     */
    public void setupGUI() {
        UI.addButton("Clear", UI::clearText);
        UI.addButton("Quit", UI::quit);
        UI.setDivider(1.0);
    }

    /**
     * Run the calculator:
     * loop forever:  (a REPL - Read Eval Print Loop)
     * - read an expression,
     * - evaluate the expression,
     * - print out the value
     * Invalid expressions could cause errors when reading or evaluating
     * The try-catch prevents these errors from crashing the program -
     * the error is caught, and a message printed, then the loop continues.
     */
    public void runCalculator() {
        UI.println("Enter expressions in pre-order format with spaces");
        UI.println("eg   ( * ( + 4 5 8 3 -10 ) 7 ( / 6 4 ) 18 )");
        while (true) {
            UI.println();
            try {
                GTNode<ExpElem> expr = readExpr();
                double value = evaluate(expr);
                UI.println(" -> " + value);
                if (!Double.isNaN(value)) printExpr(expr); // Print
            } catch (Exception e) {
                UI.println("Something went wrong!");
            }
        }
    }

    /**
     * Evaluate an expression and return the value
     * Returns Double.NaN if the expression is invalid in some way.
     * If the node is a number
     * => just return the value of the number
     * or it is a named constant
     * => return the appropriate value
     * or it is an operator node with children
     * => evaluate all the children and then apply the operator.
     */
    public double evaluate(GTNode<ExpElem> node) {
        // Check for null entries
        if (node == null || node.getItem() == null) return Double.NaN;

        // Get operator from expression
        String operator = node.getItem().operator;
        if (operator.equalsIgnoreCase("PI")) return Math.PI;
        else if (operator.equalsIgnoreCase("E")) return Math.E;

        // Evaluate the other operators/functions
        return evaluate(operator, node);
    }

    /**
     * Evaluate an expression and return the value
     *
     * @param operator An operator from the expression
     * @param node     The expression
     * @return The result if valid, otherwise Double.NaN
     */
    private double evaluate(String operator, GTNode<ExpElem> node) {
        // If expression is a single number, return it
        if (node.numberOfChildren() == 0 || operator.equals("#")) return node.getItem().value;

        // Switch case for various operators
        switch (operator) {
            case "+" -> {
                double result = 0;
                for (GTNode<ExpElem> child : node) {
                    result += evaluate(child);
                }
                return result;
            }
            case "-" -> {
                double result = evaluate(node.getChild(0));
                for (int i = 1; i < node.numberOfChildren(); i++) {
                    result -= evaluate(node.getChild(i));
                }
                return result;
            }
            case "*" -> {
                double result = 1;
                for (GTNode<ExpElem> child : node) {
                    result *= evaluate(child);
                }
                return result;
            }
            case "/" -> {
                double result = evaluate(node.getChild(0));
                for (int i = 1; i < node.numberOfChildren(); i++) {
                    result /= evaluate(node.getChild(i));
                }
                return result;
            }
            case "^" -> {
                if (node.numberOfChildren() != 2) {
                    UI.println("Invalid operands for power");
                    return Double.NaN;
                }
                double number = evaluate(node.getChild(0));
                return Math.pow(number, evaluate(node.getChild(1)));
            }
            case "sqrt" -> {
                if (node.numberOfChildren() != 1) {
                    UI.println("Invalid operands for square root");
                    return Double.NaN;
                }
                return Math.sqrt(evaluate(node.getChild(0)));
            }
            case "log" -> {
                if (node.numberOfChildren() == 2) {
                    // Change of base property of logarithms
                    return Math.log10(evaluate(node.getChild(0))) / Math.log10(evaluate(node.getChild(1)));
                } else if (node.numberOfChildren() == 1) {
                    return Math.log10(evaluate(node.getChild(0)));
                } else {
                    UI.println("Invalid operands for log");
                    return Double.NaN;
                }
            }
            case "ln" -> {
                if (node.numberOfChildren() != 1) {
                    UI.println("Invalid operands for natural log");
                    return Double.NaN;
                }
                return Math.log(evaluate(node.getChild(0)));
            }
            case "sin" -> {
                if (node.numberOfChildren() != 1) {
                    UI.println("Invalid operands sine function");
                    return Double.NaN;
                }
                return Math.sin(evaluate(node.getChild(0)));
            }
            case "cos" -> {
                if (node.numberOfChildren() != 1) {
                    UI.println("Invalid operand for cosine function");
                    return Double.NaN;
                }
                return Math.cos(evaluate(node.getChild(0)));
            }
            case "tan" -> {
                if (node.numberOfChildren() != 1) {
                    UI.println("Invalid operand for tangent function");
                    return Double.NaN;
                }
                return Math.tan(evaluate(node.getChild(0)));
            }
            case "dist" -> {
                return distance(node); // To keep code clean
            }
            case "avg" -> {
                if (node.numberOfChildren() < 1) {
                    UI.println("Invalid operands for average function");
                    return Double.NaN;
                }
                double average = 0;
                for (int i = 0; i < node.numberOfChildren(); i++) average += evaluate(node.getChild(i));
                return average;
            }
            default -> {
                UI.println(node.getItem() + " is not a valid operator.");
                return Double.NaN;
            }
        }
    }

    /**
     * Calculates distance. Helper method for operationHandler
     *
     * @param node The expression
     * @return Distance between points
     */
    private double distance(GTNode<ExpElem> node) {
        if (node.numberOfChildren() == 4) {
            // Get the points
            double x1 = evaluate(node.getChild(0));
            double y1 = evaluate(node.getChild(1));
            double x2 = evaluate(node.getChild(2));
            double y2 = evaluate(node.getChild(3));

            // Ensure numbers are positive
            double x = Math.abs(x1 - x2);
            double y = Math.abs(y1 - y2);

            // Return euclidean distance
            return Math.sqrt(Math.pow(x, 2) + Math.pow(y, 2));
        } else if (node.numberOfChildren() == 6) {
            double x1 = evaluate(node.getChild(0));
            double y1 = evaluate(node.getChild(1));
            double z1 = evaluate(node.getChild(2));
            double x2 = evaluate(node.getChild(3));
            double y2 = evaluate(node.getChild(4));
            double z2 = evaluate(node.getChild(5));
            double x = Math.abs(x1 - x2);
            double y = Math.abs(y1 - y2);
            double z = Math.abs(z1 - z2);
            return Math.sqrt(Math.pow(x, 2) + Math.pow(y, 2) + Math.pow(z, 2));
        } else {
            UI.println("Invalid operands for distance function");
            return Double.NaN;
        }
    }

    /**
     * Reads an expression from the user and constructs the tree
     */
    public GTNode<ExpElem> readExpr() {
        String expr = UI.askString("expr:");
        return readExpr(new Scanner(expr));   // the recursive reading method
    }

    /**
     * Recursive helper method.
     * Uses the hasNext(String pattern) method for the Scanner to peek at next token
     */
    public GTNode<ExpElem> readExpr(Scanner sc) {
        if (sc.hasNextDouble()) {
            // The next token is a number, so return a new node with the number as data
            return new GTNode<>(new ExpElem(sc.nextDouble()));
        } else if (sc.hasNext("\\(")) {
            // The next token is an opening bracket
            sc.next(); // Read and discard the opening '('
            try {
                if (sc.hasNext("\\)")) {
                    UI.println("Missing expression inside brackets");
                    return null;
                }
                // Read the operator
                ExpElem operatorElem = new ExpElem(sc.next());
                // Create a new node with the operator as data
                GTNode<ExpElem> node = new GTNode<>(operatorElem);
                while (!sc.hasNext("\\)")) {
                    // Loop until the closing ')'
                    GTNode<ExpElem> child = readExpr(sc);
                    // Read each operand/argument and add it as a child of the current node
                    node.addChild(child);
                }
                sc.next(); // Read and discard the closing ')'
                return node;
            } catch (Exception e) {
                UI.println("Missing closing bracket");
            }
            return null;
        } else {
            if (sc.hasNext("\\)")) {
                // If it's a random closing bracket without an opening bracket, report an error
                UI.println("Unexpected closing bracket");
                return null;
            }
            // Create a token with the name as the "operator"
            return new GTNode<>(new ExpElem(sc.next()));
        }
    }
}
