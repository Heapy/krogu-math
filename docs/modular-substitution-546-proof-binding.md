# Modular arithmetic: conditional subtype substitution (546)

SPDX-License-Identifier: Apache-2.0

This ledger binds the modular arithmetic argument to current source. It covers
all correctly completing, transitively truthful reached leaves, conditional on
the existing integer-kernel and resource premises. It introduces no production
change, fixture waiver, numerical execution or whole546/548 acceptance.

## Source and proof binding

| File | SHA-256 |
| --- | --- |
| `src/commonMain/kotlin/io/heapy/krogu/math/BigInteger.kt` | `d6d531742a7b12bd6cc09cf6b98daca11f70b5207687ecd4c04c5a11d655a9b2` |
| `docs/division530-proof.md` | `29726dd6473f79aac5fb267cfb599227077eddb2e5581c358c1870b5021fded3` |
| `docs/division530-selection-proof.md` | `c5c69c26619f21d28084f4005dc81c5930b7b19d8cb25c24be25566a73d57aa7` |
| `docs/odd-modulus-context-538-implementation.md` | `6cdb5d63468a85703a3d6f0a1beecb3e8fd962f0b924b4c6b460a3a8252138f6` |
| `docs/odd-modulus-context-538-tiny-fallback.md` | `780e680657eea9cbdd463ccaf7f96c99f1b51e6f49794045b9119e64401992b3` |

