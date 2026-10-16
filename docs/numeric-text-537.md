# Numeric text parsing and writing

Status: source draft; compilation, platform tests and performance measurements are pending.

`BigInteger.parseText` and `BigDecimal.parseText` accept a coherent `CharSequence`,
either whole or by offset and length. `BigInteger.parseChars` and the existing
decimal character-array constructors read array slices directly. They retain no
reference to caller storage after returning. Callers must keep mutable input
unchanged during the synchronous call; these APIs do not provide an atomic
snapshot of concurrent modifications. A reader exception propagates unchanged.
No `toString`, `subSequence`, whole-slice array copy or normalized digit String
is required by the ordinary parsing path.

`parseTextBounded` and `parseCharsBounded` add an explicit character-count limit.
After overflow-safe slice validation, a negative limit fails with
`IllegalArgumentException`, and an exceeded limit fails with
`ArithmeticException("Input character limit exceeded")` before reading characters.
Existing constructors acquire no new configurable limit. Supported magnitude,
scale, malformed-text diagnostics and Unicode digit grammar remain their existing
contracts. ASCII digits take a direct path; other characters use the existing
Kotlin digit conversion. Decimal exponent and scale validation finish before
coefficient construction. The existing precision-prefix constructor machinery is
retained for bounded precision; this change also removes the intermediate digit
String for unlimited parsing.

For example:

```kotlin
val integer = BigInteger.parseText(StringBuilder("[ff]"), 1, 2, 16)
val decimal = BigDecimal.parseTextBounded("[12.3400]", 1, 7, 32)
val destination = StringBuilder()
decimal.appendTo(destination, DecimalTextFormat.PLAIN, maxChars = 32)
val buffer = CharArray(32)
val written = integer.writeTo(buffer, offset = 2, length = 30, radix = 16)
```

`appendTo` returns the number of characters written as `Long`. `writeTo` returns
the number written into the supplied array slice as `Int`. Integer radix fallback
and decimal `CANONICAL`, `PLAIN`, `ENGINEERING` layouts match the corresponding
existing String formatters. These are additive APIs; existing signatures and
canonical String caching remain available. Default parameters are Kotlin defaults;
Java callers use the explicit full signatures, without new ambiguous constructors.
Byte encodings are deliberately left to an explicitly chosen caller charset.

Output is planned in `Long` before the first destination write. A nonnegative
optional `maxChars` bounds output length; an insufficient buffer or exceeded limit
throws `ArithmeticException("Output character limit exceeded")`. Invalid buffer
slices throw `IndexOutOfBoundsException`. Those failures leave the buffer unchanged.
Plain output with extreme scale can therefore be rejected without allocating or
traversing scale-sized padding. Successful unlimited streaming may intentionally
emit more characters than fit one String. Padding uses blocks of at most 256
characters; digit chunks have bounded radix width. No destination is closed or
flushed. An external Appendable can fail after partial output, and its exception
propagates unchanged.

Exact base instances use private stored magnitudes. Subtypes retain their existing
virtual String formatter dispatch. Their formatter can allocate or invoke arbitrary
callbacks before its resulting length is checked; the new limit does not bound a
subtype's own implementation. The destination remains untouched on that length
failure. Parsed inputs and written arrays never become mutable backing storage of
a published number.
Decimal output also falls back when a retained coefficient is a BigInteger subtype
(for example after supported deserialization), preserving its virtual formatting.

## Arithmetic and ownership proof

Integer input is grouped by the largest `radix^width <= Int.MAX_VALUE`. For a
base-2^30 scratch cell `a`, multiplier `p` and incoming carry `c`, the invariant
`0 <= a < B`, `0 <= c < p`, `0 < p <= Int.MAX_VALUE` gives
`a*p+c < B*p < 2^61`. Group values are below their group power. The final carry
may span two cells and is consumed in a loop. Scratch is geometrically grown,
range checked and copied on immutable publication. Decimal groups use width nine.
Before parsing each integer group, a conservative upper bit bound proves all
prefixes fit. Near the supported bit boundary, the parser resumes the existing
public scalar multiply/add path so overflow versus later malformed-digit priority
does not change. Length-based definite overflow still validates malformed digits
before rejection, as before.

Formatting divides a private scratch copy by `radix^width <= Int.MAX_VALUE`.
For each long division step, `part = remainder*B + cell < divisor*B < 2^61`;
the quotient cell stays below B. Little-endian remainder chunks belong only to
the output plan. The plan computes digit count and decimal point/exponent/padding
lengths in Long; no scale negation in Int or scale-sized temporary is needed.
Complexity remains quadratic for grouped conversion of sufficiently large general
radix inputs; no asymptotic speedup is claimed.

