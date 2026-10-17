# Сравнение Krogu с Kotlin/KMP big-number библиотеками — #598

**Итоговый actual report по четырём targets.** Все четыре v2 кампании независимо приняты для ограниченного сравнения с явными нестабильными cells. Это finite observations, а не steady/universal ranking. Интеграция и delivery фиксируются отдельно. Прежние SOURCE ONLY заголовки preparation-файлов описывают историческую подготовку; текущие факты подтверждаются actual receipts ниже.

## Зафиксированные библиотеки и источник

Krogu: принятый `main` **829ce21154c7e75f5206eeb173351c391ed21597**, после закрытия529/537/539. Изолированная локальная публикация `io.heapy.krogu:krogu-math:0.1.0-598-829ce211` содержит135 файлов и5 publications: root metadata, JVM, JS, Wasm-JS и macOS Arm64. Ledger [publication-ledger.json](final-publication-829ce211/publication-ledger.json), SHA256 `4a424029142eb791357a8bb222fca52f836f67956dee6c36289d448a580e8d23`. Новый JVM JAR побайтно совпал с принятым `e8ba87ac09532760f64bd226c7b00aea40b76d8c0b7ae21ddade521570b4517a`. Это локальная публикация для consumer, не release в Maven Central.

| Target actual comparison | Krogu | Kotlin alternatives | Reference / ограничения |
|---|---|---|---|
| JVM | final829 | IonsPin bignum0.3.10 и KBigNum desktop0.0.20 | Public BellSoft JDK25.0.4+9; все4 APIs реально прошли correctness |
| JS / Node24.16.0 | final829 | IonsPin0.3.10 | JDK unavailable; KBigNum pinned artifact не имеет JS variant |
| Wasm-JS / Node24.16.0 | final829 | IonsPin0.3.10 | JDK unavailable; KBigNum не имеет Wasm variant |
| macOS Arm64 Native | final829 | IonsPin0.3.10 | KBigNum iOS variant не является macOS runtime; прочие Native targets не измерены |

Обе Kotlin alternatives имеют Apache-2.0 по pinned public POM. Точные coordinates, licenses, публичные README/API/variant ссылки и Kotlin metadata собраны в [peer-selection-preparation.md](peer-selection-preparation.md). Kt-math имеет JS variant, но не включён: callable public API/семантика не были квалифицированы выбранным adapter. Это не доказательство отсутствия других peers или технической невозможности второго JS peer. Исходники/bytecode чужих numerical implementation не читались; runtime использует public APIs, binary SHA и Gradle resolution metadata. Toolchain: Kotlin2.4.21, Gradle9.8.1, BellSoft25.0.4+9-LTS; hardware/OS наблюдены между JS terminal и Wasm start: Apple M4 Max,14 physical/logical cores,38,654,705,664 bytes physical RAM, macOS26.6.2/Darwin25.6.0/arm64 ([host-environment-598.json](host-environment-598.json)). Это ёмкость RAM, не её свободный объём в benchmark windows; Kotlin stdlib family принудительно выровнена до2.4.21 после сохранённого dependency-conflict preflight.

## Операции и реальная корректность

Выбранные workloads: exact integer/decimal add, subtract, multiply, self-alias square; native integer/decimal parse и native formatting на четырёх exact plain-spelling literals каждой семьи. JVM дополнительно содержит8 явных fixed-scale HalfUp fixtures для Krogu/JDK/KBigNum; IonsPin не подменяется divide-then-round. Arithmetic scale fixtures сохранены, text fixtures добавлены отдельно. Актуальная [таблица всех56 workloads](actual-workload-inputs.md) и [1512 per-child входов](actual-workload-inputs.tsv) сохраняет точные строки, обе длины/знаки, integer bits либо decimal coefficient digits/scale, expected values и фактические relations. Это actual v2 inputs. Историческая [semantic matrix](operation-semantic-matrix.md) различает value-exact, cohort/scale, significant precision, rounding и реальные недоступные/неквалифицированные API. Signed quotient/bitwise/roots не выбраны для этого сравнения, а не объявлены отсутствующими APIs. MC34/HALF_EVEN не подменяется fixed-scale; общий peer-equivalent MC34 workload не квалифицирован.

