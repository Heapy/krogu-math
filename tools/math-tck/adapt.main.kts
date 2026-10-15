#!/usr/bin/env kotlinr
// SPDX-License-Identifier: GPL-2.0-only
// Source-specific adaptation rules for the pinned GPL-2.0-only OpenJDK tests.
// Generated files retain their original upstream notices. Assertion changes and lost coverage are explicit below.
// Original source notices for the six adapted files:
// Copyright (c) 1998, Oracle and/or its affiliates. All rights reserved.
// Copyright (c) 1999, 2018, Oracle and/or its affiliates. All rights reserved.
// Copyright (c) 2003, 2017, Oracle and/or its affiliates. All rights reserved.
// Copyright (c) 2014, 2022, Oracle and/or its affiliates. All rights reserved.
// Copyright (c) 2022, 2024, Oracle and/or its affiliates. All rights reserved.
// Copyright (c) 2023, Oracle and/or its affiliates. All rights reserved.

data class MathAdaptation(val source: String, val changes: List<String>, val companionSkips: List<String>)

fun adaptMathChildOrchestration(source: String): String {
    val body = Regex("(?s)(      public static void main\\(String args\\[\\]\\) throws Exception \\{)(.*?)(\\n     \\}\\n\\n    public static class ModTester)")
    val match = requireNotNull(body.find(source)) { "Pinned ModPowPowersof2 parent scaffold changed" }
    check(body.findAll(source).count() == 1)
    val replacement = match.groupValues[1] + "\n          mathharness.ChildJvm.run(\"ModPowPowersof2\$ModTester\");\n" + match.groupValues[3]
    val adapted = source.replaceRange(match.range, replacement)
    check(adapted.replace(replacement, match.value) == source) { "Changed nested numerical test body" }
    return adapted
}

