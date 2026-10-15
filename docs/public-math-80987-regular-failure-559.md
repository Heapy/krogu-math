# Exact80987 regular hosted failure

Finite metadata-only audit of original [run38788036659](https://github.com/Heapy/krogu-math/actions/runs/38788036659), attempt1, at `80987e04f11a5abd8bc45b4646bcfe5bd0312043`. The run is completed FAILURE. API metadata records six other jobs SUCCESS and only public job116638950031 FAILURE, at step6, “Run retained public baseline and port invocations”; evidence upload succeeded. Other jobs' conclusions are API observations, not an independent audit of their platform XML. No compiler, numerical helper, Kotlin/script/probe/test/build execution, workflow dispatch/retry, source edit or Git/tracker mutation occurred in this audit.

## Complete actual result

The complete actual ledger is **105 PASS / 1 FAIL / 46 SKIP**, with106 attempted primary invocations,152 unique phase/id/path/scope/directive/method semantic records and the exact118 public IDs derived from59 inventory plans. Baseline has53 PASS/17 SKIP; port has52 PASS/1 FAIL/29 SKIP. SKIPs retain12 unselected large public invocations,12 adaptation-companion records,16 upstream-disabled methods, two commented calls and four excluded private-support records. An ID's presence in this complete ledger does not mean it passed.

The first and only FAIL is:

| Field | Actual retained value |
|---|---|
| ID | `port/BigDecimal/SquareRootTests/run-1` |
| Public TEST / directive | `test/jdk/java/math/BigDecimal/SquareRootTests.java` / `main SquareRootTests` |
| Compile | exit0, timedOut=false,1144ms |
| Execution | exitCode=null, timedOut=true,120066ms |
| Reason | `Upstream invocation timeout` |
| Selected bounds |120seconds, standard profile, -Xmx512m, no extra directive VM flags/arguments |
| Baseline counterpart | PASS, exit0, timedOut=false,222ms |
| Guards | coreLoadingVerified=true, baselineStatus=PASS |
| Caught-OOM-incomplete flag | false |

All106 primary compilations passed. This is the only timed-out/nonzero-or-missing primary execution exit. The105 PASS primary stdout files each have exactly one expected `HARNESS_COMPLETE phase Class` line; the failed primary has none. All666 referenced stdout/stderr/primary-and-child class-log paths exist. The harness continued to record later public invocations, so this ledger is not truncated at the failing test. The separate baseline/port ModPowPowersof2 child records both retain exit0, no timeout, exact `success` stdout and completionVerified=true; port child coreLoadingVerified=true, its four own class-load origins are independently present, and both parent child-verification markers remain in the original logs.

The failed1019-byte stdout contains four correct own CORE_CLASS lines and six HALF_EVEN/HALF_UP/HALF_DOWN printed blocks. Its last numeric line is `123456789123456788`. Test stderr is empty, with no retained assertion mismatch, exception stack trace or per-input progress locator. This establishes the observed120-second timeout; it does **not** establish the exact stopped input/method, an arithmetic defect, OOME or the same cause as any earlier run. No missing later completion/assertions are credited as passing. All failure fields and raw bytes are retained, including the full original failure record in `all-failure-records.json`.

## Regime, caller and loading evidence

Original bounds remain unchanged: profiles standard,serialization,heavy,memory-512m; memory auto selection; max heap512MiB; seed559; assertions enabled and -XX:-OmitStackTraceInFastThrow. All106 retained primary execution commands use-Xmx512m, and all compile commands retain-J-Xmx256m. SquareRootTests has no directive arguments or timeout override; inventory fixes120seconds for both phases. Workflow outer limits are330-minute step /360-minute job. Runtime is BellSoft25.0.4+9-LTS, Kotlin2.4.21, Gradle9.8.1; the recorded jdk-25.0.4.1-ga TEST/runtime revision mismatch remains qualified.

All53 port primary invocations print exactly the four own core classes from the bound `build/public-math-input/krogu-math.jar`, and all53 have coreLoadingVerified=true. The63 existing caller audits completed with exit0/no timeout and no direct host numerical math members. Two expected integration bridge observations remain explicitly separate: DoubleFloatValueTests uses HostBridge.decimal for its host Float16 integration phase, and ModPow65537 uses HostBridge.rsaExponent for host RSA integration. The exact pinned own runner allowlists these class/member pairs; they are not evidence of port numerical behavior in those integration phases. The three SquareRootTests caller audits have no host math or integration bridge members. These are reads of retained audit metadata, not a new javap/bytecode or numerical execution.

Raw paths are relative to `artifacts/evidence/`:

* Ledger: `build/math-tck/results/1792094226248/results.json`.
* Inventory: `build/math-tck/inventory.json`.
* Failed primary: `build/math-tck/results/1792094226248/port/BigDecimal/SquareRootTests/run-1/`.
* Baseline counterpart: `build/math-tck/results/1792094226248/baseline/BigDecimal/SquareRootTests/run-1/`.
* Complete campaign log: `build/public-math-campaign.log`; complete raw consumer job log is separately `public.raw.log`.

## Exact artifact/source and licensing boundary

The original evidence ZIP (artifact12003801795,3677615 bytes) hashes to `bfda498512cd521f8d83d3a5a82e1d957d49fd5e42ddb69953a5ac42bdaa084d`; original producer ZIP (artifact12001144024,115796 bytes) hashes to `e665103289c10c15c51aa5d49bf8a05ae961ae095424491eb97964c80c9f442c`. Both match their original API digests. Both downloads were terminal before verification/extraction. ZIPs and raw job log are preserved unredacted. Metadata-only command/projection corrections are recorded in `METADATA_NOTES.md`, including the initial unquoted API URL refusal, gh log escape-sequence refusal and corrected zsh marker projection. None was a hosted or numerical rerun.

Producer and consumer SOURCE_COMMIT both equal80987; both included SHA256SUMS pass. Producer, consumer and separately accepted fresh local580 JAR are byte-identical:

`4861457772e98ef1716c1c4094c5e5422ee9b0d26235e50d10bb082c4f660df2`.

This also matches the accepted80987 hosted-large JAR. It does not turn this failed normal campaign into a combined pass. API, inventory and results ciExecution agree on run/attempt/source/workflow identity.

The copied exact-ref80987 own source archive hashes to `2378186fadf955853d5ffe557998552f425fbac69b829cf5d23385c1df2d78bd`. All225 source entries and the exact source path set match accepted manifest `b2315fea5e93c1d3b0e28c88a33f6b1e78c942cc524b2e18f90023cca05bbb5a`. Full own tools/math-tck bytes and the tools/run-math-tck.main.kts entrypoint match this archive. Existing hashes verify106 compiled primary sources,53 prepared generated sources,57 upstream locked TEST/support files and13 owned tool files. No foreign numerical implementation body/source was read; TEST files were checked as metadata bytes.

The17 hosted notice files and their exact relative path set match approved manifest `74a6769a33892aa21031cf5240ea145d3395d8b4df1e962f6a964e9d390fed1c`. Seven retained POM hashes and all14 dependency lock metadata rows match. All57 upstream TEST/support inventory entries retain GPL-2.0-only/no Classpath Exception metadata. The complete normal evidence artifact contains896 files, with GPL test sources, notices and harness evidence kept outside the library JAR. Its only retained .jar/.class binary path is the own library JAR; that JAR's45 ZIP entries contain its own classes/metadata, no embedded test/harness/upstream classes. Source/license/adaptation boundaries and the original lost-coverage SKIPs remain intact. The unrelated large-only290-file boundary is not substituted for this normal artifact's actual paths.

## Selected evidence hashes

| Evidence | SHA-256 |
|---|---|
| Results JSON | `96ae5d0512b06ff43de5a5b9b918e470a4826a63ecdde3b54c4de56bfeda3189` |
| Inventory JSON | `4bc926f0b17155ed15deb250cd9eb9246d7cc47aa4310705756ad9124fe389e9` |
| Complete raw public job log | `686b52f4e4942a515c4df2b0fd6dfd78d78cb7b5245c052b4b2d26385e3d6fa8` |
| Failed test stdout | `b9d2d3519ebcf76eb761acfbcd587f6af986411351078499e911f24694183139` |
| Failed test stderr, empty | `e3b0c44298fc1c149afbf4c8996fb92427ae41e4649b934ca495991b7852b855` |
| Failed class-load log | `277f1a02b36e4822ba79797198d014522e396d3e79c50020af1eae252eee6579` |
| Failed prepared primary TEST | `afb93448ad8b9224e7b6674226ce894a8a7ad746d3c64a55917047723c7387b3` |
| Failed original upstream TEST | `1d4abb1ea98e14a3cd28defd54d16f2ee49563e3862e86cad11399f5591274aa` |

The final index follows this report and excludes itself/check output. All own download/read/hash invocations are terminal. This finite metadata audit establishes a real normal80987 failure under its original deadline and source/JAR binding. It is not official JCK, a full pass, an all-input/range/resource proof, a stopped-input diagnosis or a claim about later581 code. Parent independent review and any future numerical locator run remain separate; no runtime is queued or implicitly granted.

## Durable audit binding and documentation adoption

The accepted report above is preserved unchanged as `REPORT.md` in
`/private/tmp/krogu-80987-regular-failure-04gi2Pzp`. Its SHA-256 is
`116aa9d1bdff66cc40d568397fb3c6138e857474d1ec462358f6c88b198f0645`.
The workspace's1514-entry `evidence-index.sha256` is
`3976e73cc488a883f0fe285b43ccfd6d1c9366f9025ed7a3d286732219895311`;
all entries verified with exit0 from the correct working directory, with empty
verification stderr and no symlinks. This index binds the original API JSON,
ZIPs, unredacted raw job log, source archive, failure records and all metadata
projections/corrections. Before this documentation adoption, the frozen report
and index were copied read-only to
`/private/tmp/krogu-80987-doc-adoption-originals-3WBNj3sp` and their hashes
were rechecked. Original evidence is not rewritten by these documentation links.

The original evidence ZIP remains `evidence-artifact.zip`, SHA-256
`bfda498512cd521f8d83d3a5a82e1d957d49fd5e42ddb69953a5ac42bdaa084d`;
the producer ZIP is `producer-artifact.zip`, SHA-256
`e665103289c10c15c51aa5d49bf8a05ae961ae095424491eb97964c80c9f442c`.
Their exact API IDs/digests and source/JAR pins above remain the binding for
this failure; older1b9 and the accepted80987 large pass remain separate evidence.

## Separate initial locator launch: environmental failure

After the hosted failure, the independently reviewed local locator supervisor
was launched once under a separate parent scope. This is a different diagnostic
attempt, with the original failed1b9 JAR e7047 and marked TEST; it is not the
hosted80987 execution or an accepted numerical result on JAR48614577.
Retained evidence is under
`/private/tmp/krogu-sqrt-locator-supervisor-Nu4Chqkt` (`outer-launch.*` and
`run-one/`). The outer supervisor compiled and entered its body, then failed at
`ProcessHandle.descendants()` with
`java.lang.RuntimeException: Operation not permitted (sysctl failed)`.
Its body recorded74ms, outerExit1 and direct driverExit143. Cleanup records the
one captured direct identity as terminal. This is not a claim to have observed
every short-lived descendant or an arithmetic/timeout result from the TEST.

The inner destination `/private/tmp/krogu-sqrt-locator-GdPaXqTl/run-new` was
absent at the documentation check. No marked-test result or stopped phase was
recorded, so the launch does not locate the hosted timeout and credits no
mathematical assertion as passed. The original bounds/input/markers and frozen
preparation files remain unchanged; a future run is a separate parent decision.

| Locator evidence | SHA-256 |
|---|---|
| Reviewed outer supervisor | `d9870f8c1270eeaf2be06d4f8a2676a1bfad7f30caba41091f604c96824f2691` |
| Frozen inner driver | `5163dbc5f927528637fb218ded5bc8f64d467f92742121ab15f85f5f5819687c` |
| Original twelve-file preparation manifest | `6db3952e4774e58f1222af2cbc03ee641004e8fd2ac5b5c5d94682ab0d7e01d4` |
| `run-one/outcome.tsv` | `af8902c147a9e8a02e154e583163fb7a60fdecda70281bc6287a6c42a20409a7` |
| `run-one/processes.tsv` | `fe57d806dd8b75dc7b0db3d3bd5a6c8df7227e2635f27beec1a3af9d28e6b5d4` |
| `outer-launch.stderr.log` | `d5ada8708a7b0c15fc568315509c177fa5ac6d29842a3985cb4fa154f4cf5580` |
| `outer-launch.owner.tsv` | `295208bc6c201473fbca20fd176bf8e8a4372f4bdf38a6b818785e0a362ac380` |
| `outer-launch.exit.txt` | `4355a46b19d348dc2f57c046f8ef63d4538ebb936000f3c9ee954a27460dd865` |

This adoption changed only this report and three evidence pointers. It ran no
script, compiler, numerical helper/probe/test/build, process scan, workflow or
Git/tracker action. Historical failures, assertions and licensing boundaries
remain intact; official JCK and broader compatibility/resource gates remain open.
