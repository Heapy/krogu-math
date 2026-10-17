#!/usr/bin/env kotlinr
@file:Import("Catalog598.main.kts")
import java.io.File
import java.security.MessageDigest
import java.time.Instant
import java.util.Properties
import java.util.concurrent.TimeUnit
require(args.size==6) { "artifactRoot artifactManifestSHA runtimeExe runtimeSHA acceptedKroguCommit NEWoutput" }
fun sha598(file:File)=file.inputStream().use { input -> val md=MessageDigest.getInstance("SHA-256");val bytes=ByteArray(65536);while(true){val n=input.read(bytes);if(n<0)break;md.update(bytes,0,n)};md.digest().joinToString(""){"%02x".format(it)} }
val base=File("/Users/yoda/dev/pet/krogu-math/.codex-work/598").canonicalFile
val script=File(base,"prepared-timing/run-timing.main.kts")
val catalogFile=File(base,"prepared-timing/Catalog598.main.kts")
val scriptSHA=sha598(script);val catalogSHA=sha598(catalogFile)
require(System.getProperty("os.name").contains("Mac",true)) { "This exact process/RSS launcher is macOS only" }
val host598=java.lang.management.ManagementFactory.getOperatingSystemMXBean()
val tree=File(args[0]).canonicalFile;val runtime=File(args[2]).canonicalFile;val output=File(args[5]).canonicalFile
require(tree.toPath().startsWith(base.toPath()) && output.toPath().startsWith(base.toPath()) && !output.exists())
require(args[1].matches(Regex("[a-f0-9]{64}")) && args[3].matches(Regex("[a-f0-9]{64}")) && args[4].matches(Regex("[a-f0-9]{40}")))
val manifest=File(tree,"manifest.sha256")
fun rows598(file:File)=file.readLines().map { line-> val f=line.split('\t');require(f.size==2 && f[0].matches(Regex("[a-f0-9]{64}")));f[0] to f[1] }
val members=rows598(manifest).map { (h,n)-> require(!File(n).isAbsolute && n.split('/').none { it in listOf("",".","..") });h to File(tree,n).canonicalFile.also{require(it.toPath().startsWith(tree.toPath()))} }
check(members.map{it.second}.distinct().size==members.size)
val metadata=Properties().apply { File(tree,"artifact.properties").inputStream().use{load(it)} }
val platform=metadata.getProperty("platform").also { require(it in listOf("JVM","JS","Wasm","Native")) }
check(metadata.getProperty("krogu_commit")==args[4] && metadata.getProperty("kotlin")=="2.4.21" && metadata.getProperty("ionspin")=="0.3.10" && metadata.getProperty("kbignum")=="0.0.20")
val sourceRows=rows598(File(tree,"sources.sha256")).map { (h,n)->h to File(n).canonicalFile.also{require(it.toPath().startsWith(base.toPath()))} }
val dependencyRoots=listOf(File("/Users/yoda/.gradle"),File("/Users/yoda/.konan"),File("/Users/yoda/.m2"),File("/Users/yoda/dev/pet/krogu-math/.codex-work")).map{it.canonicalFile.toPath()}
val dependencyRows=rows598(File(tree,"compile-inputs.sha256")).map{(h,n)->h to File(n).canonicalFile.also{f->require(dependencyRoots.any{f.toPath().startsWith(it)})}}
fun verify598() {
    check(sha598(script)==scriptSHA && sha598(catalogFile)==catalogSHA && sha598(manifest)==args[1] && sha598(runtime)==args[3])
    for((h,f) in members+sourceRows+dependencyRows) check(f.isFile && sha598(f)==h) { "Changed input:$f" }
    check(tree.walkTopDown().filter{it.isFile}.map{it.canonicalFile}.toSet()==members.map{it.second}.toSet()+manifest)
}
verify598();check(output.mkdirs())
File(output,"binding.txt").writeText("platform=$platform\nartifact_manifest_sha256=${args[1]}\nruntime_exe=$runtime\nruntime_sha256=${args[3]}\nkrogu_commit=${args[4]}\ncontroller_sha256=$scriptSHA\nidentity_scope=usr-bin-time_supervisor_only_runtime_PID_unobserved\ncatalog_sha256=$catalogSHA\nartifact_tree=$tree\ncwd=$tree\npublication_ledger_sha256=${metadata.getProperty("publication_ledger_sha256")}\n")
val catalog=catalog598(platform)
File(output,"catalog.tsv").writeText(catalog.joinToString("\n",postfix="\n"){"${it.peer}\t${it.id}"})
File(output,"NA.tsv").writeText(if(platform=="JVM") "ionspin-bignum-0.3.10\tfixed-scale/*\tNo verified native fixed-scale equivalent\nALL\tsignificant-HALF_EVEN/MC34\tNot implemented in this exact-core timing slice\n" else "JDK\t*\tNo java.math runtime on this target\nKBigNum0.0.20\t*\tNo published matching JS/Wasm/macOS variant\nALL\tfixed-scale/significant-HALF_EVEN/MC34\tNo common verified peer mapping in this slice\n")
val entry=metadata.getProperty("entry")
fun member598(name:String)=File(tree,name).canonicalFile.also { require(it in members.map{it.second}) }
val prefix=when(platform) {
    "JVM" -> {
        check(entry=="preparation598.Timing598Kt")
        val jars=File(tree,"classpath.txt").readLines().filter{it.isNotEmpty()}.map{member598(it).path}
        require(jars.isNotEmpty())
        listOf(runtime.path,"-Xms512m","-Xmx512m","-XX:+UseSerialGC","-XX:ActiveProcessorCount=1","-XX:-OmitStackTraceInFastThrow","-cp",jars.joinToString(File.pathSeparator),entry)
    }
    "JS","Wasm" -> listOf(runtime.path,"--expose-gc","--max-old-space-size=512",member598(entry).path)
    else -> {check(runtime==member598(entry));listOf(runtime.path)}
}
val campaignDeadline=System.nanoTime()+TimeUnit.HOURS.toNanos(6)
fun execute598(command:List<String>,log:File) {
    File(log.path+".argv.txt").writeText(command.joinToString("\n",postfix="\n"))
    val builder=ProcessBuilder(command).directory(tree).redirectErrorStream(true).redirectOutput(log)
    builder.environment().clear();builder.environment().putAll(mapOf("LANG" to "C","TZ" to "UTC"))
    val startWall=Instant.now();val startMono=System.nanoTime();val startLoad=host598.systemLoadAverage;val startCpuCount=host598.availableProcessors
    val child=builder.start();var terminal=false;var primary:Throwable?=null
    try {
        File(log.path+".identity").writeText("supervisor_pid=${child.pid()}\nsupervisor_start=${child.toHandle().info().startInstant().orElseThrow()}\nwall=${Instant.now()}\n")
        val until=minOf(campaignDeadline,System.nanoTime()+TimeUnit.SECONDS.toNanos(600))
        while(!terminal && System.nanoTime()<until) {
            terminal=child.waitFor(200,TimeUnit.MILLISECONDS)
            check(log.length()<=8L*1024*1024) { "Child output stop threshold exceeded" }
        }
        check(terminal && child.exitValue()==0) { "Child failure/deadline:$log" }
        File(log.path+".host").writeText("start_wall=$startWall\nend_wall=${Instant.now()}\nstart_mono_ns=$startMono\nend_mono_ns=${System.nanoTime()}\nloadavg_start=$startLoad\nloadavg_end=${host598.systemLoadAverage}\nprocessors_start=$startCpuCount\nprocessors_end=${host598.availableProcessors}\n")
    } catch(error:Throwable) {primary=error;throw error}
    finally {
        if(primary!=null || !terminal || child.isAlive) {
            var cleanup:Throwable?=null
            fun record(e:Throwable){if(cleanup==null)cleanup=e else cleanup!!.addSuppressed(e)}
            val descendants=try{child.toHandle().descendants().toList()}catch(e:Throwable){record(e);emptyList()}
            for(handle in descendants.asReversed()+child.toHandle()) try{handle.destroyForcibly()}catch(e:Throwable){record(e)}
            val until=System.nanoTime()+TimeUnit.SECONDS.toNanos(10);var interrupted=Thread.interrupted() || primary is InterruptedException
            for(handle in descendants+child.toHandle()) while(handle.isAlive && System.nanoTime()<until) {
                try{handle.onExit().get(maxOf(1L,until-System.nanoTime()),TimeUnit.NANOSECONDS);break}
                catch(_:InterruptedException){interrupted=true}
                catch(_:java.util.concurrent.TimeoutException){break}
                catch(e:Throwable){record(e);break}
            }
            if((descendants+child.toHandle()).any{it.isAlive})record(IllegalStateException("Owned child cleanup incomplete"))
            if(interrupted)Thread.currentThread().interrupt()
            cleanup?.let{if(primary!=null)primary!!.addSuppressed(it) else throw it}
        }
    }
}
for(fork in 0..2) {
    val order=if(fork==1)"reverse" else "forward"
    for((position,work) in (if(fork==1)catalog.asReversed() else catalog).withIndex()) {
        verify598();check(System.nanoTime()<campaignDeadline)
        val log=File(output,"fork$fork-case$position.log")
        execute598(listOf("/usr/bin/time","-l")+prefix+listOf(work.peer,work.id,fork.toString(),order),log)
        val lines=log.readLines()
        check(lines.count{it.startsWith("FIRST598\t")}==1 && lines.count{it.startsWith("TIME598\t")}==7 && lines.count{it.startsWith("GATE598_PASS\t")}==1)
        check(lines.single{it.startsWith("COMPLETE598_TIMING_CHILD\t")}=="COMPLETE598_TIMING_CHILD\t${work.peer}\t${work.id}\t$fork")
        File(log.path+".binding").writeText("peer=${work.peer}\nid=${work.id}\nfork=$fork\norder=$order\nlog_sha256=${sha598(log)}\nexit=0\n")
        verify598();println("COMPLETE598_CHILD fork=$fork position=$position peer=${work.peer} id=${work.id}")
    }
}
verify598()
File(output,"EXECUTED_NOT_YET_ANALYZED").writeText("Three fresh F/R/F cohorts complete; FIRST after parsing precedes correctness gate; strict inventory/counter/statistics and independent report pending.\n")
