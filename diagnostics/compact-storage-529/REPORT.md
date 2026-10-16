# Compact storage in BigInteger and BigDecimal — task529

Both existing public classes now store ordinary Long-range values in two Int halves with a representation tag and nullable wide storage. Compact integers retain no magnitude array; compact decimals retain no coefficient object until an API or fallback requests one. Positive2^63 promotes, negative2^63 remains compact. Checked primitive arithmetic, immutable promotion/demotion and the established callback, scale, serialization and observable publication behavior are preserved. These changes are independently implemented; no foreign numerical implementation was read or delegated to.

Numeric production09e6dc36b3dfaae3878dbe6304b9c9dcaa96735d is compared with accepted parentf8565cae98081ff0121ce7a40919d8a4cfd92013, which already contains537/597/535/538. Diagnostic-only commitd6d594f9bb5f8da5ea962e87ee92796135e9f3d9 adds the same unpublished Native capture hook to both measurement builds. This is Krogu-versus-Krogu, not a current JDK comparison; parser work already in the parent receives no529 speedup credit.

Full unfiltered regression46319 passed2055 methods:684 JVM and457 each JS/Wasm/macOSArm64, zero failures/errors/skips. All320 source hashes were unchanged. The later five-file opt-in benchmark hook has an explicit separate source delta,323 candidate/312 baseline hashes, and changes no tested production bytes. Initial full45317 failed12 of683 JVM methods on observable result identity; the assertions remain unchanged, the failure is retained, and the reviewed correction passed affected120 methods and the full suite. No historical/current test counts are summed.

## Measurement protocol and interpretation

Pinned BellSoft25.0.4+9-LTS, Kotlin2.4.21 and Node24.16.0 run on the same shared AppleM4Max/macOSArm64 host. No CPU affinity, power isolation or cross-platform performance transfer is claimed. JVM33 uses identical direct-call adapter bytecode, six alternating fresh JVMs, uniform warmup, seven samples per process, escaping results, complete representation checks, G1/256MiB initial/512MiB maximum heap and actual runtime/origin/hash guards. Portable21 uses the same Kotlin prepared inputs and escaping harness within each old/new pair. Compare ratios within a platform: JVM and portable harness overheads differ.

Tables contain every case, three-process ranges and paired ratios. JVM allocation is transient thread bytes/op; other platforms report NA. It includes counted construction/helpers/results, not retained memory. MC34 is the unchanged original527 sixteen-pair MC34/HALF_EVEN control, distinct from compact MC7. Fresh accumulator includes constructor, two adds and snapshot. No universal speedup or no-regression claim is made.

| Platform | Final handle | Cases | Faster in all3 pairs | Slower in all3 pairs | Mixed pairs |
|---|---:|---:|---:|---:|---:|
| JVM |87962|33|17|1|15|
| JS |71641|21|9|8|4|
| Wasm |40973|21|15|4|2|
| Native Release |77132|21|12|5|4|

All four final raw/source/artifact/statistic audits independently pass. Stable adverse cases remain visible: JVM fresh accumulator; JS fresh accumulator, MC7/MC34 and several wide controls; Wasm MC34, decimal wide construction and integer wide add/divide; Native fresh accumulator, MC34, decimal wide construction and integer wide add/divide. Wide field costs and transient-versus-retained distinctions are not hidden in an aggregate.

## JVM33: all cases

