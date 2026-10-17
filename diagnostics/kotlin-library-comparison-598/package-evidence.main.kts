#!/usr/bin/env kotlinr
import java.io.File
import java.nio.file.Files
import java.security.MessageDigest
import java.util.zip.ZipEntry
import java.util.zip.ZipFile
import java.util.zip.ZipOutputStream

// Delivery tooling only. It never launches a compiler, peer, or benchmark.
require(args.size == 3) { "create|verify|restore TASK598_ROOT ARCHIVE.zip" }
val root = File(args[1]).canonicalFile
val archive = File(args[2]).canonicalFile
require(!archive.toPath().startsWith(root.toPath()))
fun sha(file: File): String = file.inputStream().use { input ->
    val md = MessageDigest.getInstance("SHA-256")
    val buffer = ByteArray(65536)
    while (true) { val n = input.read(buffer); if (n < 0) break; md.update(buffer, 0, n) }
    md.digest().joinToString("") { "%02x".format(it) }
}
fun json(value: String) = buildString {
    append('"')
    for (c in value) when (c) {
        '"' -> append("\\\""); '\\' -> append("\\\\")
        '\n' -> append("\\n"); '\r' -> append("\\r"); '\t' -> append("\\t")
        else -> if (c.code < 32) append("\\u%04x".format(c.code)) else append(c)
    }
    append('"')
}
fun safe(name: String) = name.isNotEmpty() && !name.startsWith('/') &&
    '\\' !in name && name.split('/').none { it in listOf("", ".", "..") }
val ledger = File(archive.path + ".entries.tsv")
if (args[0] == "create") {
    require(root.isDirectory && !archive.exists() && !ledger.exists())
    // Gradle's transient state is not an evidence input. Build outputs, historical
    // failures, compiler closures, raw rows, sources, and independent audits remain.
    val files = root.walkTopDown().onEnter { dir ->
        require(!Files.isSymbolicLink(dir.toPath())) { "Symlink directory: $dir" }
        val relative = dir.relativeTo(root).invariantSeparatorsPath
        !(relative.substringBefore('/').startsWith("prepared-") &&
            (dir.name in setOf(".gradle", ".kotlin") || (dir.name == "node_modules" && "/build/" in relative)))
    }.filter { it.isFile }.map { file ->
        require(!Files.isSymbolicLink(file.toPath())) { "Symlink file: $file" }
        val name = file.relativeTo(root).invariantSeparatorsPath
        require(safe(name)); name to file
    }.sortedBy { it.first }.toList()
    require(files.isNotEmpty() && files.map { it.first }.distinct().size == files.size)
    check(archive.parentFile.mkdirs() || archive.parentFile.isDirectory)
    val rows = files.map { (name, file) ->
        listOf(sha(file), file.length().toString(), if (file.canExecute()) "x" else "-", name)
    }
    ZipOutputStream(archive.outputStream().buffered()).use { zip ->
        for ((pair, row) in files.zip(rows)) {
            val (name, file) = pair
            check(sha(file) == row[0] && file.length().toString() == row[1])
            zip.putNextEntry(ZipEntry(name).apply { time = 0 })
            file.inputStream().use { it.copyTo(zip) }; zip.closeEntry()
        }
    }
    ledger.writeText(rows.joinToString("\n", postfix = "\n") { it.joinToString("\t") })
    File(archive.path + ".entries.json").writeText("{\"entries\":[" + rows.joinToString(",") {
        "{\"path\":" + json(it[3]) + ",\"bytes\":" + it[1] + ",\"sha256\":" + json(it[0]) +
            ",\"executable\":" + (it[2] == "x") + "}"
    } + "]}\n")
} else require(args[0] in setOf("verify", "restore"))
val expected = ledger.readLines().associate { line ->
    val fields = line.split('\t'); require(fields.size == 4 && safe(fields[3]))
    require(fields[0].matches(Regex("[a-f0-9]{64}")) && fields[1].toLong() >= 0)
    require(fields[2] in setOf("x", "-"))
    fields[3] to fields
}
require(expected.size == ledger.readLines().size)
ZipFile(archive).use { zip ->
    val entries = zip.entries().asSequence().toList()
    require(entries.none { it.isDirectory } && entries.all { safe(it.name) })
    require(entries.map { it.name }.distinct().size == entries.size && entries.map { it.name }.toSet() == expected.keys)
    for (entry in entries) {
        val row = expected.getValue(entry.name); val md = MessageDigest.getInstance("SHA-256")
        var size = 0L
        zip.getInputStream(entry).use { input ->
            val buffer = ByteArray(65536)
            while (true) { val n = input.read(buffer); if (n < 0) break; size = Math.addExact(size, n.toLong()); md.update(buffer, 0, n) }
        }
        check(size == row[1].toLong() && md.digest().joinToString("") { "%02x".format(it) } == row[0]) { entry.name }
    }
}
if (args[0] == "restore") {
    require(!root.exists()) { "Recovery destination must be new" }
    check(root.mkdirs())
    ZipFile(archive).use { zip ->
        for ((name, row) in expected) {
            val file = File(root, name)
            check(file.canonicalFile.toPath().startsWith(root.toPath()) && !file.exists())
            check(file.parentFile.mkdirs() || file.parentFile.isDirectory)
            zip.getInputStream(zip.getEntry(name)).use { input -> file.outputStream().use { input.copyTo(it) } }
            if (row[2] == "x") check(file.setExecutable(true, true))
            check(file.length() == row[1].toLong() && sha(file) == row[0]) { "Restoration mismatch: $name" }
        }
    }
    check(root.walkTopDown().filter { it.isFile }.count() == expected.size)
    println("RESTORED598_FILES ${expected.size} root=$root")
}
println("VERIFIED598_ARCHIVE files=${expected.size} bytes=${archive.length()} sha256=${sha(archive)} ledger_sha256=${sha(ledger)}")
