# JVM PlatformRandom validation: narrow source proof (549)

Parent adoption: the original private memo SHA-256 is
`3a5cf4447a2a8d629adc3e3ae7ab29aaed0b21fe6ca427e027ea4d87c14a2313`.
Parent and an independent source/evidence reviewer accepted this narrow lemma.
The source and retained580 evidence below remain its read-time bindings;
this annotated copy adds no fresh execution or whole549 acceptance.

Finite SOURCE-only memo, 2026-10-15. Only this private document is written.
No source/test/fixture/public-doc/Git/tracker edits; no compiler, script, helper,
numerical operation, certificate, probe, test, build or runtime execution. Own
source/tests, retained execution metadata and official public API docs were read;
no foreign implementation source/body/bytecode.

## Claim and exact public routes

For an initialized ordinary library, a non-null java.util.Random and normal
language/runtime execution, the following two complete Int partitions satisfy
the public JDK25 validation categories:

| Public JVM entry | Exact own route | Conclusion |
|---|---|---|
| `BigInteger(numBits, rnd)` | BigInteger.kt:37 -> PlatformRandom randomBits:1081–1084 | Every negative numBits throws IllegalArgumentException. At numBits0 the constructed value is numerically zero. |
| `BigInteger(bitLength, certainty, rnd)` | :38 -> PlatformRandom generatePrime:1076–1079 | Every bitLength<2 throws ArithmeticException, for every Int certainty. |
| `BigInteger.probablePrime(bitLength, rnd)` | JVM static/companion entry:1073 -> same generatePrime, certainty100 | Every bitLength<2 throws ArithmeticException. |

The public [random constructor](https://docs.oracle.com/en/java/javase/25/docs/api/java.base/java/math/BigInteger.html#%3Cinit%3E(int,java.util.Random))
specifies the negative-numBits category. The
[certainty constructor](https://docs.oracle.com/en/java/javase/25/docs/api/java.base/java/math/BigInteger.html#%3Cinit%3E(int,int,java.util.Random))
and [probablePrime](https://docs.oracle.com/en/java/javase/25/docs/api/java.base/java/math/BigInteger.html#probablePrime(int,java.util.Random))
specify the bitLength<2 category. None requires the particular exception text.

PlatformRandomJvm.kt:3 is exactly `actual typealias PlatformRandom = java.util.Random`.
Thus these are actual Java-Random constructor/static signatures, not a claim
about the Kotlin Random factory overloads. BigInteger.kt imports kotlin.random.Random
at:6; its distinct internal Random routes are:1072,1074–1075,1086 onward.
RandomFactories.kt:8–12 supplies constructor-like Kotlin functions that call those
internal routes. A caller whose argument has the actual java.util.Random type
selects the PlatformRandom constructor/factory route above.

## Proof for all invalid sizes and the zero value

At1082, require(numBits>=0) throws IllegalArgumentException exactly when that
predicate is false. No addition, negation, absolute value or narrowing is used,
so this covers the entire negative Int interval, including Int.MIN_VALUE.
For numBits0,1083 returns the existing zero value. The public constructor then
uses private copy constructor:15, copying that value's sign/magnitude fields.
The resulting numerical value is zero; helper return identity does not establish
public constructor singleton identity.

At1077, bitLength<2 directly throws ArithmeticException. This is the complete
Int partition through1, including all negative values,0 and1. Certainty is not
read in that guard, so no special certainty value invalidates the conclusion.
The static factory fixes certainty100 and reaches precisely this same guard.
PrimeSearchLimits and the adapter are reached only after it, so their positive-
domain behavior is not a premise of this narrow invalid-size proof.

The subsequent adapter in PlatformRandom.kt:12–25 wraps the PlatformRandom for
common positive-bit processing. Neither invalid-size proof needs its numerical
or callback behavior. This source argument adds no public callback-count,
random-draw, seed-state-preservation or zero-case no-call requirement.

## Existing assertions versus this proof

* ConstructorRandom549Test's isolated invalid-size assertions use its
  SmallPrimeRandom, a kotlin.random.Random. They prove selected common-factory
  cases; they are not JVM java.util.Random constructor/static assertions.
* JavaApiContractTest.java:31–39 contains actual Java positive/zero constructor
  and positive prime/static calls. Its:63–79 requires otherwise-valid null NPEs
  but labels invalid-size-plus-null as compound diagnostics. Those diagnostics
  assert Exception rejection, not these isolated non-null categories.
* Validation578Boundary.java:25–33 deliberately supplies null java.util.Random;
  Validation578RngTest retains12 ordinary NPE requirements and13 compound-invalid
  diagnostic keys. It does not supply a non-null invalid-size oracle.
* Thus the identified dedicated controls still lack explicit isolated Java
  assertions `numBits<0 + non-null Random -> IAE` and
  `bitLength<2 + non-null Random -> AE` for both prime constructor and static
  factory. This statement describes those controls, not an exhaustive search
  claim about every public upstream test. The source proof above closes this
  specific bridge validation-accounting lemma without proposing mirror tests.

Actual compiled call-shape evidence already exists: accepted580 source225
manifest b2315fea binds the alias, helpers and named test sources; its JVM audit
9c5a997a records the three ConstructorRandom549 methods, JavaApiContractTest's
four methods and Validation578RngTest's method with empty issues. This memo read
those records; it ran no fresh compiler or test and does not promote their finite
inputs into the general proof.

The accepted NN policy remains unchanged. With null plus an otherwise-valid
size, the real JVM boundary requires NPE. With null plus an invalid size,
competing-invalid priority and VM wording remain separately retained diagnostics;
the non-null proof does not force either ordering. Java source compatibility is
only a TCK/adapter concern. Positive RNG distribution, primality/certainty,
hostile Random callbacks, large-size/resource policy and whole549 remain outside
this lemma (principally547/548). No future581 bridge implementation is adopted
or runtime grant awaited here.

## Read-time pins

| Source/evidence | SHA-256 |
|---|---|
| BigInteger.kt | `b2fb23547d97f3f7650b6d7dcf4ad53939efb3abac97d9d152376e67dd6685f5` |
| PlatformRandomJvm.kt | `5acb4c2184f981699c5fec755bf223ab61a441af84275c5151da6cc4fde345fb` |
| common PlatformRandom.kt | `e408e6fad2a92acec592520ea3eaf1c414493a6db708db9ef9ae686b1303f3d0` |
| RandomFactories.kt | `d66f6002d3e2f32b1a2f03143984eee2da55a2fc7edda2bf6e8bdada41257c20` |
| ConstructorRandom549Test.kt | `ed8c6ec4ccb8c1d6deca84123d1827a9ff6888a59402366807ad5212491a9c95` |
| JavaApiContractTest.java | `169894cb303ee9de556ed94595a92e237ef71f27dc7016d4ef0bbf50350a8d50` |
| Validation578Boundary.java | `b6d3e673282625d9cfac8f6ec2b216d943411e4e5d4bd38d714c9bbc40b6912c` |
| Validation578RngTest.kt | `165a5aaa2ee7a158240dc5008eb22c3b47121b91814f549879222ca4de2b6721` |
| accepted580 source225 manifest | `b2315fea5e93c1d3b0e28c88a33f6b1e78c942cc524b2e18f90023cca05bbb5a` |
| accepted580 JVM XML audit | `9c5a997ac94a6cb8ca4ba553b0c29759ad64f2cf14da15bc2c3abd1fc33b011c` |

Finite proof complete, subject to parent independent review. All own calls
terminal; no whole-task closure, new numerical evidence or follow-up campaign.
