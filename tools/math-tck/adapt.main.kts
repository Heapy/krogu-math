#!/usr/bin/env kotlinr
// SPDX-License-Identifier: GPL-2.0-only
// Source-specific adaptation rules for the pinned GPL-2.0-only OpenJDK tests.
// Generated files retain their original upstream notices. No assertions deleted.
// Original source notices for the four adapted files:
// Copyright (c) 1998, Oracle and/or its affiliates. All rights reserved.
// Copyright (c) 2003, 2017, Oracle and/or its affiliates. All rights reserved.
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
    val changes = mutableListOf("Redirect math imports; retain original assertions, directives and notices")
    val skips = mutableListOf<String>()
    when (path.substringAfter("test/jdk/java/math/")) {
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
