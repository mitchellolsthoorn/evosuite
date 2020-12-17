package org.evosuite.testcase.statements.grammar;

import com.google.gson.*;
import org.evosuite.Properties;
import org.evosuite.testcase.TestCase;
import org.evosuite.testcase.TestFactory;
import org.evosuite.testcase.execution.Scope;
import org.evosuite.testcase.mutators.grammar.JsonArrayMutator;
import org.evosuite.testcase.mutators.grammar.JsonObjectMutator;
import org.evosuite.testcase.statements.Statement;
import org.evosuite.testcase.statements.StringPrimitiveStatement;
import org.evosuite.utils.Randomness;

import java.io.PrintStream;
import java.lang.reflect.InvocationTargetException;

/**
 * Statement class representing a JSON structure as a string gene.
 *
 * By emulating complex structured information as a string it allows
 * the algorithm to determine if an application responds well to this
 * kind of structured data.
 *
 * @author Mitchell Olsthoorn
 */
public class JsonStatement extends StringPrimitiveStatement {

    private static final long serialVersionUID = 278735526699835887L;

    private transient Gson gson;
    private transient JsonElement jsonElement;

    static final String EMPTY_JSON_STRING = "{}";

    /**
     * Constructor for JSONStatement.
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
     * Constructor for JSONStatement.
     *
     * @param tc a {@link TestCase} object.
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
        this.value = val; // TODO: probably not needed
        return val;
    }

    /* (non-Javadoc)
     * @see org.evosuite.testcase.PrimitiveStatement#setValue(java.lang.Object)
     */
    /** {@inheritDoc} */
    @Override
    public void setValue(String val) {
        this.jsonElement = gson.fromJson(val, JsonElement.class);
        this.value = val; // TODO: probably not needed
    }

    /* (non-Javadoc)
     * @see org.evosuite.testcase.PrimitiveStatement#zero(java.lang.Object)
     */
    /** {@inheritDoc} */
    @Override
    public void zero() {
        this.jsonElement = new JsonObject();
        this.value = JsonStatement.EMPTY_JSON_STRING; // TODO: probably not needed
    }

    /* (non-Javadoc)
     * @see org.evosuite.testcase.StatementInterface#mutate(org.evosuite.testcase.TestCase, org.evosuite.testcase.TestFactory)
     */
    /** {@inheritDoc} */
    @Override
    public boolean mutate(TestCase test, TestFactory factory) {
        JsonElement oldVal = this.jsonElement;

        int current = 0;

        // Try GRAMMAR_MUTATION_RETRY_LIMIT number of times when the new value is the same as the old
        while (this.jsonElement.equals(oldVal) && current < Properties.GRAMMAR_MUTATION_RETRY_LIMIT) {
            if (Randomness.nextDouble() <= Properties.RANDOM_PERTURBATION) {
                this.randomize();
            } else {
                this.delta();
            }
            current++;
        }

        this.value = gson.toJson(this.jsonElement); // TODO: probably not needed
        return true;
    }

    /* (non-Javadoc)
     * @see org.evosuite.testcase.PrimitiveStatement#randomize()
     */
    /** {@inheritDoc} */
    @Override
    public void randomize() {
        if (Randomness.nextDouble() <= Properties.GRAMMAR_JSON_ARRAY) { // JSON array
            JsonArray array = new JsonArray();

            // Add between zero and GRAMMAR_MAX_ELEMENTS number of elements randomly
            int max_entries = Randomness.nextInt(Properties.GRAMMAR_MAX_ELEMENTS);
            for (int i = 0; i <= max_entries; i++) {
                JsonArrayMutator.insertRandomElement(array);
            }
            this.jsonElement = array;
        } else { // JSON object
            JsonObject object = new JsonObject();

            // Add between zero and GRAMMAR_MAX_ELEMENTS number of elements randomly
            int max_entries = Randomness.nextInt(Properties.GRAMMAR_MAX_ELEMENTS);
            for (int i = 0; i <= max_entries; i++) {
                JsonObjectMutator.insertRandomElement(object);
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

        if (this.jsonElement.isJsonArray()) { // JSON array
            JsonArray array = (JsonArray) this.jsonElement;

            // Delete
            if (Randomness.nextDouble() <= P) {
                JsonArrayMutator.deleteRandomElement(array);
            }

            // Change
            if (Randomness.nextDouble() <= P) {
                JsonArrayMutator.changeRandomElement(array);
            }

            // Insert
            if (Randomness.nextDouble() <= P) {
                JsonArrayMutator.insertRandomElement(array);
            }

            this.jsonElement = array;
        } else if (this.jsonElement.isJsonObject()) { // JSON object
            JsonObject object = (JsonObject) this.jsonElement;

            // Delete
            if (Randomness.nextDouble() <= P) {
                JsonObjectMutator.deleteRandomElement(object);
            }

            // Change
            if (Randomness.nextDouble() <= P) {
                JsonObjectMutator.changeRandomElement(object);
            }

            // Insert
            if (Randomness.nextDouble() <= P) {
                JsonObjectMutator.insertRandomElement(object);
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

        // With GRAMMAR_JSON_INVALID chance return a StringPrimitiveStatement to allow for invalid mutations
        if (Randomness.nextDouble() <= Properties.GRAMMAR_JSON_INVALID) {
            return new StringPrimitiveStatement(newTestCase, this.value);
        } else {
            return new JsonStatement(newTestCase, this.value); // TODO: optimize using deep copy
        }
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
