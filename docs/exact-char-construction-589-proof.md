# 589 exact CharArray construction without a whole String

SOURCE ONLY: UNCOMPILED/UNRUN. This candidate changes one exact constructor
delegation and removes its now-unused private String adapter. It reuses the
accepted588 lexer/export unchanged; no new numerical algorithm, precision cap,
public signature, fields, cache, serialization form or dependency is added.

## Exact provenance and scope

The input is the frozen private587/588 composition at
`/private/tmp/krogu-587-588-validation-BElCXtoQ/root`, source244 manifest
`17fefd8a0a95ab87e5fb9b9fd2dbec25e0d292c45afda739cc0f3727da7dd0a5`.
All244 input and copied source hashes/pathsets passed before edits. This is
not a main-source adoption: parent-labelled main0854 and all other workers'
roots/evidence remain untouched. There is no .git directory in this copy.

The read-only pre-edit archive is ../originals/source244-baseline.tar,
SHA256 `d82ff3335f0b767dbb93c268202f943b31d2d41a6ed7729c573c517a6aa3f9ce`.
It excludes build/.gradle/.git/.kotlin. The original BigDecimal is
`0ef73b1c3ee899ceaf2305d093475f1cef9fb7610908ecf34987cd98b8153195`.
DecimalText588 remains
`e70e9ff7488caab985056cbfcf349048301cd3700dbdcfff235d987846407d7c`;
its accepted proof remains
`045fe9d39180e945c418b34ae1d3d8d7be3fec8a2e4b11cdfe6b0f3a0ae328a2`.
The initial failed source244 campaign and complete433-file capsule remain
unchanged; its copied capsule index is
`1c8ac095db44e230073d3fa553a24e11233a16fd50dce2706b68b7f4edcc6ae6`.

The exact slice constructor now delegates to
`DecimalText588.parse(value, offset, len, MathContext.UNLIMITED)`. The exact
whole-array constructor still delegates to that slice with offset0/size.
The old private characters adapter had exactly one caller, this slice
constructor, and is removed. Every String, contextual-char and numerical
constructor, helper, raw fixture, consumer and previous assertion is unchanged.

## Required domain and the removed obstruction

For an available valid CharArray slice, the previous route necessarily
created a whole selected String before parsing. On a platform where the
String type has a smaller usable length than an available CharArray, this can
reject an input with a tiny required exact coefficient solely because of an
unnecessary intermediate type. An arbitrarily long valid leading-zero prefix
is the relevant family. This is a conditional platform/type/resource argument,
not a claim of a particular universal String maximum or a giant runtime/JDK
observation. Source244's588 proof explicitly left this exact/no-context route
outside its repair.

The new route copies only the selected characters into an owned CharArray.
Its length is no greater than the supplied available array. It introduces no
intermediate String type limit. Additional array/scratch allocations can still
fail under resource548; available memory and normal primitive/toolchain behavior
remain premises. No fixed-heap, timing, forced-lifetime or OOM translation is
claimed. Stable input during copying is assumed; atomic copying during
concurrent mutation is not promised.

## Specialization of the accepted588 proof to p0

MathContext.UNLIMITED has precision0; its rounding mode is irrelevant here.
The unchanged range check rejects negative offset/length before calculating
`size-length`, and accepts only `0<=offset<=offset+length<=size`. Subtraction
and accepted addition are Int-safe. Validation precedes copying, so an invalid
range remains NumberFormatException rather than a copy's bounds exception.

The SAME owned reader is scanned and then consumed for coefficient assembly.
The full588 lexical proof applies: optional ASCII sign, decimal digits with
at most one ASCII point and at least one digit, optional e/E with optional
sign and nonempty digits, then an Int-range lexical scale. It uses the SAME
Char.digitToIntOrNull(10) predicate as the old parser, including accepted BMP
digits. All selected characters and the exponent are validated before numeric
publication. No malformed suffix after significant digits is ignored.

If f is fractional digit count, the exponent accumulator before each step is
at most `Int.MAX_VALUE+1+f<=2^32-1`; its next multiply/add is below2^36 and
Long-safe. Negation and `S=f-exponent` are Long-safe, and invalid initial S
throws NumberFormatException. Zero does not rescue an invalid lexical scale.

