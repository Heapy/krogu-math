# Context-division observation inventory

This is an input-only inventory for `local:526`, covering all **75,728** rows of
the tracked context-dispatch corpus. The existing fixture, evaluator, generator
and assertions remain unchanged. This inventory does not classify every result
field or make the whole corpus a normative mathematical test.

Rows contain source/divisor decimal text, source/divisor probe mode, context
precision, rounding mode and a reference outcome with its callback trace.
Negative context precision supplies a **null MathContext**, not exact division;
zero supplies an unlimited context. A `self` divisor aliases the source including
its override mode; a `null` divisor has no metadata to classify.

The script derives sign and unscaled coefficient digit count directly from ASCII
decimal text. It preserves coefficient trailing zeros and gives zero precision
one. It classifies configured getter returns independently of whether the JDK
happened to call that getter. Changing precision always violates the invariant;
changing sign is inconsistent only for nonzero values (`-0 == 0`). Throwing
getter callbacks are tracked separately from incorrect returned metadata.

| Disjoint row category | Rows |
|---|---:|
| At least one operand configured to return inconsistent sign/precision | 57,576 |
| No inconsistent return, but a sign/precision callback can throw | 7,248 |
| Sign/precision returns consistent, without these throwing callbacks | 10,904 |
| Total | 75,728 |

These last 10,904 rows are **not certified contract-preserving subclasses**.
Every probe also overrides `scale`, `unscaledValue`, `abs` and `negate` to throw,
and exact `divide` to return the replacement `7.00`. Numerical guarantees,
explicit language dispatch, permitted callback propagation, internal callback
order and diagnostic wording still need field-level classification. An unused
bad getter alone does not identify which outcome fields are affected. Nothing
here excludes valid ordinary or truthful-subclass mathematical behavior.

Frozen SHA-256 evidence bound to this inventory:

| File | SHA-256 |
|---|---|
| `Jdk25DecimalContextDispatchResults.kt` | `88fc3c5bc8277a048a83a9ad9b6546bd8a23ae1ad9a10126409d857367391026` |
| `DecimalContextDispatchOracleTest.kt` | `2ed00a5dd3c7ec038e61f5be078697b84609606549c26b1bda1249fce860c781` |
| `generate-decimal-context-dispatch-oracle.main.kts` | `0d9e9b11af4c27892758e78d6b80639b763fcec66ffbe27721d373864a501188` |

From the repository root, with Kotlin 2.4.21:

```sh
kotlin scripts/inventory-decimal-context-dispatch.main.kts --check
```

The check verifies all three frozen hashes, row shape, configured-mode coverage
and aggregate counts. It evaluates no JDK or krogu numerical implementation and
does not delete, move or rewrite the observations. Other oracle families and the
full normative classification remain open.
