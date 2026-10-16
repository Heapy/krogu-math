#549: current API and constructor proof binding

## Source255 successor record

The [255-source manifest](source255.sha256), SHA
`8427be10a953974f1b74348dec87ea0cbbbb6f93c6e806875b049e4ff2fb339d`,
binds the later normalization successor. BigDecimal is
`82e5342ab19024aed3aee3191eb340a72eba316c843d708986e5eb5827d2468f`;
BigInteger, MathContext and RoundingMode retain the source253 hashes below.
Exactly five private division-helper lines change production; constructor,
lexer, byte, coefficient-rounding, nullability and serializer bodies remain
unchanged. Earlier source253 line references below are intentionally retained.

The [594 verification](decimal-division-normalization-594-verification.md)
records independently accepted local execution:436 suites/1757 methods across
JVM, JS Node, Wasm Node and macOS ARM64, complete94282 sqrt records/52 recorders,
own JAR `bb15a352df94be410ce79c310af6f541ee4206c59148dc30f81e9dc79270104b`.
The source-only pending/UNADOPTED paragraphs below describe the preserved
preparation milestone, superseded only by this exact-source local record.
They are not claims that source253 outcomes executed source255. Matching
source255 hosted/public acceptance remains separate; Number525/557 and the
explicit research/scale/resource qualifications remain open at their scopes.

## Preserved source253 binding milestone

SPDX-License-Identifier: Apache-2.0

This is the current binding/disposition prelude to the historical549 inventories and proofs. It preserves their original bodies, source hashes, line references and read-time limitations. It adds no production code, assertion, fixture, mapper or runtime result and does not claim unconditional whole549/DONE, complete Kotlin compatibility or JCK certification. The independent disposition input is `/private/tmp/krogu-549-disposition-audit-OQncAayJ/REPORT.md`, SHA256 `2d8ec86e67b1a4387bc22579f0e672939ac234a8207f681646e5306db9137412`.

## Current source, not cosmetic repinning

The published numerical baseline is `62bb43ccd384d5678dd22d25a95c97cc18f51362`, source253. Its [complete manifest](source253.sha256) hashes `aa90e836005efc363e9575026cd12049b0bb6ac42eb3a716f20d04c596bac1ad`. All253 source hashes and the full src pathset were checked for this preparation. Exact core hashes are:

| Type | Current source253 SHA256 |
| --- | --- |
| BigInteger | `8b6359be57ebbc8047102e5dea420e307d4fb128cbf686a054b04dff2600b1ff` |
| BigDecimal | `5761b7113f808d258deaa6266ff55a1a409ee41b238b2dc1f28a1724bd8f0b9a` |
| MathContext | `d3690387b1345f90cc927044a1a450983dd0d2ce6e3e47054384c31979fd5261` |
| RoundingMode | `e47d7aa314ddaa9c79ba13b79a00beadb8be96cbb473cd4ad2dfae3e0501a5ee` |

The [constructor inventory](constructor-overload-inventory-549.md) maps27 real public constructors:8 integer,16 decimal,3 context and0 enum, plus two commonRandom constructor-like functions. The [integer](integer-public-contract-549-ledger.md) and [decimal](decimal-public-contract-549-ledger.md) ledgers map57 and75 nonconstructor method/constant entries. These are declaration inventories, not behavioral theorems or an instruction to reproduce Java construction syntax in production. Java new/constructor-reference/super call shapes are needed only in test adapters. Kotlin caller types, operators, overloads, conversions and Number behavior remain migration obligations.

The following correspondence uses actual source253 bodies. Earlier proof line numbers remain historical; new source locations below are not substituted into old proof bodies.

