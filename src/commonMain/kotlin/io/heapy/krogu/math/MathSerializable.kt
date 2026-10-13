package io.heapy.krogu.math

/** JVM serialization marker. Other platforms have no Java serialization dependency. */
expect interface MathSerializable

/** Private serialization hooks use this alias only on JVM. */
internal expect class MathObjectInputStream
internal expect fun restoreSerializedFields(stream: MathObjectInputStream)
internal expect fun restoreSerializedMagnitude(owner: BigInteger, words: IntArray)
internal expect fun invalidSerializedState(message: String): Nothing

internal expect fun serializationProxy(value: BigInteger): Any
internal expect fun serializationProxy(value: BigDecimal): Any
internal expect fun serializationProxy(value: MathContext): Any