529 coordination: numeric readers, grammar/group validation and format layout are
owned by 537; compact/wide representation and primitive construction are owned by
529. The current private `ParsedMagnitude537.publish(sign)` boundary publishes
owned scratch as an immutable value. Both workers agreed that this boundary may
gain a compact accumulator before allocating scratch and a primitive formatter
route. There must be no allocating computed `words` getter and no competing
parser rewrite. Final composition requires independent review and platform tests.

## Validation and measurement gates

Source tests cover frozen integer values/diagnostics across String, sequence and
array readers; all radices and group boundaries; Unicode and decimal contexts;
slice/limit ordering; canonical and alternate layouts; buffer sentinels; large
scale rejection; Appendable failures and subtype callbacks. JVM tests additionally
compare public JDK arithmetic/text results. Existing parser/588/subclass tests must
also pass on the composed implementation, plus applicable JVM, JS, Wasm and Native
suites. These checks have not yet run for this draft.

Measurements must use pinned JDK/toolchain, distinct source-bound baseline and
candidate artifacts, fixed shared inputs, fresh alternating processes, warmup,
escaping results, raw samples, allocated bytes where available and host diagnostics.
Existing 527 IDs remain unchanged: decimal `format` observes cached Strings and
cannot establish cold-format performance. New cold-format IDs must prepare fresh
equivalent number pools outside the timed and allocation intervals and observe each
object once. Construction-plus-format is a separate control. Actual throughput,
allocation, crossover and platform limitations will be recorded after execution;
no improvement is claimed from this source draft.

The draft JVM diagnostic is `NumericText537Probe` and
`scripts/measure-numeric-text-537.main.kts`. Package only the compiled probe and its
nested classes in a separate probe JAR, built against the candidate; the runner
loads that same probe JAR against each production JAR. New API references execute
only in the candidate branch. The baseline branch uses equivalent existing
constructors or copies the existing formatter result into the same-sized supplied
buffer. A fresh composed-parent worktree and artifact are required; preserve its
build command/log and exact commit alongside the candidate build, then supply both
source roots and explicit JAR hashes to the runner:

```text
kotlinr scripts/measure-numeric-text-537.main.kts \
  BASELINE_ROOT BASELINE_JAR BASELINE_SHA \
  CANDIDATE_ROOT CANDIDATE_JAR CANDIDATE_SHA \
  PROBE_JAR PROBE_SHA PROJECT/.codex-work/537/measurements/NEW_CAMPAIGN
```

Use the pinned BellSoft JDK 25.0.4+9-LTS and Kotlin 2.4.21. On macOS, whole-process
RSS collection with `/usr/bin/time -l` must run in an environment that permits its
sysctl calls. The runner does not build artifacts: matching source/build provenance
must be reviewed before launch. It hashes both production source trees, the script,
probe source/JAR, production JARs and runtime library JARs before the campaign and
rechecks them around every child. Three alternating fresh processes per artifact,
72 cases, at least five warm batches accumulating at least 200 ms of timed work,
and five samples yield 2160 expected measurement rows;
missing, duplicate or malformed rows fail completion. A 240-second child deadline
preserves partial evidence and reports any owned process cleanup survivors. The
20-minute campaign budget is explicitly a launch-admission limit, not a strict
wall deadline; bounded host snapshots and final child completion can extend it.

Cold-format inputs are distinct objects reconstructed before every batch; each is
formatted once. Reused buffer cases intentionally model repeated output of the same
value, and decimal canonical baseline output can use its existing cache. They must
be interpreted separately from cold format. Calibration starts at one operation,
doubles to a 20 ms target and caps at 2048. Slow cases can therefore contain one
operation; fast cases can stay below the target at the cap. Per-thread allocated
bytes include timed formatting/parsing and result publication, exclude prepared
input pools and destination arrays, and are not retained heap size. Whole-process
RSS includes setup, JVM/runtime and all cases, so it cannot be attributed to an
individual operation. This diagnostic is still uncompiled and unexecuted; its
source and baseline-load behavior require independent review and smoke validation.
Warmup has a 100000-batch guard and the enclosing child deadline; inability to
reach its timed-work target fails the diagnostic rather than silently reducing it.
Decimal format selection is prepared outside timing and captured as an opaque
value so the shared probe's baseline lambda signatures do not require the new enum.

Power-of-two radix writers preserve linear extraction: each output chunk selects
at most 30 bits from at most two adjacent base-2^30 cells, masks to its group power,
and writes through the same bounded-chunk sink. They do not use repeated division.
Existing power-of-two String formatting remains on its previous direct route.
