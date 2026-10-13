package io.heapy.krogu.math

import kotlin.jvm.JvmField
import kotlin.jvm.JvmStatic
import kotlin.math.pow
import kotlin.random.Random

/** Immutable signed integer. Magnitudes use little-endian base-2^30 limbs. */
class BigInteger private constructor(private val sign: Int, private val words: IntArray) :
    Number(), Comparable<BigInteger>, MathSerializable {

    private constructor(value: BigInteger) : this(value.sign, value.words)
    private fun writeReplace(): Any = serializationProxy(this)
    constructor(value: String) : this(value, 10)
    constructor(value: String, radix: Int) : this(parse(value, radix))
    constructor(value: ByteArray) : this(fromBytes(value, 0, value.size, true))
    constructor(value: ByteArray, off: Int, len: Int) : this(fromBytes(value, off, len, true))
    constructor(signum: Int, magnitude: ByteArray) : this(signum, magnitude, 0, magnitude.size)
    constructor(signum: Int, magnitude: ByteArray, off: Int, len: Int) : this(signedBytes(signum, magnitude, off, len))
    constructor(numBits: Int, rnd: Random) : this(randomBits(numBits, rnd))
    constructor(bitLength: Int, certainty: Int, rnd: Random) : this(generatePrime(bitLength, certainty, rnd))

    fun signum(): Int = sign
    fun abs(): BigInteger = if (sign < 0) negate() else this
    fun negate(): BigInteger = if (sign == 0) this else BigInteger(-sign, words)
    fun add(value: BigInteger): BigInteger {
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
    fun subtract(value: BigInteger): BigInteger = add(value.negate())
    fun multiply(value: BigInteger): BigInteger {
        if (sign == 0 || value.sign == 0) return ZERO
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
    fun parallelMultiply(value: BigInteger): BigInteger = multiply(value)
    fun divide(value: BigInteger): BigInteger = divideAndRemainder(value)[0]
    fun remainder(value: BigInteger): BigInteger = divideAndRemainder(value)[1]

    fun divideAndRemainder(value: BigInteger): Array<BigInteger> {
        if (value.sign == 0) throw ArithmeticException("BigInteger divide by zero")
        val a = abs()
        val b = value.abs()
        if (a < b) return arrayOf(ZERO, this)
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
        val result = IntArray((a.magnitudeBits() + LIMB_BITS - 1) / LIMB_BITS)
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

    fun pow(exponent: Int): BigInteger {
        if (exponent < 0) throw ArithmeticException("Negative exponent")
        if (sign != 0 && (magnitudeBits().toLong() - 1) * exponent >= Int.MAX_VALUE)
            throw ArithmeticException("BigInteger overflow")
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
    fun gcd(value: BigInteger): BigInteger {
        var a = abs()
        var b = value.abs()
        while (b.sign != 0) { val r = a.remainder(b); a = b; b = r }
        return a
    }
    fun mod(m: BigInteger): BigInteger {
        if (m.sign <= 0) throw ArithmeticException("BigInteger: modulus not positive")
        val r = remainder(m)
        return if (r.sign < 0) r.add(m) else r
    }
    fun modPow(exponent: BigInteger, m: BigInteger): BigInteger {
        if (m.sign <= 0) throw ArithmeticException("BigInteger: modulus not positive")
        var base = if (exponent.sign < 0) modInverse(m) else mod(m)
        var result = ONE.mod(m)
        val e = exponent.abs()
        for (i in 0 until e.bitLength()) {
            if (e.testBit(i)) result = result.multiply(base).mod(m)
            if (i + 1 < e.bitLength()) base = base.multiply(base).mod(m)
        }
        return result
    }
    fun modInverse(m: BigInteger): BigInteger {
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
    fun sqrt(): BigInteger {
        if (sign < 0) throw ArithmeticException("Negative BigInteger")
        if (this < TWO) return this
        var x = ONE.shiftLeft((bitLength() + 1) / 2)
        while (true) {
            val next = x.add(divide(x)).shiftRight(1)
            if (next >= x) return x
            x = next
        }
    }
    fun sqrtAndRemainder(): Array<BigInteger> {
        val root = sqrt()
        return arrayOf(root, subtract(root.multiply(root)))
    }

    fun shiftLeft(n: Int): BigInteger {
        if (sign == 0 || n == 0) return this
        if (n < 0) return if (n == Int.MIN_VALUE) (if (sign < 0) ONE.negate() else ZERO) else shiftRight(-n)
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
    fun shiftRight(n: Int): BigInteger {
        if (sign == 0 || n == 0) return this
        if (n < 0) {
            if (n == Int.MIN_VALUE) throw ArithmeticException("BigInteger overflow")
            return shiftLeft(-n)
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
        return if (sign < 0 && getLowestSetBit() < n) truncated.subtract(ONE) else truncated
    }

    private fun magnitudeBits(): Int = if (words.isEmpty()) 0 else (words.size - 1) * LIMB_BITS + 32 - words.last().countLeadingZeroBits()
    fun bitLength(): Int = if (sign < 0) abs().subtract(ONE).magnitudeBits() else magnitudeBits()
    fun bitCount(): Int = (if (sign < 0) abs().subtract(ONE) else this).words.sumOf { it.countOneBits() }
    fun getLowestSetBit(): Int {
        for (i in words.indices) if (words[i] != 0) return i * LIMB_BITS + words[i].countTrailingZeroBits()
        return -1
    }
    fun testBit(n: Int): Boolean {
        if (n < 0) throw ArithmeticException("Negative bit address")
        if (sign < 0) return !abs().subtract(ONE).testBit(n)
        return n / LIMB_BITS < words.size && words[n / LIMB_BITS] and (1 shl (n % LIMB_BITS)) != 0
    }
    fun setBit(n: Int): BigInteger = if (testBit(n)) this else add(ONE.shiftLeft(n))
    fun clearBit(n: Int): BigInteger = if (testBit(n)) subtract(ONE.shiftLeft(n)) else this
    fun flipBit(n: Int): BigInteger = if (testBit(n)) clearBit(n) else setBit(n)
    fun not(): BigInteger = negate().subtract(ONE)
    fun and(value: BigInteger): BigInteger = bitwise(value) { a, b -> a and b }
    fun or(value: BigInteger): BigInteger = bitwise(value) { a, b -> a or b }
    fun xor(value: BigInteger): BigInteger = bitwise(value) { a, b -> a xor b }
    fun andNot(value: BigInteger): BigInteger = and(value.not())
    private fun bitwise(value: BigInteger, op: (Int, Int) -> Int): BigInteger {
        val length = maxOf(words.size, value.words.size) + 1
        val a = twosComplement(length)
        val b = value.twosComplement(length)
        val out = IntArray(length) { op(a[it], b[it]) and MASK.toInt() }
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

    fun isProbablePrime(certainty: Int): Boolean {
        if (certainty <= 0) return true
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
        repeat((certainty.toLong() + 1L).div(2L).coerceAtMost(Int.MAX_VALUE.toLong()).toInt()) {
            var a: BigInteger
            do { a = randomBits(n.bitLength(), Random.Default) } while (a < TWO || a >= nm1)
            var x = a.modPow(d, n)
            if (x != ONE && x != nm1) {
                var witnessed = true
                for (r in 1 until s) {
                    x = x.multiply(x).mod(n)
                    if (x == nm1) { witnessed = false; break }
                }
                if (witnessed) return false
            }
        }
        return true
    }
    fun nextProbablePrime(): BigInteger {
        if (sign < 0) throw ArithmeticException("Negative start")
        if (this < TWO) return TWO
        var n = add(ONE).setBit(0)
        while (!n.isProbablePrime(100)) n = n.add(TWO)
        return n
    }

    override fun compareTo(other: BigInteger): Int = if (sign != other.sign) sign.compareTo(other.sign) else sign * compareMagnitude(words, other.words)
    fun min(value: BigInteger): BigInteger = if (this <= value) this else value
    fun max(value: BigInteger): BigInteger = if (this >= value) this else value
    override fun equals(other: Any?): Boolean = other is BigInteger && sign == other.sign && words.contentEquals(other.words)
    override fun hashCode(): Int {
        var hash = 0
        // Java's public hash behavior uses unsigned 32-bit chunks, independent of our limb layout.
        val count = (magnitudeBits().toLong() + 31).div(32).toInt()
        for (i in count - 1 downTo 0) {
            var chunk = 0L
            for (b in 0..31) {
                val bit = i.toLong() * 32 + b
                if (bit < magnitudeBits() && abs().testBit(bit.toInt())) chunk = chunk or (1L shl b)
            }
            hash = (31L * hash + chunk).toInt()
        }
        return hash * sign
    }
    override fun toString(): String = toString(10)
    fun toString(radix: Int): String {
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
    fun toByteArray(): ByteArray {
        val length = bitLength() / 8 + 1
        val magnitude = if (sign < 0) abs().subtract(ONE) else this
        return ByteArray(length) { index ->
            var byte = 0
            for (b in 0..7) if (magnitude.testBit((length - index - 1) * 8 + b)) byte = byte or (1 shl b)
            (if (sign < 0) byte.inv() else byte).toByte()
        }
    }
    override fun toInt(): Int = toLong().toInt()
    override fun toLong(): Long {
        var value = 0L
        for (i in minOf(words.size, 3) - 1 downTo 0) value = (value shl LIMB_BITS) or words[i].toLong()
        return if (sign < 0) -value else value
    }
    override fun toByte(): Byte = toInt().toByte()
    override fun toShort(): Short = toInt().toShort()
    override fun toDouble(): Double = floatingValue(53, 1024)
    override fun toFloat(): Float = floatingValue(24, 128).toFloat()
    private fun floatingValue(precision: Int, maxExponent: Int): Double {
        if (sign == 0) return 0.0
        val bits = magnitudeBits()
        if (bits > maxExponent) return if (sign < 0) Double.NEGATIVE_INFINITY else Double.POSITIVE_INFINITY
        val shift = maxOf(0, bits - precision)
        var top = abs().shiftRight(shift).toLong()
        if (shift > 0 && abs().testBit(shift - 1) && (getLowestSetBit() < shift - 1 || top and 1L != 0L)) top++
        return sign * top.toDouble() * 2.0.pow(shift)
    }
    fun intValueExact(): Int { if (bitLength() > 31) throw ArithmeticException("BigInteger out of int range"); return toInt() }
    fun longValueExact(): Long { if (bitLength() > 63) throw ArithmeticException("BigInteger out of long range"); return toLong() }
    fun shortValueExact(): Short { val n = intValueExact(); if (n !in Short.MIN_VALUE..Short.MAX_VALUE) throw ArithmeticException("BigInteger out of short range"); return n.toShort() }
    fun byteValueExact(): Byte { val n = intValueExact(); if (n !in Byte.MIN_VALUE..Byte.MAX_VALUE) throw ArithmeticException("BigInteger out of byte range"); return n.toByte() }

    operator fun plus(other: BigInteger): BigInteger = add(other)
    operator fun minus(other: BigInteger): BigInteger = subtract(other)
    operator fun times(other: BigInteger): BigInteger = multiply(other)
    operator fun div(other: BigInteger): BigInteger = divide(other)
    operator fun rem(other: BigInteger): BigInteger = remainder(other)
    operator fun unaryMinus(): BigInteger = negate()
    operator fun unaryPlus(): BigInteger = this

    companion object {
        private const val LIMB_BITS = 30
        private const val MASK = (1L shl LIMB_BITS) - 1
        @JvmField val ZERO = BigInteger(0, intArrayOf())
        @JvmField val ONE = BigInteger(1, intArrayOf(1))
        @JvmField val TWO = BigInteger(1, intArrayOf(2))
        @JvmField val TEN = BigInteger(1, intArrayOf(10))
        @JvmStatic fun valueOf(value: Long): BigInteger {
            if (value == 0L) return ZERO
            var magnitude = if (value < 0) 0uL - value.toULong() else value.toULong()
            val result = IntArray(3)
            var i = 0
            while (magnitude != 0uL) { result[i++] = (magnitude and MASK.toULong()).toInt(); magnitude = magnitude shr LIMB_BITS }
            return normalized(if (value < 0) -1 else 1, result)
        }
        @JvmStatic fun probablePrime(bitLength: Int, rnd: Random): BigInteger = generatePrime(bitLength, 100, rnd)
        private fun generatePrime(bitLength: Int, certainty: Int, rnd: Random): BigInteger {
            if (bitLength < 2) throw ArithmeticException("bitLength < 2")
            if (bitLength == 2) return valueOf(rnd.nextInt(2, 4).toLong())
            while (true) {
                val n = randomBits(bitLength, rnd).setBit(bitLength - 1).setBit(0)
                if (n.isProbablePrime(certainty)) return n
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
            if (radix !in 2..36 || value.isEmpty()) throw NumberFormatException("Invalid BigInteger")
            var start = 0
            var sign = 1
            if (value[0] == '-' || value[0] == '+') { sign = if (value[0] == '-') -1 else 1; start++ }
            if (start == value.length) throw NumberFormatException("No digits")
            var n = ZERO
            val base = valueOf(radix.toLong())
            for (i in start until value.length) {
                val digit = value[i].digitToIntOrNull(radix) ?: throw NumberFormatException("Invalid digit")
                n = n.multiply(base).add(valueOf(digit.toLong()))
            }
            return if (sign < 0) n.negate() else n
        }
        private fun fromBytes(bytes: ByteArray, off: Int, len: Int, twos: Boolean): BigInteger {
            checkBounds(bytes, off, len)
            if (twos && len == 0) throw NumberFormatException("Zero length BigInteger")
            var n = ZERO
            for (i in off until off + len) n = n.shiftLeft(8).add(valueOf((bytes[i].toInt() and 255).toLong()))
            return if (twos && bytes[off] < 0) n.subtract(ONE.shiftLeft(len * 8)) else n
        }
        private fun signedBytes(signum: Int, bytes: ByteArray, off: Int, len: Int): BigInteger {
            if (signum !in -1..1) throw NumberFormatException("Invalid signum value")
            val n = fromBytes(bytes, off, len, false)
            if (signum == 0 && n.sign != 0) throw NumberFormatException("signum-magnitude mismatch")
            return if (signum < 0) n.negate() else n
        }
        private fun checkBounds(bytes: ByteArray, off: Int, len: Int) {
            if (off < 0 || len < 0 || off > bytes.size - len) throw IndexOutOfBoundsException("Invalid byte range")
        }
        private fun normalized(sign: Int, words: IntArray): BigInteger {
            var length = words.size
            while (length > 0 && words[length - 1] == 0) length--
            return if (length == 0) ZERO else BigInteger(sign, if (length == words.size) words else words.copyOf(length))
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
