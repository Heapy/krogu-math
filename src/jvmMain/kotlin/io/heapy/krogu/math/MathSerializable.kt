package io.heapy.krogu.math

actual typealias MathSerializable = java.io.Serializable

internal actual typealias MathObjectInputStream = java.io.ObjectInputStream
internal actual fun restoreSerializedFields(stream: MathObjectInputStream) = stream.defaultReadObject()
// Keep the normal object's storage reference final (including safe-publication
// semantics). Java deserialization alone must replace the stream-owned array.
private val serializedMagnitudeField = BigInteger::class.java.getDeclaredField("wideWords").apply { isAccessible = true }
internal actual fun restoreSerializedMagnitude(owner: BigInteger, words: IntArray) {
    serializedMagnitudeField.set(owner, words)
}
internal actual fun invalidSerializedState(message: String): Nothing = throw java.io.InvalidObjectException(message)
