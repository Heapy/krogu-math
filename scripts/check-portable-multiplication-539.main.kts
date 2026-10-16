#!/usr/bin/env kotlinr

/** Inventory/metrics validation of one source-bound portable campaign; no policy adoption. */
import java.io.File

require(args.size == 9) { "samples mode JVM|JS|Wasm|Native sourceManifestSHA artifactManifestSHA new.csv fork1.log fork2.log fork3.log" }
val samples = args[0].toInt().also { require(it in 5..15) }
val mode = args[1].also { require(it in listOf("kernel-small", "kernel-medium", "kernel-large", "cost", "public-small", "native-parallel", "square-control")) }
val platform = args[2].also { require(it in listOf("JVM", "JS", "Wasm", "Native")) }
args.slice(3..4).forEach { require(it.matches(Regex("[a-f0-9]{64}"))) }
require(mode != "native-parallel" || platform == "Native")
val output = File(args[5]).canonicalFile.also { require(!it.exists()) }
val logs = args.drop(6).map { File(it).canonicalFile }.also { require(it.toSet().size == 3) }
data class Policy(val k: Int, val s: Int, val e: Int)
val policies = (listOf(32,64,128,256).map { Policy(it,16,384) } +
    listOf(4,8,16,32).map { Policy(64,it,384) } + listOf(0,128,256,384,512,1024).map { Policy(64,16,it) }).distinct()
