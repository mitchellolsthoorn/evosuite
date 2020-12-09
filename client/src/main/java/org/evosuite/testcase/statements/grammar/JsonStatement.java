package org.evosuite.testcase.statements.grammar;

import com.google.gson.*;
import org.evosuite.Properties;
import org.evosuite.seeding.ConstantPool;
import org.evosuite.seeding.ConstantPoolManager;
import org.evosuite.testcase.TestCase;
import org.evosuite.testcase.TestFactory;
import org.evosuite.testcase.execution.Scope;
import org.evosuite.testcase.statements.Statement;
import org.evosuite.testcase.statements.StringPrimitiveStatement;
import org.evosuite.utils.Randomness;

import java.io.PrintStream;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;

public class JsonStatement extends StringPrimitiveStatement {

    private static final long serialVersionUID = 278735526699835887L;

    private transient Gson gson;
    private transient JsonElement jsonElement;

    static final String EMPTY_JSON_STRING = "{}";

    /**
     * <p>
     * Constructor for JSONStatement.
     * </p>
     *
     * @param tc    a {@link TestCase} object.
     * @param value a {@link String} object.
     */
    public JsonStatement(TestCase tc, String value) {
        super(tc, value);

        this.gson = new Gson();
        this.jsonElement = gson.fromJson(value, JsonElement.class);
    }

    /**
     * <p>
     * Constructor for JSONStatement.
     * </p>
     *
     * @param tc    a {@link TestCase} object.
     */
    public JsonStatement(TestCase tc) {
        super(tc, JsonStatement.EMPTY_JSON_STRING);

        this.gson = new Gson();
        this.jsonElement = new JsonObject();
    }

    /* (non-Javadoc)
     * @see org.evosuite.testcase.PrimitiveStatement#getValue(java.lang.Object)
     */
    /** {@inheritDoc} */
    @Override
    public String getValue() {
        String val = gson.toJson(this.jsonElement);
        this.value = val; // TODO: not needed
        return val;
    }

    /* (non-Javadoc)
     * @see org.evosuite.testcase.PrimitiveStatement#setValue(java.lang.Object)
     */
    /** {@inheritDoc} */
    @Override
    public void setValue(String val) {
        this.value = val; // TODO: not needed
        this.jsonElement = gson.fromJson(value, JsonElement.class);
    }

    /* (non-Javadoc)
     * @see org.evosuite.testcase.PrimitiveStatement#zero(java.lang.Object)
     */
    /** {@inheritDoc} */
    @Override
    public void zero() {
        this.value = JsonStatement.EMPTY_JSON_STRING; // TODO: not needed
        this.jsonElement = new JsonObject();
    }

    /* (non-Javadoc)
     * @see org.evosuite.testcase.StatementInterface#mutate(org.evosuite.testcase.TestCase, org.evosuite.testcase.TestFactory)
     */
    /** {@inheritDoc} */
    @Override
    public boolean mutate(TestCase test, TestFactory factory) {
        JsonElement oldVal = this.jsonElement;

        int current = 0;

        while (this.jsonElement.equals(oldVal) && current < Properties.GRAMMAR_JSON_MUTATION_RETRY_LIMIT) {
            if (Randomness.nextDouble() <= Properties.RANDOM_PERTURBATION) {
                this.randomize();
            } else {
                this.delta();
            }
            current++;
        }

        this.value = gson.toJson(this.jsonElement);

        return true;
    }

    /* (non-Javadoc)
     * @see org.evosuite.testcase.PrimitiveStatement#randomize()
     */
    /** {@inheritDoc} */
    @Override
    public void randomize() {
        if (Randomness.nextDouble() <= Properties.GRAMMAR_JSON_ARRAY) { // Json array
            JsonArray array = new JsonArray();
            int max_entries = Randomness.nextInt(Properties.GRAMMAR_JSON_MAX_ELEMENTS);
            for (int i = 0; i <= max_entries; i++) {
                this.addRandomElement(array);
            }
            this.jsonElement = array;
        } else { // Json object
            JsonObject object = new JsonObject();
            int max_entries = Randomness.nextInt(Properties.GRAMMAR_JSON_MAX_ELEMENTS);
            for (int i = 0; i <= max_entries; i++) {
                this.addRandomElement(object);
            }
            this.jsonElement = object;
        }
    }

