// SPDX-License-Identifier: Apache-2.0
package io.heapy.krogu.math

import kotlin.test.*

/** Small algebraic and mutation witnesses, independent of platform numerical code. */
class ValueIdentityContractTest {
    private fun equalityClass(a: Any, b: Any, c: Any) {
        for (left in listOf(a, b, c)) {
            assertTrue(left.equals(left), "reflexivity")
            assertFalse(left.equals(null))
            assertFalse(left.equals("unrelated"))
            for (right in listOf(a, b, c)) {
                assertTrue(left.equals(right), "symmetry/transitivity")
                assertEquals(left.hashCode(), right.hashCode())
                assertEquals(left.hashCode(), left.hashCode(), "stable hash")
            }
        }
        assertEquals(1, listOf(a, b, c).toSet().size)
        val map = mutableMapOf(a to "first")
        map[b] = "second"
        assertEquals(1, map.size)
        assertEquals("second", map[c])
    }

    private class DelegatingInteger(text: String) : BigInteger(text) {
        override fun signum(): Int = super.signum()
        override fun bitLength(): Int = super.bitLength()
        override fun toByteArray(): ByteArray = super.toByteArray()
    }

    private class DelegatingDecimal(text: String) : BigDecimal(text) {
        override fun signum(): Int = super.signum()
        override fun precision(): Int = super.precision()
        override fun scale(): Int = super.scale()
        override fun unscaledValue(): BigInteger = super.unscaledValue()
    }

    @Test fun integerEqualityIncludesIndependentConstructionAndTruthfulSubclasses() {
        for (text in listOf("0", "1", "-1", "1073741824", "-4294967296", "123456789012345678901234567890")) {
            val value = BigInteger(text)
            equalityClass(value, BigInteger(value.toByteArray()), DelegatingInteger(text))
            assertFalse(value.equals(BigDecimal(text)))
            assertFalse(value.equals(BigInteger(text).add(BigInteger.ONE)))
        }
    }

    @Test fun decimalEqualityRequiresTheSameScaleEvenForZero() {
        for (text in listOf("0.00", "-0.00", "2.00", "-2.00", "12345678901234567890.00")) {
            val value = BigDecimal(text)
            equalityClass(value, BigDecimal(value.unscaledValue(), value.scale()), DelegatingDecimal(text))
            val cohort = value.setScale(value.scale() + 1)
            assertEquals(0, value.compareTo(cohort))
            assertEquals(0, cohort.compareTo(value))
            assertFalse(value.equals(cohort))
            assertFalse(cohort.equals(value))
            assertFalse(value.equals(value.unscaledValue()))
            assertEquals(2, setOf(value, cohort).size)
            val map = mapOf(value to "original scale", cohort to "extra zero")
            assertEquals("original scale", map[BigDecimal(text)])
            assertEquals("extra zero", map[BigDecimal(cohort.unscaledValue(), cohort.scale())])
        }
    }

    @Test fun decimalMinAndMaxTiesReturnTheReceiverIncludingTruthfulSubclasses() {
        for (mask in 0..3) {
            val a = if (mask and 1 == 0) BigDecimal("2.0") else DelegatingDecimal("2.0")
            val b = if (mask and 2 == 0) BigDecimal("2.00") else DelegatingDecimal("2.00")
            assertEquals(0, a.compareTo(b))
            assertFalse(a.equals(b))
            assertSame(a, a.min(b), "min receiver a, mask=$mask")
            assertSame(a, a.max(b), "max receiver a, mask=$mask")
            assertSame(b, b.min(a), "min receiver b, mask=$mask")
            assertSame(b, b.max(a), "max receiver b, mask=$mask")
        }
    }

