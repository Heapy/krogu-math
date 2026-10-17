package io.heapy.krogu.math

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertNotEquals

class CanonicalDoubleTest {
    @Test fun asymmetricPowerBoundaryHasTheShortestPermittedCoefficient() {
        for (sign in listOf(0L, Long.MIN_VALUE)) {
            val value = Double.fromBits(0x0060000000000000L or sign)
            val actual = BigDecimal.valueOf(value)
            assertEquals(if (sign == 0L) "7120236347223045" else "-7120236347223045", actual.unscaledValue().toString())
            assertEquals(322, actual.scale())
        }
    }

    @Test fun portableBoundaryCases00() = checkPortableCases(0)
    @Test fun portableBoundaryCases01() = checkPortableCases(1)
    @Test fun portableBoundaryCases02() = checkPortableCases(2)
    @Test fun portableBoundaryCases03() = checkPortableCases(3)
    @Test fun portableBoundaryCases04() = checkPortableCases(4)
    @Test fun portableBoundaryCases05() = checkPortableCases(5)
    @Test fun portableBoundaryCases06() = checkPortableCases(6)
    @Test fun portableBoundaryCases07() = checkPortableCases(7)
    @Test fun portableBoundaryCases08() = checkPortableCases(8)
    @Test fun portableBoundaryCases09() = checkPortableCases(9)
    @Test fun portableBoundaryCases10() = checkPortableCases(10)
    @Test fun portableBoundaryCases11() = checkPortableCases(11)
    @Test fun portableBoundaryCases12() = checkPortableCases(12)
    @Test fun portableBoundaryCases13() = checkPortableCases(13)
    @Test fun portableBoundaryCases14() = checkPortableCases(14)
    @Test fun portableBoundaryCases15() = checkPortableCases(15)
    @Test fun portableBoundaryCases16() = checkPortableCases(16)
    @Test fun portableBoundaryCases17() = checkPortableCases(17)
    @Test fun portableBoundaryCases18() = checkPortableCases(18)
    @Test fun portableBoundaryCases19() = checkPortableCases(19)
    @Test fun portableBoundaryCases20() = checkPortableCases(20)
    @Test fun portableBoundaryCases21() = checkPortableCases(21)
    @Test fun portableBoundaryCases22() = checkPortableCases(22)
    @Test fun portableBoundaryCases23() = checkPortableCases(23)
    @Test fun portableBoundaryCases24() = checkPortableCases(24)
    @Test fun portableBoundaryCases25() = checkPortableCases(25)
    @Test fun portableBoundaryCases26() = checkPortableCases(26)
    @Test fun portableBoundaryCases27() = checkPortableCases(27)
    @Test fun portableBoundaryCases28() = checkPortableCases(28)
    @Test fun portableBoundaryCases29() = checkPortableCases(29)
    @Test fun portableBoundaryCases30() = checkPortableCases(30)
    @Test fun portableBoundaryCases31() = checkPortableCases(31)

    // Keep every frozen row and assertion, spread evenly across bounded test methods.
    // Each index belongs to exactly one residue class; the per-part count proves coverage.
    private fun checkPortableCases(part: Int) {
        var count = 0
        for ((index, row) in canonicalDoubleRows().withIndex()) {
            if (index % 32 != part) continue
            val fields = row.split('|')
            val value = Double.fromBits(fields[0].toLong())
            val actual = BigDecimal.valueOf(value)
            assertEquals(fields[1], actual.unscaledValue().toString(), "coefficient bits=${fields[0]}")
            assertEquals(fields[2].toInt(), actual.scale(), "scale bits=${fields[0]}")
            // Negative zero is lost by BigDecimal, but its canonical coefficient/scale is retained.
            assertEquals(if (value == 0.0) "0.0" else fields[3], decimalDoubleString(value), "text bits=${fields[0]}")
            count++
        }
        assertEquals((CANONICAL_DOUBLE_CASES + 31 - part) / 32, count)
    }

    @Test fun zeroNonfiniteAndMinimumLengthRules() {
        for (value in listOf(0.0, -0.0)) {
            assertEquals("0", BigDecimal.valueOf(value).unscaledValue().toString())
            assertEquals(1, BigDecimal.valueOf(value).scale())
        }
        for (bits in listOf(0x7ff0000000000000L, -0x0010000000000000L, 0x7ff8000000000000L, 0x7ff0000000000001L)) {
            assertFailsWith<NumberFormatException> { BigDecimal.valueOf(Double.fromBits(bits)) }
        }
        assertEquals("4.9E-324", decimalDoubleString(Double.fromBits(1)))
        assertEquals("9.9E-324", decimalDoubleString(Double.fromBits(2)))
    }

    @Test fun exactBinaryConstructorRemainsDistinct() {
        val exact = BigDecimal(0.1)
        assertEquals("1000000000000000055511151231257827021181583404541015625", exact.unscaledValue().toString())
        assertEquals(55, exact.scale())
        assertEquals(BigDecimal("0.1"), BigDecimal.valueOf(0.1))
        assertNotEquals(exact, BigDecimal.valueOf(0.1))
    }
}
