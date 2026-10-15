# Exact e36 large hosted verification

The original [large workflow run 38810284447](https://github.com/Heapy/krogu-math/actions/runs/38810284447),
attempt1 at `e36c1ff93c5a7809f43565960c37bd2221ebc938`, completed SUCCESS.
Same-run producer116681505418 and consumer116682035650 both succeeded. Direct
inspection of the complete retained ledger confirms **12 PASS /0 FAIL /128 SKIP**;
job status alone is not the basis for that result.

This is a finite metadata-only audit. Existing API metadata, both original ZIPs,
both raw job logs and the exact-ref own source archive were downloaded into
`/private/tmp/krogu-e36-large-evidence-ntxvlcYo`. JSON/text/path comparisons and
hash checks were used. No compiler, script, numerical/model/helper, probe, test,
build, process inspection, javap, workflow dispatch/retry, Git or tracker
operation was executed. No foreign numerical implementation bodies were read.
Only this new publication document and the private evidence directory are owned.

## Complete ledger and retained scope

The ledger has140 unique semantic records, keyed by phase/id/path/scope/
directive/method. Its118 public-invocation IDs exactly equal both phases of the
59-plan inventory. Each phase has six PASS and64 SKIP. All12 selected primary
compilations and executions have exit0, timedOut=false and
caughtOomAssertionsIncomplete=false. Every selected stdout has its exact unique
`HARNESS_COMPLETE <phase> <class>` marker once; all74 recursively referenced
stdout/stderr/class-load paths exist. There is no hidden timeout, caught-OOM
incomplete result or missing completion accepted as PASS.

The128 SKIPs comprise106 public invocations outside the selected large profiles
(90 standard,10 heavy,4 serialization,2 memory-512m),16 upstream-disabled methods,
two upstream-commented calls and four excluded private-support records. There
is **no selected-large capacity SKIP**. Complete reasons, method keys and source
paths remain in results.json. Nonselected adaptations are not exercised: the
manifest still declares their constructor/provider/storage/prime/sqrt coverage
losses, including the separately classified sqrt input10 /p1073741825 /UP call.
Their absence from this large-only execution is not a pass for those assertions.
The six selected large plans use imports-only test adaptation.

Actual execution durations are retained observations, not benchmarks:

| Invocation | Baseline | Port |
|---|---:|---:|
| BitLengthOverflow |116ms|179ms|
| DoubleValueOverflow |89ms|97ms|
| LargeValueExceptions |27,848ms|796ms|
| DivisionOverflow |502ms|5,378ms|
| StringConstructorOverflow |3,673ms|3,765ms|
| SymmetricRangeTests |25,751ms|34,688ms|

Port SymmetricRangeTests prints every retained section through byteValueExact
and its completion marker; its stderr is empty. The only nonempty primary test
stderr files are baseline/port LargeValueExceptions: both contain the same three
SLF4J no-provider/NOP warnings,186 bytes each. Full originals remain unredacted.

## Original regime and actual guards

The raw command is unfiltered `--both`, exactly profiles
large-memory-4g,large-memory-8g,large-memory-10g, maximum heap8192MiB, physical
memory `auto`, run/source attestation and owned-timeout cleanup. Seed559,
assertions and `-XX:-OmitStackTraceInFastThrow` are present on all12 child
commands. Both subset metadata fields remain0. Gradle9.8.1, Kotlin2.4.21 and
BellSoft25.0.4+9-LTS are recorded. The runtime metadata command itself exited0.
The pinned tests tag jdk-25.0.4.1-ga versus runtime revision/vendor mismatch
remains explicit, with no source-equivalence claim.

The six large inventory plans compare byte-identically as JSON metadata with
the previously audited80987 large plans. Five retain120-second deadlines;
SymmetricRangeTests retains180 seconds. LargeValueExceptions uses4GiB;
BitLengthOverflow/DoubleValueOverflow use8192MiB; DivisionOverflow and
StringConstructorOverflow retain `-Xshare:off -Xmx8g`; SymmetricRangeTests
retains `-Xmx8g -XX:+CompactStrings`. Neither timeout nor heap/input/assertion
was reduced for this audit or this evidence interpretation.

All12 fresh per-child memory gates were required and passed:4608MiB for the two
4GiB executions and8704MiB for the other ten. Initial physical total/free bytes
were16766414848 /11693699072; fresh free ranged11652726784–11868774400. The
earlier auto-selection snapshot was16766414848 /11611951104. These host snapshots
and completed cold executions do not guarantee future capacity, an uncontended
host, practical maximum-domain support or performance.

The script JVM is bounded at256MiB, with a separate campaign cache and setup's
compiled-script caching disabled. The workflow retains150-minute step and
180-minute consumer outer bounds. Producer ran21:29:43Z–21:30:54Z and consumer
21:30:56Z–21:33:26Z on2026-10-15. Those are API job times, separate from the table.

All six port records have baselineStatus=PASS and coreLoadingVerified=true.
Their six retained caller audits exited0 without timeout, direct host-math
members or integration-bridge members. A text-only scan of those public-test
caller dumps found no java.math core-type members. The24 CORE_CLASS observations
contain exactly six of each own core type, all from the same input JAR. No
bytecode inspection command was rerun by this audit.

## Same-run source and artifact binding

Producer artifact12007967692 is
`public-math-large-jvm-e36c1ff93c5a7809f43565960c37bd2221ebc938-1`,129115 bytes.
Consumer evidence artifact12007918183 is
`public-math-large-evidence-e36c1ff93c5a7809f43565960c37bd2221ebc938-1`,1152007 bytes.
Both original ZIP hashes match their API digests. Both included SOURCE_COMMIT
files equal e36; their SHA256SUMS verify. Producer, consumer and the separately
accepted local584 JAR match exactly:
`df3714cff6507cf2eef102bade9aecf2319384873d16db1b1259b0e74fb030a0`.

All234 Kotlin/Java files and the exact path set in the own source archive match
accepted manifest
`37784f5a24d8e959795b9c708897f5486b13159c11b6817be85986910f9ff6f2`.
The234-OK check output hashes to
`b47bc16181197665874348285514819f6ba36945be00d57749727700cd2c1db4`.
This includes582 and584, with BigDecimal a925bf9144044fe23e4bb618b6777b7f2f90d1f591d9a0856f3651bcaaaf3e50
and BigInteger66bb59deeeceb4f664681f99ddacc490c3054b5c42c539d3b8a3a44ed2ddb7c2.
The private583 candidate is not included.

All57 upstream TEST/support source hashes,53 generated source hashes and12
actually compiled primary source hashes verify against retained bytes. All13
owned tool-source pins verify against both the exact source archive and the
artifact copies. Entrypoint SHA-256 is
`8383d062feac7a3b2e8eda8f9e2844f7d2dbe6ac8d4e5c575f3fc92f58ad1265`;
runner is `ed32beb00f4c6896cfd783f2286f95336fee3cc2616420b9a83687c4c5f32ea0`.
The source lock remains
`03493e7b75110b9a709e126e5547bbe606b6a27f0fc2f0e9ac8747188af52e64`.
Results ciExecution agrees with the API run, attempt, repository, source and
workflow identity.

The consumer evidence has290 files. Its17 third-party notice files and exact
relative path set are byte-identical to the approved notice manifest
`74a6769a33892aa21031cf5240ea145d3395d8b4df1e962f6a964e9d390fed1c`.
All seven retained POM hashes and14 dependency-lock rows match the results
metadata; lock SHA-256 is
`85bae4a80af6f3e3b866c903a4d2ea2914f90d5f2b10486ea271e117430003fb`.
The GPL public tests/adaptations, compiled public tests, harness sources and
third-party notices belong to test-only evidence. The Apache-2.0 own library
JAR has50 ZIP entries consisting of own package classes and metadata; no
upstream test/harness source/class or third-party implementation is packaged
inside it. This metadata boundary does not relicense the retained test sources.

## Exact retained evidence and remaining gates

All paths below are relative to the private audit directory unless shown otherwise.
The actual result root is `evidence/build/math-tck/results/1792099885818`.

| Evidence | SHA-256 |
|---|---|
| `producer.zip` | `aed62e2004a401613342e56872849b2e8cf52804852ec18698e11708bcaa1441` |
| `evidence.zip` | `c5bac0101fc387d7e13806304b91d9c90363dca9a45a0d66696f2e8ffc0d30f2` |
| `source.tar.gz` | `c0fd6d1f606b8d10142fc82aa18d2c8d7c18ef0d788de4eeb59e5aad29772f4a` |
| Results JSON | `5028a472d2d77d4df5ad1e37d97708aab12305f041319a572e79bca4de885829` |
| `evidence/build/math-tck/inventory.json` | `66092666ff134e3b1572626fc77389b45705b43214fa1b571c7d0c55fb064fae` |
| `consumer.raw.log` | `d970d6856982925b83e015032f34363ce62d2d3a9df76bfd61d9a77f5343149f` |
| `producer.raw.log` | `7b2910d47386834ae3f3f9410d52143823da8b2b02ea745964c1abfbba317403` |
| `ledger-audit.json` | `00d3f662e7c81ffdff5b3e64513789e00a513954a4b4e70d6546989143c34ebf` |
| `guard-and-timing-audit.json` | `94e28b6bbf64b48acf788409dd45c23d6a6623da9de6012bb0caeb69ceb65613` |

The final private index is generated after this report, excludes itself/check
outputs and is supplied with its verification count/hash in the finite handoff.
An initial metadata-only comparison used an incorrect prior inventory path and
produced an empty projection. It is retained; the actual prior path was then
located and the six-plan comparison completed successfully without numerical
execution or a hosted retry. All downloads and metadata calls are terminal.

The separate [normal CI run38810176702](https://github.com/Heapy/krogu-math/actions/runs/38810176702)
was still in_progress with no conclusion in the retained API snapshot. This
report does not combine it with the large ledger into118 same-source passes.
Historical regular failures and adaptation losses remain their own evidence.
Private583 runtime, broader548 resource/range proof, subtype audits and remaining
compatibility work are not established by these12 passes. This is not official
JCK certification, all-input mathematical proof or performance acceptance.
