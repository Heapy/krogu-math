# Numeric decimal metadata and comparison (task 528)

SPDX-License-Identifier: Apache-2.0

This source/proof milestone removes complete coefficient decimal strings from
`BigDecimal.storedPrecision`, `compareMagnitude`, and the companion's
`compareValues`. It extends the accepted task 562 estimate without modifying
`decimalDigitLowerBound`, `decimalBinaryPrefix`, their proof or tests, binary
conversion, arithmetic kernels, formatting, or public comparison dispatch.
The implementation is independent common Kotlin; no numerical implementation
from OpenJDK, Kotlin stdlib, or third-party libraries was consulted.

## Exact digit count over the supported domain

For stored nonzero magnitude n, define D by `10^(D-1)<=n<10^D`, and let b be
its positive bit length. The accepted estimate is

`E=floor((b-1)*1292913986/2^32)+1`.

`DECIMAL_BINARY_CONVERSION.md` proves `E<=D<=E+1` for every supported
`b<=Int.MAX_VALUE`. This handoff's independent bounded rational certificate
rechecks the strict logarithm bounds and the maximum product/E. It does not
execute the port or allocate a maximal magnitude.

The maximum E is 646456993. Exact logarithm bounds certify
`bitLength(10^646456992)=2147483644` and
`bitLength(10^646456993)=2147483648`. Thus every threshold `10^E` with
`E<=646456992` is supported, by monotonicity. Comparing stored n with that
threshold selects E if smaller and E+1 otherwise, including exact equality.
At E=646456993 every supported n has `n<2^2147483647<=10^E`, so D=E;
the unsupported threshold is never constructed. There is no exception-based
classification and no smaller artificial domain cap. Stored zero has D=1.
Signs never enter this count, and the bound on E+1 easily fits Int.

The unchanged TEN.pow kernel removes the factor 2, builds powers of 5, then
restores the binary shift. For supported exponent e<=646456992, the early
odd-part guard uses `2e<Int.MAX_VALUE`, the restored shift is within Int,
and every intermediate exponent is <=e. Intermediate powers of 5 are no
larger than 5^e; the restored result is the certified supported 10^e.
The multiplication lower-bit guard cannot reject a supported actual product.
Temporary limb/carry space and available memory/time remain requirements;
this proof does not promise execution at the maximum allocation size.

## Equal-decade comparison by quotient and remainder

For positive coefficients x,y with digit counts Dx,Dy, decimal significand
ordering compares `x*10^(M-Dx)` and `y*10^(M-Dy)`, where M=max(Dx,Dy).
If Dx=Dy, stored magnitude ordering suffices. Otherwise let L be the longer
coefficient, S the shorter, and k=Dlong-Dshort>0. Since Dshort>=1,

`1<=k<=Dlong-1<=646456992` and `10^k<=10^(Dlong-1)<=L`.

The divisor is supported and no larger than an input. Write
`L=q*10^k+r`, with `0<=r<10^k`. Comparing L with `S*10^k` has the same
order as q with S when those differ: a one-unit quotient difference outweighs
any remainder. If q=S, the order is zero exactly when r=0, otherwise positive.
Reverse this order if the original left coefficient was the shorter one.
This proves equality, trailing zero cohorts, arbitrarily distant nonzero tails,
and both quotient directions, without constructing the aligned product.
Zero is ordered below every nonzero magnitude and equal to another zero;
the helper ignores coefficient signs deliberately.

### V2 local decimal-chunk quotient and sticky remainder

The v1 helper used the unchanged multi-word division kernel. Six measured forks
under1887 found128/1024/4096-digit comparison allocation ratios1.275/1.904/2.251
versus baseline. That candidate, its complete raw evidence and ten frozen files
are preserved; see DECIMAL_METADATA_RUNTIME.md. Parent source-only grant1918,
effective after terminal release1920, permits the following narrow replacement.
V2 is UNCOMPILED/UNRUN; these are proofs/source arguments, not improved results.

Clone L's little-endian30-bit words exactly once into a local quotient scratch.
Track active length rather than interpreting the untrimmed capacity as a
canonical magnitude. Divide that active prefix in place by10^9 for every full
nine-digit chunk, then by10^r only if r=k mod9 is positive. Primitive tail
power construction has at most8 multiplications and produces1<=10^r<=10^8;
the zero-digit tail is skipped. Each pass runs unconditionally before its
remainder is ORed into sticky. Short-circuiting a later division when sticky is
already true would leave a wrong quotient and is forbidden by the source.

