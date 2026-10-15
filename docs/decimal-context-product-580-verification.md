# Contextual product range repair: local verification (580)

Contextual multiplication can now round a private exact product before checking
whether the required final coefficient fits the public BigInteger range. It
does not require an unsupported public exact product merely to produce a
supported rounded decimal result. The exact BigInteger multiplication contract
and unlimited-context multiplication remain unchanged.

This is the actual local integration milestone. The
[original audit](decimal-context-product-580-audit.md),
[proposal](decimal-context-product-580-proposal.md) and
[implementation argument](decimal-context-product-580-implementation.md) retain
their historical source-preparation labels, pins and observations. Their
UNCOMPILED/UNRUN statements describe those preparation milestones; the
separately bound campaign below supplies subsequent execution evidence.

## Mathematical and dispatch scope

After the existing scale/sign prefix, a nonvirtual integer bridge reads stored
signs, bit lengths and magnitudes. For nonzero operands with bit lengths a,b
and L=Int.MAX_VALUE, a+b<=L proves the exact product supported. Larger sums
select private raw arithmetic, including the uncertain a+b=L+1 edge. There is
no smaller production cap or precision cutoff. Zero and unselected products
retain the existing materialized path and its public coefficient multiply.

The new helper copies the inputs into private paged scratch, converts them to
base1e9, forms the exact product and rounds once using guard/sticky digits and
retained parity. All eight modes and both signs are covered. The possible
precision carry is an exact extra decimal drop. Required rounding and carry
precede final coefficient/scale checks; the implementation argument binds
indices, primitive bounds, input isolation and all five live workspaces.

The result is the required coefficient/scale cohort. There is **no optional
trailing-zero stripping**: at precision3, 10^18 retains100@-16. Task566's HIGH
scale rejection when the exact product already fits the requested precision
remains, even if a different trailing-zero cohort could represent the same
number. LOW scale and existing zero/source orientation are also retained.

Truthful restored coefficient subtypes compose with the stored-value theorem:
the bridge invokes no public coefficient metadata, byte, division or multiply
callback. The original public sign prefix remains. This is a source composition
argument, not execution of the selected bridge through a giant public input or
a restored subtype. Dishonest/changing prefixes and inherited callback staging
remain separately classified under541/526; explicit caller overrides and super
entries keep their contracts.

The original giant symbolic product witness was **not executed**. Neither that
public route nor maximum-size private arithmetic has a feasible-time or heap
claim. Repeated base conversion and schoolbook multiplication can be expensive;
task548 resource/range obligations remain separate.

## Actual four-target campaign

The campaign used a fresh no-Git archive of exact1b9 plus the four frozen580
source overlays at `/private/tmp/krogu-580-validation-t7wTW1Oz/root`. The
provisioned Kotlin tooling home was reused; build/results/classes were fresh.
The retained command uses Gradle9.8.1/Kotlin2.4.21/Liberica25.0.4+9, one worker,
in-process compilation and the original4GiB Gradle heap. It builds jvmJar and
runs the full math-package JVM, JS Node, Wasm JS Node and macOS ARM64 suites.

Root execution66073 exited0: **BUILD SUCCESSFUL in16m3s**, all49 actionable
tasks executed. Exact source-derived inventories match the actual XML:

| Target | Suites | Methods | Failures/errors/skips |
|---|---:|---:|---:|
| JVM |152|518|0|
| JS |71|333|0|
| Wasm JS |71|333|0|
| macOS ARM64 |71|333|0|
| Total executions |365|1517|0|

There are no missing/extra methods, duplicate methods or duplicate suites.
The predecessor1501 methods plus four new common methods on four targets give
1517 executions. All225 frozen source files match before and after the run and
against the integrated publication tree. The fresh own JAR is bound below.

The four new methods actually ran on every target, giving16 executions:

1. Direct raw-kernel comparisons use14 bounded Long input pairs, both signs,
   seven precisions and all eight modes. The independent oracle uses a bounded
   exact Long product and a single decimal quotient/remainder, not convolution
   or guard/sticky extraction. Cases cover multiword inputs/outputs, decimal
   row carry, midpoint parity, sticky neighbors, discard and precision carry;
   input arrays are checked unchanged, including on expected failures.
2. Direct raw scale controls cover six Long scale endpoints, required cohort,
   carry/rescue and retained HIGH/no-drop rejection across signs and modes.
