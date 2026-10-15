# Exact da608 regular hosted failure

Finite metadata-only audit of [run38801826086](https://github.com/Heapy/krogu-math/actions/runs/38801826086),
attempt1, source `da6086342165711ac800e0c5c515eb2e6df7aae5`. The run is terminal
FAILURE. Public job116666303454 failed at step6, “Run retained public baseline
and port invocations”; evidence upload succeeded. The API reports six other
jobs SUCCESS: oracle, the four platform-matrix jobs, and linux-arm64. Those six
are API conclusions here, not independent platform XML audits.

Audit workspace: `/private/tmp/krogu-da608-regular-evidence-dAC5t9VZ`. Existing
artifacts/logs and the exact source archive were downloaded; direct JSON/text,
path, comparison and hash checks were performed. No script, compiler, javap,
numerical/model/helper, probe, test, build, process inspection, Git/tracker
operation, retry or workflow dispatch was run. Only this publication report
was added. The later sqrt adapter commit `efe75ff`,582 and584 are not part of
this run; their local evidence cannot relabel its result.

## Complete ledger and sole failure

The actual ledger is complete: **105 PASS /1 FAIL /46 SKIP**,106 attempted
invocations. All152 full phase/id/path/scope/directive/method records are unique.
The118 public-invocation IDs match both phases of the59 inventory plans exactly.

| Phase | PASS | FAIL | SKIP | Sum of primary compile times | Sum of primary execution times |
| --- | ---: | ---: | ---: | ---: | ---: |
| Original host baseline |53|0|17|59,201ms|58,171ms|
| Port |52|1|29|61,668ms|368,696ms|

These are sums of per-invocation timings, not campaign wall times or benchmarks.
The public job ran from20:55:22Z to21:05:39Z on2026-10-15. Its one and only FAIL
is `port/BigDecimal/SquareRootTests/run-1`, public source
`test/jdk/java/math/BigDecimal/SquareRootTests.java`, directive `main SquareRootTests`:

| Field | Actual retained result |
| --- | --- |
| Compile | exit0, timedOut=false,1,311ms |
| Execution | exitCode=null, timedOut=true,120,112ms |
| Reason | `Upstream invocation timeout` |
| Inventory bound / heap |120 seconds /512MiB, standard profile; no test arguments or extra VM directive |
| Baseline counterpart | PASS, compile1,202ms; execution exit0, timedOut=false,203ms |
| Loading / predecessor | coreLoadingVerified=true; baselineStatus=PASS |
| Caught-OOM-incomplete flag | false |

All106 primary compilations succeeded. The only primary execution with a
nonzero/missing exit or timeout is this port record. The other105 primary
stdout files contain their HARNESS_COMPLETE marker; the failed one has none.
All664 recursively referenced raw stdout/stderr/class-load paths exist. The
harness continued and recorded later invocations, so this is not a ledger
truncated at its first failure.

The46 SKIPs comprise12 unselected large-profile invocations,12 explicit adaptation
companions,16 upstream-disabled methods, two upstream-commented calls and four
excluded private-support records. Exact records/reasons remain in results.json.
The adapter companions retain lost coverage for the four constructor compound
null/range calls, five Float16 integration methods, provider integration, the
JDK-storage invariant and prime-range cutoff expectation. None is an implicit
sqrt-range omission: SquareRootTests here is still the imports-only source.

## Raw failure observations and limits

The failed1019-byte stdout contains four own CORE_CLASS origins followed by six
HALF_EVEN/HALF_UP/HALF_DOWN observations for two displayed half-integer inputs.
Its last numeric line is `123456789123456788`. Compile stdout/stderr and test
stderr are empty; no assertion-mismatch exception, failure diagnostic, stack
sample, per-input progress marker or completion marker identifies the stopped
computation. The observed failure category is the120-second deadline. Its exact
stalled input/method and cause are not established by this raw run. Missing
completion prevents claiming remaining mathematical assertions passed.

The three retained SquareRootTests caller-audit metadata records each have
exit0, timedOut=false, no host-math members and no integration-bridge members.
Four own core origins point to `build/public-math-input/krogu-math.jar`. These
are retained audit metadata, not newly executed bytecode inspection. No foreign
numerical implementation body/source was read.

The failed generated primary source hash is
`afb93448ad8b9224e7b6674226ce894a8a7ad746d3c64a55917047723c7387b3`;
upstream source hash is
`1d4abb1ea98e14a3cd28defd54d16f2ee49563e3862e86cad11399f5591274aa`.
Both the full generated source and all original assertions are retained in the
artifact. This audit hashes source bytes rather than reading implementation
bodies. Its stdout hash is identical to the earlier
[regular1b9 failure](public-math-1b9-regular-failure-559.md), but equal output
prefixes do not retrospectively identify either run's stopped input. Counts and
sole failing ID also agree; timings and source/JAR provenance are different.

## Exact provenance and original regime

Evidence artifact12006089175 is
`public-math-evidence-da6086342165711ac800e0c5c515eb2e6df7aae5-1`,3,691,990bytes,
ZIP SHA-256 `9fa117ddbe566c5c8f9872a1d25c056f02d0dae18c442a307a97e83bf017c1e6`.
Producer artifact12005684106 is
`public-math-jvm-da6086342165711ac800e0c5c515eb2e6df7aae5`,127,184bytes,
ZIP SHA-256 `602518a88291969f2c1348abef7029be0c64dde77564797e0ee1c7b7b4f44d04`.
Both ZIPs match their API digests. Producer/consumer SOURCE_COMMIT equals da608;
both included SHA256SUMS pass. Both own JARs match the expected accepted581 bytes
`487cca542c87cd4e03e7f5eee78b0a33cd4ea57748d40d7d9df2ace8de7c616e`.

Exact-ref source archive SHA-256 is
`da96b0d9b45931fac33bb08156e930a44558ceb5b5856e025586aa3b598bf051`.
All229 files in accepted manifest
`e7cfc1fd04d891d02d92ee567215a5167e34574b82f72eab49b155289ec55dfa`
match its extracted bytes. The check output hashes to
`8a7223c444652562da4565f1088dbdf45ffd988e7d398c6c2bad7deb156a835f`.
All106 compiled primary source hashes and13 owned tool-source hashes also pass
against retained source bytes. Results ciExecution agrees with API run/attempt,
repository, source SHA and workflow identity. JAR inspection was hash/metadata
only, with no new decompilation.

The retained regime uses profiles standard,serialization,heavy,memory-512m;
both baseline and port; seed559; BellSoft25.0.4+9-LTS; Kotlin2.4.21;
Gradle9.8.1; assertions; `-XX:-OmitStackTraceInFastThrow`; and512MiB primary heap.
The source tag jdk-25.0.4.1-ga versus runtime revision/vendor mismatch remains
explicit. No timeout, resource profile, test input or assertion was changed here.

Key retained files are `evidence/build/math-tck/results/1792097758701/results.json`,
`evidence/build/math-tck/inventory.json`, the failed/baseline SquareRootTests raw
directories below that results root, `evidence/build/public-math-campaign.log`,
and `public.raw.log`. Complete originals remain unredacted in the private audit.

| Evidence | SHA-256 |
| --- | --- |
| Results JSON | `38cb53b4ef26bc4cbcbcd08378e977544ee1be11ac95c02799e49f3cd2bd1352` |
| Inventory JSON | `5611cc74925648d4aac96b0b6d4c0bb38d28bc1d787e36d101510d41e4bc68eb` |
| Raw public job log | `f748cc6208fe0a5acfdb1af172b282e8a2b389f73f780bb17ffc70055f827ec5` |
| Ledger metadata projection | `870419fb6d76e8e5f1dcbe340247dfc113699e81fd591b2b52e4663f6b69967c` |
| Failed test stdout | `b9d2d3519ebcf76eb761acfbcd587f6af986411351078499e911f24694183139` |
| Failed class-load log | `2ebc168047f85adde728fd0afd834a9d35aedcc87edf14235fe1ddc26461d663` |

The private final index is generated after this report and excludes itself and
its check output; its hash is supplied in the finite handoff. All owned download
and metadata calls are terminal. An initial raw-log download was refused by gh's
terminal-escape output protection; repeating that metadata download with its
explicit allow flag succeeded. One local extraction/check command failed before
launch because its intended working directory did not yet exist; the directory
was then created and the read-only checks completed. Neither is a numerical or
hosted retry. This records an actual regular da608 failure, not official JCK,
full-suite acceptance, a later-source result, all-input proof or practical-range
closure. Parent independent adoption review remains separate.
