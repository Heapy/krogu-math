// SPDX-License-Identifier: Apache-2.0
package io.heapy.krogu.math

/** Independent binary32 interval selection; see docs/kotlin-floating-conversion-migration.md. */
internal fun decimalFloatString(value: Float): String {
    val raw = value.toBits()
    val bits = raw and Int.MAX_VALUE
    if (bits >= 0x7f800000) throw NumberFormatException("Infinite or NaN")
    // Decimal has no negative zero; both canonical zero strings have scale one.
    if (bits == 0) return "0.0"
    val selected = selectFloatDecimal(bits)
    val digits = selected.unscaledValue().toString()
    val exponent = digits.length - selected.scale() - 1
    val unsigned = if (exponent in -3..6) {
        val plain = selected.toPlainString()
        if ('.' in plain) plain else "$plain.0"
    } else {
        val fraction = if (digits.length == 1) "0" else digits.substring(1)
        digits[0] + "." + fraction + "E" + exponent
    }
    return if (raw < 0) "-$unsigned" else unsigned
}

/** Positive binary32 magnitude in units of 2^-149; also accepts the virtual successor 2^128. */
private fun floatLatticeInteger(bits: Int): BigInteger {
    val field = bits ushr 23
    val fraction = bits and 0x7fffff
    val significand = if (field == 0) fraction else fraction + 0x800000
    return BigInteger.valueOf(significand.toLong()).shiftLeft(if (field == 0) 0 else field - 1)
}

private fun selectFloatDecimal(bits: Int): BigDecimal {
    // All binary32 values are integer multiples of 2^-149; their midpoints
    // are multiples of 2^-150. Multiplication by 5^150 aligns them at scale 150.
    // This decodes bits directly, even on platforms where Float uses a wider carrier.
    val factor = BigInteger.valueOf(5).pow(150)
    val center = floatLatticeInteger(bits)
    val exact = center.shiftLeft(1).multiply(factor)
    val lower = center.add(floatLatticeInteger(bits - 1)).multiply(factor)
    val upper = center.add(floatLatticeInteger(bits + 1)).multiply(factor)
    val closed = bits and 1 == 0
    val exactDigits = exact.toString()
    val exponent = exactDigits.length - 151

    // Nine significant decimal digits suffice for every finite binary32 value.
    // The finest unit has exponent 150 + E - 8 >= 97, so all work is integral.
    val units = buildList {
        var unit = BigInteger.TEN.pow(150 + exponent - 8)
        repeat(9) {
            add(unit)
            unit = unit.multiply(BigInteger.TEN)
        }
    }
    for (budget in 2..9) {
        val first = BigInteger.TEN.pow(budget - 1)
        val last = first.multiply(BigInteger.TEN).subtract(BigInteger.ONE)
        var chosen: BigDecimal? = null
        var distance: BigInteger? = null
        // Budget two represents the union of canonical lengths one and two.
        // Once it fails, connectedness excludes crossing a decimal power.
        for (offset in if (budget == 2) -1..1 else 0..0) {
            val scale = budget - 1 - exponent - offset
            val unit = units[9 - budget + offset]
            val prefixLength = budget - offset
            val floor = BigInteger(exactDigits.take(prefixLength).padEnd(prefixLength, '0'))
            for (unbounded in listOf(floor, floor.add(BigInteger.ONE))) {
                // Clipping retains the closest endpoint in each exponent band,
                // including the last candidate immediately below a decimal carry.
                val coefficient = when {
                    unbounded < first -> first
                    unbounded > last -> last
                    else -> unbounded
                }
                val aligned = coefficient.multiply(unit)
                val low = aligned.compareTo(lower)
                val high = aligned.compareTo(upper)
                if (low < 0 || high > 0 || (!closed && (low == 0 || high == 0))) continue
                val candidate = BigDecimal(coefficient, scale).stripTrailingZeros()
                val delta = exact.subtract(aligned).abs()
                val order = distance?.let { delta.compareTo(it) } ?: -1
                if (order < 0 || (order == 0 && !candidate.unscaledValue().testBit(0))) {
                    chosen = candidate
                    distance = delta
                }
            }
        }
        if (chosen != null) return chosen
    }
    error("No nine-digit binary32 round-trip decimal")
}