    @Test fun integerMinAndMaxTiesReturnTheDistinctArgumentIncludingTruthfulSubclasses() {
        for (mask in 0..3) {
            val a = if (mask and 1 == 0) BigInteger("2") else DelegatingInteger("2")
            val b = if (mask and 2 == 0) BigInteger("2") else DelegatingInteger("2")
            assertNotSame(a, b)
            assertEquals(0, a.compareTo(b))
            assertSame(b, a.min(b), "min argument b, mask=$mask")
            assertSame(b, a.max(b), "max argument b, mask=$mask")
            assertSame(a, b.min(a), "min argument a, mask=$mask")
            assertSame(a, b.max(a), "max argument a, mask=$mask")
        }
    }

    private fun <T> ordering(values: List<T>, compare: (T, T) -> Int) {
        for (a in values) {
            assertEquals(0, compare(a, a), "self comparison")
            for (b in values) {
                val ab = compare(a, b).compareTo(0)
                assertEquals(-ab, compare(b, a).compareTo(0), "antisymmetry")
                for (c in values) {
                    if (ab <= 0 && compare(b, c) <= 0) assertTrue(compare(a, c) <= 0, "transitivity")
                    if (ab == 0) assertEquals(compare(a, c).compareTo(0), compare(b, c).compareTo(0), "cohort substitution")
                }
            }
        }
    }

    @Test fun integerAndDecimalOrderComposeWithCommonCollections() {
        val integerTexts = listOf("-4294967296", "-1", "0", "1", "1073741824", "4294967296")
        val integers = integerTexts.flatMap { listOf(BigInteger(it), DelegatingInteger(it)) }
        ordering(integers) { a, b -> a.compareTo(b) }
        assertEquals(integerTexts, integers.reversed().toSet().sorted().map { it.toString() })
        val decimalTexts = listOf("-2.00", "-0.1", "0.0", "0.00", "2", "2.0", "2.00", "10")
        val decimals = decimalTexts.flatMap { listOf(BigDecimal(it), DelegatingDecimal(it)) }
        ordering(decimals) { a, b -> a.compareTo(b) }
        assertEquals(decimalTexts, decimals.toSet().sorted().map { it.toString() })
        assertEquals(listOf("-2.00", "-0.1", "0.0", "2", "10"),
            decimals.sorted().distinctBy { it.stripTrailingZeros() }.map { it.toString() })
        // The two ill-typed null calls move to ComparisonNull574JvmTest.commonIdentityNullChecks.
    }

    @Test fun mutableInputSlicesCannotChangeCollectionKeys() {
        for (signed in listOf(false, true)) {
            val input = byteArrayOf(99, 1, 0, 0, 0, 1, 88)
            val value = if (signed) BigInteger(-1, input, 1, 5) else BigInteger(input, 1, 5)
            val expected = BigInteger(if (signed) "-4294967297" else "4294967297")
            val hash = value.hashCode()
            val map = mapOf(value to "retained")
            input.fill(0)
            val first = value.toByteArray()
            val second = value.toByteArray()
            assertNotSame(first, second)
            first.fill(0)
            assertEquals(expected, BigInteger(second))
            assertEquals(expected, value)
            assertEquals(hash, value.hashCode())
            assertEquals("retained", map[expected])
        }
        for (sliced in listOf(false, true)) for (rounded in listOf(false, true)) {
            val text = "-123.4500"
            val input = (if (sliced) "xx${text}yy" else text).toCharArray()
            val value = when {
                sliced && rounded -> BigDecimal(input, 2, text.length, MathContext(5, RoundingMode.HALF_EVEN))
                sliced -> BigDecimal(input, 2, text.length)
                rounded -> BigDecimal(input, MathContext(5, RoundingMode.HALF_EVEN))
                else -> BigDecimal(input)
            }
            val expected = BigDecimal(if (rounded) "-123.45" else text)
            val hash = value.hashCode()
            val map = mapOf(value to "retained")
            input.fill('9')
            value.unscaledValue().toByteArray().fill(0)
            assertEquals(expected, value)
            assertEquals(hash, value.hashCode())
            assertEquals("retained", map[expected])
        }
    }

