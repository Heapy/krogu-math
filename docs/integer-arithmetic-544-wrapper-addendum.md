# Task 544 wrapper revision

SOURCE ONLY; all new task 544 tests remain UNCOMPILED/UNRUN. The original proof and handoff are preserved historical milestones. Current V3 supersedes their two test-source hashes and the original review's conclusion about wrapper recursion.

Parent independent review found a concrete test-only cycle: for a positive wrapper, `super.abs()` returns that wrapper; formatting it calls the own BigInteger formatter's virtual `abs()` again. The existing positive `inheritedPower(33)` and JVM `3^31` cases reach this path. No execution was needed to establish the cycle, and no production defect is alleged.

Each common/own-JVM/host-JVM test wrapper now stores an ordinary base instance constructed from the identical immutable constructor text. Its `abs` and `shiftRight` overrides derive the fresh truthful wrapper's text from that ordinary instance. Formatting therefore starts from an ordinary receiver, avoiding the identity-result cycle and any assumption about foreign formatter/export implementation bodies. The inherited arithmetic, explicit caller/super/self controls, event writes and arithmetic byte snapshots are retained. This is a private field in test fixtures only; no library state or kernel changes.

The existing positive-wrapper power assertions exercise the corrected path. Method counts remain eight common and four JVM. Existing huge-capacity limitations and task 548 separation remain in force; this revision establishes no compiler, runtime, arbitrary subclass or allocation result.

V3 also corrects one assertion's scope. An exact two-event comparison trace implicitly prohibited inherited metadata callbacks, although those callbacks are diagnostic here. The numerical comparison assertion remains; the trace check now requires only the explicit caller's first/last entry/exit markers. This does not impose an internal callback count or absence guarantee.

New test hashes:

- Common: `5b3b89ad12de50755dced8cd83881ba397ff699eba19e12c60e923d552df91e2`.
- JVM: `e4b71fe6309877026ff4694cdc5ced02ad1c804df1949f7ea1506f5761fdfee4`.

Original four-file source archive: `/private/tmp/krogu-544-source-v1-frozen/owned-source.tar`, SHA-256 `2e262aabb84ab106e70b0f85c6bdb926bb663663862f064b54265a47194cece6`. The five-file V2 archive preceding the assertion correction is `/private/tmp/krogu-544-source-v2-frozen/owned-source.tar`, SHA-256 `bca4b497e183b9ebeb409d5a16c71a0ce550b3aef941389fe8d5a967bf2c7997`. The parent owns independent follow-up review and later runtime selection. Unpublished constructor overlays and task 572 are separate policy decisions; neither is required by these new arithmetic fixtures.
