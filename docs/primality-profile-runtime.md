# Bounded primality diagnostic: root1959 runtime evidence

The corrected diagnostic completed in 366.311629875 seconds with exit 2: three
normal calls returned true and four reached their 60-second call-phase limit.
A separate fresh profile of p=2203 and a separate host baseline returned true.
This is a completed diagnostic with retained failures, not public-test PASS,
a performance fix, an arithmetic-error finding or official JCK/TCK evidence.

Evidence is `/private/tmp/krogu-primality-profile-root1959-v2`; parent log is
`/private/tmp/krogu-root528566-validation-v1/primality-profile-parent-v2.log`.
The independent review read existing text/hash metadata only and started no
compiler, script, numerical fixture, test or profiling process.

## Exact artifact and retained first failure

Executed script SHA-256:
`72a33dd507e841357d8a9921279aff987f79302c6b8f71faa29fb89cc319f27a`.
The selected root1919 artifact is
`/private/tmp/krogu-root526565525-validation-v1/artifacts/root-integration-fresh.jar`,
SHA-256 `daf2a13324f780b4b592e946c24dc601fc678d37f4954f0907fdbaea02674bdd`.
Its copied provenance report remains SHA-256
`31d0abd229951a75320ed1bd83d61799a8856412187a602ded3a8ee7d6767a97`.
No current-source hash is retrospectively assigned to that JAR. It predates
the subsequently composed528/566 snapshot.

The original 32459 script attempt exited 3 before any child was launched:
an optional public OS memory metric threw `InternalError: errno: 1 error:
sysctlbyname failed`. Preserve
`/private/tmp/krogu-primality-profile-root1959-v1`, the original parent log
`/private/tmp/krogu-root528566-validation-v1/primality-profile-parent.log`, and
the original script/doc in
`/private/tmp/krogu-root528566-validation-v1/profile-initial-source/`.
The actual script diff is solely an additional `InternalError` catch returning
NA for that optional metric. It does not catch OOM or numerical failures,
change operands/certainty/deadlines, or grant execution. Root independently
reviewed that correction before the fresh v2 attempt; neither attempt is erased.

## All nine attempts and supervision

All seven normal fixtures are exactly `ONE.shiftLeft(p).subtract(ONE)` at
certainty 100, constructed before READY. Each fresh JVM receives one call after
parent GO. The profile selects the smallest timed-out exponent, not the slowest
ranked member of the censored group.

| Attempt | Captured PID | Result | Measured call seconds | Exit |
|---|---:|---|---:|---:|
| normal521 |88919|TRUE|1.693206000|0|
| normal607 |88930|TRUE|2.503274625|0|
| normal1279 |88971|TRUE|15.467302708|0|
| normal2203 |89109|CALL_TIMEOUT|NA|143|
| normal2281 |89380|CALL_TIMEOUT|NA|143|
| normal3217 |90011|CALL_TIMEOUT|NA|143|
| normal4253 |90426|CALL_TIMEOUT|NA|143|
| fresh profile2203 |90677|TRUE|70.642865292|0|
| fresh host baseline2203 |90939|TRUE|0.081678500|0|

For every row, captured PID/start instant equals the child's recorded identity.
Each `parent.tsv` records terminal=true, truncated=false, reader_failure=NA,
signal_refused=false and cleanup_problems=[]. The timed-out children have no
DONE or final measurement/result record: no Boolean or completed numerical-call
duration is inferred for them. Their observed parent call phases lasted
60.185–60.243 seconds, including GO/reporting overhead and 100ms supervisor polls.
These censored records cannot establish an exact completion-time ranking.
The separate profile result does not replace its earlier normal timeout.

Observed startup-to-GO was 3.204–3.810 seconds, below the 30-second startup budget;
all cleanup observations were below 0.039 seconds, below the 10-second cleanup
budget. Profile/baseline call-phase limits remained 90/10 seconds. The whole
campaign remained within 900 seconds. Direct owned JVM cleanup uses captured
identity, with no process-name or descendant search. Terminal evidence covers
those nine owned JVMs and capture threads, not an unrelated system-wide census.