| Case | Baseline ns/op [min,max] | Candidate ns/op [min,max] | Ratio | Paired ratios | Transient B/op old→new |
|---|---:|---:|---:|---|---:|
| decimal/accumulator-compact-fresh | 696.692 [536.054,1247.457] | 765.759 [763.339,1413.854] | 1.099 | 1.424/1.099/1.133 | 1880.5→1792 |
| decimal/add-small | 38.758 [33.319,49.167] | 7.535 [4.802,8.173] | 0.194 | 0.098/0.226/0.211 | 264→40 |
| decimal/create-min | 9.904 [7.495,16.766] | 5.264 [3.313,6.314] | 0.531 | 0.335/0.702/0.377 | 88→40 |
| decimal/create-small | 10.443 [8.444,16.189] | 6.057 [3.581,6.760] | 0.580 | 0.343/0.717/0.418 | 112→40 |
| decimal/divide-context | 183.618 [99.789,204.716] | 33.704 [33.348,53.114] | 0.184 | 0.184/0.334/0.259 | 410.5→45 |
| decimal/divide-scaled | 58.213 [34.704,64.056] | 21.244 [14.812,30.607] | 0.365 | 0.254/0.612/0.478 | 273→64 |
| decimal/metadata | 3.729 [3.326,5.757] | 4.605 [3.209,5.640] | 1.235 | 0.861/1.384/0.980 | 0→0 |
| decimal/multiply-small | 21.271 [13.989,21.611] | 7.159 [4.835,7.972] | 0.337 | 0.227/0.512/0.369 | 112→40 |
| decimal/original527-mc34-divide | 286.245 [179.311,293.182] | 291.567 [196.778,298.263] | 1.019 | 0.671/1.626/1.042 | 610→634 |
| decimal/parse-rounded | 107.481 [64.571,129.758] | 82.028 [58.039,92.833] | 0.763 | 0.540/1.270/0.715 | 344→184 |
| decimal/parse-small | 102.223 [94.466,125.785] | 71.705 [49.238,82.506] | 0.701 | 0.482/0.759/0.656 | 216→184 |
| decimal/round | 23.855 [16.727,30.381] | 11.801 [8.930,16.579] | 0.495 | 0.374/0.706/0.546 | 160→40 |
| decimal/setScale | 20.303 [15.871,26.644] | 7.964 [5.809,9.252] | 0.392 | 0.286/0.502/0.347 | 136→40 |
| decimal/strip | 27.348 [21.187,37.443] | 11.875 [8.498,12.135] | 0.434 | 0.311/0.560/0.324 | 328→40 |
| decimal/subtract-small | 46.212 [29.623,51.460] | 6.748 [4.854,8.185] | 0.146 | 0.105/0.228/0.159 | 232→40 |
| decimal/wide-add | 105.999 [87.228,177.761] | 87.476 [64.473,109.280] | 0.825 | 0.608/1.003/0.615 | 632→664 |
| decimal/wide-create | 4.335 [3.847,7.556] | 5.834 [3.988,7.209] | 1.346 | 0.920/1.517/0.954 | 32→40 |
| decimal/wide-divide | 2260.198 [1934.176,3650.640] | 2574.964 [2472.804,2861.766] | 1.139 | 1.139/1.278/0.784 | 7533.5→6844.5 |
| decimal/wide-multiply | 879.894 [859.152,1843.862] | 1436.961 [1106.421,1462.830] | 1.633 | 1.288/1.633/0.793 | 3680→3520 |
| decimal/wide-round | 314.332 [275.147,637.445] | 350.031 [331.572,402.403] | 1.114 | 1.114/1.205/0.631 | 1568→1376 |
| integer/add-small | 14.256 [13.603,14.563] | 6.589 [4.239,6.992] | 0.462 | 0.297/0.480/0.484 | 80→32 |
| integer/create-min | 5.061 [4.699,6.201] | 2.797 [2.050,4.396] | 0.553 | 0.436/0.451/0.869 | 56→32 |
| integer/create-small | 8.495 [7.695,9.182] | 2.904 [2.223,4.369] | 0.342 | 0.242/0.342/0.568 | 80→32 |
| integer/demote | 9.240 [8.667,12.324] | 8.325 [4.369,10.328] | 0.901 | 0.473/0.961/0.838 | 56→32 |
| integer/divide-min-overflow | 10.638 [9.498,16.121] | 12.063 [5.383,12.072] | 1.134 | 0.506/1.271/0.748 | 56→64 |
| integer/metadata | 4.046 [3.456,6.093] | 5.705 [3.094,6.869] | 1.410 | 0.765/1.987/0.936 | 0→0 |
| integer/multiply-small | 15.220 [14.045,18.377] | 7.876 [5.140,8.258] | 0.517 | 0.280/0.543/0.561 | 80→32 |
| integer/parse-small | 70.317 [67.669,84.601] | 76.805 [66.287,88.495] | 1.092 | 0.784/1.092/1.308 | 136→128 |
| integer/promote | 12.304 [12.210,13.817] | 8.447 [5.463,12.832] | 0.687 | 0.395/0.692/1.043 | 88→64 |
| integer/subtract-small | 10.805 [10.497,10.998] | 5.937 [4.047,6.424] | 0.549 | 0.368/0.595/0.566 | 48→32 |
| integer/wide-add | 21.795 [20.008,23.413] | 28.163 [15.361,34.033] | 1.292 | 0.656/1.701/1.292 | 200→208 |
| integer/wide-divide | 177.041 [156.089,383.764] | 253.303 [162.310,281.026] | 1.431 | 0.917/1.623/0.732 | 296→304 |
| integer/wide-multiply | 195.171 [163.220,255.636] | 279.233 [156.472,279.767] | 1.431 | 0.802/1.714/1.092 | 328→336 |

