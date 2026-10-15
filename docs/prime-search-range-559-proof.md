# Own probable-prime generation/search range (559)

**Historical source milestone: UNCOMPILED / UNRUN at preparation.** That milestone changed only the
two private generation overloads and successor-search seam in BigInteger,
one small internal size predicate, one new common test file and this proof.
During source preparation no compiler, script, numerical helper, certificate, test, build, probe,
benchmark, Git command or tracker operation was executed. Other workers'
parser changes, documents, mapper and assertions are preserved.

## Observed failure and the chosen public policy

The original hosted large attempt38762806007 retained two port timeouts.
After the separately reviewed parser repair, attempt38767320539 at
`88ef623506607944aad9e17dfc713ef8495d95a4` instead records11 PASS,1 FAIL and
128 SKIP. StringConstructorOverflow passes in3808ms. SymmetricRangeTests exits1
in4683ms after its `Testing overflow in BitSieve.sieveSingle` marker: its
`BigInteger(671088639, 0, RANDOM)` returns a671088639-bit value, while that public
TEST requires ArithmeticException. The subsequent successor assertion and later
sections are not established by this failed invocation. These historical results
are not rerun or relabelled here. Evidence remains under
`/private/tmp/krogu-88ef-large-ci-evidence-v1`.

The [Java25 class contract](https://docs.oracle.com/en/java/javase/25/docs/api/java.base/java/math/BigInteger.html)
permits a probable-prime range smaller than the full integer range, with a
minimum envelope through2^500000000. Its certainty-bearing constructor and
probablePrime factory permit ArithmeticException for excessive bit length;
nextProbablePrime permits ArithmeticException when its source is too large.
This is permission for an implementation limit, not a mandated JDK cutoff.
The public TEST's exact reference-implementation cutoff is not copied.

Krogu chooses **K=500000000 magnitude bits for prime generation and successor
search**. All prime values p with1<=p<=2^K remain inside the chosen envelope:
2^K is even and greater than two, hence composite, so every prime in that
inclusive interval is strictly below2^K and has at mostK bits. This is a
generation/search policy only. Arbitrary integer arithmetic, random-bit
construction and isProbablePrime retain their existing supported domains.
In particular isProbablePrime with nonpositive certainty still returns true
without this new range guard.

This policy does not guarantee available memory, successful maximum-size
allocation, termination for hostile caller RNGs, elapsed time or statistical
quality of a caller's source. Required range and probability obligations547/548
remain separate. No public TEST or harness adaptation is introduced.

## Exact source binding

The accepted parser baseline BigInteger hash is
`8db4e0f6fef21ab6d0e467dd5465bae69ccc52dd47b2162d121a3db1ec1c6615`.
Prepared sources are:

| Own source | SHA-256 |
| --- | --- |
| `src/commonMain/kotlin/io/heapy/krogu/math/BigInteger.kt` | `5a4aa675afc9cdfdff6c8761dba45c9636de5533e70fabbd6ea808dc6fa08781` |
| `src/commonMain/kotlin/io/heapy/krogu/math/PrimeSearchLimits.kt` | `66e7d92b5d6091067ee02679534060fc9d8559328c7fafc45805b3d7493c3a85` |
| `src/commonTest/kotlin/io/heapy/krogu/math/PrimeSearchRange559Test.kt` | `e38aab7ebcf00eace3323b33f75d4fe36a5740e2d35a8728feef8331d019f777` |

Read-only text-region hashes match before/after against the accepted parser
source freeze at `/private/tmp/krogu-parser559-validation-v1/evidence/source-freeze/BigInteger.kt`:

| Unchanged region, with boundary declarations excluded where indicated | SHA-256 |
| --- | --- |
| File start up to, excluding nextProbablePrime | `68df32549fe83355b35db9d50b3f3aba337529c27a6e2a5055a923b46727bbd6` |
| compareTo through, excluding PlatformRandom generatePrime | `65572aced745143a67ca58a8aaa675d9c7ed72eb57f6ce02b00359c6c3561c84` |
| PlatformRandom randomBits through, excluding common generatePrime | `982571b0fc99253b5fa8317d80a12b94f7ce8630a64b02b752c635afd7015dbf` |
| Common randomBits through end of file | `47bd879f9bb77a7ba5831e852ecad1858a1a6cba4b98796c15c1a11aa70c9e18` |

Thus the parser repair, conversions, metadata, integer arithmetic, division,
primality predicate/witness kernels, byte paths and serialization remain intact.
No public symbol, overload or interface changes. The new object is internal,
contains only a constant and a primitive comparison, and adds no per-instance
field, numerical allocation or mutable state.

## Generation validation and certainty

Both generatePrime overloads retain their original `bitLength<2` check first.
They then call PrimeSearchLimits.checkBitLength, which throws ArithmeticException
exactly when bitLength>K. For accepted generation lengths2<=b<=K, all previous
branches, random draws, candidate construction, certainty budgets, witness
selection and result decisions remain unchanged. The PlatformRandom overload
checks the upper limit before constructing its common adapter; the common
overload also checks directly. No RNG call is added.

At b=K, every b-bit candidate is below2^K and remains eligible. At b>K, the
smallest possible magnitude is at least2^K; there is no prime in the intersection
of that size class with the chosen envelope, since its only possible endpoint
is the composite2^K itself. Rejection therefore excludes no required prime
value. This rejection also applies to certainty0 and negative certainties:
the public excessive-size exception is independent of the requested probability
threshold. A nonpositive threshold does not require a specific composite result
or make every possible bit length supported. The existing low-certainty return
path remains unchanged for every supported generation length.

The policy check performs no count multiplication, limb sizing, shift or index
calculation: K and every accepted Int input compare directly without overflow.
It does not construct2^K to classify a request.

## Successor search and crossing the boundary

The original negative-source rejection, including its diagnostic formatting,
is first and unchanged. Zero and one still return TWO before the new bound.
For other inputs, private magnitudeBits checks the stored receiver magnitude;
no public bitLength, signum or other metadata callback selects the limit.
Sources at least2^K are outside the chosen search envelope: no greater prime
could remain at most2^K. Sources below2^K remain eligible.

The existing `add(ONE).setBit(0)` creates the first odd candidate. At the top of
every iteration its private stored bit count is checked **before** the existing
passesPrimeTest call or successor-round calculation. Failure therefore cannot
test or return an oversized candidate. A rejected candidate withK+1 or more
bits is at least2^K. The value2^K is even, while ordinary candidates are odd;
the first ordinary rejected candidate is consequently above that endpoint.
Every remaining greater prime would leave the chosen envelope. A source near
the boundary is not rejected merely for being close: every intervening odd
candidate having at mostK bits is still tested in its original order.

On an accepted candidate the previous primality call and one-based candidate
index are unchanged. A failed test still increments n by TWO and candidateIndex
by ONE. Returning immediately from the successful iteration yields the same
candidate as the old loop's final return. Thus the normal search does not skip
a prime, change its probability budget or add a public callback. Its existing
union-bound probability argument still applies to the retained prefix of
candidates. The new range check may end a search whose successor leaves the
chosen envelope; no unbounded search beyond it is promised.

This numerical argument assumes ordinary stored values and exact/truthful
arithmetic callbacks. Direct caller overrides still receive their explicit
calls. Poisoned replacement arithmetic/abs callbacks and broader inherited
dispatch classification remain task541; this policy does not newly classify
all such observations or promise unchanged internal behavior outside its range.

## Prepared independent controls and remaining gates

PrimeSearchRange559Test prepared three common methods, initially **UNCOMPILED/UNRUN**:

* Literal symbolic checks permit0/1/2, K-1 andK at the upper-bound helper and
  require ArithmeticException atK+1, the actual671088639-bit witness and
  Int.MAX_VALUE. Accepting0/1 in this upper-bound-only helper does not bypass
  the separate public generation minimum2.
* Actual public Kotlin Random constructor-like and probablePrime calls reject
  those oversized lengths, across Int.MIN_VALUE/0/1/100 certainty where relevant.
  Their source routes reject before any requested giant numerical allocation.
  No RNG draw count, state, message or exact host random result is asserted.
* Independent small successor literals cover0/1, prime/composite starts,
  negative rejection and a truthful direct-super bitLength override. The
  numerical19 result is required without a callback-count or noncall assertion.
  Source inspection, rather than a poisoned-getter test, establishes that the
  new guard reads private stored metadata and adds no public call. No synthetic
  small production maximum or giant String/magnitude is introduced.

Existing ConstructorRandom549Test and its successful supported-length controls,
probability tests, actual JVM bridge checks and public prime tests remain
unchanged. Symbolic bounds and source reasoning do not execute a giant successor
boundary. Root owns independent source review, fresh compilation/platform gates,
exact artifact binding and an original full SymmetricRangeTests rerun. This
source milestone closes neither559,547 nor548 and assigns no future PASS count.

## Actual focused four-target validation (root2301)

A fresh archive of `88ef623506607944aad9e17dfc713ef8495d95a4`, overlaid with
exactly the three frozen source/test files above, was built under
`/private/tmp/krogu-prime559-validation-v1/root`. Its 219-source manifest is
`161f648c8911fab126389e498d34b40f931e95d2aae0b1e1956a84bed26b2a91`.
All 219 files matched both the isolated tree and the publication workspace
after execution. No production or test change was made during the campaign.

One serial Gradle invocation ran `jvmTest`, `jsNodeTest`, `wasmJsNodeTest` and
`macosArm64Test`, with the same six class filters on every target:
PrimeSearchRange559Test, ConstructorRandom549Test, PrimeOracleTest,
PrimePrefixOracleTest, PrimalityCertaintyTest and RandomIntegerDistributionTest.
It also built `jvmJar`. The existing Gradle9.8.1/Kotlin2.4.21/Liberica25.0.4
toolchain, 4GiB in-process compiler, one worker and original limits were used;
only the already provisioned Kotlin tooling home was reused.

Exec session42246 exited0, `BUILD SUCCESSFUL in 4m55s`, 49 executed tasks.
Actual XML contains **6 suites and23 methods per target**, 92 method executions
in total, including12 executions of the three new methods. Exact source method
inventories match all four target results: no failures, errors, skips, omitted
methods or duplicate classes/methods. The first in-memory audit failed to strip
the JVM suite `[jvm]` suffix; the final audit normalizes it consistently. This
audit correction did not rerun tests or alter an assertion.

| Retained artifact | SHA-256 |
| --- | --- |
| `evidence/four-target.log` | `62805a50369b610b96178dcbbe5bf67d07d1c08542e477e5d6983f101e8e9997` |
| `evidence/xml-audit.json` | `a91de16c856ce0c612f4620a40502854f5c638669435a770eb4694107c86ca37` |
| Fresh `krogu-math-jvm-0.1.0-SNAPSHOT.jar` | `de97f2b5b68620c9b94c433999222421d53b742de82c44f9e3c2b5932a7d299e` |

All owned compiler/test/build invocations are terminal; root explicitly released
the sole local numerical window after auditing. No giant boundary magnitude or
public large harness ran locally. Full supported-platform CI and the original
hosted SymmetricRangeTests remain pending for this source. The historical
11PASS/1FAIL large report above is unchanged; these focused passes do not close
559,547 or548 or establish a complete public-suite pass.
