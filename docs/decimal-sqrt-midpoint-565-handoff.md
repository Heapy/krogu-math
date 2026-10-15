# Task565 preparation handoff

Task `local:565`, live pane `074237e5`, parent `af5f5ab4`.
Worktree `/private/tmp/krogu-math-task-565`, branch `task/565-sqrt-midpoint`.
HEAD/base `37f55f2de12a9e30b8d2bda114fd986e21b0e83b` is unchanged.
Changes are not committed. Task remains linked/in_progress.

## Deliverable and exact ownership

Only tracked edit: `src/commonMain/kotlin/io/heapy/krogu/math/BigDecimal.kt`,
13 inserted and 4 deleted lines in the inexact-root block. Euclidean division
retains q/r; delta=q-a² plus r determines exactness; midpoint sign uses
delta/a and, only on equality, d's quotient/remainder by four. No added helper.
Unchanged final increment, rounding, coefficient/scale return, and preferred
strip preserve the existing representation policy.

New files: common `DecimalSqrtMidpoint565Test.kt`, JVM
`DecimalSqrtMidpoint565JdkTest.kt`, `docs/decimal-sqrt-midpoint-565-proof.md`,
this handoff, and write-only `scripts/certify-decimal-sqrt-midpoint-565.main.kts`.
No other tracked edit, no old assertion/fixture change, no main docs commit
modification, no other worktree edit, no BigInteger/metadata528/finite563 change,
and no Git mutation. The production diff is a single hunk after numerator/d
construction and before the existing result return.

## Proof and retained limitations

For n=q*d+r, a=floor(sqrt(q)), delta=q-a², midpoint H has sign of
`(4*(delta-a)-1)*d+4*r`. Unequal delta/a decides the sign by integrality
and 0<=r<d. On equality write d=4*u+v: compare r/u, returning -sign(v)
on equality. Exactness requires delta=0 **and** r=0. Square/delta<=q<=n;
u<=d, v<=3; increment<=max(q,2). The multiply lower-bit guard fits because
a²<=q. This covers a=0,d<4,all remainders/ties and supported maximum q/d.
The full proof and independently derived literal square inequalities are in
the proof document. The giant c=4*10^646456992+1 witness is symbolic only.

Parent comment1904 requested a proof-only qualification: constructor-created
coefficient subtypes are normalized, but current own-format
`readObject`/`resolveObject` can retain a truthful BigInteger coefficient subtype.
The proof's truthful stored-value/operation premise covers consistent retained
subtypes as well as ordinary integers; it does not require every changed
arithmetic receiver to have base BigInteger dynamic type. Removed or changed
internal integer callbacks are observations, not an automatic guarantee or
waiver of callback results, exceptions, or trace parity. Serializer and legacy
scope remain unchanged. No additional subtype test was requested or added.

Upstream numerator/power/working-scale/precision and kernel costs remain outside
this task. JS string-capacity metadata is separately528; broad542 stays open.
No full sqrt correctness/completion or JDK defect/divergence claim is made.
Public prefix, finite roots, explicit override/super/exception identity and old
whole-string callback/returned-representation tests remain enabled unchanged.
The changed internal integer call graph is explicitly documented in the proof;
runtime parity is pending and no internal observation is automatically waived.

## Test/certificate method inventory: PREPARED / UNRUN

| Source | Method | Written controls | Status |
| --- | --- | --- | --- |
| common | `tinyPositiveRemainderAllEightModes` | 4+10^-k at k=1,2,17,18,19,64,256,1024; all eight modes; ordinary/truthful/explicit super; coefficient+scale and UNNECESSARY | PREPARED / UNRUN |
| common | `literalNeighborsTiesPrecisionScaleAndWideCohorts` | 23 literal cases, compact and 80-zero wide cohorts, precisions 1/2/7, positive/negative scales, even/odd midpoint ties and strict neighbors, delta decision sides and fractional quarter side, carry; ordinary/truthful/super | PREPARED / UNRUN |
| common | `exactRootsAndPreferredRepresentationRemain` | preferred exact coefficients/scales for every mode plus inexact trailing-zero result | PREPARED / UNRUN |
| common | `explicitOverrideSuperAndDirectExceptionIdentity` | 25 null/unlimited/finite contexts; replacement and direct exception identity; inherited null/unlimited errors; direct sign/precision exception identity | PREPARED / UNRUN |
| JVM | `boundedTinyRemaindersMatchPublicJdkAndLiterals` | bounded public-JDK/live port coefficient+scale+text versus independently required tiny-remainder literals; eight modes | PREPARED / UNRUN |
| JVM | `boundedNeighborsTiesAndCohortsMatchPublicJdkAndLiterals` | same 23 independent literal cases with compact/wide cohorts; JDK coefficient+scale independently constrained before port differential | PREPARED / UNRUN |
| certificate | top-level `.main.kts` | frozen source block, direct squared-reference enumeration q0..128/d1..64/all r, exactness, a0/d<4/ties, symbolic maximum-bit and witness metadata | PREPARED / UNCOMPILED / UNRUN |

