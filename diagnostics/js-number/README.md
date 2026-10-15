# Kotlin/JS Number dispatch control

This is a standalone compiler-checked diagnostic for local:557. It imports no
krogu code and builds no project modules. `NumberProbe.kt` uses only `Number`
and the standard library. The separate `NumberCoercionProbe.kt` tests JS hooks
as potential library-facing workarounds. Both execute unchanged compiler output.

## Run

From the repository root, with the already installed Kotlin and JDK:

```sh
JAVA_HOME=/Users/yoda/.sdkman/candidates/java/25.0.4-librca \
JAVA_OPTS=-Xmx512m \
KOTLIN_MAIN_KTS_COMPILED_SCRIPTS_CACHE_DIR=/private/tmp/number-probe-cache-unique-snapshot \
/Users/yoda/.sdkman/candidates/kotlin/2.4.21/bin/kotlin \
scripts/probe-js-number.main.kts baseline
```

Use a different cache directory for each script snapshot. Other modes are
`es2015`, `bigint`, `noinline`, `coercion`, and `timeout-check`; invoke them serially. No downloads,
Gradle, Native compiler, project build, or generated-JS rewriting are involved.
The installed distribution provides `bin/kotlin`; this exact executable was used.

The runner snapshots its input and itself in a fresh temporary directory and
prints its path. `transcript.txt` records input, commands, environment overrides,
versions, child exit codes, timeouts and SHA-256 hashes. Each command also has a
separate log capped at 512 KiB. Compiler processes have a 512 MiB heap and a
120 second timeout; Node has a 128 MiB heap and a 10 second timeout. Only the
runner's own directly launched process can be terminated, with process cleanup
and output-reader joins each limited to five seconds. The compiler is launched
as the pinned Java process with `org.jetbrains.kotlin.cli.js.K2JSCompiler`, without
shell launcher descendants. `timeout-check` deliberately stops an owned Node
loop after one second and requires completed cleanup; it does not compile a fixture.
Each compilation mode compiles source to a
KLIB, then links that KLIB with the matching installed JS stdlib into CommonJS.
The runner removes inherited `KOTLIN_RUNNER` so its compiler child is actually
the JS compiler. `kotlinc-js -version` prints its version and exits 1 for missing
compilation inputs; this is recorded separately from real compilation outcomes.

## Independent oracle and boundary

Conversion order is Byte, Short, Int, Long, Float, Double. The small constant
represents 42.75: integral overrides return 42 and both floating overrides return
42.75. The wide constant represents 9007199254740993: narrow overrides return 1,
Long returns that exact integer. The executed floating overrides return
`9.007199E15` for Float and `9.007199254740992E15` for Double.
Every returned floating constant is exactly representable. The second control
explicitly chooses Float 16777216 and Double 16777217 for the integer 16777217.
It tests preservation of the override bodies; it does not assume that all JS
Float values are rounded to 32-bit precision. Callback labels distinguish Float
and Double even when their results coincide. A throwing control also requires
each override's exception to remain observable.

Expected values come from literal override bodies and the intended virtual-call
contract, without a second numerical implementation. Concrete, Number-typed and
generic `<T : Number>` callers are separate non-inline functions. The program
collects all baseline rows before failing. A native JS exception in an option
variant can terminate that variant sooner; its complete output is retained.

Verified on 2026-10-14: Kotlin/JS 2.4.21, its bundled JS stdlib, Liberica OpenJDK
25.0.4+9-LTS, Node v24.21.0. Baseline, ES2015 and no-inline mode compile in both
stages and exit 1 in Node with **7 failing rows out of 11**:

| Receiver | Small / wide / rounded results | Callbacks |
| --- | --- | --- |
| Concrete custom class | Exact expected values | All six, in order |
| Number | `0, 0, 0, 0, NaN, NaN` | None |
| Generic Number | `0, 0, 0, 0, NaN, NaN` | None |
| Concrete throwing class | Six expected exceptions | Override bodies executed |
| Number throwing class | `0, 0, 0, 0, NaN, NaN` | Override exceptions bypassed |

