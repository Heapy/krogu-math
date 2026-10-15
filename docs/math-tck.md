# Public OpenJDK math harness

This harness uses public OpenJDK tests and is separate from official JCK/TCK
access or certification. Executable runner code has passed a selective host/port
smoke run; broader campaigns and final review remain open. Task `local:559`
remains `in_progress`. Code-only comment1753 was superseded by execution grant
**1772** after560's terminal release and parent integration. This worker has run
no Gradle/Native build.

## Metadata preparation

From the repository root, use Kotlin **2.4.21** and BellSoft **25.0.4+9-LTS**:

```sh
JAVA_HOME=/Users/yoda/.sdkman/candidates/java/25.0.4-librca \
KOTLIN_MAIN_KTS_COMPILED_SCRIPTS_CACHE_DIR="$PWD/build/math-tck/script-cache" \
kotlinr -J-Xmx256m tools/run-math-tck.main.kts --prepare
```

One command fetches the exact allowlisted sources and pinned framework artifacts,
verifies hashes/licenses, and writes generated tests and `inventory.json` under
ignored `build/math-tck`. No repository checkout or production source download
occurs. Corrupt existing bytes fail rather than being silently refreshed.
`--verify` checks cached source/dependency hashes **and generated-source equality**
without fetching or rewriting. Missing cache entries fail.

`--plan` prepares the same files and parses every supported jtreg invocation,
without launching javac, javap, a JVM test or Gradle:

```sh
kotlinr -J-Xmx256m tools/run-math-tck.main.kts --plan \
  --profiles standard,serialization --os-memory-gib 0
```

Zero capacity is deliberately unknown in metadata plans: memory prerequisites
cannot become an executed pass. The current inventory has 57 files, 53 prepared
test files and 59 original directive/implicit-main invocations. All 53 test files
are `NOT_RUN` in metadata mode. A plan's `PLANNED`/`SKIP` states describe
eligibility, not execution results; actual results use timestamped directories.

## Execute only after the next parent grant

The following is the prepared **single clean-fetch baseline + port command**.
The parent must supply the exact grant comment ID, jar and authorized capacity;
the placeholders are not permission or default values:

```sh
JAVA_HOME=/Users/yoda/.sdkman/candidates/java/25.0.4-librca \
KOTLIN_MAIN_KTS_COMPILED_SCRIPTS_CACHE_DIR="$PWD/build/math-tck/script-cache" \
kotlinr -J-Xmx256m tools/run-math-tck.main.kts --both \
  --execution-grant EXECUTION_COMMENT_ID \
  --allow-owned-timeout-cleanup \
  --port-jar EXACT_PARENT_SELECTED_JAR \
  --profiles standard,serialization \
  --max-heap-mib GRANTED_HEAP_MIB --os-memory-gib ACTUAL_OS_MEMORY_GIB
```

`--baseline` runs original numerical test bodies with the one documented nested
process-scaffold adaptation; `--port` runs port adaptations. `--both` runs
all baseline invocations serially, then port invocations. No Gradle build is
performed. The jar is mandatory for port modes and is identified by SHA-256.
Execution requires an explicit operator attestation; the script does not query
or independently authenticate tracker grants. Comment 1753 is explicitly rejected.
`--allow-owned-timeout-cleanup` must be included in the parent's execution grant:
only child processes created by this invocation may be terminated on timeout;
there is no PID search, cancellation of another worker, or CI cancellation.

`--tests BigDecimal/AddTests.java,BigInteger/ModPow.java` selects exact files;
every unselected test invocation still receives a `SKIP` with lost coverage.
An invocation with no actual test attempts exits nonzero; an empty corpus cannot
be a pass. Every compilation failure remains `FAIL`; no source pruning occurs.
The first Java-helper/framework validation passed for six selected files in both
phases. Other caller files retain independent compile/result records.

## Provenance and dependency boundary

