# Integrated math validation: tasks 538, 567 and 541

This is the historical ff80 snapshot report. Later task 530 division evidence
and its exact candidate are recorded in
[normalized division verification](normalized-division-verification.md).

This report records completed local integration gates and a bounded modPow
measurement campaign. It does not establish complete public-test coverage,
official JCK certification or blanket performance acceptance. No new execution
is authorized by this report. Historical source milestones marked UNRUN remain
historical; the evidence below records the later runs without rewriting them.

The integrated candidate is
`/private/tmp/krogu-root538567541-validation-v1/artifacts/integrated-fresh.jar`,
SHA-256 `80ea49801d8de21aafc4ac76488a389363c706e3b1e2d801b8e346660e43fb68`.
Its source snapshot contains BigInteger `ff80e3cd…`, BigDecimal `9cbaef67…`
and NumericExtensions `20fdf7c5…`. The new power-bound V4 proposal
(`1ff4d77d…`) is accepted at SOURCE ONLY level, still UNCOMPILED / UNRUN and
unintegrated. Native task 530 worker 5c644211 completed its SOURCE ONLY milestone
and was archived. Its source passed independent static review but remains
UNCOMPILED / UNRUN and unintegrated. The current BigInteger source is ff80.

The mathematical changes are independent common Kotlin. Direct contextual
`plus`/`round` now give zero results scale 0, including at precision 0, as specified
by the public API. Private rounding seams retain the separate preferred-scale
rules of constructors, arithmetic and square root. The chosen stored-zero unary
policy is recorded separately from required explicit override dispatch.
Contextual Double conversion keeps the canonical factory's coefficient/scale
pair, including signed-zero conversion 0@1. The finite oracle classifications
preserve complete input-key checks and all other comparisons.

Odd-modulus exponentiation adds a private radix 2^30 fixed-modulus context with
bounded scratch storage and a proved tiny-power fallback. Public validation,
even-modulus handling, primality/RNG behavior and existing fallback calculations
are retained. No constant-time or cryptographic guarantee is asserted. The task 541
overload inventory and three common controls are also included; their finite
coverage does not complete the 39-entry decimal audit.

## Behavioral gates and the preserved first failure

The final source-derived XML audits passed all four supported local targets:

| Target | Suites | Method executions | New 538/567/541 executions | Failures/errors/skips |
|---|---:|---:|---:|---:|
| JVM, including Java and migration tests | 122 | 416 | 34 | 0/0/0 |
| JS | 58 | 277 | 27 | 0/0/0 |
| Wasm JS | 58 | 277 | 27 | 0/0/0 |
| macOS ARM64 | 58 | 277 | 27 | 0/0/0 |
| Total | — | **1,247** | **115** | **0/0/0** |

All final audits report no missing or extra methods, duplicates, XML anomalies
or new-method inventory anomalies. The JVM new-method set is 538:16, 567:15, 541:3;
each other target uses the common set 538:10, 567:14, 541:3. These are method
executions across targets, not 1,247 distinct test definitions.

The first complete JVM run executed 416 methods with 415 passes and one failure:
PortableOracleTest rejected the finite zero-policy's exact input-key set. The
ordinary unary schema stores reported sign in the operation suffix and literal 0
in its reserved field. Two helper key constructions and one live JVM consumer
key construction had encoded those fields incorrectly. The narrow correction
changed those three constructions and corresponding proof wording; it retained
all 212 selected cases, frozen vectors and result/error/identity/trace checks.
The resulting helper and consumer hashes are `e524aebf…` and `6791b171…`.
Production was not changed for this correction. A full JVM replay then passed 416;
the other three full target gates followed.

The original failure remains in
`/private/tmp/krogu-root538567541-validation-v1/first-jvm.log`, `first-audit.log`,
`first-xml/`, `first-results.tar` and the initial `source-before.tar`.
The final replay has separate `retry1-source-before.tar`, `retry1-results.tar`
and `retry1-xml/`; final audit logs are `retry1-audit.log`, `js-audit.log`,
`wasm-audit.log` and `mac-audit.log` in the same directory. The corrected final
input manifest stayed identical before the replay and after JVM, JAR, JS, Wasm
and macOS work: SHA-256
`52375341d60d4db8f246db17ca91180a572d40dd625e15ad81b49aea49995899`.
This stability claim applies to that final corrected snapshot, not to the
earlier snapshot before the explicitly recorded key correction.

## Fresh matched artifacts and bounded modPow measurements

