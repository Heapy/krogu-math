package io.heapy.krogu.math

/** Platform-independent shortest round-trip representation with Java's decimal notation thresholds. */
internal fun decimalDoubleString(value: Double): String {
    if (!value.isFinite()) throw NumberFormatException("Infinite or NaN")
    if (value == 0.0) return "0.0"
    val magnitude = if (value < 0) -value else value
    val stripped = shortestDoubleDecimal(magnitude).stripTrailingZeros()
    val exponent = stripped.precision().toLong() - stripped.scale() - 1
    if (exponent in -3..6) {
        val plain = stripped.toPlainString()
        val text = if ('.' in plain) plain else "$plain.0"
        return (if (value < 0) "-" else "") + text
    }
    val digits = stripped.unscaledValue().abs().toString()
    val mantissa = digits[0] + "." + (if (digits.length == 1) "0" else digits.substring(1))
    return (if (value < 0) "-" else "") + mantissa + "E" + exponent
}

/** Independent grid selection; the sufficiency argument is in docs/canonical-double-contract.md. */
private fun shortestDoubleDecimal(magnitude: Double): BigDecimal {
    val exact = BigDecimal(magnitude)
    val exactDigits = exact.unscaledValue().toString()
    val exponent = exactDigits.length - exact.scale() - 1
    val bits = magnitude.toBits()
    // Construct the binary round-to-nearest interval once, using exact decimals.
    // At the largest finite value, the virtual successor is one ulp above it;
    // the midpoint is the finite/infinity rounding threshold.
    val previous = BigDecimal(Double.fromBits(bits - 1))
    val next = Double.fromBits(bits + 1)
    val successor = if (next.isFinite()) BigDecimal(next) else null
    val includesMidpoints = bits and 1L == 0L
    // Every searched grid has scale <= 16-exponent (including budget-two
    // adjacent bands). Align once, then test membership and distance with integers.
    // The extra decimal place makes each aligned coefficient even, so halving
    // midpoint sums/differences is an exact bit shift, with no decimal division.
    val commonScale = maxOf(maxOf(exact.scale(), previous.scale(), successor?.scale() ?: 0), 16 - exponent) + 1
    val exactCoefficient = exact.setScale(commonScale).unscaledValue()
    val previousCoefficient = previous.setScale(commonScale).unscaledValue()
    val lowerCoefficient = exactCoefficient.add(previousCoefficient).shiftRight(1)
    val upperCoefficient = if (successor != null) {
        exactCoefficient.add(successor.setScale(commonScale).unscaledValue()).shiftRight(1)
    } else exactCoefficient.add(exactCoefficient.subtract(previousCoefficient).shiftRight(1))
    // Start at the finest required grid, then multiply by ten for coarser ones.
    // This avoids repeated large powers and large-quotient divisions by ten.
    val units = buildList {
        var unit = BigInteger.TEN.pow(commonScale + exponent - 16)
        repeat(17) {
            add(unit)
            unit = unit.multiply(BigInteger.TEN)
        }
    }
    for (digits in 2..17) {
        val minimum = BigInteger.TEN.pow(digits - 1)
        val maximum = minimum.multiply(BigInteger.TEN).subtract(BigInteger.ONE)
        var best: BigDecimal? = null
        var bestDistance: BigInteger? = null
        // Budget two includes canonical lengths one AND two. The broad subnormal
        // interval can cross a decimal decade, so include adjacent exponent bands.
        // If this budget fails, no power of ten is in the connected interval;
        // all subsequent admissible decimals are in the exact value's band.
        val offsets = if (digits == 2) -1..1 else 0..0
        for (offset in offsets) {
            val scale = digits - 1 - exponent - offset
            val unit = units[17 - digits + offset]
            // Dividing by a decimal power takes the leading digits, or pads
            // with zeros when the exact decimal already has fewer digits.
            val length = digits - offset
            val floor = BigInteger(exactDigits.take(length).padEnd(length, '0'))
            for (coefficient in listOf(floor, floor.add(BigInteger.ONE))) {
                // Clipping includes the last point just below a decimal carry,
                // even when both ordinary bracketing points are outside that band.
                val bounded = when {
                    coefficient < minimum -> minimum
                    coefficient > maximum -> maximum
                    else -> coefficient
                }
                val aligned = bounded.multiply(unit)
                val lowerComparison = aligned.compareTo(lowerCoefficient)
                val upperComparison = aligned.compareTo(upperCoefficient)
                if (lowerComparison < 0 || upperComparison > 0 ||
                    (!includesMidpoints && (lowerComparison == 0 || upperComparison == 0))) continue
                // The closest admissible point need not be the closest grid point
                // overall: binary power boundaries have asymmetric intervals.
                val decimal = BigDecimal(bounded, scale).stripTrailingZeros()
                val distance = exactCoefficient.subtract(aligned).abs()
                val comparison = bestDistance?.let { distance.compareTo(it) } ?: -1
                if (comparison < 0 || (comparison == 0 && !decimal.unscaledValue().testBit(0))) {
                    best = decimal
                    bestDistance = distance
                }
            }
        }
        if (best != null) return best
    }
    error("No seventeen-digit round-trip decimal")
}
