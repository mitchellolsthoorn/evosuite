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
import java.util.ArrayList;
import java.util.HashMap;

public class XmlStatement extends StringPrimitiveStatement {

    private static final long serialVersionUID = 278735526699835887L;

    private transient XmlMapper xmlMapper;
    private transient HashMap<String, Object> xmlElement;

    public static final String EMPTY_XML_STRING = "<HashMap></HashMap>";

    /**
     * <p>
     * Constructor for XmlStatement.
     * </p>
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
     * <p>
     * Constructor for XmlStatement.
     * </p>
     *
     * @param tc    a {@link TestCase} object.
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
        this.value = XmlStatement.EMPTY_XML_STRING; // TODO: not needed
        this.xmlElement = new HashMap<>();
    }

    /* (non-Javadoc)
     * @see org.evosuite.testcase.StatementInterface#mutate(org.evosuite.testcase.TestCase, org.evosuite.testcase.TestFactory)
     */
    /** {@inheritDoc} */
    @Override
    public boolean mutate(TestCase test, TestFactory factory) {
        HashMap<String, Object> oldVal = this.xmlElement;

        int current = 0;

        while (this.xmlElement.equals(oldVal) && current < Properties.GRAMMAR_MUTATION_RETRY_LIMIT) {
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
        HashMap<String, Object> object = new HashMap<>();
        int max_entries = Randomness.nextInt(Properties.GRAMMAR_MAX_ELEMENTS);
        for (int i = 0; i <= max_entries; i++) {
            this.addRandomElement(object);
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
            this.deleteRandomElement(this.xmlElement);
        }

        // Change
        if (Randomness.nextDouble() <= P) {
            this.changeRandomElement(this.xmlElement);
        }

        // Insert
        if (Randomness.nextDouble() <= P) {
            this.addRandomElement(this.xmlElement);
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

    private HashMap<String, Object> getCandidate(HashMap<String, Object> object) {
        ArrayList<HashMap<String, Object>> candidates = new ArrayList<>();

        if (object != null) {
            for (String property : object.keySet()) {
                Object element = object.get(property);
                if (element instanceof HashMap) {
                    HashMap<String, Object> map = (HashMap<String, Object>) element;
                    candidates.add(map);
                }
            }
            return Randomness.choice(candidates);
        }
        return null;
    }

    private Object getRandomPrimitive() {
        ConstantPool constantPool = ConstantPoolManager.getInstance().getConstantPool();

        double P = Randomness.nextDouble();
        if (P <= 1d / 6d) {
            return Randomness.nextBoolean();
        } else if (P <= 2d / 6d) {
            return constantPool.getRandomInt();
        } else if (P <= 3d / 6d) {
            return constantPool.getRandomLong();
        } else if (P <= 4d / 6d) {
            return constantPool.getRandomDouble();
        } else if (P <= 5d / 6d) {
            return constantPool.getRandomFloat();
        } else {
            return constantPool.getRandomString();
        }
        // TODO: Maybe add Null element
    }

    private void addRandomElement(HashMap<String, Object> object) {
        ConstantPool constantPool = ConstantPoolManager.getInstance().getConstantPool();

        if (Randomness.nextDouble() <= Properties.GRAMMAR_JSON_NESTED) { // Add second layer elements
            HashMap<String, Object> candidate = this.getCandidate(object);
            if (candidate != null) {
                candidate.put(constantPool.getRandomString(), this.getRandomPrimitive());
                return;
            }
        }

        if (Randomness.nextDouble() <= Properties.GRAMMAR_JSON_NULL) {
            object.put(constantPool.getRandomString(), null);
        } else if (Randomness.nextDouble() <= Properties.GRAMMAR_JSON_PRIMITIVE) {
            object.put(constantPool.getRandomString(), this.getRandomPrimitive());
        } else {
            object.put(constantPool.getRandomString(), new HashMap<>());
        }
    }

    private void deleteRandomElement(HashMap<String, Object> object) {
        if (object.size() < 1)
            return;

        if (Randomness.nextDouble() <= Properties.GRAMMAR_JSON_NESTED) { // Delete second layer elements
            HashMap<String, Object> candidate = this.getCandidate(object);
            if (candidate != null) {
                if (candidate.size() > 0) {
                    Object candidate_property = Randomness.choice(candidate.keySet());
                    candidate.remove(candidate_property);
                }
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

    private Object changeRandomPrimitiveElement(Object element) {
        if (element instanceof Boolean) {
            Boolean bool = (Boolean) element;
            return !bool;
        }

        if (element instanceof Integer) {
            Integer number = (Integer) element;
            int delta = (int) Math.floor(Randomness.nextGaussian() * Properties.MAX_DELTA);
            return number + delta;
        }

        if (element instanceof Long) {
            Long number = (Long) element;
            long delta = (long) Math.floor(Randomness.nextGaussian() * Properties.MAX_DELTA);
            return number + delta;
        }

        if (element instanceof Double) {
            Double number = (Double) element;
            double delta = (double) Math.floor(Randomness.nextGaussian() * Properties.MAX_DELTA);
            return number + delta;
        }

        if (element instanceof Float){
            Float number = (Float) element;
            float delta = (float) Math.floor(Randomness.nextGaussian() * Properties.MAX_DELTA);
            return number + delta;
        }

        if (element instanceof String) {
            String s = (String) element;
            return this.mutateString(s);
        }

        return element;
    }

    private void changeRandomElement(HashMap<String, Object> object) {
        ConstantPool constantPool = ConstantPoolManager.getInstance().getConstantPool();

        if (object.size() < 1)
            return;

        String property = Randomness.choice(object.keySet());
        Object element = object.get(property);

        double P = Randomness.nextDouble();
        if (P <= Properties.GRAMMAR_JSON_PROPERTY) { // Change property name
            object.remove(property);
            String newProperty = this.mutateString(property);
            object.put(newProperty, element);
        } else { // Change property value
            if (element == null) {
                object.put(property, this.getRandomPrimitive());
                // TODO: Maybe add array or object elements
            } else if (element instanceof HashMap) {
                HashMap<String, Object> object2 = (HashMap<String, Object>) element;
                if (object2.size() > 0) {
                    String property2 = Randomness.choice(object2.keySet());
                    Object element2 = object2.get(property2);
                    object.put(property, this.changeRandomPrimitiveElement(element2));
                }
            } else {
                object.put(property, this.changeRandomPrimitiveElement(element));
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
        return new XmlStatement(newTestCase, this.value); // TODO: optimize using deep copy
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
