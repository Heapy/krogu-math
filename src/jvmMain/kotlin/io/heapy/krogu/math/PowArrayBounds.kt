// SPDX-License-Identifier: Apache-2.0
package io.heapy.krogu.math

internal actual fun emptyPowMagnitude(): Nothing =
    throw java.lang.ArrayIndexOutOfBoundsException("Index 0 out of bounds for length 0")
