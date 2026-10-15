# Bounded primality diagnostic preparation

Corrected-source status: **SOURCE ONLY, UNCOMPILED and UNRUN**. Initial parent
task559 grant1943 permitted preparation while native task528 grant1935 held
the sole numerical window. The first later granted attempt and its terminal
pre-child failure are retained below. Independent source review and a later
explicit parent execution grant are required before compiling or running this
corrected diagnostic.

The independently authored Apache-2.0 script is
[`scripts/profile-primality.main.kts`](../scripts/profile-primality.main.kts),
SHA-256 `72a33dd507e841357d8a9921279aff987f79302c6b8f71faa29fb89cc319f27a`.
No foreign numerical production implementation was inspected. This preparation
does not edit public OpenJDK tests, adapters, original operands, certainty,
directives, deadlines or the existing timeout ledger.

The retained evidence in
[`tools/math-tck/primality-timeout-classification.md`](../tools/math-tck/primality-timeout-classification.md)
isolates a shared large-Mersenne primality performance candidate. It does not
identify an exact slow exponent, prove an arithmetic defect or establish an
allocation hot spot. These diagnostic attempts cannot turn public-corpus FAIL
or resource gaps into PASS/SKIP.

## Inputs and provenance

The parent selected the existing root1919 fresh own artifact
`/private/tmp/krogu-root526565525-validation-v1/artifacts/root-integration-fresh.jar`,
SHA-256 `daf2a13324f780b4b592e946c24dc601fc678d37f4954f0907fdbaea02674bdd`.
The exact reviewed provenance report is
`/Users/yoda/dev/pet/krogu-math/docs/math-port-integration-verification.md`,
SHA-256 `31d0abd229951a75320ed1bd83d61799a8856412187a602ded3a8ee7d6767a97`.
That report binds this artifact to its forced build log SHA-256
`e0816885bd3045ae261598ff3503fa253a75c5e419d4b4d2efcb2b2a3a8d08f0`
and byte-identical before/after 28-input source manifests SHA-256
`46486e0138f850331b6d48ffb0a5cfccafdb08daf2103b3b8599fa53ef8ca37d`.
The caller supplies the exact JAR/report paths and report hash. The report is
byte-copied into evidence as `pinned-provenance-ledger`; the script records its
digest, but does not read or automatically interpret it as a proof of source
provenance. The parent independently reviewed that binding. Source, JAR and
report are rehashed at the end.

This is a diagnostic of the root1919 port snapshot. The original public559
timeout failures concern a separate JAR with SHA-256
`54ec9af64675eba2a89d7cba68a1c404220cfaf3169bd1de8a31193514e31db0`.
That original artifact is not relabelled: no root1919 source hash or timing is
assigned to it, and this diagnostic cannot claim to reproduce its timeout.
Original failures, draft sources and evidence remain preserved. An unchanged
public `PrimeTest` rerun is still required later for the newer snapshot.
Inputs and their ancestor paths must resolve without symlinks. Evidence must
be a new directory; there is no overwrite, resume or automatic retry.

Runtime pins are BellSoft Java `25.0.4+9-LTS` at the canonical SDKMAN
`25.0.4-librca` installation and Kotlin runtime/SDK `2.4.21`. Each child gets
`-Xms64m -Xmx512m -XX:+UseSerialGC -ea -Dfile.encoding=UTF-8` in that order.
Actual JVM arguments are checked exactly, including the Kotlin home and cache
properties. Injected JVM/runner/classpath environment variables are rejected in
the parent and removed before every child launch. The own JAR is absent from
the script compiler/runtime parent classpath.

The parent compiled-script cache is explicitly disabled, so bootstrap always
compiles the supplied script. Every child uses a new empty directory keyed by
runner-source SHA, pinned-JAR SHA, ledger SHA and JVM/runner option SHA, plus
attempt name. Both child cache environment and system property are checked.
Java is launched directly with SDK `lib/*` and
`org.jetbrains.kotlin.runner.Main`; the parent holds the actual child JVM
`Process`, not a shell launcher. The child branch never starts a process.

