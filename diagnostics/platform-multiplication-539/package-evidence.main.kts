import java.io.File
import java.security.MessageDigest
import java.util.zip.ZipEntry
import java.util.zip.ZipOutputStream
import java.util.zip.Deflater

val root = File("/Users/yoda/dev/pet/krogu-math/.codex-work/539").canonicalFile
val worktree = File(root, "worktree")
val canonical = File(worktree, "diagnostics/platform-multiplication-539")
val retained = File(root, "retained-evidence")
val resume = args.toList() == listOf("--finish")
check("BUILD SUCCESSFUL" in File(root, "final-retention-full-regression-1.log").readText())
if (resume) check(canonical.isDirectory && retained.isDirectory)
else { check(!canonical.exists() && canonical.mkdirs()); check(!retained.exists() && retained.mkdirs()) }
val production = "3f0e1fb8f763b6518395535e222c18407112c9aa"
fun sha(file: File): String = file.inputStream().use { input ->
    val digest = MessageDigest.getInstance("SHA-256")
    val buffer = ByteArray(1024 * 1024)
    while (true) { val n = input.read(buffer); if (n < 0) break; digest.update(buffer, 0, n) }
    digest.digest().joinToString("") { "%02x".format(it) }
}
fun q(value: String): String = buildString {
    append('"')
    for (c in value) when (c) {
        '\\' -> append("\\\\")
        '"' -> append("\\\"")
        '\n' -> append("\\n")
        '\r' -> append("\\r")
        '\t' -> append("\\t")
        else -> { check(c.code >= 32); append(c) }
    }
    append('"')
}
fun json(value: Any?): String = when (value) {
    null -> "null"
    is String -> q(value)
    is Boolean, is Number -> value.toString()
    is Map<*, *> -> value.entries.joinToString(",", "{", "}") { q(it.key as String) + ":" + json(it.value) }
    is Iterable<*> -> value.joinToString(",", "[", "]") { json(it) }
    else -> error("Unsupported JSON value")
}
fun write(file: File, value: Any?) { file.writeText(json(value) + "\n") }
fun safe(name: String): String {
    check(name.isNotEmpty() && !name.startsWith("/") && '\\' !in name && name.split('/').none { it.isBlank() || it == "." || it == ".." })
    return name
}
fun fileRow(path: String, file: File) = linkedMapOf<String, Any>("path" to safe(path), "bytes" to file.length(), "sha256" to sha(file))
fun regularFiles(directory: File): List<File> = directory.walkTopDown().filter { it.isFile && !java.nio.file.Files.isSymbolicLink(it.toPath()) }.toList()
fun zip(name: String, files: List<Pair<String, File>>, destination: File = canonical) {
    check(files.map { it.first }.toSet().size == files.size)
    val entries = mutableListOf<Map<String, Any>>()
    val archive = File(destination, name + ".zip")
    if (resume && archive.isFile) {
        check(File(destination, name + ".entries.json").isFile)
        println("RETAIN_EXISTING_ARCHIVE " + archive.name)
        return
    }
    ZipOutputStream(archive.outputStream().buffered()).use { output ->
        output.setLevel(Deflater.BEST_SPEED)
        for ((path, file) in files.sortedBy { it.first }) {
            val entry = ZipEntry(safe(path)); entry.time = 0
            output.putNextEntry(entry)
            file.inputStream().use { it.copyTo(output) }
            output.closeEntry()
            entries += fileRow(path, file)
        }
    }
    write(File(destination, name + ".entries.json"), mapOf("entries" to entries))
    println("ARCHIVED " + archive.name + " members=" + entries.size + " bytes=" + archive.length())
}
val source = File(root, "final-native-retention-source.tsv")
val tested = source.readLines().map { line ->
    val fields = line.split('\t'); check(fields.size == 2)
    val file = File(fields[1]).canonicalFile
    check(file.toPath().startsWith(worktree.toPath()) && sha(file) == fields[0])
    fileRow(file.relativeTo(worktree).invariantSeparatorsPath, file)
}
check(tested.size == 405)
write(File(canonical, "tested-source-map.json"), mapOf("source_commit" to production, "entries" to tested, "files" to tested.associate { it["path"] as String to it["sha256"] }))
for (name in listOf("REPORT.md", "REQUIREMENTS.md", "REPRODUCE.md")) {
    val lines = File(root, "canonical-draft/" + name).readLines().map { line ->
        when {
            name == "REPORT.md" && line.startsWith("Settled production source") ->
                "Settled production source is 3f0e1fb8f763b6518395535e222c18407112c9aa, based on accepted parent c9415d55fe8d9a743266e75709cf95738011d311. Source manifest has 405 paths, SHA48c4b61794256a738e36af34fe8d1ccc32a897590fa86c7cf7813c24170e5e0a. Compared with aa9, 400 files have exact bytes; three Native-only files change and two common/JVM files have reviewed KDoc/comments only. Four additional documentation files have no executable effect. Final full3267 independently passed2112 tests: JVM702, JS468, Wasm468, Native474, zero failures/errors/skips. These are current actual counts, not historical sums."
            name == "REPORT.md" && line.startsWith("Final full four-target correctness3267") ->
                "Final full3267 completed in8m59s. Independent9b61ec84 verifies all530 XML files,2112 tests and405 source hashes. JVM supplement51596f4a verifies fresh final JAR e8ba87ac equals producer:106/114 byte-equal entries, exact method instruction listings in eight changed classes,34B public entry. Debug/metadata changes are not executable changes; whole-JAR identity is not claimed. Prior2108 suites remain historical. Lossless raw/provenance archives preserve failed/partial attempts, guard matrices and NO_NUMERIC_RUN. Task539 remains incomplete while JS small clearance is unresolved."
            name == "REQUIREMENTS.md" && line.startsWith("**Overall:") ->
                "**Overall: INCOMPLETE_JS_SMALL_UNRESOLVED.** This maps the literal task body and frozen protocol, not a new gate. Final full2112 correctness independently passed on settled source. Remote export/benchmark requires separate approval still pending."
            name == "REQUIREMENTS.md" && line.startsWith("| Correct synchronous fallback") ->
                "| Correct synchronous fallback / mathematical identity | Source-era school/JDK/NodeBigInt/polynomial witnesses, declined published KLIB consumers, Native11contracts and retained45/1260; final full3267 independently passed2112 tests with zero failures/errors/skips. Virtual subclass/sign/alias/input/range/failure assertions retained. | PASS current source and explicit historical witness scopes |"
            name == "REQUIREMENTS.md" && line.startsWith("| Preserve public contracts") ->
                "| Preserve public contracts / independence / other work | Exact-class and virtual fallback, physical alias/sign/range/input/failure/ownership tests; independent reviews; separate feature worktree. Final full2112 passed. | PASS contracts and full correctness |"
            name == "REPRODUCE.md" && line.startsWith("No --tests orKROGU profile.") ->
                "No --tests or KROGU profile. Invocation3267/log/XML archive bind2112 actual tests (702/468/468/474), zero failures/errors/skips. Native calibration Release is separate with -Pmultiplication539=true and :linkCalibration539ReleaseTestMacosArm64. Production JS/Wasm Sync is separate from development tests because an observed dependency failure showed that combining them was invalid."
            else -> line
        }
    }
    File(canonical, name).writeText(lines.joinToString("\n", postfix = "\n"))
}
File(root, "selected-policy-decisions.json").copyTo(File(canonical, "selected-policy-decisions.json"), overwrite = resume)
File(canonical, "README.md").writeText("# Platform multiplication539 evidence\n\nStatus: INCOMPLETE_JS_SMALL_UNRESOLVED. Current full2112 correctness and scoped production decisions do not close the six JS small flags. One local monitor admitted no numerical run; remote approval is pending and no export occurred. See REPORT.md, REQUIREMENTS.md and REPRODUCE.md. Preserve external retained-evidence and original measured eras.\n")

