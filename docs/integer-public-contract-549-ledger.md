# Adopted BigInteger public-entry ledger (549)

The following historical source/spec ledger was independently read and adopted
for published numerical source247 at commit47447f2. Its original private bytes
remain at `/private/tmp/krogu-549-biginteger-ledger-v4XUt3NH/LEDGER.md`, SHA256
`9ee42fe7d4cd40ce22bad09e7c07339a26effcc205cb1bfb035210602960c7e3`.
Root verified its complete14-entry evidence index. Adoption changes no code,
fixture or assertion and supplies no new runtime evidence. Subsequent private
comparison normalization is separate from this published-source snapshot.
The named conditional leaves remain explicit; this is not whole549 closure.

---

# BigInteger public-member/error ledger — task 549

Source/spec review, 2026-10-16. No source, fixture, test, compiler, script, numerical helper, benchmark, build, Git or tracker execution/change was made. Only this private evidence directory was written. This accounts for BigInteger nonconstructor entries; it does not close task 549 or the project compatibility goal.

## Binding and inventory

Current own source is `src/commonMain/kotlin/io/heapy/krogu/math/BigInteger.kt`, SHA256 `12fd0a14164bfd81b004c66673ae37ea40719bf3552f171ba20b434a296d8602`. All BigInteger line numbers below refer to this exact file. `source-proof-evidence-pins.sha256` binds 28 existing source/proof/test files; its SHA256 is `834734ed8d60192fdc3ca883ea1e34192c7c306c791b4187f70d02652dae4abb`. Explicit `local:549` task response is retained in `task-549.json`.

The Java 25 public API has **51 declared nonconstructor methods**, **2 inherited Number methods** (`byteValue`, `shortValue`), and **4 declared constants**: 57 entries below. The declared-method anchor inventory and common declarations are retained separately. Kotlin has 61 public nonconstructor callables plus 4 constants: the same 53 Java method entries, one additional Kotlin-Random `probablePrime` overload, and seven member operators. Internal decimal bridges, constructor-shaped random factories and package extension operators are not extra Java BigInteger entries. Inherited Java Object ABI-only operations are excluded; overridden `equals`, `hashCode`, `toString` are included.