For each small divisor m,0<m<=10^9<2^30. The high-to-low loop invariant is
0<=remainder<m. With stored limb0<=w<2^30, its next intermediate is
t=remainder*2^30+w <m*2^30<=10^9*2^30<2^60<Long.MAX_VALUE.
Hence floor(t/m)<2^30 fits a stored Int limb and t mod m fits nonnegative Int.
These bounds make the primitive shift/add/division exact, without runtime
overflow guards or numerical estimates.
Induction over the processed prefix proves exact one-word long division.
An iteration reads each original lower limb before replacing that limb, so
in-place mutation is safe. Trimming only leading zero scratch words preserves
the quotient; active length does not grow. The shorter input remains read-only.

For passes Q(j-1)=Qj*mj+rj with0<=rj<mj, their product is exactly10^k.
Substitution gives L=Qfinal*10^k + sum(rj*product(m1,...,m(j-1))).
Every remainder contribution is nonnegative, so the original remainder is
nonzero iff any pass remainder is nonzero. The Boolean sticky is exact even
when a nonzero middle pass is followed by zero remainders. No large remainder
or10^k is stored. Comparing Qfinal's active length then high-to-low stored words
with S gives the quotient order; on equality sticky selects positive or zero.
The generic canonical-array size comparator is never used on scratch capacity.
Original operand orientation is reversed once, and coefficient signs are ignored
as before. No caller-overridable integer method participates in chunk division.

Because every cumulative drop<=k<=Dlong-1, each partial quotient is nonzero
and no larger than L. Scratch capacity is exactly the supported input's limb
count; no aligned product, larger intermediate, unsupported power or artificial
domain cap is introduced. Drop/subtractions/active indices and the at-most8 tail
power steps fit Int. Existing pow/division/arithmetic kernels, fields and the
accepted562 helpers remain byte-identical. For n input limbs, the chunk phase
uses O(n) local storage and O(n*ceil(k/9)) primitive divisions with shrinking
active length. It removes the old per-binary-bit intermediate remainder objects
in this path; actual allocation/time improvements require later measurements.

## Exponents, signs, and public override behavior

The public `compareTo` compact shortcut, virtual signum prefix, zero decision,
and negative-order reversal are byte-for-byte unchanged. `compareMagnitude`
retains stored-zero handling and the same-scale fast path. For different scales
and nonzero magnitudes it still calls left `precision()` and then right
`precision()` through `adjustedExponent()`, once each, and uses their answers
with stored Int scales. The Long expression is safe even for any returned Int
precision. Exceptions from these retained explicit public signum/precision
calls propagate as the same objects at the same call site/order. Truthful
BigDecimal direct-super overrides retain their numerical meaning and public
metadata call order; compact same-scale comparisons still bypass these calls.
This does not promise unchanged internal coefficient abs/toString observations
when an inherited decimal operation operates on a restored coefficient subtype.

Misleading precision is classified separately: if the *reported* exponents
differ, the old reported-decade decision is retained. If they match, the old
padded-string ordering was ordering of *stored* normalized significands,
independent of those precision answers. The quotient/remainder helper computes
exactly that same ordering for truthful stored-value-consistent coefficients.
No public BigDecimal precision answer is silently replaced by stored metadata,
and no existing diagnostic/fixture assertion is disabled or relaxed. Existing
coefficient abs/sign/ordering staging before the equal-exponent step is retained
in compareMagnitude. Internal coefficient formatting calls disappear from its
equal-exponent step and storedPrecision, and abs/formatting calls disappear
from compareValues. Their inherited call traces are observations rather than
inherited BigDecimal callback-count guarantees.

Constructors ordinarily normalize subclass coefficients into base BigIntegers,
but this is not an unconditional invariant: current own-format JVM readObject
uses defaultReadObject and checks only that the coefficient is non-null.
ObjectInputStream.resolveObject can supply a consistent BigInteger subtype
during an ordinary current-format roundtrip, which the decimal retains.
No constructor or serializer change, normalization of reader-supplied objects,
legacy stream, or cross-format compatibility is introduced here.

The companion's private `compareValues` has exactly two callers in this base:

1. Context division passes absolute coefficients, ordinarily base values but
   potentially truthful subtypes from a resolved coefficient's abs override, using the
   **reported operand precisions as Long scales**. These may be misleading or
   negative; their role is preserved without new callbacks or a truthfulness
   assumption. Downstream code consumes only `normalized>0` and related signs.
