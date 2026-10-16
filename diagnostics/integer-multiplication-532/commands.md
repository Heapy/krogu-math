# Reproducing integer multiplication observations

These are individually invoked commands, not a standalone shell runner. Run from
an accepted 532 source checkout in the coordinated heavy slot. Preserve frozen
artifacts; changed source must get a new JAR name and source manifest.
Actual campaign JSON manifests under `.codex-work/532/logs` retain every command,
launch order, hash and output path for all original and final runs.

```shell
export JAVA_HOME=/Users/yoda/.sdkman/candidates/java/25.0.4-librca
KOTLIN_RUNNER=/Users/yoda/.sdkman/candidates/kotlin/2.4.21/bin/kotlin
ARTIFACTS=/Users/yoda/dev/pet/krogu-math/.codex-work/532/artifacts
PROBE=io.heapy.krogu.math.IntegerMultiplication532Probe
BASE_JAR="$ARTIFACTS/integrated-parent-baseline.jar"
NEW_JAR="$ARTIFACTS/selected-candidate-384.jar"
PROBE_JAR="$ARTIFACTS/entry-warmed-probe.jar"
BASE_SHA=c04f08c4f2cfefb034db3ff208eec25fd8e13db311b2e31ae3a901d391dd02f0
NEW_SHA=e9331e7e6285eec56718ed5e10d2967364d694466ca30c6e90699ab1c9741f9e
PROBE_SHA=$(shasum -a 256 "$PROBE_JAR" | cut -d ' ' -f 1)
# Repeat each public mode in three fresh forks; reverse implementation order in fork 2.
"$KOTLIN_RUNNER" -J-Xms128m -J-Xmx1g -classpath "$BASE_JAR:$PROBE_JAR" "$PROBE" public "$BASE_JAR" "$BASE_SHA" "$PROBE_JAR" "$PROBE_SHA" forward
"$KOTLIN_RUNNER" -J-Xms128m -J-Xmx1g -classpath "$NEW_JAR:$PROBE_JAR" "$PROBE" public "$NEW_JAR" "$NEW_SHA" "$PROBE_JAR" "$PROBE_SHA" forward
# Repeat with public-wide for the 26 medium/large cases.
# square-entry uses three separate sweeps: forward, reverse, forward.
"$KOTLIN_RUNNER" -J-Xms128m -J-Xmx1g -classpath "$NEW_JAR:$PROBE_JAR" "$PROBE" square-entry "$NEW_JAR" "$NEW_SHA" "$PROBE_JAR" "$PROBE_SHA" forward
# Each memory mode is a fresh process; repeat with memory-adaptive/memory-square.
/usr/bin/time -l "$KOTLIN_RUNNER" -J-Xms128m -J-Xmx1g -classpath "$NEW_JAR:$PROBE_JAR" "$PROBE" memory-school "$NEW_JAR" "$NEW_SHA" "$PROBE_JAR" "$PROBE_SHA" forward
# Fresh packaging before freezing any changed candidate: :jvmJar is required.
./gradlew --no-daemon :jvmJar :jvmTest --tests '*IntegerMultiplication532*' --console=plain
./gradlew --no-daemon :jsNodeTest --tests '*IntegerMultiplication532Test*' \
  :wasmJsNodeTest --tests '*IntegerMultiplication532Test*' \
  :macosArm64Test --tests '*IntegerMultiplication532Test*' --console=plain
# Accepted full composed-core gate, without filters:
./gradlew --no-daemon :jvmTest :jsNodeTest :wasmJsNodeTest :macosArm64Test --console=plain
```

Each process requires explicit main/probe JAR paths and SHA values, asserts loaded
origins and rechecks hashes after work. The baseline is bound to integrated parent
`e95836f97e91433b49b005f9589ca6d359bfa2ac` by source-equivalence metadata. Exact
JDK references are outside timing. Final public modes use ≥200 ms warmup,
three warm/seven measured 20 ms-target batches (8,388,608-operation cap). The robust
square-entry mode uses the same prewarm and five samples. The original forced
calibration retains its 8192-operation cap; its full intermediate logs are preserved.
Timing/allocation and heap-polling batches are separate. RSS includes runtime and
reference preparation; no isolated residency claim is made. `/usr/bin/time -l`
needs host permission for sysctl on this machine; the original denied wrapper
result and successful authorized reruns are retained.
