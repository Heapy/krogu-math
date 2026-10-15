# Non-null Kotlin API: tasks573–578 and extrema results525

The adopted candidate passes all four local platform gates, its48-input compiler campaign and30 adapted selected public invocations. Exact published-head CI remains open. This report does not establish full Kotlin compatibility or JCK conformance.

## Scope and source

The user confirmed that rejecting-null inputs should be non-null throughout the API, including constructors, and permits matching Kotlin caller/override edits. Task549's51-slot inventory becomes48 non-null inputs while retaining three equals(Any?) inputs. Four OrNull results remain nullable. Java source shapes remain a test-harness concern.

| Focused task | Inputs |
| --- | ---: |
|574 comparisons/Comparable |6|
|575 decimal exact and contextual binary operations |21|
|576 contextual unary operations |4|
|577 integer bitwise operands |4|
|578 validation, constructors, RNG, legacy divide, sqrt and rounding mode |13|
| Total |48|

Task525 separately makes four min/max results non-null. Both branches return the non-null receiver or argument; comparison bodies, virtual dispatch and inequalities are unchanged. BigInteger keeps argument-on-tie and BigDecimal receiver-on-tie. The three nullable equals inputs and four OrNull results remain unchanged.

Frozen revision4 source is `/private/tmp/krogu-573525-combined-source-v4/candidate`, based on published60aea413e0c7af007844aace6ec28ecdfa019b67. Its full213-source manifest is `cd40057ad6bc8c72e87653572496a7e0945db846dab923596115ecdd6562ea42`; archive `combined-source.tar` is `b7c906e39e177ee45ccd636d646cd91f720dab7e2bfa7aaf7b631fac7d1858c5`. Independent source review accepted the composition and narrow post-review corrections.

Production SHA-256:

```text
df55cb8c8c6a291b59f6ee6daf365c9b4c38e4e1f33583e108183c5a28039fb3  BigInteger.kt
08b4329ea5e902f1043107d42ddb02592be537497f6b0990927e8c67c4ed3f29  BigDecimal.kt
d3690387b1345f90cc927044a1a450983dd0d2ce6e3e47054384c31979fd5261  MathContext.kt
```

Numerical kernels, frozen fixtures/generators, current-own serialization and build/toolchain inputs retain their prior meaning. The unadopted538 exact-power optimization is excluded.

## Preserved behavior and diagnostics

Ill-typed common null calls have real JVM Java counterparts:5227 original executions are reassigned, including503 portable rows. Existing fixture keys/ordinals, virtual reflection, direct Java override, inherited/base and actual explicit-super paths remain distinct. Reflection retains13 selected signatures/3781 keys, partitioned819 ordinary null requirements and2962 competing-invalid diagnostics. The original host-zero/null RNG observation remains visible while own ordinary null entry requires NPE. No adapter manufactures an expected NPE or uses caller-side null assertions.

Two first JVM failures are preserved. Revision2's new zero-bit test counted an empty byte callback as a draw. Revision3 then reached its previously unexecuted seeded-state equality and failed. Public random-result contracts and own documentation do not require unchanged subsequent RNG state. Independent review classified those stronger assertions as unsupported; revision4 retains zero/result-range/distribution/null requirements and records callbacks/subsequent values as diagnostics. Production RNG code is unchanged. The candidate's RNG_CONTRACT_REVIEW.md records sources, exact outputs and capsule hashes.

## Compiler evidence

Four identical non-null min/max assignments compile with JDK imports. The48-input candidate's nullable results produced exactly four INITIALIZER_TYPE_MISMATCH diagnostics at lines6–9,column32. After the four result annotations changed, the same assignments,32 migrated non-null override examples and three equals(null) controls compiled against fresh JARded66cd6a325b73a9a393e0fd56281d7bf63d571dab4b146e8f1cb9c0dba2fd1. Four existing legacy-overload deprecation warnings remain. Independent review accepted this bounded witness; no compiled function was invoked.