An isolated `URLClassLoader` loads the own JAR while sharing only the already
present Kotlin runtime parent. The script first rejects an own math class
visible through that parent. All four own core types must have this loader,
the exact JAR code source and the exact corresponding `jar:` class resource.
Reflection avoids a script compile dependency on own math. No BigInteger
value is formatted into decimal as diagnostic setup.

## Work and bounds

Seven fresh JVMs run sequentially for exponents
`521,607,1279,2203,2281,3217,4253`. Each constructs
`ONE.shiftLeft(p).subtract(ONE)` before READY and makes exactly one
`isProbablePrime(100)` call after the parent writes GO. Fixture construction,
class loading and counter enablement are outside that measured call.
The port uses its public API and `Random.Default`; these attempts are explicitly
**UNSEEDED**. The seeded public-harness campaign does not seed this API.

| Budget | Configured bound |
|---|---:|
| Child JVM launch, script compilation and setup to READY | 30 seconds |
| Normal call, including completion reporting and child exit | 60 seconds |
| Optional separate fresh profile call | 90 seconds |
| Optional separate fresh `java.math` baseline call | 10 seconds |
| Graceful/forced process and reader cleanup, combined | 10 seconds |
| Whole campaign, including parent JVM startup/compilation and optional attempts | 15 minutes |

Whole elapsed time uses parent JVM uptime, which has millisecond resolution.
Before each attempt the parent reserves its complete startup, call and cleanup
budgets; a case that cannot fit is recorded `NOT_STARTED_WHOLE_BUDGET` and is
not silently shortened. The parent polls at 100 ms and records observed
startup/call/cleanup/total durations separately from configured bounds. Process
metadata, scheduler and file/stream IO latency can overshoot a poll boundary;
this is not a hard realtime supervisor. An exceeded cleanup/whole bound stops
the campaign and remains a failure. There is no deadline increase or retry.

If requested, a separate fresh profile selects the smallest exponent with a
normal-call timeout; otherwise it selects the slowest completed measured call.
Several timeouts are not ranked by speed. A second optional fresh JVM runs
the public JDK call for that same selected input. Neither replaces the seven
normal records. Startup failures without a measured call are not candidates.

Only the captured process PID plus start instant may be revalidated and
signalled. Cleanup requests graceful exit, waits up to 2 seconds and then
requests forced exit if necessary, with a shared 10-second budget for process
and reader completion. No descendants, process inventories, name matches or
unrelated signals are used. If captured identity cannot be revalidated, the
script refuses a signal and stops; the operator must preserve evidence and
resolve that owned-process condition before any later numerical owner starts.
An invocation or reader that remains live stops subsequent launches.
The protective `try/finally` begins immediately after successful process start;
handle/start metadata, evidence writes and reader creation are all inside it.
Nullable captured state permits cleanup of a partially initialized capture.
Missing/changed identity still refuses signalling; it never authorizes a
broader process search or substitute identity. Reader/stream cleanup is
attempted independently even when another cleanup action fails, and any such
failure stops subsequent attempts.

At parent startup and immediately before each child launch, timestamped public
`com.sun.management.OperatingSystemMXBean` readings retain total/free physical
memory, total/free swap and the parent's committed virtual memory in bytes.
Unsupported, denied or negative values are explicitly `NA`. Each snapshot
also records the actual parent maximum heap and the pinned 512 MiB parent and
512 MiB child heap limits: both JVMs coexist during a call. These are point
samples, not a reservation, reclaimable-memory estimate or uncontended-host
claim. Free swap is recorded as free swap; no compressed-memory or swap-activity
measurement is inferred. Changes between samples and other processes' demand
remain unobserved. No invented availability threshold, resource SKIP or new
approval condition is introduced by these readings.

## Measurements and interpretation