All nine child records retain the pinned BellSoft 25.0.4+9-LTS/Kotlin 2.4.21 runtime,
exact 512MiB/SerialGC/assertion-enabled flags and distinct keyed script caches.
All four owned core class origins/resources match the selected JAR in each
child. The baseline constructs and calls public `java.math.BigInteger` via
reflection; owned core loading there is provenance setup, not its timed API.
The full argv records and injected-environment/cache checks remain in evidence.
Every stdout is empty; each 928-byte stderr contains the retained Kotlin/JDK
native-access and deprecated-Unsafe warnings. No capture truncation or OOM was
reported. Parent and child 512MiB heaps coexist; timestamped physical/swap/VM
readings are point observations, not available-memory reservations or proof of
an uncontended host.

## Counters and actual sampling

Completed-call counters are supported computation-thread deltas:

| Attempt | Thread CPU seconds | Allocated bytes |
|---|---:|---:|
| normal521 |1.628873|15448553152|
| normal607 |2.418392|23219513392|
| normal1279 |14.948512|175296016808|
| profile2203 |68.186054|818625623872|
| host baseline2203 |0.078891|16831712|

These are cumulative allocations during one reflective-call window, including
the documented counter/marker/dispatch overhead. They are not retained bytes,
peak heap, object layout or allocation-site measurements. Profile GC deltas are
process-wide: Copy 24730 collections/2260ms; MarkSweepCompact 0/0ms. They are not
attributed exclusively to the computation thread.

`profile-2203/stacks.tsv` contains 319 raw rows, all with frames_truncated=false.
The first row observes the setup counter read after the measuring marker;
318 rows contain owned math frames. First-to-last retained timestamps span
approximately 70.445 seconds: observed mean spacing is approximately 0.22 seconds,
not a measured 100ms cadence. Stack retrieval, scheduling, counter reads and
GC can perturb that cadence. The file is below the 900-row/32-frame/2MiB caps.

All 318 owned-math samples contain the retained witness/modPow/multiplyMod/mod/
remainder chain. The summary includes 181 occurrences at binary division's
shift step, 89 at its conditional add and 24 at its subtract step. These nested
frame frequencies are observations of sampled stacks, not exclusive CPU
percentages or bytes attributed to those frames. Raw foreign frame names were
read as stack text only; no foreign implementation body was inspected.

## Proportional next investigation

Task 538 should focus its source design on repeated reduction in the owned odd
modular-exponentiation path. The retained stacks plus high cumulative allocation
support binary long division and its immutable shift/add/subtract intermediates
as a bottleneck hypothesis. They do not isolate every allocated byte or prove
which replacement will improve total time. A local modulus context/odd reduction
proposal should first supply carry/range/input-immutability proofs and bounded
independent validation while retaining public dispatch, negative exponents and
even-modulus CRT behavior. No kernel rewrite or benchmark is accepted here.

Calls are explicitly UNSEEDED: this public API internally uses Random.Default
and exposes no seed argument. The host baseline uses its own algorithm and
witness selection; the two single-call durations are descriptive observations,
not a controlled speed ratio, statistical result or universal complexity claim.
Do not reduce certainty, witnesses or the seven-input domain, and do not add a
known-prime cache as a fix. Original public559 timeout FAILs concern 54ec and
remain intact. An unchanged public PrimeTest rerun on a selected newer artifact
is still required; this independent diagnostic cannot supply that PASS.

Key evidence SHA-256:

| File | SHA-256 |
|---|---|
| run.tsv | `15bb58af254a7000e85e00d78a95abf3a8cc2607203971cb31ad97f77a5f7d2a` |
| attempts.tsv | `cad6b4b7a2a00da65d920882c2318d8469d899cf5bdaa907d77c2a1aa2702502` |
| profile2203 child.tsv | `77ab7610275f8ff0d5c41af2e69c5039478a01c0d0f280db2a6ab65a674186db` |
| stacks.tsv | `3627238df70c8f6b3d6d4f8eb53de940b0bdcc6fe5fe9d3e3ea611ea1394a644` |
| own-frame-frequency.tsv | `85ac2ccfff94f808e12d78622e8510acc2cad4839191d6077a51179521818371` |
