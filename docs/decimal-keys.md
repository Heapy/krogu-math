# Numerical decimal equality and collection keys

`BigDecimal.equals` and `hashCode` still include scale: `2.0 != 2.00`.
Explicitly import `numericallyEquals` to compare exact decimal values:

```kotlin
import io.heapy.krogu.math.BigDecimal
import io.heapy.krogu.math.numericallyEquals
import io.heapy.krogu.math.toDecimalKey

check(BigDecimal("2.0").numericallyEquals(BigDecimal("2.00")))
val prices = hashMapOf(BigDecimal("2.0").toDecimalKey() to "two")
check(prices[BigDecimal("2.00").toDecimalKey()] == "two")
```

`DecimalKey(value)` and `value.toDecimalKey()` are equivalent. Keys are immutable,
final, and only equal other keys. `BigDecimal`, `BigInteger`, primitive numbers,
JDK decimals and keys never become cross-type equals operands. Convert explicitly
before inserting or looking up values. Natural ordering only accepts `DecimalKey`;
it compares as zero exactly when keys are equal, so hash and sorted collections
agree. Different signs and different nonzero values remain distinct.

Each API observes the coefficient and scale through the source's public getters.
A key snapshots those observations at construction; later subclass getter changes
cannot change the key. `numericallyEquals` compares two such snapshots, without
calling an overridable `compareTo`, `precision`, `signum`, or `stripTrailingZeros`
on the source. Getters must describe a coherent value during the call. Concurrent
mutation of a subclass is not an atomic snapshot guarantee.

All zero representations share one key. Nonzero keys remove coefficient trailing
zeros and retain a Long canonical scale, including scales below Int.MIN_VALUE.
For example `(100, Int.MIN_VALUE)` equals `(1000, Int.MIN_VALUE + 1)` as keys even
though `stripTrailingZeros()` cannot express their canonical scale as an Int.
Ordering uses the sign, Long adjusted exponent, and zero-padded significand
comparison without allocating huge powers or padding. The original BigDecimal
retains its coefficient, scale, equality, and hash.

Construction converts the coefficient to decimal text, scans its digits, and
caches the canonical hash. It needs O(d) additional digit work and retained
storage, in addition to the coefficient's decimal conversion cost. Hashing an
existing key is O(1); comparison takes O(d) in the worst case. Reuse keys when
performing repeated collection lookups. `numericallyEquals` constructs temporary
keys and has the corresponding cost.

A diagnostic JVM test measures creation plus first hash and volatile publication,
repeated cached hashing with a rolling checksum,
`compareTo` on the same value, and `stripTrailingZeros` plus hash where valid:

```shell
KROGU_MEASURE_DECIMAL_KEYS=1 ./gradlew jvmTest --tests '*DecimalKey536JvmTest*' --rerun-tasks
```

Publication prevents construction elimination; a separate publication baseline is
reported. Repeated hashing has its own warmup. These raw loop timings include
harness overhead, are not allocation measurements, and do not establish the cost
of every escaping-key workload.

The measurements are observational, with no timing assertions or acceleration
claims; `compareTo` and stripping do different work and are contextual baselines.
The stripping baseline is omitted for canonical-scale underflow. See
`diagnostics/decimal-keys-536/README.md` for the recorded run.
