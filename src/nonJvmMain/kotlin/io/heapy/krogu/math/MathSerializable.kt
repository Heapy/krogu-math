package io.heapy.krogu.math

actual interface MathSerializable

internal actual class MathObjectInputStream
internal actual fun restoreSerializedFields(stream: MathObjectInputStream) = Unit
internal actual fun restoreSerializedMagnitude(owner: BigInteger, words: IntArray) = Unit
internal actual fun invalidSerializedState(message: String): Nothing = throw IllegalStateException(message)

internal actual fun serializationProxy(value: BigInteger): Any = value
internal actual fun serializationProxy(value: BigDecimal): Any = value
internal actual fun serializationProxy(value: MathContext): Any = value
