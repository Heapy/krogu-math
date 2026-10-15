# e36 six supported/oracle jobs: existing-run metadata audit

Qualified ACCEPT of the six completed non-public jobs of [Compatibility run38810176702, attempt1](https://github.com/Heapy/krogu-math/actions/runs/38810176702), exact commit `e36c1ff93c5a7809f43565960c37bd2221ebc938`. This audit read existing API metadata, original job logs, archives, source declarations and XML; it ran no compiler, mathematical model, production helper, test, binary, oracle generator or workflow.

The preserved API snapshot has the overall run `in_progress` and public job116696602827 `in_progress`, with null conclusion. That job was not polled or audited here. Six supported/oracle successes do not establish the public-job outcome, official JCK status, all-input correctness or resource feasibility.

## Actual completed jobs and executions

| Job | Actual retained evidence | Suites / methods |
|---|---|---|
|116681133581 oracle |25 original kotlinr commands, all steps success; raw verification outputs retained | oracle/metadata checks, not platform XML |
|116681133783 Windows2025 |jvmTest; mingwX64Test |158/532;75/345 |
|116681133798 macOS15 Intel |jvmTest; macosX64Test; iosX64Test |158/532;75/345;75/345 |
|116681133881 macOS15 ARM |jvmTest; macosArm64Test; iosSimulatorArm64Test |158/532;75/345;75/345 |
|116681133902 Ubuntu24.04 x64 |jvmTest; jsNodeTest; wasmJsNodeTest; linuxX64Test |158/532;75/345;75/345;75/345 |
|116696602794 actual Ubuntu24.04 ARM |downloaded same-run native executable, then original `./test.kexe` |75/345 |

All six API jobs are `completed/success`; every recorded step is success or an explicitly skipped conditional step. No failed/cancelled/incomplete step was found. `six-job-steps.json` retains all step statuses.

The four artifact trees contain exactly1,232 XML files across12 executed target instances: four JVM sets and eight common-only sets. Declared tests, actual direct testcase counts and failure/error/skipped child counts agree in every suite. Totals are4,888 XML method executions, zero failures/errors/skips. Every normalized method multiset equals the exact source inventory; there are zero duplicate, missing or extra methods. Suite inventories also match testcase classes (JVM display names use the original simple-name `[jvm]` convention). All XML timestamps fall inside their corresponding fresh job intervals; original target tasks executed rather than reporting cache/up-to-date results. Only later setup-gradle cleanup `noop` tasks report up-to-date.

Linux ARM64 adds345 executions: exactly345 unique `[ RUN ]` names and345 matching `[ OK ]` names. Both sorted lists equal the complete345-method common source inventory and the same-source Wasm XML list. Its raw footer reports345 tests from75 cases, all passed. There is no ARM XML artifact; this gate is established by the actual native log and exact per-method inventory, not by cross-compilation alone. The aggregate is5,233 method executions across13 target instances, with no observed nonpass outcome.

`compileKotlinIosArm64` completed successfully on macOS ARM. This is **iOS device compilation only**; no device-runtime execution is claimed. Ubuntu `linkDebugTestLinuxArm64` likewise only produced the binary; job116696602794 supplies its separate actual ARM execution.

## Exact source, artifact and toolchain binding

* Source manifest234 entries: `source234.sha256`, SHA256 `37784f5a24d8e959795b9c708897f5486b13159c11b6817be85986910f9ff6f2`. All234 checks pass against the exact e36 archive; source pathsets compare identically. `source234-check.txt`, `actual-source-paths.txt`, `expected-source-paths.txt` and `exact-source-paths-final.txt` preserve this accounting.
* Exact source archive `source.tar.gz`, SHA256 `c0fd6d1f606b8d10142fc82aa18d2c8d7c18ef0d788de4eeb59e5aad29772f4a`, was copied byte-for-byte from the independently accepted same-e36 large-run evidence at `/private/tmp/krogu-e36-large-evidence-ntxvlcYo/source.tar.gz`. It was not reconstructed from the later checkout. The five checkout jobs' original logs identify e36; ARM consumes the same-run artifact.
* Same-run Ubuntu producer artifact12009505546 contains `SOURCE_COMMIT=e36c1ff93c5a7809f43565960c37bd2221ebc938`; its original SHA256SUMS both pass. Own JAR SHA256 is `df3714cff6507cf2eef102bade9aecf2319384873d16db1b1259b0e74fb030a0`, identical to the accepted e36 large-profile and local584 artifact. It is not the later source236/583 JAR.
* Native executable SHA256 `346695c22917d494f20fc84ea41c056a1b4a55e24d88d54665d4855f2eabaf19`. ARM's original log verifies the downloaded artifact ZIP digest `92bc4f91ef4d0b7294ddedabfbf1e3dd1cf250bfff65d8ab2b463d4be0a4f513` before executing that archive's sole `test.kexe`.
* All six downloaded artifact ZIP digests match GitHub API values, including the four test-result archives, native binary archive and JVM producer archive. Exact IDs, names, lengths and digests are in `artifacts.json`; `artifact-api.sha256` and `artifact-api-check.txt` retain the verification.
* Original source workflow pins Liberica25.0.4+9 and Kotlin2.4.21; setup logs confirm those configured toolchains. The exact source build uses Kotlin multiplatform2.4.21, JVM toolchain25 and Gradle wrapper9.8.1. Matrix commands preserve `--max-workers=1`, `-Xmx4g`, UTF-8 and `--stacktrace`; JS Mocha remains120s, JVM tests retain `-XX:-OmitStackTraceInFastThrow`. Original commands/configuration are retained in source and logs, with excerpts in `actual-platform-task-lines.txt`, `platform-toolchain-lines.txt` and `oracle-commands.txt`.

The source-derived common inventory contains345 annotated methods. JVM adds126 Kotlin and61 Java methods, giving532. `source-*-annotated-methods.txt`, `source-common-final.methods`, `source-jvm-final.methods`, individual `xml-audit/*.methods`, `method-set-checks.tsv`, `xml-summary-final.tsv` and `xml-freshness-counter-checks.tsv` preserve exact keys and counts. XML parsing used `xmllint --nonet`; source extraction inspected only declarations and annotations, not execution of tests or numerical bodies.

For comparison, the later publication checkout has236 source paths: three changed production files (`BigDecimal.kt`, `BigInteger.kt`, `DecimalDivision581.kt`) and two added583 test files relative to this234-source manifest. The retained `current-publication-source-comparison.*` and path inventory show exactly those differences. These are expected historical-binding differences, not a checksum PASS for the current checkout. This report does not promote historical e36 evidence to later583/585 sources.

## Oracle and audit limits

The oracle job's25 original commands include the retained JDK generators/checks, context-field classification, returned-representation extraction and the static exact-root inventory. Its API steps and original output certify those executed checks only. No new generator was run during this audit. In particular, the context mapper itself states that its metadata stage evaluates no production/JDK arithmetic; the static exact-root source inventory is not a new numerical campaign.

This is finite integration evidence for the recorded methods and toolchains. It neither proves every supported mathematical/range value nor removes current resource548, subtype541/526, source-confirmed585 or other backlog obligations. No test assertion, input, deadline, memory profile, adapter or source was changed. No foreign numerical implementation source or bytecode was read. Public-test artifacts, public class-origin/caller guards and GPL source boundaries belong to the separately audited public campaigns, not these platform XML jobs. These jobs do not publish equivalent per-runtime class-origin captures; their evidence is the exact checkout/build commands, fresh XML inventories and same-run binary/JAR linkage described above.

## Retained metadata corrections

The initial six `gh api .../logs` downloads were blocked by gh's terminal-escape protection with: `the response contains terminal escape sequences; pass --allow-escape-sequences to output it anyway`. The resulting empty files remain as `job-*.initial-empty.log`; a subsequent metadata-only download with `--allow-escape-sequences` saved each full original log unchanged. No job was retried.

Initial XML summary counted blank separators as suites, producing316/150 instead of158/75; that file remains `xml-summary.tsv`, while `xml-summary-final.tsv` correctly requires a populated XML row. Initial declaration extraction retained three multiline Kotlin `fun` prefixes; the original inventories remain, and the final normalized inventories explicitly remove those prefixes before exact comparison. JVM suite display names were initially compared to fully qualified testcase names; separate final display-name normalization resolves that format difference. An initial jq expression accidentally scoped artifact `.id` to the digest string and failed; the corrected original-object expression produced `artifact-api.sha256`. An exploratory read of nonexistent `source/gradle/libs.versions.toml` failed; the actual pinned Kotlin version is in `source/build.gradle.kts`. These are metadata-reading corrections only, with no test/build/oracle rerun or altered original evidence.

The evidence root is `/private/tmp/krogu-e36-supported-ci-c5gYBTDE`. API snapshots, six raw logs, six original ZIPs, extracted artifacts, exact source archive and all derived metadata are indexed by a final SHA256 inventory. All owned downloads and metadata calls completed before the index was frozen. Public-job outcome remains outside this finite report.

The frozen index contains1,966 entries; all verify, and its SHA-256 is
`6ca26e842f4f93cafa766a91a67ab383c295f5161bc18755d8d9551b0ca4335e`.
The later overall workflow success does not change the historical in-progress
snapshot above. Its public ledger is a separate audit, rather than part of these
platform XML/native results.
