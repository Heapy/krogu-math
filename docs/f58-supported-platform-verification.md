# f58 supported/oracle jobs: completed actual evidence

Qualified ACCEPT of the six supported/oracle jobs of [Compatibility run38813463685, attempt1](https://github.com/Heapy/krogu-math/actions/runs/38813463685), exact source `f58d8651b44e45e190e96e10f9ff587b52720f56`. All six are terminal SUCCESS in the preserved new API snapshot. This audit establishes their actual retained finite test inventories, not merely their API conclusions.

The overall run and public job116711014364 are still `in_progress` with null conclusions in this snapshot. Public evidence is excluded: no public outcome, combined normal/large public result or official JCK claim is made here. No later polling was performed.

## Actual gates and complete inventories

|Terminal job|Executed targets/evidence|Actual suites / methods|
|---|---|---|
|116699851699 oracle|25 original kotlinr generator/check/classification commands|original logs/step outcomes; not platform XML|
|116699852266 Windows2025|jvmTest;mingwX64Test|160/537;76/349|
|116699852150 macOS15 Intel|jvmTest;macosX64Test;iosX64Test|160/537;76/349;76/349|
|116699852187 macOS15 ARM|jvmTest;macosArm64Test;iosSimulatorArm64Test|160/537;76/349;76/349|
|116699852072 Ubuntu24.04 x64|jvmTest;jsNodeTest;wasmJsNodeTest;linuxX64Test|160/537;76/349;76/349;76/349|
|116711014458 Ubuntu24.04 actual ARM|same-run native executable `./test.kexe`|76/349, complete console inventory|

Exactly1,248 fresh XML files cover12 target instances: four JVM sets plus eight common-only sets. They contain4,940 actual method executions. Declared suite counts equal actual testcase/failure/error/skipped child counts in every file. Failures, errors and skips are all zero. Every normalized method multiset equals the independently extracted exact source inventory, with zero missing, extra or duplicate methods. Suite ownership/multiplicity also matches the testcase class sets; original JVM simple-name `[jvm]` suite labels are normalized explicitly. XML timestamps fall inside their corresponding job intervals, and raw target task lines show actual execution rather than cache/up-to-date test results. Only setup-gradle post-action `noop` cleanup tasks report up-to-date.

Linux ARM64 separately records349 unique RUN names and349 matching OK names. Their full sorted lists equal both the349-method source inventory and same-source Wasm XML. Its raw start/footer report349 tests from76 cases, all passed,334,371ms recorded total. There is no ARM XML artifact; this is actual native console execution evidence, not a cross-linking inference. Aggregate: **5,289 method executions across13 target instances**, with no observed nonpass, omission, extra or duplicate.

The source-derived common inventory has76 classes/349 methods; JVM adds84 classes/188 methods (127 Kotlin +61 Java), producing160/537. Exact method keys, original annotation matches, every suite/counter/timestamp and per-target comparison are retained in `source-*.methods`, `source-*-annotated-methods.txt`, `xml-audit/`, `xml-summary.tsv`, `xml-freshness-counter-checks.tsv` and `method-set-checks.tsv`. Source declaration extraction is metadata accounting, not execution of a numerical oracle.

`compileKotlinIosArm64` completed on macOS ARM. This is **device compilation only**; no iOS device runtime is claimed. Ubuntu's `linkDebugTestLinuxArm64` likewise only produced the executable; the independent ARM job supplies its actual runtime gate. Its job interval is2026-10-15T22:44:10Z–22:49:51Z.

All six job/step outcomes remain in `jobs.json` and `six-job-steps.json`: no failed, cancelled or incomplete step exists among these six; conditional skipped steps are retained. Oracle output documents its original25 commands, JDK vector checks and metadata/static classifications. The latter are not represented as new numerical tests. No existing expected assertion, fixture or diagnostic was changed in this audit.

## Exact source and producer provenance

Source236 manifest SHA256 is `10bff57aa852664b35a73c9e8e7d35d0f4be870181008c7dd57bd5c82c504551`. All236 checks pass against the exact f58 archive and its full source pathset compares identically. The publication src tree at audit time also matches those236 hashes and paths; that observation is a snapshot, not a claim about later integration. The separately running private585/586 local validation and its240-source composition are not part of this hosted run.

Exact source archive `source.tar.gz`, SHA256 `9bf0eb55592e0b0b0dbacfa528073d099675c596da44d8599739077b641b1fd9`, and the236 manifest were copied byte-for-byte from the accepted same-f58 large evidence `/private/tmp/krogu-f58-large-evidence-HUDb7OMN`. They were not reconstructed from a later checkout. Five original checkout-job logs identify the exact f58 SHA. The ARM job consumes the same-run producer binary through its verified artifact download.

Same-run Ubuntu JVM artifact12012566053 contains SOURCE_COMMIT exactly f58 and both original SHA256SUMS entries pass. Its own JAR is `391b73ee1923851773cd8e65bb8ebad4674b47a2e98c5eb49feeefdb45566420`, identical to the accepted local583 and f58 large-run JARs. The JAR has51 archive-directory entries confined to META-INF/io paths. This is metadata/hash verification, not decompilation. Other OS JVM jobs do not upload separate own JARs or runtime class-origin dumps: their source/build binding rests on the exact checkout, pinned commands and fresh complete XML; no unavailable per-OS JAR hash/origin evidence is invented.

Native artifact12012665504 contains only `test.kexe`, SHA256 `d6e02fa91acbb1c257bd4ad88d58d5bac2da16dfc36c5034ab2dc0e690eff76e`. The actual ARM log records that artifact ID, independently verifies ZIP digest `2f6b5952b4f821f5a57550277c0c0061e00ff0ca002e0582fc3fd761095404ed`, then executes the downloaded file. The independently downloaded identical ZIP and its extracted binary are retained here.

## Artifact digests and unchanged commands/toolchain

All six original ZIPs match GitHub API digests (`artifact-api.sha256` and final `artifact-api-check.txt`):

|Artifact|ID|ZIP SHA256|
|---|---|---|
|linux-arm64-tests|12012665504|`2f6b5952b4f821f5a57550277c0c0061e00ff0ca002e0582fc3fd761095404ed`|
|public-math-jvm-f58…|12012566053|`122afd918266c52ab7e05d509066dda42b0da5a9bcace8b8800b4369adbc0f07`|
|test-results-macos-15|12012230132|`cb594a69be38739b2f8627c492866f252aa7f5017dae6c99af0356290e4acd86`|
|test-results-ubuntu-24.04|12011902243|`b28efd716d0d2b46179f2ab25a4eb383bffb7b5d864d22cb126ae90eb802c2f1`|
|test-results-macos-15-intel|12011743015|`4879a434daeac29156c16d9dfea6dbe0f3b64917ec05fece8d7371fab3039d19`|
|test-results-windows-2025|12010639593|`5a663e4ee7a77d7bccc1a938f89822af6c040161be23bf41c9d895dd1bfac0e3`|

API metadata preserves names, sizes, job identities and timestamps. Six raw job logs, six original ZIPs and their extracted files remain unredacted.

Exact workflow SHA256 is `af3813bbf060def341086f4dc4edcacc0c5f9059d39c40462c3888110af5be9b`, byte-identical to the previously reviewed e36 workflow. Build SHA256 is `b883bf096dd0c80f1dfec459af4a439c26232c92f0cdff6687dca23cd631efe6`; wrapper properties SHA256 `db7a6b948fb1e4fd362a4bd6ee3ef9cdb90967be4c403e45d75891f052d00965` selects Gradle9.8.1. Exact source/build selects Kotlin multiplatform2.4.21, JVM toolchain25 and Liberica25.0.4+9. Setup and Native bundle logs confirm those recorded versions. Oracle setup verifies Kotlin compiler2.4.21; matrix Native paths identify2.4.21 bundles.

Original matrix commands preserve `--max-workers=1`, `-Dorg.gradle.jvmargs=-Xmx4g -Dfile.encoding=UTF-8`, `--stacktrace` and their exact task lists. Source JS Mocha configuration remains120s; JVM tests retain `-XX:-OmitStackTraceInFastThrow`. No timeout, precision, resource or input setting was modified here. No separate explicit test-Node executable version is claimed from action-runtime deprecation warnings. Existing Node action warnings, Native bundle-overwrite notices and Kotlin override-parameter-name warnings remain in original logs; they are not test failures or newly waived assertions.

## Scope, historical prep and limitations

The earlier finite prep remains immutable at `/private/tmp/krogu-f58-supported-ci-AD6RcQcL`: report `98d5f532ee23cf9f67c5e9a0f13ebf26eae491ab7b8b410e1bb54684d54e2c0f`, index `d5a2782f46b0b4e81806e3733084f53165a898a62a7773689e2d2d60fbd32d83`. Its then-running ARM snapshot is not rewritten. Only its report/index are copied here for provenance. The earlier e36 supported audit supplied a checking method, not f58 execution outcomes; all counts in this report derive from actual f58 logs/XML/source inventories.

No runtime script, compiler, numerical/helper/model/certificate, test, build, probe, process inspection, javap, workflow dispatch/retry, repository edit, Git or tracker operation was performed by this audit. Direct API downloads/read/hash/archive/XML tools performed metadata checks only. No foreign numerical implementation bodies or bytecode were read. These platform jobs do not export the public harness's caller/origin/license-boundary ledger; that separate running public job is excluded rather than filled with evidence from another source/run.

This is finite platform/oracle integration evidence under the recorded configurations. It does not establish full supported-range/allocation feasibility, mathematical correctness for every input, actual RNG independence, broad subtype541/526 behavior, source-confirmed585 repair, official JCK or complete compatibility. Private585/586 local stages and their later independent review remain separate.

## Key evidence pins and metadata correction

|Evidence|SHA256|
|---|---|
|run.json|`be69f2ebf1c715a94db87fae1784c9298463f7b2f818aec1507ee9989a869d51`|
|jobs.json|`e52f305a9b5fc508fd5c87acd149209477922ac243b2ab4b30c2345c0aae8fee`|
|artifacts.json|`ebb5165f6a17651476b73b276123d05057386595a9dddb0debb6dd44f4430c22`|
|XML summary|`3cc8ead579d3e118352b27a576fb0323e882240412c7dbeb0395e7cdc676dc2a`|
|Method-set checks|`3cb7d66567de37cde030e18134aad5b2b76105b6f5be3592b507fc73951cc352`|
|Common source keys|`f572ce0471116f32582507c320a950c0a13647145ae98cce196a7ff8b952dcc7`|
|JVM source keys|`f1e512c9ff59313e268c421aba88ef43c550d97e18fc15964cc2e65387f10f66`|
|Actual ARM raw log|`c527ee68fd36c50e66361bd5181453f1108922c0e54d9d4d000a527c13d96202`|

An initial metadata command checked/extracted the ARM ZIP while that download still had a running tool session. Its checksum therefore failed and unzip reported an incomplete central directory; `artifact-api-check-initial-incomplete-download.txt` preserves the first checksum result (the other five ZIPs already passed). This was premature local evidence inspection, not a hosted failure, corrupt final artifact or numerical retry. After the original download reached exit0, the final full ZIP digest and extraction passed; no redownload or hosted action was retried. All initial/final records remain distinct.

Evidence root: `/private/tmp/krogu-f58-supported-actual-T42M46dj`. A final SHA256 index covers all retained originals and derived metadata, excluding itself and its check/path-list files. All owned downloads and metadata invocations are terminal before handoff. No future campaign wait remains; independent parent review/publication is separate.

## Publication binding

The frozen original report SHA256 is `b47b26d86a94c4a75d28fad3c6ddc9f3b2e01eecb90cc69d6b5243130336e027`. Evidence index `f89b5704731977f063fd701ddf9ea92fec417fc574f145d017985f6dd8c4e62c` covers1,986 retained files; both parent and independent reviewer verified every entry. Independent actual-adoption review accepted the raw inventories, artifact/source/JAR provenance, unchanged commands and stated limits. This publication adds no new runtime and does not relabel the source240585/586 run as f58 evidence.

Later root API inspection at2026-10-15T22:58Z records all seven jobs and the overall run as terminal SUCCESS, including public job116711014364. The original audit snapshot above remains unchanged; its six-job evidence does not supply the separate public ledger audit, which is being verified independently.

