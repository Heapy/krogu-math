# Task566 JVM expectation adaptation

These four original sources were copied before editing into `original-jvm/`:

| Source | Original SHA-256 |
|---|---|
| DecimalMultiplicationSubclassTest.kt | `a5c943ec2689dfa088b0850ec0a353e71c01b7e1f8daa27d9ebf36df42b2975f` |
| JdkOracleTest.kt | `1bd75f38cb1b84b97b9361e03371599d78dc9ce01bb1e2507ed217af6ad1edb5` |
| ScaleBoundaryConformanceTest.kt | `4adae2dbb5efa52154dca7b1ec92a56854f48ea4558c367225d2c7f9ab206b37` |
| SetScaleBoundaryTest.kt | `eb79a6eeaab66287abffdd91bc116d00088af4f74cf7522b350f53d2edb17d45` |

The initial combined JVM command and its failures remain under
`/private/tmp/krogu-root528566-validation-v1/jvm-initial.log` and
`xml-jvm-initial`. This adaptation is SOURCE ONLY: these edited callers have
not yet compiled or executed. No generated fixture or production source changed.

The common `DecimalScaleRounding566Expected` helper supplies expectations only
for its independently bounded three triggers: nonzero downscale distance above
646456992, finite zero-addition with the removed preferred-distance rejection,
and nonzero finite HIGH multiplication requiring a precision reduction. Its
ordinary inputs have at most512 coefficient digits, and zero-add padding is
bounded. Null classification retains the original reference comparison. The
helper computes decimal-string arithmetic, rounding and output spelling; it
does not call production multiply, setScale, add or round for expected values.

Every classified live JVM row prints `566_JDK_OBSERVATION` with the actual full
host result or exception class/message before comparing the port against the
independent result. No host result is inferred or reconstructed. Exception
message comparison becomes exception-type comparison only within a classified
domain; outside it the old message/type/value comparisons remain as before.
The complete original loops, modes, signs and operand orders remain active.

The subclass multiplication matrix deliberately overrides signum/precision.
Its ordinary model receiver is reconstructed from the original constructor
coefficient literal and scale, never those reported getters. This is justified
only by the task566 finite HIGH path: it reads private stored coefficients and
scales, creates a base product, and rounds that base product. This does not
assert arbitrary poisoned callback parity. The actual original receiver still
executes every operation; expected/actual explicit dispatch traces must match.
Successful classified results must still be distinct from the receiver and
have the base BigDecimal runtime class. Exact, unlimited, zero and other
unclassified rows keep their original oracle/identity/class/trace comparisons.

Existing division, sqrt, task554 addition, null and validation controls are
untouched. Root owns later compilation, execution and complete observation
capture; no passing result follows from this source adaptation.
