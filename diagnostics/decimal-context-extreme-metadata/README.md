# Extreme replacement-metadata observations

These 8,192 public-JDK probes are diagnostic evidence, outside the normal KMP test source sets. They override BigDecimal metadata while leaving the stored coefficient unchanged. They are not a corpus of required numerical results.

The public [BigDecimal precision contract](https://docs.oracle.com/en/java/javase/25/docs/api/java.base/java/math/BigDecimal.html#precision()) defines precision from the digits of the unscaled value, including precision one for zero. Actual coefficients here have 1–33 digits. Every source and divisor instead reports zero, a negative Int endpoint, or a positive value near Int.MAX_VALUE. All 8,192 rows violate that invariant for both operands. Forced signum additionally conflicts with the coefficient in 1,536 source and 512 divisor observations. [signum](https://docs.oracle.com/en/java/javase/25/docs/api/java.base/java/math/BigDecimal.html#signum()) defines the sign of the represented value.

This finding concerns the actual recorded inputs. It does not exclude extreme scales, methods, contract-preserving subclasses, or language-level dispatch. In particular, the bounded valid-input scale defects tracked by local:554 remain required mathematical regressions.

The data includes coefficient/scale/metadata/context inputs, result or exception text, and callback traces from BellSoft Liberica 25.0.4+9-LTS. Its classification is **contract-violating subclass input**. Exact results, messages and internal traces remain observable reference data. Differences do not establish a required port defect or prove a JDK defect on these inputs.

The original generated data and evaluation source are retained byte-for-byte:

| Artifact | SHA-256 |
|---|---|
| Jdk25DecimalContextExtremeMetadataResults.kt | `ad85b1bc3ffa88446cc093cee162bfe33e2239611e5dc75c25ec3642c6e1688e` |
| DecimalContextExtremeMetadataOracleTest.kt | `2b22a717ca28e87721b90526617c60097b3210ef575125f0d6071b2ea2d87209` |

The generator retains the same inputs and public operations; its output location now points here. The archived evaluator shows the original port-comparison procedure. It deliberately asserts exact diagnostic equivalence and is not compiled by the default build. Its presence does not claim that the port matches the observations. A previous comparison found 1,072 mismatches; this archive does not repair or waive valid mathematical behavior to reproduce them.

From the repository root:

```shell
# Input classification uses stored evidence; no library build or JDK oracle run.
kotlinr scripts/classify-decimal-context-extreme-metadata.main.kts --check

# Reference reproducibility requires JDK 25.0.4+9 and Kotlin 2.4.21.
JAVA_TOOL_OPTIONS=-XX:-OmitStackTraceInFastThrow kotlinr scripts/generate-decimal-context-extreme-metadata-oracle.main.kts --check
```

The classification script derives digit counts and signs from the recorded small ASCII coefficients independently of krogu-math. A frozen hash check protects the diagnostic data. Regeneration without `--check` intentionally updates the stored reference observations and requires a new evidence review.

This is the first classified corpus in local:526, not a claim that all existing oracle families have been classified. local:531 records the former arbitrary-metadata compatibility investigation.
