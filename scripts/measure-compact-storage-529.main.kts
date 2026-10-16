#!/usr/bin/env kotlinr
// Source-only prepared diagnostic. Run only in the coordinated numerical slot.
import java.io.File
import java.security.MessageDigest
import java.util.Base64
import java.util.concurrent.TimeUnit
import java.util.jar.Attributes
import java.util.jar.JarEntry
import java.util.jar.JarOutputStream
import java.util.jar.Manifest
import javax.tools.ToolProvider

require(args.size == 5) { "baseline.jar candidate.jar NEW-output-directory baseline-sha256 candidate-sha256" }
check(KotlinVersion.CURRENT.toString() == "2.4.21")
check(System.getProperty("java.vendor") == "BellSoft" && System.getProperty("java.runtime.version") == "25.0.4+9-LTS")
fun sha(file: File) = MessageDigest.getInstance("SHA-256").digest(file.readBytes()).joinToString("") { "%02x".format(it) }
val artifacts = args.take(2).map { File(it).canonicalFile }
val expected = args.drop(3)
check(expected.all { it.matches(Regex("[0-9a-f]{64}")) } && expected.distinct().size == 2)
check(artifacts.all { it.isFile } && artifacts.distinct().size == 2)
artifacts.indices.forEach { check(sha(artifacts[it]) == expected[it]) }
val source = File("scripts/measure-compact-storage-529.main.kts").canonicalFile
check(source.isFile) { "Run from the task worktree root" }
val sourceSha = sha(source)
val output = File(args[2]).canonicalFile
val permitted = File("../").canonicalFile // .codex-work/529 when invoked from its worktree.
check(permitted.name == "529" && output.toPath().startsWith(permitted.toPath()))
check(!output.exists() && output.mkdirs()) { "Output directory must be new" }
val classes = File(output, "adapter").apply { check(mkdir()) }
val javaSource = File(classes, "Compact529Probe.java")
javaSource.writeText("""
import io.heapy.krogu.math.*;
import java.io.*;
import java.lang.instrument.Instrumentation;
import java.lang.management.ManagementFactory;
import java.lang.reflect.*;
import java.nio.file.*;
import java.security.MessageDigest;
import java.util.*;
import java.util.function.IntFunction;

public final class Compact529Probe {
    private static Instrumentation instrumentation;
    private static volatile Object[] ring = new Object[256];
    public static void premain(String ignored, Instrumentation value) { instrumentation = value; }
    private record Case(String name, IntFunction<Object> run) {}
    private static String sha(Path path) throws Exception {
        return HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256").digest(Files.readAllBytes(path)));
    }
    private static String representation(Object value) {
        if (value instanceof BigDecimal decimal) return decimal.unscaledValue().toString()+"@"+decimal.scale();
        return value.toString();
    }
    private static List<Case> cases() {
        long[] small = new long[16];
        String[] integerText = new String[16];
        String[] decimalText = new String[16];
        BigInteger[] a = new BigInteger[16], b = new BigInteger[16], wide = new BigInteger[16];
        BigDecimal[] d = new BigDecimal[16], e = new BigDecimal[16], dw = new BigDecimal[16];
        for (int i=0;i<16;i++) {
            small[i]=12345000001L+i; integerText[i]=Long.toString(small[i]); decimalText[i]="123450000."+(11+i);
            a[i]=BigInteger.valueOf(small[i]); b[i]=BigInteger.valueOf(123L+i);
            wide[i]=new BigInteger("123456789".repeat(16)+(i+11));
            d[i]=BigDecimal.valueOf(small[i],2); e[i]=BigDecimal.valueOf(123L+i,2); dw[i]=new BigDecimal(wide[i],2);
        }
        MathContext mc = new MathContext(7,RoundingMode.HALF_EVEN);
        MathContext mc34 = new MathContext(34,RoundingMode.HALF_EVEN);
        BigDecimal[] original527A=new BigDecimal[16], original527B=new BigDecimal[16];
        for(int i=0;i<16;i++) {
            original527A[i]=new BigDecimal((123+i)+".45");
            original527B[i]=new BigDecimal((11+i)+".37");
        }
        List<Case> result=new ArrayList<>();
        result.add(new Case("integer/create-small", i->BigInteger.valueOf(small[i&15])));
        result.add(new Case("integer/create-min", i->BigInteger.valueOf(Long.MIN_VALUE+(i&15))));
        result.add(new Case("integer/parse-small", i->new BigInteger(integerText[i&15])));
        result.add(new Case("integer/add-small", i->a[i&15].add(b[i&15])));
        result.add(new Case("integer/subtract-small", i->a[i&15].subtract(b[i&15])));
        result.add(new Case("integer/multiply-small", i->a[i&15].multiply(b[i&15])));
        BigInteger max=BigInteger.valueOf(Long.MAX_VALUE), minus=BigInteger.valueOf(-1L), min=BigInteger.valueOf(Long.MIN_VALUE);
        result.add(new Case("integer/promote", i->max.add(b[i&15])));
        BigInteger above=new BigInteger("9223372036854775808");
        result.add(new Case("integer/demote", i->above.subtract(b[i&15])));
        result.add(new Case("integer/divide-min-overflow", i->min.divide(minus)));
        result.add(new Case("integer/metadata", i->a[i&15].bitLength()));
        result.add(new Case("integer/wide-add", i->wide[i&15].add(wide[(i+1)&15])));
        result.add(new Case("integer/wide-multiply", i->wide[i&15].multiply(wide[(i+1)&15])));
        result.add(new Case("integer/wide-divide", i->wide[i&15].divide(a[i&15])));
        result.add(new Case("decimal/create-small", i->BigDecimal.valueOf(small[i&15],2)));
        result.add(new Case("decimal/create-min", i->BigDecimal.valueOf(Long.MIN_VALUE+(i&15),2)));
        result.add(new Case("decimal/parse-small", i->new BigDecimal(decimalText[i&15])));
        result.add(new Case("decimal/parse-rounded", i->new BigDecimal(decimalText[i&15],mc)));
        result.add(new Case("decimal/add-small", i->d[i&15].add(e[i&15])));
        result.add(new Case("decimal/subtract-small", i->d[i&15].subtract(e[i&15])));
        result.add(new Case("decimal/multiply-small", i->d[i&15].multiply(e[i&15])));
        result.add(new Case("decimal/divide-context", i->d[i&15].divide(e[i&15],mc)));
        result.add(new Case("decimal/original527-mc34-divide", i->original527A[i&15].divide(original527B[i&15],mc34)));
        result.add(new Case("decimal/accumulator-compact-fresh", i->{
            DecimalAccumulator sum=new DecimalAccumulator(MathContext.UNLIMITED,DecimalRounding.FINAL_RESULT,null,null);
            return sum.add(d[i&15]).add(e[i&15]).snapshot();
        }));
        result.add(new Case("decimal/divide-scaled", i->d[i&15].divide(e[i&15],4,RoundingMode.HALF_EVEN)));
        result.add(new Case("decimal/round", i->d[i&15].round(mc)));
        result.add(new Case("decimal/setScale", i->d[i&15].setScale(1,RoundingMode.HALF_EVEN)));
        BigDecimal tail=BigDecimal.valueOf(1234500000000L,7);
        result.add(new Case("decimal/strip", i->tail.stripTrailingZeros()));
        result.add(new Case("decimal/metadata", i->d[i&15].precision()));
        result.add(new Case("decimal/wide-create", i->new BigDecimal(wide[i&15],2)));
        result.add(new Case("decimal/wide-add", i->dw[i&15].add(dw[(i+1)&15])));
        result.add(new Case("decimal/wide-multiply", i->dw[i&15].multiply(dw[(i+1)&15],mc)));
        result.add(new Case("decimal/wide-divide", i->dw[i&15].divide(e[i&15],mc)));
        result.add(new Case("decimal/wide-round", i->dw[i&15].round(mc)));
        return result;
    }
    private static void graph(String name, Object root) throws Exception {
        IdentityHashMap<Object,Boolean> seen=new IdentityHashMap<>();
        ArrayDeque<Object> pending=new ArrayDeque<>(); pending.add(root);
        long total=0, arrays=0, arrayBytes=0;
        TreeMap<String,Integer> types=new TreeMap<>();
        while (!pending.isEmpty()) {
            Object value=pending.removeLast(); if (seen.put(value,true)!=null) continue;
            long bytes=instrumentation.getObjectSize(value); total+=bytes;
            Class<?> type=value.getClass(); types.merge(type.getName(),1,Integer::sum);
            if (type.isArray()) {
                arrays++; arrayBytes+=bytes;
                if (!type.getComponentType().isPrimitive()) for (Object item:(Object[])value) if(item!=null) pending.add(item);
            } else {
                if (!type.getName().startsWith("io.heapy.krogu.math.")) throw new AssertionError("Unexpected graph object "+type);
                for (Class<?> owner=type;owner!=null;owner=owner.getSuperclass())
                    for(Field field:owner.getDeclaredFields()) if(!Modifier.isStatic(field.getModifiers())&&!field.getType().isPrimitive()) {
                        field.setAccessible(true); Object item=field.get(value); if(item!=null) pending.add(item);
                    }
            }
        }
        System.out.println("GRAPH,"+name+","+instrumentation.getObjectSize(root)+","+total+","+seen.size()+","+arrays+","+arrayBytes+","+types.toString().replace(',',';'));
    }
    private static long batch(Case row,int count,int offset) {
        long start=System.nanoTime();
        for(int i=0;i<count;i++) ring[i&255]=row.run.apply(i+offset);
        return System.nanoTime()-start;
    }
    public static void main(String[] args) throws Exception {
        if(args.length!=6) throw new AssertionError("artifact sha probe sha label check|measure");
        Path artifact=Path.of(args[0]).toRealPath(), probe=Path.of(args[2]).toRealPath();
        if(!sha(artifact).equals(args[1])||!sha(probe).equals(args[3])||instrumentation==null) throw new AssertionError("input identity");
        if(!System.getProperty("java.vendor").equals("BellSoft")||!System.getProperty("java.runtime.version").equals("25.0.4+9-LTS"))throw new AssertionError("runtime");
        for(Class<?> type:List.of(BigInteger.class,BigDecimal.class,MathContext.class,RoundingMode.class))
            if(!Path.of(type.getProtectionDomain().getCodeSource().getLocation().toURI()).toRealPath().equals(artifact))throw new AssertionError("artifact origin");
        if(!Path.of(Compact529Probe.class.getProtectionDomain().getCodeSource().getLocation().toURI()).toRealPath().equals(probe))throw new AssertionError("probe origin");
        if(!Set.of("baseline","candidate").contains(args[4])||!Set.of("check","measure").contains(args[5]))throw new AssertionError("mode");
        List<String> flags=ManagementFactory.getRuntimeMXBean().getInputArguments();
        if(!flags.equals(List.of("-Xms256m","-Xmx512m","-XX:+UseG1GC","-javaagent:"+probe)))throw new AssertionError("JVM flags: "+flags);
        if(Runtime.getRuntime().maxMemory()!=536870912L)throw new AssertionError("heap configuration");
        System.out.println("FLAGS,"+Base64.getEncoder().encodeToString(flags.toString().getBytes(java.nio.charset.StandardCharsets.UTF_8))+","+Runtime.getRuntime().maxMemory()+","+ManagementFactory.getGarbageCollectorMXBeans().stream().map(java.lang.management.GarbageCollectorMXBean::getName).toList().toString().replace(',',';'));
        System.out.println("META,"+args[4]+","+args[5]+","+args[1]+","+args[3]+","+System.getProperty("os.arch"));
        List<Case> cases=cases(); if(cases.size()!=33||cases.stream().map(Case::name).distinct().count()!=33)throw new AssertionError("inventory");
        for(Case row:cases)for(int i=0;i<16;i++)System.out.println("CHECK,"+row.name+","+i+","+Base64.getEncoder().encodeToString(representation(row.run.apply(i)).getBytes(java.nio.charset.StandardCharsets.UTF_8)));
        if(args[5].equals("check")) {
            graph("integer/small",BigInteger.valueOf(12345000001L));
            graph("integer/min",BigInteger.valueOf(Long.MIN_VALUE));
            graph("integer/max",BigInteger.valueOf(Long.MAX_VALUE));
            graph("integer/wide",new BigInteger("123456789".repeat(16)));
            graph("decimal/small",BigDecimal.valueOf(12345000001L,2));
            graph("decimal/min",BigDecimal.valueOf(Long.MIN_VALUE,2));
            graph("decimal/wide",new BigDecimal(new BigInteger("123456789".repeat(16)),2));
        } else {
            com.sun.management.ThreadMXBean bean=(com.sun.management.ThreadMXBean)ManagementFactory.getThreadMXBean();
            if(!bean.isThreadAllocatedMemorySupported())throw new AssertionError("allocation unavailable");
            bean.setThreadAllocatedMemoryEnabled(true); long thread=Thread.currentThread().threadId();
            for(Case row:cases) {
                long until=System.nanoTime()+200_000_000L; int offset=0;
                do{ring[offset&255]=row.run.apply(offset++);}while(System.nanoTime()<until);
                int count=1;while(batch(row,count,offset)<20_000_000L&&count<8_388_608)count*=2;
                for(int warm=0;warm<3;warm++)batch(row,count,offset+warm);
                for(int sample=0;sample<7;sample++) {
                    long before=bean.getThreadAllocatedBytes(thread);long elapsed=batch(row,count,offset+sample);
                    long allocated=bean.getThreadAllocatedBytes(thread)-before;
                    int digest=1;for(Object value:ring)digest=31*digest+value.hashCode();
                    System.out.println("TIME,"+row.name+","+sample+","+count+","+elapsed+","+(double)allocated/count+","+digest);
                }
            }
        }
        if(!sha(artifact).equals(args[1])||!sha(probe).equals(args[3]))throw new AssertionError("identity changed");
        System.out.println("PASS");
    }
}
""".trimIndent())
val stdlib = File(kotlin.Unit::class.java.protectionDomain.codeSource.location.toURI()).canonicalFile
check(ToolProvider.getSystemJavaCompiler().run(null, System.out, System.err, "--release", "25", "-classpath",
    artifacts[1].path + File.pathSeparator + stdlib.path, "-d", classes.path, javaSource.path) == 0)