    /* (non-Javadoc)
     * @see org.evosuite.testcase.PrimitiveStatement#delta()
     */
    /** {@inheritDoc} */
    @Override
    public void delta() {
        final double P = 1d / 3d;

        if (this.jsonElement.isJsonArray()) { // Json array
            JsonArray array = (JsonArray) this.jsonElement;

            // Delete
            if (Randomness.nextDouble() <= P) {
                this.deleteRandomElement(array);
            }

            // Change
            if (Randomness.nextDouble() <= P) {
                this.changeRandomElement(array);
            }

            // Insert
            if (Randomness.nextDouble() <= P) {
                this.addRandomElement(array);
            }

            this.jsonElement = array;
        } else if (this.jsonElement.isJsonObject()) { // Json object
            JsonObject object = (JsonObject) this.jsonElement;

            // Delete
            if (Randomness.nextDouble() <= P) {
                this.deleteRandomElement(object);
            }

            // Change
            if (Randomness.nextDouble() <= P) {
                this.changeRandomElement(object);
            }

            // Insert
            if (Randomness.nextDouble() <= P) {
                this.addRandomElement(object);
            }

            this.jsonElement = object;
        }
    }

    /* (non-Javadoc)
     * @see org.evosuite.testcase.PrimitiveStatement#increment()
     */
    /** {@inheritDoc} */
    @Override
    public void increment() {
        this.delta();
    }

