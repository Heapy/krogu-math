#!/usr/bin/env kotlinr
@file:DependsOn("com.google.code.gson:gson:2.14.0")

/**
 * Assemble a consumable local Maven repository from KTC 0.13's publication.
 * Preserve the existing wasm-js coordinate and include task-generated sources.
 * Runtime artifacts are copied unchanged. This never publishes to a server.
 */
import com.google.gson.*
import java.io.File
import java.nio.file.Files
import java.nio.file.StandardCopyOption
import java.security.MessageDigest
import java.util.concurrent.TimeUnit
import java.util.zip.*
import javax.xml.XMLConstants
import javax.xml.parsers.DocumentBuilderFactory

require(args.size == 1 || (args.size == 3 && args[1] == "--from-raw")) {
    "Usage: kotlinr tools/publish-local.main.kts NEW_REPOSITORY [--from-raw RAW_REPOSITORY]"
}
val root = File(".").canonicalFile
check(File(root, "module.yaml").isFile) { "Run from the repository root" }
val destination = File(args[0]).canonicalFile
check(!destination.exists()) { "Choose a new repository directory; existing repositories are never overwritten" }
val build = File(root, "build").apply { mkdirs() }
val raw = if (args.size == 3) File(args[2]).canonicalFile else
    Files.createTempDirectory(build.toPath(), "raw-maven-").toFile()
val windows = System.getProperty("os.name").startsWith("Windows")
fun run(command: List<String>, environment: Map<String, String> = emptyMap()) {
    val process = ProcessBuilder(command).directory(root).inheritIO().apply {
        environment().putAll(environment)
    }.start()
    try {
        check(process.waitFor(30, TimeUnit.MINUTES)) { "Publication build timed out" }
        check(process.exitValue() == 0) { "Publication build failed: " + process.exitValue() }
    } finally {
        if (process.isAlive) {
            val children = runCatching { process.descendants().toList().asReversed() }.getOrDefault(emptyList())
            children.forEach { it.destroy() }; process.destroy()
            if (!process.waitFor(5, TimeUnit.SECONDS)) {
                children.forEach { if (it.isAlive) it.destroyForcibly() }
                process.destroyForcibly(); process.waitFor(5, TimeUnit.SECONDS)
            }
        }
    }
}
if (args.size == 1) {
    check(File(root, ".native-test-friends.module-template.yaml").isFile) {
        "Run kotlinr tools/configure-native-tests.main.kts first"
    }
    val option = "-Dmaven.repo.local=" + raw.path
    require(option.none { it == '\n' || it == '\r' })
    val quoted = "\"" + (if (windows) option else option.replace("\\", "\\\\"))
        .replace("\"", "\\\"") + "\""
    val wrapper = File(root, if (windows) "kotlin.bat" else "kotlin").path
    run((if (windows) listOf("cmd.exe", "/c", wrapper) else listOf(wrapper)) +
        listOf("publish", "-m", "krogu-math", "mavenLocal"),
        mapOf("KOTLIN_CLI_JAVA_OPTIONS" to
            (System.getenv("KOTLIN_CLI_JAVA_OPTIONS").orEmpty() + " " + quoted)))
}
val gson = GsonBuilder().setPrettyPrinting().disableHtmlEscaping().create()
fun digest(file: File, algorithm: String = "SHA-256"): String = file.inputStream().use { input ->
    val hash = MessageDigest.getInstance(algorithm)
    val buffer = ByteArray(65536)
    while (true) { val count = input.read(buffer); if (count < 0) break; hash.update(buffer, 0, count) }
    hash.digest().joinToString("") { "%02x".format(it) }
}
fun files(directory: File): List<File> = directory.walkTopDown().onEnter {
    check(!Files.isSymbolicLink(it.toPath())); true
}.filter { it.isFile }.onEach { check(!Files.isSymbolicLink(it.toPath())) }
    .sortedBy { it.relativeTo(directory).invariantSeparatorsPath }.toList()
