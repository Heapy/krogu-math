package io.heapy.krogu.math

actual typealias MathSerializable = java.io.Serializable

internal actual typealias MathObjectInputStream = java.io.ObjectInputStream
internal actual fun restoreSerializedFields(stream: MathObjectInputStream) = stream.defaultReadObject()
// Keep the normal object's storage reference final (including safe-publication
// semantics). Java deserialization alone must replace the stream-owned array.
private val serializedMagnitudeField = BigInteger::class.java.getDeclaredField("words").apply { isAccessible = true }
internal actual fun restoreSerializedMagnitude(owner: BigInteger, words: IntArray) {
    serializedMagnitudeField.set(owner, words)
}
internal actual fun invalidSerializedState(message: String): Nothing = throw java.io.InvalidObjectException(message)

private inline fun validatedProxy(restore: () -> Any): Any = try {
    restore()
} catch (failure: RuntimeException) {
    throw java.io.InvalidObjectException("Invalid math serialization proxy").apply { initCause(failure) }
}

internal actual fun serializationProxy(value: BigInteger): Any = IntegerProxy(value.toByteArray())
internal actual fun serializationProxy(value: BigDecimal): Any = DecimalProxy(value.unscaledValue().toByteArray(), value.scale())
internal actual fun serializationProxy(value: MathContext): Any = ContextProxy(value.precision, value.roundingMode.name)

private class IntegerProxy(private val bytes: ByteArray) : java.io.Serializable {
    private fun readResolve(): Any = validatedProxy { BigInteger(bytes) }
    companion object { private const val serialVersionUID: Long = 1L }
}
private class DecimalProxy(private val bytes: ByteArray, private val scale: Int) : java.io.Serializable {
    private fun readResolve(): Any = validatedProxy { BigDecimal(BigInteger(bytes), scale) }
    companion object { private const val serialVersionUID: Long = 1L }
}
private class ContextProxy(private val precision: Int, private val rounding: String) : java.io.Serializable {
    private fun readResolve(): Any = validatedProxy { MathContext(precision, RoundingMode.valueOf(rounding)) }
    companion object { private const val serialVersionUID: Long = 1L }
}
