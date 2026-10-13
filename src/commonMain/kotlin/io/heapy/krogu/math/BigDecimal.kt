package io.heapy.krogu.math

import kotlin.jvm.JvmField
import kotlin.jvm.JvmStatic
import kotlin.math.pow

/** Immutable decimal, represented exactly as coefficient × 10^(-scale). */
// Preserve virtual java.lang.Number methods instead of Kotlin's default final bridges.
@Suppress("INAPPLICABLE_JVM_NAME")
open class BigDecimal(private val coefficient: BigInteger, private val decimalScale: Int) :
    Number(), Comparable<BigDecimal>, MathSerializable {

    private constructor(value: BigDecimal) : this(value.coefficient, value.decimalScale)
    private fun writeReplace(): Any = serializationProxy(this)
    constructor(value: BigInteger) : this(value, 0)
    constructor(value: BigInteger, mc: MathContext) : this(BigDecimal(value).round(mc))
    constructor(unscaledVal: BigInteger, scale: Int, mc: MathContext) : this(BigDecimal(unscaledVal, scale).round(mc))
    constructor(value: Int) : this(value.toLong())
    constructor(value: Long) : this(BigInteger.valueOf(value), 0)
    constructor(value: Int, mc: MathContext) : this(value.toLong(), mc)
    constructor(value: Long, mc: MathContext) : this(BigDecimal(value).round(mc))
    constructor(value: String) : this(parse(value))
    constructor(value: String, mc: MathContext) : this(parse(value).round(mc))
    constructor(value: CharArray) : this(value, 0, value.size)
    constructor(value: CharArray, offset: Int, len: Int) : this(characters(value, offset, len))
    constructor(value: CharArray, mc: MathContext) : this(value, 0, value.size, mc)
    constructor(value: CharArray, offset: Int, len: Int, mc: MathContext) : this(characters(value, offset, len), mc)
    constructor(value: Double) : this(fromDouble(value))
    constructor(value: Double, mc: MathContext) : this(fromDouble(value).round(mc))

    open fun unscaledValue(): BigInteger = coefficient
    open fun scale(): Int = decimalScale
    open fun precision(): Int = coefficient.abs().toString().length
    open fun signum(): Int = coefficient.signum()
    open fun abs(): BigDecimal = if (signum() < 0) negate() else this
    open fun abs(mc: MathContext): BigDecimal = abs().round(mc)
    open fun negate(): BigDecimal = BigDecimal(coefficient.negate(), decimalScale)
    open fun negate(mc: MathContext): BigDecimal = negate().round(mc)
    open fun plus(): BigDecimal = this
    open fun plus(mc: MathContext): BigDecimal = round(mc)

    open fun add(augend: BigDecimal): BigDecimal {
        val scale = maxOf(decimalScale, augend.decimalScale)
        return BigDecimal(scaledCoefficient(scale).add(augend.scaledCoefficient(scale)), scale)
    }
    open fun add(augend: BigDecimal, mc: MathContext): BigDecimal {
        if (mc.precision == 0) return add(augend)
        val preferredScale = maxOf(decimalScale, augend.decimalScale)
        if (signum() == 0 || augend.signum() == 0) {
            val nonzero = (if (signum() == 0) augend else this).round(mc)
            if (nonzero.signum() == 0) return BigDecimal(BigInteger.ZERO, preferredScale)
            if (preferredScale.toLong() - nonzero.decimalScale > Int.MAX_VALUE)
                throw ArithmeticException("Underflow")
            val scale = minOf(preferredScale.toLong(), nonzero.decimalScale.toLong() + mc.precision - nonzero.precision())
            return nonzero.setScale(checkedScale(scale))
        }
        checkAdditionAlignment(augend, mc)
        if (kotlin.math.abs(decimalScale.toLong() - augend.decimalScale) < 10000 ||
            kotlin.math.abs(adjustedExponent() - augend.adjustedExponent()) <= mc.precision.toLong() + 3)
            return add(augend).round(mc)
        // Keep a guard digit plus a sticky digit when the exponent gap is enormous.
        // Replacing a remote tail by a signed sticky unit preserves every rounding decision.
        val large = if (adjustedExponent() > augend.adjustedExponent()) this else augend
        val small = if (large === this) augend else this
        val workingScale = maxOf(large.decimalScale.toLong(), mc.precision.toLong() + 2L - large.adjustedExponent())
        val result = large.add(small.stickyAt(workingScale)).round(mc)
        val preferred = maxOf(decimalScale, augend.decimalScale)
        return if (result.signum() == 0) result.setScale(preferred) else result.stripTo(preferred)
    }
    private fun checkAdditionAlignment(other: BigDecimal, mc: MathContext) {
        if (decimalScale == other.decimalScale) return
        val dominant = if (decimalScale <= other.decimalScale) this else other
        val remote = if (dominant === this) other else this
        val leadingPosition = remote.decimalScale.toLong() - remote.precision() + 1
        val roundingScale = dominant.decimalScale.toLong() - dominant.precision() + mc.precision
        // The pinned JDK's alignment threshold is a signed 32-bit quantity. Its
        // wrap at MAX_VALUE is observable as an ArithmeticException before rounding.
        val alignmentThreshold = (dominant.decimalScale + 2).toLong()
        if (leadingPosition > alignmentThreshold && leadingPosition > roundingScale + 2)
            checkedScale(maxOf(dominant.decimalScale.toLong(), roundingScale) + 3)
    }
    private fun stickyAt(scale: Long): BigDecimal {
        if (signum() == 0 || decimalScale.toLong() <= scale) return this
        val drop = decimalScale.toLong() - scale
        if (drop <= precision()) {
            val divisor = tenPow(drop)
            val qr = coefficient.divideAndRemainder(divisor)
            val sticky = if (qr[1].signum() == 0) qr[0] else qr[0].multiply(BigInteger.TEN).add(BigInteger.valueOf(signum().toLong()))
            return if (qr[1].signum() == 0) BigDecimal(sticky, checkedScale(scale)) else BigDecimal(sticky, checkedScale(scale + 1))
        }
        // This tail is strictly less than one unit at 'scale'. Replacing it by
        // a whole unit can carry through 999... or cancel a retained +1 digit.
        return BigDecimal(BigInteger.valueOf(signum().toLong()), checkedScale(scale + 1))
    }
    open fun subtract(subtrahend: BigDecimal): BigDecimal = add(subtrahend.negate())
    open fun subtract(subtrahend: BigDecimal, mc: MathContext): BigDecimal = add(subtrahend.negate(), mc)
    open fun multiply(multiplicand: BigDecimal): BigDecimal = BigDecimal(
        coefficient.multiply(multiplicand.coefficient), scaleForValue(decimalScale.toLong() + multiplicand.decimalScale, signum() == 0))
    open fun multiply(multiplicand: BigDecimal, mc: MathContext): BigDecimal = multiply(multiplicand).round(mc)

    open fun divide(divisor: BigDecimal): BigDecimal {
        checkDivisor(divisor)
        val preferred = scaleForValue(decimalScale.toLong() - divisor.decimalScale, signum() == 0)
        if (signum() == 0) return BigDecimal(BigInteger.ZERO, preferred)
        // A terminating denominator can require fewer than (10/3)*its decimal digits
        // of additional precision (10/3 > log2(10)). The pinned oracle checks the
        // normalization scale at this conservative precision even for exact division.
        val exactPrecision = (precision().toLong() + (10L * divisor.precision() + 2) / 3)
            .coerceAtMost(Int.MAX_VALUE.toLong()).toInt()
        checkDivisionNormalization(divisor, exactPrecision)
        val gcd = coefficient.gcd(divisor.coefficient)
        var denominator = divisor.coefficient.abs().divide(gcd)
        var numerator = coefficient.divide(gcd)
        if (divisor.signum() < 0) numerator = numerator.negate()
        val twos = denominator.getLowestSetBit()
        denominator = denominator.shiftRight(twos)
        val five = BigInteger.valueOf(5)
        var fives = 0
        while (true) {
            val qr = denominator.divideAndRemainder(five)
            if (qr[1].signum() != 0) break
            denominator = qr[0]; fives++
        }
        if (denominator != BigInteger.ONE) throw ArithmeticException("Non-terminating decimal expansion")
        val digits = maxOf(twos, fives)
        numerator = numerator.shiftLeft(digits - twos).multiply(five.pow(digits - fives))
        return BigDecimal(numerator, checkedScale(decimalScale.toLong() - divisor.decimalScale + digits)).stripTo(preferred)
    }
    open fun divide(divisor: BigDecimal, roundingMode: RoundingMode): BigDecimal = divide(divisor, decimalScale, roundingMode)
    @Deprecated("Use RoundingMode")
    open fun divide(divisor: BigDecimal, roundingMode: Int): BigDecimal = divide(divisor, RoundingMode.valueOf(roundingMode))
    @Deprecated("Use RoundingMode")
    open fun divide(divisor: BigDecimal, scale: Int, roundingMode: Int): BigDecimal = divide(divisor, scale, RoundingMode.valueOf(roundingMode))
    open fun divide(divisor: BigDecimal, scale: Int, roundingMode: RoundingMode): BigDecimal {
        checkDivisor(divisor)
        val shift = scale.toLong() + divisor.decimalScale - decimalScale
        if (kotlin.math.abs(shift) > 646456992L) throw ArithmeticException("BigInteger overflow")
        if (signum() == 0) return BigDecimal(BigInteger.ZERO, scale)
        // Avoid constructing an unbounded power of ten for a quotient strictly below 0.1.
        val exponent = quotientExponent(divisor) + scale
        if (exponent < -1) return BigDecimal(roundTiny(signum() * divisor.signum(), roundingMode), scale)
        val numerator = if (shift >= 0) coefficient.multiply(tenPow(shift)) else coefficient
        val denominator = if (shift < 0) divisor.coefficient.multiply(tenPow(-shift)) else divisor.coefficient
        return BigDecimal(roundedDivide(numerator, denominator, roundingMode), scale)
    }
    open fun divide(divisor: BigDecimal, mc: MathContext): BigDecimal {
        if (mc.precision == 0) return divide(divisor)
        checkDivisor(divisor)
        val preferred = scaleForValue(decimalScale.toLong() - divisor.decimalScale, signum() == 0)
        if (signum() == 0) return BigDecimal(BigInteger.ZERO, preferred)
        checkDivisionNormalization(divisor, mc.precision)
        val target = checkedScale(mc.precision.toLong() - 1 - quotientExponent(divisor))
        val result = divide(divisor, target, mc.roundingMode).round(mc)
        // Exact quotients use the nearest representable scale to the preferred scale.
        val exact = compareValues(result.coefficient.multiply(divisor.coefficient),
            result.decimalScale.toLong() + divisor.decimalScale, coefficient, decimalScale.toLong()) == 0
        return if (exact) result.stripTo(preferred) else result
    }
    private fun checkDivisionNormalization(divisor: BigDecimal, precision: Int) {
        val adjustment = if (normalizedComparison(divisor) > 0) 1 else 0
        checkedScale(decimalScale.toLong() - divisor.decimalScale + precision + divisor.precision() - precision() - adjustment)
    }
    private fun quotientExponent(divisor: BigDecimal): Long {
        val a = coefficient.abs().toString()
        val b = divisor.coefficient.abs().toString()
        return a.length.toLong() - b.length + divisor.decimalScale.toLong() - decimalScale - if (normalizedComparison(divisor) < 0) 1 else 0
    }
    private fun normalizedComparison(divisor: BigDecimal): Int {
        val a = coefficient.abs().toString()
        val b = divisor.coefficient.abs().toString()
        val length = maxOf(a.length, b.length)
        return a.padEnd(length, '0').compareTo(b.padEnd(length, '0'))
    }
    open fun divideToIntegralValue(divisor: BigDecimal): BigDecimal = integralDivide(divisor, MathContext.UNLIMITED)
    open fun divideToIntegralValue(divisor: BigDecimal, mc: MathContext): BigDecimal = integralDivide(divisor, mc)
    private fun integralDivide(divisor: BigDecimal, mc: MathContext): BigDecimal {
        checkDivisor(divisor)
        val preferred = saturatedScale(decimalScale.toLong() - divisor.decimalScale)
        if (signum() == 0 || abs() < divisor.abs()) return BigDecimal(BigInteger.ZERO, preferred)
        var result = divide(divisor, 0, RoundingMode.DOWN)
        if (preferred < 0) result = result.stripTo(preferred)
        if (mc.precision > 0 && result.precision() > mc.precision) {
            val reduced = result.stripTrailingZeros()
            if (reduced.precision() > mc.precision) throw ArithmeticException("Division impossible")
            result = reduced.setScale(minOf(preferred, checkedScale(mc.precision.toLong() - reduced.adjustedExponent() - 1)))
        } else if (preferred > result.decimalScale) {
            val room = if (mc.precision == 0) preferred else minOf(preferred, result.decimalScale + mc.precision - result.precision())
            result = result.setScale(room)
        }
        return result
    }
    open fun divideAndRemainder(divisor: BigDecimal): Array<BigDecimal> = divideAndRemainder(divisor, MathContext.UNLIMITED)
    open fun divideAndRemainder(divisor: BigDecimal, mc: MathContext): Array<BigDecimal> {
        val quotient = divideToIntegralValue(divisor, mc)
        return arrayOf(quotient, subtract(quotient.multiply(divisor)))
    }
    open fun remainder(divisor: BigDecimal): BigDecimal = divideAndRemainder(divisor)[1]
    open fun remainder(divisor: BigDecimal, mc: MathContext): BigDecimal = divideAndRemainder(divisor, mc)[1]

    open fun round(mc: MathContext): BigDecimal {
        if (mc.precision == 0) return this
        var result = this
        while (result.precision() > mc.precision) {
            val drop = result.precision() - mc.precision
            result = BigDecimal(roundedDivide(result.coefficient, tenPow(drop.toLong()), mc.roundingMode),
                checkedScale(result.decimalScale.toLong() - drop))
        }
        return result
    }
    open fun setScale(newScale: Int): BigDecimal = setScale(newScale, RoundingMode.UNNECESSARY)
    @Deprecated("Use RoundingMode")
    open fun setScale(newScale: Int, roundingMode: Int): BigDecimal = setScale(newScale, RoundingMode.valueOf(roundingMode))
    open fun setScale(newScale: Int, roundingMode: RoundingMode): BigDecimal {
        if (decimalScale == newScale) return this
        if (signum() == 0) return BigDecimal(BigInteger.ZERO, newScale)
        val shift = newScale.toLong() - decimalScale
        if (kotlin.math.abs(shift) > 646456992L) throw ArithmeticException("BigInteger overflow")
        val value = when {
            shift > 0 -> coefficient.multiply(tenPow(shift))
            -shift > precision() -> roundTiny(signum(), roundingMode)
            else -> roundedDivide(coefficient, tenPow(-shift), roundingMode)
        }
        return BigDecimal(value, newScale)
    }
    open fun stripTrailingZeros(): BigDecimal = if (signum() == 0) ZERO else stripTo(Int.MIN_VALUE, failOnOverflow = true)
    private fun stripTo(minScale: Int, failOnOverflow: Boolean = false): BigDecimal {
        var value = coefficient
        var scale = decimalScale
        while (value.signum() != 0 && scale > minScale) {
            val qr = value.divideAndRemainder(BigInteger.TEN)
            if (qr[1].signum() != 0) break
            value = qr[0]; scale--
        }
        if (failOnOverflow && scale == Int.MIN_VALUE && value.signum() != 0 && value.remainder(BigInteger.TEN).signum() == 0)
            throw ArithmeticException("Overflow")
        return if (scale == decimalScale) this else BigDecimal(value, scale)
    }
    open fun movePointLeft(n: Int): BigDecimal = movePoint(decimalScale.toLong() + n)
    open fun movePointRight(n: Int): BigDecimal = movePoint(decimalScale.toLong() - n)
    private fun movePoint(scale: Long): BigDecimal {
        val result = BigDecimal(coefficient, scaleForValue(scale, signum() == 0))
        return if (result.decimalScale < 0) result.setScale(0) else result
    }
    open fun scaleByPowerOfTen(n: Int): BigDecimal = BigDecimal(coefficient, scaleForValue(decimalScale.toLong() - n, signum() == 0))
    open fun ulp(): BigDecimal = BigDecimal(BigInteger.ONE, decimalScale)

    open fun pow(n: Int): BigDecimal {
        if (n !in 0..999999999) throw ArithmeticException("Invalid operation")
        if (n == 0) return ONE
        return BigDecimal(coefficient.pow(n), scaleForValue(decimalScale.toLong() * n, signum() == 0))
    }
    open fun pow(n: Int, mc: MathContext): BigDecimal {
        if (mc.precision == 0) return pow(n)
        if (n !in -999999999..999999999) throw ArithmeticException("Invalid operation")
        if (n == 0) return ONE
        val magnitude = if (n < 0) -n else n
        val exponentDigits = magnitude.toString().length
        if (exponentDigits > mc.precision) throw ArithmeticException("Invalid operation")
        val work = MathContext(checkedScale(mc.precision.toLong() + exponentDigits + 1), mc.roundingMode)
        var result = ONE
        val bits = 32 - magnitude.countLeadingZeroBits()
        for (i in bits - 1 downTo 0) {
            result = result.multiply(result, work)
            if (magnitude and (1 shl i) != 0) result = result.multiply(this, work)
        }
        if (n < 0) result = ONE.divide(result, work)
        return result.round(mc)
    }

    open fun sqrt(mc: MathContext): BigDecimal {
        if (signum() < 0) throw ArithmeticException("Attempted square root of negative BigDecimal")
        val preferred = decimalScale / 2
        if (signum() == 0) return BigDecimal(BigInteger.ZERO, preferred)
        if (mc.precision == 0 || mc.roundingMode == RoundingMode.UNNECESSARY) {
            val odd = decimalScale and 1
            val value = if (odd == 0) coefficient else coefficient.multiply(BigInteger.TEN)
            val root = value.sqrtAndRemainder()
            if (root[1].signum() != 0) throw ArithmeticException("Computed square root not exact")
            val exact = BigDecimal(root[0], checkedScale((decimalScale.toLong() + odd) / 2)).stripTo(preferred)
            // Exact-mode validation in the oracle squares a minimally scaled root.
            // Its scale overflow is observable even when the preferred form could be squared.
            checkedScale(2L * exact.stripTrailingZeros().decimalScale)
            return exact.round(mc)
        }
        val exponent = adjustedExponent()
        val rootExponent = if (exponent < 0) (exponent - 1) / 2 else exponent / 2
        val scale = checkedScale(mc.precision.toLong() - 1 - rootExponent)
        val shift = 2L * scale - decimalScale
        val numerator = if (shift >= 0) coefficient.multiply(tenPow(shift)) else coefficient
        val denominator = if (shift < 0) tenPow(-shift) else BigInteger.ONE
        var root = numerator.divide(denominator).sqrt()
        val exact = root.multiply(root).multiply(denominator) == numerator
        if (!exact) {
            val midpoint = root.shiftLeft(1).add(BigInteger.ONE)
            val half = numerator.shiftLeft(2).compareTo(midpoint.multiply(midpoint).multiply(denominator))
            if (increment(mc.roundingMode, 1, half, root.testBit(0))) root = root.add(BigInteger.ONE)
        }
        return BigDecimal(root, scale).round(mc).stripTo(preferred)
    }

    override fun compareTo(other: BigDecimal): Int {
        if (signum() != other.signum()) return signum().compareTo(other.signum())
        if (signum() == 0) return 0
        val exponent = adjustedExponent().compareTo(other.adjustedExponent())
        if (exponent != 0) return exponent * signum()
        val a = coefficient.abs().toString()
        val b = other.coefficient.abs().toString()
        val length = maxOf(a.length, b.length)
        return a.padEnd(length, '0').compareTo(b.padEnd(length, '0')) * signum()
    }
    open fun min(value: BigDecimal): BigDecimal = if (this <= value) this else value
    open fun max(value: BigDecimal): BigDecimal = if (this >= value) this else value
    override fun equals(other: Any?): Boolean = other is BigDecimal && decimalScale == other.decimalScale && coefficient == other.coefficient
    override fun hashCode(): Int = 31 * coefficient.hashCode() + decimalScale
    private fun adjustedExponent(): Long = precision().toLong() - decimalScale - 1
    private fun scaledCoefficient(scale: Int): BigInteger = if (signum() == 0) coefficient else coefficient.multiply(tenPow(scale.toLong() - decimalScale))

    override fun toString(): String {
        val digits = coefficient.abs().toString()
        val exponent = adjustedExponent()
        if (decimalScale >= 0 && exponent >= -6) return toPlainString()
        val body = if (digits.length == 1) digits else digits[0] + "." + digits.substring(1)
        return (if (signum() < 0) "-" else "") + body + exponentText(exponent)
    }
    open fun toPlainString(): String {
        val digits = coefficient.abs().toString()
        val body = when {
            decimalScale == 0 -> digits
            decimalScale < 0 -> if (signum() == 0) "0" else digits + zeros(-decimalScale.toLong())
            decimalScale < digits.length -> digits.substring(0, digits.length - decimalScale) + "." + digits.substring(digits.length - decimalScale)
            else -> "0." + zeros(decimalScale.toLong() - digits.length) + digits
        }
        return (if (signum() < 0) "-" else "") + body
    }
    open fun toEngineeringString(): String {
        if (decimalScale >= 0 && adjustedExponent() >= -6) return toPlainString()
        val digits = coefficient.abs().toString()
        val exp = adjustedExponent()
        val mod = ((exp % 3 + 3) % 3).toInt()
        var engineeringExp = exp - mod
        val body: String
        if (signum() == 0) {
            val decimals = ((decimalScale.toLong() % 3 + 3) % 3).toInt()
            engineeringExp = -decimalScale.toLong() + decimals
            body = if (decimals == 0) "0" else "0." + "0".repeat(decimals)
        } else {
            val before = mod + 1
            body = if (digits.length <= before) digits.padEnd(before, '0') else digits.substring(0, before) + "." + digits.substring(before)
        }
        return (if (signum() < 0) "-" else "") + body + if (engineeringExp == 0L) "" else exponentText(engineeringExp)
    }

    open fun toBigInteger(): BigInteger {
        if (signum() == 0) return BigInteger.ZERO
        if (decimalScale <= 0) return coefficient.multiply(tenPow(-decimalScale.toLong()))
        if (decimalScale >= precision()) return BigInteger.ZERO
        return coefficient.divide(tenPow(decimalScale.toLong()))
    }
    open fun toBigIntegerExact(): BigInteger = setScale(0).coefficient
    @kotlin.jvm.JvmName("intValue")
    override fun toInt(): Int = integerLowBits(32).toInt()
    @kotlin.jvm.JvmName("longValue")
    override fun toLong(): Long = integerLowBits(64)
    private fun integerLowBits(bits: Int): Long {
        if (decimalScale >= 0) return toBigInteger().toLong()
        // A multiple of 10^bits vanishes modulo 2^bits; don't materialize a huge integer.
        if (-decimalScale.toLong() >= bits) return 0
        return coefficient.multiply(tenPow(-decimalScale.toLong())).toLong()
    }
    @kotlin.jvm.JvmName("byteValue")
    override fun toByte(): Byte = toInt().toByte()
    @kotlin.jvm.JvmName("shortValue")
    override fun toShort(): Short = toInt().toShort()
    open fun intValueExact(): Int = toBigIntegerExact().intValueExact()
    open fun longValueExact(): Long = toBigIntegerExact().longValueExact()
    open fun shortValueExact(): Short = toBigIntegerExact().shortValueExact()
    open fun byteValueExact(): Byte = toBigIntegerExact().byteValueExact()
    @kotlin.jvm.JvmName("doubleValue")
    override fun toDouble(): Double = binaryValue(53, -1022, 1023)
    @kotlin.jvm.JvmName("floatValue")
    override fun toFloat(): Float = binaryValue(24, -126, 127).toFloat()
    private fun binaryValue(precision: Int, minExponent: Int, maxExponent: Int): Double {
        if (signum() == 0) return 0.0
        val decimalExponent = adjustedExponent()
        if (decimalExponent > 400) return if (signum() < 0) Double.NEGATIVE_INFINITY else Double.POSITIVE_INFINITY
        if (decimalExponent < -400) return if (signum() < 0) -0.0 else 0.0
        val numerator = if (decimalScale < 0) coefficient.abs().multiply(tenPow(-decimalScale.toLong())) else coefficient.abs()
        val denominator = if (decimalScale > 0) tenPow(decimalScale.toLong()) else BigInteger.ONE
        var exponent = numerator.bitLength() - denominator.bitLength()
        if (exponent >= 0) { if (numerator < denominator.shiftLeft(exponent)) exponent-- }
        else if (numerator.shiftLeft(-exponent) < denominator) exponent--
        if (exponent > maxExponent) return if (signum() < 0) Double.NEGATIVE_INFINITY else Double.POSITIVE_INFINITY
        val shift = precision - 1 - maxOf(exponent, minExponent)
        val top = if (shift >= 0) roundedDivide(numerator.shiftLeft(shift), denominator, RoundingMode.HALF_EVEN)
            else roundedDivide(numerator, denominator.shiftLeft(-shift), RoundingMode.HALF_EVEN)
        // Multiplication by a power of two is exact, including gradual underflow.
        return signum() * top.toLong().toDouble() * 2.0.pow(-shift)
    }

    operator fun plus(other: BigDecimal): BigDecimal = add(other)
    operator fun minus(other: BigDecimal): BigDecimal = subtract(other)
    operator fun times(other: BigDecimal): BigDecimal = multiply(other)
    operator fun div(other: BigDecimal): BigDecimal = divide(other)
    operator fun rem(other: BigDecimal): BigDecimal = remainder(other)
    operator fun unaryMinus(): BigDecimal = negate()
    operator fun unaryPlus(): BigDecimal = this

    // Preserve the non-final static factories exposed by JDK.
    @Suppress("NON_FINAL_MEMBER_IN_OBJECT")
    companion object {
        private fun compareValues(a: BigInteger, aScale: Long, b: BigInteger, bScale: Long): Int {
            if (a.signum() != b.signum()) return a.signum().compareTo(b.signum())
            if (a.signum() == 0) return 0
            val aDigits = a.abs().toString()
            val bDigits = b.abs().toString()
            val exponent = (aDigits.length - aScale).compareTo(bDigits.length - bScale)
            if (exponent != 0) return exponent * a.signum()
            val length = maxOf(aDigits.length, bDigits.length)
            return aDigits.padEnd(length, '0').compareTo(bDigits.padEnd(length, '0')) * a.signum()
        }
        @JvmField val ZERO = BigDecimal(BigInteger.ZERO, 0)
        @JvmField val ONE = BigDecimal(BigInteger.ONE, 0)
        @JvmField val TWO = BigDecimal(BigInteger.TWO, 0)
        @JvmField val TEN = BigDecimal(BigInteger.TEN, 0)
        @Deprecated("Use RoundingMode.UP") const val ROUND_UP = 0
        @Deprecated("Use RoundingMode.DOWN") const val ROUND_DOWN = 1
        @Deprecated("Use RoundingMode.CEILING") const val ROUND_CEILING = 2
        @Deprecated("Use RoundingMode.FLOOR") const val ROUND_FLOOR = 3
        @Deprecated("Use RoundingMode.HALF_UP") const val ROUND_HALF_UP = 4
        @Deprecated("Use RoundingMode.HALF_DOWN") const val ROUND_HALF_DOWN = 5
        @Deprecated("Use RoundingMode.HALF_EVEN") const val ROUND_HALF_EVEN = 6
        @Deprecated("Use RoundingMode.UNNECESSARY") const val ROUND_UNNECESSARY = 7

        @JvmStatic open fun valueOf(value: Long): BigDecimal = BigDecimal(value)
        @JvmStatic open fun valueOf(unscaledVal: Long, scale: Int): BigDecimal = BigDecimal(BigInteger.valueOf(unscaledVal), scale)
        @JvmStatic open fun valueOf(value: Double): BigDecimal = BigDecimal(decimalDoubleString(value))

        private fun parse(value: String): BigDecimal {
            if (value.isEmpty()) throw NumberFormatException("Empty BigDecimal")
            var i = 0
            var negative = false
            if (value[i] == '-' || value[i] == '+') { negative = value[i] == '-'; i++ }
            val digits = StringBuilder()
            var point = false
            var fraction = 0L
            while (i < value.length && value[i] != 'e' && value[i] != 'E') {
                val c = value[i++]
                if (c == '.' && !point) { point = true; continue }
                val d = c.digitToIntOrNull(10) ?: throw NumberFormatException("Invalid decimal digit")
                digits.append(('0'.code + d).toChar())
                if (point) fraction++
            }
            if (digits.isEmpty()) throw NumberFormatException("No digits")
            var exponent = 0L
            if (i < value.length) {
                i++
                var expSign = 1
                if (i < value.length && (value[i] == '+' || value[i] == '-')) { if (value[i] == '-') expSign = -1; i++ }
                if (i == value.length) throw NumberFormatException("No exponent digits")
                while (i < value.length) {
                    val d = value[i++].digitToIntOrNull(10) ?: throw NumberFormatException("Invalid exponent")
                    exponent = exponent * 10 + d
                    if (exponent > Int.MAX_VALUE.toLong() + 1 + fraction) throw NumberFormatException("Exponent overflow")
                }
                exponent *= expSign
            }
            val scale = fraction - exponent
            if (scale !in Int.MIN_VALUE.toLong()..Int.MAX_VALUE.toLong()) throw NumberFormatException("Scale out of range")
            val number = BigInteger(digits.toString())
            return BigDecimal(if (negative) number.negate() else number, scale.toInt())
        }
        private fun characters(value: CharArray, offset: Int, len: Int): String {
            if (offset < 0 || len < 0 || offset > value.size - len) throw NumberFormatException("Bad character range")
            return value.concatToString(offset, offset + len)
        }
        private fun fromDouble(value: Double): BigDecimal {
            if (!value.isFinite()) throw NumberFormatException("Infinite or NaN")
            if (value == 0.0) return ZERO
            val bits = value.toBits()
            val field = ((bits ushr 52) and 2047).toInt()
            var mantissa = bits and ((1L shl 52) - 1)
            if (field != 0) mantissa = mantissa or (1L shl 52)
            var exponent = (if (field == 0) 1 else field) - 1023 - 52
            if (exponent < 0) {
                val remove = minOf(mantissa.countTrailingZeroBits(), -exponent)
                mantissa = mantissa ushr remove; exponent += remove
            }
            var coefficient = BigInteger.valueOf(mantissa)
            val scale: Int
            if (exponent >= 0) { coefficient = coefficient.shiftLeft(exponent); scale = 0 }
            else { scale = -exponent; coefficient = coefficient.multiply(BigInteger.valueOf(5).pow(scale)) }
            return BigDecimal(if (value < 0) coefficient.negate() else coefficient, scale)
        }
        private fun tenPow(exponent: Long): BigInteger {
            if (exponent < 0 || exponent > 646456992L) throw ArithmeticException("BigInteger overflow")
            return BigInteger.TEN.pow(exponent.toInt())
        }
        private fun checkedScale(scale: Long): Int {
            if (scale !in Int.MIN_VALUE.toLong()..Int.MAX_VALUE.toLong()) throw ArithmeticException(if (scale < 0) "Overflow" else "Underflow")
            return scale.toInt()
        }
        private fun saturatedScale(scale: Long): Int = scale.coerceIn(Int.MIN_VALUE.toLong(), Int.MAX_VALUE.toLong()).toInt()
        private fun scaleForValue(scale: Long, zero: Boolean): Int = if (zero) saturatedScale(scale) else checkedScale(scale)
        private fun zeros(count: Long): String {
            if (count > Int.MAX_VALUE - 8) throw ArithmeticException("String too large")
            return "0".repeat(count.toInt())
        }
        private fun exponentText(exponent: Long): String = "E" + (if (exponent >= 0) "+" else "") + exponent
        private fun checkDivisor(divisor: BigDecimal) {
            if (divisor.signum() == 0) throw ArithmeticException("Division by zero")
        }
        private fun roundedDivide(numerator: BigInteger, denominator: BigInteger, mode: RoundingMode): BigInteger {
            val qr = numerator.divideAndRemainder(denominator)
            if (qr[1].signum() == 0) return qr[0]
            val sign = numerator.signum() * denominator.signum()
            val half = qr[1].abs().shiftLeft(1).compareTo(denominator.abs())
            return if (increment(mode, sign, half, qr[0].testBit(0))) qr[0].add(BigInteger.valueOf(sign.toLong())) else qr[0]
        }
        private fun roundTiny(sign: Int, mode: RoundingMode): BigInteger =
            if (increment(mode, sign, -1, false)) BigInteger.valueOf(sign.toLong()) else BigInteger.ZERO
        private fun increment(mode: RoundingMode, sign: Int, half: Int, odd: Boolean): Boolean = when (mode) {
            RoundingMode.UP -> true
            RoundingMode.DOWN -> false
            RoundingMode.CEILING -> sign > 0
            RoundingMode.FLOOR -> sign < 0
            RoundingMode.HALF_UP -> half >= 0
            RoundingMode.HALF_DOWN -> half > 0
            RoundingMode.HALF_EVEN -> half > 0 || half == 0 && odd
            RoundingMode.UNNECESSARY -> throw ArithmeticException("Rounding necessary")
        }
    }
}
