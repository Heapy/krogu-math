# Task530 selected-output SOURCE milestone

Scope: only files beneath `/private/tmp/krogu-530-selection-source-v2` were
created or edited. Neither the root repository nor the running task530 tree
was edited. No Git/tracker/mutex operation was performed. No build, compiler,
Kotlin script, helper, symbolic evaluation, test, JAR, probe, benchmark,
harness or fixture was executed. All new code and tests are UNCOMPILED/UNRUN.

The first task530 runtime evidence belongs to source `590d9391…`, not this
selection proposal; no previous PASS is relabelled as validating this source.

## Files and SHA-256

| File relative to this directory | SHA-256 |
| --- | --- |
| original/src/commonMain/kotlin/io/heapy/krogu/math/BigInteger.kt | 590d93919ce590b098708a5f567730fca2374cd8bf5d027f7164974203c99607 |
| src/commonMain/kotlin/io/heapy/krogu/math/BigInteger.kt | d0e757ebd5955604d5fb66d8dda13b1c15b2ecb300a37a5fbcb1fcfaeb65c16f |
| src/commonTest/kotlin/io/heapy/krogu/math/Division530Test.kt (unchanged) | 8bc7853264804970a7bfcada861294022d9f2b06a05b7a994e1689581f6be2a0 |
| src/jvmTest/kotlin/io/heapy/krogu/math/Division530JvmTest.kt (unchanged) | 4642ffbd0ebf050eadfe518050b4ebcb7ed32dd1401da52418a40a129a84ca57 |
| src/commonTest/kotlin/io/heapy/krogu/math/Division530SelectionTest.kt (two new methods) | a763624437197218611aaf32eaf440f98635b339b4a5414dd3ea809092f64e38 |
| src/jvmTest/kotlin/io/heapy/krogu/math/Division530SelectionJvmTest.kt (one new method) | 8313d3111babc31a2022fb501ad060d73e12cd46c8a27363cabf2eb47ef8f209 |
| docs/division530-selection.patch | 144df20e1fd28bfaca0a33f90554b7db2e355a7b39d1221a678e216f897a1d19 |
| docs/division530-selection-proof.md | c5c69c26619f21d28084f4005dc81c5930b7b19d8cb25c24be25566a73d57aa7 |
| original.sha256 | 0181fa4ab7ebf4aa6cbfc97e3191e6f5c813e0c9878879494bd234e35316dd85 |

`original/` and `original.sha256` were made read-only before candidate editing.
`sha256sum -c original.sha256` reported three OK files. Direct `cmp` of both
retained test sources against their originals returned zero. These are
read-only file checks, not numerical execution.

## Change and boundaries

One private inline selector and three fixed wrappers choose quotient only,
remainder only, or both. The numerical kernel remains a single source loop.
Unrequested output buffers, normalization/export objects and the pair carrier
are omitted; required remainder scratch and estimation still run. Public
divide/remainder, gcd and sqrt select the outputs they already consumed.
Public divideAndRemainder, modInverse, decimalBinaryPrefix and virtual
toString/public mod call boundaries retain their required behavior.

The proof inventories every private call, signs, zero/smaller/scalar paths,
subclass callback boundaries, aliases/identity, all-path single selector
invocation, and conservative limb-buffer bounds. Existing tests are untouched.
New tests address the newly rewired sqrt consumer using independent algebraic
boundaries, stored subclass callbacks, and pinned JDK public API comparisons.

No unresolved source design choice remains. Independent review may still
identify defects. Required future gates remain open: parent integration into
a frozen source snapshot; compilation and complete target tests; matching
source/JAR provenance; public workload checks; allocation, runtime and maximum
capacity evidence. No measured performance improvement, complete division
acceptance or task completion is claimed.
