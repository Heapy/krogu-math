# Decimal binary conversion verification

Task `local:562` fixes an unsupported intermediate denominator for supported
coefficients and scales. The independent algorithm and full-domain argument are
in [DECIMAL_BINARY_CONVERSION.md](../DECIMAL_BINARY_CONVERSION.md). The common
implementation extracts a bounded numerical prefix and exact sticky information;
it never formats the entire coefficient before reducing it.

## Executed checks

The isolated implementation was tested with Gradle 9.8.1, Kotlin 2.4.21 and
Liberica 25.0.4+9-LTS. All selections included the new prefix and conversion tests
and the retained portable, canonical, floating, scale, context and division tests.
Every listed suite was present in the actual XML, with no failures, errors or
skipped tests:

| Target | XML suites | Test methods |
| --- | ---: | ---: |
| JVM | 22 | 111 |
| JS / Node | 14 | 98 |
| Wasm / Node | 14 | 98 |
| macOS ARM64 | 14 | 98 |

The common tests include 419 independently expected prefix cases, 248 signed
cohort/midpoint conversions, 10 zero checks and 16 truthful subclass checks.
JVM also checks 248 conversions against the pinned JDK and separately records
16 subclass numerical and callback observations. The counts in the table are
test methods, not the individual checks within their loops.

The packaged implementation passed 40 additional independent raw-bit checks.
The audit verified that the arithmetic classes loaded from its own JAR, whose
SHA-256 was
`046b24f4888f94802d530114cc1837dbfb868c00c64c50720f4b6e4a07398033`.
The executed production source hashes were:

```text
1587d2351f85399bb3d3dbd505fffd812cb7e8b8aedab4f371d4c27520334f2f  BigDecimal.kt
73c52b66a263c51ee57461acc9fc38c99950a43e27ad6a490713785d5e4d435b  BigInteger.kt
```

Parent integration copied these sources, the three tests, proof and bounded
certificate byte-for-byte. The frozen fixtures, generators and existing test
assertions were unchanged.

## Initial failures retained

The first JVM attempt did not compile because the new JDK subclass test called
abstract `super.toByte()` and `super.toShort()`. Narrowing `toInt()` corrected
those two test methods. The second attempt ran all 111 tests and found one new
assertion that incorrectly required identical internal callback traces. All
16 numerical results agreed. The final test asserts the independently recorded
traces separately and retains both live JDK and independent expected-bit checks.
Production code did not change during these corrections. The complete final
JVM selection then passed.

Historical commands, raw XML, initial failures, source audits, reviews and JAR
audit remain in the isolated task handoff at
`/private/tmp/krogu-math-task-562/diagnostics/decimal-binary-cancellation/`.
The worktree-specific freeze script remains there because it deliberately
requires its original base and inventories that isolated checkout.

## Limits

The mathematical certificate checks the range witness symbolically; no giant
coefficient or giant decimal string was allocated. Its proof assumes the own
integer arithmetic kernels are exact and does not establish a machine memory
or runtime guarantee. These focused results do not establish exhaustive
compatibility, complete the broader conversion/range audits, or constitute an
official JCK/TCK pass. Publication and supported-platform CI are separate gates.
