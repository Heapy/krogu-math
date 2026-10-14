# Returned decimal representation contract controls

This `local:526` addition transfers a specific public obligation from the frozen
context-dispatch results: a valid decimal representation has a specified unscaled
coefficient, scale and canonical `toString()` spelling. It does not require
context division on poisoned operands to select any particular result.

The [Java SE 25 BigDecimal public API](https://docs.oracle.com/en/java/javase/25/docs/api/java.base/java/math/BigDecimal.html#toString())
defines canonical spelling from coefficient digits and scale. The adjusted
exponent is the coefficient's decimal digit count minus one minus scale.
Nonnegative scale and adjusted exponent at least -6 select plain decimal
placement; otherwise scientific notation uses a signed exponent. Zero has one
coefficient digit. Trailing coefficient zeros and scale are preserved. Exponent
arithmetic uses `Long`, including when the scale is `Int.MIN_VALUE`.

## Complete extraction and independent expectations

`scripts/extract-decimal-context-returned-representations.main.kts` binds the old
input fixture to SHA-256
`88fc3c5bc8277a048a83a9ad9b6546bd8a23ae1ad9a10126409d857367391026`, and the ordinary/truthful
control fixture to SHA-256
`1245d75ed6837139c939496e602a085cba37b35dd4d3c98f197bdaece2ce7cc9`.
It parses every frozen row and rejects changed hashes, unexpected schemas,
unparsed successes, scale disagreements and canonical-spelling disagreements.

| Evidence | Count |
|---|---:|
| Frozen input rows | 75,728 |
| Successful rows, each parsed and checked | 58,093 |
| Error rows retained in the frozen source | 17,635 |
| Distinct successful coefficient/scale pairs | 675 |
| Distinct successful ordinary/truthful control pairs | 334 |
| Additional returned pairs absent from those controls | 341 |
| Unparsed successes or scale/spelling disagreements | 0 |

The extractor parses decimal digits, fractional length and exponent itself.
For example, `1.2300E+5` supplies coefficient `12300` and scale `4-5=-1`.
Leading coefficient zeros are removed; trailing zeros are retained; a zero
coefficient is normalized to `0`. No JDK or krogu numerical constructor,
formatter, arithmetic implementation or division supplies these expectations.
Nineteen independent literal witnesses challenge the parser and formatter,
including both sides of the -6 threshold, zero, signs, trailing zeros and both
Int scale endpoints.

The generated `DecimalContextReturnedRepresentationData.kt` stores coefficient,
scale, independently expected canonical spelling, separately preserved observed
spelling, first old row ID, occurrence count and ordinary-control membership.
Pairs follow first occurrence order. The occurrence counts sum to all 58,093
successes. Every ordinary-control pair is present in the returned-pair set.
`D` IDs retain the old fixture's one-based row numbering; the exact frozen source
retains all original inputs, outcomes and subsequent IDs.

| First old ID | Representation | Observed canonical spelling | Interaction |
|---|---|---|---|
| D00001 | `700@2` | `7.00` | Unlimited `0.00/0.0` enters the replacing exact override; the truthful mathematical control throws `ArithmeticException` |
| D09801 | `1@-3` | `1E+3` | Precision-one UP division of `10.50/2.0` with divisor precision replaced by -1 |
| D09802 | `0@-3` | `0E+3` | The same poisoned precision input under DOWN |

These examples are representations to construct directly. Their inclusion does
not endorse the poisoned numerical result or require inherited exact-overload
forwarding.

## Common contract controls

`DecimalContextReturnedRepresentationContractTest` constructs all 675 pairs
using `BigDecimal(BigInteger(coefficient), scale)`, with both an ordinary decimal
and a subclass whose coefficient, scale, precision, sign and text observers fully
delegate. For each constructed value it asserts unscaled coefficient, coefficient
text, scale and independently expected canonical text. This gives 1,350 directly
constructed representation controls, including both forms of every additional
pair. Division, poisoned metadata, callback counts/order and result selection are
never exercised by this test.

The test also derives expected spelling directly from digits and scale and checks
both generated expected spelling and separately retained observation against it.
Its nineteen literal witnesses additionally check the formatter and both directly
constructed classes, adding 38 literal representation controls. For example,
`1@Int.MIN_VALUE` spells `1E+2147483648`, and `0@Int.MAX_VALUE` spells
`0E-2147483647`; neither requires allocating a huge plain-decimal string.

Formal coverage statement: every successful old row maps to exactly one of the
675 distinct pairs by the independently parsed coefficient and scale. Each pair
has a direct ordinary and truthful-subclass representation control. This preserves
the canonical-spelling/representation evidence for every old successful prefix,
while leaving the poisoned invocation's success, selected numerical value and
selected scale classification separate. The old input-mathematics projection and
explicit caller/super controls remain in `DecimalContextContractTest`.

## Null guards and poisoned interactions remain separate

The public null-input clause and documented arithmetic errors remain obligations
for inherited context division on ordinary/truthful inputs. The existing input
controls preserve those obligations. This extraction neither reclassifies nor
weakens them. A result-format control is not a null/error-precedence control.

For example D07457 records `7.00:2|A.exact-null;` for an unlimited context with
a null divisor. Its ordinary/truthful control requires `NullPointerException`.
The replacing exact override, the choice to enter it internally and the inherited
method's null obligation require their own interaction analysis. The replacement
alone proves neither a JDK defect nor a contractual inherited-forwarding rule.
Direct caller override entry/arguments and caller-owned `super` selection also
remain language obligations, with controls in the existing contract suite.

The 75,728 old whole-string assertions, frozen fixtures/generators, correspondence
mapper and unresolved statuses remain unchanged. This addition authorizes no
archival, disabling or blanket exemption of subclass behavior. It does not finish
`local:526` or prove full compatibility.

## Reproduction and verification limits

From the repository root with Kotlin 2.4.21, generate the owned data file:

```sh
JAVA_OPTS='-Xmx256m -Dkotlin.main.kts.compiled.scripts.cache.dir=/private/tmp/krogu526-representation-cache' \
/Users/yoda/.sdkman/candidates/kotlin/2.4.21/bin/kotlin \
scripts/extract-decimal-context-returned-representations.main.kts
```

Verify byte-identical reproduction without writing the data file:

```sh
JAVA_OPTS='-Xmx256m -Dkotlin.main.kts.compiled.scripts.cache.dir=/private/tmp/krogu526-representation-cache' \
/Users/yoda/.sdkman/candidates/kotlin/2.4.21/bin/kotlin \
scripts/extract-decimal-context-returned-representations.main.kts --check
```

Both commands passed at creation. They checked both bound hashes, complete row
parsing, all counts, ordinary-pair inclusion, 19 literal parser/formatter witnesses
and zero observed spelling/scale disagreements. Data SHA-256:
`d1aa47c77d9987b1dea1057da98e8c9fa2af12aed13b228e969fd9464c8aa56d`.

The extractor checks use at most a 256 MiB heap and execute no numerical
implementation. CI also runs its `--check` mode to enforce byte-identical data
reproduction and both frozen input hashes.

The two common test methods subsequently passed on JVM, JS/Node, Wasm/Node and
macOS ARM64 using Gradle 9.8.1, Kotlin 2.4.21 and Liberica 25.0.4+9-LTS. Each
target's separate run produced exactly one XML suite with both required methods,
zero failures/errors/skips, 1,350 ordinary/truthful pair controls and 38 literal
controls. These ran in task562's isolated checkout after its main campaign;
both borrowed source files were copied byte-for-byte with their supplied hashes.
Parent independently parsed the archived XML and checked the exact method names.

These focused platform results preserve returned-representation obligations.
They neither resolve poisoned invocation result selection nor finish task526.
Full supported-platform CI at the integrated head remains a separate gate.
