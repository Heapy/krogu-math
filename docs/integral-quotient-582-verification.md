# Integral quotient 582: fresh local verification

The frozen quotient implementation and its separately reviewed finite consumer
successor completed all four local math selections: **1,559 method executions,
zero failures, errors, skips, missing/extra methods or duplicate method keys**.
All **1,684 selected inherited-dispatch diagnostic records** are present and
match their required recorded numeric triples. This is a local verification
milestone for quotient-only582; full integration and matching hosted gates remain
separate.

## Preserved chronology and exact source

The first582 campaign remains failed: JVM156 suites /529 methods, two failed
methods in `DecimalIntegralDispatchOracleTest`; the other three targets were
UNRUN. Its inherited method reported160 mismatches but displayed only12, so the
remaining148 actual observations remain unknown. The metadata method displayed
one mismatch, key `[3,2,1,1]`. The original four common and one JVM582 tests
passed in that campaign. The immutable first-failure capsule is
`/private/tmp/krogu-582-validation-KPmhL8e0/initial-failure-capsule.tar`, SHA-256
`dca7c14a27638f77e4f54b20b0184c94dc0531075712bf4e49f851ec27f52bf5`.
No fresh record below reconstructs those unprinted historical observations.

The fresh root is `/private/tmp/krogu-582-v2-validation-G4SM9xrm/root`, prepared
by the parent from archive `da608` plus seven frozen582 files and four finite
consumer files/their handoff. Its exact233-source manifest and source path set
are bound below. Relative to the first campaign's231-source manifest, only
`DecimalIntegralDispatchOracleTest.kt` changed; the finite diagnostic policy and
separate caller-contract test were added. All production source hashes stayed
identical. The two campaigns' own JARs are byte-identical as well.

Preparation-time UNCOMPILED/UNRUN statements in the implementation and consumer
handoffs remain historical. This report supplies later execution evidence for
their frozen sources, without changing those documents or their original scope.

| Input or evidence | SHA-256 |
|---|---|
| `BigDecimal.kt` | `00e392444ce4b13122ec6dcb7ccbee357b437da1e6554cc78876e909346c1599` |
| `BigInteger.kt` | `66bb59deeeceb4f664681f99ddacc490c3054b5c42c539d3b8a3a44ed2ddb7c2` |
| `DecimalDivision581.kt` | `00557b2517c70f9294ed1be484177962c441841334585cba14ac7a94c647fa6f` |
| `DecimalIntegralQuotient582Test.kt` | `f3736aae2cca21147dbb878a64bef77277746b899a9b88653ec08242d0b24219` |
| `DecimalIntegralQuotient582SubtypeJvmTest.kt` | `688710730bc63cdb0c6940f78e19958295c02af550366a007f2dfa48c6db45f0` |
| `DecimalIntegralDispatchOracleTest.kt` | `ae3c8ec204fc3b077c9d3b9c59c400747e28fe9c16f2ac429dbe2b7ced643d43` |
| `DecimalIntegral582DiagnosticPolicy.kt` | `ce9751b8576299fe0bc4324023c79363c01a50308212acd8f5d9baa450a669ab` |
| `DecimalIntegral582CallerContractTest.kt` | `ab7a04d738e8084b9e20ca7ddc90be83ccbdd32961d480bdf5dff5ea24ededf8` |
| `evidence/source-pre.sha256`,233 entries | `9860ba6dbbc671d3bb2ef2092d987a545e1664feb8a48960ed5556ba3da70dc0` |
| `evidence/source-post-check.log` | `a798d662b2f97ad1821c27ab28fb1293aeea2065ceb8795c619dfafd00faf817` |
| `build/libs/krogu-math-jvm-0.1.0-SNAPSHOT.jar` | `79c77769a8e85d8914a68c0f3e6f796a3b326d8a13a8d9e0db1c21017f0931f4` |

Within that retained fresh root, the independently reviewed source arguments
remain in `docs/integral-quotient-582-implementation.md`, SHA-256
`150c94a9e895e37ff5bf2bf1158a5d04cc1b3c83a875ed4e679f46dcbf85e70f`,
and `docs/integral-582-consumer-policy.md`, SHA-256
`b2217d41e2fc6f211f0c5bf913cd7c1f1ba650b5ca33d99cb8ac8ac2713310cc`.
Their frozen handoffs have hashes
`7c974bdfe508cfe6f747472cbd11a7d3083f32610937deff70dbb5ffcbe68b97`
and `17e9af4da6760a667d9d4ee788200f8d142225223c67b0533391402ac483f1f5`,
respectively.

## Actual command, inventory and artifact

`evidence/command.txt` records Gradle9.8.1 with BellSoft25.0.4, one worker,
`--no-daemon`,4GiB Gradle heap and the existing pinned Kotlin user home. It
cleared inherited Java/Kotlin option/classpath variables and selected `jvmJar`
plus `jvmTest`, `jsNodeTest`, `wasmJsNodeTest` and `macosArm64Test`, each with
`--tests 'io.heapy.krogu.math.*'`. Parent session47553 exited0; the retained log
ends `BUILD SUCCESSFUL in 14m 18s`,49 tasks executed. Warnings remain in the raw
log. Build duration is not a performance claim.

