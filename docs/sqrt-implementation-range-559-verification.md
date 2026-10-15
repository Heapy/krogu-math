# Focused sqrt implementation-range policy: actual verification

The separately granted focused campaign completed **2 PASS / 0 FAIL / 139 SKIP**:
the full original host SquareRootTests and the port's retained assertions passed.
The port's single omitted10/precision1073741825/UP call remains an explicit
adaptation-companion SKIP. Its numerical outcome was not tested or credited as
a pass. This verifies the [narrow adapter policy](sqrt-implementation-range-559-policy.md)
on one bound local artifact; it does not repair the original high-precision path.

## Actual scope and source transformation

Root scope2358 records harness session72069 terminal exit0. The complete ledger
is `results/1792097036011/results.json` beneath
`/private/tmp/krogu-sqrt-range-policy-BQZ3iKLT/root/build/math-tck`.
It contains141 distinct semantic records and the exact118 public baseline/port
IDs derived from59 original plans. Only two public invocations were attempted.
The139 SKIPs retain116 unselected public slots,16 upstream-disabled methods,
two commented calls, four excluded private-support records and the one new
SquareRootTests range companion. Other prepared adaptation metadata is retained
in inventory; unselected files and their numerical assertions were not executed.

| Phase | Compiled source | Compile | Execution |
|---|---|---|---|
| Baseline | Full original upstream TEST1d4abb1e | exit0/no timeout,364ms | PASS, exit0/no timeout,68ms |
| Port | Generated TESTeca38c8f | exit0/no timeout,422ms | Retained assertions PASS, exit0/no timeout,135ms |

Baseline source is byte-identical to the original upstream file, including
main's `scaleOverflowTest` call and its complete method body. Compared with the
previous imports-only port TESTafb93448, the actual generated source changes
exactly one line:

```diff
-        failures += scaleOverflowTest();
+        // KROGU_REFERENCE_ONLY_SQRT_IMPLEMENTATION_RANGE: original scaleOverflowTest call omitted.
```

The method body, other calls/assertions, order, inputs and notices remain.
Independent metadata text reversal recovers the imports-only TEST byte-for-byte.
Both actual compiled-source hashes agree with the inventory and raw compile
commands. The source-preparation UNCOMPILED/UNRUN labels in the earlier policy
describe that historical milestone; this report records the later actual run.

The single companion is
`port/BigDecimal/SquareRootTests/run-1/adaptation-1`, scope
`adaptation-companion`, status SKIP. Its full reason/lostCoverage text identifies
the omitted numerical input10, precision1073741825, UP and expected
ArithmeticException assertion, states that the port call was not executed,
and preserves the original unmarked FAILs/local marked timeout as failures.
The primary PASS was independently decided by the unchanged runner; the
companion was not substituted for a failed process or missing completion.

## Raw completion, binding and regime

Each primary stdout has exactly one expected HARNESS_COMPLETE marker. All18
referenced compile/test/caller-audit/class-load/runtime raw paths exist. The
port prints the four own core classes from the exact selected JAR, and its
class-load log independently contains those four origins. All three retained
caller audits completed with exit0/no timeout, own math members and no host math
or integration-bridge members. Port coreLoadingVerified=true, baselineStatus=PASS;
both caught-OOM-incomplete flags are false. Test/compile stderr and the parent
harness/checker stderr are empty. No new javap or numerical operation was run
by the independent evidence audit.
The separate runtime `-XshowSettings:properties -version` probe exited0/no
timeout in37ms; its2161-byte stderr contains retained property/version metadata,
not a TEST failure, and is included among the raw evidence.

The original TEST bounds remain512MiB/120s; compiler/audit JVMs retain256MiB.
Commands retain assertions, seed559 and -XX:-OmitStackTraceInFastThrow. Selection
was exactly SquareRootTests with standard/serialization profiles. Runtime was
BellSoft25.0.4+9-LTS on Mac OS X/aarch64, Kotlin2.4.21 and Gradle9.8.1. The
jdk-25.0.4.1-ga TEST/runtime revision difference remains recorded.

Recorded free-memory samples are observations, not a large-profile gate or
capacity guarantee; freshMemoryGateRequired=false for these standard runs.
The durations above are invocation records after a numerical call was omitted,
not a speedup, benchmark, improved algorithm or uncontended-host result.

## Actual metadata-check milestone

Before the harness campaign, root reports the one fresh-cache metadata-check
session45090 terminal exit0 under the granted256MiB script setting. The retained
`check.stdout.log` shows the exact original/generated fingerprints, the one
companion, rejection of missing, adjacent-duplicate, separated-duplicate and
already-present replacement anchors, then metadata-only PASS. Its stderr is
empty. The adjacent overlap case exercises the corrected first/last-index
uniqueness guard. The checker executes no public TEST or numerical helper;
its PASS is transformation/guard evidence, separate from the two later TEST
invocations. Parent launch settings are parent accounting, not reconstructed
from that stdout alone.