Normative sources are the public [Java 25 BigInteger API](https://docs.oracle.com/en/java/javase/25/docs/api/java.base/java/math/BigInteger.html) and [Number API](https://docs.oracle.com/en/java/javase/25/docs/api/java.base/java/lang/Number.html). Their public HTML responses are retained; no linked implementation source or foreign numerical body/bytecode was read. BigInteger HTML SHA256 is `5e0cf08f22e03ebb6b123e0660b5fce3becd31a9f76d73d0ec833fb615169bb9`. The public [Kotlin Int.compareTo API](https://kotlinlang.org/api/core/kotlin-stdlib/kotlin/-int/compare-to.html) is retained for the one named portability premise below.

## Shared premises and evidence keys

* **N**: object operands and Random arguments are non-null under the adopted migration policy; an otherwise valid null invocation must reject with NPE at the boundary. `equals(Any?)` remains nullable and returns false for null. Nullability/override edits during Kotlin migration are accepted. Accepted 573–578 boundary work and the existing constructor ledger cover this policy. VM/compiler-generated NPE wording, competing-invalid precedence and incidental callback traces are not promoted into new required contracts here. Library-defined errors and explicit caller-thrown exceptions are not blanket-waived.
* **C/T**: canonical immutable stored sign/magnitude, truthful mathematical substitution, and completing reached virtual leaves. A directly invoked override/super method must preserve its caller-visible result/exception identity where required. Correct leaf overrides compose transitively; internal callback count/order/which equal result instance are diagnostic unless independently specified. False/changing metadata, poisoned replacements, throwing callbacks and current-own-format restored subtypes retain their existing evidence and separate classification; this ledger does not waive them as a family.
* **R548**: supported final magnitude and feasible resource/allocation premises remain task 548. `MagnitudeLimits` enforces at most L = Int.MAX_VALUE stored magnitude bits, matching the documented minimum BigInteger range; unsupported final magnitudes reject with AE. A proof of arithmetic value alone is not a proof that every intermediate/allocation is feasible. No smaller arithmetic cap or universal OOM translation is introduced.
* **A544**: `integer-arithmetic-544-proof.md`, wrapper addendum, existing division-530 proof/selection argument, and relevant `NUMERICAL_ARGUMENTS.md` sections. Covers signed arithmetic, Euclid, powering and integer-root refinement under their stated C/T/R premises. Existing `IntegerArithmetic544ContractTest`, `IntegerArithmeticOracleTest`, `JdkOracleTest` provide finite assertions, not all-input proofs.
* **B545**: `integer-bits-545-proof.md` and endpoint addendum: infinite sign-extended two's-complement semantics, shifts, metadata, negative-index and MAX/MIN endpoints. Existing `IntegerBits545ContractTest` and bit oracle fixtures are finite evidence.
* **M546**: `modular-substitution-546-proof-binding.md`: ordinary stored modular arithmetic, odd/even/power-of-two/CRT composition, negative powers, alias isolation and truthful transitive leaves. Its numbered source references explicitly use baseline BI d6d53174; current BI 12fd adds an 18-line decimal-sum bridge after the same original region. Reuse here is by symbol/body equality, not by pretending those baseline lines are current lines.
* **P547**: `primality-547-proof-binding.md`: strict certainty-round endpoints, conclusive small range, conditional uniform-witness rejection, generation acceptance conditioning/density and successor-search union/no-skip. Platform or caller PRNG uniformity/independence is an explicit mathematical premise, not established by finite output samples and not merely a performance issue. The same baseline-line qualification as M546 applies.
* **F543/F557**: `floating-conversion-543-proof-binding.md` and `NUMERICAL_ARGUMENTS.md` bind nearest-even integer conversion and reached virtual leaves to current source. Task 557's erased/generic JavaScript Number lowering barrier remains external/open; concrete finite tests or other green jobs do not discharge it.
* **V/API**: `ValueIdentityContractTest`, byte-isolation tests and Java primitive/exact-conversion subclass tests cover named finite behaviors. `ApiSurfaceTest.jdkPublicSurfaceIsPresent` checks reflected JVM shape only. Java source compatibility beyond TCK adapters and legacy Java serialization interoperability are not requirements.

In the tables, “none” means no isolated mathematical invalid-input condition beyond shared N/R/C/T premises; it is not a guarantee of completing arbitrary physical allocation. AE means ArithmeticException.

## Constants and factories — 6 entries

| Java entry → common mapping | Required result / isolated invalid condition | Current guard or construction; evidence / remaining leaf |
|---|---|---|
| `ZERO` → `ZERO` | Integer 0; none | 1147: sign 0, empty limbs. C, V; no cache/identity guarantee inferred. |
| `ONE` → `ONE` | Integer 1; none | 1148: sign 1, limb 1. C, V. |
| `TWO` → `TWO` | Integer 2; none | 1149: sign 1, limb 2. C, V. |
| `TEN` → `TEN` | Integer 10; none | 1150: sign 1, limb 10. C, V. |
| `valueOf(long)` → companion `valueOf(Long)` | Exact signed Long value, including MIN; none | 1160–1166: small-value lookup or unsigned magnitude `0uL - value.toULong()`, at most 3 limbs, normalized. C, V/API; identity of cached results is not a requirement. |
| `probablePrime(int, Random)` → companion `probablePrime(Int, PlatformRandom)` | Positive requested-bit-length probable prime, composite probability at most 2^-100 under P547. `bitLength < 2` or supported generation limit exceeded → AE | 1170 → 1173–1177: lower guard, `PrimeSearchLimits.checkBitLength`, adapter, common generation 1183–1191 with certainty 100. JVM PlatformRandom is java.util.Random. Generation/search cap is 500,000,000 bits; not a general arithmetic/predicate cap. Existing accepted platform-Random validation proof accounts for all invalid Int partitions; positive distribution/primality remain P547/R548. Extra 1169 Kotlin Random overload has the same result/guards. |

## Arithmetic and roots — 14 entries

| Java entry → common method | Required result / isolated invalid condition | Current path; evidence / remaining leaf |
|---|---|---|
| `add(BigInteger)` → `add` | a+b; none | 43–53: zero identities, sign cases, magnitude add/subtract, normalize. A544/C/T/R548. |
| `subtract(BigInteger)` → `subtract` | a−b; none | 54–64: zero/sign cases; zero receiver uses virtual `value.negate()`. A544/C/T/R548. |
| `multiply(BigInteger)` → `multiply` | ab; none | 65–80: zero, Long bit lower-bound guard, 30-bit limb product/carry, normalize. A544/R548. |
| `parallelMultiply(BigInteger)` → `parallelMultiply` | Same exact product as multiply; none | 81 delegates virtual multiply. API permits parallel implementation; parallel execution or a speedup is not required. A544/T/R548. |
| `divide(BigInteger)` → `divide` | q = trunc(a/b); b=0 → AE | 189 → quotientStored → selector 205–212; stored zero guard at 212, magnitude division. A544/530/R548. |
| `divideAndRemainder(BigInteger)` → same | Pair [q,r], a=qb+r; b=0 → AE | 192 → divideStored → same selector with both outputs requested. Remainder has a's sign or zero and abs(r)<abs(b). A544/530/V/R548. |
| `remainder(BigInteger)` → `remainder` | r=a−trunc(a/b)b; b=0 → AE | 190 → remainderStored → same zero guard/division selector; omitted quotient need not be allocated. A544/530/R548. |
| `pow(int)` → `pow(Int)` | a^e; e=0 gives 1 including 0^0; e<0 → AE | 300–358: guard 301, zero exponent 302, special values, factor-of-two extraction and repeated squaring with supported-range checks. A544/T/R548. |
| `sqrt()` → `sqrt` | floor(sqrt(a)); a<0 → AE | 657 → sqrtStored 659–671; stored negative guard 660, zero, descending integer Newton route. A544/R548. |
| `sqrtAndRemainder()` → same | [s,a−s²], s=floor(sqrt(a)); a<0 → AE | 672–676 calls stored root, materializes root² (≤a), subtracts. A544/V/R548. |
| `gcd(BigInteger)` → `gcd` | Nonnegative gcd(abs(a),abs(b)); gcd(0,0)=0; none | 359–365: zero exits via abs, fresh positive stored wrappers and Euclidean stored remainder. A544/T/R548. |
| `abs()` → `abs` | abs(a); none | 41: negative calls virtual negate; otherwise this. A544/T/V. |
| `negate()` → `negate` | −a; none | 42: zero or new opposite stored sign sharing immutable limbs. A544/C/V. |
| `signum()` → `signum` | Exactly −1,0,1 according to a; none | 40 returns canonical stored sign. C/V. Unlike compareTo below, no sign-only primitive result is forwarded. |

## Modular and primality — 5 entries

| Java entry → common method | Required result / isolated invalid condition | Current path; evidence / remaining leaf |
|---|---|---|
| `mod(BigInteger)` → `mod` | Least nonnegative a mod m; m≤0 → AE | 367–370: stored m sign guard 368; virtual remainder, add m when negative. M546/T/R548. |
| `modPow(BigInteger,BigInteger)` → `modPow` | Least nonnegative residue of a^e; m≤0 → AE; e<0 and a not relatively prime to m → AE | 372–482 and private odd/even/CRT helpers: modulus guard 373, special values, reduction, stored odd path/power-of-two/CRT, inverse for negative exponent. e=0 valid; m=1 gives 0. M546/T/R548; no raw-product assumption silently replaces bounded modular fallback. |
| `modInverse(BigInteger)` → `modInverse` | Least nonnegative inverse; m≤0 or no inverse → AE | 616–638: sign guard 617, m=1 special case, reduced-zero rejection 621, fresh-wrapper extended Euclid, gcd check 636, final mod. M546/T/R548. |
| `isProbablePrime(int)` → `isProbablePrime` | True means probability exceeding 1−2^-c under stated random premises; false means definitely composite. c≤0 returns true. API permits AE for excessively large values | 825–870: c≤0 immediate true; `testRounds`, small/conclusive cases and rejection-sampled witnesses. No own PrimeSearchLimits cap is imposed on this predicate. Negative input magnitude handling, q=0 and strict probability inequalities remain exactly as analyzed in P547; no new sign rule or stronger platform-PRNG claim. P547/T/R548. |
| `nextProbablePrime()` → same | Smallest probable prime >a, skipping no actual prime; composite bound per API. a<0 or generation/search size limit → AE | 872–884: negative guard 873, 0/1→2, size check 875 and during increasing odd candidates; conditional test-round union. P547/T/R548. Exception interpolation can call toString; wording/compound callback traces remain separate diagnostics. |

## Bits and metadata — 14 entries

All Boolean operations use infinitely sign-extended two's complement. None introduces a finite-width unsigned interpretation.

| Java entry → common method | Required result / isolated invalid condition | Current path; evidence / remaining leaf |
|---|---|---|
| `shiftLeft(int)` → `shiftLeft` | a·2^n for n≥0; negative n reverses direction with arithmetic right shift; none | 678–695: zero/zero-distance exits, negative-distance reversal including MIN handling, Long bit bound for positive growth. B545/R548. |
| `shiftRight(int)` → `shiftRight` | floor(a/2^n) for n≥0; negative n reverses direction; none | 696–717: saturated sign tail, discarded-bit correction for negative values, reverse-shift MIN overflow only when required magnitude unsupported. B545/R548. |
| `and(BigInteger)` → infix `and` | Infinite signed a AND b; none | 782 → bitwise 788–803, finite sufficient sign-extended representation then decode. B545/T/R548. |
| `or(BigInteger)` → infix `or` | a OR b; none | 783 → same bitwise route. B545/T/R548. |
| `xor(BigInteger)` → infix `xor` | a XOR b; none | 784 → same route. B545/T/R548. |
| `not()` → `not` | ~a = −a−1; none | 776–780: sufficient finite two's-complement width, invert, decode. B545/T/R548. |
| `andNot(BigInteger)` → `andNot` | a AND ~b; none | 785 → bitwise with second word inverted. B545/T/R548. |
| `testBit(int)` → `testBit` | Whether bit n is set in infinite representation; n<0 → AE | 753 → 739–752: negative-index guard 740 before sign-tail query; MAX query allocates no giant mask. B545. |
| `setBit(int)` → `setBit` | a OR 2^n; n<0 → AE | 754 → changeBit 757–773, guard 758; MAX sign-tail no-op is admitted, otherwise unsupported final result rejects. B545/T/R548. |
| `clearBit(int)` → `clearBit` | a AND ~2^n; n<0 → AE | 755 → same route/guard; MAX no-op case distinguished from unsupported changed result. B545/T/R548. |
| `flipBit(int)` → `flipBit` | a XOR 2^n; n<0 → AE | 756 → same route/guard. B545/T/R548. |
| `getLowestSetBit()` → same | Least set bit of nonzero a; −1 for zero; none | 729 → lowestSetBitValue 730–733, first nonzero stored word. B545. |
| `bitLength()` → `bitLength` | Bits excluding sign; 0 for zero; none | 719–724: stored magnitude bits minus one for negative exact power of two. B545. |
| `bitCount()` → `bitCount` | Number of bits differing from sign bit; none | 725–728: positive popcount; negative popcount(abs(a))+lowestSetBit(a)−1. B545/T. |

## Ordering, identity and output — 9 entries

| Java entry → common method | Required result / isolated invalid condition | Current path; evidence / remaining leaf |
|---|---|---|
| `compareTo(BigInteger)` → override `compareTo` | **Exactly −1,0,1**, according to order; none | 886–889: differing signs use Int.compareTo; same signs multiply compareMagnitude by sign; helper 1298–1301 forwards Int.compareTo for length/highest differing limb. A544 supplies the comparison argument under its primitive-comparison premises. **CMP-IMAGE remains a named return-image/negative-sign-reversal portability premise**, detailed below. |
| `equals(Object)` → override `equals(Any?)` | True iff other own BigInteger has same numerical value; null/unrelated object false | 892 stored sign/content equality. Canonical representation proves equality; V. No cross-equality with host java.math.BigInteger is required by package migration. |
| `min(BigInteger)` → `min` | Numerically smaller argument; either argument allowed on tie; none | 890 uses compareTo<0. Needs correct order rather than the exact unit image; A544/T/V/N and the sign-reversal qualification below. |
| `max(BigInteger)` → `max` | Numerically larger argument; either argument allowed on tie; none | 891 uses compareTo>0. Same premises; own tie returns operand, but tests retaining this behavior do not create a universal tie-identity requirement. |
| `hashCode()` → override `hashCode` | Equal values have equal stable hashes; none | 893–911 folds the canonical stored value into Int hash. C/V. Exact host-JDK hash recurrence is retained observational evidence, not a distinct public numerical formula. |
| `toString(int)` → `toString(Int)` | Canonical signed lowercase radix representation; invalid radix uses decimal, **not** NFE | 914–932: radix fallback, virtual abs; exact-class stored bit grouping for 2/4/8/16/32, otherwise repeated quotient/remainder. Capacity guard in 934–960. B545/A544/T/R548; `PowerOfTwoRadix559Test` and radix proof. |
| `toString()` → override `toString` | Canonical decimal representation; none | 913 virtual `toString(10)`. Same format premises and caller override distinction. |
| `toByteArray()` → `toByteArray` | Fresh minimal big-endian two's-complement byte array, sign bit included; none | 962–969: length=bitLength/8+1; negative abs−1 then complemented queried bits. B545/T/R548 plus byte composition below; IntegerByteOracleTest/JdkOracleTest and array isolation controls. |
| `intValue()` → Number override `toInt()` | Low 32 bits interpreted as signed Int; truncation/wrap, not AE | 971–976 JVM name annotation, low two 30-bit limbs then sign. A544/F543/F557/T. |

`intValue` appears here to keep the official inventory partition exact; all other primitive methods are below.

## Remaining primitive conversions — 9 entries

| Java entry → common mapping | Required result / isolated invalid condition | Current path; evidence / remaining leaf |
|---|---|---|
| `longValue()` → Number override `toLong()` | Low 64 bits interpreted as signed Long; no exactness AE | 977–982, low three limbs/sign. A544/F543/F557/T. |
| `floatValue()` → Number override `toFloat()` | Nearest IEEE binary32 value, ties even, signed infinity on overflow | 1097–1098 → floatingValue(24,128), then Float; avoids integer→Double→Float double-rounding. F543/F557/T/R548. |
| `doubleValue()` → Number override `toDouble()` | Nearest IEEE binary64 value, ties even, signed infinity on overflow | 1095–1096 → floatingValue(53,1024), guard/sticky 1100–1115. F543/F557/T/R548. |
| inherited `Number.byteValue()` → override `toByte()` | Low 8 bits signed; no exactness AE | 1091–1092 delegates virtual toInt then narrows. PrimitiveConversionSubclassTest preserves the meaningful leaf dispatch. F557/T. |
| inherited `Number.shortValue()` → override `toShort()` | Low 16 bits signed; no exactness AE | 1093–1094 delegates virtual toInt then narrows. Same evidence/premises. |
| `longValueExact()` → `longValueExact` | Exact Long iff in [−2^63,2^63−1], else AE | 1120–1123: stored magnitude>64 or virtual bitLength>63 rejects, then virtual toLong. C/B545/T; ExactConversionSubclassTest. |
| `intValueExact()` → `intValueExact` | Exact Int iff in [−2^31,2^31−1], else AE | 1116–1119: stored magnitude>32 or bitLength>31 rejects, then toInt. C/B545/T; same tests. |
| `shortValueExact()` → `shortValueExact` | Exact Short iff in [−2^15,2^15−1], else AE | 1124–1127: stored magnitude>32 or bitLength>15 rejects, then toShort. C/B545/T; same tests. |
| `byteValueExact()` → `byteValueExact` | Exact Byte iff in [−2^7,2^7−1], else AE | 1128–1131: stored magnitude>32 or bitLength>7 rejects, then toByte. C/B545/T; same tests. |

These partitions total 6+14+5+14+9+9 = **57 entries**, including four constants and the two inherited Number methods. The extra common member operators at 1133–1139 delegate to add/subtract/multiply/divide/remainder/negate or return the same value for unary plus; they add no new invalid mathematical category. Package `OperatorExtensions.kt` supplies additional Kotlin syntax separately from this class inventory.

## Small wrapper/composition arguments and exact remaining premise

**Exact primitive endpoints.** For positive x, bitLength is magnitude bit length; for negative x it is the bits needed excluding sign. Consequently bitLength≤w−1 admits exactly signed w-bit values, including −2^(w−1) but excluding +2^(w−1). The stored magnitude preguard prevents an oversized stored value from being admitted solely by a falsely small virtual bitLength. Correct virtual bitLength plus correct reached narrowing leaf proves all isolated exact-range partitions; false metadata/callback diagnostics are not collapsed into this proof. Finite exact-conversion tests are supporting evidence, not the all-Int argument.

**Byte export.** For truthful bitLength b, `floor(b/8)+1 = ceil((b+1)/8)` is the minimal signed byte count. For negative a, m=abs(a)−1 is nonnegative and complementing m's bytes yields a=−(m+1), including sign extension. For b≤L, the highest requested bit is at most `(floor(b/8))*8+7≤L` because L=2^31−1 ends in seven modulo eight; indices remain Int-valued. Existing 545 bit semantics and immutable representation therefore cover the mathematical export leaf. Allocation and truthful virtual abs/subtract/testBit/metadata remain explicit premises; byte-isolation assertions check finite alias cases.

**Floating conversions.** Nonzero integers cannot produce nonzero subnormal/underflow results. Stored guard/sticky extraction and tie-even carry handle finite/infinity transitions under F543. Small-value reached toLong behavior and JavaScript erased/generic dispatch are distinct premises; this ledger neither reopens the accepted conversion argument nor dismisses task 557.

**CMP-IMAGE (specific unresolved requirement).** The public Java BigInteger.compareTo API promises −1,0,1, whereas the public Kotlin Int.compareTo contract promises only negative, zero or positive. At 888/1299/1300 the current implementation forwards those primitive results, possibly multiplied by stored sign, without explicit normalization. A544's comparison argument does not by itself establish the exact three-element image on every backend. In addition, if relying only on that abstract sign-only contract, a negative primitive result equal to Int.MIN_VALUE must be excluded before multiplying by −1 at 889: its wrapped negation would remain negative. This is a dependency of the source-level ordering argument, not an observation about any current primitive implementation. Existing finite oracle/target evidence constrains actual exercised values and targets. `IntegerArithmetic544ContractTest.highestDifferentDigitDominatesAllOpposingLowDigits` explicitly coerces the result to [−1,1], and collection-order controls likewise test order; those assertions do not discharge the stronger exact-image premise. This is **not an observed failing target or a confirmed current runtime defect**. The smallest remaining source-accounting scope is to establish the admissible primitive return-image for supported backends, or separately authorize explicit normalization before sign reversal/public return. No stdlib/compiler implementation, new probe, test or fix was read/run/added here. Min/max need correct comparison sign and share that sign-reversal premise; direct internal comparisons using only <0, >0 or ==0 do not require an exact unit image.

## Evidence limits and disposition

The 28-file evidence manifest binds the existing proofs and named finite tests actually inspected. Additional existing proof references (division530, radix559 and accepted Random549 validation) are reused as documented dependencies, not new execution evidence. The historical 544/545 actual-verification report retains its own original production/source and 1392-method scope; it is not relabeled as current execution. Current BI 12fd is also part of the previously accepted source247 local 1687-method campaign and complete sqrt ledger, but those finite gates do not prove all BigInteger inputs, probability assumptions, allocation budgets or JavaScript erased Number behavior. No campaign was launched for this ledger.

No missing Java public nonconstructor entry was found. Isolated error categories are accounted for by the explicit source guards and accepted conditional leaves above. **CMP-IMAGE** is a precise remaining portability proof premise; **R548**, **P547 actual-random premises**, **F557**, and separately classified caller/subtype obligations retain their stated scope. Constructors, BigDecimal per-entry accounting, unresolved task526 diagnostic classification and matching hosted gates remain outside this finite ledger. There is no full549/JCK/all-platform/all-domain acceptance claim.