The `bigint` variant (`-target es2015 -Xes-long-as-bigint`) also compiles in both
stages, but exits 1 before completing the rows. Its first concrete row passes;
the first Number row throws a RangeError converting NaN to BigInt during Long
conversion. This is a failed workaround check, not a complete 11-row result.

The `coercion` variant compiles both stages and fails **all 8 erased rows**.
Both `valueOf` and `Symbol.toPrimitive` preserve the concrete overrides but
bypass them on Number and generic calls. For the rounding witness, Float becomes
16777217 instead of the override's 16777216; Double remains 16777217. For the wide
witness, Byte/Short/Int/Long become `-1/-1/2147483647/9007199254740992`, versus
`1/1/1/9007199254740993` in the concrete control. Coercion therefore loses both
low bits and the exact Long. The last two Symbol hooks (Float and Double) both
receive the `number` hint. A stateful hook based on call counts or stack inspection
would not preserve arbitrary caller order and is not a valid general fix.

The baseline's generated **own caller expressions**, inspected without reading
any standard-library helper body, are:

```js
// concreteValues / roundedValues:
[n.toByte_edm0nx_k$(), n.toShort_ja8oqn_k$(), n.toInt_1tsl84_k$(),
 n.toLong_edfucp_k$(), n.toFloat_jhbgwv_k$(), n.toDouble_ygsx0s_k$()]
// numberValues / genericValues:
[numberToByte(n), numberToShort(n), numberToInt(n),
 numberToLong(n), numberToDouble(n), numberToDouble(n)]
```

Thus the compiled erased Float and Double callers use the same helper. No helper
body was inspected or copied. A library's independent Float and Double overrides
cannot both be selected by an ordinary single deterministic coercion hook with
the same input and hint. The coercion diagnostic checks that boundary directly.

## Public contract and official tracker

