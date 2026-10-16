package io.heapy.krogu.math

import kotlin.jvm.JvmField
import kotlin.jvm.JvmStatic
import kotlin.math.pow
import kotlin.random.Random

/** Immutable signed integer. Magnitudes use little-endian base-2^30 limbs. */
// JVM names keep Number's virtual methods overridable from Java. Without them Kotlin
// generates final bridges; JavaApiContractTest checks Java and Kotlin dispatch.
@Suppress("INAPPLICABLE_JVM_NAME")
open class BigInteger private constructor(private val sign: Int, private val words: IntArray) :
    Number(), Comparable<BigInteger>, MathSerializable {

    private constructor(value: BigInteger) : this(value.sign, value.words)
    private fun readObject(stream: MathObjectInputStream) {
        restoreSerializedFields(stream)
        // Stream fields are untrusted even when their Kotlin declarations are non-null.
        val restored: IntArray? = words
        if (restored == null || sign !in -1..1 || (sign == 0) != restored.isEmpty() ||
            restored.any { it < 0 || it.toLong() > MASK } || restored.lastOrNull() == 0)
            invalidSerializedState("Invalid BigInteger representation")
        if (restored.isNotEmpty()) {
            try { MagnitudeLimits.checkedBitLength(restored.size, restored.last()) }
            catch (_: ArithmeticException) { invalidSerializedState("BigInteger overflow") }
        }
        // Do not retain an array that another object in the stream can mutate.
        restoreSerializedMagnitude(this, restored.copyOf())
    }
    private fun readObjectNoData(): Unit = invalidSerializedState("Missing BigInteger data")
    constructor(value: String) : this(value, 10)
    constructor(value: String, radix: Int) : this(parse(value, radix))
    constructor(value: ByteArray) : this(fromBytes(value, 0, value.size, true))
    constructor(value: ByteArray, off: Int, len: Int) : this(fromBytes(value, off, len, true))
    constructor(signum: Int, magnitude: ByteArray) : this(signum, magnitude, 0, magnitude.size)
    constructor(signum: Int, magnitude: ByteArray, off: Int, len: Int) : this(signedBytes(signum, magnitude, off, len))
    constructor(numBits: Int, rnd: PlatformRandom) : this(randomBits(numBits, rnd))
    constructor(bitLength: Int, certainty: Int, rnd: PlatformRandom) : this(generatePrime(bitLength, certainty, rnd))

    open fun signum(): Int = sign
    open fun abs(): BigInteger = if (sign < 0) negate() else this
    open fun negate(): BigInteger = if (sign == 0) this else BigInteger(-sign, words)
    open fun add(value: BigInteger): BigInteger {
        if (sign == 0) return value
        if (value.sign == 0) return this
        if (sign == value.sign) return normalized(sign, addMagnitude(words, value.words))
        val cmp = compareMagnitude(words, value.words)
        return when {
            cmp == 0 -> ZERO
            cmp > 0 -> normalized(sign, subtractMagnitude(words, value.words))
            else -> normalized(value.sign, subtractMagnitude(value.words, words))
        }
    }
    open fun subtract(value: BigInteger): BigInteger {
        if (value.sign == 0) return this
        if (sign == 0) return value.negate()
        if (sign != value.sign) return normalized(sign, addMagnitude(words, value.words))
        val cmp = compareMagnitude(words, value.words)
        return when {
            cmp == 0 -> ZERO
            cmp > 0 -> normalized(sign, subtractMagnitude(words, value.words))
            else -> normalized(-sign, subtractMagnitude(value.words, words))
        }
    }
    open fun multiply(value: BigInteger): BigInteger {
        if (sign == 0 || value.sign == 0) return ZERO
        MagnitudeMultiplication.checkBitLengths(magnitudeBits(), value.magnitudeBits())
        return normalized(sign * value.sign, MagnitudeMultiplication.multiplyAdaptive(words, value.words))
    }

    open fun parallelMultiply(value: BigInteger): BigInteger = multiply(value)

    // Stored, nonvirtual certification only. Null retains the entire old sum
    // expression; unsupported aligned intermediates are never published here.
    @kotlin.jvm.JvmSynthetic
    internal fun alignedDecimalSum585(value: BigInteger, leftScale: Int, rightScale: Int,
        subtract: Boolean, mc: MathContext?): Pair<BigInteger, Int>? {
        val precision = mc?.precision ?: 0
        // Equal-scale exact output is itself the required public coefficient.
        // Two H-digit inputs sum to <=H+1 digits, so p>H cannot reduce it.
        if (leftScale == rightScale && (precision == 0 || precision > 646456993)) return null
        val rightSign = if (subtract) -value.sign else value.sign
        if (materializedDecimalSum585(sign, rightSign, magnitudeBits(), value.magnitudeBits(),
                leftScale, rightScale)) return null
        val leftDigits = if (sign == 0) 0 else decimalMagnitudeDigits()
        val rightDigits = if (value.sign == 0) 0 else value.decimalMagnitudeDigits()
        if (!certifiedDecimalSumSpan585(leftDigits, rightDigits, leftScale, rightScale, precision)) return null
        return DecimalSum585.sum(sign, words, leftScale, rightSign, value.words, rightScale, mc)
    }

    // No public metadata/byte/arithmetic callbacks select this private scratch
    // route. The helper reads these immutable magnitudes without retaining them.
    @kotlin.jvm.JvmSynthetic
    internal fun roundedDecimalProduct580(value: BigInteger, scale: Long, mc: MathContext): Pair<BigInteger, Int>? {
        if (sign == 0 || value.sign == 0 ||
            !needsRawDecimalProduct580(magnitudeBits(), value.magnitudeBits())) return null
        return DecimalProduct580.rounded(sign * value.sign, words, value.words, scale, mc)
    }

    @kotlin.jvm.JvmSynthetic
    internal fun roundedDecimalQuotient581(value: BigInteger, preferred: Long, target: Long, shift: Long,
        sourceDigits: Int, divisorDigits: Int, normalized: Int, mc: MathContext): Pair<BigInteger, Int>? {
        if (sign == 0 || value.sign == 0) return null
        // A proved sufficient bound keeps ordinary materialized calls unchanged.
        if (materializedDecimalDivision581(magnitudeBits(), value.magnitudeBits(), mc.precision,
                preferred, target, shift)) return null
        // Eligibility reads stored metadata; public prefix callbacks already ran.
        if (sourceDigits != decimalMagnitudeDigits() || divisorDigits != value.decimalMagnitudeDigits() ||
            normalized != compareDecimalSignificand(value)) return null
        if (mc.precision > 646456993)
            return DecimalDivision581.largePrecision(sign * value.sign, BigInteger(1, words),
                BigInteger(1, value.words), preferred, mc.precision)
        return DecimalDivision581.rounded(sign * value.sign, words, value.words, preferred, mc)
    }

    // Captured public metadata selects this route without any additional callback.
    @kotlin.jvm.JvmSynthetic
    internal fun integralDecimalQuotient582(value: BigInteger, sourceScale: Int, divisorScale: Int,
        sourceDigits: Int, divisorDigits: Int, reportedSourceScale: Int, reportedDivisorScale: Int,
        sourceSign: Int, magnitude: Int): Pair<BigInteger, Int>? {
        val preferred = sourceScale.toLong() - divisorScale
        if (preferred !in Int.MIN_VALUE.toLong()..Int.MAX_VALUE.toLong() || sign == 0 || value.sign == 0 ||
            sourceSign != sign || reportedSourceScale != sourceScale || reportedDivisorScale != divisorScale ||
            sourceDigits != decimalMagnitudeDigits() || divisorDigits != value.decimalMagnitudeDigits()) return null
        val exponents = (sourceDigits.toLong() - sourceScale).compareTo(divisorDigits.toLong() - divisorScale)
        val storedMagnitude = if (exponents != 0) exponents else compareDecimalSignificand(value)
        if (magnitude.compareTo(0) != storedMagnitude.compareTo(0) || storedMagnitude < 0) return null
        if (preferred < 0) {
            val zeros = -preferred
            // A nonzero final residual then forces a minimal coefficient >H.
            // Preserve exact supported cohorts through the existing reduced 2/5 escape.
            if (zeros >= divisorDigits && sourceDigits.toLong() + zeros > 646456993L + 2L * divisorDigits - 1)
                return DecimalDivision581.largePrecision(sign * value.sign, BigInteger(1, words),
                    BigInteger(1, value.words), preferred, Int.MAX_VALUE)
            return DecimalDivision581.integral(sign * value.sign, words, value.words, preferred.toInt())
        }
        // B*10^S <= A follows from the checked magnitude prefix. All factors,
        // the scaled denominator and K*10^S are therefore supported.
        val power = TEN.pow(preferred.toInt())
        val absoluteDivisor = BigInteger(1, value.words)
        val denominator = if (preferred == 0L) absoluteDivisor else absoluteDivisor.multiply(power)
        val quotient = BigInteger(1, words).quotientStored(denominator)
        val coefficient = if (preferred == 0L) quotient else quotient.multiply(power)
        return (if (sign == value.sign || coefficient.sign == 0) coefficient
            else BigInteger(-1, coefficient.words)) to preferred.toInt()
    }

    // Decline preserves the whole old multiply/subtract chain. No public metadata callbacks.
    @kotlin.jvm.JvmSynthetic
    internal fun certifiedDecimalResidual583(value: BigInteger, sourceScale: Int, divisorScale: Int,
        quotient: BigInteger, quotientScale: Int): Pair<BigInteger, Int>? {
        if (value.sign == 0 || materializedDecimalResidual583(magnitudeBits(), value.magnitudeBits(),
                quotient.magnitudeBits(), sourceScale, divisorScale, quotientScale)) return null
        val preferred = sourceScale.toLong() - divisorScale
        if (preferred < Int.MIN_VALUE)
            return certifiedExactLowResidual590(value, sourceScale, divisorScale, quotient, quotientScale)
        if (preferred !in Int.MIN_VALUE.toLong()..Int.MAX_VALUE.toLong() ||
            quotientScale.toLong() > maxOf(preferred, 0L)) return null
        if (sign == 0) return if (quotient.sign == 0 && quotientScale.toLong() == preferred)
            ZERO to sourceScale else null
        val sourceDigits = decimalMagnitudeDigits()
        val divisorDigits = value.decimalMagnitudeDigits()
        val exponents = (sourceDigits.toLong() - sourceScale).compareTo(divisorDigits.toLong() - divisorScale)
        val magnitude = if (exponents != 0) exponents else compareDecimalSignificand(value)
        if (quotient.sign == 0) return if (quotientScale.toLong() == preferred && magnitude < 0)
            BigInteger(sign, words) to sourceScale else null
        if (quotient.sign != sign * value.sign || magnitude < 0) return null
        if (preferred < 0)
            return DecimalDivision581.certifiedResidual583(sign, words, value.words, quotient.words,
                preferred.toInt(), divisorScale, quotientScale)
        // B*10^S <= A, so power, denominator, quotient and residual are supported.
        val absoluteDivisor = BigInteger(1, value.words)
        val denominator = if (preferred == 0L) absoluteDivisor
            else absoluteDivisor.multiply(TEN.pow(preferred.toInt()))
        val qr = BigInteger(1, words).divideStored(denominator)
        if (!DecimalDivision581.matchesInteger583(quotient.words, quotientScale, qr[0].words)) return null
        val residual = qr[1]
        return (if (sign < 0 && residual.sign != 0) BigInteger(-1, residual.words) else residual) to sourceScale
    }

    // Sufficient old-product bounds; only explicit fixed-scale division calls this.
    @kotlin.jvm.JvmSynthetic
    internal fun fixedScaleDecimalQuotient592(value: BigInteger, shift: Long, mode: RoundingMode): BigInteger? {
        if (sign == 0 || value.sign == 0) return null
        if (shift in -646456992L..646456992L &&
            magnitudeBits().toLong() + 4L * maxOf(shift, 0L) <= Int.MAX_VALUE &&
            value.magnitudeBits().toLong() + 4L * maxOf(-shift, 0L) <= Int.MAX_VALUE) return null
        return DecimalDivision581.fixedScale592(sign * value.sign, words, value.words, shift, mode)
    }

    // Exact-only outside-LOW certificate; no expanded integer or public product.
    @kotlin.jvm.JvmSynthetic
    internal fun certifiedExactLowResidual590(value: BigInteger, sourceScale: Int, divisorScale: Int,
        quotient: BigInteger, quotientScale: Int): Pair<BigInteger, Int>? {
        val preferred = sourceScale.toLong() - divisorScale
        if (preferred >= Int.MIN_VALUE || sign == 0 || value.sign == 0 ||
            quotient.sign == 0 || quotient.sign != sign * value.sign) return null
        val residualScale = maxOf(sourceScale.toLong(), quotientScale.toLong() + divisorScale)
        if (residualScale !in Int.MIN_VALUE.toLong()..Int.MAX_VALUE.toLong()) return null
        // Only our fresh base wrappers enter this catch. An unsupported exact
        // coefficient/natural scale or nonterminating ratio retains the old path.
        val exact = try {
            DecimalDivision581.largePrecision(sign * value.sign, BigInteger(1, words),
                BigInteger(1, value.words), preferred, Int.MAX_VALUE)
        } catch (_: ArithmeticException) {
            return null
        }
        if (exact.first.decimalMagnitudeDigits().toLong() - exact.second !=
            quotient.decimalMagnitudeDigits().toLong() - quotientScale ||
            exact.first.compareDecimalSignificand(quotient) != 0) return null
        return ZERO to residualScale.toInt()
    }

    // Only selected pow work enters this final stored view; zero keeps its old route.
    @kotlin.jvm.JvmSynthetic
    internal fun decimalPowerState593(scale: Int): DecimalPower593.State? =
        if (sign == 0) null else DecimalPower593.fromMagnitude(sign, words, scale)

    open fun divide(value: BigInteger): BigInteger = quotientStored(value)
    open fun remainder(value: BigInteger): BigInteger = remainderStored(value)

    open fun divideAndRemainder(value: BigInteger): Array<BigInteger> = divideStored(value)

    private fun quotientStored(value: BigInteger): BigInteger =
        selectStoredDivision(value, true, false) { quotient, _ -> quotient }

    private fun remainderStored(value: BigInteger): BigInteger =
        selectStoredDivision(value, false, true) { _, remainder -> remainder }

    private fun divideStored(value: BigInteger): Array<BigInteger> =
        selectStoredDivision(value, true, true) { quotient, remainder -> arrayOf(quotient, remainder) }

    // Only the three wrappers above call this selector. An omitted output is ZERO
    // internally; each single-output wrapper selects only its requested result.
    private inline fun <T> selectStoredDivision(
        value: BigInteger,
        needQuotient: Boolean,
        needRemainder: Boolean,
        select: (BigInteger, BigInteger) -> T,
    ): T {
        if (value.sign == 0) throw ArithmeticException("BigInteger divide by zero")
        // Read stored magnitudes and bits without invoking operand subclass callbacks.
        if (compareMagnitude(words, value.words) < 0)
            return select(ZERO, if (needRemainder) normalized(sign, words) else ZERO)
        if (value.words.size == 1) {
            val result = if (needQuotient) IntArray(words.size) else null
            var remainder = 0L
            for (i in words.indices.reversed()) {
                val part = (remainder shl LIMB_BITS) + words[i]
                if (result != null) result[i] = (part / value.words[0]).toInt()
                remainder = part % value.words[0]
            }
            return select(
                if (result != null) normalized(sign * value.sign, result) else ZERO,
                if (needRemainder) normalized(sign, intArrayOf(remainder.toInt())) else ZERO,
            )
        }
        // Independent radix-2^30 long division. All scratch is local to this call;
        // neither operand's stored array is ever written or exported as scratch.
        val n = value.words.size
        val shift = value.words.last().countLeadingZeroBits() - (32 - LIMB_BITS)
        val divisor = IntArray(n)
        normalizeStoredDivision(value.words, divisor, shift) // No top carry for the divisor.
        val dividend = IntArray(words.size + 1)
        dividend[words.size] = normalizeStoredDivision(words, dividend, shift)
        val quotient = if (needQuotient) IntArray(words.size - n + 1) else null
        val radix = MASK + 1
        val high = divisor[n - 1].toLong()
        val next = divisor[n - 2].toLong()
        for (j in words.size - n downTo 0) {
            // The n+1 digit window U satisfies 0 <= U < radix * divisor.
            val top = (dividend[j + n].toLong() shl LIMB_BITS) + dividend[j + n - 1]
            var estimate = minOf(top / high, MASK)
            var residual = top - estimate * high
            // Guard before shifting: residual may be >= radix after capping/correction.
            // With a normalized high digit, at most two estimate decrements occur.
            while (residual < radix &&
                estimate * next > (residual shl LIMB_BITS) + dividend[j + n - 2]) {
                estimate--
                residual += high
            }
            var borrow = 0L
            for (i in 0 until n) {
                val product = estimate * divisor[i] + borrow
                val difference = dividend[j + i].toLong() - (product and MASK)
                dividend[j + i] = (difference and MASK).toInt()
                borrow = (product ushr LIMB_BITS) + if (difference < 0) 1 else 0
            }
            val difference = dividend[j + n].toLong() - borrow
            dividend[j + n] = (difference and MASK).toInt()
            if (difference < 0) {
                // The leading-three-digit test leaves at most one excess divisor.
                estimate--
                var carry = 0L
                for (i in 0 until n) {
                    val sum = dividend[j + i].toLong() + divisor[i] + carry
                    dividend[j + i] = (sum and MASK).toInt()
                    carry = sum ushr LIMB_BITS
                }
                dividend[j + n] = ((dividend[j + n].toLong() + carry) and MASK).toInt()
            }
            if (quotient != null) quotient[j] = estimate.toInt()
        }
        val remainder = if (needRemainder) IntArray(n) else null
        if (remainder != null) {
            for (i in 0 until n) {
                remainder[i] = if (shift == 0) dividend[i] else
                    (dividend[i] ushr shift) or
                        ((dividend[i + 1].toLong() shl (LIMB_BITS - shift)) and MASK).toInt()
            }
        }
        // Scratch is dead before output construction; any two requested outputs are disjoint.
        return select(
            if (quotient != null) normalized(sign * value.sign, quotient) else ZERO,
            if (remainder != null) normalized(sign, remainder) else ZERO,
        )
    }

    /** Copy source * 2^shift into a distinct target, returning the extra high digit. */
    private fun normalizeStoredDivision(source: IntArray, target: IntArray, shift: Int): Int {
        var carry = 0L
        for (i in source.indices) {
            val digit = (source[i].toLong() shl shift) or carry
            target[i] = (digit and MASK).toInt()
            carry = digit ushr LIMB_BITS
        }
        return carry.toInt()
    }

    open fun pow(exponent: Int): BigInteger {
        if (exponent < 0) throw ArithmeticException("Negative exponent")
        if (exponent == 0) return ONE
        if (equals(ONE)) return ONE
        if (sign == 0 || exponent == 1) return this
        val absolute = abs()
        val twos = absolute.getLowestSetBit()
        val shift = twos.toLong() * exponent
        if (shift > Int.MAX_VALUE)
            throw ArithmeticException("BigInteger would overflow supported range")
        val part = absolute.shiftRight(twos)
        val bits = part.bitLength()
        val negative = sign < 0 && exponent and 1 != 0
        if (bits == 1) return shiftPower(if (negative) valueOf(-1) else ONE, shift.toInt())
        if ((bits.toLong() - 1) * exponent >= Int.MAX_VALUE)
            throw ArithmeticException("BigInteger would overflow supported range")
        val estimate = bits.toLong() * exponent
        if (estimate <= 64) {
            if (part.words.isEmpty()) emptyPowMagnitude()
            // A compact calculation observes the leading unsigned 32-bit magnitude word.
            // False subclass metadata can select it even for a wider stored magnitude.
            val count = (part.magnitudeBits().toLong() + 31).div(32).toInt()
            var factor = part.magnitudeWord32(count - 1)
            // The observed compact leading-word-two path uses the masked Long shift distance.
            var e = if (factor == 2L) 0 else exponent
            var product = if (factor == 2L) 1L shl exponent else 1L
            while (e != 0) {
                if (e and 1 != 0) product *= factor
                e = e ushr 1
                if (e != 0) factor *= factor
            }
            val compactShift = estimate + shift <= 64
            if (compactShift) product = product shl shift.toInt()
            var magnitude = product.toULong()
            val packed = IntArray(3)
            var size = 0
            while (magnitude != 0uL) {
                packed[size++] = (magnitude and MASK.toULong()).toInt()
                magnitude = magnitude shr LIMB_BITS
            }
            val value = if (size == 0) BigInteger(0, intArrayOf())
                else normalized(if (negative) -1 else 1, packed)
            return if (compactShift) value else shiftPower(value, shift.toInt())
        }
        var e = exponent
        var base = normalized(part.sign, part.words)
        var result = ONE
        while (e != 0) {
            if (e and 1 != 0) result = result.multiply(base)
            e = e ushr 1
            if (e != 0) base = base.multiply(base)
        }
        return shiftPower(if (negative) result.negate() else result, shift.toInt())
    }
    private fun shiftPower(value: BigInteger, distance: Int): BigInteger {
        if (distance > 0 && value.sign != 0 && value.magnitudeBits().toLong() + distance > Int.MAX_VALUE)
            throw ArithmeticException("BigInteger would overflow supported range")
        return value.shiftLeftValue(distance)
    }
    open fun gcd(value: BigInteger): BigInteger {
        if (value.sign == 0) return abs()
        if (sign == 0) return value.abs()
        var a = BigInteger(1, words)
        var b = BigInteger(1, value.words)
        while (b.sign != 0) { val r = a.remainderStored(b); a = b; b = r }
        return normalized(1, a.words)
    }
    open fun mod(m: BigInteger): BigInteger {
        if (m.sign <= 0) throw ArithmeticException("BigInteger: modulus not positive")
        val r = remainder(m)
        return if (r.sign < 0) r.add(m) else r
    }
    open fun modPow(exponent: BigInteger, m: BigInteger): BigInteger {
        if (m.sign <= 0) throw ArithmeticException("BigInteger: modulus not positive")
        val power = exponent
        if (power.sign == 0) return if (m == ONE) ZERO else ONE
        if (this == ONE) return if (m == ONE) ZERO else ONE
        if (this == ZERO && power.sign >= 0) return ZERO
        if (this == valueOf(-1) && !power.testBit(0)) return if (m == ONE) ZERO else ONE
        val inverse = power.sign < 0
        val e: BigInteger? = if (inverse) power.negate() else power
        val base = if (sign < 0 || compareTo(m) >= 0) mod(m) else this
        val result = if (m.testBit(0)) base.oddModPowStored(e, m) else {
            val p = m.getLowestSetBit()
            val oddModulus = m.shiftRight(p)
            val oddBase = if (sign < 0 || compareTo(oddModulus) >= 0) mod(oddModulus) else this
            val oddResidue = if (oddModulus == ONE) ZERO else oddBase.oddModPowStored(e, oddModulus)
            val binaryResidue = base.modPowPowerOfTwo(e, p)
            val binaryModulus = ONE.shiftLeftValue(p)
            val binaryInverse = binaryModulus.modInverse(oddModulus)
            val oddInverse = oddModulus.modInverse(binaryModulus)
            if (m.magnitudeBits().toLong() * 2 + 2 <= Int.MAX_VALUE) {
                oddResidue.multiply(binaryModulus).multiply(binaryInverse)
                    .add(binaryResidue.multiply(oddModulus).multiply(oddInverse)).mod(m)
            } else {
                val modulus = BigInteger(1, m.words)
                val left = oddResidue.multiply(binaryModulus).multiplyMod(binaryInverse, modulus)
                val right = binaryResidue.multiply(oddModulus).multiplyMod(oddInverse, modulus)
                val gap = modulus.subtract(right)
                if (left >= gap) left.subtract(gap) else left.add(right)
            }
        }
        return if (inverse) result.modInverse(m) else result
    }
    private fun oddModPowStored(exponent: BigInteger?, modulus: BigInteger): BigInteger {
        val power = exponent ?: throw NullPointerException()
        if (power == ONE) return this
        if (words.isEmpty()) return ZERO
        val m = BigInteger(1, modulus.words)
        var base = BigInteger(1, words).mod(m)
        var result = ONE.mod(m)
        val bits = power.magnitudeBits()
        val trivialBase = base.sign == 0 || (base.words.size == 1 && base.words[0] == 1)
        // If a^e < m, every original loop product is also below m; avoid context setup.
        val belowModulus = power.words.size == 1 &&
            power.words[0].toLong() * base.magnitudeBits().toLong() <= m.magnitudeBits().toLong() - 1
        val context = if (bits == 0 || trivialBase || belowModulus) null else OddModulusContext.create(m)
        if (context != null) return context.pow(base, power, bits)
        for (i in 0 until bits) {
            if (power.words[i / LIMB_BITS] and (1 shl (i % LIMB_BITS)) != 0)
                result = result.multiplyMod(base, m)
            if (i + 1 < bits) base = base.multiplyMod(base, m)
        }
        return if (result.sign == 0) BigInteger(result) else result
    }

    /** Fixed-modulus parameters only; mutable digits belong to one invocation's workspace. */
    private class OddModulusContext private constructor(
        private val modulus: IntArray,
        private val rho: Long,
        private val oneMont: IntArray,
        private val r2: IntArray,
    ) {
        private class Workspace(size: Int) {
            val scratch = IntArray(size + 2)
            val input = IntArray(size)
            val factor = IntArray(size)
            val result = IntArray(size)
            val next = IntArray(size)
        }

        fun pow(base: BigInteger, power: BigInteger, bits: Int): BigInteger {
            val workspace = Workspace(modulus.size)
            base.words.copyInto(workspace.input)
            var factor = workspace.factor
            var result = workspace.result
            var next = workspace.next
            // MM(x,R^2 mod m) encodes x; the multiplicative identity is R mod m.
            multiply(workspace.input, r2, factor, workspace.scratch)
            oneMont.copyInto(result)
            for (i in 0 until bits) {
                if (power.words[i / LIMB_BITS] and (1 shl (i % LIMB_BITS)) != 0) {
                    multiply(result, factor, next, workspace.scratch)
                    val previous = result
                    result = next
                    next = previous
                }
                if (i + 1 < bits) {
                    multiply(factor, factor, next, workspace.scratch)
                    val previous = factor
                    factor = next
                    next = previous
                }
            }
            // A null second operand denotes ordinary one, without another digit array.
            multiply(result, null, next, workspace.scratch)
            if (next.all { it == 0 }) return BigInteger(ZERO)
            // normalized may trim again; neither copy may alias any mutable workspace slot.
            return normalized(1, next.copyOf())
        }

        /** One candidate's witnesses share immutable parameters and one resettable workspace. */
        fun witnesses(power: BigInteger, twos: Int, minusOne: BigInteger): WitnessSession =
            WitnessSession(this, power, twos, minusOne)

        class WitnessSession(
            private val context: OddModulusContext,
            private val power: BigInteger,
            private val twos: Int,
            minusOne: BigInteger,
        ) {
            private val workspace = Workspace(context.modulus.size)
            private val minusOneMont = IntArray(context.modulus.size)
            private val bits = power.magnitudeBits()

            init {
                minusOne.words.copyInto(workspace.input)
                context.multiply(workspace.input, context.r2, minusOneMont, workspace.scratch)
            }

            fun passes(base: BigInteger): Boolean {
                // Keep the ordinary stored reduction of the old private entry. No subtype
                // callback is skipped here: the caller admits only exact working values.
                val reduced = BigInteger(base).mod(BigInteger(1, context.modulus))
                workspace.input.fill(0)
                reduced.words.copyInto(workspace.input)
                var factor = workspace.factor
                var result = workspace.result
                var next = workspace.next
                context.multiply(workspace.input, context.r2, factor, workspace.scratch)
                context.oneMont.copyInto(result)
                for (i in 0 until bits) {
                    if (power.words[i / LIMB_BITS] and (1 shl (i % LIMB_BITS)) != 0) {
                        context.multiply(result, factor, next, workspace.scratch)
                        val previous = result
                        result = next
                        next = previous
                    }
                    if (i + 1 < bits) {
                        context.multiply(factor, factor, next, workspace.scratch)
                        val previous = factor
                        factor = next
                        next = previous
                    }
                }
                if (result.contentEquals(context.oneMont) || result.contentEquals(minusOneMont)) return true
                // Stay encoded through the same 1 until s sequence; multiplication is
                // injectively encoded, so equality with encoded +/-1 is exact.
                for (r in 1 until twos) {
                    context.multiply(result, result, next, workspace.scratch)
                    val previous = result
                    result = next
                    next = previous
                    if (result.contentEquals(minusOneMont)) return true
                }
                return false
            }
        }

        /**
         * MM(a,b) = ab/R mod m, B=2^30 and R=B^n. Inputs are canonical padded
         * residues (b=null means ordinary one). Output/scratch are distinct from
         * both inputs and every context array; a===b is allowed for squaring.
         */
        private fun multiply(a: IntArray, b: IntArray?, out: IntArray, scratch: IntArray) {
            val n = modulus.size
            scratch.fill(0)
            for (i in 0 until n) {
                val word = if (b == null) { if (i == 0) 1L else 0L } else b[i].toLong()
                addProduct(a, word, scratch)
                val q = (scratch[0].toLong() * rho) and MASK
                addProduct(modulus, q, scratch)
                // The low digit is zero. Retain both high cells during the division by B.
                for (j in 0..n) scratch[j] = scratch[j + 1]
                scratch[n + 1] = 0
            }
            // T<2m; its n-th digit can be one, even when the low n digits are <m.
            if (scratch[n] != 0 || compareLow(scratch, modulus) >= 0) {
                var borrow = 0L
                for (j in 0 until n) {
                    val difference = scratch[j].toLong() - modulus[j] - borrow
                    scratch[j] = (difference and MASK).toInt()
                    borrow = if (difference < 0) 1 else 0
                }
                scratch[n] = (scratch[n].toLong() - borrow).toInt()
            }
            // Canonicality proves scratch[n]==0; no significant high cell is truncated.
            scratch.copyInto(out, endIndex = n)
        }

        private fun addProduct(a: IntArray, word: Long, scratch: IntArray) {
            var carry = 0L
            for (j in a.indices) {
                // <=(B-1)^2+2(B-1)=B^2-1; carry remains <=B-1.
                val sum = a[j].toLong() * word + scratch[j] + carry
                scratch[j] = (sum and MASK).toInt()
                carry = sum ushr LIMB_BITS
            }
            val high = scratch[a.size].toLong() + carry
            scratch[a.size] = (high and MASK).toInt()
            // The unshifted value is <2BR, so this cell never overflows the radix.
            scratch[a.size + 1] = (scratch[a.size + 1].toLong() + (high ushr LIMB_BITS)).toInt()
        }

        companion object {
            private const val MAX_WORDS = 4096

            fun create(m: BigInteger): OddModulusContext? {
                val n = m.words.size
                if (m.sign <= 0 || n == 0 || n > MAX_WORDS ||
                    m.words[0] and 1 == 0 || (n == 1 && m.words[0] == 1)) return null
                // Resource checks precede allocations/narrowing; the cap is an optimization,
                // never a new numerical rejection or an exception/OOM retry policy.
                val size = n.toLong()
                val scratchCells = size + 2
                val setupSteps = 2L * LIMB_BITS * size
                val payloadCells = 16L * size + 8
                val payloadBytes = 4L * payloadCells
                if (scratchCells > Int.MAX_VALUE || setupSteps > Int.MAX_VALUE ||
                    payloadCells > Int.MAX_VALUE || payloadBytes > Int.MAX_VALUE) return null

                val modulus = m.words.copyOf()
                // Lift the inverse one bit at a time. All positive products are <B^2.
                var inverse = 1L
                for (bit in 1 until LIMB_BITS) {
                    if ((modulus[0].toLong() * inverse) and (1L shl bit) != 0L)
                        inverse += 1L shl bit
                }
                val rho = ((1L shl LIMB_BITS) - inverse) and MASK
                val oneMont = IntArray(n)
                val r2 = IntArray(n)
                val gap = IntArray(n)
                r2[0] = 1
                val half = (setupSteps / 2).toInt()
                for (step in 0 until setupSteps.toInt()) {
                    doubleResidue(r2, modulus, gap)
                    if (step + 1 == half) r2.copyInto(oneMont)
                }
                return OddModulusContext(modulus, rho, oneMont, r2)
            }

            /** Compare the low b.size digits; the caller handles any extra high cell. */
            private fun compareLow(a: IntArray, b: IntArray): Int {
                for (j in b.lastIndex downTo 0) {
                    if (a[j] != b[j]) return a[j].compareTo(b[j])
                }
                return 0
            }

            private fun doubleResidue(value: IntArray, modulus: IntArray, gap: IntArray) {
                var borrow = 0L
                for (j in modulus.indices) {
                    val difference = modulus[j].toLong() - value[j] - borrow
                    gap[j] = (difference and MASK).toInt()
                    borrow = if (difference < 0) 1 else 0
                }
                if (compareLow(value, gap) >= 0) {
                    // value-(m-value)=2value-m, in [0,m); safe with output==value.
                    borrow = 0L
                    for (j in value.indices) {
                        val difference = value[j].toLong() - gap[j] - borrow
                        value[j] = (difference and MASK).toInt()
                        borrow = if (difference < 0) 1 else 0
                    }
                } else {
                    // Here 2value<m<R, so the final carry is provably zero.
                    var carry = 0L
                    for (j in value.indices) {
                        val sum = value[j].toLong() * 2 + carry
                        value[j] = (sum and MASK).toInt()
                        carry = sum ushr LIMB_BITS
                    }
                }
            }
        }
    }

    private fun moduloPowerOfTwo(bits: Int): BigInteger {
        if (bitLength() <= bits) return this
        if (bits == 0) return ZERO
        val out = words.copyOf(((bits.toLong() + LIMB_BITS - 1) / LIMB_BITS).toInt())
        if (bits % LIMB_BITS != 0) out[out.lastIndex] = out.last() and ((1 shl (bits % LIMB_BITS)) - 1)
        return normalized(1, out)
    }
    private fun modPowPowerOfTwo(exponent: BigInteger?, bits: Int): BigInteger {
        var base = moduloPowerOfTwo(bits)
        var result = ONE
        val power = exponent ?: throw NullPointerException()
        var limit = power.bitLength()
        if (testBit(0)) limit = minOf(bits - 1, limit)
        for (i in 0 until limit) {
            if (power.testBit(i)) result = result.multiplyPowerOfTwo(base, bits)
            if (i + 1 < limit) base = BigInteger(base.sign, base.words).multiplyPowerOfTwo(base, bits)
        }
        return result
    }
    private fun multiplyPowerOfTwo(value: BigInteger, bits: Int): BigInteger {
        if (magnitudeBits().toLong() + value.magnitudeBits() <= Int.MAX_VALUE)
            return multiply(value).moduloPowerOfTwo(bits)
        val modulus = ONE.shiftLeftValue(bits)
        val a = BigInteger(1, words).mod(modulus)
        val b = BigInteger(1, value.words).mod(modulus)
        return a.multiplyModuloBounded(b, modulus)
    }
    open fun modInverse(m: BigInteger): BigInteger {
        if (m.sign <= 0) throw ArithmeticException("BigInteger: modulus not positive")
        if (m == ONE) return ZERO
        val reduced = if (sign < 0 || compareMagnitude(words, m.words) >= 0) mod(m) else this
        if (reduced == ONE) return ONE
        if (reduced.sign == 0) throw ArithmeticException("BigInteger not invertible.")
        // Working values deliberately bypass arithmetic/metadata overrides.
        val modulus = BigInteger(1, m.words)
        var r = modulus
        var nextR = BigInteger(1, reduced.words)
        var t = ZERO
        var nextT = ONE
        while (nextR.sign != 0) {
            val qr = r.divideStored(nextR)
            val temp = t.subtract(qr[0].multiply(nextT))
            t = nextT; nextT = temp; r = nextR; nextR = qr[1]
        }
        if (r != ONE) throw ArithmeticException("BigInteger not invertible.")
        return t.mod(modulus)
    }
    private fun multiplyMod(value: BigInteger, m: BigInteger): BigInteger =
        if (magnitudeBits().toLong() + value.magnitudeBits() <= Int.MAX_VALUE) multiply(value).mod(m)
        else multiplyModuloBounded(value, m)

    /** Residue multiplication that never constructs the potentially out-of-range full product. */
    @kotlin.jvm.JvmSynthetic
    internal fun multiplyModuloBounded(value: BigInteger, m: BigInteger): BigInteger {
        require(sign >= 0 && value.sign >= 0 && m.sign > 0 && this < m && value < m)
        fun addResidues(a: BigInteger, b: BigInteger): BigInteger {
            val gap = m.subtract(b)
            return if (a >= gap) a.subtract(gap) else a.add(b)
        }
        var result = ZERO
        var addend = this
        val bits = value.magnitudeBits()
        for (i in 0 until bits) {
            if (value.testBit(i)) result = addResidues(result, addend)
            if (i + 1 < bits) addend = addResidues(addend, addend)
        }
        return result
    }
    open fun sqrt(): BigInteger = sqrtStored()

    private fun sqrtStored(): BigInteger {
        if (sign < 0) throw ArithmeticException("Negative BigInteger")
        if (sign == 0) return ZERO
        val bits = magnitudeBits()
        if (bits == 1) return normalized(1, words)
        val value = BigInteger(1, words)
        var x = ONE.shiftLeftValue(MagnitudeLimits.initialSqrtShift(bits))
        while (true) {
            val next = x.add(value.quotientStored(x)).shiftRightValue(1)
            if (compareMagnitude(next.words, x.words) >= 0) return normalized(1, x.words)
            x = next
        }
    }
    open fun sqrtAndRemainder(): Array<BigInteger> {
        val root = sqrtStored()
        val square = root.multiply(root)
        return arrayOf(root, normalized(1, subtractMagnitude(words, square.words)))
    }

    open fun shiftLeft(n: Int): BigInteger = shiftLeftValue(n)
    private fun shiftLeftValue(n: Int): BigInteger {
        if (sign == 0) return ZERO
        if (n == 0) return this
        if (n < 0) return if (n == Int.MIN_VALUE) (if (sign < 0) ONE.negate() else ZERO) else shiftRightValue(-n)
        if (magnitudeBits().toLong() + n > Int.MAX_VALUE) throw ArithmeticException("BigInteger overflow")
        val whole = n / LIMB_BITS
        val bits = n % LIMB_BITS
        val result = IntArray(words.size + whole + 1)
        var carry = 0L
        for (i in words.indices) {
            val v = (words[i].toLong() shl bits) or carry
            result[i + whole] = (v and MASK).toInt()
            carry = v ushr LIMB_BITS
        }
        result[words.size + whole] = carry.toInt()
        return normalized(sign, result)
    }
    open fun shiftRight(n: Int): BigInteger = shiftRightValue(n)
    private fun shiftRightValue(n: Int): BigInteger {
        if (sign == 0) return ZERO
        if (n == 0) return this
        if (n < 0) {
            if (n == Int.MIN_VALUE) throw ArithmeticException("BigInteger overflow")
            return shiftLeftValue(-n)
        }
        if (n >= magnitudeBits()) return if (sign < 0) ONE.negate() else ZERO
        val whole = n / LIMB_BITS
        val bits = n % LIMB_BITS
        val result = IntArray(words.size - whole)
        for (i in result.indices) {
            var v = words[i + whole] ushr bits
            if (bits != 0 && i + whole + 1 < words.size)
                v = v or ((words[i + whole + 1] shl (LIMB_BITS - bits)) and MASK.toInt())
            result[i] = v
        }
        val truncated = normalized(sign, result)
        return if (sign < 0 && lowestSetBitValue() < n) truncated.subtract(ONE) else truncated
    }

    private fun magnitudeBits(): Int = if (words.isEmpty()) 0 else (words.size - 1) * LIMB_BITS + 32 - words.last().countLeadingZeroBits()
    open fun bitLength(): Int {
        val bits = magnitudeBits()
        if (sign >= 0 || words.last().countOneBits() != 1) return bits
        for (i in 0 until words.lastIndex) if (words[i] != 0) return bits
        return bits - 1
    }
    open fun bitCount(): Int {
        if (sign >= 0) return words.sumOf { it.countOneBits() }
        return words.sumOf { it.countOneBits() } + getLowestSetBit() - 1
    }
    open fun getLowestSetBit(): Int = lowestSetBitValue()
    private fun lowestSetBitValue(): Int {
        for (i in words.indices) if (words[i] != 0) return i * LIMB_BITS + words[i].countTrailingZeroBits()
        return -1
    }
    private fun firstNonzeroWord(): Int {
        var first = 0
        while (words[first] == 0) first++
        return first
    }
    private fun testBitValue(n: Int): Boolean {
        if (n < 0) throw ArithmeticException("Negative bit address")
        val index = n / LIMB_BITS
        val raw = words.getOrElse(index) { 0 }
        val word = if (sign >= 0) raw else {
            val first = firstNonzeroWord()
            when {
                index < first -> 0
                index == first -> -raw
                else -> raw.inv()
            }
        }
        return word and (1 shl (n % LIMB_BITS)) != 0
    }
    open fun testBit(n: Int): Boolean = testBitValue(n)
    open fun setBit(n: Int): BigInteger = changeBit(n, 1)
    open fun clearBit(n: Int): BigInteger = changeBit(n, 0)
    open fun flipBit(n: Int): BigInteger = changeBit(n, 2)
    private fun changeBit(n: Int, operation: Int): BigInteger {
        if (n < 0) throw ArithmeticException("Negative bit address")
        val length32 = bitwiseLength32()
        // Avoid allocating a sign-extension-only array at the largest address.
        val present = testBitValue(n)
        if (n == Int.MAX_VALUE && ((operation == 1 && present) || (operation == 0 && !present))) return BigInteger(this)
        if (n == Int.MAX_VALUE) throw ArithmeticException("BigInteger would overflow supported range")
        val index = n / LIMB_BITS
        val addressedLength32 = if (operation == 0) ((n.toLong() + 1) / 32 + 1).toInt() else n / 32 + 2
        val width = maxOf(length32, addressedLength32).toLong() * 32
        val result = twosComplement(((width + LIMB_BITS - 1) / LIMB_BITS).toInt())
        val mask = 1 shl (n % LIMB_BITS)
        result[index] = when (operation) {
            0 -> result[index] and mask.inv()
            1 -> result[index] or mask
            else -> result[index] xor mask
        }
        return bitwiseResult(result, width)
    }
    open fun not(): BigInteger {
        val width = bitwiseLength32().toLong() * 32
        val result = twosComplement(((width + LIMB_BITS - 1) / LIMB_BITS).toInt())
        for (i in result.indices) result[i] = result[i].inv() and MASK.toInt()
        return bitwiseResult(result, width)
    }
    open infix fun and(value: BigInteger): BigInteger = bitwise(value) { a, b -> a and b }
    open infix fun or(value: BigInteger): BigInteger = bitwise(value) { a, b -> a or b }
    open infix fun xor(value: BigInteger): BigInteger = bitwise(value) { a, b -> a xor b }
    open fun andNot(value: BigInteger): BigInteger = bitwise(value) { a, b -> a and b.inv() }
    // Public bitLength is intentionally virtual, including on a subclass operand.
    private fun bitwiseLength32(): Int = (bitLength() ushr 5) + 1
    private fun bitwise(value: BigInteger, op: (Int, Int) -> Int): BigInteger {
        val width = maxOf(bitwiseLength32(), value.bitwiseLength32()).toLong() * 32
        val length = ((width + LIMB_BITS - 1) / LIMB_BITS).toInt()
        val a = twosComplement(length)
        val b = value.twosComplement(length)
        val out = IntArray(length) { op(a[it], b[it]) and MASK.toInt() }
        return bitwiseResult(out, width)
    }
    private fun bitwiseResult(out: IntArray, width: Long): BigInteger {
        val highBits = ((width - 1) % LIMB_BITS + 1).toInt()
        val highMask = (1 shl highBits) - 1
        out[out.lastIndex] = out.last() and highMask
        if (out.last() and (1 shl (highBits - 1)) != 0)
            out[out.lastIndex] = out.last() or (MASK.toInt() xor highMask)
        val result = fromTwosComplement(out)
        return if (result.sign == 0) BigInteger(result) else result
    }
    private fun fromTwosComplement(out: IntArray): BigInteger {
        if (out.last() and (1 shl (LIMB_BITS - 1)) == 0) return normalized(1, out)
        var carry = 1L
        for (i in out.indices) {
            val v = (out[i].inv() and MASK.toInt()).toLong() + carry
            out[i] = (v and MASK).toInt(); carry = v ushr LIMB_BITS
        }
        return normalized(-1, out)
    }
    private fun twosComplement(length: Int): IntArray {
        if (sign >= 0) return words.copyOf(length)
        val out = IntArray(length)
        var carry = 1L
        for (i in out.indices) {
            val v = ((words.getOrElse(i) { 0 }).inv() and MASK.toInt()).toLong() + carry
            out[i] = (v and MASK).toInt(); carry = v ushr LIMB_BITS
        }
        return out
    }

    open fun isProbablePrime(certainty: Int): Boolean {
        if (certainty <= 0) return true
        return passesPrimeTest(PrimalityCertainty.testRounds(certainty).toLong())
    }

    private fun passesPrimeTest(rounds: Long): Boolean {
        val n = abs()
        if (n == TWO) return true
        if (!n.testBit(0) || n == ONE) return false
        // Preserve the public metadata callback before private primality shortcuts.
        n.bitLength()
        if (n.sign < 0) return false
        val smallPrimes = intArrayOf(2, 3, 5, 7, 11, 13, 17, 19, 23, 29, 31, 37, 41, 43, 47)
        for (p in smallPrimes) {
            val prime = valueOf(p.toLong())
            if (n == prime) return true
            if (n.remainder(prime).sign == 0) return false
        }
        val nm1 = n.subtract(ONE)
        val s = nm1.getLowestSetBit()
        val d = nm1.shiftRight(s)
        // Public prefix callbacks above remain observable. Subclass working values
        // retain the original virtual modPow/multiply route, including their errors.
        val ordinary = n::class == BigInteger::class && nm1::class == BigInteger::class &&
            d::class == BigInteger::class
        var attemptedContext = false
        var witnesses: OddModulusContext.WitnessSession? = null
        fun passesWitness(a: BigInteger): Boolean {
            if (ordinary && a::class == BigInteger::class) {
                if (!attemptedContext) {
                    attemptedContext = true
                    witnesses = OddModulusContext.create(n)?.witnesses(d, s, nm1)
                }
                witnesses?.let { return it.passes(a) }
            }
            var x = a.modPow(d, n)
            if (x == ONE || x == nm1) return true
            for (r in 1 until s) {
                x = x.multiplyMod(x, n)
                if (x == nm1) return true
            }
            return false
        }
        // Sorenson–Webster, Theorem 1.1: below this strict bound, testing the
        // first thirteen prime bases is conclusive, independently of certainty.
        // https://arxiv.org/abs/1509.00864 (mathematical result, not library code).
        if (n < DETERMINISTIC_PRIME_LIMIT) {
            for (base in DETERMINISTIC_PRIME_BASES)
                if (!passesWitness(valueOf(base.toLong()))) return false
            return true
        }
        var remaining = rounds
        while (remaining > 0) {
            remaining--
            var a: BigInteger
            do { a = randomBits(n.bitLength(), Random.Default) } while (a < TWO || a >= nm1)
            if (!passesWitness(a)) return false
        }
        return true
    }
    open fun nextProbablePrime(): BigInteger {
        if (sign < 0) throw ArithmeticException("start < 0: $this")
        if (sign == 0 || (words.size == 1 && words[0] == 1)) return TWO
        PrimeSearchLimits.checkBitLength(magnitudeBits())
        var n = add(ONE).setBit(0)
        var candidateIndex = ONE
        while (true) {
            PrimeSearchLimits.checkBitLength(n.magnitudeBits())
            if (n.passesPrimeTest(PrimalityCertainty.successorRounds(candidateIndex.bitLength()))) return n
            n = n.add(TWO)
            candidateIndex = candidateIndex.add(ONE)
        }
    }

    override fun compareTo(other: BigInteger): Int {
        val operand = other
        if (sign != operand.sign) return if (sign < operand.sign) -1 else 1
        // The public result is exactly -1/0/1. Primitive comparisons promise
        // only a sign, so normalize before reversing a negative receiver.
        val magnitude = compareMagnitude(words, operand.words)
        return when {
            magnitude < 0 -> -sign
            magnitude > 0 -> sign
            else -> 0
        }
    }
    open fun min(value: BigInteger): BigInteger = if (compareTo(value) < 0) this else value
    open fun max(value: BigInteger): BigInteger = if (compareTo(value) > 0) this else value
    override fun equals(other: Any?): Boolean = other is BigInteger && sign == other.sign && words.contentEquals(other.words)
    override fun hashCode(): Int {
        var hash = 0
        // Java's public hash behavior uses unsigned 32-bit chunks, independent of our limb layout.
        val count = (magnitudeBits().toLong() + 31).div(32).toInt()
        for (i in count - 1 downTo 0) {
            val chunk = magnitudeWord32(i)
            hash = (31L * hash + chunk).toInt()
        }
        return hash * sign
    }
    private fun magnitudeWord32(index: Int): Long {
        val start = index.toLong() * 32
        val word = (start / LIMB_BITS).toInt()
        val offset = (start % LIMB_BITS).toInt()
        // A 32-bit boundary has an even offset in 30-bit limbs (0..28),
        // so at most two limbs supply the entire unsigned chunk.
        var chunk = words[word].toLong() ushr offset
        if (word + 1 < words.size) chunk = chunk or (words[word + 1].toLong() shl (LIMB_BITS - offset))
        return chunk and 0xffffffffL
    }
    override fun toString(): String = toString(10)
    open fun toString(radix: Int): String {
        if (sign == 0) return "0"
        val r = if (radix in 2..36) radix else 10
        val magnitude = abs()
        if (radix in 2..36 && this::class == BigInteger::class && magnitude::class == BigInteger::class && magnitude.sign > 0) {
            when (r) {
                2, 4, 8, 16, 32 -> return magnitude.toPowerOfTwoString(r, sign < 0)
            }
        }
        if (this::class == BigInteger::class && magnitude::class == BigInteger::class && magnitude.sign > 0) {
            val digits = radixDigits537(r)
            val length = digits.length + if (sign < 0) 1 else 0
            if (length > Int.MAX_VALUE) plainStringTooLarge()
            return buildString(length.toInt()) {
                val sink = AppendableSink537(this)
                if (sign < 0) sink.character('-')
                digits.write(sink)
            }
        }
        val divisor = valueOf(r.toLong())
        val text = StringBuilder()
        var value = magnitude
        while (value.sign != 0) {
            val qr = value.divideAndRemainder(divisor)
            text.append("0123456789abcdefghijklmnopqrstuvwxyz"[qr[1].toInt()])
            value = qr[0]
        }
        if (sign < 0) text.append('-')
        return text.reverse().toString()
    }

    /**
     * Write without a whole result String. Invalid radix falls back to decimal, as in toString.
     * An opt-in limit is checked before output. Subtypes retain their virtual formatter;
     * allocation performed by arbitrary user callbacks is outside this writer's control.
     */
    fun appendTo(destination: Appendable, radix: Int = 10, maxChars: Long? = null): Long {
        checkTextLimit537(0, maxChars)
        if (this::class != BigInteger::class) {
            val text = toString(radix)
            checkTextLimit537(text.length.toLong(), maxChars)
            destination.append(text)
            return text.length.toLong()
        }
        val prefix = if (sign < 0) 1L else 0L
        checkTextLimit537(prefix + 1, maxChars)
        val digits = radixDigits537(if (radix in 2..36) radix else 10, maxChars?.minus(prefix))
        val length = prefix + digits.length
        checkTextLimit537(length, maxChars)
        val sink = AppendableSink537(destination)
        if (sign < 0) sink.character('-')
        digits.write(sink)
        return length
    }

    /** Capacity/range failures occur before modifying the destination slice. */
    fun writeTo(destination: CharArray, offset: Int = 0, length: Int = destination.size - offset,
        radix: Int = 10): Int {
        checkTextBuffer537(destination, offset, length)
        if (this::class != BigInteger::class) {
            val text = toString(radix)
            checkTextLimit537(text.length.toLong(), length.toLong())
            ArraySink537(destination, offset).text(text, 0, text.length)
            return text.length
        }
        val prefix = if (sign < 0) 1L else 0L
        checkTextLimit537(prefix + 1, length.toLong())
        val digits = radixDigits537(if (radix in 2..36) radix else 10, length - prefix)
        val required = prefix + digits.length
        checkTextLimit537(required, length.toLong())
        val sink = ArraySink537(destination, offset)
        if (sign < 0) sink.character('-')
        digits.write(sink)
        return required.toInt()
    }

    @kotlin.jvm.JvmSynthetic
    internal fun radixDigits537(radix: Int, maxDigits: Long? = null): RadixDigits537 {
        require(radix in 2..36)
        val upperBits = 32 - (radix - 1).countLeadingZeroBits()
        val lowerDigits = if (sign == 0) 1L else (magnitudeBits() - 1).toLong() / upperBits + 1
        checkTextLimit537(lowerDigits, maxDigits)
        if (sign == 0) return RadixDigits537(intArrayOf(0), 1, radix, 1)
        var power = radix
        var width = 1
        while (power.toLong() * radix <= Int.MAX_VALUE) { power *= radix; width++ }
        if ((radix and (radix - 1)) == 0) {
            val bitsPerDigit = radix.countTrailingZeroBits()
            val bitsPerChunk = bitsPerDigit * width
            val bitLength = magnitudeBits().toLong()
            val digits = (bitLength + bitsPerDigit - 1) / bitsPerDigit
            checkTextLimit537(digits, maxDigits)
            val count = ((bitLength + bitsPerChunk - 1) / bitsPerChunk).toInt()
            val chunks = IntArray(count)
            for (index in chunks.indices) {
                val bit = index.toLong() * bitsPerChunk
                val word = (bit / LIMB_BITS).toInt()
                val offset = (bit % LIMB_BITS).toInt()
                var chunk = words[word].toLong() ushr offset
                if (offset + bitsPerChunk > LIMB_BITS && word + 1 < words.size)
                    chunk = chunk or (words[word + 1].toLong() shl (LIMB_BITS - offset))
                chunks[index] = (chunk and (power - 1).toLong()).toInt()
            }
            return RadixDigits537(chunks, count, radix, width)
        }
        val scratch = words.copyOf()
        var remaining = scratch.size
        var chunks = IntArray(4)
        var count = 0
        while (remaining > 0) {
            if (count == chunks.size) {
                val next = minOf(Int.MAX_VALUE.toLong(), chunks.size.toLong() * 2).toInt()
                if (next == chunks.size) plainStringTooLarge()
                chunks = chunks.copyOf(next)
            }
            chunks[count++] = divideDecimalChunk(scratch, remaining, power)
            while (remaining > 0 && scratch[remaining - 1] == 0) remaining--
        }
        return RadixDigits537(chunks, count, radix, width)
    }
    private fun toPowerOfTwoString(radix: Int, negative: Boolean): String {
        val bitsPerDigit = when (radix) {
            2 -> 1
            4 -> 2
            8 -> 3
            16 -> 4
            else -> 5
        }
        val top = words.last()
        val bitLength = (words.size - 1).toLong() * LIMB_BITS + (32 - top.countLeadingZeroBits())
        val digitCountLong = (bitLength + bitsPerDigit - 1) / bitsPerDigit
        val capacityLong = digitCountLong + if (negative) 1 else 0
        if (capacityLong > Int.MAX_VALUE.toLong()) plainStringTooLarge()
        val digitCount = digitCountLong.toInt()
        val mask = (1L shl bitsPerDigit) - 1
        val text = StringBuilder(capacityLong.toInt())
        if (negative) text.append('-')
        for (index in digitCount - 1 downTo 0) {
            val bit = index.toLong() * bitsPerDigit
            val word = (bit / LIMB_BITS).toInt()
            val offset = (bit % LIMB_BITS).toInt()
            var digit = words[word].toLong() ushr offset
            if (offset + bitsPerDigit > LIMB_BITS && word + 1 < words.size)
                digit = digit or (words[word + 1].toLong() shl (LIMB_BITS - offset))
            text.append("0123456789abcdefghijklmnopqrstuvwxyz"[(digit and mask).toInt()])
        }
        return text.toString()
    }
    open fun toByteArray(): ByteArray {
        val length = bitLength() / 8 + 1
        val magnitude = if (sign < 0) abs().subtract(ONE) else this
        return ByteArray(length) { index ->
            var byte = 0
            for (b in 0..7) if (magnitude.testBit((length - index - 1) * 8 + b)) byte = byte or (1 shl b)
            (if (sign < 0) byte.inv() else byte).toByte()
        }
    }
    @kotlin.jvm.JvmName("intValue")
    override fun toInt(): Int {
        val low = words.getOrElse(0) { 0 } or (words.getOrElse(1) { 0 } shl LIMB_BITS)
        return if (sign < 0) -low else low
    }
    @kotlin.jvm.JvmName("longValue")
    override fun toLong(): Long {
        var value = 0L
        for (i in minOf(words.size, 3) - 1 downTo 0) value = (value shl LIMB_BITS) or words[i].toLong()
        return if (sign < 0) -value else value
    }
    // Sufficient old-route proof for 0 <= raise < 64; no caller metadata.
    @kotlin.jvm.JvmSynthetic
    internal fun decimalLowBitsProductFits591(raise: Int): Boolean =
        magnitudeBits().toLong() + 4L * raise <= Int.MAX_VALUE

    // Only low bits are required: wrapping arithmetic never publishes c*10^raise.
    @kotlin.jvm.JvmSynthetic
    internal fun decimalProductLowBits591(raise: Int, bits: Int): Long {
        require(bits == 32 || bits == 64)
        require(raise in 0 until bits)
        var value = 0L
        for (i in minOf(words.size, 3) - 1 downTo 0) value = (value shl LIMB_BITS) or words[i].toLong()
        if (sign < 0) value = -value
        repeat(raise) { value *= 10L }
        return if (bits == 32) value.toInt().toLong() else value
    }

    // Decimal conversion needs the stored value without dispatching to a
    // subclass's Number methods. Long.MIN_VALUE is the noncompact boundary.
    @kotlin.jvm.JvmSynthetic
    internal fun decimalCompactValue(): Long? {
        if (words.size > 3 || (words.size == 3 && words[2] >= 8)) return null
        var value = 0L
        for (i in words.indices.reversed()) value = (value shl LIMB_BITS) or words[i].toLong()
        return if (sign < 0) -value else value
    }

    // Stored magnitude metadata for decimal binary conversion. The certified
    // lower approximation 1292913986 / 2^32 < log10(2) loses less than one
    // decimal digit over the entire supported bit range: D is E or E+1.
    // See DECIMAL_BINARY_CONVERSION.md; no full decimal string is required.
    @kotlin.jvm.JvmSynthetic
    internal fun decimalDigitLowerBound(): Int = if (sign == 0) 1 else
        (((magnitudeBits() - 1).toLong() * 1292913986L) ushr 32).toInt() + 1

    // Exact stored decimal digit count. The accepted estimate E has D in
    // {E,E+1}. At maximal E, 10^E needs 2147483648 bits and every supported
    // magnitude is below it. Every smaller threshold is supported.
    // See DECIMAL_METADATA_COMPARISON.md for the bounds and comparison proof.
    @kotlin.jvm.JvmSynthetic
    internal fun decimalMagnitudeDigits(): Int {
        if (sign == 0) return 1
        val lower = decimalDigitLowerBound()
        if (lower == 646456993) return lower
        val threshold = DecimalPowers597.lookup(lower) ?: TEN.pow(lower)
        return lower + if (compareMagnitude(words, threshold.words) >= 0) 1 else 0
    }

    // Compare positive decimal significands, ignoring the signs and scales.
    // Drop decimal chunks in one local scratch array; no padded integer or
    // multi-word division remainder objects are constructed.
    @kotlin.jvm.JvmSynthetic
    internal fun compareDecimalSignificand(other: BigInteger): Int {
        if (sign == 0 || other.sign == 0)
            return (if (sign == 0) 0 else 1).compareTo(if (other.sign == 0) 0 else 1)
        val difference = decimalMagnitudeDigits() - other.decimalMagnitudeDigits()
        if (difference == 0) return compareMagnitude(words, other.words)
        val longer = if (difference > 0) this else other
        val shorter = if (difference > 0) other else this
        // Every partial quotient is nonzero and no larger than the input.
        val quotient = longer.words.copyOf()
        var length = quotient.size
        var drop = if (difference > 0) difference else -difference
        var sticky = false
        while (drop >= 9) {
            // Always divide before testing sticky: later passes still change q.
            val remainder = divideDecimalChunk(quotient, length, 1_000_000_000)
            sticky = sticky || remainder != 0
            while (length > 0 && quotient[length - 1] == 0) length--
            drop -= 9
        }
        if (drop > 0) {
            var divisor = 1
            repeat(drop) { divisor *= 10 }
            val remainder = divideDecimalChunk(quotient, length, divisor)
            sticky = sticky || remainder != 0
            while (length > 0 && quotient[length - 1] == 0) length--
        }
        val quotientOrder = compareDecimalScratch(quotient, length, shorter.words)
        val order = if (quotientOrder != 0) quotientOrder else if (sticky) 1 else 0
        return if (difference > 0) order else -order
    }

    // For 0 < divisor <= Int.MAX_VALUE, part < divisor*2^30 < 2^61.
    // Quotient limbs therefore fit 30 bits; the final remainder fits Int.
    private fun divideDecimalChunk(magnitude: IntArray, length: Int, divisor: Int): Int {
        var remainder = 0L
        for (i in length - 1 downTo 0) {
            val part = (remainder shl LIMB_BITS) + magnitude[i]
            magnitude[i] = (part / divisor).toInt()
            remainder = part % divisor
        }
        return remainder.toInt()
    }

    private fun compareDecimalScratch(magnitude: IntArray, length: Int, other: IntArray): Int {
        if (length != other.size) return length.compareTo(other.size)
        for (i in length - 1 downTo 0) {
            val order = magnitude[i].compareTo(other[i])
            if (order != 0) return order
        }
        return 0
    }

    internal class DecimalBinaryPrefix(
        val magnitude: BigInteger,
        val decimalDigits: Int,
        val discardedDigits: Int,
        val sticky: Boolean,
    )

    @kotlin.jvm.JvmSynthetic
    internal fun decimalBinaryPrefix(maxDigits: Int): DecimalBinaryPrefix {
        require(maxDigits > 0)
        if (sign == 0) return DecimalBinaryPrefix(ZERO, 1, 0, false)
        val trim = maxOf(0, decimalDigitLowerBound() - maxDigits)
        val absolute = BigInteger(1, words)
        // trim <= D-maxDigits, so this power is no larger than the input.
        // The unchanged division kernel extracts at most maxDigits+1 digits.
        val qr = if (trim == 0) arrayOf(absolute, ZERO) else absolute.divideStored(TEN.pow(trim))
        val digits = qr[0].toString()
        val extra = maxOf(0, digits.length - maxDigits)
        val sticky = qr[1].sign != 0 || (maxDigits until digits.length).any { digits[it] != '0' }
        val prefix = if (extra == 0) qr[0] else BigInteger(digits.substring(0, maxDigits))
        return DecimalBinaryPrefix(prefix, trim + digits.length, trim + extra, sticky)
    }
    @kotlin.jvm.JvmName("byteValue")
    override fun toByte(): Byte = toInt().toByte()
    @kotlin.jvm.JvmName("shortValue")
    override fun toShort(): Short = toInt().toShort()
    @kotlin.jvm.JvmName("doubleValue")
    override fun toDouble(): Double = floatingValue(53, 1024)
    @kotlin.jvm.JvmName("floatValue")
    override fun toFloat(): Float = floatingValue(24, 128).toFloat()
    private fun floatingValue(precision: Int, maxExponent: Int, dispatchSmallValue: Boolean = true): Double {
        if (sign == 0) return 0.0
        val bits = magnitudeBits()
        // JDK delegates nonzero magnitudes below 2^63 to the virtual longValue.
        if (dispatchSmallValue && bits <= 63) {
            val small = toLong()
            return if (precision == 53) small.toDouble()
            else valueOf(small).floatingValue(precision, maxExponent, false)
        }
        if (bits > maxExponent) return if (sign < 0) Double.NEGATIVE_INFINITY else Double.POSITIVE_INFINITY
        val shift = maxOf(0, bits - precision)
        fun magnitudeBit(n: Int): Boolean = words[n / LIMB_BITS] and (1 shl (n % LIMB_BITS)) != 0
        var top = 0L
        for (bit in bits - 1 downTo shift) top = (top shl 1) or if (magnitudeBit(bit)) 1L else 0L
        if (shift > 0 && magnitudeBit(shift - 1) && (lowestSetBitValue() < shift - 1 || top and 1L != 0L)) top++
        return sign * top.toDouble() * 2.0.pow(shift)
    }
    open fun intValueExact(): Int {
        if (magnitudeBits() > 32 || bitLength() > 31) throw ArithmeticException("BigInteger out of int range")
        return toInt()
    }
    open fun longValueExact(): Long {
        if (magnitudeBits() > 64 || bitLength() > 63) throw ArithmeticException("BigInteger out of long range")
        return toLong()
    }
    open fun shortValueExact(): Short {
        if (magnitudeBits() > 32 || bitLength() > 15) throw ArithmeticException("BigInteger out of short range")
        return toShort()
    }
    open fun byteValueExact(): Byte {
        if (magnitudeBits() > 32 || bitLength() > 7) throw ArithmeticException("BigInteger out of byte range")
        return toByte()
    }

    // 'open' on @JvmStatic functions prevents final static bridges, preserving Java hiding.
    @Suppress("NON_FINAL_MEMBER_IN_OBJECT")
    companion object {
        private const val serialVersionUID: Long = -8287574255936472291L
        private const val LIMB_BITS = MagnitudeLimits.WORD_BITS
        private const val MASK = (1L shl LIMB_BITS) - 1
        @JvmField val ZERO = BigInteger(0, intArrayOf())
        @JvmField val ONE = BigInteger(1, intArrayOf(1))
        @JvmField val TWO = BigInteger(1, intArrayOf(2))
        @JvmField val TEN = BigInteger(1, intArrayOf(10))
        private val SMALL_VALUES = Array(33) { index ->
            val value = index - 16
            when (value) {
                0 -> ZERO; 1 -> ONE; 2 -> TWO; 10 -> TEN
                else -> BigInteger(if (value < 0) -1 else 1, intArrayOf(if (value < 0) -value else value))
            }
        }
        private val DETERMINISTIC_PRIME_LIMIT = BigInteger("3317044064679887385961981")
        private val DETERMINISTIC_PRIME_BASES = intArrayOf(2, 3, 5, 7, 11, 13, 17, 19, 23, 29, 31, 37, 41)
        @JvmStatic open fun valueOf(value: Long): BigInteger {
            if (value in -16L..16L) return SMALL_VALUES[(value + 16).toInt()]
            var magnitude = if (value < 0) 0uL - value.toULong() else value.toULong()
            val result = IntArray(3)
            var i = 0
            while (magnitude != 0uL) { result[i++] = (magnitude and MASK.toULong()).toInt(); magnitude = magnitude shr LIMB_BITS }
            return normalized(if (value < 0) -1 else 1, result)
        }
        @kotlin.jvm.JvmSynthetic
        @JvmStatic open fun probablePrime(bitLength: Int, rnd: Random): BigInteger = generatePrime(bitLength, 100, rnd)
        @JvmStatic open fun probablePrime(bitLength: Int, rnd: PlatformRandom): BigInteger = generatePrime(bitLength, 100, rnd)
        internal fun randomValue(numBits: Int, rnd: Random): BigInteger = randomBits(numBits, rnd)
        internal fun primeValue(bitLength: Int, certainty: Int, rnd: Random): BigInteger = generatePrime(bitLength, certainty, rnd)
        private fun generatePrime(bitLength: Int, certainty: Int, rnd: PlatformRandom): BigInteger {
            if (bitLength < 2) throw ArithmeticException("bitLength < 2")
            PrimeSearchLimits.checkBitLength(bitLength)
            return generatePrime(bitLength, certainty, rnd.asCommonRandom())
        }
        private fun randomBits(numBits: Int, rnd: PlatformRandom): BigInteger {
            require(numBits >= 0) { "numBits must be non-negative" }
            if (numBits == 0) return ZERO
            return randomBits(numBits, rnd.asCommonRandom())
        }
        private fun generatePrime(bitLength: Int, certainty: Int, rnd: Random): BigInteger {
            if (bitLength < 2) throw ArithmeticException("bitLength < 2")
            PrimeSearchLimits.checkBitLength(bitLength)
            if (bitLength == 2) return BigInteger(valueOf(rnd.nextInt(2, 4).toLong()))
            val rounds = PrimalityCertainty.generationRounds(bitLength, certainty)
            while (true) {
                val n = randomBits(bitLength, rnd).setBit(bitLength - 1).setBit(0)
                if (rounds == 0 || n.passesPrimeTest(rounds.toLong())) return n
            }
        }
        private fun randomBits(numBits: Int, rnd: Random): BigInteger {
            require(numBits >= 0) { "numBits must be non-negative" }
            val bytes = ByteArray((numBits.toLong() + 7L).div(8).toInt())
            rnd.nextBytes(bytes)
            if (bytes.isNotEmpty()) bytes[0] = (bytes[0].toInt() and (255 ushr ((8 - numBits % 8) % 8))).toByte()
            return fromBytes(bytes, 0, bytes.size, false)
        }
        /** Parse a coherent character sequence without materializing its String representation. */
        @JvmStatic fun parseText(value: CharSequence, radix: Int = 10): BigInteger =
            parse(NumericText537.of(value), radix)

        /** Slice bounds and malformed text use NumberFormatException, like decimal character parsing. */
        @JvmStatic fun parseText(value: CharSequence, offset: Int, length: Int, radix: Int = 10): BigInteger =
            parse(NumericText537.of(value, offset, length), radix)

        /** The caller must keep this array slice unchanged throughout the synchronous call. */
        @JvmStatic fun parseChars(value: CharArray, offset: Int = 0, length: Int = value.size - offset,
            radix: Int = 10): BigInteger = parse(NumericText537.of(value, offset, length), radix)

        /** Opt-in input limit, checked before character traversal or magnitude allocation. */
        @JvmStatic fun parseTextBounded(value: CharSequence, offset: Int, length: Int,
            maxInputChars: Int, radix: Int = 10): BigInteger =
            parse(NumericText537.of(value, offset, length).within(maxInputChars), radix)

        @JvmStatic fun parseCharsBounded(value: CharArray, offset: Int, length: Int,
            maxInputChars: Int, radix: Int = 10): BigInteger =
            parse(NumericText537.of(value, offset, length).within(maxInputChars), radix)

        private fun parse(value: String, radix: Int): BigInteger = parse(NumericText537.of(value), radix)

        /** Fully validated decimal significand; no intermediate digit String or byte encoding. */
        @kotlin.jvm.JvmSynthetic
        internal fun decimalPrefix537(value: NumericText537, first: Int, digits: Int, sign: Int): BigInteger {
            val builder = ParsedMagnitude537()
            var index = first
            var remaining = digits
            var width = (digits - 1) % 9 + 1
            while (remaining > 0) {
                var power = 1
                var block = 0
                var count = 0
                while (count < width) {
                    val c = value[index++]
                    if (c == '.') continue
                    block = block * 10 + checkNotNull(numericDigit537(c, 10))
                    power *= 10
                    count++
                }
                builder.multiplyAdd(power, block)
                remaining -= width
                width = 9
            }
            return builder.publish(sign)
        }

        private fun parse(value: NumericText537, radix: Int): BigInteger {
            if (radix !in 2..36) throw NumberFormatException("Radix out of range")
            if (value.length == 0) throw NumberFormatException("Zero length BigInteger")
            for (index in 1 until value.length)
                if (value[index] == '-' || value[index] == '+')
                    throw NumberFormatException("Illegal embedded sign character")
            var start = 0
            var sign = 1
            if (value[0] == '-' || value[0] == '+') { sign = if (value[0] == '-') -1 else 1; start++ }
            if (start == value.length) throw NumberFormatException("Zero length BigInteger")
            while (start < value.length && numericDigit537(value[start], radix) == 0) start++
            if (start == value.length) return ZERO
            if (MagnitudeLimits.parserDefinitelyOverflows(value.length - start, radix)) {
                // Invalid text must retain its digit error, even when its length proves
                // every valid number of this shape is outside the supported range.
                for (i in start until value.length)
                    if (numericDigit537(value[i], radix) == null) invalidIntegerDigit(value, radix, start, i)
                throw ArithmeticException("BigInteger overflow")
            }
            var groupPower = radix
            var groupWidth = 1
            while (groupPower.toLong() * radix <= Int.MAX_VALUE) {
                groupPower *= radix
                groupWidth++
            }
            val builder = ParsedMagnitude537()
            var index = start
            var width = (value.length - start - 1) % groupWidth + 1
            val radixUpperBits = 32 - (radix - 1).countLeadingZeroBits()
            while (index < value.length) {
                // Every prefix of this group fits if this conservative upper bound fits.
                // Otherwise retain old per-digit overflow/bad-digit exception precedence.
                if (builder.bits().toLong() + width.toLong() * radixUpperBits > Int.MAX_VALUE) {
                    var n = builder.publish(1)
                    val base = valueOf(radix.toLong())
                    for (at in index until value.length) {
                        val digit = numericDigit537(value[at], radix) ?: invalidIntegerDigit(value, radix, start, at)
                        n = n.multiply(base).add(valueOf(digit.toLong()))
                    }
                    return if (sign < 0) n.negate() else n
                }
                var block = 0
                var power = 1
                repeat(width) {
                    val digit = numericDigit537(value[index], radix) ?: invalidIntegerDigit(value, radix, start, index)
                    block = block * radix + digit
                    power *= radix
                    index++
                }
                builder.multiplyAdd(power, block)
                width = groupWidth
            }
            return builder.publish(sign)
        }

        /** Mutable only during parsing; each multiply-add is below B*Int.MAX_VALUE < 2^61. */
        private class ParsedMagnitude537 {
            private var words = IntArray(4)
            private var size = 0
            fun bits(): Int = if (size == 0) 0 else (size - 1) * LIMB_BITS + 32 - words[size - 1].countLeadingZeroBits()
            fun multiplyAdd(power: Int, block: Int) {
                var carry = block.toLong()
                for (index in 0 until size) {
                    val part = words[index].toLong() * power + carry
                    words[index] = (part and MASK).toInt()
                    carry = part ushr LIMB_BITS
                }
                while (carry != 0L) {
                    if (size == words.size) {
                        val next = minOf((Int.MAX_VALUE.toLong() + LIMB_BITS - 1) / LIMB_BITS,
                            words.size.toLong() * 2).toInt()
                        if (next == words.size) throw ArithmeticException("BigInteger overflow")
                        words = words.copyOf(next)
                    }
                    words[size++] = (carry and MASK).toInt()
                    carry = carry ushr LIMB_BITS
                }
                if (size > 0) MagnitudeLimits.checkedBitLength(size, words[size - 1])
            }
            fun publish(sign: Int): BigInteger = normalized(sign, words.copyOf(size))
        }

        private fun invalidIntegerDigit(value: NumericText537, radix: Int, start: Int, invalid: Int): Nothing {
            // The observable diagnostic identifies a group whose radix power fits a signed Int.
            // Arithmetic itself remains independent of this grouping.
            var power = radix.toLong()
            var width = 1
            while (power * radix <= Int.MAX_VALUE) { power *= radix; width++ }
            val firstWidth = ((value.length - start - 1) % width) + 1
            val groupStart = if (invalid < start + firstWidth) start
                else start + firstWidth + ((invalid - start - firstWidth) / width) * width
            val groupEnd = if (groupStart == start) start + firstWidth else groupStart + width
            val suffix = if (radix == 10) "" else " under radix $radix"
            throw NumberFormatException("For input string: \"${value.diagnostic(groupStart, groupEnd)}\"$suffix")
        }
        private fun fromBytes(bytes: ByteArray, off: Int, len: Int, twos: Boolean): BigInteger {
            if (twos && bytes.isEmpty()) throw NumberFormatException("Zero length BigInteger")
            checkBounds(bytes, off, len)
            if (len == 0) return ZERO
            val negative = twos && bytes[off] < 0
            var start = off
            val end = off + len
            if (negative) {
                while (end - start > 1 && bytes[start] == (-1).toByte()) start++
            } else {
                while (start < end && bytes[start] == 0.toByte()) start++
                if (start == end) return ZERO
            }
            val result = IntArray(MagnitudeLimits.packedWords(end - start, negative))
            var buffer = 0L
            var bufferedBits = 0
            var word = 0
            var carry = if (negative) 1 else 0
            for (i in end - 1 downTo start) {
                val raw = bytes[i].toInt() and 255
                val converted = if (negative) (raw xor 255) + carry else raw
                if (negative) carry = converted ushr 8
                buffer = buffer or ((converted and 255).toLong() shl bufferedBits)
                bufferedBits += 8
                if (bufferedBits >= LIMB_BITS) {
                    result[word++] = (buffer and MASK).toInt()
                    buffer = buffer ushr LIMB_BITS
                    bufferedBits -= LIMB_BITS
                }
            }
            // Stripping FF sign extension can leave an all-zero payload (e.g. FF 00).
            // Its complement carry is a real extra magnitude bit, not signed overflow.
            buffer = buffer or (carry.toLong() shl bufferedBits)
            if (buffer != 0L) result[word] = buffer.toInt()
            return normalized(if (negative) -1 else 1, result)
        }
        private fun signedBytes(signum: Int, bytes: ByteArray, off: Int, len: Int): BigInteger {
            if (signum !in -1..1) throw NumberFormatException("Invalid signum value")
            val magnitude = bytes
            if (signum == 0) {
                checkBounds(magnitude, off, len)
                for (i in off until off + len)
                    if (magnitude[i] != 0.toByte()) throw NumberFormatException("signum-magnitude mismatch")
                return ZERO
            }
            val n = fromBytes(magnitude, off, len, false)
            return if (signum < 0) n.negate() else n
        }
        private fun checkBounds(bytes: ByteArray, off: Int, len: Int) {
            if (off < 0 || len < 0 || off > bytes.size - len)
                throw IndexOutOfBoundsException("Range [$off, $off + $len) out of bounds for length ${bytes.size}")
        }
        private fun normalized(sign: Int, words: IntArray): BigInteger {
            var length = words.size
            while (length > 0 && words[length - 1] == 0) length--
            if (length == 0) return ZERO
            MagnitudeLimits.checkedBitLength(length, words[length - 1])
            return BigInteger(sign, if (length == words.size) words else words.copyOf(length))
        }
        private fun compareMagnitude(a: IntArray, b: IntArray): Int {
            if (a.size != b.size) return a.size.compareTo(b.size)
            for (i in a.indices.reversed()) if (a[i] != b[i]) return a[i].compareTo(b[i])
            return 0
        }
        private fun addMagnitude(a: IntArray, b: IntArray): IntArray {
            val out = IntArray(maxOf(a.size, b.size) + 1)
            var carry = 0L
            for (i in 0 until out.lastIndex) {
                val n = a.getOrElse(i) { 0 }.toLong() + b.getOrElse(i) { 0 } + carry
                out[i] = (n and MASK).toInt(); carry = n ushr LIMB_BITS
            }
            out[out.lastIndex] = carry.toInt()
            return out
        }
        private fun subtractMagnitude(a: IntArray, b: IntArray): IntArray {
            val out = IntArray(a.size)
            var borrow = 0L
            for (i in a.indices) {
                val n = a[i].toLong() - b.getOrElse(i) { 0 } - borrow
                out[i] = (n and MASK).toInt(); borrow = if (n < 0) 1 else 0
            }
            return out
        }
    }
}