Candidate compilation was forced and completed in 7 seconds (`fresh-jar.log`).
The matched baseline was freshly rebuilt in 13 seconds, with unchanged inputs
before and after; its SHA-256 is
`64255072cdaea0e8815d6c77be68539b09ceb8dd24917b02099761d19321ed62`
at `/private/tmp/krogu-538-integrated-pair-evidence-v3/artifacts/baseline-fresh.jar`.
Its equality to historical JAR bytes does not supply freshness evidence; the
actual forced build log and input manifests do. The full matched input diff
contains only BigInteger ff80→01d8, obtained by reversing the exact 538 patch,
retaining 528 metadata and the 567/541 composition. Both build logs and full input
bindings are recorded by `/private/tmp/krogu-538-integrated-pair-evidence-v3/pair-ledger.tsv`,
SHA-256 `26c3425855084fbaf660361853afc7354f9c8ad3da100a53b01868eb34c5666b`.

V3 completed 16 fresh measurement children, 28 individual CALL records and 16
TOTAL records. Every numerical result matched the checked public-host result.
All 69 captured owned process accounts ended DEAD, with no signals or unresolved
identities; the separately captured guardian PID was absent. The raw directory
is `/private/tmp/krogu-538-integrated-runtime-v3/`; `calls.tsv` retains every call,
`samples.tsv` retains every total, and per-pair logs retain origins and values.
The complete guardian log is
`/private/tmp/krogu-538-integrated-pair-evidence-v3/measurement-outer.log`.
The frozen evidence index `/private/tmp/krogu-538-integrated-v3-runtime.sha256`
has SHA-256 `0f804608cb62e92213a1ee59b83c8269ebe731010cb9bc9f8f6fdb829750a9bf`.

The following are TOTAL candidate/baseline ratios copied from that frozen log;
smaller than 1 favors the candidate. The raw measured totals are in samples.tsv.
Each row is one cold batch, without warmup or statistical replication.

| Pair | Cohort | Limbs | K public calls | Wall C/B | Thread CPU C/B | Thread allocated bytes C/B |
|---|---|---:|---:|---:|---:|---:|
| 1 | tiny, dense | 1 | 1 | 0.92030 | 0.92222 | 1.00000 |
| 2 | tiny, dense | 142 | 1 | 0.48874 | 0.48357 | 1.00000 |
| 3 | ordinary, random | 18 | 1 | 0.03620 | 0.04245 | 0.00050236 |
| 4 | ordinary, random | 142 | 1 | 0.02924 | 0.03202 | 0.000012462 |
| 5 | guard boundary-at, sparse | 18 | 1 | **1.01478** | **1.01651** | 1.00000 |
| 6 | guard boundary-over, sparse | 18 | 1 | **4.09347** | **3.86364** | **1.52181** |
| 7 | tiny, dense | 142 | 4 | **1.05637** | **1.04175** | 0.99820 |
| 8 | ordinary, random | 18 | 4 | 0.04131 | 0.04602 | 0.00012820 |

All regressions remain evidence. Pair 6 measured 1,691,458 ns versus 413,209 ns
wall time and 160,472 versus 105,448 allocated bytes. Its actual operands are
a=2^255, e=2, m=2^510+3: the generic sufficient guard misses even though a²<m.
Pair 5 has smaller wall/CPU regressions. Pair 7 regresses in wall/CPU TOTAL;
all four individual wall calls also regress. Its individual CPU changes are
mixed, and its total allocated bytes are slightly lower. No unfavorable call
was filtered from calls.tsv. V4 proposes a narrow extra stored-power predicate
for investigation; it has not repaired these measured V3 observations.

The measured child JVMs used pinned Liberica 25.0.4+9-LTS and Kotlin 2.4.21 with
`-Xms32m -Xmx256m -XX:+UseSerialGC -XX:ActiveProcessorCount=1
-XX:-OmitStackTraceInFastThrow`; native-access/Unsafe allowance and kotlin.home
flags are retained verbatim in the raw logs. The unchanged limits were 30 seconds
per child including compilation/oracle/startup, 70 seconds per supervisor,
720 seconds for the campaign, at least 100 seconds remaining before launch, and
campaign cleanup bounded to 12 seconds. No retry or automatic limit increase occurred.

These are descriptive observations from cold single batches, not statistically
stable benchmark estimates. Wall time includes scheduling effects; thread CPU
has different scope and clock granularity and can slightly exceed a very short
wall observation. Supported ThreadMXBean allocated-byte deltas count cumulative
allocation on the measured thread, including first-use/reflection overhead;
they are neither resident memory nor retained heap. A baseline can cumulatively
allocate more than its 256 MiB maximum heap while GC reclaims objects.

An uncontended host is not established. Before/after snapshots in the pair-evidence
directory record 36 GiB physical capacity, roughly 13,215→13,191 MiB swap used,
substantial compressed memory and load averages around 5. There is no reclaimable
memory or isolated-host claim. K4 repeats unchanged public modPow calls with
immutable operands in one JVM; it provides no private hook or shared-context
reuse. No setup component, allocation site or reuse crossover was measured.
Guard labels are reviewed source-ledger declarations, not observed private
dispatch. Task 538 performance acceptance remains open despite ordinary-cohort
improvements in this finite campaign.

