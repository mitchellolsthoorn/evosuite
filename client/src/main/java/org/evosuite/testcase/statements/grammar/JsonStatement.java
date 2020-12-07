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

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.PrintStream;
import java.lang.reflect.InvocationTargetException;

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

        int lim = 4;
        int current = 0;

        while (this.jsonElement.equals(oldVal) && current < lim) {
            if (Randomness.nextDouble() <= Properties.RANDOM_PERTURBATION) {
                randomize();
            } else {
                delta();
            }
            current++;
        }

        this.value = gson.toJson(this.jsonElement);

        return true;
    }

    private void addRandomPrimitiveElement(JsonArray array) {
        ConstantPool constantPool = ConstantPoolManager.getInstance().getConstantPool();

        double P1 = Randomness.nextDouble();
        if (P1 <= 1d / 6d) {
            array.add(Randomness.nextBoolean());
        } else if (P1 <= 2d / 6d) {
            array.add(constantPool.getRandomInt());
        } else if (P1 <= 3d / 6d) {
            array.add(constantPool.getRandomLong());
        } else if (P1 <= 4d / 6d) {
            array.add(constantPool.getRandomDouble());
        } else if (P1 <= 5d / 6d) {
            array.add(constantPool.getRandomFloat());
        } else {
            array.add(constantPool.getRandomString());
        }
    }

    private void addRandomPrimitiveElement(JsonObject object) {
        ConstantPool constantPool = ConstantPoolManager.getInstance().getConstantPool();

        double P1 = Randomness.nextDouble();
        if (P1 <= 1d / 6d) {
            object.addProperty(constantPool.getRandomString(), Randomness.nextBoolean());
        } else if (P1 <= 2d / 6d) {
            object.addProperty(constantPool.getRandomString(), constantPool.getRandomInt());
        } else if (P1 <= 3d / 6d) {
            object.addProperty(constantPool.getRandomString(), constantPool.getRandomLong());
        } else if (P1 <= 4d / 6d) {
            object.addProperty(constantPool.getRandomString(), constantPool.getRandomDouble());
        } else if (P1 <= 5d / 6d) {
            object.addProperty(constantPool.getRandomString(), constantPool.getRandomFloat());
        } else {
            object.addProperty(constantPool.getRandomString(), constantPool.getRandomString());
        }
    }

    private void addRandomJsonElement(JsonArray array) {
        if (Randomness.nextDouble() <= 0.2d) { // Add second layer elements
            for (JsonElement element : array) {
                if (element.isJsonArray()) {
                    JsonArray array2 = element.getAsJsonArray();
                    this.addRandomPrimitiveElement(array2);
                    return;
                } else if (element.isJsonObject()) {
                    JsonObject object2 = element.getAsJsonObject();
                    this.addRandomPrimitiveElement(object2);
                    return;
                }
            }
        }

        // TODO: Add null element
        if (Randomness.nextDouble() <= Properties.GRAMMAR_JSON_PRIMITIVE) {
            this.addRandomPrimitiveElement(array);
        } else {
            if (Randomness.nextDouble() <= Properties.GRAMMAR_JSON_ARRAY) {
                JsonArray array2 = new JsonArray();
                array.add(array2);
            } else {
                JsonObject object2 = new JsonObject();
                array.add(object2);
            }
        }
    }

    private void addRandomJsonElement(JsonObject object) {
        ConstantPool constantPool = ConstantPoolManager.getInstance().getConstantPool();

        if (Randomness.nextDouble() <= 0.2d) { // Add second layer elements
            for (String property : object.keySet()) {
                JsonElement element = object.get(property);
                if (element.isJsonArray()) {
                    JsonArray array2 = element.getAsJsonArray();
                    this.addRandomPrimitiveElement(array2);
                    return;
                } else if (element.isJsonObject()) {
                    JsonObject object2 = element.getAsJsonObject();
                    this.addRandomPrimitiveElement(object2);
                    return;
                }
            }
        }

        // TODO: Add null element
        if (Randomness.nextDouble() <= Properties.GRAMMAR_JSON_PRIMITIVE) {
            this.addRandomPrimitiveElement(object);
        } else {
            if (Randomness.nextDouble() <= Properties.GRAMMAR_JSON_ARRAY) {
                JsonArray array2 = new JsonArray();
                object.add(constantPool.getRandomString(), array2);
            } else {
                JsonObject object2 = new JsonObject();
                object.add(constantPool.getRandomString(), object2);
            }
        }
    }

    /* (non-Javadoc)
     * @see org.evosuite.testcase.PrimitiveStatement#randomize()
     */
    /** {@inheritDoc} */
    @Override
    public void randomize() {
        // skip Json primitives

        if (Randomness.nextDouble() <= Properties.GRAMMAR_JSON_ARRAY) { // Json array
            JsonArray array = new JsonArray();
            int max_entries = Randomness.nextInt(Properties.GRAMMAR_JSON_MAX_ENTRIES);
            for (int i = 0; i <= max_entries; i++) {
                this.addRandomJsonElement(array);
            }
            this.jsonElement = array;
        } else { // Json object
            JsonObject object = new JsonObject();
            int max_entries = Randomness.nextInt(Properties.GRAMMAR_JSON_MAX_ENTRIES);
            for (int i = 0; i <= max_entries; i++) {
                this.addRandomJsonElement(object);
            }
            this.jsonElement = object;
        }
    }

    private void deleteRandomJsonElement(JsonArray array) {
        if (array.size() <= 0) {
            return;
        }

        if (Randomness.nextDouble() <= 0.2d) { // delete second layer elements
            for (JsonElement element : array) { // TODO: Don't pick the first one
                if (element.isJsonArray()) {
                    JsonArray array2 = element.getAsJsonArray();

                    if (array2.size() <= 0) {
                        break;
                    }

                    int index = Randomness.nextInt(array2.size());
                    array2.remove(index);
                    return;
                } else if (element.isJsonObject()) {
                    JsonObject object2 = element.getAsJsonObject();

                    if (object2.size() <= 0) {
                        break;
                    }

                    int index = Randomness.nextInt(object2.size());
                    //Randomness.choice(object2.keySet());
                    // TODO: look into collection.choice
                    int current = 0;
                    String property = null; // TODO: remove null
                    for (String prop: object2.keySet()) {
                        if (current == index) {
                            property = prop;
                            break;
                        }
                        current++;
                    }
                    object2.remove(property);
                    return;
                }
            }
        }

        int index = Randomness.nextInt(array.size());
        array.remove(index);
    }

    private void deleteRandomJsonElement(JsonObject object) {
        if (object.size() <= 0) {
            return;
        }

        if (Randomness.nextDouble() <= 0.2d) { // Delete second layer elements
            for (String property : object.keySet()) { // TODO: Don't pick the first one
                JsonElement element = object.get(property);
                if (element.isJsonArray()) {
                    JsonArray array2 = element.getAsJsonArray();

                    if (array2.size() <= 0) {
                        break;
                    }

                    int index = Randomness.nextInt(array2.size());
                    array2.remove(index);
                    return;
                } else if (element.isJsonObject()) {
                    JsonObject object2 = element.getAsJsonObject();

                    if (object2.size() <= 0) {
                        break;
                    }

                    int index2 = Randomness.nextInt(object2.size());

                    int current2 = 0;
                    String property2 = null; // TODO: remove null
                    for (String prop2: object2.keySet()) {
                        if (current2 == index2) {
                            property = prop2;
                            break;
                        }
                        current2++;
                    }
                    object2.remove(property);
                    return;
                }
            }
        }

        int index = Randomness.nextInt(object.size());

        int current = 0;
        String property = null; // TODO: remove null
        for (String prop: object.keySet()) {
            if (current == index) {
                property = prop;
                break;
            }
            current++;
        }
        object.remove(property);
    }

    private void changeRandomPrimitiveElement(JsonArray array) {
        ConstantPool constantPool = ConstantPoolManager.getInstance().getConstantPool();

        if (array.size() <= 0) {
            return;
        }

        int index = Randomness.nextInt(array.size());
        JsonElement element = array.get(index);

        if (element.isJsonPrimitive()) {
            JsonPrimitive primitive = element.getAsJsonPrimitive();

            if (primitive.isBoolean()) {
                array.set(index, new JsonPrimitive(!primitive.getAsBoolean()));
            } else if (primitive.isNumber()) {
                double P1 = Randomness.nextDouble();
                if (P1 <= 1d / 4d) {
                    array.set(index, new JsonPrimitive(constantPool.getRandomInt()));
                } else if (P1 <= 2d / 4d) {
                    array.set(index, new JsonPrimitive(constantPool.getRandomInt()));
                } else if (P1 <= 3d / 4d) {
                    array.set(index, new JsonPrimitive(constantPool.getRandomLong()));
                } else {
                    array.set(index, new JsonPrimitive(constantPool.getRandomDouble()));
                }
            } else if (primitive.isString()) {
                array.set(index, new JsonPrimitive(constantPool.getRandomString()));
            }
        }
    }

    private void changeRandomPrimitiveElement(JsonObject object) {
        ConstantPool constantPool = ConstantPoolManager.getInstance().getConstantPool();

        if (object.size() <= 0) {
            return;
        }

        int index = Randomness.nextInt(object.size());

        int current = 0;
        String property = null; // TODO: remove null
        for (String prop: object.keySet()) {
            if (current == index) {
                property = prop;
                break;
            }
            current++;
        }

        JsonElement element = object.get(property);

        double P = Randomness.nextDouble();
        if (P <= 1d / 2d) { // Change property name
            object.remove(property);
            object.add(constantPool.getRandomString(), element);
        } else {
            if (element.isJsonPrimitive()) {
                JsonPrimitive primitive = element.getAsJsonPrimitive();

                if (primitive.isBoolean()) {
                    object.add(property, new JsonPrimitive(!primitive.getAsBoolean()));
                } else if (primitive.isNumber()) {
                    double P1 = Randomness.nextDouble();
                    if (P1 <= 1d / 4d) {
                        object.add(property, new JsonPrimitive(constantPool.getRandomInt()));
                    } else if (P1 <= 2d / 4d) {
                        object.add(property, new JsonPrimitive(constantPool.getRandomInt()));
                    } else if (P1 <= 3d / 4d) {
                        object.add(property, new JsonPrimitive(constantPool.getRandomLong()));
                    } else {
                        object.add(property, new JsonPrimitive(constantPool.getRandomDouble()));
                    }
                } else if (primitive.isString()) {
                    object.add(property, new JsonPrimitive(constantPool.getRandomString()));
                }
            }
        }
    }

    private void changeRandomJsonElement(JsonArray array) {
        ConstantPool constantPool = ConstantPoolManager.getInstance().getConstantPool();

        if (array.size() <= 0) {
            return;
        }

        int index = Randomness.nextInt(array.size());
        JsonElement element = array.get(index);

        if (element.isJsonNull()) {
            // TODO: inspect
            array.remove(index);
            this.addRandomPrimitiveElement(array);
        } else if (element.isJsonPrimitive()) {
            this.changeRandomPrimitiveElement(array);
        } else if (element.isJsonArray()) {
            JsonArray array2 = element.getAsJsonArray();
            this.changeRandomPrimitiveElement(array2);
        } else if (element.isJsonObject()) {
            JsonObject object2 = element.getAsJsonObject();
            this.changeRandomPrimitiveElement(object2);
        }
    }

    private void changeRandomJsonElement(JsonObject object) {
        ConstantPool constantPool = ConstantPoolManager.getInstance().getConstantPool();

        if (object.size() <= 0) {
            return;
        }

        int index = Randomness.nextInt(object.size());

        int current = 0;
        String property = null; // TODO: remove null
        for (String prop: object.keySet()) {
            if (current == index) {
                property = prop;
                break;
            }
            current++;
        }

        JsonElement element = object.get(property);

        double P = Randomness.nextDouble();
        if (P <= 1d / 2d) { // Change property name
            object.remove(property);
            object.add(constantPool.getRandomString(), element);
        } else { // Change property value
            if (element.isJsonNull()) {
                // TODO: inspect
                object.remove(property);
                this.addRandomPrimitiveElement(object);
            } else if (element.isJsonPrimitive()) {
                this.changeRandomPrimitiveElement(object);
            } else if (element.isJsonArray()) {
                JsonArray array2 = element.getAsJsonArray();
                this.changeRandomPrimitiveElement(array2);
            } else if (element.isJsonObject()) {
                JsonObject object2 = element.getAsJsonObject();
                this.changeRandomPrimitiveElement(object2);
            }
        }
    }

    /* (non-Javadoc)
     * @see org.evosuite.testcase.PrimitiveStatement#delta()
     */
    /** {@inheritDoc} */
    @Override
    public void delta() {
        JsonElement element = this.jsonElement;

        if (element.isJsonArray()) { // Json array
            JsonArray array = (JsonArray) element;

            // Delete
            if (Randomness.nextDouble() <= Properties.P_TEST_DELETE) {
                this.deleteRandomJsonElement(array);
            }

            // Change
            if (Randomness.nextDouble() <= Properties.P_TEST_CHANGE) {
                this.changeRandomJsonElement(array);
            }

            // Insert
            if (Randomness.nextDouble() <= Properties.P_TEST_INSERT) {
                this.addRandomJsonElement(array);
            }

            this.jsonElement = array;
        } else if (element.isJsonObject()) { // Json object
            JsonObject object = (JsonObject) element;

            // Delete
            if (Randomness.nextDouble() <= Properties.P_TEST_DELETE) {
                this.deleteRandomJsonElement(object);
            }

            // Change
            if (Randomness.nextDouble() <= Properties.P_TEST_CHANGE) {
                this.changeRandomJsonElement(object);
            }

            // Insert
            if (Randomness.nextDouble() <= Properties.P_TEST_INSERT) {
                this.addRandomJsonElement(object);
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
        JsonElement element = this.jsonElement;

        if (element.isJsonNull()) {
            this.delta();
            return;
        } else if (element.isJsonPrimitive()) {
            JsonPrimitive primitive = (JsonPrimitive) element;
            if (primitive.isBoolean()) {
                element = new JsonPrimitive(!primitive.getAsBoolean());
            } else if (primitive.isNumber()) {
                element = new JsonPrimitive(primitive.getAsNumber().floatValue() + 1);
            } else if (primitive.isString()) {
                String s = primitive.getAsString();
                if (s.isEmpty()) {
                    s += Randomness.nextChar();
                } else {
                    s = replaceCharAt(s, Randomness.nextInt(s.length()), Randomness.nextChar());
                }
                element = new JsonPrimitive(s);
            }
        } else if (element.isJsonArray()) {
            JsonArray array = element.getAsJsonArray();
            this.delta();
            return;
        } else if (element.isJsonObject()) {
            JsonObject object = element.getAsJsonObject();
            this.delta();
            return;
        }

        this.jsonElement = element;
        this.value = gson.toJson(this.jsonElement);
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

//    private void writeObject(ObjectOutputStream oos) throws IOException {
//        oos.defaultWriteObject();
//        this.value = gson.toJson(this.jsonElement);
//        oos.writeObject(this.value);
//    }
//
//    private void readObject(ObjectInputStream ois) throws ClassNotFoundException,
//            IOException {
//        ois.defaultReadObject();
//        this.value = (String) ois.readObject();
//        this.jsonElement = gson.fromJson(this.value, JsonElement.class);
//    }
}
