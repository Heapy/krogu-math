#!/usr/bin/env kotlinr

/** Unrun isolated JS/Wasm common-kernel worker experiment. No production backend. */
import java.io.File
import java.security.MessageDigest
import java.util.concurrent.TimeUnit
import java.nio.file.Files

require(args.size == 10) { "node artifactDirectory entryRelative artifactSHA256Manifest newOutputDirectory js|wasm forward|reverse samples rootArtifactDirectory rootManifestSHA256" }
val node = File(args[0]).canonicalFile.also { require(it.isFile && it.canExecute()) }
val directory = File(args[1]).canonicalFile.also { require(it.isDirectory) }
val entryRelative = args[2].also { require(!it.startsWith('/') && it.split('/').none { p -> p in listOf("", ".", "..") }) }
val entry = File(directory, entryRelative).canonicalFile.also { require(it.isFile && it.toPath().startsWith(directory.toPath())) }
val manifest = File(args[3]).canonicalFile
val output = File(args[4]).canonicalFile.also { require(!it.exists()) }
val kind = args[5].also { require(it in listOf("js", "wasm")) }
val order = args[6].also { require(it in listOf("forward", "reverse")) }
val samples = args[7].toInt().also { require(it in 5..15) }
val rootDirectory = File(args[8]).canonicalFile.also { require(it.isDirectory) }
val rootManifestSha = args[9].also { require(it.matches(Regex("[a-f0-9]{64}"))) }
val rootManifest = File(rootDirectory, "manifest.sha256").also { require(it.isFile && !Files.isSymbolicLink(it.toPath())) }
fun sha(file: File): String = MessageDigest.getInstance("SHA-256").digest(file.readBytes()).joinToString("") { "%02x".format(it) }
val manifestLines = manifest.readLines()
val expected = manifestLines.associate { line ->
    require(line.matches(Regex("[a-f0-9]{64}  .+")))
    val relative = line.substring(66)
    require(!relative.startsWith('/') && relative.split('/').none { it in listOf("", ".", "..") })
    relative to line.substring(0, 64)
}
require(expected.isNotEmpty() && expected.size == manifestLines.size)
require(expected.keys.any { it.endsWith(".wasm") } == (kind == "wasm"))
val source = File("scripts/measure-worker-transport-539.main.kts").canonicalFile.also { require(it.isFile) }
val rootExpected = rootManifest.readLines().associate { line ->
    val parts = line.split('\t'); require(parts.size == 2 && parts[0].matches(Regex("[a-f0-9]{64}")))
    val relative = parts[1]; require(!relative.startsWith('/') && relative.split('/').none { it in listOf("", ".", "..") })
    relative to parts[0]
}.also { require(it.isNotEmpty() && it.size == rootManifest.readLines().size) }
fun verifyRoot() {
    check(sha(rootManifest) == rootManifestSha)
    check(rootDirectory.walkTopDown().filter { it.isFile }.sumOf { it.length() } <= 512L * 1024 * 1024)
    val actual = rootDirectory.walkTopDown().onEnter { check(!Files.isSymbolicLink(it.toPath())); true }
        .filter { it.isFile && it != rootManifest }.associate { file ->
            check(!Files.isSymbolicLink(file.toPath()) && file.canonicalFile.toPath().startsWith(rootDirectory.toPath()))
            file.relativeTo(rootDirectory).invariantSeparatorsPath to sha(file)
        }
    check(actual == rootExpected) { "Parent root probe closure drift" }
}
verifyRoot()
val sourceSha = sha(source)
val nodeSha = sha(node)
val manifestSha = sha(manifest)
fun verify() {
    verifyRoot()
    check(sha(node) == nodeSha && sha(manifest) == manifestSha && sha(source) == sourceSha)
    val files = directory.walkTopDown().onEnter { check(!Files.isSymbolicLink(it.toPath())); true }.filter { it.isFile }.toList()
    check(files.sumOf { it.length() } <= 512L * 1024 * 1024) { "Owned artifact package exceeds512MiB" }
    val actual = files.associate { file ->
        check(!Files.isSymbolicLink(file.toPath()))
        check(file.canonicalFile.toPath().startsWith(directory.toPath()))
        file.relativeTo(directory).invariantSeparatorsPath to sha(file)
    }
    check(actual == expected) { "Artifact tree drift" }
}
verify()
require(output.mkdirs())
val host = File(output, "owned-host.mjs")
host.writeText("""
import { Worker, MessageChannel, receiveMessageOnPort, isMainThread } from 'node:worker_threads';
import { availableParallelism } from 'node:os';
import { pathToFileURL } from 'node:url';
import { performance } from 'node:perf_hooks';
import fs from 'node:fs';
import path from 'node:path';
import {createRequire} from 'node:module';
const [entry, kind, order, samplesText, rootDirectoryArg] = process.argv.slice(2);
const samples = Number(samplesText);
const entryURL = pathToFileURL(entry).href;
if (process.version !== 'v24.16.0') throw Error('Pinned Node v24.16.0 required');
const started = performance.now();
await import(entryURL);
if (typeof globalThis.__krogu539Invoke !== 'function') throw Error('Private diagnostic callback not registered');
const originalTinyInvoke=globalThis.__krogu539Invoke;
console.log('MODULE539,parent_import_ms,' + (performance.now() - started));
const rootDirectory=fs.realpathSync(rootDirectoryArg);
const rootRequire=createRequire(path.join(rootDirectory,'mocha','package.json'));
function rootPath(relative) {
  if(path.isAbsolute(relative)||relative.split('/').some(p=>p===''||p==='.'||p==='..')) throw Error('Invalid parent artifact path');
  const resolved=fs.realpathSync(path.join(rootDirectory,relative));
  if(!resolved.startsWith(rootDirectory+path.sep)) throw Error('Parent origin outside bound tree');
  return resolved;
}
function verifyRootCache(phase) {
  let count=0;
  for(const filename of Object.keys(rootRequire.cache)) {
    const real=fs.realpathSync(filename);
    if(!real.startsWith(rootDirectory+path.sep) && !real.startsWith(fs.realpathSync(path.dirname(entry))+path.sep)) throw Error('Parent CJS outside both bound closures: '+real);
    count++;
  }
  console.log('ROOTCACHE539,'+phase+','+count+',owned');
}
const rootStarted=performance.now();
const rootEntry=rootPath(fs.readFileSync(rootPath('entry.txt'),'utf8').trim());
const mochaEntry=rootPath('mocha/node_modules/mocha/index.js');
const Mocha=rootRequire(mochaEntry);
const mocha=new Mocha({ui:'bdd',reporter:'dot',timeout:0,grep:/MultiplicationCalibration539.*optionalCampaign/});
mocha.addFile(rootEntry);
await mocha.loadFilesAsync();
if(kind==='wasm') {
  const module=await import(pathToFileURL(rootEntry).href);
  if(typeof module.startUnitTests!=='function') throw Error('Missing parent Wasm startUnitTests');
  if(globalThis.kotlinTest===undefined) globalThis.kotlinTest={adapterTransformer:adapter=>adapter};
  module.startUnitTests();
}
const selected=[];
mocha.suite.eachTest(test=>{
  if(!/MultiplicationCalibration539.*optionalCampaign/.test(test.fullTitle())) return;
  selected.push(test.fullTitle());
  for(let suite=test.parent;suite!==undefined&&suite!==null;suite=suite.parent) {
    for(const name of ['_beforeAll','_beforeEach','_afterEach','_afterAll']) {
      if(!Array.isArray(suite[name])||suite[name].length!==0) throw Error('Parent registration must have no inherited hooks: '+name);
    }
  }
});
if(selected.length!==1) throw Error('Expected one parent registration test');
verifyRootCache('before-register');
await new Promise((resolve,reject)=>{
  const runner=mocha.run(failures=>{
    const stats=runner.stats;
    if(failures!==0||stats.tests!==1||stats.passes!==1||stats.pending!==0) reject(Error('Parent registration test failed'));
    else resolve();
  });
});
verifyRootCache('after-register');
if(globalThis.__krogu539Invoke!==originalTinyInvoke) throw Error('Root replaced tiny callback');
if(typeof globalThis.__krogu539PublicInvoke!=='function') throw Error('Missing parent public callback');
console.log('ROOTMODULE539,parent_load_register_ms,'+(performance.now()-rootStarted));
console.log('ROOTORIGIN539,'+rootEntry+','+mochaEntry);

const workerSource = `
const { workerData } = require('node:worker_threads');
const { port, shared, entryURL } = workerData;
const status = new Int32Array(shared);
function fail(id, error) {
  port.postMessage({id, error: String(error && error.message || error)});
  Atomics.store(status, 0, 3); Atomics.notify(status, 0);
}
(async () => {
  await import(entryURL);
  if (typeof globalThis.__krogu539Invoke !== 'function') throw Error('Missing worker callback');
  port.on('message', job => {
    try {
      if (!Number.isSafeInteger(job.id)) throw Error('Invalid job id');
      let result;
      if (job.mode === 'empty') result = new Int32Array(0);
      else if (job.mode === 'copy') result = job.inputs[0].slice();
      else {
        globalThis.__krogu539Request = job;
        globalThis.__krogu539Invoke(4);
        result = globalThis.__krogu539Result[0];
      }
      if (!(result instanceof Int32Array)) throw Error('Result must be owned Int32Array');
      port.postMessage({id: job.id, result}, [result.buffer]);
      Atomics.store(status, 0, 2); Atomics.notify(status, 0);
    } catch (error) { fail(job.id, error); }
    finally { globalThis.__krogu539Request = null; globalThis.__krogu539Result = null; }
  });
  Atomics.store(status, 0, 1); Atomics.notify(status, 0);
})().catch(error => fail(-1, error));
`;
const deadline = () => performance.now() + 20000;
class Slot {
  constructor() {
    this.port = null; this.childPort = null; this.worker = null;
    this.shared = new SharedArrayBuffer(4);
    this.status = new Int32Array(this.shared);
    this.next = 0; this.pending = null;
    this.failure = null;
  }
  start() {
    const channel = new MessageChannel();
    this.port = channel.port1; this.childPort = channel.port2;
    this.worker = new Worker(workerSource, {eval: true, workerData: {port: this.childPort, shared: this.shared, entryURL},
      transferList: [this.childPort], env: {TZ:'UTC', LANG:'C', LC_ALL:'C'}, execArgv: [],
      resourceLimits: {maxOldGenerationSizeMb:128, maxYoungGenerationSizeMb:16, stackSizeMb:4}});
    this.worker.on('error', error => { this.failure = error; });
    // Async errors are observed after parent unblocks; shared status plus deadline
    // still controls synchronous waits, never the parent event-loop callback.
  }
  ready() {
    const until = deadline();
    while (Atomics.load(this.status,0) === 0) {
      if (performance.now() > until) throw Error('Worker startup timeout');
      Atomics.wait(this.status,0,0,10);
    }
    if (Atomics.load(this.status,0) !== 1) throw Error('Worker initialization failed');
  }
  submit(job, transfer = []) {
    if (this.pending !== null || Atomics.load(this.status,0) !== 1) throw Error('Queue is bounded to one owned request');
    const id = ++this.next;
    this.port.postMessage({...job,id}, transfer);
    this.pending = id;
  }
  join() {
    const id = this.pending;
    if (id === null) throw Error('No owned request');
    const until = deadline();
    while (![2,3].includes(Atomics.load(this.status,0))) {
      if (performance.now() > until) throw Error('Worker computation timeout');
      Atomics.wait(this.status,0,1,10);
    }
    let message;
    while (!(message = receiveMessageOnPort(this.port)?.message)) {
      if (performance.now() > until) throw Error('Worker result transport timeout');
      // Parent event-loop callbacks are NOT needed while this synchronous wait blocks.
      Atomics.wait(this.status,0,Atomics.load(this.status,0),1);
    }
    this.pending = null;
    if (message.id !== id) throw Error('Response identity mismatch');
    if (message.error !== undefined) throw Error(message.error);
    if (!(message.result instanceof Int32Array)) throw Error('Invalid transported result');
    Atomics.store(this.status,0,1);
    return message.result;
  }
  async close() { this.port?.close(); this.childPort?.close(); if(this.worker!==null) await this.worker.terminate(); }
}
const slots = [null,null];
let slotCount = 0;
let busy = false;
let escape = null;
function invoke(mode, inputs, policy, same=false, child=0) {
  globalThis.__krogu539Request = {inputs,policy,same,child};
  globalThis.__krogu539Invoke(mode);
  return globalThis.__krogu539Result;
}
function parallel(a,b,policy,same,workers) {
  if (busy || !isMainThread || availableParallelism() <= workers) return invoke(0,[a,b],policy,same)[0];
  busy = true;
  const accepted = [false,false];
  const values = [null,null,null];
  let primary = null;
  try {
    try {
      const jobs = invoke(1,[a,b],policy,same);
      for (let i=0;i<workers;i++) {
        const inputs = [jobs[2*i],jobs[2*i+1]];
        const transfer = [...new Set(inputs.map(x => x.buffer))];
        slots[i].submit({mode:'kernel',inputs,policy,same:jobs[4][i] === 1}, transfer);
        accepted[i] = true; // Fixed nonallocating owner store after accepted postMessage.
      }
      for (let i=workers;i<3;i++) values[i] = invoke(2,[],policy,false,i)[0];
    } catch (error) { primary = error; }
    finally {
      for (let i=0;i<workers;i++) {
        if (!accepted[i]) continue;
        try { values[i] = slots[i].join(); }
        catch (error) { if (primary === null) primary = error; }
      }
    }
    if (primary !== null) throw primary;
    return invoke(3,values,policy)[0];
  } finally {
    invoke(5,[],policy);
    busy = false;
    globalThis.__krogu539Request = null;
    globalThis.__krogu539Result = null;
  }
}
function reference(words) { let n=0n; for(let i=words.length-1;i>=0;i--) n=(n<<30n)+BigInt(words[i]); return n; }
function oracleWords(value) { const words=[]; do { words.push(Number(value&0x3fffffffn)); value >>=30n; } while(value!==0n); return Int32Array.from(words); }
function verifyPublic() { globalThis.__krogu539PublicInvoke(2); if(!Number.isInteger(globalThis.__krogu539PublicHash)) throw Error('Missing verified public hash'); }
function equal(words, expected) { if(reference(words)!==expected) throw Error('Public BigInt oracle mismatch'); }
function batch(operation,count) { const begin=process.hrtime.bigint(); for(let i=0;i<count;i++) escape=operation(); return Number(process.hrtime.bigint()-begin); }
function reverse(values) { return order==='reverse' ? values.reverse() : values; }
let campaignFailure=null;
try {
  const cpus = availableParallelism();
  console.log('# worker539 kind='+kind+' node='+process.version+' processors='+cpus+' order='+order+' samples='+samples+' pid='+process.pid+' start_ms='+Date.now());
  console.log('# v8='+process.versions.v8+' platform='+process.platform+' arch='+process.arch);
  console.log('# CPU=whole_process heap=parent_realm_only RSS=whole_process_including_workers_and_reference_preparation allocation=NA');
  const beforePoolRSS=process.memoryUsage().rss;
  const startup=process.hrtime.bigint();
  // At most two additional worker threads; no worker queue receives two active jobs.
  for(let i=0;i<Math.min(2,cpus-1);i++) { const slot=new Slot(); slots[i]=slot; slotCount++; slot.start(); slot.ready(); }
  console.log('STARTUP539,'+slotCount+','+Number(process.hrtime.bigint()-startup));
  console.log('POOL_RESOURCE539,'+beforePoolRSS+','+process.memoryUsage().rss+','+slotCount);
  console.log('# per_case_RSS_includes_idle_pool_even_for_serial_variants; POOL_RESOURCE539_is_raw_startup_control_not_exact_retained_size');
  const policy=[64,16,384];
  for(const size of reverse([256,1024,4096,8192,16384])) {
    const a=Int32Array.from({length:size},(_,i)=>(i*104729+31)&0x3fffffff);
    for(const shape of reverse(['distinct','square','ratio2'])) {
      const b=shape==='square'?a:shape==='ratio2'?new Int32Array(2*size).fill(0x3fffffff):a.slice();
      const expected=reference(a)*reference(b);
      const originalA=a.slice(),originalB=b.slice();
      globalThis.__krogu539PublicRequest={inputs:[a,b,oracleWords(expected),Int32Array.from(policy)],same:shape==='square'};
      globalThis.__krogu539PublicInvoke(0); globalThis.__krogu539PublicRequest=null;
      const variants=reverse(['kernel','frame','public','empty','copy','worker1','worker2']);
      for(const variant of variants) {
        const workers=variant==='worker1'?1:variant==='worker2'?2:0;
        const id=size+'/'+shape+'/'+variant;
        if((workers>slotCount)||((variant==='empty'||variant==='copy')&&slotCount===0)) { console.log('INELIGIBLE_WORKER539,'+id); continue; }
        const operation=variant==='public'?()=>globalThis.__krogu539PublicInvoke(1):variant==='kernel'?()=>invoke(0,[a,b],policy,shape==='square')[0]:
          variant==='frame'?()=>invoke(6,[a,b],policy,shape==='square')[0]:
          variant==='empty'||variant==='copy'?()=>{ slots[0].submit({mode:variant,inputs:variant==='empty'?[]:[a,b],policy,same:false}); return slots[0].join(); }:
          ()=>parallel(a,b,policy,shape==='square',workers);
        const check=()=>{ const result=operation(); if(variant==='public') verifyPublic();
          else if(variant==='empty') {if(result.length!==0) throw Error('Empty transport result');}
          else if(variant==='copy') equal(result,reference(a)); else equal(result,expected); };
        check(); console.log('CHECKWORKER539,'+id+',PASS');
        const warm=performance.now(); while(performance.now()-warm<200) escape=operation();
        let count=1; while(batch(operation,count)<20000000 && count<1048576) count*=2;
        for(let i=0;i<3;i++) batch(operation,count);
        for(let sample=0;sample<samples;sample++) {
          const cpu=process.cpuUsage(); const wall=batch(operation,count); const used=process.cpuUsage(cpu);
          if(variant==='public') { verifyPublic(); console.log('PUBLICRESULT539,'+id+','+sample+','+globalThis.__krogu539PublicHash); }
          else if(variant==='empty') {if(escape.length!==0) throw Error('Empty result');}
          else equal(escape,variant==='copy'?reference(a):expected);
          const memory=process.memoryUsage();
          console.log('WORKER539,'+id+','+sample+','+count+','+wall+','+((used.user+used.system)*1000)+','+memory.heapUsed+','+memory.rss+','+process.resourceUsage().maxRSS);
        }
        if(a.length!==originalA.length||b.length!==originalB.length||a.some((v,i)=>v!==originalA[i])||b.some((v,i)=>v!==originalB[i])) throw Error('Caller buffer mutated/detached');
      }
      globalThis.__krogu539PublicInvoke(3);
    }
  }
  verifyRootCache('after-campaign');
  if(globalThis.__krogu539Invoke!==originalTinyInvoke) throw Error('Root replaced tiny callback');
  console.log('PASS_WORKER539');
} catch(error) { campaignFailure=error; throw error; } finally {
  globalThis.__krogu539Request=null; globalThis.__krogu539Result=null; escape=null;
  try { globalThis.__krogu539PublicInvoke(3); globalThis.__krogu539PublicRequest=null; }
  catch(error) { if(campaignFailure===null) throw error; }
  finally { await Promise.all(slots.filter(slot=>slot!==null).map(slot=>slot.close())); }
}
""".trimIndent())
val hostSha = sha(host)
val stdout = File(output, "raw.log")
val stderr = File(output, "stderr.log")
val command = listOf(node.path, "--max-old-space-size=512", host.path, entry.path, kind, order, samples.toString(), rootDirectory.path)
File(output, "binding.txt").writeText("node_sha256=$nodeSha\nmanifest_sha256=$manifestSha\nsource_sha256=$sourceSha\nhost_sha256=$hostSha\nroot_directory=${rootDirectory.path}\nroot_manifest_sha256=$rootManifestSha\nargv=" + command.joinToString("\n") + "\n")
val process = ProcessBuilder(command).also {
    it.environment().clear()
    it.environment().putAll(mapOf("TZ" to "UTC", "LANG" to "C", "LC_ALL" to "C", "KROGU539_ARGS" to "worker-public-register"))
}.redirectOutput(stdout).redirectError(stderr).start()
fun stopOwned(): Boolean {
    var interrupted = false
    val owned = process.toHandle().descendants().toList()
    owned.forEach { it.destroy() }; process.destroy()
    fun waitBounded() {
        val until = System.nanoTime() + TimeUnit.SECONDS.toNanos(5)
        while (process.isAlive && System.nanoTime() < until) {
            try { process.waitFor(50, TimeUnit.MILLISECONDS) }
            catch (_: InterruptedException) { interrupted = true }
        }
    }
    waitBounded()
    owned.filter { it.isAlive }.forEach { it.destroyForcibly() }
    if (process.isAlive) process.destroyForcibly()
    waitBounded()
    File(output, "survivors.txt").writeText((owned.filter { it.isAlive }.map { it.pid() } + if (process.isAlive) listOf(process.pid()) else emptyList()).joinToString("\n"))
    return interrupted
}
// JS full corpus exceeded600s in the retained first attempt; bounded launch/wait only, plus cleanup/hash overhead.
val campaignBudgetSeconds = if (kind == "js") 1200L else 600L
val terminal = try { process.waitFor(campaignBudgetSeconds, TimeUnit.SECONDS) }
catch (problem: Throwable) {
    val interrupted = stopOwned() || problem is InterruptedException
    if (interrupted) Thread.currentThread().interrupt()
    throw problem
}
if (!terminal) {
    if (stopOwned()) Thread.currentThread().interrupt()
    error("Owned worker campaign timed out; partial output retained")
}
File(output, "raw.log.exit").writeText(process.exitValue().toString())
verify()
check(sha(host) == hostSha)
check(process.exitValue() == 0) { "Worker campaign failed; see retained logs" }
check(stdout.readLines().count { it == "PASS_WORKER539" } == 1)
println("CAPTURED_WORKER539_NOT_YET_AGGREGATE_VALIDATED")
