package io.heapy.krogu.math

import kotlin.test.Test
import kotlin.test.assertEquals

/** Compare the frozen portable expectations with the live public reference, then the port. */
class CanonicalDoubleConformanceTest {
    @Test fun livePublicBoundaryOracle() {
        assertEquals("BellSoft", System.getProperty("java.vendor"))
        assertEquals("25.0.4+9-LTS", System.getProperty("java.runtime.version"))
        var count = 0
        for (row in canonicalDoubleRows()) {
            val fields = row.split('|')
            val value = Double.fromBits(fields[0].toLong())
            val expected = java.math.BigDecimal.valueOf(value)
            assertEquals(fields[1], expected.unscaledValue().toString(), row)
            assertEquals(fields[2].toInt(), expected.scale(), row)
            assertEquals(fields[3], java.lang.Double.toString(value), row)
            val actual = BigDecimal.valueOf(value)
            assertEquals(expected.unscaledValue().toString(), actual.unscaledValue().toString(), row)
            assertEquals(expected.scale(), actual.scale(), row)
            count++
        }
        assertEquals(CANONICAL_DOUBLE_CASES, count)
    }
}
