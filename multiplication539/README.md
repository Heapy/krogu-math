# Historical multiplication experiment

These sources belong to the completed, unpublished #539 common-kernel worker
experiment. They are retained for provenance, not registered in the current
Kotlin Toolchain build and not part of the kotlinx-benchmark suites.

Reproduce its original Gradle compilation from an isolated checkout of
`975182f9bca911be86ef1bc4efedc7039cc8fe36`, where the opt-in
`-Pmultiplication539=true` module, original source layout and build descriptors
are preserved. Use the exact artifact bindings and commands in
`diagnostics/platform-multiplication-539`; current-toolchain output must not be
substituted for those historical measured artifacts.

Current annotated comparisons are under `benchmarks/standard` and
`benchmarks/peers`. The production embedded worker is built independently by
`plugins/worker-payload` from the original kernel and `nodeWorker539` sources.
