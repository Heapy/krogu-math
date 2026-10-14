# Finite decimal square-root verification

Task 563 detects a finite exact decimal square root before input normalization,
decimal formatting or precision-derived working powers. For positive stored
coefficient `c`, an even scale requires `c = r²`; an odd scale requires
`c = 10r²`. Integer division and square-root remainders establish these identities.
The raw root scale is `scale / 2` for even scales and `(scale.toLong() - 1) / 2`
for odd scales. Rounding the root needs its own digits, and preferred-scale
padding adds at most one zero.

This avoids false arithmetic failures at very large valid MathContext precision
and negative scale boundaries. UNNECESSARY still permits discarding only zero
digits. The [parity/range proof](decimal-exact-root-563-parity-proof.md) and
[classification evidence](decimal-exact-root-563-scope-evidence.md) state the
complete scoped argument. Inexact-root and zero/null policy work remains in
tasks 542/549.

The isolated source snapshot was tested with Gradle 9.8.1, Kotlin 2.4.21 and
Liberica 25.0.4+9-LTS. Parent independently parsed raw XML against the annotated
methods in the selected source files:

| Target | Suites | Methods | Failures/errors/skips |
| --- | ---: | ---: | ---: |
| JVM | 16 | 54 | 0/0/0 |
| JS/Node | 7 | 36 | 0/0/0 |
| Wasm/Node | 7 | 36 | 0/0/0 |
| macOS ARM64 | 7 | 36 | 0/0/0 |

The selections include all new root/callback controls and retained scale,
integer-root, null and portable oracle suites. The selected 1,656 callback rows
remain archived with their whole fields and original indices; truthful numeric
and explicit caller/super controls supply separate requirements. The 22 compact
reference failures have independent coefficient/scale expectations and retain
their original JDK observations. This classifies the selected cases without
waiving other poisoned-subclass outcomes or changing the original fixtures.

A fresh isolated JVM JAR also passed 3,458 bounded checks, including all rounding
modes, Int.MAX_VALUE precision, negative odd/extreme scales, ties/carry,
UNNECESSARY zero tails and truthful/caller-super controls. Its SHA-256 is
`19e02a1eba926952dc78c7e7b0efa08bca952b88df73785ede29cccff9ff960d`;
the audit verified all four own math class origins. The first audit invocation
failed its source-hash argument guard before arithmetic; the corrected invocation
passed with the same script/source/JAR. Both logs are retained in the isolated
handoff. This is finite evidence, not an official JCK result.

Integration preserves the accepted binary-conversion and formatter changes.
Parent checked the exact sqrt section from isolated BigDecimal SHA
`c1505e2db8fc80d13b32d203d810722b3125b1f3d419e2f66b1189d41aa507f6`
against the surrounding `9557992` source. The combined BigDecimal SHA is
`c632db6b20fec28882bdd052f09f0a0dc4c52cfc3046237cd8fe46efbd38d810`.
All 18 other source/test/proof/static-evidence files were copied byte-identically.
Staging subsequently identified one generated blank line at EOF. Main removes
that blank line and normalizes the inventory generator's final newline; the
1,656 rows and all their fields remain identical to the isolated archive.
The combined tree subsequently passed a focused selection of the new roots,
Kotlin operators/conversions, binary prefixes and plain-string preflight:
36 methods/10 suites on JVM and 24 methods/six suites each on JS, Wasm and
macOS ARM64, with exact inventories and no failures, errors, skips or omissions.
This separate selection does not replace the isolated retained-suite checks.

Its fresh JAR SHA-256 is
`d613a5e79c08133adf5b00e81b1e8f85bc466856d4a65950ccf6dea85903681c`.
It contains the numeric, text and operator extension classes and passed the same
3,458 bounded sqrt checks with all four math class origins verified. The JAR was
built by the JVM test task after the combined production source was frozen.

The original public OpenJDK `BigDecimal/SquareRootTests.java` also passed in both
baseline and port, with all assertions retained: two actual invocations, zero
failures. Run `1792019462820` used that exact JAR and verified own-core loading
plus all three test-class bytecode audits without host-math members. Its result
JSON SHA-256 is
`e9ecc4e2bc83863a0193f5d6f5bb0c233a1b95d4dc7ae91db4673fcffb2055c8`.
The other 138 harness records were outside this targeted selection. This does
not replace the earlier broad corpus ledger or resolve its prime timeouts and
large-memory coverage gaps. Official JCK was not run.

Supported-platform CI at the new integrated head remains the final task 563
acceptance gate; earlier-head CI does not prove that gate.

`scripts/inventory-decimal-exact-root-callbacks-563.main.kts` can verify the frozen
callback projection and path structure from the integrated repository root.
The separate `certify-decimal-exact-root-563.main.kts` intentionally verifies the
original isolated branch/baseline; see the
[evidence scope](../diagnostics/decimal-exact-root-563/README.md).
