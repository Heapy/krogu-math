package io.heapy.krogu.math

import kotlin.math.pow
import kotlin.test.*

class FloatingPowerTest {
    @Test fun everyScalingPowerMatchesItsIeeeEncoding() {
        // Entire finite exponent domain used by BigInteger.floatingValue.
        for (exponent in 0..971) {
            val expectedBits = (exponent.toLong() + 1023L) shl 52
            assertEquals(expectedBits, 2.0.pow(exponent).toBits(), "scale 2^$exponent")
        }
    }

    @Test fun integerPowersHaveExactFiniteAndInfinityEncodings() {
        for (exponent in 0..1100) {
            val magnitude = BigInteger.ONE.shiftLeft(exponent)
            val doubleBits = if (exponent <= 1023) (exponent.toLong() + 1023L) shl 52 else 0x7ff0000000000000L
            val floatBits = if (exponent <= 127) (exponent + 127) shl 23 else 0x7f800000
            assertEquals(doubleBits, magnitude.toDouble().toBits(), "double 2^$exponent")
            assertEquals(doubleBits or Long.MIN_VALUE, magnitude.negate().toDouble().toBits(), "double -2^$exponent")
            assertEquals(floatBits, magnitude.toFloat().toBits(), "float 2^$exponent")
            assertEquals(floatBits or Int.MIN_VALUE, magnitude.negate().toFloat().toBits(), "float -2^$exponent")
        }
    }
}
