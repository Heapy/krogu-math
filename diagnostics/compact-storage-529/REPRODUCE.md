# Reproduction and lossless recovery

All final measurements compare accepted Krogu parentf8565cae98081ff0121ce7a40919d8a4cfd92013 with numeric production09e6dc36b3dfaae3878dbe6304b9c9dcaa96735d; matching benchmark-only hookd6d594f9bb5f8da5ea962e87ee92796135e9f3d9 is a separate source delta. Main JAR SHAs aref54132036bcd7b0fa4cda473a1576cb715a7356e4fbdb188f0d650d32735397c and64625bcdbba069763d9da0318dfc143ea9c0a5bcd5e99334744495379d371e37. Preserve the distinction between compiled Release arithmetic and matched Native Debug graph observations.

## Recover exact recorded evidence

Each ZIP member path is relative to the original project-owned `.codex-work/529/` directory. Its paired `.entries.json` enumerates every non-directory member with exact bytes/SHA256. Recover into a NEW directory, not over an active task. For example, from the repository root:

```bash
python3 - <<'PY'
from pathlib import Path
from zipfile import ZipFile
import hashlib, json
package = Path('diagnostics/compact-storage-529')
recovered = Path('.codex-work/529/recovered')
recovered.mkdir(parents=True, exist_ok=False)
archives = list(package.glob('*.zip')) + list(Path('.codex-work/529/retained-evidence').glob('*.zip'))
for archive in sorted(archives):
    ledger = json.loads(archive.with_suffix('.entries.json').read_text())
    rows = ledger['entries']
    with ZipFile(archive) as source:
        assert set(source.namelist()) == {r['path'] for r in rows}
        for row in rows:
            name = row['path']
            assert not Path(name).is_absolute() and '..' not in Path(name).parts
            data = source.read(name)
            assert len(data) == row['bytes']
            assert hashlib.sha256(data).hexdigest() == row['sha256']
            target = recovered / name
            target.parent.mkdir(parents=True, exist_ok=True)
            target.write_bytes(data)
            target.chmod((source.getinfo(name).external_attr >> 16) & 0o777)
print('PASS lossless ZIP recovery and every member hash')
PY
```

The index/SHA ledger binds canonical files; the source manifests bind actual before/after build content, and raw controller envelopes bind loaded origins/flags/process identities. Recovery itself does not rerun arithmetic or turn an old result into a new measurement. Recorded heap gzip ledgers additionally bind recovered uncompressed snapshot bytes. Original failed controls/startup/parser attempts remain alongside successful replacements.

## Pinned commands

Recorded local installation: `JAVA_HOME=/Users/yoda/.sdkman/candidates/java/25.0.4-librca`; Kotlin runner `/Users/yoda/.sdkman/candidates/kotlin/2.4.21/bin/kotlinr`; Node `/Users/yoda/.gradle/nodejs/node-v24.16.0-darwin-arm64/bin/node`. The archived prelaunch/build/raw ledgers contain binary SHA256, actual argv and environment policy. Do not substitute an unverified runtime. Absolute task-owned guard paths in experimental controllers are recorded intentionally; fresh runs require the documented task directory or an explicitly reviewed path adaptation with a new source hash. Never reuse an existing output directory.

From the candidate task worktree, the final unfiltered correctness command was:

```bash
JAVA_HOME=/Users/yoda/.sdkman/candidates/java/25.0.4-librca ./gradlew --no-daemon :jvmTest :jsNodeTest :wasmJsNodeTest :macosArm64Test --console=plain
```

The matched final Release artifact build in each baseline/candidate worktree was:

```bash
JAVA_HOME=/Users/yoda/.sdkman/candidates/java/25.0.4-librca ./gradlew --no-daemon :jvmJar :benchmarks:jvmClasspath :benchmarks:jsProductionExecutableCompileSync :benchmarks:wasmJsProductionExecutableCompileSync :benchmarks:linkReleaseExecutableMacosArm64 --console=plain
```

The unchanged JVM33 runner requires the WORKTREE ROOT as cwd. Its five arguments are baselineJAR, candidateJAR, NEW output directory, baselineSHA, candidateSHA:

```text
<pinned-kotlinr> scripts/measure-compact-storage-529.main.kts BASELINE_JAR CANDIDATE_JAR NEW_OUTPUT f54132036bcd7b0fa4cda473a1576cb715a7356e4fbdb188f0d650d32735397c 64625bcdbba069763d9da0318dfc143ea9c0a5bcd5e99334744495379d371e37
```

Recovered `run-portable-pairs.main.kts` takes platform(js/wasm/native), exact baselineTree, candidateTree, pinnedNode, NEW output, and `scripts/check-compact-portable-529.main.kts`. It executes six alternating processes with `compact529 3 7 20 100000`; each pair is checked separately with `BASELINE_LOG CANDIDATE_LOG 7 100000 TARGET`. Failed JS/Native retained controls are expected recorded failures, not assertions to relax.

For graph replay, the provenance archive contains the reviewed Node/V8 capture/analyzer/reproduction records and Native `native-graph-plan.md`, actual source/executable/dump bindings, Unit file-image proof, schema/ABI provenance and complete per-phase analysis. Native capture uses `:benchmarks:linkDebugExecutableMacosArm64`, then the reviewed180s controller. Actual new captures require their OWN image/ASLR proof and source hashes. Native dump payload+ABI instance extent is not allocator bytes; failed optional Release metadata transfer is preserved, not silently retried or assumed. JVM allocation, runtime slopes and owned graphs are separate protocols.

## Large retained inputs

The complete V8 snapshot archive and all exact measured binaries are preserved under `.codex-work/529/retained-evidence/`, with their own evidence-index/SHA/member ledgers. `retained-evidence.json` binds that durable project-local package and the canonical copies of member manifests. These large payloads are intentionally local retained evidence, not Git blobs; preserve them when removing the completed worktree. No snapshot/member/input was dropped. Both packages must verify.

A fresh checkout alone does not contain these two large archives; obtain/preserve the hash-bound local retained package before full recovery.
