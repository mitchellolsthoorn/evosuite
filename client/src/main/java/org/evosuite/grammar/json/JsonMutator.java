package org.evosuite.grammar.json;

import com.google.gson.Gson;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonToken;
import com.google.gson.stream.MalformedJsonException;
import org.evosuite.Properties;
import org.evosuite.ga.Chromosome;
import org.evosuite.seeding.ConstantPool;
import org.evosuite.seeding.ConstantPoolManager;
import org.evosuite.seeding.DynamicConstantPool;
import org.evosuite.testcase.TestCase;
import org.evosuite.testcase.TestChromosome;
import org.evosuite.testcase.statements.Statement;
import org.evosuite.testcase.statements.StringPrimitiveStatement;
import org.evosuite.utils.Randomness;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Random;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.google.gson.stream.JsonToken.END_DOCUMENT;

public class JsonMutator<T extends Chromosome> {

    private static final Logger logger = LoggerFactory.getLogger(JsonMutator.class);

    private static Random r = new Random();

    private static Pattern stringPattern = Pattern.compile("\"[a-zA-Z0-9]+\"");

    private static com.natpryce.snodge.JsonMutator mutator = new com.natpryce.snodge.JsonMutator();

    private static ConstantPool dynamicConstantPool = ConstantPoolManager.getInstance().getConstantPool();

    public void mutate(T chromosome) {
        TestChromosome testChromosome = (TestChromosome) chromosome;
        TestCase testCase = testChromosome.getTestCase();

        double count = this.stringPrimitiveCount(testCase);

        // For each test case in the test suite mutate the strings
        for (Statement statement : testCase) {

            // Only apply the mutation on the String primitives
            if (statement instanceof StringPrimitiveStatement) {
                StringPrimitiveStatement stringStatement = (StringPrimitiveStatement) statement;

                // Only apply the mutation with a probability of FUZZER_PROBABILITY
                if (r.nextDouble() <= 1.0 / count) {

                    // Fuzzer seed
                    String seed;

                    if (this.isValidStupid(stringStatement.getValue())) {
                        seed = stringStatement.getValue();

                        int rounds = r.nextInt(Properties.FUZZER_MAX_MUTATION_ROUNDS);
                        for (int i = 0; i <= rounds; i++) {
                            Optional<String> strings = mutator.forStrings().mutate(seed, 1).findFirst();
                            if (strings.isPresent()) {
                                seed = strings.get();
                                String somethingNew = seed.replace("\"[a-zA-Z0-9].*\"", "\"" + dynamicConstantPool.getRandomString() + "\"");

                                if (somethingNew != null) {
                                    seed = somethingNew;
                                }
                            }
                        }

                        // Replace input with mutated input
                        stringStatement.setValue(seed);
                    }
                }
            }
        }
    }

    public void inject(T chromosome) {
        TestChromosome testChromosome = (TestChromosome) chromosome;
        TestCase testCase = testChromosome.getTestCase();

        double count = this.stringPrimitiveCount(testCase);

        Random r = new Random();

        // For each test case in the test suite mutate the strings
        for (Statement statement : testCase) {

            // Only apply the mutation on the String primitives
            if (statement instanceof StringPrimitiveStatement) {
                StringPrimitiveStatement stringStatement = (StringPrimitiveStatement) statement;

                // Only apply the mutation with a probability of FUZZER_PROBABILITY
                if (r.nextDouble() <= 1.0 / count) {
                    // Replace input with mutated input

                    int seedIndex = r.nextInt(Properties.FUZZER_SEED.length);
                    stringStatement.setValue(Properties.FUZZER_SEED[seedIndex]);
                }
            }
        }
    }

    protected boolean isValid(String input) {
        Gson gson = new Gson();
        try {
            gson.fromJson(input, Object.class);
            return true;
        } catch (com.google.gson.JsonSyntaxException ex) {
            return false;
        }
    }

    protected boolean isValidStupid(String input) {
        try {
            JsonReader jsonReader = new JsonReader(new StringReader(input));
            JsonToken token;
            loop:
            while ((token = jsonReader.peek()) != END_DOCUMENT && token != null) {
                switch (token) {
                    case BEGIN_ARRAY:
                        jsonReader.beginArray();
                        break;
                    case END_ARRAY:
                        jsonReader.endArray();
                        break;
                    case BEGIN_OBJECT:
                        jsonReader.beginObject();
                        break;
                    case END_OBJECT:
                        jsonReader.endObject();
                        break;
                    case NAME:
                        jsonReader.nextName();
                        break;
                    case STRING:
                    case NUMBER:
                    case BOOLEAN:
                    case NULL:
                        jsonReader.skipValue();
                        break;
                    case END_DOCUMENT:
                        break loop;
                    default:
                        throw new AssertionError(token);
                }
            }
            return true;
        } catch (MalformedJsonException e) {
            return false;
        } catch (IOException e) {
            return false;
        }
    }

    protected int stringPrimitiveCount(TestCase testCase) {
        int count = 0;

        for (Statement statement : testCase) {
            // Only apply the mutation on the String primitives
            if (statement instanceof StringPrimitiveStatement) {
                count++;
            }
        }

        return count;
    }
}