| Target | XML files / suites | Methods | Failures / errors / skips |
|---|---:|---:|---:|
| JVM |157|530|0 /0 /0|
| JavaScript/Node |74|343|0 /0 /0|
| Wasm/Node |74|343|0 /0 /0|
| macOS/ARM64 |74|343|0 /0 /0|
| Total |379|1,559|0 /0 /0|

The source-derived XML auditor ran separately as parent session86709, exit0.
Its source SHA-256 is
`ae57b2795b7da0d1fde5ae1e0e8a8bd1e068702ef213e07f1a35d6ae4923e5b4`;
the initial-to-successor diff changes only preparation comments, manifest pin
and source count. It preserves exact source ownership and method extraction,
complete multiset comparison, secure XML parsing, suite/testcase consistency,
duplicate/nonpass rejection and pre/post source checks. `issues.tsv` has only
its header. Independent metadata inspection rehashed all233 sources and checked
the complete path set, all379 raw XML suite counters/elements, all1,559 raw
class/method pairs and normalized expected/actual multisets. There were no
omissions or duplicate keys.

The four common quotient methods and the one JVM current-format coefficient
subtype method remain byte-identical to the original milestone. The new common
caller method adds one execution per target and preserves explicit context
override argument identity, returned alias, exception identity and independent
caller-super context/integral numerical controls. The policy does not turn
inherited callback counts or callback absence into normative requirements.

## Complete selected diagnostics

Each target's actual `DecimalIntegralDispatchOracleTest` XML contains exactly
421 `INTEGRAL_582` rows:420 dispatch rows plus metadata ordinal804. Non-JVM
filenames include their actual target prefix, for example
`build/test-results/jsNodeTest/TEST-jsNodeTest.io.heapy.krogu.math.DecimalIntegralDispatchOracleTest.xml`.
The parent initially attempted three incorrect non-JVM filenames; corrected
metadata reads used the actual inventory. This was not a test failure or retry.

Selection covers all seven sources, three nonzero divisors, both no-context/
unlimited selectors and all ten replacement modes, before inspecting outcomes.
All420 keys and original full diagnostics match the frozen literal ledger;
ordinal and uniqueness gates are complete. The single metadata row retains its
full old exception/trace diagnostic. The remaining980 dispatch and1,175 metadata
rows retain their old exact assertions. No fixture or generator changed.

Every selected row retains the complete old and current diagnostic, both
classification fields, actual coefficient/scale/canonical-text triple and the
independently required triple. All actual/expected triples match and none is
null. The executed tests supply the independent bounded Long rational/cohort
oracle; this metadata review compared recorded fields without rerunning that
oracle, JDK mathematics or production arithmetic.

The four full421-row record files are byte-identical. Per target,260 historical
diagnostics are unchanged and161 changed:160 dispatch rows and one metadata row.
Full transition counts and all1,684 current records are retained. These counts
describe this new run; they do not assign unseen initial outcomes. The selected
inherited internal `divide` recipe is classified reference-only while required
stored-value quotient results remain asserted. Direct caller override/super
obligations remain active; there is no all-family subtype waiver.

## Durable evidence and limits

Evidence is under `/private/tmp/krogu-582-v2-validation-G4SM9xrm/evidence`:

| File | SHA-256 |
|---|---|
| `four-target.log` | `920280b97e566a19b5709df2539652a5baf376670f0b948d05f4a7fd497b21e3` |
| `xml-audit/summary.tsv` | `e18efaff26be93bc1fe70ac3aab8d56cc9d8a89c6726479209c55df45c31df12` |
| Parent `evidence-capsule.tar`, read-only | `c46fbfac18c3003c6ae8004a4d54a9d6cee6b1462e1cfc53f2f229bfff165cef` |

The independent metadata audit directory is
`/private/tmp/krogu-582-v2-independent-audit-O8o9TqcO`; it retains the decoded
system-out, every selected record, full transition counts, raw XML/method
accounting, source checks and an index of exact evidence inputs. `AUDIT.md` has
SHA-256 `309b3671cae18856826ec96c8940d4cf379768772d275957183051bd2c52fb83`;
the700-entry `index.sha256` is
`fe401b67fe431b4d6d0530f3792c28772b75866790d138dff95fdb607a65ae1b`,
and all700 entries verified successfully. Neither frozen capsule nor any
test/source/evidence file was edited by this review.

The parent reported both build/auditor invocations terminal, explicitly released
the582 window and recorded an empty mutex at revision804. This reviewer used
retained metadata only and did not inspect live processes or acquire a runtime
window. The original failed campaign remains failed. This result concerns
quotient-only582: the separately confirmed583 finite-negative-scale admission
and pair/remainder product obstruction remains open; private584 preparation is
not execution evidence. Outside-Int fallback, broader541/526 subtype and548
range composition, publication integration and full hosted gates retain their
own requirements. This report claims neither complete public TCK/JCK passage,
all-input support from test counts, giant-witness execution nor resource or
performance acceptance.
