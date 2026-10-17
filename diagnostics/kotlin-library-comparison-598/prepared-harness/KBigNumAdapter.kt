package preparation598

import io.github.gatrongdev.kbignum.math.*

/** Public tagged READMEv0.0.20; exact signatures and artifact compatibility UNCOMPILED. */
object KBigNumAdapter : PeerAdapter<KBigInteger, KBigDecimal> {
    override val id = "kbignum-0.0.20-desktop"
    override fun integer(text: String) = text.toKBigInteger()
    override fun decimal(text: String) = KBigDecimal.fromString(text)
    override fun integerOperation(operation: ExactOperation, a: KBigInteger, b: KBigInteger) = when (operation) {
        ExactOperation.ADD -> a + b
        ExactOperation.SUBTRACT -> a - b
        ExactOperation.MULTIPLY -> a * b
    }
    override fun decimalOperation(operation: ExactOperation, a: KBigDecimal, b: KBigDecimal) = when (operation) {
        ExactOperation.ADD -> a + b
        ExactOperation.SUBTRACT -> a - b
        ExactOperation.MULTIPLY -> a * b
    }
    override fun integerText(value: KBigInteger) = value.toString()
    override fun decimalText(value: KBigDecimal) = value.toString()
    override val fixedScaleHalfUp = Capability(true, "documented explicit divide(scale,rounding); signed/cohort fixture gate required")
    override fun divideFixedScaleHalfUp(a: KBigDecimal, b: KBigDecimal, scale: Int) =
        a.divide(b, scale = scale, rounding = KBRoundingMode.HalfUp)
    override val significantHalfEven = Capability(false, "no documented significant-digit MathContext34/HALF_EVEN equivalent")
}
