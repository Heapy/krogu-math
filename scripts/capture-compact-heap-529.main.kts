#!/usr/bin/env kotlinr
// Capture owned small WasmGC/JS cohorts; no arithmetic timing runs in this process.
import java.io.File
import java.security.MessageDigest
import java.util.concurrent.TimeUnit
import java.util.zip.GZIPInputStream
import java.util.zip.GZIPOutputStream

require(args.size == 6) { "node-executable artifact-directory entry-relative SHA256-manifest NEW-output-directory baseline|candidate" }
check(KotlinVersion.CURRENT.toString() == "2.4.21")
val node = File(args[0]).canonicalFile
val artifact = File(args[1]).canonicalFile
val entry = File(artifact, args[2]).canonicalFile
val manifest = File(args[3]).canonicalFile
val output = File(args[4]).canonicalFile
val label = args[5]
check(node.isFile && artifact.isDirectory && entry.isFile && entry.toPath().startsWith(artifact.toPath()))
check(label in listOf("baseline", "candidate"))
val owned = File("..").canonicalFile
check(owned.name == "529" && output.toPath().startsWith(owned.toPath()) && !output.exists() && output.mkdirs())
val source = File("scripts/capture-compact-heap-529.main.kts").canonicalFile
check(source.isFile) { "Run from the task worktree root" }
fun sha(file: File) = MessageDigest.getInstance("SHA-256").digest(file.readBytes()).joinToString("") { "%02x".format(it) }
val sourceSha = sha(source)
val nodeSha = sha(node)
val manifestSha = sha(manifest)
val expected = manifest.readLines().filter(String::isNotBlank).associate { line ->
    check(line.length > 66 && line.take(64).matches(Regex("[0-9a-f]{64}")) && line.substring(64, 66) == "  ")
    line.substring(66) to line.take(64)
}
check(expected.isNotEmpty() && expected.size == manifest.readLines().count(String::isNotBlank) && args[2] in expected)
fun verify() {
    check(sha(source) == sourceSha && sha(node) == nodeSha && sha(manifest) == manifestSha)
    val files = artifact.walkTopDown().filter(File::isFile).map { it.relativeTo(artifact).invariantSeparatorsPath }.toSet()
    check(files == expected.keys) { "Artifact tree differs from frozen manifest" }
    expected.forEach { (relative, hash) ->
        val file = File(artifact, relative).canonicalFile
        check(file.toPath().startsWith(artifact.toPath()) && file.isFile && sha(file) == hash)
    }
}
verify()
val module = """
import fs from 'node:fs';
import path from 'node:path';
import v8 from 'node:v8';
import crypto from 'node:crypto';
import {pathToFileURL} from 'node:url';
const [entry,out,label] = process.argv.slice(1);
const phases=['unit','integer_small','integer_wide','decimal_small','decimal_wide','control_bytes0','control_bytes256','control_ints64'];
const summaries=[];
if(typeof global.gc!=='function')throw Error('explicit GC unavailable');
console.log('RUNTIME,'+process.version+','+process.versions.v8+','+process.arch+','+JSON.stringify(process.execArgv));
process.argv=[process.argv[0],entry,'compact529-heap','256'];
globalThis.__compact529Snapshot=(phase)=>{
  if(phase!==phases[summaries.length])throw Error('unexpected phase '+phase);
  global.gc();
  const file=path.join(out,label+'-'+phase+'.heapsnapshot');
  if(fs.existsSync(file))throw Error('snapshot already exists');
  v8.writeHeapSnapshot(file,{exposeInternals:true,exposeNumericValues:true});
  const bytes=fs.readFileSync(file);
  const data=JSON.parse(bytes.toString('utf8'));
  const meta=data.snapshot.meta;
  const width=meta.node_fields.length;
  const typeAt=meta.node_fields.indexOf('type'),nameAt=meta.node_fields.indexOf('name');
  const sizeAt=meta.node_fields.indexOf('self_size'),edgesAt=meta.node_fields.indexOf('edge_count');
  if([typeAt,nameAt,sizeAt,edgesAt].some(x=>x<0)||data.nodes.length%width!==0)throw Error('unsupported snapshot schema');
  const types={};
  for(let at=0;at<data.nodes.length;at+=width){
    const kind=meta.node_types[typeAt][data.nodes[at+typeAt]];
    const name=data.strings[data.nodes[at+nameAt]];
    const key=kind+'|'+name;
    const row=types[key]??(types[key]={count:0,shallowBytes:0,edges:0});
    row.count++;row.shallowBytes+=data.nodes[at+sizeAt];row.edges+=data.nodes[at+edgesAt];
  }
  const row={phase,file:path.basename(file),bytes:bytes.length,sha256:crypto.createHash('sha256').update(bytes).digest('hex'),nodeFields:meta.node_fields,nodeTypes:meta.node_types,edgeFields:meta.edge_fields,edgeTypes:meta.edge_types,types};
  const summaryFile=label+'-'+phase+'-summary.json';
  fs.writeFileSync(path.join(out,summaryFile),JSON.stringify(row,null,2)+'\n');
  // Do not retain the large census/schema dictionaries across phases.
  summaries.push({phase,file:row.file,bytes:row.bytes,sha256:row.sha256,summaryFile});
  console.log('SNAPSHOT,'+phase+','+row.bytes+','+row.sha256);
};
await import(pathToFileURL(entry).href);
if(summaries.length!==phases.length)throw Error('incomplete snapshot inventory');
fs.writeFileSync(path.join(out,'snapshot-summary.json'),JSON.stringify({status:'CAPTURED_NOT_YET_GRAPH_VALIDATED',label,node:process.version,v8:process.versions.v8,arch:process.arch,argv:process.execArgv,rootCount:256,phases:summaries},null,2)+'\n');
console.log('PASS capture529');
""".trimIndent()
File(output, "wrapper-source.txt").writeText(module)
val command = listOf(node.path, "--expose-gc", "--max-old-space-size=512", "--input-type=module", "-e", module, entry.path, output.path, label)
File(output, "argv.txt").writeText(command.mapIndexed { index, value -> "$index=$value" }.joinToString("\n", postfix = "\n"))
val builder = ProcessBuilder(command).redirectErrorStream(true).redirectOutput(File(output, "capture.log"))
// Whole-heap captures must not inherit ambient tokens/options/other secrets.
// Absolute executable/module paths need no inherited PATH or home directory.
builder.environment().clear()
builder.environment().putAll(mapOf("TZ" to "UTC", "LANG" to "C", "LC_ALL" to "C"))
val process = builder.start()
if (!process.waitFor(180, TimeUnit.SECONDS)) {
    process.destroy(); if (!process.waitFor(5, TimeUnit.SECONDS)) process.destroyForcibly()
    check(process.waitFor(5, TimeUnit.SECONDS)) { "Owned Node child survived cleanup: ${process.pid()}" }
    error("Owned snapshot child deadline; partial outputs retained")
}
check(process.exitValue() == 0 && File(output, "capture.log").readLines().lastOrNull() == "PASS capture529")
val snapshots = output.listFiles()!!.filter { it.extension == "heapsnapshot" }
check(snapshots.size == 8)
val ledger = StringBuilder("script=${source.path};sha256=$sourceSha\nnode=${node.path};sha256=$nodeSha\nartifact=${artifact.path}\nmanifest=${manifest.path};sha256=$manifestSha\n")
// Keep one lossless compressed copy of each generated snapshot; verify its raw
// bytes before disposing of the redundant original. The raw hash stays in both ledgers.
for (snapshot in snapshots.sortedBy(File::getName)) {
    val rawHash = sha(snapshot)
    val zipped = File(snapshot.path + ".gz")
    GZIPOutputStream(zipped.outputStream()).use { sink -> snapshot.inputStream().use { it.copyTo(sink) } }
    val restoredHash = GZIPInputStream(zipped.inputStream()).use { input ->
        val digest = MessageDigest.getInstance("SHA-256"); val buffer = ByteArray(65536)
        while (true) { val count = input.read(buffer); if (count < 0) break; digest.update(buffer, 0, count) }
        digest.digest().joinToString("") { "%02x".format(it) }
    }
    check(restoredHash == rawHash)
    ledger.appendLine("${snapshot.name} raw_sha256=$rawHash gzip_sha256=${sha(zipped)}")
    check(snapshot.delete())
}
verify()
File(output, "input-and-recovery-ledger.txt").writeText(ledger.toString())
println("CAPTURED ${output.path}; Wasm node/edge/root ownership and retained-size analysis still required")
