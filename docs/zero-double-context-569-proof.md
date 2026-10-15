# Double signed-zero context: task 569 source proof

**SOURCE ONLY — all changed code and tests UNCOMPILED / UNRUN. Changes are not committed.**

Worktree `/private/tmp/krogu-math-task-569`, branch `task/569-zero-double-context`,
unchanged HEAD `5fb00ee449570effbd27556ad4c248a26faf787c`.
Native worker session `9543a8cb`, linked task `local:569`, parent task `local:526`
and session `af5f5ab4`. Parent alone owns the numerical/compiler/helper window.
No runtime grant is requested or awaited. No compiler, numerical helper, script,
test, probe, build, JAR, harness, generator or benchmark was invoked here.

## Independent public contract

The [Java SE 25 BigDecimal class contract](https://docs.oracle.com/en/java/javase/25/docs/api/java.base/java/math/BigDecimal.html)
requires `NullPointerException` for null reference inputs to constructors and
methods. The [Double/context constructor](https://docs.oracle.com/en/java/javase/25/docs/api/java.base/java/math/BigDecimal.html#%3Cinit%3E(double,java.math.MathContext))
does not exempt signed zero. Both `+0.0` and `-0.0` are finite ordinary values;
their only invalid input in `BigDecimal(value, null)` is the null context.
NPE is therefore required independently of any reference implementation output.
Only public API documentation, our own implementation and existing observations
were read; no foreign numerical implementation body was consulted.

The implementation's zero coefficient and scale zero remain the expected
valid-context representation. Zero is exact at every allowed precision and
rounding mode; no rounding or sign distinction is needed. This is distinct from
the canonical-string floating conversion extensions, whose zero cohort may have
scale one. Those extensions and their contracts are outside this change.

## Historical observations, not a fresh probe

The unchanged `scripts/generate-null-oracle.main.kts` gives the exact Double/null
input order below. Direct text inspection of chunk 3 of the unchanged frozen
`Jdk25NullPrecedenceResults.kt` gives these outcomes:

| Double input | Frozen outcome | Required test policy |
| --- | --- | --- |
| NaN | `!NumberFormatException` | Retain historical staging comparison |
| negative infinity | `!NumberFormatException` | Retain historical staging comparison |
| positive infinity | `!NumberFormatException` | Retain historical staging comparison |
| `-0.0` | `0` | Retain observation separately; require NPE |
| `+0.0` | `0` | Retain observation separately; require NPE |
| `0.1` | `!NullPointerException` | Retain comparison |
| `1.0` | `!NullPointerException` | Retain comparison |

These are supplied historical observations. This source milestone supplies no
fresh black-box host or own-library result and no runtime confirmation of a JDK
defect. The source predicts the baseline own implementation returns zero for
both signed-zero/null inputs because `fromDouble` returns `ZERO`, bypassing the
old context check. Parent will obtain separately authorized baseline/candidate
observations. Task 568 runtime evidence is not task 569 evidence.

## Minimal production reasoning

The only production edit is in `BigDecimal.roundedDouble`:

```kotlin
val exact = fromDouble(value)
val context = mc ?: throw NullPointerException()
return if (exact.signum() == 0) exact else exact.round(context)
```

`fromDouble` remains first and unchanged. Its nonfinite validation and complete
finite conversion still occur before context validation. Nonfinite/null inputs
retain their existing `NumberFormatException` staging; this is not a claim that
the API imposes a universal precedence between two invalid arguments. Finite
nonzero/null inputs already reached this same NPE guard. Finite zero/null now
reaches it too. With a valid context, the same zero result or same exact value
and same virtual `round` call receive the identical context object as before.

No fields, cache, kernels, signatures, constructor nullability, other
constructors, `pow`, sqrt, serialization or API shape change. `BigInteger.kt`
and its d0 division implementation remain untouched, including task 528/530
metadata and evidence. No task 538 proposal or task 568 snapshot is modified.

## Exact consumer bounds

1. `NullPrecedenceOracleTest`: the two full input keys are
   `double/null context -0.0` and `double/null context 0.0`. Only the signed-zero
   labels are stabilized using raw input bits, avoiding platform-dependent
   Double string formatting. Selection uses no observed output. Each selected
   frozen outcome is still checked against literal `0` and printed separately
   from independently required `!NullPointerException`. Duplicate rejection,
   exact key-set equality and selected count two bound the change. The existing
   two sqrt selections and their assertions remain. Total 3462 rows, 3458
   unchanged host comparisons, four independent null requirements and the
   exhausted-cursor assertion remain explicit.
2. `NullArgumentConformanceTest`: selection requires exact declaring class
   `java.math.BigDecimal`, constructor parameter types `(double, MathContext)`,
   null second argument, and first-argument raw bits `0L` or `Long.MIN_VALUE`.
   It cannot select other constructors, nonzero inputs, methods or outputs.
   Each selected live host outcome and own outcome is printed; own NPE is
   independently required. Duplicate rejection, exact signed-zero key set and
   count two bound the new selection. Existing sqrt selection, reflection/TCK
   adaptation, all invocations and every other comparison remain in place.

Direct call-site reads found valid-context `BigDecimal(0.0, mc)` in
`ZeroContextScale567Test`; that assertion already requires zero at scale zero
and is unchanged. Its floating extension controls use nonnull contexts and a
different canonical-string cohort. `JdkOracleTest` Double construction uses the
one-argument constructor. No additional affected consumer was identified or
changed. The frozen fixture and generator are byte-identical to baseline.

## Prepared finite test intent

`DoubleZeroContext569Test` has four common test methods:

- Both raw-bit-distinct signed zeros with a typed nullable null context require
  NPE through Kotlin public construction syntax. Exact bit-set and count checks
  reject missing or duplicate signed-zero inputs.
- Two signs × precisions `0, 1, 7, Int.MAX_VALUE` × eight rounding modes = 64
  valid-context constructions, each independently requiring coefficient zero
  and scale zero. Both one-argument zero construction controls remain exact.
- Five finite nonzero/null controls (`0.1`, `1.0`, `-1.25`, Double MIN/MAX),
  plus four exact binary fractions (`1.0`, `-1.0`, `0.5`, `-1.25`) with literal
  coefficients/scales at p0/p7/pMAX and every mode: 96 exact results. Another
  eight p2 controls on exact `1.25` require seven literal rounded results
  (`1.2`/`1.3`) and the `UNNECESSARY` error. The decimal tie cases are specified
  independently, without calling another library to derive expectations.
- Three nonfinite inputs retain historical null-context staging, one-argument
  rejection, and nonnull p7 rejection at all modes. These are finite retained
  staging controls rather than a general competing-invalid precedence rule.

`DoubleZeroContext569DiagnosticTest` has three JVM test methods and an exact
BellSoft / `25.0.4+9-LTS` runtime gate. Two zero/null inputs retain raw live host
coefficient/scale observations and compare them to the historical zero outcome;
own NPE is separately required. Signed-bit key-set/count/duplicate checks bound
those two inputs. Another 64 zero/valid-context pairs check independent fields
and public JDK equality. Five remaining historical Double/null rows preserve
host outcomes and existing comparisons; one p2/HALF_EVEN `1.25` pair checks the
literal `12@1` result. Every own construction uses Kotlin call syntax; no new
reflection, Java constructor-reference or Java source/ABI requirement is added.

These cardinalities are source reasoning and future test intent, not executed
coverage counts. All source and test changes are **UNCOMPILED / UNRUN**.

## Original archive and exact file identities

Before any edit, exact original production, existing consumer, frozen fixture
and generator files were saved read-only under the unique own directory
`/private/tmp/krogu-569-source-9543a8cb.WwEQfM`.

- `original.tar` SHA256:
  `93fcb3699ad86300e18fc1d21b072e928581ebc709c3fc2a9f3f4b91ab8087b5`
- `original.sha256` SHA256:
  `4798a181d7cb5ddbd3a82436e0687ebd1092701750ef111bdd4d8cc6982799bb`

| File | Original SHA256 | Prepared SHA256 |
| --- | --- | --- |
| BigDecimal.kt | `9f14989a75a21662f4dfd1b61bf71c4ad0c04bcc3d9e8be4b4038103b443ab21` | `57a5af7b71c37ef4a1b460fbae0fd793ae000ea0eabafca34e85e89567a5a0a1` |
| NullPrecedenceOracleTest.kt | `05daa7a14d3ab90a8e6af781a119f4268fdbccc4e5639d167285412f7cdfe0cc` | `99bb9d7794959bb0c36cf1b4f1deaff8f00059a6ea861150bfc42d4490af5baf` |
| NullArgumentConformanceTest.kt | `6bcc75b1ccbeed51c40540bb78afe4ac2e596663fb9b5c6d4d8f04efd61b8ca0` | `e8dcc1a91b92aa0a548eb3dc1791a61768624471b237e3ad52d56a66a8f03028` |
| DoubleZeroContext569Test.kt | New | `a554b7a556361ccb4966fd47713e3936685693e68fcf97ec5322249549c09b1d` |
| DoubleZeroContext569DiagnosticTest.kt | New | `7f6b06471831ca7ad041d7dd261c5c4fed031df80647c440a23b51b4c2763681` |

Unchanged SHA256: `BigInteger.kt`
`d0e757ebd5955604d5fb66d8dda13b1c15b2ecb300a37a5fbcb1fcfaeb65c16f`;
`Jdk25NullPrecedenceResults.kt`
`a6a4d9edc0b4b02d56f9665a1a4c3646ee8a57ef6f708da2c288926d55a67da9`;
`scripts/generate-null-oracle.main.kts`
`319b5994c939abe909d5c20e71518df603f2062915e9e542a1e2c12ccd24837f`.

Final proof/handoff and full archive-diff hashes, plus independent read-only
review disposition, are recorded in the terminal ref-less task comment and
archive directory manifest to avoid self-referential document hashes.

Parent-owned remaining acceptance gates are the separately authorized fresh
pinned baseline/candidate black-box replay, fresh own JAR and before/after input
checks, retained/new JVM/JS/Wasm/macARM methods, unchanged decimal controls and
fresh exact-published-HEAD all-six-job CI. This source milestone does not satisfy
those runtime gates. Task 569 remains linked and `in_progress`; no Git mutations
or task review/done/unlink transitions are authorized here.