Official `openjdk/jdk25u` tag `jdk-25.0.4.1-ga` resolves to commit
`7b65d74bcbd7c3a0b4c1f5c5bd85e64aa2e51b4a`; `provenance.json` also retains the
annotated tag object and three test subtree hashes. `source-lock.tsv` records
Git blob SHA-1, SHA-256 and size for 56 subtree files (28 BigDecimal, 27 BigInteger
including private-access test helpers, one RoundingMode) and the sole additional
support source `test/lib/jdk/test/lib/RandomFactory.java`. Its dependencies are
only java.util PRNGs. `manifest.tsv` records each file's scope/resources/lost coverage.

Tests are **25.0.4.1**; the BellSoft reference is **25.0.4+9-LTS**, release source
`.:git:cb897ec0b2a9+`. Same major/update does not establish source equivalence.
Execution validates the JDK release metadata and actual JVM version, records the
mismatch, VM command lines, OS/architecture, source/adapter/tool hashes and jar hash.
Kotlin stays **2.4.21**, Gradle **9.8.1**. Ambient classpaths and injected JVM options
are removed from the harness's child environment.

Real TestNG **7.11.0** and JUnit console standalone **1.13.4** runners are pinned,
including TestNG's SLF4J API **2.0.16**, JCommander **1.83**, jquery **3.7.1**, and
Kotlin stdlib **2.4.21** / annotations **13.0**. All seven jars and seven POMs have
frozen SHA-256 plus official Central checksum values. The first smoke used the
real framework binaries. The JUnit console API was checked against its
[pinned API documentation](https://docs.junit.org/5.13.4/api/org.junit.platform.console/org/junit/platform/console/ConsoleLauncher.html).
Bundled notices, including JUnit4/EPL-1.0 and Hamcrest/BSD-3-Clause, are retained.
See [licensing](../tools/math-tck/LICENSING.md) for the exact boundary.

All OpenJDK files are GPL-2.0-only without Classpath Exception. Source-specific
`adapt.main.kts` is GPL-2.0-only; independently authored orchestration/helpers
are Apache-2.0. Generated files retain original notices. Downloaded sources,
derivatives, framework jars and combined test binaries stay under ignored build
and are never inserted into library publications. No numerical production
implementation from OpenJDK, Kotlin stdlib or another library is read or copied.

## Assertion-preserving adaptations

Ordinary tests change math imports only. Every generated source has an inverse
transformation check and unchanged upstream notice. Each test/directive compiles
in its own directory because upstream class names repeat across subtrees.

Four explicit source-specific adaptations retain numerical test bodies:

- **ModPow65537:** move the original RSA statements into an optional host-provider
  helper and bridge only its three exponent arguments. The port invocation runs
  all three unchanged modular-power known-answer assertions. The original baseline
  also runs key generation and signing. The port provider phase is recorded as a
  separate `SKIP`, never krogu arithmetic evidence. Original key generation has
  unseeded provider randomness; this is documented baseline integration behavior.
- **DoubleFloatValueTests:** retain the complete source, including Float16 methods.
  A bridge at the single Float16 conversion call allows type adaptation; it is
  not numerical port evidence. Port execution invokes all ten original double/float
  methods reflectively, with every original assertion. Each of the five Float16
  test groups is listed as lost host-converter integration coverage in the inventory.
  The original baseline runs all groups. The vector module directive is retained.
- **ByteArrayConstructorTest:** retain both original racy constructors, threads,
  duration property, Jupiter lifecycle and `assertTrue` expression. Test-only
  `ArrayAccessor` reflects krogu's own `words`, copies/reverses its word order and
  exposes the leading-word view. It does not implement arithmetic or fall back to
  host math. The assertion becomes a krogu storage-normalization observation;
  JDK-specific `mag` layout coverage is explicitly lost. Public constructor race
  execution is retained. The original baseline compiles its original `Accessor`
  with a narrow java.base patch. Missing own storage fails rather than passing.
- **ModPowPowersof2:** replace only the parent process scaffold in both phases;
  the nested ModTester numerical body, three calls/arguments and literal `success`
  stay unchanged. The original parent accepted any first stdout line and ignored
  exit/termination. ChildJvm/ChildEntry now propagate heap, `-ea`, seed, diagnostic
  flags, phase and exact port jar. CoreGuard writes child proof to stderr; stdout
  must contain exactly `success`, exit must be zero, and completion/child class-load
  evidence must agree. Child command, terminal properties and logs are retained.
  The child has the original parent timeout budget less ten seconds for wrapper
  startup/cleanup. Both layers wait for termination; owned descendants are tracked
  and cannot outlive a passing parent. Baseline orchestration is thus a controlled
  stronger crash assertion, explicitly separate from untouched numerical code.

Only `MutableBigIntegerShiftTests.java` and `MutableBigIntegerBox.java` remain
excluded: internal mutable limb-shift invariants are not a public numerical API.
Their lost coverage is recorded separately in both baseline and port results.
Current-universe serialization round trips, including subclasses, remain selected;
no legacy/cross-format payloads are added. The BigDecimal concrete unscaled-value
class assertion and historical BigInteger hashes remain observations to classify
if they differ. Cross-universe enum/context identity hashes are not numerical identity.

Eight upstream-disabled LargeValueExceptions methods remain disabled:
`squareNoOverflow`, `squareIndefiniteOverflowSuccess`, `multiplyNoOverflow`,
`squareIndefiniteOverflowFailure`, `multiplyIndefiniteOverflowSuccess`,
`multiplyIndefiniteOverflowFailure`, `powOverflow3`, `powOverflow4`. Their unchanged annotations,
names and separate `SKIP` coverage records prevent implying they ran.
SymmetricRangeTests also comments out `testModInverse()` upstream; that unscheduled
probe is retained and receives its own lost-coverage record.

## Directives, resources and evidence

Every explicit `@run` is separate, including autobox-cache VM variants. Tests with
no run directive use their original main. Fresh JVMs preserve isolation at least
as strong as `othervm`. Original flags, arguments, timeout values, requires/module
predicates, library and support-build intent are retained; unknown directives fail.
The jtreg default timeout is 120 seconds. Compiler/javap JVMs use 256MiB. Timeouts
are failures, with logs and owned-child cleanup only after explicit authorization.

`-Dseed=559` seeds RandomFactory; source-fixed seeds remain unchanged.
`BigIntegerTest` uses `-Dsubset=0` for both original run directives;
`SymmetricRangeTests` also uses `-Dsubset=0`, preserving all four scheduled regimes.
Sizes/batches remain unchanged. Numerical and exception-message runs use
`-XX:-OmitStackTraceInFastThrow`; assertions are enabled.

| Profile | Requirement / scope |
|---|---|
| standard / serialization | Ordinary framework/main tests and own serialization, default 512MiB test heap |
| heavy | Full BigIntegerTest, prime/certainty and Fibonacci(1,000,000) parallel multiplication |
| memory-512m | ExtremeShiftingTests, 512MiB heap and upstream OS memory >=1GiB |
| large-memory-4g | LargeValueExceptions, 64-bit VM, OS >=4GiB, 4GiB heap |
| large-memory-8g | Division/StringConstructorOverflow: 64-bit VM, OS >8GiB, 8GiB heap; billion-bit BitLength/DoubleValueOverflow also use this profile |
| large-memory-10g | SymmetricRangeTests: 64-bit VM, OS >=10GiB, 8GiB heap, CompactStrings, 180 seconds |

Profiles/capacity barriers receive explicit per-directive `SKIP` records, never
domain waivers. Observed physical memory must cover the declared capacity.
Every test launch records a fresh MXBean memory sample. The parent-required
8/10GiB profile gate requires known free memory to cover the heap plus a 512MiB
orchestration margin. macOS free pages exclude reclaimable cache; this conservative
large-profile gate does not invent availability or gate granted 512MiB standard
runs. Standard runs retain their granted heap limit and unresolved OOM classification.
The nested child case records both JVM heap budgets.
Samples and exact barriers are recorded. The four overflow tests that print skipped OutOfMemoryError but
exit zero are detected as incomplete assertions and `FAIL` / unresolved
resource-or-implementation barriers. A resource SKIP requires concrete capacity,
baseline and input evidence; same-heap baseline PASS plus port OOM is investigated,
not waived. ToPlainStringTests instead deliberately asserts
preemptive OutOfMemoryError for 14 impossible-length strings; those original
assertions remain intact and are not treated as swallowed resource skips.

Port launchers call CoreGuard before the test class is initialized. It verifies
all four math type locations equal the exact parent jar. Compiled test and nested
classes are audited with javap: host java/math member owners fail, core references
are required, and only the two explicit source-specific host bridge names are
permitted. Class-load logs and completion markers must also be present. This
proves binding for the retained caller code, not branch coverage or full compatibility.
At the initial preparation stage, no such execution evidence existed; the later
campaign reports below record the execution evidence and its exact scope.

`build/math-tck/results/<timestamp>/results.json` is checkpointed after each
invocation and includes PASS/FAIL/SKIP, source/adaptation scope, commands, raw logs,
framework reports, elapsed times, seeds, runtime, checksums, resource barriers,
upstream-disabled cases and adaptation companion coverage. FAIL exits nonzero;
unattempted or skipped coverage is not counted as numerical success.

Every numerical failure needs a minimal public reproducer and independent
mathematical/spec classification, retaining pinned diagnostics. No proved JDK bug
is emulated and no port failure may be hidden. Parent fixes 558/560, production
regressions, CI/COMPATIBILITY edits and KMP integration need separate exact grants.
Final review follows real checks; this finite corpus cannot establish the full
migration goal or official JCK status.

## Preparation evidence

Initial milestone1740 verified a clean fetch, byte-for-byte repeated downloads,
cache corruption rejection/restoration and 50 import-only rewrites. Code-only
continuation under1753 verified seven jar/POM pairs against official checksums,
retained bundled notices, inverse-checked all 53 generated tests, parsed all 59
original invocations, and recorded upstream-disabled methods plus the
upstream-commented inverse call. Findings1754/1756 are addressed by forcing both
subset selectors, preserving nested numerical code with independent child proof,
owned-descendant lifecycle tracking, fresh resource checks and `-ea` in both JVMs.
`build/math-tck/code-only-plan.log` and `inventory.json` retain current metadata
checks. Cached `--verify` also passed; the all-profile metadata plan with the
parent's 36GiB capacity metadata parsed 59 eligible invocations without execution.
These are planning observations, not PASS results. Comment1777 corrected disabled
method parsing to all eight forms and required unresolved OOM classification.

Execution grant1772 selected jar SHA-256
`54ec9af64675eba2a89d7cba68a1c404220cfaf3169bd1de8a31193514e31db0`.
The sandbox's sysctl restriction prevented descendant enumeration before javac;
the permission-reviewed local execution retained that environmental failure.
An initially excessive standard free-page gate produced 12 successful compiles
and six bytecode audits but zero test attempts; run1791999132614 is retained with
PASS0/FAIL0/SKIP140 and is not pass evidence. Its gate scope was corrected to the
parent's 8/10GiB-profile prerequisite.

Smoke run `results/1791999260558/results.json` then recorded **12 PASS, zero FAIL,
135 SKIP**, six original baseline and six port invocations: Constructor,
DoubleFloatValueTests, ValueOfDouble, ByteArrayConstructorTest, ModPow65537,
ModPowPowersof2. The skips preserve all unselected/resource/disabled/companion
coverage; this is not a full-corpus pass. Real framework lifecycle/assertions,
Accessor baseline patch, core class locations/logs, test+nested bytecode owners,
and child exit/exact success/core proof passed. Full commands/logs are retained.
Task remains in_progress. Changes are not committed.

The full standard/serialization run `results/1791999552945/results.json` records
**92 PASS, two FAIL, 53 SKIP**, 94 actual invocations (47 host PASS; 45 port PASS,
two port FAIL). Both failures are unchanged ToPlainStringTests directives.
[Independent public witnesses and classification](../tools/math-tck/plain-overflow-classification.md)
preserve the nine exception-type disagreements and five non-preemptive allocations
among 14 impossible-length strings. No production fix or assertion waiver is claimed.

The resource campaign `results/1792000017580/results.json` completed with
**11 PASS, three FAIL, 126 SKIP**, 14 actual invocations. Host passed seven;
port passed four and timed out both full-subset BigIntegerTest directives plus
PrimeTest. [Phase/input performance evidence](../tools/math-tck/primality-timeout-classification.md)
retains baseline durations and distinguishes the known primality phase from an
unproved exact slow input/algorithmic cause. Five 8/10GiB invocations per phase
were not launched: fresh known-free memory was below the 8GiB heap +512MiB margin.
Raw samples range from roughly0.5 to2.6GiB; reclaimable memory was not invented.

Metadata-only `summarize.main.kts 1791999552945 1792000017580` merges completed
campaigns by exact phase/path/directive, rejecting mixed jar/revision/seed evidence.
Actual FAIL takes precedence over PASS; a profile-filtered SKIP cannot replace
an attempt. The unique **public-directive** ledger is **103 PASS, five FAIL,
ten resource SKIP** across118 phase/directive slots. Separate lost-coverage scopes
contain16 upstream-disabled, two upstream-commented, four excluded-private and
seven adaptation-companion SKIP records. Those counts are neither individual
assertions nor full compatibility/certification. Summary with source records is
`build/math-tck/summary-1791999552945-1792000017580/summary.json`.

Parent1790 adopted the text preflight requirement for isolated task564 and
requested terminal handoff. This worker leaves both text failures and all three
timeouts intact, does not replace sealed jar54ec..., and performs no repaired
test rerun before a fresh parent grant. CI/COMPATIBILITY integration remains
outside ownership and must be approved separately after the relevant fixes and
fresh evidence. Task559 remains in_progress; no review/done transition occurs.

## Parent integration checks

The orchestration sources were copied byte-for-byte from the terminal worker
snapshot; the licensing boundary and all retained failures remain intact.
On 2026-10-15, metadata-only `--prepare` from an empty harness workspace and
subsequent `--verify` both passed on Liberica25.0.4+9-LTS / Kotlin2.4.21 with a
256MiB script heap: 57 pinned source files, 14 dependency records and 53 prepared
test sources. Neither command invoked javac, Gradle or mathematical tests.
The recorded numerical results above still refer to their original campaigns
and exact sealed JAR; this integration check does not replace those runs or
resolve their five failures and ten resource skips.

## Targeted plain-string repair verification

Task564 subsequently ran both original ToPlainStringTests directives against its
fresh formatter-fix JAR and the pinned JDK. Run1792016503091 completed all four
baseline/port invocations with PASS, preserving the original559 failures as
historical evidence. Parent independently checked successful compilation and
execution, own-JAR class loading, the bytecode audit and no incomplete OOM
assertions. See [plain-string preflight](plain-string-preflight.md) for exact
artifact/result hashes, four-target regressions and integration limits.

This targeted follow-up resolves those two failure witnesses on that JAR. It
does not replace the original broad campaign, its three primality timeouts or
ten capacity skips, and does not establish a full corpus or official JCK pass.

## Later division and PrimeTest follow-ups

The [normalized-division report](normalized-division-verification.md#public-workload-and-publication)
records the later d0-JAR campaign1792044478663:19PASS/1PrimeTestFAIL/120SKIP.
Both original BigIntegerTest directives completed in41632/42003ms within
unchanged120/400-second limits. Those later passes resolve the earlier timeout
witnesses on that artifact; they do not rewrite the original broad ledger.
PrimeTest completed its first19 Mersenne checks and failed only its mandatory
exception-cutoff expectation above the API's minimum supported prime range.

A subsequent [port-only range-policy adaptation](prime-test-range-policy-559.md)
preserves that original FAIL, the original host baseline and one explicit
lost-cutoff companion. Targeted campaign1792074254976 on current-production
JARded66 completed original baseline and adapted port PrimeTest:
**2PASS/0FAIL/139SKIP**, complete=true, officialJck=false. All other public files
were filtered; this is not a refreshed full-corpus ledger. The adapted PASS
does not record the huge-input branch/Boolean or prove broader range support.
The historical103/5/10, later19/1/120 and current30-adapted/115SKIP campaigns
remain distinct evidence with their original source/artifact/coverage scopes.
