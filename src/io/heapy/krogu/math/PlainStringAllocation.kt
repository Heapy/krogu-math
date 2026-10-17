// SPDX-License-Identifier: Apache-2.0
package io.heapy.krogu.math

/** Fail before constructing a plain layout whose length cannot fit in an Int. */
internal expect fun plainStringTooLarge(): Nothing