fun inventory(processors: Int): Set<String> = buildSet {
    when (mode) {
        "cost" -> addAll(listOf("signed-long-256", "unsigned-long-256", "int-carry-256", "allocate-256-words", "allocate-4096-words").map { "cost/$it" })
        "public-small" -> for (bits in listOf(1,30,63,64,127,511,1023)) for (shape in listOf("distinct","square")) for (method in listOf("public","parallel-public")) add("$method/$bits/$shape")
        "square-control" -> for(size in listOf(128,192,256,384,512,1024)) for(entry in listOf(0,384,1024,Int.MAX_VALUE)) add("square-control/$size/e$entry")
        "native-parallel" -> for (size in listOf(256,1024,4096,8192,16384)) for (shape in listOf("distinct","square","ratio2")) {
            add("native-parallel/$size/$shape/kernel"); add("native-parallel/$size/$shape/frame")
            if (processors > 1) add("native-parallel/$size/$shape/worker1")
            if (processors > 2) add("native-parallel/$size/$shape/worker2")
        }
        else -> {
            val sizes = when(mode) { "kernel-small" -> listOf(8,16,31,32,63,64); "kernel-medium" -> listOf(96,128,192,256,383,384,512); else -> listOf(1024,4096,8192) }
            for(size in sizes) for(shape in listOf("distinct","square","ratio2","ratio4")) {
                for(p in policies.filter { shape=="square" || (it.s==16 && it.e==384) }) add("kernel/$size/$shape/k${p.k}-s${p.s}-e${p.e}")
                if(size >= 64 && shape != "ratio4") add("frame-serial/$size/$shape")
            }
        }
    }
}
data class Row(val id:String,val sample:Int,val count:Long,val wall:Long,val cpu:Long?,val bytes:Long?,val heap:Long?)
val rows = mutableListOf<List<Row>>()
val orders = mutableSetOf<String>()
val runtimes = mutableSetOf<String>()
val identities = mutableSetOf<String>()
var expectedIds: Set<String>? = null
var representations: Map<String,String>? = null
for(log in logs) {
    check(File(log.path+".exit").readText().trim()=="0")
    val binding=File(log.path+".binding").readLines()
    fun bound(key:String)=binding.single { it.startsWith("$key=") }.substringAfter('=')
    check(bound("source_manifest_sha256")==args[3] && bound("artifact_manifest_sha256")==args[4])
    check(bound("pid").toLong()>0 && bound("start_epoch_ms").toLong()>0)
    check(identities.add(bound("pid")+":"+bound("start_epoch_ms")))
    runtimes+=bound("runtime_id").also { check(it.isNotBlank()) }
    val lines=log.readLines()
    val header=lines.single { it.startsWith("# calibration539 ") }
    check("platform=$platform " in header && "mode=$mode " in header)
    val processors=if(platform=="Native") Regex("processors=([0-9]+)").find(header)!!.groupValues[1].toInt().also { check(it>=1) } else 1
    orders+=Regex("order=(forward|reverse)").find(header)!!.groupValues[1]
    val ids=inventory(processors)
    check(Regex("cases=([0-9]+)$").find(header)!!.groupValues[1].toInt()==ids.size)
    if(expectedIds==null) expectedIds=ids else check(expectedIds==ids)
    check(lines.count { it=="PASS539,${ids.size},$samples" }==1)
    val checks=lines.filter { it.startsWith("CHECK539,") }.map {
        val p=it.split(','); check(p.size==3 && p[2]=="PASS"); p[1]
    }
    check(checks.size==ids.size && checks.toSet()==ids)
    val reps=lines.filter { it.startsWith("REP539,") }.map {
        val p=it.split(','); check(p.size==3 && p[2].matches(Regex("[0-9]+"))); p[1] to p[2]
    }
    if(mode=="public-small") {
        check(reps.size==ids.size && reps.map { it.first }.toSet()==ids)
        if(representations==null) representations=reps.toMap() else check(representations==reps.toMap())
    } else check(reps.isEmpty())
    val skipped=lines.filter { it.startsWith("INELIGIBLE_NATIVE539,") }.map {
        val p=it.split(','); check(p.size==3 && p[2]=="processor_budget"); "native-parallel/"+p[1]
    }
    val allNative=if(mode=="native-parallel") inventory(3) else emptySet()
    check(skipped.size==skipped.toSet().size && skipped.toSet()==allNative-ids)
    fun metric(text:String):Long?=if(text=="NA") null else text.toLong().also { check(it>=0) }
    val timed=lines.filter { it.startsWith("TIME539,") }.map {
        val p=it.split(','); check(p.size==9)
        p[8].toInt()
        Row(p[1],p[2].toInt(),p[3].toLong(),p[4].toLong(),metric(p[5]),metric(p[6]),metric(p[7])).also { row ->
            check(row.id in ids && row.sample in 0 until samples && row.count in 1..8_388_608 && row.wall in 1..60_000_000_000L)
            if(platform=="JVM") check(row.cpu!=null && row.bytes!=null)
            if(platform!="JVM") check(row.bytes==null)
            if(platform in listOf("JS","Wasm")) check(row.cpu!=null)
            if(platform=="Wasm") check(row.heap==null)
        }
    }
    check(timed.size==ids.size*samples && timed.map { it.id }.toSet()==ids)
    for((id,group) in timed.groupBy { it.id }) {
        check(group.size==samples && group.map { it.sample }.toSet()==(0 until samples).toSet()) { id }
        check(group.map { it.count }.toSet().size==1)
    }
    rows+=timed
}
check(orders==setOf("forward","reverse") && runtimes.size==1)
fun median(values:List<Double>):Double {
    val sorted=values.sorted(); val middle=sorted.size/2
    return if(sorted.size%2==1) sorted[middle] else (sorted[middle-1]+sorted[middle])/2
}
output.bufferedWriter().use { writer ->
    writer.appendLine("case,wall_median_ns,wall_min_process_median_ns,wall_max_process_median_ns,cpu_median_ns,caller_allocated_median_bytes,coarse_heap_max_bytes")
    for(id in checkNotNull(expectedIds).sorted()) {
        val groups=rows.map { it.filter { row -> row.id==id } }
        val wall=groups.map { median(it.map { row -> row.wall.toDouble()/row.count }) }
        fun optional(metric:(Row)->Long?):String {
            if(groups.flatten().any { metric(it)==null }) return "NA"
            return median(groups.map { group -> median(group.map { metric(it)!!.toDouble()/it.count }) }).toString()
        }
        val heap=groups.flatten().mapNotNull { it.heap }.maxOrNull()?.toString() ?: "NA"
        writer.appendLine("$id,${median(wall)},${wall.min()},${wall.max()},${optional { it.cpu }},${optional { it.bytes }},$heap")
    }
}
println("CHECKED_PORTABLE539_NOT_ADOPTED,mode=$mode,platform=$platform,forks=3,cases=${expectedIds!!.size}")
