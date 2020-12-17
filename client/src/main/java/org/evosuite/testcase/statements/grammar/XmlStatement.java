package org.evosuite.testcase.statements.grammar;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import org.evosuite.Properties;
import org.evosuite.testcase.TestCase;
import org.evosuite.testcase.TestFactory;
import org.evosuite.testcase.execution.Scope;
import org.evosuite.testcase.mutators.grammar.XmlMutator;
import org.evosuite.testcase.statements.Statement;
import org.evosuite.testcase.statements.StringPrimitiveStatement;
import org.evosuite.utils.Randomness;

import java.io.PrintStream;
import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;

/**
 * Statement class representing a XML structure as a string gene.
 *
 * By emulating complex structured information as a string it allows
 * the algorithm to determine if an application responds well to this
 * kind of structured data.
 *
 * @author Mitchell Olsthoorn
 */
public class XmlStatement extends StringPrimitiveStatement {

    private static final long serialVersionUID = 278735526699835887L;

    private transient XmlMapper xmlMapper;
    private transient HashMap<String, Object> xmlElement;

    // TODO: Allow for a variable root name
    public static final String EMPTY_XML_STRING = "<HashMap></HashMap>";

    /**
     * Constructor for XmlStatement.
     *
     * @param tc    a {@link TestCase} object.
     * @param value a {@link String} object.
     */
    public XmlStatement(TestCase tc, String value) {
        super(tc, value);

        this.xmlMapper = new XmlMapper();
        try {
            this.xmlElement = xmlMapper.readValue(value, HashMap.class);
        } catch (JsonProcessingException e) {
            this.xmlElement = new HashMap<>();
            this.value = XmlStatement.EMPTY_XML_STRING;
            e.printStackTrace();
        }
    }

    /**
     * Constructor for XmlStatement.
     *
     * @param tc a {@link TestCase} object.
     */
    public XmlStatement(TestCase tc) {
        super(tc, XmlStatement.EMPTY_XML_STRING);

        this.xmlMapper = new XmlMapper();
        this.xmlElement = new HashMap<>();
    }

    /* (non-Javadoc)
     * @see org.evosuite.testcase.PrimitiveStatement#getValue(java.lang.Object)
     */
    /** {@inheritDoc} */
    @Override
    public String getValue() {
        String val;
        try {
            val = xmlMapper.writeValueAsString(this.xmlElement);
        } catch (JsonProcessingException e) {
            val = this.value;
            e.printStackTrace();
        }
        this.value = val; // TODO: probably not needed
        return val;
    }

    /* (non-Javadoc)
     * @see org.evosuite.testcase.PrimitiveStatement#setValue(java.lang.Object)
     */
    /** {@inheritDoc} */
    @Override
    public void setValue(String val) {
        try {
            this.xmlElement = this.xmlMapper.readValue(val, HashMap.class);
            this.value = val; // TODO: probably not needed
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }
    }

    /* (non-Javadoc)
     * @see org.evosuite.testcase.PrimitiveStatement#zero(java.lang.Object)
     */
    /** {@inheritDoc} */
    @Override
    public void zero() {
        this.xmlElement = new HashMap<>();
        this.value = XmlStatement.EMPTY_XML_STRING; // TODO: probably not needed
    }

    /* (non-Javadoc)
     * @see org.evosuite.testcase.StatementInterface#mutate(org.evosuite.testcase.TestCase, org.evosuite.testcase.TestFactory)
     */
    /** {@inheritDoc} */
    @Override
    public boolean mutate(TestCase test, TestFactory factory) {
        HashMap<String, Object> oldVal = this.xmlElement;

        int current = 0;

        // Try GRAMMAR_MUTATION_RETRY_LIMIT number of times when the new value is the same as the old
        while (this.xmlElement.equals(oldVal) && current < Properties.GRAMMAR_MUTATION_RETRY_LIMIT) {
            if (Randomness.nextDouble() <= Properties.RANDOM_PERTURBATION) {
                randomize();
            } else {
                delta();
            }
            current++;
        }

        try {
            this.value = this.xmlMapper.writeValueAsString(this.xmlElement); // TODO: probably not needed
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }
        return true;
    }

