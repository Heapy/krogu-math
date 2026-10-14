#!/usr/bin/env kotlinr
// SPDX-License-Identifier: Apache-2.0
// Metadata-only bootstrap; verifies official Maven Central checksums before use.
import java.io.File
import java.net.HttpURLConnection
import java.net.URI
import java.security.MessageDigest
import java.util.zip.ZipFile

val work = File("build/math-tck/dependency-metadata").canonicalFile.apply { mkdirs() }
val artifacts = linkedMapOf(
    "org/testng/testng/7.11.0/testng-7.11.0" to "Apache-2.0",
    "org/slf4j/slf4j-api/2.0.16/slf4j-api-2.0.16" to "MIT",
    "org/jcommander/jcommander/1.83/jcommander-1.83" to "Apache-2.0",
    "org/webjars/jquery/3.7.1/jquery-3.7.1" to "MIT",
    "org/junit/platform/junit-platform-console-standalone/1.13.4/junit-platform-console-standalone-1.13.4" to "EPL-2.0;EPL-1.0;Apache-2.0;BSD-3-Clause",
    "org/jetbrains/kotlin/kotlin-stdlib/2.4.21/kotlin-stdlib-2.4.21" to "Apache-2.0",
    "org/jetbrains/annotations/13.0/annotations-13.0" to "Apache-2.0",
)
fun hash(algorithm: String, bytes: ByteArray) = MessageDigest.getInstance(algorithm).digest(bytes)
    .joinToString("") { "%02x".format(it.toInt() and 255) }
fun get(path: String): ByteArray? {
    val connection = URI("https://repo.maven.apache.org/maven2/$path").toURL().openConnection() as HttpURLConnection
    connection.connectTimeout = 20000; connection.readTimeout = 30000
    if (connection.responseCode == 404) return null
    check(connection.responseCode == 200) { "HTTP ${connection.responseCode}: $path" }
    return connection.inputStream.use { it.readBytes() }
}
val rows = mutableListOf<String>()
for ((base, license) in artifacts) {
    for (extension in listOf("jar", "pom")) {
        val path = "$base.$extension"
        var algorithm = ""
        var expected = ""
        for ((suffix, name) in listOf("sha512" to "SHA-512", "sha256" to "SHA-256", "sha1" to "SHA-1")) {
            val checksum = get("$path.$suffix") ?: continue
            expected = checksum.toString(Charsets.UTF_8).trim().substringBefore(' ').lowercase()
            algorithm = name
            check(expected.matches(Regex("[0-9a-f]+")))
            break
        }
        check(algorithm.isNotEmpty()) { "No official checksum for $path" }
        val bytes = requireNotNull(get(path))
        check(hash(algorithm, bytes) == expected) { "Official checksum mismatch: $path" }
        val file = File(work, path.substringAfterLast('/'))
        file.writeBytes(bytes)
        rows += listOf(path, hash("SHA-256", bytes), algorithm, expected, license).joinToString("\t")
        if (extension == "jar") {
            ZipFile(file).use { zip ->
                val notices = zip.entries().asSequence().filter {
                    !it.isDirectory && Regex("(?i)(license|notice|copyright)").containsMatchIn(it.name) &&
                        !it.name.endsWith(".class")
                }.toList()
                for ((index, entry) in notices.withIndex()) {
                    File(work, "${file.name}.notice-$index.txt").writeBytes(zip.getInputStream(entry).use { it.readBytes() })
                }
                File(work, "${file.name}.notice-entries.tsv").writeText(notices.mapIndexed { index, entry -> "$index\t${entry.name}" }.joinToString("\n", postfix = "\n"))
                println("Verified $path ($license); retained ${notices.size} notice entries")
            }
        }
    }
}
File(work, "dependency-lock.candidate.tsv").writeText("path\tsha256\tofficialAlgorithm\tofficialChecksum\tlicense\n" + rows.joinToString("\n", postfix = "\n"))
println("Pinned ${rows.size} artifact/POM records; no binaries executed.")
