# Task528 V2 source/proof milestone — grant1918

SPDX-License-Identifier: Apache-2.0

**SOURCE ONLY: V2 production/new tests/auditor are UNCOMPILED/UNRUN.** This grant
became effective after all-terminal1887 release1920 at2026-10-15 00:16:36 UTC.
Root owns the next orthogonal525/565/pow numerical window. No V2 performance or
test pass is claimed; task528 improvement gate remains OPEN and linked/in_progress.
Changes are not committed; no Git/task-state mutations.

## Preserved V1 before editing

All ten original accepted frozen source/proof/test/audit/certificate/driver/plan
files were copied into uniquely named `/private/tmp/task528-v1-frozen-1920` and
verified with `shasum -c FROZEN_SOURCE.sha256` BEFORE any V2 change. Files and
directories were made read-only with `chmod -R a-w`; later verification still
passes all ten entries. Manifest SHA256
`4d7d7bbd9fab26b4bda0fd5c288461f2130734711aa641e2dff727694b371371`.
Read-only archive `/private/tmp/task528-v1-frozen-1920.tar` SHA256
`76d100d073d5db7d142038681482bbcca49c70e67dcc8594b2f4c74b282a23ec`.
Read-only here describes filesystem mode protection, not a claim about an OS
immutable flag or adversarial deletion protection.

V1 baseline/candidate fresh JARs, complete4320 samples, supervised process logs,
source/build manifests, initial audit failure and every regression stay untouched
in `/private/tmp/task528-execution-v1`; evidence manifest SHA256
`0c076b6f37f2aec5b83f7686a965736236b9bae6bb1d0027c2f628cc32722d4e`.
Original runtime report SHA256
`496d8b243bd76fe691be4272f05908a00087bc2e151d9b0437dc582a744d39ff`
and original proposal SHA256
`c5968c5f2ade38866013481542472b3230f3e2d116585d8993c788b38781bb64`
remain historical records. The proposal's earlier UNIMPLEMENTED status refers
to that V1 milestone; its algorithm now has an uncompiled V2 implementation.

## Implementation and proof scope

Only the owned `compareDecimalSignificand` region plus two private stored-word
helpers changes in BigInteger versus V1. It clones the longer stored words once,
divides the active quotient prefix in place by10^9 and a positive-only10^r tail,
then compares active length/words with the shorter stored magnitude. Each pass
computes remainder BEFORE ORing sticky, even when sticky was already true.
The helper never calls the generic capacity-based comparator on untrimmed
scratch. Inputs are read-only; scratch never aliases, escapes or is retained.

`DECIMAL_METADATA_COMPARISON.md` now proves each Long intermediate
`t<m*2^30<=10^9*2^30<2^60`, exact prefix division, the quotient/remainder product
identity and sticky equivalence, nonzero supported partial quotients, active
length and primitive tail bounds. The chunk phase has one O(n) local scratch
and O(n*ceil(k/9)) shrinking-prefix primitive divisions. Digit-count threshold
temporaries remain; one quotient scratch does not mean only one allocation
in the entire comparison. Avoiding the old intermediate remainder objects is
a source argument, not a measured allocation/time result.

BigDecimal is byte-identical to V1, including public precision/exponent decisions
and full-Long guards. Exact E/E+1/maximal-threshold helpers and accepted562 binary
helpers are unchanged. No existing arithmetic kernel, field, cache, power table,
public API, serializer/legacy compatibility or inherited callback guarantee
changes. Original common/JVM test files and their seven/three methods/assertions
remain byte-identical.
No original diagnostic/fixture/assertion is weakened or deleted.

## Three new prepared common methods

`DecimalChunkComparisonTest.kt` is a separate file with bounded literal models,
maximum coefficient149 decimal digits, including the extra-zero equality control.
Its cases cover:

- k1,8,9,10,17,18,27,36,63,129, both signs/orientations; exact cohorts, quotient
  below/equal/above S, one-unit remainder and discarded middle10^9 remainder
  followed by zero remainder passes.
- Literal neighbors of10^9,2^30 and2^60, including quotient limb/carry boundaries
  and leading zero scratch capacity. Predecessor-plus-tail cases detect skipped
  passes after sticky became true; equal cohorts detect an untrimmed-size compare.
- Ordinary and truthful integer subtype input strings/bytes/hash values across
  repeated and subsequent comparisons, plus truthful/reported decimal precision
  controls and retained explicit precision dispatch order. No inherited integer
  abs/string callback count is asserted; current own-format JVM controls stay
  in their original frozen test file.

