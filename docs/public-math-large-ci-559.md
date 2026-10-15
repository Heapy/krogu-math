# Separate large-memory public math workflow (559/548, root2288)

The original worker handoff was a finite source-only milestone. The workflow and
changed entrypoint were UNCOMPILED/UNRUN at that handoff: no script, compiler, helper, numerical
operation, test, build, probe, Git mutation, tracker mutation or hosted dispatch
was performed. Root owns independent review, preflight, publication and a later
separately authorized workflow dispatch. Existing eaa evidence is unchanged.

## Parent metadata preflight

After independent source review, root scope 2289 compiled and ran the changed
entrypoint once in metadata-only `--plan` mode, using a fresh isolated source
snapshot/cache, Kotlin 2.4.21, Liberica 25.0.4+9 and a 256MiB script heap. Session 86633
terminated with exit0. No javac, Gradle, caller audit or numerical test was run;
`testsExecuted` is zero. The local compiler/script window was then released.

The inventory contains exactly 59 directives: six PLANNED and 53 profile SKIPs.
The six paths, 4096/8192MiB heaps and five 120s/one 180s deadlines match the table
below. Automatic observation recorded 38654705664 total bytes and 173719552 free
bytes on the local host. PLANNED means selection eligibility only: this preflight
did not perform the fresh-free execution gate or claim that a large JVM could
run on this host. Its summary also retains PASS 0/FAIL 0/SKIP 0/NOT_RUN 53 for the
prepared source inventory, separately from the directive plan.

Evidence is retained at `/private/tmp/krogu-559-large-preflight-v1`:

* `root/build/math-tck/inventory.json` SHA-256
  `2177e23e89d6ec05b92cc21988063c161d10064e8a16f2b67d182155c030449a`.
* `preflight.log` SHA-256
  `7010f02548206d9bff5f42969ba8819e5a90bc37fa87c2cad1a8dc9007d578f3`.

The reviewed workflow 25d97734 and entrypoint 11e5ddd8 source hashes remained
unchanged. Hosted compilation/execution and actual large-profile coverage are
still unverified at this publication-preparation stage. Two exploratory jq
display commands used the wrong object/array shape and failed; corrected
metadata reads inspected the original inventory without rerunning the script.

## Why the separate workflow exists

