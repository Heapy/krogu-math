package io.heapy.krogu.math

import kotlin.jvm.JvmField
import kotlin.jvm.JvmStatic
import kotlin.math.pow

/** Immutable decimal, represented exactly as coefficient × 10^(-scale). */
// Preserve virtual java.lang.Number methods instead of Kotlin's default final bridges.
@Suppress("INAPPLICABLE_JVM_NAME", "DEPRECATION")
open class BigDecimal private constructor(
    private val decimalScale: Int,
    private val compactHigh: Int,
    private val compactLow: Int,
    private val representation: Int,
    private val wideCoefficient: BigInteger?,
) : Number(), Comparable<BigDecimal>, MathSerializable {

    private constructor(value: Long, scale: Int) : this(scale, (value shr 32).toInt(), value.toInt(), 1, null)
    // The public constructor normalizes subclass byte encoding before selecting storage.
    constructor(coefficient: BigInteger, decimalScale: Int) : this(normalizedCoefficient529(coefficient), decimalScale, Unit)
    private constructor(coefficient: BigInteger, scale: Int, @Suppress("UNUSED_PARAMETER") owned: Unit) : this(
        scale,
        if (coefficient.hasCompactStorage529()) (coefficient.compactStoredValue529() shr 32).toInt() else 0,
        if (coefficient.hasCompactStorage529()) coefficient.compactStoredValue529().toInt() else 0,
        if (coefficient.hasCompactStorage529()) 1 else 2,
        if (coefficient.hasCompactStorage529()) null else coefficient,
    )
    private constructor(value: BigDecimal) : this(value.decimalScale, value.compactHigh, value.compactLow,
        value.representation, value.wideCoefficient)

    @kotlin.concurrent.Volatile
    @kotlin.jvm.Transient
    private var cachedStoredPrecision597: Int = 0

    private fun compactValue529(): Long = CompactInteger529.value(compactHigh, compactLow)
    private fun coefficientSnapshot(): BigInteger = wideCoefficient ?: BigInteger.valueOf(compactValue529())
    @kotlin.jvm.JvmSynthetic
    internal fun hasCompactStorage529(): Boolean = representation == 1
    //535 keeps its existing MIN eligibility and observable subclass fallback.
    @kotlin.jvm.JvmSynthetic
    internal fun accumulatorCompactEligible529(): Boolean = this::class == BigDecimal::class &&
        representation == 1 && compactValue529() != Long.MIN_VALUE
    @kotlin.jvm.JvmSynthetic
    internal fun accumulatorCompactValue529(): Long = compactValue529()
    private fun storedSign529(): Int = if (representation == 1) CompactInteger529.sign(compactValue529())
        else wideCoefficient!!.signum()
    private fun readObject(stream: MathObjectInputStream) {
        restoreSerializedFields(stream)
        when (representation) {
            1 -> if (wideCoefficient != null) invalidSerializedState("Invalid compact BigDecimal")
            2 -> {
                val restored = wideCoefficient ?: invalidSerializedState("Missing BigDecimal coefficient")
                if (compactHigh != 0 || compactLow != 0 ||
                    restored::class == BigInteger::class && restored.hasCompactStorage529())
                    invalidSerializedState("Invalid wide BigDecimal")
                // A stream-restored coefficient subclass keeps its virtual behavior,
                // subtype fields and graph identity. Public construction normalizes it.
            }
            else -> invalidSerializedState("Missing BigDecimal representation")
        }
    }
    private fun readObjectNoData(): Unit = invalidSerializedState("Missing BigDecimal data")
    constructor(value: BigInteger) : this(value, 0)
    constructor(value: BigInteger, mc: MathContext) : this(BigDecimal(value).roundValue(mc))
    constructor(unscaledVal: BigInteger, scale: Int, mc: MathContext) : this(BigDecimal(unscaledVal, scale).roundValue(mc))
    constructor(value: Int) : this(value.toLong())
    constructor(value: Long) : this(value, 0)
    constructor(value: Int, mc: MathContext) : this(value.toLong(), mc)
    constructor(value: Long, mc: MathContext) : this(BigDecimal(value).roundValue(mc))
    constructor(value: String) : this(parse(value))
    constructor(value: String, mc: MathContext) : this(
        // Sufficient old-route proof, not an input-length limit. Its exact
        // coefficient is <10^K<2^(Int.MAX_VALUE-3).
        if (value.length <= 646456992) parse(value).roundValue(mc) else DecimalText588.parse(value, mc))
    constructor(value: CharArray) : this(value, 0, value.size)
    constructor(value: CharArray, offset: Int, len: Int) : this(DecimalText588.parse(value, offset, len, MathContext.UNLIMITED))
    constructor(value: CharArray, mc: MathContext) : this(value, 0, value.size, mc)
    constructor(value: CharArray, offset: Int, len: Int, mc: MathContext) : this(DecimalText588.parse(value, offset, len, mc))
    constructor(value: Double) : this(fromDouble(value))
    constructor(value: Double, mc: MathContext) : this(roundedDouble(value, mc))

    open fun unscaledValue(): BigInteger = coefficientSnapshot()
    open fun scale(): Int = decimalScale
    open fun precision(): Int = storedPrecision()
    private fun storedPrecision(): Int {
        // Preserve the597 subtype bypass without materializing compact coefficients.
        if (representation == 2 && wideCoefficient!!::class != BigInteger::class)
            return wideCoefficient.decimalMagnitudeDigits()
        val cached = cachedStoredPrecision597
        if (cached != 0) return cached
        val digits = if (representation == 1) CompactInteger529.decimalDigits(compactValue529())
            else wideCoefficient!!.decimalMagnitudeDigits()
        cachedStoredPrecision597 = digits
        return digits
    }
    open fun signum(): Int = storedSign529()
    open fun abs(): BigDecimal = if (signum() < 0) negate() else this
    open fun abs(mc: MathContext): BigDecimal = if (signum() < 0) negate(mc) else contextualUnaryRound(mc)
    open fun negate(): BigDecimal = if (representation == 1 && compactValue529() != Long.MIN_VALUE)
        BigDecimal(-compactValue529(), decimalScale) else BigDecimal(coefficientSnapshot().negate(), decimalScale)
    open fun negate(mc: MathContext): BigDecimal = negate().contextualUnaryRound(mc)
    private fun contextualUnaryRound(mc: MathContext): BigDecimal =
        // Unary cohort compatibility is separate from the direct plus/round rule.
        // Keep virtual plus on nonzero replacements with a valid context.
        if (storedSign529() == 0) roundValue(mc) else plus(mc)
    open fun plus(): BigDecimal = this
    open fun plus(mc: MathContext): BigDecimal {
        val result = roundValue(mc)
        // Direct plus/round specifies scale zero even for unlimited precision.
        // Constructors and arithmetic retain their own preferred scales.
        return if (result.storedSign529() == 0) ZERO else result
    }

    open fun add(augend: BigDecimal): BigDecimal = rawSum585(augend, false, null) ?: addValue(augend)
    private fun rawSum585(other: BigDecimal, subtract: Boolean, mc: MathContext?): BigDecimal? {
        if (representation == 1 && other.representation == 1) {
            val scale = maxOf(decimalScale, other.decimalScale)
            val a = scaledCompact529(compactValue529(), scale.toLong() - decimalScale)
            val b = scaledCompact529(other.compactValue529(), scale.toLong() - other.decimalScale)
            if (a != null && b != null) {
                val result = if (subtract) a - b else a + b
                if (if (subtract) CompactInteger529.subtractFits(a, b, result) else CompactInteger529.addFits(a, b, result)) {
                    val decimal = BigDecimal(result, scale)
                    return if (mc == null) decimal else decimal.roundValue(mc)
                }
            }
        }
        val coefficient = coefficientSnapshot()
        val otherCoefficient529 = other.coefficientSnapshot()
        val result = coefficient.alignedDecimalSum585(otherCoefficient529, decimalScale,
            other.decimalScale, subtract, mc) ?: return null
        return BigDecimal(result.first, result.second)
    }
    private fun addValue(augend: BigDecimal): BigDecimal {
        val scale = maxOf(decimalScale, augend.decimalScale)
        return BigDecimal(scaledCoefficient(scale).add(augend.scaledCoefficient(scale)), scale)
    }
    open fun add(augend: BigDecimal, mc: MathContext): BigDecimal {
        val context = mc
        if (context.precision == 0) return add(augend)
        return addContextValue(augend, context)
    }
    private fun addContextValue(augend: BigDecimal, mc: MathContext): BigDecimal {
        val preferredScale = maxOf(decimalScale, augend.decimalScale)
        if (signum() == 0 || augend.signum() == 0) {
            val nonzero = (if (signum() == 0) augend else this).round(mc)
            if (nonzero.signum() == 0) return BigDecimal(BigInteger.ZERO, preferredScale)
            val scale = minOf(preferredScale.toLong(), nonzero.decimalScale.toLong() + mc.precision - nonzero.precision())
            return nonzero.setScale(checkedScale(scale))
        }
        checkAdditionAlignment(augend, mc)
        if (kotlin.math.abs(decimalScale.toLong() - augend.decimalScale) < 10000 ||
            kotlin.math.abs(adjustedExponent() - augend.adjustedExponent()) <= mc.precision.toLong() + 3)
            return rawSum585(augend, false, mc) ?: addValue(augend).roundValue(mc)
        // Keep a guard digit plus a sticky digit when the exponent gap is enormous.
        // Replacing a remote tail by a signed sticky unit preserves every rounding decision.
        val large = if (adjustedExponent() > augend.adjustedExponent()) this else augend
        val small = if (large === this) augend else this
        val workingScale = maxOf(large.decimalScale.toLong(), mc.precision.toLong() + 2L - large.adjustedExponent())
        val sticky = small.stickyAt(workingScale)
        val result = large.rawSum585(sticky, false, mc) ?: large.addValue(sticky).roundValue(mc)
        val preferred = maxOf(decimalScale, augend.decimalScale)
        return if (result.signum() == 0) result.setScale(preferred) else result.stripTo(preferred)
    }
    private fun checkAdditionAlignment(other: BigDecimal, mc: MathContext) {
        if (decimalScale == other.decimalScale) return
        val dominant = if (decimalScale <= other.decimalScale) this else other
        val remote = if (dominant === this) other else this
        val leadingPosition = remote.decimalScale.toLong() - remote.precision() + 1
        val roundingScale = dominant.decimalScale.toLong() - dominant.precision() + mc.precision
        // A guard position may exceed the scale range even when the aligned
        // exact sum and its rounded result are representable. Do not wrap it.
        val alignmentThreshold = dominant.decimalScale.toLong() + 2
        if (leadingPosition > alignmentThreshold && leadingPosition > roundingScale + 2)
            checkedScale(maxOf(dominant.decimalScale.toLong(), roundingScale) + 3)
    }
    private fun stickyAt(scale: Long): BigDecimal {
        val coefficient = coefficientSnapshot()
        if (signum() == 0 || decimalScale.toLong() <= scale) return this
        val drop = decimalScale.toLong() - scale
        val reportedPrecision = precision()
        if (drop == reportedPrecision.toLong() && coefficient.decimalCompactValue() != 0L &&
            drop == storedPrecision().toLong()) {
            // A nonzero D-digit coefficient is strictly below 10^D.
            // Its quotient is zero and its remainder supplies the signed tail.
            return BigDecimal(BigInteger.valueOf(signum().toLong()), checkedScale(scale + 1))
        }
        if (drop <= reportedPrecision) {
            val divisor = tenPow(drop)
            val qr = coefficient.divideAndRemainder(divisor)
            val sticky = if (qr[1].signum() == 0) qr[0] else qr[0].multiply(BigInteger.TEN).add(BigInteger.valueOf(signum().toLong()))
            return if (qr[1].signum() == 0) BigDecimal(sticky, checkedScale(scale)) else BigDecimal(sticky, checkedScale(scale + 1))
        }
        // This tail is strictly less than one unit at 'scale'. Replacing it by
        // a whole unit can carry through 999... or cancel a retained +1 digit.
        return BigDecimal(BigInteger.valueOf(signum().toLong()), checkedScale(scale + 1))
    }
    open fun subtract(subtrahend: BigDecimal): BigDecimal {
        val operand = subtrahend
        rawSum585(operand, true, null)?.let { return it }
        val scale = maxOf(decimalScale, operand.decimalScale)
        return BigDecimal(scaledCoefficient(scale).subtract(operand.scaledCoefficient(scale)), scale)
    }
    open fun subtract(subtrahend: BigDecimal, mc: MathContext): BigDecimal {
        val context = mc
        return if (context.precision == 0) subtract(subtrahend)
            else add(subtrahend.negate(), context)
    }
    open fun multiply(multiplicand: BigDecimal): BigDecimal = multiplyValue(multiplicand)
    private fun multiplyValue(multiplicand: BigDecimal): BigDecimal {
        val scale = scaleForValue(decimalScale.toLong() + multiplicand.decimalScale, storedSign529() == 0)
        if (representation == 1 && multiplicand.representation == 1) {
            val a = compactValue529(); val b = multiplicand.compactValue529()
            val result = a * b
            if (CompactInteger529.multiplyFits(a, b, result)) return BigDecimal(result, scale)
        }
        val coefficient = coefficientSnapshot()
        val multiplicandCoefficient529 = multiplicand.coefficientSnapshot()
        val otherCoefficient = multiplicandCoefficient529
        return BigDecimal(coefficient.multiply(otherCoefficient), scale)
    }
    open fun multiply(multiplicand: BigDecimal, mc: MathContext): BigDecimal {
        val context = mc
        if (context.precision == 0) return multiply(multiplicand)
        val operand = multiplicand
        val logicalScale = decimalScale.toLong() + operand.decimalScale
        if (representation == 1 && operand.representation == 1) {
            val a = compactValue529(); val b = operand.compactValue529()
            val product = a * b
            if (CompactInteger529.multiplyFits(a, b, product)) {
                if (logicalScale <= Int.MAX_VALUE || a == 0L || b == 0L)
                    return BigDecimal(product, scaleForValue(logicalScale, a == 0L)).roundValue(context)
                val neutral = BigDecimal(product, 0)
                if (neutral.storedPrecision() <= context.precision) checkedScale(logicalScale)
                val rounded = neutral.round(context)
                return rounded.withScale529(checkedScale(logicalScale + rounded.decimalScale))
            }
        }
        val coefficient = coefficientSnapshot()
        val operandCoefficient = operand.coefficientSnapshot()
        // Preserve exact, low-scale and zero/source behavior. Only mandatory
        // precision reduction of a supported nonzero product rescues HIGH scale.
        if (logicalScale <= Int.MAX_VALUE || coefficient.signum() == 0 || operandCoefficient.signum() == 0) {
            // Retain multiplyValue's scale/sign prefix before raw selection.
            val scale = scaleForValue(logicalScale, coefficient.signum() == 0)
            val raw = coefficient.roundedDecimalProduct580(operandCoefficient, scale.toLong(), context)
            if (raw != null) return BigDecimal(raw.first, raw.second)
            return BigDecimal(coefficient.multiply(operandCoefficient), scale).roundValue(context)
        }
        val raw = coefficient.roundedDecimalProduct580(operandCoefficient, logicalScale, context)
        if (raw != null) return BigDecimal(raw.first, raw.second)
        val product = BigDecimal(coefficient.multiply(operandCoefficient), 0)
        if (product.storedPrecision() <= context.precision) checkedScale(logicalScale)
        // Rounding decisions depend on the coefficient, not the logical scale.
        // The neutral result's nonpositive scale includes any extra carry drop.
        val rounded = product.round(context)
        return BigDecimal(rounded.coefficientSnapshot(), checkedScale(logicalScale + rounded.decimalScale))
    }

    open fun divide(divisor: BigDecimal): BigDecimal {
        val operand = divisor
        checkDivisionByZero(operand)
        return try { divideExactly(operand) }
        catch (_: ArithmeticException) {
            throw ArithmeticException("Non-terminating decimal expansion; no exact representable decimal result.")
        }
    }
    private fun checkDivisionByZero(divisor: BigDecimal) {
        if (divisor.signum() == 0)
            throw ArithmeticException(if (signum() == 0) "Division undefined" else "Division by zero")
    }
    private fun divideExactly(divisor: BigDecimal): BigDecimal {
        val preferred = decimalScale.toLong() - divisor.decimalScale
        if (signum() == 0) return BigDecimal(BigInteger.ZERO, saturatedScale(preferred))
        // Retain legacy metadata diagnostics, but a manufactured normalization
        // scale cannot reject a supported exact quotient of truthful operands.
        val exactPrecision = (precision().toLong() + (10L * divisor.precision() + 2) / 3)
            .coerceAtMost(Int.MAX_VALUE.toLong()).toInt()
        checkDivisionNormalization(divisor, exactPrecision)
        compactExactQuotient529(divisor)?.let { return it.stripTo(saturatedScale(preferred)) }
        val quotient = exactDivisionCoefficient(divisor)
            ?: throw ArithmeticException("Non-terminating decimal expansion")
        return BigDecimal(quotient.first, checkedScale(quotient.second)).stripTo(saturatedScale(preferred))
    }
    // Keep scale wide: an unsupported exact cohort may still round into range.
    private fun exactDivisionCoefficient(divisor: BigDecimal, forContext: Boolean = false): Pair<BigInteger, Long>? {
        val coefficient = coefficientSnapshot()
        val divisorCoefficient529 = divisor.coefficientSnapshot()
        val gcd = coefficient.gcd(divisorCoefficient529)
        var denominator = divisorCoefficient529.abs().divide(gcd)
        var numerator = coefficient.divide(gcd)
        val divisorSign = if (forContext) divisorCoefficient529.signum() else divisor.signum()
        if (divisorSign < 0) numerator = numerator.negate()
        val twos = denominator.getLowestSetBit()
        denominator = denominator.shiftRight(twos)
        val five = BigInteger.valueOf(5)
        var fives = 0
        while (true) {
            val qr = denominator.divideAndRemainder(five)
            if (qr[1].signum() != 0) break
            denominator = qr[0]; fives++
        }
        if (denominator != BigInteger.ONE) return null
        val digits = maxOf(twos, fives)
        val scale = decimalScale.toLong() - divisor.decimalScale + digits
        // A natural exact scale below MIN would need a different padded cohort;
        // that rescue is outside the proved contract. Above MAX, rounding may
        // still select fewer digits and carry into a supported final scale.
        if (scale < Int.MIN_VALUE.toLong()) checkedScale(scale)
        if (scale > Int.MAX_VALUE.toLong()) {
            if (forContext) return null
            checkedScale(scale)
        }
        numerator = numerator.shiftLeft(digits - twos).multiply(five.pow(digits - fives))
        return numerator to scale
    }
    open fun divide(divisor: BigDecimal, roundingMode: RoundingMode): BigDecimal = divide(divisor, decimalScale, roundingMode)
    @Deprecated("Use RoundingMode")
    open fun divide(divisor: BigDecimal, roundingMode: Int): BigDecimal {
        val mode = RoundingMode.valueOf(roundingMode)
        return divide(divisor, mode)
    }
    @Deprecated("Use RoundingMode")
    open fun divide(divisor: BigDecimal, scale: Int, roundingMode: Int): BigDecimal {
        val mode = RoundingMode.valueOf(roundingMode)
        return divide(divisor, scale, mode)
    }
    open fun divide(divisor: BigDecimal, scale: Int, roundingMode: RoundingMode): BigDecimal {
        if (divisor.signum() == 0) failScaledZeroDivision(divisor.decimalScale, scale)
        compactQuotient529(divisor, scale, roundingMode)?.let { return it }
        val coefficient = coefficientSnapshot()
        val divisorCoefficient529 = divisor.coefficientSnapshot()
        val shift = scale.toLong() + divisor.decimalScale - decimalScale
        // Certify before disposable scale checks or whole coefficient Strings.
        val raw = coefficient.fixedScaleDecimalQuotient592(divisorCoefficient529, shift, roundingMode)
        if (raw != null) return BigDecimal(raw, scale)
        // Retain useful range diagnostics only when an enormous coefficient
        // really is required. Zero/tiny results and bounded net shifts must not
        // fail merely because a temporary scale sum is outside Int.
        if (coefficient.signum() != 0 && kotlin.math.abs(shift) > 646456992L && quotientExponent(divisor) + scale >= -1) {
            scaleForValue(scale.toLong() + divisor.decimalScale, false)
            if (shift <= 0) scaleForValue(decimalScale.toLong() - scale, false)
        }
        return divideScaledCoefficients(divisor, coefficient, divisorCoefficient529, scale, roundingMode, shift >= 0, kotlin.math.abs(shift))
    }
    private fun divideScaledCoefficients(divisor: BigDecimal, coefficient: BigInteger,
        divisorCoefficient529: BigInteger, scale: Int, roundingMode: RoundingMode,
        scaleNumerator: Boolean, raise: Long, checkSourceSign: Boolean = true): BigDecimal {
        if (checkSourceSign && signum() == 0) return BigDecimal(BigInteger.ZERO, scale)
        // Avoid constructing an unbounded power of ten for a quotient strictly below 0.1.
        val exponent = quotientExponent(divisor) + decimalScale - divisor.decimalScale +
            if (scaleNumerator) raise else -raise
        if (exponent < -1 && coefficient.signum() != 0 && divisorCoefficient529.signum() != 0) {
            val sign = if (checkSourceSign) signum() * divisor.signum()
                else coefficient.signum() * divisorCoefficient529.signum()
            return BigDecimal(roundTiny(sign, roundingMode), scale)
        }
        if (raise > 646456992L) throw ArithmeticException("BigInteger would overflow supported range")
        val numerator = if (scaleNumerator && raise > 0) coefficient.multiply(tenPow(raise)) else coefficient
        val denominator = if (!scaleNumerator && raise > 0) divisorCoefficient529.multiply(tenPow(raise)) else divisorCoefficient529
        if (!checkSourceSign && denominator.signum() == 0 && numerator.decimalCompactValue() != null)
            throw ArithmeticException("/ by zero")
        return BigDecimal(roundedDivide(numerator, denominator, roundingMode), scale)
    }
    private fun failScaledZeroDivision(zeroScale: Int, target: Int): Nothing {
        val coefficient = coefficientSnapshot()
        val compact = coefficient.decimalCompactValue()
        val adjusted = scaleForValue(target.toLong() + zeroScale, compact == 0L)
        val numerator = adjusted > decimalScale
        // Public-JDK probes expose different staging on the two rescaling paths.
        // The final Int subtraction deliberately retains its wraparound behavior.
        val raise = if (numerator) target + zeroScale - decimalScale
            else saturatedScale(decimalScale.toLong() - target) - zeroScale
        if (raise > 646456992) throw ArithmeticException("BigInteger would overflow supported range")
        if (compact == null || raise > 18) throw ArithmeticException("BigInteger divide by zero")
        if (numerator && raise > 0) {
            var factor = 1L
            repeat(raise) { factor *= 10 }
            if (kotlin.math.abs(compact) > Long.MAX_VALUE / factor)
                throw ArithmeticException("BigInteger divide by zero")
        }
        throw ArithmeticException("/ by zero")
    }
    open fun divide(divisor: BigDecimal, mc: MathContext): BigDecimal {
        val context = mc
        if (context.precision == 0) return divide(divisor)
        val operand = divisor
        checkDivisionByZero(operand)
        val preferredScale = decimalScale.toLong() - operand.decimalScale
        if (signum() == 0) return BigDecimal(BigInteger.ZERO, saturatedScale(preferredScale))
        val sourceDigits = precision()
        val divisorDigits = operand.precision()
        if (this::class == BigDecimal::class && operand::class == BigDecimal::class &&
            representation == 1 && operand.representation == 1) {
            val exact = compactExactQuotient529(operand)
            if (exact != null && exact.storedPrecision() <= context.precision)
                return exact.stripTo(saturatedScale(preferredScale))
            val normalizedCompact = CompactInteger529.compareDecimalSignificands(compactValue529(), operand.compactValue529())
            val targetCompact = preferredScale + context.precision.toLong() + divisorDigits - sourceDigits -
                if (normalizedCompact > 0) 1 else 0
            if (preferredScale in Int.MIN_VALUE.toLong()..Int.MAX_VALUE.toLong() &&
                targetCompact in Int.MIN_VALUE.toLong()..Int.MAX_VALUE.toLong()) {
                val result = compactQuotient529(operand, targetCompact.toInt(), context.roundingMode)?.roundValue(context)
                if (result != null) {
                    val exactResult = compactProductEquals529(result, operand)
                    if (exactResult != null) return if (exactResult) result.stripTo(preferredScale.toInt()) else result
                }
            }
        }
        val coefficient = coefficientSnapshot()
        val operandCoefficient529 = operand.coefficientSnapshot()
        val normalized = compareValues(coefficient.abs(), sourceDigits.toLong(),
            operandCoefficient529.abs(), divisorDigits.toLong())
        val adjustment = if (normalized > 0) 1 else 0
        val targetScale = preferredScale + context.precision.toLong() + divisorDigits - sourceDigits - adjustment
        val shift = targetScale + operand.decimalScale - decimalScale
        val streamed = coefficient.roundedDecimalQuotient581(operandCoefficient529, preferredScale,
            targetScale, shift, sourceDigits, divisorDigits, normalized, context)
        if (streamed != null) return BigDecimal(streamed.first, streamed.second)
        // Only observed truthful metadata enters the mathematical repair. No
        // extra virtual calls or runtime-class exclusions change old probes.
        if ((preferredScale !in Int.MIN_VALUE.toLong()..Int.MAX_VALUE.toLong() ||
            targetScale !in Int.MIN_VALUE.toLong()..Int.MAX_VALUE.toLong() || kotlin.math.abs(shift) > 646456992L) &&
            sourceDigits == storedPrecision() && divisorDigits == operand.storedPrecision() &&
            coefficient.signum() != 0 && operandCoefficient529.signum() != 0)
            return try { divideWideContext(operand, context, preferredScale, normalized, sourceDigits, divisorDigits) }
                catch (failure: ArithmeticException) {
                    // If no supported result exists, preserve the old range diagnostic.
                    divisionScale(targetScale)
                    if (preferredScale !in Int.MIN_VALUE.toLong()..Int.MAX_VALUE.toLong()) {
                        if (kotlin.math.abs(shift) > 646456992L)
                            throw ArithmeticException("BigInteger would overflow supported range")
                        divisionScale(preferredScale)
                    }
                    throw failure
                }
        divisionScale(targetScale)
        // Public-JDK errors expose the intermediate range check before the
        // preferred/result scale checks. Keep all computations wide until then.
        if (kotlin.math.abs(shift) > 646456992L)
            throw ArithmeticException("BigInteger would overflow supported range")
        val preferred = divisionScale(preferredScale)
        val target = checkedScale(targetScale)
        val compactSource = coefficient.decimalCompactValue()
        val compactDivisor = operandCoefficient529.decimalCompactValue()
        val compactNormalization = sourceDigits <= divisorDigits && divisorDigits < 18 && context.precision < 18 &&
            compactSource != null && compactDivisor != null
        if (compactNormalization && normalizedCompactOverflow(compactSource!!, divisorDigits.toLong() - sourceDigits))
            return BigDecimal(BigInteger.ZERO, preferred)
        // The compact equality path treats normalized coefficients as a unit
        // ratio, including stored zeros whose public signum reports nonzero.
        if (compactNormalization && normalized == 0) {
            val sign = if ((coefficient.signum() < 0) != (operandCoefficient529.signum() < 0)) -1L else 1L
            return BigDecimal(BigInteger.valueOf(sign), divisionScale(preferredScale + divisorDigits - sourceDigits))
        }
        val result = divideScaledCoefficients(operand, coefficient, operandCoefficient529, target, context.roundingMode, shift >= 0,
            kotlin.math.abs(shift), checkSourceSign = false).roundValue(context)
        // Exact quotients use the nearest representable scale to the preferred scale.
        val exact = compareValues(result.coefficientSnapshot().multiply(operandCoefficient529),
            result.decimalScale.toLong() + operand.decimalScale, coefficient, decimalScale.toLong()) == 0
        return if (!exact) result
            else if (result.coefficientSnapshot().signum() == 0) BigDecimal(BigInteger.ZERO, minOf(result.decimalScale, preferred))
            else result.stripTo(preferred)
    }
    private fun divideWideContext(divisor: BigDecimal, mc: MathContext, preferred: Long,
        normalized: Int, sourceDigits: Int, divisorDigits: Int): BigDecimal {
        val coefficient = coefficientSnapshot()
        val divisorCoefficient529 = divisor.coefficientSnapshot()
        // A small exact cohort must not be padded to a huge requested precision.
        val exact = exactDivisionCoefficient(divisor, forContext = true)
        if (exact != null && exact.first.abs().toString().length <= mc.precision)
            return BigDecimal(exact.first, divisionScale(exact.second))
        val exponent = sourceDigits.toLong() - divisorDigits - if (normalized < 0) 1 else 0
        var scale = preferred + mc.precision - 1L - exponent
        val shift = scale - preferred
        if (kotlin.math.abs(shift) > 646456992L)
            throw ArithmeticException("BigInteger would overflow supported range")
        val numerator = if (shift > 0) coefficient.multiply(tenPow(shift)) else coefficient
        val denominator = if (shift < 0) divisorCoefficient529.multiply(tenPow(-shift)) else divisorCoefficient529
        val isExact = numerator.remainder(denominator).signum() == 0
        var value = roundedDivide(numerator, denominator, mc.roundingMode)
        // A new leading digit consumes one position; the division is exact here.
        if (value.abs().toString().length > mc.precision) {
            value = value.divide(BigInteger.TEN)
            scale--
        }
        if (isExact) while (scale > preferred && scale > Int.MIN_VALUE.toLong()) {
            val qr = value.divideAndRemainder(BigInteger.TEN)
            if (qr[1].signum() != 0) break
            value = qr[0]; scale--
        }
        return BigDecimal(value, divisionScale(scale))
    }
    private fun normalizedCompactOverflow(value: Long, raise: Long): Boolean {
        var magnitude = kotlin.math.abs(value)
        if (magnitude == 0L) return false
        if (raise > 18) return true
        repeat(raise.toInt()) {
            if (magnitude > Long.MAX_VALUE / 10) return true
            magnitude *= 10
        }
        return false
    }
    private fun checkDivisionNormalization(divisor: BigDecimal, precision: Int,
        sourcePrecision: Int? = null, divisorPrecision: Int? = null) {
        val adjustment = if (normalizedComparison(divisor) > 0) 1 else 0
        val divisorDigits = divisorPrecision ?: divisor.precision()
        val sourceDigits = sourcePrecision ?: precision()
        val preferred = decimalScale.toLong() - divisor.decimalScale
        val normalization = preferred + precision + divisorDigits - sourceDigits - adjustment
        if (sourceDigits == storedPrecision() && divisorDigits == divisor.storedPrecision() &&
            storedSign529() != 0 && divisor.storedSign529() != 0) return
        divisionScale(normalization)
    }
    private fun divisionScale(scale: Long): Int {
        val narrowed = scale.toInt()
        // The pinned runtime's error wording follows the narrowed Int sign,
        // including a second wrap past 2^32, rather than the original Long sign.
        if (narrowed.toLong() != scale)
            throw ArithmeticException(if (narrowed > 0) "Underflow" else "Overflow")
        return narrowed
    }
    private fun quotientExponent(divisor: BigDecimal): Long {
        //597 exact ordinary coefficient/MIN eligibility, read directly from storage.
        if (representation == 1 && divisor.representation == 1 &&
            compactValue529() != Long.MIN_VALUE && divisor.compactValue529() != Long.MIN_VALUE)
            return CompactInteger529.decimalDigits(compactValue529()).toLong() -
                CompactInteger529.decimalDigits(divisor.compactValue529()) +
                divisor.decimalScale.toLong() - decimalScale -
                if (CompactInteger529.compareDecimalSignificands(compactValue529(), divisor.compactValue529()) < 0) 1 else 0
        val coefficient = coefficientSnapshot()
        val divisorCoefficient529 = divisor.coefficientSnapshot()
        if (coefficient.decimalCompactValue() == null || divisorCoefficient529.decimalCompactValue() == null)
            return coefficient.decimalMagnitudeDigits().toLong() - divisorCoefficient529.decimalMagnitudeDigits() +
                divisor.decimalScale.toLong() - decimalScale - if (coefficient.compareDecimalSignificand(divisorCoefficient529) < 0) 1 else 0
        val a = coefficient.abs().toString()
        val b = divisorCoefficient529.abs().toString()
        return a.length.toLong() - b.length + divisor.decimalScale.toLong() - decimalScale - if (normalizedComparison(divisor) < 0) 1 else 0
    }
    private fun normalizedComparison(divisor: BigDecimal): Int {
        //597 exact ordinary coefficient/MIN eligibility, read directly from storage.
        if (representation == 1 && divisor.representation == 1 &&
            compactValue529() != Long.MIN_VALUE && divisor.compactValue529() != Long.MIN_VALUE)
            return CompactInteger529.compareDecimalSignificands(compactValue529(), divisor.compactValue529())
        val coefficient = coefficientSnapshot()
        val divisorCoefficient529 = divisor.coefficientSnapshot()
        if (coefficient.decimalCompactValue() == null || divisorCoefficient529.decimalCompactValue() == null)
            return coefficient.compareDecimalSignificand(divisorCoefficient529)
        val a = coefficient.abs().toString()
        val b = divisorCoefficient529.abs().toString()
        val length = maxOf(a.length, b.length)
        return a.padEnd(length, '0').compareTo(b.padEnd(length, '0'))
    }
    open fun divideToIntegralValue(divisor: BigDecimal): BigDecimal {
        val coefficient = coefficientSnapshot()
        val operand = divisor
        val operandCoefficient529 = operand.coefficientSnapshot()
        val preferred = saturatedScale(decimalScale.toLong() - operand.decimalScale)
        val magnitude = compareMagnitude(operand)
        if (magnitude < 0) return BigDecimal(BigInteger.ZERO, preferred)
        val sourceSign = signum()
        if (sourceSign == 0 && operand.signum() != 0)
            return BigDecimal(BigInteger.ZERO, preferred)
        // Retain the historical guard budget and metadata order for fallback.
        val sourcePrecision = precision()
        val sourceDigits = sourcePrecision.toLong()
        val divisorPrecision = operand.precision()
        val divisorDigits = 10L * divisorPrecision
        val guardDigits = divisorDigits / 3 + if (divisorDigits % 3 > 0) 1 else 0
        val sourceScale = scale()
        val divisorScale = operand.scale()
        val digits = sourceDigits + guardDigits +
            kotlin.math.abs(sourceScale.toLong() - divisorScale) + 2
        val legacyContext = MathContext(minOf(digits, Int.MAX_VALUE.toLong()).toInt(), RoundingMode.DOWN)
        val integral = coefficient.integralDecimalQuotient582(operandCoefficient529, decimalScale,
            operand.decimalScale, sourcePrecision, divisorPrecision, sourceScale, divisorScale, sourceSign, magnitude)
        if (integral != null) return BigDecimal(integral.first, integral.second)
        var result = divide(operand, legacyContext)
        if (result.decimalScale > 0) {
            result = result.setScale(0, RoundingMode.DOWN).integralStrip(preferred, preserveIdentity = true)
        }
        return if (preferred > result.decimalScale) result.setScale(preferred, ROUND_UNNECESSARY) else result
    }
    open fun divideToIntegralValue(divisor: BigDecimal, mc: MathContext): BigDecimal {
        val coefficient = coefficientSnapshot()
        val context = mc
        if (context.precision == 0) return divideToIntegralValue(divisor)
        val operand = divisor
        val operandCoefficient529 = operand.coefficientSnapshot()
        if (compareMagnitude(operand) < 0) return divideToIntegralValue(operand)
        val preferred = saturatedScale(decimalScale.toLong() - operand.decimalScale)
        var result = divide(operand, MathContext(context.precision, RoundingMode.DOWN))
        val resultScale = result.scale()
        if (resultScale < 0) {
            val certified = if (resultScale == result.decimalScale)
                coefficient.certifiedDecimalResidual583(operandCoefficient529, decimalScale,
                    operand.decimalScale, result.coefficientSnapshot(), result.decimalScale) else null
            if (certified == null) {
                val residual = subtract(result.multiply(operand))
                if (residual.compareMagnitude(operand) >= 0) throw ArithmeticException("Division impossible")
            }
        } else if (result.scale() > 0) {
            result = result.setScale(0, RoundingMode.DOWN)
        }
        if (preferred > result.scale()) {
            val room = context.precision - result.precision()
            if (room > 0) {
                val scale = result.scale()
                return result.setScale((scale.toLong() + minOf(room.toLong(), preferred.toLong() - scale)).toInt())
            }
        }
        return result.integralStrip(preferred, preserveIdentity = false)
    }
    private fun integralStrip(preferred: Int, preserveIdentity: Boolean): BigDecimal {
        val coefficient = coefficientSnapshot()
        if (coefficient.signum() == 0) {
            val scale = minOf(decimalScale, preferred)
            if (preserveIdentity && decimalScale == scale) return this
            return BigDecimal(BigInteger.ZERO, scale)
        }
        val stripped = stripTo(preferred)
        return if (preserveIdentity) stripped else BigDecimal(stripped.coefficientSnapshot(), stripped.decimalScale)
    }
    open fun divideAndRemainder(divisor: BigDecimal): Array<BigDecimal> {
        val coefficient = coefficientSnapshot()
        val divisorCoefficient529 = divisor.coefficientSnapshot()
        val quotient = divideToIntegralValue(divisor)
        val residual = coefficient.certifiedDecimalResidual583(divisorCoefficient529, decimalScale,
            divisor.decimalScale, quotient.coefficientSnapshot(), quotient.decimalScale)
        return arrayOf(quotient, if (residual == null) subtract(quotient.multiply(divisor))
            else BigDecimal(residual.first, residual.second))
    }
    open fun divideAndRemainder(divisor: BigDecimal, mc: MathContext): Array<BigDecimal> {
        val coefficient = coefficientSnapshot()
        val divisorCoefficient529 = divisor.coefficientSnapshot()
        val context = mc
        if (context.precision == 0) return divideAndRemainder(divisor)
        val quotient = divideToIntegralValue(divisor, context)
        val residual = coefficient.certifiedDecimalResidual583(divisorCoefficient529, decimalScale,
            divisor.decimalScale, quotient.coefficientSnapshot(), quotient.decimalScale)
        return arrayOf(quotient, if (residual == null) subtract(quotient.multiply(divisor))
            else BigDecimal(residual.first, residual.second))
    }
    open fun remainder(divisor: BigDecimal): BigDecimal = remainderElement(divideAndRemainder(divisor))
    open fun remainder(divisor: BigDecimal, mc: MathContext): BigDecimal = remainderElement(divideAndRemainder(divisor, mc))
    private fun remainderElement(pair: Array<BigDecimal>): BigDecimal {
        if (pair.size < 2) arrayIndexFailure(1, pair.size)
        return pair[1]
    }

    open fun round(mc: MathContext): BigDecimal = plus(mc)
    // Precision reduction only; internal callers retain operation-specific scales.
    private fun roundValue(mc: MathContext): BigDecimal {
        if (mc.precision == 0) return this
        // Keep the first public precision observation; subsequent values are
        // ordinary stored coefficients. Only publish the final decimal wrapper.
        var drop = precision() - mc.precision
        if (drop <= 0) return this
        if (representation == 1 && this::class == BigDecimal::class) {
            var compact = compactValue529()
            var scale = decimalScale
            while (drop > 0) {
                scale = checkedRoundingScale(scale.toLong() - drop)
                compact = roundedCompactPower529(compact, drop.toLong(), mc.roundingMode)
                drop = CompactInteger529.decimalDigits(compact) - mc.precision
            }
            return BigDecimal(compact, scale)
        }
        val coefficient = coefficientSnapshot()
        var value = coefficient
        var scale = decimalScale
        while (drop > 0) {
            scale = checkedRoundingScale(scale.toLong() - drop)
            value = roundedDivide(value, tenPow(drop.toLong()), mc.roundingMode)
            drop = value.decimalMagnitudeDigits() - mc.precision
        }
        return BigDecimal(value, scale)
    }
    // Convenience overloads retain the public legacy-Int dispatch used by JDK.
    open fun setScale(newScale: Int): BigDecimal = setScale(newScale, ROUND_UNNECESSARY)
    @Deprecated("Use RoundingMode")
    open fun setScale(newScale: Int, roundingMode: Int): BigDecimal = setScaleValue(newScale, RoundingMode.valueOf(roundingMode))
    open fun setScale(newScale: Int, roundingMode: RoundingMode): BigDecimal = setScale(newScale, roundingMode.ordinal)
    private fun setScaleValue(newScale: Int, roundingMode: RoundingMode): BigDecimal {
        if (decimalScale == newScale) return this
        if (signum() == 0) return BigDecimal(BigInteger.ZERO, newScale)
        val shift = newScale.toLong() - decimalScale
        if (representation == 1 && this::class == BigDecimal::class) {
            val compact = compactValue529()
            if (shift <= 0) return BigDecimal(roundedCompactPower529(compact, -shift, roundingMode), newScale)
            val scaled = scaledCompact529(compact, shift)
            if (scaled != null) return BigDecimal(scaled, newScale)
        }
        val coefficient = coefficientSnapshot()
        val value = if (shift > 0) {
            val distance = checkedScale(shift)
            if (distance > 646456992) throw ArithmeticException("BigInteger would overflow supported range")
            coefficient.multiply(tenPow(shift))
        } else {
            val digits = storedPrecision().toLong()
            when {
                -shift > digits -> roundTiny(signum(), roundingMode)
                -shift == digits -> roundSubunitCoefficient(-shift, roundingMode)
                else -> roundedDivide(coefficient, tenPow(-shift), roundingMode)
            }
        }
        return BigDecimal(value, newScale)
    }
    // Here |coefficient| has exactly 'digits' decimal digits: the quotient at
    // 10^digits is zero. 10^(digits-1) is supported even at maximum precision.
    private fun roundSubunitCoefficient(digits: Long, mode: RoundingMode): BigInteger {
        val coefficient = coefficientSnapshot()
        val qr = coefficient.abs().divideAndRemainder(tenPow(digits - 1))
        val leadingHalf = qr[0].compareTo(BigInteger.valueOf(5))
        val half = if (leadingHalf == 0 && qr[1].signum() != 0) 1 else leadingHalf
        val sign = coefficient.signum()
        return if (increment(mode, sign, half, false)) BigInteger.valueOf(sign.toLong()) else BigInteger.ZERO
    }
    open fun stripTrailingZeros(): BigDecimal = if (storedSign529() == 0) ZERO else stripTo(Int.MIN_VALUE, failOnOverflow = true)
    private fun stripTo(minScale: Int, failOnOverflow: Boolean = false): BigDecimal {
        var scale = decimalScale
        if (representation == 1) {
            var unscaled = compactValue529()
            while (unscaled != 0L && scale > minScale && unscaled % 10L == 0L) {
                unscaled /= 10L
                scale--
            }
            if (failOnOverflow && scale == Int.MIN_VALUE && unscaled != 0L && unscaled % 10L == 0L)
                throw ArithmeticException("Overflow")
            return if (scale == decimalScale) this else BigDecimal(unscaled, scale)
        }
        var value = coefficientSnapshot()
        // A one-digit probe keeps ordinary nonmultiples of ten on one division.
        // After a successful probe, prove at most nine exact digits using a
        // remainder-only pass (10^9 fits one radix-2^30 word), then divide only
        // by the proven factor. No discarded full quotient or scale overshoot.
        if (value.signum() != 0 && scale > minScale) {
            val qr = value.divideAndRemainder(BigInteger.TEN)
            if (qr[1].signum() == 0) {
                value = qr[0]; scale--
                // Select a proven exact chunk from the small remainder first.
                // Single-output remainder avoids allocating a speculative wide
                // quotient; quotient-only division then publishes only exact work.
                var twos = value.getLowestSetBit()
                while (scale > minScale && twos > 0) {
                    val chunk = minOf(9L, scale.toLong() - minScale, twos.toLong()).toInt()
                    val remainder = value.remainder(tenPow(chunk.toLong())).decimalCompactValue()!!
                    var exact = chunk
                    if (remainder != 0L) {
                        var low = remainder
                        exact = 0
                        while (low % 10L == 0L) {
                            low /= 10L
                            exact++
                        }
                    }
                    if (exact == 0) break
                    value = value.divide(tenPow(exact.toLong()))
                    scale -= exact
                    twos -= exact
                    // A nonzero remainder proves the first nonzero decimal digit
                    // before the end of this chunk, so the tail is complete.
                    if (exact < chunk) break
                }
            }
        }
        if (failOnOverflow && scale == Int.MIN_VALUE && value.signum() != 0 && value.remainder(BigInteger.TEN).signum() == 0)
            throw ArithmeticException("Overflow")
        return if (scale == decimalScale) this else BigDecimal(value, scale)
    }
    open fun movePointLeft(n: Int): BigDecimal = movePoint(decimalScale.toLong() + n)
    open fun movePointRight(n: Int): BigDecimal = movePoint(decimalScale.toLong() - n)
    private fun movePoint(scale: Long): BigDecimal {
        val coefficient = coefficientSnapshot()
        if (scale == decimalScale.toLong() && decimalScale >= 0) return this
        val result = BigDecimal(coefficient, scaleForValue(scale, coefficient.signum() == 0))
        return if (result.decimalScale < 0) result.setScale(0) else result
    }
    open fun scaleByPowerOfTen(n: Int): BigDecimal = withScale529(scaleForValue(decimalScale.toLong() - n, storedSign529() == 0))
    open fun ulp(): BigDecimal = BigDecimal(BigInteger.ONE, decimalScale)

    open fun pow(n: Int): BigDecimal {
        val coefficient = coefficientSnapshot()
        if (n !in 0..999999999) throw ArithmeticException("Invalid operation")
        if (n == 0) return ONE
        val scale = scaleForValue(decimalScale.toLong() * n, coefficient.signum() == 0)
        return BigDecimal(coefficient.pow(n), scale)
    }
    open fun pow(n: Int, mc: MathContext): BigDecimal {
        val coefficient = coefficientSnapshot()
        if (mc.precision == 0) return pow(n)
        if (n !in -999999999..999999999) throw ArithmeticException("Invalid operation")
        if (n == 0) return ONE
        val magnitude = if (n < 0) -n else n
        val exponentDigits = magnitude.toString().length
        if (exponentDigits > mc.precision) throw ArithmeticException("Invalid operation")
        val mathematicalPrecision = mc.precision.toLong() + exponentDigits + 1
        if (mathematicalPrecision > 646456992L) {
            val base = coefficient.decimalPowerState593(decimalScale)
            if (base != null) {
                val mode = mc.roundingMode
                var accumulator = DecimalPower593.accumulate(base, magnitude, mathematicalPrecision, mode)
                if (n < 0) accumulator = DecimalPower593.reciprocal(accumulator,
                    mathematicalPrecision, mc.precision, mode)
                val result = DecimalPower593.publish(DecimalPower593.round(accumulator, mc.precision.toLong(), mode))
                return BigDecimal(result.first, result.second)
            }
        }
        // Every supported coefficient has fewer digits than Int.MAX_VALUE.
        // Both this cap and a larger mathematical working precision therefore
        // retain all its digits; an internal context must not wrap negative.
        val workPrecision = minOf(mc.precision.toLong() + exponentDigits + 1, Int.MAX_VALUE.toLong()).toInt()
        val work = MathContext(workPrecision, mc.roundingMode)
        var result = ONE
        val bits = 32 - magnitude.countLeadingZeroBits()
        for (i in bits - 1 downTo 0) {
            result = result.multiply(result, work)
            if (magnitude and (1 shl i) != 0) result = result.multiply(this, work)
        }
        if (n < 0) result = ONE.divide(result, work)
        return result.roundValue(mc)
    }

    open fun sqrt(mc: MathContext): BigDecimal {
        val coefficient = coefficientSnapshot()
        val sign = signum()
        if (sign < 0) throw ArithmeticException("Attempted square root of negative BigDecimal")
        val context = mc
        val preferred = decimalScale / 2
        if (sign == 0) return BigDecimal(BigInteger.ZERO, preferred)
        // Use scale parity and integer squares before normalization, decimal
        // input formatting, or any precision-derived working scale/power.
        // A supported radicand can exceed an engine's String limit while its
        // root coefficient (at most half as many digits) remains supported.
        val finiteRoot = finiteExactSquareRoot()
        if (finiteRoot != null) return finiteSquareRootResult(finiteRoot, context, preferred)
        if (context.precision == 0 || context.roundingMode == RoundingMode.UNNECESSARY) {
            val normalized = stripTrailingZeros()
            if (normalized.decimalScale and 1 != 0) throw ArithmeticException("Computed square root not exact.")
            val root = if (normalized.unscaledValue() == BigInteger.ONE) arrayOf(BigInteger.ONE, BigInteger.ZERO)
                else normalized.unscaledValue().sqrtAndRemainder()
            if (root[1].signum() != 0 || (context.precision > 0 && root[0].abs().toString().length > context.precision))
                throw ArithmeticException("Computed square root not exact.")
            val exact = BigDecimal(root[0], normalized.decimalScale / 2).stripTo(preferred)
            val rounded = exact.roundValue(context)
            val targetScale = if (context.precision == 0) preferred else minOf(preferred.toLong(),
                rounded.decimalScale.toLong() + context.precision - rounded.storedPrecision()).toInt()
            if (rounded.coefficientSnapshot().signum() == 0) return BigDecimal(BigInteger.ZERO, targetScale)
            return if (rounded.decimalScale < targetScale) rounded.setScaleValue(targetScale, RoundingMode.UNNECESSARY) else rounded
        }
        // A subclass can report a positive sign for a stored zero.
        val coefficientSign = coefficient.signum()
        if (coefficientSign == 0) {
            precision()
            return BigDecimal(BigInteger.ZERO, preferred)
        }
        val exponent = adjustedExponent()
        val rootExponent = if (exponent < 0) (exponent - 1) / 2 else exponent / 2
        val workingScale = context.precision.toLong() - 1 - rootExponent
        val shift = 2L * workingScale - decimalScale
        // Keep the original reported-precision route for misleading metadata.
        // On the stored-value route a rejected materialization bound selects
        // raw scratch, never a new public precision/coefficient range limit.
        if (coefficientSign > 0 && shift > 0 && context.precision > Int.MAX_VALUE / 8) {
            val digits = storedPrecision()
            if (exponent == digits.toLong() - decimalScale - 1) {
                // The complete finite-root prefix already returned or declined.
                // An ordinary irrational root requires p significant digits;
                // a shorter numerically equal cohort cannot replace them.
                if (DecimalSquareRoot579.inexactPrecisionOverflows(context.precision))
                    throw ArithmeticException("BigInteger overflow")
                if (!DecimalSquareRoot579.canMaterialize(digits, shift, workingScale)) {
                    val result = DecimalSquareRoot579.normalized(coefficient, shift,
                        context.precision, workingScale, preferred, context.roundingMode)
                    return BigDecimal(result.first, result.second)
                }
            }
        }
        val scale = checkedScale(workingScale)
        val numerator = if (shift >= 0) coefficient.multiply(tenPow(shift)) else coefficient
        val denominator = if (shift < 0) tenPow(-shift) else BigInteger.ONE
        val qr = numerator.divideAndRemainder(denominator)
        var root = qr[0].sqrt()
        val delta = qr[0].subtract(root.multiply(root))
        val exact = delta.signum() == 0 && qr[1].signum() == 0
        if (!exact) {
            // n = q*d+r, a = floor(sqrt(q)), delta = q-a*a.
            // The midpoint sign is sign((4*(delta-a)-1)*d+4*r).
            // Unequal delta/a decides it; equality needs only r versus d/4.
            val rootComparison = delta.compareTo(root)
            val half = if (rootComparison != 0) rootComparison else {
                val quarter = denominator.divideAndRemainder(BigInteger.valueOf(4))
                val remainderComparison = qr[1].compareTo(quarter[0])
                if (remainderComparison != 0) remainderComparison else -quarter[1].signum()
            }
            if (increment(context.roundingMode, 1, half, root.testBit(0))) root = root.add(BigInteger.ONE)
        }
        val result = BigDecimal(root, scale).roundValue(context)
        return if (exact) result.stripTo(preferred) else result
    }

    private fun finiteExactSquareRoot(): Pair<BigInteger, Int>? {
        val coefficient = coefficientSnapshot()
        if (coefficient.signum() <= 0) return null
        val odd = decimalScale and 1 != 0
        val integer = if (odd) {
            // For odd scale, a finite root requires c = 10*r^2. Dividing by
            // ten keeps the intermediate within the input's supported range.
            val qr = coefficient.divideAndRemainder(BigInteger.TEN)
            if (qr[1].signum() != 0) return null
            qr[0]
        } else coefficient
        val root = integer.sqrtAndRemainder()
        if (root[1].signum() != 0) return null
        val scale = if (odd) (decimalScale.toLong() - 1) / 2 else decimalScale.toLong() / 2
        return root[0] to checkedScale(scale)
    }

    private fun finiteSquareRootResult(root: Pair<BigInteger, Int>, context: MathContext, preferred: Int): BigDecimal {
        // Rounding drops at most the root's own digits. The unrounded scale
        // equals preferred, or is one less for negative odd input scales.
        // Padding therefore needs at most one zero, never precision-sized powers.
        val rounded = BigDecimal(root.first, root.second).roundValue(context)
        val target = if (context.precision == 0) preferred else minOf(preferred.toLong(),
            rounded.decimalScale.toLong() + context.precision - rounded.storedPrecision()).toInt()
        return if (rounded.decimalScale < target)
            rounded.setScaleValue(target, RoundingMode.UNNECESSARY) else rounded
    }

    override fun compareTo(other: BigDecimal): Int {
        // Physical MIN storage is compact, but the established comparison
        // shortcut excludes it and observes both public signum callbacks.
        if (decimalScale == other.decimalScale && representation == 1 && other.representation == 1 &&
            compactValue529() != Long.MIN_VALUE && other.compactValue529() != Long.MIN_VALUE) {
            val order = compactValue529().compareTo(other.compactValue529())
            return if (order < 0) -1 else if (order > 0) 1 else 0
        }
        val coefficient = coefficientSnapshot()
        val operand = other
        val operandCoefficient529 = operand.coefficientSnapshot()
        if (decimalScale == operand.decimalScale) {
            val left = coefficient.decimalCompactValue()
            val right = operandCoefficient529.decimalCompactValue()
            if (left != null && right != null) {
                val order = left.compareTo(right)
                return if (order < 0) -1 else if (order > 0) 1 else 0
            }
        }
        val leftSign = signum()
        val rightSign = operand.signum()
        if (leftSign != rightSign) return if (leftSign < rightSign) -1 else 1
        if (leftSign == 0) return 0
        val magnitude = compareMagnitude(operand)
        // Normalize before reversing a negative receiver: primitive comparison
        // contracts promise a sign, not an exact unit or a safely negatable Int.
        return when {
            magnitude < 0 -> if (leftSign > 0) -1 else 1
            magnitude > 0 -> if (leftSign > 0) 1 else -1
            else -> 0
        }
    }
    private fun compareMagnitude(other: BigDecimal): Int {
        if (representation == 1 && other.representation == 1) {
            val left = compactValue529(); val right = other.compactValue529()
            if (left == 0L || right == 0L) return (if (left == 0L) 0 else 1).compareTo(if (right == 0L) 0 else 1)
            val negativeLeft = if (left > 0) -left else left
            val negativeRight = if (right > 0) -right else right
            if (decimalScale == other.decimalScale) return negativeRight.compareTo(negativeLeft)
            val exponent = adjustedExponent().compareTo(other.adjustedExponent())
            if (exponent != 0) return exponent
            return CompactInteger529.compareDecimalSignificands(left, right)
        }
        val coefficient = coefficientSnapshot()
        val otherCoefficient529 = other.coefficientSnapshot()
        val left = coefficient.abs()
        val right = otherCoefficient529.abs()
        if (left.signum() == 0 || right.signum() == 0) return left.signum().compareTo(right.signum())
        if (decimalScale == other.decimalScale) return left.compareTo(right)
        val exponent = adjustedExponent().compareTo(other.adjustedExponent())
        if (exponent != 0) return exponent
        // Keep the reported exponent decision above, including subclass
        // precision answers. Only the equal-exponent significand step reads
        // exact stored digit counts; it is independent of those answers.
        return left.compareDecimalSignificand(right)
    }
    open fun min(value: BigDecimal): BigDecimal = if (compareTo(value) <= 0) this else value
    open fun max(value: BigDecimal): BigDecimal = if (compareTo(value) >= 0) this else value
    override fun equals(other: Any?): Boolean = other is BigDecimal && decimalScale == other.decimalScale &&
        if (representation == 1 && other.representation == 1) compactHigh == other.compactHigh && compactLow == other.compactLow
        else coefficientSnapshot() == other.coefficientSnapshot()
    override fun hashCode(): Int {
        if (representation != 1) return 31 * wideCoefficient.hashCode() + decimalScale
        val value = compactValue529()
        val magnitude = if (value < 0) -value else value
        val hash = (31L * (magnitude ushr 32).toInt() + (magnitude and 0xffffffffL)).toInt()
        return 31 * (if (value < 0) -hash else hash) + decimalScale
    }
    private fun compactExactQuotient529(divisor: BigDecimal): BigDecimal? {
        if (this::class != BigDecimal::class || divisor::class != BigDecimal::class ||
            representation != 1 || divisor.representation != 1) return null
        val a = compactValue529(); val b = divisor.compactValue529()
        if (a == 0L || b == 0L) return null
        var x = if (a > 0) -a else a
        var y = if (b > 0) -b else b
        while (y != 0L) { val remainder = x % y; x = y; y = remainder }
        // gcd is negative, including MIN. Reject only this optimization's
        // positive2^63 quotient; the general exact path still handles it.
        if (x == -1L && (a == Long.MIN_VALUE || b == Long.MIN_VALUE)) return null
        var numerator = a / x
        var denominator = b / x
        if (denominator < 0) {
            if (numerator == Long.MIN_VALUE || denominator == Long.MIN_VALUE) return null
            numerator = -numerator; denominator = -denominator
        }
        val twos = denominator.countTrailingZeroBits()
        denominator = denominator ushr twos
        var fives = 0
        while (denominator % 5 == 0L) { denominator /= 5; fives++ }
        if (denominator != 1L) return null
        val digits = maxOf(twos, fives)
        repeat(digits - twos) {
            val next = numerator * 2L
            if (!CompactInteger529.multiplyFits(numerator, 2L, next)) return null
            numerator = next
        }
        repeat(digits - fives) {
            val next = numerator * 5L
            if (!CompactInteger529.multiplyFits(numerator, 5L, next)) return null
            numerator = next
        }
        val scale = decimalScale.toLong() - divisor.decimalScale + digits
        if (scale !in Int.MIN_VALUE.toLong()..Int.MAX_VALUE.toLong()) return null
        return BigDecimal(numerator, scale.toInt())
    }

    private fun compactProductEquals529(quotient: BigDecimal, divisor: BigDecimal): Boolean? {
        if (quotient.representation != 1) return null
        val q = quotient.compactValue529(); val d = divisor.compactValue529()
        val product = q * d
        if (!CompactInteger529.multiplyFits(q, d, product)) return null
        val productScale = quotient.decimalScale.toLong() + divisor.decimalScale
        val commonScale = maxOf(decimalScale.toLong(), productScale)
        val left = scaledCompact529(compactValue529(), commonScale - decimalScale) ?: return null
        val right = scaledCompact529(product, commonScale - productScale) ?: return null
        return left == right
    }

    private fun compactQuotient529(divisor: BigDecimal, scale: Int, mode: RoundingMode): BigDecimal? {
        if (this::class != BigDecimal::class || divisor::class != BigDecimal::class ||
            representation != 1 || divisor.representation != 1) return null
        var a = compactValue529(); var b = divisor.compactValue529()
        if (b == 0L) return null
        val shift = scale.toLong() + divisor.decimalScale - decimalScale
        if (a == 0L) return BigDecimal(0L, scale)
        if (shift >= 0) a = scaledCompact529(a, shift) ?: return null
        else b = scaledCompact529(b, -shift) ?: return null
        if (a == Long.MIN_VALUE && b == -1L) return null
        val quotient = a / b
        val remainder = a % b
        if (remainder == 0L) return BigDecimal(quotient, scale)
        val sign = if ((a < 0) == (b < 0)) 1 else -1
        val negativeRemainder = if (remainder > 0) -remainder else remainder
        val negativeDivisor = if (b > 0) -b else b
        val half = (negativeDivisor - negativeRemainder).compareTo(negativeRemainder)
        return BigDecimal(if (increment(mode, sign, half, quotient and 1L != 0L)) quotient + sign else quotient, scale)
    }

    private fun roundedCompactPower529(value: Long, drop: Long, mode: RoundingMode): Long {
        if (drop == 0L || value == 0L) return value
        val sign = CompactInteger529.sign(value)
        if (drop >= 19) {
            val negative = if (value > 0) -value else value
            val half = if (drop > 19) -1 else (-5000000000000000000L).compareTo(negative)
            return if (increment(mode, sign, half, false)) sign.toLong() else 0L
        }
        var divisor = 1L
        repeat(drop.toInt()) { divisor *= 10 }
        val quotient = value / divisor
        val remainder = value % divisor
        if (remainder == 0L) return quotient
        val magnitude = if (remainder < 0) -remainder else remainder
        val half = magnitude.compareTo(divisor - magnitude)
        return if (increment(mode, sign, half, quotient and 1L != 0L)) quotient + sign else quotient
    }

    private fun scaledCompact529(value: Long, raise: Long): Long? {
        if (value == 0L) return 0L
        if (raise > 18) return null
        var result = value
        repeat(raise.toInt()) {
            val next = result * 10L
            if (!CompactInteger529.multiplyFits(result, 10L, next)) return null
            result = next
        }
        return result
    }

    private fun withScale529(scale: Int): BigDecimal =
        // Restored coefficient subtypes retain the constructor's virtual byte
        // normalization; a direct storage copy would silently bypass it.
        if (representation == 2 && wideCoefficient!!::class != BigInteger::class) BigDecimal(wideCoefficient, scale)
        else BigDecimal(scale, compactHigh, compactLow, representation, wideCoefficient)
    private fun adjustedExponent(): Long = precision().toLong() - decimalScale - 1
    private fun scaledCoefficient(scale: Int): BigInteger {
        val coefficient = coefficientSnapshot()
        if (coefficient.signum() == 0) return coefficient
        val distance = checkedScale(scale.toLong() - decimalScale)
        return coefficient.multiply(tenPow(distance.toLong()))
    }

    @kotlin.concurrent.Volatile
    @kotlin.jvm.Transient
    private var stringCache: String? = null

    /**
     * Stream the selected existing layout, without scale-sized padding or a whole result String.
     * maxChars is opt-in; ordinary values are fully planned before the first destination write.
     * A subtype retains its existing virtual formatter (whose own allocations cannot be bounded here).
     */
    fun appendTo(destination: Appendable, format: DecimalTextFormat = DecimalTextFormat.CANONICAL,
        maxChars: Long? = null): Long {
        checkTextLimit537(0, maxChars)
        if (this::class != BigDecimal::class || (representation == 2 && wideCoefficient!!::class != BigInteger::class)) {
            val text = legacyText537(format)
            checkTextLimit537(text.length.toLong(), maxChars)
            destination.append(text)
            return text.length.toLong()
        }
        if (format == DecimalTextFormat.CANONICAL) stringCache?.let { cached ->
            checkTextLimit537(cached.length.toLong(), maxChars)
            var start = 0
            while (start < cached.length) {
                val end = minOf(start.toLong() + 256, cached.length.toLong()).toInt()
                destination.append(cached, start, end)
                start = end
            }
            return cached.length.toLong()
        }
        val layout = textLayout537(format, maxChars)
        layout.write(AppendableSink537(destination))
        return layout.length
    }

    /** Returns the characters written; a short or invalid slice is unchanged by this writer. */
    fun writeTo(destination: CharArray, offset: Int = 0, length: Int = destination.size - offset,
        format: DecimalTextFormat = DecimalTextFormat.CANONICAL): Int {
        checkTextBuffer537(destination, offset, length)
        if (this::class != BigDecimal::class || (representation == 2 && wideCoefficient!!::class != BigInteger::class)) {
            val text = legacyText537(format)
            checkTextLimit537(text.length.toLong(), length.toLong())
            ArraySink537(destination, offset).text(text, 0, text.length)
            return text.length
        }
        if (format == DecimalTextFormat.CANONICAL) stringCache?.let { cached ->
            checkTextLimit537(cached.length.toLong(), length.toLong())
            cached.toCharArray(destination, offset, 0, cached.length)
            return cached.length
        }
        val layout = textLayout537(format, length.toLong())
        layout.write(ArraySink537(destination, offset))
        return layout.length.toInt()
    }

    private fun legacyText537(format: DecimalTextFormat): String = when (format) {
        DecimalTextFormat.CANONICAL -> toString()
        DecimalTextFormat.PLAIN -> toPlainString()
        DecimalTextFormat.ENGINEERING -> toEngineeringString()
    }

    private fun textLayout537(format: DecimalTextFormat, maxChars: Long?): DecimalLayout537 {
        val sign = storedSign529()
        val prefix = if (sign < 0) 1L else 0L
        val lowerDigits = if (representation == 1) CompactInteger529.decimalDigits(compactValue529()).toLong()
            else wideCoefficient!!.decimalDigitLowerBound().toLong()
        val lowerLength = when {
            format == DecimalTextFormat.PLAIN && decimalScale < 0 && sign == 0 -> 1L
            format == DecimalTextFormat.PLAIN && decimalScale <= 0 -> lowerDigits - decimalScale.toLong() + prefix
            format == DecimalTextFormat.PLAIN -> maxOf(lowerDigits + 1, decimalScale.toLong() + 2) + prefix
            else -> lowerDigits + prefix
        }
        checkTextLimit537(lowerLength, maxChars)
        val digits = if (representation == 1) BigInteger.compactRadixDigits529(compactValue529(), 10, maxChars?.minus(prefix))
            else wideCoefficient!!.radixDigits537(10, maxChars?.minus(prefix))
        val layout = DecimalLayout537(digits, decimalScale, sign < 0, format)
        checkTextLimit537(layout.length, maxChars)
        return layout
    }

    override fun toString(): String {
        stringCache?.let { return it }
        return canonicalStringValue().also { stringCache = it }
    }

    private fun canonicalStringValue(): String {
        val digits = magnitudeDigits529()
        val sign = layoutSign()
        val exponent = digits.length.toLong() - decimalScale - 1
        if (decimalScale >= 0 && exponent >= -6) return canonicalPlain(digits, sign)
        val body = if (digits.length == 1) digits else digits[0] + "." + digits.substring(1)
        return (if (sign < 0) "-" else "") + body + exponentText(exponent)
    }
    private fun magnitudeDigits529(): String = if (representation == 1) compactValue529().toString().removePrefix("-")
        else wideCoefficient!!.abs().toString()
    private fun layoutSign(): Int {
        val compact = if (representation == 1 && compactValue529() != Long.MIN_VALUE) compactValue529() else null
        val directSign = decimalScale == 0 || (decimalScale == 2 && compact != null &&
            compact >= 0 && compact < Int.MAX_VALUE.toLong())
        return if (directSign) storedSign529() else signum()
    }
    private fun canonicalPlain(digits: String, sign: Int): String {
        if (plainStringLength(digits.length, sign) > Int.MAX_VALUE.toLong()) plainStringTooLarge()
        val body = when {
            decimalScale == 0 -> digits
            decimalScale < digits.length -> digits.substring(0, digits.length - decimalScale) + "." + digits.substring(digits.length - decimalScale)
            else -> "0." + "0".repeat(decimalScale - digits.length) + digits
        }
        return (if (sign < 0) "-" else "") + body
    }
    // Complete layout length, before padding/concatenation. Negative-scale zero
    // suppression belongs to the caller, including its virtual signum() dispatch.
    private fun plainStringLength(digits: Int, sign: Int): Long {
        val signLength = if (sign < 0) 1L else 0L
        return signLength + when {
            decimalScale <= 0 -> digits.toLong() - decimalScale.toLong()
            decimalScale < digits -> digits.toLong() + 1L
            else -> decimalScale.toLong() + 2L
        }
    }
    open fun toPlainString(): String {
        val text = if (representation == 1) compactValue529().toString() else wideCoefficient!!.toString()
        if (decimalScale == 0) return text
        if (decimalScale < 0) {
            if (signum() == 0) return "0"
            if (text.length.toLong() - decimalScale.toLong() > Int.MAX_VALUE.toLong()) plainStringTooLarge()
            return text + "0".repeat((-decimalScale.toLong()).toInt())
        }
        return canonicalPlain(magnitudeDigits529(), signum())
    }
    open fun toEngineeringString(): String {
        val digits = magnitudeDigits529()
        val sign = layoutSign()
        val exp = digits.length.toLong() - decimalScale - 1
        if (decimalScale >= 0 && exp >= -6) return canonicalPlain(digits, sign)
        val mod = ((exp % 3 + 3) % 3).toInt()
        var engineeringExp = exp - mod
        val body: String
        if (signum() == 0) {
            val decimals = (3 - mod) % 3
            if (mod != 0) engineeringExp += 3
            body = if (decimals == 0) "0" else "0." + "0".repeat(decimals)
        } else {
            val before = mod + 1
            body = if (digits.length <= before) digits.padEnd(before, '0') else digits.substring(0, before) + "." + digits.substring(before)
        }
        return (if (sign < 0) "-" else "") + body + if (engineeringExp == 0L) "" else exponentText(engineeringExp)
    }

    open fun toBigInteger(): BigInteger {
        val coefficient = coefficientSnapshot()
        if (signum() == 0) return BigInteger.ZERO
        if (decimalScale <= 0) return coefficient.multiply(tenPow(-decimalScale.toLong()))
        if (decimalScale >= precision()) return BigInteger.ZERO
        return coefficient.divide(tenPow(decimalScale.toLong()))
    }
    open fun toBigIntegerExact(): BigInteger = setScale(0, ROUND_UNNECESSARY).coefficientSnapshot()
    @kotlin.jvm.JvmName("intValue")
    override fun toInt(): Int = integerLowBits(32).toInt()
    @kotlin.jvm.JvmName("longValue")
    override fun toLong(): Long = integerLowBits(64)
    private fun integerLowBits(bits: Int): Long {
        if (representation == 1 && this::class == BigDecimal::class) {
            var value = compactValue529()
            if (decimalScale >= 19) return 0L
            if (decimalScale > 0) repeat(decimalScale) { value /= 10L }
            if (decimalScale < 0) {
                val raise = -decimalScale.toLong()
                if (raise >= bits) return 0L
                repeat(raise.toInt()) { value *= 10L }
            }
            return if (bits == 32) value.toInt().toLong() else value
        }
        val coefficient = coefficientSnapshot()
        if (decimalScale >= 0) return toBigInteger().toLong()
        // A multiple of 10^bits vanishes modulo 2^bits; don't materialize a huge integer.
        if (-decimalScale.toLong() >= bits) return 0
        val raise = -decimalScale.toLong()
        if (!coefficient.decimalLowBitsProductFits591(raise.toInt()))
            return coefficient.decimalProductLowBits591(raise.toInt(), bits)
        return coefficient.multiply(tenPow(-decimalScale.toLong())).toLong()
    }
    @kotlin.jvm.JvmName("byteValue")
    override fun toByte(): Byte = toInt().toByte()
    @kotlin.jvm.JvmName("shortValue")
    override fun toShort(): Short = toInt().toShort()
    open fun intValueExact(): Int = narrowExact(Int.MIN_VALUE.toLong(), Int.MAX_VALUE.toLong()).toInt()
    open fun shortValueExact(): Short = narrowExact(Short.MIN_VALUE.toLong(), Short.MAX_VALUE.toLong()).toShort()
    open fun byteValueExact(): Byte = narrowExact(Byte.MIN_VALUE.toLong(), Byte.MAX_VALUE.toLong()).toByte()
    private fun narrowExact(minimum: Long, maximum: Long): Long {
        val value = longValueExact()
        if (value < minimum || value > maximum) throw ArithmeticException("Overflow")
        return value
    }
    open fun longValueExact(): Long {
        if (representation == 1 && decimalScale == 0 &&
            (compactValue529() != Long.MIN_VALUE || this::class == BigDecimal::class)) return compactValue529()
        val coefficient = coefficientSnapshot()
        val compact = coefficient.decimalCompactValue()
        if (compact != null && decimalScale == 0) return compact
        if (signum() == 0) return 0
        // Keep these virtual calls separate: subclass traces observe both guards.
        if (precision().toLong() - decimalScale <= 0) throw ArithmeticException("Rounding necessary")
        if (precision().toLong() - decimalScale > 19) throw ArithmeticException("Overflow")
        val integer = setScale(0, ROUND_UNNECESSARY)
        if (integer.precision() >= 19 && integer.coefficientSnapshot().bitLength() > 63)
            throw ArithmeticException("Overflow")
        return integer.coefficientSnapshot().toLong()
    }
    @kotlin.jvm.JvmName("doubleValue")
    override fun toDouble(): Double = binaryValue(53, -1022, 1023)
    @kotlin.jvm.JvmName("floatValue")
    override fun toFloat(): Float = binaryValue(24, -126, 127).toFloat()
    private fun binaryValue(precision: Int, minExponent: Int, maxExponent: Int): Double {
        val coefficient = coefficientSnapshot()
        val compact = coefficient.decimalCompactValue()
        if (compact == 0L) return 0.0
        val compactPrimitive = compact?.let {
            if (precision == 24) BigInteger.valueOf(it).toFloat().toDouble() else it.toDouble()
        }
        val maxCompactScale = if (precision == 24) 10L else 22L
        val compactPath = compact != null && (decimalScale == 0 ||
            (decimalScale.toLong() in -maxCompactScale..maxCompactScale &&
                compactPrimitive!!.toLong() == compact))
        // Public-JDK probes distinguish compact paths (no virtual calls) from
        // the general rational path (one virtual signum, no virtual precision).
        val resultSign = if (compactPath) compact!!.compareTo(0L) else signum()
        val lowerExponent = coefficient.decimalDigitLowerBound().toLong() - decimalScale - 1
        if (lowerExponent > 400) return resultSign * Double.POSITIVE_INFINITY
        if (lowerExponent + 1 < -400) return resultSign * 0.0
        // Every IEEE midpoint has a terminating decimal with at most this
        // many significant digits. Keep its decimal grid and one sticky digit
        // instead of constructing a potentially unsupported 10^decimalScale.
        // See DECIMAL_BINARY_CONVERSION.md for the bound and endpoint proof.
        val midpointDigits = maxOf(precision - minExponent + (precision + 3) / 3,
            (maxExponent + 3) / 3)
        val prefix = coefficient.decimalBinaryPrefix(midpointDigits)
        val decimalExponent = prefix.decimalDigits.toLong() - decimalScale - 1
        if (decimalExponent > 400) return resultSign * Double.POSITIVE_INFINITY
        if (decimalExponent < -400) return resultSign * 0.0
        val magnitude = if (prefix.sticky) prefix.magnitude.multiply(BigInteger.TEN).add(BigInteger.ONE)
            else prefix.magnitude
        val scale = decimalScale.toLong() - prefix.discardedDigits + if (prefix.sticky) 1 else 0
        val numerator = if (scale < 0) magnitude.multiply(tenPow(-scale)) else magnitude
        val denominator = if (scale > 0) tenPow(scale) else BigInteger.ONE
        var exponent = numerator.bitLength() - denominator.bitLength()
        if (exponent >= 0) { if (numerator < denominator.shiftLeft(exponent)) exponent-- }
        else if (numerator.shiftLeft(-exponent) < denominator) exponent--
        if (exponent > maxExponent) return resultSign * Double.POSITIVE_INFINITY
        val shift = precision - 1 - maxOf(exponent, minExponent)
        val top = if (shift >= 0) roundedDivide(numerator.shiftLeft(shift), denominator, RoundingMode.HALF_EVEN)
            else roundedDivide(numerator, denominator.shiftLeft(-shift), RoundingMode.HALF_EVEN)
        // Multiplication by a power of two is exact, including gradual underflow.
        return resultSign * top.toLong().toDouble() * 2.0.pow(-shift)
    }

    // Preserve the non-final static factories exposed by JDK.
    @Suppress("NON_FINAL_MEMBER_IN_OBJECT")
    companion object {
        private fun normalizedCoefficient529(value: BigInteger): BigInteger =
            if (value::class == BigInteger::class) value else BigInteger(value.toByteArray())

        private const val serialVersionUID: Long = 6108874887143696463L
        private fun compareValues(a: BigInteger, aScale: Long, b: BigInteger, bScale: Long): Int {
            if (a.signum() != b.signum()) return a.signum().compareTo(b.signum())
            if (a.signum() == 0) return 0
            val digitDifference = a.decimalMagnitudeDigits().toLong() - b.decimalMagnitudeDigits()
            // Compare Da-sa with Db-sb as (sb + Da-Db) versus sa.
            // Detect a mathematical sum outside Long rather than wrapping it.
            val exponent = when {
                digitDifference > 0 && bScale > Long.MAX_VALUE - digitDifference -> 1
                digitDifference < 0 && bScale < Long.MIN_VALUE - digitDifference -> -1
                else -> (bScale + digitDifference).compareTo(aScale)
            }
            if (exponent != 0) return exponent * a.signum()
            return a.compareDecimalSignificand(b) * a.signum()
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
        @JvmStatic open fun valueOf(unscaledVal: Long, scale: Int): BigDecimal = BigDecimal(unscaledVal, scale)
        @JvmStatic open fun valueOf(value: Double): BigDecimal = BigDecimal(decimalDoubleString(value))

        /** Coherent sequence parsing; no call to an arbitrary CharSequence.toString(). */
        @JvmStatic fun parseText(value: CharSequence, mc: MathContext = MathContext.UNLIMITED): BigDecimal =
            DecimalText588.parse(value, 0, value.length, mc)

        @JvmStatic fun parseText(value: CharSequence, offset: Int, length: Int,
            mc: MathContext = MathContext.UNLIMITED): BigDecimal = DecimalText588.parse(value, offset, length, mc)

        /** Opt-in length limit; range/configuration checks precede character traversal. */
        @JvmStatic fun parseTextBounded(value: CharSequence, offset: Int, length: Int, maxInputChars: Int,
            mc: MathContext = MathContext.UNLIMITED): BigDecimal =
            DecimalText588.parseBounded(value, offset, length, maxInputChars, mc)

        @JvmStatic fun parseCharsBounded(value: CharArray, offset: Int, length: Int, maxInputChars: Int,
            mc: MathContext = MathContext.UNLIMITED): BigDecimal =
            DecimalText588.parseBounded(value, offset, length, maxInputChars, mc)

        private fun parse(value: String): BigDecimal = DecimalText588.parse(value, MathContext.UNLIMITED)

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
        private fun roundedDouble(value: Double, mc: MathContext): BigDecimal {
            val exact = fromDouble(value)
            val context = mc
            return if (exact.signum() == 0) exact else exact.round(context)
        }
        private fun tenPow(exponent: Long): BigInteger {
            if (exponent < 0) throw ArithmeticException("BigInteger overflow")
            if (exponent > 646456992L) throw ArithmeticException("BigInteger would overflow supported range")
            if (exponent < SMALL_TEN_POWERS.size) return SMALL_TEN_POWERS[exponent.toInt()]
            DecimalPowers597.lookup(exponent.toInt())?.let { return it }
            return BigInteger.TEN.pow(exponent.toInt())
        }
        // Fully initialized immutable bounded table: 19 references, no dynamic
        // growth, retained inputs, synchronization or public mutable arrays.
        private val SMALL_TEN_POWERS: Array<BigInteger> = run {
            var power = 1L
            Array(19) { index ->
                if (index != 0) power *= 10L
                BigInteger.valueOf(power)
            }
        }
        private fun checkedScale(scale: Long): Int {
            if (scale !in Int.MIN_VALUE.toLong()..Int.MAX_VALUE.toLong()) throw ArithmeticException(if (scale < 0) "Overflow" else "Underflow")
            return scale.toInt()
        }
        private fun checkedRoundingScale(scale: Long): Int {
            // The pinned rounding diagnostic follows the narrowed Int's sign.
            val narrowed = scale.toInt()
            if (narrowed.toLong() != scale) throw ArithmeticException(if (narrowed > 0) "Underflow" else "Overflow")
            return narrowed
        }
        private fun saturatedScale(scale: Long): Int = scale.coerceIn(Int.MIN_VALUE.toLong(), Int.MAX_VALUE.toLong()).toInt()
        private fun scaleForValue(scale: Long, zero: Boolean): Int = if (zero) saturatedScale(scale) else checkedScale(scale)
        private fun zeros(count: Long): String {
            if (count > Int.MAX_VALUE - 8) throw ArithmeticException("String too large")
            return "0".repeat(count.toInt())
        }
        private fun exponentText(exponent: Long): String = "E" + (if (exponent >= 0) "+" else "") + exponent
        private fun roundedDivide(numerator: BigInteger, denominator: BigInteger, mode: RoundingMode): BigInteger {
            if (numerator::class == BigInteger::class && denominator::class == BigInteger::class) {
                val a = numerator.decimalCompactValue()
                val b = denominator.decimalCompactValue()
                if (a != null && b != null && b != 0L) {
                    val quotient = a / b
                    val remainder = a % b
                    if (remainder == 0L) return BigInteger.valueOf(quotient)
                    val sign = if ((a < 0L) == (b < 0L)) 1 else -1
                    val r = if (remainder < 0L) -remainder else remainder
                    val d = if (b < 0L) -b else b
                    val half = r.compareTo(d - r)
                    return BigInteger.valueOf(if (increment(mode, sign, half, quotient and 1L != 0L))
                        quotient + sign else quotient)
                }
            }
            val qr = numerator.divideAndRemainder(denominator)
            if (qr[1].signum() == 0) return qr[0]
            val sign = numerator.signum() * denominator.signum()
            val remainder = qr[1].abs()
            val divisor = denominator.abs()
            // For 0 < r < d, compare(2r, d) = compare(r, d-r); both operands fit.
            val half = remainder.compareTo(divisor.subtract(remainder))
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