| Current entry/body | Existing argument and current composition | Required boundary retained |
| --- | --- | --- |
| BI31–36,1265–1303: String/radix parsing | [Constructor grammar coverage](constructor-contract-549-coverage.md) plus parser559's conservative Long lower bound. Valid digits update `n=radix*n+d`; supported final magnitude bounds every nonnegative prefix. The true definite-size branch validates the remaining suffix first; the false branch validates while accumulating. | Valid supported strings are not lost to an arbitrary smaller cap. Isolated empty/sign-only/radix/invalid-digit categories remain required. Unsupported-prefix plus malformed-suffix priority is separately qualified below. BMP digit mapping is a primitive/toolchain premise supplemented by exhaustive single-code-unit coverage, not by pretending that enumeration alone proves arbitrary-string grammar. |
| BI1304–1362: byte/signed-byte/slice construction | [Byte549 proof](integer-byte-constructor-549-proof.md): accepted stable slice polynomial, retained negative sign, FF stripping, complement carry,30-bit packing and final high-word check. Bounds use `off<=size-len`; checked end cannot wrap. `packedWords` sizes staging; `normalized` checks actual trimmed magnitude. | Empty valid magnitudes/zero, sign mismatch and invalid sign, empty two's-complement backing, valid/invalid slices, big-endian two's-complement values and post-return isolation keep their separate laws. Effective byte-count admission is not the final bit-length theorem; feasible allocations and stable input while reading remain premises. |
| BD15–42: primary/integer/Int/Long contextual constructors | [Coefficient549 rounding proof](contextual-constructor-rounding-549-proof.md). Primary normalization retains an exact BI directly or calls a constructing BI subtype's virtual byte encoding, then uses a base BI. Context overloads round a fresh base decimal temporary and copy its private fields. Current `roundValue`509–520 is the same significant-digit/drop/carry composition. | Truthful/reached byte encoding, immutable normalized state and exact integer leaves are premises. The constructing decimal subclass's public round/plus override is not the private temporary's method. This is a reached-path argument, not a universal callback noncall guarantee. Constructors retain their own zero scale. |
| BD509–520,1050–1065: rounded coefficient/drop | Coefficient549 proves p0 exactness, signed all-mode ties/parity, a supported divisor no larger than input, one inexact round and at most one extra exact carry division. [584 midpoint](scaled-division-midpoint-584-proof.md) now implements half comparison using r versus d−r, preserving that rounding theorem without unsupported2r. | Inexact UNNECESSARY and unrepresentable required rounded scale remain AE; a second inexact rounding is not introduced. Scale-before-round ordering in the own body is not promoted into a universal compound-invalid priority rule. |
| BD32–40,971–1004 and DecimalText58818–25/scan/parse | Small exact/context String paths retain the proven lexer, while the large context String route uses [588](contextual-text-588-proof.md). All whole/slice char constructors use its owned indexed reader; [589](exact-char-construction-589-proof.md) specializes that SAME lexer to precision0. | Full selected lexical validation, exponent/initial-scale checks, zero/leading-zero handling, signed guard/sticky rounding and raw carry normalization precede final coefficient publication. No whole char-slice String or p-only cap is required. Accepted slice ownership is not an atomic concurrent-snapshot guarantee. |
| BD1005–1027: exact Double and context Double | [543 field-class proof](floating-conversion-543-proof-binding.md) covers zero/nonfinite, normal/subnormal mantissa/exponent classes, exact power-of-two removal and `M*5^s@ s`. M<2^53 and s<=1074 give even the loose3275-bit coefficient bound. `roundedDouble` first constructs the exact base value, then applies the existing context path for nonzero. | Nonfinite Double rejectsNFE; finite value is its exact binary rational, not the canonical shortest decimal text. Canonical valueOf/Float/Double extension routes have their separate543 interval/text arguments. Finite sample counts do not replace the exhaustive field-class proof, and no2^64 test enumeration is required. |
| BI37–38,1238–1264; JVM PlatformRandom typealias | [PlatformRandom549 lemma](platform-random-validation-549-proof.md): actual `java.util.Random` entry guards every negative numBits withIAE and every bitLength<2 withAE before adapter delegation; zero bits gives numericalzero. CommonRandom overloads remain distinct. | No missing mirror invalid-size JVM matrix is inferred where the guard/typealias composition already proves the whole Int partition. Required positive generation/probability properties retain [547](primality-547-proof-binding.md)'s sampling premises, generation/search-only limit and548 capacity premises. Caller exceptions are not swallowed; no exact draw/state sequence is a new obligation. |
| MathContext.kt5–58; RoundingMode.kt4–12 and generated enum members | [Small-type549 ledger](context-mode-contract-549-ledger.md): all nonnegative Int precisions, eight exact modes, negative precisionIAE, canonical text/roundtrip, final-class field/equality/hash laws and checked enum lookup. | Precision0 retains its stored mode although arithmetic ignores it. Exact numerical enum/context hash parity across runtimes is observational; equality/hash consistency and enum identity/array isolation are required. Additional numeric precision lexemes retain the explicit extension policy below. |
| All48 rejecting input slots, three nullable equals inputs and four OrNull results | [573–578 actual migration](non-null-api-573-verification.md) binds the adopted declarations, matching Kotlin overrides,49 compiler jobs and real Java/reflection boundary controls; original observations are retained in adapters. | Otherwise-valid null requires NPE. Library-defined/specified errors and caller-created exception identity/text remain required. VM wording, unrecorded null messages and unknown competing-invalid order are not universal guarantees. Java source compatibility beyond TCK is excluded; Kotlin Number525/557 remains explicitly unwaived. |
| Current integer/decimal public arithmetic entries | [590 exact-LOW certificate](decimal-exact-low-residual-590-implementation.md), [591 low bits/comparison](decimal-primitive-low-bits-591-proof.md), [592 fixed-scale division](decimal-fixed-scale-592-proof.md), [593 work precision](decimal-power-593-implementation.md) and their [250](decimal-range-590-592-verification.md)/[253](decimal-power-593-verification.md) actual records supersede named historical source247 obstructions. | These are focused repaired leaves, not proof of every resource, scale or callback composition. Current pow's nonzero private593 selection is true working precision W>K646456992, including K<W<=Int.MAX_VALUE; true W can reachMAX+10. Neither a cap atMAX nor a missing593 implementation is the current source rule. |

