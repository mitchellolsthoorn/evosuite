package org.evosuite.testcase.mutators.grammar;

import org.evosuite.Properties;
import org.evosuite.seeding.ConstantPool;
import org.evosuite.seeding.ConstantPoolManager;
import org.evosuite.testcase.mutators.StringMutator;
import org.evosuite.utils.Randomness;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * Mutator class for mutating XML structures.
 *
 * @author Mitchell Olsthoorn
 */
public class XmlMutator {

    /**
     * Delete random element from the XML structure.
     *
     * @param object the XML structure to remove the element from.
     */
    public static void deleteRandomElement(HashMap<String, Object> object) {
        if (object.size() < 1)
            return;

        // With GRAMMAR_XML_NESTED chance remove an element from the second layer
        if (Randomness.nextDouble() <= Properties.GRAMMAR_XML_NESTED) {
            HashMap<String, Object> candidate = XmlMutator.getRandomCandidate(object);
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

    /**
     * Change random element from the XML structure.
     *
     * @param object the XML structure to change.
     */
    public static void changeRandomElement(HashMap<String, Object> object) {
        if (object.size() < 1)
            return;

        String property = Randomness.choice(object.keySet());
        Object element = object.get(property);

        double P = Randomness.nextDouble();
        if (P <= Properties.GRAMMAR_XML_PROPERTY) { // Change property name
            object.remove(property);
            String newProperty = StringMutator.mutateString(property);
            object.put(newProperty, element);
        } else { // Change property value
            if (element == null) {
                object.put(property, XmlMutator.getRandomPrimitive());
                // TODO: Maybe add array or object elements
            } else if (element instanceof HashMap) {
                HashMap<String, Object> object2 = (HashMap<String, Object>) element;
                if (object2.size() > 0) {
                    String property2 = Randomness.choice(object2.keySet());
                    Object element2 = object2.get(property2);
                    object.put(property, XmlMutator.changePrimitiveElement(element2));
                }
            } else {
                object.put(property, XmlMutator.changePrimitiveElement(element));
            }
        }
    }

    /**
     * Change primitive element randomly.
     *
     * @param element the primitive object to change.
     * @return the changed primitive object.
     */
    protected static Object changePrimitiveElement(Object element) {
        // TODO: Put these in their own mutators

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
            return StringMutator.mutateString(s);
        }

        return element;
    }

    /**
     * Insert random element into the XML structure.
     *
     * @param object the XML structure to insert the element in.
     */
    public static void insertRandomElement(HashMap<String, Object> object) {
        ConstantPool constantPool = ConstantPoolManager.getInstance().getConstantPool();

        // With GRAMMAR_JSON_NESTED chance remove an element from the second layer
        if (Randomness.nextDouble() <= Properties.GRAMMAR_XML_NESTED) {
            HashMap<String, Object> candidate = XmlMutator.getRandomCandidate(object);
            if (candidate != null) {
                candidate.put(constantPool.getRandomString(), XmlMutator.getRandomPrimitive());
                return;
            }
        }

        if (Randomness.nextDouble() <= Properties.GRAMMAR_XML_NULL) {
            object.put(constantPool.getRandomString(), null);
        } else if (Randomness.nextDouble() <= Properties.GRAMMAR_XML_PRIMITIVE) {
            object.put(constantPool.getRandomString(), XmlMutator.getRandomPrimitive());
        } else {
            object.put(constantPool.getRandomString(), new HashMap<>());
        }
    }

    /**
     * Select a random candidate from the XML structure that is a HashMap.
     *
     * @param object the XML structure to select the candidate from.
     * @return a XML object.
     */
    protected static HashMap<String, Object> getRandomCandidate(HashMap<String, Object> object) {
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

    /**
     * Get a new random primitive.
     *
     * @return a primitive object.
     */
    protected static Object getRandomPrimitive() {
        ConstantPool constantPool = ConstantPoolManager.getInstance().getConstantPool();

        double P = Randomness.nextDouble();
        // TODO: Maybe add Null element
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
    }
}
