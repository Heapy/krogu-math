package io.heapy.krogu.math

internal actual fun platformParallelAvailable539(): Boolean = nativeParallelContext539()
internal actual fun platformParallelProduct539(a: IntArray, b: IntArray): IntArray? {
    if (!nativeParallelContext539()) return null
    return NativeFrontend539.engine.tryMultiply539(a, b, NativeFrontend539.policy)
}
private object NativeFrontend539 {
    val engine = NativeParallelEngine539(2)
    val policy = MultiplicationPolicy539(64, 16, 384)
}
