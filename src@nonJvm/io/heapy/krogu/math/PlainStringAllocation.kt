// SPDX-License-Identifier: Apache-2.0
package io.heapy.krogu.math

// Error is available on JS, Wasm and Native. Explicitly throwing it avoids
// relying on platform heap exhaustion (which can terminate a Native process).
internal actual fun plainStringTooLarge(): Nothing =
    throw Error("too large to fit in a String")
