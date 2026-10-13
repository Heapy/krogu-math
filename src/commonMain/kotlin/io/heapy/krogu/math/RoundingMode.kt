package io.heapy.krogu.math

/** Direction used when discarded digits are nonzero. Ordinals match the legacy Java constants. */
enum class RoundingMode {
    UP, DOWN, CEILING, FLOOR, HALF_UP, HALF_DOWN, HALF_EVEN, UNNECESSARY;

    companion object {
        @kotlin.jvm.JvmStatic
        fun valueOf(rm: Int): RoundingMode = entries.getOrNull(rm)
            ?: throw IllegalArgumentException("Invalid rounding mode")
    }
}
