#!/usr/bin/env kotlinr
// SPDX-License-Identifier: Apache-2.0
// Builds the initial per-file selection candidate from the verified inventory.
import java.io.File

val work = File("build/math-tck")
val rows = File(work, "source-lock.candidate.tsv").readLines().drop(1).filter { it.isNotBlank() }
val records = rows.map { row ->
    val path = row.substringBefore('\t')
    val short = path.removePrefix("test/jdk/java/math/")
    val text = File(work, "upstream/$path").readText()
    var disposition = "selected"
    var resource = "standard"
    var adaptation = "imports-only"
    var reason = "Public test; retain assertions and input domain"
    var lost = "none planned; execution pending"
    when (short) {
        "BigInteger/MutableBigIntegerShiftTests.java" -> {
            disposition = "excluded"; adaptation = "none"
            reason = "Calls package-private MutableBigInteger through patched java.base wrapper"
            lost = "Internal limb shifting and mutation invariants; public shift tests remain selected"
        }
        "BigInteger/java.base/java/math/MutableBigIntegerBox.java" -> {
            disposition = "excluded"; adaptation = "none"
            reason = "Test-only wrapper for package-private MutableBigInteger"
            lost = "Support for excluded internal shift assertions"
        }
        "BigInteger/ByteArrayConstructorTest.java" -> {
            disposition = "adapted"; adaptation = "own-representation-accessor"
            reason = "Retain both original racy constructor tests and assertTrue via a test-only leading-word view of krogu storage"
            lost = "JDK mag layout minimality becomes krogu words normalization observation; original public constructor race and thread/duration coverage retained"
        }
        "BigInteger/java.base/java/math/Accessor.java" -> {
            disposition = "support"; adaptation = "baseline-patch-module"; resource = "support"
            reason = "Original private JDK accessor used only for unchanged baseline; port uses independently authored ArrayAccessor"
            lost = "No port dependency on JDK mag storage; representation correspondence is explicitly adapted"
        }
        "BigInteger/ModPow65537.java" -> {
            disposition = "adapted"; adaptation = "separate-provider-phase"
            reason = "Keep all three public modPow known-answer assertions; SunRsaSign phase invokes host math"
            lost = "Provider key generation and signing are host-JDK integration observations, outside port numerical evidence"
        }
        "BigDecimal/DoubleFloatValueTests.java" -> {
            disposition = "adapted"; adaptation = "separate-float16-phase"
            reason = "Keep all double/float conversion cases; Float16.valueOf accepts host java.math.BigDecimal"
            lost = "Incubator Float16 BigDecimal integration; preserve exact Float16 call-site inventory before any patch"
        }
        "BigDecimal/SerializationTests.java", "BigInteger/SerializationTests.java" -> {
            reason = "Current-universe round trips and subclass preservation; no legacy or cross-format payloads"
            resource = "serialization"
        }
        "BigInteger/BigIntegerTest.java" -> {
            resource = "heavy"; adaptation = "imports-only-subsets"
            reason = "Two upstream run directives; run each with subset=0 and fixed seed; keep own serialization round trips"
        }
        "BigInteger/ModPowPowersof2.java" -> {
            disposition = "adapted"; adaptation = "verified-descendant-jvm"
            reason = "Retain nested ModTester body; strengthen parent crash check to exact success plus exit/completion/core proof; both phases adapt orchestration"
        }
        "BigInteger/BigIntegerParallelMultiplyTest.java" -> resource = "heavy"
        "BigInteger/PrimeTest.java" -> resource = "heavy"
        "BigInteger/ProbablePrime.java" -> resource = "heavy"
        "BigInteger/BitLengthOverflow.java", "BigInteger/DoubleValueOverflow.java" -> {
            resource = "large-memory-8g"
            reason = "No requires directive, but billion-bit inputs and swallowed OOM require explicit SKIP detection and 8g profile"
        }
        "BigInteger/LargeValueExceptions.java" -> resource = "large-memory-4g"
        "BigInteger/ExtremeShiftingTests.java" -> resource = "memory-512m"
        "BigInteger/largeMemory/DivisionOverflow.java", "BigInteger/largeMemory/StringConstructorOverflow.java" -> resource = "large-memory-8g"
        "BigInteger/largeMemory/SymmetricRangeTests.java" -> {
            resource = "large-memory-10g"
            adaptation = "imports-only-subsets"
            reason = "Force subset=0 to retain all four regimes; retain public range/overflow assertions and own round trip; classify implementation-specific probes after baseline"
        }
        "BigDecimal/ValueOfDouble.java" -> {
            resource = "standard"
            reason = "JUnit assertions require real pinned JUnit runner; no annotation stripping"
        }
        "BigInteger/HashCode.java" -> reason = "Historical BigInteger hash observations retained as compatibility evidence; not cross-universe enum/context identity"
        "test/lib/jdk/test/lib/RandomFactory.java" -> {
            disposition = "support"; adaptation = "none"; resource = "support"
            reason = "Only required external OpenJDK support; java.util PRNG and seed logging; no other support dependencies"
        }
    }
    val runner = when {
        disposition == "excluded" || disposition == "support" -> "none"
        text.contains("@run testng") -> "testng"
        text.contains("@run junit") -> "junit"
        else -> "main"
    }
    listOf(path, disposition, resource, runner, adaptation, reason, lost).joinToString("\t")
}
File(work, "manifest.candidate.tsv").writeText("path\tdisposition\tresource\trunner\tadaptation\treason\tlostCoverage\n" + records.joinToString("\n", postfix = "\n"))
println("Catalogued ${records.size} paths. No tests executed.")
