import java.io.File
import java.security.MessageDigest
import java.util.zip.ZipFile
val root = File("/Users/yoda/dev/pet/krogu-math/.codex-work/539")
val canonical = File(root, "worktree/diagnostics/platform-multiplication-539")
check(File(canonical, "evidence-index.json").isFile)
fun sha(file: File) = file.inputStream().use { input ->
    val digest = MessageDigest.getInstance("SHA-256"); val buffer = ByteArray(1024 * 1024)
    while (true) { val n = input.read(buffer); if (n < 0) break; digest.update(buffer, 0, n) }
    digest.digest().joinToString("") { "%02x".format(it) }
}
fun q(s: String) = "\"" + s.replace("\\", "\\\\").replace("\"", "\\\"").replace("\n", "\\n") + "\""
fun json(v: Any?): String = when(v) {
    null -> "null"
    is String -> q(v)
    is Number, is Boolean -> v.toString()
    is Iterable<*> -> v.joinToString(",", "[", "]") { json(it) }
    is Map<*, *> -> v.entries.joinToString(",", "{", "}") { q(it.key as String) + ":" + json(it.value) }
    else -> error("Unsupported")
}
fun names(name: String) = ZipFile(File(canonical, name)).use { it.entries().asSequence().filter { !it.isDirectory }.map { it.name }.toList() }
val provenance = names("provenance-tools.zip")
val raw = names("raw-campaigns.zip")
val groups = ZipFile(File(canonical, "raw-campaigns.zip")).use { zip ->
    zip.entries().asSequence().filter { !it.isDirectory }.toList().groupBy { it.name.substringBefore('/') }.toSortedMap().map { (prefix, members) ->
        mapOf("prefix" to prefix + "/", "files" to members.size, "uncompressed_bytes" to members.sumOf { it.size })
    }
}
File(canonical, "raw-index.json").writeText(json(mapOf(
    "archive" to "raw-campaigns.zip",
    "member_ledger" to "raw-campaigns.entries.json",
    "groups" to groups,
    "provenance_archive" to "provenance-tools.zip",
    "provenance_member_ledger" to "provenance-tools.entries.json",
    "source_eras" to "source-era-ledger.json",
    "scope" to "Actual archive directory/member index, not merged measurements. Every source-specific summary, status/failure/partial inventory and independent reproduction remains in its original archive member. No absent numerical rows invented for NO_NUMERIC_RUN."
)) + "\n")
File(canonical, "raw-selection.json").writeText(json(mapOf(
    "raw_directories" to raw.map { it.substringBefore('/') }.distinct().sorted(),
    "root_regular_files" to provenance.filter { '/' !in it }.sorted(),
    "raw_archive_members" to raw.size,
    "provenance_archive_members" to provenance.size,
    "scope" to "Inventory derived from actual completed archives, including failed/partial/historical/final rows. Compiler/project caches and remote payload/archive are excluded explicitly; independent publication bytes are retained in measured-artifacts. Failed generation75914 and finish34723 are separate packaging metadata, not numerical failures."
)) + "\n")
for ((source, name) in listOf(
    File(root, "recover-artifacts.main.kts") to "recover-artifacts.main.kts",
    File(root, "packaging/verify-package.main.kts") to "verify-package.main.kts",
    File(root, "package-evidence-1.log") to "package-generation-failure.log",
    File(root, "package-evidence-finish-1.log") to "package-generation-finish.log",
    File(root, "finalize-package-metadata.main.kts") to "finalize-package-metadata.main.kts"
)) source.copyTo(File(canonical, name), overwrite = true)
for ((sourceName, canonicalName) in listOf(
    "packaging/package-binding-verification-1.log" to "precommit-package-binding-proof.json",
    "packaging/artifact-recovery-native-1.log" to "native-artifact-restoration-proof.json",
    "packaging/source-recovery-verification-1.log" to "restored-source-binding-proof.json",
    "independent-canonical-final-report-literal-scope-review.json" to "independent-report-scope-review.json",
    "independent-canonical-three-status-correction-supplement.json" to "independent-status-correction-review.json"
)) {
    val file = File(root, sourceName)
    if (file.isFile) file.copyTo(File(canonical, canonicalName), overwrite = true)
}
val reproduce = File(canonical, "REPRODUCE.md")
val current = reproduce.readText().substringBefore("\n## Exact package commands")
reproduce.writeText((current + """

## Exact package commands

Canonical tree: diagnostics/platform-multiplication-539. Required local retained root: .codex-work/539/retained-evidence. The source history bundle requires accepted parent c9415d55; source-tree.zip provides an independent build/source byte snapshot. Git bundle recovery preserves tracked modes; when using the ZIP snapshot directly, restore the Gradle wrapper owner executable bit with chmod u+x recovered/gradlew before invoking it. Host JDK/Node/compiler installations are external prerequisites pinned by their original runtime ledgers, not an archived operating system. Hashes and member counts come from actual archives, never hardcoded anticipated totals.

Run the packaged verify-package.main.kts with the canonical directory, --project-root PROJECT, --tested-source CANONICAL/tested-source-map.json --source-root WORKTREE --recovery-map file-map.json=retained:measured-artifacts.zip. After committing the package, add --repo WORKTREE --git-ref FULL_COMMIT for exact canonical and405 tested-source Git blobs. Output is PACKAGE_BINDINGS_PASS_NOT_TASK_ACCEPTANCE; JS small clearance remains unresolved.

The packaged recover-artifacts.main.kts takes canonicalDirectory projectRoot newOwnedOutput exactArtifactPath. It restores only a selected original artifact path from hash-addressed bytes into a fresh owned output, verifies size/SHA and restores its recorded executable bit. It never runs restored binaries. Full map/member validation verifies every recoverable file, not just a representative extraction.

Generation75914 completed all four ZIP archives but failed creating a Git bundle from an unnamed SHA range. Correction34723 used the named HEAD ref and retained the original ZIPs without re-compression. Both logs are preserved. This is a packaging failure/correction, not a numerical rerun or erased evidence.
""").trimEnd() + "\n")
val requirements = File(canonical, "REQUIREMENTS.md")
requirements.writeText(requirements.readText()
    .replace("PASS scoped; package verification pending", "PASS scoped measurements; exact package bindings verified separately")
    .replace("All epochs/failures/partiallogs/checkerreproductions will be in losslessraw+provenance archives", "All epochs/failures/partial logs/checker reproductions are in lossless raw and provenance archives")
    .replace("PACKAGE IN PROGRESS", "See exact member/index/source/recovery/Git-blob verification; not task acceptance"))
val entries = canonical.listFiles()!!.filter { it.isFile && it.name !in setOf("evidence-index.json", "evidence.sha256") }.sortedBy { it.name }.map {
    linkedMapOf<String, Any>("path" to it.name, "bytes" to it.length(), "sha256" to sha(it))
}
File(canonical, "evidence-index.json").writeText(json(mapOf("task" to "local:539", "status" to "INCOMPLETE_JS_SMALL_UNRESOLVED", "source" to "3f0e1fb8f763b6518395535e222c18407112c9aa", "entries" to entries)) + "\n")
File(canonical, "evidence.sha256").writeText(canonical.listFiles()!!.filter { it.isFile && it.name != "evidence.sha256" }.sortedBy { it.name }.joinToString("") { sha(it) + "  " + it.name + "\n" })
println("METADATA_FINALIZED_NOT_ACCEPTANCE files=" + entries.size)
