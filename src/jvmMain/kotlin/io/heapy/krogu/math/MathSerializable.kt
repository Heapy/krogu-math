package io.heapy.krogu.math

actual typealias MathSerializable = java.io.Serializable

internal actual fun serializationProxy(value: BigInteger): Any = IntegerProxy(value.toByteArray())
internal actual fun serializationProxy(value: BigDecimal): Any = DecimalProxy(value.unscaledValue().toByteArray(), value.scale())
internal actual fun serializationProxy(value: MathContext): Any = ContextProxy(value.precision, value.roundingMode.name)

private class IntegerProxy(private val bytes: ByteArray) : java.io.Serializable {
    private fun readResolve(): Any = BigInteger(bytes)
    companion object { private const val serialVersionUID: Long = 1L }
}
private class DecimalProxy(private val bytes: ByteArray, private val scale: Int) : java.io.Serializable {
    private fun readResolve(): Any = BigDecimal(BigInteger(bytes), scale)
    companion object { private const val serialVersionUID: Long = 1L }
}
private class ContextProxy(private val precision: Int, private val rounding: String) : java.io.Serializable {
    private fun readResolve(): Any = MathContext(precision, RoundingMode.valueOf(rounding))
    companion object { private const val serialVersionUID: Long = 1L }
}
