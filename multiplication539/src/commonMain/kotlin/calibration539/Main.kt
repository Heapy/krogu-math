package calibration539

import io.heapy.krogu.math.MagnitudeMultiplication
import io.heapy.krogu.math.MultiplicationPolicy539

internal expect fun register539(handler: (Int) -> Unit)
internal expect fun input539(index: Int): IntArray
internal expect fun same539(): Boolean
internal expect fun parameter539(index: Int): Int
internal expect fun childIndex539(): Int
internal expect fun clearOutputs539()
internal expect fun output539(index: Int, words: IntArray)
private var pending539: MagnitudeMultiplication.ParallelFrame539? = null

/** Separate executable entry: worker imports never register or require Mocha. */
fun main() = register539(::invoke539)

private fun policy539() = MultiplicationPolicy539(parameter539(0), parameter539(1), parameter539(2))
private fun operands539(): Pair<IntArray, IntArray> {
    val a = input539(0)
    val b = input539(1)
    require(a.size <= 65536 && b.size <= 65536)
    require(a.all { it in 0..0x3fffffff } && b.all { it in 0..0x3fffffff })
    return a to if (same539()) { require(a.contentEquals(b)); a } else b
}

/** Private diagnostic protocol; all mathematics invokes exact shared production source. */
private fun invoke539(mode: Int) {
    clearOutputs539()
    try {
        when (mode) {
            0, 4, 6 -> {
                val (a, b) = operands539()
                val policy = policy539()
                val result = when (mode) {
                    // Whole-operation serial comparison, with normal entry policy.
                    0 -> MagnitudeMultiplication.multiplyConfigured539(a, b, policy)
                    // A recursive child must NOT reapply whole-square entry override.
                    4 -> MagnitudeMultiplication.multiply(a, b, policy.karatsuba, policy.squareLeaf)
                    else -> {
                        val frame = checkNotNull(MagnitudeMultiplication.prepareParallel539(a, b, policy))
                        frame.combine(frame.child(0), frame.child(1), frame.child(2))
                    }
                }
                output539(0, result)
            }
            1 -> {
                check(pending539 == null)
                val (a, b) = operands539()
                val frame = checkNotNull(MagnitudeMultiplication.prepareParallel539(a, b, policy539()))
                pending539 = frame
                // Only low/high inputs cross to workers; caller computes the middle.
                val aliases = IntArray(2)
                for (child in 0..1) {
                    val (left, right) = frame.copiedChildInputs539(child)
                    aliases[child] = if (left === right) 1 else 0
                    output539(2 * child, left); output539(2 * child + 1, right)
                }
                output539(4, aliases)
            }
            2 -> output539(0, checkNotNull(pending539).child(childIndex539()))
            3 -> {
                val frame = checkNotNull(pending539)
                try { output539(0, frame.combine(input539(0), input539(1), input539(2))) }
                finally { pending539 = null }
            }
            5 -> pending539 = null
            else -> error("Unknown diagnostic operation")
        }
    } catch (problem: Throwable) {
        pending539 = null
        throw problem
    }
}
