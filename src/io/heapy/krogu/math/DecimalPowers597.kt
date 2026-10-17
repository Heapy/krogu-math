package io.heapy.krogu.math

/** Bounded immutable metadata thresholds; larger exponents retain the full numerical route. */
internal object DecimalPowers597 {
    // Covers the profiled precision/context cohorts with bounded retained storage.
    // At most 129 integers, 129 references and 129*15 radix-2^30 cells, excluding
    // object/array headers. This is a cache bound, never a supported-range cap.
    private const val LAST = 128
    private val powers: Array<BigInteger> = run {
        var value = BigInteger.ONE
        Array(LAST + 1) { exponent ->
            if (exponent != 0) value = value.multiply(BigInteger.TEN)
            value
        }
    }

    fun lookup(exponent: Int): BigInteger? = if (exponent in 0..LAST) powers[exponent] else null
}
