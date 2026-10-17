package io.heapy.krogu.math

/** Immutable decimal precision and rounding policy; zero precision means unlimited. */
@Suppress("NON_FINAL_MEMBER_IN_FINAL_CLASS")
class MathContext(open val precision: Int, roundingMode: RoundingMode) : MathSerializable {
    init { require(precision >= 0) { "Digits < 0" } }
    open val roundingMode: RoundingMode = roundingMode
    private fun readObject(stream: MathObjectInputStream) {
        restoreSerializedFields(stream)
        val restored: RoundingMode? = roundingMode
        if (precision < 0 || restored == null) invalidSerializedState("Invalid MathContext")
    }
    private fun readObjectNoData(): Unit = invalidSerializedState("Missing MathContext data")
    constructor(setPrecision: Int) : this(setPrecision, RoundingMode.HALF_UP)
    private constructor(parsed: Pair<Int, RoundingMode>) : this(parsed.first, parsed.second)
    constructor(value: String) : this(parse(value))

    // Common callers can use both Kotlin properties and the JDK method names.
    // JVM already exposes the property accessors; hide the duplicate aliases from Java.
    @kotlin.jvm.JvmSynthetic
    @kotlin.jvm.JvmName("getPrecisionMethod")
    fun getPrecision(): Int = precision
    @kotlin.jvm.JvmSynthetic
    @kotlin.jvm.JvmName("getRoundingModeMethod")
    fun getRoundingMode(): RoundingMode = roundingMode

    override fun equals(other: Any?): Boolean = other is MathContext && precision == other.precision && roundingMode == other.roundingMode
    override fun hashCode(): Int = precision + roundingMode.hashCode() * 59
    override fun toString(): String = "precision=$precision roundingMode=$roundingMode"

    companion object {
        private const val serialVersionUID: Long = 5579720004786848255L
        @kotlin.jvm.JvmField val UNLIMITED = MathContext(0, RoundingMode.HALF_UP)
        @kotlin.jvm.JvmField val DECIMAL32 = MathContext(7, RoundingMode.HALF_EVEN)
        @kotlin.jvm.JvmField val DECIMAL64 = MathContext(16, RoundingMode.HALF_EVEN)
        @kotlin.jvm.JvmField val DECIMAL128 = MathContext(34, RoundingMode.HALF_EVEN)
        private fun parse(value: String): Pair<Int, RoundingMode> {
            fun badFormat(): Nothing = throw IllegalArgumentException("bad string format")
            val separator = value.indexOf(' ')
            if (!value.startsWith("precision=") || separator <= 10 ||
                !value.startsWith("roundingMode=", separator + 1)) badFormat()
            var index = 10
            val negative = value[index] == '-'
            if (negative || value[index] == '+') index++
            if (index == separator) badFormat()
            val limit = if (negative) 2147483648L else 2147483647L
            var magnitude = 0L
            while (index < separator) {
                val digit = value[index++].digitToIntOrNull(10) ?: badFormat()
                magnitude = magnitude * 10 + digit
                if (magnitude > limit) badFormat()
            }
            val rounding = try { RoundingMode.valueOf(value.substring(separator + 14)) }
                catch (_: IllegalArgumentException) { badFormat() }
            val precision = (if (negative) -magnitude else magnitude).toInt()
            // Validate negative precision only after the entire grammar and mode.
            return precision to rounding
        }
    }
}