| Actual correctness | Arithmetic / fixed-scale | Дополнительные exact text | Explicit NA | Scope |
|---|---:|---:|---:|---|
| JVM |662: JDK168, Krogu168, Ions159, KBig167 |128:32 на каждого |7 | Больший16+16-fixture корпус, identity-qualified cases и8 fixed-scale |
| JS |128 mandatory +30 distinct |64 |7 | Прежний меньший portable subset, не полный JVM corpus |
| Wasm-JS |128+30 |64 |7 | То же; первый sandbox cache-lock failure сохранён отдельно,0 numerical rows |
| macOS Arm64 |128+30 |64 |7 | Реальный Release executable, без подмены iOS runtime |

Все numeric parity проверки истинны, text output совпал полностью на выбранном plain-spelling subset. Проверяются знаки, self alias, actual distinct identity, сохранность inputs и ранее возвращённых результатов. Decimal numeric parity не доказывает совпадение внутренней scale/cohort; отличающийся текст arithmetic observations сохранён. Parser interning помечен как NA distinct-object case, не маскируется клонированием внутри timing. Независимый actual audit [db549b5d](independent-full-four-target-correctness-and-publication-audit.json); owner aggregate [full-correctness-1-actual.json](full-correctness-1-actual.json).

## Метод и состояния

Каждый peer/workload получает собственный fresh child,3 чередующихся F/R/F cohorts на одном host/runtime. Peers идут рядом по одинаковому workload; reverse cohort меняет порядок workloads и peers. JVM:512MiB, SerialGC, ActiveProcessorCount1. Node:512MiB V8 old space и `--expose-gc`; Native — один Release binary с чистыми LANG/TZ. Flags одинаковы между peers одного target, а не одинаковы между разными runtimes.

FIRST — первая явная выбранная public operation после описанной подготовки: arithmetic и FORMAT после native parsing, но без prior formatting этого FORMAT operand; PARSE — после подготовки только source String. Parsing/module initialization могут прогреть внутренние caches. Это не pristine VM/cache или process startup. Result/input проверяются сразу после FIRST, затем полный untimed gate, затем7 warm samples с одним выбранным count. Warm FORMAT следует observations и может быть cached; PARSE повторяет native parser одного immutable String. Отдельный fresh-value-pool formatting workload не заявлен.

V2 training достигает минимум200ms elapsed, включая untimed checks; это не200ms суммарной numeric bracket работы. Batch ограничен1,048,576 operations, target20ms не гарантирован при достижении этого cap. Сохраняются actual cumulative ops/durations, checked Long accounting,600s child/6h campaign deadlines. Native values/Strings реально возвращаются в64-slot ring; все затронутые retained slots и inputs проверяются после counters. Adapter/dispatch/loop/ring cost включён. Throughput вычислен из того же wall bracket; нет timing correctness assertion, significance threshold или вычитания придуманного overhead.

### JVM v2: где наблюдалось быстрее и медленнее

Полный actual run:216 workloads×3 =648 FIRST и4536 warm rows. Все648 training phases достигли200ms (200.016–283.191ms). Независимый raw audit [c2575ef6](independent-jvm-timing-v2-actual-audit.json) и [case disposition6d721078](independent-jvm-timing-v2-case-disposition.json) допускают **finite post-minimum-training сравнение с нестабильными cells**, а не steady/universal ranking.

Ниже диапазоны3 per-fork medians; wall ratios перечислены в F/R/F как **peer ns / Krogu ns** (>1 означает, что peer медленнее в этой измеренной паре). CPU подтверждает направление этих выбранных примеров. Они иллюстрируют и выигрыши, и проигрыши, без выбора лучшего fork.

