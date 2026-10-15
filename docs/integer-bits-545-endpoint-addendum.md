# Task 545 bounded endpoint addition

SOURCE ONLY; all task 545 tests remain UNCOMPILED/UNRUN. This addendum supersedes the V1 JVM source identity and method count only. The original proof/handoff and six common methods remain unchanged historical source milestones.

Parent review found retained live-JDK comparisons for shiftLeft(Int.MIN_VALUE) and shiftRight(Int.MAX_VALUE), but no paired zero cases for the opposite directions. The new JVM method `safeZeroShiftAndNegativeIndexEndpointsMatchRequiredValuesAndHost` adds exactly five independent own/JDK pairs: zero shifted right by Int.MIN_VALUE, zero shifted left by Int.MAX_VALUE, and zero setBit/clearBit/flipBit at Int.MIN_VALUE. Both shifts must return numerical zero; each negative-index operation must throw ArithmeticException. No identity, message or internal callback assertion is added.

Only zero receivers are used for the potentially growing directions. This does not authorize or establish the host allocation behavior of nonzero shiftRight(Int.MIN_VALUE), shiftLeft(Int.MAX_VALUE), or MAX bit updates; that resource work remains in task 548. The five bounded pairs supply ten primary endpoint operations plus formatting of the four shift results. Their results have not yet been observed.

The changed JVM source SHA-256 is `1dbbd3d65403bf6712c9d2c504480af680c22a4329eb7b55cbbc5427d099b020`. There are now six common and four JVM methods: future full inventories would be 136 suites / 473 JVM methods and 65 / 314 on each common target, totaling 1,415 executions only if every gate passes. The original source archive is preserved at `/private/tmp/krogu-545-source-v1-frozen/accepted-545.tar`, SHA-256 `3259aae9c92d00e6518d81aee38fd9036848b5c9ee2d6bf474646c78f2e5c882`; frozen V1 auditor remains `63d859d2…` and must not be used unchanged for V2.

No production, other pre-existing test, fixture, generator or 570/571 source changes accompany this addition. The parent owns independent source review, updated auditor preparation and all later serial execution grants.
