package org.evosuite.testcase.mutators.grammar;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import org.evosuite.Properties;
import org.evosuite.seeding.ConstantPool;
import org.evosuite.seeding.ConstantPoolManager;
import org.evosuite.testcase.mutators.StringMutator;
import org.evosuite.utils.Randomness;

/**
 * Abstract mutator class for mutating a JSON structure.
 *
 * @author Mitchell Olsthoorn
 */
abstract public class JsonMutator {

    /**
     * Delete random element from the candidate structure.
     *
     * @param candidate the {@link JsonArray} or {@link JsonObject} element to remove the element from.
     */
    protected static void deleteRandomNestedElement(JsonElement candidate) {
        if (candidate != null) {
            if (candidate.isJsonArray()) { // JSON array
                JsonArray array2 = candidate.getAsJsonArray();
                if (array2.size() > 0) {
                    int array2_index = Randomness.nextInt(array2.size());
                    array2.remove(array2_index);
                }
            } else if (candidate.isJsonObject()) { // JSON object
                JsonObject object2 = candidate.getAsJsonObject();
                if (object2.size() > 0) {
                    String object2_property = Randomness.choice(object2.keySet());
                    object2.remove(object2_property);
                }
            }
        }
    }

    /**
     * Change primitive element randomly.
     *
     * @param primitive the {@link JsonPrimitive} object to change.
     * @return the changed {@link JsonPrimitive} object.
     */
    protected static JsonPrimitive changePrimitiveElement(JsonPrimitive primitive) {
        if (primitive.isBoolean()) { // Boolean
            return new JsonPrimitive(!primitive.getAsBoolean());
        }

        if (primitive.isNumber()) { // Number
            double P = Randomness.nextDouble();

            // TODO: possible to convert to string and parse it
            // TODO: Put these in their own mutators
            if (P <= 1d / 4d) { // Integer
                int number = primitive.getAsInt();
                int delta = (int) Math.floor(Randomness.nextGaussian() * Properties.MAX_DELTA);
                return new JsonPrimitive(number + delta);
            } else if (P <= 2d / 4d) { // Long
                long number = primitive.getAsLong();
                long delta = (long) Math.floor(Randomness.nextGaussian() * Properties.MAX_DELTA);
                return new JsonPrimitive(number + delta);
            } else if (P <= 3d / 4d) { // Double
                double number = primitive.getAsDouble();
                double delta = Math.floor(Randomness.nextGaussian() * Properties.MAX_DELTA);
                return new JsonPrimitive(number + delta);
            } else { // Float
                float number = primitive.getAsFloat();
                float delta = (float) Math.floor(Randomness.nextGaussian() * Properties.MAX_DELTA);
                return new JsonPrimitive(number + delta);
            }
        }

        if (primitive.isString()) { // String
            String s = primitive.getAsString();
            s = StringMutator.mutateString(s);
            return new JsonPrimitive(s);
        }

        return primitive;
    }

    /**
     * Change complex element randomly.
     *
     * @param element the {@link JsonArray} or {@link JsonObject} element to change.
     */
    protected static void changeComplexElement(JsonElement element) {
        if (element.isJsonArray()) { // JSON array
            JsonArray array = element.getAsJsonArray();

            if (array.size() > 0) {
                int index = Randomness.nextInt(array.size());
                JsonElement element2 = array.get(index);
                if (element2.isJsonPrimitive()) {
                    JsonPrimitive primitive = element2.getAsJsonPrimitive();
                    array.set(index, JsonMutator.changePrimitiveElement(primitive));
                }
            }
        } else if (element.isJsonObject()) { // JSON object
            JsonObject object = element.getAsJsonObject();

            if (object.size() > 0) {
                String property = Randomness.choice(object.keySet());
                JsonElement element2 = object.get(property);
                if (element2.isJsonPrimitive()) {
                    JsonPrimitive primitive = element2.getAsJsonPrimitive();
                    object.add(property, JsonMutator.changePrimitiveElement(primitive));
                }
            }
        }
    }

    /**
     * Insert nested random element into the JSON structure.
     *
     * @param element the {@link JsonArray} or {@link JsonObject} element to insert the element in.
     */
    protected static void insertRandomNestedElement(JsonElement element) {
        ConstantPool constantPool = ConstantPoolManager.getInstance().getConstantPool();

        // TODO: Maybe add JsonNull element
        if (element != null) {
            if (element.isJsonArray()) {
                JsonArray array = element.getAsJsonArray();
                array.add(JsonMutator.getRandomPrimitiveElement());
            } else if (element.isJsonObject()) {
                JsonObject object = element.getAsJsonObject();
                object.add(constantPool.getRandomString(), JsonMutator.getRandomPrimitiveElement());
            }
        }
    }

    /**
     * Get a new random primitive element.
     *
     * @return a {@link JsonPrimitive} element.
     */
    protected static JsonPrimitive getRandomPrimitiveElement() {
        ConstantPool constantPool = ConstantPoolManager.getInstance().getConstantPool();

        double P = Randomness.nextDouble();
        if (P <= 1d / 6d) {
            return new JsonPrimitive(Randomness.nextBoolean());
        } else if (P <= 2d / 6d) {
            return new JsonPrimitive(constantPool.getRandomInt());
        } else if (P <= 3d / 6d) {
            return new JsonPrimitive(constantPool.getRandomLong());
        } else if (P <= 4d / 6d) {
            return new JsonPrimitive(constantPool.getRandomDouble());
        } else if (P <= 5d / 6d) {
            return new JsonPrimitive(constantPool.getRandomFloat());
        } else {
            return new JsonPrimitive(constantPool.getRandomString());
        }
    }
}