    private fun integerPair(operation: () -> Array<BigInteger>, first: String, second: String) {
        val a = operation()
        val b = operation()
        assertNotSame(a, b)
        assertEquals(listOf(BigInteger(first), BigInteger(second)), a.toList())
        a[0].toByteArray().fill(0)
        a[1].toByteArray().fill(0)
        a[0] = BigInteger("999")
        a[1] = BigInteger("-999")
        val expected = listOf(BigInteger(first), BigInteger(second))
        assertEquals(expected, b.toList())
        assertEquals(expected, operation().toList())
    }

    @Test fun integerResultPairSlotsAreIndependentAcrossCalls() {
        for ((numerator, divisor, quotient, remainder) in listOf(
            listOf("100", "7", "14", "2"), listOf("-100", "7", "-14", "-2"),
            listOf("1", "10", "0", "1"), listOf("100", "10", "10", "0"))) {
            val n = BigInteger(numerator)
            val d = BigInteger(divisor)
            val hashes = n.hashCode() to d.hashCode()
            integerPair({ n.divideAndRemainder(d) }, quotient, remainder)
            assertEquals(BigInteger(numerator), n)
            assertEquals(BigInteger(divisor), d)
            assertEquals(hashes, n.hashCode() to d.hashCode())
        }
        for ((text, root, remainder) in listOf(listOf("0", "0", "0"), listOf("1", "1", "0"),
            listOf("100", "10", "0"), listOf("101", "10", "1"))) {
            val n = BigInteger(text)
            val hash = n.hashCode()
            integerPair({ n.sqrtAndRemainder() }, root, remainder)
            assertEquals(BigInteger(text), n)
            assertEquals(hash, n.hashCode())
        }
    }

    @Test fun decimalResultPairSlotsAreIndependentAcrossCalls() {
        for (context in listOf<MathContext?>(null, MathContext(7, RoundingMode.HALF_EVEN))) {
            val n = BigDecimal("100.00")
            val d = BigDecimal("7.0")
            val hashes = n.hashCode() to d.hashCode()
            fun pair() = if (context == null) n.divideAndRemainder(d) else n.divideAndRemainder(d, context)
            val a = pair()
            val b = pair()
            val expected = listOf(BigDecimal("14.0"), BigDecimal("2.00"))
            assertNotSame(a, b)
            assertEquals(expected, a.toList())
            a[0].unscaledValue().toByteArray().fill(0)
            a[1].unscaledValue().toByteArray().fill(0)
            a[0] = BigDecimal("999")
            a[1] = BigDecimal("-999")
            assertEquals(expected, b.toList())
            assertEquals(expected, pair().toList())
            assertEquals(BigDecimal("100.00"), n)
            assertEquals(BigDecimal("7.0"), d)
            assertEquals(hashes, n.hashCode() to d.hashCode())
        }
    }

    @Test fun sharedConstantsRemainStableAfterCompositionsAndOutputMutations() {
        val integers = listOf(BigInteger.ZERO, BigInteger.ONE, BigInteger.TEN)
        val decimals = listOf(BigDecimal.ZERO, BigDecimal.ONE, BigDecimal.TEN)
        val ih = integers.map { it.hashCode() }
        val dh = decimals.map { it.hashCode() }
        for (value in integers) {
            value.add(BigInteger.ZERO).negate().abs().toByteArray().fill(0)
            value.multiply(BigInteger.ONE).divideAndRemainder(BigInteger.TEN)[0] = BigInteger("999")
            value.sqrtAndRemainder()[0] = BigInteger("999")
        }
        for (value in decimals) {
            value.plus().add(BigDecimal.ZERO).multiply(BigDecimal.ONE).unscaledValue().toByteArray().fill(0)
            value.divideAndRemainder(BigDecimal.TEN)[0] = BigDecimal("999")
        }
        assertEquals(listOf("0", "1", "10"), integers.map { it.toString() })
        assertEquals(listOf("0", "1", "10"), decimals.map { it.toString() })
        assertEquals(ih, integers.map { it.hashCode() })
        assertEquals(dh, decimals.map { it.hashCode() })
        assertEquals(listOf(0, 0, 0), decimals.map { it.scale() })
    }