2. Exact-result checking passes a product coefficient with a sum of
   two stored Int scales, and the source coefficient with its stored Int scale.
   It consumes only equality with zero. Either the original source coefficient
   or a result of a truthful arithmetic override can retain a consistent subtype.

Both private callers can therefore receive a BigInteger subtype. Numerical
equivalence is proved for truthful stored-value-consistent coefficients: their
signum reports the stored sign, abs returns its exact stored magnitude, relevant
arithmetic/ordering overrides return the exact stored-value result, and decimal
formatting describes that value truthfully. Exact stored digits and normalized
significands then equal the former truthful abs/string result, regardless of
runtime class. Sign checks retain their previous structure, zero remains equal
at all scales, and negative nonzero ordering is reversed. Removing internal
abs/toString calls may change subtype traces or remove an exception that an
inherited decimal operation formerly encountered at one of those internal calls.
Those observations are not an inherited callback guarantee. Explicit caller
calls still dispatch to overrides; direct super calls retain their language
meaning, and exceptions thrown directly by an explicit override retain identity.
This argument neither guarantees nor waives every possible poisoned inherited
outcome; each such claim requires its own classification and evidence.
Internal Long scales include sums outside Int:
the actual scale ranges are reported Int precisions, [-4294967296,4294967294]
for the summed scale, and ordinary Int scale. Even beyond those callers, the
new helper arithmetic works for every Long scale without overflowing subtraction.

For digit difference delta=Da-Db, compare `(sb+delta)` with sa. This is
equivalent to comparing `Da-sa` with `Db-sb`, but it never forms those potentially
overflowing adjusted quantities. Delta is within [-646456992,646456992].
If delta>0 and `sb>Long.MAX_VALUE-delta`, the mathematical sum exceeds every
possible sa, so the result is positive. If delta<0 and
`sb<Long.MIN_VALUE-delta`, it is negative. Otherwise the addition fits Long
and an ordinary comparison is exact. Both guard subtractions fit Long.
On exponent equality the significand helper supplies normalized -1/0/1.
The old private String.compareTo result could have a larger absolute value;
both callers consume only sign/equality, so normalizing that return magnitude
preserves their comparison-result branch decisions for truthful,
stored-value-consistent inputs. This statement concerns return normalization
only; removed internal callbacks/exceptions and poisoned inputs remain subject
to the classification above. New JVM tests directly exercise full-Long
private-kernel boundaries.

## State, memory, and performance claims

There is no new field, mutable cache, lazy state, or serialization member.
Metadata is computed from immutable stored limbs, so there is no new cache
publication race across JVM, JS, Wasm, or Native. The quotient scratch is cloned
once per unequal-digit comparison and never aliases or escapes into either
input, another call, a result object, a field or shared state. Threshold digit
computations still allocate their existing power temporaries; one quotient
scratch is not a claim of only one array allocation in the entire comparison.
No extra retained per-instance cache memory is introduced by this source diff.
This is a structural statement, not a measured instance-size or speed result.

The current implementation can recompute digits in the significand step after
the exponent step; threshold powers and scratch storage can allocate O(input
bits) temporary storage. V1 allocation regressions remain evidence; V2 is not
claimed faster or lower-allocation until a separately granted measured campaign.
No performance claim follows just from removing String calls or counting objects.

## Prepared checks and later execution gates

New common tests independently use bounded known decimal text to cover decimal
and binary power neighbors, compact boundaries, signs, zero, all Int-scale
extremes, normalized quotient/remainder ties, long zero tails, stored-helper
subclass isolation, truthful direct-super dispatch, exception identity, and
misleading precision. They do not construct the symbolic giant witness.
New JVM tests compare bounded precision/comparison with public JDK behavior and
use exact JDK integer exponent expressions for the private full-Long comparison.
One further bounded JVM test writes the current own BigDecimal object format
and resolves its coefficient to a truthful subtype during reading. It checks
retained coefficient identity/value, numeric precision and different-scale
neighbor comparisons against literal orders and an ordinary coefficient control
for zero and both signs. Separate explicit abs/string, direct-super and directly
thrown exception-identity controls verify caller dispatch; no inherited
abs/string callback count or poisoned inherited outcome is asserted.
The certificate uses 256-term rational logarithm bounds plus symbolic
digit/scale endpoints. It never loads the port or uses a decimal parser.

