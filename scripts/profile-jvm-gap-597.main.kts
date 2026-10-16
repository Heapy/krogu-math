#!/usr/bin/env kotlinr
// SPDX-License-Identifier: Apache-2.0
// ORIGINAL527.jar POST533.jar NEW-output timings|profiles|prepare
// Heavy: run ONLY after direct serialized-slot handoff. prepare executes builds, not numerical campaigns.
import java.io.File
import java.security.MessageDigest
import java.time.Instant
import java.util.concurrent.TimeUnit

require(args.size == 4 && args[3] in listOf("timings", "profiles", "prepare"))
check(System.getProperty("java.runtime.version") == "25.0.4+9-LTS" && System.getProperty("java.vendor") == "BellSoft")
check(KotlinVersion.CURRENT.toString() == "2.4.21")
val root = File(".").canonicalFile
check(File(root, "settings.gradle.kts").isFile)
val original = File(args[0]).canonicalFile; val post = File(args[1]).canonicalFile
fun sha(file: File) = MessageDigest.getInstance("SHA-256").digest(file.readBytes()).joinToString("") { "%02x".format(it) }
val originalSha = "089a414d64ddc7822e0174227689c309603ff28772a189967303b17473dad3aa"
val postSha = "c04f08c4f2cfefb034db3ff208eec25fd8e13db311b2e31ae3a901d391dd02f0"
check(original != post && sha(original) == originalSha && sha(post) == postSha)
val output = File(args[2]).canonicalFile
require(!output.exists()); check(output.mkdirs())
val script = File(root, "scripts/profile-jvm-gap-597.main.kts")
val probeSource = File(root, "diagnostics/jvm-gap-597/Gap597Probe.kt")
val workloads = File(root, "benchmarks/src/commonMain/kotlin/bench/Workloads.kt")
val jdkWorkloads = File(root, "benchmarks/src/jvmMain/kotlin/bench/JdkWorkloads.kt")
fun sources(): String = (listOf("src/commonMain", "src/jvmMain").flatMap { directory ->
    File(root, directory).walkTopDown().filter { it.isFile }.toList()
} + listOf(File(root,"build.gradle.kts"),File(root,"settings.gradle.kts"),script,probeSource,workloads,jdkWorkloads))
    .sortedBy { it.relativeTo(root).path }.joinToString("\n", postfix="\n") { "${sha(it)}  ${it.relativeTo(root).path}" }
val sourceBefore = sources()
File(output,"sources-before.sha256").writeText(sourceBefore)
fun stopOwned(process: Process): String {
    val handles = process.descendants().toList() + process.toHandle()
    handles.reversed().filter { it.isAlive }.forEach { it.destroy() }
    process.waitFor(3,TimeUnit.SECONDS)
    handles.filter { it.isAlive }.forEach { it.destroyForcibly() }
    val deadline=System.nanoTime()+TimeUnit.SECONDS.toNanos(5)
    for(handle in handles) {
        val left=deadline-System.nanoTime()
        if(handle.isAlive && left>0) try { handle.onExit().get(left,TimeUnit.NANOSECONDS) }
        catch(_:java.util.concurrent.TimeoutException) { }
    }
    return "owned_survivors=${handles.filter { it.isAlive }.map { it.pid() }} root_alive=${process.isAlive}"
}
fun run(command: List<String>, name: String, seconds: Long) {
    File(output,"$name.command.txt").writeText(command.joinToString("\n",postfix="\n"))
    val builder=ProcessBuilder(command).directory(root).redirectOutput(File(output,"$name.stdout.txt"))
        .redirectError(File(output,"$name.stderr.txt"))
    listOf("JAVA_TOOL_OPTIONS","JDK_JAVA_OPTIONS","_JAVA_OPTIONS","KOTLIN_OPTS","KOTLIN_RUNNER","JAVACMD","JAVA_OPTS").forEach { builder.environment().remove(it) }
    builder.environment()["JAVA_HOME"]=System.getProperty("java.home")
    println("${Instant.now()} launch $name")
    val child=builder.start();println("$name pid=${child.pid()}")
    if(!child.waitFor(seconds,TimeUnit.SECONDS)) {
        val cleanup=stopOwned(child);File(output,"$name.timeout.txt").writeText(cleanup)
        error("$name timeout; $cleanup; partials retained without retry")
    }
    check(child.exitValue()==0) { "$name exit=${child.exitValue()}; partials retained" }
}
// Fresh JVM artifact is produced INSIDE the same source freeze, not falsely inferred
// from an arbitrary supplied JAR hash. Gradle's normal input/build-cache contract applies.
run(listOf(File(root,"gradlew").path,"--no-daemon",":jvmJar","--console=plain"),"build-current",600)
check(sources()==sourceBefore)
val built=File(root,"build/libs/krogu-math-jvm-0.1.0-SNAPSHOT.jar")
val current=File(output,"current.jar");built.copyTo(current)
val kotlinHome=File(System.getProperty("kotlin.home")).canonicalFile
val probe=File(output,"probe.jar")
run(listOf(File(kotlinHome,"bin/kotlinc").path,"-classpath",current.path,
    workloads.path,jdkWorkloads.path,probeSource.path,"-d",probe.path),"build-probe",180)
