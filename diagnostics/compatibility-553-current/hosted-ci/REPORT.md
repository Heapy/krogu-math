# Current-source hosted validation for #553

Status: hosted execution and its independent evidence audits complete on the isolated audit ref. This report does not mark #553, #539 or the overall compatibility goal complete. The final literal requirement disposition and integration binding remain separate.

## Exact source identities

| Identity | Commit / binding |
| --- | --- |
| Audited public ref | `audit/553-current-3f0-docs8404`, `975182f9bca911be86ef1bc4efedc7039cc8fe36` |
| Final numerical production | `3f0e1fb8f763b6518395535e222c18407112c9aa` |
| Production manifest | 405 files, SHA256 `48c4b61794256a738e36af34fe8d1ccc32a897590fa86c7cf7813c24170e5e0a` |
| Immutable #539 evidence base | `0db660d5e30983eafbee60bca7a9480c86fc7fc4` |
| Current main at final read | `df6bc9e69e6549c791f19981ec18691cc5b09412`; accepted529 production plus reviewed docs and test adapter, not production3f0 |
| Documentation source | `8404b22e1d8ca769f73b5916c24da3658648b689` |
| Test-only adapter source | `df6bc9e69e6549c791f19981ec18691cc5b09412` |
| Same-run JVM JAR | SHA256 `e8ba87ac09532760f64bd226c7b00aea40b76d8c0b7ae21ddade521570b4517a` |

The audit ref differs from0db in exactly three documentation files and `tools/math-tck/ArrayAccessor.java`. Those four blobs equal reviewed df6; all other tracked files, including405 production files, workflows, worker sources and canonical539 evidence, equal0db. Composition audit: [independent-targeted-public-and-hosted-ref-review.json](../independent-targeted-public-and-hosted-ref-review.json), SHA256 `22dc7fd793b23ddf340004eb400fcf9656757d3d804e7358223822c42bf424ff`.

The existing requirement matrix and its source applicability proof remain authoritative: [REQUIREMENTS.md](../REQUIREMENTS.md), [public-validation-actual.json](../public-validation-actual.json), source-composition SHA256 `fb6faad6a21b8cbcc1dbd920877246a96fa502e4a3949668a00917b0919ae052`. The proof accounts for43 inherited main files,41 exact and2 exactly reversible after reviewed539 changes, and15 new internal files. All58 final main source paths match975. This report adds actual hosted bindings rather than replacing conditional algorithm, API, range, randomness or subtype arguments with test counts.

## Completed execution

| Required scope | Actual result | Independent evidence |
| --- | --- | --- |
| Supported runtime matrix | 13 instances,7062 tests, zero failure/error/skip: Ubuntu2112, Windows1176, macOS ARM1650, macOS Intel1650, same-run Linux ARM474 | [Ubuntu/Intel audit](../independent-hosted-ubuntu-macosintel-actual-audit.json), SHA256 `95d7be653bc05ca2f6422ad49a5fbb22c943db983c96fd4db20c0a79bb207a37`; [final ARM/oracle/iOS join](../independent-hosted-arm-oracle-ios-same-run-join-audit.json), SHA256 `9a45e867fa9a91f37150f06ce698eafdb2f0c7af71e8baca7249f34cf5c61422` |
| Oracle/inventory scripts | All25 actual workflow commands succeeded | Same final join audit; retained `oracle-117323442263.log` |
| iOS device target | Actual `compileKotlinIosArm64` succeeded; no device runtime claim | Same final join audit; retained `macos-arm-117323442272.log` |
| Normal public corpus | 106 actual PASS =53 baseline+53 port,0 FAIL,46 explicit SKIP; standard/serialization/heavy/memory-512m | [Normal and final supported/public join](../independent-hosted-normal-and-final-supported-public-join-audit.json), SHA256 `1fe60f94277014ca58c42209a7702776585d79a7a34ec2a0da4f12badaeba6de` |
| Large public corpus | 12 actual PASS =6 baseline+6 port,0 FAIL,128 separate inventory SKIP;4g/8g/10g profiles | [Large audit](../independent-hosted-large-39007728056-actual-audit.json), SHA256 `276bf654c83990ab6bf43ec2f5e2ce8b4a9c085493e91a61f96fc67aff277a0f` |

