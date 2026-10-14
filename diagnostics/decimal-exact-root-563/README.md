# Task 563 evidence scope

The baseline and 19-artifact handoff manifests describe the isolated branch
`task/563-exact-root` at `85941d18908a8155d205b04d6e1a9b3e8f10b31e`.
They are historical source hashes, not a manifest of the integrated main tree.
Binary conversion, plain formatting and Kotlin extension changes are integrated
separately. The original numerical fixtures remain byte-identical.
Besides the composed BigDecimal source, main normalizes the generated callback
snapshot's final newline and its inventory generator accordingly. The archived
manifest retains the original two hashes; all callback fields and rows are equal.

`scripts/certify-decimal-exact-root-563.main.kts` preserves that isolated
branch/baseline guard intentionally. Its recorded success certifies the symbolic
identities and the original narrowly owned diff. Running it in main correctly
rejects the different checkout; it is not a main-tree build or CI command.

`scripts/inventory-decimal-exact-root-callbacks-563.main.kts` instead verifies
the 1,656 selected rows, whole archived fields, original indices, generated common
snapshot and finite-root entry structure in the integrated tree. It executes no
port or reference numerical implementation.

`callback-observations.tsv` retains the selected reference observations.
`retained-conflicts.tsv` retains 22 ordinary reference errors alongside independent
mathematical expectations. Their classification is specific to the cases in the
[proof](../../docs/decimal-exact-root-563-parity-proof.md); other outcomes do not
acquire a blanket guarantee or waiver.

See the [verification record](../../docs/decimal-exact-root-verification.md) for
actual isolated test/JAR results and the separate integration gates.
