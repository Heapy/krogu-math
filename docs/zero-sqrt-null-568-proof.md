# Zero sqrt null contract: task 568 source proof

**SOURCE ONLY — UNCOMPILED / UNRUN. Changes are not committed.**

Base: `bc1d51117a2125acc991607213b6c0bca4c2f8f6`, branch
`task/568-zero-sqrt-null`, native session `990acd40`, linked task `local:568`.
The parent retains the sole runtime window. No numerical helper, compiler,
script, probe, test, generator, build, artifact or harness was invoked here.
No fresh JDK defect verification or integration pass is claimed.

## Public contract and independently required outcome

The [Java SE 25 BigDecimal class contract](https://docs.oracle.com/en/java/javase/25/docs/api/java.base/java/math/BigDecimal.html)
requires NPE for null object inputs. The
[sqrt specification](https://docs.oracle.com/en/java/javase/25/docs/api/java.base/java/math/BigDecimal.html#sqrt(java.math.MathContext))
gives zero a preferred scale of `this.scale()/2`; its special case permits every
MathContext instance and supplies no null exception to the class rule.
Therefore ordinary `BigDecimal("0.00").sqrt(null)` independently requires
`NullPointerException`. Expected exception type does not come from a JDK result,
an exception message, a callback trace, or another numerical implementation.
Only public API documentation and our source/frozen observations were read.

The existing frozen oracle records the seven sqrt/null inputs in this order:

| Exact text | Frozen observation | Task 568 policy |
| --- | --- | --- |
| `-10.00` | `!ArithmeticException` | Retain comparison |
| `-1.01` | `!ArithmeticException` | Retain comparison |
| `0` | `0` | Retain raw observation; require `!NullPointerException` |
| `0.00` | `0.0` | Retain raw observation; require `!NullPointerException` |
| `1` | `!NullPointerException` | Retain comparison |
| `1.00` | `!NullPointerException` | Retain comparison |
| `10.10` | `!NullPointerException` | Retain comparison |

These are historical observations, not a newly executed probe. The old common
563 assertion also records a source expectation: coefficient-zero at scale
`Int.MIN_VALUE`, null context, returning scale `-1073741824`. Its replacement
requires NPE and checks that same preferred scale separately with nonnull
`MathContext.UNLIMITED`. It is not an independently fresh host observation.

## Minimal source argument

`BigDecimal.sqrt` still dispatches `signum()` and retains its negative check.
The sole production change moves the existing `mc ?: throw NullPointerException()`
ahead of the zero return. No arithmetic expression, field, method signature,
visibility, virtual dispatch site, or result representation changes.

- Nonnegative sign and null context: NPE before the zero return or arithmetic.
  Truthful ordinary and subclass zero are single-invalid inputs covered by the
  null contract. Positive/null already reached this same check.
- Any nonnull context: the same context reference, stored preferred scale,
  zero construction and subsequent arithmetic remain. Integer scale division
  truncates toward zero, including negative odd and extreme representable scales.
- Negative sign: the existing ArithmeticException still occurs first. A throwing
  sign getter also propagates as before. Neither competing-invalid case is used
  to establish a universal error order or justify the zero/null repair.
- A public caller override remains ordinary virtual dispatch. If it returns a
  marker or throws a sentinel without calling super, identity is preserved even
  with null context. A caller-owned `super.sqrt` enters the repaired base method.
- A nonzero stored coefficient whose override falsely reports zero also changes
  from fabricated zero to NPE with null context. That sign override violates its
  own contract. This collateral source observation is separate from truthful
  zero conformance; no existing null-context consumer was found for it and no
  general exception waiver is added.

## Affected consumers and exact selection

1. `NullPrecedenceOracleTest`: selection uses only full input labels
   `sqrt/null context 0` and `sqrt/null context 0.00`, never observed output.
   It checks each raw frozen value, rejects duplicate selected keys, requires
   exact selected-set equality and two selected rows, and retains all 3460
   other equality comparisons, total 3462 and exhausted-cursor assertion.
2. `NullArgumentConformanceTest`: selection requires exact declaring class
   `java.math.BigDecimal`, method `sqrt`, one `java.math.MathContext` parameter,
   arguments `[null]`, exact ordinary receiver class, and representation equality
   to `JD.ZERO` or `JD("0.00")`. It retains and prints both live host outcomes
   and own outcomes, then independently asserts NPE. Duplicate rejection and
   exact `{0, 0.00}` key-set/two-row checks bound the exception. All constructors,
   reflection traversal, invocations and other comparison logic remain intact.
3. `DecimalExactRoot563Test.genuineErrorsAndPublicEntryRemain`: only its zero/null
   assertion changes, plus the independent valid-context preferred-scale check.
   Adjacent positive/null NPE and negative/null ArithmeticException stay intact.

Read-only call-site inventory found nonnull contexts in `DecimalPowerSubclassTest`
(sqrtSign, precision and normalization), `DecimalNormalizedRootTest`, and the
portable H/A/N and D arithmetic evaluator paths. The 563 callback controls also
construct contexts. Those paths and every frozen evaluator remain untouched.
Only the three consumers above required adaptation; frozen results and the
generator remain byte-identical.

## Prepared bounded regression coverage (not executed)

`ZeroSqrtNull568Test` has four common methods. Null controls cover the exact
String witnesses plus ordinary/truthful/caller-super zero at 11 literal scale
cohorts. Valid contexts cover these same three origins at all eight modes and
precisions `0, 1, 7, Int.MAX_VALUE`, expecting coefficient zero and independently
listed preferred scales: 1056 result pairs. Positive finite/inexact radicands
with null, retained negative/null staging, nonnull positive and negative controls,
and explicit override result/exception identity include null context. No internal
trace/order assertion or mirrored numerical algorithm is introduced.

`ZeroSqrtNull568DiagnosticTest` has two JVM methods, gated by exact vendor
`BellSoft` and runtime `25.0.4+9-LTS` (Liberica 25.0.4+9). Six exact null keys
combine scales `0, 2` with `ordinary`, `truthful`, and `callerSuper`. Every row
uses the exact ordinary String inputs `"0"` / `"0.00"` or subclass coefficient
zero and scale `0` / `2`, respectively. Every row
prints a raw host coefficient/scale or exception and the own outcome separately
from the required NPE. Only the two ordinary host results are asserted against
existing frozen observations; no historical host result is invented for subclass
origins. Exact six-key equality and duplicate rejection bound this replay.
Another 240 valid-context pairs cover five scale cohorts, p0/p7, all modes and
three origins, checking independent zero/scale and raw public JDK equality.
This prepared replay has not run. Future host divergences must be retained.

## Original archive and source hashes

Exact originals were archived before edits in
`/private/tmp/krogu-568-original-990acd40/original.tar`.
SHA256: `82be5ec59560ede1a296e6f5e384f8ea46767039f8fb3d15c0c85b0d0656dfe9`.
The sibling `original.sha256` manifest has SHA256
`21bee1ad0d7111baca1954615b728e01daff0a39e0805f9bfb7e3161233c0922`.

| File | Original SHA256 | Prepared SHA256 |
| --- | --- | --- |
| BigDecimal.kt | 9cbaef673f966579e52f44bb9f6bc7c3b01f3ef9adae412f9d96c86c063a1446 | 9f14989a75a21662f4dfd1b61bf71c4ad0c04bcc3d9e8be4b4038103b443ab21 |
| NullPrecedenceOracleTest.kt | 29e50bfb3a835dc228c1a102b9d71c7a363d181df0f0e9c2d90a78cf41be4cf0 | 05daa7a14d3ab90a8e6af781a119f4268fdbccc4e5639d167285412f7cdfe0cc |
| NullArgumentConformanceTest.kt | 3cf6d0057bd72e065b3a2fa76b999343ee852c8078650180af1cb7a4205b1bec | 6bcc75b1ccbeed51c40540bb78afe4ac2e596663fb9b5c6d4d8f04efd61b8ca0 |
| DecimalExactRoot563Test.kt | cfb803998cb01df11e0cd76492be793c21243b62402978c9509fef86f6f2cc7f | 855a57ae7f5ea2dcaa33dc7980eae3f4943e0e9573702502e16b8143913f6e7c |
| ZeroSqrtNull568Test.kt | New | ac419512a6a811251efbb444170d5c435b44e26ce94ffb91798a952acdf11de8 |
| ZeroSqrtNull568DiagnosticTest.kt | New | b53cf2dd18f63fa642890d461625692051ac9c2ff2351f6d6e98d536ed7954eb |

Unchanged SHA256: `Jdk25NullPrecedenceResults.kt`
`a6a4d9edc0b4b02d56f9665a1a4c3646ee8a57ef6f708da2c288926d55a67da9`;
`scripts/generate-null-oracle.main.kts`
`319b5994c939abe909d5c20e71518df603f2062915e9e542a1e2c12ccd24837f`.
Final proof/handoff hashes, exact archive diff and independent review disposition
are recorded in the terminal task comment to avoid self-referential hashes.

Independent collaboration reviewer `/root/source_contract_review_568` completed
a read-only source/contract pass with no actionable findings. It checked the
production move, bounded consumer selection, regression scope, caller semantics
and official API contract. It did not compile/run code, execute numerical
coverage counts, or independently verify hashes. Its result is source review,
not a runtime or integration gate.

Parent-owned later gates remain: separately authorized bounded pinned replay,
fresh artifact, full retained JVM/JS/Wasm/macARM runs and exact-final-HEAD CI.
The task remains linked/in_progress. No Git mutations, task review/done/unlink,
mutex operations, other-worktree edits, or runtime grant waiting are part of this
source milestone.
