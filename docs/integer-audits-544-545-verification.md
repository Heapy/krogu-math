# Integer arithmetic and bit audits 544/545

All four local target gates passed; **exact published-head CI remains open**. These bounded audits found no production defect. They add proofs and tests; huge-capacity and allocation questions remain task548. They do not establish performance, full compatibility or official JCK conformance.

## Candidate and source review

The isolated candidate at `/private/tmp/krogu-integer-audits-validation-v1/root` contains published commit `99c81a42a5d104e045bcfe6eca2246231d37bf80` plus five owned545 V2 and five owned544 V3 files. It excludes the discarded570/571/572 proposals. Production remains unchanged:

| Source | SHA-256 |
| --- | --- |
| BigInteger | `d0e757ebd5955604d5fb66d8dda13b1c15b2ecb300a37a5fbcb1fcfaeb65c16f` |
| BigDecimal | `57a5af7b71c37ef4a1b460fbae0fd793ae000ea0eabafca34e85e89567a5a0a1` |
| MathContext | `6aa634ebf23ca5dc2bae21c1899f470909eca18539d31a02c9426ff7cca4f0de` |

Accepted archives beside the runtime root are `accepted-545-v2.tar` (`8114a5d0ab351f368740084b72f7b96be52116b2ef46a671335f521ae9c8786d`) and `accepted-544-v3.tar` (`f0ddd57a0d564982f940e420ed948117a42c93ef526b910e9d502d7ed80acf71`). The exact test hashes are:

```text
cefa4a6b5766061357bc97553ca71009d4f8fd62bb964fd21287635f351471c9  IntegerBits545ContractTest.kt
1dbbd3d65403bf6712c9d2c504480af680c22a4329eb7b55cbbc5427d099b020  IntegerBits545JvmTest.kt
5b3b89ad12de50755dced8cd83881ba397ff699eba19e12c60e923d552df91e2  IntegerArithmetic544ContractTest.kt
e4b71fe6309877026ff4694cdc5ced02ad1c804df1949f7ea1506f5761fdfee4  IntegerArithmetic544JvmTest.kt
```

Independent source review corrected two544 test issues before execution: wrapper formatting could recurse through positive `super.abs()` returning the wrapper; an exact comparison-event assertion also prohibited unspecified internal callbacks. V2 uses an ordinary base instance derived from the original constructor text for wrapper transformations. V3 retains the numerical comparison and explicit first/last caller markers. Neither correction changes production. The superseded V2 archive remains preserved (`bca4b497e183b9ebeb409d5a16c71a0ce550b3aef941389fe8d5a967bf2c7997`).

545 V2 adds five bounded own/JDK endpoint pairs: two zero shifts and three negative-index updates. It does not allocate huge MAX-address updates or execute nonzero growing endpoint shifts. The original proof/handoff UNCOMPILED/UNRUN statements describe historical source milestones; only the completed gates below supersede them.

## Behavioral gates

Pinned tools are Kotlin2.4.21, BellSoft Liberica25.0.4+9-LTS and Gradle9.8.1.

| Target | Suites | Passed methods | Duration |
| --- | ---: | ---: | --- |
| JVM | 134 | 465 | 3m6s |
| JS | 64 | 309 | 9m33s |
| Wasm JS | 64 | 309 | 2m27s |
| macOS ARM64 | 64 | 309 | 4m28s |

The source-derived auditor (`6dc6ef4b92120c22cbe124d2812ca1191cbdb61d1af55d0e6837bf69e843e449`) checks the complete inventory and exact fourteen common/eight JVM additions. All four gates have zero failures, errors, skips, omitted/extra/duplicate methods or XML anomalies: 1,392 method executions, including 64 additions versus569. Independent read-only review accepted all four actual results. Explicit caller/marker and numerical assertions remain separate from internal callback observations, including the own/host `parallelMultiply` diagnostic difference. macOS logged FSEvents watcher warnings before/after its successful build; the raw log retains them.

Logs, archived XML and manifests are retained in `/private/tmp/krogu-integer-audits-validation-v1/`:

```text
236d40d2eb362f021fb323a4374497f57a92502662f8e64e0ad876913fa3c36b  jvm.log
2be9f5a9ed08112fd12b4a51ec29d0b8df0a39fba0b648245b1ba36677ab5b1f  jvm-audit.log
34ac2f7eb4b507b5be43bbdcdf1e834baf04121026e0d386b1437018bc235ede  js.log
b315be099c7e3f67473f43e5132e7401abb7f65133da6d635e661a20e5d11329  js-audit.log
f73e3bd971d09a067f05761eec00fc82d47a3dab271c20d4d0e6dce6583864aa  wasm.log
4c21733848fb64c4a6a31135577a2658e787012670e043409d7aef687ed94c39  wasm-audit.log
e75f69ed1d17f4198bb369707bc02466e8cb08740b669d773557e6b8913bd22d  mac.log
ba71510e8966f3e1b9a1af9f308ea6d6b537ec87fa490275749ca72798ef1292  mac-audit.log
```

## Integrity and remaining gates

The205-file input manifest before execution and after JVM/JS has SHA `50bb2d07af1d1fdc71ef70fcf3f66afb403540cabe7028edde9fc2dc1007ee0f`. Wasm added only a generated `kotlin-js-store/wasm/yarn.lock` (`970a85c50fa072f8543e444af2d28a16de073c3e7ead47b86527d83a557b4dcc`); all pre-existing inputs remained unchanged. The raw206-file manifest (`a0dc62a30a96469ddfe46c12826edb2a85831568b4397aee61098ab73f603531`) and exact diff (`b368402f8952c525b43e10df748a610c36087704031c5620aceef820a06e74b2`) are preserved. Moving that own generated output outside the runtime root restored the identical205-file manifest. The complete inventory changed during Wasm execution.

Earlier545 execution with constructor overlays is historical only: `/private/tmp/krogu-545-constructor-overlay-evidence-v2.tar`, SHA `ad230d12c39422f5bbb9d36ac81e1f1a83db1bed0ab98354fde66a60ae36c4c0`. It does not validate this candidate. Production is unchanged, so [569's fresh JAR and selected public campaign](zero-double-context-569-verification.md) retain their production-source binding; this report represents no new public campaign.

The after-macOS205-file manifest also matches the original `50bb2d07…` manifest. Independent final review accepted the macOS XML, hashes and input checks; all four local gates are accepted. Publication checkout verification matches204 raw input hashes; its only raw difference is `gradlew.bat`, whose Git-normalized blob is identical in runtime, publication checkout and index (`3185a43f78fabec15ca99b0ca6bf1e666218d5ca`). All ten adopted files match their accepted hashes. The checksum log retains the single raw line-ending difference.

Publication requires all six CI jobs at the exact published head before task closure. Published99's prior CI does not certify these added tests. Tasks544/545 remain open.

The local evidence archive is `/private/tmp/krogu-integer-audits-local-evidence-v1.tar`, SHA-256 `8602dc636141b53753f6d801adf8d85fbefcb14e6248a4cfde0edce7febe0f08`. It contains the published baseline, accepted source archives, auditors, logs, XML, manifests and publication checksum diagnostics; build directories and caches are excluded.
