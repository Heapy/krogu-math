# Contributing

Keep all arithmetic in `src/commonMain`; JVM code may adapt Java APIs and
serialization, but must not provide an alternate arithmetic implementation.

Use only public Java SE API specifications and black-box behavior as references.
Do not copy or mechanically translate implementation sources from OpenJDK or
other arithmetic libraries. Contributions must be original work that can be
licensed under Apache 2.0. Preserve `LICENSE` and `NOTICE`.

For a compatibility fix, reproduce the observable difference against the pinned
JDK, add a regression to the portable oracle generator or common tests, and run
JVM plus the available JS, Wasm and Native tests. Keep fixture generation
deterministic. Compare exception types; message wording is not a compatibility
guarantee. Use `.main.kts` for repository automation scripts.

Use Gradle 9.8.1 through the wrapper. Do not replace the common arithmetic by
platform BigInt/BigInteger/BigDecimal delegation.