A dedicated computation thread remains alive through post-call result/counter
observation. Wall nanoseconds, supported thread CPU nanoseconds and supported
`com.sun.management.ThreadMXBean` allocated-byte deltas bracket one reflective
API invocation. The small counter-read, atomic marker and reflective-dispatch
overhead is retained; this is not a claim of pure arithmetic-kernel cost.
Unsupported/unavailable/negative CPU or allocation counters are `NA`, never
zero. GC collector names/counts/milliseconds are recorded before and after;
nonnegative supported deltas are process-wide, and unavailable deltas are `NA`.
They are not attributed solely to the computation thread.

The optional sampler observes only the computation thread, at approximately
100 ms intervals, up to 900 samples, 32 retained frames per sample and 2 MiB.
Each sample records monotonic time, thread state, a `partial_not_final` marker,
call-start/current supported CPU/allocation snapshots, frame truncation and
frame names. These cumulative snapshots can survive a timeout; they are not
final-call deltas. Raw samples flush every ten samples (about a second), and
normally at close, so the final unflushed fraction may be lost on termination.
Sampler allocations occur on its separate thread. Sampling can perturb call
wall time, especially relative to the normal attempt.

Foreign frame names may appear in raw samples; no foreign method body is read.
On normal profile completion, `own-frame-frequency.tsv` counts occurrences
of distinct own math frames per sample. Frequency is neither CPU accounting
nor allocation-site evidence. Exact supported total allocation and CPU counters
are reported separately. A retained sampled chain such as own witness /
`modPow` / `oddModPowStored` / modular multiply / division would support a
later investigation, not establish which operation allocated each byte.
There is no JFR, JMH, statistical-significance or uncontended-host claim.

## Evidence schema and failures

All TSV cells escape backslash, tab, CR and LF. A top-level `run.tsv` records
schema, exact input/runtime/options/cache identities, grant, configured bounds,
parent PID/start instant, startup host-memory snapshot, selection and campaign completion. `attempts.tsv`
contains kind, exponent, diagnostic status, measured-call nanoseconds when
available, observed parent call duration and exit code. There is no public
test PASS field.

Each `port-p`, optional `profile-p` or `baseline-p` directory contains:

- `command.tsv`: every argv element with its index, preserving exact launch.
- `parent.tsv`: captured child identity, terminal/exit state, observed bounds,
  stdout/stderr truncation, reader/cleanup failures, signal refusal and the
  timestamped pre-launch host-memory snapshot.
- `stdout.log` and `stderr.log`: at most 2 MiB each; excess output is drained
  until cleanup and classified `OUTPUT_TRUNCATED`, never accepted silently.
- `child.tsv`: actual pins/core origins, input expression, certainty, unseeded
  status, computation thread ID, counter support, measurements, GC and result.
- `READY`, parent `GO`, and child `DONE`: explicit setup/call/completion protocol.
- Profile only: bounded `stacks.tsv`, and the own frame summary if the sampler
  and call finish normally enough to publish it.

`TRUE` means that single diagnostic call returned true and the child exited
successfully. `FALSE`, `EXCEPTION`, `CAUGHT_OOM`, startup/call timeout,
launch/child/capture/sampler failure, output truncation and unstarted cases
remain diagnostic failures. A timed-out attempt has no claimed Boolean and
retains its partial files, even when a later fresh profile/baseline succeeds.
Exception type and a bounded message are retained when reporting is possible;
an OOM can also prevent result writing and become `CHILD_FAILURE`.
The script exits 0 only when all requested attempts return diagnostic TRUE;
otherwise it exits 2, or fails immediately for violated safety/provenance bounds.
`complete=true` records terminal campaign completion, not mathematical or
public-corpus acceptance. Failed/partial directories are never discarded.

## Future command — requires a later explicit execution grant

Do not execute this prepared command under source-only grant1943. Replace all
angle-bracket placeholders with reviewed paths/digests and the later parent
grant. The evidence directory must not exist. The pinned report's binding must
be reviewed before the command; its content is not validated by hashing alone.