All three methods are SOURCE/PREPARED only. Original methods passed only on V1;
their execution on V2 remains pending. No fixture construction or oracle/port
comparison was executed while preparing these sources.

## Refreshed static auditor SOURCE

The revised `.main.kts` auditor binds the immutable ten-file snapshot and seven
unchanged live files, verifies only the significand/private-helper region changes
against V1 and all owned insertion scope against03ebb60, pins V2 source/test/proof
hashes, retains accepted562 hashes, checks one quotient clone/eager division/
positive tail/active-length use and no formatting/padding/callbacks in stored
helpers, and records three new test annotations. It has not been compiled or run.
Source review and CLI hashes/whitespace inspection are the only current checks.
The original bounded certificate is unchanged and was not rerun under1918.

## Current exact hashes

| Artifact | SHA256 |
|---|---|
| BigInteger V2 | 24cfd50f0dd9edd25590877b23b4c2e72c89ce9fab71f4b75bc0fe11def56a5f |
| New DecimalChunkComparisonTest.kt | b0aadb817b8f90e89d88032451cc26db1030eb722ae98370e2fb1a77c842bfa4 |
| Comparison proof V2 | 67b22ddb9af0d32483f7624b4be12770ca7b7f6456e388361d490090acb792b0 |
| Static auditor SOURCE V2 | 4c4281fb0e830cd09b7549a0e5a128a589f813b29ee47a472bf74206627643a5 |
| Unchanged BigDecimal | 92de3354da9545f5b4867ee2ed32e72c91fac4ed32aa5c666f5762bd9f723370 |
| Unchanged original common tests | c839d17adf6312e5faaa4a6336f75ee663f17eee901f6f545b5aa744ea055ddf |
| Unchanged original JVM tests | 28815bdaa115bfffac83e9810b17c80a15751cfb804ce0b3810be899c726b849 |
| Unchanged certificate | bf457b19276a9deb49d2804cf026eccb1f04e1f5575fd84d9985784161a50acc |
| Unchanged original handoff | 64b2a8eaf263192a3bce86abb31e8d6096ff25fc9210020f9847b8c001cc772f |
| Unchanged measurement driver | 0e4f59ecc33350eb097d4bfe262a54a53853c2e00078f04275035bca7b2c09c5 |
| Unchanged measurement plan | edb96d8216d31e80cb8aa3f633dd147d4d59c499a0f55effdd87651bd3a05ae0 |

Independent implementation/bounds/test/auditor-source review found no numerical,
immutability or test-expectation defect and confirmed exact scope, seven retained
live hashes, ten historical hashes, accepted binary artifacts and archive hashes.
Two nonblocking wording findings were corrected: proved arithmetic bounds versus
runtime guards, and only later chunks after the nonzero second remainder have
zero remainder (drop18 ends at that second pass). Test/proof hash bindings in the
auditor were refreshed; BigInteger source is unchanged from its reviewed24cfd50f
version. Final read-only correction/handoff verification found no remaining
actionable finding and accepted the source/prepared-test milestone. It verified
the pre-final-status handoff hash
`d3dbe0e54fca7e6827b7637a45fdd9a09bb67825310a07d1febd6b9ab25a5bb6`
and all current source/test/proof/auditor bindings. Reviewer executed no script,
library/compiler/test/probe/certificate and edited nothing. The final handoff
hash is recorded separately in the parent task comment. No compiled
script/library/test/probe/certificate/benchmark,
Gradle/javac/Native/public harness or new JAR was produced for V2.

## Later grant requirements

Parent1926 independently accepted the final code/test/proof SOURCE conditionally,
with no concrete defect and no runtime/performance pass. Root1919 owns the
current orthogonal numerical window. Final auditor/handoff source acceptance and
a separate sole numerical-window transfer remain required before any V2 script/
library/fixture/test/build/measurement execution.
Retain all original methods plus the three new common methods in complete
serial JVM/JS/Wasm/macARM gates and root exact-integrated-head all-six CI.

The unchanged accepted measurement driver intentionally hard-pins the V1
candidate production hashes. It cannot measure V2 as-is. A later narrow parent
grant must permit a new provenance-bound driver revision for V2 hashes while
preserving the historical0e4f59ec driver and every cohort/budget/fixture/result/
counter/cache/origin guard. No such revision or measurement is made in1918.
Later fresh matched JARs and the unchanged balanced six-fork protocol must show
actual allocation/time evidence; V1 regressions and noisy timings remain.
No automatic overall task acceptance follows from this source/proof milestone.