## JS21: all cases

| Case | Baseline ns/op [min,max] | Candidate ns/op [min,max] | Ratio | Paired ratios | Transient B/op old→new |
|---|---:|---:|---:|---|---:|
| decimal/accumulator-compact-fresh | 18256.490 [14550.273,18568.949] | 23049.316 [20549.724,24258.342] | 1.263 | 1.107/1.667/1.263 | NA |
| decimal/add-small | 1672.752 [1666.039,1997.442] | 290.076 [287.230,408.716] | 0.173 | 0.205/0.172/0.173 | NA |
| decimal/create-min | 531.642 [501.389,576.419] | 46.086 [44.898,46.291] | 0.087 | 0.078/0.092/0.087 | NA |
| decimal/create-small | 570.642 [469.140,577.157] | 39.194 [35.352,47.966] | 0.069 | 0.068/0.102/0.062 | NA |
| decimal/divide-context | 12291.016 [11462.036,14848.633] | 14985.270 [14735.677,16031.677] | 1.219 | 1.009/1.304/1.286 | NA |
| decimal/metadata | 71.235 [51.504,74.030] | 11.903 [10.635,15.884] | 0.167 | 0.144/0.231/0.223 | NA |
| decimal/multiply-small | 767.231 [757.573,2365.850] | 779.023 [673.383,793.713] | 1.015 | 1.035/1.028/0.285 | NA |
| decimal/original527-mc34-divide | 21836.426 [19208.577,26637.979] | 36487.915 [35886.312,36594.645] | 1.671 | 1.370/1.905/1.643 | NA |
| decimal/round | 2199.949 [1625.964,2202.489] | 1730.654 [1691.096,1974.965] | 0.787 | 0.787/1.040/0.897 | NA |
| decimal/setScale | 2133.967 [1549.761,2220.319] | 1448.413 [1293.760,1478.803] | 0.679 | 0.583/0.954/0.679 | NA |
| decimal/wide-add | 7924.052 [5814.168,9668.742] | 10787.415 [10071.086,11574.137] | 1.361 | 1.116/1.732/1.461 | NA |
| decimal/wide-create | 70.301 [50.287,81.522] | 92.417 [84.790,93.248] | 1.315 | 1.315/1.686/1.144 | NA |
| decimal/wide-divide | 423302.734 [323741.211,597520.188] | 648467.453 [470615.234,648519.531] | 1.532 | 1.085/1.454/1.532 | NA |
| decimal/wide-multiply | 121022.137 [90044.598,179949.867] | 154334.797 [131455.566,205271.484] | 1.275 | 0.858/1.460/1.696 | NA |
| integer/add-small | 407.540 [342.494,465.657] | 266.969 [232.966,268.216] | 0.655 | 0.500/0.783/0.655 | NA |
| integer/create-min | 398.207 [379.849,498.526] | 90.297 [88.436,95.770] | 0.227 | 0.177/0.252/0.227 | NA |
| integer/create-small | 412.422 [408.513,500.443] | 185.855 [153.385,227.195] | 0.451 | 0.306/0.556/0.451 | NA |
| integer/multiply-small | 718.962 [543.451,732.014] | 661.019 [589.027,675.399] | 0.919 | 0.805/1.243/0.919 | NA |
| integer/wide-add | 1301.503 [1294.352,1434.980] | 1571.744 [1324.552,1865.580] | 1.208 | 1.023/1.208/1.300 | NA |
| integer/wide-divide | 17600.077 [17323.201,18870.870] | 20218.100 [19958.984,20295.227] | 1.149 | 1.075/1.167/1.134 | NA |
| integer/wide-multiply | 35578.207 [34121.785,36554.240] | 33401.652 [32365.112,34035.318] | 0.939 | 0.885/0.939/0.997 | NA |

