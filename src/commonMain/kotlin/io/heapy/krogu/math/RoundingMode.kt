package io.heapy.krogu.math

/** Direction used when discarded digits are nonzero. Ordinals match the legacy Java constants. */
enum class RoundingMode {
    UP, DOWN, CEILING, FLOOR, HALF_UP, HALF_DOWN, HALF_EVEN, UNNECESSARY;

    @Suppress("NON_FINAL_MEMBER_IN_OBJECT")
    companion object {
        @kotlin.jvm.JvmStatic
        open fun valueOf(rm: Int): RoundingMode = entries.getOrNull(rm)
            ?: throw IllegalArgumentException("Invalid rounding mode")
    }
}
