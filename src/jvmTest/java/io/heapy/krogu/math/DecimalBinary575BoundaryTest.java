// SPDX-License-Identifier: Apache-2.0
package io.heapy.krogu.math;

import java.util.LinkedHashSet;
import java.util.Set;
import org.junit.Test;
import static org.junit.Assert.*;

public class DecimalBinary575BoundaryTest {
    private static Object exact(BigDecimal source, String operation, BigDecimal operand) {
        return switch (operation) {
            case "add" -> source.add(operand);
            case "subtract" -> source.subtract(operand);
            case "multiply" -> source.multiply(operand);
            case "divide" -> source.divide(operand);
            case "divideToIntegralValue" -> source.divideToIntegralValue(operand);
            case "divideAndRemainder" -> source.divideAndRemainder(operand);
            case "remainder" -> source.remainder(operand);
            default -> throw new AssertionError(operation);
        };
    }
    private static Object contextual(BigDecimal source, String operation, BigDecimal operand, MathContext context) {
        return switch (operation) {
            case "add" -> source.add(operand, context);
            case "subtract" -> source.subtract(operand, context);
            case "multiply" -> source.multiply(operand, context);
            case "divide" -> source.divide(operand, context);
            case "divideToIntegralValue" -> source.divideToIntegralValue(operand, context);
            case "divideAndRemainder" -> source.divideAndRemainder(operand, context);
            case "remainder" -> source.remainder(operand, context);
            default -> throw new AssertionError(operation);
        };
    }
    @Test public void allTwentyOneSlotsRejectNullAtOrdinaryJavaEntry() {
        Set<String> slots = new LinkedHashSet<>();
        BigDecimal source = new BigDecimal("17.25");
        for (String operation : new String[] {"add", "subtract", "multiply", "divide",
                "divideToIntegralValue", "divideAndRemainder", "remainder"}) {
            assertTrue(slots.add(operation + "(BigDecimal)#0"));
            try { exact(source, operation, null); fail(operation); }
            catch (NullPointerException expected) { /* The invoked library method rejected null. */ }
            for (int slot = 0; slot < 2; slot++) {
                assertTrue(slots.add(operation + "(BigDecimal,MathContext)#" + slot));
                for (MathContext context : new MathContext[] {MathContext.UNLIMITED, new MathContext(2)}) {
                    try {
                        contextual(source, operation, slot == 0 ? null : new BigDecimal("5.0"), slot == 1 ? null : context);
                        fail(operation + " slot " + slot);
                    } catch (NullPointerException expected) { /* Actual target exception. */ }
                }
            }
        }
        assertEquals(21, slots.size());
    }
}