val oldWasm = "krogu-math-wasmjs"
val newWasm = "krogu-math-wasm-js"
fun rename(text: String) = text.replace(oldWasm, newWasm)
val groupPath = "io/heapy/krogu"
val group = File(raw, groupPath)
val suffixes = listOf("", "-jvm", "-js", "-wasmjs", "-macosarm64", "-macosx64",
    "-linuxx64", "-linuxarm64", "-mingwx64", "-iosarm64", "-iossimulatorarm64", "-iosx64")
check(group.listFiles()?.filter { it.isDirectory }?.map { it.name }?.toSet() ==
    suffixes.map { "krogu-math$it" }.toSet()) { "Expected root plus all eleven target modules" }
val versions = suffixes.map { suffix ->
    File(group, "krogu-math$suffix").listFiles()!!.filter { it.isDirectory }.single().name
}.toSet()
check(versions.size == 1)
val version = versions.single()
val rawFiles = files(group)
check(rawFiles.none { it.path.contains("nodeWorker539") || it.path.contains("worker-payload") })
destination.parentFile.mkdirs()
val stage = Files.createTempDirectory(destination.parentFile.toPath(), ".krogu-publication-").toFile()
val changes = mutableListOf<Map<String, Any>>()
for (source in rawFiles) {
    if (source.name == "_remote.repositories") continue // Resolver-local state, not a Maven artifact.
    val relative = groupPath + "/" + rename(source.relativeTo(group).invariantSeparatorsPath)
    val target = File(stage, relative).apply { parentFile.mkdirs() }
    if (source.extension in setOf("pom", "xml", "module"))
        target.writeText(rename(source.readText()))
    else source.copyTo(target)
    changes += mapOf("rawPath" to source.relativeTo(raw).invariantSeparatorsPath,
        "path" to relative, "rawSha256" to digest(source))
}
val getters = listOf("js" to "javascript", "wasmJs" to "wasm").map { (platform, task) ->
    val source = File(root, "build/tasks/_krogu-math_" + task +
        "@worker-payload/generated/io/heapy/krogu/math/EmbeddedNodeWorker539.kt")
    check(source.isFile && source.readText().contains("internal fun embeddedNodeWorker539()"))
    val artifact = if (platform == "js") "krogu-math-js" else newWasm
    val jar = File(stage, "$groupPath/$artifact/$version/$artifact-$version-sources.jar")
    val entries = ZipFile(jar).use { zip ->
        val members = zip.entries().asSequence().toList()
        check(members.map { it.name }.distinct().size == members.size) { "Duplicate source-JAR member" }
        members.associate { entry ->
            check(!entry.name.startsWith("/") && '\\' !in entry.name &&
                entry.name.split('/').none { it == ".." })
            entry.name to zip.getInputStream(entry).readBytes()
        }.toMutableMap()
    }
    check(entries.keys.none { it.endsWith("/EmbeddedNodeWorker539.kt") })
    val name = platform + "Main/io/heapy/krogu/math/EmbeddedNodeWorker539.kt"
    entries[name] = source.readBytes()
    ZipOutputStream(jar.outputStream()).use { zip ->
        for ((path, bytes) in entries.toSortedMap()) {
            zip.putNextEntry(ZipEntry(path).apply { time = 0 })
            zip.write(bytes); zip.closeEntry()
        }
    }
    mapOf("platform" to platform, "generatedSource" to source.relativeTo(root).invariantSeparatorsPath,
        "sha256" to digest(source), "sourceJar" to jar.relativeTo(stage).invariantSeparatorsPath, "entry" to name)
}
fun inside(base: File, relative: String): File {
    check(!File(relative).isAbsolute)
    val file = File(base, relative).canonicalFile
    check(file.toPath().startsWith(stage.toPath())) { "Metadata points outside the repository: $relative" }
    check(file.isFile) { "Missing metadata member: $file" }
    return file
}
for (module in files(stage).filter { it.extension == "module" }) {
    val json = JsonParser.parseString(module.readText()).asJsonObject
    check(json.getAsJsonObject("createdBy").getAsJsonObject("kotlinToolchain").get("version").asString == "0.13.0")
    val component = json.getAsJsonObject("component")
    if (component.has("url")) {
        val owner = inside(module.parentFile, component.get("url").asString)
        val declared = JsonParser.parseString(owner.readText()).asJsonObject.getAsJsonObject("component")
        for (key in listOf("group", "module", "version"))
            check(component.get(key).asString == declared.get(key).asString)
        check(owner.parentFile.parentFile.name == component.get("module").asString)
    } else check(component.get("module").asString == module.parentFile.parentFile.name)
    for (variant in json.getAsJsonArray("variants")) {
        val value = variant.asJsonObject
        value.getAsJsonObject("available-at")?.let { redirect ->
            val leaf = inside(module.parentFile, redirect.get("url").asString)
            check(leaf.parentFile.parentFile.name == redirect.get("module").asString)
        }
        value.getAsJsonArray("files")?.forEach { member ->
            val metadata = member.asJsonObject
            val file = inside(module.parentFile, metadata.get("url").asString)
            metadata.addProperty("size", file.length())
            for ((key, algorithm) in listOf("md5" to "MD5", "sha1" to "SHA-1",
                "sha256" to "SHA-256", "sha512" to "SHA-512"))
                metadata.addProperty(key, digest(file, algorithm))
        }
    }
    module.writeText(gson.toJson(json) + "\n")
}
// Keep XML semantics and snapshot metadata valid after the coordinate rename.
val factory = DocumentBuilderFactory.newInstance().apply {
    setFeature("http://apache.org/xml/features/disallow-doctype-decl", true)
    setAttribute(XMLConstants.ACCESS_EXTERNAL_DTD, "")
    setAttribute(XMLConstants.ACCESS_EXTERNAL_SCHEMA, "")
}
for (file in files(stage).filter { it.extension in setOf("pom", "xml") }) {
    val document = factory.newDocumentBuilder().parse(file)
    check(document.getElementsByTagName("groupId").item(0).textContent == "io.heapy.krogu")
    val expectedArtifact = if (file.parentFile.name == version) file.parentFile.parentFile.name else file.parentFile.name
    check(document.getElementsByTagName("artifactId").item(0).textContent == expectedArtifact)
}
// All compiler runtime artifacts must retain their exact original bytes.
for (mapping in changes) {
    val target = File(stage, mapping.getValue("path") as String)
    if (target.extension == "klib" || (target.extension == "jar" && !target.name.endsWith("-sources.jar")))
        check(digest(target) == mapping.getValue("rawSha256"))
}
check(files(stage).none { it.name.contains(oldWasm) })
for (file in files(stage)) {
    for ((extension, algorithm) in listOf("md5" to "MD5", "sha1" to "SHA-1",
        "sha256" to "SHA-256", "sha512" to "SHA-512"))
        File(file.path + "." + extension).writeText(digest(file, algorithm) + "\n")
}
val inventory = files(stage).map { file ->
    mapOf("path" to file.relativeTo(stage).invariantSeparatorsPath, "bytes" to file.length(), "sha256" to digest(file))
}
File(stage, "publication-ledger.json").writeText(gson.toJson(mapOf(
    "producer" to "Kotlin Toolchain 0.13.0", "scope" to "local publication compatibility assembly; no external release",
    "group" to "io.heapy.krogu", "version" to version, "modules" to suffixes.map { rename("krogu-math$it") },
    "rawRepository" to raw.path, "scriptSha256" to digest(File(root, "tools/publish-local.main.kts")),
    "generatedSources" to getters, "rawMappings" to changes, "entries" to inventory)) + "\n")
Files.move(stage.toPath(), destination.toPath(), StandardCopyOption.ATOMIC_MOVE)
println("LOCAL_PUBLICATION_COMPLETE repository=$destination modules=12 targets=11 version=$version")