Let D count digits beginning with the first nonzero, including trailing zeros.
For an all-zero mantissa D0, the helper returns coefficient0 at lexical S,
including negative-zero input and Int endpoint scales; public precision is1.
Otherwise the production scalar classifier at p0 returns k=D, so drop=D-k=0.
The scale calculation is S-0=S, already proved Int. Neither the inexactness
branch nor increment can run. A correctly assembled D-digit prefix cannot
enter the carry-division branch. Thus the published result is exactly C@S:
no rounding, optional zero strip, preferred-cohort substitution or endpoint
clamp. For example 0001200.00 requires120000@2, not contextual120@-1.

Existing L=Int.MAX_VALUE/H=646456993 certificate premises apply unchanged.
D>H necessarily needs coefficient>=10^H>2^L-1 and is unsupported at its
required exact cohort. D<=H alone is not sufficient; actual final bit length
is checked. D0 and leading-zero lexical length never acquire a magnitude cap.
The decimal grouping/export/capacity proof is exactly588's: active zero
chunks remain represented; all final-prefix binary partial values are bounded
by C; public range rejection therefore concerns required C, not an oversized
discardable temporary. Final sign/magnitude bytes pass through the unchanged
own BigInteger constructor. There is no whole-mantissa StringBuilder, full
input String, 10^drop, new alias or shared scratch in this route.

## Existing contracts and diagnostic inventory

|Existing evidence|Required behavior retained; limit|
|---|---|
|ValueIdentityContractTest mutable-input constructor loop|Exact whole/slice values, scale, hash/map stability and caller mutation; earlier expected values use the String constructor|
|DecimalConformanceTest.byteAndCharacterConstructorRanges|Finite exact char text/range versus JDK exception class; outcome omits messages|
|CharacterOracleTest and constructor-contract-549-coverage.md|BMP digit mapping, selected supplementary behavior and lexical/range source argument; not an all-string finite test claim|
|DecimalTextRounding588Test|The SAME helper at p0 and owned contextual-char route, partial groups/zeros/Unicode/errors/scale; historical initial source244 JVM method PASS only|
|NullArgumentConformanceTest / NN policy|Otherwise valid null object arguments still reject; exact signature remains non-null, with no new public context parameter|
|NullPrecedenceOracleTest / Validation578StandaloneTest|Contextual null/range diagnostic partitions are unchanged because contextual constructors are untouched|

The lexer retains all selected ordinary old NumberFormatException messages:
Bad character range, Empty BigDecimal, Invalid decimal digit, No digits,
No exponent digits, Invalid exponent, Exponent overflow, Scale out of range.
The exact p0 route introduces no Rounding necessary or rounded-scale exception.
Pure-null char rejection remains NPE under the accepted non-null API policy;
VM-generated wording/check placement is not asserted. No nullable migration,
legacy serialization, Java constructor replacement or callback-order promise
is introduced.

For an unsupported giant exact coefficient, earlier String capacity/allocation
or internal arithmetic failure can have preempted later validation/export.
This change deliberately removes that String obstruction. Existing588 proof's
unobserved giant diagnostic message/cause/resource-priority limits still apply;
they are not a blanket waiver of library-defined errors. No known bounded
required-record conflict was found. Existing fixtures/assertions remain active;
if actual retained records expose a required conflict, parent526 must classify
that concrete case before acceptance rather than normalize or suppress it.

No caller-provided numerical subtype participates in character decoding.
Constructing Kotlin subclasses still receive the exact stored coefficient and
scale through the existing private copy route. Explicit caller override/super
contracts remain required; incidental constructor-internal callbacks and object
identity do not become new guarantees. The new subclass control checks exact
construction followed by real super.round arithmetic, not a sentinel selftest.

## Prepared tests and outstanding gates

DecimalExactChars589Test has four independent literal-based common methods:
exact whole/slice coefficient/scale/precision rows (including19/30-digit
multiword values, Unicode, signed zero and Int scale endpoints); an actual
16,384-leading-zero char input with tiny exact coefficient and postconstruction
caller mutation; full malformed selected texts and overflow-shaped Int ranges;
and whole/slice super construction followed by independently specified rounding.
The invalid characters outside a selected slice are ignored, as required.
Expected fields do not use the own/JDK decimal constructor as an oracle.

The leading-zero test is bounded and exercises the real new public route;
it does not lower a production cap or pretend to exceed a platform String
ceiling. Giant input/type-limit cases remain symbolic. No existing test,
consumer, fixture or expectation is modified. All four new methods are
UNCOMPILED/UNRUN. Root owns independent source review, eventual composition,
initial compilation/testing, retained-record classification and fresh artifact/
hosted gates. Source244's original16 sqrt failures and incomplete/unclassified
tails remain failed historical evidence;589 neither repairs nor waives them.
This is not whole549/548/541/526 or project closure.
