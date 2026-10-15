# Retained primality timeout barriers

Resource campaign `results/1792000017580/results.json` completed; three port
timeouts remain FAIL, not SKIP. Seed559, exact jar54ec9af64675eba2a89d7cba68a1c404220cfaf3169bd1de8a31193514e31db0,
512MiB JVM heaps, assertions enabled, original operands/batches retained.

| Original invocation | Host completion | Port terminal time |
|---|---:|---:|
| BigIntegerTest default main | 9.107s PASS | 120.262s timeout FAIL |
| BigIntegerTest timeout=400 | 8.134s PASS | 400.172s timeout FAIL |
| PrimeTest default main | 1.708s PASS | 120.053s timeout FAIL |

Both BigIntegerTest runs used **subset=0**, not the upstream random subset.
Their retained stderr ends `Constructor: Passed`. Public main then calls
`prime()` and reports `Prime: Passed/Failed` only after that method returns.
Thus the stall is isolated to the public primality section, not arithmetic,
roots, bit operations, inverse, modular exponentiation, serialization or later
large operations. Those later assertions were **not reached** by these invocations.
No failure-value output preceded the timeout.

The public prime section first checks ten random100-bit prime consistency cases,
then seven Mersenne candidates with exponents **521,607,1279,2203,2281,3217,4253**
at certainty100, customer primes, known/computed Carmichaels and composite products.
These exact calls remain in the test; no batch/operand/round count was reduced.
The existing logs do not identify which individual BigIntegerTest input stalled.

PrimeTest separately completed its100001-known-prime checks and seeded non-prime
checks (both report SUCCESS), then printed `Checking first19 Mersenne primes`.
It timed out inside that Mersenne group, whose largest exponent is4253 and whose
input expression is `BigInteger.ONE.shiftLeft(n).subtract(BigInteger.ONE)` with
`isProbablePrime(100)`. The same seven large exponents occur in both tests.
This isolates a shared **large-Mersenne primality performance candidate**, not
a proved arithmetic error or a proven exact slow exponent/algorithmic cause.

Exact logs are under each port invocation's test.stdout.log/test.stderr.log;
full commands, class/bytecode proof, original timeout, exit/owned cleanup and
baseline comparison are retained. A future granted phase should time the listed
public inputs individually and retain exact returned results before selecting
any performance fix. No new math probe was launched after parent1790 requested
terminal handoff. No timeout is waived, no production source was edited, and no
JDK/stdlib/thirdparty numerical implementation was consulted.

## Later integrated campaign: the failure phase changed

Campaign `results/1792039864274/results.json` used fresh integrated JAR
`80ea49801d8de21aafc4ac76488a389363c706e3b1e2d801b8e346660e43fb68`, the same
seed559, subset0, original deadlines and 512MiB child heaps. Its final ledger
is 17 PASS, 3 FAIL and 120 SKIP. These results supplement the historical campaign
above; they do not rewrite it or establish a global public-test pass.

Both port BigIntegerTest directives still timed out, at 120.218s and 400.229s.
Their retained stderr now records `Prime: Passed` and `nextProbablePrime: Passed`.
The last completed marker is modular inverse for 2760 bits; the next TEST segment
is 1000 cases of 8000-bit inverse and inverse-product remainder verification.
This locates the next uncompleted phase without proving a particular internal
bottleneck. Task530 investigates stored division separately.

PrimeTest completed its known-prime, non-prime and first19 Mersenne sections,
then exited1 in 22.001s at `checkHugeFails`, rather than timing out. That TEST
requires `ArithmeticException` for `2^500000001+1` and discards the Boolean from
`isProbablePrime(1)`. A normal return therefore fails this assertion without
demonstrating a false positive. The value is divisible by3; own-source analysis
predicts small-factor rejection, but the returned Boolean was not recorded.

Independent review classifies this exact exception cutoff as a permitted range
difference: the [public API](https://docs.oracle.com/en/java/javase/25/docs/api/java.base/java/math/BigInteger.html)
states a minimum probable-prime range through2^500000000, with no mandatory
ceiling at that value. No production cap or test modification was made.
The original PrimeTest FAIL remains in the ledger. This is neither a proof of
the port's full primality range nor a JCK result.

All seven selected BigDecimal files passed in both phases. All ten port core
loading checks and twelve bytecode audits passed; no caught OOM was recorded.
See the [integration report](../../docs/context-modular-integration-verification.md)
for the complete frozen evidence and remaining work.
