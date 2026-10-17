package io.heapy.krogu.math

import java.util.TreeMap
import java.util.TreeSet
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.time.measureTime

class DecimalKey536JvmTest {
    @Test
    fun sortedAndHashCollectionsAgree() {
        val values = listOf("-20", "-20.00", "0", "0.000", "2", "2.0", "2.00", "200")
            .map(::BigDecimal) + listOf(
                BigDecimal(BigInteger("100"), Int.MIN_VALUE),
                BigDecimal(BigInteger("1000"), Int.MIN_VALUE + 1),
                BigDecimal(BigInteger.ZERO, Int.MAX_VALUE),
                BigDecimal("7".repeat(2048)), BigDecimal("7".repeat(2048) + ".00"))
        val hashMap = HashMap<DecimalKey, Int>()
        val treeMap = TreeMap<DecimalKey, Int>()
        values.forEachIndexed { index, value ->
            hashMap[value.toDecimalKey()] = index
            treeMap[value.toDecimalKey()] = index
        }
        assertEquals<Map<DecimalKey, Int>>(hashMap, treeMap)
        assertEquals<Set<DecimalKey>>(values.map(::DecimalKey).toHashSet(), TreeSet(values.map(::DecimalKey)))
        values.forEach { assertEquals(hashMap[it.toDecimalKey()], treeMap[it.toDecimalKey()]) }
    }

    @Test
    fun measureCreationAndRepeatedHashCost() {
        if (System.getenv("KROGU_MEASURE_DECIMAL_KEYS") != "1") return
        val cases = linkedMapOf(
            "compact" to BigDecimal("12345.6700"),
            "zero-extreme" to BigDecimal(BigInteger.ZERO, Int.MAX_VALUE),
            "scale-underflow" to BigDecimal(BigInteger("1000"), Int.MIN_VALUE),
            "coefficient-4096-digits" to BigDecimal("7".repeat(4092) + "0000"),
        )
        var checksum = 1
        for ((name, value) in cases) {
            val iterations = if (name == "coefficient-4096-digits") 200 else 20_000
            val keys = List(64) { value.toDecimalKey() }
            repeat(iterations / 5) { publishedKey = value.toDecimalKey() }
            repeat(200_000) { checksum = 31 * checksum + keys[it % keys.size].hashCode() + it }
            val creation = measureTime {
                repeat(iterations) {
                    val key = value.toDecimalKey()
                    checksum = 31 * checksum + key.hashCode() + it
                    publishedKey = key // Volatile escape prevents removal of key construction.
                }
            }
            val publishBaseline = measureTime { repeat(iterations) { publishedKey = keys[it % keys.size] } }
            val compare = measureTime { repeat(iterations) { checksum = 31 * checksum + value.compareTo(value) + it } }
            val stripped = if (name == "scale-underflow") null else measureTime {
                repeat(iterations) { checksum = 31 * checksum + value.stripTrailingZeros().hashCode() + it }
            }
            val hashing = measureTime {
                repeat(1_000_000) { checksum = 31 * checksum + keys[it % keys.size].hashCode() + it }
            }
            println("DecimalKey536 $name creation+first-hash+publish=${creation.inWholeNanoseconds.toDouble() / iterations} ns/op repeated-hash+checksum=${hashing.inWholeNanoseconds / 1_000_000.0} ns/op creation-iterations=$iterations")
            println("DecimalKey536 $name volatile-publish=${publishBaseline.inWholeNanoseconds.toDouble() / iterations} ns/op compareTo-self+checksum=${compare.inWholeNanoseconds.toDouble() / iterations} ns/op strip+hash+checksum=${stripped?.let { it.inWholeNanoseconds.toDouble() / iterations }} ns/op")
        }
        println("DecimalKey536 checksum=$checksum published=${publishedKey != null} java=${System.getProperty("java.version")} vm=${System.getProperty("java.vm.name")} os=${System.getProperty("os.name")} arch=${System.getProperty("os.arch")}")
    }

    companion object {
        @Volatile private var publishedKey: DecimalKey? = null
    }
}