## Isolated rejection categories remain mandatory

The following categories are not waived by diagnostic-message classification. Each row assumes the other inputs are valid; competing-invalid ordering needs its own clause/disposition.

| Entry/condition | Required category |
| --- | --- |
| Integer text: invalid radix, empty/sign-only text or invalid digit | NumberFormatException; actual unsupported generated magnitude is separately ArithmeticException, with the compound prefix/suffix question below. |
| Integer bytes: empty two's-complement backing input, illegal sign or sign0/nonzero-magnitude mismatch; otherwise valid backing with invalid slice bounds | NumberFormatException for the former isolated conditions; IndexOutOfBoundsException for the isolated invalid byte slice. Empty valid unsigned magnitudes remain zero. |
| Random constructor negative numBits; prime requested bitLength<2 or chosen supported generation/search limit exceeded | IllegalArgumentException for negative random-bit count; ArithmeticException for the prime size conditions. |
| Decimal malformed text/exponent/initial lexical scale, invalid character-slice range or nonfinite Double | NumberFormatException. A valid context text whose required rounded result is inexact under UNNECESSARY, outside required coefficient support or outside the admitted rounded scale has the separately specified ArithmeticException category. |
| MathContext negative precision or malformed/out-of-range setting text; RoundingMode unknown name or legacy code outside0..7 | IllegalArgumentException. Precision0/Int.MAX_VALUE and valid named modes are not narrower domains. |
| Otherwise-valid rejecting-null reference input at the Java/TCK boundary | NullPointerException; nullable equals accepts null and returns false. VM wording is not a substitute for this type requirement. |
| Public arithmetic/exact conversion isolated invalids | Retain the public-entry ledgers' ArithmeticException conditions for zero division, forbidden inexactness, exact primitive/integer range and invalid mathematical exponent/root/modulus domains; invalid legacy rounding tokens remain IllegalArgumentException. The current-path range/scale permissions keep their separate proofs. |

Canonical spelling, lexical acceptance, numeric/cohort fields, array isolation and specified exception/caller identities remain independent required fields. Equal error types alone do not prove a valid result, required text or prescribed diagnostic correct.

## Historical premises and explicit qualifications

The old source247 CMP-IMAGE, LOWBITS,590 and scaled-normalization592 entries are preserved historical questions with later successors. Current BI926–936 normalizes comparison sign before negative reversal; BD720–742 does likewise after retaining reported-sign/getter staging. Current BD848 lowprimitive conversion selects591's width-bounded ring for an unsafe exact product;592 avoids unsupported fixed-scale normalized numerator/denominator;590 certifies exact outsideLOW residuals without publishing Q*d. No new implementation is required merely to repeat those historical labels. The earlier G1/G2 proof gaps are addressed by the linked general byte and coefficient-rounding arguments; the earlier chars→wholeString description is superseded by588/589. This is chronology plus actual current-path correspondence, not cosmetic replacement of old hash tables.

Two constructor-wording issues retain precise dispositions. MathContext's canonical format/roundtrip and malformed/range rejection remain mandatory; shared plus signs, leading zeros, negative zero and accepted decimal digit lexemes remain a compatibility extension. Whether every extension is prescribed by the toString-format wording remains qualified; this does not authorize changing the existing grammar or calling the JDK defective. For `BigDecimal(BigInteger,MathContext)`, the general significant-digit rule can require a negative final scale despite literal scale-zero wording. The coefficient549 document preserves that tension and its16-call diagnostic on the earlier exact artifact. Matching reference observations do not settle the normative ambiguity; no new probe, forced finalscale0 or JDK-bug claim follows.

A separate compound question is symbolic radix10 text consisting of H=646456993 nines followed by `!`. Its bound `1+3H=1939370980<Int.MAX_VALUE` selects the legacy integer accumulator. The unsupported numeric prefix can fail before the malformed suffix; universal full-string NFE priority has not been established for that competing-invalid class. This is neither a supported valid integer nor a small isolated malformed input. The independent disposition2d8ec86e records the precise question, not an accepted arbitraryAE, a required defect or a repair grant.559's true definite-size branch still validates its entire suffix first, and ordinary malformed-text assertions remain active.

