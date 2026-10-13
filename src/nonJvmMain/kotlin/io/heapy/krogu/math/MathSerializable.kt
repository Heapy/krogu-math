package io.heapy.krogu.math

actual interface MathSerializable

internal actual fun serializationProxy(value: BigInteger): Any = value
internal actual fun serializationProxy(value: BigDecimal): Any = value
internal actual fun serializationProxy(value: MathContext): Any = value
