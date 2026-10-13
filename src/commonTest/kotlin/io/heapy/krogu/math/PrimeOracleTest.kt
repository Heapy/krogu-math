package io.heapy.krogu.math

import kotlin.test.*

class PrimeOracleTest {
    @Test fun primeCertaintyAndSuccessorContracts() {
        for (row in jdk25PrimeCases()) {
            val n = BigInteger(row[1])
            val actual = try {
                when (row[0]) {
                    "prime" -> n.isProbablePrime(row[2].toInt()).toString()
                    "next" -> n.nextProbablePrime().toString()
                    else -> error("Unknown prime case ${row[0]}")
                }
            } catch (e: Exception) { "!${e::class.simpleName}" }
            assertEquals(row[3], actual, row.take(3).toString())
        }
    }
    @Test fun publishedStrongPseudoprimesAreRejected() {
        // These pass many initial prime bases; a truncated deterministic basis is insufficient.
        for (text in listOf("3825123056546413051", "318665857834031151167461", "3317044064679887385961981"))
            assertFalse(BigInteger(text).isProbablePrime(100), text)
        assertEquals(BigInteger("318665857834031151167461"), BigInteger("399165290221") * BigInteger("798330580441"))
    }
}