    @Test fun everyContextModeAndPrecisionHasFieldBasedIdentity() {
        val contexts = listOf(0, 1, 7, 34, Int.MAX_VALUE).flatMap { precision ->
            RoundingMode.entries.map { mode -> MathContext(precision, mode) }
        }
        for (context in contexts) {
            val text = "precision=${context.precision} roundingMode=${context.roundingMode.name}"
            assertEquals(text, context.toString())
            assertEquals(context.precision, context.getPrecision())
            assertSame(context.roundingMode, context.getRoundingMode())
            equalityClass(context, MathContext(context.precision, context.roundingMode), MathContext(text))
            // Enum hash is local object identity: compare the equal objects, never JDK enum hashes.
            for (other in contexts) assertEquals(
                context.precision == other.precision && context.roundingMode == other.roundingMode,
                context.equals(other))
        }
        assertEquals(40, contexts.toSet().size)
        assertEquals(MathContext(1, RoundingMode.HALF_UP), MathContext(1))
        for ((constant, precision, mode) in listOf(
            Triple(MathContext.UNLIMITED, 0, RoundingMode.HALF_UP),
            Triple(MathContext.DECIMAL32, 7, RoundingMode.HALF_EVEN),
            Triple(MathContext.DECIMAL64, 16, RoundingMode.HALF_EVEN),
            Triple(MathContext.DECIMAL128, 34, RoundingMode.HALF_EVEN))) {
            equalityClass(constant, MathContext(precision, mode), MathContext(constant.toString()))
        }
        assertFailsWith<IllegalArgumentException> { MathContext(-1) }
        // Null rounding mode entry retained in Validation578StandaloneTest (JVM).
    }

    @Suppress("DEPRECATION")
    @Test fun roundingModesHaveStableNamesLegacyMappingAndIsolatedSnapshots() {
        val names = listOf("UP", "DOWN", "CEILING", "FLOOR", "HALF_UP", "HALF_DOWN", "HALF_EVEN", "UNNECESSARY")
        val legacy = listOf(BigDecimal.ROUND_UP, BigDecimal.ROUND_DOWN, BigDecimal.ROUND_CEILING,
            BigDecimal.ROUND_FLOOR, BigDecimal.ROUND_HALF_UP, BigDecimal.ROUND_HALF_DOWN,
            BigDecimal.ROUND_HALF_EVEN, BigDecimal.ROUND_UNNECESSARY)
        val snapshot = RoundingMode.values()
        val second = RoundingMode.values()
        assertNotSame(snapshot, second)
        for ((index, name) in names.withIndex()) {
            val mode = RoundingMode.entries[index]
            assertEquals(index, mode.ordinal)
            assertEquals(name, mode.name)
            assertEquals(name, mode.toString())
            assertEquals(index, legacy[index])
            assertSame(mode, RoundingMode.valueOf(name))
            assertSame(mode, RoundingMode.valueOf(legacy[index]))
            equalityClass(mode, snapshot[index], second[index])
        }
        snapshot.fill(RoundingMode.DOWN)
        val mutableCopy = RoundingMode.entries.toMutableList()
        mutableCopy.clear()
        assertEquals(names, second.map { it.name })
        assertEquals(names, RoundingMode.values().map { it.name })
        assertEquals(names, RoundingMode.entries.map { it.name })
        assertEquals(8, RoundingMode.entries.toSet().size)
        for (invalid in listOf(Int.MIN_VALUE, -1, 8, Int.MAX_VALUE))
            assertFailsWith<IllegalArgumentException> { RoundingMode.valueOf(invalid) }
        for (invalid in listOf("", "up", " UP", "UP ", "HALF-EVEN", "8"))
            assertFailsWith<IllegalArgumentException> { RoundingMode.valueOf(invalid) }
    }
}
