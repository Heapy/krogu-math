# Impossible-length plain strings: retained mismatch

Standard run `build/math-tck/results/1791999552945/results.json` records 47 host
PASS and 45 port PASS plus two port FAIL, both original ToPlainStringTests
directives. No assertion was changed or excluded. Both port invocations report
9 errors; all ordinary positive/negative formatting examples passed.

Independent minimal public input:

```kotlin
BigDecimal(BigInteger.ONE, Int.MAX_VALUE).toPlainString()
```

For this coefficient/scale the required plain text is `0.` followed by
`Int.MAX_VALUE - 1` zeros and `1`, length **2,147,483,649**. It exceeds an
Int-valued JVM String length. This conclusion uses coefficient/scale semantics,
not an implementation or a reference-generated numerical expected value.
Positive-scale ASCII text has length `scale + 2 + signLength`; negative-scale
integer text has length `coefficientDigits - scale + signLength`, computed in Long.

`probe-plain-overflow.main.kts` constructs all 14 equivalent coefficient/scale
witnesses directly, avoiding dependence on string parsing. All constructions and
scales are valid. In the same BellSoft25.0.4+9-LTS / 256MiB JVM:

- Host: all 14 throw preemptive `java.lang.OutOfMemoryError`, message
  `too large to fit in a String`.
- Port: nine throw `java.lang.ArithmeticException: String too large`; five throw
  `java.lang.OutOfMemoryError: Java heap space` after attempting allocation.

The jar is the parent-selected SHA-256
`54ec9af64675eba2a89d7cba68a1c404220cfaf3169bd1de8a31193514e31db0`.
`build/math-tck/plain-overflow-probe.log` retains each input, mathematical length,
both outcomes, runtime and hash. Reproduce:

```sh
JAVA_HOME=/Users/yoda/.sdkman/candidates/java/25.0.4-librca \
KOTLIN_MAIN_KTS_COMPILED_SCRIPTS_CACHE_DIR="$PWD/build/math-tck/script-cache" \
kotlinr -J-Xmx256m \
  -cp /Users/yoda/dev/pet/krogu-math/build/libs/krogu-math-jvm-0.1.0-SNAPSHOT.jar \
  tools/math-tck/probe-plain-overflow.main.kts
```

The [public toPlainString contract](https://docs.oracle.com/en/java/javase/25/docs/api/java.base/java/math/BigDecimal.html#toPlainString())
defines value/scale/sign formatting but does not prescribe the error subtype for
an unrepresentable String allocation. Thus this is proven exception/preflight
behavior divergence on valid decimal inputs, with an independently impossible
text length; it is not a wrong representable numerical string or a proven JDK
bug. The public OpenJDK test specifically requires preemptive OOM. Parent1790
adopted that JVM behavior as a compatibility requirement and created isolated
production task **local564**; this does not claim normative OOM wording or a JDK
defect. The two failures remain FAIL pending an explicitly granted fresh-jar rerun;
they must not become a blanket toPlainString exclusion or a test-only error bridge.
Candidate production scope is text-length preflight/error handling, plus independent
supported-target regressions; this worker has not edited production sources.
