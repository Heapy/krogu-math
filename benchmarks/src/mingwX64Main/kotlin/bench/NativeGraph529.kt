package bench

// Explicit unsupported opt-in graph mode; ordinary benchmarks remain available.
internal actual fun compactNativeSnapshot529(label: String): Unit =
    error("Native graph capture supports macOS only: $label")
