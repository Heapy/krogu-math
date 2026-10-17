package preparation598

import com.ionspin.kotlin.bignum.integer.BigInteger
import com.ionspin.kotlin.bignum.decimal.BigDecimal

/** Public README0.3.10 arithmetic only. No presumed JDK scale or tie-mode mapping. */
object IonsPinAdapter : PeerAdapter<BigInteger, BigDecimal> {
    override val id = "ionspin-bignum-0.3.10"
    override fun integer(text: String) = BigInteger.parseString(text, 10)
    override fun decimal(text: String) = BigDecimal.parseString(text)
    override fun integerOperation(operation: ExactOperation, a: BigInteger, b: BigInteger) = when (operation) {
        ExactOperation.ADD -> a + b
        ExactOperation.SUBTRACT -> a - b
        ExactOperation.MULTIPLY -> a * b
    }
    override fun decimalOperation(operation: ExactOperation, a: BigDecimal, b: BigDecimal) = when (operation) {
        ExactOperation.ADD -> a + b
        ExactOperation.SUBTRACT -> a - b
        ExactOperation.MULTIPLY -> a * b
    }
    override fun integerText(value: BigInteger) = value.toString(10)
    override fun decimalText(value: BigDecimal) = value.toStringExpanded()
    override val fixedScaleHalfUp = Capability(false, "DecimalMode precision/scale propagation is not JDK fixed-scale divide")
    override fun divideFixedScaleHalfUp(a: BigDecimal, b: BigDecimal, scale: Int): BigDecimal =
        throw CapabilityUnavailable("No verified native fixed-scale equivalent; no divide-then-round adapter")
    override val significantHalfEven = Capability(false, "HALF_EVEN public mapping unverified; README tie descriptions conflict; untimed oracle gate pending")
}