3. Symbolic selector controls cover zero, sum=L, uncertain L+1, definitely
   oversized and maximum bit-length sums without allocating giant operands.
4. Bounded ordinary, truthful override and caller-written-super public controls
   cover value/scale, scale endpoints, unlimited multiplication and asymmetric
   zero behavior. These public controls use the old materialized path.

Only the direct internal helper calls exercise new raw arithmetic at these
practical sizes. No giant public selected-bridge execution, restored-coefficient
bridge execution or synthetic smaller threshold is claimed. The retained566,
579, conversion, oracle, scale and dispatch suites also completed.

## Source and evidence binding

| Artifact | SHA-256 |
|---|---|
| BigDecimal.kt | `1c1559c44de0a51bfcd7df0c325dfb9de22e7b6a4588b6dcb612370dc850fa55` |
| BigInteger.kt | `b2fb23547d97f3f7650b6d7dcf4ad53939efb3abac97d9d152376e67dd6685f5` |
| DecimalProduct580.kt | `2ef14e2a2d52b75286f2e5830301a75fe586ed41cb3fbbacb9fd96c346d5234d` |
| Common test | `e8cff7f799607b7902a5e9f37f4fb272357793f33d5ff1b336047301ed743e2b` |
| Implementation argument | `d3c4e6eaf84a4146c935164cb2157a33e8b627ef044ab5ad1a15bb67fa815a59` |
| 225-source manifest | `b2315fea5e93c1d3b0e28c88a33f6b1e78c942cc524b2e18f90023cca05bbb5a` |
| Source test inventory | `e6d42c51b249e572b19685d1e3a4cd21a9e05014b055c06e217207749e216689` |
| Full build/test log | `bb5b990bb03ba7d8d1cef22826b942e036354281c59d80cbf7d21910dbd73455` |
| JVM exact XML audit | `9c5a997ac94a6cb8ca4ba553b0c29759ad64f2cf14da15bc2c3abd1fc33b011c` |
| JS exact XML audit | `3d79db959fcb8afdb2462c83e79cb4069c4f0e3d9c52ac59f41501921c811876` |
| Wasm exact XML audit | `fa19f898e534a632e8d7b26f699078a54171edc456e60db03615e9b21561c6f5` |
| Native exact XML audit | `fae30db7c5d336f30091f34b9c271ba84b6f345264555c2e5b145b2f07e6e370` |
| Post-run/publication source checks | `3863bda19064748f8b7e1c36ef7f24658bed1b42037fa453214bd760ea7ec06a` |
| Fresh own JVM JAR | `4861457772e98ef1716c1c4094c5e5422ee9b0d26235e50d10bb082c4f660df2` |
| Read-only source/runtime evidence capsule | `250f2a2eeca42a614c29378585dfab681139b54d5ca8f54ba307b6de5612b2fc` |

Raw XML and the JAR remain under the isolated root's build directory; command,
log, compact tags, exact audits and source checks remain in its sibling
`evidence/` directory. The parent froze those records, all225 source files and
the fresh JAR in `/private/tmp/krogu-580-validation-t7wTW1Oz/evidence-capsule.tar`.
Independent read-only review accepted the actual exact
inventories, source/JAR binding and finite execution scope. All owned runtime
calls are terminal; the parent released the local numerical mutex (empty
revision782). This documentation work ran no compiler, script, numerical
helper, probe, test or build and grants no further execution.

The earlier floating-conversion binding is preserved read-only at
`/private/tmp/krogu-580-floating-binding-original.je0jHlAb/floating-conversion-543-proof-binding.md`,
SHA-256 `8eec9e86fe8ee9a2c0d30400192a6e41d297ec0916d4bef505939a3229793ac8`.
Its successor changes only the two source pins and one paragraph: all conversion
methods and their digit/prefix/division leaves remain byte-for-byte unchanged.

## Remaining gates

Exact-head all-seven supported CI jobs and fresh hosted public campaigns for
this580 source/JAR remain pending. Earlier public campaigns stay bound to their
own source/JARs; their successes or failures are not relabelled. Official JCK
has not run. These finite local passes do not close task548, broader541/526
classification,542 power composition, Number/JS migration or the full project
goal, and do not claim an all-input execution or performance acceptance.
