#!/usr/bin/env kotlinr
@file:Import("Catalog598.main.kts")
import java.io.File
import java.security.MessageDigest
import java.util.Properties
require(args.size==2) { "completedRunRoot NEWsummaryDirectory" }
val root=File(args[0]).canonicalFile;val output=File(args[1]).canonicalFile
val allowed=File("/Users/yoda/dev/pet/krogu-math/.codex-work/598").canonicalFile.toPath()
require(root.toPath().startsWith(allowed) && output.toPath().startsWith(allowed) && !output.exists())
check(File(root,"EXECUTED_NOT_YET_ANALYZED").isFile)
fun properties598(file:File)=Properties().apply { file.inputStream().use{load(it)} }
fun sha598(file:File)=file.inputStream().use{input-> val md=MessageDigest.getInstance("SHA-256");val bytes=ByteArray(65536);while(true){val n=input.read(bytes);if(n<0)break;md.update(bytes,0,n)};md.digest().joinToString(""){"%02x".format(it)}}
val binding=properties598(File(root,"binding.txt"));val platform=binding.getProperty("platform")
val catalog=catalog598(platform)
val warm=mutableListOf<List<String>>();val first=mutableListOf<List<String>>();val identities=mutableSetOf<Pair<String,String>>()
val relations=mutableMapOf<Triple<String,String,Int>,String>()
val resources=mutableListOf<String>()
fun numeric598(value:String):Double? = if(value=="NA")null else value.toDouble().also{require(it.isFinite() && it>=0)}
for(fork in 0..2) for((position,work) in (if(fork==1)catalog.asReversed() else catalog).withIndex()) {
    val log=File(root,"fork$fork-case$position.log");val meta=properties598(File(log.path+".binding"))
    check(meta.getProperty("peer")==work.peer && meta.getProperty("id")==work.id && meta.getProperty("fork")==fork.toString() && meta.getProperty("order")==if(fork==1)"reverse" else "forward")
    check(meta.getProperty("exit")=="0" && meta.getProperty("log_sha256")==sha598(log))
    val identity=properties598(File(log.path+".identity"));check(identity.getProperty("supervisor_pid").toLong()>0)
    check(identities.add(identity.getProperty("supervisor_pid") to identity.getProperty("supervisor_start")))
    val lines=log.readLines();val firstLine=lines.single{it.startsWith("FIRST598\t")};val gateLine=lines.single{it.startsWith("GATE598_PASS\t")}
    check(lines.indexOf(firstLine)<lines.indexOf(gateLine))
    check(gateLine=="GATE598_PASS\t${work.peer}\t${work.id}\t$fork")
    check(lines.single{it.startsWith("COMPLETE598_TIMING_CHILD\t")}=="COMPLETE598_TIMING_CHILD\t${work.peer}\t${work.id}\t$fork")
    val input=lines.single{it.startsWith("INPUT598\t")}.split('\t');check(input.size==5)
    val expected=expected598(work.id)
    check(input[1]==expected.left && input[2]==expected.right && input[4]==expected.expected)
    if(expected.family=="text")check(input[3]=="unary-text")
    else if(expected.self)check(input[3]=="self-alias")
    else {
        check(input[3] in listOf("binary-native-alias","binary-distinct"))
        if(input[3]=="binary-native-alias")check(canonical598(expected.left)==canonical598(expected.right))
    }
    val key=Triple(work.peer,work.id,fork);relations[key]=input[3]
    val identityRow=lines.single{it.startsWith("IDENTITY598\t")}.split('\t')
    val runtime=when(platform){"JVM"->"JVM/BellSoft25.0.4+9-LTS";"JS"->"JS/Node24.16.0";"Wasm"->"Wasm/Node24.16.0";else->"macOSArm64/KotlinNative2.4.21"}
    check(identityRow==listOf("IDENTITY598",runtime,work.peer,work.id,fork.toString(),if(fork==1)"reverse" else "forward"))
    check(lines.count{it.startsWith("SCOPE598\t")}==1 && lines.count{it.startsWith("WARM598\t")}==1 && lines.count{it.startsWith("COLD598_SCOPE\t")}==1)
    if(platform=="JVM") {
        val counts=lines.filter{it.startsWith("GATE_COUNT598\t")}.map{it.split('\t')}
        check(counts.size==4 && counts.map{it[1]}.toSet()==catalog.map{it.peer}.toSet())
        counts.forEach{check(it.size==4 && it[2].toInt() in 128..168 && it[3].toInt()>=1)}
    } else {
        val complete=lines.single{it.startsWith("COMPLETE598_UNTIMED_ONLY\t")}
        check(Regex("COMPLETE598_UNTIMED_ONLY\tcore=128\tdistinct=([0-9]+)").matches(complete))
        val distinct=complete.substringAfter("distinct=").toInt();check(distinct in 0..32)
        check(lines.count{it.startsWith("CHECK598\t")}==128+distinct)
    }
    val f=firstLine.split('\t');check(f.size==12 && f[1]==work.peer && f[2]==work.id && f[3]==fork.toString() && f[4]=="1" && f[5].toLong()>0)
    (6..10).forEach{numeric598(f[it])};check(f[11].toInt()==checksum598(expected.canonical));first+=f
    val training=lines.single{it.startsWith("WARM598\t")}.split('\t')
    check(training.size==8 && training.take(4)==listOf("WARM598",work.peer,work.id,fork.toString()))
    check(training[4].toLong()>0 && training[5].toLong()>=200_000_000L && training[6].toInt() in 1..1_048_576)
    check(training[7]=="MINIMUM200ms_BATCHCAP1048576_TARGET20msbatch")
    val selectedCount=training[6].toInt()
    check(lines.indexOf(training.joinToString("\t"))>lines.indexOf(gateLine))
    val rows=lines.filter{it.startsWith("TIME598\t")}.map{it.split('\t')};check(rows.size==7)
    for((sample,row) in rows.withIndex()) {
        check(row.size==13 && row[1]==work.peer && row[2]==work.id && row[3]==fork.toString() && row[4]==sample.toString())
        check(row[5].toInt()==selectedCount && row[6].toLong()>0)
        (7..11).forEach{numeric598(row[it])};check(row[12].toInt()==ringChecksum598(expected.canonical,selectedCount))
        check(lines.indexOf(row.joinToString("\t"))>lines.indexOf(training.joinToString("\t")));warm+=row
    }
    val maximum=lines.mapNotNull{Regex("\\s*([0-9]+)\\s+maximum resident set size").matchEntire(it)?.groupValues?.get(1)}.single().toLong()
    check(maximum>0)
    resources+="$fork\t${work.peer}\t${work.id}\t$maximum\tWHOLE_CHILD_MACOS_MAXRSS_BYTES_INCLUDING_GATE_AND_EXIT"
}
check(first.size==catalog.size*3 && warm.size==catalog.size*21)
fun median598(values:List<Double>):Double {require(values.isNotEmpty());val a=values.sorted();return if(a.size%2==1)a[a.size/2] else (a[a.size/2-1]+a[a.size/2])/2}
check(output.mkdirs())
fun write598(name:String,lines:List<String>) {File(output,name).bufferedWriter().use{writer->lines.forEach{writer.appendLine(it)}}}
write598("raw-first.tsv",first.map{it.joinToString("\t")});write598("raw-warm.tsv",warm.map{it.joinToString("\t")});write598("whole-child-resources.tsv",resources)
val summary=mutableListOf("peer\tid\tfork\tns_per_op_median\tns_per_op_min\tns_per_op_max\tderived_ops_per_second\tcpu_ns_per_op_median\tallocation_B_per_op_median\trelation")
val medians=mutableMapOf<Triple<String,String,Int>,Double>()
for(work in catalog) for(fork in 0..2) {
    val rows=warm.filter{it[1]==work.peer && it[2]==work.id && it[3]==fork.toString()}
    val ns=rows.map{it[6].toDouble()/it[5].toLong()};val m=median598(ns);medians[Triple(work.peer,work.id,fork)]=m
    fun counter(column:Int):String {val v=rows.map{numeric598(it[column])};check(v.all{it==null} || v.all{it!=null});return if(v.all{it==null})"NA" else median598(rows.map{it[column].toDouble()/it[5].toLong()}).toString()}
    summary+="${work.peer}\t${work.id}\t$fork\t$m\t${ns.min()}\t${ns.max()}\t${1e9/m}\t${counter(7)}\t${counter(8)}\t${relations[Triple(work.peer,work.id,fork)]}"
}
write598("per-fork.tsv",summary)
val paired=mutableListOf("id\tpeer\tfork\tpeer_ns_over_krogu_ns\tpeer_minus_krogu_ns\tsame_observed_relation")
for(work in catalog.filter{it.peer!="krogu-final539-PENDING"}) for(fork in 0..2) {
    val k=medians[Triple("krogu-final539-PENDING",work.id,fork)]?:error("Missing matched Krogu operation")
    val p=medians.getValue(Triple(work.peer,work.id,fork));paired+="${work.id}\t${work.peer}\t$fork\t${p/k}\t${p-k}\t${relations[Triple(work.peer,work.id,fork)]==relations[Triple("krogu-final539-PENDING",work.id,fork)]}"
}
write598("paired.tsv",paired)
val firstSummary=mutableListOf("peer\tid\tfirst_ns_median_of3\tfirst_ns_min\tfirst_ns_max\tSCOPE")
val firstPairs=mutableListOf("id\tpeer\tfork\tfirst_peer_over_krogu\tfirst_peer_minus_krogu_ns\tsame_observed_relation")
for(work in catalog) {
    val values=first.filter{it[1]==work.peer && it[2]==work.id}.map{it[5].toDouble()}
    check(values.size==3);firstSummary+="${work.peer}\t${work.id}\t${median598(values)}\t${values.min()}\t${values.max()}\tFIRST_SELECTED_OP_AFTER_DECLARED_PREPARATION_NOT_PRISTINE_VM"
    if(work.peer!="krogu-final539-PENDING") for(fork in 0..2) {
        val p=first.single{it[1]==work.peer && it[2]==work.id && it[3]==fork.toString()}[5].toDouble()
        val k=first.single{it[1]=="krogu-final539-PENDING" && it[2]==work.id && it[3]==fork.toString()}[5].toDouble()
        firstPairs+="${work.id}\t${work.peer}\t$fork\t${p/k}\t${p-k}\t${relations[Triple(work.peer,work.id,fork)]==relations[Triple("krogu-final539-PENDING",work.id,fork)]}"
    }
}
write598("first-summary.tsv",firstSummary);write598("first-paired.tsv",firstPairs)
write598("SCOPE.txt",listOf("All per-fork/adverse rows retained. FIRST after declared preparation precedes gate, not VM/process cold; PARSE has only source String prepared, FORMAT has a parsed unobserved native operand. Warm FORMAT is observed/cached. Throughput is derived from the same wall bracket; latency includes adapter/ring escape. Mixed-alias relations flagged, not silently comparable. RSS/heap snapshots are process metrics, never owned-retained or allocation. No speedup threshold or final task verdict.","Source artifact binding:"+File(root,"binding.txt").readText()))
File(output,"CHECKED_NOT_YET_INDEPENDENTLY_ACCEPTED").writeText("Exact inventory and arithmetic/counter parsing checked; source/runtime/metrology and comparative report need independent review.\n")
