# DecimalKey536 validation and diagnostic cost observation

Base source: `b3a70fc5427f3771cfea07903c3b89155cff8b39` plus this task's
additive files. `source-sha256.txt` binds the implementation and test harness.
No foreign numerical implementation sources were consulted.

Environment: macOS Darwin 25.6.0 / arm64, BellSoft Liberica JDK 25.0.4+9-LTS,
OpenJDK 64-Bit Server VM, Kotlin 2.4.21, Gradle 9.8.1. Exact VM/architecture also
appear in the retained test XML. Run on 2026-10-16 UTC.

```shell
JAVA_HOME=/Users/yoda/.sdkman/candidates/java/25.0.4-librca \
KROGU_MEASURE_DECIMAL_KEYS=1 ./gradlew jvmTest --tests '*DecimalKey536*' --console=plain

JAVA_HOME=/Users/yoda/.sdkman/candidates/java/25.0.4-librca ./gradlew --no-daemon \
  jsNodeTest --tests '*DecimalKey536Test*' \
  wasmJsNodeTest --tests '*DecimalKey536Test*' \
  macosArm64Test --tests '*DecimalKey536Test*' --console=plain
```

The JVM run passed five common tests and two JVM tests. The latter verify
HashMap/TreeMap and HashSet/TreeSet agreement and record timing observations.
`jvm-test.log`, `jvm-common.xml`, and `jvm-measurements.xml` retain the run.
Cross-target JS, Wasm and macOS arm64 each passed all five common tests.
Results are recorded in `cross-target-test.log`, `js.xml`, `wasm.xml`, and
`native.xml`. Cross-target execution used a separate unsandboxed Gradle process
because the initial sandbox daemon could not write the Kotlin Wasm tooling lock
under ~/.kotlin. No numerical test failure occurred.

Raw JVM observations, in ns/op:

| Input | Create + first hash + volatile publish | Cached hash + checksum | Publish existing key | CompareTo self + checksum | Strip + hash + checksum |
| --- | ---: | ---: | ---: | ---: | ---: |
| 12345.6700 | 655.975 | 73.729292 | 78.3125 | 133.22915 | 315.9604 |
| zero at Int.MAX_VALUE scale | 61.7 | 43.664958 | 44.6771 | 235.275 | 99.73125 |
| coefficient 1000 at Int.MIN_VALUE scale | 223.95415 | 77.33425 | 6.53335 | 362.97295 | unsupported |
| 4096-digit coefficient, four trailing zeros | 5628360.415 | 10.90775 | 42.915 | 2759.375 | 15513.955 |

Creation uses 20,000 measured iterations (200 for the 4096-digit case), preceded
by 20% as many warmup constructions. Each new key escapes through a volatile
field. Cached hashing uses 200,000 warmup and 1,000,000 measured iterations over
64 prebuilt keys with a rolling checksum; checksum and publication state are
printed. Other columns are contextual baselines, not equivalent functionality.
The strip baseline is intentionally omitted where canonical Int scale overflows.

These are single-process diagnostic loop timings, including checksum/publication
and loop overhead, with limited warmup and no forks or uncertainty estimate.
JIT compilation order and runtime noise affect the numbers. They do not measure
allocation bytes, retained heap, isolated hash invocation latency, or cross-platform
performance, and establish no speedup. The digit conversion dominates this large
coefficient's construction; cached hashing avoids that repeated conversion.
The structural O(d) retained digit storage and O(1) cached hash contract follow
from the implementation, rather than these timings.
