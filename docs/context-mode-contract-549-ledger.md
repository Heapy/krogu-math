# MathContext and RoundingMode: finite public-entry/error ledger (549)

Historical source-only ledger adopted after independent source/spec review.
Original private bytes remain at
`/private/tmp/krogu-549-context-mode-contract-ledger.md`, SHA-256
`f5c9a0f445afa88e3155bde93381936ddbaff0e563e6169dd830e8d7e28c09ca`.
No implementation or test assertion changes follow from this adoption.

SOURCE/spec and retained-evidence audit, 2026-10-15. Ownership is only this new
private document. No implementation/test/fixture/evaluator/public-doc/Git/tracker
changes; no script, compiler, helper, certificate, numerical operation, probe,
test, build or benchmark execution. Only own code, metadata, tests and official
public JDK25 API documentation were read. No foreign implementation bodies.

## Result and scope

The handwritten constructors, properties/aliases, constants, equality/hash/text
and integer rounding-mode lookup admit the general arguments below. These cover
arbitrary supported Int precisions and arbitrary Int legacy codes, not merely
the tested values. Generated enum and platform behavior is explicitly conditional
on language/runtime contracts. No new implementation defect was established.

One precise normative grammar lemma remains unresolved: whether MathContext's
requirement that its input have the format produced by toString permits the
broader precision lexemes accepted by both our parser and the pinned corpus
(leading plus/zeros, negative zero and BMP non-ASCII digits). Canonical strings
and the exact own accepted language are proved below, but their full normative
equivalence is not assumed. No JDK defect, whole549 closure or all-platform runtime
certification follows from this memo.

Project compatibility policy retains the existing equivalent precision lexemes
as a compatibility extension: plus signs, leading zeros, negative zero and the
accepted decimal digit characters continue to parse to the checked settings.
Canonical output and canonical roundtrip remain required. The wording ambiguity
does not justify changing this already shared JDK/port behavior, declaring a JDK
bug, or weakening range/format validation. Its normative interpretation remains
qualified; no parser change is planned from this item. The independent follow-up
is retained at `/private/tmp/krogu-549-context-string-classification.md`, SHA-256
`e50d48a88a7a0b5164feb2c72c6f855a751818e3d16d230821b50f55e667327a`.

The governing migration is Java -> Kotlin/java.math -> Kotlin/krogu-math. Rejecting
inputs stay NN, matching Kotlin caller/override edits are allowed, Java source
shape is only a TCK/adapter concern, and legacy/cross-format serialization is
excluded. Current own-format roundtrip invariants remain separate and supported.

## Public sources and classification