    private JsonPrimitive getRandomPrimitiveElement() {
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

    private JsonElement getCandidate(JsonArray array) {
        ArrayList<JsonElement> candidates = new ArrayList<>();
        for (JsonElement element : array) {
            if (element.isJsonArray() || element.isJsonObject()) {
                candidates.add(element);
            }
        }

        return Randomness.choice(candidates);
    }

    private JsonElement getCandidate(JsonObject object) {
        ArrayList<JsonElement> candidates = new ArrayList<>();
        for (String property : object.keySet()) {
            JsonElement element = object.get(property);
            if (element.isJsonArray() || element.isJsonObject()) {
                candidates.add(element);
            }
        }

        return Randomness.choice(candidates);
    }

    private void addRandomNestedElement(JsonElement element) {
        ConstantPool constantPool = ConstantPoolManager.getInstance().getConstantPool();

        if (element != null) {
            if (element.isJsonArray()) {
                JsonArray array = element.getAsJsonArray();
                array.add(this.getRandomPrimitiveElement());
            } else if (element.isJsonObject()) {
                JsonObject object = element.getAsJsonObject();
                object.add(constantPool.getRandomString(), this.getRandomPrimitiveElement());
            }
        }

        // TODO: Maybe add JsonNull element
    }

    private void addRandomElement(JsonArray array) {
        if (Randomness.nextDouble() <= Properties.GRAMMAR_JSON_NESTED) { // Add second layer elements
            JsonElement candidate = this.getCandidate(array);
            if (candidate != null) {
                this.addRandomNestedElement(candidate);
                return;
            }
        }

        if (Randomness.nextDouble() <= Properties.GRAMMAR_JSON_NULL) {
            array.add(JsonNull.INSTANCE);
        } else if (Randomness.nextDouble() <= Properties.GRAMMAR_JSON_PRIMITIVE) {
            array.add(this.getRandomPrimitiveElement());
        } else if (Randomness.nextDouble() <= Properties.GRAMMAR_JSON_ARRAY) {
            array.add(new JsonArray());
        } else {
            array.add(new JsonObject());
        }
    }

    private void addRandomElement(JsonObject object) {
        ConstantPool constantPool = ConstantPoolManager.getInstance().getConstantPool();

        if (Randomness.nextDouble() <= Properties.GRAMMAR_JSON_NESTED) { // Add second layer elements
            JsonElement candidate = getCandidate(object);
            if (candidate != null) {
                this.addRandomNestedElement(candidate);
                return;
            }
        }

        if (Randomness.nextDouble() <= Properties.GRAMMAR_JSON_NULL) {
            object.add(constantPool.getRandomString(), JsonNull.INSTANCE);
        } else if (Randomness.nextDouble() <= Properties.GRAMMAR_JSON_PRIMITIVE) {
            object.add(constantPool.getRandomString(), this.getRandomPrimitiveElement());
        } else if (Randomness.nextDouble() <= Properties.GRAMMAR_JSON_ARRAY) {
            object.add(constantPool.getRandomString(), new JsonArray());
        } else {
            object.add(constantPool.getRandomString(), new JsonObject());
        }
    }

    private void removeRandomNestedElement(JsonElement candidate) {
        if (candidate != null) {
            if (candidate.isJsonArray()) {
                JsonArray array2 = candidate.getAsJsonArray();
                if (array2.size() > 0) {
                    int array2_index = Randomness.nextInt(array2.size());
                    array2.remove(array2_index);
                }
            } else if (candidate.isJsonObject()) {
                JsonObject object2 = candidate.getAsJsonObject();
                if (object2.size() > 0) {
                    String object2_property = Randomness.choice(object2.keySet());
                    object2.remove(object2_property);
                }
            }
        }
    }

    private void deleteRandomElement(JsonArray array) {
        if (array.size() < 1)
            return;

        if (Randomness.nextDouble() <= Properties.GRAMMAR_JSON_NESTED) { // delete second layer elements
            JsonElement candidate = this.getCandidate(array);
            if (candidate != null) {
                this.removeRandomNestedElement(candidate);
                return;
            }
        }

        int index = Randomness.nextInt(array.size());
        array.remove(index);
    }

    private void deleteRandomElement(JsonObject object) {
        if (object.size() < 1)
            return;

        if (Randomness.nextDouble() <= Properties.GRAMMAR_JSON_NESTED) { // Delete second layer elements
            JsonElement candidate = getCandidate(object);
            if (candidate != null) {
                this.removeRandomNestedElement(candidate);
                return;
            }
        }

        String property = Randomness.choice(object.keySet());
        object.remove(property);
    }

    private String mutateString(String s) {
        final double P = 1d / 3d;

        // Delete
        if (Randomness.nextDouble() <= P) {
            double P1 = 1d / s.length();
            for (int i = s.length(); i > 0; i--) {
                if (Randomness.nextDouble() < P1) {
                    s = removeCharAt(s, i - 1);
                }
            }
        }

        // Change
        if (Randomness.nextDouble() <= P) {
            double P1 = 1d / s.length();
            for (int i = 0; i < s.length(); i++) {
                if (Randomness.nextDouble() < P1) {
                    s = replaceCharAt(s, i, Randomness.nextChar());
                }
            }
        }

        // Insert
        if (Randomness.nextDouble() <= P) {
            int pos = 0;
            if (s.length() > 0)
                pos = Randomness.nextInt(s.length());
            s = StringInsert(s, pos);
        }

        return s;
    }

    private JsonPrimitive changeRandomPrimitiveElement(JsonPrimitive primitive) {
        if (primitive.isBoolean()) {
            return new JsonPrimitive(!primitive.getAsBoolean());
        }

        if (primitive.isNumber()) {
            double P = Randomness.nextDouble();
            if (P <= 1d / 4d) {
                int number = primitive.getAsInt();
                int delta = (int) Math.floor(Randomness.nextGaussian() * Properties.MAX_DELTA);
                return new JsonPrimitive(number + delta);
            } else if (P <= 2d / 4d) {
                long number = primitive.getAsLong();
                long delta = (long) Math.floor(Randomness.nextGaussian() * Properties.MAX_DELTA);
                return new JsonPrimitive(number + delta);
            } else if (P <= 3d / 4d) {
                double number = primitive.getAsDouble();
                double delta = Math.floor(Randomness.nextGaussian() * Properties.MAX_DELTA);
                return new JsonPrimitive(number + delta);
            } else {
                float number = primitive.getAsFloat();
                float delta = (float) Math.floor(Randomness.nextGaussian() * Properties.MAX_DELTA);
                return new JsonPrimitive(number + delta);
            }
        }

        if (primitive.isString()) {
            String s = primitive.getAsString();
            s = this.mutateString(s);
            return new JsonPrimitive(s);
        }

        return primitive;
    }

    private void changeRandomComplexElement(JsonElement element) {
        if (element.isJsonArray()) {
            JsonArray array = element.getAsJsonArray();

            if (array.size() > 0) {
                int index = Randomness.nextInt(array.size());
                JsonElement element2 = array.get(index);
                if (element2.isJsonPrimitive()) {
                    JsonPrimitive primitive = element2.getAsJsonPrimitive();
                    array.set(index, this.changeRandomPrimitiveElement(primitive));
                }
            }
        } else if (element.isJsonObject()) {
            JsonObject object = element.getAsJsonObject();

            if (object.size() > 0) {
                String property = Randomness.choice(object.keySet());
                JsonElement element2 = object.get(property);
                if (element2.isJsonPrimitive()) {
                    JsonPrimitive primitive = element2.getAsJsonPrimitive();
                    object.add(property, this.changeRandomPrimitiveElement(primitive));
                }
            }
        }
    }

    private void changeRandomElement(JsonArray array) {
        if (array.size() < 1)
            return;

        int index = Randomness.nextInt(array.size());
        JsonElement element = array.get(index);

        if (element.isJsonNull()) {
            array.set(index, this.getRandomPrimitiveElement());
            // TODO: Maybe add array or object elements
        } else if (element.isJsonPrimitive()) {
            JsonPrimitive primitive = element.getAsJsonPrimitive();
            array.set(index, this.changeRandomPrimitiveElement(primitive));
        } else {
            changeRandomComplexElement(element);
        }
    }

    private void changeRandomElement(JsonObject object) {
        ConstantPool constantPool = ConstantPoolManager.getInstance().getConstantPool();

        if (object.size() < 1)
            return;

        String property = Randomness.choice(object.keySet());
        JsonElement element = object.get(property);

        double P = Randomness.nextDouble();
        if (P <= Properties.GRAMMAR_JSON_PROPERTY) { // Change property name
            object.remove(property);
            String newProperty = this.mutateString(property);
            object.add(newProperty, element);
        } else { // Change property value
            if (element.isJsonNull()) {
                object.add(property, this.getRandomPrimitiveElement());
                // TODO: Maybe add array or object elements
            } else if (element.isJsonPrimitive()) {
                JsonPrimitive primitive = element.getAsJsonPrimitive();
                object.add(property, this.changeRandomPrimitiveElement(primitive));
            } else {
                changeRandomComplexElement(element);
            }
        }
    }

    /* (non-Javadoc)
     * @see org.evosuite.testcase.StatementInterface#execute(org.evosuite.testcase.execution.Scope, java.io.PrintStream)
     */
    /** {@inheritDoc} */
    @Override
    public Throwable execute(Scope scope, PrintStream out)
            throws InvocationTargetException, IllegalArgumentException,
            IllegalAccessException, InstantiationException {
        this.value = gson.toJson(this.jsonElement);
        return super.execute(scope, out);
    }

    /* (non-Javadoc)
     * @see org.evosuite.testcase.StatementInterface#copy(org.evosuite.testcase.TestCase, int)
     */
    /** {@inheritDoc} */
    @Override
    public Statement copy(TestCase newTestCase, int offset) {
        this.value = gson.toJson(this.jsonElement);
        return new JsonStatement(newTestCase, this.value); // TODO: optimize using deep copy
    }

    /* (non-Javadoc)
     * @see org.evosuite.testcase.StatementInterface#equals(java.lang.Object)
     */
    /** {@inheritDoc} */
    @Override
    public boolean equals(Object s) {
        this.value = gson.toJson(this.jsonElement);
        return super.equals(s);
    }

    /* (non-Javadoc)
     * @see org.evosuite.testcase.StatementInterface#hashCode()
     */
    /** {@inheritDoc} */
    @Override
    public int hashCode() {
        this.value = gson.toJson(this.jsonElement);
        return super.hashCode();
    }

    /* (non-Javadoc)
     * @see org.evosuite.testcase.StatementInterface#same(org.evosuite.testcase.statements.Statement)
     */
    /** {@inheritDoc} */
    @Override
    public boolean same(Statement s) {
        this.value = gson.toJson(this.jsonElement);
        return super.same(s);
    }
}