| Peer | Workload | Krogu ns/op | Peer ns/op | Krogu derived Mops/s | Wall ratio F/R/F | Alloc B/op Krogu / peer |
|---|---|---:|---:|---:|---|---|
| JDK25 | `int/signed/PARSE` | 18.92–20.54 | 17.16–18.15 | 48.69–52.85 | 0.896 / 0.959 / 0.836 | 96.00–96.00 / 88.00–112.00 |
| JDK25 | `int/signed/self` | 7.09–9.08 | 10.95–14.04 | 110.15–141.08 | 1.547 / 1.701 / 1.478 | 32.00–32.00 / 88.00–88.00 |
| IonsPin0.3.10 | `int/carry30/SUBTRACT` | 5.85–9.25 | 16.93–24.76 | 108.16–170.91 | 2.678 / 2.832 / 2.894 | 32.00–32.00 / 56.00–56.00 |
| IonsPin0.3.10 | `int/carry30/PARSE` | 41.66–69.15 | 238.56–448.49 | 14.46–24.00 | 6.430 / 6.486 / 5.727 | 128.00–128.00 / 1216.00–1216.00 |
| IonsPin0.3.10 | `int/wide10/FORMAT` | 85.93–94.98 | 1062.06–1276.60 | 10.53–11.64 | 12.360 / 13.746 / 12.793 | 296.00–296.00 / 5784.00–5880.00 |
| KBigNum0.0.20 | `int/carry30/PARSE` | 41.66–69.15 | 33.50–58.20 | 14.46–24.00 | 0.842 / 0.842 / 0.804 | 128.00–128.00 / 272.00–272.00 |
| KBigNum0.0.20 | `decimal/signed/ADD` | 6.74–8.81 | 11.52–17.94 | 113.55–148.28 | 1.709 / 1.957 / 2.037 | 40.00–40.00 / 136.00–136.00 |

Все160 peer/workload cells доступны в disposition JSON и480 per-fork pairs в [paired.tsv](timing-jvm-v2-summary/paired.tsv); absolute medians/min/max, CPU и allocations — [per-fork.tsv](timing-jvm-v2-summary/per-fork.tsv). Сначала отдельно выделены6 cells с несовпадением observed operand relation. В оставшихся154 cells направление совпало во всех3 наблюдениях в123, а31 имеет mixed direction. **123 не являются числом надёжных побед:** среди них есть нестабильные величины даже при одинаковом знаке ratios. Например IonsPin `int/carry30/MULTIPLY` даёт6.923/7.532/1.127, а KBigNum `decimal/scale/SUBTRACT`1.964/3.626/10.864; такой разброс нельзя свести к одному устойчивому speedup. JDK `decimal/zero/ADD` меняет направление1.189/0.077/0.068. Все648 seven-sample traces сохранены в [sample-trends.json](independent-jvm-timing-v2-sample-trends.json). CPU-coherent смены режима наблюдаются, конкретный JIT/GC механизм не доказан. Нестабильные cells — результат с границами интерпретации, не скрытый FAIL или peer NA.

### Portable timing

Каждый portable план96 workloads×3 =288 FIRST/2016 warm rows. JS v2 полностью исполнен и strict checked; actual [audit124b0570](independent-js-timing-v2-actual-audit.json), [finite disposition14ad4596](independent-js-timing-v2-case-disposition.json). Во всех288 children training достиг200ms (200.087–246.568ms). Из48 Ions/Krogu cells Ions медленнее во всех3 наблюдениях в38, быстрее в9, mixed —1 (`int/zero/self`). Это descriptive directions, а не38 доказанных универсальных побед: три zero binary cells (`ADD/SUBTRACT/MULTIPLY`) имеют несовпадение actual alias relation и не являются равной object-shape работой. Полные48 cells сохранены в [comparisons.json](independent-js-timing-v2-comparisons.json),288 traces — [sample-trends.json](independent-js-timing-v2-sample-trends.json).