The five frozen preparation files remain unchanged: adapter5fe38c0d,
manifest04bae68a, math-tck documentation59692e0e, policy72ed8ec7 and checker90553613.
The existing runnered32beb0, entrypoint8383d062 and source-lock03493e7b are unchanged.

## Artifact, sources and licensing

The exact selected own JAR is
`/private/tmp/krogu-581-v2-validation-spIgpouC/root/build/libs/krogu-math-jvm-0.1.0-SNAPSHOT.jar`,
SHA-256 `487cca542c87cd4e03e7f5eee78b0a33cd4ea57748d40d7d9df2ace8de7c616e`.
This is the accepted581V2 artifact identified by its separate original build
evidence, not the earlier579 e7047 or580/80987 artifact48614577. Its recorded
229-source manifest and exact source path set were independently rechecked;
all229 entries pass. This audit did not rebuild or reassign an older run to
that JAR.

Metadata hash checks also pass for57 locked upstream TEST/support sources,
53 prepared generated files,14 dependency JAR/POM files and13 owned tool files.
All17 notice files and their exact path set match the approved relative-path
manifest74a6769a. The GPL-2.0-only upstream notices/source lock remain intact;
the source-specific adapter remains GPL-2.0-only and derivatives stay inside
the private harness boundary. The inspected own library ZIP contains no
public TEST/harness/framework namespaces. No foreign numerical implementation
body/source was read or copied and no library production source was changed.

## Exact evidence and limits

The private independent audit is
`/private/tmp/krogu-sqrt-range-focused-audit-qgRdcQwr`. Its514-entry external
evidence index verified with exit0 and empty stderr. It binds the prepared
harness tree/raws, five frozen files/check logs, source229 paths, selected
historical failure records and audit projections. It excludes itself/check
output and this later publication document to avoid circular hashes.

| Evidence | SHA-256 |
|---|---|
| Actual results JSON | `a7f4d9acfed2b2b8dfb4fd78c7e58e073991050cbb11e472af459ff16e974d58` |
| Inventory JSON | `66bdae020065b381bf2e53fe93b986cbfbb77dabf01e37f997df25043bbc1ce0` |
| Independent AUDIT.md | `f12855196eae49c48d768217536e3ef855b4e202c6ef01c581a99fe488ce67fd` |
| 514-entry evidence-index.sha256 | `df19b5cf602d66a3e930d71311e2d1b6323c5de2f86577700037c68395926de0` |
| Own229-source manifest | `e7cfc1fd04d891d02d92ee567215a5167e34574b82f72eab49b155289ec55dfa` |
| Original/full baseline TEST | `1d4abb1ea98e14a3cd28defd54d16f2ee49563e3862e86cad11399f5591274aa` |
| Generated port TEST | `eca38c8fbcb3fae6f82a1e98ff3cf63ad2e58784be034b2c76a6f1a5867704d5` |
| Corrected adapter | `5fe38c0df1b650041a36c1afd07257c6befb143158b7f45fe9529e3e61ea303e` |
| Manifest | `04bae68ac18f5f1ad8bbe30f728312dbd73502ed530938be91e48cfd05a6b07c` |
| Frozen math-tck documentation | `59692e0eb418330f0797bf0cc9dfd2a356d7bca4f59d2cdd2d008150f27790d0` |
| Policy preparation | `72ed8ec79e8e6123c79e21310ea2f6c1de0a83efc1246614ebd5175ab1d4b0f7` |
| Metadata checker | `905536131b2c379aef04f09ceb01a082a7ba7e8b2893fe52a518f388f0198883` |
| Checker stdout | `060faec9777bf5835cfc54675dca806e5e8027a3322f7616a4452eb10bbb58ee` |
| Harness stdout | `ea2a4f4213199e6491324f34aecbd332c8af38fdc89f51833f2d303c1863bef9` |
| Baseline TEST stdout | `8e9a45d1c054e853ec2d09709fe20b9dca787f8d8483d914b6b06272378f1bdb` |
| Port TEST stdout | `d71950b34cfbd55dfaf6401608457e7bde198bf24947e5517621d2cf7494d802` |
| Approved17-notice manifest | `74a6769a33892aa21031cf5240ea145d3395d8b4df1e962f6a964e9d390fed1c` |

The [unmarked1b9](public-math-1b9-regular-failure-559.md) and
[unmarked80987](public-math-80987-regular-failure-559.md) original ledgers still
record their120113ms/120066ms SquareRootTests FAILs. The
[local marked locator](sqrt-public-locator-579-result.md) still records its
120134ms timeout and51-marker prefix. None is relabelled by this focused
adapted result, and the different source/JAR/host scopes remain separate.

The omitted high-precision port input remains unexecuted here. Numerical range,
inexact-cohort and resource obligations, including any later independently
established mandatory own result/error, are not waived by this reference-only
classification. This is no all118 pass, broad public campaign, official JCK,
maximum-domain/performance acceptance or whole559/579/548/526 closure.

Root reports explicit all-terminal release with empty mutex revision802.
This independent audit/doc preparation ran metadata reads/hash checks only;
all own calls are terminal. Adoption and any further runtime remain separate
parent decisions, with no implicit worker numerical grant or queued retry.
