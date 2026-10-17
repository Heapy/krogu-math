package preparation598

import java.math.BigDecimal
import java.math.BigInteger
import java.math.RoundingMode

object JdkAdapter : PeerAdapter<BigInteger, BigDecimal> {
    override val id = "jdk-bellsoft-25.0.4+9"
    override fun integer(text: String) = BigInteger(text)
    override fun decimal(text: String) = BigDecimal(text)
    override fun integerOperation(operation: ExactOperation, a: BigInteger, b: BigInteger) = when (operation) {
        ExactOperation.ADD -> a.add(b)
        ExactOperation.SUBTRACT -> a.subtract(b)
        ExactOperation.MULTIPLY -> a.multiply(b)
    }
    override fun decimalOperation(operation: ExactOperation, a: BigDecimal, b: BigDecimal) = when (operation) {
        ExactOperation.ADD -> a.add(b)
        ExactOperation.SUBTRACT -> a.subtract(b)
        ExactOperation.MULTIPLY -> a.multiply(b)
    }
    override fun integerText(value: BigInteger) = value.toString()
    override fun decimalText(value: BigDecimal) = value.toString()
    override val fixedScaleHalfUp = Capability(true, "explicit scale and HALF_UP; fixture gate required")
    override fun divideFixedScaleHalfUp(a: BigDecimal, b: BigDecimal, scale: Int) = a.divide(b, scale, RoundingMode.HALF_UP)
    override val significantHalfEven = Capability(true, "MathContext available; original MC34 separate from fixed-scale")
}
