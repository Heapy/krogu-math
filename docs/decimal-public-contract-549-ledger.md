# Adopted BigDecimal public-entry ledger (549)

The following historical source/spec ledger was independently read and adopted
for published numerical source247 at commit47447f2. Its original private bytes
remain at `/private/tmp/krogu-549-bigdecimal-ledger-DrKM1uVR/LEDGER.md`, SHA256
`7591d58fc5706aada0517277d0a7736353fe4d0e9cabfc2e546fb020a8de482a`.
Root verified its complete10-entry evidence index. Adoption changes no code,
fixture or assertion and supplies no new runtime evidence. The later focused
591 low-bit correction and592 fixed-scale normalization work are successors;
their [local source250 verification](decimal-range-590-592-verification.md)
must be read separately. This ledger's statement
that no successor execution is claimed remains a historical read-time limit,
not a replacement for later root test records. The named mathematical/subtype/
resource leaves remain explicit; this is not whole549 closure.

---

# Task 549 — BigDecimal nonconstructor public-member/error ledger

2026-10-16; finite SOURCE/spec accounting only. Current publication BigDecimal SHA256 is `5731c8df6ba366a51d907aec97cdef0874df21607638bdcf37b865e85426f741`; BigInteger is `12fd0a14164bfd81b004c66673ae37ea40719bf3552f171ba20b434a296d8602`. BigDecimal line numbers below refer to that exact source. No repository, source, fixture or task was edited; no numerical/compiler/script/helper/certificate/model/probe/test/build/benchmark/runtime command was run. Only this new private directory was written.

## Inventory and policies

