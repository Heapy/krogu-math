# Primality, generation and successor error budgets (547)

SPDX-License-Identifier: Apache-2.0

This is the conditional mathematical binding for all supported ordinary
size/certainty classes. It assumes correct completing integer leaves, candidate
blocks uniform conditional on previous history, and witness blocks uniform
conditional on previous history and the current candidate. It establishes no
unconditional theorem about actual platform PRNGs,
hostile callbacks,548 allocation capacity, whole547 or full-project acceptance.
Preparing this document performs no numerical run or fixture/assertion change.

## Current binding

| File | SHA-256 |
| --- | --- |
| `src/commonMain/kotlin/io/heapy/krogu/math/BigInteger.kt` (locally validated composition) | `12fd0a14164bfd81b004c66673ae37ea40719bf3552f171ba20b434a296d8602` |
| `BigInteger.kt` baseline for line references and historical583 execution | `d6d531742a7b12bd6cc09cf6b98daca11f70b5207687ecd4c04c5a11d655a9b2` |
| `src/commonMain/kotlin/io/heapy/krogu/math/PrimalityCertainty.kt` | `93f0c4743f9a7d8d9acdedd283760904bbfd2caf55e5e4973dd6331d2aa57479` |
| `src/commonMain/kotlin/io/heapy/krogu/math/PrimeSearchLimits.kt` | `66e7d92b5d6091067ee02679534060fc9d8559328c7fafc45805b3d7493c3a85` |
| `src/commonMain/kotlin/io/heapy/krogu/math/PlatformRandom.kt` | `e408e6fad2a92acec592520ea3eaf1c414493a6db708db9ef9ae686b1303f3d0` |
| `src/jvmMain/kotlin/io/heapy/krogu/math/PlatformRandomJvm.kt` | `5acb4c2184f981699c5fec755bf223ab61a441af84275c5151da6cc4fde345fb` |
| `src/nonJvmMain/kotlin/io/heapy/krogu/math/PlatformRandom.kt` | `8673184b6bfcd9117a12c91a3152f265afec42f2f17d1309963ac5498c472319` |
| `src/commonMain/kotlin/io/heapy/krogu/math/RandomFactories.kt` | `d66f6002d3e2f32b1a2f03143984eee2da55a2fc7edda2bf6e8bdada41257c20` |
| `docs/prime-search-range-559-proof.md` | `002d68d8fb6162ec27c356273a5f0b0d34f2d238d83b3653e0064a5367f16d5f` |
| `docs/platform-random-validation-549-proof.md` | `20921ad4f1e4729849871b6b129ebf2c628ca7329cee065f2cd36986bd0a3f59` |

This table binds the locally validated585/586 composition at
`/private/tmp/krogu-585-586-validation-g0etGe15/root`, whose240-source manifest is
`e3539c0bda309350d72ebb89656a9f54e3c7f720fe943616153050e2be493aa9`.
All BigInteger line numbers and ranges below use the preserved `d6d53174…`
baseline. The new `12fd0a14…` file adds only18 lines after baseline line82 for
the nonvirtual decimal-only `alignedDecimalSum585` bridge; every preexisting byte
before/after that insertion is unchanged. All cited BigInteger regions therefore
occur18 lines later in the current source. PrimalityCertainty and other named
files keep their existing line references and hashes.

Primality predicates, candidate generation, successor search, RNG adapters,
round budgets/constants and their modular/integer leaves remain unchanged and
do not enter the bridge. BigDecimal's sum seams and private sticky equality586
branch are outside these call graphs. The conditional probability argument,
actual-PRNG assumptions and resource/callback limits below are unchanged. No new
math waiver, cap, API/layout/cache or serialization condition is introduced.
The separate [local verification](decimal-sum-sticky-585-586-verification.md)
records1,610 passing method executions across JVM/JS/Wasm/macOS ARM with unchanged
source240 and own JAR
`1cacc5b41ede2e154e9c7baf948bd1748f4d424932f3fb7e4a592085b1ed00e1`.
This is finite integration evidence, not a new probability experiment or proof
of actual-PRNG premises. Matching hosted/public gates for this240-source revision
remain pending; independent actual-adoption review and publication remain
separate. This draft executes nothing and adopts
no historical run as a new pass.

