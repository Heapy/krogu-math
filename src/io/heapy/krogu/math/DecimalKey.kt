package io.heapy.krogu.math

/**
 * Compares exact decimal values, ignoring scale. Only [BigDecimal] operands are
 * accepted: integers, floating point numbers and [DecimalKey] require explicit conversion.
 * The operands are observed through [BigDecimal.unscaledValue] and [BigDecimal.scale].
 */
fun BigDecimal.numericallyEquals(other: BigDecimal): Boolean = toDecimalKey() == other.toDecimalKey()

/** Creates an immutable, scale-independent key for this exact decimal value. */
fun BigDecimal.toDecimalKey(): DecimalKey = DecimalKey(this)

/**
 * An immutable decimal collection key with numerical equality and natural ordering.
 *
 * For example, keys for `2.0` and `2.00` are equal, have equal hashes, and compare
 * as zero. Keys only equal other keys; they never equal a [BigDecimal] or another
 * [Number]. Ordering is likewise restricted to keys, and is consistent with equality.
 *
 * Construction snapshots the value through [BigDecimal.unscaledValue] and
 * [BigDecimal.scale], then removes decimal trailing zeros using a Long scale.
 * Thus even values whose canonical scale lies below [Int.MIN_VALUE] are supported.
 * The source's value, scale, equality and hash code are untouched.
 *
 * Creation needs a decimal coefficient conversion and O(d) additional digit work/storage,
 * where d is its decimal digit count. Hashing a constructed key is O(1).
 * Comparison is O(d) in the worst case, without allocating scale-sized powers of ten.
 */
class DecimalKey(value: BigDecimal) : Comparable<DecimalKey> {
    private val sign: Int
    private val digits: String
    private val scale: Long
    private val hash: Int

    init {
        // Normalize a possible BigInteger subclass using BigDecimal's existing
        // defensive coefficient snapshot before converting it to decimal digits.
        val snapshot = BigDecimal(value.unscaledValue(), value.scale())
        sign = snapshot.signum()
        if (sign == 0) {
            digits = "0"
            scale = 0
        } else {
            val text = snapshot.unscaledValue().toString().removePrefix("-")
            var end = text.length
            while (text[end - 1] == '0') end--
            digits = text.substring(0, end)
            scale = snapshot.scale().toLong() - (text.length - end)
        }
        hash = 31 * (31 * sign + digits.hashCode()) + scale.hashCode()
    }

    override fun equals(other: Any?): Boolean = other is DecimalKey &&
        sign == other.sign && scale == other.scale && digits == other.digits

    override fun hashCode(): Int = hash

    override fun compareTo(other: DecimalKey): Int {
        if (sign != other.sign) return sign.compareTo(other.sign)
        if (sign == 0) return 0
        val exponentOrder = (digits.length.toLong() - scale).compareTo(other.digits.length.toLong() - other.scale)
        if (exponentOrder != 0) return sign * exponentOrder
        for (index in 0 until maxOf(digits.length, other.digits.length)) {
            val left = if (index < digits.length) digits[index] else '0'
            val right = if (index < other.digits.length) other.digits[index] else '0'
            if (left != right) return sign * left.compareTo(right)
        }
        return 0
    }

    /** Canonical coefficient and exponent; no expansion of very large scales. */
    override fun toString(): String = (if (sign < 0) "-" else "") + digits + "E" + (-scale)
}
