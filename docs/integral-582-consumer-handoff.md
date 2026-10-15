# 582 finite consumer source handoff

Candidate: `/private/tmp/krogu-582-consumers-m91cSXs6/root`. SOURCE-FROZEN after this handoff. All prepared changes are UNCOMPILED / UNRUN. No numerical model, script, compiler, helper, test, build, probe, Git or tracker operation was executed. Standard text reads/copies/filtering, `cmp`, `diff`, permissions and SHA-256 hashing were used.

## Owned changes

| File | SHA-256 |
| --- | --- |
| `src/commonTest/kotlin/io/heapy/krogu/math/DecimalIntegralDispatchOracleTest.kt` | `ae3c8ec204fc3b077c9d3b9c59c400747e28fe9c16f2ac429dbe2b7ced643d43` |
| `src/commonTest/kotlin/io/heapy/krogu/math/DecimalIntegral582DiagnosticPolicy.kt` | `ce9751b8576299fe0bc4324023c79363c01a50308212acd8f5d9baa450a669ab` |
| `src/commonTest/kotlin/io/heapy/krogu/math/DecimalIntegral582CallerContractTest.kt` | `ab7a04d738e8084b9e20ca7ddc90be83ccbdd32961d480bdf5dff5ea24ededf8` |
| `docs/integral-582-consumer-policy.md` | `b2217d41e2fc6f211f0c5bf913cd7c1f1ba650b5ca33d99cb8ac8ac2713310cc` |

Exactly one existing consumer file changed; the finite helper, one-method separate caller test and policy document are new. There are no source/fixture/generator/old-581 edits. The consumer's old formatters complete their original diagnostic strings before optional selected-result observation; unselected calls have a null observer and keep their exact historical assertion. Numeric failures are accumulated so the entire selected matrix emits full diagnostics before the final assertion.

## Archived originals and text checks

`originals/DecimalIntegralDispatchOracleTest.kt`: `8ceced16945b50a33a522c90860495309b4c0c8823d51ff5861a2f4dad45fbb2`.

`originals/Jdk25DecimalIntegralDispatchResults.kt`: `31e6d042d91c4c3b5a0cdea8845e284c5e9a9fef94f1ff2c412514ae11720f6f`.

Both were copied before editing and made mode 0444. `cmp` of original and candidate fixture returned 0. Text-only `cmp` of the archived fixture's 420 selected full records against the new literal ledger returned 0. The helper is 554 lines and about 42 KiB including the complete ledger, below the JVM string-constant byte limit; no compile claim is made. A plain `diff -u` was inspected for the one existing consumer; no Git command was used.

## Selection and required observations

Dispatch: all 1,400 keys and zero-based ordinals are gated; exactly 420 input-selected rows use precision selector -1 or 0 and one of the three nonzero divisors. Their complete original five-field records are literal-bound. The remaining 980 rows retain exact original diagnostics as assertions. Metadata: all 1,176 keys/ordinals are gated; only truthful stored key `[3,2,1,1]`, ordinal 804, is selected and literal-bound, leaving 1,175 exact old assertions.

Expected numerical results use fixed stored coefficient/scale dictionaries and bounded signed Long rational truncation, required preferred-cohort arithmetic, and character formatting; no production/JDK quotient supplies the oracle. All selected successes must match coefficient text, scale and canonical text; every exception fails. Every selected row, including matches, prints original full record, kind, ordinal, historical-dispatch classification, required numeric classification, current full diagnostic and actual/expected numeric triple. Full key/duplicate/count gates reject selection drift.

One new common test method preserves explicit base-typed context override argument identity, return alias and exception identity, and checks caller-super context/integral results with independent fixed coefficient/scale/text literals. The existing four common and one JVM 582 tests remain byte-identical.

## Frozen non-owned source and consumer pins

| File | SHA-256 |
| --- | --- |
| `BigDecimal.kt` | `00e392444ce4b13122ec6dcb7ccbee357b437da1e6554cc78876e909346c1599` |
| `BigInteger.kt` | `66bb59deeeceb4f664681f99ddacc490c3054b5c42c539d3b8a3a44ed2ddb7c2` |
| `DecimalDivision581.kt` | `00557b2517c70f9294ed1be484177962c441841334585cba14ac7a94c647fa6f` |
| `DecimalContextDivision581Test.kt` | `c198132e64c3ac1f9815c6227b2322d041d972f079207edb6d0eec94fa74a6e1` |
| `DecimalContextDivision581SubtypeJvmTest.kt` | `12fad4905a903927a630b227da5cc8d24ba98959d2fc724bda9d07355f7f2c41` |
| `DecimalIntegralQuotient582Test.kt` | `f3736aae2cca21147dbb878a64bef77277746b899a9b88653ec08242d0b24219` |
| `DecimalIntegralQuotient582SubtypeJvmTest.kt` | `688710730bc63cdb0c6940f78e19958295c02af550366a007f2dfa48c6db45f0` |
| `DecimalDivision581DiagnosticPolicy.kt` | `79926238dec13d85a053e70c5d1174025c17a2a76d99efeaf0e849bc69915f5c` |
| `PortableOracleTest.kt` | `197aef5b300fb1b7e53cbcc874d289d626382c6b86c2ecaf78edadfa6c32429c` |
| `DecimalDivisionPrecisionTest.kt` | `1ad11c367327a0f62813a3ed426a302d0a7b9e273f2d2868cfc1e74bfd5f0259` |

Two initial metadata-only hash lookups used nonexistent guessed 581 test filenames; those reads failed without mutations. The exact filenames above were then located by filename listing and hashed. No source/model execution or runtime retry occurred.

## Evidence boundary

The original first campaign stays 529 JVM methods / two failures, not a successful campaign. Its 160 inherited mismatches have only 12 displayed actual records; the remaining 148 remain unknown. The one displayed metadata failure remains preserved. The first-failure capsule SHA-256 is `dca7c14a27638f77e4f54b20b0184c94dc0531075712bf4e49f851ec27f52bf5`. Fresh diagnostics after a future authorized run are new observations, not a reconstruction of the first run. The added common caller method changes a future inventory by one method per target; no future method counts or pass results are claimed here.

Independent source review is the next gate. The parent owns any runtime grant, integration, publication and task disposition. This finite inherited-dispatch classification does not waive direct virtual calls or all poisoned subtype behavior, and does not close outside-Int fallback, finite positive-context residual/pair range, 548 capacity, broader 541/526, full public TCK or JCK obligations.
