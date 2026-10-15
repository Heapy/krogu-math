# Task 567 V2 finite source proof

**SOURCE ONLY / UNCOMPILED / UNRUN. Changes are not committed.**
Task `local:567` stays linked/in_progress. Worktree
`/private/tmp/krogu-math-task-567`, branch `task/567-zero-context-scale`,
HEAD `bbdd2f4a7c3d3729f2597205b794f2853cc28e67`.

This document supplements the unchanged V1 proof/handoff. V1's unary cohort
choice and unchanged sqrt/Double callers describe V1 only; the decisions below
supersede them for V2. Only own source, retained raw observations and supplied
public API contract evidence were used. No foreign numerical implementation
bodies, web search, compilation, Kotlin/Java scripts, helpers, probes, numerical
execution, tests, generators or benchmarks were used in this source milestone.

## Preservation and evidence boundary

Before edits, the four exact V1 artifacts were copied to unique read-only
`/private/tmp/krogu-567-v2-original-9146edda/`:

| Original file | SHA-256 |
| --- | --- |
| BigDecimal.kt | `7214a0fda8a4ae8a6836829c2bf74fd9dfeb671e01b41f8e7ada33732b9b68d1` |
| ZeroContextScale567Test.kt | `736472d71a2b216b1e367fbc220ec648dd11f2069bc63a19f86102cb4483bc59` |
| zero-context-scale-567-proof.md | `5054a2a9110d41c8ad4f8226c1967ee9b67da5f23eb277e40166d2351cd1c4ac` |
| zero-context-scale-567-handoff.md | `ab6f38b9701758f23df41655127071ad540ae67c321dc227892a69073f19ba76` |

The prior worker `9974b07a` is stopped/archived. Its retained task2009 handoff,
release evidence and empty post-release mutex list were read before edits.
Its immutable `/private/tmp/krogu-567-first-runtime-9974b07a/` remains untouched:
first-jvm.log, first XML, inventory and classification-handoff were inspected.
Inventory: 115 suites / 391 methods, 382 passed, 9 failed, zero errors/skips/
missing/extra/duplicates; all nine V1 new methods passed. Fresh V1 JAR SHA-256
`4728453c6b16b12ef1cad3784dfe20560d5d15d92e27ded44057fc089ad4131e`,
all four input manifests `c88084dd800622f13d83bc5f77ce60a6f0859fd59691fd6804b498ca49091731`.
These are V1 results, never evidence of a V2 pass. First failure caps and early
aborts remain material: Portable's 472 mismatches were not a complete printed
case inventory; the frozen scale cursor stopped at 63508, not final139239.

Frozen fixtures remain unchanged:

- Jdk25Vectors.kt: `cb4d2327da9850e347dd1db6b96519bb228a0072ccd778b8372a647cd7f1ac4f`.
- Jdk25ScaleBoundaryResults.kt: `ebca3fbfaf3baddfccad62da50ef00180ceafa4e0e6289d785fea778fece23a9`.
- Historical observations.tsv at
  `/private/tmp/krogu-541-Z1-538-1991-6d0b1536-v1/observations.tsv`:
  `ec2f3c0e865edf2e644a0e253e33474373b6443fe5e7bb025654dcc809609cce`.

## A. Contain indirect representation changes

