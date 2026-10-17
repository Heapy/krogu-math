// SPDX-License-Identifier: Apache-2.0
package io.heapy.krogu.math

import kotlin.test.Test
import kotlin.test.assertEquals

class DecimalPrimitiveLowBits591Test {
    private val magnitudes = listOf(
        "0", "1", "9", "f", "3fffffff", "40000000", "40000001",
        "ffffffff", "100000000", "fffffffffffffff", "1000000000000000",
        "1000000000000001", "7fffffffffffffff", "8000000000000000",
        "ffffffffffffffff", "10000000000000000", "3ffffffffffffffffffffff",
        "40000000000000000000000", "123456789abcdef0123456789abcdef",
    )

    // Independent width-bounded bit-vector oracle. Hex input selects bits;
    // multiplication by ten is (v<<1)+(v<<3) with one-bit carries. It uses no
    // production integer arithmetic, Long multiplication or stored limb layout.
    private fun expected(hex: String, negative: Boolean, width: Int, raise: Long): Long {
        if (raise >= width) return 0L
        var value = BooleanArray(width)
        for ((index, character) in hex.reversed().withIndex()) {
            val digit = character.digitToInt(16)
            for (bit in 0..3) {
                val target = 4 * index + bit
                if (target < width) value[target] = digit and (1 shl bit) != 0
            }
        }
        repeat(raise.toInt()) {
            val next = BooleanArray(width)
            var carry = 0
            for (bit in 0 until width) {
                val sum = (if (bit >= 1 && value[bit - 1]) 1 else 0) +
                    (if (bit >= 3 && value[bit - 3]) 1 else 0) + carry
                next[bit] = sum and 1 != 0
                carry = sum ushr 1
            }
            value = next
        }
        if (negative) {
            var carry = true
            for (bit in 0 until width) {
                val inverted = !value[bit]
                value[bit] = inverted xor carry
                carry = inverted && carry
            }
        }
        var result = 0L
        for (bit in 0 until width) if (value[bit]) result = result or (1L shl bit)
        return if (width == 32) result.toInt().toLong() else result
    }

    @Test fun storedProductMatchesIndependentBitRingAcrossWordAndCarryBoundaries() {
        for (hex in magnitudes) for (negative in listOf(false, true)) {
            val source = BigInteger((if (negative) "-" else "") + hex, 16)
            val before = source.toString(16)
            for (width in listOf(32, 64)) {
                val raises = if (width == 32) listOf(0, 1, 2, 7, 9, 18, 19, 29, 30, 31)
                    else listOf(0, 1, 2, 7, 9, 18, 19, 29, 30, 31, 32, 33, 60, 62, 63)
                for (raise in raises) {
                    assertEquals(expected(hex, negative, width, raise.toLong()),
                        source.decimalProductLowBits591(raise, width), "$hex negative=$negative width=$width raise=$raise")
                }
            }
            assertEquals(before, source.toString(16))
        }
    }

    @Test fun publicScalesRetainLowBitsTruncationAndIntEndpointShortcuts() {
        val scales = listOf(0, -1, -2, -7, -9, -18, -19, -29, -30, -31, -32, -33,
            -60, -62, -63, -64, Int.MIN_VALUE)
        for (hex in magnitudes) for (negative in listOf(false, true)) {
            val coefficient = BigInteger((if (negative) "-" else "") + hex, 16)
            for (scale in scales) {
                val source = BigDecimal(coefficient, scale)
                val intValue = expected(hex, negative, 32, -scale.toLong()).toInt()
                val longValue = expected(hex, negative, 64, -scale.toLong())
                val label = "$hex negative=$negative scale=$scale"
                assertEquals(intValue, source.toInt(), label)
                assertEquals(longValue, source.toLong(), label)
                assertEquals(intValue.toByte(), source.toByte(), label)
                assertEquals(intValue.toShort(), source.toShort(), label)
            }
        }
        for ((text, integer) in listOf("12.75" to 12L, "-12.75" to -12L,
            "0.999" to 0L, "-0.999" to 0L, "4294967297.99" to 4294967297L,
            "-4294967297.99" to -4294967297L)) {
            val source = BigDecimal(text)
            assertEquals(integer.toInt(), source.toInt(), text)
            assertEquals(integer, source.toLong(), text)
        }
        val tiny = BigDecimal(BigInteger.ONE, Int.MAX_VALUE)
        assertEquals(0, tiny.toInt())
        assertEquals(0L, tiny.toLong())
        assertEquals(0, BigDecimal(BigInteger.ZERO, Int.MIN_VALUE).toInt())
        assertEquals(0L, BigDecimal(BigInteger.ZERO, Int.MIN_VALUE).toLong())
    }

    @Test fun publicComparisonReturnsRawUnitsAcrossEqualAndDifferentCohorts() {
        val groups = listOf(
            listOf("-1E+50"),
            listOf("-340282366920938463463374607431768211457"),
            listOf("-340282366920938463463374607431768211456"),
            listOf("-2000.00", "-2000.0", "-2E+3"),
            listOf("-1", "-1.00"),
            listOf("-0.00", "0E+10", "0", "0.00"),
            listOf("0.1", "0.10"),
            listOf("1", "1.0", "1.00"),
            listOf("2000.0", "2000.00", "2E+3"),
            listOf("340282366920938463463374607431768211456"),
            listOf("340282366920938463463374607431768211457"),
            listOf("1E+50"),
        ).map { group -> group.map(::BigDecimal) }
        for (i in groups.indices) for (j in groups.indices) {
            val expected = when {
                i < j -> -1
                i > j -> 1
                else -> 0
            }
            for (left in groups[i]) for (right in groups[j])
                assertEquals(expected, left.compareTo(right), "$left vs $right")
        }
        val positiveEndpoint = BigDecimal(BigInteger.ONE, Int.MIN_VALUE)
        val negativeEndpoint = BigDecimal(BigInteger.ONE.negate(), Int.MIN_VALUE)
        assertEquals(1, positiveEndpoint.compareTo(BigDecimal.ONE))
        assertEquals(-1, negativeEndpoint.compareTo(BigDecimal.ONE.negate()))
    }
}