The final input campaign passed all49 compiler jobs directly against the current JAR: one batch with48 valid calls/32 migrated overrides/three equals controls, then48 independent nullable-argument rejections. Every negative exited1 with exactly one ARGUMENT_TYPE_MISMATCH at its pinned argument position. No compiled function was invoked. All49 captured children were DEAD, no cleanup signal or identity refusal was recorded, and every compile stayed below30s. Independent actual review accepted source/toolchain/artifact pins and all raw diagnostic sites.

Two earlier campaigns remain failed/incomplete: one stopped at a relative diagnostic-path checker bug; the next accepted negatives01–31, then stopped at32 because a wildcard import also offered a bitwise extension. Reviewed V6 corrects only those fixture/checker issues and current provenance. No prior run is relabelled as a complete PASS.

Final compiler evidence is `/private/tmp/krogu-573-48-compiler-success-evidence-v3.tar`, SHA-256 `3822ef53a6a10a63cda623e729e54fff22a75fdda9db1ea99399af89334485e5`. Campaign log SHA is `3d8a480e358353f66d2d22894b2d866c418416e9ad4798ce6da646b5627d672c`; status ledger SHA is `19996034902bcba8be543ee1ba60c0e9e1ee61b22da50567e342add28882841e`.

## Runtime gates

Pinned Kotlin2.4.21, Liberica25.0.4+9-LTS and Gradle9.8.1 are used serially.

| Target | Suites/methods | Status |
| --- | --- | --- |
| JVM |143/492|PASS|
| JS |63/308|PASS|
| Wasm JS |63/308|PASS|
| macOS ARM64 |63/308|PASS|

JVM invocation98895 completed successfully in1m39s. The source-derived auditor93464 also terminated0 with zero failures, errors, skips, omissions, extras or duplicate/inventory anomalies. Independent review accepted actual XML and migrated-row assertions, including3462 precedence rows,3781 reflection keys,73 standalone578 calls,25 RNG rows and current-own serialization tests.

All222 input hashes before/after JVM are identical: `c2bfc722c3628b5d0b48d26d4ae02a970d8e95ea7bdf4821ba99f3817f419d0d`. Logs under `/private/tmp/krogu-573525-validation-v4`:

```text
4841ed19807949288ee0acf22e0be9e6512c68a84020e4b5d2ff191956d5f493  jvm.log
e2a95ae76b5f4ee48e437d3d32b5632b60fef39899a96b03c63e5a896d260912  jvm-audit.log
07860c7d2418c12d188ad8f404207f631ab444046c25d7cfc5496e8e4c86b90d  js.log
9c896b7b6c1c9ed5e2dddd2f9ad5b2152795445f9574d64333aa5594d355e8c5  js-audit.log
0004e3a0e8c69587d7340d25d457696f9b4e1323f3fb6291eba30a4f5b43d95d  wasm.log
fc3a38eb41f249c277c93581f47bae9a3fa00cabc17c0bf6edd9d0e753c88c37  wasm-audit.log
63b5b7b98bb1e44a702d7c133b970801a00a3dfffea4c63fa248e1e4c5b7ebc1  mac.log
70f963a819a488429b6e8802134effd139539699b4fea50f8f6aba4401d03bca  audit-mac.log
```

The main JAR was freshly created during this first revision4 build, with caching disabled/no history, and equals the earlier revision3 artifact used by the accepted extrema compiler witness. No separate forced JAR rebuild is claimed.

JS invocation12944 completed in7m30s; source-derived auditor59677 terminated0 with63 suites/308 methods and zero status/inventory anomalies. All222 inputs still match the same manifest. Independent actual review accepted the complete JS gate, portable503-row partition, extrema controls and retained numeric/RNG assertions and diagnostics.

Wasm invocation83570 completed in2m; source-derived auditor89600 terminated0 with63 suites/308 methods and zero status/inventory anomalies. Its complete input inventory changed222→223 solely from newly generated kotlin-js-store/wasm/yarn.lock (`970a85c50fa072f8543e444af2d28a16de073c3e7ead47b86527d83a557b4dcc`). The raw223-file manifest (`99390c31a34ab42edc49d85157cc0564061fc12ccb56d568e3a3e6807fb9fa44`), exact diff and generated output are preserved. Moving only that new output outside the runtime restored the identical222-file manifest. Every pre-existing input remained unchanged; the complete inventory changed during execution.