// Snapshot only actual evidence. Recreated Gradle project caches are not raw.
val excludedDirectories = setOf("worktree", "artifacts", "packaging", "canonical-draft", "retained-evidence", "remote-js-small-guard")
val allDirectories = root.listFiles()!!.filter { it.isDirectory }
val rawDirectories = allDirectories.filter { it.name !in excludedDirectories && !it.name.startsWith("consumer-project-") && !it.name.startsWith("consumer-publication-") }
zip("raw-campaigns", rawDirectories.flatMap { directory -> regularFiles(directory).map { it.relativeTo(root).invariantSeparatorsPath to it } })
val rootFiles = root.listFiles()!!.filter { it.isFile && it.name != "package-evidence-1.log" }
val packagingFiles = regularFiles(File(root, "packaging"))
val remoteMetadata = regularFiles(File(root, "remote-js-small-guard")).filter {
    val path = it.relativeTo(File(root, "remote-js-small-guard")).invariantSeparatorsPath
    !path.startsWith("payload/") && !path.endsWith(".tar") && !path.endsWith(".zip")
}
val consumerSources = allDirectories.filter { it.name.startsWith("consumer-project-") }.flatMap { directory ->
    regularFiles(directory).filter {
        val path = it.relativeTo(directory).invariantSeparatorsPath
        !path.startsWith("build/") && !path.startsWith(".gradle/") && !path.startsWith("kotlin-js-store/")
    }
}
zip("provenance-tools", (rootFiles + packagingFiles + remoteMetadata + consumerSources).distinctBy { it.canonicalPath }.map { it.relativeTo(root).invariantSeparatorsPath to it })
write(File(canonical, "raw-selection.json"), mapOf(
    "raw_directories" to rawDirectories.map { it.name }.sorted(),
    "root_regular_files" to rootFiles.map { it.name }.sorted(),
    "scope" to "All raw/partial/failed/intermediate/final campaigns, build logs, controllers, reviews and source-era bindings retained. Consumer project sources retained; disposable compiler/dependency caches excluded, published output files retained separately. Remote archive/payload remain untouched and unexported; their metadata is preserved."
))

