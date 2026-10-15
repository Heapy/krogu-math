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
sources, dependency POMs/notices, inventory, JSON results, stdout/stderr,
class-load logs, framework XML and caller dumps. It also includes the verified
own input JAR/source/checksum records, campaign log, exact math tooling/locks,
GPL text/licensing/provenance and this contract document. Explicit exclusions
remove every `build/math-tck/**/*.jar` and `build/math-tck/**/*.class`, preserving
the existing combined dependency/test-binary distribution boundary. The own
Apache library input JAR is separate and retained. The temporary compiled-script
cache is outside all uploaded paths. Startup failures may have only partial
evidence; `always()` cannot guarantee upload after infrastructure/job termination.

This is a controlled public OpenJDK test harness, not official JCK or a new
whole-corpus/full-domain acceptance. Existing vendor/test-revision disclosures
remain. The unchanged regular workflow and prior accepted eaa records keep
their original source/artifact/run scope; this candidate's source review and
metadata preflight do not replace the required actual hosted evidence.