The retained [Java 25 BigDecimal public API](https://docs.oracle.com/en/java/javase/25/docs/api/java.base/java/math/BigDecimal.html) has **61 declared nonconstructor methods, two inherited Number conversions, and 12 constants**: **75 entries** individually mapped below. `jdk25-declared-methods.txt` retains the61 method anchors; full HTML SHA256 is `28f5594a27f16aed03c149c2a87a4e68b95386744f36ca54353619a8cbf6f822`. Common Kotlin supplies the63 mapped methods plus seven member operators, or70 callables and12 constants. No Java public nonconstructor entry is missing. Constructors, MathContext/RoundingMode implementations and inherited Object-only Java ABI are excluded; overridden equals/hash/toString remain included. Java source compatibility is needed only inside TCK adapters.

Notation: x=a@s means a·10^-s; y=b@t; C=(precision p,mode m); D=BigDecimal; R=RoundingMode; L=Int.MAX_VALUE, MIN=Int.MIN_VALUE, MAX=Int.MAX_VALUE, H=646456993. AE=ArithmeticException, IAE=IllegalArgumentException, NFE=NumberFormatException. A blank mathematical invalid category does not promise physical allocation can never fail.

**N/C/T**: all object inputs except equals(Any?) are non-null under accepted Kotlin migration/override policy; otherwise-valid null rejects with NPE. Canonical immutable stored fields, truthful metadata/numerical leaves and completing reached operations are the shared proof premises. Explicit caller override/super/result/exception identities remain requirements. Internal callback order/count/which equal instance and false/changing metadata are retained diagnostics unless separately specified; no poisoned-subtype family is waived. Current-own-format restored coefficient subtypes remain reachable and covered by conditional reached-leaf reasoning. Legacy/cross-format Java serialization compatibility is excluded. Accepted NPE wording, compound-priority and constructor audits are not reopened.

**R548**: distinguish an unsupported required final coefficient/scale from an avoidable unsupported temporary. Source acceptance and finite tests do not prove all allocation/time budgets. BigInteger's L-bit range, exact/unlimited endpoint-cohort policy, physical String/array limits and 541/526 subtype/error classification remain explicit. Scale overflow permission does not excuse arbitrary failure when final primitive bits need no scale/coefficient materialization. Known concrete narrowing obstruction is recorded below.

Evidence shorthand (all reused conditionally, without new kernel proofs):

* **A585/586**: decimal-sum585 proof and sticky-equality586 proof; exact/finite close alignment, signs/cancellation, raw private rounding and remote post-sticky route. **M580**: contextual product580 proof, mandatory rounding before final coefficient range check; retained566 HIGH/LOW/zero scale policy.
* **D581/560**: context quotient581 proof plus preferred-scale560 policy. Reduced exact 2/5 division, finite p-digit rounding/carry, closest exact cohort, old safe fallback, and expressly admitted endpoint errors remain distinct. **I582/R583**: no-context integral quotient582 and residual583 proofs; their endpoint/eligibility/prefix limits are not erased. **Q584**: complement midpoint identity r versus d−r, all signs/modes/parity, no doubled-residual range failure.
* **S566/Z567**: rescale/rounding and zero-context proofs. Direct plus(C)/round(C) zero scale0 is separate from contextual abs/negate's scoped zero cohort. **SQ563/565/579/587**: finite square classification, scale-parity path, safe midpoint test, raw normalized root fallback and accepted literal p-digit inexact cohort policy. **P542**: prescribed pow recurrence/capped working precision under completing supported multiply/divide leaves; public wording is “in general” two ulps, not an invented universal2ulp theorem.
* **F543/F557**: current floating binding, exact decimal-prefix/IEEE rounding proof, and the distinct open erased/generic Kotlin/JS Number barrier557. **V555**: canonical Double factory grid-selection/format proof and finite canonical tests; finite Double domain intermediates are bounded. **U541/V**: ulp source proof, value/cohort/identity controls and overload finite controls. These do not close all541/P1–P6.

`evidence-pins.sha256` binds30 current source/proof/test files. Its constructor-related588/589/569 entries establish current source chronology only; this report does not duplicate their constructor inventories. Existing local source247 report records1687 passed method executions and the retained complete sqrt ledger on this exact BD/BI source; that finite evidence is not an all-input theorem. Hosted and private source248 execution are not claimed here.

## Constants — 12 entries

| Java → common entry | Required value/meaning; invalid category | Current source / accounting |
|---|---|---|
| ZERO → ZERO | 0@0 | 927, canonical constant; V. |
| ONE → ONE | 1@0 | 928; V. |
| TWO → TWO | 2@0 | 929; V. |
| TEN → TEN | 10@0 | 930; V. |
| ROUND_UP → ROUND_UP | legacy token0, away from zero | 931; enum mode mapping, Q584. |
| ROUND_DOWN → ROUND_DOWN | token1, toward zero | 932; same. |
| ROUND_CEILING → ROUND_CEILING | token2, toward +infinity | 933; same. |
| ROUND_FLOOR → ROUND_FLOOR | token3, toward −infinity | 934; same. |
| ROUND_HALF_UP → ROUND_HALF_UP | token4, nearest/tie away | 935; same. |
| ROUND_HALF_DOWN → ROUND_HALF_DOWN | token5, nearest/tie toward zero | 936; same. |
| ROUND_HALF_EVEN → ROUND_HALF_EVEN | token6, nearest/tie even | 937; same. |
| ROUND_UNNECESSARY → ROUND_UNNECESSARY | token7, forbid discarded nonzero digits | 938; increment throws AE when rounding is needed. |

## Static factories — 3 entries

| Java signature → common | Result/isolated invalid category | Current path; evidence/remaining leaf |
|---|---|---|
| valueOf(long) → valueOf(Long) | Exact v@0; none | 940 → Long constructor/BI.valueOf; signed Long endpoints supported. No cache identity promise. |
| valueOf(long,int) → valueOf(Long,Int) | Exact v@s, all Int s; none | 941 → BI.valueOf + primary decimal. C/T. |
| valueOf(double) → valueOf(Double) | Decimal corresponding to canonical Double string; NaN or either infinity → NFE | 942 → own decimalDoubleString, finite guard at DoubleFormatting:5, shortest-grid selection then exact String parse. ±0 gives0.0 representation. V555, canonical tests; distinct from exact binary-value constructor. |

## Arithmetic — 12 entries

For finite arithmetic, p>0 means exact mathematical operation followed by m rounding, with preferred scale and mandatory carry/drop rules; p=0 delegates exact arithmetic. UNNECESSARY rejects when a nonzero discarded part prevents exactness. Unsupported final range/resource premises are R548, not a blanket implementation exception waiver.

| Java signature → common same-named mapping | Result/scale and isolated invalid category | Current path; evidence/remaining leaf |
|---|---|---|
| add(D) | x+y at max(s,t) | 65–74: stored585 certificate or aligned add; A585/C/T/R548. |
| add(D,C) | Rounded x+y, preferred max(s,t); required rounding with UNNECESSARY → AE | 75–133: p0, zeros, alignment check, close/raw or remote sticky; A585/586,S566,Z567. Wider prefix/range obligations541/548 retained. |
| subtract(D) | x−y at max(s,t) | 134–139: raw585 or aligned subtract; A585/C/T/R548. |
| subtract(D,C) | Rounded x−y, preferred max(s,t); forbidden rounding → AE | 140–144: p0 or virtual subtrahend.negate then context add; A585/586/T. |
| multiply(D) | xy at s+t when representable | 145–149: scaleForValue then BI product. Required coefficient overflow → AE; endpoint zero/scale policy retained. M580/S566/R548. |
| multiply(D,C) | Rounded xy, preferred s+t; forbidden rounding → AE | 150–172: p0, old scale/sign prefix, stored580 or safe product; separate HIGH mandatory rounding/carry. M580/S566/C/T/R548. |
| divide(D,int,int) → divide(D,Int,Int) | Rounded x/y at requested scale u; invalid legacy mode → IAE; y=0/forbidden rounding → AE | 235–238 validates mode then enum explicit-scale call; Q584/S566/R548. |
| divide(D,int,R) → divide(D,Int,R) | Same requested-scale result; y=0/forbidden rounding → AE | 239–286: zero-divisor staging, Long net shift, exact-zero/tiny shortcut, scaled numerator/denominator and roundedDivide. Q584; remaining scaled-normalization range premise548. |
| divide(D,int) → divide(D,Int) | Rounded x/y at **receiver scale s**; invalid token → IAE; y=0/forbidden rounding → AE | 230–234 validates mode then enum rounding-only call. Legacy Int here is a mode, not a requested scale. |
| divide(D,R) → divide(D,R) | Rounded x/y at receiver scale s; y=0/forbidden rounding → AE | 228 delegates explicit scale using stored s; Q584/T/R548. |
| divide(D) | Exact x/y, preferred s−t, increased scale as necessary for finite decimal; y=0/nontermination → AE | 174–227: zero guard outside catch, reduced denominator factors2/5, natural scale check, normalize/strip. D581/560/T/R548. Exact natural LOW padding is not newly required; internal AE text replacement is diagnostic accounting, not proof of nontermination. |
| divide(D,C) | Context quotient, preferred s−t; y=0, p0 nontermination, forbidden rounding → AE | 287–404: p0/exact; captured normalization metadata; stored581 or retained safe/wide route; exact cohort selection. D581/560/Q584/T/R548. No optional endpoint LOW rescue inferred. |

## Integral quotient/remainder families — 6 entries

Q=trunc(x/y), r=x−Qy; r is exact and has x's sign or zero, abs(r)<abs(y). Quotient preferred scale is s−t, selected within representable/precision constraints. Remainder scale is the exact composition cohort, not a p-digit rounding requirement.

| Java signature → common | Isolated errors/result distinction | Current path; evidence/remaining leaf |
|---|---|---|
| divideToIntegralValue(D) | y=0 → AE; Q independent of rounding mode | 416–443: magnitude/zero prefix, captured old guard context, stored582 when eligible, retained fallback. I582/D581/560/T/R548. |
| divideToIntegralValue(D,C) | y=0 or integer quotient cannot fit finite p → AE; m does not alter Q | 444–480: p0, DOWN context division, negative-scale residual admission, rescale/strip. I582/R583; current published exact-LOW residual obstruction590 remains separately noted below. |
| remainder(D) | Exact r; y=0 → AE | 497 → virtual pair then checked slot1; R583/U541/T/R548; not a new identity/order promise. |
| remainder(D,C) | Exact r can exceed p digits; zero divisor/quotient precision errors → AE | 498 → context pair/checked slot. Public UNNECESSARY clause does not authorize rounding r; ordinary r itself is exact. Same residual590 boundary. |
| divideAndRemainder(D) | Fresh two-slot result [Q,r]; y=0 → AE | 481–487: virtual integral quotient, stored residual583 or old subtract(Q.multiply(y)). R583/V/T;590 boundary. |
| divideAndRemainder(D,C) | [Q,r], finite precision constrains Q, not exact r | 488–496: p0 exact pair; finite integral, same residual selection/fallback. R583/V/T;590 boundary. |

Original malformed replacement arrays/identity/throwing callbacks remain classified by541/526; the mathematical pair law is not proof that arbitrary overrides obey it.

## Powers and root — 3 entries

| Java signature → common | Result/isolated invalid category | Current path; evidence/remaining leaf |
|---|---|---|
| pow(int) → pow(Int) | Exact x^n at logical scale n·s; n outside0..999999999 → AE; 0^0=1@0 | 573–578: range guard, n0, Long scale/product. P542/C/T/R548. |
| pow(int,C) → pow(Int,C) | Prescribed single-accumulator repeated squaring with work p+digits(abs(n))+1, reciprocal for n<0, final context round; stated accuracy in general within2ulps | 579–599: p0 exact restriction; n outside±999999999 or exponent digit count>p → AE; zero negative power → division AE; UNNECESSARY/scale/range failures as applicable. Long working precision capped atMAX only where completing supported coefficients lose no required digits. P542/M580/D581; no invented universal2ulp barrier, unsupported working values and endpoint permissions remain scoped. |
| sqrt(C) | Nonnegative root; preferred trunc(s/2); within1ulp, half modes within0.5ulp, accepted587 inexact p-digit cohort | 601–703: negative→AE; zero preferred cohort; complete finite/parity563 prefix; p0 irrational or UNNECESSARY nonexact/insufficient digits→AE; otherwise materialized565 or raw579 with587 literal cohort/range bound. SQ563/565/579/587/T/R548. Large symbolic domains remain unexecuted; no blind p>H cap before finite classification. |

## Unary and metadata — 11 entries

| Java signature → common | Result/scale and isolated invalid category | Current path; evidence/remaining leaf |
|---|---|---|
| abs() | abs(x) at s | 49: signum/conditional virtual negate; T/U541. No general reuse identity promise. |
| abs(C) | Context-rounded abs(x), operation-specific cohort; forbidden rounding→AE | 50–56: sign branch/negate(C), contextualUnaryRound; scoped zero preserves its own cohort, not direct plus(C)'s zero rule. Z567/T/541. |
| negate() | −x at s | 51: coefficient.negate, same stored scale; C/T/U541. |
| negate(C) | Context-rounded −x; forbidden rounding→AE | 52–56: virtual negate then contextualUnaryRound; Z567/T/541. |
| plus() | x at s; **same receiver reference** | 57 exactly this; U541 source proof and explicit caller/super controls. |
| plus(C) | Context-rounded x; **zero result scale0**, including p0; forbidden rounding→AE | 58–63: private roundValue, stored zero→ZERO. Z567/S566/T; ZERO cache identity is not this overload's promise. |
| signum() | Exactly−1,0,1 according to x | 48 virtual coefficient.signum; canonical BI/T. |
| scale() | Stored Int s | 45; no arithmetic or allocation; C. |
| precision() | Digits of abs(a), zero has precision1 | 46–47 → stored decimal digit helper528; C/R548. |
| unscaledValue() | BigInteger value a | 44 returns coefficient; value requirement, no general identity guarantee; C/T. |
| round(C) | Same effect as plus(C), including zero scale0; forbidden rounding→AE | 504 virtual plus(C); private roundValue505–516 drops digits/handles carry with checked reduced scale. Z567/S566/T; explicit override return/exception preserved. |

## Scale and unit operations — 8 entries

| Java signature → common | Result/scale and isolated invalid category | Current path; evidence/remaining leaf |
|---|---|---|
| setScale(int,R) → setScale(Int,R) | Same value if exact, else m-rounded coefficient at **requested u**; UNNECESSARY inexact→AE | 522 enum.ordinal→legacy virtual overload→private523–549. Downscale partitions k>D,k=D,k<D avoid unsupported10^D. S566/Q584/T/R548. |
| setScale(int,int) → setScale(Int,Int) | Same requested-scale rule; token outside0..7→IAE; forbidden rounding→AE | 521 validates R.valueOf before same-scale/zero checks; no mutation. S566/Q584/T. |
| setScale(int) → setScale(Int) | Exact same value at u; necessary rounding→AE | 519 virtual legacy UNNECESSARY call. Upward-scale coefficient is required output; unsupported output remains R548. |
| movePointLeft(int) → movePointLeft(Int) | Value x·10^-n, scale max(s+n,0) when representable | 563→movePoint565–569, Long sum/check then setScale0 if negative. Scale overflow→AE as public permission; endpoint zero policy/R548 distinct. U541/S566/T. |
| movePointRight(int) → movePointRight(Int) | Value x·10^n, scale max(s−n,0) when representable | 564→same route; Long handles n=MIN without wrapped negation. Same premises. |
| scaleByPowerOfTen(int) → scaleByPowerOfTen(Int) | Value x·10^n, scale s−n | 570 uses Long/scaleForValue, no zero-clamp-to-scale0; unsupported scale→AE, retained zero saturation policy. U541/R548. |
| stripTrailingZeros() | Same value, remove all decimal trailing zeros; numerical zero returns **BigDecimal.ZERO** | 550–562: stored zero shortcut, /10 loop; required stripped scale belowMIN→AE. Source/value/identity law conditionalT/R548; no blanket reuse promise for nonzero. |
| ulp() | Positive1@s for every a and Int s | 571 directly constructs ONE at stored s. U541 complete simple source argument plus selected endpoint/caller controls; no magnitude-dependent range obstacle. |

## Comparison/equality — 5 entries

| Java signature → common | Required result | Current path; evidence/remaining leaf |
|---|---|---|
| compareTo(D) → override compareTo | **Exactly−1/0/1**, numerical cohort order independent of scale | 705–730: compact Long compare, signs, adjusted exponents, stored significands; **CMP-BD-IMAGE** below. Mathematical exponent/significand comparison binding528 is distinct from exact return-image. |
| equals(Object) → equals(Any?) | True iff own D has same coefficient **and scale**; null/unrelated false | 733 stored representation equality; V. No cross-equality with host java.math.BigDecimal required. |
| min(D) → min | Numerically smaller; **receiver returned on numerical tie** | 731 compareTo<=0; V/T, dependent comparison-order premise below. |
| max(D) → max | Numerically larger; **receiver returned on numerical tie** | 732 compareTo>=0; same. This differs from BigInteger's either-argument tie permission. |
| hashCode() → override hashCode | Equal representations have equal stable hashes; equal numeric/different-scale objects need not hash equal | 734 Int fold31*coefficient.hashCode+s; V/C/T. Exact host hash recurrence is observational, not a separately mandated formula. |

## Text output — 3 entries

| Java signature → common | Required text/representation | Current path; evidence/remaining leaf |
|---|---|---|
| toString() → override toString | Locale-independent canonical coefficient/scale encoding, exact String-constructor representation roundtrip | 746–783: cache then private layout; plain iff s≥0 and adjusted exponent≥−6, otherwise scientific signed exponent. U541/P6/C/T/R548; cache/private metadata traces diagnostic. |
| toEngineeringString() → same | Engineering exponent multiple of3; same value, zero scale preserved by zero syntax; nonzero parsed scale need not equal original | 794–811 modulo3/zero cases and sign/layout. U541 finite literals; general formatting proof/resource leafP6/R548 remains conditional. |
| toPlainString() → same | Exponent-free same value; s>0 retains fractional places; s≤0 uses scale0 spelling, negative-scale zero is0 | 784–793 plus canonicalPlain/Long length guard. U541/S566/T/R548; impossible String length is distinct from coefficient range. |

## Numeric conversions — 12 entries

| Java signature → common | Valid result/isolated invalid category | Current path; evidence/remaining leaf |
|---|---|---|
| toBigInteger() → same | Truncate fractional part toward0; unsupported required BigInteger magnitude→AE under R548 | 813–818: zero, multiply10^-s if s≤0, otherwise tiny0 or supported divisor. C/T/BI arithmetic; physical result limits remain explicit. |
| toBigIntegerExact() → same | Exact integer; nonzero fraction→AE; unsupported final magnitude→AE | 819 virtual setScale(0,UNNECESSARY) then stored coefficient. S566/Q584/T/R548. |
| longValue() → Number override toLong | Trunc(x) modulo2^64 interpreted signed; no exactness overflow error | 823→integerLowBits824–829; **LOWBITS-548** below for s<0. C/T/F557; Float proofF543 does not cover this separate temporary-product leaf. |
| intValue() → Number override toInt | Trunc(x) modulo2^32 signed; no exactness overflow error | 821 same helper then narrow; LOWBITS-548/F557/T. |
| longValueExact() → same | Exact signed64-bit integer; nonzero fraction or out of range→AE | 842–853: compact scale0; zero; adjusted-digit guards; virtual exact rescale0; bitLength endpoint guard. C/T/S566, reached BI conversion; wider callbacks541 remain classified. |
| intValueExact() → same | Exact [−2^31,2^31−1], otherwise AE | 834→narrowExact837–841→virtual longValueExact then literal range; no 32-bit wrap admitted. T. |
| shortValueExact() → same | Exact [−2^15,2^15−1], otherwise AE | 835→same Long leaf/literal bounds. T. |
| byteValueExact() → same | Exact [−2^7,2^7−1], otherwise AE | 836→same Long leaf/literal bounds. T. |
| floatValue() → Number override toFloat | IEEE binary32 nearest/ties-even, signed zero underflow/infinity overflow | 857→binaryValue(24,−126,127); stored bounded decimal prefix + rational rounding, subnormal/join/overflow. F543/Q584/F557/T/R548. |
| doubleValue() → Number override toDouble | IEEE binary64 nearest/ties-even, signed zero/infinity endpoints | 855→binaryValue(53,−1022,1023), same argument. F543/Q584/F557/T/R548. |
| inherited Number.byteValue() → override toByte | Signed low8 bits after truncation | 831 virtual toInt().toByte(); LOWBITS-548 and F557/T. Meaningful explicit leaf dispatch retained. |
| inherited Number.shortValue() → override toShort | Signed low16 bits after truncation | 833 virtual toInt().toShort(); same premises. |

Partitions total12+3+12+6+3+11+8+5+3+12=**75**. Seven extra member operators901–907 map to add/subtract/multiply/remainder/negate, unary plus=this, and division at receiver scale with HALF_EVEN. In particular Kotlin `/` here does **not** mean the exact one-argument Java divide operation; existing DecimalOperatorContractTest documents this migration syntax. These extra operators are outside the61-method Java inventory.

## Exact named residuals, not a request for generic more testing

### LOWBITS-548: supported primitive result obstructed by a temporary product

This is a **concrete source/math witness**, not an observed giant runtime/JDK failure. Let valid canonical A=2^L−1, and construct x=A@−1. The coefficient has exactlyL bits and scale−1 is Int, so this is a supported input representation. No giant object is allocated by this audit.

For w=32 or64, L≥w and2^L≡0 mod2^w, so trunc(x)=10(2^L−1)≡−10 mod2^w. The required int/long result is therefore **−10**. The public Java intValue/longValue clauses explicitly discard the fraction and retain low-order bits rather than requiring an exact intermediate BigInteger result.

At BD824–829, s=−1 bypasses the `−s>=bits` modular-zero shortcut and evaluates `coefficient.multiply(tenPow(1)).toLong()`. With BI12fd:65–69, magnitudeBits(A)=L, magnitudeBits(10)=4, so `L+4−1>L` triggers the correct BigInteger public-product AE **before** primitive narrowing. This product is an avoidable internal representation, not the required primitive result. The inherited Byte/Short routes delegate to toInt and inherit the obstruction. For toBigInteger(), in contrast, the same enormous product really is its required result and may properly reject under the supported magnitude policy.

This identifies the exact uncovered entry/condition for548: negative-scale primitive narrowing where a supported stored coefficient times a small power exceeds the public BigInteger range, although only modulo2^w is requested. Existing completed543 concerns Float/Double and cannot establish this integer-low-bits leaf; finite low-value conversion tests cannot discharge this maximum-magnitude condition. Parent independently checked the source/public API argument and created **local:591**, with548/549 depending on it. This ledger adds no algorithm, test, implementation scope or runtime grant. No current platform failure or JDK implementation cause is alleged.

### CMP-BD-IMAGE: exact return-image and dependent negative ordering premise

Java BigDecimal.compareTo promises−1/0/1. BD710 returns raw Kotlin Long.compareTo for compact equal-scale inputs; BD714 returns Int.compareTo for differing signs. BD716–717 returns raw magnitude comparison or its negation. Magnitude comparison can use adjusted-exponent Long.compareTo at724 or stored significand comparisons; public primitive contracts guarantee ordering signs, not exact unit images. Under only those abstract contracts, a hypothetical Int.MIN_VALUE negative result also needs exclusion before negation at717 to prove correct negative-receiver order.

This is **an unclosed source/API portability premise, not an observed failing target**. Existing 528/541 comparison and finite target evidence establishes its stated scope; do not infer exact image merely from sign assertions. Root's separate549 BI normalization at private BI866efd closes that BI public return, not these BD paths/private magnitude leaves. Min/max need correct sign rather than unit image, but share the dependent negative sign-reversal premise. No compiler/stdlib body or new probe/test/fix was read/run/added.

### Current exact-LOW residual590: unpublished successor, not silently fixed base

BD5731's finite integral admission and pair/remainder routes can still fall back to public quotient.multiply(divisor) when the stored583 certificate declines outside-LOW preference. The accepted symbolic590 obstruction has a supported quotient and zero remainder but an unsupported unnecessary materialized product/alignment. This ledger does not mark the published functions repaired.

The separately reviewed private successor has author BI4d325b3272c3b9bd120f919bf7a60f7b46523b1266cd304f4cf1020c0a3651cb; root590+549 composed BI866efd55315de05a850671ebf8648a66b146c898e81c008144a1d3b9682850e3 with source248 manifestd9074c29e8b4680663eb6ace92b83a38fafb3bcf15604be2f81a71e9058af17a. It recomputes only an exact supported natural quotient on fresh base wrappers, validates numeric candidate equality and returns the exact zero cohort, with decline preserving old fallback. The independent SOURCE reviewdb8e9cf1904007ca001c877cfba931fafa5bffaf59e857e6cff5aa2ba8491f31 accepts that limited refinement. Its runtime/root adoption remain parent-owned; no conclusion about the concurrently running248 campaign is made here. General alternative-cohort LOW rescue is still not adopted.

## Evidence and completion limits

Existing DecimalOverloadContract541Test provides three finite ordinary/truthful integral/movement/text and explicit caller/super controls; DecimalUlp541ContractTest provides the direct ulp/no-context-plus laws and explicit override/super distinctions. DecimalScaleContractTest covers bounded net shifts, zero divisors, all mode/tie cases, legacy/operator routing and selected truthful subclasses. ValueIdentityContractTest covers representation/cohort equality, collections, mutation isolation, min/max ties and constants. CanonicalDoubleTest covers named shortest-grid boundaries/nonfinite/zero/exact-binary distinctions. Existing580–589 proof/test/verification files preserve their exact source/runtime provenance. Constructor proofs are not new method behavior evidence by repetition.

No API entry was omitted and no finite fixture/error family was deleted or reclassified by this ledger. The concrete LOWBITS-548 condition, CMP-BD-IMAGE premise and still-published590 obstruction are specifically named; remaining R548 physical/endpoints, T541/526 caller/poison partitions, P542 conditional accuracy scope and external F557 remain explicit. No universal arithmetic/range proof, full549/541/548 closure, all-platform guarantee, benchmark or official JCK/TCK certification follows from this report or source247's finite green methods.
