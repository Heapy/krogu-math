# Power-of-two radix formatting proof

`BigInteger` stores a nonzero magnitude as little-endian base-`2^30` limbs.
For radix `2^k`, where `k` is 1 through 5, `toPowerOfTwoString` emits one
digit for each successive `k`-bit group of that stored magnitude.

The highest limb is normalized and nonzero. Its bit length is therefore
`(words.size - 1) * 30 + (32 - countLeadingZeroBits(top))`, and the emitted
digit count is `ceil(bitLength / k)`. Both calculations use `Long`; the final
capacity is converted to `Int` only after checking the public string-size
bound. If the sign prefix plus digits cannot fit in an `Int`, the existing
`plainStringTooLarge()` contract is used: JVM throws `OutOfMemoryError`, and
non-JVM targets throw the platform-independent `Error` before allocation.

For output position `i`, the low-order bit of its group is `i * k`. The
corresponding limb is `floor((i * k) / 30)` and the intra-limb offset is the
remainder. Since `k <= 5`, a group touches at most two adjacent limbs. The
highest group starts below `bitLength`, so its computed word index and every
other index are within `words`; the second-limb read is guarded by the same
bound. The only shifts are the stored offset and `30 - offset` (at most 29),
and masking yields a digit index in `0..31`. Iterating positions from the
most significant group to the least significant group produces canonical
leading-zero-free text. This covers every limb boundary, including groups
that cross the 30-bit boundary.

The largest symbolic hexadecimal witness has `2^31 - 1` magnitude bits, so
its hexadecimal digit count is `ceil((2^31 - 1) / 4) = 536870912`; a
negative result adds one prefix character. The loop uses a `Long` bit offset,
so its `Int.MAX_VALUE`-scale index distance does not overflow. No smaller
arbitrary cap is introduced.

The fast path is entered only for a valid power-of-two radix, an exact
`BigInteger` receiver, and an exact positive `BigInteger` result from the
existing virtual `abs()` call. The sign prefix is emitted from the original
receiver after that call. Zero, invalid radices, and substituted subclasses
retain the existing division-based path and its dispatch behavior. The fast
path visits each output digit once and uses constant temporary storage; its
cost is linear in the output length plus the string builder's required
memory, without relying on a timing claim.

## Actual focused validation (root2313)

The accepted source is BigInteger
`e4dc75b253c689b0f3c634f02f9e491c9a1dcfae396a11135be4427c69ce7b68`;
new common tests are `5bdf9eb29c231dfdaa195c32fdbbe59f80f70773b1f1c55dca7055b24bd3dd1e`.
The original accepted proof `ca52c1bb…` is preserved in the evidence source
freeze. Initial literal errors and callback-count assertions were corrected
before compilation; accepted tests assert numerical behavior.

Fresh `/private/tmp/krogu-radix559-validation-zfdIvp/root` is an archive of
`b419d1d1b8fb5fe5aeaf6b83e8c27b13e843ae1b` plus exactly those two overlays.
All220 source files matched both isolated and publication trees after execution;
manifest `849d826ddad5ff05976a19847807c6bbacd510a7a7c8b1dd61e83181a6c6cdae`.
Task579 remained isolated and was not compiled in this campaign.

One serial Gradle9.8.1/Kotlin2.4.21/Liberica25.0.4 invocation used the existing
4GiB in-process compiler, one worker and original limits. Each of JVM, JS/Node,
Wasm/Node and macOS ARM64 selected PowerOfTwoRadix559Test, PortableOracleTest,
IntegerParserOracleTest, IntegerParserRange559Test and CharacterOracleTest;
`jvmJar` was also built. Only the provisioned Kotlin tooling home was reused.
Session22212 exited0, BUILD SUCCESSFUL6m5s,49 executed tasks. Actual XML contains
5suites/18 exact methods per target: **72 executions,8 new**, with no failures,
errors, skips, omissions or duplicates. All calls are terminal and the sole
local window was explicitly released (empty mutex revision778).

| Retained artifact | SHA-256 |
|---|---|
| `evidence/four-target.log` | `8c2ad0b88988694bced67f65fc3ddc5e212b19f7ab0ed1132ea917d321d0276c` |
| `evidence/xml-audit.json` | `5df53233d1608ebf2debb7420fe9e8168274ebbc15a76b4136749da02cbb3f2a` |
| Fresh own JVM JAR | `856fc0d43404d03c7fe3e0947d2b9d14eb853ed1fae41ac57167c8d12e229dfd` |

A subsequent read-only javap inspection of this own JAR confirms JVM exact-class
checks compile to ordinary Object.getClass comparisons. No foreign numerical
implementation was inspected. No giant magnitude/string, public harness or
benchmark ran locally. The historical timeout does not isolate its stalled
stage; full supported-platform CI and a fresh original large run remain gates.
