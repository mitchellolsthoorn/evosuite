package org.evosuite.ga.operators.crossover;

import org.evosuite.Properties;
import org.evosuite.testcase.statements.numeric.*;
import org.evosuite.utils.Randomness;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static java.lang.Math.abs;
import static java.lang.Math.pow;

public class SimulatedBinaryCrossOver {
    private static final Logger logger = LoggerFactory.getLogger(SimulatedBinaryCrossOver.class);
    private final static double _contiguity = 2.5;

    public static void crossover(final NumericalPrimitiveStatement s1, final NumericalPrimitiveStatement s2) {
        // TODO: Add Byte and Char
        logger.debug("Old values: " + s1.getValue().toString() + " and " + s2.getValue().toString());
        if (s1 instanceof DoublePrimitiveStatement && s2 instanceof DoublePrimitiveStatement) {
            DoublePrimitiveStatement d1 = (DoublePrimitiveStatement) s1;
            DoublePrimitiveStatement d2 = (DoublePrimitiveStatement) s2;
            final double newValue1 = getNewValue(d1.getValue(), d2.getValue());
            final double newValue2 = getNewValue(d2.getValue(), d1.getValue());
            d1.setValue(newValue1);
            d2.setValue(newValue2);
        } else if (s1 instanceof FloatPrimitiveStatement && s2 instanceof FloatPrimitiveStatement) {
            FloatPrimitiveStatement f1 = (FloatPrimitiveStatement) s1;
            FloatPrimitiveStatement f2 = (FloatPrimitiveStatement) s2;
            final double newValue1 = getNewValue((double) f1.getValue(), (double) f2.getValue());
            final double newValue2 = getNewValue((double) f2.getValue(), (double) f1.getValue());
            f1.setValue((float) newValue1);
            f2.setValue((float) newValue2);
        } else if (s1 instanceof IntPrimitiveStatement && s2 instanceof IntPrimitiveStatement) {
            IntPrimitiveStatement i1 = (IntPrimitiveStatement) s1;
            IntPrimitiveStatement i2 = (IntPrimitiveStatement) s2;
            final double newValue1 = getNewValue(i1.getValue(), i2.getValue());
            final double newValue2 = getNewValue(i2.getValue(), i1.getValue());
            i1.setValue((int) newValue1);
            i2.setValue((int) newValue2);
        } else if (s1 instanceof LongPrimitiveStatement && s2 instanceof LongPrimitiveStatement) {
            LongPrimitiveStatement l1 = (LongPrimitiveStatement) s1;
            LongPrimitiveStatement l2 = (LongPrimitiveStatement) s2;
            final double newValue1 = getNewValue((double) l1.getValue(), (double) l2.getValue());
            final double newValue2 = getNewValue((double) l2.getValue(), (double) l1.getValue());
            l1.setValue((long) newValue1);
            l2.setValue((long) newValue2);
        } else if (s1 instanceof ShortPrimitiveStatement && s2 instanceof ShortPrimitiveStatement) {
            ShortPrimitiveStatement t1 = (ShortPrimitiveStatement) s1;
            ShortPrimitiveStatement t2 = (ShortPrimitiveStatement) s2;
            final double newValue1 = getNewValue((double) t1.getValue(), (double) t2.getValue());
            final double newValue2 = getNewValue((double) t2.getValue(), (double) t1.getValue());
            t1.setValue((short) newValue1);
            t2.setValue((short) newValue2);
        }
        logger.debug("New values: " + s1.getValue().toString() + " and " + s2.getValue().toString());
    }

    protected static double getNewValue(double v1, double v2) {
        final double u = Randomness.nextDouble();
        final double beta;
        if (u < 0.5) {
            // If u is smaller than 0.5 perform a contracting crossover.
            beta = pow(2 * u, 1.0 / (_contiguity + 1));
        } else if (u > 0.5) {
            // Otherwise perform an expanding crossover.
            beta = pow(0.5 / (1.0 - u), 1.0 / (_contiguity + 1));
        } else if (u == 0.5) {
            beta = 1;
        } else {
            beta = 0;
        }

        final double v = Randomness.nextBoolean()
                ? ((v1 - v2) * 0.5) - beta * 0.5 * abs(v1 - v2)
                : ((v1 - v2) * 0.5) + beta * 0.5 * abs(v1 - v2);

        final double min = -1 * Properties.MAX_INT;
        final double max = Properties.MAX_INT;

        return Math.min(max, Math.max(v, min));
    }
}
