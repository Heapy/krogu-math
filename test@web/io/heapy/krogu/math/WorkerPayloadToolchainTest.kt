package io.heapy.krogu.math

import kotlin.test.Test
import kotlin.test.assertContentEquals
import kotlin.test.assertNotNull

/** Exercise the independently linked embedded closure without enabling the declined public route. */
class WorkerPayloadToolchainTest {
    @Test
    fun embeddedOriginalWorkerClosureExecutesBothSlots() {
        if (!nodeParallelAvailable539()) {
            println("WORKER_PAYLOAD_CAPABILITY_UNAVAILABLE: Node builtins and more than two available CPUs are required")
            return
        }
        val lease = assertNotNull(acquireNodeParallel539())
        try {
            // Polynomial digits in base 2^30; these products need no carries.
            lease.submit(0, intArrayOf(3, 2), intArrayOf(5, 4), false, 64, 16)
            val square = intArrayOf(7, 11)
            lease.submit(1, square, square, true, 64, 16)
            val product = lease.join(0)
            val squared = lease.join(1)
            // The kernel returns the full left.size + right.size buffer.
            assertContentEquals(intArrayOf(15, 22, 8, 0), product)
            assertContentEquals(intArrayOf(49, 154, 121, 0), squared)
            println("WORKER_PAYLOAD_EXECUTED slots=2 products=verified")
        } finally {
            // Retire this test-owned generation after both joins (or quarantine
            // accepted work on failure); production awaits terminal workers.
            lease.release(true)
        }
    }
}
