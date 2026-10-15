package io.heapy.krogu.math

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class ScaleBoundaryOracleTest {
    private class OracleCursor(offset: Int) {
        private val expected = scaleBoundaryExpected().drop(offset).iterator()
        private var count = offset
        fun verify(label: String, action: () -> Any?) {
            verifyExpected(label, null, action)
        }
        fun verifyAddition(label: String, a: BigDecimal, b: BigDecimal, mc: MathContext) {
            val contract = if (DecimalScaleContractExpected.affectedAddition(a, b, mc))
                DecimalScaleContractExpected.addition(a, b, mc) else null
            verifyExpected(label, contract) { a.add(b, mc) }
        }
        fun verifyDivision(label: String, a: BigDecimal, b: BigDecimal, mc: MathContext? = null) {
            verifyExpected(label, DivisionNormalizationExpected.correctedText(a, b, mc) ?: PreferredScaleStringModel.correctedText(a, b, mc)) {
                if (mc == null) a.divide(b) else a.divide(b, mc)
            }
        }
        fun verifySqrt(label: String, a: BigDecimal, mc: MathContext) {
            val contract = DecimalExactRoot563Expected.corrected(a.unscaledValue().toString(), a.scale(), mc)
            if (contract == null) {
                verify(label) { a.sqrt(mc) }
                return
            }
            assertTrue(expected.hasNext(), "Oracle ended before $label")
            val diagnostic = expected.next()
            assertEquals("!ArithmeticException", diagnostic, "retained JDK sqrt defect: $label")
            val result = a.sqrt(mc)
            assertEquals(contract.coefficient, result.unscaledValue().toString(), "$count: coefficient $label; JDK=$diagnostic")
            assertEquals(contract.scale, result.scale(), "$count: scale $label; JDK=$diagnostic")
            assertEquals(contract.text(), result.toString(), "$count: text $label; JDK=$diagnostic")
            count++
        }
        private fun verifyExpected(label: String, contract: String?, action: () -> Any?) {
            assertTrue(expected.hasNext(), "Oracle ended before $label")
            val actual = try { action().toString() } catch (e: Exception) { "!${e::class.simpleName}" }
            val diagnostic = expected.next()
            // Keep consuming and checking the frozen JDK defect, while requiring
            // the bounded exact-intermediate contract for this proved domain.
            if (contract != null) assertEquals("!ArithmeticException", diagnostic, "retained JDK defect: $label")
            assertEquals(contract ?: diagnostic, actual, "$count: $label; JDK=$diagnostic")
            count++
        }
        fun finish(end: Int, requireEnd: Boolean = false) {
            assertEquals(end, count)
            if (requireEnd) assertFalse(expected.hasNext(), "Unconsumed oracle outcomes")
        }
    }

    @Test fun compactCoefficientsAndScaleEndpoints() {
        val cursor = OracleCursor(0)
        val verify = cursor::verify
        val scales = listOf(Int.MIN_VALUE, Int.MIN_VALUE + 1, Int.MIN_VALUE + 5, -1, 0, 1, Int.MAX_VALUE - 5, Int.MAX_VALUE - 1, Int.MAX_VALUE)
        val inputs = listOf(-1000L, -100, -3, -1, 0, 1, 3, 100, 1000).flatMap { n -> scales.map { BigDecimal.valueOf(n, it) } }
        for (a in inputs) {
            verify("canonical parse $a") { BigDecimal(a.toString()) }
            verify("engineering $a") { a.toEngineeringString() }
            verify("strip $a") { a.stripTrailingZeros() }
            for (b in inputs) {
                verify("multiply $a,$b") { a.multiply(b) }
                cursor.verifyDivision("exact divide $a,$b", a, b)
                for (p in listOf(1, 2, 7)) for (mode in listOf(RoundingMode.DOWN, RoundingMode.HALF_EVEN, RoundingMode.UNNECESSARY)) {
                    val context = MathContext(p, mode)
                    cursor.verifyDivision("divide $a,$b,$context", a, b, context)
                    cursor.verifyAddition("add $a,$b,$context", a, b, context)
                }
            }
            for (p in listOf(0, 1, 2, 7)) for (mode in RoundingMode.entries) {
                val context = MathContext(p, mode)
                cursor.verifySqrt("sqrt $a,$context", a, context)
                verify("round $a,$context") { a.round(context) }
                verify("pow $a,2,$context") { a.pow(2, context) }
            }
        }
        cursor.finish(139239)
    }

    @Test fun wideCoefficientsBatch00() = wideBatch(0)
    @Test fun wideCoefficientsBatch01() = wideBatch(1)
    @Test fun wideCoefficientsBatch02() = wideBatch(2)
    @Test fun wideCoefficientsBatch03() = wideBatch(3)
    @Test fun wideCoefficientsBatch04() = wideBatch(4)
    @Test fun wideCoefficientsBatch05() = wideBatch(5)
    @Test fun wideCoefficientsBatch06() = wideBatch(6)
    @Test fun wideCoefficientsBatch07() = wideBatch(7)
    @Test fun wideCoefficientsBatch08() = wideBatch(8)
    @Test fun wideCoefficientsBatch09() = wideBatch(9)

    private fun wideBatch(batch: Int) {
        val cursor = OracleCursor(139239 + batch * 6800)
        val verify = cursor::verify
        val random = kotlin.random.Random(20261015)
        val wideScales = listOf(Int.MIN_VALUE, Int.MIN_VALUE + 1, Int.MIN_VALUE + 2, Int.MIN_VALUE + 16,
            Int.MIN_VALUE + 32, Int.MIN_VALUE / 2, -10, 0, 10, Int.MAX_VALUE / 2,
            Int.MAX_VALUE - 32, Int.MAX_VALUE - 16, Int.MAX_VALUE - 2, Int.MAX_VALUE - 1, Int.MAX_VALUE)
        fun coefficient(): BigInteger {
            val value = if (random.nextBoolean()) {
                BigInteger.TEN.pow(listOf(1, 16, 17, 18, 19, 31, 32, 63, 64, 100)[random.nextInt(10)])
                    .add(BigInteger.valueOf(random.nextLong(-1, 2)))
            } else BigInteger(random.nextBytes(listOf(1, 4, 8, 16, 32, 64)[random.nextInt(6)]))
            return if (random.nextBoolean()) value.negate() else value
        }
        repeat((batch + 1) * 200) { index ->
            val a = BigDecimal(coefficient(), wideScales[random.nextInt(wideScales.size)])
            val b = BigDecimal(coefficient(), wideScales[random.nextInt(wideScales.size)])
            val selected = index / 200 == batch
            if (selected) {
                verify("parse $a") { BigDecimal(a.toString()) }
                cursor.verifyDivision("exactDivide $a,$b", a, b)
            }
            for (p in listOf(1, 2, 7, 17, 18, 19, 34, 120)) {
                // Consume RNG for earlier batches too, but execute each numerical case once.
                val context = MathContext(p, RoundingMode.entries[random.nextInt(8)])
                if (selected) {
                    cursor.verifyAddition("add $a,$b,$context", a, b, context)
                    cursor.verifyDivision("divide $a,$b,$context", a, b, context)
                    verify("multiply $a,$b,$context") { a.multiply(b, context) }
                    cursor.verifySqrt("sqrt $a,$context", a, context)
                }
            }
        }
        cursor.finish(139239 + (batch + 1) * 6800, requireEnd = batch == 9)
    }
}