Direct [plus(MathContext)](https://docs.oracle.com/en/java/javase/25/docs/api/java.base/java/math/BigDecimal.html#plus(java.math.MathContext))
requires a zero result's scale to be 0; the supplied public contract makes
round equivalent. V1's direct implementation stays: validate context, apply
unchanged precision kernel, select stored coefficient0/scale0/canonicaltext0.
Precision0 is included. Identity with cached ZERO is not a contract expectation.
Public round still dispatches virtual plus; no-context plus retains self/scale.

### Unary compatibility choice

This is **COMPATIBILITY_SCOPE**, not an additional specification defect.
`abs(mc)` still calls virtual signum first and virtual negate(mc) on the negative
branch. Its nonnegative branch enters private `contextualUnaryRound`.
`negate(mc)` still calls virtual negate() first, then enters that seam on the
returned object. The seam reads that object's private stored coefficient:

- Stored zero: validate mc at this point and use unchanged roundValue. Ordinary
  and truthful zero has precision1, so p0/1/7/MAX preserves its own cohort.
- Stored nonzero: call virtual plus(mc), preserving exact callback replacement
  and exception identity, including a zero replacement and null-context behavior.

There is no early universal null check and no rewriting of a callback return.
Poisoned signum is not evidence of zero. A stored-zero object's inherited unary
implementation now bypasses its internal plus callback; that is a scoped
implementation choice, not a universal API guarantee about callback forwarding.
Explicit abs/negate/plus/round overrides remain ordinary language calls, and
caller-owned super wrappers retain their normal dispatch. Kernel precision
callbacks on a stored-zero receiver are not bypassed or normalized.

The original common unary method now checks chosen contextual scale retention
across seven scales MIN..MAX, p0/1/7/MAX, all modes, base/truthful receivers. Its
callback replacement receiver is nonzero12.34, ensuring exact returned zero and
sentinel identity are still required even with null contexts. Two additional
unary methods cover: returned stored-zero at every scale with poisoned sign,
explicit plus/round and super calls, negative abs and negate ordering, null
validation after negate, sign/negate sentinels, explicit abs/negate overrides,
and caller-owned super unary wrappers. (Two new unary methods plus the revised
original method; these are source controls, not runtime results.)

### Double conversion

`NumericExtensions.Double.toBigDecimal(mc)` still obtains its decimal from the
existing canonical factory, exactly once. It now passes that base result's
coefficient and scale to the existing contextual coefficient/scale constructor,
which uses precision-only roundValue. This preserves canonical signed-zero
conversion `0@1`, rather than inheriting direct round's separate0@0 rule.
Nonzero coefficient precision rounding is the same private kernel. Factory
nonfinite errors precede the constructor as before; the public context parameter
stays nonnullable and unchanged. Float(mc) and all task525 migration assertions
remain untouched. No text reparse, exact binary constructor, post-round
normalization or special host-output matching was introduced.

One new common method checks both signed Double zeros, Float zeros, p0/1/7/MAX
and every mode, literal0@1; no-context Double zeros, nonzero0.1 and exact1.25,
and all three nonfinite Double categories. Existing migration midpoint/carry,
conversion and error controls remain the broader runtime gate.

### Three owned sqrt receivers

Only three final precision reductions change from public round to private
roundValue: the freshly constructed exact root (private stripTo returns that
base object or a new base object), freshly constructed inexact root, and freshly
constructed finite exact root. No caller-supplied decimal can be the receiver at
these seams. Every preceding virtual source/normalized callback remains intact.
The kernel body and sqrt algorithm are unchanged. No H assertion is adapted.

Concrete own-source derivation for the observed key `2/reportedPrecision7/p1/DOWN`:
adjusted exponent6, root exponent3, working scale-3; integer quotient0 and
positive residual2/10^6; DOWN keeps root coefficient0. The owned base result
therefore is0@-3, and roundValue leaves it there; stripTo's zero loop does not
change its scale. Canonical text is `0E+3`, with the original source callbacks
`sign;precision;`. Direct plus normalization had instead erased that cohort.
This is containment of the observed indirect change, without classifying the
untruthful receiver as a direct-zero case or waiving its callback checks.

V1's constructor/add/multiply/divide/pow private precision seams remain in place.
Other public internal callbacks remain unchanged. No public declaration,
roundValue numerical body, BigInteger, metadata helper or serialization change.

## B. Finite direct-zero consumer predicate

Initial predicate: **direct plus(mc) or round(mc), valid nonnull context including
p0, input stored coefficient0 and original scale!=0, ordinary or demonstrably
truthful receiver**. Expected coefficient0/scale0/canonicaltext0 is literal and
independent of the observed host cohort; no cached identity promise. None of the
selectors inspect actual output or poisoned signum to decide applicability.

| Consumer | Exact finite selector | Prepared count |
| --- | --- | --- |
| Portable D | Full six-field keys: round/0.00/0/5/UP; round/0E+20/0/9/HALF_EVEN; round/0E-8/0/10/UP; round/0E-9/0/11/HALF_DOWN; round/0E-10/0/17/HALF_DOWN | 5 |
| Portable L | op=round, coefficient literal0, scale in MIN,MIN+1,MIN+2,MIN+3,MAX; p0/1/2/7; all modes; constructor excluded | 160 |
| Portable S | text0.00, reportedprecision1, behavior super or sequence; p0/1/7, all modes | 48 |
| Portable C | plusContext:0, text0.00, auxiliary field0, p0/1/7, HALF_EVEN or UNNECESSARY | 6 |
| ScaleBoundaryOracle compact | Ordinary direct round, stored0 and scale!=0; eight nonzero scales, four precisions, eight modes | 256 |
| ScaleBoundaryConformance compact | Same ordinary direct round domain | 256 |
| DecimalMultiplicationSubclass.roundingScaleValidation | constructor=false, literal coefficient0, scale!=0; five scales, four precisions, eight modes | 160 |
| DecimalRoundSubclass | text0.00, reportedprecision1, super/sequence; p0/1/7, all modes | 48 |
| DecimalUnarySubclass.unaryDispatch | text0.00, reportedsign0, op=plusContext; p0/1/7, HALF_EVEN/UNNECESSARY | 6 |

Prepared count assertions guard these scopes, including unique Portable keys.
Every original vector and field remains. Adapted Portable keys validate the
complete raw host text/flags/trace before comparing the new literal expectation;
all adapted paths print complete original diagnostics with keys/cursor plus
candidate outcome where available. Live matrices still invoke the host and
retain complete host observations. Existing mismatches/exception checks stay.

For S/C scale2 receiver becomes scale0, so self=true is impossible for immutable
input. Only inherited plus runs after the explicit trace wrapper; it selects a
base result. Expected fields are therefore text0/self=false/base=true. This is
a separately scoped base-result assertion; no class-wide normalization is used.
S exact trace remains `plus;` at p0 and `plus;precision;` otherwise. C exact trace
remains `plusContext;`. Explicit replace/throw/self/null controls remain unadapted.
String-only consumers demand canonical `0`, uniquely coefficient0/scale0 for
these ordinary or inherited base results; common direct controls and both scale
boundary consumers additionally assert coefficient and scale independently.

Frozen scale-cursor consumption remains one original field per call, original
order, final139239; live compact comparisons explicitly require139239 too.
No constructor, multiply, sqrt, abs, negate, null or poisoned metadata result is
admitted by the direct-zero adaptation. Actual multiplication and JdkOracleTest
methods passed V1; their unrelated checks remain untouched.

## Exact unresolved source-selected keys (no waiver)

These selectors describe source-predicted differences or review domains, not a
new execution inventory. Complete raw vectors/assertions remain operative. Let
M8 be all eight modes; M2={HALF_EVEN,UNNECESSARY}; B={super,sequence}.

| Retained key format | Exact selector / issue |
| --- | --- |
| Portable S and DecimalRound `$text/$reported/$p/$mode/$behavior` | text0.00, reported{-1,0}, p{0,1,7}, M8, B (96); text0.00/reported3/p{0,7}/M8/B (32); text0.00/reported9/p{0,1}/M8/B (32). Zero stored input with untruthful precision; outside initial truthful predicate. Other32 zero poisoned rows already end at0@0 and need no adaptation. |
| Same S key format | text-123.456/reported9/p1/mode{DOWN,CEILING,HALF_UP,HALF_DOWN,HALF_EVEN}/B; text999.99/reported9/p1/mode{DOWN,FLOOR,HALF_UP,HALF_DOWN,HALF_EVEN}/B. Stored input is nonzero but replaced precision makes a zero intermediate; 20 source-selected cases outside predicate. |
| Portable C and DecimalUnary direct | plusContext/sign{-1,1}/text0.00/p{0,1,7}/M2, 12 keys. Poisoned sign, no truthful admission. |
| Portable C and DecimalUnary indirect | absContext/sign{0,1}/text0.00/p{0,1,7}/M2, 12 keys. Cohort/self/base restored but old internal plusContext trace no longer runs; preserve exact old comparisons pending parent choice. |
| Portable C null | `[C,nullAbs:behavior,0.00,reportedSign,0,HALF_EVEN]`, behavior{return,self,throw,super}, reportedSign{0,1}: eight keys. Old internal plus(null) is bypassed; new source outcome !NullPointerException with trace sign;. Return/self/throw change outcome+trace; super changes trace only. |
| DecimalNullUnarySubclassTest.nullUnaryContextsReachSubclassCallbacks | text0.00/sign{0,1}/behavior{return,self,null,throw,super}/abs=true, ten keys. Same seam; Java null-return case also affected. All assertions untouched; V1 passed, V2 is source-predicted to fail this previously passing method. |

No blanket S/C/trace/baseclass exemption, skip, fixture rewrite, assertion removal
or output-based force-pass helper is present. In particular preserving the V1
cohort in inherited abs is not sufficient to keep its old internal plus trace.
Parent must decide how narrowly to retain those observations and require the
selected implementation independently, or revisit the chosen compatibility seam.
This finite milestone does not silently resolve that policy question.

## Checks and remaining gates

All twelve common methods are prepared source; nine retain their V1 method
names, with the unary method revised, and three are new. V1 passed only its
original nine, not this modified file. Source/diff/hash/whitespace reads and an
independent read-only review are the only V2 checks. Review disposition and final
hashes are in the V2 handoff/task comment. No execution is queued or authorized.

Parent-owned remaining gates: classify exact unresolved keys/internal callback
observations; independently review any further adaptations; explicit sole-owner
runtime grant; compile and identify a fresh V2 artifact; retain full JVM failures
and full inventory; then full retained JS/Wasm/macARM suites and supported
exact-final-HEAD CI. No V2 behavioral success or acceptance completion is claimed.