Existing tests/fixtures are unchanged, including DecimalComparisonSubclassTest,
DecimalContextDispatchOracleTest, DecimalIntegralDispatchOracleTest, the public
comparison fixture evaluator, DecimalBinaryPrefixTest and conversion suites.
The older string-based argument in NUMERICAL_ARGUMENTS.md is historical
baseline evidence and remains untouched; this document supplies its numeric
replacement within the granted scope.

The seven original common/three original JVM methods passed on V1 under1887;
those files/assertions remain frozen and must be retained for V2. Three further
common methods in DecimalChunkComparisonTest.kt are SOURCE/PREPARED only. They
cover k1,8,9,10,17,18,27,36,63,129, literal neighbors of10^9/2^30/2^60,
quotients below/equal/above S, remainder1, middle10^9 remainder followed by final
zero remainders, both signs/orientations, ordinary/truthful input immutability
and truthful/misleading reported exponent controls. The predecessor-plus-tail
cases catch skipped divisions after sticky became true; exact equal cohorts
catch capacity-versus-active-length errors. Expectations are literal models,
not outcomes of port arithmetic. Existing current own-format/explicit caller
dispatch controls remain unchanged; no inherited callback-count guarantee.

Only source/static metadata reads and independent read-only review are authorized
by1918. NO certificate/script compilation or execution, library compilation,
tests/probes/fixture execution, benchmark/harness/Native/javac occurs in this
phase. Root owns the next numerical window. Do not treat prepared V2 tests as
passing. After a separate later grant, run JVM,
JS, Wasm and macARM relevant retained suites with each target's trailing
wildcard filters repeated after that target. Retain exact commands, logs,
counts/XML, source hashes and historical failures. Root must integrate serially
and require all-six CI at the exact integrated head.

Measurement plan under that later grant: freeze baseline and candidate hashes;
measure cold/repeated precision and unequal-scale comparisons at compact,
128-, 1024-, and 4096-digit sizes, with equal cohorts, early differing decades,
quotient differences and a last-digit nonzero remainder. Compare identical
iterations/warmup/heap/toolchains and consume results. Record JVM thread
allocated bytes or allocation recordings as well as elapsed time, and report
temporary allocation plus retained-instance cost explicitly. Extend timing to
JS/Wasm/macARM as granted. Existing formatting/binary-conversion cases must
remain in retained regression checks. Report regressions and noise, without
discarding baseline evidence or claiming a giant-input measurement.

## Snapshot and handoff record

Base HEAD: `03ebb60a482873acb3fe892f0c86510472f2e0b3`.
Branch: `task/528-numeric-decimal-metadata`; initial worktree clean.
No commits, staging, branch switches, or other Git mutations performed.

Baseline SHA256:

| Path | SHA256 |
| --- | --- |
| BigDecimal.kt | 1587d2351f85399bb3d3dbd505fffd812cb7e8b8aedab4f371d4c27520334f2f |
| BigInteger.kt | 73c52b66a263c51ee57461acc9fc38c99950a43e27ad6a490713785d5e4d435b |
| DECIMAL_BINARY_CONVERSION.md | f77248b6219a9ca99566caa3ef95456e30e3c0bb3b3cce8b742f4c87fcb48a47 |
| scripts/certify-decimal-binary-range.main.kts | 7c2c4b98e0f1081dcd8030b2138e06b89b4b355375ba95f2b816ffd81d4b36ff |
| DecimalBinaryPrefixTest.kt | 8632faf6eb22fc32af9de5a9c999694898e747afcf8c14ef8bda9764dcb8eaa3 |

V1's ten exact frozen files were archived and verified BEFORE V2 changes at
/private/tmp/task528-v1-frozen-1920, with read-only files/directories and manifest
SHA4d7d7bbd9fab26b4bda0fd5c288461f2130734711aa641e2dff727694b371371.
Read-only archive /private/tmp/task528-v1-frozen-1920.tar SHA
76d100d073d5db7d142038681482bbcca49c70e67dcc8594b2f4c74b282a23ec.
shasum -c verified all ten entries before changes. V1 fresh JARs/raw evidence/
initial failures remain untouched in /private/tmp/task528-execution-v1.

V2 source/proof/test/static-auditor source hashes and independent review are
recorded in DECIMAL_METADATA_V2_HANDOFF.md. The refreshed static auditor is
SOURCE ONLY, UNCOMPILED/UNRUN; the original symbolic certificate stays frozen.
Task remains linked/in_progress; root alone grants runtime/integrates/closes.
