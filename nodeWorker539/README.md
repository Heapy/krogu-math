# Private embedded worker sources

The root library's `worker-payload` Kotlin Toolchain plugin compiles these
original JS/Wasm worker sources together with the allowlisted common kernel in
an isolated task workspace. It embeds the complete linked runtime closure in
the root KLIB; consumers need no asset-copy step or dependency on this directory.

This directory is not a published module. Its former Gradle descriptor is
available in historical commit `975182f9bca911be86ef1bc4efedc7039cc8fe36` for
reproducing the completed #539 measurements. Those outputs remain distinct from
current Kotlin Toolchain builds.
