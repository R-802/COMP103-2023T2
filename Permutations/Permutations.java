// This program is copyright VUW.
// You are granted permission to use it to construct your answer to a COMP103 assignment.
// You may not distribute it in any other way without permission.

/* Code for COMP103 - 2023T2, Assignment 4
 * Name:
 * Username:
 * ID:
 */

import ecs100.UI;

import java.util.*;

/**
 * Compute all permutations of a list of Strings
 * You only have to write one method - the extendPermutations(...) method
 * which does the recursive search.
 */
public class Permutations {

    // Counter for the number of complete permutations found
    private long counter = 0;

    // Main
    public static void main(String[] arguments) {
        Permutations p = new Permutations();
        p.setupGUI();
    }

    //===================================================
    // User Interface code

    /**
     * Constructs a list of all permutations of the given items
     * by calling a recursive method, passing in a set of the items to permute
     * and an empty list to build up.
     * Prints the total number of permutations in the message window (with
     * UI.printMessage(...);
     */
    public List<List<String>> findPermutations(Set<String> items) {
        Set<String> copyOfItems = new HashSet<>(items);   // a copy of the set of items that can be modified
        List<List<String>> ans = new ArrayList<>(); // where we will collect the answer
        counter = 0;
        Stack<String> permutationSoFar = new Stack<>(); // where we are building up a permutation
        extendPermutation(copyOfItems, permutationSoFar, ans);
        return ans;
    }

    /**
     * Recursive method to build all permutations possible by adding the
     * remaining items on to the end of the permutation built up so far
     * If there are no remaining items, then permutationSoFar is complete,
     * => add a copy of the permutation to allPermutations.
     * Otherwise,
     * for each of the remaining items,
     * extend the permutationSoFar with the item, and do a recursive call to extend it more:
     * - remove the item from remaining items and
     * - push it onto the permutation so far,
     * - do the recursive call,
     * - pop the item from the permutation so far and
     * - put it back into the remaining items.
     * <p>
     * So that you don't run out of memory, only add the first 10000 permutations to the allPermutations.
     */
    public void extendPermutation(Set<String> remainingItems, Stack<String> permutationSoFar, List<List<String>> allPermutations) {
        if (remainingItems.isEmpty()) { // Base case
            if (allPermutations.size() <= 10000) // Only add the first 10000 permutations to the allPermutations list
            {   // Create a copy of the permutation so far
                List<String> permutationCopy = new ArrayList<>(permutationSoFar);
                allPermutations.add(permutationCopy); // Add a copy of the permutation to allPermutations
            }
            counter++;
            return;
        }
        List<String> remainingWords = new ArrayList<>(remainingItems);
        for (String word : remainingWords) { // Recursive case
            remainingItems.remove(word); // Remove the item from remaining items
            permutationSoFar.push(word); // Push it onto the permutation so far
            extendPermutation(remainingItems, permutationSoFar, allPermutations);
            remainingItems.add(permutationSoFar.pop());
        }
    }

    /**
     * Setup GUI
     * Buttons to run permutations on either letters or words
     */
    public void setupGUI() {
        UI.addButton("A B C D E", () -> printAll(findPermutations(Set.of("A", "B", "C", "D", "E"))));
        UI.addTextField("Letters", (String v) -> printAll(findPermutations(makeSetOfLetters(v))));
        UI.addTextField("Words", (String v) -> printAll(findPermutations(makeSetOfWords(v))));
        UI.addButton("Quit", UI::quit);
        UI.setDivider(1.0);
    }

    public void printAll(List<List<String>> permutations) {
        UI.clearText();
        for (List<String> permutation : permutations) {
            for (String str : permutation) {
                UI.print(str + " ");
            }
            UI.println();
        }
        UI.println("----------------------");
        UI.printf("%d items:\n", permutations.get(0).size());
        UI.printf("%,d permutations:\n", counter);
        UI.println("----------------------");
    }

    /**
     * Makes a set of strings, one string for each character in the argument
     */
    public Set<String> makeSetOfLetters(String str) {
        Set<String> ans = new HashSet<>();
        for (int i = 0; i < str.length(); i++) {
            if (str.charAt(i) != ' ') {
                ans.add("" + str.charAt(i));
            }
        }
        return Collections.unmodifiableSet(ans);
    }

    /**
     * Makes a set of strings, one string for each word in the argument
     */
    public Set<String> makeSetOfWords(String str) {
        Set<String> ans = new HashSet<>();
        Collections.addAll(ans, str.split(" "));
        return Collections.unmodifiableSet(ans);
    }

}
