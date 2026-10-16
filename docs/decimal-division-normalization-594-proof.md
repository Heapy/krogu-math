#594: noncompact division normalization without coefficient Strings

SPDX-License-Identifier: Apache-2.0

SOURCE ONLY. Production and three common/one JVM test methods are UNCOMPILED/
UNRUN. No numerical/helper/model/certificate/script/compiler/probe/test/build/
benchmark/process/mutex/Git/tracker/workflow activity occurred. Only this new
isolated candidate and its evidence/documents were written; no main or frozen
source/evidence was edited. No foreign numerical/compiler/stdlib implementation
source/body/bytecode was read. Parent owns independent review and any execution.

## Binding and narrow delta

Candidate: `/private/tmp/krogu-math-task-594-eGORPc9W/root`.
Baseline parent publication62bb43ccd384d5678dd22d25a95c97cc18f51362/source253
manifest aa90e836005efc363e9575026cd12049b0bb6ac42eb3a716f20d04c596bac1ad.
All253 original source hashes passed before copying and again in the no-Git/
no-build clone before edits. The original complete archive was made before
any source edit, retained read-only in `../evidence/original-published62bb.tar`,
SHA256 a7bcf9d4fa715738ec03f66dae551eecbd0b89023a53bcadc4d92d241719356a.
Original BD5761b7113f808d258deaa6266ff55a1a409ee41b238b2dc1f28a1724bd8f0b9a
is separately read-only. A narrow five-added-line diff and exact text reversal
recover that entire original BD hash, not just a matching numerical region.

Only private BD quotientExponent and normalizedComparison change. No new public
or internal selector API, numerical kernel, BI body, field/cache, serializer,
dependency, configuration, old test or fixture is changed. All252 other old
sources match original253. Two new test classes make255 source files. All
unchanged original routes/bodies remain available; the candidate manifest and
exact pathset are retained in sibling evidence.

The earlier accepted source map548 (947c69c9…) identifies a conditional resource
witness, not a measured engine failure: supported A=10^K, K646456992, and x=y=A@0
require1@0 under exact and fixed-scale0/UNNECESSARY division. Old normalization
needs H=K+1-digit coefficient Strings before producing that tiny result. This
candidate removes those two numerical metadata uses of whole Strings when
noncompact, without changing standalone text formatting or claiming any actual
Node/JS/Wasm String ceiling or giant allocation.

## Selector and preservation boundary

Each helper first reads FINAL stored BI decimalCompactValue() on the source
and, if needed by short-circuit, the divisor. If BOTH are non-null, every line
of the old helper body remains byte-identical, including original virtual abs/
toString calls, string padding, delegation, exceptions and order. For ordinary
or transitively truthful coefficients those two returned strings have at most
19 digits, since stored magnitudes are below2^63; Long.MIN_VALUE's magnitude
is deliberately noncompact. This is a sufficient short-route selector, never
a public magnitude limit or runtime-class exclusion.

On any noncompact coefficient, normalizedComparison calls the existing FINAL
BI compareDecimalSignificand. quotientExponent uses the existing FINAL
BI decimalMagnitudeDigits for both coefficients and that same significand
comparison directly, with the original Long scale expression. No new virtual
call on an operand/coefficient subclass is added. The accepted helpers may
internally invoke numerical leaves on their OWN fresh/base receivers (such as
TEN.pow); claiming no dispatch anywhere would be wrong. Caller callbacks are
not substituted for those stored reads.

## Significand and exponent equivalence

Reuse accepted `DECIMAL_METADATA_COMPARISON.md` and unchanged528 helpers in
BI8b6359be57ebbc8047102e5dea420e307d4fb128cbf686a054b04dff2600b1ff.
For canonical A, its ordinary abs().toString() has D(A) digits with no leading
zero, D(0)=1. decimalMagnitudeDigits returns exactly D(|A|), ignoring sign.
Sign does not participate in either old metadata helper: the old abs strings
are positive magnitudes as well.

For nonzero A,B, write dA,dB for those lengths and pad the shorter canonical
digit string on the RIGHT with zeros to max(dA,dB). Lexicographic order is
numeric order of those equal-length decimal integers, or equivalently order
of their decimal significands. compareDecimalSignificand proves that same
order without forming the padded number or String: equal lengths compare
stored magnitudes; unequal lengths divide the longer magnitude by10^|dA-dB|,
compare the quotient with the shorter and retain any nonzero discarded suffix
as sticky. Equal quotients plus sticky imply strictly larger, not equality.
A zero magnitude compares below every nonzero magnitude and zero to zero is
equal, exactly as a padded zero string. No negative sign spelling is read.

The helpers' raw comparison magnitudes need not equal String.compareTo's
raw return magnitude. Every current caller of normalizedComparison uses only
`>0` or `<0` (normalization adjustment or quotient exponent), so order/zero
is the complete required private result. This does not invent a new public
return-image or identity contract. Its integer-kernel/primitive comparison
premises are the existing accepted528 premises, not newly waived here.

For positive magnitudes at scales Sa,Sb,

    floor(log10((A*10^-Sa)/(B*10^-Sb)))
      = dA-dB+Sb-Sa - [significand(A)<significand(B)].

The normalized ratio is in[0.1,10), with equality1 precisely at equal padded
significands. Thus the final adjustment is exactly the old one. The new formula
is a literal substitution of exact stored lengths/order in the old Long sum;
it does not replace a precision getter or change the operation's selected
preferred/target scale. For stored zero its algebraic quotient exponent is not
mathematically defined, but the old lexical convention is still reproduced:
digits1 and the corresponding zero/nonzero significand order. Public stored-zero
and zero-divisor prefix behavior remains unchanged; misleading sign metadata
and deliberate diagnostic replacement flows keep separate541/526 treatment.

