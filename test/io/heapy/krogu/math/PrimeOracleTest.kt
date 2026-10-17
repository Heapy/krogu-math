package io.heapy.krogu.math

import kotlin.test.*

class PrimeOracleTest {
    @Test fun primalityCertaintyContracts() = verify { it[0] == "prime" }
    @Test fun boundedSuccessorContracts() = verify { it[0] == "next" && BigInteger(it[1]).bitLength() <= 81 }
    @Test fun successorsThrough127Bits() = verify { it[0] == "next" && BigInteger(it[1]).bitLength() in 82..127 }
    @Test fun successorsThrough255Bits() = verify { it[0] == "next" && BigInteger(it[1]).bitLength() in 128..255 }
    // Keep each family within Mocha's per-test limit on hosted runners,
    // without dropping fixtures or relaxing the timeout.
    @Test fun successorsInLower256BitHalf() = verify {
        it[0] == "next" && BigInteger(it[1]).let { n -> n.bitLength() == 256 && !n.testBit(254) }
    }
    @Test fun successorsInUpper256BitHalf() = verify {
        it[0] == "next" && BigInteger(it[1]).let { n -> n.bitLength() == 256 && n.testBit(254) }
    }
    @Test fun successorsAt257Bits() = verify { it[0] == "next" && BigInteger(it[1]).bitLength() == 257 }
    @Test fun successorsAbove257Bits() = verify { it[0] == "next" && BigInteger(it[1]).bitLength() > 257 }
    @Test fun successorCallbackContracts() = verify { it[0] == "nextTrace" }

    private fun verify(include: (Array<String>) -> Boolean) {
        for (row in jdk25PrimeCases().filter(include)) {
            val n = BigInteger(row[1])
            val actual = try {
                when (row[0]) {
                    "prime" -> n.isProbablePrime(row[2].toInt()).toString()
                    "next" -> n.nextProbablePrime().toString()
                    "nextTrace" -> successorTrace(row[1])
                    else -> error("Unknown prime case ${row[0]}")
                }
            } catch (e: Exception) { "!${e::class.simpleName}" }
            assertEquals(row[3], actual, row.take(3).toString())
        }
    }
    private fun successorTrace(text: String): String {
        val calls = mutableListOf<String>()
        val observed = object : BigInteger(text) {
            override fun compareTo(other: BigInteger): Int { calls += "compareTo"; return super.compareTo(other) }
            override fun add(value: BigInteger): BigInteger { calls += "add"; return super.add(value) }
            override fun setBit(n: Int): BigInteger { calls += "setBit"; return super.setBit(n) }
            override fun isProbablePrime(certainty: Int): Boolean { calls += "prime:$certainty"; return super.isProbablePrime(certainty) }
            override fun bitLength(): Int { calls += "bitLength"; return super.bitLength() }
            override fun signum(): Int { calls += "signum"; return super.signum() }
            override fun abs(): BigInteger { calls += "abs"; return super.abs() }
        }
        val result = try { observed.nextProbablePrime().toString() }
            catch (e: Exception) { "!${e::class.simpleName}:${e.message}" }
        return "$result|${calls.joinToString(",")}"
    }
    @Test fun publishedStrongPseudoprimesAreRejected() {
        // These pass many initial prime bases; a truncated deterministic basis is insufficient.
        for (text in listOf("3825123056546413051", "318665857834031151167461", "3317044064679887385961981"))
            assertFalse(BigInteger(text).isProbablePrime(100), text)
        assertEquals(BigInteger("318665857834031151167461"), BigInteger("399165290221") * BigInteger("798330580441"))
    }
}
