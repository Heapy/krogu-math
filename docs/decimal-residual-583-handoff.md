# 583 source handoff

Candidate `/private/tmp/krogu-math-task-583-saq4mH3L/root`, archive `a62f59b` plus the parent's frozen234-source582/584 overlay. **All new code/tests are UNCOMPILED / UNRUN.** Source is frozen for independent review. No runtime is requested or owned by this worker.

## Exact prepared files

| File | SHA-256 |
| --- | --- |
| `src/commonMain/kotlin/io/heapy/krogu/math/BigDecimal.kt` | `1f65c1d99d3144989068dfbdc8ffa70a32ceab2fc0a6ea2a4eb3ac142e03bf3e` |
| `src/commonMain/kotlin/io/heapy/krogu/math/BigInteger.kt` | `d6d531742a7b12bd6cc09cf6b98daca11f70b5207687ecd4c04c5a11d655a9b2` |
| `src/commonMain/kotlin/io/heapy/krogu/math/DecimalDivision581.kt` | `e974af13310f8aec453223760e4d6d9554ed11d251cfe5358ec5cbee1d4131dc` |
| `src/commonTest/kotlin/io/heapy/krogu/math/DecimalResidual583Test.kt` | `80b264447716b13bd1d0cf74067a80a63a54d2f17b31330e1f638e674432d025` |
| `src/jvmTest/kotlin/io/heapy/krogu/math/DecimalResidual583SubtypeJvmTest.kt` | `9f0445fc29d732f7f4082e290834de2c0542d16cbe1ca1019cc6b57185b4cf81` |
| `docs/decimal-residual-583-implementation.md` | `04ae86178c5c5e402f4ec948b7b2f6d6291ef38e7c4a2f9f8d328381f02f99d5` |

This handoff is the seventh file; its hash is returned separately to avoid self-reference.

## Preservation and source diff

Three owned production originals were archived BEFORE editing under read-only `../originals-583/`: BigDecimal `a925bf9144044fe23e4bb618b6777b7f2f90d1f591d9a0856f3651bcaaaf3e50`, BigInteger `66bb59deeeceb4f664681f99ddacc490c3054b5c42c539d3b8a3a44ed2ddb7c2`, DecimalDivision581 `00557b2517c70f9294ed1be484177962c441841334585cba14ac7a94c647fa6f`. Files are mode0444, directory0555. The parent's inherited all234-source-match log remains setup evidence, SHA-256 `b47bc16181197665874348285514819f6ba36945be00d57749727700cd2c1db4`; it is not a new build/test.

Plain diffs of all three originals were inspected. BigDecimal has only the finite-negative-scale certificate and the two pair seams; positive branch callbacks, finite postprocessing, no-context582 quotient and both remainder bodies are unchanged. BigInteger adds one stored nonvirtual bridge. DecimalDivision581 adds a separate sufficient predicate/certificate/residual/view and suffix-removal operation; all old method bodies remain intact. Recursive read-only `diff -qr` against the frozen584 candidate source tree lists exactly these three modified files plus the two new tests. Every other source file is byte-identical; no fixture or old consumer edit was made.

The frozen584 midpoint complement is outside all three BD diff hunks and remains unchanged; its common test SHA-256 remains `f3b76ad72fa1215646749f0716d9ec1a55efca6ff17c7061f4c7109c42f410bb`. Frozen582 common/JVM tests remain `f3736aae2cca21147dbb878a64bef77277746b899a9b88653ec08242d0b24219` / `688710730bc63cdb0c6940f78e19958295c02af550366a007f2dfa48c6db45f0`; dispatch consumer/policy/caller remain `ae3c8ec204fc3b077c9d3b9c59c400747e28fe9c16f2ac429dbe2b7ced643d43` / `ce9751b8576299fe0bc4324023c79363c01a50308212acd8f5d9baa450a669ab` / `ab7a04d738e8084b9e20ca7ddc90be83ccbdd32961d480bdf5dff5ea24ededf8`.

