package nodeworker539
import io.heapy.krogu.math.MagnitudeMultiplication

internal expect fun register539(handler: () -> Unit)
internal expect fun length539(side: Int): Int
internal expect fun word539(side: Int, index: Int): Int
internal expect fun parameter539(index: Int): Int
internal expect fun same539(): Boolean
internal expect fun output539(words: IntArray)
fun main() = register539 {
    val k = parameter539(0); val s = parameter539(1)
    require(k == 64 && s == 16) { "Worker policy identity" }
    fun input(side: Int): IntArray = IntArray(length539(side).also { require(it >= 0) }) { index ->
        word539(side, index).also { require(it in 0..0x3fffffff) }
    }
    val a = input(0); val right = input(1)
    val b = if (same539()) { require(a.contentEquals(right)); a } else right
    // Recursive child policy: never reapply whole-operation square entry.
    output539(MagnitudeMultiplication.multiply(a, b, k, s))
}
