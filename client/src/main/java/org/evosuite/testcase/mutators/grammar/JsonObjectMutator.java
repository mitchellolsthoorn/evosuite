package org.evosuite.testcase.mutators.grammar;

import com.google.gson.*;
import org.evosuite.Properties;
import org.evosuite.seeding.ConstantPool;
import org.evosuite.seeding.ConstantPoolManager;
import org.evosuite.testcase.mutators.StringMutator;
import org.evosuite.utils.Randomness;

import java.util.ArrayList;

/**
 * Mutator class for mutating JSON objects.
 *
 * @author Mitchell Olsthoorn
 */
public class JsonObjectMutator extends JsonMutator {

    /**
     * Delete random element from the JSON object structure.
     *
     * @param object the {@link JsonObject} element to remove the element from.
     */
    public static void deleteRandomElement(JsonObject object) {
        if (object.size() < 1)
            return;

        // With GRAMMAR_JSON_NESTED chance remove an element from the second layer
        if (Randomness.nextDouble() <= Properties.GRAMMAR_JSON_NESTED) {
            JsonElement candidate = JsonObjectMutator.getRandomCandidate(object);
            if (candidate != null) {
                JsonObjectMutator.deleteRandomNestedElement(candidate);
                return;
            }
        }

        String property = Randomness.choice(object.keySet());
        object.remove(property);
    }

    /**
     * Change random element from the JSON object structure.
     *
     * @param object the {@link JsonObject} element to change.
     */
    public static void changeRandomElement(JsonObject object) {
        if (object.size() < 1)
            return;

        String property = Randomness.choice(object.keySet());
        JsonElement element = object.get(property);

        double P = Randomness.nextDouble();
        if (P <= Properties.GRAMMAR_JSON_PROPERTY) { // Change property name
            object.remove(property);
            String newProperty = StringMutator.mutateString(property);
            object.add(newProperty, element);
        } else { // Change property value
            if (element.isJsonNull()) {
                object.add(property, JsonObjectMutator.getRandomPrimitiveElement());
                // TODO: Maybe add array or object elements
            } else if (element.isJsonPrimitive()) {
                JsonPrimitive primitive = element.getAsJsonPrimitive();
                object.add(property, JsonObjectMutator.changePrimitiveElement(primitive));
            } else {
                changeComplexElement(element);
            }
        }
    }

    /**
     * Insert random element into the JSON object structure.
     *
     * @param object the {@link JsonObject} element to insert the element in.
     */
    public static void insertRandomElement(JsonObject object) {
        ConstantPool constantPool = ConstantPoolManager.getInstance().getConstantPool();

        // With GRAMMAR_JSON_NESTED chance remove an element from the second layer
        if (Randomness.nextDouble() <= Properties.GRAMMAR_JSON_NESTED) {
            JsonElement candidate = JsonObjectMutator.getRandomCandidate(object);
            if (candidate != null) {
                JsonObjectMutator.insertRandomNestedElement(candidate);
                return;
            }
        }

        if (Randomness.nextDouble() <= Properties.GRAMMAR_JSON_NULL) {
            object.add(constantPool.getRandomString(), JsonNull.INSTANCE);
        } else if (Randomness.nextDouble() <= Properties.GRAMMAR_JSON_PRIMITIVE) {
            object.add(constantPool.getRandomString(), JsonObjectMutator.getRandomPrimitiveElement());
        } else if (Randomness.nextDouble() <= Properties.GRAMMAR_JSON_ARRAY) {
            object.add(constantPool.getRandomString(), new JsonArray());
        } else {
            object.add(constantPool.getRandomString(), new JsonObject());
        }
    }

    /**
     * Select a random candidate from the {@link JsonObject} that is a {@link JsonArray}
     * or a {@link JsonObject}.
     *
     * @param object the {@link JsonObject} element to select the candidate from.
     * @return a {@link JsonArray} or {@link JsonObject} element from the {@link JsonObject}.
     */
    protected static JsonElement getRandomCandidate(JsonObject object) {
        ArrayList<JsonElement> candidates = new ArrayList<>();

        if (object != null) {
            for (String property : object.keySet()) {
                JsonElement element = object.get(property);
                if (element.isJsonArray() || element.isJsonObject()) {
                    candidates.add(element);
                }
            }
            return Randomness.choice(candidates);
        }
        return null;
    }
}
