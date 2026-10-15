# Preserved pow working-precision observations

SPDX-License-Identifier: Apache-2.0

`DecimalPowerBoundaryTest.before.kt.txt` is the complete original own JVM test
from base37f55f2de12a9e30b8d2bda114fd986e21b0e83b, preserved byte-for-byte:

```text
017338c7d11eba085c936692d67cadf49ef202746531dc49bdd70289003dc938
```

Its boundary method has128 inputs: sourceONE, destination precisionsMAX,
MAX-1,MAX-2,MAX-10; exponents0,1,10,999999999; all eight rounding modes. It
originally required own value/error/message equality with public JDK results.
Its unrelated accumulator method is still unchanged in the compiled test.

The revised boundary method prints every pinned-reference result as:

```text
POW_WORKING_PRECISION_REFERENCE<TAB>1@0<TAB>precision<TAB>exponent<TAB>mode<TAB>original-outcome
```

The outcome keeps the original formatter: successful `toString()`, or
`!ExceptionSimpleName:message`. The marker does not classify diagnostic wording
as required behavior. The first combined JVM run executed both boundary methods
successfully and retained the raw128-row capture in its archived XML under
`/private/tmp/krogu-root526565525-validation-v1/xml-jvm-initial`.
The subsequent complete four-target gates and standalone TSV audit passed;
see [the integration record](../../docs/math-port-integration-verification.md).
Retain all128 rows, including successful reference values; no sampling, message
normalization or reconstructed runtime outcomes are authorized by this document.

After528 released the local window, the parent ran this alongside new
common `DecimalPowWorkingPrecisionContractTest`, the retained accumulator and
pow/oracle regressions, with pinned Gradle9.8.1/Kotlin2.4.21/Liberica25.0.4+9-LTS.
The invocation records its complete command, source/JDK identity and
raw XML/logs, verify all128 keys exactly once, and preserve every failure. No
new build window or numerical execution is granted here. See the
[proof and scope](../../docs/decimal-pow-working-precision.md).

The first full JVM command ended with360 methods,359 passes and one failure in
`PortableOracleTest`: its generated plain-ONE boundary rows still required the
same JDK working-precision error. Its complete pre-classification source is
preserved as `PortableOracleTest.before.kt.txt`, SHA256
`4453b8e28ba2a9a1a4b604343e9758d175c810c913ef7e728bed7e3f7481d662`.
The generated `Jdk25Vectors.kt` remains unchanged at SHA256
`cb4d2327da9850e347dd1db6b96519bb228a0072ccd778b8372a647cd7f1ac4f`.
The narrow classification retains all128 exact keys and all64 error/64 value
diagnostics, independently requires the mathematical result1, and checks the
complete key bijection. Other rows, indices, callback handling and assertions
remain unchanged. Initial raw logs/XML remain archived; independently reviewed
full JVM retry passed, followed by JS/Wasm/macOS ARM64.

[`reference-outcomes.tsv`](reference-outcomes.tsv) is the actual audited capture:
128 unique original keys,64 values and64 diagnostic errors with their original
messages. SHA256 `0e23103fc29a148a15787dbe79f0d67befdcac64667e78ecd00f97125f0c00ec`.
The auditor checks both exact JVM method identities and zero errors/skips, and
records XML/source/supplied-JAR hashes. Supplied-JAR metadata alone does not prove
test-JVM/build provenance; the separate integration ledger supplies that context.
