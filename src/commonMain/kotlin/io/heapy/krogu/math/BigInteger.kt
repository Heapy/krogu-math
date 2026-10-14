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
    Number(), Comparable<BigInteger?>, MathSerializable {

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
    constructor(signum: Int, magnitude: ByteArray?, off: Int, len: Int) : this(signedBytes(signum, magnitude, off, len))
    constructor(numBits: Int, rnd: PlatformRandom?) : this(randomBits(numBits, rnd))
    constructor(bitLength: Int, certainty: Int, rnd: PlatformRandom?) : this(generatePrime(bitLength, certainty, rnd))

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
        if (magnitudeBits().toLong() + value.magnitudeBits() - 1 > Int.MAX_VALUE)
            throw ArithmeticException("BigInteger overflow")
        val result = IntArray(words.size + value.words.size)
        for (i in words.indices) {
            var carry = 0L
            for (j in value.words.indices) {
                val product = words[i].toLong() * value.words[j] + result[i + j] + carry
                result[i + j] = (product and MASK).toInt()
                carry = product ushr LIMB_BITS
            }
            result[i + value.words.size] = carry.toInt()
        }
        return normalized(sign * value.sign, result)
    }
    open fun parallelMultiply(value: BigInteger): BigInteger = multiply(value)
    open fun divide(value: BigInteger): BigInteger = divideAndRemainder(value)[0]
    open fun remainder(value: BigInteger): BigInteger = divideAndRemainder(value)[1]

    open fun divideAndRemainder(value: BigInteger): Array<BigInteger> {
        if (value.sign == 0) throw ArithmeticException("BigInteger divide by zero")
        val a = abs()
        val b = value.abs()
        if (compareMagnitude(a.words, b.words) < 0) return arrayOf(ZERO, this)
        if (b.words.size == 1) {
            val result = IntArray(words.size)
            var remainder = 0L
            for (i in words.indices.reversed()) {
                val part = (remainder shl LIMB_BITS) + words[i]
                result[i] = (part / b.words[0]).toInt()
                remainder = part % b.words[0]
            }
            return arrayOf(normalized(sign * value.sign, result), valueOf(remainder * sign))
        }
        // Binary long division: at each step the partial remainder stays below twice b.
        val result = IntArray(a.words.size)
        var rem = ZERO
        for (bit in a.magnitudeBits() - 1 downTo 0) {
            rem = rem.shiftLeft(1)
            if (a.testBit(bit)) rem = rem.add(ONE)
            if (rem >= b) {
                rem = rem.subtract(b)
                result[bit / LIMB_BITS] = result[bit / LIMB_BITS] or (1 shl (bit % LIMB_BITS))
            }
        }
        return arrayOf(normalized(sign * value.sign, result), if (sign < 0) rem.negate() else rem)
    }

    open fun pow(exponent: Int): BigInteger {
        if (exponent < 0) throw ArithmeticException("Negative exponent")
        if (sign != 0 && (magnitudeBits().toLong() - 1) * exponent >= Int.MAX_VALUE)
            throw ArithmeticException("BigInteger would overflow supported range")
        var e = exponent
        var base = this
        var result = ONE
        while (e != 0) {
            if (e and 1 != 0) result = result.multiply(base)
            e = e ushr 1
            if (e != 0) base = base.multiply(base)
        }
        return result
    }
    open fun gcd(value: BigInteger): BigInteger {
        var a = abs()
        var b = value.abs()
        while (b.sign != 0) { val r = a.remainder(b); a = b; b = r }
        return a
    }
    open fun mod(m: BigInteger): BigInteger {
        if (m.sign <= 0) throw ArithmeticException("BigInteger: modulus not positive")
        val r = remainder(m)
        return if (r.sign < 0) r.add(m) else r
    }
    open fun modPow(exponent: BigInteger?, m: BigInteger): BigInteger {
        if (m.sign <= 0) throw ArithmeticException("BigInteger: modulus not positive")
        val power = exponent ?: throw NullPointerException()
        var base = if (power.sign < 0) modInverse(m) else mod(m)
        var result = ONE.mod(m)
        val e = power.abs()
        for (i in 0 until e.bitLength()) {
            if (e.testBit(i)) result = result.multiplyMod(base, m)
            if (i + 1 < e.bitLength()) base = base.multiplyMod(base, m)
        }
        return result
    }
    open fun modInverse(m: BigInteger): BigInteger {
        if (m.sign <= 0) throw ArithmeticException("BigInteger: modulus not positive")
        var r = m
        var nextR = mod(m)
        var t = ZERO
        var nextT = ONE
        while (nextR.sign != 0) {
            val qr = r.divideAndRemainder(nextR)
            val temp = t.subtract(qr[0].multiply(nextT))
            t = nextT; nextT = temp; r = nextR; nextR = qr[1]
        }
        if (r != ONE) throw ArithmeticException("BigInteger not invertible")
        return t.mod(m)
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
    open fun sqrt(): BigInteger {
        if (sign < 0) throw ArithmeticException("Negative BigInteger")
        if (this < TWO) return this
        var x = ONE.shiftLeft(MagnitudeLimits.initialSqrtShift(bitLength()))
        while (true) {
            val next = x.add(divide(x)).shiftRight(1)
            if (next >= x) return x
            x = next
        }
    }
    open fun sqrtAndRemainder(): Array<BigInteger> {
        val root = sqrt()
        return arrayOf(root, subtract(root.multiply(root)))
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
        val first = firstNonzeroWord()
        var count = first * LIMB_BITS + (words[first] - 1).countOneBits()
        for (i in first + 1 until words.size) count += words[i].countOneBits()
        return count
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
        val present = testBitValue(n)
        if ((operation == 1 && present) || (operation == 0 && !present)) return BigInteger(this)
        if (n == Int.MAX_VALUE) throw ArithmeticException("BigInteger overflow")
        val index = n / LIMB_BITS
        val result = twosComplement(maxOf(words.size + 1, index + 2))
        val mask = 1 shl (n % LIMB_BITS)
        result[index] = when (operation) {
            0 -> result[index] and mask.inv()
            1 -> result[index] or mask
            else -> result[index] xor mask
        }
        return fromTwosComplement(result)
    }
    open fun not(): BigInteger {
        val result = twosComplement(words.size + 1)
        for (i in result.indices) result[i] = result[i].inv() and MASK.toInt()
        return fromTwosComplement(result)
    }
    open fun and(value: BigInteger): BigInteger = bitwise(value) { a, b -> a and b }
    open fun or(value: BigInteger): BigInteger = bitwise(value) { a, b -> a or b }
    open fun xor(value: BigInteger): BigInteger = bitwise(value) { a, b -> a xor b }
    open fun andNot(value: BigInteger): BigInteger = bitwise(value) { a, b -> a and b.inv() }
    private fun bitwise(value: BigInteger, op: (Int, Int) -> Int): BigInteger {
        val length = maxOf(words.size, value.words.size) + 1
        val a = twosComplement(length)
        val b = value.twosComplement(length)
        val out = IntArray(length) { op(a[it], b[it]) and MASK.toInt() }
        return fromTwosComplement(out)
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
        if (n < TWO) return false
        val smallPrimes = intArrayOf(2, 3, 5, 7, 11, 13, 17, 19, 23, 29, 31, 37, 41, 43, 47)
        for (p in smallPrimes) {
            val prime = valueOf(p.toLong())
            if (n == prime) return true
            if (n.remainder(prime).sign == 0) return false
        }
        val nm1 = n.subtract(ONE)
        val s = nm1.getLowestSetBit()
        val d = nm1.shiftRight(s)
        fun passesWitness(a: BigInteger): Boolean {
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
        var n = add(ONE).setBit(0)
        var candidateIndex = ONE
        while (!n.passesPrimeTest(PrimalityCertainty.successorRounds(candidateIndex.bitLength()))) {
            n = n.add(TWO)
            candidateIndex = candidateIndex.add(ONE)
        }
        return n
    }

    override fun compareTo(other: BigInteger?): Int {
        val operand = other ?: throw NullPointerException()
        return if (sign != operand.sign) sign.compareTo(operand.sign) else sign * compareMagnitude(words, operand.words)
    }
    open fun min(value: BigInteger?): BigInteger? = if (compareTo(value) < 0) this else value
    open fun max(value: BigInteger?): BigInteger? = if (compareTo(value) > 0) this else value
    override fun equals(other: Any?): Boolean = other is BigInteger && sign == other.sign && words.contentEquals(other.words)
    override fun hashCode(): Int {
        var hash = 0
        // Java's public hash behavior uses unsigned 32-bit chunks, independent of our limb layout.
        val count = (magnitudeBits().toLong() + 31).div(32).toInt()
        for (i in count - 1 downTo 0) {
            val start = i.toLong() * 32
            val word = (start / LIMB_BITS).toInt()
            val offset = (start % LIMB_BITS).toInt()
            // A 32-bit boundary has an even offset in 30-bit limbs (0..28),
            // so at most two limbs supply the entire unsigned chunk.
            var chunk = words[word].toLong() ushr offset
            if (word + 1 < words.size) chunk = chunk or (words[word + 1].toLong() shl (LIMB_BITS - offset))
            chunk = chunk and 0xffffffffL
            hash = (31L * hash + chunk).toInt()
        }
        return hash * sign
    }
    override fun toString(): String = toString(10)
    open fun toString(radix: Int): String {
        if (sign == 0) return "0"
        val r = if (radix in 2..36) radix else 10
        val divisor = valueOf(r.toLong())
        val text = StringBuilder()
        var value = abs()
        while (value.sign != 0) {
            val qr = value.divideAndRemainder(divisor)
            text.append("0123456789abcdefghijklmnopqrstuvwxyz"[qr[1].toInt()])
            value = qr[0]
        }
        if (sign < 0) text.append('-')
        return text.reverse().toString()
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
    // Decimal conversion needs the stored value without dispatching to a
    // subclass's Number methods. Long.MIN_VALUE is the noncompact boundary.
    @kotlin.jvm.JvmSynthetic
    internal fun decimalCompactValue(): Long? {
        if (words.size > 3 || (words.size == 3 && words[2] >= 8)) return null
        var value = 0L
        for (i in words.indices.reversed()) value = (value shl LIMB_BITS) or words[i].toLong()
        return if (sign < 0) -value else value
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

    operator fun plus(other: BigInteger): BigInteger = add(other)
    operator fun minus(other: BigInteger): BigInteger = subtract(other)
    operator fun times(other: BigInteger): BigInteger = multiply(other)
    operator fun div(other: BigInteger): BigInteger = divide(other)
    operator fun rem(other: BigInteger): BigInteger = remainder(other)
    operator fun unaryMinus(): BigInteger = negate()
    operator fun unaryPlus(): BigInteger = this

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
        private val DETERMINISTIC_PRIME_LIMIT = BigInteger("3317044064679887385961981")
        private val DETERMINISTIC_PRIME_BASES = intArrayOf(2, 3, 5, 7, 11, 13, 17, 19, 23, 29, 31, 37, 41)
        @JvmStatic open fun valueOf(value: Long): BigInteger {
            if (value == 0L) return ZERO
            var magnitude = if (value < 0) 0uL - value.toULong() else value.toULong()
            val result = IntArray(3)
            var i = 0
            while (magnitude != 0uL) { result[i++] = (magnitude and MASK.toULong()).toInt(); magnitude = magnitude shr LIMB_BITS }
            return normalized(if (value < 0) -1 else 1, result)
        }
        @kotlin.jvm.JvmSynthetic
        @JvmStatic open fun probablePrime(bitLength: Int, rnd: Random): BigInteger = generatePrime(bitLength, 100, rnd)
        @JvmStatic open fun probablePrime(bitLength: Int, rnd: PlatformRandom?): BigInteger = generatePrime(bitLength, 100, rnd)
        internal fun randomValue(numBits: Int, rnd: Random): BigInteger = randomBits(numBits, rnd)
        internal fun primeValue(bitLength: Int, certainty: Int, rnd: Random): BigInteger = generatePrime(bitLength, certainty, rnd)
        private fun generatePrime(bitLength: Int, certainty: Int, rnd: PlatformRandom?): BigInteger {
            if (bitLength < 2) throw ArithmeticException("bitLength < 2")
            return generatePrime(bitLength, certainty, (rnd ?: throw NullPointerException()).asCommonRandom())
        }
        private fun randomBits(numBits: Int, rnd: PlatformRandom?): BigInteger {
            require(numBits >= 0) { "numBits must be non-negative" }
            if (numBits == 0) return ZERO
            return randomBits(numBits, (rnd ?: throw NullPointerException()).asCommonRandom())
        }
        private fun generatePrime(bitLength: Int, certainty: Int, rnd: Random): BigInteger {
            if (bitLength < 2) throw ArithmeticException("bitLength < 2")
            if (bitLength == 2) return valueOf(rnd.nextInt(2, 4).toLong())
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
        private fun parse(value: String, radix: Int): BigInteger {
            if (radix !in 2..36) throw NumberFormatException("Radix out of range")
            if (value.isEmpty()) throw NumberFormatException("Zero length BigInteger")
            if (value.indexOf('-', 1) >= 0 || value.indexOf('+', 1) >= 0)
                throw NumberFormatException("Illegal embedded sign character")
            var start = 0
            var sign = 1
            if (value[0] == '-' || value[0] == '+') { sign = if (value[0] == '-') -1 else 1; start++ }
            if (start == value.length) throw NumberFormatException("Zero length BigInteger")
            while (start < value.length && value[start].digitToIntOrNull(radix) == 0) start++
            if (start == value.length) return ZERO
            var n = ZERO
            val base = valueOf(radix.toLong())
            for (i in start until value.length) {
                val digit = value[i].digitToIntOrNull(radix) ?: invalidIntegerDigit(value, radix, start, i)
                n = n.multiply(base).add(valueOf(digit.toLong()))
            }
            return if (sign < 0) n.negate() else n
        }
        private fun invalidIntegerDigit(value: String, radix: Int, start: Int, invalid: Int): Nothing {
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
            throw NumberFormatException("For input string: \"${value.substring(groupStart, groupEnd)}\"$suffix")
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
        private fun signedBytes(signum: Int, bytes: ByteArray?, off: Int, len: Int): BigInteger {
            if (signum !in -1..1) throw NumberFormatException("Invalid signum value")
            val magnitude = bytes ?: throw NullPointerException()
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
