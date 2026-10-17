package io.heapy.krogu.math

/** Non-JVM adapter for source sets that need the platform-neutral random bridge. */
actual open class PlatformRandom(private val source: kotlin.random.Random = kotlin.random.Random.Default) {
    actual open fun nextBytes(bytes: ByteArray) { source.nextBytes(bytes) }
}