Native invocation96504 completed in4m3s; auditor89935 terminated0 with63 suites/308 methods and zero status/inventory anomalies. Its after-input snapshot retains the same222-file manifest. Captured Gradle PID82333 was absent after completion. FSEvents watcher warnings remain in the successful build log. Together the four gates contain1416 successful method executions; this is a sum across targets, not1416 distinct methods.

The four local gates, XML, input manifests, auditor and fresh JAR are archived at `/private/tmp/krogu-573525-local-gates-evidence-v1.tar`, SHA-256 `971e9b79a01dd6e01c2f85d45f71aeef8ded6f1555d617b45b67069edb308dd1`. Independent actual review accepted every target. The root adopted all213 source files byte-identically; its source manifest remains cd40057a…ea42.

## First current-artifact public campaign

The original15-file,30-invocation public campaign terminated1, complete=true and officialJck=false:29 PASS /1 FAIL /111 declared SKIP. Port BigDecimal/Constructor ran all five original TestNG methods: stringConstructor passed; four char-array range methods expected NumberFormatException but received the non-null MathContext entry NPE. Each original call supplies both an invalid range and null context. Independent contract review found no production fix required under the agreed Kotlin non-null policy; it did not classify this as a JDK bug.

The complete failure remains preserved at `/private/tmp/krogu-573525-public-failure-evidence-v1.tar`, SHA-256 `9a447e47ce9e0c445432546cc9c671f8736b84c76310bd180fc75f2e8b24313f`. Raw results1792066236575 JSON SHA is `003ac6cf980dd16c3caebab2c45fca5ca5a25f43fd4ce269eeace3f2ce63be96`; campaign-log SHA is `6818c7f93b2dd078cfefd26a57ab622aed9712eaec802c39b26cb2eab0a63a8d`. Scoped late parent-process inspection found the log owners already terminal; no captured parent start identities are claimed. Child process records and the outer exit remain in the evidence. Host swap/load was substantial; no uncontended-host claim applies.

A narrow test-only adaptation uses a real own UNLIMITED context for these four calls while keeping all original range operands, method annotations and NumberFormatException/cause assertions. Four explicit lost-coverage records identify the original joint-invalid priority cases. The [adaptation record](non-null-573/PUBLIC_CONSTRUCTOR_ADAPTATION.md) describes exact inverse-source guards and the corrected manifest declaration. It cannot turn the original failure into PASS.

The fresh revised campaign1792066692563 terminated0, complete=true, officialJck=false: **30 adapted selected PASS /0 FAIL /115 declared SKIP**. Independent actual review accepted all30 exact phase/directive IDs,31 successful compilations,18 clean caller-bytecode audits and15 port core bindings to the unchanged ded66 JAR. No timeout/OOM occurred. TestNG passed5/5 and Jupiter2/2 in each phase. All four original joint-invalid cases have separate companion SKIPs; no assertion was deleted. Before/after tool manifests match. Captured own parent identities94680/94690/94698 were absent after termination.

Success evidence: `/private/tmp/krogu-573525-public-success-evidence-v2.tar`, SHA-256 `de8f3839b8c6793d4125d2e787ab11e6325fb16b3e69703a836dd0f4be06ac84`. Raw JSON SHA is `58591a9fdd5fd8c59c42a846d8c995e2046ae5923f30d308e723479fd2eeec0b`; campaign-log SHA is `2db7d0f8c6aa0c60a5525029b4bac373474e0598ba4a6fca0f8d5b7bf1232992`. Adapter SHA is `05c05df6ce1c92d75eef368d7d9730f67df8ce3cef3919072431b15e37e4ad2f`; manifest SHA is `05b30e6abf14c9584146e22d2b720ece8898770b7e9562bea1b9884ec60e40e7`. Official JCK, the full public corpus, previous PrimeTest failure and broader range/resource gaps remain separate.

All-six CI success at published60 certifies the earlier544/545 integration, not this unpublished API candidate. Tasks573–578 remain open until exact published-head CI. Task549 retains broader grammar/call-site/exception-matrix obligations beyond this nullability milestone. Broader525/526, the known compiler issue557 and official JCK status remain separate.
