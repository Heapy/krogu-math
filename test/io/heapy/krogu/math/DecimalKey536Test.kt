package io.heapy.krogu.math

import kotlin.random.Random
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotEquals
import kotlin.test.assertTrue

class DecimalKey536Test {
    @Test
    fun cohortsShareEqualityHashAndOrdering() {
        val cohorts = listOf(
            listOf(BigDecimal("2"), BigDecimal("2.0"), BigDecimal("2.00")),
            listOf(BigDecimal("-2"), BigDecimal("-2.0"), BigDecimal("-2.00")),
            listOf(BigDecimal(BigInteger.ZERO, Int.MIN_VALUE), BigDecimal.ZERO,
                BigDecimal(BigInteger.ZERO, Int.MAX_VALUE)),
            listOf(BigDecimal(BigInteger("123"), Int.MIN_VALUE),
                BigDecimal(BigInteger("1230"), Int.MIN_VALUE + 1)),
            // Canonical scale is Int.MIN_VALUE - 2: stripTrailingZeros would overflow.
            listOf(BigDecimal(BigInteger("100"), Int.MIN_VALUE),
                BigDecimal(BigInteger("1000"), Int.MIN_VALUE + 1)),
            listOf(BigDecimal(BigInteger("-100"), Int.MIN_VALUE),
                BigDecimal(BigInteger("-10000"), Int.MIN_VALUE + 2)),
            listOf(BigDecimal(BigInteger.ONE, Int.MAX_VALUE),
                BigDecimal(BigInteger.TEN, Int.MAX_VALUE)), // distinct values at the high endpoint
            listOf(BigDecimal("9".repeat(2048) + ".00"), BigDecimal("9".repeat(2048) + ".000")),
        )
        cohorts.forEachIndexed { index, cohort ->
            if (index == 6) {
                assertFalse(cohort[0].numericallyEquals(cohort[1]))
                return@forEachIndexed
            }
            val keys = cohort.map(::DecimalKey)
            for (left in keys) for (right in keys) {
                assertEquals(left, right)
                assertEquals(left.hashCode(), right.hashCode())
                assertEquals(0, left.compareTo(right))
            }
            assertEquals(1, keys.toHashSet().size)
            assertEquals("found", hashMapOf(keys.first() to "found")[keys.last()])
            assertTrue(cohort.first().numericallyEquals(cohort.last()))
        }
    }

    @Test
    fun originalRepresentationAndCrossTypeEqualityRemainExplicit() {
        val source = BigDecimal("2.00")
        val hash = source.hashCode()
        val key: Any = source.toDecimalKey()
        assertFalse(key.equals(source))
        assertFalse(source.equals(key))
        assertFalse(key.equals(BigInteger("2")))
        assertFalse(key.equals(2))
        assertNotEquals(source, BigDecimal("2.0"))
        assertEquals(BigInteger("200"), source.unscaledValue())
        assertEquals(2, source.scale())
        assertEquals(hash, source.hashCode())
        assertEquals(2, hashSetOf(source, BigDecimal("2.0")).size)
        assertEquals("1E2147483650", DecimalKey(BigDecimal(BigInteger("100"), Int.MIN_VALUE)).toString())
    }

    @Test
    fun orderingMatchesDecimalComparison() {
        val random = Random(536)
        val values = mutableListOf(BigDecimal.ZERO, BigDecimal("-1"), BigDecimal.ONE)
        repeat(80) {
            val coefficient = BigInteger(random.nextLong(-1_000_000_000, 1_000_000_000).toString())
            val scale = random.nextInt(-100, 100)
            values += BigDecimal(coefficient, scale)
            values += BigDecimal(coefficient.multiply(BigInteger.TEN), scale + 1)
        }
        values += listOf(Int.MIN_VALUE, Int.MIN_VALUE + 1, Int.MAX_VALUE - 1, Int.MAX_VALUE).flatMap { scale ->
            listOf("-12300", "-1", "0", "1", "12300").map { BigDecimal(BigInteger(it), scale) }
        }
        for (left in values) for (right in values) {
            val actual = left.toDecimalKey().compareTo(right.toDecimalKey())
            val expected = left.compareTo(right)
            assertEquals(expected.compareTo(0), actual.compareTo(0), "$left vs $right")
            assertEquals(actual == 0, left.numericallyEquals(right))
        }
        val keys = values.map(::DecimalKey).sorted()
        for (index in 1 until keys.size) assertTrue(keys[index - 1] <= keys[index])
        assertEquals(values.sorted().distinctBy { it.toDecimalKey() }.map(::DecimalKey), keys.distinct())
    }

    @Test
    fun hugeSignificandsAndNegativeOrdering() {
        val prefix = "8".repeat(2048)
        val values = listOf("-" + prefix + ".1", "-" + prefix, "0", prefix, prefix + ".01", prefix + ".1")
            .map(::BigDecimal)
        val keys = values.map(::DecimalKey)
        for (index in 1 until keys.size) assertTrue(keys[index - 1] < keys[index])
        assertEquals(keys, keys.reversed().sorted())
        assertEquals(keys[3], BigDecimal(prefix + ".000").toDecimalKey())
    }

    @Test
    fun keysSnapshotMutableSubclassMetadata() {
        val source = object : BigDecimal("2.00") {
            var currentScale = 2
            override fun scale(): Int = currentScale
        }
        val key = source.toDecimalKey()
        source.currentScale = -10
        assertEquals(BigDecimal("2").toDecimalKey(), key)
        assertEquals(0, key.compareTo(BigDecimal("2.0").toDecimalKey()))
    }
}
