package bench

import io.heapy.krogu.math.BigDecimal
import io.heapy.krogu.math.BigInteger
import io.heapy.krogu.math.MathContext
import io.heapy.krogu.math.RoundingMode

data class Workload(val name: String, val run: (Int) -> Any)

// All parsing/preparation is outside measurement except the explicitly named parse workload.
// 16 deterministic operands rotate by index; no randomness or platform-dependent text generation.
fun kroguWorkloads(): List<Workload> = buildList {
    for (digits in listOf(9, 77, 309, 1234)) {
        val text = List(16) { i -> "8" + "1234567890".repeat((digits + 9) / 10).take(digits - 2) + (i % 9 + 1) }
        val a = text.map(::BigInteger)
        val b = text.map { BigInteger(it.take((digits / 2).coerceAtLeast(2))) }
        val prefix = "integer/$digits"
        add(Workload("$prefix/parse") { BigInteger(text[it and 15]) })
        add(Workload("$prefix/format") { a[it and 15].toString() })
        add(Workload("$prefix/compare") { a[it and 15].compareTo(a[(it + 1) and 15]) })
        add(Workload("$prefix/multiply") { a[it and 15].multiply(b[it and 15]) })
        add(Workload("$prefix/square") { a[it and 15].multiply(a[it and 15]) })
        add(Workload("$prefix/divide") { a[it and 15].divide(b[it and 15]) })
    }
    for ((label, base) in listOf(
        "small" to "123.45", "wide" to "123456789012345678901234567890.12345678901234567890",
        "negative-scale" to "12345678901234567890E+80", "large-scale" to "12345678901234567890E-80"
    )) {
        val text = List(16) { i -> base.replaceFirst("123", (123 + i).toString()) }
        val a = text.map(::BigDecimal)
        val b = List(16) { BigDecimal("${it + 11}.37") }
        val mc = MathContext(34, RoundingMode.HALF_EVEN)
        val rounding = MathContext(7, RoundingMode.HALF_EVEN)
        val prefix = "decimal/$label"
        add(Workload("$prefix/parse") { BigDecimal(text[it and 15]) })
        add(Workload("$prefix/format") { a[it and 15].toString() })
        add(Workload("$prefix/precision") { a[it and 15].precision() })
        add(Workload("$prefix/compare") { a[it and 15].compareTo(a[(it + 1) and 15]) })
        add(Workload("$prefix/multiply") { a[it and 15].multiply(b[it and 15], mc) })
        add(Workload("$prefix/square") { a[it and 15].multiply(a[it and 15], mc) })
        add(Workload("$prefix/divide") { a[it and 15].divide(b[it and 15], mc) })
        add(Workload("$prefix/round") { a[it and 15].round(rounding) })
        add(Workload("$prefix/setScale") { a[it and 15].setScale(4, RoundingMode.HALF_EVEN) })
        add(Workload("$prefix/chain") { a[it and 15].multiply(b[it and 15], mc).add(b[it and 15], mc).divide(b[it and 15], mc) })
    }
}