```sh
env -u JAVA_TOOL_OPTIONS -u JDK_JAVA_OPTIONS -u _JAVA_OPTIONS -u JAVA_OPTS \
  -u KOTLIN_OPTS -u CLASSPATH -u KOTLIN_RUNNER -u KOTLIN_COMPILER \
  -u KOTLIN_COMPILER_ENVIRONMENT_KEEPALIVE \
  KOTLIN_MAIN_KTS_COMPILED_SCRIPTS_CACHE_DIR= \
  /Users/yoda/.sdkman/candidates/java/25.0.4-librca/bin/java \
  -Xms64m -Xmx512m -XX:+UseSerialGC -ea -Dfile.encoding=UTF-8 \
  -Dkotlin.home=/Users/yoda/.sdkman/candidates/kotlin/2.4.21 \
  -Dkotlin.main.kts.compiled.scripts.cache.dir= \
  -cp '/Users/yoda/.sdkman/candidates/kotlin/2.4.21/lib/*' \
  org.jetbrains.kotlin.runner.Main \
  /Users/yoda/dev/pet/krogu-math/scripts/profile-primality.main.kts \
  --mode parent \
  --script /Users/yoda/dev/pet/krogu-math/scripts/profile-primality.main.kts \
  --runner-sha 72a33dd507e841357d8a9921279aff987f79302c6b8f71faa29fb89cc319f27a \
  --jar /private/tmp/krogu-root526565525-validation-v1/artifacts/root-integration-fresh.jar \
  --ledger /Users/yoda/dev/pet/krogu-math/docs/math-port-integration-verification.md \
  --ledger-sha 31d0abd229951a75320ed1bd83d61799a8856412187a602ded3a8ee7d6767a97 \
  --grant '<later-parent-grant>' \
  --evidence '<new-unique-evidence-directory>' --profile true --baseline true
```

Known runtime uncertainties remain: this corrected source has not compiled; direct
runner compatibility, cold compilation within 30 seconds, JVM counter support,
exact reflected pinned API/class-resource identities, process metadata
permissions, sampler/capture behavior and cleanup timing have not been tested.
No source-only check can certify these. A future granted run must preserve
the first failure and obtain review for any necessary source revision before
rerunning, rather than silently relaxing pins, measurements or bounds.

## First granted attempt and narrow correction

Root grant1959 executed the preceding runner SHA-256
`32459d986c7951ea4669947552e0dc7a76ce924aa29fab2c8b5c897b85da507e`.
It compiled, validated source/JAR/report/runtime/options identities and wrote
initial metadata, then failed at the parent startup memory snapshot:
`OperatingSystemMXBean.getTotalSwapSpaceSize()` raised
`java.lang.InternalError: errno: 1 error: sysctlbyname failed`.
The metric reader caught `Exception`, which does not include `InternalError`.
The root owner confirmed its invocation terminal with exit3. No child JVM,
attempt directory, READY/GO or mathematical call was launched.

The complete first parent log is retained at
`/private/tmp/krogu-root528566-validation-v1/primality-profile-parent.log`,
SHA-256 `263ada97a874e615a6bccfdd23daa678ae9f2fca28411c47975520b582864d06`.
The partial evidence is preserved at
`/private/tmp/krogu-primality-profile-root1959-v1/`. Before correction, the exact
runner32459 and preparation document70f6 were copied to
`/private/tmp/krogu-root528566-validation-v1/profile-initial-source/`, with
their original hashes verified. The source correction adds only a specific
`InternalError` catch returning `NA` in the host-memory metric reader.
`OutOfMemoryError` and other fatal errors remain unsuppressed; protocol,
operands, counters and deadlines are unchanged. The corrected runner remains
**UNCOMPILED/UNRUN**, awaiting independent review and a later parent grant for
a new unique campaign. The first failure is not replaced or classified PASS.
