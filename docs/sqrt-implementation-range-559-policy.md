# SquareRootTests: one reference-range call omitted on the port

The preparation labels below describe the frozen source milestone. Its later
[focused verification](sqrt-implementation-range-559-verification.md) supplies
actual baseline/retained-port results and keeps the omitted call as explicit SKIP.

SOURCE-ONLY policy/adaptation preparation,2026-10-15. This candidate is
UNCOMPILED/UNRUN in `/private/tmp/krogu-sqrt-range-policy-BQZ3iKLT/root`, a fresh
archive of `da6086342165711ac800e0c5c515eb2e6df7aae5`. Parent and independent
source review precede any adoption or separately granted harness execution.
No numerical implementation changes, compiler/script/helper/probe/test/build
execution, Git/tracker mutation or public-checkout write occurred. Public TEST
source and official public API were read; no foreign numerical implementation.

## Exact classification and coverage loss

Only the port's single main call to `scaleOverflowTest` is omitted. The entire
original method stays in the generated source, as do all other main calls,
assertions, arithmetic inputs, loop conditions, order and resource directives.
The full original host baseline remains scheduled and unchanged. The omission
loses execution of one unique numerical port input and its expected exception
assertion; it is not merely removal of a message check.

That method invokes sqrt on `BigDecimal.valueOf(1,-1)`, value10, with precision
`(1 << 30)+1 =1073741825`, rounding mode UP. It succeeds only after catching
ArithmeticException; normal return increments the failure count without checking
the returned value/scale. Its diagnostic expressly recognizes that the
algorithm's supported range may have extended. This is a reference algorithm's
range/overflow expectation, not a specified universal precision cutoff.