check(sources()==sourceBefore)
val libraries=listOf("kotlin-stdlib.jar","kotlin-stdlib-jdk7.jar","kotlin-stdlib-jdk8.jar")
    .map { File(kotlinHome,"lib/$it") }.filter { it.isFile }
check(libraries.any { it.name=="kotlin-stdlib.jar" })
val artifacts=(listOf(original,post,current,probe)+libraries).associateWith(::sha)
File(output,"artifacts.sha256").writeText(artifacts.entries.joinToString("\n",postfix="\n") { "${it.value}  ${it.key}" })
fun binding() {check(sources()==sourceBefore);check(artifacts.all { (file,hash)->sha(file)==hash })}
if(args[3]=="prepare") {
    binding()
    File(output,"PREPARED.txt").writeText("Fresh current/probe build and frozen artifacts prepared; no numerical campaign executed\n")
    println("PREPARED current/probe artifacts; timings/profiles not executed")
    kotlin.system.exitProcess(0)
}
val labels=listOf("small","wide","negative-scale","large-scale")
val priority=labels.flatMap { listOf("decimal/$it/divide","decimal/$it/precision") }
val expected=(priority+labels.flatMap { listOf("precision-first/$it/string","precision-first/$it/coefficient","precision-construction/$it/string") }).toSet()
check(expected.size==20)
fun host(name:String) {
    run(listOf("uptime"),"$name-load",10)
    // comm is executable identity only; no unrelated arguments are published.
    run(listOf("ps","-axo","pid,ppid,pcpu,pmem,comm"),"$name-processes",10)
    val processes=File(output,"$name-processes.stdout.txt")
    processes.writeText(processes.readLines().filterIndexed { index,line ->
        index==0 || (line.trim().split(Regex("\\s+"),limit=5).getOrNull(2)?.toDoubleOrNull() ?: 0.0)>=10
    }.joinToString("\n",postfix="\n"))
    if(System.getProperty("os.name")=="Mac OS X") run(listOf("sysctl","vm.swapusage"),"$name-swap",10)
}
val java=File(System.getProperty("java.home"),"bin/java").path
fun execute(era:String,jar:File,fork:Int,mode:String,case:String) {
    binding()
    val name="$era-$fork-$mode-${case.replace('/','_')}"
    val destination=File(output,name)
    val classpath=(listOf(jar,probe)+libraries).joinToString(File.pathSeparator)
    val command=listOf(java,"-Xms256m","-Xmx1g","-XX:+UseG1GC","-cp",classpath,"bench.Gap597Probe",
        mode,if(era=="jdk") "jdk" else "krogu",jar.path,artifacts.getValue(jar),probe.path,artifacts.getValue(probe),
        "$fork",if(fork%2==0) "forward" else "reverse",case,destination.path)
    host("$name-before")
    val timing=if(!File("/usr/bin/time").canExecute()) {
        File(output,"$name-external-process-metrics.txt").writeText("RSS/whole-process external time unavailable; exact Java counters remain in samples.csv\n")
        emptyList()
    } else if(System.getProperty("os.name")=="Mac OS X") listOf("/usr/bin/time","-l") else listOf("/usr/bin/time","-v")
    run(timing+command,name,if(mode=="timing") 300 else 120)
    host("$name-after")
    check(File(destination,"COMPLETE.txt").isFile)
    val rows=File(destination,"samples.csv").readLines().drop(1).map { it.split(',') }
    val wanted=if(mode=="timing") expected else setOf(case)
    check(rows.size==wanted.size*(if(mode=="timing")5 else 1))
    check(rows.all { it.size==10 && it[0]==if(era=="jdk") "jdk" else "krogu" })
    check(rows.map { it[1] }.toSet()==wanted)
    for(id in wanted) check(rows.filter { it[1]==id }.map { it[2].toInt() }.toSet()==if(mode=="timing") (0..4).toSet() else setOf(0))
    check(rows.map { it[1] to it[2] }.toSet().size==rows.size)
    check(rows.all { it[3].toLong()>0 && it[4].toLong()>0 && it[5].toLong()>=0 && it[6].toLong()>=0 && it[8].toInt() in 0..15 && it[9].toInt()==
        (if(it[3].toLong()>=16) 65535 else (0 until it[3].toInt()).fold(0) { mask,i -> mask or (1 shl ((i+it[8].toInt()) and 15)) }) })
    if(mode=="profile") check(File(destination,"own-profile.jfr").isFile && File(destination,"attribution.csv").isFile)
    binding()
}
if(args[3]=="timings") {
    val eras=listOf("original527" to original,"post533" to post,"current" to current,"jdk" to current)
    for(fork in 0..2) for((era,jar) in if(fork%2==0) eras else eras.reversed()) execute(era,jar,fork,"timing","all")
    File(output,"COMPLETE.txt").writeText("PASS1200 rows,12 fresh processes,20 cases; profiles NOT executed by this phase\n")
} else {
    for((index,id) in priority.withIndex()) for(mode in if(index%2==0) listOf("profile-control","profile") else listOf("profile","profile-control"))
        execute("current",current,0,mode,id)
    File(output,"COMPLETE.txt").writeText("PASS8 own-code JFR profiles and8 separate overhead controls; sampled attribution remains estimated\n")
}
