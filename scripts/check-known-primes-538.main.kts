#!/usr/bin/env kotlinr
// SPDX-License-Identifier: Apache-2.0
// Parent: PORT.jar SHA256 NEW-output. Strict public primality outcomes, no expected-false adapter.
import java.io.File
import java.lang.management.ManagementFactory
import java.net.URLClassLoader
import java.security.MessageDigest
import java.util.concurrent.TimeUnit

fun sha(file: File): String = MessageDigest.getInstance("SHA-256").digest(file.readBytes())
    .joinToString("") { "%02x".format(it) }
check(System.getProperty("java.runtime.version") == "25.0.4+9-LTS" && System.getProperty("java.vendor") == "BellSoft")
check(KotlinVersion.CURRENT.toString() == "2.4.21")
val exponents = listOf(521, 607, 1279, 2203, 2281, 3217, 4253)
if (args.firstOrNull() == "--child") {
    require(args.size == 4)
    val jar = File(args[1]).canonicalFile
    check(sha(jar) == args[2])
    val exponent = args[3].toInt().also { require(it in exponents) }
    val runtime = File(System.getProperty("kotlin.home"), "lib/kotlin-stdlib.jar").canonicalFile
    check(runtime.isFile)
    URLClassLoader(arrayOf(jar.toURI().toURL(), runtime.toURI().toURL()), ClassLoader.getPlatformClassLoader()).use { loader ->
        val type = loader.loadClass("io.heapy.krogu.math.BigInteger")
        check(File(type.protectionDomain.codeSource.location.toURI()).canonicalFile == jar)
        val text = java.math.BigInteger.ONE.shiftLeft(exponent).subtract(java.math.BigInteger.ONE).toString()
        val value = type.getConstructor(String::class.java).newInstance(text)
        val method = type.getMethod("isProbablePrime", Int::class.javaPrimitiveType)
        val memory = ManagementFactory.getThreadMXBean() as com.sun.management.ThreadMXBean
        check(memory.isThreadAllocatedMemorySupported && memory.isCurrentThreadCpuTimeSupported)
        memory.isThreadAllocatedMemoryEnabled = true
        memory.isThreadCpuTimeEnabled = true
        val thread = Thread.currentThread().threadId()
        val allocation = memory.getThreadAllocatedBytes(thread)
        val cpu = memory.currentThreadCpuTime
        val start = System.nanoTime()
        val result = method.invoke(value, 100)
        val elapsed = System.nanoTime() - start
        val cpuElapsed = memory.currentThreadCpuTime - cpu
        val allocated = memory.getThreadAllocatedBytes(thread) - allocation
        println("exponent,certainty,result,elapsed_ns,cpu_ns,allocated_bytes,input_sha256,artifact_sha256")
        val inputSha = MessageDigest.getInstance("SHA-256").digest(text.toByteArray())
            .joinToString("") { "%02x".format(it) }
        println("$exponent,100,$result,$elapsed,$cpuElapsed,$allocated,$inputSha,${args[2]}")
        check(result == true) { "Known Mersenne prime returned $result" }
        check(sha(jar) == args[2])
    }
} else {
    require(args.size == 3)
    val jar = File(args[0]).canonicalFile
    check(sha(jar) == args[1])
    val output = File(args[2]).canonicalFile
    require(!output.exists()) { "Refusing overwrite $output" }
    output.mkdirs()
    val script = File("scripts/check-known-primes-538.main.kts").canonicalFile
    val scriptSha = sha(script)
    File(output, "bindings.txt").writeText("jar=$jar\njar_sha256=${args[1]}\nscript_sha256=$scriptSha\n" +
        "certainty=100\nchild_deadline_seconds=120_including_script_compilation\ninternal_witness_randomness_not_seeded=true\n")
    val runner = File(System.getProperty("kotlin.home"), "bin/kotlinr").canonicalFile
    for (exponent in exponents) {
        check(sha(jar) == args[1] && sha(script) == scriptSha)
        val command = listOf(runner.path, script.path, "--child", jar.path, args[1], "$exponent")
        val builder = ProcessBuilder(command).redirectOutput(File(output, "$exponent.csv"))
            .redirectError(File(output, "$exponent.stderr.txt"))
        listOf("JAVA_TOOL_OPTIONS", "JDK_JAVA_OPTIONS", "_JAVA_OPTIONS", "KOTLIN_OPTS", "JAVACMD").forEach { builder.environment().remove(it) }
        builder.environment()["JAVA_HOME"] = System.getProperty("java.home")
        builder.environment()["JAVA_OPTS"] = "-Xms64m -Xmx512m -XX:+UseSerialGC -XX:ActiveProcessorCount=1"
        builder.environment()["KOTLIN_MAIN_KTS_COMPILED_SCRIPTS_CACHE_DIR"] = File(output, "script-cache").apply { mkdirs() }.path
        println("launch p=$exponent")
        val process = builder.start()
        println("p=$exponent pid=${process.pid()}")
        if (!process.waitFor(120, TimeUnit.SECONDS)) {
            val handles = process.descendants().toList() + process.toHandle()
            handles.reversed().filter { it.isAlive }.forEach { it.destroy() }
            process.waitFor(3, TimeUnit.SECONDS)
            handles.filter { it.isAlive }.forEach { it.destroyForcibly() }
            val deadline = System.nanoTime() + TimeUnit.SECONDS.toNanos(5)
            for (handle in handles) {
                val left = deadline - System.nanoTime()
                if (handle.isAlive && left > 0) try { handle.onExit().get(left, TimeUnit.NANOSECONDS) }
                catch (_: java.util.concurrent.TimeoutException) { }
            }
            val cleanup = "surviving_owned_pids=${handles.filter { it.isAlive }.map { it.pid() }} root_alive=${process.isAlive}"
            File(output, "$exponent.timeout.txt").writeText(cleanup)
            error("p=$exponent deadline; $cleanup; no Boolean outcome claimed")
        }
        check(process.exitValue() == 0) { "p=$exponent exit=${process.exitValue()}, inspect retained evidence" }
        val lines = File(output, "$exponent.csv").readLines()
        check(lines.size == 2 && lines[1].split(',').let { it.size == 8 && it[0] == "$exponent" && it[1] == "100" && it[2] == "true" && it[7] == args[1] })
        check(sha(jar) == args[1] && sha(script) == scriptSha)
        println("p=$exponent TRUE")
    }
    File(output, "COMPLETE.txt").writeText("PASS seven strict public isProbablePrime(100) results; not official TCK certification\n")
}
