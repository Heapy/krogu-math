# Source247: public large-profile verification

The original large workflow [38837441144](https://github.com/Heapy/krogu-math/actions/runs/38837441144),
attempt1, completed successfully at exact commit
`47447f2111d44735de374ec98d0a6a099a08888f`. This records six retained public
OpenJDK test plans in both baseline and port phases, **12 PASS / 0 FAIL /
128 SKIP**, with complete=true and officialJck=false. It is not official JCK.

Independent actual review report SHA256:
`a1dd45937491ec6cb4b7c1607aec1c6458099617d814c6ebdf0a0a08c560d7bd`;
its346-entry index is
`01fb927522e9edc458739f02427278e4fb32ece336893785ef3711221109a144`.
Parent read the report and verified all346 indexed entries. The original
private review remains at `/private/tmp/krogu-47447-large-producer-review-MWllLHi2`.
No test/compiler/harness was rerun during that metadata review.

## Source and producer/consumer binding

Source247 manifest is `docs/source247.sha256`, SHA
`529cc142d9a42110084155987b3022b36c2309168a159b745bf5191a0359c5a4`.
API source archive SHA is
`3fbf81ef92342b25affc56e1c4071d868f1c6b607b81685e6318ef85b5144e82`;
all247 extracted source hashes verify. Buildb883, Gradle9.8.1/Kotlin2.4.21 and
Liberica25.0.4+9-LTS remain pinned. The unchanged manual workflow retains its
original build, step/job limits, action pins, profiles and resource parameters.

Producer job116769611532 passed its actual3-task JVM build. Its artifact
12019624296 contains only own JAR, SOURCE_COMMIT and SHA256SUMS. The157123-byte
ZIP digest matches both API and upload/download evidence:
`cdad2bdc5789f32a36bb4c4943013ee3931dbf2de5cffed2c3b77ebd335460c4`.
SOURCE_COMMIT equals47447f2 in full. Both checksums pass. Producer, separately
packaged consumer and accepted local source247 JARs are measured byte-identical:
`6e6bc44856543cd79ad3d182d858a31c674e5154873991076ac1732c3c39a421`.
Caches were restored; no empty-cache/hermetic-build claim is made.

Consumer job116769911377 passed. Evidence artifact12019448492 is1179047 bytes;
API and upload digest agree:
`7a830372aacbdfdfe452bcc47a2c011b957a90cad023ff8cc651da500bacda0b`.
Actual results1792115654273 SHA is
`b369806ec69685b5bdac119e61ac2a339ca268c34d1f6f9aad765905bcfc86b1`.
The CI metadata binds run/attempt/head, repository, workflow and own input JAR.

## Actual retained selection and original gates

All59 retained plans produce118 unique primary baseline/port keys. The selected
12 PASS keys are exactly the six PLANNED large plans times both phases, without
omissions, extras or duplicates. All140 semantic rows are unique.

|Plan|Profile|Heap MiB|Original deadline seconds|
|---|---|---:|---:|
|BitLengthOverflow|large-memory-8g|8192|120|
|DoubleValueOverflow|large-memory-8g|8192|120|
|LargeValueExceptions|large-memory-4g|4096|120|
|largeMemory/DivisionOverflow|large-memory-8g|8192|120|
|largeMemory/StringConstructorOverflow|large-memory-8g|8192|120|
|largeMemory/SymmetricRangeTests|large-memory-10g|8192|180|

All12 compile/execute commands exit0 without timeout or incomplete caught-OOM
assertions. Both subsets remain0; seed559, assertions, fast-throw setting,
original VM flags and max-heap8192/physical-memory-auto inputs are retained.
The10g label denotes a physical-memory profile, not a replacement10GiB heap.
Every selected invocation passed its own fresh memory gate:2 require4608MiB
and10 require8704MiB. Actual total physical memory was16766414848 bytes;
fresh free bytes ranged11694419968–11852976128. No benchmark or uncontended-host
claim follows from terminal elapsed times.

All74 referenced raw paths exist. Twelve exact HARNESS_COMPLETE markers and
10 TestNG HARNESS_CASE PASS lines verify completion. Six port core-loading
guards,24 stdout origins and24 class-load entries bind the same own JAR.
All six retained caller audits exit0 without host-math member references or
host-integration bridges. Caller audit metadata was reviewed; no numerical
implementation body was inspected or executed.

All57 upstream,53 prepared and12 actually compiled source pins verify, along
with13 owned-tool source pins and seven dependency POMs. The pinned public TEST
revision/vendor mismatch remains explicit. GPL test notices/text, provenance,
locks and quarantine remain packaged; foreign compiled test/framework artifacts
are excluded from this evidence ZIP. These are observed packaging boundaries.

## Skips and limits

All128 SKIP rows are accounted for:106 unselected normal primary invocations,
16 original disabled methods, two original commented calls and four private
MutableBigInteger exclusions. No selected plan is skipped for capacity.

The inventory retains12 other adaptation-companion descriptions, but their
parents are unselected here and no companion SKIP rows are emitted. Therefore
140/128 remains unchanged after removal of the old sqrt companion. Both
SquareRootTests primary rows are SKIP because standard is unselected. This
large-only result supplies **no restored sqrt-call execution**, normal-profile
acceptance or118 passing primary IDs. Those require their separate exact-head
normal campaign. Supported-platform coverage and the broader numerical/range/
API obligations likewise remain separate from this finite large-profile pass.
