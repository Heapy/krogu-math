# Exact f58 large hosted verification

Original [run38813535671](https://github.com/Heapy/krogu-math/actions/runs/38813535671),
attempt1 at `f58d8651b44e45e190e96e10f9ff587b52720f56`, completed SUCCESS.
Same-run producer116692430979 and consumer116692768276 both succeeded.
Independent inspection of the complete actual ledger confirms
**12 PASS /0 FAIL /128 SKIP**. This is a large-only result, not a conclusion
about the separate normal CI, official JCK or the full supported mathematical
domain.

Audit workspace: `/private/tmp/krogu-f58-large-evidence-HUDb7OMN`. Only this
private directory was written. Existing API metadata, original ZIPs, both raw job
logs and an exact-ref own source archive were downloaded. Direct CLI JSON/text/
archive/path/hash checks were used. No script, compiler, numerical/model/helper,
probe, test, build, process inspection, javap, workflow dispatch/retry, Git,
tracker or repository mutation occurred. No foreign numerical implementation
source/body/bytecode was read.

## Complete actual execution

The complete ledger has140 unique semantic records keyed by phase, id, path,
scope, directive and method. Its118 public-invocation IDs exactly match both
phases of the59-plan inventory. The12 actual primary IDs exactly match both
phases of the six large plans. Each phase has six PASS and64 SKIP. All12 primary
compilations and executions have exit0, timedOut=false and
caughtOomAssertionsIncomplete=false. Every primary stdout contains its exact
unique `HARNESS_COMPLETE <phase> <class>` marker once. All74 recursively
referenced stdout/stderr/class-load paths exist.

The128 SKIPs comprise106 public invocations outside the selected profiles
(90 standard,10 heavy,4 serialization,2 memory-512m),16 upstream-disabled
methods, two upstream-commented calls and four excluded private-support records.
There is no selected-large capacity SKIP, accepted timeout, caught-OOM incomplete
PASS or missing completion. Full semantic records/reasons remain in results.json.
Nonselected manifest adaptations retain their explicit constructor, Float16/
provider, storage, prime and sqrt coverage losses; this large campaign does not
execute or recover those assertions. The six selected large plans are
imports-only adaptations, with their original baseline tests retained.

Actual primary execution times are observations, not benchmarks:

| Invocation | Baseline | Port |
|---|---:|---:|
| BitLengthOverflow |81ms|131ms|
| DoubleValueOverflow |61ms|68ms|
| LargeValueExceptions |17,336ms|460ms|
| DivisionOverflow |343ms|3,394ms|
| StringConstructorOverflow |1,940ms|2,189ms|
| SymmetricRangeTests |13,940ms|20,000ms|

Port SymmetricRangeTests reaches byteValueExact and its completion marker;
its stderr is empty. Only baseline and port LargeValueExceptions have nonempty
primary stderr:186 bytes each, identical three SLF4J no-provider/NOP warnings.
The raw warnings match the retained e36 run. All original logs remain unredacted;
there are no FAIL records to omit.

## Unchanged regime, capacity and origin guards

The raw CI command is unfiltered `--both`, exactly profiles
large-memory-4g,large-memory-8g,large-memory-10g, max heap8192MiB, physical
memory `auto`, run/source attestation and owned-timeout cleanup. The six large
plan metadata records are byte-identical to e36. Five deadlines remain120s;
SymmetricRangeTests remains180s. LargeValueExceptions uses `-Xmx4g`;
BitLengthOverflow and DoubleValueOverflow use `-Xmx8192m`; DivisionOverflow
and StringConstructorOverflow retain `-Xshare:off -Xmx8g`; SymmetricRangeTests
retains `-Xmx8g -XX:+CompactStrings`. Actual primary commands match these
flags. Assertions, seed559 and `-XX:-OmitStackTraceInFastThrow` are present on
all12 children. Both recorded subset fields are0. No input, assertion, resource
profile, timeout or heap was reduced.

All12 fresh memory gates were required and passed:4608MiB for the two4GiB
executions and8704MiB for the other ten. Initial physical total/free bytes were
16766414848 /11691565056; fresh free ranged11686641664–11882622976. The earlier
auto-selection snapshot was16766414848 /11558637568. These per-child host
observations do not guarantee future capacity, full-range allocations or
uncontended-host timing. No performance acceptance is inferred.

The retained toolchain is Gradle9.8.1, Kotlin2.4.21 and BellSoft25.0.4+9-LTS.
The runtime metadata command exited0 without timeout in36ms. The pinned public
tests tag jdk-25.0.4.1-ga versus runtime revision/vendor mismatch remains explicit.
The script JVM is bounded at256MiB, has a separate campaign cache and setup's
compiled-script caching disabled. Workflow outer bounds remain150 minutes for
the step and180 minutes for the consumer. Producer ran21:56:01Z–21:56:50Z;
consumer ran21:56:52Z–21:58:31Z on2026-10-15. Those API job times are separate
from the per-invocation table.

All six port records have baselineStatus=PASS and coreLoadingVerified=true.
Their six retained public-test caller audits exited0 without timeout, host-math
members or integration-bridge members. A text-only scan of the retained caller
dumps found no java.math core-type members. Each port stdout has four distinct
own CORE_CLASS entries; the24 entries contain exactly six of each core type,
all from the same own input JAR path. The audit did not execute javap or class
loading anew.

## Same-run source and artifact binding

Producer artifact12008774549 is
`public-math-large-jvm-f58d8651b44e45e190e96e10f9ff587b52720f56-1`,133351 bytes.
Evidence artifact12009676961 is
`public-math-large-evidence-f58d8651b44e45e190e96e10f9ff587b52720f56-1`,1154443 bytes.
Both ZIP hashes match their API digests. Producer/consumer SOURCE_COMMIT equals
f58; both included SHA256SUMS verify. Producer, consumer and accepted local583
JAR bytes all match
`391b73ee1923851773cd8e65bb8ebad4674b47a2e98c5eb49feeefdb45566420`.

All236 source hashes and the exact Kotlin/Java path set match manifest
`10bff57aa852664b35a73c9e8e7d35d0f4be870181008c7dd57bd5c82c504551`.
The downloaded exact-ref source archive and current publication source tree
both passed the complete manifest/path-set check. Both236-OK check outputs
hash to `3c7ee5cf4ae2cfbf7c206943107b95c5001123fc4003245d0b05a909899e2bb3`.
This source includes the accepted583 residual repair plus582/584. It is distinct
from e36's234-source/JARdf3714 evidence; neither earlier source nor result is
relabelled. The selected large tests do not by themselves supply new coverage
of the separate tiny contextual-decimal endpoint matrix.

All57 upstream TEST/support hashes,53 generated source hashes and12 compiled
primary source hashes verify. All13 owned tool pins verify against both the
same-run artifact and exact source archive, and are byte-identical to e36.
The entrypoint, runner and large workflow are also byte-identical to e36:

| Input | SHA-256 |
|---|---|
| `tools/run-math-tck.main.kts` | `8383d062feac7a3b2e8eda8f9e2844f7d2dbe6ac8d4e5c575f3fc92f58ad1265` |
| `tools/math-tck/runner.main.kts` | `ed32beb00f4c6896cfd783f2286f95336fee3cc2616420b9a83687c4c5f32ea0` |
| `.github/workflows/public-math-large.yml` | `25d97734ca350b2cd4fd70750964be4549ba70b22246ec641954bfb9c0badb61` |
| Source lock | `03493e7b75110b9a709e126e5547bbe606b6a27f0fc2f0e9ac8747188af52e64` |
| Dependency lock | `85bae4a80af6f3e3b866c903a4d2ea2914f90d5f2b10486ea271e117430003fb` |

All14 dependency-lock rows match results metadata and all seven retained POM
hashes verify. The17 notice files and exact relative path set match approved
notice manifest
`74a6769a33892aa21031cf5240ea145d3395d8b4df1e962f6a964e9d390fed1c`.
The consumer evidence has290 files. The library JAR has51 ZIP entries, all own
package classes and metadata; no upstream test/harness class/source or third-party
implementation is packaged inside it. GPL public TEST/support/adaptation and
compiled-test/harness evidence remains test-only, with its original licensing;
the own implementation remains Apache-2.0. No relicense or foreign implementation
source inspection follows from this metadata boundary.

## Exact retained evidence and limits

Actual results are at `evidence/build/math-tck/results/1792101438747/results.json`
inside the private audit directory. API run/attempt/source/repository/workflow
identity agrees with results ciExecution and producer/consumer identity.

| Evidence | SHA-256 |
|---|---|
| `run.json` | `03e8c3c3aeffc6c48dc5016caba0c8a65c3c6af79f8d4e8297566ff5b1c0cf3b` |
| `jobs.json` | `5bbd336ade92079752ca960d58571e2a4024c55d01283f51f81c272c51038ef3` |
| `artifacts.json` | `09afe01211d55ebb31cd7b5d81c47cad92560bc1327d892f8c81ca868efe2ce6` |
| `evidence.zip` | `cf5173f1a622c8da5ae82dda563cf659d28b89dd17fddd612cb1042389ed92c8` |
| `producer.zip` | `2b5a4a71c71a8a9b3dacfa61dd4fedd40c2a41901536cacf20939f1f0cb0f997` |
| `source.tar.gz` | `9bf0eb55592e0b0b0dbacfa528073d099675c596da44d8599739077b641b1fd9` |
| Results JSON | `e5476c5fcdc3680843b2a0091c4e7d20e26e8ab6009ebb2c752d1c339f5d4326` |
| Inventory JSON | `19078f9d1dd458cf8e87b7b07ebe279ca6aeb67ae0829d39e2f1fb05184ebd60` |
| `consumer.raw.log` | `e59b9b29c0a7518311e5bc855e3b2a0b4a326162d5ae358434e139302c386917` |
| `producer.raw.log` | `8b3f5220f66e2d2930cbea2bba8ef8bea7cf0c1b2f68b63c30d03d304ef430dd` |
| `ledger-audit.json` | `7a939febff80bea761c5feeadce4180b4d3429ce833c05465304e6a7feb71dda` |

The frozen evidence index contains935 entries, verifies without errors, and has
SHA-256 `df1f5865b62c9f17baaad1c13683adcc3e9965ec95cc50f2648efd3b3c66a197`.
A first metadata-only
tool-pin check started before its generated hash list existed; its empty output
is retained. The subsequent ordered checks verified all13 source and artifact
tool files. This was not a numerical failure or hosted retry. All downloaded
originals, raw outcomes and metadata checks remain preserved.

No normal-CI XML/result audit was performed here. This large-only ledger cannot
be combined into118 full same-source public passes or official JCK certification.
Broader548 resource/range proof, general subtype obligations, tiny-decimal
matrix coverage, practical maximum allocation and performance remain separate.
This report records the accepted finite metadata audit; broader task disposition
remains separate. All owned calls are terminal, with
no background campaign or future-window waiting.
