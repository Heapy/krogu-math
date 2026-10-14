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