The [modular-power and inverse arguments](../NUMERICAL_ARGUMENTS.md#modular-exponentiation)
provide the residue, Euclidean and CRT invariants; the dedicated proofs above
bind current division/context bodies, including the later tiny-power guard.
Historical source-only statements in those milestones remain historical.
Compatibility-summary wording is not an implementation/proof hash dependency.

## Required values, language dispatch and input boundary

The public JDK25 [mod](https://docs.oracle.com/en/java/javase/25/docs/api/java.base/java/math/BigInteger.html#mod(java.math.BigInteger)),
[modInverse](https://docs.oracle.com/en/java/javase/25/docs/api/java.base/java/math/BigInteger.html#modInverse(java.math.BigInteger))
and [modPow](https://docs.oracle.com/en/java/javase/25/docs/api/java.base/java/math/BigInteger.html#modPow(java.math.BigInteger,java.math.BigInteger))
contracts select nonnegative residues below a positive modulus. Inversion and
negative powers require coprimality. Their descriptive expressions specify
mathematical values rather than a required inherited callback recipe.

All three own signatures have non-null arguments (BigInteger.kt349,354,598).
The accepted Java-to-Kotlin migration permits explicit nullability edits in
overrides; Java source compatibility is confined to TCK adapters. Ordinary null
arguments require NPE. Competing invalid-input priority and compiler/VM-generated
NPE messages retain their diagnostic scope. No nullable-returning numerical
override becomes a truthful non-null leaf by historical observation.

An explicit caller invocation of an override must retain its actual argument
references, returned object and thrown exception. Caller-owned super invocation
selects the inherited body, which can itself call virtual methods. These language
obligations do not prescribe inherited internal callback count/order, singleton
identity, returned runtime class or fresh-instance behavior. Existing assertions
and historical observations are preserved, not silently relabelled or removed.

## Transitive refinement condition

Let V(x) be the immutable integer represented by x's stored sign/magnitude.
Every reached equality, comparison, sign, bit and arithmetic leaf must return
the specified result for V and complete normally on valid inputs. Returned
objects must encode the correct value and satisfy the same condition for all
later reached leaves. Equality is truthful; comparison needs only the correct
sign, not the particular integer-1/0/+1. Correct aliases and stateful logging
are allowed. Values cannot mutate, answers cannot become false, and arbitrary
user recursion/resource failure is not assumed to terminate successfully.

This condition is transitive over returned objects, not limited to the original
receiver/exponent/modulus, nor to overrides that delegate to super. It is the
premise for the following complete reached-seam ledger:

| Source seam | Required invariant under those leaves |
| --- | --- |
| `mod`,349–352 | Stored modulus sign rejects m<=0. Correct remainder has magnitude<m; adding m only to a negative remainder gives the unique residue in[0,m). |
| `modInverse`,598–616 | Modulus1 selects0; otherwise correct reduction and equality select only valid0/1 cases. Fresh positive stored wrappers enter Euclid; strictly decreasing remainders and Bezout coefficients yield the inverse iff gcd=1. Existing coefficient/product bound is at most m. |
| `modPow`,354–363 | Reject m<=0 before shortcuts. Exponent0 selects1 mod m, including0^0. Bases1,0 and-1 select their correct shortcuts. Negative exponent negation produces its positive magnitude, and correct mod normalizes a negative/large base. |
| Odd route,386–405; context423–450 | Correct exponent-one equality returns an already reduced base. Otherwise copied positive words and stored exponent bits feed context/binary powering. Reading stored bits preserves truthful exponent value even when public bit callbacks are bypassed. |
| Binary route,571–596 | Correct length/bit leaves preserve masking. For odd bases, retaining low p-1 exponent bits is valid modulo2^p; even bases use all positive exponent bits. Sufficient full-product bounds or bounded residues avoid an unnecessary oversized product. |
| Even split and CRT,364–383 | Correct parity/lowest-bit/shift give m=q*2^p with odd q and p>=1. The two residues and inverse coefficients give the unique result in[0,m). q=1 correctly suppresses the odd component. |
| Negative suffix,384 | Inverting the computed positive-power residue succeeds exactly when gcd(a,m)=1; m=1 still gives0. |
| Bounded multiplication/addition,618–637 | Canonical residues use gap=m-b: a-gap when a>=gap, otherwise a+b<m. Repeated doubling/bit accumulation preserves[0,m) without constructing2m or the full product. |

Every truthful virtual replacement therefore preserves the mathematical
predicate of each branch and every helper precondition. The existing helper
proofs then preserve its value invariant; induction over the finite completed
call/iteration graph establishes the same required residue for any compounded
truthful returned-object graph. This is an all-class conditional source argument,
not an inference from a finite matrix or a new unconditional resource theorem.

For the direct CRT branch,2*bitLength(m)+2<=L is a sufficient bound for products
and sum. Otherwise u*2^p<m and v*q<m are formed first, then multiplied/added as
bounded residues. The binary modulus fits since p+1<=bitLength(m). Euclidean
coefficient products stay within m by the existing terminal-row bound m/g.
Correct supported integer division, array feasibility and548 capacity remain
explicit premises, not newly imposed subtype requirements.

## Aliasing and diagnostic limits

Receiver=modulus>0 gives mod0; inverse fails for m>1; positive powers give0,
zero power gives1 mod m and negative powers fail when m>1. Modulus1 gives0
throughout. Other receiver/exponent/modulus aliases preserve the same invariants.
For example, a correct negation may return another already-existing positive
object. Equal value, not freshness or a particular reference, selects the
arithmetic. Current-own-format truthful subtypes are included; no legacy
serialization requirement is added.

Euclid and binary kernels use immutable stored views/local scratch. The context
copies the base, rotates disjoint workspace slots and detaches exported arrays
(423–450). A correct returned alias is therefore not writable numerical scratch.

False equality/order/parity/length/lowest-bit answers, wrong or null arithmetic
returns, negative/zero negation replacements and later-changing returned leaves
are separate diagnostic classes. A mode name such as mod:self does not classify
all arguments: it can be mathematically truthful on some. Required numeric/error
fields cannot be waived solely because another method is poisoned. Direct caller
sentinel/alias evidence also remains distinct from an internal-trigger trace.

## Existing finite evidence and remaining scope

Named active controls are [IntegerInverseOracleTest](../src/commonTest/kotlin/io/heapy/krogu/math/IntegerInverseOracleTest.kt),
[IntegerModPowOracleTest](../src/commonTest/kotlin/io/heapy/krogu/math/IntegerModPowOracleTest.kt),
[OddModulusContext538Test](../src/commonTest/kotlin/io/heapy/krogu/math/OddModulusContext538Test.kt)
and [its JVM public differential controls](../src/jvmTest/kotlin/io/heapy/krogu/math/OddModulusContext538JvmTest.kt).
They retain wide values, residues/congruences, aliases, snapshots and deliberate
replacement/throwing/identity/trace observations. Most oracle dispatch dimensions
vary one nondelegate position; the compound538 probe deliberately poisons unused
leaves. These facts neither exhaust truthful graphs nor invalidate this
transitive source argument. All fixtures and assertions remain unchanged.

The [current583 local report](decimal-residual-583-verification.md), SHA-256
`43809d15ca7ee37613a613c934be9fec47b8db88288027af1a26d327463824fc`,
records1,584 exact method executions on JVM/JS/Wasm/macOS ARM64 with the pinned
BigInteger source. That is finite integration evidence, not a new modular
campaign, proof of every graph, hostile callback policy, practical maximum
allocation or all546/548/public-TCK/JCK acceptance. This binding adds no test
execution and requires no unbounded fuzz or language-mirroring test campaign.