The public [Number API](https://kotlinlang.org/api/core/kotlin-stdlib/kotlin/-number/)
declares an abstract class, a constructor and six abstract conversions, including
on JS. The [inheritance specification](https://kotlinlang.org/spec/inheritance.html#overriding)
allows platform limitations on overriding, but does not itself identify this
Number behavior as an intended exclusion. The
[JS interop documentation](https://kotlinlang.org/docs/js-interop.html#equality)
documents the shared JavaScript Number representation for Byte, Short, Int,
Float and Double. That representation does not establish correctness of the
custom override dispatch observed here. Ordinary extensions cannot intercept a
member call under [member priority](https://kotlinlang.org/docs/extensions.html#extensions-are-resolved-statically).

The exact official issue is
[KT-79282: Virtual calls to custom Number implementors do not work correctly](https://youtrack.jetbrains.com/issue/KT-79282).
Its public record classifies this as **Bug**, **Backend. JS**, **Backlog**, affected
version **2.2.0**, with empty Planned for and Available in fields as checked on
2026-10-14. Its standalone Number-subclass example expects 42, but Number-typed
Double/Long/Int calls print NaN/0/0. A maintainer comment discusses the performance
cost of extra checks in conversion helpers. This is authoritative evidence of a
known compiler/runtime bug, not an unsupported-custom-Number ruling or a waiver
of this project's requirements. No fixed release is advertised in that record.

Related [KT-70277](https://youtrack.jetbrains.com/issue/KT-70277) is an open bug
about custom Number runtime type checks. Historical
[KT-17345](https://youtrack.jetbrains.com/issue/KT-17345) is a different constructor
TypeError, closed as fixed with IR; its generated-code-patching suggestions do not
resolve the present virtual-call bug. Tracker records were read through the
public YouTrack REST API because the web renderer could not retrieve these pages.
No upstream messages, comments or issues were posted.

[Compiler CLI documentation](https://kotlinlang.org/docs/compiler-reference.html)
and the installed compiler's captured `-help` / `-X` establish the options used.
`-Xes-long-as-bigint` changes Long representation; `-Xno-inline` disables method
inlining. Neither is documented as a Number dispatch fix. Advanced options are
explicitly non-standard. Only Kotlin 2.4.21 is installed, so this work makes no
claim to have tested other releases and does not change the project toolchain.

## Evidence snapshots

### Executed backend-phase listing

The new `phase-list` mode executed under parent1919 after the combined four-target
math gates and public PowTests/SquareRootTests were terminal. It is a separate
diagnostic step for task557. A fresh source-bound script cache compiled the
unchanged own fixture to KLIB, then added `-Xlist-phases`
only to the JS linking command. It does not execute the fixture in Node.

The pinned2.4.21 advanced-help capture from the earlier root baseline replay
`krogu-js-number-baseline-8594182732502465407/advanced.log` advertises that flag,
as well as phase-dump/filter controls. Its SHA-256 is
`e3092e9d313cb594157a25cd6c3a25faa374aa0f6ccd728209513158fe1def17`.
The mode checks the current compiler's help again before using the flag, retains
the existing direct compiler/time/heap/output bounds, and fails if the link log
reaches its512KiB capture limit. Existing numerical modes and both fixture
sources are unchanged.

An exit0 for this mode means only that the requested compiler commands finished;
its final marker explicitly reports `number_tests_executed=false` and
`node_exit=NOT_RUN`. Inspect the actual captured phase names and log before
selecting a later phase dump or disable experiment. No phase names are guessed,
no phase is disabled here, and no generated stdlib numerical helper body is
inspected. Unknown or unhelpful listing output is inconclusive. A later probe
must distinguish KLIB creation from JS linking and verify both Number and
generic callers plus concrete controls; listing phases is no compatibility fix.

Actual parent run: script exit0; KLIB and JS-link commands exit0, all owned
processes terminal without timeout/cleanup. The version-only CLI exit1 is the
already documented identity-preflight behavior. Runner SHA256 is
`7d5fd8544fad3e2e18fdc5a7b795d156288faf5577721698930de6a4098de6b7`;
the unchanged fixture is `2a2785f98fc7d9a77e85dd9fe639a57bc028596a0f1d0ec026752f717a28fe26`.
Scratch directory: `krogu-js-number-phase-list-296742234258679469` beneath the
same macOS temporary root as the retained snapshots below. Full parent log is
`/private/tmp/krogu-root526565525-validation-v1/js-phase-list.log`, SHA256
`8262ee6a05c13cd0bcd92114ecd4cec34f2a0a112612341c74654303d5fd2337`.

The actual [phase catalog](phase-list-2.4.21.txt), SHA256
`804d07a1e01283af9db5097282b888f1695efa694027c8035c5f1c3235007381`,
contains `JsBridgesConstruction`, `AutoboxingTransformer` and `CallsLowering`.
These are observed phase names, not identified causes. No phase was disabled,
no own-caller IR dump was produced, and no compiler integration was validated.
The terminal marker explicitly reports `number_tests_executed=false` and
`node_exit=NOT_RUN`; task557 and both original Number regressions remain open.

The official KT-79282 record was rechecked through its public API on2026-10-15:
it still reports Bug/Backend.JS/Backlog and no Planned for or Available in value.
This remains an external bug diagnosis, not a waiver of Number/generic dispatch.

### Retained executed snapshots

The baseline and ES2015 snapshots use the same fixture hash:
`2a2785f98fc7d9a77e85dd9fe639a57bc028596a0f1d0ec026752f717a28fe26`.
Their runner snapshot predates addition of the coercion mode; the executed
runner source is retained in each scratch directory.

| Mode | Scratch directory beneath `/private/var/folders/yh/h124ks9x1vqd0v6sz6slshkc0000gn/T/` |
| --- | --- |
| baseline | `krogu-js-number-baseline-3939095111691984101` |
| es2015 | `krogu-js-number-es2015-2640360229979016417` |
| bigint | `krogu-js-number-bigint-7045780199116409869` |
| noinline | `krogu-js-number-noinline-3046121405550956721` |
| coercion | `krogu-js-number-coercion-9903064912320245088` |

Coercion fixture SHA-256:
`ba986e18cfbe25d267d1072979b47935863d9264297c2ebb4c8696595fd86339`.
Historical wrapper-runner SHA-256 (used by bigint, noinline and coercion):
`53cc155ef40a0674a4709be710b4b95a64a2a12f72b0db4820f0da4272bd59f0`.

Baseline generated JS SHA-256:
`8033cecda7e5339ebc6f630b9ddb7c85cc19848c1fd9e1d2a7e6d7bbaa11527a`.
Installed compiler JAR SHA-256:
`dc96436fac5160a39a071f3b82d7683f2912bbbf19fe7e26be66533c045a824e`.
Installed JS stdlib SHA-256:
`1c2181c291848fdc00161dce3ab6f886a04a6085fa7beb27e4e436f301e8075c`.
Public KT-79282 API snapshot: `/private/tmp/krogu-js-number-557-KT-79282.json`,
SHA-256 `b85ba73d0528c6e12ea349acdefa132c1f002ea62c0f544a9fa7430e07ae5ac6`.

The initial runner attempts failed before compiling the fixture due to inherited
launcher mode and the CLI's version-only exit code. They are not reproduction
evidence. The successful compiler runs above separately establish the actual
source-to-JS boundary.

## Parent runner correction and replay

Independent review found that terminating only the historical shell launcher
could leave its JVM descendant alive and make an unbounded reader join hang.
The current runner directly launches the installed JVM compiler, bounds process
cleanup and reader joins, and treats cleanup/capture failure as a failed run.
No generated caller or Number fixture was changed. The executed parent-replay
runner snapshot, before the prepared `phase-list` addition, has SHA-256:
`2e90330308727dcb2a79d7d42d678b59894a96d4ea22b0a0a2c637d9ea1d10c3`.

Parent replay on the same pinned toolchain completed:

| Mode | Compilation | Terminal result |
|---|---|---|
| `timeout-check` | No compilation | Exit 0; owned Node loop exceeded one second, was terminated, and recorded `alive=false` with completed cleanup |
| `baseline` | Both stages exit 0 | Node exit 1; same seven failing rows out of eleven |
| `coercion` | Both stages exit 0 | Node exit 1; all eight erased rows fail |

Logs are `/private/tmp/krogu-root-js-number-timeout.log`,
`/private/tmp/krogu-root-js-number-baseline.log`, and
`/private/tmp/krogu-root-js-number-coercion.log`. Each log identifies its retained
input/runner snapshots and complete command transcript. Baseline replay records
wide Float `9007199000000000` and Double `9007199254740992`, correcting the
previous description that conflated those executed values. The expected
diagnostic failures reproduce the external bug; they are not passing Number
compatibility tests or a production workaround.

## Next decision

Keep the enabled common regression tests and local:557 in progress. These
standalone results establish an independent upstream boundary; they do not prove
krogu's full numerical behavior or close local:525. No production, test, build,
toolchain, root documentation or Git state was changed in the original worker phase.

The parent must assign the next narrow fix scope. A candidate fix must make the
unchanged Number and generic callers return all six expected values, including
the exact wide Long and independently chosen Float/Double results. A helper
mock, generated-code splice, concrete cast, renamed extension or platform skip
does not meet that condition. A compiler fix or separately justified compiler
integration needs its own review and consumer verification before any toolchain
change. Until then this remains a demonstrated external compiler bug with no
validated production workaround.
