# Task 528 prepared measurement protocol

SPDX-License-Identifier: Apache-2.0

**PREPARED, UNCOMPILED, UNRUN.** Parent comment1865 grants only preparation
of this plan and `scripts/measure-decimal-metadata.main.kts`. Task563 retains
the sole runtime window under1859. This document is not an execution grant.
Task528 remains linked/in_progress; no Git mutations or changes to accepted
production, proof, tests, certificate, audit, or handoff are authorized here.

## Frozen snapshots and build provenance

Parent prepared the clean non-Git baseline snapshot at
`/private/tmp/krogu-math-task-528-baseline03`, from exact HEAD
`03ebb60a482873acb3fe892f0c86510472f2e0b3`. Parent-reported archive
`/private/tmp/krogu-528-base03.tar` SHA256 is
`fe9379f9f32b0206f5944440c97879ef523245f7111abeb48f78ba57d7de31b0`.
Do not edit this snapshot. Candidate is the accepted task528 worktree at
`/private/tmp/krogu-math-task-528`.

The driver hard-checks the selected snapshot's own BigDecimal/BigInteger hashes:

| Variant | BigDecimal SHA256 | BigInteger SHA256 |
| --- | --- | --- |
| baseline | 1587d2351f85399bb3d3dbd505fffd812cb7e8b8aedab4f371d4c27520334f2f | 73c52b66a263c51ee57461acc9fc38c99950a43e27ad6a490713785d5e4d435b |
| candidate | 92de3354da9545f5b4867ee2ed32e72c91fac4ed32aa5c666f5762bd9f723370 | bdbd1a0f6d0ec9571c641b7a34ff73ac969da051c648fa6b606ff775c8739f57 |

Later, after the explicit sole-window transfer, build baseline and candidate
serially using their own fresh `jvmJar`, pinned Gradle9.8.1/Kotlin2.4.21 and
Liberica25.0.4+9-LTS. Use the same granted flags, one worker, distinct approved
Gradle/cache roots and isolated Kotlin homes; the parent must provide their
concrete isolation flags in the runtime grant. No existing build/CI file is
edited. Baseline and candidate builds have their own logs, commands, environment
identity, source manifests, JAR paths and JAR hashes. Do not borrow root955 or
any older JAR. Do not build now.

Before and after each future build, freeze a sorted manifest of **all** regular
files under `src/commonMain` and `src/jvmMain`, plus `build.gradle.kts`,
`settings.gradle.kts`, `gradle.properties`, `gradlew`, `gradlew.bat`, and both
Gradle wrapper files. Each manifest row is `sha256`, two spaces, relative path,
with LF line endings. It must have exactly the driver's complete sorted input
set; no omitted sources, duplicated rows, extra metadata files or symlink escape.
The driver rejects symbolic links in source trees, their entry directories and
every manifest path component, including metadata inputs. It never silently
omits a directory link that a build might otherwise follow.
The manifest itself receives a SHA256. Record any unexpected change as a failed
gate rather than adapting away the evidence.

A manifest-generation command template from each snapshot (not executed):

```sh
rg --files src/commonMain src/jvmMain build.gradle.kts settings.gradle.kts gradle.properties gradlew gradlew.bat gradle/wrapper/gradle-wrapper.properties gradle/wrapper/gradle-wrapper.jar | LC_ALL=C sort | xargs shasum -a 256 > "$task528Manifest"
```

All current input paths contain no whitespace. The driver's Files.walk check
also rejects an unlisted regular file, including incidental hidden files that
the template's rg might omit. Resolve a mismatch explicitly before building;
do not silently omit it. The build ledger binds each exact manifest to its
fresh JAR. Runtime SHA/origin checks alone cannot prove that a JAR was actually
compiled from those sources; the serial fresh-build ledger supplies that link.

## Identity and cache checks before measurement

