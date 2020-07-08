package org.evosuite.grammar.json;

import com.google.gson.Gson;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonToken;
import com.google.gson.stream.MalformedJsonException;
import org.evosuite.Properties;
import org.evosuite.ga.Chromosome;
import org.evosuite.seeding.ConstantPool;
import org.evosuite.seeding.ConstantPoolManager;
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
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.google.gson.stream.JsonToken.END_DOCUMENT;

public class JsonMutator<T extends Chromosome> {

    private static final Logger logger = LoggerFactory.getLogger(JsonMutator.class);

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
                if (Randomness.nextDouble() <= 1.0 / count) {

                    // Fuzzer seed
                    String seed;

                    if (this.isValidStupid(stringStatement.getValue())) {
                        seed = stringStatement.getValue();

                        int rounds = r.nextInt(Properties.FUZZER_MAX_MUTATION_ROUNDS);
                        for (int i = 0; i <= rounds; i++) {
                            Optional<String> strings = mutator.forStrings().mutate(seed, 1).findFirst();
                            if (strings.isPresent()) {
                                seed = strings.get();
                            }
                        }

                        //logger.error("Before = {}", seed);
                        String somethingNew = replaceString(seed);

                        //logger.error("After = " + somethingNew);
                        if (somethingNew != null) {
                            seed = somethingNew;
                        }

                        // Replace input with mutated input
                        stringStatement.setValue(seed);
                    }
                }
            }
        }
    }

    protected static String generateRandomJson(){
        double prob = Randomness.nextDouble();
        String attribute = "\"" + dynamicConstantPool.getRandomString() + "\"";
        String value;

        if (prob <= 1.0/7.0){
            value = "\"" +  dynamicConstantPool.getRandomString() + "\"";
        } else if (prob <= 2.0/7.0) {
            value = "" + dynamicConstantPool.getRandomDouble();
        } else if (prob <= 3.0/7.0) {
            value = "" + dynamicConstantPool.getRandomFloat();
        } else if (prob <= 4.0/7.0) {
            value = "" + dynamicConstantPool.getRandomInt();
        } else if (prob <= 5.0/7.0) {
            value = "" + dynamicConstantPool.getRandomLong();
        } else if (prob <= 6.0/7.0) {
            value = "[" + dynamicConstantPool.getRandomString() +"]";
        } else {
            value = "" + Randomness.nextBoolean();
        }

        return "{" + attribute + ":" + value + "}";
    }

    protected String replaceString(String string){
        dynamicConstantPool = ConstantPoolManager.getInstance().getConstantPool();

        // if the grammar produces a null or an empty string,
        // we generate random json strings
        if (string == null || string.equals("") ||
                string.equals("null"))
            return generateRandomJson();

        //regex matcher
        Matcher matcher = stringPattern.matcher(string);
        List<String> matches = new ArrayList<>();

        while(matcher.find()) {
            matches.add(matcher.group());
        }


        if (matches.size()>0){
            int index = Randomness.nextInt(matches.size());
            String new_item = "\"" + dynamicConstantPool.getRandomString() + "\"";
            String new_string = string.replace(matches.get(index), new_item);
            return new_string;
        } else {
            return string;
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
                if (Randomness.nextDouble() <= 1.0 / count) {
                    // Replace input with mutated input

                    int seedIndex = Randomness.nextInt(Properties.FUZZER_SEED.length);
                    String seed = Properties.FUZZER_SEED[seedIndex];

                    Optional<String> strings = mutator.forStrings().mutate(seed, 1).findFirst();
                    if (strings.isPresent()) {
                        seed = strings.get();
                    }

                    stringStatement.setValue(seed);
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
        } catch (Exception e) {
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