* [MathContext JDK25 API](https://docs.oracle.com/en/java/javase/25/docs/api/java.base/java/math/MathContext.html):
  all constructors, four constants, getters, equals, hashCode and toString.
* [RoundingMode JDK25 API](https://docs.oracle.com/en/java/javase/25/docs/api/java.base/java/math/RoundingMode.html):
  eight policies, values and both valueOf overloads.
* [Enum JDK25 API](https://docs.oracle.com/en/java/javase/25/docs/api/java.base/java/lang/Enum.html):
  generated/inherited enum semantics; [Object hashCode](https://docs.oracle.com/en/java/javase/25/docs/api/java.base/java/lang/Object.html#hashCode())
  supplies equality consistency, not a fixed host/port hash value.
* [NPE.getMessage](https://docs.oracle.com/en/java/javase/25/docs/api/java.base/java/lang/NullPointerException.html#getMessage())
  permits implementation-specific or null text without a supplied non-null message.

In the ledger, **P** is a source argument conditional on ordinary Kotlin/primitive
and allocation contracts, **G** a generated/compiler/runtime contract, **D** a
retained diagnostic observation and **U** the exact unresolved grammar lemma.
An exception condition/type marked P is required even if its message is D.
None of these labels suppresses an existing assertion or archives an oracle row.

## MathContext: every handwritten public entry

The class is final. The two properties are declared open in a final class, so
ordinary Kotlin callers cannot supply a MathContext subclass with lying getters.
Reflection corruption is not a valid public constructor input. Private parse/copy
constructors and serialization hooks are not extra public constructors.

| Entry / own source | Required domain and value/error | Argument/evidence and boundary |
|---|---|---|
| `MathContext(precision: Int, roundingMode: RoundingMode)`, MathContext.kt:5–7 | Every `precision>=0` and one of eight enum values stores exactly those settings. Negative precision with non-null mode throws IllegalArgumentException. Otherwise-valid null mode at the JVM boundary throws NPE. | P: the single require is exactly the sign predicate; assignments do not narrow or compute precision. G: NN parameter rejection outside Kotlin's accepted domain. E1/E3/E4 below. Negative precision plus null has two invalid inputs; exact priority/message is D. |
| `MathContext(setPrecision: Int)`, :14 | Same entire Int partition, mode HALF_UP. | P: exact delegation to the primary constructor, no second algorithm. |
| `MathContext(value: String)`, :16,37–57 | Required canonical context strings parse to the represented fields; invalid range/format throws IllegalArgumentException. Null string requires NPE at the JVM boundary. | P for canonical language and exact own language below; U for whether all extra accepted numeric lexemes are normative. G for NN boundary. No number kernel or giant coefficient is involved. |
| `precision` property, :5 | Int in0..Int.MAX_VALUE for every ordinarily constructed value. | P: only constructor assignments; immutable val in a final class. Its JVM accessor supplies Java getPrecision shape. |
| `roundingMode` property, :7 | Exactly the stored enum constant, non-null. | P/G as above; no rounding operation occurs here. Its JVM accessor supplies Java getRoundingMode shape. |
| `getPrecision()`, :20–22 | Returns precision unchanged. | P: direct field/property read. Kotlin alias is JVM-renamed/synthetic to avoid a duplicate JVM getter. These annotations do not change the numerical value. |
| `getRoundingMode()`, :23–25 | Returns the stored enum object. | P: direct read, identity retained. Same alias qualification. |
| `equals(other: Any?)`, :27 | True iff the other object is this own MathContext type with equal precision and equal mode; false for null/unrelated types. Precision0 still compares the mode. | P: conjunction implements the stated pair equality; finality prevents subtype symmetry problems. JDK-object assignability/cross-library equality is not required. E1/E3. |
| `hashCode()`, :28 | Equal contexts have equal, stable hashes within an execution. No fixed numeric hash or collision-freedom is promised. | P/G: equal fields feed the same deterministic Int expression and the same stable enum hash. Overflow cannot break equal-input equality. Different fields may collide. Cross-run/vendor/own-vs-JDK numeric enum/context hashes are not compared. |
| `toString()`, :29 | Exactly two ASCII-space-separated setting words, no padding/extra words; decimal precision and exact enum name. | P/G: fixed literal separators plus nonnegative Int formatting and enum's default name text. Arbitrary P/mode canonical roundtrip is proved below. E1/E3. |
| `UNLIMITED`, :33 | precision0, HALF_UP; no rounding performed by constructing the context. | P: literal constructor arguments. Constant does not constrain a decimal operation to a finite exponent range. |
| `DECIMAL32`, :34 | precision7, HALF_EVEN. | P: literal pair. IEEE decimal32 exponent bounds are not imposed by MathContext. |
| `DECIMAL64`, :35 | precision16, HALF_EVEN. | P: same; no IEEE exponent bound. |
| `DECIMAL128`, :36 | precision34, HALF_EVEN. | P: same; no IEEE exponent bound. |
| `Companion`, :31 | Public Kotlin holder for the four vals; no additional handwritten public factory. | G: generated holder/access paths are language/toolchain surfaces, not a fifth context or a public constructor. Java static field shape remains an existing TCK convenience. |

### P: precision, state and equality over the entire ordinary domain

For all Int P, `P>=0` either succeeds unchanged or the require throws the required
IllegalArgumentException. In particular0 and Int.MAX_VALUE do not overflow any
constructor arithmetic, since none occurs. The one-argument constructor supplies
exactly HALF_UP. All four constants are instances of this same accepted domain.
Immutable field reads preserve these invariants. Private current-format readObject
also rejects negative/null restored fields, but that is a separate validated-state
route, not permission to prove arbitrary reflection-modified states correct.

The relation on field pairs `(P,R)` is reflexive, symmetric and transitive because
Int equality and enum equality are. The initial type test makes null/unrelated
objects unequal. Equal pairs have identical hash expression operands, hence equal
Int results regardless of arithmetic wraparound. Stable immutable pairs and stable
enum hashes give stable hashes. A specific multiplier or a host enum's identity
hash is not specified by the MathContext API. No cross-library hash equality or
specific collision distribution is required.

### P: exact own parser language, arithmetic and index bounds

Define `digit(c)` by Kotlin Char.digitToIntOrNull(10), conditional on the target's
documented UTF-16 digit behavior. Let `D+` mean one or more such decimal digit
code units, `S` be empty or one ASCII `+`/`-`, and `R` be one exact enum name.
The source accepts exactly

`precision= S D+ <one ASCII space> roundingMode= R`

with the signed digit value in0..Int.MAX_VALUE. Spaces in the display delimit
tokens, not extra input characters: the only actual separating space is the one
before roundingMode. A negative sign with numerical zero is accepted; a negative
nonzero result is rejected by the primary require after mode parsing. Leading
zeros and ASCII plus are permitted by the source. Non-ASCII decimal digit code
units accepted by the Char primitive are permitted; supplementary scalar digits
encoded as surrogate pairs do not become one decimal Char.

The first-space search, literal prefix and second-label guard admit no leading
space, second separating space, trailing mode text, alternate label or whitespace
separator. `separator>10` ensures a first precision character exists at index10.
The optional sign advances once; equality with separator rejects a bare sign.
Every later precision code unit must be a digit. Enum lookup enforces the entire
remaining name without trimming or case folding.

Before each digit step, magnitude M is at most B, where B is2147483647 for a
nonnegative token or2147483648 for a minus token. The next value is at most
`10*B+9<=21474836489`, strictly below Long.MAX_VALUE. Values above B are rejected
immediately, so induction excludes Long overflow even for arbitrarily long
representable strings, including a million or Int-bounded number of leading
zeros. No multiplication proceeds with an already over-limit accumulator.
Negation of accepted M is in[-2147483648,0], then toInt is exact. Negative
nonzero values reject at the primary constructor; negative zero becomes0.

Indexing also does not require an unbounded temporary. A successful second-label
prefix match implies all13 label characters exist, so separator+14<=value.length
and the substring index cannot wrap. Precision index increments remain at most
separator. Missing/short prefix checks fail before the dependent indexing. These
arguments assume normal String.indexOf/startsWith/substring contracts and required
allocation success, not an independently proved compiler/runtime implementation.

Malformed format, digits, over-limit magnitude or enum suffix produces
IllegalArgumentException via badFormat. A valid negative nonzero magnitude reaches
the primary IllegalArgumentException. The particular messages and absence of a
cause in current tests are D; their existing assertions are retained. Invalid
precision plus invalid mode/format has no newly imposed public priority rule.

### P: all canonical context strings roundtrip; U: noncanonical acceptance

For every supported pair `(P,R)`, toString prints the fixed first label followed
by canonical nonnegative ASCII Int text, one space, the second label and R's
exact name. The parser's digit recurrence evaluates that text to P without
exceeding Int.MAX_VALUE, and enum lookup returns R. Thus parse(toString(context))
has equal fields and equal hash; every canonical setting pair is covered by the
source argument, not an extrapolation from40 tested pairs.

The [MathContext String constructor](https://docs.oracle.com/en/java/javase/25/docs/api/java.base/java/math/MathContext.html#%3Cinit%3E(java.lang.String))
requires the format produced by toString, while the toString clause describes
precision as if formatted by Integer.toString. That canonical formatter produces
neither ASCII plus, extra leading zeros, negative zero nor non-ASCII digit text.
Our accepted language strictly includes such strings, for example
`precision=+1 roundingMode=UP`, `precision=0001 roundingMode=UP`,
`precision=-0 roundingMode=UP` and `precision=١ roundingMode=UP`.

This raises a precise acceptance-language interpretation question. If “same
format” requires the exact canonical image, these are counterexamples to required
malformed-input rejection. If it permits equivalent integer numeric tokens in
that field, their acceptance may be permitted. This memo does not choose that
interpretation or call an unexecuted example a new observed JDK failure.
The existing generator and fixtures intentionally record the broader pinned
language, including plus/zero/Unicode cases. JDK acceptance would reproduce an
observation, not alone settle the normative lemma. No assertion or input domain
is narrowed here. This is the specific remaining549/526 wording classification.

## RoundingMode: all eight constants and handwritten factory

The source declares the following exact order at RoundingMode.kt:4–5. BigDecimal's
legacy constants are0..7 in this same order at BigDecimal.kt:882–889.

| Constant / legacy code | Required rounding policy when an operation discards a nonzero fraction |
|---|---|
| UP /0 | Increase magnitude to the adjacent representable value away from zero. |
| DOWN /1 | Truncate toward zero. |
| CEILING /2 | Select toward positive infinity; increase magnitude only for positive values. |
| FLOOR /3 | Select toward negative infinity; increase magnitude only for negative values. |
| HALF_UP /4 | Select nearest; exact midpoint goes away from zero. |
| HALF_DOWN /5 | Select nearest; exact midpoint goes toward zero. |
| HALF_EVEN /6 | Select nearest; exact midpoint selects an even retained least-significant digit. |
| UNNECESSARY /7 | Exact operations succeed; an inexact result requiring rounding throws ArithmeticException. |

The enum file supplies policy tags; it performs no rounding by itself. The
accepted `docs/contextual-constructor-rounding-549-proof.md` supplies one actual
consumer composition over all signs, ties and modes. Full correctness of every
BigDecimal consumer is not proved by enum declaration or this ledger.

| Entry / own source | Contract, argument and evidence |
|---|---|
| Eight public enum constants, :5 | G: unique immutable enum values in exactly the listed declaration order, names and ordinals0..7. P: that order agrees with all eight legacy constants. No public enum constructor exists. E1/E3. |
| `Companion.valueOf(rm: Int)`, :7–11; JVM static bridge | P/G: `entries.getOrNull(rm)` returns the indexed enum for0<=rm<8; for every other Int it yields null and throws IllegalArgumentException. No rm arithmetic or narrowing occurs; Int.MIN_VALUE/MAX_VALUE are covered by the bounds predicate. E1/E3. The exact “Invalid rounding mode” text is D. |
| `Companion`, :8 | G: public holder for that one handwritten factory; not a public enum constructor or a new rounding policy. |

### Generated/inherited surfaces: conditional, not missing handwritten methods

| Surface | Required value/error behavior and limit of this source audit |
|---|---|
| `values()` | G: all eight entries in declaration order, fresh mutable array snapshots; mutating one does not alter entries or later arrays. E1 explicitly checks snapshot isolation. |
| `entries` | G: Kotlin ordered read-only EnumEntries; lists the same eight constants. It is a Kotlin addition, not a missing JDK method. No requirement to allocate a fresh entries object per read. |
| `valueOf(name: String)` | G: exact match against the eight declared names returns that enum; every other non-null string throws IllegalArgumentException. No trimming, normalization or case folding. Null at a real JVM entry requires NPE; normal Kotlin input is NN. E1/E3/E4. The all-name argument depends on Kotlin's generated enum lookup contract, not a handwritten implementation hidden in this file. |
| `name`, `ordinal`, default `toString()` | G: exact declared identifier, position0..7 and unchanged default name text. No enum-specific toString override exists. |
| `equals(Any?)`, `hashCode()` | G: enum identity equality; null/unrelated objects false, stable identity-compatible hash during an execution. No fixed cross-run/platform/JDK numeric hash. |
| `compareTo(RoundingMode)` | G: declaration-order comparison, including equality for the same entry. Exact negative/positive numeric magnitude is not separately promised. Null/wrong-type calls via erased Java/runtime routes depend on their language/runtime contracts and are not a new common nullable API. |
| JVM `getDeclaringClass`, `describeConstable`, Object monitor/getClass methods, protected clone/finalize machinery | Inherited JVM runtime behavior, not declared common math API. JVM Enum/Object contracts apply where available; KMP does not need Java reflection/monitor/Constable facades on every target. Public Java source shape only belongs in TCK adapters. Existing ApiSurfaceTest checks declared JDK math members and cannot by itself prove every inherited JVM/runtime contract. |

MathContext likewise inherits Any/Object runtime machinery beyond its three own
overrides; those platform contracts are not extra handwritten math algorithms.
The declared MathSerializable marker and private hooks have no additional public
math methods. Current-format state validation is retained; legacy binary forms,
serialVersionUID equality with java.math and cross-format streams are not revived.

## Existing source and actual execution evidence

**E1 common laws:** ValueIdentityContractTest.kt:8–24,236–293 checks context
field equality/hash, aliases, canonical text, mode distinctions even at precision0,
four constants, non-null negative precision, names/ordinals/legacy codes, invalid
name/int categories and array/list isolation. Five selected precisions×eight modes
are finite controls; the domain-wide claim comes from the preceding argument.

**E2 grammar:** MathContextGrammarTest.kt:6–51 checks the frozen compound corpus,
all BMP code units in three token positions, exact current error text/cause and
two long-token sizes. Jdk25ContextResults.kt identifies its public oracle generator;
`scripts/generate-context-oracle.main.kts:10–48` records its public JDK method calls,
ASCII-sign/leading-zero/Unicode cases and runtime pin. The generator was read,
not run. No corpus count substitutes for the grammar proof or resolves U.

**E3 JVM public comparisons:** ValueIdentityConformanceTest.kt:81–120 compares
context fields/text/equality/constants and enum names/ordinals/legacy mapping,
with selected invalid names/ints. It explicitly does not require numeric hash
equality across distinct enum classes. ApiSurfaceTest.kt:20–45 checks declarations,
constructors and fields; it does not prove all behavior or impose a permanent
public Java class/constructor requirement.

**E4 real null boundaries:** NullArgumentConformanceTest.kt:18–50,93–117,135–195
reflectively invokes actual constructors/methods, unwraps only target exceptions,
and distinguishes MathContext's otherwise-valid precision from negative+null.
It includes null String constructors and generated enum name lookup among public
entries, preserving host/port type comparisons where not owned by578.
NullPrecedenceOracleTest.kt:80–88 includes12 precision/null-mode keys,10 ordinary;
Validation578Boundary.java:24 and Validation578StandaloneTest.java:17–20,93 provide
actual Java calls and type assertions. Normalized VM messages and competing-invalid
priority are D, not absent exception-type requirements. No Kotlin `!!` or nullable
cast in this memo is presented as a library-boundary test.

**Existing accepted actual artifact:** source225 manifest
`/private/tmp/krogu-580-validation-t7wTW1Oz/evidence/source-before.sha256`
binds both unchanged small-type sources and the E1/E2/E3 sources to the accepted580
four-target campaign. Metadata read here confirms MathContextGrammarTest's ten
methods and ValueIdentityContractTest's eleven methods in JVM/JS/Wasm/macOS ARM64
audits with empty issue arrays. The JVM audit also contains both focused E3 methods.
Those class-wide counts include unrelated tests and are not newly attributed as
all-small-type proof. The accepted total1517 report is the parent evidence, not a
new run performed for this ledger. Exact-head hosted/platform gates for later
source remain separate; no JCK/TCK or all-runtime enum correctness is claimed.

## Remaining small-type lemmas, without invented test work

1. U: classify canonical-vs-equivalent-numeric MathContext String acceptance under
   the cited public wording; preserve current extended-language oracle evidence.
2. G: generated enum lookup, identity/hash, compare/order, snapshots and primitive
   Int/String/UTF-16 semantics remain compiler/runtime assumptions. Existing
   portable and JVM tests support selected instances; this is not a proof of
   every compiler lowering, platform null intrusion or monitor/Constable method.
3. Keep exact message/cause/compound-priority fields as retained observations
   unless a specific clause requires them. Do not waive explicit caller-created
   exception identity/message under the VM-message policy; these final small
   types themselves expose no user-overridable numerical callback seam.
4. Bind final publication/target acceptance to the actual adopted sources. No
   new test is proposed merely to mirror these field assignments, bound checks
   or generated language features. Full549's other types, complete error ledger,
   migration525/557, range548, dispatch541 and oracle526 remain open.

## Read-time provenance

| Input | SHA-256 |
|---|---|
| MathContext.kt | `d3690387b1345f90cc927044a1a450983dd0d2ce6e3e47054384c31979fd5261` |
| RoundingMode.kt | `e47d7aa314ddaa9c79ba13b79a00beadb8be96cbb473cd4ad2dfae3e0501a5ee` |
| ValueIdentityContractTest.kt | `d2770af18c2fdbe462aa27ebfb68888e1789757c1de208d634e632d1a4db9452` |
| MathContextGrammarTest.kt | `4bb8bf1704a1bf2d48595d7358b7f7f16f1793bfc86de08bf92cd4d672d0e038` |
| Jdk25ContextResults.kt | `c63daab7094c449959a094a6dbbac95cf6640872bc66d0134d2f8dc4b5913dce` |
| generate-context-oracle.main.kts | `9be1e07a48910ba7cc4c8c47137e8d56faf3a7461028db11e6d2b937ca198971` |
| ValueIdentityConformanceTest.kt | `fde54f7a9bf1dc8f936927fe1722ceee27b4ce24947c920712e20abac97109e3` |
| NullArgumentConformanceTest.kt | `272e5dc384dae7761041e1afe58c7c530bf7ce5d05c37579b9d9febe6a3348ea` |
| Validation578StandaloneTest.java | `a1807219e57f02e78eaca892e2d8d50a06efe976bdbc31c9639338bcc9d41e39` |
| Validation578Boundary.java | `b6d3e673282625d9cfac8f6ec2b216d943411e4e5d4bd38d714c9bbc40b6912c` |
| ApiSurfaceTest.kt | `4e190dc8879fdd0658e29fa9a99b662dc4dbb3629edd0f5d362c62d5c3be13a0` |
| docs/value-identity-contract.md | `782a051e9852d119225d8c6ec2104c669f64ee97c1d3355f91d37a83f4444cde` |
| docs/constructor-overload-inventory-549.md | `4d4b29dabe64335753c061085a721b08941f72b0c3bdf065a183e27d1f36b60e` |
| docs/constructor-contract-549-coverage.md | `2b758ac6be30096ae54bb676a4c1d98abe9dd9da1c76de2ebf8230560214c174` |
| docs/contextual-constructor-rounding-549-proof.md | `b801d4e0777d763b445c4f742ddccbcccc8f7245b62c807947c6d55d2525fce6` |
| private current549 task/feed JSON | `14f56f2e77b128146f4804a87cdd8bbe82b23c204cbbd39c842938210ed18e88` |
| accepted580 source225 manifest | `b2315fea5e93c1d3b0e28c88a33f6b1e78c942cc524b2e18f90023cca05bbb5a` |
| accepted580 JVM XML audit | `9c5a997ac94a6cb8ca4ba553b0c29759ad64f2cf14da15bc2c3abd1fc33b011c` |
| accepted580 JS XML audit | `3d79db959fcb8afdb2462c83e79cb4069c4f0e3d9c52ac59f41501921c811876` |
| accepted580 Wasm XML audit | `fa19f898e534a632e8d7b26f699078a54171edc456e60db03615e9b21561c6f5` |
| accepted580 Native XML audit | `fae30db7c5d336f30091f34b9c271ba84b6f345264555c2e5b145b2f07e6e370` |

Historical proof/source references retain their original snapshots. This memo's
read-time pins do not relabel old runs as new execution. Finite scope complete;
no future window request or background work.
