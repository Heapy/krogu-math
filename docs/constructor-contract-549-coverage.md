# Constructor contract 549: source coverage map

This memo records source and test evidence for integer and decimal constructor validation. It is a coverage map, not a claim that every possible call site or diagnostic has been proved. The repository semantic index was unavailable; evidence below comes from direct reads of the named files and public JDK test sources.

| Family | Evidence | Coverage and limits |
|---|---|---|
| Integer text grammar and radix | `src/commonMain/kotlin/io/heapy/krogu/math/BigInteger.kt:31-32,1055-1085`; `scripts/generate-integer-parser-oracle.main.kts:9-37`; `src/commonTest/kotlin/io/heapy/krogu/math/IntegerParserOracleTest.kt:8-16` | Radices outside 2..36, empty/sign-only input, embedded signs, leading zeros, invalid-digit group boundaries, and all valid radices are represented by generated JDK25 cases. Grammar acceptance/rejection and documented exception types are semantic requirements; offending substrings, radix suffixes and compound-invalid ordering are observations unless separately guaranteed. |
| Integer Unicode/radix characters | `scripts/generate-character-oracle.main.kts:10-57`; `src/commonTest/kotlin/io/heapy/krogu/math/CharacterOracleTest.kt:8-65` | All 65,536 BMP code units are checked against `digitToIntOrNull` for radices 2..36; BMP and accepted supplementary UTF-16 constructor behavior are replayed for `BigInteger(String, 36)`. This is exhaustive single-character digit coverage, not exhaustive all-string grammar. |
| Integer byte and sign/magnitude constructors | `scripts/generate-integer-bytes-oracle.main.kts:9-39`; `src/commonMain/kotlin/io/heapy/krogu/math/BigInteger.kt:1087-1137`; `src/commonTest/kotlin/io/heapy/krogu/math/IntegerByteOracleTest.kt:8-33` | Generated inputs include empty arrays/slices, `FF 00`, all-zero/all-FF forms, signum values outside -1..1, negative and overflow-shaped offsets/lengths, and boundary slices. The test also mutates input and returned arrays, proving isolation. Exact exception messages remain diagnostic observations. |
| Decimal string grammar | `src/commonMain/kotlin/io/heapy/krogu/math/BigDecimal.kt:32-33,874-906`; `build/math-tck/upstream/test/jdk/java/math/BigDecimal/StringConstructor.java:41-168` | Public TCK source covers malformed signs/points/exponents, scale limits, abnormal exponents, round trips, and Arabic-Indic zero. The finite malformed corpus is not universal proof of every possible string. |
| Decimal Unicode digits | `scripts/generate-character-oracle.main.kts:18-20,36-50`; `src/commonTest/kotlin/io/heapy/krogu/math/CharacterOracleTest.kt:22-39,53-65`; `build/math-tck/upstream/test/jdk/java/math/BigDecimal/StringConstructor.java:123-168` | BMP single-character decimal acceptance and supplementary UTF-16 behavior are covered, plus Arabic-Indic zero examples. BMP exhaustive digit mapping does not prove arbitrary multi-character decimal grammar. |
| Decimal char-array slices and aliasing | `src/commonMain/kotlin/io/heapy/krogu/math/BigDecimal.kt:34-37,908-910`; `src/commonTest/kotlin/io/heapy/krogu/math/ValueIdentityContractTest.kt:136-153`; `src/commonTest/kotlin/io/heapy/krogu/math/ZeroContextScale567Test.kt:155-165`; `src/jvmTest/kotlin/io/heapy/krogu/math/DecimalConformanceTest.kt:82-94` | Valid wrapped slices, with and without `MathContext`, contextual zero scales, source mutation, malformed selected text, and finite nearby ranges are covered. Four adapted public char-range tests use `UNLIMITED`; the original four null+invalid cases are explicitly lost from this adapted coverage. |
| Decimal char-array range checks | `src/commonMain/kotlin/io/heapy/krogu/math/BigDecimal.kt:908-910`; `build/math-tck/upstream/test/jdk/java/math/BigDecimal/Constructor.java:39-71`; `src/jvmTest/kotlin/io/heapy/krogu/math/DecimalConformanceTest.kt:89-93` | Negative offset/length, ordinary out-of-bounds, and an `Integer.MAX_VALUE` overflow-shaped offset are represented. All four public methods expect `NumberFormatException`; only the overflow method additionally rejects an `IndexOutOfBoundsException` cause. This is range validation evidence; required numerical range 548 is separate. |
| Null plus invalid char slices | `src/jvmTest/kotlin/io/heapy/krogu/math/NullPrecedenceOracleTest.kt:64-70,83-94` | Frozen black-box rows exercise null `MathContext` together with valid and invalid slice/range combinations. This is the project’s non-null policy and preserved diagnostic observation, not a JDK-normative NPE priority. The old joint-invalid order must not be treated as portable constructor grammar. |
| JVM constructor differential campaign | `src/jvmTest/kotlin/io/heapy/krogu/math/DecimalConformanceTest.kt:82-94` | Integer byte constructors and decimal char constructors are compared with public JDK calls; exception classes are retained. `outcome` discards exception messages, so it does not prove wording or message precedence. |

