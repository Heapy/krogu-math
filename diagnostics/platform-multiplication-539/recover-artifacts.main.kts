@file:DependsOn("org.jetbrains.kotlinx:kotlinx-serialization-json-jvm:1.11.0")
import java.io.File
import java.security.MessageDigest
import java.util.zip.ZipFile
import kotlinx.serialization.json.*

require(args.size == 4) { "canonicalDirectory projectRoot newOwnedOutput exactArtifactPath" }
val canonical = File(args[0]).canonicalFile
val project = File(args[1]).canonicalFile
val output = File(args[2]).canonicalFile
check(!output.exists() && output.mkdirs())
fun safe(name: String): String {
    check(name.isNotEmpty() && !name.startsWith("/") && '\\' !in name && name.split('/').none { it.isBlank() || it == "." || it == ".." })
    return name
}
val selected = safe(args[3])
val reference = Json.parseToJsonElement(File(canonical, "retained-evidence.json").readText()).jsonObject
val retained = File(project, safe(reference.getValue("project_relative_root").jsonPrimitive.content)).canonicalFile
check(retained.toPath().startsWith(project.toPath()))
val map = Json.parseToJsonElement(File(canonical, "file-map.json").readText()).jsonObject.getValue("entries").jsonArray
val row = map.single { it.jsonObject.getValue("path").jsonPrimitive.content == selected }.jsonObject
val hash = row.getValue("sha256").jsonPrimitive.content
val blob = safe(row.getValue("blob").jsonPrimitive.content)
check(hash.matches(Regex("[a-f0-9]{64}")) && blob == "blobs/" + hash)
val target = File(output, selected).canonicalFile
check(target.toPath().startsWith(output.toPath()) && target.parentFile.mkdirs())
val digest = MessageDigest.getInstance("SHA-256")
var bytes = 0L
ZipFile(File(retained, "measured-artifacts.zip")).use { zip ->
    val entry = checkNotNull(zip.getEntry(blob))
    zip.getInputStream(entry).use { input ->
        target.outputStream().use { sink ->
            val buffer = ByteArray(1024 * 1024)
            while (true) {
                val count = input.read(buffer); if (count < 0) break
                sink.write(buffer, 0, count); digest.update(buffer, 0, count); bytes += count
            }
        }
    }
}
check(bytes == row.getValue("bytes").jsonPrimitive.long)
check(digest.digest().joinToString("") { "%02x".format(it) } == hash)
if (row["executable"]?.jsonPrimitive?.booleanOrNull == true) check(target.setExecutable(true, true))
println(buildJsonObject {
    put("status", "RESTORED_BYTES_PASS_NOT_RUNTIME_EXECUTION")
    put("path", selected); put("bytes", bytes); put("sha256", hash)
    put("restored_path", target.path)
}.toString())
