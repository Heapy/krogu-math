// SPDX-License-Identifier: Apache-2.0
package io.heapy.krogu.math

private class ArrayIndexOutOfBoundsException(message: String) : IndexOutOfBoundsException(message)

internal actual fun emptyPowMagnitude(): Nothing = arrayIndexFailure(0, 0)

internal actual fun arrayIndexFailure(index: Int, length: Int): Nothing =
    throw ArrayIndexOutOfBoundsException("Index $index out of bounds for length $length")
