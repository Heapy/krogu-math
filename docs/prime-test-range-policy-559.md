# PrimeTest range policy and targeted verification: task 559

The targeted follow-up completed with **2 PASS / 0 FAIL / 139 SKIP**: original
baseline `PrimeTest` and the adapted port invocation. This is not an original
port-test PASS, a full public-corpus pass, or a JCK result. The parent verification
section below records the exact artifact and observed limits.

**Historical source-only milestone, before execution grant2274:** this changed only the
test adapter, the `PrimeTest` manifest row and this document. It does not
change production code, a JAR, the pinned upstream test, original evidence,
deadlines, fixtures or certainty. Parent task 526 comment 2273 authorizes this
preparation following the public-API classification in task 559 comment 2229.
Execution and independent review belonged to the parent.

## Behavioral assertion and exact scope

The pinned public OpenJDK `BigInteger/PrimeTest.java` has SHA-256
`e539033a27d32ad15924a1522ab3ba4d444b2b2283ca033099c0fa60c2c9eb09`.
Its `checkHugeFails()` constructs
`BigInteger.ONE.shiftLeft(500_000_000 + 1).setBit(0)`, calls
`a.isProbablePrime(1)`, and rejects every normal return. Its original
`ArithmeticException` catch returns true.

The port-only adaptation changes that behavioral assertion: a normal return
must be `false`; a normal `true` fails. The original `ArithmeticException`
catch remains byte-for-byte intact and continues to accept that exception.
No exception is manufactured and no other exception is caught. The operand,
certainty 1, `main` call to `checkHugeFails()` and every other public test
statement, directive and notice are retained. The imported type alone is
redirected as in the other port tests. The host baseline uses the original
public test and retains its original behavior.

The adapter pins the complete original `checkHugeFails()` method as `before`,
requires `source.split(before).size == 2` (exactly one occurrence), constructs
`after` by replacing only the original normal-return statement/comment block,
and rejects an unchanged or already-present replacement. After replacement it
requires exactly one `after` and checks
`source.replace(after, before) == importOnly`. Thus the inverse must recover
the entire import-only source exactly. These are source guards prepared here;
they have not been executed at this milestone.

## Why the normal-return assertion is appropriate