## Wasm21: all cases

| Case | Baseline ns/op [min,max] | Candidate ns/op [min,max] | Ratio | Paired ratios | Transient B/op old→new |
|---|---:|---:|---:|---|---:|
| decimal/accumulator-compact-fresh | 1016.417 [1013.531,1033.220] | 1039.524 [1029.602,1048.934] | 1.023 | 0.996/1.035/1.023 | NA |
| decimal/add-small | 126.177 [125.674,127.062] | 23.273 [23.142,23.749] | 0.184 | 0.182/0.189/0.184 | NA |
| decimal/create-min | 24.044 [24.032,25.308] | 11.887 [11.863,11.970] | 0.494 | 0.470/0.498/0.494 | NA |
| decimal/create-small | 28.574 [27.801,33.228] | 11.676 [11.642,11.996] | 0.409 | 0.351/0.420/0.419 | NA |
| decimal/divide-context | 321.709 [320.110,326.731] | 121.012 [117.013,121.239] | 0.376 | 0.358/0.376/0.379 | NA |
| decimal/metadata | 19.136 [19.124,19.251] | 13.455 [13.102,14.588] | 0.703 | 0.704/0.762/0.681 | NA |
| decimal/multiply-small | 51.496 [51.355,51.781] | 21.715 [21.628,21.949] | 0.422 | 0.418/0.426/0.423 | NA |
| decimal/original527-mc34-divide | 443.792 [441.811,445.654] | 470.949 [469.366,473.289] | 1.061 | 1.061/1.062/1.062 | NA |
| decimal/round | 73.190 [70.658,76.160] | 34.537 [34.162,34.664] | 0.472 | 0.453/0.491/0.467 | NA |
| decimal/setScale | 68.563 [68.554,68.668] | 33.551 [33.315,33.557] | 0.489 | 0.489/0.489/0.485 | NA |
| decimal/wide-add | 277.094 [276.997,280.539] | 216.059 [213.422,218.033] | 0.780 | 0.780/0.787/0.761 | NA |
| decimal/wide-create | 17.334 [17.295,17.674] | 23.699 [23.532,24.202] | 1.367 | 1.361/1.396/1.341 | NA |
| decimal/wide-divide | 4394.831 [4364.924,4425.252] | 3612.193 [3599.792,3656.993] | 0.822 | 0.816/0.838/0.819 | NA |
| decimal/wide-multiply | 2125.203 [2111.966,2140.432] | 2033.127 [2022.786,2105.403] | 0.957 | 0.984/0.952/0.963 | NA |
| integer/add-small | 30.346 [30.121,31.736] | 20.221 [19.809,20.709] | 0.666 | 0.637/0.688/0.653 | NA |
| integer/create-min | 16.271 [16.165,16.491] | 11.664 [11.471,11.709] | 0.717 | 0.722/0.710/0.705 | NA |
| integer/create-small | 19.810 [19.423,20.462] | 11.812 [11.486,12.008] | 0.596 | 0.596/0.587/0.591 | NA |
| integer/multiply-small | 41.535 [41.354,42.589] | 19.663 [18.984,20.239] | 0.473 | 0.459/0.475/0.473 | NA |
| integer/wide-add | 62.968 [62.547,64.072] | 74.416 [73.571,76.326] | 1.182 | 1.182/1.220/1.148 | NA |
| integer/wide-divide | 171.520 [171.175,172.565] | 204.949 [204.749,212.403] | 1.195 | 1.188/1.238/1.196 | NA |
| integer/wide-multiply | 350.263 [347.120,367.021] | 358.119 [355.714,361.348] | 1.022 | 0.969/1.041/1.022 | NA |

## Native Release21: all cases