val manifest = Manifest().apply {
    mainAttributes[Attributes.Name.MANIFEST_VERSION] = "1.0"
    mainAttributes.putValue("Premain-Class", "Compact529Probe")
}
val probe = File(output, "probe.jar")
JarOutputStream(probe.outputStream(), manifest).use { jar ->
    classes.walkTopDown().filter { it.isFile && it.extension == "class" }.sortedBy { it.path }.forEach { file ->
        jar.putNextEntry(JarEntry(file.relativeTo(classes).invariantSeparatorsPath)); jar.write(file.readBytes()); jar.closeEntry()
    }
}
val probeSha = sha(probe)
File(output, "input-manifest.txt").writeText(buildString {
    appendLine("script ${source.path} $sourceSha"); appendLine("adapter-java ${sha(javaSource)}"); appendLine("probe ${probe.path} $probeSha")
    artifacts.indices.forEach { appendLine("${listOf("baseline", "candidate")[it]} ${artifacts[it].path} ${expected[it]}") }
    appendLine("JDK=${System.getProperty("java.runtime.version")};arch=${System.getProperty("os.arch")};stdlib=${stdlib.path};stdlib_sha=${sha(stdlib)}")
    appendLine("Exact JVM Instrumentation graph is uncached numeric root + owned nonstatic referenced objects, excluding class/static caches.")
    appendLine("Timing includes direct Java lambda dispatch, loop and escaping result ring; compile/setup/checks excluded. 200ms uniform prewarm,20ms adaptive batches,3warm,7samples.")
})
val java = File(System.getProperty("java.home"), "bin/java").canonicalFile
fun child(index: Int, mode: String, fork: Int): File {
    check(sha(source) == sourceSha && sha(probe) == probeSha && sha(artifacts[index]) == expected[index])
    val label = listOf("baseline", "candidate")[index]
    val log = File(output, "$mode-$fork-$label.log")
    val command = listOf(java.path, "-Xms256m", "-Xmx512m", "-XX:+UseG1GC", "-javaagent:${probe.path}", "-cp",
        listOf(probe, artifacts[index], stdlib).joinToString(File.pathSeparator) { it.path }, "Compact529Probe",
        artifacts[index].path, expected[index], probe.path, probeSha, label, mode)
    File(output, "$mode-$fork-$label-argv.txt").writeText(command.mapIndexed { position, value -> "$position=$value" }.joinToString("\n", postfix = "\n"))
    val builder = ProcessBuilder(command).redirectErrorStream(true).redirectOutput(log)
    listOf("JAVA_TOOL_OPTIONS", "JDK_JAVA_OPTIONS", "_JAVA_OPTIONS").forEach { builder.environment().remove(it) }
    val process = builder.start()
    if (!process.waitFor(180, TimeUnit.SECONDS)) {
        process.destroy(); if (!process.waitFor(5, TimeUnit.SECONDS)) process.destroyForcibly()
        check(process.waitFor(5, TimeUnit.SECONDS)) { "Owned child survived cleanup: ${process.pid()}" }
        error("Owned child deadline: $label/$mode/$fork; raw log retained")
    }
    check(process.exitValue() == 0 && log.readLines().lastOrNull() == "PASS") { "Failed child: ${log.path}" }
    check(sha(source) == sourceSha && sha(probe) == probeSha && sha(artifacts[index]) == expected[index])
    return log
}
val checks = artifacts.indices.map { index -> child(index, "check", 0).readLines().filter { it.startsWith("CHECK,") } }
check(checks[0].size == 33 * 16 && checks[0] == checks[1]) { "Full result representation differs before measurement" }
val checkedCases = checks[0].map { it.split(',') }.groupBy { it[1] }
check(checkedCases.size == 33 && checkedCases.values.all { rows -> rows.map { it[2].toInt() }.sorted() == (0..15).toList() })
for (fork in 0..2) for (index in if (fork % 2 == 0) listOf(0, 1) else listOf(1, 0)) {
    val rows = child(index, "measure", fork).readLines()
    check(rows.filter { it.startsWith("CHECK,") } == checks[0])
    val timed = rows.filter { it.startsWith("TIME,") }.map { it.split(',') }
    check(timed.size == 33 * 7 && timed.all { row ->
        row.size == 7 && row[3].toIntOrNull()?.let { it in 1..8_388_608 && it and (it - 1) == 0 } == true &&
            row[4].toLongOrNull()?.let { it in 1..180_000_000_000L } == true &&
            row[5].toDoubleOrNull()?.let { it.isFinite() && it >= 0.0 } == true && row[6].toIntOrNull() != null
    })
    check(timed.map { it[1] }.toSet() == checkedCases.keys)
    check(timed.groupBy { it[1] }.values.all { group -> group.map { it[2].toInt() }.sorted() == (0..6).toList() })
}
File(output, "PASS").writeText("All input identities, origin checks,33×16 full representations and6 fresh-process sample inventories passed.\n")
println("PASS ${output.path}")
