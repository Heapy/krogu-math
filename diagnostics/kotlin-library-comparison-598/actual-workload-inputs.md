# Actual final v2 input inventory — #598

Derived from all1512 retained INPUT598 rows, not a proposed corpus. [Complete per-child table](actual-workload-inputs.tsv) retains both exact input strings, both sizes/signs, expected string, actual alias relation, peer/fork and raw-log path. [Machine-readable unique tuples](actual-workload-inputs.json) includes size semantics and peer/target membership. No numeric campaign was repeated.

Integer size is abs(value).bit_length; decimal size is the spelling-derived coefficient digit count and scale, not an object allocation or retained-size claim. Leading/trailing decimal spelling is retained. PARSE/FORMAT use the stated text input, and their input_b column is retained harness metadata, not a second timed operand.

| ID | Input A | Input B | Independent expected | Targets |
|---|---|---|---|---|
| `decimal/scale/ADD` | `1.20` | `3.400` | `4.600` | JS, JVM, Native-macOSArm64, Wasm |
| `decimal/scale/FORMAT` | `1.2` | `1.2` | `1.2` | JS, JVM, Native-macOSArm64, Wasm |
| `decimal/scale/MULTIPLY` | `1.20` | `3.400` | `4.08000` | JS, JVM, Native-macOSArm64, Wasm |
| `decimal/scale/PARSE` | `1.2` | `1.2` | `1.2` | JS, JVM, Native-macOSArm64, Wasm |
| `decimal/scale/SUBTRACT` | `1.20` | `3.400` | `-2.200` | JS, JVM, Native-macOSArm64, Wasm |
| `decimal/scale/self` | `1.20` | `1.20` | `1.4400` | JS, JVM, Native-macOSArm64, Wasm |
| `decimal/signed/ADD` | `-1.25` | `0.05` | `-1.20` | JS, JVM, Native-macOSArm64, Wasm |
| `decimal/signed/FORMAT` | `-1.25` | `-1.25` | `-1.25` | JS, JVM, Native-macOSArm64, Wasm |
| `decimal/signed/MULTIPLY` | `-1.25` | `0.05` | `-0.0625` | JS, JVM, Native-macOSArm64, Wasm |
| `decimal/signed/PARSE` | `-1.25` | `-1.25` | `-1.25` | JS, JVM, Native-macOSArm64, Wasm |
| `decimal/signed/SUBTRACT` | `-1.25` | `0.05` | `-1.30` | JS, JVM, Native-macOSArm64, Wasm |
| `decimal/signed/self` | `-1.25` | `-1.25` | `1.5625` | JS, JVM, Native-macOSArm64, Wasm |
| `decimal/wide10/ADD` | `100000000000000000000.0` | `-10.00` | `99999999999999999990.00` | JS, JVM, Native-macOSArm64, Wasm |
| `decimal/wide10/FORMAT` | `100000000000000000001.25` | `100000000000000000001.25` | `100000000000000000001.25` | JS, JVM, Native-macOSArm64, Wasm |
| `decimal/wide10/MULTIPLY` | `100000000000000000000.0` | `-10.00` | `-1000000000000000000000.000` | JS, JVM, Native-macOSArm64, Wasm |
| `decimal/wide10/PARSE` | `100000000000000000001.25` | `100000000000000000001.25` | `100000000000000000001.25` | JS, JVM, Native-macOSArm64, Wasm |
| `decimal/wide10/SUBTRACT` | `100000000000000000000.0` | `-10.00` | `100000000000000000010.00` | JS, JVM, Native-macOSArm64, Wasm |
| `decimal/wide10/self` | `100000000000000000000.0` | `100000000000000000000.0` | `10000000000000000000000000000000000000000.00` | JS, JVM, Native-macOSArm64, Wasm |
| `decimal/zero/ADD` | `0.00` | `-0.000` | `0.000` | JS, JVM, Native-macOSArm64, Wasm |
| `decimal/zero/FORMAT` | `0` | `0` | `0` | JS, JVM, Native-macOSArm64, Wasm |
| `decimal/zero/MULTIPLY` | `0.00` | `-0.000` | `0.00000` | JS, JVM, Native-macOSArm64, Wasm |
| `decimal/zero/PARSE` | `0` | `0` | `0` | JS, JVM, Native-macOSArm64, Wasm |
| `decimal/zero/SUBTRACT` | `0.00` | `-0.000` | `0.000` | JS, JVM, Native-macOSArm64, Wasm |
| `decimal/zero/self` | `0.00` | `0.00` | `0.0000` | JS, JVM, Native-macOSArm64, Wasm |
| `fixed-scale/both-negative` | `-1` | `-8` | `0.13` | JVM |
| `fixed-scale/carry` | `9.995` | `1` | `10.00` | JVM |
| `fixed-scale/exact` | `12.00` | `3` | `4.00` | JVM |
| `fixed-scale/negative-divisor` | `1` | `-8` | `-0.13` | JVM |
| `fixed-scale/negative-tie` | `-1` | `8` | `-0.13` | JVM |
| `fixed-scale/nonterminating` | `1` | `3` | `0.33333333333333333333` | JVM |
| `fixed-scale/positive-tie` | `1` | `8` | `0.13` | JVM |
| `fixed-scale/zero` | `0.000` | `7` | `0.0000` | JVM |
| `int/carry30/ADD` | `1073741823` | `1` | `1073741824` | JS, JVM, Native-macOSArm64, Wasm |
| `int/carry30/FORMAT` | `1073741823` | `1073741823` | `1073741823` | JS, JVM, Native-macOSArm64, Wasm |
| `int/carry30/MULTIPLY` | `1073741823` | `1` | `1073741823` | JS, JVM, Native-macOSArm64, Wasm |
| `int/carry30/PARSE` | `1073741823` | `1073741823` | `1073741823` | JS, JVM, Native-macOSArm64, Wasm |
| `int/carry30/SUBTRACT` | `1073741823` | `1` | `1073741822` | JS, JVM, Native-macOSArm64, Wasm |
| `int/carry30/self` | `1073741823` | `1073741823` | `1152921502459363329` | JS, JVM, Native-macOSArm64, Wasm |
| `int/signed/ADD` | `-7` | `3` | `-4` | JS, JVM, Native-macOSArm64, Wasm |
| `int/signed/FORMAT` | `-7` | `-7` | `-7` | JS, JVM, Native-macOSArm64, Wasm |
| `int/signed/MULTIPLY` | `-7` | `3` | `-21` | JS, JVM, Native-macOSArm64, Wasm |
| `int/signed/PARSE` | `-7` | `-7` | `-7` | JS, JVM, Native-macOSArm64, Wasm |
| `int/signed/SUBTRACT` | `-7` | `3` | `-10` | JS, JVM, Native-macOSArm64, Wasm |
| `int/signed/self` | `-7` | `-7` | `49` | JS, JVM, Native-macOSArm64, Wasm |
| `int/wide10/ADD` | `100000000000000000000` | `-10` | `99999999999999999990` | JS, JVM, Native-macOSArm64, Wasm |
| `int/wide10/FORMAT` | `100000000000000000000` | `100000000000000000000` | `100000000000000000000` | JS, JVM, Native-macOSArm64, Wasm |
| `int/wide10/MULTIPLY` | `100000000000000000000` | `-10` | `-1000000000000000000000` | JS, JVM, Native-macOSArm64, Wasm |
| `int/wide10/PARSE` | `100000000000000000000` | `100000000000000000000` | `100000000000000000000` | JS, JVM, Native-macOSArm64, Wasm |
| `int/wide10/SUBTRACT` | `100000000000000000000` | `-10` | `100000000000000000010` | JS, JVM, Native-macOSArm64, Wasm |
| `int/wide10/self` | `100000000000000000000` | `100000000000000000000` | `10000000000000000000000000000000000000000` | JS, JVM, Native-macOSArm64, Wasm |
| `int/zero/ADD` | `0` | `0` | `0` | JS, JVM, Native-macOSArm64, Wasm |
| `int/zero/FORMAT` | `0` | `0` | `0` | JS, JVM, Native-macOSArm64, Wasm |
| `int/zero/MULTIPLY` | `0` | `0` | `0` | JS, JVM, Native-macOSArm64, Wasm |
| `int/zero/PARSE` | `0` | `0` | `0` | JS, JVM, Native-macOSArm64, Wasm |
| `int/zero/SUBTRACT` | `0` | `0` | `0` | JS, JVM, Native-macOSArm64, Wasm |
| `int/zero/self` | `0` | `0` | `0` | JS, JVM, Native-macOSArm64, Wasm |

The48 common tuples run on every measured target; eight fixed-scale tuples are JVM-only (Krogu/JDK/KBigNum, Ions NA). JVM correctness uses its larger original16+16 fixture corpus: the timing subset does not relabel portable correctness as that full corpus.
