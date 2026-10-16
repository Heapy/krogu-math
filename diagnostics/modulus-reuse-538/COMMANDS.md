# Executed 538 commands

All commands use `JAVA_HOME=/Users/yoda/.sdkman/candidates/java/25.0.4-librca`.
Gradle commands use `--no-daemon --max-workers=1 --console=plain`. No numerical
jobs overlap. Candidate worktree `/private/tmp/krogu-math-538`, commit
`be76fb7fea5de133cc22b685b89faa025d0adb2f`; fresh parent checkout
`PROJECT/.codex-work/538/baseline-worktree`, commit
`781d961b973274771999d0805d2238b7b8831511`.

Focused JVM (terminal exit0, 58 methods/17 suites):

```sh
./gradlew --no-daemon --max-workers=1 :jvmJar :jvmTest \
  --tests '*OddModulusContext538*' --tests '*ModulusReuse538*' \
  --tests '*IntegerModPow*' --tests '*IntegerInverse*' --tests '*Prime*' \
  --tests '*Primality*' --console=plain
```

Focused other targets (terminal exit0, 43 methods/10 suites per target):

```sh
./gradlew --no-daemon --max-workers=1 \
  :jsNodeTest --tests '*OddModulusContext538*' --tests '*ModulusReuse538*' --tests '*IntegerModPow*' --tests '*IntegerInverse*' --tests '*Prime*' --tests '*Primality*' \
  :wasmJsNodeTest --tests '*OddModulusContext538*' --tests '*ModulusReuse538*' --tests '*IntegerModPow*' --tests '*IntegerInverse*' --tests '*Prime*' --tests '*Primality*' \
  :macosArm64Test --tests '*OddModulusContext538*' --tests '*ModulusReuse538*' --tests '*IntegerModPow*' --tests '*IntegerInverse*' --tests '*Prime*' --tests '*Primality*' \
  --console=plain
```

Parent build from the fresh baseline checkout (terminal exit0):

```sh
./gradlew --no-daemon --max-workers=1 :jvmJar --console=plain
```

Artifacts copied before measurement:

```text
baseline.jar  91f0d6a48b8f677f468cbd8b6cee0a617a6d456b96b8132cbd1488c9e92adbf8
candidate.jar 7f084311aea30b74f68db340e28e2ea60e7f4ce2decd7d219d603456f51be79b
```

Smoke from the candidate worktree (terminal exit0; all 130 cases, 260 rows):

```sh
JAVA_OPTS='-Xms64m -Xmx512m -XX:+UseSerialGC -XX:ActiveProcessorCount=1 -XX:-OmitStackTraceInFastThrow' \
  /Users/yoda/.sdkman/candidates/kotlin/current/bin/kotlinr \
  scripts/measure-modulus-reuse-538.main.kts --campaign \
  PROJECT/.codex-work/538/artifacts/baseline.jar 91f0d6a48b8f677f468cbd8b6cee0a617a6d456b96b8132cbd1488c9e92adbf8 \
  PROJECT/.codex-work/538/artifacts/candidate.jar 7f084311aea30b74f68db340e28e2ea60e7f4ce2decd7d219d603456f51be79b \
  PROJECT/.codex-work/538/smoke-1 1 1 1
```

Full campaign: same invocation and options, replacing the final arguments with
`PROJECT/.codex-work/538/campaign-1 3 5 5`. It has been launched; its completion
is not claimed in this record yet. Runtime child argv and options are additionally
recorded by the runner per fork. Parent/child stdout, stderr, process RSS and
executable-only host snapshots remain in the respective raw campaign directory.

`PROJECT` means `/Users/yoda/dev/pet/krogu-math`, expanded to that absolute path in
all actual invocations. Source checksums were captured before builds/measurements;
the baseline/candidate common/JVM manifests differ only in BigInteger.kt. Source
and diagnostic checks after the smoke exited0, with logs retained.


Full unfiltered four-target handle 82656 exited 0: 1898 methods PASS. Bounded paired JS handle 1369 exited 0: 108 rows PASS. The exact additional invocation is in paired-js-1/source-lineage.json; parent JS compile-only command is retained in logs/paired-js-baseline-build.log and the launch command was :compileTestDevelopmentExecutableKotlinJs :jsTestTestDevelopmentExecutableCompileSync :kotlinNpmInstall on the exact parent worktree. No production change followed these executions.
