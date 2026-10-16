# Input spelling correction before first execution

SOURCE ONLY; all247 sources and seven comparison-helper methods are still
UNCOMPILED/UNRUN. This successor is isolated at
/private/tmp/krogu-587-input-correction-21SB8qy2/root. Its complete manifest is
../evidence/input-corrected247-sources.sha256, SHA256
529cc142d9a42110084155987b3022b36c2309168a159b745bf5191a0359c5a4.

The prior247 composition a00c8ac3 at
/private/tmp/krogu-587-588-589-validation-1mYbbcTf/root is preserved unchanged,
as are frozen policy9/source246, exact-char589/source245 and initial failed244.
Policy proof/handoff hashes in this successor still identify their ORIGINAL
frozen revision. This document records the narrow subsequent correction.

During SOURCE-only ledger-auditor preparation, the author found that the
canonical-output parser was also being used for raw INPUT0.0000001. Its numeric
fields are1@7, whose canonical output is1E-7; strict canonical output validation
would reject the original input spelling. The independent mathematical reviewer
confirmed this static blocker. No compiler/test/helper was run to find it.

Root separated the existing bounded lexical field reader from strict output
validation. parse(text) still applies the SAME validate() and remains a strict
canonical-output parser. parseInput(text) instead returns normalized numeric
fields and retains the ORIGINAL spelling as Input.source. It retains the200
character, bounded exponent/Int-scale and155 coefficient-digit profile guards.
Its grammar is only the existing bounded source-profile grammar; it is not a
replacement for the production constructor grammar.

Portable's selected raw input uses parseInput; reference fields still use
strict parse. The test-only input adapter also uses parseInput. Existing seven
methods now explicitly distinguish source0.0000001 from canonical1E-7 with
the same1@7 numeric fields, reject0.0000001 as an output, and reject malformed
input/overflow scales. No production, fixture, original row/key, arithmetic
certificate, output assertion, selector, RNG draw, loop, deadline or resource
changes. No additional test methods or weaker output comparison were added.

Exactly THREE source hashes differ from a00: PortableOracleTest, the comparison
helper and its tests. All244 other source hashes, including589 BigDecimal5731,
588 helper e70e and all production/other consumers, remain unchanged. Root must
obtain independent correction/composition review before any separate execution
grant. The XML auditor is newly pinned to529c; its lexer/inventory/XML behavior
is otherwise unchanged and is itself UNCOMPILED/UNRUN. The separate complete
ledger auditor must be pinned and independently reviewed before its execution.

All earlier failures/unclassified468/unobserved tails remain historical open
evidence. No current test, complete ledger or public-harness success is claimed.