Supported counts lie1..H=646456993, so dA-dB lies within±646456992.
Sb-Sa lies within±4294967295 and the adjustment is0/1. The exact expression is
within[-4941424288,4941424287], well inside Long. Promotion precedes every scale
subtraction; no Int negation of MIN is introduced. No full coefficient String,
padded decimal integer or scale-sized power is created by the new formulas.

## Resources and scopes retained

The528 digit threshold is supported or avoided at its certified maximum; its
significand kernel clones one raw limb array and divides in bounded decimal
chunks, with partial quotients bounded by the original magnitude and active
length tracked. This candidate reuses that accepted proof without editing the
kernel or creating a smaller arithmetic cap. It removes the independent host
String representation requirement for these noncompact numerical metadata
steps. Dense numeric allocation/time may still be enormous or quadratic; this
is not a heap, latency, allocation-reduction or full548 guarantee. Requested
formatting outputs and other numerical formatting sites retain their own scope.

Exact divide reaches normalizedComparison before its truthful metadata return;
explicit fixed-scale divide can retain old592 arithmetic at shift0 and then
reach quotientExponent. Thus the real new branch is reachable with the small
noncompact operands in the tests; no fake test-only cap is required. Existing
context581/592 guards and their conditional truthful mathematics are unchanged,
as are public prefix checks and standalone arithmetic. No AE/OOM/other Error
is caught or replayed by the added code.

Ordinary/fully truthful subclasses have the same required numeric/cohort/error
answers. Current own serialization can retain a noncompact BI subtype directly;
a public constructor's byte normalization is not a valid reason to exclude it.
The final stored helpers cover its canonical magnitude without a runtime-class
gate. False/replacing/throwing abs/toString diagnostics can differ on the new
noncompact branch; that does not authorize any assertion removal or blanket
subtype waiver. All old diagnostics remain active for the first later run.
Outer explicit override/super entry, arguments and caller-owned result/exception
identity remain required; no inherited callback count/order/noncall guarantee
is introduced. No serialization format is changed or legacy stream supported.

## Independent prepared tests and their bounds

Three common methods and one JVM method are SOURCE ONLY:

1. Exact noncompact ratios use N=10^20+1, 2N and10N, mixed compact/noncompact
   operands, both signs and common scales MIN,-10,0,10,MAX. Independent
   cancellations give.5,5,10,N,10^-20 with explicit coefficient/scale literals.
   Different lengths, padded-significand equality and same-length different
   significands are included. All100 exact divisions have supported small
   results and no endpoint-scale alignment. Input coefficient text and scales
   are checked unchanged, rather than a mutation-insensitive shared alias.
2. Fixed-scale105N/(20N)=5.25 uses both signs and common scales
   MIN,-3,0,7,MAX, requested scales−1,0,1,2, all eight modes. Expected coefficient
   is independently rounded primitive fraction525/d, d=1000,100,10,1. Quotients
   are<=525, remainders<1000,2r<=1998, q+1<=526, so no primitive overflow exists.
   There are640 selected public divisions. This tests below/above half, genuine
   even retained-digit midpoint and exact tails/UNNECESSARY, not the production
   metadata/normalization algorithm. Both raw coefficients are noncompact;
   small shifts and bit sizes retain old592 so quotientExponent really reaches
   its new branch. Immutability checks are bounded<=23 coefficient digits.
3. N/(10N+1)<.1 has a sticky normalized suffix, tested at scale0 in every mode.
   The true noncompact2^63 boundary is paired with compact2^63−1 in both operand
   orders, giving just-above1/just-below1 controls. Zero/source-zero/divisor-zero
   and exact nontermination N/3 are retained across common scale endpoints;
   10≡1(mod3) gives N≡2 so its denominator3 does not cancel. Two cross-endpoint
   exact quotients have scale distance±4294967295 and cannot be represented by
   any supported coefficient/Int-scale cohort: positive exponent would require
   at least2147483647 coefficient zeros at scaleMIN, beyond H digits; negative
   exponent is below the smallest positive1@MAX. Those AE controls are not a
   request for a giant rescaling or a new intermediate-scale failure policy.
   This method has41 public division calls. Some prefix controls deliberately
   return before the changed helpers; they are not misreported as branch hits.
4. One JVM method restores only the CURRENT own format with truthful noncompact
   coefficient objects for105N and20N. Replacement identity establishes setup
   reachability, then independent literal525@2 and signed52/53@1 midpoint
   results are asserted for both signs/all modes, with UNNECESSARY AE.18 public
   division calls are prepared. The overrides delegate abs/signum/toString/
   toByteArray without sentinels/counters; no incidental inherited trace or
   absence assertion is used. This is not a legacy/cross-format test.

Largest literal coefficient has23 digits (<10^23), all current buffers are tiny;
no prepared test constructs the symbolic maximum witness or queries a host
String ceiling. Source-prescribed division counts are not observed executions.
All new error assertions require category only, not diagnostic wording.
Existing explicit caller/super/exception identity suites stay unchanged.

## Remaining gates

Independent code/proof/test review is required before any compiler or numerical
activity. Parent alone may later authorize initial compilation and complete
JVM/JS/Wasm/macOSARM selections, exact255-source/XML/JAR/full94282-ledger audits,
composition/publication and matching-head supported/public gates. These proofs
and prepared tests do not establish physical maximum allocation, broader548/
549/541/526, toolchain557, performance, universal arithmetic or official JCK.
All owned calls are terminal at the finite handoff; no background work or
future-window waiting is needed.
