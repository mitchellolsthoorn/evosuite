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

        while (this.jsonElement.equals(oldVal)) {
            if (Randomness.nextDouble() <= Properties.RANDOM_PERTURBATION) {
                randomize();
            } else
                delta();
        }

        this.value = gson.toJson(this.jsonElement);

        return true;
    }

    private void addRandomJsonElements(JsonArray array, int level) {
        ConstantPool constantPool = ConstantPoolManager.getInstance().getConstantPool();

        int newLevel = level + 1;

        if (Randomness.nextDouble() <= Properties.GRAMMAR_JSON_PRIMITIVE || level >= Properties.GRAMMAR_JSON_MAX_LEVEL) {
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
        } else {
            if (Randomness.nextDouble() <= Properties.GRAMMAR_JSON_ARRAY) {
                JsonArray array2 = new JsonArray();
                int max_entries = Randomness.nextInt(Properties.GRAMMAR_JSON_MAX_ENTRIES);
                for (int i = 0; i < max_entries; i++) {
                    this.addRandomJsonElements(array2, newLevel);
                }
                array.add(array2);
            } else {
                JsonObject object2 = new JsonObject();
                int max_entries = Randomness.nextInt(Properties.GRAMMAR_JSON_MAX_ENTRIES);
                for (int i = 0; i < max_entries; i++) {
                    this.addRandomJsonElements(object2, newLevel);
                }
                array.add(object2);
            }
        }
    }

    private void addRandomJsonElements(JsonObject object, int level) {
        ConstantPool constantPool = ConstantPoolManager.getInstance().getConstantPool();

        int newLevel = level + 1;

        if (Randomness.nextDouble() <= Properties.GRAMMAR_JSON_PRIMITIVE || level >= Properties.GRAMMAR_JSON_MAX_LEVEL) {
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
        } else {
            if (Randomness.nextDouble() <= Properties.GRAMMAR_JSON_ARRAY) {
                JsonArray array2 = new JsonArray();
                int max_entries = Randomness.nextInt(Properties.GRAMMAR_JSON_MAX_ENTRIES);
                for (int i = 0; i < max_entries; i++) {
                    this.addRandomJsonElements(array2, newLevel);
                }
                object.add(constantPool.getRandomString(), array2);
            } else {
                JsonObject object2 = new JsonObject();
                int max_entries = Randomness.nextInt(Properties.GRAMMAR_JSON_MAX_ENTRIES);
                for (int i = 0; i < max_entries; i++) {
                    this.addRandomJsonElements(object2, newLevel);
                }
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
            for (int i = 0; i < max_entries; i++) {
                this.addRandomJsonElements(array, 0);
            }
            this.jsonElement = array;
        } else { // Json object
            JsonObject object = new JsonObject();
            int max_entries = Randomness.nextInt(Properties.GRAMMAR_JSON_MAX_ENTRIES);
            for (int i = 0; i < max_entries; i++) {
                this.addRandomJsonElements(object, 0);
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
        JsonElement element = this.jsonElement;

        double P1 = 1d / 3d;

        if (element.isJsonArray()) { // Json array
            JsonArray array = (JsonArray) element;

            // Delete
            if (Randomness.nextDouble() <= P1) {
                double P2 = 1d / array.size();
                JsonElement del = null;
                for (JsonElement element1 : array) {
                    if (Randomness.nextDouble() <= P2) {
                        del = element1;
                    }
                }
                if (del != null) {
                    array.remove(del);
                }
            }

            // Change
            if (Randomness.nextDouble() < P1) {
                double P2 = 1d / array.size();
                JsonElement del = null;
                for (JsonElement element1 : array) {
                    if (Randomness.nextDouble() <= P2) {
                        del = element1;
                    }
                }
                if (del != null) {
                    array.remove(del);
                    this.addRandomJsonElements(array, 0);
                }
            }

            // Insert
            if (Randomness.nextDouble() < P1) {
                this.addRandomJsonElements(array, 0);
            }

            this.jsonElement = array;
        } else if (element.isJsonObject()) { // Json object
            JsonObject object = (JsonObject) element;

            // Delete
            if (Randomness.nextDouble() <= P1) {
                double P2 = 1d / object.size();
                String del = null;
                for (String property: object.keySet()) {
                    if (Randomness.nextDouble() <= P2) {
                        del = property;
                    }
                }
                if (del != null) {
                    object.remove(del);
                }
            }

            // Change
            if (Randomness.nextDouble() < P1) {
                double P2 = 1d / object.size();
                String del = null;
                for (String property: object.keySet()) {
                    if (Randomness.nextDouble() <= P2) {
                        del = property;
                    }
                }
                if (del != null) {
                    object.remove(del);
                    this.addRandomJsonElements(object, 0);
                }
            }

            // Insert
            if (Randomness.nextDouble() < P1) {
                this.addRandomJsonElements(object, 0);
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
            this.randomize();
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
            JsonArray array = (JsonArray) element;
            this.delta();
            return;
        } else if (element.isJsonObject()) {
            JsonObject object = (JsonObject) element;
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
