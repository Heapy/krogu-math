// SPDX-License-Identifier: Apache-2.0
package io.heapy.krogu.math

private class ArrayIndexOutOfBoundsException(message: String) : IndexOutOfBoundsException(message)

internal actual fun emptyPowMagnitude(): Nothing =
    throw ArrayIndexOutOfBoundsException("Index 0 out of bounds for length 0")
