# Integer text range preflight: task559 source preparation

The [later parent validation](#parent-focused-validation) passed the selected
JVM, JS, Wasm and macOS ARM64 tests. Fresh hosted verification of the two
original timeouts remains required.

**Historical SOURCE ONLY / UNCOMPILED / UNRUN handoff.** This milestone changes only private
`BigInteger.parse`, one internal mathematical predicate in `MagnitudeLimits`,
one new common test file and this proof. No compiler, script, numerical helper,
certificate, test, build, probe or benchmark was executed. No Git or tracker
state was changed. Other workers' constructor documents/tests are outside this
ownership and are preserved.

## Actual failure and exact scope

The retained hosted run38762806007, attempt1, at
`870575e664c4276c2d6d7610632ce638a7c872f8` has complete=true,
officialJck=false and **10 PASS / 2 FAIL / 128 SKIP**: six host-baseline and
four port public invocations passed. Port `StringConstructorOverflow/run-1`
timed out after120061ms; port `SymmetricRangeTests/run-1` after180093ms.
Those failures remain failures, with unchanged120/180-second test limits.

The raw latter stdout reaches `Testing Bug 8021204`. Both pinned public TEST
sources construct the same hexadecimal shape: an initial `1`, then
`2^30-1` zero digits, then a final `1`. The significant digit count is therefore
`D=2^30+1`, not a smaller substituted fixture. No foreign numerical
implementation body was read. The actual raw results are retained under
`/private/tmp/krogu-870-large-ci-evidence-v1/artifacts/` in the
`public-math-large-evidence-870575e664c4276c2d6d7610632ce638a7c872f8-1/`
artifact, result1792079104822.

The original own parser multiplied the entire growing magnitude by the radix
and added each digit before eventually reaching its numerical range guard.
This source preparation predicts a different path for a valid text whose
length alone proves that every magnitude of its shape is unsupported: validate
the remaining digits and reject before constructing that magnitude. It does
not claim that the original timeout had no string-construction/GC contribution,
that the new run will meet its deadline, or that later SymmetricRange assertions
will pass. Root owns independent review and separately authorized execution.

## Source binding and preservation

| File | Before SHA-256 | Prepared SHA-256 |
| --- | --- | --- |
| `src/commonMain/kotlin/io/heapy/krogu/math/BigInteger.kt` | `df55cb8c8c6a291b59f6ee6daf365c9b4c38e4e1f33583e108183c5a28039fb3` | `8db4e0f6fef21ab6d0e467dd5465bae69ccc52dd47b2162d121a3db1ec1c6615` |
| `src/commonMain/kotlin/io/heapy/krogu/math/MagnitudeLimits.kt` | `3024002fc88ea3e90329905041eaa609fd0d2f2e08323a78e84c8640debff161` | `5bf687bb968d22908a36b8898f19649bdbdf7e5a2596575f1808053fca0e4769` |
| `src/commonTest/kotlin/io/heapy/krogu/math/IntegerParserRange559Test.kt` | new | `7384ebaf82a89afdbe9b659e8b7118b9a710c2dc999448a1bbc3c8edc42ba713` |

The only BigInteger edit is the guard block after sign/leading-zero handling
and before `var n = ZERO`. The original accumulator and every other BigInteger
method remain unchanged. Direct before/after text-region hashes are identical:

* File start through the private `parse` declaration:
  `fdc3b2aea1aee86f3458e1a951c1bf523a9a96ffe3fcabc52bc835abf8a18df9`.
* `invalidIntegerDigit` declaration through the end of the file:
  `a8e7c34867b422cba6632103ef02f98eebee683cb025e3926a1c8fc536a1a2a0`.

In particular, division, decimal prefix helpers, floating conversions,
modular-context code, primality, byte constructors and serialization are not
modified. The earlier [task543 conversion binding](floating-conversion-543-proof-binding.md)
retains its original whole-file source pin; this parser-only delta does not
retroactively change that historical hash or its evidence. Its conversion
method bodies are preserved by the first region check.

## Conservative mathematical lower bound

Assume the input String remains stable, the accepted UTF-16 character-to-digit
mapping is correct, primitive arithmetic/bit operations satisfy their contracts,
and required string traversal completes. No new assumptions about a foreign
parser implementation are used.

Existing code first validates radix `r` in2..36, checks nonempty input and
embedded ASCII signs, consumes at most one leading ASCII sign, and strips only
characters whose digit value in this radix is zero. The all-zero case still
returns canonical ZERO before the new predicate. Otherwise `start<length` and
`D=length-start` is in1..Int.MAX_VALUE. Neither the sign nor any leading zero
participates in this count. Zero padding, including accepted non-ASCII zero
digits, cannot create a false range rejection.

For valid remaining text, let its unsigned value be `n`. Its first digit is
at least one. Therefore `n>=r^(D-1)`. Put `k=floor(log2(r))`; for the accepted
radices, `1<=k<=5` and `r>=2^k`. Consequently

`n >= 2^(k*(D-1))`, so `bitLength(n) >= 1+k*(D-1)`.

The new internal `MagnitudeLimits.parserDefinitelyOverflows(D,r)` computes
exactly this lower bound. The expression `31-r.countLeadingZeroBits()` gives
`k` for the positive accepted Int radix. Multiplication occurs only after
converting the count to Long. At the largest permitted count and radix class,
the result is at most `1+5*(Int.MAX_VALUE-1)=10737418231`, far below Long's
maximum. No logarithm approximation, table of floating constants or wrapped
Int multiplication decides rejection.

The predicate is true only when that lower bound exceeds Int.MAX_VALUE.
The existing normalized representation permits at most that many magnitude
bits. Thus no supported valid number can enter the rejection branch. The
sign changes no magnitude bound, so this holds for both signs. Equality is
deliberately allowed. A false result is not a proof that the input fits; the
bound is conservative for non-power-of-two radices, and the original parser
and its actual numerical guards remain responsible for such inputs.

For the retained hex witness `r=16,k=4,D=2^30+1`, the bound is
`1+4*2^30=4294967297` bits, definitively above Int.MAX_VALUE. This proof uses
its symbolic count and does not allocate the huge String or integer.

## Digit validation and exception behavior

Length alone is insufficient to classify malformed text as a number. Therefore
a true bound first scans every remaining character using the **same**
`digitToIntOrNull(radix)` mapping as the original accumulator. On the first
invalid digit, it calls the unchanged `invalidIntegerDigit(value,radix,start,i)`.
Only an entirely valid remaining sequence reaches the new
`ArithmeticException("BigInteger overflow")`.

This scan does not change the grammar. It preserves radix validation,
empty/sign-only handling and the global embedded-sign check before the guard;
all-zero text still returns ZERO. It preserves the existing invalid-digit
diagnostic helper, its grouping inputs and first-invalid index. A supported
input follows the unchanged normal loop, so ordinary numerical values and
their exceptions are unaffected. For a malformed enormous text, NumberFormatException
is deliberately obtained before an arithmetic range rejection rather than
treating an invalid digit as evidence of a valid oversized value. This is the
source requirement for this change, not a new universal priority claim for
all combinations of null, malformed and unsupported inputs.

The scan's `start until length` indices are valid, with no `length+1`
construction. Leading-zero increments cannot exceed length. The unchanged
diagnostic group partition also fits: its first group has width in1..w and
subsequent groups have widthw, with every group ending at or before length.
Here `w<=30` because `r>=2` and the grouping radix power fits positive Int.
The Long product tested to compute w is below `Int.MAX_VALUE*36`, safely in
Long. Thus the diagnostic selects a bounded group instead of copying the
entire huge text.

The oversized-valid branch performs no BigInteger accumulation and no
magnitude-array allocation. It retains the existing sign scans/zero stripping
and adds one linear digit-validation pass with constant auxiliary state. It
does not remove the caller's String allocation, promise resource availability,
or bound elapsed time on any host. No artificial smaller domain, timeout
extension, input truncation, skipped character, OOM catch or numerical retry
is introduced.

## Prepared common tests and their limits

`IntegerParserRange559Test` is UNCOMPILED/UNRUN. Its four methods prepare:

* Literal last-nonrejected significant-count boundaries for every radix class:
  Int.MAX_VALUE for2..3;1073741824 for4..7;715827883 for8..15;
  536870912 for16..31;429496730 for32..36. Their immediate successors, where
  representable, must reject. Single-digit and Int.MAX_VALUE counts constrain
  off-by-one and Long-arithmetic behavior.
* The exact symbolic public hexadecimal count1073741825, without allocating
  its String. Binary and ternary maximum-count false results explicitly retain
  the guard's conservative meaning.
* Bounded ordinary values across all radices, both signs,96 ASCII zero digits
  and accepted Arabic-Indic/full-width zero padding; zero signs, signed Long
  endpoint text and an uppercase radix36 digit.
* Bounded malformed digits/UTF-16 units, embedded signs, empty/sign-only input
  and invalid radices, asserting NumberFormatException without new message or
  compound-invalid priority requirements. Existing parser/character oracle
  suites and their frozen generators remain unchanged.

These bounded String tests do not execute the new oversized public branch:
that branch requires a truly enormous count. The symbolic helper checks test
its production bound, and the source argument above supplies the branch's
validation/rejection reasoning. No configurable small maximum or synthetic
short-string production mode is added just to claim such a runtime test.

## Remaining acceptance

The source predicts that the known valid hex shape avoids per-digit
whole-magnitude arithmetic and reaches its required range exception after digit
validation. Fresh compilation, existing/new common parser suites, independent
review and a separately granted actual large-profile run remain root-owned.
The original two timeout witnesses remain failures until new evidence records
the exact candidate artifact and unchanged test scope.

This narrow change does not prove every oversized numeral is detected early,
every remaining SymmetricRange assertion, practical full-range allocation or
task548, all constructor549 diagnostics, all of559, full compatibility,
performance acceptance or official JCK. No future result count is assigned.

## Parent focused validation

After independent source/math review accepted the final guard, predicate and
category-only tests, root scope2296 ran one isolated four-target campaign. The
snapshot was commit `05109af11700d36514ef4cf92b45649e72d07e43` plus the two
production files and two new common test files. All 217 source files matched
the recorded manifest before/after execution and in the publication workspace.
The source-only proof above was preserved separately at its original SHA-256
`98ad2fa93b2c186c5eea3e2db027dce7b354f57bb48e10d1cbc11d4a32ae597c`.

With Gradle9.8.1, Kotlin2.4.21 and Liberica25.0.4+9, session59332 terminated
exit0: `BUILD SUCCESSFUL in 6m 2s`, 49 tasks executed. Each of `jvmTest`,
`jsNodeTest`, `wasmJsNodeTest` and `macosArm64Test` selected these same four
suite filters, repeated after each target:

```text
--tests '*IntegerParserRange559Test*'
--tests '*ConstructorRandom549Test*'
--tests '*IntegerParserOracleTest*'
--tests '*CharacterOracleTest*'
```

The command also requested `jvmJar`, used `--no-daemon --max-workers=1
--stacktrace` and a fresh temporary Kotlin tooling home. It retained the
existing 4GiB in-process compiler heap and test timeouts. No giant input or
public harness was executed locally, and no retry was needed.

Exact XML inspection checked all 16 suite files, each expected method name,
suite/testcase counts and absence of duplicate, failure, error or skip records:

| Target | Suites | Methods | New methods | Failures/errors/skips |
| --- | ---: | ---: | ---: | --- |
| JVM | 4 | 15 | 7 | 0/0/0 |
| JS / Node | 4 | 15 | 7 | 0/0/0 |
| Wasm / Node | 4 | 15 | 7 | 0/0/0 |
| macOS ARM64 | 4 | 15 | 7 | 0/0/0 |

The total is 60 method executions, including 28 executions of the four parser
and three constructor controls. Existing BMP/radix and parser fixtures remained
unchanged. This is a focused selection, not a complete project-suite result.

Evidence is `/private/tmp/krogu-parser559-validation-v1/evidence/`: the complete
Gradle log, exact XML and method audit, source freeze/manifests and fresh own JAR.

```text
a449803a1b694042460da08e299d63de1caf1e20f43ef1c00b93d0c70f0b489b  source-before.sha256
3218538607b24caa929ca0faba2a264edf8409eb5c7b7c9120b9711273866b83  four-target.log
5f17c70e64431a8569478b2e73f2f69e2a4141e2a3973e4ac35973e72e1aef75  xml-audit.json
4f7337871a3b32761cc6c25e0dc36a92f56ef759723cb0baf1dd699430ab0ed0  artifacts/own.jar
```

All owned compiler/test calls terminated and the local execution window was
explicitly released. Matching-head supported-platform CI and a new original
large-profile run remain separate gates; the old two timeouts are unchanged.
