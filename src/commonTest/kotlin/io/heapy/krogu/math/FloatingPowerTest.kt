package io.heapy.krogu.math

import kotlin.math.pow
import kotlin.test.*

class FloatingPowerTest {
    @Test fun everyScalingPowerMatchesItsIeeeEncoding() {
        // Includes integer conversion and decimal conversion down to the least
        // subnormal quantum. Subnormal powers have no implicit leading bit.
        for (exponent in -1074..1023) {
            val expectedBits = if (exponent < -1022) 1L shl (exponent + 1074)
                else (exponent.toLong() + 1023L) shl 52
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
