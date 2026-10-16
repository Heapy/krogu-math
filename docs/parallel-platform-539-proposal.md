# Target concurrency feasibility proposal (539)

This is a source/design proposal, not evidence that any parallel policy wins.
All mathematical multiplication remains the reviewed common Kotlin kernel.
The public synchronous method and virtual subclass route must remain valid on
every target. No platform implementation is adopted by this document.

## Required frontend boundary

Only ordinary exact-class operands with certified sign/range metadata may enter
an internal scheduling route. Zero, small/ineligible/unbalanced shapes, missing
runtime capability and nested/resource exhaustion take the existing synchronous
route. Subtypes retain virtual `multiply` behavior. After accepted529 composition,
obtain each compact/wide magnitude once using its explicit representation seam;
never allocate a new array per limb getter. Publication uses the same normalized
sign/final-range checks. Scheduling must not mutate either operand.

## Native: genuine candidate proposed before source opt-ins

Kotlin's [Worker API](https://kotlinlang.org/api/core/kotlin-stdlib/kotlin.native.concurrent/-worker/)
provides a private scheduling mechanism but is marked
[ObsoleteWorkersApi](https://kotlinlang.org/api/core/kotlin-stdlib/kotlin.native.concurrent/-obsolete-workers-api/).
It remains supported; official guidance recommends encapsulation because its API
will eventually be replaced. That warning is not proof of poor multiplication
performance. A private candidate can evaluate it without changing public API or
introducing a foreign arithmetic backend.

Proposed candidate lifetime is one invocation, with one/two owned Workers and at
most one queued arithmetic child each. The caller computes remaining children.
Preallocate Worker/Future/result owner slots before starting. A global AtomicInt
nonblocking two-permit gate limits additional active workers across callers;
per-thread nested depth forces reentrant synchronous fallback. Two extra workers
require at least three available processor slots. A Worker receives an immutable
common-frame reference and child index; publication through the scheduling API
orders its read-only input. Child output/arena belongs exclusively to that child
until its Future is terminal. Parent combines only after every accepted Future
has been consumed. Only the middle output is mutated during recombination.

Worker/Future are value classes: nullable generic owner-slot stores may box and
allocate. Each still-local nonnullable handle remains owned through registration.
A failed Worker registration terminates/joins that local Worker; failed accepted
Future registration consumes that local Future before outer cleanup drains all
previous owners. Primary failure identity survives best-effort cleanup suppression.
Deterministic second-worker registration-fault tests cover both boundaries.
Failure at a later start or submission joins prior accepted Futures, requests
termination for every created Worker and joins termination before releasing
permits/returning. Preserve the first arithmetic exception; do not retry OOM or
return while readers still own inputs. No persistent Worker queue, global pool,
idle timer or process-liveness leak is necessary for this first candidate.
Start/enqueue/join/termination costs are part of its measured invocation cost.
This differs intentionally from a warmed JVM pool; both targets must report their
own actual execution policy. If startup prevents benefit, a reusable pool would
need a separately reviewed lifecycle policy and new measurements.

Implementation would require explicit source-only opt-ins for ObsoleteWorkersApi
and the current common atomic API. No compiler/toolchain flag or numerical backend
would change. This design was sent to the coordinator BEFORE adding those
opt-ins. The approved design is now implemented only in nativeTest as an UNRUN
diagnostic candidate; implementation review and pinned-runtime verification are
still pending. Production has no new Native opt-in or scheduling route.

## JS and Wasm: transport and synchronous-call constraints

[Node worker threads](https://nodejs.org/api/worker_threads.html) run JavaScript
in separate workers and support ArrayBuffer transfer and SharedArrayBuffer
sharing. They do not automatically share Kotlin object graphs. Transferring an
operand's backing buffer would detach the caller's immutable input; any transport
candidate must instead use owned copies or explicit read-only shared storage.
Kotlin/WasmGC object references are not assumed transferable to another module's
heap. A Wasm transport must explicitly copy magnitude words across the host seam;
its copy, second runtime/module setup and result publication costs belong in the
measurement. No shared-WasmGC compiler experiment is authorized by this proposal.

The public method returns a value synchronously. Promise callbacks cannot deliver
a result while the same caller blocks its event loop. A Node-specific candidate
therefore needs a real synchronous completion protocol: a separate worker entry,
owned MessageChannel data and SharedArrayBuffer atomic status, with explicit
worker failure/deadline handling. A naive Promise plus busy wait is invalid.
Browser availability differs, so missing worker/shared-memory/synchronous-wait
capabilities must retain synchronous arithmetic, without an unconditional Node
module import in generic library code.

Concrete first feasibility controls should separately measure:

- empty worker startup/termination and warm empty roundtrip;
- copied two-input/one-output Int32 magnitude payloads at the SAME planned sizes;
- module/runtime startup and copy into/out of Wasm-owned arrays;
- synchronous wakeup/protocol cost, worker CPU and retained runtime/payload cost.

These are necessary lower-bound controls, not enough to claim a speedup. If even
the transport lower bound exceeds same-shape serial multiplication, that rules
out that size/policy in the tested runtime. If affordable, a real common-kernel
child module must be measured before deciding adoption. It is not valid to reject
all sizes/platforms from an API warning or one transport observation.

A dedicated calibration compilation/module may be needed to expose a private
worker entry without running Mocha on import or publishing new library API. It
must compile the exact source-bound common kernel, preserve artifact provenance,
and avoid accidentally measuring a copied divergent implementation. This build
seam and the synchronous protocol need review before implementation. The optional unpublished multiplication539 module now compiles the SAME original
source paths directly and registers a diagnostic-only host callback, with no
root-library dependency, physical source copy, new public library API or compiler
flag. Its JS/Wasm bridge is UNRUN. The synchronous host worker protocol and common-frame recombination now have an
UNRUN source implementation in measure-worker-transport-539.main.kts. Only owned
child-input copies cross/transfer, accepted jobs are joined before combine, and
shared status plus synchronous port reads avoid parent event-loop dependence.
Implementation review and actual runtime remain pending; no performance decision
follows.

## Final decisions remain empirical

The task requires platform thresholds and CPU/latency/peak-memory tradeoffs after
accepted537/529. Serial threshold calibration and primitive Long/unsigned/Int/
allocation controls already have reviewed source infrastructure. JVM parallel
observation infrastructure is drafted separately. Native source, the JS/Wasm module and host protocol are prepared; independent
source review, actual runtime, aggregation and final adoption gates remain explicit
pending work, not waived acceptance. Every eventual policy
must keep a measured small-input guard and exact synchronous fallback. Retaining
synchronous execution is a valid final choice only with a concrete target-level
capability/cost explanation and the relevant actual evidence.
