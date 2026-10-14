# Plain decimal string length preflight

Task `local:564` follows up the two public `ToPlainStringTests` failures from
task559. The old implementation reported ArithmeticException for nine of the
14 impossible layouts and reached allocation failure for five. The pinned JDK
rejects all 14 with OutOfMemoryError. This is adopted JVM compatibility behavior,
not a claim that toPlainString specifies an allocation-error type or that the
JDK behavior is a defect.

## Length and dispatch argument

For scale s and d unsigned coefficient digits, nonnegative-scale layout length
before an optional minus is d when s=0, d+1 when 0<s<d, and s+2 when s>=d.
Add one for a negative layout sign. Compute all arithmetic in Long and reject
only lengths greater than Int.MAX_VALUE before padding or concatenation.
Accepted lengths remain subject to actual platform string and heap capacity.

Negative-scale toPlainString retains its original virtual signum zero shortcut
and exactly one coefficient toString call. For returned signed text length l,
the result needs `l.toLong() - s.toLong()` characters. This avoids a new
coefficient signum callback. Scale-zero returns coefficient text directly;
positive-scale layout retains its existing sign dispatch. No serialization
mechanism or arithmetic kernel changes.

The private helper's conservative maximum, with Int.MAX_VALUE digits, a minus
and Int.MIN_VALUE scale, is 4,294,967,296, safely within Long. The actual negative
branch starts with signed text length bounded by Int.MAX_VALUE, so its maximum
is 4,294,967,295. Passing the guard also proves the padding count fits Int before
narrowing. The internal bridge throws JVM OutOfMemoryError with diagnostic
`too large to fit in a String`; JS, Wasm and Native use common Error with the
same diagnostic. Their exception class is not asserted to be JVM-equivalent.
No real Native heap-exhaustion probe is needed.

## Executed evidence

With Gradle 9.8.1, Kotlin 2.4.21 and Liberica 25.0.4+9-LTS:

| Target | XML suites | Test methods | Failures/errors/skips |
| --- | ---: | ---: | --- |
| JVM | 11 | 70 | 0/0/0 |
| JS / Node | 4 | 51 | 0/0/0 |
| Wasm / Node | 4 | 51 | 0/0/0 |
| macOS ARM64 | 4 | 51 | 0/0/0 |

Parent independently parsed and archived the XML, matching every method against
its selected source. All five new methods ran on JVM; the three common methods
ran on every target. They cover all 14 impossible layouts, ordinary/sign/zero
cohorts, inherited and caller-owned dispatch, and private length boundaries
without allocating accepted giant strings. Retained portable, context,
canonical and JVM formatting/conformance tests passed.

The bounded certificate checked 2,376 independently counted small layouts,
14 witnesses, 12 symbolic boundaries and the Long bound. Its revised signed-text
checks added 1,152 small negative layouts, four witnesses and six boundaries.
These static checks are distinct from the runtime tests.

Both original public ToPlainStringTests directives passed in baseline and port,
four actual invocations and no failures, in run `1792016503091`. The test source
is pinned to OpenJDK commit `7b65d74bcbd7c3a0b4c1f5c5bd85e64aa2e51b4a`.
Port JAR SHA-256:
`7cd58bea3f9f6c9d3ca25db038eb7c0d7f423d5a135cbac12667ab8dbe9635de`.
Result JSON SHA-256:
`b9ad0b67e20f9e4061571c59bf7fbff38f34b7875494dd32f05c8b90c4c6b4b8`.
Class-origin and bytecode audits verified own core classes and no host math
members in the adapted test. This pre-borrowed isolated JAR contains baseline859
plus the formatter fix; it does not contain later binary562 or root525 additions.
Integration preserves the tested formatter section and five new files while
retaining the binary fix. Fresh integrated-head CI remains required.

## Retained history and limits

The initial JS XML audit rejected target-qualified class names; correcting that
exact prefix normalization fixed the audit without changing or rerunning tests.
The first harness attempt hit the sandbox's descendant-process enumeration
restriction before javac/test execution. It was preserved, then the authorized
command passed with process-tracking access.

Sources, manifests, reviews and certificate remain in the isolated task564
`tools/plain-preflight-564/` handoff; raw XML, commands, pre-borrowed JAR and
harness records are under its `build/plain-preflight-564/execution/` directory.
Original559 failure records remain separate. Other public-test timeouts and
capacity skips are not reclassified. No broad public rerun or official JCK/TCK
pass is claimed.
