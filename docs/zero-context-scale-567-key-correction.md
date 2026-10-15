# Unary oracle key encoding correction

The first integrated JVM run executed416 methods:415 passed and one failed.
The failure was PortableOracleTest's exact
finite-key set assertion. Its original log/XML and unchanged source inputs are
retained in `/private/tmp/krogu-root538567541-validation-v1/first-xml` and
`source-before.tar`; no retry preceded their preservation.

Normal C unary rows encode the reported sign in the operation suffix, for
example `[C,plusContext:-1,0.00,0,1,HALF_EVEN]`. Their fourth field is the
reserved literal0. The evaluator explicitly reads `substringAfter(':')` for
these operations. In contrast, nullAbs rows use the suffix for behavior and
the fourth field for the reported sign.

The first finite helper mistakenly used suffix0 and put the ordinary unary
sign in the reserved field. The live JVM consumer repeated that encoding, so
its independent loop passed while the frozen portable vectors exposed18
nonexistent expected keys:12 poisoned direct-plus keys and6 abs/sign1 keys.
The complete frozen input set and duplicate/set-equality assertions prevented
that metadata mistake from being accepted as complete coverage.

The correction changes only two helper key constructions and the live consumer
key construction to the actual unchanged schema. It preserves all212 selected
cases, numerical/exception/identity/trace expectations, healthy controls,
fixtures, generators and production. The finite proof's key spelling is
corrected accordingly. No additional policy or waived comparison is introduced.

The accepted source milestone's original proof/hash and all original source
files remain archived both in the isolated567 worktree and the first integrated
source snapshot. The subsequent complete replay passed416 JVM methods; JS,
Wasm and macOS ARM64 each passed277. Source-derived inventories report no
failures, errors, skips or missing methods. The
[integration report](context-modular-integration-verification.md) binds the
separate logs, frozen inputs and limits; the first failure remains preserved.
