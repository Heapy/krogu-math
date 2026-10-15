# Supported remainder blocked by an unsupported dispensable product

Adopted as the independently reviewed task583 obstruction. Original private
memo SHA-256: `4b767448266197cc081cdba8fe7df8d474b23f3a4b8aa260fc886c7b386cb7bd`.
The reviewed582 candidate is distinct from the then-published quotient source;
its fresh validation does not close this surviving pair/remainder obligation.

SOURCE / SPEC / SYMBOLIC review only, 2026-10-15. No numerical operation,
script, compiler, helper, certificate, probe, test, build, process inspection,
Git or tracker operation was run. The giant witness is UNRUN. This memo changes
no repository file and makes no observation or defect claim about a JDK runtime.

## Bound source and premises

Reviewed private582 root: `/private/tmp/krogu-math-task-582-u84RlRs4/root`.

| File | SHA-256 |
| --- | --- |
| `src/commonMain/kotlin/io/heapy/krogu/math/BigDecimal.kt` | `00e392444ce4b13122ec6dcb7ccbee357b437da1e6554cc78876e909346c1599` |
| `src/commonMain/kotlin/io/heapy/krogu/math/BigInteger.kt` | `66bb59deeeceb4f664681f99ddacc490c3054b5c42c539d3b8a3a44ed2ddb7c2` |
| `src/commonMain/kotlin/io/heapy/krogu/math/MagnitudeLimits.kt` | `5bf687bb968d22908a36b8898f19649bdbdf7e5a2596575f1808053fca0e4769` |
| `src/commonMain/kotlin/io/heapy/krogu/math/DecimalDivision581.kt` | `00557b2517c70f9294ed1be484177962c441841334585cba14ac7a94c647fa6f` |
| `docs/integral-quotient-582-implementation.md` | `150c94a9e895e37ff5bf2bf1158a5d04cc1b3c83a875ed4e679f46dcbf85e70f` |

Reuse the existing528 proof, not a newly executed certificate:
`/private/tmp/krogu-math-task-528/DECIMAL_METADATA_COMPARISON.md`, SHA-256
`67b22ddb9af0d32483f7624b4be12770ca7b7f6456e388361d490090acb792b0`,
lines16–33, and its certificate source `scripts/certify-decimal-metadata.main.kts`,
SHA-256 `bf457b19276a9deb49d2804cf026eccb1f04e1f5575fd84d9985784161a50acc`,
lines50–59. These certify bitLength(10^H)=L+1 for L=2147483647 and H=646456993.
Thus 2^L <= 10^H < 2^(L+1); equality on the left is impossible because 10^H
has a factor5. In particular the strict bound requested here follows without
assuming an additional decimal approximation: 2^L < 10^H < 2^(L+1).

## Exact witness and required cohorts

Use ordinary positive inputs x=[1,-H] and d=[3,0], where [C,s] means C*10^-s.
Both coefficients are tiny and both scales are in Int; constructing x does not
require constructing 10^H. Since 10^H is 1 modulo3,

    Q = floor(10^H/3) = (10^H-1)/3,    R = 10^H - 3Q = 1.

Q consists of H decimal digits, all3. Its upper bound is
Q < 2^(L+1)/3 < 2^L, so Q is a supported positive public coefficient. Its
precision H fits Int. The preferred integral scale is -H, but Q has no trailing
decimal zero: no negative-scale integer cohort is possible. Consequently the
closest-to-preferred integral result is exactly [Q,0], not an unsupported padded
or alternative-cohort requirement. The exact remainder is [1,0]: multiplication
would have scale0, and the subtraction preferred scale is max(-H,0)=0. Both
returned elements are supported, with |R|<d and x=Q*d+R as mathematical values.

The dispensable intermediate M=3Q=10^H-1 is different. Strict 10^H>2^L and
integrality imply M>=2^L, while M<2^(L+1), so its bitLength is L+1. It ends in9
and cannot be rescued by stripping decimal zeros. Public exact multiply at
scale0 correctly rejects this genuinely unsupported *product result*.