## Isolated null8 coverage metadata validation

The accepted mapper source `e94afbc7…` was first evaluated in
`/private/tmp/krogu-526-null8-validation-v1/root/`. Its old coverage artifact
`06e5cc81…` and new artifact `431cc1e1…` retain identical 75729 noncomment payload
lines, including the header: cmp exited 0 and both payload hashes are `892bab81…`.
The generated dictionary changes exactly eight logical field 12 statuses,
D07425–D07432/C0609–C0616, to REQUIRED_NULL_ARGUMENT_CONTRACT. Only exception
TYPE is mandatory there. Trace/message observations, unobserved identity,
latent poison flags and all 75,728 archival blocks remain; every other logical
row and every payload byte is unchanged. This is not a production repair,
blanket waiver or archival clearance.

The initial extraction omitted the diagnostic TSV and stopped before helper
execution; setup-notes.txt preserves the omission and subsequent exact-hash
copy. The first --check then exited 3 against the expected old manifest. That
failure is preserved as first-check.log; generate and final --check exited 0,
with logs and coverage.diff alongside both full payloads. The validation used
metadata only and evaluated no production or JDK arithmetic. These isolated
results were independently reviewed before installing the exact mapper and
generated metadata in the root tree. The original v2 artifact and all failed
setup/check evidence remain archived.

After integration, a separate root-tree `--check` also exited 0. Its log is
`/private/tmp/krogu-526-null8-root-integration-check.log`; it verified the same
75,728 mappings and 11,472 valid class-mask checks without evaluating arithmetic.

## Public campaign and remaining work

Campaign 1792039864274 is terminal: harness session 15900 exited 1. The final JSON
records **17 PASS, 3 FAIL and 120 SKIP**. The skips comprise 98 filtered
invocations, 16 disabled, 4 private and 2 commented entries; they supply no executed
public coverage. All 10 selected port invocations verified their core class
loading, and no OOM was recorded in those invocations. All seven selected
BigDecimal files passed in both phases. Baseline BigIntegerTest's two directives
and PrimeTest passed.

The two port BigIntegerTest directives failed by their original deadlines:
120,218 ms for the default 120-second invocation and 400,229 ms for the extended
400-second invocation. Both last reported `inverse2760bits` passed; the next
scheduled batch is 1,000 inverse cases at 8,000 bits. The logs do not establish
progress within that batch. Their Prime/nextPrime sections passed; these section
successes do not complete either invocation.

Port PrimeTest exited 1 after 22,001 ms, without a timeout. Known-prime, non-prime
and first 19 Mersenne sections reported SUCCESS. Then checkHugeFails expected
isProbablePrime(1) to throw for a=2^500000001+1, but the call returned normally.
The test discarded its return value, so this records neither a false positive
nor an observed false result. Own-source analysis predicts the small factor 3
would yield false, but the actual return was not recorded and is not inferred
as an observation. The retained failure remains unchanged.

Independent review classifies the failure of that exact size-exception assertion
as a difference in the permitted implementation range. The [public BigInteger contract](https://docs.oracle.com/en/java/javase/25/docs/api/java.base/java/math/BigInteger.html)
requires a probable-prime range extending at least through 2^500000000; it does
not mandate that exact ceiling. The method's too-large exception wording does
not supply one. No production cap or test modification follows, and all three
FAIL entries remain in the ledger. This classification does not prove the
port's entire supported primality range or turn the original invocation into PASS.

The exact artifacts are
`/private/tmp/krogu-public538567541-first.log` and
`/Users/yoda/dev/pet/krogu-math/build/math-tck/results/1792039864274/`, including
results.json and original per-invocation stdout/stderr/class-loading records.
The JSON has SHA-256 `56cc04fee5f529c6bf9ff41700635e4ef9d42d789ea67da649a79e7b6a740d01`.
The complete first-result archive is `/private/tmp/krogu-public538567541-results-first.tar`,
SHA-256 `9ebf76d4e3c50d701971c6f56d3a05dbb7c7aea38897633d2a15b632fd46c506`.
This report claims no total public PASS and authorizes no retry or source edit.
The harness seed 559 does not seed the port's internal Random.Default witnesses.

Task 525's JS Number work is unmerged/open; broader work on tasks 526, 541, 538
and 559 remains open. V4's source acceptance supplies no runtime acceptance;
the archived task 530 source milestone still needs execution. Further integration and
execution require their separate scopes and grants. The published six-platform
CI result at historical bbdd remains historical; exact new-head CI is pending.
No official JCK has been run. Compatibility and performance claims must stay
within the completed evidence above until the remaining work is resolved.
