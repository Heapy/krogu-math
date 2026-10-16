// SPDX-License-Identifier: Apache-2.0
package io.heapy.krogu.math

/**
 * Calls [BigInteger.toByte] or [BigDecimal.toByte] through the concrete type,
 * preserving subclass overrides despite Kotlin/JS issue KT-79282.
 * Other [Number] implementations use their usual platform conversion.
 * Import and call this extension explicitly; it does not replace [Number.toByte].
 */
fun Number.toKroguByte(): Byte = when (this) {
    is BigInteger -> toByte()
    is BigDecimal -> toByte()
    else -> toByte()
}

/**
 * Calls the krogu type's virtual [Number.toShort] conversion, working around KT-79282.
 * Other [Number] implementations retain their usual platform behavior, including its limitations.
 */
fun Number.toKroguShort(): Short = when (this) {
    is BigInteger -> toShort()
    is BigDecimal -> toShort()
    else -> toShort()
}

/**
 * Calls the krogu type's virtual [Number.toInt] conversion, working around KT-79282.
 * This retains truncation/low-bit semantics; it is not an exactness check.
 * Other [Number] implementations retain their usual platform behavior, including its limitations.
 */
fun Number.toKroguInt(): Int = when (this) {
    is BigInteger -> toInt()
    is BigDecimal -> toInt()
    else -> toInt()
}

/**
 * Calls the krogu type's virtual [Number.toLong] conversion, working around KT-79282.
 * This retains truncation/low-bit semantics; it is not an exactness check.
 * Other [Number] implementations retain their usual platform behavior, including its limitations.
 */
fun Number.toKroguLong(): Long = when (this) {
    is BigInteger -> toLong()
    is BigDecimal -> toLong()
    else -> toLong()
}

/**
 * Calls the krogu type's virtual [Number.toFloat] conversion, working around KT-79282.
 * The Float conversion is selected directly, rather than converting through Double.
 * Other [Number] implementations retain their usual platform behavior, including its limitations.
 */
fun Number.toKroguFloat(): Float = when (this) {
    is BigInteger -> toFloat()
    is BigDecimal -> toFloat()
    else -> toFloat()
}

/**
 * Calls the krogu type's virtual [Number.toDouble] conversion, working around KT-79282.
 * Other [Number] implementations retain their usual platform behavior, including its limitations.
 */
fun Number.toKroguDouble(): Double = when (this) {
    is BigInteger -> toDouble()
    is BigDecimal -> toDouble()
    else -> toDouble()
}
