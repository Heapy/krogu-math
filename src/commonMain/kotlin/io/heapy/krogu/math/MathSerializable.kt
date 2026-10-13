package io.heapy.krogu.math

/** JVM serialization marker. Other platforms have no Java serialization dependency. */
expect interface MathSerializable

internal expect fun serializationProxy(value: BigInteger): Any
internal expect fun serializationProxy(value: BigDecimal): Any
internal expect fun serializationProxy(value: MathContext): Any
