# Consolidated public math campaign: task 559

Campaign `1792075080136` completed with **106 PASS / 0 FAIL / 46 SKIP**:
53 eligible public invocations passed in each of the host baseline and
controlled port phases. `complete=true`, `testsExecuted=106`, `officialJck=false`.
This finite adapted corpus does not establish official JCK certification,
original-test equivalence, the full numerical domain or all Kotlin contracts.

## Exact selection and provenance

Root task526 grant2276 authorized one fresh, unfiltered `--both` campaign.
Profiles were `standard,serialization,heavy,memory-512m`; maximum test heap512MiB,
seed559, BigIntegerTest subset0. Original test directives, operands, certainty,
assertions outside the explicit adapters and per-process deadlines were retained.
Ordinary test/compiler/caller-audit deadlines remain120 seconds; the second
BigIntegerTest directive retains400 seconds, and the runtime precheck30 seconds.
No whole-campaign deadline, retry or changed resource limit was introduced.

The independent library JAR was reused byte-for-byte from the accepted
current-production build, not rebuilt for a harness-only change:
`ded66cd6a325b73a9a393e0fd56281d7bf63d571dab4b146e8f1cb9c0dba2fd1`.
Root verified no commonMain/jvmMain/build-input changes from d499 through
`ccff47c8bb2b6fdd7545fe995b2d76b87661335b`. The clean workspace inherited no
build directory, results, generated sources, classes or compiled-script cache.
It fetched and verified57 pinned public source files and14 dependency records.

The campaign deliberately froze entrypoint
`b73026d2269abf5d4be67c04ad49717dc8d154268e19f7be94f7d639a6061f12`.
Its before/after tools manifests are identical. The later CI integration changes
the entrypoint to82b129a8; this result is **not** an execution of that later entrypoint.
The original/adapted PrimeTest source and range-policy classification remain
documented in [the targeted report](prime-test-range-policy-559.md).

The environment was macOS ARM64, BellSoft Liberica25.0.4+9-LTS and Kotlin2.4.21,
with a256MiB script JVM. Public tests remain pinned to OpenJDK jdk25u
`7b65d74bcbd7c3a0b4c1f5c5bd85e64aa2e51b4a`, tag25.0.4.1; the vendor/revision
difference from the reference runtime is disclosed, not asserted equivalent.

## Complete result accounting

Exactly118 unique public invocation IDs cover all59 planned directives in
each phase:53 PASS and6 resource SKIP per phase. No public ID is duplicated
or omitted. The remaining34 records describe separately lost or excluded coverage.

| Scope | PASS | SKIP |
|---|---:|---:|
| Host baseline public invocations |53|6|
| Controlled port public invocations |53|6|
| Port adaptation companions |0|12|
| Upstream disabled methods, both phases |0|16|
| Upstream commented calls, both phases |0|2|
| Excluded private implementation tests, both phases |0|4|
| Total |106|46|

The six resource directives per phase are four8GiB, one4GiB and one10GiB
profile. These profiles were not authorized by this invocation; their original
requirements and lost coverage remain recorded. The12 adaptation companions
are Constructor4, DoubleFloatValueTests5, ByteArrayConstructorTest1,
ModPow65537 one and PrimeTest one. Six source files are adapted; the sixth,
ModPowPowersof2, changes child orchestration without an additional companion.
The manifest and adapter retain the precise boundaries of these adaptations.

All53 port invocations have successful caller-bytecode audits with no host-math
member owners, verified exact-JAR core loading, successful compilation and
completion markers. All executed process records exit0 without timeout; the
nested JVM invocation also retains its child exit/completion/origin evidence.
No caught-OOM incomplete-assertion or framework-skip result was counted as PASS.

The current port's original BigIntegerTest directives pass in43876/43408ms;
adapted PrimeTest passes in25332ms. Both ToPlainStringTests directives pass.
These are elapsed observations, not controlled performance comparisons.
PrimeTest success still accepts normal false or ArithmeticException; the
huge-input branch and Boolean remain unobserved. Earlier failing campaigns
remain immutable evidence with their own source, artifact and adaptation scope.

## Host, termination and evidence limits

Fresh OS physical capacity was38654705664 bytes (36GiB), but the initial
free-memory sample was only115703808 bytes. Eligible512MiB profiles do not
apply the separate8/10GiB fresh-free-memory gate. This run establishes neither
large-profile eligibility nor an uncontended host or performance improvement.

Outer invocation77161 exited0. Captured parent identities49543/49553/49561
were absent at the scoped final process check; all recorded child invocations
completed. No cleanup signal, foreign process action or background campaign
was needed. Independent read-only review accepted the phase/ID/count inventory,
coverage attribution, caller/origin/completion evidence and provenance limits.

Private evidence workspace: `/private/tmp/krogu-559-consolidated-v1`.
The full invocation, raw sources/classes/logs/results, own JAR, process identities,
exact-result audit and identical tool manifests are retained in a local capsule;
the compiled-script cache is excluded. This private capsule is not a public
combined-binary distribution artifact.

```text
f23dfaed74bdcd016c90aefbd81dfc138bc1d46a820a03bd136f6df4fc4de38b  krogu-559-consolidated-v1.tar
e7bacf735adad2ebd5243b80293c7d2122b129d4d1e12ef587ca5284bede2431  results.json
0c15c67a1cb3347c92f0fe2f257be671dc24d35167a84d789e151da16eadacfa  campaign.log
a17fcf0a2f50e2d495e4f90ce263410edc183add6c62699a2facbccf60410f74  exact-audit.txt
a633ba713d787485790f9426dc8319ad17b343926a91306bb63d5bf195fccafa  tools-before.sha256 / tools-after.sha256
```

The separate [CI integration](public-math-ci-559.md) must provide its own
exact-commit hosted execution evidence. The official-JCK access barrier,
large profiles and broader compatibility tasks remain separate obligations.

The subsequent [hosted CI verification](public-math-ci-559.md#hosted-verification)
now supplies that separate82b1-entrypoint result:106 PASS /0 FAIL /46 SKIP on
published eaaea5. It does not replace this frozen-b730 campaign. Both host
baselines preserve the original numerical assertions; the documented nested-JVM
orchestration adaptation also applies to its host invocation, so “baseline”
does not mean every generated host source is byte-identical to upstream.