## Implemented route and proof

The sufficient materialization predicate covers product scale, both alignment factors/coefficient ranges and signed subtraction's carry. Success retains the whole old expression. Uncertainty is not an error; stored quotient certification follows, retaining the selected quotient object. All mismatches/unsupported scope decline to the whole old expression.

For S>=0, independently checked stored magnitude bounds B10^S by A; supported530 simultaneous division verifies K and obtains r at source scale. For S<0, a separate residual-only restoring pass checks each integer quotient digit against a stored coefficient with a virtual suffix, then derives the signed residual without Q*d. Exact-tail skip requires r0 AND no unread original nonzero digits. Required remainder scale is U=max(Sa,T+Sb), not automatically Sb. Removing min(-S,-T) exact decimal zeros obtains a coefficient below supported B; zero has explicit U publication. Private suffix removal handles whole and partial radix cells with checked exactness. The proof binds Long/index/primitive/all-live-buffer bounds and remains conditional on completing own530/548 leaves/resources.

The finite negative-scale check is included BEFORE the old Q*d admission expression. It preserves the existing first scale callback and declines a reported/stored mismatch. A certificate proves the candidate is already the true integer part, hence admissible. It never substitutes a newly computed quotient for an insufficient-precision or poisoned candidate. The subsequent finite postprocessing remains unchanged. Keeping virtual calls/no caches can repeat certification at admission and pair; direct verification can allocate a second bounded K. No single-pass, timing or heap improvement is claimed.

## Prepared tests

Four new common methods and one JVM method (17 new method executions across a future full four-target run, not an executed result):

- Raw helper:360 valid bounded Long/rational cases +720 wrong coefficient/scale declines, plus6 independent symbolic suffix-removal cases. Includes zero quotient, exact/nonzero residuals, unread nonzero original digits, genuine exact tails, signs, radix borrow/carry and input-array immutability. No production quotient supplies expected values.
- Integer certificate:13 padding/fraction/zero/multiword/MIN/MAX cases, including a gigantic virtual candidate length rejected by length before iteration.
- Real public witnesses:4 tiny endpoint rows ×4 signs ×8 modes, with640 explicit accepted finite/p0 primary calls and96 insufficient-precision AE checks. Both S signs use P=MIN-1, forcing the actual new branch without a fake cap. Cases100/1 and2001/20 at Sa=MIN+1,Sb=MIN+2 yield1@-3 and0@Sa / signed1@Sa; positive-S100/1 and101/1 yield1@-1 and0@Sa / signed1@Sa.
- Caller control: selected quotient alias, exact argument/context and thrown exception identities, whole nontruthful mismatch fallback, and zero-divisor errors. No callback count/order/noncall assertion.
- JVM:512 pair checks across4 fixed literals ×4 signs ×4 ordinary/current-format truthful-coefficient masks ×8 modes, asserting quotient/remainder coefficient/scale and input immutability. Setup reachability is bounded to own current format, not legacy identity compatibility.

These are prepared source-derived inventories, not numerical/model executions. Every old test and raw evidence remains unmodified. The accepted proposal and both independent tiny witness addenda are fully pinned in the proof. The maximal supported-output witness is still symbolic and UNRUN.

## Gates and limits

Independent source/proof/test review is next; root alone owns later runtime, source manifests, integration and task disposition. S outside Int, unrecognized selected cohorts, earlier contextual-prefix failures, broader541/526 poisoned arithmetic/internal dispatch classifications, full548 capacity, public TCK/JCK and performance remain outside this acceptance. Explicit public multiply/subtract semantics remain unchanged. No blanket poison waiver or automatic endpoint/LOW rescue was added.

Only direct source text reads/copies/diffs, hashing, permissions and scoped source/document edits occurred. No compiler, script, numerical/model/helper/probe/certificate/test/build/process, Git or tracker operation was executed. No background work, future-window wait or worker execution grant remains.