## Bounded remaining work for 549

The evidence does not justify saying “constructor coverage complete.” The remaining proof obligation is complete call-site coverage and exception classification outside the finite families above: every public constructor overload and every validation path still needs to be accounted for, with common-platform and JVM-only evidence distinguished. Existing generated or TCK rows prove only their enumerated domains.

Diagnostic messages are observations, not mathematical or grammar requirements. In particular, `NumberFormatException` wording, offending substring grouping, wrapped-cause absence, and compound-invalid ordering should be reported as compatibility observations unless a separate project policy explicitly freezes them.

The four adapted public char-range methods check invalid ranges with a non-null context. Valid slices, context parity and immutability are covered separately by `ValueIdentityContractTest` and `ZeroContextScale567Test`; `DecimalConformanceTest` adds finite selected-text/range comparisons. The original four null-plus-invalid calls remain in the preserved failed public campaign and explicit companion SKIPs, not in the adapted PASS. `NullPrecedenceOracleTest` is a separate finite matrix with different inputs: it requires NPE for otherwise valid arguments and rejection for competing-invalid arguments. It does not reproduce those four exact original calls or impose their old exception priority.

Historical task-body counts are intentionally not repeated as fresh row counts. Pending CI or runtime claims are outside this source-only memo. Range 548 remains a separate concern.

## Conditional lexical and scale argument

The following argument concerns the current own parsers at the references above,
with BigInteger source SHA-256
`df55cb8c8c6a291b59f6ee6daf365c9b4c38e4e1f33583e108183c5a28039fb3`
and BigDecimal source SHA-256
`08b4329ea5e902f1043107d42ddb02592be537497f6b0990927e8c67c4ed3f29`.
It assumes the primitive UTF-16 digit mapping and supported exact integer
operations satisfy their contracts, and that required allocations complete.
It proves neither the full numerical range in 548 nor every constructor's
rounding, dispatch, null boundary or diagnostic behavior.

For integer strings, the radix guard rejects precisely radices outside 2..36.
The empty/sign-only checks require at least one digit after the optional ASCII
sign. An embedded ASCII sign cannot be a digit in any permitted radix, so its
early rejection changes only diagnostic priority. Skipping leading zero digits
does not change the value. The remaining loop accepts exactly digit sequences
in the requested radix: after each accepted digit d, its accumulator is
`previous * radix + d`. Induction gives the unsigned positional value, and the
last sign application gives the signed value, including normalized zero.

For decimal strings, the mantissa scan consumes the optional ASCII sign and
then digits with at most one decimal point, stopping only at ASCII e/E or the
end. A second point or any other non-digit is rejected; the nonempty digit
buffer requires a digit on at least one side of the optional point. The exponent
branch consumes its indicator, optional ASCII sign and one or more decimal
digits, rejecting all extra characters. Thus the accepted lexical forms match
the [Java25 public string grammar](https://docs.oracle.com/en/java/javase/25/docs/api/java.base/java/math/BigDecimal.html#%3Cinit%3E(java.lang.String)).
The digit buffer is the significand without its point, so its signed integer
value and scale `fractionDigits - signedExponent` represent exactly that text.

Let f be the number of fractional UTF-16 digits and B = 2^31 + f. String length
is Int-bounded, so 0 <= f <= 2^31-1 and B <= 2^32-1. Before every exponent-digit
step its nonnegative accumulator is at most B. The next multiply/add is at most
`10*B + 9 < 2^36`, safely within Long, and a larger-than-B result is rejected
immediately. Such rejection cannot discard a representable scale: a positive
exponent greater than B gives scale below -2^31; a negative exponent of that
magnitude gives scale above 2^31-1. Remaining exponents are negated and
subtracted in Long; the final explicit Int-range check is therefore exact.
Leading exponent zeros do not change this invariant. Java25 constrains the
resulting scale, rather than requiring the written exponent itself to fit Int.

For char-array slices, after rejecting negative offset/length, the check
`offset > size - len` avoids addition overflow. If len exceeds size the
right-hand side is negative and every nonnegative offset is rejected. Otherwise
the accepted bounds imply `0 <= offset <= offset+len <= size <= Int.MAX_VALUE`.
The copied substring is exactly the selected UTF-16 sequence and the same parser
then applies. The context overloads additionally require their separate rounding
and explicit-dispatch arguments; lexical equivalence alone does not prove them.