## Exact reached obstruction after582

BigDecimal lines399–425 first compute the integral quotient. For these truthful
ordinary operands the582 bridge at BigInteger lines109–137 accepts S=-H and
stored metadata. Its extreme-negative preflight does not fire: Da=Db=1 gives
Da+k=H+1, equal to (not greater than) H+2Db-1. The separate integral stream
therefore computes Q and its required scale0, subject to its established source
correctness and ordinary resource-completion assumptions. This witness does
not invalidate that quotient proof or claim a fast maximal-domain execution.

BigDecimal lines458–460 then execute `quotient.multiply(divisor)` before
`subtract`. Exact multiply, lines128–131, selects scale0 and calls coefficient
multiply. BigInteger lines65–79 either rejects by its sound lower-bit guard or
normalizes the exact product; MagnitudeLimits.checkedBitLength rejects its
L+1 bits. This happens before subtraction can recover the tiny exact remainder.
The no-context pair consequently cannot return its required supported [Q,0]
and [1,0]. The precision0 pair delegates at lines462–464; both remainder
overloads delegate through their pair at lines468–469 and inherit this failure.

There is also a bounded-in-parameters finite-context extension: choose precision
H and mode DOWN. The exact integer quotient requires exactly H digits, so it is
admissible. A correct completing581 contextual divide at H/DOWN selects Q@0:
the normalized exponent is -1, target scale is -H+H-1-(-1)=0, and the discarded
fraction is positive. BigDecimal lines427–447 neither enters its negative-scale
residual branch nor changes this nonzero-ending cohort. The pair at lines465–466
then reaches the same unsupported product. This is a conditional source proof
for that explicit context, not a new run or an assertion about every finite
precision/mode. Precision below H legitimately cannot contain Q.

## Normative classification and limits

The JDK25 public [BigDecimal class contract](https://docs.oracle.com/en/java/javase/25/docs/api/java.base/java/math/BigDecimal.html)
explicitly treats its arithmetic expressions as mathematical pseudocode. Its
[remainder contract](https://docs.oracle.com/en/java/javase/25/docs/api/java.base/java/math/BigDecimal.html#remainder(java.math.BigDecimal))
defines the remainder value, while
[divideAndRemainder](https://docs.oracle.com/en/java/javase/25/docs/api/java.base/java/math/BigDecimal.html#divideAndRemainder(java.math.BigDecimal))
requires the integral quotient and remainder. The displayed subtract/multiply
formula therefore does not mandate publication of that intermediate coefficient
or supply a separate algorithmic range exception. No result scale overflows
here, the divisor is nonzero, and the required final coefficients are supported.
The class's allocation/complexity caveat remains real: this memo asserts a
deterministic arithmetic-range obstruction under completion/resources, not a
promise that giant allocations succeed under a bounded heap or on every target.

Qualified finding: this is a distinct ordinary supported-output P4 source-range
bug in pair/remainder composition, even after582 repairs the quotient route.
It is not a defect in exact public multiply, a poisoned-subtype waiver, a JDK
bug observation, or whole541/548 closure. Current publication may still fail
earlier through the old quotient guard; that does not refute this surviving
private582 composition obstruction.

Smallest next source scope: expose an independently proved private residual
alongside the integral stream, or another supported residual route, so ordinary
pair/remainder do not materialize Q*d or a huge scaled dividend solely to subtract
them. Preserve explicit caller override/super contracts and separately classify
captured-metadata mismatch fallback and internal callback diagnostics. Prove the
remainder's signed scale/cohort publication, finite-context quotient admission,
and all temporary/index/buffer bounds; then add meaningful bounded composition
tests and run fresh gates only under a separate runtime grant. No implementation
or new endpoint saturation policy is authorized or supplied by this memo.
