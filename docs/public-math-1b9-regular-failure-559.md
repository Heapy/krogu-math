# Exact 1b9 regular hosted failure

Parent adoption of the finite audit at
`/private/tmp/krogu-1b9-regular-failure-Nn3qD8GP/REPORT.md`, original SHA-256
`dbf4462aace08f916ec78a756d89aaa24ba8de0b35edc14e169d3a708de550a7`.
Parent rechecked all1469 indexed entries and the complete ledger's sole FAIL.
The root index check hashes to
`8e9a1c2c8dd6aa929e1387c70f817f331a4dbc5ac7a45f3c4dbf1794aa3895dc`;
the original index is
`d0c882a96d299fdfab2a2071dd3dd2a2cfc335c543fbf3612cc983a089e60b46`.
This annotated copy adds no execution or cause diagnosis.

Finite metadata-only audit of original [run38783134507](https://github.com/Heapy/krogu-math/actions/runs/38783134507), attempt1, at `1b9a5f2e04e4147145465fca829a45abd63cc5fa`. The run concluded FAILURE. The API lists six other jobs SUCCESS and only public job116619276890 FAILURE. Within that job, step6, “Run retained public baseline and port invocations”, failed; evidence upload still completed. This audit downloaded existing metadata, the original public evidence and producer ZIPs, and the raw public job log. No retry, dispatch, compiler, javap, script, numerical helper, probe, test, build, Git/tracker mutation or source edit occurred.

## Complete failure classification

The actual complete ledger is **105 PASS / 1 FAIL / 46 SKIP**, with106 attempted invocations (53 baseline,53 port),152 distinct phase/id/path/scope/directive/method records and the exact118 public IDs from59 plans. Baseline has53 PASS and17 SKIP; port has52 PASS,1 FAIL and29 SKIP. The46 SKIPs preserve12 unselected large public invocations,12 adaptation-companion records,16 upstream-disabled methods, two commented calls and four excluded private-support records.

The first and only FAIL is:

| Field | Retained value |
|---|---|
| ID | `port/BigDecimal/SquareRootTests/run-1` |
| Public source / directive | `test/jdk/java/math/BigDecimal/SquareRootTests.java` / `main SquareRootTests` |
| Compile | exit0, timedOut=false,927ms |
| Execution | exitCode=null, timedOut=true,120113ms |
| Reason | `Upstream invocation timeout` |
| Original selected bound |120 seconds, standard profile, -Xmx512m, no extra VM directive/arguments |
| Baseline counterpart | PASS, exit0, timedOut=false,231ms |
| Loading / predecessor | coreLoadingVerified=true, baselineStatus=PASS |
| Caught-OOM-incomplete flag | false |

All106 primary compilations succeeded. The sole execution with timeout/nonzero-or-missing exit is this port record. The other105 executions have their exact unique HARNESS_COMPLETE markers; this failed stdout has no completion marker. All664 referenced raw stdout/stderr/class-load paths exist. The harness continued after the timeout and recorded the remaining public port invocations; this is not a report truncated at the first failing test.

The failed stdout contains four own CORE_CLASS origin lines followed by six printed HALF_EVEN/HALF_UP/HALF_DOWN observations for the two displayed half-integer values. Its final printed numeric line is `123456789123456788`. The1019-byte stdout contains no failure diagnostic. Test stderr, compile stdout/stderr and caller-audit stderr are empty. There is no retained assertion-mismatch exception, stack trace, sampled stack or per-input progress marker identifying the timed-out computation. Thus the observed failure category is the120-second deadline; neither the exact stalled input/method nor its root cause is established here. Missing completion also prevents claiming that all remaining mathematical assertions were reached or passed.

The three existing SquareRootTests caller audits succeeded with no host-math or integration-bridge members. Its four own core origins point to the bound own input JAR. These are existing audit metadata; no new bytecode or numerical inspection was executed. No foreign numerical implementation source/body was consulted.

## Raw locations and original regime

Paths below are inside `artifacts/evidence/` in this audit workspace:

* Ledger: `build/math-tck/results/1792091671245/results.json`.
* Inventory: `build/math-tck/inventory.json`.
* Failed raw directory: `build/math-tck/results/1792091671245/port/BigDecimal/SquareRootTests/run-1/` (test/compile stdout/stderr, class-load log and three retained caller-audit stdout/stderr pairs).
* Baseline counterpart: `build/math-tck/results/1792091671245/baseline/BigDecimal/SquareRootTests/run-1/`.
* Full retained campaign log: `build/public-math-campaign.log`; complete consumer job log is separately `public.raw.log`.

The raw command retains --both, profiles standard,serialization,heavy,memory-512m, max heap512MiB, observed OS memory auto selection, seed559 and the exact run/source attestation. Assertions and -XX:-OmitStackTraceInFastThrow are enabled. SquareRootTests has no test arguments or explicit timeout override in its retained directive; the parsed120-second harness bound applies equally to baseline and port. The workflow retains330-minute step /360-minute job outer bounds. No timeout, input regime or assertion was changed for this audit. Runtime is BellSoft25.0.4+9-LTS, Kotlin2.4.21, Gradle9.8.1; the jdk-25.0.4.1-ga test/runtime revision/vendor mismatch remains recorded.

## Artifact and source provenance

The original evidence ZIP (artifact12000579257,3661153 bytes) hashes to `731a72b8e97ac1b67f51008a6d0de55b5464eea15274cfbe187e9371ae135ba3`; producer ZIP (11998898233,106938 bytes) hashes to `a9ec502b6a321da3c0e0e7ab37866e656d4844e94afd3d4fe3b4715046b69f1f`. Both match their retained API digests. The complete original ZIPs and raw logs remain unchanged and unredacted. A preliminary local ZIP inspection ran before its download was terminal and failed to find the directory; after the same download completed, its digest matched and extraction succeeded. This was not a hosted or numerical retry.

Producer and consumer SOURCE_COMMIT both equal1b9; their included SHA256SUMS pass. Both own JARs hash to `e7047f824deb0e4f9170ad223037a0d42f49cdfa3025eed16b776ece9be9c5ea`, the same bytes as the accepted579/large1b9 artifact, rather than the later580 JAR. Results ciExecution agrees with API run/attempt/source/workflow identity.

The previously downloaded exact-ref own1b9 source archive is copied unchanged here (SHA-256 `a8bf0c946928ea75a664672f1b1589d876ff9f14b2794e13a98a134aa6eee215`). All223 accepted source pins in manifest `25bc9cce8426a00861c480810ea99aad47c5ac504d91c98582fdf8e4294ccfb4` match its extracted bytes. Full tools/math-tck and entrypoint bytes match the archive. Current metadata hash checks also pass for106 compiled primary sources,57 upstream locked files and13 owned tool files. The failed generated primary source is `afb93448ad8b9224e7b6674226ce894a8a7ad746d3c64a55917047723c7387b3`; its upstream source is `1d4abb1ea98e14a3cd28defd54d16f2ee49563e3862e86cad11399f5591274aa`. Source files were hashed as bytes, without reading foreign numerical algorithm bodies.

## Key SHA-256 hashes

| Evidence | SHA-256 |
|---|---|
| Results JSON | `2119509bec8ea80121592fc8dc1f3af199316b6c40823091d11c6bf77f070589` |
| Inventory JSON | `eb4e9bda90186cab7d1cc8c23c1776e451dfe22edff33e5017eeb5be82ba59c6` |
| Full raw public job log | `7ecf2c044b1c8e9e42c37b3bf53196cbe32fae9fe200f2714d0c0f377a2333d0` |
| Failed test stdout | `b9d2d3519ebcf76eb761acfbcd587f6af986411351078499e911f24694183139` |
| Failed test stderr (empty) | `e3b0c44298fc1c149afbf4c8996fb92427ae41e4649b934ca495991b7852b855` |
| Failed class-load log | `a18028d2596eb0780c11d2d173e31a031bea7a2e9ff5b6cfe80f33d290a48070` |
| Entrypoint | `8383d062feac7a3b2e8eda8f9e2844f7d2dbe6ac8d4e5c575f3fc92f58ad1265` |
| Runner | `ed32beb00f4c6896cfd783f2286f95336fee3cc2616420b9a83687c4c5f32ea0` |

The original152 records, all downloaded artifact bytes, source archive, metadata projections and raw logs are preserved. The final index is made after this report and excludes itself. All owned metadata calls are terminal. Six other job-success conclusions are API observations here, not an independent audit of every platform XML. This establishes a real regular1b9 failure under its retained deadline, not a mathematical mismatch diagnosis, official JCK, full-suite acceptance, later-source result or all-input/resource claim. Parent independent review and any future diagnostic scope remain separate; no runtime is queued or implicitly granted.
