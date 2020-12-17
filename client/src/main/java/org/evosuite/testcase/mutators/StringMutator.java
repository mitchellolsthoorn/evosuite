package org.evosuite.testcase.mutators;

import org.evosuite.Properties;
import org.evosuite.utils.Randomness;

/**
 * Mutator class for mutating a string.
 *
 * @author Mitchell Olsthoorn
 */
public class StringMutator {

    public static final double P = 1d / 3d;

    /**
     * Delete a character from a string.
     *
     * @param s the string to delete the chararcter from.
     * @param pos the position of the character to delete.
     * @return the new string with the character removed.
     */
    public static String deleteCharAt(String s, int pos) {
        return s.substring(0, pos) + s.substring(pos + 1);
    }

    /**
     * Replace a character in a string.
     *
     * @param s the string to change the character in.
     * @param pos the position of the character to change.
     * @param c the character to remove the old character with.
     * @return the new string with the character replaced.
     */
    public static String replaceCharAt(String s, int pos, char c) {
        return s.substring(0, pos) + c + s.substring(pos + 1);
    }

    /**
     * Insert a character in a string.
     *
     * @param s the string to insert the character in.
     * @param pos the position in the string to insert the character.
     * @param c the character to insert.
     * @return the new string with the inserted character.
     */
    public static String insertCharAt(String s, int pos, char c) {
        return s.substring(0, pos) + c + s.substring(pos);
    }

    /**
     * Insert characters into a string up to a certain predefined length with a certain probability.
     *
     * @param s the string to insert the characters in.
     * @param pos the position in which to insert the characters.
     * @return the new string with the inserted characters.
     */
    public static String stringInsert(String s, int pos) {
        final double ALPHA = 0.5;
        int count = 1;

        while (Randomness.nextDouble() <= Math.pow(ALPHA, count) && s.length() < Properties.STRING_LENGTH) {
            count++;
            s = insertCharAt(s, pos, Randomness.nextChar());
        }
        return s;
    }

    /**
     * Mutate a string.
     *
     * @param s the string to be mutated.
     * @return the new string with the mutations applied.
     */
    public static String mutateString(String s) {
        // Delete
        if (Randomness.nextDouble() <= StringMutator.P) {
            int i = Randomness.nextInt(s.length());
            s = deleteCharAt(s, i - 1);
        }

        // Change
        if (Randomness.nextDouble() <= StringMutator.P) {
            int i = Randomness.nextInt(s.length());
            s = replaceCharAt(s, i, Randomness.nextChar());
        }

        // Insert
        if (Randomness.nextDouble() <= StringMutator.P) {
            int pos = 0;
            if (s.length() > 0)
                pos = Randomness.nextInt(s.length());
            s = stringInsert(s, pos);
        }

        return s;
    }
}