Public genuine midpoint ties use the earlier finite-exact root path; no tie
branch coverage is attributed to those public tests. Private formula tie and
arbitrary-d endpoints are handled by the mathematical proof/certificate.
No huge numerical input, output generation or JDK numerical probe occurred.
The certificate uses primitive Long only, one source String and counters, no
subprocess/writes, and is designed for <=256MiB future execution. Its witness
bit-length premise refers to the existing own bounded logarithm certificate.

## Read-only verification and review

Frozen reviewed source/proof SHA256 values:

```text
645d1d04eee33c4e00d36dbfd620489b277e5f5e32734fb8893cb80a3ec5aa53  src/commonMain/kotlin/io/heapy/krogu/math/BigDecimal.kt
a60d94b475d6752b153209277001885b690e1b8c2d0a6ecfe663583efc0cf6d8  src/commonTest/kotlin/io/heapy/krogu/math/DecimalSqrtMidpoint565Test.kt
3163ee36357a6a76a435e269864ec4b24f3c135e07b782570db46c79ed95cb1b  src/jvmTest/kotlin/io/heapy/krogu/math/DecimalSqrtMidpoint565JdkTest.kt
dc61fce2adf1590c3e5ca352096dd1d7effa04d18659175b7abc6ff7e2b7997d  scripts/certify-decimal-sqrt-midpoint-565.main.kts
7f2c32cc3f826ae71472af7798b438c5c30c8297d9f06853a73ee96aca0b4815  docs/decimal-sqrt-midpoint-565-proof.md
```

The initial handoff's own SHA256 was recorded externally in comment1899.
This amended handoff's final SHA256 is supplied in the follow-up response to
avoid a self-referential hash; this documentation-only follow-up permits no
task mutations.

Executed operations only: task/project/status/source reads, Git read-only
diff/status/HEAD checks, `git diff --check`, and SHA256 hashing. Whitespace
check passed for the tracked edit. Initial exact hashes are recorded in the
authoritative ref-less task feed; amended documentation hashes accompany the
follow-up response. No Gradle/Kotlin/Native/javac, numerical
script/certificate/probe/test/benchmark/harness or background numerical process
was started. No dependencies/caches/services/processes/CI changed or cancelled.

Independent read-only reviewer `midpoint_review` found no production or test
issue. It required removal of an extra expected-source newline in the unrun
certificate; that was fixed. Static author follow-up also changed a mixed
Int/Long comparison to Long literals. The reviewer verified the revised
certificate matches the production block and is statically type-plausible.
Its documentation nit clarified that the bounded enumeration stays below
2^31 while symbolic endpoint metadata uses Long. This was corrected.
Final frozen-source preparation verdict is recorded in the task feed;
no reviewer compilation or numerical execution occurred.
The comment1904 follow-up changes only proof/handoff wording and their hashes;
production, both tests, and certificate retain their initial frozen hashes.
Independent read-only wording follow-up reviews the amended documents; its
final verdict accompanies the response. No task comment/state mutation or
parent notification resend is
authorized or performed in this follow-up. Parent read the initial handoff
directly from the task feed, resolving the earlier transport issue.
Parent must explicitly transfer the sole execution window before any compilation
or execution. Future required gates retain all existing roots, returned-
representation, callback and whole-string oracle tests, run focused new common
tests on JVM/JS/Wasm/macARM and bounded JVM comparisons, then integrated all-six
CI on the final parent head. Do not infer passing tests from this handoff.