| JS workload | Krogu ns/op, range3 medians | Ions ns/op | Ions/Krogu F/R/F |
|---|---:|---:|---|
| `decimal/scale/MULTIPLY` | 404.45–405.14 | 1796.88–1825.43 | 4.443 / 4.450 / 4.506 |
| `int/carry30/SUBTRACT` | 108.10–109.00 | 264.48–268.17 | 2.440 / 2.460 / 2.455 |
| `int/signed/PARSE` | 1750.77–1798.54 | 402.35–412.94 | 0.227 / 0.236 / 0.224 |

Полные absolute latency/ranges и throughput в ops/s (`derived_ops_per_second = 1e9/ns_per_op_median`) для обоих peers и всех3 forks доступны в [JS per-fork.tsv](timing-js-v2-summary/per-fork.tsv); [FIRST summary](timing-js-v2-summary/first-summary.tsv) хранится отдельно. Это примеры и выигрышей, и проигрышей на одинаковых input values; процессный CPU corroborates выбранные finite comparisons. Огромные cached-FORMAT ratios относятся к observed repeated format того же immutable value, не к общему fresh formatting или новым distributions. Process heap/RSS не выводится в allocation. Результаты Native v2 приведены ниже.

### Wasm-JS v2

Полностью исполнены288 FIRST и2016 warm rows. [Actual audit3375d735](independent-wasm-timing-v2-actual-audit.json) и [disposition eed365df](independent-wasm-timing-v2-case-disposition.json) допускают finite comparisons, без universal ranking. Все288 training phases достигли200ms (200.068–249.524ms). В48 Ions/Krogu cells Ions медленнее во всех3 forks в45, быстрее в3, mixed нет. Три zero binary relation mismatches сохранены отдельно; эти counts не превращаются в45 надёжных общих побед. Величины ratios меняются между forks и внутри sample series.

| Wasm workload | Krogu ns/op, range3 medians | Ions ns/op | Ions/Krogu F/R/F |
|---|---:|---:|---|
| `decimal/scale/MULTIPLY` | 16.89–30.37 | 389.88–606.38 | 23.082 / 30.811 / 18.086 |
| `decimal/zero/FORMAT` | 9.54–16.23 | 9.30–16.19 | 0.974 / 0.925 / 0.998 |
| `int/signed/PARSE` | 65.16–67.50 | 107.15–127.71 | 1.892 / 1.616 / 1.645 |

Near-parity `decimal/zero/FORMAT` ratios0.974/0.925/0.998 не являются material win. Все absolute latency/min/max и derived throughput в ops/s для обоих peers и каждого fork доступны в [Wasm per-fork.tsv](timing-wasm-v2-summary/per-fork.tsv), [FIRST summary](timing-wasm-v2-summary/first-summary.tsv) отдельно; все48 cells и288 traces — в [comparisons](independent-wasm-timing-v2-comparisons.json) и [sample trends](independent-wasm-timing-v2-sample-trends.json). Process CPU измерен; allocation и Wasm heap строго NA, RSS относится к процессу. Это не исправление причины drift и не перенос JS/JVM ranking на Wasm.

### macOS Arm64 Native v2

Все288 FIRST/2016 warm rows прошли [actual audit aa5430fb](independent-native-timing-v2-actual-audit.json) и [finite disposition bec14d0a](independent-native-timing-v2-case-disposition.json). Training200.011–261.192ms. Из48 Ions/Krogu cells Ions медленнее во всех3 forks в47 и быстрее в1 (`int/zero/PARSE`:0.526/0.527/0.537), mixed нет. Три zero binary physical-relation differences сохранены; direction counts относятся к этим48 finite cells, не к универсальной эффективности библиотеки.

| Native workload | Krogu ns/op, range3 medians | Ions ns/op | Ions/Krogu F/R/F |
|---|---:|---:|---|
| `int/carry30/SUBTRACT` | 13.18–15.20 | 58.73–64.17 | 4.382 / 4.223 / 4.463 |
| `decimal/scale/MULTIPLY` | 14.20–16.37 | 245.61–321.51 | 19.646 / 17.603 / 17.268 |
| `int/zero/PARSE` | 23.42–43.50 | 12.31–23.36 | 0.526 / 0.527 / 0.537 |

