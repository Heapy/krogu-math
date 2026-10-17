// SPDX-License-Identifier: Apache-2.0
package io.heapy.krogu.math

internal actual fun emptyPowMagnitude(): Nothing = arrayIndexFailure(0, 0)

internal actual fun arrayIndexFailure(index: Int, length: Int): Nothing =
    throw java.lang.ArrayIndexOutOfBoundsException("Index $index out of bounds for length $length")
