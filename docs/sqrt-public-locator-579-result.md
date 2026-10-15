# Public sqrt locator: actual final-phase timeout (579)

A separately authorized local marked-TEST run reached `scaleOverflowTest` and
did not return before the unchanged120-second deadline. This locates the
unreturned method in that local execution. It does not establish an internal
arithmetic cause or identify the stopped input in the unmarked hosted runs.

## Actual local observation

Parent scope2349 launched the frozen outer supervisor once outside the sandbox
after the earlier environmental failure. Attached session74361 terminated with
exit1. The same supervisor d9870 and inner driver5163 were used, with fresh
cache/output and the original marked public TEST. No driver retry, input change,
precision reduction, timeout/heap increase or assertion removal occurred.

| Observation | Retained actual result |
|---|---|
| Marked TEST compile | exit0, no timeout,379ms;256MiB/60s bound |
| TEST execution | TIMEOUT, timedOut=true, exit143,120134ms;512MiB/120s bound |
| Marker prefix | Exactly51 records, ordinals0–50, matching the expected52-record sequence |
| Last marker | `LOCATOR 50 BEFORE scaleOverflowTest` |
| Current unreturned phase | `scaleOverflowTest` |
| Prior phases | All earlier main phases have AFTER markers, including halfWay and exactResultTests |
| Final completion | No `AFTER scaleOverflowTest`; no HARNESS_COMPLETE marker |
| Binding | Four own core origins and caller audit verified |
| Outer result | driverExit1, outerExit1, firstFailure `Driver nonzero exit 1`,123848ms |

Returning phases are not independently declared numerical passes: the original
TEST accumulates failures and checks the total at the end, which was not reached.
The inspected compile stdout/stderr, TEST stderr and outer/inner stderr are empty.
The full original stdout and commands are retained without filtering.

The unchanged final method contains one numerical invocation:

```java
BigDecimal.valueOf(1, -1).sqrt(
    new MathContext((1 << 30) + 1, RoundingMode.UP));
```

This is input10, precision1073741825, UP. The public TEST catches
ArithmeticException as its expected outcome. No return or exception was recorded
before termination. The phase marker does not sample the internal stack or
prove a specific instruction/algorithmic cause. It also does not prove that
ArithmeticException is required by the full public API contract, that the final
required representation is unsupported, or that a precision cap/test adaptation
would be valid. Those mathematical/range/resource questions remain separate.

## Source, host and diagnostic limits

The local runtime was BellSoft25.0.4+9-LTS on **Mac OS X/aarch64**, Kotlin2.4.21,
with assertions, seed559 and the original512MiB/120s TEST regime. The exact input
JAR is the accepted579/1b9 artifact:

`e7047f824deb0e4f9170ad223037a0d42f49cdfa3025eed16b776ece9be9c5ea`.

All223 own source entries match manifest
`25bc9cce8426a00861c480810ea99aad47c5ac504d91c98582fdf8e4294ccfb4`.
This is not the later80987/580 JAR48614577, later581 source, or the hosted
Linux/x86_64 environment. The [normal80987 hosted failure](public-math-80987-regular-failure-559.md)
and [earlier1b9 failure](public-math-1b9-regular-failure-559.md) retain their own
unmarked logs and120-second timeouts; their precise stopped methods/inputs are
not retrospectively proved by this marked local run. Marker printing and the
different host also prevent a performance-equivalence claim.

All twelve frozen preparation hashes still pass. Removing only the marker
helper/calls recovers the original port TEST byte-for-byte; reversing only its
import rewrite recovers the upstream TEST byte-for-byte. Original calls,
assertions, ordering, precision, inputs and GPL notices are unchanged. The
five compiled class hashes match the existing caller audit; the three TEST
classes have own math references and the frozen checker rejects host math/bridge
members. No new javap/bytecode or numerical execution was made by this audit.

## Terminal and initial-failure evidence

The inner ledger records the compile and TEST identities terminal. The outer
registry captured five identities:32359,32360,32370,32407,32408, with their start
instants; all five historical TERMINAL_CHECK records sayfalse. The TEST received
the owned cleanup request after its deadline, and its actual exit143 is retained.
These are captured-identity observations, not a current process scan or a claim
to have observed every hypothetical uncaptured process. Parent reports attached
session74361 terminal and explicit window release with empty mutex revision796.