The public API's prime-domain support guarantee through
`2^500000000` is a minimum supported range, not a mandatory ceiling requiring
every larger input to throw. See the public
[BigInteger API class contract](https://docs.oracle.com/en/java/javase/25/docs/api/java.base/java/math/BigInteger.html)
and its `isProbablePrime` contract. This classification does not add a
production cap or claim that all larger inputs are supported.

For the retained input, let `k = 500000001` and `n = 2^k + 1`. Since
`2 ≡ -1 (mod 3)` and `k` is odd, `n ≡ (-1)^k + 1 ≡ 0 (mod 3)`.
Also `n > 3`, so `n` is composite. This gives a mathematical reason to require
`false` if this call returns normally; it does not establish what the old
execution returned or prove resource bounds.

The probabilistic public API permits false positives for composite inputs;
this is not a universal demand that `isProbablePrime(1)` return false for
every composite. The parent reviewed the owned implementation's small-prime
trial-division branch (`BigInteger.kt:718–734`), including divisor 3. For this
specific fixture, that branch supplies the owned normal-false guarantee,
conditional on the arithmetic. That source reasoning still does not recover
the unobserved historical Boolean.

## Lost coverage and retained evidence

The original mandatory-cutoff expectation is explicitly lost port coverage.
The adapter emits one companion SKIP describing it, and the manifest marks
the test as adapted under `port-prime-range-policy`, rather than imports-only.
An eventual adapted PASS would be an adapted PASS only, never an original
public-test PASS.

Frozen campaign `1792044478663` remains **19 PASS / 1 PrimeTest FAIL /
120 SKIP**. Its original `PrimeTest` FAIL and original host-baseline behavior
remain evidence. The old normal-return Boolean was not observed: a normal
`false` is a new explicit check, not a retrospective observation or recovered
historical result. No future success counts are assigned here. Earlier
timeout evidence is preserved without reopening completed tasks 564 or 530.

The four `BigDecimal/Constructor.java` non-null-context adaptations and their
four lost-coverage companions remain unchanged. Neither this source change
nor future success for this one adapted test supplies official JCK/TCK
acceptance, other public-test coverage, full-range conformance, or a proof of
runtime, allocation or resource behavior. Those questions remain open.

## Artifact boundaries and preservation

Before editing, exact originals were archived in the new directory
`/private/tmp/prime559-source-preparation-v1/`:

| Original file | SHA-256 |
|---|---|
| `adapt.main.kts.original` | `05c05df6ce1c92d75eef368d7d9730f67df8ce3cef3919072431b15e37e4ad2f` |
| `manifest.tsv.original` | `05b30e6abf14c9584146e22d2b720ece8898770b7e9562bea1b9884ec60e40e7` |

The source baseline is commit
`71fb80e07b871c4a25d9da7f129b4e4c5feb1fbc`. The current parent-selected JAR
identified by prefix `ded66` is outside this preparation; no current source
hash is retrospectively assigned to it. The parent's unchanged-production
classification since `de5` is retained, not re-proved by this source milestone.

The adapter remains `SPDX-License-Identifier: GPL-2.0-only` and identifies the
additional original OpenJDK copyright. Generated test notices are preserved
by the existing notice guard; no foreign numerical implementation body or
bytecode is introduced. No script, helper, compiler, numerical test, probe,
build, harness or benchmark was run, and no Git index, refs or tracker state
was mutated.
Only source reads, copies, edits, hashes and diffs belong to this milestone.

## Parent targeted verification

Independent read-only source review accepted the adapter, manifest, inverse
guard, original-baseline separation and one lost-cutoff companion before
execution. Root grant2274 then authorized one fresh `--both` campaign filtering
only `BigInteger/PrimeTest.java`, profile `heavy`, seed559 and subset0. It used
Liberica25.0.4+9-LTS / Kotlin2.4.21, a256MiB script heap and512MiB test heaps.
The original120-second test/compiler/audit and30-second runtime-precheck
limits were unchanged; no whole-campaign absolute deadline is claimed.

The clean workspace had no inherited build/results/classes/generated/cache.
The runner verified57 pinned public sources and14 dependency records. Both
compilations, the runtime-version precheck and the port caller-bytecode audit
exited0 without timeouts. Port core loading was verified against exact JAR
`ded66cd6a325b73a9a393e0fd56281d7bf63d571dab4b146e8f1cb9c0dba2fd1`;
the caller audit has no host math member references. This JAR is reused from
the accepted current-production build, not rebuilt for a test-only change.
The parent checked unchanged commonMain/jvmMain/build inputs from published
d499 through71fb and rehashed the artifact.

Actual run `1792074254976` is complete=true and officialJck=false:

| Invocation | Result | Elapsed |
|---|---|---|
| `baseline/BigInteger/PrimeTest/run-1` | PASS, original source |1093ms|
| `port/BigInteger/PrimeTest/run-1` | PASS, adapted source |22584ms|

The139 SKIPs comprise116 filtered public records,16 upstream-disabled,
two upstream-commented, four excluded-private and exactly one PrimeTest
adaptation companion. The four Constructor companions remain in source but
do not execute in this filtered campaign. Original baseline source hashes to
`e539033a27d32ad15924a1522ab3ba4d444b2b2283ca033099c0fa60c2c9eb09`;
the compiled adapted port source to
`6099915bfab3b7cadfa91313f504434086b619752d11c7a5f68bea1a70c54d31`.

The raw output confirms completion of the first19 Mersenne checks. The adapted
success accepts normal false or the original ArithmeticException. No branch
or Boolean telemetry was added: this result does **not** observe which huge-input
outcome occurred. In particular, it does not establish broader prime-domain
support or recover the old unobserved Boolean. The old19/1/120 ledger stays FAIL.

Outer invocation94935 terminated0; all captured parent PIDs45244/45254/45262
were absent at the scoped terminal check. All child records completed; no
timeout, retry or cleanup signal occurred. Before/after tool manifests are
identical. Host total memory was freshly observed as36GiB; free-memory samples
were below1GiB, and this512MiB heavy profile did not require the large-memory
capacity gate. No capacity, timing-comparison or uncontended-host claim follows.
The pinned test tag25.0.4.1 and BellSoft runtime25.0.4 revision/vendor difference
remains disclosed by the runner.

Execution capsule `/private/tmp/prime559-range-execution-v1-final.tar`:
`250e58f17df62f54c054a2a91dd72e9ebfe6ed17f4e7e8ccee13f28ee67a5001`.
It includes raw results/logs, actual sources/classes, origin/caller audits,
tools and invocation/process evidence; the compiled-script cache is excluded.
The preceding capsule remains retained; its only later change is one wording
correction in invocation.txt, with no raw/source/runtime change.

```text
8fb7a0b3bb80e187a7bd6918a8123789b1cc5bf46bb2d9de87e8941389d38ef3  results.json
08af437e1393e3ae51e2dd904dbf0c9d6eb5a9cb378f5f7a73a4db73ff20daa8  campaign.log
f62592f09eba93f117bc474d0f71bd616f2c882f8dd9a9a621058fa66abfaf89  tools-before.sha256 / tools-after.sha256
```

The accepted source-only document and exact three files were frozen before
this parent report update in `/private/tmp/prime559-source-preparation-v1.tar`,
SHA-256 `b485e117175c491a722fa3dfad2d6038c8665add70e016234f827cd633f11f1b`.
Its original document hash is
`aa8c566ab7e3d12954218acbeb56d23309ab82098b173d77906c2a3ca35ed75d`.
The adapter45db93c7 and manifest60bf46b5 remain unchanged during execution.
