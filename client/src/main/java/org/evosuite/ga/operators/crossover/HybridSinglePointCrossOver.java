package org.evosuite.ga.operators.crossover;

import org.evosuite.Properties;
import org.evosuite.ga.Chromosome;
import org.evosuite.ga.ConstructionFailedException;
import org.evosuite.testcase.TestCase;
import org.evosuite.testcase.TestChromosome;
import org.evosuite.testcase.statements.ConstructorStatement;
import org.evosuite.testcase.statements.MethodStatement;
import org.evosuite.testcase.statements.Statement;
import org.evosuite.testcase.statements.StringPrimitiveStatement;
import org.evosuite.testcase.statements.numeric.NumericalPrimitiveStatement;
import org.evosuite.testcase.variable.VariableReference;
import org.evosuite.utils.Randomness;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class HybridSinglePointCrossOver extends CrossOverFunction<TestChromosome> {

    private static final Logger logger = LoggerFactory.getLogger(HybridSinglePointCrossOver.class);

    SinglePointCrossOver<TestChromosome> structureCrossover = new SinglePointCrossOver<>();

    /**
     * Replace parents with crossed over individuals
     *
     * @param parent1 a {@link Chromosome} object.
     * @param parent2 a {@link Chromosome} object.
     * @throws ConstructionFailedException if any.
     */
    @Override
    public void crossOver(TestChromosome parent1, TestChromosome parent2) throws ConstructionFailedException {
        this.structureCrossover.crossOver(parent1, parent2);

        if (Randomness.nextDouble() <= Properties.DATA_CROSSOVER_PROP) {
            TestCase p1 = parent1.getTestCase();
            TestCase p2 = parent2.getTestCase();

            HashMap<String, List<ConstructorStatement>> constructorMatches1 = new HashMap<>();
            HashMap<String, List<ConstructorStatement>> constructorMatches2 = new HashMap<>();
            HashMap<String, List<MethodStatement>> methodMatches1 = new HashMap<>();
            HashMap<String, List<MethodStatement>> methodMatches2 = new HashMap<>();

            for (int i = 0; i < p1.size(); i++) {
                for (int j = 0; j < p2.size(); j++) {
                    Statement s1 = p1.getStatement(i);
                    Statement s2 = p2.getStatement(j);

                    if (s1 instanceof ConstructorStatement && s2 instanceof ConstructorStatement) {
                        ConstructorStatement c1 = (ConstructorStatement) s1;
                        ConstructorStatement c2 = (ConstructorStatement) s2;

                        if (this.isConstructorCompatible(c1, c2)) {
                            storeConstructorCompatibilityInformation(constructorMatches1, c1);
                            storeConstructorCompatibilityInformation(constructorMatches2, c2);
                        }
                    } else if (s1 instanceof MethodStatement && s2 instanceof MethodStatement) {
                        MethodStatement m1 = (MethodStatement) s1;
                        MethodStatement m2 = (MethodStatement) s2;

                        if (this.isMethodCompatible(m1, m2)) {
                            storeMethodCompatibilityInformation(methodMatches1, m1);
                            storeMethodCompatibilityInformation(methodMatches2, m2);
                        }
                    }
                }
            }

//            logger.error("Matches");
//            logger.error(constructorMatches1.toString());
//            logger.error(constructorMatches2.toString());
//            logger.error(methodMatches1.toString());
//            logger.error(methodMatches2.toString());

            if (constructorMatches1.size() > 0 && constructorMatches2.size() > 0) {
                for (String signature : constructorMatches1.keySet()){
                    List<ConstructorStatement> l1 = constructorMatches1.get(signature);
                    List<ConstructorStatement> l2 = constructorMatches2.get(signature);

                    ConstructorStatement i1 = Randomness.choice(l1);
                    ConstructorStatement i2 = Randomness.choice(l2);

                    assert i1 != null;
                    assert i2 != null;
                    crossoverGene(p1, p2, i1.getParameterReferences(), i2.getParameterReferences());
                }
            }

            if (methodMatches1.size() > 0 && methodMatches2.size() > 0) {
                for (String signature : methodMatches1.keySet()) {
                    List<MethodStatement> l1 = methodMatches1.get(signature);
                    List<MethodStatement> l2 = methodMatches2.get(signature);

                    MethodStatement i1 = Randomness.choice(l1);
                    MethodStatement i2 = Randomness.choice(l2);

                    assert i1 != null;
                    assert i2 != null;
                    crossoverGene(p1, p2, i1.getParameterReferences(), i2.getParameterReferences());
                }
            }
        }
    }

    protected boolean isConstructorCompatible(ConstructorStatement s1, ConstructorStatement s2) {
        if (!s1.toString().equals(s2.toString())) {
            logger.debug("Incompatible constructor");
            return false;
        }

        logger.debug("Compatible constructor");
        return true;
    }

    protected boolean isMethodCompatible(MethodStatement s1, MethodStatement s2) {
        if (!s1.getDeclaringClassName().equals(s2.getDeclaringClassName())) {
            logger.debug("Incompatible method");
            return false;
        }

        if (!s1.toString().equals(s2.toString())) {
            logger.debug("Incompatible method");
            return false;
        }

        logger.debug("Compatible method");
        return true;
    }

    protected void storeConstructorCompatibilityInformation(HashMap<String, List<ConstructorStatement>> map, ConstructorStatement statement) {
        if (map.containsKey(statement.toString())) {
            List<ConstructorStatement> list = map.get(statement.toString());
            list.add(statement);
        } else {
            List<ConstructorStatement> list = new ArrayList<>();
            list.add(statement);
            map.put(statement.toString(), list);
        }
    }

    protected void storeMethodCompatibilityInformation(HashMap<String, List<MethodStatement>> map, MethodStatement statement) {
        String methodSignature = statement.getDeclaringClassName() + "|" + statement.toString();
        if (map.containsKey(methodSignature)) {
            List<MethodStatement> list = map.get(methodSignature);
            list.add(statement);
        } else {
            List<MethodStatement> list = new ArrayList<>();
            list.add(statement);
            map.put(methodSignature, list);
        }
    }

    protected void crossoverGene(TestCase p1, TestCase p2, List<VariableReference> parameterReferences1, List<VariableReference> parameterReferences2) {
        logger.debug("Perform gene crossover");
        for (int i = 0; i < parameterReferences1.size(); i++) {
            Statement param1 = p1.getStatement(parameterReferences1.get(i).getStPosition());
            Statement param2 = p2.getStatement(parameterReferences2.get(i).getStPosition());

            if (param1 instanceof StringPrimitiveStatement && param2 instanceof StringPrimitiveStatement) {
                logger.debug("Perform string crossover");
                this.crossoverString((StringPrimitiveStatement) param1, (StringPrimitiveStatement) param2);
            } else if (param1 instanceof NumericalPrimitiveStatement && param2 instanceof NumericalPrimitiveStatement) {
                logger.debug("Perform numerical crossover");
                SimulatedBinaryCrossOver.crossover((NumericalPrimitiveStatement) param1, (NumericalPrimitiveStatement) param2);
            } else {
                logger.debug("Unsupported crossover type");
            }
        }
    }

    protected void crossoverString(StringPrimitiveStatement s1, StringPrimitiveStatement s2) {
        logger.debug("Old values: " + s1.getValue() + " and " + s2.getValue());

        int length1 = s1.getValue().length();
        int length2 = s2.getValue().length();
        if (length1 > 0 && length2 > 0) {
            int position1 = Randomness.nextInt(length1);
            int position2 = Randomness.nextInt(length2);
            logger.debug("position1: " + position1);
            logger.debug("position2: " + position2);

            String newValue1 = s1.getValue().substring(0, position1) + s2.getValue().substring(position2);
            String newValue2 = s2.getValue().substring(0, position2) + s1.getValue().substring(position1);
            s1.setValue(newValue1);
            s2.setValue(newValue2);
        } else {
            logger.debug("One of the string parameters is empty");
            if (length1 == 0 && length2 > 0){
                int position = Randomness.nextInt(length2);
                String newValue1 = s2.getValue().substring(0, position);
                String newValue2 =  s2.getValue().substring(position);
                s1.setValue(newValue1);
                s2.setValue(newValue2);
            } else if (length1 > 0 && length2 == 0){
                int position = Randomness.nextInt(length1);
                String newValue1 = s1.getValue().substring(0, position);
                String newValue2 =  s1.getValue().substring(position);
                s1.setValue(newValue1);
                s2.setValue(newValue2);
            }
        }

        logger.debug("New values: " + s1.getValue() + " and " + s2.getValue());
    }
}