| Case | Baseline ns/op [min,max] | Candidate ns/op [min,max] | Ratio | Paired ratios | Transient B/op old→new |
|---|---:|---:|---:|---|---:|
| decimal/accumulator-compact-fresh | 878.513 [864.386,1306.994] | 906.003 [897.458,1573.006] | 1.031 | 1.022/1.048/1.204 | NA |
| decimal/add-small | 115.828 [112.988,129.158] | 28.694 [28.527,40.520] | 0.248 | 0.246/0.254/0.314 | NA |
| decimal/create-min | 30.982 [30.438,31.676] | 12.585 [12.494,17.985] | 0.406 | 0.397/0.410/0.581 | NA |
| decimal/create-small | 36.867 [36.800,37.567] | 12.683 [12.609,19.561] | 0.344 | 0.338/0.342/0.532 | NA |
| decimal/divide-context | 266.988 [262.949,414.737] | 121.612 [119.930,208.201] | 0.455 | 0.455/0.456/0.502 | NA |
| decimal/metadata | 12.187 [12.062,13.534] | 10.022 [9.990,15.324] | 0.822 | 0.822/0.828/1.132 | NA |
| decimal/multiply-small | 50.907 [48.911,72.316] | 18.893 [18.866,27.907] | 0.371 | 0.371/0.386/0.386 | NA |
| decimal/original527-mc34-divide | 361.565 [360.927,561.745] | 453.007 [451.336,783.899] | 1.253 | 1.255/1.248/1.395 | NA |
| decimal/round | 75.203 [74.963,117.713] | 32.075 [32.027,54.628] | 0.427 | 0.426/0.428/0.464 | NA |
| decimal/setScale | 68.641 [67.915,113.377] | 30.962 [30.722,47.466] | 0.451 | 0.448/0.456/0.419 | NA |
| decimal/wide-add | 166.871 [165.615,318.412] | 182.604 [179.344,238.973] | 1.094 | 1.075/1.103/0.751 | NA |
| decimal/wide-create | 14.648 [14.514,16.032] | 17.345 [17.248,27.692] | 1.184 | 1.184/1.188/1.727 | NA |
| decimal/wide-divide | 3788.081 [3637.451,5790.263] | 3462.997 [3419.617,3558.355] | 0.914 | 0.952/0.903/0.615 | NA |
| decimal/wide-multiply | 1662.247 [1634.532,2789.663] | 1602.547 [1589.238,1966.270] | 0.964 | 0.972/0.964/0.705 | NA |
| integer/add-small | 29.463 [29.367,29.893] | 18.834 [18.570,26.402] | 0.639 | 0.621/0.641/0.896 | NA |
| integer/create-min | 19.339 [19.200,20.119] | 14.593 [14.008,23.364] | 0.755 | 0.724/0.760/1.161 | NA |
| integer/create-small | 24.963 [24.807,27.679] | 14.261 [13.959,23.541] | 0.571 | 0.563/0.571/0.851 | NA |
| integer/multiply-small | 35.076 [34.860,35.363] | 18.398 [18.226,24.064] | 0.525 | 0.515/0.528/0.686 | NA |
| integer/wide-add | 41.458 [40.981,42.314] | 53.197 [51.251,62.077] | 1.283 | 1.211/1.298/1.497 | NA |
| integer/wide-divide | 174.068 [172.318,177.557] | 181.583 [178.724,262.912] | 1.043 | 1.007/1.054/1.510 | NA |
| integer/wide-multiply | 247.749 [242.409,250.104] | 228.752 [225.800,300.152] | 0.923 | 0.903/0.923/1.238 | NA |

## Retained storage: method-specific evidence

| Method/configuration | BI small | BD small | BI wide cohort | BD wide cohort |
|---|---:|---:|---:|---:|
| Current JVM Instrumentation owned graph B/root |48→32|80→40|104→112|136→152|
| Observed9641 JS strong owned graph B/root |264→96|320→96|331.5→371.5|387.5→467.5|
| Observed9641 Wasm strong owned graph B/root |144→72|216→80|203→219|275→299|
| Current matched Native Debug modeled instance extent B/root |44→32|76→44|101.5→113.5|133.5→157.5|