The [accepted regular CI report](public-math-ci-559.md#hosted-verification)
records seven successful jobs and 106 PASS/46 SKIP at
`eaaea5ab29114950ac752afff2bf065bbf6035d5`. Twelve of its public invocation
records were skipped because the large profiles were unselected. Its recorded
host total/free memory did not establish a capacity failure for those records.
This source change prepares a separate attempt; it neither converts those
historical SKIPs to passes nor promises the next host has the same capacity.

`.github/workflows/public-math-large.yml` has only `workflow_dispatch`, with no
push, pull-request, schedule or workflow-call trigger. Per-ref concurrency queues
rather than cancels an active manual run. The workflow requires only
`contents: read`; there are no secrets or write-token permissions. Publication
does not itself request a large numerical run. GitHub documents the
[manual/default-branch workflow requirement](https://docs.github.com/en/actions/how-tos/manage-workflow-runs/manually-run-a-workflow)
and [dispatch source-ref semantics](https://docs.github.com/en/actions/reference/workflows-and-actions/events-that-trigger-workflows#workflow_dispatch).

## Same-run own-library artifact

The producer job `jvm-jar` uses the repository's pinned checkout, Liberica
25.0.4+9 and Gradle-setup actions, then the checksum-pinned Gradle 9.8.1 wrapper
to request only `jvmJar`, with one worker, no persistent daemon and a 4GiB Gradle
heap. This does not run the math test tasks. The consumer runs on a separate
Ubuntu 24.04 job, so the producer's compiler heap is not resident on its host.

After building, the producer checks HEAD against `GITHUB_SHA`, copies
`build/libs/krogu-math-jvm-0.1.0-SNAPSHOT.jar` to the canonical
`build/public-math-large-input/krogu-math.jar`, writes `SOURCE_COMMIT`, and hashes
both files into `SHA256SUMS`. The pinned upload action publishes
`public-math-large-jvm-${{ github.sha }}-${{ github.run_attempt }}`. The consumer
depends on that producer and downloads exactly that name from the current
workflow run, then checks its own HEAD, source-commit record and both checksums.
There is no Maven/download fallback, stale local archive search or consumer
rebuild. An archive-version change fails until its explicit path is updated.

The attempt suffix keeps separately built attempts distinct. Re-running the
consumer alone without a same-attempt producer artifact fails closed; a later
retry must include the producer, not substitute another attempt's artifact.
These are workflow provenance and byte-identity checks, not signed artifact
attestations. Existing harness JAR-digest, exact-core-loading and caller-bytecode
guards remain active. See GitHub's
[same-run artifact contract](https://docs.github.com/en/actions/tutorials/store-and-share-data).

## Exact invocation and distinct CI attestation

The consumer uses the same pinned setup-main-kts action as regular CI, Kotlin
2.4.21 and Liberica25.0.4+9. It disables shared compiled-script cache restoration
and uses a runner-temporary isolated cache outside evidence. Its script JVM is
limited to 256MiB. The exact harness selection is:

```sh
env -u JAVA_TOOL_OPTIONS -u JDK_JAVA_OPTIONS -u _JAVA_OPTIONS -u KOTLIN_OPTS -u JAVACMD -u CLASSPATH \
  kotlinr -J-Xmx256m tools/run-math-tck.main.kts --both \
  --port-jar "$PWD/build/public-math-large-input/krogu-math.jar" \
  --profiles large-memory-4g,large-memory-8g,large-memory-10g \
  --max-heap-mib 8192 --os-memory-gib auto \
  --ci-execution "$GITHUB_RUN_ID:$GITHUB_SHA" \
  --allow-owned-timeout-cleanup
```

`JAVA_OPTS=-Xmx256m` also constrains the script launcher. Shell `pipefail` and
`tee` retain the real exit status and campaign log. Both phases are complete
manifest traversals: there is no `--tests` filter, subset reduction or arbitrary
test-assertion change. Baseline/port executions remain serial on the consumer.

The entrypoint retains its existing environment/run/SHA/workspace validation
and maps the CI job to its exact workflow path and allowed selection:

| CI job | Required workflow file | Exact profiles | Maximum heap |
| --- | --- | --- | --- |
| `public-math` | `.github/workflows/ci.yml` | standard, serialization, heavy, memory-512m | 512MiB |
| `public-math-large` | `.github/workflows/public-math-large.yml` | large-memory-4g, large-memory-8g, large-memory-10g | 8192MiB |

The large job additionally requires `GITHUB_EVENT_NAME=workflow_dispatch`.
Swapping the job/file pair, using another profile set or heap, a test filter,
one-phase mode or explicit numeric memory fails CI attestation validation.
Both CI variants require automatic memory observation and owned-timeout-cleanup
authorization. Existing numeric parent-comment grants remain separate, mutually
exclusive with CI attestations, and still reject code-only comment 1753.
`executionGrantComment` remains null for CI; it does not mislabel a workflow ID
as a parent comment. Existing `ciExecution` records retain run/attempt, source
and workflow SHAs, repository, workflow ref/name and job. Environment validation
constrains this CI interface; it does not authenticate arbitrary local strings.

## Six pinned public test directives and capacity gates

Read-only inspection of the actual locked public TEST sources gives:

| Relative test path | Retained upstream requirement | Public heap | Public deadline |
| --- | --- | --- | --- |
| `BigInteger/BitLengthOverflow.java` | none | 8192MiB profile default | implicit main/default120s |
| `BigInteger/DoubleValueOverflow.java` | none | 8192MiB profile default | implicit main/default120s |
| `BigInteger/LargeValueExceptions.java` | 64-bit and total memory >=4GiB | explicit `-Xmx4g` | TestNG/default120s |
| `BigInteger/largeMemory/DivisionOverflow.java` | 64-bit and total memory >8GiB | explicit `-Xmx8g` | main/default120s |
| `BigInteger/largeMemory/StringConstructorOverflow.java` | 64-bit and total memory >8GiB | explicit `-Xmx8g` | main/default120s |
| `BigInteger/largeMemory/SymmetricRangeTests.java` | 64-bit and total memory >=10GiB | explicit `-Xmx8g` | explicit main180s |

The two implicit-main tests have no `@run` or `@requires`; the existing parser
retains their main fallback. Each of the other requirements is a single flat
parenthesized conjunction. Existing `mathResourceBarrier` removes the outer
parentheses, splits on `&`, trims predicates, checks the exact 64-bit predicate,
and parses the remaining `os.maxMemory` comparison with its strict/non-strict
operator preserved. These exact six files need no parser change; this is source
inspection, not an executed parser test or proof for arbitrary jtreg syntax.
`tools/math-tck/runner.main.kts` remains unchanged at SHA-256
`ed32beb00f4c6896cfd783f2286f95336fee3cc2616420b9a83687c4c5f32ea0`.

`--os-memory-gib auto` still records the existing operating-system bean total
snapshot and retains the later total-capacity precondition. It does not assume
a fixed hosted-runner RAM size or turn total memory into a free-memory promise.
For every eligible large public test JVM, after its compile/caller audit and
immediately before launching it, the runner records a new free-memory snapshot.
The fresh gate now covers **all three** large profiles, including 4g:

* The 4g invocation needs at least 4608MiB observed free (4096+512).
* Every 8g invocation, including the 10g-total-memory profile, needs at least
  8704MiB observed free (8192+512).

Insufficient fresh capacity produces the existing explicit resource SKIP with
lost-coverage text; it does not mark the invocation passed. This gate also
applies to later locally granted 4g profile execution. It is not a new memory
gate on the small compiler/runtime-probe JVMs, nor a reservation against other
OS processes allocating after observation. OOM/timeouts remain actual recorded
outcomes; the existing swallowed-OOM failure classification is unchanged.

If every total-memory/architecture/fresh-free gate passes, the six directives
produce 12 public attempts across two phases. The 106 regular-profile invocations
are intentionally unselected here and remain explicit records; their earlier
passes are not retroactively rebound to the new JAR/run. Disabled/commented
methods and private exclusions retain their own records. Framework skips,
exceptions or resource barriers must be reported as actually observed, not
replaced by an expected all-pass count. Original tests, locked hashes, source
notices, per-process flags, seed 559, subset 0, serialization scope, bytecode
audits and completion/loading guards are preserved. Nothing in this workflow
waives assertions or proves the whole required numerical domain in 548.

The locked six source SHA-256 values, checked against existing cached source
bytes without executing them, are:

```text
4a30bce8e94fa93cda5c175bb70ba02094fd41d5114286fe379fd034feaeaf8c  BitLengthOverflow.java
b388e80a42bc4dc7b8a727b38c7e43f057cd4076a23bf1f78efad9575d050259  DoubleValueOverflow.java
abafe2950c3472667cb3b1ce1ff4463090d888c2096b738b152d64e3476c714e  LargeValueExceptions.java
8bc16fb1cc0330fa0f54dfb8ab788487168884e5f05de43d821b9e4828f2fda8  largeMemory/DivisionOverflow.java
56586c2b98402b1ef3916dc3ac38823ef55a16a6fe9145227ecd1c7e1c54f2a2  largeMemory/StringConstructorOverflow.java
da79e08429dc3072acb25e75d161943ae5cda872c7d183c487b33633039bc8a5  largeMemory/SymmetricRangeTests.java
```

## Deadlines and retained source/report evidence

The producer has a 30-minute build step and 45-minute job. The consumer campaign
step has 150 minutes inside a 180-minute job, leaving time for setup and evidence
upload. These outer limits are separate from retained 120/180-second public test
deadlines, 120-second javac/individual caller-audit limits and 30-second runtime
probe. Public test deadlines alone total 26 minutes if all 12 run. Setup,
compilation, multiple class audits, process cleanup and infrastructure delays
are additional; no worst-case aggregate completion guarantee is asserted.
The unchanged timeout cleanup targets only this runner's captured descendants.
An outer cancellation can leave incomplete evidence and never implies a pass.

An `always()` upload requests `public-math-large-evidence-${{ github.sha }}-${{ github.run_attempt }}`
containing the ignored math workspace's exact upstream/generated/baseline test
sources, dependency POMs, inventory, JSON results, stdout/stderr,
class-load logs, framework XML and caller dumps. It also includes the verified
own input JAR/source/checksum records, campaign log, exact math tooling/locks,
GPL text/licensing/provenance and this contract document. Explicit exclusions
remove every `build/math-tck/**/*.jar` and `build/math-tck/**/*.class`, preserving
the existing combined dependency/test-binary distribution boundary. The own
Apache library input JAR is separate and retained. The temporary compiled-script
cache is outside all uploaded paths. Startup failures may have only partial
evidence; `always()` cannot guarantee upload after infrastructure/job termination.
Dependency-notice text is also intended evidence, but the historical directory
name was excluded; see the source-only packaging correction below.

This is a controlled public OpenJDK test harness, not official JCK or a new
whole-corpus/full-domain acceptance. Existing vendor/test-revision disclosures
remain. The unchanged regular workflow and prior accepted eaa records keep
their original source/artifact/run scope; this candidate's source review and
metadata preflight do not replace the required actual hosted evidence.

## Actual hosted attempt: two retained port timeouts (root2295)

[Run 38762806007, attempt 1](https://github.com/Heapy/krogu-math/actions/runs/38762806007)
completed with **FAILURE** at source commit
`870575e664c4276c2d6d7610632ce638a7c872f8`. The
[producer 116518231447](https://github.com/Heapy/krogu-math/actions/runs/38762806007/job/116518231447)
succeeded; the
[consumer 116518862794](https://github.com/Heapy/krogu-math/actions/runs/38762806007/job/116518862794)
failed its campaign step and successfully uploaded its evidence afterward.
This actual attempt supersedes the earlier preflight's unverified-hosted status,
while preserving that metadata-only history above.

Result 1792079104822 contains **10 PASS / 2 FAIL / 128 SKIP**, with 140 unique
phase/path/scope/id/method keys and 118 unique public IDs. Twelve public JVMs
executed: all six baseline invocations passed; four port invocations passed and
two port invocations timed out. `complete=true` means the result loop finished,
not that the failed invocations completed their assertions. `officialJck=false`.

| Public test | Baseline | Port |
| --- | --- | --- |
| BitLengthOverflow | PASS | PASS |
| DoubleValueOverflow | PASS | PASS |
| LargeValueExceptions | PASS | PASS |
| largeMemory/DivisionOverflow | PASS | PASS |
| largeMemory/StringConstructorOverflow | PASS | FAIL: original 120s deadline, elapsed 120061ms |
| largeMemory/SymmetricRangeTests | PASS | FAIL: original 180s deadline, elapsed 180093ms |

Both failed process records retain `timedOut=true`, null exit code and
`Upstream invocation timeout`. StringConstructorOverflow stdout contains the four
own-core origin lines and no completion marker. SymmetricRangeTests records
seed 559, all subsets, the makePositive-overflow marker, then ends at
`Testing Bug 8021204`; it has no completion marker either. Both stderr files
are empty. Reaching that last marker does not prove the subsection or any later
assertion completed. There is no observed arithmetic exception or numerical
result to reconstruct from these incomplete traces. All ten PASS invocations
have their raw `HARNESS_COMPLETE` marker.

The original 4096/8192MiB heaps and five 120s/one 180s directive deadlines per phase
were retained, including subset 0 for SymmetricRangeTests and no test filter.
All twelve compilations completed successfully. All six port caller audits
completed successfully with own-core member references and no host-math member
owners. Each port stdout and class-load log records all four core classes from
the exact selected JAR, including both timed-out invocations. Source hashes for
all twelve compiled inputs match the retained source bytes.

Auto total memory was 16766414848 bytes. Every launch's fresh-free gate passed:
the smallest recorded prelaunch free value was 11759235072 bytes, above its
applicable 4608/8704MiB threshold. No large invocation was capacity-skipped.
Those prelaunch observations do not establish an uncontended host, absence of
later memory pressure, or a cause/performance ratio for the timeouts. These are
actual FAIL records, not capacity waivers.

The 128 SKIPs are exactly 106 unselected regular-profile invocations
(90 standard, 10 heavy, 4 serialization, 2 memory-512m), 16 upstream-disabled
methods, two upstream-commented calls and four private exclusions. There are
no adaptation-companion records for the selected six large files. Earlier
regular-profile passes remain in their distinct eaa run; they are not counted
as fresh passes in this large-only attempt.

CI metadata binds the manual event's run/attempt, source/workflow SHA, repository,
workflow file and `public-math-large` job; `executionGrantComment` is null.
The [same-run input artifact 11985724523](https://github.com/Heapy/krogu-math/actions/runs/38762806007/artifacts/11985724523)
and consumer input copies have matching SHA256SUMS and SOURCE_COMMIT. Both own
JAR copies hash to accepted current-production `ded66cd6...`. Runtime records
retain Liberica 25.0.4+9-LTS, Kotlin 2.4.21, Gradle 9.8.1 and seed 559.

Read-only existing-byte checks verified all 57 locked upstream sources, 53
generated-source hashes, thirteen recorded tool-source hashes, seven retained
dependency POMs from fourteen dependency records, and the unchanged manifest and
source/dependency locks. Entrypoint `11e5ddd8`, runner `ed32beb0` and adapter `45db93c7`
match the frozen executed tooling. The
[evidence artifact 11986274104](https://github.com/Heapy/krogu-math/actions/runs/38762806007/artifacts/11986274104)
contains 273 files, including exact sources, locks, GPL text/licensing, logs,
caller dumps, inventory and results. There is no JAR or class file under its
math workspace, and no compiled-script cache. Its only JAR is the separate own
Apache library input, preserving the reviewed packaging boundary.
This binary-exclusion check did not establish dependency-notice preservation;
the later packaging correction below records the missing notice tree.

The retained evidence root is `/private/tmp/krogu-870-large-ci-evidence-v1`.
This audit read only downloaded bytes and metadata; it ran no compiler, script,
numerical helper, test, build or benchmark and retried neither failure. Tasks 559
and 548 remain open: the two incomplete port paths need their own diagnosis,
reviewed repair if warranted, and separately authorized exact-artifact reruns.
No test assertion, deadline, source domain or historical FAIL is waived here.

```text
906b51c11339306b3cd804c428ecd7456e4793960ec353444f6beb5bded82ac5  results.json
8223608e9b020bb9b4edb03aa261dbeb52234cc2cafbceb388a791d6410e161d  final-run.json
b3b45689569c3315c223f7ab01f9a4aa2d2dc97104113021cac372d0d9287508  large.raw.log
94e82fa046dcac9113ef8500ad2cbae860e15527ce92c032320661d1ee8aa93e  public-math-large-campaign.log
2a12fe962ad34acd51e1119e6904e772cdaee5aa6f6430bf42804954f89ab5b4  inventory.json
ded66cd6a325b73a9a393e0fd56281d7bf63d571dab4b146e8f1cb9c0dba2fd1  krogu-math.jar
6ebc1a4377fc5aa243632ee4113eb09bb24e5f93b30300042d7b6d7ca859bc47  port/StringConstructorOverflow/test.stdout.log
25bc2d865f961a5f1fe6a68c7d293cc833e64a8c7af218ab9d6aa66fb62cbd89  port/SymmetricRangeTests/test.stdout.log
e3b0c44298fc1c149afbf4c8996fb92427ae41e4649b934ca495991b7852b855  both failed test.stderr.log files (empty)
```

## Second hosted attempt: parser witness passes, later range assertion fails

The separately authorized manual
[run 38767320539, attempt 1](https://github.com/Heapy/krogu-math/actions/runs/38767320539)
executed source `88ef623506607944aad9e17dfc713ef8495d95a4` after the
[parser range guard and its focused validation](integer-parser-range-559-proof.md).
The [producer 116533816367](https://github.com/Heapy/krogu-math/actions/runs/38767320539/job/116533816367)
succeeded. The [consumer 116534300818](https://github.com/Heapy/krogu-math/actions/runs/38767320539/job/116534300818)
failed its campaign step and successfully uploaded the retained evidence.
The event is `workflow_dispatch`; CI metadata records the same source/workflow
SHA, run/attempt, repository, workflow file and `public-math-large` job.
`executionGrantComment` remains null.

Result 1792080974229 contains **11 PASS / 1 FAIL / 128 SKIP**, with twelve executed
public JVMs, 140 unique phase/path/scope/id/method keys and 118 unique public IDs.
All six baseline invocations passed. The port passed BitLengthOverflow,
DoubleValueOverflow, LargeValueExceptions, DivisionOverflow and
StringConstructorOverflow; SymmetricRangeTests failed. `complete=true` records
completion of the result loop, and `officialJck=false` remains explicit.

| Public test | Baseline | Port |
| --- | --- | --- |
| BitLengthOverflow | PASS | PASS |
| DoubleValueOverflow | PASS | PASS |
| LargeValueExceptions | PASS | PASS |
| largeMemory/DivisionOverflow | PASS | PASS |
| largeMemory/StringConstructorOverflow | PASS, 3710ms | PASS, 3808ms |
| largeMemory/SymmetricRangeTests | PASS, 26263ms | FAIL, exit 1 after 4683ms |

StringConstructorOverflow stdout records `Overflow is reported by
ArithmeticException, as expected` followed by
`HARNESS_COMPLETE port StringConstructorOverflow`; its stderr is empty. This
is actual acceptance of that retained oversized-text witness on the new JAR.
The elapsed values are trace observations, not a performance acceptance claim.

SymmetricRangeTests stdout records seed 559, all subsets, the makePositive
overflow marker, `Testing Bug 8021204`, then
`Testing overflow in BitSieve.sieveSingle`. The pinned public test source shows
that reaching the latter marker requires the giant hex constructor assertion
to complete by its expected `ArithmeticException` catch. The process then exits
1 with `timedOut=false`, rather than exhausting its original deadline:

```text
java.lang.RuntimeException: new BigInteger(bitLength, 0, null).bitLength()=671088639
    at SymmetricRangeTests.testOverflowInBitSieve(SymmetricRangeTests.java:123)
    at SymmetricRangeTests.main(SymmetricRangeTests.java:639)
```

The diagnostic literal contains `null`, but line 122 actually calls
`new BigInteger(bitLength, 0, RANDOM)`, where `bitLength=(5 << 27)-1` and
`RANDOM` is initialized by `RandomFactory.getRandom()`. The retained support
source returns `new Random(getSeed())`, so this observed call uses a non-null
random generator. The uncaught assertion reports a normally returned value
with bit length 671088639 where this public test expected an arithmetic range
exception. It is not evidence about null acceptance. The subsequent
`nextProbablePrime()` assertion in that method was not reached; no outcome for
it or the later subsets is reconstructed from this trace. Classification of
this remaining range-policy assertion is separate work; this report introduces
no production cutoff, adapter, assertion waiver or JDK defect claim.

The command retained both phases, the exact three large profiles, auto memory,
8192MiB maximum heap, seed 559, subset 0 and no test filter. Workflow `25d97734`,
entrypoint `11e5ddd8`, runner `ed32beb0`, adapter `45db93c7`, manifest and both
locks are unchanged from the first hosted attempt. The same six paths retain
their original directives: five 120s deadlines and one 180s deadline per phase,
with 4096MiB for LargeValueExceptions and 8192MiB for the other five paths.
All twelve compilations exited 0 without timeout. All six port caller audits
exited 0 with nonempty own-core member references and empty host-math member
lists. All six port class-load logs record the four own core classes from the
selected JAR, and all six core-origin guards passed. Every PASS has its raw
completion marker; the failed invocation has none. No execution timed out,
no owned descendant PID is retained, and no incomplete caught-OOM assertion is
recorded.

Auto total memory was 16766402560 bytes. All twelve fresh-free gates passed;
the minimum recorded prelaunch free memory was 11744198656 bytes, above its
applicable 4608/8704MiB threshold. No invocation was capacity-skipped. This is
prelaunch capacity evidence, not a guarantee about subsequent host contention.
The unchanged 128 SKIPs are 106 unselected regular-profile invocations,
16 upstream-disabled methods, two commented calls and four private exclusions.
There are no adaptation-companion records in this selected large-file scope.

The [same-run producer artifact 11988464079](https://github.com/Heapy/krogu-math/actions/runs/38767320539/artifacts/11988464079)
and consumer input have matching `SOURCE_COMMIT` and verified `SHA256SUMS`.
Their own JARs and the local focused-validation `artifacts/own.jar` are byte
identical at `4f733787...`; this differs from the first attempt's `ded66cd6...`.
Runtime metadata retains Liberica 25.0.4+9-LTS, Kotlin 2.4.21 and Gradle 9.8.1.
Read-only checks verified all 57 locked upstream source hashes, 53 generated
source hashes, twelve compiled-input hashes, thirteen tool-source hashes and
seven retained dependency POM hashes from fourteen dependency records. The
retained tooling directory also matches the reviewed local tooling bytes.

The [evidence artifact 11989293059](https://github.com/Heapy/krogu-math/actions/runs/38767320539/artifacts/11989293059)
contains 273 files, including the sources, GPL text, licensing, provenance,
locks, exact entrypoint, raw process logs, caller dumps, inventory and results.
There is no JAR or class file under `build/math-tck` and no compiled-script
cache. Its only JAR is the separate own Apache library input. Dependency JAR
contents are not redistributed or inspected by this audit; retained POMs and
lock metadata record the dependency provenance.
The extracted dependency-notice tree is absent; the later source-only
correction below does not change this historical artifact.

Evidence is retained at `/private/tmp/krogu-88ef-large-ci-evidence-v1`.
This audit read existing bytes and appended only this report section; it ran
no script, compiler, numerical helper, test, build, probe or benchmark and
retried no invocation. The first `870575e6` result remains **10 PASS / 2 FAIL /
128 SKIP**, including its two raw timeout failures. This second attempt
establishes the parser witness pass and exposes a later retained assertion
failure on a new artifact; it does not establish full SymmetricRangeTests,
all public tests, task559/task548 closure, performance acceptance or official
JCK compliance.

```text
88a26fedb77eafa0e9e660449b4d63d8ab4916eb552bebc19b80c7a1dd11ece5  results.json
d54c02c190bd748b623c679bcd7b9f1245a4876f4c8054b0c51e06c5d8133883  final-run.json
a124ce7a6c8545f3a218c2f715ac4b4c77006bb3869df753380e30624618a5f4  large.raw.log
f72d5d96f111247a4ee9dbc43e33d212c5d0575e47ca7287cb3dda0de6e47b07  public-math-large-campaign.log
48d1b83331db7640e514bfee68bb6579ecdafc67fbed16fd7cd5e7660409d5d4  inventory.json
4f7337871a3b32761cc6c25e0dc36a92f56ef759723cb0baf1dd699430ab0ed0  krogu-math.jar
1355f1bebb1420f9413ab2cf0ff959eefc1362bbc0679c7efa1ee44f7711b2b2  port/StringConstructorOverflow/test.stdout.log
e3b0c44298fc1c149afbf4c8996fb92427ae41e4649b934ca495991b7852b855  port/StringConstructorOverflow/test.stderr.log (empty)
8c3429822512c039dca32d0cc52035b75ed8081dde90c22825c0da4182fbfa19  port/SymmetricRangeTests/test.stdout.log
792c0bd066eb800e1dac920505c2d708a3c5e7c347ad4f3e549337dd5b76dc3b  port/SymmetricRangeTests/test.stderr.log
```

## Dependency-notice omission and source-only correction

Read-only inspection of both retained large evidence artifacts (870575e6 and
88ef6235) found their GPL text, `LICENSING.md`, exact sources/locks, raw results
and dependency POMs, but no extracted dependency-notice tree. The executed
entrypoint used `notices/<dependency>.jar/`, which the unchanged
`!build/math-tck/**/*.jar` exclusion removed as a directory. Source notices
retained inside test sources are distinct from those missing dependency notice
files. Earlier preparation descriptions of intended dependency-notice uploads
are not proof of actual preservation; historical results and artifact hashes
remain intact.

The separately prepared one-line source change writes instead to
`notices/<dependency>.jar.notices/`, retaining `entries.json` and numbered text
files beneath it. The pinned validated basename and fixed suffix contain no
new separator/traversal; ZIP entry names are not used as output paths. The new
directory and its text children do not match either binary exclusion, while
dependency/test JARs and `.class` files stay excluded. Extraction selection,
dependency hashes, tests, flags and deadlines are unchanged.

This correction was **SOURCE-ONLY, UNCOMPILED/UNRUN** at preparation. Root2306
then validated actual extraction in one isolated metadata-only `--prepare`:
seven directories/indices and ten text files, all ten exact ZIP-entry byte
comparisons passing, with no excluded `.jar`/`.class` output path segment.
The entrypoint remains `8383d062…`; no Java test compiler or numerical harness
ran in that metadata preparation. Hosted notice preservation was then a separate
gate; the actual 8e6 upload below now supplies that evidence for the large
workflow. The old artifacts are not repaired or
relabeled. The [regular CI correction](public-math-ci-559.md#dependency-notice-omission-and-source-only-correction)
records the shared pre-edit snapshot, actual metadata hashes/limits and the
historical regular870 audit.

## Actual hosted attempt: 8e6 timeout in the toString section

[Run38772275107](https://github.com/Heapy/krogu-math/actions/runs/38772275107),
attempt1, executed exact commit
`8e6d6b6b8c0cbb3ee2ccb7d102d3454b9115397d`. Producer job116550897068 succeeded;
consumer116551738101 failed. The retained ledger has **140 unique semantic
records / 118 unique public invocation IDs**, with **11 PASS / 1 FAIL /
128 SKIP**. All twelve original large invocations were attempted once:
baseline 6 PASS, port 5 PASS plus one timeout. The 128 skips retain 106 unselected
regular-profile invocations, sixteen upstream-disabled methods, two commented
calls and four private-internal exclusions. `complete=true` describes the
completed ledger loop, not completion of the timed-out invocation's assertions.

The only failure is `port/BigInteger/largeMemory/SymmetricRangeTests/run-1`:
**timeout 180076ms** under its original 180s bound, exitCode=null. Raw stderr is
empty; the final stdout marker is `Testing BigInteger.toString`. Original public
TEST source lines449–454 pass actual radix 16,
`BigInteger.valueOf(-1).shiftLeft(Integer.MAX_VALUE - 1)`, expected length
`(1 << 29) + 1`, prefix `"-4"` and fill `'0'`. The diagnostic string alone is
not the argument evidence. Helper lines434–446 call `bi.toString(radix)`, then
check length, prefix and remaining characters. The timeout does not distinguish
argument evaluation, formatting or that validation loop. Later byte-array,
primitive-conversion, serialization and exact-conversion assertions have no
reached/completed claim. StringConstructorOverflow and the other four port
invocations passed, as did all six baseline invocations.

Every large invocation passed its fresh observed capacity gate: 4608MiB for
the 4g processes and 8704MiB for the 8g processes. Total observed memory was
16766410752 bytes. No invocation was capacity-skipped. Original heap/VM flags,
seed 559, subset 0 and 120/180s deadlines remain. This timeout is not a capacity
waiver, performance ratio or permission to extend a deadline.

The [same-run producer artifact11992455858](https://github.com/Heapy/krogu-math/actions/runs/38772275107/artifacts/11992455858)
and consumer copies verify SOURCE_COMMIT and SHA256SUMS and compare byte-identical
at own JAR `de97f2b5…`, the same artifact bound by parent to the focused-prime
validation. This hosted run is fresh evidence for that JAR; older runs keep
their own artifacts. Runtime is Liberica25.0.4+9-LTS, Kotlin2.4.21 and Gradle9.8.1;
the existing public-test/runtime revision mismatch disclosure remains. The
executed entrypoint is `8383d062…` and helper runner `ed32beb0…`.

All twelve compilations, six port caller audits and six core-loading guards
passed. All 24 recorded core-class origins name the own JAR; all 72 referenced
raw paths exist. Read-only byte checks verified 57 locked upstream sources,
53 generated sources, twelve compiled inputs, thirteen tooling sources and
seven dependency POMs. No compiler, script, helper, numerical operation, probe,
test, build or retry was executed by the evidence audit.

The [hosted evidence artifact11992282760](https://github.com/Heapy/krogu-math/actions/runs/38772275107/artifacts/11992282760)
has 290 files, including **all 17 dependency-notice files**: seven JSON indices
and ten text files. Its complete relative-path SHA-256 list is byte-identical
to `/private/tmp/krogu-notice559-validation-jhPGz0/root/build/math-tck/notices`.
Four indices intentionally contain no matching entry; the ten retained texts
are eight JUnit, one SLF4J and one TestNG. This confirms hosted preservation of
the existing extraction selection. Top-level GPL text, `LICENSING.md`, exact
sources, locks, POMs and raw evidence remain. No JAR/class file under the math
workspace or compiled-script cache is distributed; the own Apache library input
JAR remains separate. Earlier missing-notice artifacts are unchanged, and
notice preservation by a newer regular job is not established by this large run.

Evidence is retained at `/private/tmp/krogu-8e6-large-ci-evidence-v1/`.
Independent actual review accepted this bounded evidence. The first870 attempt
still has its two timeouts; the 88ef constructor/assertion failure remains a
separate failed artifact. This attempt's 11 PASS / 1 TIMEOUT does not relabel either
older failure or establish later unreached assertions. A separately scoped
formatting candidate has no accepted runtime or timing result in this report;
no speedup claim or full public/domain/JCK acceptance follows.

| Evidence | SHA-256 |
|---|---|
| `REPORT.md` | `910992c04c1c635e5999598313f588a9519ae283bf1a5ecd6af486c9f29ee147` |
| 313-entry `evidence-index.sha256` | `ea9226220fcc9e9a4ea1fb1d82d9f2287b31c29ac6f24cb04db5fe3479a67c63` |
| `results.json` | `662e160efbf3e078781ec3a71f5aa3444a977e574d6919eed4594fb678b147e0` |
| `run.json` | `867a7082e2a08550d986aaa5c18aa2aaf75a02f8f4ae142a23f266c68f7c0c8e` |
| `jobs.json` | `3fbc678fd130cafdef0c3bc05eee18606f048a8e7d6ae2bf9744dd714db40d92` |
| `artifacts.json` | `6f531c73737bb6f6e1fd14a935cc27f36dbdbceb6100fc3f215780626f762bb2` |
| `large.raw.log` | `9dddb48a80483d5181b2ea81a5ed1e9a3f18fc43247d0c0b3cc6d1c2b5002ab3` |
| `public-math-large-campaign.log` | `3476bd4bd36198ebab7b5133edd5651e4b6d10f47afdcf6198c775f39148513d` |
| `inventory.json` | `21ddd1c7ef0795425afc649245bfbeea567368aba96b22a2c640b254f38dcb28` |
| Own producer/consumer JAR | `de97f2b5b68620c9b94c433999222421d53b742de82c44f9e3c2b5932a7d299e` |
| Executed entrypoint | `8383d062feac7a3b2e8eda8f9e2844f7d2dbe6ac8d4e5c575f3fc92f58ad1265` |
| Executed helper runner | `ed32beb00f4c6896cfd783f2286f95336fee3cc2616420b9a83687c4c5f32ea0` |
| Hosted/local notice-list | `74a6769a33892aa21031cf5240ea145d3395d8b4df1e962f6a964e9d390fed1c` |
| Original Symmetric TEST source | `da79e08429dc3072acb25e75d161943ae5cda872c7d183c487b33633039bc8a5` |
| Port Symmetric stdout | `97153617b84fb72026ed6feb03013d8bad99658cea81b9e5d1a15662fce58130` |
| Port Symmetric stderr (empty) | `e3b0c44298fc1c149afbf4c8996fb92427ae41e4649b934ca495991b7852b855` |

## Actual hosted success: 35086 original large invocations

[Run38776732952](https://github.com/Heapy/krogu-math/actions/runs/38776732952),
attempt1, succeeded at exact commit
`35086ecf44b72618449575510bfb5b45fe4ded57`. Producer116566215748 and
consumer116566721588 both succeeded. The actual ledger has **12 PASS / 0 FAIL /
128 SKIP**: six original large invocations on the baseline and six on the port.
All twelve were compiled and executed once, with exit0, no timeout and no
caught-OOM-incomplete flag. Every fresh capacity gate passed; none was
capacity-skipped. The 140 semantic records and 118 public IDs are unique, and
the public ID set exactly matches the retained 59-plan inventory across both
phases. `complete=true` accompanies actual execution and completion here.

Port SymmetricRangeTests completed in **35296ms**, under the original **180s /
8GiB** bound; baseline completed in 24382ms. The command retains
`-Xmx8g -XX:+CompactStrings -Dsubset=0`, seed559 and the original main entry.
Stdout reaches every later section through formatting, byte-array construction,
primitive conversions, serialization and exact conversions, then ends
`HARNESS_COMPLETE port SymmetricRangeTests`; stderr is empty. Its scheduled
retained public assertions returned without an escaping failure. Disabled,
commented, private and other unselected coverage is not inferred. These elapsed
times document completion, not a benchmark or speedup ratio.

All original heaps/VM flags and 120/180s per-process deadlines remain. Observed
total memory was 16766414848 bytes; fresh free observations were 11762241536–
11893616640 bytes. The required gates were 4608MiB for 4g processes and 8704MiB
for the others. This establishes observed eligibility on that host, not a
general allocation or full numerical-range proof.

The [producer artifact11994501033](https://github.com/Heapy/krogu-math/actions/runs/38776732952/artifacts/11994501033)
and consumer own input verify `SOURCE_COMMIT` and `SHA256SUMS`. Both JARs are
byte-identical to both focused-format local copies at `856fc0d4…`. All 220
source-manifest checks pass against the focused and publication trees, using
manifest `849d826d…`, with parent binding that source set to35086. The executed
entrypoint remains `8383d062…` and helper runner `ed32beb0…`. Runtime remains
Liberica25.0.4+9-LTS, Kotlin2.4.21 and Gradle9.8.1, with the existing
public-test/runtime revision mismatch disclosure. All six caller audits,
six loading guards and 24 core-class origin records bind to the own JAR.
All 72 referenced raw files exist; existing-byte checks pass for 57 locked
upstream sources, 53 generated sources, 12 compiled inputs, 13 tooling sources
and seven dependency POMs.

The [evidence artifact11994182103](https://github.com/Heapy/krogu-math/actions/runs/38776732952/artifacts/11994182103)
has 290 files, including all 17 dependency-notice files byte-identical to the
approved local validation tree. GPL text, `LICENSING.md`, sources, locks,
POMs and raw evidence remain; dependency/test JARs, `.class` files and compiled
script cache remain excluded. The only evidence JAR is the separate own library
input. Both original artifact ZIPs are retained and locally rehashed to their
GitHub API digests. Earlier missing-notice artifacts and all older failed
campaigns remain unchanged.

Independent parent metadata review accepted the actual results and checked all
318 original evidence-index entries. The evidence is retained at
`/private/tmp/krogu-35086-large-ci-evidence-v1/`. This later run supplies the
runtime evidence absent at the earlier 8e6 formatting-candidate milestone; it
does not rewrite that earlier timeout or any older assertion failure. The
configured public ID union is **106 regular + 12 large = 118 baseline/port
invocation IDs**, across separately identified artifacts and runs. That is
configured public-harness coverage, including its documented adaptations and
exclusions, not official JCK or an all-input/full-domain proof. **Normal CI at
exact35086 remains pending separately**; earlier regular successes are not
rebound to this JAR. Task559 acceptance and task548's domain obligations remain
separate parent gates. This text update ran no compiler, script, helper,
numerical operation, probe, test, build or API campaign.

| Evidence | SHA-256 |
|---|---|
| Frozen `REPORT.md` | `0128b0754471827a43eeb687b30b3126d5aceefab407db7d5be0e22f7bcce45d` |
| 318-entry `evidence-index.sha256` | `ea49bb667e0a1644d45e08a6e3cf8bff1b7f3178e7b8a987e88d56a5aa953b9e` |
| Independent `root-actual-audit.json` | `75056d334db69490632c24073662da9fc7fe49dd3e489084955b9b48af9b5701` |
| `root-index-check.txt` | `94239c77daea5d5539b42fc9e3306388ef799e572eb59cc82ec83e08fee58777` |
| `results.json` | `e70113b1ec4d6ae0347399353a4cac66a83f5ef96170d65eb1c7cfcdb30b027a` |
| `large.raw.log` | `8aa9dfb9d4681af9b21f1b03c1bb96f5653ca3b39b47382fc1053873579c63c0` |
| `public-math-large-campaign.log` | `8cbed42285586ad4206fb70eed782feb78dda2022d0e74f37866fcef5dd99fc6` |
| `inventory.json` | `6609913a5eb32211c7be78d917564ae450d929ab96169154d963e1767068fb53` |
| Own focused/producer/consumer JAR | `856fc0d43404d03c7fe3e0947d2b9d14eb853ed1fae41ac57167c8d12e229dfd` |
| Source220 manifest | `849d826ddad5ff05976a19847807c6bbacd510a7a7c8b1dd61e83181a6c6cdae` |
| Hosted/local notice list | `74a6769a33892aa21031cf5240ea145d3395d8b4df1e962f6a964e9d390fed1c` |
| Symmetric port stdout | `e8f709bf63f9732dc73ee111811593f361b524788062183a3c610279add9a3af` |
| Symmetric port stderr (empty) | `e3b0c44298fc1c149afbf4c8996fb92427ae41e4649b934ca495991b7852b855` |
| Producer ZIP | `0be0de492be24cdb5a17169f5cdf6c272413eca233add24ad110341aa25a97bf` |
| Evidence ZIP | `33fc8020debc2b12c92cefdb51d24678d73b495bfabf11c6c47d48ed20f17572` |
| Read-only evidence capsule including parent audit | `f5d5fc6707c5117425124b15537894c6268b064149921e651b55fc9deec5de78` |
