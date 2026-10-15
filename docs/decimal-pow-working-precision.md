# Decimal pow working-precision overflow

SPDX-License-Identifier: Apache-2.0

This root `local:526` classification/repair slice addresses a specific
`local:542` gap and was published in commit
`d660475bc14b26ca365b8880d1d65bfe52d6db4f`. Compilation and execution completed;
the source-phase and subsequent execution records are distinguished below.
This does not complete either task or the remaining pow/sqrt domain audit.

## Required value and reference observation

The public [JDK25 pow contract](https://docs.oracle.com/en/java/javase/25/docs/api/java.base/java/math/BigDecimal.html#pow(int,java.math.MathContext))
defines repeated squaring with a working precision enlarged by the exponent's
decimal length plus one, followed by destination rounding. Its documented
validation rejects out-of-range exponents, negative exponents with unlimited
precision, or an exponent whose decimal length exceeds a positive destination
precision. None excludes `ONE.pow(1, MathContext(Int.MAX_VALUE, HALF_EVEN))`.
Its exact coefficient and scale are independently `1@0`; there are no discarded
digits, scale overflow or oversized numerical values.

The previous own code instead added destination precision, exponent length and
one in `Int`. Wraparound made the internal MathContext constructor reject a
negative precision. A pinned-JDK failure was copied by the old boundary test,
but matching that failure does not supply a mathematical reason to reject ONE.
This finding concerns the internal context, not an invalid caller MathContext.

The original boundary-test source is retained byte-for-byte in
[`diagnostics/decimal-pow-working-precision`](../diagnostics/decimal-pow-working-precision/README.md).
The revised JVM test keeps all128 original `(1@0, precision, exponent, mode)`
keys and prints every reference outcome, including success strings and exact
exception class/message, to its test output. It separately requires own `1@0`.
No observation of result identity or callback ordering is invented. At the
source-phase review the new capture was UNRUN. The first JVM execution and its
remaining gates are recorded below.

## Range argument

Let `p>0` be the caller precision and `e` the decimal length of a valid nonzero
exponent's magnitude. Then `1<=e<=9`. Compute mathematical working precision
`w=p.toLong()+e+1`, then construct the internal context with
`min(w, Int.MAX_VALUE)`. Validation, precision-zero delegation and exponent-zero
return remain in their original order.

For `w<=Int.MAX_VALUE`, the calculation is unchanged. Otherwise
`p>=Int.MAX_VALUE-9` and `w<=Int.MAX_VALUE+10`. The retained own
[coefficient range proof](../DECIMAL_BINARY_CONVERSION.md) bounds every supported
stored coefficient by646456993decimal digits, strictly below both `p` and the
capped working precision. Therefore rounding any supported product with either
working precision keeps its entire coefficient and scale; the final destination
rounding does too. Multiplication still validates its actual product and scale;
this change does not manufacture support for an oversized intermediate.

Reciprocal division needs a separate argument. For truthful divisor digit count
`d>=1`, `ONE.divide(result, work)` has a normalization shift at least
`work.precision-1`. With cappedMAX, and with every larger ideal precision, that
exceeds the existing ten-power limit646456992. The truthful path therefore
enters `divideWideContext`. That method checks for a terminating exact cohort
before attempting precision-sized padding. Any supported exact coefficient has
fewer digits than either working precision, so both admit the same exact
coefficient and scale. A nonterminating fallback still needs an unsupported
working power. Capping does not create that existing limitation or claim to fix
it. For a zero divisor, the existing zero-division check still applies first.

This is conditional numerical equivalence where the existing stored arithmetic
and metadata operations complete. It is not a proof about arbitrary dishonest
precision callbacks, callback replacements, unsupported intermediate products,
or engine string-capacity failures. No public method becomes final; explicit
caller overrides, caller-owned `super`, and directly thrown exception identity
remain available. Internal working-context observation is not promised to
reproduce the former constructor failure.

## Prepared controls and remaining gates

The six new common methods independently cover both unit signs, positive and
negative exponents through999999999, all11 destination precisions fromMAX-10
throughMAX, all eight rounding modes, monomial scales, terminating reciprocals,
cohort trailing zeros, zero scale saturation, exponent validation and explicit
caller dispatch/super/exception identity. Successful assertions check coefficient
and scale. No giant coefficient or power-sized output is constructed.

The old JVM accumulator-rounding method remains unchanged. Its separate
boundary method preserves all128 reference inputs while separating diagnostic
constructor failures from own required values. The original source SHA is
`017338c7d11eba085c936692d67cadf49ef202746531dc49bdd70289003dc938`.

Independent read-only review of the mathematical proposal found no numerical
counterexample within the stated domain. Final frozen-source review, fresh
JVM/JS/Wasm/macOS ARM64 compilation and tests, all128 captured reference rows,
retained pow/oracle regressions and integrated all-six CI were required.
These gates were all UNRUN at source-phase acceptance. Under parent1919, the
first full JVM command compiled the source and executed360 methods in2m55s:
359 passed and one retained portable-oracle method failed. All six new common
methods and both JVM power-boundary methods passed. Raw XML, including every
reference outcome, and the initial failed command/log are preserved under
`/private/tmp/krogu-root526565525-validation-v1/xml-jvm-initial`.

The portable fixture contains the same128 plain-ONE boundary keys. Its first
overflow row still required the pinned JDK constructor error. The narrow
portable classification retains the complete generated fixture unchanged,
asserts each original64 error/64 value diagnostic, requires1 independently,
and checks all128 exact keys once. No other input, index, callback assertion
or outcome changes. The pre-classification portable test is archived beside
the original JVM source in `diagnostics/decimal-pow-working-precision/`.
Independent review accepted the exact correction; full JVM retry and all three
other targets subsequently passed. The combined package gates total1059
successful method executions, with no failures/errors/skips/omissions. See the
[executed integration report](math-port-integration-verification.md) for exact
counts, initial failure, fresh JAR and the CI gate that remained at that milestone.
The published successor `bbdd2f4a7c3d3729f2597205b794f2853cc28e67`, retaining
this repair and its tests, subsequently passed all six
[Compatibility CI jobs](https://github.com/Heapy/krogu-math/actions/runs/38672668098),
including execution of 250 common methods on Linux ARM64. This closes the
internal working-precision overflow gap for supported unit results; the broader
`local:542` proof and coverage obligations remain open.

The metadata-only
[`audit-decimal-pow-working-precision.main.kts`](../scripts/audit-decimal-pow-working-precision.main.kts)
checked the actual JVM XML for both retained methods, zero failures/errors/
skips and all128 original reference keys exactly once, then preserved the complete
outcome strings in a new TSV. Invoke it from the repository root with the XML,
fresh own JAR and a new output path, under256MiB with a fresh script cache.
It verified original/revised test-source hashes and recorded the XML/JAR hashes;
JAR provenance and the test JVM's pinned identity still require the separate
execution ledger. The auditor does not execute a numerical API or establish
that a supplied JAR was built from those sources. It compiled/executed under256MiB
and passed:128 unique keys,64 reference values and64 diagnostic errors. The
complete original outcome strings are retained in
[`reference-outcomes.tsv`](../diagnostics/decimal-pow-working-precision/reference-outcomes.tsv).
