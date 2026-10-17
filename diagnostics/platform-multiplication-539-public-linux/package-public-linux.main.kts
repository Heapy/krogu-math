import java.io.File
import java.security.MessageDigest
import java.util.zip.ZipFile

// Additive package only. The accepted 0db package and its indices are never rewritten.
val taskRoot = File("/Users/yoda/dev/pet/krogu-math/.codex-work/539").canonicalFile
val staging = File(taskRoot, "packaging/public-linux-addendum")
require(args.size == 2) { "OUTPUT_NEW_DIRECTORY INDEPENDENT_DISPOSITION_JSON" }
val output = File(args[0]).canonicalFile
val disposition = File(args[1]).canonicalFile
require(!output.exists() && disposition.isFile)
require(output.toPath().startsWith(taskRoot.toPath()))
require(output.mkdirs())

fun sha(file: File): String = file.inputStream().use { input ->
    val digest = MessageDigest.getInstance("SHA-256")
    val buffer = ByteArray(1024 * 1024)
    while (true) { val n = input.read(buffer); if (n < 0) break; digest.update(buffer, 0, n) }
    digest.digest().joinToString("") { "%02x".format(it) }
}
fun quote(text: String): String = buildString {
    append('"')
    for (c in text) when (c) {
        '\\' -> append("\\\\")
        '"' -> append("\\\"")
        '\n' -> append("\\n")
        '\r' -> append("\\r")
        '\t' -> append("\\t")
        else -> { require(c.code >= 32); append(c) }
    }
    append('"')
}
fun json(value: Any?): String = when (value) {
    null -> "null"
    is String -> quote(value)
    is Boolean, is Number -> value.toString()
    is Map<*, *> -> value.entries.joinToString(",", "{", "}") { quote(it.key as String) + ":" + json(it.value) }
    is Iterable<*> -> value.joinToString(",", "[", "]") { json(it) }
    else -> error("Unsupported JSON value")
}
fun safe(name: String): String {
    require(name.isNotEmpty() && !name.startsWith('/') && '\\' !in name && name.none { it.code < 32 })
    require(name.split('/').none { it.isEmpty() || it == "." || it == ".." })
    return name
}
fun row(name: String, file: File) = mapOf("path" to safe(name), "bytes" to file.length(), "sha256" to sha(file))
val sourceFiles = linkedMapOf(
    "success-artifact.zip" to File(taskRoot, "public-linux-guard-67acda4-artifact.zip"),
    "success-logs.zip" to File(taskRoot, "public-linux-guard-67acda4-logs.zip"),
    "success-artifacts-api.json" to File(taskRoot, "public-linux-guard-67acda4-artifacts.json"),
    "success-status.json" to File(taskRoot, "public-linux-guard-67acda4-status.json"),
    "owner-receipt.json" to File(taskRoot, "public-linux-guard-actual-39009038930-owner-receipt.json"),
    "failure-artifact.zip" to File(taskRoot, "public-linux-guard-c0ad6de0-artifact.zip"),
    "failure-logs.zip" to File(taskRoot, "public-linux-guard-c0ad6de0-logs.zip"),
    "failure-artifacts-api.json" to File(taskRoot, "public-linux-guard-c0ad6de0-artifacts.json"),
    "pre-numeric-failure.json" to File(taskRoot, "public-linux-guard-pre-numeric-failure.json"),
    "source-method-review.json" to File(taskRoot, "independent-public-linux-guard-source-method-review.json"),
    "pre-numeric-fix-review.json" to File(taskRoot, "independent-public-linux-pre-numeric-failure-fix-review.json"),
    "public-linux-js-guard-539.yml" to File(taskRoot, "public-linux-guard-worktree/.github/workflows/public-linux-js-guard-539.yml"),
    "run-public-linux-js-guard.main.kts" to File(taskRoot, "public-linux-guard-worktree/scripts/run-public-linux-js-guard.main.kts"),
    "verify-package.main.kts" to File(taskRoot, "packaging/verify-package.main.kts"),
    "independent-disposition.json" to disposition,
    "independent-disposition.md" to File(taskRoot, "independent-final-js-linux-retention-disposition.md"),
    "independent-actual-audit.json" to File(taskRoot, "independent-public-linux-39009038930-actual-audit.json"),
    "independent-source-binding.json" to File(taskRoot, "independent-public-linux-source-and-tooling-binding-supplement.json"),
    "independent-reproduction.md" to File(taskRoot, "independent-public-linux-39009038930-reproduction.md"),
    "package-public-linux.main.kts" to File(staging, "package-public-linux.main.kts"),
    "REPORT.md" to File(staging, "REPORT.md"),
    "REPRODUCE.md" to File(staging, "REPRODUCE.md"),
    "integration-plan.json" to File(staging, "integration-plan.json"),
    "current-policy-decision.json" to File(staging, "current-policy-decision.json"),
    "integration-history.bundle" to File(staging, "integration-history.bundle"),
    "integration-history-verification.log" to File(staging, "integration-history-verification.log")
)
val pinned = mapOf(
    "success-artifact.zip" to "b702abbe93502159afb1a1892acc360b9eaaf3f60cc7b4925ce86981a20943c0",
    "failure-artifact.zip" to "cffabaf5e037481d6e394dd9e1d93c7ee847978535d5036950f355237263ff9e",
    "owner-receipt.json" to "2aa9f324d08716d85b742b6f16ab25fafeed429171602301a0dc72f9ba59a85f",
    "public-linux-js-guard-539.yml" to "4b025b7b1037051e1152647172cbe9aabc1525df8ea447933ef380a81f9fc253",
    "run-public-linux-js-guard.main.kts" to "5f49fd114d7f75336973d62d8f4972d737aa45add36f39dd8cee2e4b245ee8e3",
    "independent-disposition.json" to "dfcd43bc2ed9ec3283bbf6ce1a79c526cb4b057c1092178732038ec6bef9f0e4",
    "independent-actual-audit.json" to "6e5d64d988e655e08c1818b6a7562ce5eb5197141590dbac00a44f20a8f513b5",
    "independent-source-binding.json" to "69b501f28e045ed6724259e2b46c35c5d01642b7f815d88a59778f4946bdf3de"
)
for ((name, source) in sourceFiles) {
    require(source.isFile && !java.nio.file.Files.isSymbolicLink(source.toPath()))
    pinned[name]?.let { require(sha(source) == it) { "Changed frozen input: $name" } }
    val target = File(output, name)
    source.copyTo(target)
    require(sha(target) == sha(source))
}
for (name in sourceFiles.keys.filter { it.endsWith(".zip") }) {
    val rows = mutableListOf<Map<String, Any>>()
    val seen = mutableSetOf<String>()
    ZipFile(File(output, name)).use { zip ->
        val members = zip.entries()
        while (members.hasMoreElements()) {
            val entry = members.nextElement()
            if (entry.isDirectory) { safe(entry.name.removeSuffix("/")); continue }
            val path = safe(entry.name); require(seen.add(path))
            val digest = MessageDigest.getInstance("SHA-256"); var bytes = 0L
            zip.getInputStream(entry).use { input ->
                val buffer = ByteArray(1024 * 1024)
                while (true) { val n = input.read(buffer); if (n < 0) break; bytes += n; digest.update(buffer, 0, n) }
            }
            require(bytes == entry.size)
            rows += mapOf("path" to path, "bytes" to bytes, "sha256" to digest.digest().joinToString("") { "%02x".format(it) })
        }
    }
    File(output, name.removeSuffix(".zip") + ".entries.json").writeText(json(mapOf("entries" to rows.sortedBy { it["path"] as String })) + "\n")
    println("ARCHIVE_MEMBERS $name ${rows.size}")
}
val entries = output.listFiles()!!.filter { it.isFile }.sortedBy { it.name }.map { row(it.name, it) }
val index = mapOf("task" to "local:539", "status" to "QUALIFIED_ACCEPT_CURRENT_RETAINED_POLICY_PUBLIC_LINUX_ADDENDUM", "production" to "3f0e1fb8f763b6518395535e222c18407112c9aa", "old_evidence_commit" to "0db660d5e30983eafbee60bca7a9480c86fc7fc4", "entries" to entries)
File(output, "evidence-index.json").writeText(json(index) + "\n")
File(output, "evidence.sha256").writeText(output.listFiles()!!.filter { it.isFile && it.name != "evidence.sha256" }.sortedBy { it.name }.joinToString("") { sha(it) + "  " + it.name + "\n" })
println("ADDITIVE_PACKAGE_CREATED files=${entries.size + 2} index_sha256=${sha(File(output, "evidence-index.json"))}")
