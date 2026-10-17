package preparation598

/** UNCOMPILED source preparation. All checks/observations are OUTSIDE future timing. */
enum class ExactOperation { ADD, SUBTRACT, MULTIPLY }

data class Capability(val eligibleAfterFixtureGate: Boolean, val reason: String)

interface PeerAdapter<I : Any, D : Any> {
    val id: String
    fun integer(text: String): I
    fun decimal(text: String): D
    fun integerOperation(operation: ExactOperation, a: I, b: I): I
    fun decimalOperation(operation: ExactOperation, a: D, b: D): D
    fun integerText(value: I): String
    /** Decimal numeric observation only; this does not establish scale/cohort parity. */
    fun decimalText(value: D): String
    val fixedScaleHalfUp: Capability
    fun divideFixedScaleHalfUp(a: D, b: D, scale: Int): D
    val significantHalfEven: Capability
}

class CapabilityUnavailable(message: String) : IllegalStateException(message)