The [public sqrt contract](https://docs.oracle.com/en/java/javase/25/docs/api/java.base/java/math/BigDecimal.html#sqrt(java.math.MathContext))
specifies rounding, preferred scale and accuracy, with negative/exact/UNNECESSARY
error conditions. It does not prescribe this particular internal overflow.
This does not prove that own fixed-range sqrt(10) must succeed or that no
separate error obligation exists. A later established mandatory own error/result
must retain independent coverage; the reference-only label cannot waive it.

The new branch appends exactly one adaptation-companion SKIP/lostCoverage item
for this omitted10/p/UP call. The existing runner creates that separate port
record alongside its independently determined primary invocation result. A
later PASS would mean only that retained port phases passed under their original
limits; the omitted call did not pass. No new all118/no-skips result exists.

## Strict transformation and provenance

The original public TEST SHA-256 is
`1d4abb1ea98e14a3cd28defd54d16f2ee49563e3862e86cad11399f5591274aa`.
Its notice is **Copyright2016,2024**, GPL-2.0-only; the full header is retained.
The unchanged source-lock row binds path
`test/jdk/java/math/BigDecimal/SquareRootTests.java`, original Git blob
`7d9fce4caa0d803306b3c51531d30f144d484d2f`,34762 bytes and that full SHA.
This preparation reads no foreign implementation body or bytecode.

After the existing import-only rewrite, its source is pinned by
`afb93448ad8b9224e7b6674226ce894a8a7ad746d3c64a55917047723c7387b3`.
The SquareRootTests-only branch requires exactly one occurrence of the complete
newline-delimited anchor:

```java
        failures += scaleOverflowTest();
```

It replaces that one line with:

```java
        // KROGU_REFERENCE_ONLY_SQRT_IMPLEMENTATION_RANGE: original scaleOverflowTest call omitted.
```

Zero or duplicate anchors fail the `indexOf(before)>=0` and
`indexOf(before)==lastIndexOf(before)` checks, including adjacent lines whose
newline-delimited anchors overlap. A pre-existing replacement is rejected; one
verified range is replaced, and the same first/last-index uniqueness check is
required for its replacement afterward. Reversing
it must recover the entire import-only source, byte-for-byte, before the generic
unchanged-notice guard returns. No numerical branch/error is caught, no success
value/boolean is supplied, and no method body is deleted or edited.

The expected generated-source fingerprint is
`eca38c8fbcb3fae6f82a1e98ff3cf63ad2e58784be034b2c76a6f1a5867704d5`.
This is a static text projection of the frozen imports-only TEST, not an executed
adaptation or harness result. Reversing that text projection independently
recovers `afb93448…`. The unchanged runner records the actual generatedSha256,
retainedChanges and companionCoverage in inventory metadata when later run;
no generated inventory or actual SKIP has been fabricated here.

The manifest changes only this one row, from imports-only selected to explicit
port-sqrt-implementation-range-policy with its exact loss. All existing adaptation
branches are unchanged. tools/run-math-tck.main.kts, source-lock, original sources,
primary PASS/FAIL/completion/origin logic, JVM resources, seed and deadline are
unchanged. There is no host-math fallback, production precision cap or failing-call
catch. The source-specific adaptation script remains GPL-2.0-only; derivatives
retain upstream notices and stay in the harness boundary, not library artifacts.

## Existing failures and locator remain actual failures

The [unmarked1b9 hosted run](public-math-1b9-regular-failure-559.md) and
[unmarked80987 run](public-math-80987-regular-failure-559.md) each retain
105PASS/1SquareRootTestsFAIL/46SKIP under the original120s/512MiB regime.
Their raw output does not prove the precise stopped input/method; neither is
retrospectively relabelled by this adaptation.

The separately authorized local marked run is documented at publication
`docs/sqrt-public-locator-579-result.md`, SHA-256
`1e2f0727536e39e189c0b929df4ec7357d61459c7f947d59accd9a3f0ccbcb8b`.
It produced exactly51 markers0–50, last `BEFORE scaleOverflowTest`; no AFTER or
completion marker. TEST timeout120134ms/exit143 and outer exit1 remain failure.
Returning earlier phases are not individually credited as passes because the
original accumulated failure total was never checked. This identifies only the
unreturned method of that marked local run, not an internal cause or hosted
stopped input. The local artifact is579/1b9 JAR e7047f82, not a later581 artifact.

| Retained locator evidence | SHA-256 |
|---|---|
| `/private/tmp/krogu-sqrt-locator-GdPaXqTl/run-new/RESULT.tsv` | `0d8fa1cf8e20355082224bb79559c7d880778e14828f12a2c27ef3ac14d84908` |
| Outer `run-escalated-one/outcome.tsv` under `/private/tmp/krogu-sqrt-locator-supervisor-Nu4Chqkt` | `97e74de45bb6aa75adf708e62fc7536180f74aeb6d00a8d01aa29c11a5b06764` |
| `/private/tmp/krogu-sqrt-locator-actual-audit-sxWcjP3n/AUDIT.md` | `e12c20308a6dc422d3e3896e32034cd0e08eafeb5353b4e38d389f57152f5b09` |
| Same audit's288-entry index | `60a068bbf2cdac10c8c6f851e56516a961c5ee2ca519042ead658d2e0eb0a5ce` |

The preceding sandbox environmental failure remains separate, with no marked
TEST outcome. This preparation performs no retry, process query, extra locator
or numerical execution. Removing a port test call does not repair the expensive
production path, prove a current-source timing result or establish practical
maximum-domain support.

## Why there is no blind production precision cap

The independently accepted SOURCE/SPEC memo
`/private/tmp/krogu-sqrt-range-classification.md`, SHA-256
`9d3911d95045766febff476129c2bb98b593dda680ed048d2aee2994334cf5d2`,
supplies a symbolic irrational witness at this same precision. Let q=2^29,
p=2q+1, x=(10^q+2)@q and y=(10^q+1)@q. Both coefficients have q+1 digits,
strictly within the public bit range by10^3<2^10. Since q is even and10^q+2
is2 modulo4, sqrt(x) is irrational. On the p-digit grid with spacing u=10^-2q,
0<y−sqrt(x)=u/(y+sqrt(x))<u/2, so UP's numerical result is exactly y.
Its provisional coefficient has q removable trailing zeros; the existing579
strip-before-publication rule yields the supported compressed pair y.

This defeats a precision-only rejection even after the finite-exact-root shortcut
under that existing own cohort rule. It is not the different input10, an executed
giant object, practical allocation evidence or an all-input range theorem. The
public inexact-cohort wording versus the existing stripping rule remains a
separate representation lemma. No mandatory public error/result, broader fixed-L
cohort support, performance acceptance or whole579/559/548 closure is inferred.

## Gates remaining

Independent review must verify the exact branch, inverse, notice, manifest row,
one companion and limits before any later parent adoption. A future authorized
harness preparation/execution must produce and verify actual fingerprints and
lost-coverage records, retain the full original baseline, and judge retained
port assertions independently. Compiler/runner failures and timeouts remain
failures; no automatic retry or resource relaxation is introduced. No passing
adapted result, official JCK/TCK status or production arithmetic fix is claimed.
