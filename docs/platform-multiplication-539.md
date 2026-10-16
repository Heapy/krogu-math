# Platform multiplication and parallel candidate (539)

Status: source preparation, not compiled or measured. No platform threshold or
parallel execution has been adopted. Public `parallelMultiply` still calls the
virtual `multiply` route and the accepted532 constants remain 64/16/384.

Final calibration must use the accepted537 + accepted529 + current main source
composition. Changing the representation may change small-operation cost and
materialization, so today's parent is not a valid final performance baseline.

## Prepared experiment

`MultiplicationPolicy539` separates recursive entry, square leaf and whole-square
entry. The opt-in common-test `MultiplicationCalibration539` is dormant without
an explicit configuration. Its kernel modes sweep one policy dimension around
64/16/384, with distinct operands, self squares, 1:2 and 1:4 shapes. Every forced
result is checked against independent school multiplication before warmup;
JVM additionally checks public JDK products. Inputs and policies are prepared
outside timing. Forward/reverse forks, identical 200 ms prewarming, adaptive
bounded batches and seven default samples are required. Original adverse samples
must remain available; thresholds are conservative choices, not exact optima.

`cost` measures signed Long, unsigned Long, Int carry and array allocation costs
separately. These diagnostics do not prove that another limb radix is faster.
The implementation remains common Kotlin base2^30. A radix or intrinsic/backend
change would need a separate explicit architecture proposal and end-to-end proof.

`public-small` is a paired same-harness guard on the final composed public path.
The wrapper must compare case identities, exact output representations, per-fork
ranges, allocations where observable and declared per-case regression margins.
No averaging may hide a small-case regression.

Time rows distinguish wall time, supported process CPU, caller-thread allocation
and a coarse live-heap sample. Caller allocation excludes worker allocation.
Native last-GC heap is not invocation residency; Wasm host heap is not an owned
WasmGC graph. External whole-process RSS/high-water and scoped JVM pool peaks
must be reported with their preparation/runtime overhead, not as exact scratch
size. Array ownership and arena upper bounds supply a separate logical payload
accounting. No unsupported metric is silently converted to zero.

## JVM candidate and ownership

The common top-level frame owns only sums and borrows immutable operand views.
Three child products each own distinct output and scratch. Combine runs exactly
once after all children are terminal; it mutates only the middle child. A fair
scheduling comparison uses the SAME frame serially and in parallel, alongside
the existing serial adaptive kernel, so tree/allocation differences are visible.
Memory accounting includes sums, three kept child outputs, final output, live
child arenas, scheduler objects and input views; it must not claim a memory win.

`ParallelCandidate539` is an unadopted JVM scheduler. A shared nonblocking gate
bounds additional active workers to two across instances. Parent and child
nested invocations, enclosing ForkJoinPool tasks, unavailable processor budget
and pre-interrupted callers fall back synchronously. Queue capacity is two;
workers are daemon threads with a 30-second idle timeout. Multiple experimental
instances may retain idle threads, so the gate does not bound all idle thread
objects or unrelated application load. Any adopted route would use a single
library-owned scheduler with a measured size threshold.

Executor acceptance follows `Executor.execute`: returning accepts the task;
throwing rejects it. Accepted task owner slots are preallocated and recorded
without allocation after submission. Rejection joins earlier children before
synchronous fallback. Child or caller arithmetic failures, including OOM, are
propagated without retry only after all accepted children finish. Interruption
is remembered while joining and restored before return. References and permits
are released in finally. Four source tests exercise nested/processor/interrupt
entry, partial rejection, interruption during join, global exhaustion and child
failure. Their shared global gate assumes sequential test-method execution.

JS/Wasm have no adopted worker transport; serialization/copy/startup costs and
runtime threading support must be evaluated rather than assume JVM behavior.
Native concurrency requires an explicit supported worker/ownership design;
no obsolete API opt-in or intrinsic backend is introduced by this candidate.
An unavailable or unprofitable parallel route retains the public synchronous
fallback, but that conclusion requires target evidence and a recorded decision.

## Execution and evidence gates still required

All current source and tests are UNRUN. Final source/artifact manifests, exact
runtime/toolchain identities and fresh build logs are mandatory before any
performance claim. One numerical campaign per host; local correctness and
remote performance may overlap only under coordinator authorization.

The read-only `claws` audit found Linux x86_64 KVM, Ryzen7700X (8 cores/16 threads),
26 GiB RAM and BellSoft25.0.4+9-LTS at an explicit SDK path. User-owned0700 scratch
is `/home/kokoko/.local/share/krogu-math/perf`. Only a symlink to the existing JDK
was staged. No remote numerical work or compiler ran. Transfer only manifest
listed immutable artifacts/probes/runtime dependencies, with bounded disk use;
verify hashes remotely, clear ambient options/environment and own timeout/process
cleanup. Node/Kotlin/Native toolchain installation is not implied by this audit.
