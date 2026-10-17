# Public-source Linux addendum recovery and reproduction

This is an additive evidence epoch. Keep the original `diagnostics/platform-multiplication-539` package at0db and its separately retained measured artifacts/source history unchanged. Their old INCOMPLETE status is an accurate historical snapshot, not a relabelled Linux result.

The original public runs are [successful39009038930](https://github.com/Heapy/krogu-math/actions/runs/39009038930) at67ac and [pre-numeric failure39008435845](https://github.com/Heapy/krogu-math/actions/runs/39008435845) atc0ad. The successful artifact API ID12092745268 has SHA256 `b702abbe93502159afb1a1892acc360b9eaaf3f60cc7b4925ce86981a20943c0`,18585761 bytes. This package preserves that exact ZIP, its API ledger, workflow status and complete job-log ZIP. Failed artifact18414425 bytes/SHA256 `cffabaf5e037481d6e394dd9e1d93c7ee847978535d5036950f355237263ff9e` and its logs are also retained.

## Verify and recover existing evidence

Run the existing copied `verify-package.main.kts` against this addendum directory. It validates the exact index/SHA ledger and every ZIP member against its companion `.entries.json`; its output is mechanical `PACKAGE_BINDINGS_PASS_NOT_TASK_ACCEPTANCE`.

```text
kotlinr -J-Xmx512m verify-package.main.kts /absolute/path/to/addendum
```

For an exact committed Git binding, pass `--repo /absolute/path/to/repository --git-ref FULL_EVIDENCE_COMMIT`; the addendum directory must be inside that repository. After integration also verify the original package with its own existing invocation and declared retained-root/source options only if a new actual delta requires it; this addendum does not recreate its accepted7933-member audit.

Extract `success-artifact.zip` only into a fresh owned directory after package verification. Do not overlay an existing runtime/source tree. The archive restores `guard/payload/baseline` and `guard/payload/current` completely: each1090 regular files includes its manifest and all9 hidden members. `guard/run` restores all six baseline/current logs, argv, bindings, CPU/process timelines, result/input/representative checks and child exits; `guard/paired-summary.{json,csv}` retains all28 cases. The failed archive is a separate incomplete-returned-closure epoch: upload omitted9 hidden members per root, and its inventory-order failure occurred before any numeric child. Do not use it as a complete runtime recovery or a performance result.

The original checker command is retained literally in `guard/checker.log.argv.txt`. Its historical Python helper is an existing public539 tool included under `guard/payload/check-public-paired-guard.py`; no new Python automation was introduced. Any local reanalysis writes summaries outside the immutable package and preserves original path/identity records. It is not a numerical rerun.

`git bundle verify integration-history.bundle` and `git bundle list-heads integration-history.bundle` recover/check the new source history relative to the two exact prerequisites in `integration-plan.json`. Fetch bundle heads only into fresh recovery refs. The bundle preserves main docs/adapter and audited975/public tooling commits; it does not pretend to be a prerequisite-free clone or an executed integrated merge. The main integration preview is read-only `git merge-tree`, not a performed merge.

## Fresh execution recipe and scope

The copied workflow and `.main.kts` controller record the exact build/run recipe. It uses clean public checkouts: baseline `07910edf6d5687f1d89dbe3369c4546f648feed2`, current `975182f9bca911be86ef1bc4efedc7039cc8fe36`, tooling `67acda4549e38f235bb842aedeb570f8e1220cea`. Both roots compile sequentially using Gradle9.8.1, Kotlin2.4.21 and BellSoft Liberica25.0.4+9; the stage binds actual compiler/Mocha files and exact source inventories. No macOS/private TAR or manual asset copy supplies the Linux compiler output.

Pinned Node is24.16.0 Linux x64, official archive URL `https://nodejs.org/dist/v24.16.0/node-v24.16.0-linux-x64.tar.xz`, archive SHA256 `d804845d34eddc21dc1092b519d643ef40b1f58ec5dec5c22b1f4bd8fabde6c9`; actual executable SHA256 `b2959781cc5a74c357ffa02367efa8a0330cbb1c9cb347732fdfaaaca381cbcd`. The runtime host reports Linux6.17.0-1022-azure/x64,4 available CPUs and16766414848 total bytes; these observations describe that runner, not an isolation guarantee.

The retained workflow triggers on its isolated audit branch and supports dispatch once its workflow definition is available under GitHub's dispatch rules. Do not trigger it merely to recreate a green outcome. The performed campaign is one fixed six-child, three-pair F/R/F full28 run:1176 TIME,168 REP and168 CHECK. Frozen investigate rule remains median paired ratio>1.15 AND median delta>5ns. CPU is continuously sampled descriptively; it is not an admission filter or proof of causation. All post-start load and adverse pairs remain in the summaries. New executions, if separately authorized, are new epochs and must not overwrite this evidence.