Use the *same driver source* for both variants, with exactly one selected own
JVM JAR on the runner's classpath. No @DependsOn, extra dependency, reflective
numeric invocation, production field or cache is introduced. The driver hashes
its supplied source path, the selected JAR, source manifest and VM flag manifest.
It validates every manifest entry against the snapshot and verifies the runtime
class origins of both own BigDecimal and BigInteger point to that exact JAR.

Supply exactly twelve positional arguments:

```text
variant fork-id snapshot-root source-manifest manifest-sha256
jar-path jar-sha256 driver-path driver-sha256
vm-flags-file vm-flags-sha256 forward|reverse
```

The driver requires an explicitly created script-cache directory named:

```text
task528-measure-<driver SHA first12>-<JAR SHA first12>-<source manifest SHA first12>-<VM flags SHA first12>-<fork-id>
```

Both the cache system property and cache environment variable will point there.
Use a fresh directory per independent fork; any changed source/JAR/flags gets
a new identity. The system property is checked by the driver. No compiled
cache from a different classpath or flag configuration is reused. The original
script runner's cache key does not by itself include all command-line options.

Pinned measurement flags are `-Xms256m -Xmx256m -XX:+UseSerialGC
-XX:-OmitStackTraceInFastThrow`; heap must be <=256MiB. Record the entire ordered
RuntimeMXBean input-argument list. The flag manifest contains that exact list
except the per-fork cache property, which the driver checks separately. Before
any target-call window, driver equality-checks this manifest and verifies Java
runtime25.0.4+9-LTS, BellSoft vendor and Kotlin stdlib2.4.21.

Read-only inspection of the installed pinned `kotlin`/`kotlinc` launcher shows
that, after JAVA_OPTS, this runner appends native-access, unsafe-access and
kotlin-home arguments. Thus the proposed flags manifest is the following
ordered lines; runtime validation will fail if the actual launcher differs:

```text
-Xms256m
-Xmx256m
-XX:+UseSerialGC
-XX:-OmitStackTraceInFastThrow
--enable-native-access=ALL-UNNAMED
--sun-misc-unsafe-memory-access=allow
-Dkotlin.home=/Users/yoda/.sdkman/candidates/kotlin/2.4.21
```

The proposed runner is `/Users/yoda/.sdkman/candidates/kotlin/2.4.21/bin/kotlin`
with JAVA_HOME `/Users/yoda/.sdkman/candidates/java/25.0.4-librca`. Future
launches clear inherited JAVA_TOOL_OPTIONS, JDK_JAVA_OPTIONS and _JAVA_OPTIONS,
and use explicit JAVA_OPTS. Do not set HOME/CODEX_HOME. A future command
template, with every path/hash filled from the verified ledger, is:

```sh
env -u JAVA_TOOL_OPTIONS -u JDK_JAVA_OPTIONS -u _JAVA_OPTIONS JAVA_HOME=/Users/yoda/.sdkman/candidates/java/25.0.4-librca JAVA_OPTS="-Xms256m -Xmx256m -XX:+UseSerialGC -XX:-OmitStackTraceInFastThrow -Dkotlin.main.kts.compiled.scripts.cache.dir=$task528Cache" KOTLIN_MAIN_KTS_COMPILED_SCRIPTS_CACHE_DIR="$task528Cache" /Users/yoda/.sdkman/candidates/kotlin/2.4.21/bin/kotlin -classpath "$task528Jar" "$task528Driver" "$task528Variant" "$task528Fork" "$task528Snapshot" "$task528Manifest" "$task528ManifestSha" "$task528Jar" "$task528JarSha" "$task528Driver" "$task528DriverSha" "$task528FlagsFile" "$task528FlagsSha" "$task528Order" > "$task528Log" 2>&1
```

This template remains UNRUN. Script compilation/launch and fixture construction
must wait for the future runtime grant. A first script launch may compile more
slowly; script compilation and startup are excluded from target-call samples.
The driver does not require an executable bit or shebang for this runner form.

## Cases, expectations, and fixed bounds

