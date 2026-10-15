# Public math tests in CI (task 559)

The initial source-only integration and independent review did not execute the
workflow or script. Root subsequently compiled the entrypoint in the metadata-only
preflight recorded below. The subsequent exact-commit GitHub Actions run passed
all seven jobs, including106 public baseline/port invocations with46 explicit
coverage skips. Source preparation, hosted results and the remaining large
profiles are distinguished below; this is not official JCK certification.

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
pinned downloaded test sources, generated/baseline sources, dependency POMs,
inventory, result JSON, caller dumps, stdout/stderr,
class-load logs, framework reports and nested-child evidence when produced.
Dependency-notice text is also intended evidence; the historical omission and
prepared directory-name correction are recorded below.
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

## Hosted verification

[Run38754517747, attempt1](https://github.com/Heapy/krogu-math/actions/runs/38754517747)
completed **seven/seven jobs SUCCESS** on published
`eaaea5ab29114950ac752afff2bf065bbf6035d5`. The actual public job116503120465
used entrypoint82b129a8, separately from the earlier local frozen-b730 campaign.
Its recorded `ciExecution` validates run/attempt, source/workflow commit,
repository, workflow path and job `public-math`; `executionGrantComment=null`.
The real hosted CI branch and automatic-memory path were therefore exercised.

Result `1792077270926` is complete=true, officialJck=false, testsExecuted106:
**106 PASS /0 FAIL /46 SKIP**. All118 unique public IDs are accounted for:
53 PASS and6 unselected-large-profile SKIP per phase. Additional SKIPs remain
12 adaptation companions,16 upstream-disabled methods, two commented calls
and four excluded-private records. Both BigIntegerTest port directives passed
in100668/102635ms within the original120/400-second limits; adapted PrimeTest
passed in46812ms. No controlled performance conclusion follows.

All53 port invocations have successful exact-core loading and nonempty caller
audits with no host-math member owners. All106 raw completion markers, nested
child proofs, compilations and executed process records pass with no timeout.
PrimeTest still observes no huge-input branch/Boolean. Host nested-process
orchestration retains its documented adaptation; no original-source-equivalence
claim is inferred from the baseline result.

Same-run input artifact11984491074 and evidence artifact11984508646 bind to
this run/commit. Downloaded input checksums and SOURCE_COMMIT pass. The JAR is
byte-identical to the accepted current-production ded66 artifact. The evidence
contains the exact entrypoint, math-tool sources/locks/licenses and raw caller
dumps/logs/results; no `.jar` or `.class` under `build/math-tck` was distributed.
The separately owned Apache library JAR remains included as designed.

The other six jobs pass, including actual Linux ARM64 job116503120398.
One fresh256MiB metadata audit using accepted helper7c7a42ec checked65 suites,
314 unique RUN/OK IDs, exact accepted common XML inventory/order and no
omissions/extras/duplicates/failures/skips. All215 source hashes match manifest
e4a136, and the de5-to-eaa `src` diff is empty. This is a fresh hosted runtime
log audit, not another local numerical-test execution.

Independent read-only actual review accepted the run/source/artifact bindings,
all result IDs/counts/guards, source/license packaging and ARM evidence.
The later notice audit below narrows that packaging acceptance: it did not
establish preservation of extracted dependency notices.
All local helper/compiler calls are terminal and the local mutex was released.

An important remaining boundary is visible in the actual memory metadata:
auto total16766414848 bytes, free11844018176 bytes. The12 large public SKIPs
were caused by the four-profile selection, **not an observed capacity failure**.
Task559 stays open for a separately scoped large-profile attempt with fresh
capacity gates; task548's full numerical-range proof remains separate.
This green workflow does not establish full public coverage, full Kotlin/domain
compatibility, official JCK certification or performance acceptance.

Evidence: `/private/tmp/krogu-eaa-public-ci-evidence-v1`, including source and
artifact records, raw logs, checksum checks and exact-result audit. The capsule
excludes the metadata helper's compiled-script cache.

```text
71af0df7c806f0597460ce9ec43018ec7731df96c7dddfb24ff210b5e7dd3f57  krogu-eaa-public-ci-evidence-v1.tar
99801aeab40aa1a480f984c209fab332677d3ec973b8312233d3a96841524002  results.json
a5ad51d84ed49718ebc3ff18f34d51579fbb59e1af9e964c6489d992512333fb  final-run.json
70beb6c2d4baecc6e9a5106f37cad4162e6a1805657b0d0b866ddf2369f32a68  public-job.raw.log
7b7d3a7ab578cbfccb3fb1ad617606d61f1c36a6392ede1bdbaf4e6566076d19  arm.raw.log
bf64dfe291893100a574499832296f6854dbeb93fbf9783e5ef8abab3292647a  arm-audit.log
```

## Dependency-notice omission and source-only correction

Later read-only inspection found that the eaa artifact and the regular
870575e6 artifact retain the top-level GPL text, `LICENSING.md`, exact sources,
locks and dependency POMs, but **not the extracted dependency-notice tree**.
The executed entrypoint wrote `notices/<dependency>.jar/entries.json` and
numbered text files. The workflow's `!build/math-tck/**/*.jar` exclusion removed
those `.jar`-named directories along with actual JAR binaries. Earlier artifact
descriptions must not be read as complete dependency-notice preservation.
The old artifacts, hashes, numerical outcomes and adverse packaging evidence
remain unchanged.

The prepared source correction changes only the directory name to
`notices/<dependency>.jar.notices/`. Each child is still `entries.json` or a
numbered `.txt` file; neither that directory nor its children ends in `.jar` or
`.class`, so the existing binary exclusions no longer select this notice tree.
The parent name derives from the same validated, pinned dependency basename;
adding the fixed `.notices` suffix adds no path separator or traversal. ZIP
entry names remain metadata only, never output paths. Dependency checksums,
selected entries, text bytes, tests, flags, deadlines and binary exclusions do
not change.

The source preparation was **UNCOMPILED/UNRUN**; actual extraction is recorded
below. Hosted upload preservation was pending at that milestone; the later
8e6 large-run confirmation is recorded below. No compiled test/framework binaries are newly
included. The frozen pre-edit entrypoint `11e5ddd8…` and both reports were
preserved read-only at `/private/tmp/krogu-559-notice-source-before-IWZu4n/`.
The historical regular870 metadata audit is retained at
`/private/tmp/krogu-870-regular-ci-evidence-v1/REPORT.md`, SHA-256
`ec096b20b2fa230bbd84d264f6cb8d5a18eefb9a967aa7529f1ba39b17f47e4f`;
its public results remain 106 PASS / 0 FAIL / 46 SKIP, with the notice omission
explicitly unresolved by that run. No historical artifact is relabeled as
containing the future correction.

Root2306 subsequently ran one metadata-only `--prepare` in the fresh isolated
`/private/tmp/krogu-notice559-validation-jhPGz0/root`, using the exact c373789
tools archive plus only the corrected entrypoint (`8383d062…`). Kotlin2.4.21
and Liberica25.0.4 ran with a 256MiB heap and fresh script cache. Session91146
exited0:57 pinned public TEST sources and14 dependency records verified,
53 generated sources prepared, no javac/Gradle/math test invocation.

Actual extraction produced seven `.jar.notices` directories, seven JSON indices
and ten text files. All ten text files match their exact pinned ZIP entries
byte-for-byte. Four archives have empty matching-entry indices; no missing
notice text is invented for them. None of the17 output paths has a `.jar` or
`.class` path segment. The existing upload exclusions therefore do not select
this tree, but actual hosted preservation then still needed inspection of a new
artifact. The entrypoint hash remained unchanged; the compiler invocation and
all owned calls are terminal and the sole local window was explicitly released.

The retained prepare log is `7010f02548206d9bff5f42969ba8819e5a90bc37fa87c2cad1a8dc9007d578f3`;
the metadata audit is `a29b7441e0aaf743351d1735a9e5356a08bc4dd6016dc268cf5d9a0ad62ea13f`.
An initial JavaScript orchestration cell had an invalid regex and executed no
calls; the preparation script was not repeated. This metadata validation is
neither a new numerical test result nor repair of an old hosted artifact.

### Actual shared notice upload: 8e6 large workflow

The separately authorized [large run38772275107](https://github.com/Heapy/krogu-math/actions/runs/38772275107)
at exact commit `8e6d6b6b8c0cbb3ee2ccb7d102d3454b9115397d` used entrypoint
`8383d062…`. Its hosted evidence contains all **17 notice files: seven JSON
indices and ten texts**, with every relative path and byte hash identical to
the local root2306 validation tree. The hosted notice-list SHA-256 is
`74a6769a33892aa21031cf5240ea145d3395d8b4df1e962f6a964e9d390fed1c`.
Top-level GPL text, `LICENSING.md`, sources, locks and dependency POMs remain;
dependency/test binaries and the compiled-script cache remain excluded. Four
indices are intentionally empty, matching the extraction's existing selection.

This establishes actual notice preservation in the **LARGE workflow**.
Preservation by a newer **regular** job has not yet been observed. The old eaa
and regular870 artifacts still lack extracted notices and are not repaired or
relabeled. The large run itself has 11 PASS / one timeout / 128 SKIP; packaging
acceptance does not turn that mathematical timeout into a pass. Full result,
artifact and raw-log hashes are in the [8e6 large-run report](public-math-large-ci-559.md#actual-hosted-attempt-8e6-timeout-in-the-tostring-section).

## Subsequent actual regular result: exact 8e6

[Compatibility run38772258915](https://github.com/Heapy/krogu-math/actions/runs/38772258915),
attempt1, completed with **all seven jobs SUCCESS** at exact commit
`8e6d6b6b8c0cbb3ee2ccb7d102d3454b9115397d`. Its regular public ledger is
**106 PASS / 0 FAIL / 46 SKIP**, complete=true, with 106 actual compile/execution
records and correctly bound completion markers. Baseline and port each pass 53
invocations. All 53 port origin/baseline guards, 63 caller audits and two nested
child proofs pass; all 666 referenced raw paths exist. Original four profiles,
512MiB heaps, flags, seed559, subset0 and per-process deadlines are retained.
The 46 skips retain twelve unselected large invocations, sixteen upstream-disabled,
two commented calls, four private-internal exclusions and twelve adaptation
companions; none is relabeled as a pass.

The same-run producer/consumer JAR and `SOURCE_COMMIT` verify at
`de97f2b5b68620c9b94c433999222421d53b742de82c44f9e3c2b5932a7d299e`.
The exact8e6 archive contains 219 own source files. Independently derived test
declarations yield 68 common suites/324 methods and 148 JVM suites/508 methods.
All 1136 platform XMLs across twelve exact workflow OS/target groups match those
source inventories without omissions, extras, duplicates, failures, errors or
skips. ARM raw has 324 unique RUN and 324 unique OK entries in identical order,
equal to both the source common set and same-run Wasm XML; its 68-suite/324-test
summary appears once with no FAILED/SKIPPED marker. A prior 65/314 inventory was
not reused or forced onto this source revision.

This later regular artifact also confirms the notice preservation that was
still unobserved at the preceding historical LARGE-report milestone. Its 896
files include **all 17 notice files**, seven JSON indices and ten texts,
byte-identical by relative path/hash to the approved local tree (`74a6769a…`).
GPL text, `LICENSING.md`, sources, locks and POMs remain. Dependency/test binaries
under the math workspace and compiled-script caches stay excluded; the own input
JAR is separate. All seven artifact ZIPs were retained and locally rehashed to
their API digests. The old eaa/regular870 missing-notice artifacts remain unchanged.

The parent independently rechecked the raw public JSON, ARM/source inventory
and all 2631 frozen index entries. An initial metadata-key projection omitted
phase/method and collapsed distinct disabled/commented/excluded records; that
rejected audit is preserved read-only as `f915620e…`. The corrected key
`[phase,id,path,scope,directive,method]` identifies all 152 semantic records
uniquely, with 118 distinct public invocation IDs and zero audit issues. This
was a metadata correction, not an artifact/result rewrite or runtime retry.

Evidence is retained at `/private/tmp/krogu-8e6-regular-ci-evidence-v1/`.
This is exact8e6/source219/JARde97 regular and platform evidence. Its separate
8e6 LARGE run remains **11 PASS / 1 TIMEOUT / 128 SKIP**, a distinct failed
campaign on that JAR. This report supplies no new35086 or1b9 result, official
JCK certification, all-input/domain proof or performance claim. At the parent
scope grant for this text update, published1b9's matching normal CI was pending
and large run38783275927 was running; neither is accepted by this historical
record. All older failures retain their original sources, JARs and coverage
limits. This text update ran no compiler, script, helper, numerical operation,
test, probe, build or API campaign.

| Evidence | SHA-256 |
|---|---|
| Frozen `REPORT.md` | `e5b1d818e5c7baa17a1fbed58bd70dd978e44391df3de1c6a06e08cd3ef43325` |
| 2631-entry `evidence-index.sha256` | `6b5ee365c96d660dc0d3b4365b33c31cb4f44f08cad748f0a693cca8381fcc8c` |
| Corrected `root-actual-audit.json` | `b85615d21e99c87e53637d398dbf63bcc98581d3b76fd2f8bdd37254858be7e0` |
| Preserved rejected key-projection audit | `f915620efb3db53917a9f0a4f0a82b474d792dd12781f045dfb32e8952590443` |
| Public `results.json` | `95fe9d27663e11ef7ea371a57912c57654853221bb9d500a8fb3774d34c945cd` |
| `arm.raw.log` | `57c2a1e6c1d64ed649ece5220c20a8940682a26c8ded6e0506b718d9730fd2bb` |
| `platform-source-arm-audit.json` | `2f0b6e46c4dd63b52b8d0d0efd3af985ad24c350225c5a937d250c359bc57254` |
| Read-only evidence capsule including parent audit | `230f323f4f8672454e0cde6d69dfb6eb6c6df3f19d80e2c22f2e49daf69acf17` |
