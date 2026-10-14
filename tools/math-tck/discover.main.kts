#!/usr/bin/env kotlinr
// SPDX-License-Identifier: Apache-2.0
// Metadata-only bootstrap. Never invokes javac, Gradle, or a math test.
import java.io.File
import java.net.URI
import java.security.MessageDigest

val revision = "7b65d74bcbd7c3a0b4c1f5c5bd85e64aa2e51b4a"
val work = File("build/math-tck").canonicalFile
check(File(".gitignore").readLines().contains("build/")) { "build/ must be ignored" }
work.mkdirs()
fun digest(algorithm: String, bytes: ByteArray) = MessageDigest.getInstance(algorithm)
    .digest(bytes).joinToString("") { "%02x".format(it.toInt() and 255) }
fun download(url: String): ByteArray = URI(url).toURL().openConnection().apply {
    connectTimeout = 20000; readTimeout = 30000
    setRequestProperty("User-Agent", "krogu-math-public-test-inventory")
}.getInputStream().use { it.readBytes() }
val trees = linkedMapOf(
    "BigDecimal" to "2db454303a4e6c3e03caf3e7e41437b91584c482",
    "BigInteger" to "cc136ecb7056f63b5ccf73a11722cee4a0866777",
    "RoundingMode" to "343d977c74562099ca18652fdfc76d13c70c31cf",
)
val expectedCounts = mapOf("BigDecimal" to 28, "BigInteger" to 27, "RoundingMode" to 1)
val records = mutableListOf<String>()
for ((directory, tree) in trees) {
    val json = download("https://api.github.com/repos/openjdk/jdk25u/git/trees/$tree?recursive=1").toString(Charsets.UTF_8)
    check(Regex("\"truncated\"\\s*:\\s*false").containsMatchIn(json)) { "truncated or invalid subtree $directory" }
    File(work, "$directory-tree.json").writeText(json)
    // This deliberately narrow parser accepts only the flat Git tree blob records.
    val objects = Regex("\\{[^{}]*\\}").findAll(json).map { it.value }.toList()
    var count = 0
    for (entry in objects) {
        fun field(name: String) = Regex("\"$name\"\\s*:\\s*\"([^\"]*)\"").find(entry)?.groupValues?.get(1)
        if (field("type") != "blob") continue
        val relative = requireNotNull(field("path"))
        check(relative.matches(Regex("[A-Za-z0-9_./-]+")) && relative.split('/').none { it == ".." })
        check(field("mode") == "100644") { "unexpected file mode: $relative" }
        val path = "test/jdk/java/math/$directory/$relative"
        val target = File(work, "upstream/$path")
        val bytes = if (target.isFile) target.readBytes() else
            download("https://raw.githubusercontent.com/openjdk/jdk25u/$revision/$path")
        val blob = digest("SHA-1", "blob ${bytes.size}\u0000".toByteArray() + bytes)
        check(blob == field("sha")) { "Git blob mismatch: $path" }
        target.parentFile.mkdirs(); target.writeBytes(bytes)
        records += listOf(path, blob, digest("SHA-256", bytes), bytes.size.toString()).joinToString("\t")
        count++
        println("verified $path")
    }
    check(count == expectedCounts.getValue(directory)) { "Incomplete inventory: $directory ($count)" }
}
val supportPath = "test/lib/jdk/test/lib/RandomFactory.java"
val supportBlob = "8930a380008afcdde696e50e81c9c56a2738739e"
val support = File(work, "upstream/$supportPath")
val supportBytes = if (support.isFile) support.readBytes() else
    download("https://raw.githubusercontent.com/openjdk/jdk25u/$revision/$supportPath")
check(digest("SHA-1", "blob ${supportBytes.size}\u0000".toByteArray() + supportBytes) == supportBlob)
support.parentFile.mkdirs(); support.writeBytes(supportBytes)
records += listOf(supportPath, supportBlob, digest("SHA-256", supportBytes), supportBytes.size.toString()).joinToString("\t")
check(records.isNotEmpty())
File(work, "source-lock.candidate.tsv").writeText("path\tgitBlobSha1\tsha256\tbytes\n" + records.sorted().joinToString("\n", postfix = "\n"))
println("Verified ${records.size} files. Candidate lock: build/math-tck/source-lock.candidate.tsv")