JVM MIN/MAX integers56→32 and MIN decimals88→40 are additionally measured. Its80B wide primitive array is shallow including header/alignment, not80B raw payload. JVM graphs traverse identity-reachable owned library fields/arrays and exclude static caches and operation garbage. They are not a universal retained-heap guarantee.

JS/Wasm rows are actual historical9641 snapshot cohort averages across256 distinct uncached roots, with rooted carrier, field/array/backing edges, exclusive strong closure and exact256B control contrasts. Equal carriers2096B(JS)/2128B(Wasm), shared Unit/runtime/class metadata are excluded with explicit sharing proof. JS compact roots own a real PropertyArray:56+40=96B; no retained emulated-Long payload or coefficient is present. Actual PropertyArray bytes are not claimed as fresh09e observations. Final structural applicability is supported by15 exact generated JS constructor/cache/retention functions and matching Wasm type/import sections, selected fields and global/reference mapping. The Wasm global initializer section differs and is disclosed. The captures retain their original module hashes; no new final capture is invented.

Native16 matched Debug dumps measure actual serialized payload, object counts, types/reference slots, global marker→carrier256→roots, strong ownership and0/256/64 array controls. Object/array header extents are separately modeled from the pinned actual runtime ABI: object dumpdata+8; eligible array payload+16. These are runtime instance extents, not allocator prefixes, size-class rounding, allocator-slot/tail rounding beyond the TypeInfo extent, pages, GC extras, RSS or total process bytes. Unit is a proven permanent image singleton omitted from enumerated GCroots; exact executable/raw/ASLR image-symbol/section proof with a pre-relocation file header (not a live-memory header) corrects that classification, and the original failed strict check remains preserved. Optional Release ExtendedTypeInfo decoding failed on a null pointer; no Release graph/retained-size transfer is claimed. Native arithmetic above is Release; memory evidence is explicitly Debug-only.

The new nineteen-cell power cache is shared, not charged per number.152B is logical raw payload. JVM observed array168B plus associated helper16B; historical JS exclusive cache1072B(including19Long760/Array32/elements168/PropertyArray40/exclusiveMap72) plus helper96B; Wasm232B(wrapper56/backing176) plus helper64B. NativeDebug observes152 payload and models168B array plus associated helper24B. Associated helper sizes are not incremental cache-only owner growth. JVM unchanged helper/decimal class byte hashes and final generated structural binding scope the old cache observations; unavailable primitive snapshot contents are not invented.

## Failed methods, source eras and limitations

Final JS and Native whole-runtime retained-delta controls each fail all three strict pairs. Their486 observations per campaign and exact failures remain FAILED_UNVERIFIED; no per-value size is inferred from them or RSS. Wasm controls pass with exact256B/root contrast, but those are runtime slopes, not ownership graphs. Native GC.collect already waits for completion; asynchronous-GC speculation was not used as a cause or excuse. Separate actual owned-graph evidence supplies the stated memory results.

Earlier source phases and all adverse measurements are retained: initial compact/facade setup failures; first allocation regressions; scalar-row correctionef3db6c; decimal cache/digit correction9641a95; identity correction09e6dc3; diagnostic-only hookd6d594f. Initial parser compilation/npm setup/facade collisions, cwd-only no-measurement start failure, Native Unit/cache infrastructure failures and optional Release metadata failure are not relabelled as numerical passes. Old measurements are not mixed into current final statistics.

Current implementation accepts wide extra fields and remaining arithmetic costs. Retained-size reduction for small values does not imply lower transient allocation or faster every operation. No new numerical solver, dependency, compiler backend flag, public class replacement or cross-version serialization guarantee was introduced.

## Evidence and recovery

REQUIREMENTS.md maps the literal task to accepted checks. Numerical tables come from the four final owner summaries and independent raw audits; all raw campaigns, graph envelopes, source manifests, reviewer judgments and earlier failures are preserved in indexed lossless archives. Each ZIP has a per-member SHA256/byte ledger. evidence-index.json and evidence.sha256 bind canonical files; exact measured artifact paths/hashes are retained separately and included as selected inputs. REPRODUCE.md records pinned commands and scope. Packaging/Git-blob verification and serial integration are required before closure; this report itself does not close the task.
