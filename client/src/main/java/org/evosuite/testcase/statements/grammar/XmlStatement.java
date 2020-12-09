package org.evosuite.testcase.statements.grammar;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;
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
import java.util.HashMap;

public class XmlStatement extends StringPrimitiveStatement {

    private static final long serialVersionUID = 278735526699835887L;

    private transient XmlMapper xmlMapper;
    private transient HashMap xmlElement;

    /**
     * <p>
     * Constructor for XmlStatement.
     * </p>
     *
     * @param tc    a {@link TestCase} object.
     * @param value a {@link String} object.
     */
    public XmlStatement(TestCase tc, String value) throws JsonProcessingException {
        super(tc, value);

        this.xmlMapper = new XmlMapper();
        this.xmlElement = xmlMapper.readValue(value, HashMap.class);
    }

    /**
     * <p>
     * Constructor for XmlStatement.
     * </p>
     *
     * @param tc    a {@link TestCase} object.
     */
    public XmlStatement(TestCase tc) {
        super(tc, "");

        this.xmlMapper = new XmlMapper();
        this.xmlElement = new HashMap();
    }

    /* (non-Javadoc)
     * @see org.evosuite.testcase.PrimitiveStatement#getValue(java.lang.Object)
     */
    /** {@inheritDoc} */
    @Override
    public String getValue() {
        String val = null;
        try {
            val = xmlMapper.writeValueAsString(this.xmlElement);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }
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
        try {
            this.xmlElement = this.xmlMapper.readValue(value, HashMap.class);
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
        this.value = ""; // TODO: not needed
        this.xmlElement = new HashMap();
    }

    /* (non-Javadoc)
     * @see org.evosuite.testcase.StatementInterface#mutate(org.evosuite.testcase.TestCase, org.evosuite.testcase.TestFactory)
     */
    /** {@inheritDoc} */
    @Override
    public boolean mutate(TestCase test, TestFactory factory) {
        HashMap oldVal = this.xmlElement;

        int lim = 4;
        int current = 0;

        while (this.xmlElement.equals(oldVal) && current < lim) {
            if (Randomness.nextDouble() <= Properties.RANDOM_PERTURBATION) {
                randomize();
            } else {
                delta();
            }
            current++;
        }

        try {
            this.value = this.xmlMapper.writeValueAsString(this.xmlElement);
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
        HashMap object = new HashMap();
        int max_entries = Randomness.nextInt(Properties.GRAMMAR_JSON_MAX_ELEMENTS);
        for (int i = 0; i <= max_entries; i++) {
            this.addRandomXmlElement(object);
        }
        this.xmlElement = object;
    }

    private void deleteRandomXmlElement(HashMap object) {
        if (object.size() <= 0) {
            return;
        }

        int index = Randomness.nextInt(object.size());

        int current = 0;
        Object property = null; // TODO: remove null
        for (Object prop: object.keySet()) {
            if (current == index) {
                property = prop;
                break;
            }
            current++;
        }
        object.remove(property);
    }

    private void changeRandomXmlElement(HashMap object) {
        ConstantPool constantPool = ConstantPoolManager.getInstance().getConstantPool();

        if (object.size() <= 0) {
            return;
        }

        int index = Randomness.nextInt(object.size());

        int current = 0;
        Object property = null; // TODO: remove null
        for (Object prop: object.keySet()) {
            if (current == index) {
                property = prop;
                break;
            }
            current++;
        }

        object.remove(property);
        this.addRandomXmlElement(object);
    }

    private void addRandomXmlElement(HashMap object) {
        ConstantPool constantPool = ConstantPoolManager.getInstance().getConstantPool();

        double P1 = Randomness.nextDouble();
        if (P1 <= 1d / 6d) {
            object.put(constantPool.getRandomString(), Randomness.nextBoolean());
        } else if (P1 <= 2d / 6d) {
            object.put(constantPool.getRandomString(), constantPool.getRandomInt());
        } else if (P1 <= 3d / 6d) {
            object.put(constantPool.getRandomString(), constantPool.getRandomLong());
        } else if (P1 <= 4d / 6d) {
            object.put(constantPool.getRandomString(), constantPool.getRandomDouble());
        } else if (P1 <= 5d / 6d) {
            object.put(constantPool.getRandomString(), constantPool.getRandomFloat());
        } else {
            object.put(constantPool.getRandomString(), constantPool.getRandomString());
        }
    }

    /* (non-Javadoc)
     * @see org.evosuite.testcase.PrimitiveStatement#delta()
     */
    /** {@inheritDoc} */
    @Override
    public void delta() {
        // Delete
        if (Randomness.nextDouble() <= Properties.P_TEST_DELETE) {
            this.deleteRandomXmlElement(this.xmlElement);
        }

        // Change
        if (Randomness.nextDouble() <= Properties.P_TEST_CHANGE) {
            this.changeRandomXmlElement(this.xmlElement);
        }

        // Insert
        if (Randomness.nextDouble() <= Properties.P_TEST_INSERT) {
            this.addRandomXmlElement(this.xmlElement);
        }
    }

    /* (non-Javadoc)
     * @see org.evosuite.testcase.PrimitiveStatement#increment()
     */
    /** {@inheritDoc} */
    @Override
    public void increment() {
        return;
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
        try {
            return new XmlStatement(newTestCase, this.value); // TODO: optimize using deep copy
        } catch (JsonProcessingException e) {
            e.printStackTrace();
            return null;
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
