package io.heapy.krogu.math

/** Forced calibration policy, prepared outside timing. No platform optimum is implied. */
internal data class MultiplicationPolicy539(
    val karatsuba: Int,
    val squareLeaf: Int,
    val wholeSquareEntry: Int,
) {
    init { require(karatsuba >= 4 && squareLeaf >= 1 && wholeSquareEntry >= 0) }
}