// Preserve complete measured closures and isolated publications without duplicate blobs.
val artifactDirectories = listOf(File(root, "artifacts")) + allDirectories.filter { it.name.startsWith("consumer-publication-") }
val measuredFiles = artifactDirectories.flatMap(::regularFiles).sortedBy { it.path }
val blobs = linkedMapOf<String, File>()
val map = measuredFiles.map { file ->
    val h = sha(file)
    blobs.putIfAbsent(h, file)
    linkedMapOf<String, Any>("path" to safe(file.relativeTo(root).invariantSeparatorsPath), "blob" to "blobs/" + h, "bytes" to file.length(), "sha256" to h, "executable" to file.canExecute())
}
write(File(canonical, "file-map.json"), mapOf("entries" to map, "scope" to "Exact measured artifact/publication files by original task-relative path. Restore only into a fresh owned directory; never overwrite source. No rebuilt binary is relabelled as historical."))
zip("measured-artifacts", blobs.entries.map { "blobs/" + it.key to it.value }, retained)

// Source snapshot contains every tracked build/source/doc file except historical diagnostics.
val process = ProcessBuilder("git", "ls-tree", "-r", "--name-only", production).directory(worktree).start()
val tracked = process.inputStream.bufferedReader().use { it.readLines() }
check(process.waitFor() == 0)
val sourceFiles = tracked.filter { !it.startsWith("diagnostics/") }.map { safe(it) to File(worktree, it) }
zip("source-tree", sourceFiles)
val sourceHistory = File(retained, "source-history.bundle")
val bundle = ProcessBuilder("git", "bundle", "create", sourceHistory.path, "c9415d55fe8d9a743266e75709cf95738011d311..HEAD").directory(worktree).redirectErrorStream(true).start()
val bundleText = bundle.inputStream.bufferedReader().use { it.readText() }
check(bundle.waitFor() == 0) { bundleText }
File(canonical, "source-recovery.json").writeText(json(mapOf(
    "production" to production,
    "accepted_parent_prerequisite" to "c9415d55fe8d9a743266e75709cf95738011d311",
    "bundle" to "retained:source-history.bundle",
    "snapshot" to "source-tree.zip",
    "scope" to "Git bundle retains every feature commit after accepted parent; git bundle verify requires that parent in the existing repository. Independent source-tree snapshot supplies all tracked build/source/docs excluding historical diagnostics; tested-source-map binds405 exact files. Prior accepted diagnostics remain their separate packages."
)) + "\n")
File(root, "package-evidence.main.kts").copyTo(File(canonical, "package-evidence.main.kts"), overwrite = resume)
val retainedEntries = retained.listFiles()!!.filter { it.isFile }.sortedBy { it.name }.map { fileRow(it.name, it) }
write(File(retained, "evidence-index.json"), mapOf("task" to "local:539", "status" to "INCOMPLETE_JS_SMALL_UNRESOLVED", "entries" to retainedEntries))
File(retained, "evidence.sha256").writeText(retained.listFiles()!!.filter { it.isFile && it.name != "evidence.sha256" }.sortedBy { it.name }.joinToString("") { sha(it) + "  " + it.name + "\n" })
write(File(canonical, "retained-evidence.json"), mapOf(
    "task" to "local:539",
    "project_relative_root" to ".codex-work/539/retained-evidence",
    "entries" to retainedEntries,
    "index_sha256" to sha(File(retained, "evidence-index.json")),
    "sha_ledger_sha256" to sha(File(retained, "evidence.sha256")),
    "scope" to "Required local retained binary evidence and source history, not exported. Preserve after any worktree cleanup. Canonical file-map/member ledgers bind all bytes; fresh source checkout alone does not reproduce historical binaries."
))
write(File(canonical, "evidence-index.json"), mapOf(
    "task" to "local:539",
    "status" to "INCOMPLETE_JS_SMALL_UNRESOLVED",
    "source" to production,
    "entries" to canonical.listFiles()!!.filter { it.isFile && it.name !in setOf("evidence-index.json", "evidence.sha256") }.sortedBy { it.name }.map { fileRow(it.name, it) }
))
File(canonical, "evidence.sha256").writeText(canonical.listFiles()!!.filter { it.isFile && it.name != "evidence.sha256" }.sortedBy { it.name }.joinToString("") { sha(it) + "  " + it.name + "\n" })
println("PACKAGE_GENERATED_NOT_ACCEPTANCE " + canonical.path)
