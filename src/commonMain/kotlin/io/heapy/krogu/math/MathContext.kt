package io.heapy.krogu.math

/** Immutable decimal precision and rounding policy; zero precision means unlimited. */
class MathContext(val precision: Int, val roundingMode: RoundingMode) : MathSerializable {
    init { require(precision >= 0) { "Digits < 0" } }
    private fun writeReplace(): Any = serializationProxy(this)
    constructor(setPrecision: Int) : this(setPrecision, RoundingMode.HALF_UP)
    constructor(value: String) : this(parse(value).first, parse(value).second)

    override fun equals(other: Any?): Boolean = other is MathContext && precision == other.precision && roundingMode == other.roundingMode
    override fun hashCode(): Int = precision + roundingMode.hashCode() * 59
    override fun toString(): String = "precision=$precision roundingMode=$roundingMode"

    companion object {
        @kotlin.jvm.JvmField val UNLIMITED = MathContext(0, RoundingMode.HALF_UP)
        @kotlin.jvm.JvmField val DECIMAL32 = MathContext(7, RoundingMode.HALF_EVEN)
        @kotlin.jvm.JvmField val DECIMAL64 = MathContext(16, RoundingMode.HALF_EVEN)
        @kotlin.jvm.JvmField val DECIMAL128 = MathContext(34, RoundingMode.HALF_EVEN)
        private fun parse(value: String): Pair<Int, RoundingMode> {
            val match = Regex("precision=([^ ]+) roundingMode=([A-Z_]+)").matchEntire(value)
                ?: throw IllegalArgumentException("Bad string format")
            val precision = try { BigInteger(match.groupValues[1]).intValueExact() }
                catch (e: NumberFormatException) { throw IllegalArgumentException("Bad precision", e) }
                catch (e: ArithmeticException) { throw IllegalArgumentException("Bad precision", e) }
            return precision to RoundingMode.valueOf(match.groupValues[2])
        }
    }
}