Current modular leaves/context/fallback and their precise proof pins are bound
in [modular substitution546](modular-substitution-546-proof-binding.md). The
[numerical arguments](../NUMERICAL_ARGUMENTS.md#primality-and-prime-generation)
remain the general mathematical foundation. Compatibility-summary prose is not
a substitute for these source/proof pins or the probability premises.

## Complete predicate and witness partition

BigInteger.kt807–809 returns true for every certainty c<=0 before value/callback
work. For c>0, truthful abs reduces either sign to N>=0; lines812–827 exactly
handle2,0/1/even values and prime/small-factor cases through47. Every surviving
candidate is at least53; every fixed base2..41 is in its permitted range. With
N-1=2^s*d, d odd, lines828–835 implement the strong test. Every prime passes.
Omitting an early reject on an intermediate1 cannot turn a composite failure
into success: subsequent squares remain1.

Lines837–843 and constants1140–1141 use the first thirteen prime bases and strict
N<3317044064679887385961981. [Sorenson–Webster Theorem1.1](https://arxiv.org/html/1509.00864v1)
certifies conclusive decisions below that threshold; equality and larger values
use the randomized route. No probability round cap is substituted for certainty.

For odd composites, the [Rabin strong-liar theorem](https://doi.org/10.1016/0022-314X(80)90084-0)
bounds the passing fraction on1..N-1 by1/4. Removing its two always-passing
endpoints gives on2..N-2 at most `((N-1)/4-2)/(N-3)<1/4`. Thus r independent
uniform accepted witnesses have false acceptance strictly below4^-r, not merely
a non-strict bound. PrimalityCertainty.testRounds uses r=ceil(c/2), proving
strict error<2^-c for every positive Int c. At MAX,r=1073741824. The Long
countdown at845–852 does not wrap or silently run zero rounds.

## Sampling and generation conditioning

For b>=0, randomBits at1175–1180 uses m=ceil(b/8) bytes with Long addition;
m<=268435456 over all nonnegative Int b. Masking retains exactly b bits. Each
integer0..2^b-1 has2^(8m-b) preimages, so fresh fair bytes give exact uniform
integers. Witness rejection at849 retains exactly2..N-2 and preserves conditional
uniformity. On that large-N branch its accepted set has positive constant
fraction, so independent rejection sampling terminates almost surely, without
a fixed callback-count/worst-case-time guarantee.

For b>=3, generation at1165–1173 forces the high/low bits, giving each odd b-bit
candidate four preimages among b-bit strings. Let p be the prime fraction and q
the probability that a single draw is composite and accepted. True primes pass.
If q=0, eventual returned-composite probability is0. If q>0, it is q/(p+q)<q/p.
The strict inequality must not be claimed at q=0, though the desired final
positive-certainty error bound still holds strictly there.

For b>=6 set x=2^b. [Rosser–Schoenfeld Corollary1,(3.5)/(3.6)](https://denisevellachemla.eu/Rosser-Schoenfeld-1962.pdf#page=6)
gives pi(x)>x/ln(x) for x>=17 and pi(x/2)<1.25506*(x/2)/ln(x/2) for x/2>1.
Since ln(x)/ln(x/2)<=6/5, subtraction gives more than0.246964*x/ln(x), hence
more than x/(5ln(x)), primes in the b-bit interval. Its odd candidate count is
x/4, so p>4/(5b ln2)>1/b. For b=3,4,5 the exact fractions1,1/2,5/8 give the
same strict lower bound. At b=2 both outputs2/3 are prime.

Put ell=ceil(log2 b)=bitLength(b-1). PrimalityCertainty.kt8–15 chooses
r=ceil((c+ell)/2) for c>0. Consequently `q/p<b*2^(-2r)<=2^-c` when q>0;
the separated q=0 case is immediate. Long addition makes the generic positive
Int maximum round budget1073741839; the current b<=500000000 domain has ell<=29
and maximum1073741838. For c<=0 generation uses zero test rounds and the first
odd b-bit candidate. For c=0 its prime probability p>0 satisfies the trivial
strict lower bound0 under fair candidates; negative c demands no positive
accuracy bound. The b=2 branch remains prime independently of certainty.

With fresh uniform candidates, p>0 and true primes always accepted imply almost
sure termination; the outer-draw tail is at most(1-p)^m. This is acceptance
conditioning for generation, not an inference from a single candidate's bound.

## Successor ordering, union bound and supported range

At854–865 negative starts reject;0/1 return2. Otherwise add(ONE).setBit(0) selects
the first greater odd candidate, and adding2 visits every subsequent odd value.
Every true prime passes, so no intervening prime is skipped. A false-positive
composite may be returned earlier; its probability is bounded separately.

For one-based candidate index i let k=bitLength(i). There are2^(k-1) such indices.
successorRounds assigns r=50+k, so the union bound over even an infinite search
is at most `sum(k>=1)2^(k-1)*2^(-100-2k)=2^-101<2^-100`. It requires no
independence between candidate-error events, but does require the fresh-witness
conditional bound at each reached candidate. Early stopping/range rejection only
removes events. The counter is BigInteger; n=n0+2(i-1),n0>=3 gives i<=(n-1)/2.
It cannot exceed the supported candidate magnitude. Budgets use Long, including
generic k=MAX giving2147483697 rounds; under the current K-bit cap k<=K-1,
giving at most500000049. The inner squaring range/countdown also cannot wrap.

K=500000000 is solely the generation/search envelope, checked at1157/1167 and
857/861. Every prime value through2^K has at most K bits because2^K is composite.
Crossing to a K+1-bit candidate can correctly reject after forming that still
ordinary-supported value; it skips no remaining K-bit prime. This policy does
not cap arithmetic, random integer construction or isProbablePrime at K.

## Actual randomness, callbacks and finite evidence

Uniform marginals or distinct RNG objects alone do not establish the premise:
candidate blocks must remain uniform conditional on prior history, and witness
blocks conditional also on the current candidate. Caller
candidate sampling and Random.Default witness sampling may share one ideal
independent stream; fresh disjoint blocks suffice. Actual finite-state platform
PRNGs, seeded reproducibility and arbitrary caller RNGs are not thereby proved
to meet this mathematical premise. This statistical obligation is separate from
performance and capacity, not silently discharged by them. Repeating/biased
sources can defeat candidate distribution, termination or error analysis.

PlatformRandom is java.util.Random on JVM. The common adapter forwards the same
allocated byte array and catches no callback exception; its four-byte nextBits
mapping is uniform under fair bytes. BitCount0 returns0; neither zero-bit draw
count nor exact seed advancement is a required sequence. Constructor validation
and ordinary-null NN boundaries retain the separate549 proof. Correct numerical
leaves include the reached primality/modular methods; false abs/parity/metadata,
mutable buffers, replacement values and internal throws remain separately
classified diagnostics, never an automatic waiver of required numeric fields
or explicit caller result/exception/argument identity.

Active finite controls include [PrimalityCertaintyTest](../src/commonTest/kotlin/io/heapy/krogu/math/PrimalityCertaintyTest.kt),
[RandomIntegerDistributionTest](../src/commonTest/kotlin/io/heapy/krogu/math/RandomIntegerDistributionTest.kt),
[PrimeOracleTest](../src/commonTest/kotlin/io/heapy/krogu/math/PrimeOracleTest.kt),
[PrimeSearchRange559Test](../src/commonTest/kotlin/io/heapy/krogu/math/PrimeSearchRange559Test.kt),
PrimePrefixOracleTest, ConstructorRandom549Test and JVM PrimalityConformanceTest.
They retain exact budget endpoints,16 finite index groups, bounded trial-division
decisions/successors, exhaustive small byte multiplicities, callback diagnostics
and finite conclusive-threshold liveness. None proves ideal platform randomness
or practical billion-round completion above the deterministic cutoff.

The [historical583 local report](decimal-residual-583-verification.md), SHA-256
`43809d15ca7ee37613a613c934be9fec47b8db88288027af1a26d327463824fc`, records
the baseline `d6d53174…`236-source integration's1,584 exact method executions across
four local targets, not execution of current `12fd0a14…`, whose new local result
is recorded separately above. This is existing finite
evidence, not a new547 campaign or an all-input
probability experiment. All old assertions/fixtures remain active. Conditional
budget refinement does not close actual-PRNG guarantees,546/548 kernels/capacity,
hostile callback policy, other targets, matching hosted/public gates or JCK.