All fixtures are ordinary own values created before timing/allocation starts.
Precision uses known nonzero coefficient text `3` followed by ones; its expected
digits are the supplied length for either sign. Comparison uses a shorter
coefficient S with half the listed digits, scale0, and a longer coefficient
with at most the listed digit count. Let k be their digit-count difference.
No port or JDK comparison computes these expected results.

| Cohort (short left, positive sign) | Right coefficient/scale | Expected order |
| --- | --- | ---: |
| equal cohort with trailing zeros | S followed by k zeros, scale k | 0 |
| early differing decade | S followed by k zeros, scale k+1 | +1 |
| quotient difference | `2` then nines to short length, then k zeros, scale k | +1 |
| distant last-digit remainder | S then k-1 zeros and `1`, scale k | -1 |

Signs negate the nonzero order, and reversing short/long orientation negates it
again. All four cohorts run in both directions and both signs. The compact
case uses at most9 coefficient digits, so every coefficient fits Long. The
4096-digit case's last nonzero suffix is2048 places from the leading cohort;
no giant witness is constructed. The bound is solely the measurement cohort,
not a new supported-domain restriction.

| Maximum coefficient digits | Fresh-once target calls/sample | Repeated calls/sample | Warmup calls/batch |
| ---: | ---: | ---: | ---: |
| 9 (compact) | 128 | 512 | 64 |
| 128 | 16 | 64 | 8 |
| 1024 | 4 | 8 | 2 |
| 4096 | 2 | 2 | 1 |

There are72 cases: four sizes × two signs × (one precision case + four comparison
cohorts × two orientations). Each case has two modes, two warmup batches per
mode, and five measured samples per mode. Per fork this is720 sample rows,
66,240 measured public precision/compareTo calls plus5,400 warmup target calls,
71,640 total target calls. Six forks total429,840 target calls. Fixture/parser
construction and startup perform other work outside these windows; those are
bounded by the fixed arrays/coefficient lengths but are not timing/allocation
results. Artifact guards cap manifest1MiB, VM flag file/driver64KiB and JAR64MiB;
hashes stream through an8KiB buffer.

Fresh-once means a newly constructed value per invocation, each target used
once in that sample; it does not mean un-warmed machine code. Every warmup
batch owns separately constructed objects. Measured fresh arrays are never
borrowed from warmup or a previous sample. Repeated mode uses one fixture pair
for all calls in a sample; the next sample constructs another pair. Identity
and ordinary-class checks occur before the windows.

Expected result/checksum construction and IntArray allocation are outside the
windows. A statically compiled loop invokes own `precision()`/`compareTo()`
directly, stores every result and computes a primitive rolling checksum.
After counters/time stop, every stored result is checked against its known
expectation, checksum is checked, and a volatile driver-only sink consumes it.
This also handles equal-order zero cases without relying on a zero sum. Wrong
results abort the fork and preserve its log. These controls prevent merely
discarded results; they do not claim JMH-grade protection against every compiler
optimization. Long checksum wraparound is intentional JVM integer behavior,
not decimal arithmetic.

## Measurements, unsupported counters, and memory claims

Each raw sample records variant/fork/case/mode/repetition/iterations/fresh count,
elapsed ns, current-thread allocated bytes or NA with reason, consumed checksum,
and every GC bean's raw before/after counts/time plus deltas. No output,
expectation generation, fixture construction or reflection happens inside the
target-call loop. The timed window includes direct calls plus the identical
primitive checksum/index work and preallocated-array stores; loop/storage
overhead is not subtracted or presented as pure kernel time.

ThreadMXBean allocation support/enabling is checked before warmup. Missing or
unsupported extension, denied enabling, still-disabled counter, negative read,
or decreasing counter is explicitly reported. Unsupported bytes are NA, never
fabricated0. GC negative counters report unsupported; decreasing counters report
reset-or-invalid. GC snapshots/output are outside allocation/timing windows.
Their interval is slightly broader than the timed loop, so GC deltas are
context for a sample rather than pure target-method attribution.
The allocation interval includes the two nanoTime calls and target-call loop,
but excludes result checking/logging; counter-call perturbation is an inherent
limitation and the same protocol is used for both variants.

