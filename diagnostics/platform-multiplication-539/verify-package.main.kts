#!/usr/bin/env kotlinr
// Read-only metadata/evidence verifier. Run only in an owner-agreed I/O window.
@file:DependsOn("org.jetbrains.kotlinx:kotlinx-serialization-json-jvm:1.11.0")

import java.nio.file.Files
import java.nio.file.Path
import java.security.MessageDigest
import java.io.InputStream
import java.util.zip.ZipFile
import kotlinx.serialization.json.*

val task539 = "local:539"
val digestPattern = Regex("[a-f0-9]{64}")
fun safeName(name:String):String {
    require(name.isNotEmpty() && !name.startsWith('/') && '\\' !in name && name.none { it.code<32 || it.code==127 }) { "Unsafe name: $name" }
    require(name.split('/').all { it.isNotEmpty() && it!="." && it!=".." }) { "Noncanonical name: $name" }
    return name
}
fun regular(root:Path,name:String):Path {
    val relative=safeName(name)
    var current=root
    for(part in relative.split('/')) { current=current.resolve(part); require(!Files.isSymbolicLink(current)) { "Symlink: $current" } }
    require(Files.isRegularFile(current) && current.toRealPath().startsWith(root)) { "Missing/escaping regular file: $name" }
    return current
}
data class Bytes539(val bytes:Long,val sha256:String)
fun hashStream(stream:InputStream):Bytes539 {
    val digest=MessageDigest.getInstance("SHA-256");val buffer=ByteArray(1024*1024);var bytes=0L
    while(true) { val n=stream.read(buffer);if(n<0)break;digest.update(buffer,0,n);bytes=Math.addExact(bytes,n.toLong()) }
    return Bytes539(bytes,digest.digest().joinToString("") { "%02x".format(it) })
}
fun hashFile(file:Path)=Files.newInputStream(file).use(::hashStream)
fun document(path:Path)=Json.parseToJsonElement(Files.readString(path)).jsonObject
fun text(o:JsonObject,key:String):String {
    val v=o[key]?.jsonPrimitive?:error("Missing $key");require(v.isString);return v.content
}
fun item(o:JsonObject):Pair<String,Bytes539> {
    val name=safeName(text(o,"path"));val size=o["bytes"]?.jsonPrimitive?:error("Missing bytes")
    require(!size.isString);val n=size.long;val sha=text(o,"sha256")
    require(n>=0 && digestPattern.matches(sha));return name to Bytes539(n,sha)
}
fun entries(o:JsonObject):Map<String,Bytes539> {
    val result=linkedMapOf<String,Bytes539>()
    for(row in o["entries"]?.jsonArray?:error("Expected entries array")) {
        val (name,bytes)=item(row.jsonObject);require(result.put(name,bytes)==null) { "Duplicate entry: $name" }
    }
    return result
}
fun inventory(root:Path):Set<String> = Files.walk(root).use { paths ->
    paths.filter { it!=root }.map { p ->
        require(!Files.isSymbolicLink(p)) { "Symlink: $p" }
        require(Files.isDirectory(p)||Files.isRegularFile(p)) { "Nonregular object: $p" }
        if(Files.isDirectory(p)) null else safeName(root.relativize(p).toString().replace(java.io.File.separatorChar,'/'))
    }.filter { it!=null }.map { it!! }.toList().toSet()
}
data class Package539(val root:Path,val index:JsonObject,val indexed:Map<String,Bytes539>,val observed:Map<String,Bytes539>,val archives:Map<String,Map<String,Bytes539>>)
fun verifyDirectory(root:Path,archiveOverrides:Map<String,String> = emptyMap()):Package539 {
    val indexName="evidence-index.json";val shaName="evidence.sha256"
    val indexFile=regular(root,indexName);val shaFile=regular(root,shaName);val index=document(indexFile)
    require(text(index,"task")==task539) { "Wrong task" }
    val indexed=entries(index);require(indexName !in indexed && shaName !in indexed) { "Self-hash recursion" }
    require(inventory(root)==indexed.keys+setOf(indexName,shaName)) { "Exact package inventory mismatch" }
    val observed=linkedMapOf<String,Bytes539>()
    for((name,expected) in indexed) { val got=hashFile(regular(root,name));require(got==expected) { "Index bytes/hash: $name" };observed[name]=got }
    val ledger=linkedMapOf<String,String>()
    for(line in Files.readAllLines(shaFile)) {
        val match=Regex("([a-f0-9]{64})  (.+)").matchEntire(line)?:error("Malformed SHA ledger")
        val name=safeName(match.groupValues[2]);require(ledger.put(name,match.groupValues[1])==null) { "Duplicate SHA entry" }
    }
    require(ledger.keys==indexed.keys+indexName) { "Exact SHA ledger inventory mismatch" }
    observed[indexName]=hashFile(indexFile)
    for((name,expected) in ledger) require(observed.getValue(name).sha256==expected) { "SHA ledger mismatch: $name" }
    observed[shaName]=hashFile(shaFile)
    val archives=linkedMapOf<String,Map<String,Bytes539>>()
    val zipNames=indexed.keys.filter { it.endsWith(".zip") }.toSet()
    require(archiveOverrides.keys.all { it in zipNames }) { "Archive override not indexed" }
    for(name in zipNames) {
        val memberName=archiveOverrides[name]?:name.removeSuffix(".zip")+".entries.json"
        require(memberName in indexed) { "Missing indexed ZIP member ledger: $name" }
        val members=entries(document(regular(root,memberName)));val seen=mutableSetOf<String>();val names=mutableSetOf<String>()
        ZipFile(regular(root,name).toFile()).use { zip ->
            val iterator=zip.entries()
            while(iterator.hasMoreElements()) {
                val entry=iterator.nextElement();val member=if(entry.isDirectory) safeName(entry.name.removeSuffix("/")) else safeName(entry.name)
                require(names.add(member)) { "Duplicate ZIP file/directory: $member" }
                if(entry.isDirectory) continue
                require(seen.add(member) && member in members) { "Unindexed ZIP member: $member" }
                require(entry.size==members.getValue(member).bytes) { "ZIP size: $member" }
                // ZIP links are additionally rejected through central-directory Unix mode inspection below.
                zip.getInputStream(entry).use { require(hashStream(it)==members.getValue(member)) { "ZIP member bytes: $member" } }
            }
        }
        require(seen==members.keys) { "Missing ZIP members: $name" }
        for(member in seen) { val parts=member.split('/');for(end in 1 until parts.size)require(parts.take(end).joinToString("/") !in seen) { "ZIP file-as-parent collision: $member" } }
        rejectZipLinks539(regular(root,name))
        archives[name]=members
    }
    return Package539(root,index,indexed,observed,archives)
}
// java.util.zip does not expose Unix modes. Read only the central-directory headers,
// then reject symlink/special mode bits; never extract archive contents.
fun rejectZipLinks539(file:Path) {
    java.io.RandomAccessFile(file.toFile(),"r").use { raf ->
        val length=raf.length();require(length>=22)
        val tailLength=minOf(length,65557L).toInt();val tail=ByteArray(tailLength);raf.seek(length-tailLength);raf.readFully(tail)
        fun u16(b:ByteArray,i:Int)=(b[i].toInt() and 255) or ((b[i+1].toInt() and 255) shl 8)
        fun u32(b:ByteArray,i:Int)=u16(b,i).toLong() or (u16(b,i+2).toLong() shl 16)
        fun u64(b:ByteArray,i:Int):Long { val high=u32(b,i+4);require(high<=0x7fffffffL);return u32(b,i) or (high shl 32) }
        var end=-1
        for(i in tail.size-22 downTo 0)if(u32(tail,i)==0x06054b50L && i+22+u16(tail,i+20)==tail.size) { end=i;break }
        require(end>=0) { "Missing ZIP directory" }
        require(u16(tail,end+4)==0 && u16(tail,end+6)==0) { "Multipart ZIP unsupported" }
        val endOffset=length-tailLength+end
        var count=u16(tail,end+10).toLong();var size=u32(tail,end+12);var offset=u32(tail,end+16)
        if(count==65535L || size==0xffffffffL || offset==0xffffffffL) {
            require(endOffset>=20);raf.seek(endOffset-20);val locator=ByteArray(20);raf.readFully(locator)
            require(u32(locator,0)==0x07064b50L && u32(locator,4)==0L && u32(locator,16)==1L) { "Invalid/multipart ZIP64" }
            val recordOffset=u64(locator,8);require(recordOffset<=endOffset-76);raf.seek(recordOffset)
            val record=ByteArray(56);raf.readFully(record)
            require(u32(record,0)==0x06064b50L && u64(record,4)>=44 && u64(record,4)<=endOffset-20-recordOffset-12 && u32(record,16)==0L && u32(record,20)==0L)
            count=u64(record,32);require(u64(record,24)==count);size=u64(record,40);offset=u64(record,48)
            require(offset<=recordOffset && size<=recordOffset-offset)
        } else require(u16(tail,end+8).toLong()==count)
        require(offset<=endOffset && size<=endOffset-offset && count<=size/46)
        raf.seek(offset)
        var memberIndex=0L
        while(memberIndex<count) {
            val header=ByteArray(46);raf.readFully(header);require(u32(header,0)==0x02014b50L)
            require(u16(header,8) and 1 == 0) { "Encrypted ZIP unsupported" }
            val mode=(u32(header,38) ushr 16).toInt();val type=mode  and 0xf000
            require(type==0 || type==0x8000 || type==0x4000) { "ZIP symlink/special member rejected" }
            val skip=u16(header,28)+u16(header,30)+u16(header,32);require(raf.filePointer+skip<=offset+size);raf.seek(raf.filePointer+skip);memberIndex++
        }
        require(raf.filePointer==offset+size) { "Central-directory extent mismatch" }
    }
}

