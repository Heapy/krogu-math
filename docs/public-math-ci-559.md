# Public math tests in CI (task 559)

The initial source-only integration and independent review did not execute the
workflow or script. Root subsequently compiled the entrypoint in the metadata-only
preflight recorded below. An actual exact-commit GitHub Actions execution remains
required; source review and metadata preparation supply no public-suite pass or
official JCK certification.

## Artifact and source binding

The existing Ubuntu platform matrix entry explicitly requests `jvmJar` alongside
its existing tests. After that successful build, a step verifies checkout HEAD
equals `GITHUB_SHA`, copies the known project archive
`build/libs/krogu-math-jvm-0.1.0-SNAPSHOT.jar` to
`build/public-math-input/krogu-math.jar`, writes `SOURCE_COMMIT`, and records both
files in `SHA256SUMS`. An archive-name/version change therefore fails closed
until this explicit path is updated.

The pinned upload action publishes `public-math-jvm-${{ github.sha }}`. A separate
`public-math` job depends on the platform matrix and downloads that exact name
with the pinned download action. No foreign run ID, token, repository, fallback
JAR search, Maven publication or locally rebuilt replacement is supplied. The
download action's default is the current workflow run. The consumer checks its
own checkout HEAD and `SOURCE_COMMIT` against `GITHUB_SHA`, then verifies both
checksums before launching the harness. The harness separately records the JAR's
absolute path and SHA-256; existing CoreGuard/class-loading and caller-bytecode
checks continue to bind port execution to that archive.

These checks establish same-run build/check-out provenance and byte identity
inside this workflow. They are not signed artifact attestations. Existing
workflow-level `contents: read` is sufficient; no token write scope, secret or
cross-run artifact permission is introduced. The job may attempt its download
after another platform fails, provided the workflow was not cancelled; a missing
Linux artifact fails the job rather than selecting another artifact.

## Exact execution selection and provenance

The workflow expands the following command after verification, with a 256MiB
script JVM and an isolated runner-temporary compiled-script cache:

```sh
env -u JAVA_TOOL_OPTIONS -u JDK_JAVA_OPTIONS -u _JAVA_OPTIONS -u KOTLIN_OPTS -u JAVACMD -u CLASSPATH \
  kotlinr -J-Xmx256m tools/run-math-tck.main.kts --both \
  --port-jar "$PWD/build/public-math-input/krogu-math.jar" \
  --profiles standard,serialization,heavy,memory-512m \
  --max-heap-mib 512 --os-memory-gib auto \
  --ci-execution "$GITHUB_RUN_ID:$GITHUB_SHA" \
  --allow-owned-timeout-cleanup
```

`pipefail` preserves the harness exit status while `tee` captures the campaign
log. There is no `--tests` filter or subset reduction. BellSoft Liberica
`25.0.4+9` and Kotlin `2.4.21` retain the repository's existing pinned setup action.
The harness still checks the actual BellSoft release file and child runtime.
Existing source/dependency checksum locks and seed 559 remain unchanged. The
workflow disables shared compiled-script cache restoration; its temporary cache
is outside the evidence artifact.

`--ci-execution` accepts the exact environment pair `GITHUB_RUN_ID:GITHUB_SHA`;
it does not pretend a workflow run number is a parent comment ID. Before source
preparation, it requires `GITHUB_ACTIONS=true`, a positive numeric run ID/attempt,
a 40-character lowercase commit SHA, a repository-shaped name, the checked-out
`GITHUB_WORKSPACE`, job `public-math`, a workflow reference to that repository's
`.github/workflows/ci.yml`, a nonblank workflow name, and a 40-character workflow
SHA. It also enforces `--both`, the exact four profiles, a 512MiB ceiling, automatic
memory observation and absence of a test filter. Run, attempt, source SHA,
workflow SHA/ref/name, repository, job and attestation are recorded as
`ciExecution` in inventory and execution JSON. On pull requests the source SHA
can be GitHub's tested merge commit; the workflow SHA is recorded separately.

The CLI rejects simultaneous CI and parent-comment attestations. Without
`--ci-execution`, the existing numeric parent-comment requirement and rejection
of code-only comment 1753 remain intact. CI environment checks record and
constrain this workflow's execution contract; environment strings are not an
independent authentication mechanism for arbitrary local callers.

## Memory and retained coverage

`--os-memory-gib auto` reads `totalBytes` from the existing
`mathMemorySnapshot()` operating-system bean snapshot and divides by 2^30. The
snapshot and original input `auto` are retained in inventory and results. It
requires positive observed capacity, then preserves the later fresh snapshot
and check that the resolved total does not exceed observed total capacity.
Explicit numeric memory inputs retain their parsing, positive execution check,
planning behavior and later capacity precondition. Automatic total observation
does not promise free memory or absence of OOM.