fun adaptMathTest(path: String, original: String): MathAdaptation {
    var source = Regex("(?m)^(import\\s+(?:static\\s+)?)java\\.math\\.").replace(original) {
        it.groupValues[1] + "io.heapy.krogu.math."
    }
    check(source.replace("io.heapy.krogu.math.", "java.math.") == original)
    val importOnly = source
    val changes = mutableListOf("Redirect math imports; retain original source, directives and notices except explicitly listed adaptations")
    val skips = mutableListOf<String>()
    when (path.substringAfter("test/jdk/java/math/")) {
        "BigDecimal/Constructor.java" -> {
            val calls = listOf(
                "charArrayConstructorNegativeOffset" to "new BigDecimal(new char[5], -1, 4, null)",
                "charArrayConstructorNegativeLength" to "new BigDecimal(new char[5], 0, -1, null)",
                "charArrayConstructorIntegerOverflow" to "new BigDecimal(new char[5], Integer.MAX_VALUE - 5,\n                6, null)",
                "charArrayConstructorIndexOutOfBounds" to "new BigDecimal(new char[5], 1, 5, null)",
            )
            val replacements = calls.map { (method, before) ->
                check(source.split(before).size == 2) { "Pinned Constructor call changed: $method" }
                val after = before.removeSuffix("null)") + "io.heapy.krogu.math.MathContext.UNLIMITED)"
                check(after !in source) { "Constructor adaptation already present: $method" }
                source = source.replace(before, after)
                skips += "$method: original port call with invalid char-array range and null MathContext no longer tests joint-invalid error precedence; only null is replaced with MathContext.UNLIMITED, retaining the range operands and NumberFormatException assertion. Original port/BigDecimal/Constructor/run-1 FAIL in frozen campaign1792066236575 remains evidence, not a pass for this original call"
                before to after
            }
            var restored = source
            for ((before, after) in replacements) {
                check(restored.split(after).size == 2) { "Constructor replacement changed: $before" }
                restored = restored.replace(after, before)
            }
            check(restored == importOnly) { "Constructor adaptation changed retained source" }
            changes += "Use MathContext.UNLIMITED instead of exactly four null contexts for invalid char-array range tests under the non-null Kotlin constructor policy; retain all original range/numeric operands, five TestNG assertions, method names, annotations and error-cause check. Original joint-invalid precedence is separate lost coverage, not an adapted pass"
        }
        "BigInteger/PrimeTest.java" -> {
            val before = """
                |    private static boolean checkHugeFails() {
                |        try {
                |            // huge odd integer
                |            BigInteger a = BigInteger.ONE.shiftLeft(500_000_000 + 1)
                |                    .setBit(0);
                |            a.isProbablePrime(1);
                |            // not expected to reach here
                |            return false;
                |        } catch (ArithmeticException e) {
                |            // this is the expected behavior
                |            return true;
                |        }
                |    }
            """.trimMargin()
            val after = before.replace(
                "            a.isProbablePrime(1);\n            // not expected to reach here\n            return false;",
                "            // A normal result must identify this multiple of 3 as composite.\n            return !a.isProbablePrime(1);",
            )
            check(source.split(before).size == 2) { "Pinned PrimeTest huge-range method changed" }
            check(after != before && after !in source) { "PrimeTest range adaptation already present or unchanged" }
            source = source.replace(before, after)
            check(source.split(after).size == 2) { "PrimeTest range replacement changed" }
            check(source.replace(after, before) == importOnly) { "PrimeTest range adaptation changed retained source" }
            changes += "Port-only range policy: retain huge operand, certainty 1, main call and original ArithmeticException catch; require false on normal return instead of requiring the range exception. Normal true fails; other exceptions remain uncaught"
            skips += "checkHugeFails: original mandatory-cutoff expectation above the public minimum prime-domain guarantee is not executed on the port; original frozen campaign1792044478663 PrimeTest FAIL remains FAIL, not an adapted pass. A normal false result is newly checked, not historically observed; original host-baseline behavior is retained separately"
        }
        "BigInteger/ModPowPowersof2.java" -> {
            source = adaptMathChildOrchestration(source)
            changes += "Replace parent child-process scaffold only; preserve nested ModTester math body and literal success; prove child exit and binding in both phases"
        }
        "BigInteger/ModPow65537.java" -> {
            val phase = Regex("(?s)(        // SunRsaSign uses BigInteger internally\\n.*?)(        // basic known answer test)")
            val match = requireNotNull(phase.find(source)) { "Pinned RSA phase changed" }
            check(phase.findAll(source).count() == 1)
            var host = match.groupValues[1]
            for (exponent in listOf(65537, 65539, 3)) {
                val expression = "new RSAKeyGenParameterSpec(512, BigInteger.valueOf($exponent))"
                check(host.split(expression).size == 2) { "Pinned exponent call changed: $exponent" }
                host = host.replace(expression, "new RSAKeyGenParameterSpec(512, mathharness.HostBridge.rsaExponent($exponent))")
            }
            source = source.replaceRange(match.range,
                "        if (Boolean.getBoolean(\"math.harness.hostProvider\")) hostProviderPhase();\n\n" + match.groupValues[2])
            val end = source.lastIndexOf('}')
            val helper = "    private static void hostProviderPhase() throws Exception {\n" + host + "    }\n\n"
            source = source.substring(0, end) + helper + source.substring(end)
            check(source.replace(helper, "").replace(
                "        if (Boolean.getBoolean(\"math.harness.hostProvider\")) hostProviderPhase();\n\n",
                match.groupValues[1],
            ) == importOnly) { "RSA adaptation changed retained source" }
            changes += "Move RSA statements intact into optional hostProviderPhase; bridge only the three provider exponent arguments"
            skips += "SunRsaSign key generation/signing phase: host-provider integration; all three modPow known-answer assertions retained"
        }
        "BigDecimal/DoubleFloatValueTests.java" -> {
            val expression = "Float16.valueOf(bv);"
            check(source.split(expression).size == 2) { "Pinned Float16 conversion call changed" }
            source = source.replace(expression, "Float16.valueOf(mathharness.HostBridge.decimal(bv));")
            check(source.replace("Float16.valueOf(mathharness.HostBridge.decimal(bv));", expression) == importOnly)
            changes += "Bridge only Float16 input for compilation; port invocation reflects the ten original double/float test methods"
            skips += listOf("testFloat16ValueNearMinValue", "testFloat16ValueNearMinNormal", "testFloat16ValueNearMaxValue", "testFloat16ValueRandom", "testFloat16ValueExtremes")
                .map { "$it: incubator Float16 host-decimal conversion integration" }
        }
        "BigInteger/ByteArrayConstructorTest.java" -> {
            val expression = "import io.heapy.krogu.math.Accessor;"
            check(source.split(expression).size == 2)
            source = source.replace(expression, "import mathharness.ArrayAccessor;").replace("Accessor.mag(bi)", "ArrayAccessor.mag(bi)")
            check(source.replace("import mathharness.ArrayAccessor;", expression).replace("ArrayAccessor.mag(bi)", "Accessor.mag(bi)") == importOnly)
            changes += "Replace private JDK accessor with an explicit big-endian view of krogu words; retain both original racy tests, duration, threads and assertTrue"
            skips += "JDK-specific mag storage invariant replaced by krogu normalized-word invariant; racy public constructor execution retained"
        }
    }
    // Upstream copyright and GPL notice are never rewritten.
    check(source.startsWith(original.substringBefore("*/") + "*/"))
    return MathAdaptation(source, changes, skips)
}

val floatDoubleMethods = listOf(
    "testDoubleValueNearMinValue", "testDoubleValueNearMinNormal", "testDoubleValueNearMaxValue", "testDoubleValueRandom", "testDoubleValueExtremes",
    "testFloatValueNearMinValue", "testFloatValueNearMinNormal", "testFloatValueNearMaxValue", "testFloatValueRandom", "testFloatValueExtremes",
)