require(args.isNotEmpty()) { "package [--project-root ROOT] [--repo ROOT --git-ref FULL_SHA] [--tested-source JSON --source-root ROOT] [--archive ZIP=LEDGER] [--recovery-map MAP=ZIP|retained:ZIP]" }
val root=Path.of(args[0]).toRealPath();require(Files.isDirectory(root))
val options=linkedMapOf<String,String>();val archives=linkedMapOf<String,String>();val recovery=linkedMapOf<String,String>()
var i=1
while(i<args.size) {
    val key=args[i++];require(i<args.size);val value=args[i++]
    if(key=="--archive"||key=="--recovery-map") {
        val pair=value.split('=',limit=2);require(pair.size==2)
        val target=if(key=="--archive") archives else recovery;require(target.put(safeName(pair[0]),pair[1])==null)
    } else { require(key in setOf("--project-root","--repo","--git-ref","--tested-source","--source-root") && options.put(key,value)==null) { "Unknown/repeated option: $key" } }
}
val canonical=verifyDirectory(root,archives)
var retained:Package539?=null
if("retained-evidence.json" in canonical.indexed) {
    val reference=document(regular(root,"retained-evidence.json"));require(text(reference,"task")==task539)
    val project=Path.of(options["--project-root"]?:error("Retained reference requires --project-root")).toRealPath()
    val relative=safeName(text(reference,"project_relative_root"));val target=project.resolve(relative)
    var ownedPath=project;for(part in relative.split('/')) { ownedPath=ownedPath.resolve(part);require(!Files.isSymbolicLink(ownedPath)) }
    require(!Files.isSymbolicLink(target) && target.toRealPath().startsWith(project))
    val retainedPackage=verifyDirectory(target.toRealPath());retained=retainedPackage
    require(entries(reference)==retainedPackage.indexed) { "Retained reference exact inventory mismatch" }
    require(text(reference,"index_sha256")==retainedPackage.observed.getValue("evidence-index.json").sha256)
    require(text(reference,"sha_ledger_sha256")==retainedPackage.observed.getValue("evidence.sha256").sha256)
}
val recoveries=mutableListOf<JsonObject>()
for((mapName,archiveName) in recovery) {
    require(mapName in canonical.indexed)
    val fromRetained=archiveName.startsWith("retained:");val name=if(fromRetained)archiveName.removePrefix("retained:") else archiveName
    safeName(name);val collection=if(fromRetained)retained?:error("Missing retained package") else canonical
    val blobs=collection.archives[name]?:error("Recovery archive not verified: $name")
    val seen=mutableSetOf<String>();val used=mutableSetOf<String>()
    for(row in document(regular(root,mapName))["entries"]?.jsonArray?:error("Missing recovery entries")) {
        val objectRow=row.jsonObject;val(original,expected)=item(objectRow);require(seen.add(original)) { "Duplicate recovery path" }
        val blob=safeName(text(objectRow,"blob"));require(blob=="blobs/${expected.sha256}" && blobs[blob]==expected) { "Recovery bytes/hash mismatch: $original" };used.add(blob)
    }
    require(used==blobs.keys.filter { it.startsWith("blobs/") }.toSet()) { "Unmapped recovery blobs" }
    recoveries+=buildJsonObject { put("map",mapName);put("archive",archiveName);put("restorable_paths",seen.size);put("distinct_blobs",used.size) }
}
val tested=linkedMapOf<String,Bytes539>();var sourceRoot:Path?=null
require(("--tested-source" in options)==("--source-root" in options))
if("--tested-source" in options) {
    val sourceDir=Path.of(options.getValue("--source-root")).toRealPath();sourceRoot=sourceDir
    val source=document(Path.of(options.getValue("--tested-source")))
    for((name,value) in source["files"]?.jsonObject?:error("Expected tested source files map")) {
        safeName(name);val expected=value.jsonPrimitive.content;require(digestPattern.matches(expected))
        val observed=hashFile(regular(sourceDir,name));require(observed.sha256==expected) { "Tested source changed: $name" };tested[name]=observed
    }
    require(tested.isNotEmpty())
}
require(("--repo" in options)==("--git-ref" in options))
var blobsChecked=0
if("--git-ref" in options) {
    val repo=Path.of(options.getValue("--repo")).toRealPath();val ref=options.getValue("--git-ref");require(Regex("[a-f0-9]{40}").matches(ref))
    fun verifyBlob(name:String,expected:Bytes539) {
        val child=ProcessBuilder("git","-C",repo.toString(),"cat-file","blob","$ref:$name").start()
        val got=child.inputStream.use(::hashStream);val error=child.errorStream.readAllBytes().toString(Charsets.UTF_8)
        require(child.waitFor()==0 && got==expected) { "Git blob mismatch $name: $error" };blobsChecked++
    }
    require(root.startsWith(repo));val prefix=repo.relativize(root).toString().replace(java.io.File.separatorChar,'/')
    for((name,expected) in canonical.observed)verifyBlob(if(prefix.isEmpty())name else "$prefix/$name",expected)
    val sourceDir=sourceRoot
    if(sourceDir!=null) { require(sourceDir.startsWith(repo));val sourcePrefix=repo.relativize(sourceDir).toString().replace(java.io.File.separatorChar,'/');for((name,expected) in tested)verifyBlob(if(sourcePrefix.isEmpty())name else "$sourcePrefix/$name",expected) }
}
println(Json { prettyPrint=true }.encodeToString(JsonObject.serializer(),buildJsonObject {
    put("status","PACKAGE_BINDINGS_PASS_NOT_TASK_ACCEPTANCE");put("task",task539)
    put("declared_package_status",canonical.index["status"]?:JsonNull)
    put("indexed_files",canonical.indexed.size);put("index_sha256",canonical.observed.getValue("evidence-index.json").sha256)
    put("canonical_archives",canonical.archives.size);put("retained_archives",retained?.archives?.size?:0)
    put("recovery",JsonArray(recoveries));put("tested_source_files",tested.size);put("git_blobs_checked",blobsChecked)
}))