The first sandboxed attempt remains a separate environmental failure: its outer
supervisor compiled but ProcessHandle.descendants failed with the sysctl
permission error; direct child exit143 and absent inner output supplied no
marked-TEST outcome. The later run does not rewrite that evidence or retrospectively
improve its incomplete descendant capture. Its read-only capsule is
`/private/tmp/krogu-sqrt-locator-initial-failure-2347.tar`, SHA-256
`09c9bb2e321cd9c36204511f78e45fb6902c9fcb542bf23e3d32787fef784de2`.
The separate initial-failure review is retained at
`/private/tmp/krogu-sqrt-locator-supervisor-Nu4Chqkt/initial-failure-review.md`,
SHA-256 `8af5d122460f41878501c3456e75031a9b8885ab176fe484e45c4b0874055e70`.

## Retained evidence and exact pins

The raw inner directory is `/private/tmp/krogu-sqrt-locator-GdPaXqTl/run-new`.
Outer evidence is
`/private/tmp/krogu-sqrt-locator-supervisor-Nu4Chqkt/run-escalated-one` plus
`escalated-launch.stdout.log`/`escalated-launch.stderr.log` in its parent directory.
An independent metadata-only audit is retained at
`/private/tmp/krogu-sqrt-locator-actual-audit-sxWcjP3n`.
Its288-entry evidence index verified with exit0 and empty stderr. It binds raw
results/classes/commands, all frozen preparation files, original223 source paths,
the initial capsule/review and the audit's marker/hash/text-recovery checks.
The index excludes itself/check output and later publication documents/diffs.

| Evidence | SHA-256 |
|---|---|
| Independent AUDIT.md | `e12c20308a6dc422d3e3896e32034cd0e08eafeb5353b4e38d389f57152f5b09` |
| 288-entry evidence-index.sha256 | `60a068bbf2cdac10c8c6f851e56516a961c5ee2ca519042ead658d2e0eb0a5ce` |
| Outer supervisor | `d9870f8c1270eeaf2be06d4f8a2676a1bfad7f30caba41091f604c96824f2691` |
| Inner driver | `5163dbc5f927528637fb218ded5bc8f64d467f92742121ab15f85f5f5819687c` |
| Twelve-file preparation manifest | `6db3952e4774e58f1222af2cbc03ee641004e8fd2ac5b5c5d94682ab0d7e01d4` |
| Marked TEST | `429d301ffdd4e86780ae2095ff658e9ddb0abb8a1bfb53197a9a6c56e8f11866` |
| Recovered original port TEST | `afb93448ad8b9224e7b6674226ce894a8a7ad746d3c64a55917047723c7387b3` |
| Recovered original upstream TEST | `1d4abb1ea98e14a3cd28defd54d16f2ee49563e3862e86cad11399f5591274aa` |
| Inner RESULT.tsv | `0d8fa1cf8e20355082224bb79559c7d880778e14828f12a2c27ef3ac14d84908` |
| TEST stdout | `320b4433666049a17d1a5b497f2e16a6fc924b68b06ffa2c69c7d34592d82d4e` |
| TEST outcome | `015e93533b798d24f620ebb8757f42dc589407867958ee3a4cb3f841c1ba226d` |
| Caller audit | `0d7625b0fa397240415e6d447ef1cf71751d3d412eb91676bd78efe3a45a051d` |
| Outer outcome | `97e74de45bb6aa75adf708e62fc7536180f74aeb6d00a8d01aa29c11a5b06764` |
| Outer process ledger | `90eaf7fc54d25e1afdf3aaa269626b91a05e647cd08030817f6b95b0ffc91d47` |
| Identical outer frozen before/after | `1288d7ec11cc41c859a43782ba208c83604b4cc335c4f5409993b3de4cbbc6bb` |

This finite audit/doc update ran no compiler, script, numerical helper/probe/test,
process scan, benchmark, build, Git or tracker operation. The observed locator
timeout is diagnostic evidence, not numerical success, a root-cause proof,
official JCK/full-gate acceptance or an all-input resource guarantee. Further
proof/fix/runtime scope remains a parent decision; no retry is queued here.
