# 581 source preparation handoff

**UNCOMPILED / UNRUN / NOT INTEGRATED.** Parent2335 authorized source work only
in `/private/tmp/krogu-math-task-581.xC0S84dl/root`, base
`80987e04f11a5abd8bc45b4646bcfe5bd0312043`. The grant is retained outside root
at `/private/tmp/krogu-math-task-581.xC0S84dl/root-source-grant.txt`; the
independently accepted proposal is
`/private/tmp/krogu-math-task-581.xC0S84dl/accepted-proposal.md`, SHA
`03337ca552cfd18bde80382fc56977c50457701f65d5fa895b93fd743f82a963`.
No publication-root edit, compiler, script, numerical helper, certificate, probe,
test, build, measurement, Git or tracker action occurred. No execution is queued
or implicitly granted. The worker is terminal after this finite handoff.

## Prepared files

| File | SHA-256 |
|---|---|
| `src/commonMain/kotlin/io/heapy/krogu/math/BigDecimal.kt` | `24e4e63001ea72a2fed9d29361f3707ad59cbe59c0547d47a648993146cf0849` |
| `src/commonMain/kotlin/io/heapy/krogu/math/BigInteger.kt` | `e284f3aac0a09d293784d38265d00656dec632d18a61985c756dc42c0c4d6d31` |
| `src/commonMain/kotlin/io/heapy/krogu/math/DecimalDivision581.kt` | `9f882c2ee58fc6e38d41819e7d258d382088a7a9ecb0f8fd538b296f7b484446` |
| `src/commonTest/kotlin/io/heapy/krogu/math/DecimalContextDivision581Test.kt` | `c198132e64c3ac1f9815c6227b2322d041d972f079207edb6d0eec94fa74a6e1` |
| `src/jvmTest/kotlin/io/heapy/krogu/math/DecimalContextDivision581SubtypeJvmTest.kt` | `12fad4905a903927a630b227da5cc8d24ba98959d2fc724bda9d07355f7f2c41` |
| `docs/decimal-context-division-581-implementation.md` | `90b51a81ae062008c273b997f31214fee70858add8e960b41480fcdb71f47111` |

Original BI/BD/proposal copies remain read-only at
`/private/tmp/krogu-581-originals.dyPRTKc2/`. Their hashes are respectively
`b2fb23547d97f3f7650b6d7dcf4ad53939efb3abac97d9d152376e67dd6685f5`,
`1c1559c44de0a51bfcd7df0c325dfb9de22e7b6a4588b6dcb612370dc850fa55`
and the accepted proposal pin above. Direct diffs show only three inserted
contextual-divide lines and one internal nonvirtual bridge. The new helper,
two tests and two documents are separate. Existing580/579 helper hashes remain
`2ef14e2a…`/`ce779a45…`; no other kernel, existing test/fixture, field, cache,
serialization layout or public declaration was changed.

## Review boundaries

The implementation argument binds actual lines for:

- a conservative old-path proof covering all powers/products, doubled remainder,
  final result*divisor, compact branches and the possible one-scale decrement;
- matching captured metadata with stored data, preserving the entire public
  zero/sign/precision/normalization prefix and old mismatched-probe fallback;
- a complete private quotient stream for failed safety proofs, all signed modes,
  guard/sticky, unread input tails, exact preferred cohort and one exact carry;
- an efficient p>H branch using supported own gcd/division/factor leaves on fresh
  base-class wrappers: nonterminating results necessarily exceed final range,
  while supported terminating coefficients already fit precision;
- checked paged indexing, Long scales/counters, primitive products, input
  isolation, seven simultaneous stream buffers and separate large-precision
  leaf lifetime/resource assumptions.

The5 common methods prepare independent bounded Long comparisons, multiword
trial borrow/carry, true ties/parity/sticky, required exact/inexact cohorts,
endpoint scales, pMAX exact/nonterminating controls and real public raw-bridge
coverage. The separate JVM method prepares current-own-format restored truthful
coefficients on both operands, with public LOW/HIGH stream and large-precision
controls. There are no callback count/order/noncall assertions, false production
caps, sentinel-only leaves or legacy serialization requirements.

The three symbolic accepted obstructions are covered by the failed-fast-bound
route: power-ceiling1/3,pH; below-cap10*A for A=2^(L-1),B10,p=digits(A); and
post-round C*B for A=2^L-1,B=2^(L-1)+1,p1/UP. These giant witnesses were not
constructed or run. Exact endpoint interval-clamping is absent; admitted560
diagnostics and qualified negative-power scale policy remain unchanged.

Parent must independently review source/proofs/test-oracle/seam before granting
any compilation or execution. Existing historical passes do not execute581.
Then only separately authorized focused/full target checks, exact artifact/CI/
public evidence and548 resource/performance work can establish their own gates.
No original timeout or precision is reduced, no task is marked complete, and no
official JCK, full-domain execution, performance or maximum-resource claim is made.
