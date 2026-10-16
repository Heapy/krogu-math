#!/usr/bin/env kotlin
// One bounded paired diagnostic of unchanged, assertion-bearing Kotlin/JS test callbacks.
// Usage: kotlinr scripts/check-primality-js-538.main.kts BASELINE_ROOT CANDIDATE_ROOT NODE OUTPUT
import java.io.File
import java.security.MessageDigest
import java.util.concurrent.TimeUnit

fun sha(file: File): String = MessageDigest.getInstance("SHA-256").digest(file.readBytes()).joinToString("") { "%02x".format(it) }
require(args.size == 4)
val roots = args.take(2).map { File(it).canonicalFile }
require(roots[0] != roots[1])
val node = File(args[2]).canonicalFile
val output = File(args[3]).canonicalFile
require(!output.exists())
check(node.isFile)
output.mkdirs()
val script = File("scripts/check-primality-js-538.main.kts").canonicalFile
check(script.isFile)
val fixtures = listOf("PrimeOracleTest.kt", "Jdk25PrimeResults.kt", "OddModulusContext538Test.kt", "Division530Test.kt")
val manifests = roots.map { root ->
    val files = File(root, "build/js/packages/krogu-math-test/kotlin").walkTopDown().filter { it.isFile }.toList()
    check(files.any { it.name == "krogu-math-test.js" })
    files + fixtures.map { File(root, "src/commonTest/kotlin/io/heapy/krogu/math/$it") } +
        File(root, "src/commonMain").walkTopDown().filter { it.isFile }.toList() +
        File(root, "build/js/node_modules/mocha").walkTopDown().filter { it.isFile }.toList()
}
val before = manifests.map { files -> files.associate { it.canonicalFile to sha(it) } }
for (name in fixtures) check(sha(File(roots[0], "src/commonTest/kotlin/io/heapy/krogu/math/$name")) ==
    sha(File(roots[1], "src/commonTest/kotlin/io/heapy/krogu/math/$name"))) { "Fixture differs: $name" }
check(sha(File(roots[0], "build/js/packages/krogu-math-test/kotlin/krogu-math.js")) !=
    sha(File(roots[1], "build/js/packages/krogu-math-test/kotlin/krogu-math.js")))
val nodeSha = sha(node)
val scriptSha = sha(script)
before.forEachIndexed { index, map -> File(output, "${if (index == 0) "baseline" else "candidate"}-before.sha256")
    .writeText(map.entries.sortedBy { it.key.path }.joinToString("\n", postfix = "\n") { "${it.value}  ${it.key}" }) }
File(output, "bindings.txt").writeText("node=$node\nnode_sha256=$nodeSha\nscript_sha256=$scriptSha\nchild_deadline_seconds=180\n" +
    "three_alternating_pairs=true\nallocation_metric=NA\ninternal_Random_Default_unseeded=true\n" +
    "fixed_inputs_and_expected_truth=unchanged_PrimeOracleTest_and_Jdk25PrimeResults_source_hashes\n")
