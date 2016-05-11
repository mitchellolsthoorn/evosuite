package org.evosuite.mock.javax.swing;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.util.Set;

import org.evosuite.Properties;
import org.evosuite.Properties.Criterion;
import org.evosuite.TestGenerationContext;
import org.evosuite.classpath.ClassPathHandler;
import org.evosuite.coverage.branch.BranchCoverageSuiteFitness;
import org.evosuite.instrumentation.InstrumentingClassLoader;
import org.evosuite.runtime.RuntimeSettings;
import org.evosuite.symbolic.TestCaseBuilder;
import org.evosuite.testcase.TestCase;
import org.evosuite.testcase.TestFitnessFunction;
import org.evosuite.testcase.variable.VariableReference;
import org.evosuite.testsuite.TestSuiteChromosome;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import com.examples.with.different.packagename.mock.javax.swing.ShowInputDialogExample;

public class MockShowInputDialogTest {

	private static final boolean DEFAULT_MOCK_GUI = RuntimeSettings.mockGUI;
	private static final boolean DEFAULT_REPLACE_GUI = Properties.REPLACE_GUI;

	@BeforeClass
	public static void init() {
		String cp = System.getProperty("user.dir") + "/target/test-classes";
		ClassPathHandler.getInstance().addElementToTargetProjectClassPath(cp);
	}

	@Before
	public void setUp() {
		Properties.CRITERION = new Properties.Criterion[] { Criterion.BRANCH };
		Properties.TARGET_CLASS = ShowInputDialogExample.class.getCanonicalName();
		Properties.REPLACE_GUI = true;
		RuntimeSettings.mockGUI = true;
		TestGenerationContext.getInstance().resetContext();
	}

	@After
	public void tearDown() {
		RuntimeSettings.mockGUI = DEFAULT_MOCK_GUI;
		Properties.REPLACE_GUI = DEFAULT_REPLACE_GUI;
		TestGenerationContext.getInstance().resetContext();
	}

	@Test
	public void testShowOptionDialog() throws Exception {
		TestSuiteChromosome suite = new TestSuiteChromosome();
		InstrumentingClassLoader cl = new InstrumentingClassLoader();
		TestCase t1 = buildTestCase0(cl);
		suite.addTest(t1);

		BranchCoverageSuiteFitness ff = new BranchCoverageSuiteFitness(cl);
		double fitness = ff.getFitness(suite);

		Set<TestFitnessFunction> coveredGoals = suite.getCoveredGoals();
		Assert.assertEquals(7, coveredGoals.size());
	}

	private static TestCase buildTestCase0(InstrumentingClassLoader cl)
			throws ClassNotFoundException, NoSuchMethodException, SecurityException {
		TestCaseBuilder builder = new TestCaseBuilder();

		Class<?> clazz = cl.loadClass(ShowInputDialogExample.class.getCanonicalName());
		Constructor<?> constructor = clazz.getConstructor();
		VariableReference showMessageDialogExample0 = builder.appendConstructor(constructor);

		Method showInputDialogsMethod = clazz.getMethod("showInputDialogs");
		VariableReference int0 = builder.appendMethod(showMessageDialogExample0, showInputDialogsMethod);

		return builder.getDefaultTestCase();
	}

}