Полные ns/op, derived ops/s, min/max и CPU для всех peers/forks: [Native per-fork.tsv](timing-native-v2-summary/per-fork.tsv). Все48 cells/288 sample traces: [comparisons](independent-native-timing-v2-comparisons.json), [trends](independent-native-timing-v2-sample-trends.json). Например cached `decimal/wide10/FORMAT` ratios2312/1266/1264 заметно различаются между forks; ни cached state, ни величина такого ratio не означают general fresh formatting gain. getrusage измеряет process CPU, allocation/heap NA. Другие Native architectures/OS здесь не измерены.

### FIRST отдельно от warm

Ниже min–max трёх FIRST observations вns для одного и того же `int/signed/PARSE`: source String готов до bracket, native result проверен сразу после. Это не process startup и не доказательство pristine внутренних caches. Full FIRST tables включают все workloads и peers, без выбора только быстрых cases.

| Target | Krogu FIRST ns | Ions FIRST ns | JDK FIRST ns | KBigNum FIRST ns |
|---|---:|---:|---:|---:|
| JVM | 5416209–6359166 | 6285167–8078292 | 3041083–3211917 | 4166875–6222875 |
| JS | 1140250–1202458 | 1167208–1201334 | NA | NA |
| Wasm | 463708–517500 | 1388875–1470958 | NA | NA |
| macOS Arm64 | 14084–21917 | 26625–34541 | NA | NA |

Full FIRST summaries: [JVM](timing-jvm-v2-summary/first-summary.tsv), [JS](timing-js-v2-summary/first-summary.tsv), [Wasm](timing-wasm-v2-summary/first-summary.tsv), [Native](timing-native-v2-summary/first-summary.tsv). FIRST CPU/allocation raw имеют соседний counter-getter overhead; tiny FIRST не следует сравнивать как чистый intrinsic cost.

## Allocation и memory

| Metric | JVM | JS Node | Wasm Node | macOS Native |
|---|---|---|---|---|
| CPU | Current-thread CPU, если enabled/supported | Process user+system | Process user+system | getrusage process user+system |
| Allocation B/op | ThreadMXBean current-thread delta |NA |NA |NA |
| Heap | Process used-heap snapshots, coarse |V8 process heapUsed, coarse |NA: JS heap не Wasm graph |NA |
| Peak RSS through snapshot |NA внутренне |Node maxRSS, KiB→bytes |Node maxRSS, KiB→bytes |macOS ru_maxrss bytes |
| Completed whole-child peak |`/usr/bin/time -l` |То же |То же |То же |
| Owned graph / retained per value |NA: нет квалифицированного общего метода в данном runner |NA |NA |NA |

Allocation и RSS не взаимозаменяемы. Whole-child peak включает startup, FIRST, full gates, warm work, checking и exit; это не footprint одной операции/значения. Heap snapshots не являются retained graph, snapshots maxRSS не completed lifetime peak. CPU/alloc getter overhead у крошечного FIRST частично входит в delta по описанному порядку счётчиков. Host `.host` сохраняет load average/processors в точных child windows; свободная host RAM не измерена, host isolation не доказана. PID/start относится к `/usr/bin/time` supervisor; runtime descendant PID не выдаётся за независимо установленный.

### Наблюдённая whole-child память

Диапазон и медиана peak RSS по всем fresh children данного peer/target, MiB (bytes/1048576). Включены startup, FIRST, full gates, warm, checks и exit. Различия не являются per-operation allocations, retained object graph или isolated library footprint; gates также используют другие peers.

