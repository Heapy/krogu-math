// SPDX-License-Identifier: Apache-2.0
package io.heapy.krogu.math

internal actual fun plainStringTooLarge(): Nothing =
    throw java.lang.OutOfMemoryError("too large to fit in a String")
