# Public constructor tests under the non-null Kotlin API

This records the SOURCE-ONLY preparation for tasks 573/578. At that milestone the adaptation had not been compiled, generated or executed. Its later accepted execution is recorded in the [current verification report](../non-null-api-573-verification.md). Production constructors, accepted V4 sources/JAR, frozen public tests, assertions and historical results are unchanged.

The user approved non-null inputs throughout the API, explicitly including constructors, and permits Java adapters inside the public-test harness. Kotlin constructor entry therefore rejects a null `MathContext` before parsing a char-array range. Keeping a nullable public constructor merely to reproduce competing-invalid-argument precedence is outside the approved migration policy.

The first unchanged selected public campaign on corrected V4 completed with **29 PASS / 1 FAIL / 111 SKIP**, `complete=true`, shell exit 1. Its exact JAR SHA-256 is `ded66cd6a325b73a9a393e0fd56281d7bf63d571dab4b146e8f1cb9c0dba2fd1`. The failed invocation was `port/BigDecimal/Constructor/run-1`; the pinned JDK baseline passed. The port's string-constructor method passed and four char-array methods failed because they received entry NPE where the unchanged TestNG annotation expected NumberFormatException. Preserve that FAIL and all raw TestNG reports:

`/private/tmp/krogu-573525-public-execution-v1/root/build/math-tck/results/1792066236575/`

The source-specific adaptation in `tools/math-tck/adapt.main.kts` affects only generated port `BigDecimal/Constructor.java`. It replaces exactly four literal null context arguments with fully qualified `io.heapy.krogu.math.MathContext.UNLIMITED`:

| Original method | Preserved range operands | Context supplied to adapted port call |
| --- | --- | --- |
| `charArrayConstructorNegativeOffset` | `new char[5], -1, 4` | `MathContext.UNLIMITED` |
| `charArrayConstructorNegativeLength` | `new char[5], 0, -1` | `MathContext.UNLIMITED` |
| `charArrayConstructorIntegerOverflow` | `new char[5], Integer.MAX_VALUE - 5, 6` | `MathContext.UNLIMITED` |
| `charArrayConstructorIndexOutOfBounds` | `new char[5], 1, 5` | `MathContext.UNLIMITED` |

Each complete pinned call must occur exactly once before replacement. Each resulting complete call must occur exactly once during reversal, and reversing all four substitutions must recover the import-only source byte for byte. The original source lock and original copyright/GPL notice remain intact; the adapter's notice list records the newly adapted source's 1999/2018 attribution. No constructor wrapper or expected-exception bridge is introduced.

All numeric/range operands, method names, five TestNG expected-exception assertions and annotations remain. `stringConstructor` remains unchanged. The integer-overflow method still rejects a NumberFormatException whose cause is IndexOutOfBoundsException, using its original catch/rethrow body. Baseline runs the original test unchanged. This retains invalid-range NumberFormatException behavior with a valid context; it does not retain the original simultaneous invalid-range/null-context input in the adapted port invocation.

The runner receives one changes record and **four separate adaptation-companion SKIP/lostCoverage records**, one for each original joint-invalid call. Each states that original port precedence is not executed and links the frozen failed campaign. An adapted invocation's possible PASS must not be reported as success of those four original calls, nor may filtered or companion SKIPs replace the historical FAIL. This is a disclosed test-input adaptation to the approved Kotlin API boundary, not a claim that JDK precedence is a bug.

The single `BigDecimal/Constructor.java` row in `tools/math-tck/manifest.tsv` now declares disposition `adapted` and adaptation `four-non-null-contexts`. Its reason identifies the four UNLIMITED substitutions and retained assertions; its lostCoverage field records the original joint-invalid precedence, four companion SKIPs and frozen failed campaign. The path, standard resource profile and TestNG runner are unchanged. All other manifest rows and the source lock remain unchanged. This metadata correction prevents inventory/results from describing the changed port inputs as import-only or as preserving the original input domain. Refresh any prepared execution-v2 tool/manifest copy before launch; the original execution-v1 manifest and results stay frozen.

If root later repeats the same 15 selected files / 30 phase-directive invocations against the same V4 JAR with only this reviewed adapter and manifest-metadata change, and every actual invocation passes, the conditional top-level ledger becomes **30 PASS / 0 FAIL / 115 SKIP**: four new companion SKIPs in addition to the original 111. This is a planned count, not execution evidence. A Constructor-only baseline/port rerun would instead conditionally be **2 PASS / 0 FAIL / 142 SKIP** with the same manifest inventory and corrected Constructor metadata: 138 existing filtered/disabled/commented/excluded records plus four new companions. Do not merge different runner or manifest hashes into one unchanged-campaign result or overwrite campaign1792066236575.

Independent source review, pinned metadata generation/reversal validation, actual javac/bytecode/core-origin/framework evidence and a later explicit sole-window grant are still required. Keep seed559, original directive timeouts, profiles and heaps; preserve all failed attempts. The known historical PrimeTest FAIL and full-suite/range/resource gaps remain separate. Official JCK/TCK is false. No execution or public compatibility improvement is claimed by this source milestone.