Runtime/OS/version/architecture/processor count/max heap/VM flags/cache identity,
manifest/JAR/driver hashes, origins and declared fields are emitted before
measurement. Runtime metadata includes the process start epoch for fork
identification. No System.gc forcing or GC-affected row deletion is performed.

Precision cache cost is assessed by the frozen source's absence of a new
field/cache and by comparing actual runtime **declared field rows** for both
own classes across variants, outside measurement. Compare instance fields
separately from static/synthetic fields. These rows describe declarations;
they do not measure VM field offsets, object-header size, retained bytes or a
byte-sized instance delta. Per-thread allocated bytes measure transient work
in the protocol, excluding fixture construction. No new per-instance cache
exists, but do not fabricate an instance-size measurement or expand legacy
serialization scope to support such a claim.

## Balanced future forks and reporting

After source/runtime gates and explicit grant, run all six independent JVM
processes serially, never alongside563 or another local window owner:

| Sequence | Variant/fork | Case order |
| ---: | --- | --- |
| 1 | baseline B1 | forward |
| 2 | candidate C1 | forward |
| 3 | candidate C2 | reverse |
| 4 | baseline B2 | reverse |
| 5 | baseline B3 | forward |
| 6 | candidate C3 | forward |

All variants use the same driver source, budgets, warmup, repetitions, pins and
normalized flags. The per-fork cache directory is the only excluded flag value.
Retain all six stdout/stderr logs, exit codes, hashes, metadata, GC information
and each of the4,320 sample rows. Failed/partial forks remain evidence; any
corrected run is a new explicitly identified run, not a replacement of failure.
Verify exactly720 ROWs plus DONE per successful fork, complete72-case coverage
and five repetitions per case/mode, expected checksum equality and matching
input manifests/build ledgers. Record compile/startup failures as UNMEASURED,
not as zero allocation or slow numerical samples.

Report per-case/per-mode raw results, each fork's per-invocation values and
within/between-fork variation, with comparable balanced pairs. Show allocation
and elapsed ratios/differences for improvements *and regressions*, together
with unsupported counters, GC deltas and low-sample/loop-overhead limits. Do not
cherry-pick a fastest fork, hide a regression, pool mismatched protocols, claim
statistical significance, or claim JMH-quality uncertainty from this driver.
Especially the4096-digit cases have very few calls: disclose instability and
request a new bounded protocol only if the evidence warrants it; retain the
original protocol and results.

These JVM measurements do not replace serial JVM/JS/Wasm/macARM retained/new
runtime tests, serialization control execution, independent gate review, or
root's exact-integrated-head all-six CI. Any further target measurements or
protocol change need the applicable future parent grant. Current source/proof
acceptance remains conditional on those gates. No speed/allocation results
exist from this preparation phase.

## Preparation review and handoff

The ref-less task528 preparation comment records final driver/plan hashes and
read-only independent review. Reviewer `/root/numeric_bounds_review` verified
the literal cohorts, signs/orientations, budgets, separate fixture ownership,
direct target calls, counter/window boundaries, unsupported-counter reporting,
cache/origin/manifest checks, declared-field limits and balanced fork plan.
It found one initial provenance gap concerning directory symlinks; the driver
now rejects them explicitly. Final read-only follow-up confirmed that finding
resolved and no remaining actionable finding, without compiling or executing
anything. The prepared driver SHA256 is
`0e4f59ecc33350eb097d4bfe262a54a53853c2e00078f04275035bca7b2c09c5`.
This review establishes source-level protocol acceptance, not runnable status
or performance evidence. Accepted files remain byte-identical to
revision1860/acceptance1864. No script compile/execution, fixture construction,
measurement, build, probe, test, JAR creation, or Git mutation is performed by
preparing these files. Parent will issue the exact execution transfer after563
terminal release; this task remains linked/in_progress until then.