| Target / peer | Children | Peak RSS min / median / max MiB |
|---|---:|---:|
| JVM / ionspin-bignum-0.3.10 | 144 | 163.30 / 214.24 / 231.09 |
| JVM / jdk-bellsoft-25.0.4+9 | 168 | 161.38 / 212.46 / 227.42 |
| JVM / kbignum-0.0.20-desktop | 168 | 163.58 / 215.69 / 225.52 |
| JVM / krogu-final539-PENDING | 168 | 161.70 / 212.38 / 224.48 |
| JS / ionspin-bignum-0.3.10 | 144 | 66.72 / 81.34 / 98.31 |
| JS / krogu-final539-PENDING | 144 | 66.58 / 77.91 / 105.50 |
| Wasm / ionspin-bignum-0.3.10 | 144 | 69.20 / 74.57 / 83.78 |
| Wasm / krogu-final539-PENDING | 144 | 68.31 / 74.55 / 84.20 |
| Native-macOSArm64 / ionspin-bignum-0.3.10 | 144 | 18.06 / 18.22 / 19.20 |
| Native-macOSArm64 / krogu-final539-PENDING | 144 | 18.03 / 18.23 / 18.56 |

Машинная [сводка resources](actual-resource-summary.json); оригинальные time-l footer и `.host` сохранены в каждом raw child. Available host memory — **NA**:36GiB physical capacity не доказывает свободный RAM во время замеров. Current-thread JVM allocation отдельно в `per-fork.tsv`; portable allocation и graph/retained size остаются NA.

## Exact actual bindings

| Target | Frozen runtime manifest SHA256 | Own source-map SHA256 | Actual receipt SHA256 |
|---|---|---|---|
| jvm | `647f13bfaa0181cf99c6d9f28f87c9e25384ea5f56db312423729bb64adccdcd` | `cefcafd651ca0bb88d068aa1f12706f9691ad22baf1fe9a6be7178701b18a473` | `8333999cd132ec2d194e0f2de9852da385f4291bd0b8af51296b2b2023c0fae1` |
| js | `d5871785f1056288507273d5402343b6cf31fd5e03b3bd71caa74a0afa9feb66` | `d74497eb93bf97278ddf3c54fb350a590241ced8f84ff5da72858b6bbb1607b7` | `e267411c92caa9c0285b60f0f13b15d2cc464e6766558668cc949445620489b6` |
| wasm | `b9f472acd3a3aa9c83701700f3d96227ddbe2337ffb5c1ff2202add4d05e2270` | `99a6a9db215839c21053f31b1388ea5562e2c22dd6fb6c66c551ea10a579ea49` | `3698104d226bafc40829e9037a47679b2075d3a73425e59cd7c207169bcddfbd` |
| native | `702286f6302253c45ff905f99c8b9d5233d148a498f2356c35c93239a3a04390` | `71bd2266e0da0a7eb3750386187afb52c3df652d05a07439d386576338fd5f17` | `7c9fbf55ca9bf7ebe9e9819d372face50a8916e3b905bdd23a7d2faa02eb9677` |

## История, raw и воспроизведение

- Initial JVM v1 полностью сохранён:648 FIRST/4536 TIME, [raw receipt](timing-jvm-1-actual.json), raw/stat audit и [training disposition](independent-jvm-timing-1-training-disposition.json). Cumulative operation cap закончил565 training phases раньше200ms; regime transitions делают robust warm ranking неподдержанным. V1 не переименован вv2.
- Initial JS `timing-js-1` содержит first-child setup failure: own generated mainWrapper вызвал main([]);0 INPUT/FIRST/TIME/gates. [Argument fix](node-timing-argv-fix.json) сохраняет source/failure mapping. Node argv bridge и minimum200ms correction прошли [composed review94884adc](independent-timing-v2-composed-source-review.json). Никаких повторов до favorable outcome нет.
- Фиксированные original adapters/fixtures, прошлые manifests/descriptors, dependency/std-lib и Wasm cache/runtime/link failures сохранены под history и прежними actual receipts. Их counts не подставляются вместо full final consumer.

Точные runnable commands/metric boundaries доступны в [prepared-timing/README.md](prepared-timing/README.md), immutable runtime trees/source snapshots и raw `.argv.txt`/`.binding`/`.host` файлах каждого child. Final accepted SHA/GAV/ledger выше заменяют исторические placeholders. Конкретные команды и recovery boundaries: [REPRODUCE.md](REPRODUCE.md). Delivery manifest и независимые final review/completion verdicts фиксируются вместе с tracked package; до verified delivery task598 не объявляется завершённым.