    /* (non-Javadoc)
     * @see org.evosuite.testcase.PrimitiveStatement#randomize()
     */
    /** {@inheritDoc} */
    @Override
    public void randomize() {
        HashMap<String, Object> object = new HashMap<>();

        // Add between zero and GRAMMAR_MAX_ELEMENTS number of elements randomly
        int max_entries = Randomness.nextInt(Properties.GRAMMAR_MAX_ELEMENTS);
        for (int i = 0; i <= max_entries; i++) {
            XmlMutator.insertRandomElement(object);
        }
        this.xmlElement = object;
    }

    /* (non-Javadoc)
     * @see org.evosuite.testcase.PrimitiveStatement#delta()
     */
    /** {@inheritDoc} */
    @Override
    public void delta() {
        final double P = 1d / 3d;

        // Delete
        if (Randomness.nextDouble() <= P) {
            XmlMutator.deleteRandomElement(this.xmlElement);
        }

        // Change
        if (Randomness.nextDouble() <= P) {
            XmlMutator.changeRandomElement(this.xmlElement);
        }

        // Insert
        if (Randomness.nextDouble() <= P) {
            XmlMutator.insertRandomElement(this.xmlElement);
        }
    }

    /* (non-Javadoc)
     * @see org.evosuite.testcase.PrimitiveStatement#increment()
     */
    /** {@inheritDoc} */
    @Override
    public void increment() {
        this.delta();;
    }

    /* (non-Javadoc)
     * @see org.evosuite.testcase.StatementInterface#execute(org.evosuite.testcase.execution.Scope, java.io.PrintStream)
     */
    /** {@inheritDoc} */
    @Override
    public Throwable execute(Scope scope, PrintStream out)
            throws InvocationTargetException, IllegalArgumentException,
            IllegalAccessException, InstantiationException {
        try {
            this.value = this.xmlMapper.writeValueAsString(this.xmlElement);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }
        return super.execute(scope, out);
    }

    /* (non-Javadoc)
     * @see org.evosuite.testcase.StatementInterface#copy(org.evosuite.testcase.TestCase, int)
     */
    /** {@inheritDoc} */
    @Override
    public Statement copy(TestCase newTestCase, int offset) {
        try {
            this.value = this.xmlMapper.writeValueAsString(this.xmlElement);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }

        // With GRAMMAR_JSON_INVALID chance return a StringPrimitiveStatement to allow for invalid mutations
        if (Randomness.nextDouble() <= Properties.GRAMMAR_XML_INVALID) {
            return new StringPrimitiveStatement(newTestCase, this.value);
        } else {
            return new XmlStatement(newTestCase, this.value); // TODO: optimize using deep copy
        }
    }

    /* (non-Javadoc)
     * @see org.evosuite.testcase.StatementInterface#equals(java.lang.Object)
     */
    /** {@inheritDoc} */
    @Override
    public boolean equals(Object s) {
        try {
            this.value = this.xmlMapper.writeValueAsString(this.xmlElement);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }
        return super.equals(s);
    }

    /* (non-Javadoc)
     * @see org.evosuite.testcase.StatementInterface#hashCode()
     */
    /** {@inheritDoc} */
    @Override
    public int hashCode() {
        try {
            this.value = this.xmlMapper.writeValueAsString(this.xmlElement);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }
        return super.hashCode();
    }

    /* (non-Javadoc)
     * @see org.evosuite.testcase.StatementInterface#same(org.evosuite.testcase.statements.Statement)
     */
    /** {@inheritDoc} */
    @Override
    public boolean same(Statement s) {
        try {
            this.value = this.xmlMapper.writeValueAsString(this.xmlElement);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }
        return super.same(s);
    }
}
