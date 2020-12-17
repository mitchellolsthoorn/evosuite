package org.evosuite.testcase.mutators.grammar;

import com.google.gson.*;
import org.evosuite.Properties;
import org.evosuite.utils.Randomness;

import java.util.ArrayList;

/**
 * Mutator class for mutating JSON arrays.
 *
 * @author Mitchell Olsthoorn
 */
public class JsonArrayMutator extends JsonMutator {

    /**
     * Delete random element from the JSON array structure.
     *
     * @param array the {@link JsonArray} element to remove the element from.
     */
    public static void deleteRandomElement(JsonArray array) {
        if (array.size() < 1)
            return;

        // With GRAMMAR_JSON_NESTED chance remove an element from the second layer
        if (Randomness.nextDouble() <= Properties.GRAMMAR_JSON_NESTED) {
            JsonElement candidate = JsonArrayMutator.getRandomCandidate(array);
            if (candidate != null) {
                JsonArrayMutator.deleteRandomNestedElement(candidate);
                return;
            }
        }

        int index = Randomness.nextInt(array.size());
        array.remove(index);
    }

    /**
     * Change random element from the JSON array structure.
     *
     * @param array the {@link JsonArray} element to change.
     */
    public static void changeRandomElement(JsonArray array) {
        if (array.size() < 1)
            return;

        int index = Randomness.nextInt(array.size());
        JsonElement element = array.get(index);

        if (element.isJsonNull()) {
            array.set(index, JsonArrayMutator.getRandomPrimitiveElement());
            // TODO: Maybe add array or object elements
        } else if (element.isJsonPrimitive()) {
            JsonPrimitive primitive = element.getAsJsonPrimitive();
            array.set(index, JsonArrayMutator.changePrimitiveElement(primitive));
        } else {
            JsonObjectMutator.changeComplexElement(element);
        }
    }

    /**
     * Insert random element into the JSON array structure.
     *
     * @param array the {@link JsonArray} element to insert the element in.
     */
    public static void insertRandomElement(JsonArray array) {
        // With GRAMMAR_JSON_NESTED chance remove an element from the second layer
        if (Randomness.nextDouble() <= Properties.GRAMMAR_JSON_NESTED) {
            JsonElement candidate = JsonArrayMutator.getRandomCandidate(array);
            if (candidate != null) {
                JsonArrayMutator.insertRandomNestedElement(candidate);
                return;
            }
        }

        if (Randomness.nextDouble() <= Properties.GRAMMAR_JSON_NULL) {
            array.add(JsonNull.INSTANCE);
        } else if (Randomness.nextDouble() <= Properties.GRAMMAR_JSON_PRIMITIVE) {
            array.add(JsonArrayMutator.getRandomPrimitiveElement());
        } else if (Randomness.nextDouble() <= Properties.GRAMMAR_JSON_ARRAY) {
            array.add(new JsonArray());
        } else {
            array.add(new JsonObject());
        }
    }

    /**
     * Select a random candidate from the {@link JsonArray} that is a {@link JsonArray}
     * or a {@link JsonObject}.
     *
     * @param array the {@link JsonArray} element to select the candidate from.
     * @return a {@link JsonArray} or {@link JsonObject} element from the {@link JsonArray}.
     */
    protected static JsonElement getRandomCandidate(JsonArray array) {
        ArrayList<JsonElement> candidates = new ArrayList<>();

        if (array != null) {
            for (JsonElement element : array) {
                if (element.isJsonArray() || element.isJsonObject()) {
                    candidates.add(element);
                }
            }
            return Randomness.choice(candidates);
        }
        return null;
    }
}