For the current pinned corpus there are 59 selected public run directives per
phase. On a 64-bit runner with the retained ExtremeShifting OS-memory requirement
satisfied, these four profiles admit 53 per phase: 106 baseline/port invocations.
The other six per phase remain explicit resource/profile barriers. No 4/8/10GiB
profile is authorized or automatically enabled. A successful complete run is
expected to retain 46 SKIP records: 12 resource invocations, 16 upstream-disabled
methods, two upstream-commented calls, four excluded-private records, and 12
adaptation companions. These are an inventory expectation, not results observed
for this source milestone. Lower actual capacity can add barriers; failures,
framework skips and incomplete runs must be reported as they occur.

All existing test adapters, assertions, retained domains, serialization scope,
original timeouts, nested-child checks, swallowed-OOM failure classification,
raw assertions and bytecode audits are unchanged by this integration. It runs
public OpenJDK tests through the existing controlled harness, not jtreg itself
or the proprietary official JCK. The existing vendor/revision mismatch remains
reported in harness metadata; a green job is not full API/domain compatibility.

## Time limits and evidence

Each ordinary public invocation keeps its original 120-second harness default;
the second BigIntegerTest directive keeps 400 seconds. Compiler and each caller
bytecode audit process retain 120 seconds, and the runtime probe retains 30.
The existing runner may clean up only its own captured descendants after timeout;
this integration adds no PID sweep, foreign cancellation or numerical changes.

The campaign step has a separate 330-minute limit inside a 360-minute job. This
allows a long serial corpus run and reserves time for setup and evidence upload.
It is not a claim that the sum of all worst-case process limits fits this outer
limit: test deadlines alone total 221 minutes 20 seconds for the current 106
eligible invocations, before compilation/audits/setup/cleanup. Cancellation,
resource pressure or a stalled process can leave a partial run. Original
per-process deadlines have not been increased or reduced to force acceptance.
The harness only marks `complete=true` after its full result loop finishes.

An `always()` upload step requests the ignored `build/math-tck/` evidence workspace,
verified input JAR/commit/checksums and campaign log, named
`public-math-evidence-${{ github.sha }}-${{ github.run_attempt }}`. This includes
pinned downloaded test sources, generated/baseline sources, dependency POMs
and notices, inventory, result JSON, caller dumps, stdout/stderr,
class-load logs, framework reports and nested-child evidence when produced.
The artifact also retains the exact `tools/math-tck/` directory and entrypoint,
including the GPL-2.0 text, licensing/provenance notes, source/dependency locks,
adapters, runner and Java harness sources, plus this CI contract document.
All JARs and `.class` files under `build/math-tck/` are explicitly excluded,
including dependency/combined test binaries and baseline patch classes. This
preserves the existing `tools/math-tck/LICENSING.md` binary-distribution boundary.
The separately built own Apache library input JAR remains included under
`build/public-math-input/`; exact caller bytecode dumps, source and JAR origin/hash
metadata remain available without redistributing compiled test/framework code.
It deliberately excludes the compiled-script cache. A warning for absent files
allows startup failures to retain any available diagnostics; it does not make
the failed verification or campaign successful. `always()` cannot guarantee an
upload after infrastructure termination or exhausted job time.

GitHub documents same-run artifact sharing and separate job timeouts in
[workflow artifacts](https://docs.github.com/en/actions/tutorials/store-and-share-data)
and [workflow syntax](https://docs.github.com/en/actions/reference/workflows-and-actions/workflow-syntax#jobsjob_idtimeout-minutes).
This source review uses those primary interface contracts; no GitHub job was
launched and no foreign numerical implementation was inspected.

## Parent metadata preflight

After the separate frozen-b730 numerical campaign and its owned invocations
were terminal, root compiled entrypoint82b129a8 in a fresh isolated script cache
using Kotlin2.4.21/Liberica25.0.4+9-LTS and a256MiB script JVM. One invocation
of `--plan --profiles standard,serialization,heavy,memory-512m --max-heap-mib 512
--os-memory-gib auto` exited0 (session42007). It fetched/verified57 source files
and14 dependency records and prepared53 test sources.

The retained inventory reports `memoryInput=auto`, observed total38654705664
bytes, `testsExecuted=0`, `ciExecution=null`,53 PLANNED and6 SKIP directives.
An exact metadata check passed. No javac, caller audit, numerical JVM test or
Gradle build was invoked by this preflight. In particular, it does not exercise
the hosted CI-environment branch or replace the later actual workflow run.
Evidence is retained at `/private/tmp/krogu-559-ci-preflight-v1`.