[Compatibility39007702192](https://github.com/Heapy/krogu-math/actions/runs/39007702192) and [large39007728056](https://github.com/Heapy/krogu-math/actions/runs/39007728056) both completed SUCCESS on exact975, attempt1. All seven Compatibility jobs and both large jobs succeeded. No CI restart or replacement workflow was used for this553 validation.

Normal+large results cover exactly59 retained plans in each phase. Normal46 SKIP are12 large-profile rows,22 upstream disabled/package-private/commented rows and12 explicit port adaptation losses. Large128 SKIP retain nonlarge and other inventory rows. None is counted as a passed assertion. All12 selected large invocations actually ran with sufficient fresh memory; no timeout, incomplete OOM or resource-failure waiver was used. Existing official-JCK and adaptation-loss limitations remain.

## Recovered evidence

- `compat-39007702192/` contains all four XML artifacts, the same-run JAR/SOURCE_COMMIT/SHA256SUMS, ARM executable and normal public artifact. `compat-artifacts-final.json` preserves GitHub artifact IDs, archive digests and exact workflow head.
- `compat-39007702192.log` is the complete terminal run log, SHA256 `e881a1f44a2be06a617814862822929bda669fc04d6029832fcfc2757a8ebb7b`.
- Normal results: `compat-39007702192/public-math-evidence/build/math-tck/results/1792217553863/results.json`, SHA256 `2827ab0721c0e7c114e5e6b42f51123005405f796f183582d498220125ea6a52`.
- `large-39007728056/` contains both same-run producer and public evidence artifacts; `large-39007728056.log` preserves the complete terminal log. Large results reside under `build/math-tck/results/1792216334507/results.json` in its evidence artifact.
- `run-39007702192-checkpoint-29.json` records final SUCCESS and exact975 head. Earlier checkpoints preserve actual progress. `run-39007728056-checkpoint-3.json` records large terminal SUCCESS.

The earlier local full run2531 remains105 PASS/1 FAIL/46 SKIP. Its sole stale test-only `words` reflection failure is retained. Reviewed adapterdf6 and targeted68473 then passed baseline and port ByteArrayConstructorTest, two JUnit methods each. Neither historical local run nor targeted filtered SKIPs is relabelled as the current hosted full result.

## Integration and closure sequencing

1. Finish the independent literal17-domain disposition using the existing requirement matrix and source-composition proof; retain every conditional premise and explicit loss. Hosted118 passes do not establish exhaustive compatibility by themselves.
2. If final acceptance requires integrated main, integrate the already-audited source ref only through the authorized repository flow after its source acceptance decisions. Main df6 is not an ancestor of975 because its docs/adapter were cherry-picked; a blind fast-forward is not the current topology. A merge must preserve exact audited production/workflows and the four reviewed documentation/adapter blobs. Record the resulting commit and tree/source equality; diagnose any real conflict or source delta before acceptance.
3. Describe these measurements as execution on975. An integrated commit with equal executable/source tree may receive an explicit scope-qualified carry-forward; do not rename975 same-run attestations to a new main SHA. No extra numerical rerun is justified solely by commit ancestry or metadata. Any actual changed numerical source requires the affected validation justified by that delta.
4. #539 performance acceptance remains separate: its JS small-input issue has a separately owned public-source diagnostic, while private archive export remains unperformed. Do not infer539 completion from this553 corpus. Conversely,539 performance is not an artificial mathematical prerequisite of553; the concrete main/source integration binding is stated explicitly.
5. Record task closure only after the governing literal/source integration disposition. No task state was changed by this report, and no official JCK execution, unrestricted resource theorem or unqualified all-input compatibility claim is made.

No local owned execution remains from these two hosted validations. Immutable539 package and production sources were not modified.