val probe = File(output, "probe.cjs")
probe.writeText("""
'use strict';
const fs=require('node:fs'),path=require('node:path'),crypto=require('node:crypto');
const root=fs.realpathSync(process.argv[2]);
if(process.cwd()!==root) throw Error('cwd mismatch');
if(process.version!=='v24.16.0') throw Error('unexpected Node '+process.version);
const pkg=path.join(root,'build/js/packages/krogu-math-test');
const mochaPath=require.resolve('mocha',{paths:[pkg]});
const Mocha=require(mochaPath);
const mochaVersion=require(require.resolve('mocha/package.json',{paths:[pkg]})).version;
if(mochaVersion!=='11.7.6') throw Error('Mocha version '+mochaVersion);
const mocha=new Mocha({ui:'bdd'});
const bundle=path.join(pkg,'kotlin/krogu-math-test.js');
mocha.suite.emit('pre-require',global,bundle,mocha);
require(bundle);
const methods=[['PrimeOracleTest','successorsAt257Bits'],['PrimeOracleTest','successorsInLower256BitHalf'],
 ['PrimeOracleTest','successorsInUpper256BitHalf'],['PrimeOracleTest','successorsThrough255Bits'],
 ['OddModulusContext538Test','carryHeavyAndNearRadixPowers'],['Division530Test','literalMultiLimbSignedDivision']];
const titles=methods.map(x=>'io.heapy.krogu.math '+x.join(' '));
const selected=[];
mocha.suite.eachTest(t=>{if(titles.includes(t.fullTitle())) selected.push(t);});
if(selected.length!==6 || new Set(selected.map(t=>t.fullTitle())).size!==6) throw Error('selection '+selected.map(t=>t.fullTitle()));
for(const t of selected){
 if(t.pending || !t.fn || t.fn.length!==0 || t.fn.constructor.name==='AsyncFunction') throw Error('non synchronous fixture '+t.fullTitle());
 for(let s=t.parent;s;s=s.parent) for(const hook of ['_beforeAll','_beforeEach','_afterAll','_afterEach'])
  if(!Array.isArray(s[hook]) || s[hook].length!==0) throw Error('fixture lifecycle requires hooks: '+t.fullTitle()+' '+hook);
}
const hash=p=>crypto.createHash('sha256').update(fs.readFileSync(p)).digest('hex');
const loaded=Object.keys(require.cache).sort().map(p=>({path:p,sha256:hash(p)}));
const production=fs.realpathSync(path.join(pkg,'kotlin/krogu-math.js'));
if(!loaded.some(x=>fs.realpathSync(x.path)===production)) throw Error('selected production module not loaded');
fs.writeFileSync(process.argv[3]+'.modules.json',JSON.stringify({node:process.version,mocha:mochaVersion,titles,loaded},null,2));
let completed=0;
function invoke(t,n){for(let i=0;i<n;i++){const r=t.fn.call(t.ctx);if(r && typeof r.then==='function')throw Error('async result');completed++;}}
function batch(t,n){const cpu=process.cpuUsage(),start=process.hrtime.bigint();invoke(t,n);const ns=Number(process.hrtime.bigint()-start),c=process.cpuUsage(cpu);return {ns,cpu_ns:(c.user+c.system)*1000};}
console.log('title,sample,count,elapsed_ns,process_cpu_ns,ns_per_fixture,process_cpu_ns_per_fixture,completed_assertion_callbacks');
for(const title of titles){
 const t=selected.find(t=>t.fullTitle()===title);
 let warmNs=0,warmCalls=0;
 do {const x=batch(t,1);warmNs+=x.ns;warmCalls++;} while(warmNs<200000000 || warmCalls<2);
 let count=1;
 while(true){const x=batch(t,count);if(x.ns>=20000000 || count>=64)break;count*=2;}
 for(let sample=0;sample<3;sample++){const x=batch(t,count);console.log([title,sample,count,x.ns,x.cpu_ns,x.ns/count,x.cpu_ns/count,completed].join(','));}
}
for(const x of loaded)if(hash(x.path)!==x.sha256)throw Error('loaded module changed '+x.path);
if(JSON.stringify(Object.keys(require.cache).sort())!==JSON.stringify(loaded.map(x=>x.path)))throw Error('dependency set changed during callbacks');
fs.writeFileSync(process.argv[3]+'.complete.json',JSON.stringify({pass:true,completed,titles}));
""".trimIndent())
val probeSha = sha(probe)
for (fork in 0..2) for (index in if (fork % 2 == 0) listOf(0, 1) else listOf(1, 0)) {
    before.forEach { map -> check(map.all { (file, digest) -> sha(file) == digest }) }
    check(sha(node) == nodeSha && sha(script) == scriptSha && sha(probe) == probeSha)
    val label = "${if (index == 0) "baseline" else "candidate"}-$fork"
    val csv = File(output, "$label.csv")
    val builder = ProcessBuilder(node.path, "--max-old-space-size=512", probe.path, roots[index].path, csv.path)
        .directory(roots[index]).redirectOutput(csv).redirectError(File(output, "$label.stderr.txt"))
    builder.environment().clear()
    builder.environment().putAll(mapOf("TZ" to "UTC", "LANG" to "C", "LC_ALL" to "C"))
    println("launch $label")
    val process = builder.start()
    println("$label pid=${process.pid()}")
    if (!process.waitFor(180, TimeUnit.SECONDS)) {
        val handles = process.descendants().toList() + process.toHandle()
        handles.reversed().filter { it.isAlive }.forEach { it.destroy() }
        process.waitFor(3, TimeUnit.SECONDS)
        handles.filter { it.isAlive }.forEach { it.destroyForcibly() }
        val deadline = System.nanoTime() + TimeUnit.SECONDS.toNanos(5)
        for (handle in handles) if (handle.isAlive) {
            val left = deadline - System.nanoTime()
            if (left > 0) try { handle.onExit().get(left, TimeUnit.NANOSECONDS) }
            catch (_: java.util.concurrent.TimeoutException) { }
        }
        val survivors = handles.filter { it.isAlive }.map { it.pid() }
        File(output, "$label.timeout.txt").writeText("surviving_owned_pids=$survivors root_alive=${process.isAlive}\n")
        error("$label deadline; no comparative conclusion; survivors=$survivors")
    }
    check(process.exitValue() == 0) { "$label failed; retained original output, no automatic retry" }
    val rows = csv.readLines().drop(1).map { it.split(',') }
    check(rows.size == 18 && rows.all { it.size == 8 && it[2].toInt() in 1..64 && it[3].toLong() > 0 && it[4].toLong() >= 0 })
    check(rows.groupBy { it[0] }.let { it.size == 6 && it.values.all { group -> group.map { it[1].toInt() }.sorted() == listOf(0,1,2) } })
    check(File(csv.path + ".complete.json").isFile)
    before.forEach { map -> check(map.all { (file, digest) -> sha(file) == digest }) }
    check(sha(node) == nodeSha && sha(script) == scriptSha && sha(probe) == probeSha)
    println("$label PASS 18 rows")
}
File(output, "COMPLETE.txt").writeText("PASS six processes / six exact original assertion callbacks / 108 samples; fixture diagnostic, not operation throughput\nprobe_sha256=$probeSha\n")
