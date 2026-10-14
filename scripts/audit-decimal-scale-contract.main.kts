#!/usr/bin/env kotlinr

// Usage: KOTLIN_MAIN_KTS_COMPILED_SCRIPTS_CACHE_DIR=/private/tmp/krogu-scale-script-cache
// kotlinr -classpath build/libs/krogu-math-jvm-0.1.0-SNAPSHOT.jar
// scripts/audit-decimal-scale-contract.main.kts [--check-port]
// Public API execution only; no numerical implementation sources are consulted.
import java.math.BigDecimal as JD
import java.math.BigInteger as JI
import java.math.MathContext as JC
import java.math.RoundingMode as JR
import io.heapy.krogu.math.BigDecimal as KD
import io.heapy.krogu.math.BigInteger as KI
import io.heapy.krogu.math.MathContext as KC
import io.heapy.krogu.math.RoundingMode as KR

require(args.all { it == "--check-port" })
val checkPort = "--check-port" in args
println("runtime=${System.getProperty("java.vendor")} ${System.getProperty("java.runtime.version")}")
fun j(action: () -> JD): String = try {
    val r = action(); "${r.unscaledValue()}@${r.scale()}"
} catch (e: ArithmeticException) { "!ArithmeticException:${e.message}" }
fun k(action: () -> KD): String = try {
    val r = action(); "${r.unscaledValue()}@${r.scale()}"
} catch (e: ArithmeticException) { "!ArithmeticException:${e.message}" }
fun verify(label: String, expected: String, reference: String, port: String) {
    println("$label expected=$expected JDK=$reference krogu=$port")
    if (checkPort) check(if (expected.startsWith("!")) port.startsWith("!ArithmeticException:") else port == expected)
}

// q = 1@MAX is within the supported scale range. q * (1@MIN) = 10 exactly.
// q < 0.5 follows directly from MAX > 1, without constructing any huge power.
val huge = JD(JI.ONE, Int.MIN_VALUE)
val quotient = JD(JI.ONE, Int.MAX_VALUE)
check(quotient.multiply(huge).compareTo(JD.TEN) == 0)
check(quotient.compareTo(JD("0.5")) < 0)
println("division witness: (1@MAX)*(1@MIN)=10; 0 < 1@MAX < 0.5")
for (sign in listOf(-1L, 1L)) for (mode in JR.entries) {
    val expected = when (mode) {
        JR.UNNECESSARY -> "!ArithmeticException"
        JR.UP -> "$sign@0"
        JR.CEILING -> "${if (sign > 0) 1 else 0}@0"
        JR.FLOOR -> "${if (sign < 0) -1 else 0}@0"
        else -> "0@0"
    }
    verify("divide ${10 * sign}@0 / 1@MIN scale=0 $mode", expected,
        j { JD.valueOf(10 * sign).divide(huge, 0, mode) },
        k { KD.valueOf(10 * sign).divide(KD(KI.ONE, Int.MIN_VALUE), 0, KR.valueOf(mode.name)) })
}

// The aligned coefficient is just 10000*10+1=100001 (six digits).
// One retained digit is 1, discarded tail 00001 is nonzero and below half.
for (sign in listOf(-1L, 1L)) {
    val left = JD(JI.valueOf(10000 * sign), Int.MAX_VALUE - 1)
    val right = JD(JI.valueOf(sign), Int.MAX_VALUE)
    val exact = JD(JI.valueOf(100001 * sign), Int.MAX_VALUE)
    check(left.add(right) == exact)
    for (mode in JR.entries) {
        val increment = mode == JR.UP || mode == JR.CEILING && sign > 0 || mode == JR.FLOOR && sign < 0
        val expected = if (mode == JR.UNNECESSARY) "!ArithmeticException"
            else "${sign * if (increment) 2 else 1}@${Int.MAX_VALUE - 5}"
        val jc = JC(1, mode)
        val independentlyRounded = j { exact.round(jc) }
        check(if (expected.startsWith("!")) independentlyRounded.startsWith("!ArithmeticException:") else independentlyRounded == expected)
        verify("add ${10000 * sign}@MAX-1 + $sign@MAX p=1 $mode", expected,
            j { left.add(right, jc) },
            k { KD(KI.valueOf(10000 * sign), Int.MAX_VALUE - 1)
                .add(KD(KI.valueOf(sign), Int.MAX_VALUE), KC(1, KR.valueOf(mode.name))) })
    }
}
// Separate range diagnostic: this requested coefficient is not a bounded witness
// for guaranteed success. 10^2147483648 > 2^(3*2147483648) exceeds the supported
// BigInteger coefficient range. Preserve the wrapped JDK output without requiring it.
verify("range diagnostic 1@MIN / 1@0 scale=0 HALF_EVEN", "!ArithmeticException",
    j { JD(JI.ONE, Int.MIN_VALUE).divide(JD.ONE, 0, JR.HALF_EVEN) },
    k { KD(KI.ONE, Int.MIN_VALUE).divide(KD.ONE, 0, KR.HALF_EVEN) })
// The full-suite consumers also contain false range errors for zero and tiny
// results. Their existing frozen JDK observations must not force these errors.
for (mode in JR.entries) {
    verify("zero diagnostic 0@MIN / 1@MIN targetMAX $mode", "0@${Int.MAX_VALUE}",
        j { JD(JI.ZERO, Int.MIN_VALUE).divide(JD(JI.ONE, Int.MIN_VALUE), Int.MAX_VALUE, mode) },
        k { KD(KI.ZERO, Int.MIN_VALUE).divide(KD(KI.ONE, Int.MIN_VALUE), Int.MAX_VALUE, KR.valueOf(mode.name)) })
    for (sign in listOf(-1L, 0L, 1L)) {
        val expected = when {
            sign == 0L -> "0@0"
            mode == JR.UNNECESSARY -> "!ArithmeticException"
            mode == JR.UP || mode == JR.CEILING && sign > 0 || mode == JR.FLOOR && sign < 0 -> "$sign@0"
            else -> "0@0"
        }
        verify("zero/tiny range diagnostic $sign@MAX / ONE target0 $mode", expected,
            j { JD(JI.valueOf(sign), Int.MAX_VALUE).divide(JD.ONE, 0, mode) },
            k { KD(KI.valueOf(sign), Int.MAX_VALUE).divide(KD.ONE, 0, KR.valueOf(mode.name)) })
    }
}
println("Independent witnesses passed; port checks=${if (checkPort) "required and passed" else "diagnostic only"}.")