Current-own-format serialization continues to require value/subclass state, shared-reference/cycle preservation, validation and detached limb storage. A constructing coefficient's reached byte encoding and a restored truthful coefficient's subsequent arithmetic are separate reachable paths. Explicit caller override/super arguments and result/exception identities remain required; an unused poisoned getter never waives inherited mathematics. Hostile/changing/replacing leaves stay method/input-selected541/526 questions. Legacy proxy/binary forms and java.math stream migration remain excluded without narrowing the mathematical contract.

## Pending594 is not current constructor acceptance

The private candidate `/private/tmp/krogu-math-task-594-eGORPc9W/root` has source255 manifest `8427be10a953974f1b74348dec87ea0cbbbb6f93c6e806875b049e4ff2fb339d`, BD `82e5342ab19024aed3aee3191eb340a72eba316c843d708986e5eb5827d2468f`, unchanged BI/MC/RM and two separate new tests. Its entire production delta from published253 is five added lines in private `quotientExponent`/`normalizedComparison`: bothcompact retains old Strings; any noncompact coefficient selects final stored528 digits/significand helpers. Constructor declarations, parser/lexer, byte and rounding methods, nullability, serialization, public API and all old fixtures remain unchanged.

This is **UNADOPTED, runtime pending** at this document's preparation milestone. It is not a new constructor implementation, source255 publication identity or observed engine String-limit failure. Its source proof and prepared3common/1JVM tests remain subject to parent independent review, actual execution and adoption. Any later accepted594 binding must be appended as a later exact-source record, not silently applied to these253 results.

## Actual evidence and remaining scope

| Record | Accepted actual scope and exact artifact | Explicit limit |
| --- | --- | --- |
| Source253 local593 integration | [Durable local report](decimal-power-593-verification.md):431 XML/1744 passing methods,94282 retained sqrt-ledger records/52 recorders; sourceaa90, own JAR `5e07541044a098601d0cdb05ae1c73eaae063fb8181836b5da3ff6a1a97d72c1` | Four local targets; no maximum-input, all-domain or255 runtime theorem. |
| Published62bb stage1 supported/oracle | `/private/tmp/krogu-62bb-supported-stage1-OIgOzUwE/REPORT.md`, SHA `5dac040620f6faf703dfbbe1a3134e743cce1c0b40cb15fe2f23fdf0c94a4c89`:9 XML instances/1035 suites/4068 methods plus25 ordered oracle commands; exact253 manifest/JAR above | No MacIntel, bareARM consumer or normal-public result is borrowed into the original stage1 report. |
| Later MacIntel addendum, independently reviewed and parent accepted | `/private/tmp/krogu-62bb-macintel-addendum-Z43FpoDx/REPORT.md`, SHA `4e675b334454519dc2bdc17518ba9a3df3045de7f6002054c2f421787d2e6954`:3 distinct instances/345 suites/1356 methods, zero issues. Combined four matrix jobs:12 XML instances/1380 suites/5424 methods;25 oracle commands stay separate | iOSx64 simulator execution, not device execution. BareLinuxARM388 methods and complete13-instance5812 total remain unaccepted here. |
| Separate62bb manual-large public campaign | `/private/tmp/krogu-62bb-large-actual-OMDZVwDT/REPORT.md`, SHA `2f4978c69ededd953be4bc43ff21a339a467190c0895c97493ca649d375d6b9e`:12PASS/0FAIL/128SKIP,140 semantic records, exact253/JAR5e075 | No normal106 or118-ID union acceptance; no currentARM completion inferred, no JCK or giant593 execution. |

Parent reports all seven current Compatibility jobs are API-success, but actual ARM-consumer and normal-public artifact review remains separate/pending acceptance at this preparation milestone. Workflow success alone supplies neither their raw inventories nor a full current public union. This document reuses accepted exact reports; it performs no acquisition, auditor or numerical rerun. Earlier source247/250 results retain their historical artifacts and are not relabelled253 or255.

The finite549 constructor/API/error-accounting milestone can be assessed for qualified REVIEW using this current binding and its preserved questions. It cannot waive known Number525/557 behavior, broader541 subtype compositions,548 supported-range/live-allocation/scale obligations, pending594 or any known required mismatch. It does not impose endless new fuzz enumeration, require realJava production constructors or turn finitegreen counters into a universal theorem. Existing active strict oracle assertions and raw observations remain untouched; parent owns task state, publication and subsequent acceptance.
