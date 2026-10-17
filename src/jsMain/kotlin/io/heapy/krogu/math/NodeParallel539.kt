package io.heapy.krogu.math
import kotlin.js.unsafeCast
internal actual fun nodeParallelAvailable539(): Boolean = js("""(function(){var p=globalThis.process;if(!p||typeof p.getBuiltinModule!=='function'||typeof SharedArrayBuffer!=='function')return false;var w=p.getBuiltinModule('worker_threads'),o=p.getBuiltinModule('os');if(!w||!o||(w.workerData&&w.workerData.krogu539Role==='io.heapy.krogu.node-parallel-539.v1'))return false;return (typeof o.availableParallelism==='function'?o.availableParallelism():o.cpus().length)>2;})()""") as Boolean
internal actual fun acquireNodeParallel539(): NodeParallelLease539? {
    val host = acquireHost539(embeddedNodeWorker539()) ?: return null
    try { return object : NodeParallelLease539 {
        override fun submit(index: Int, left: IntArray, right: IntArray, same: Boolean, k: Int, s: Int) { host.submit(index,left,right,same,k,s) }
        override fun join(index: Int): IntArray = host.join(index).unsafeCast<IntArray>()
        override fun release(failed: Boolean) { host.release(failed) }
    } } catch (problem: Throwable) {
        try { host.release(true) } catch (_: Throwable) {}
        throw problem
    }
}
private fun acquireHost539(payload: String): dynamic = acquireHostFactory539(payload)
private val acquireHostFactory539: dynamic = js("""(function(payloadString) {
const PROTOCOL = 'io.heapy.krogu.node-parallel-539.v1';
const KEY = Symbol.for(PROTOCOL);
const DEADLINE_MS = 20000;
const IDLE_MS = 30000;
const CHILD = "\n'use strict';\nconst {workerData}=require('node:worker_threads');\nconst {port,status:shared,entryURL,identity}=workerData;\nconst state=new Int32Array(shared);\nlet bridge;\nfunction sendFailure(id,error) {\n let message='Worker failure';\n try { message=String(error && error.message || error); } catch (_) {}\n port.postMessage({id,identity,error:message});\n}\n(async()=>{\n await import(entryURL);\n bridge=workerData.invoke;\n if(typeof bridge!=='function') throw Error('Missing production worker entry');\n port.on('message',job=>{\n  let id=job.id;\n  let product=null;\n  try {\n   if(job.identity!==identity || !Number.isSafeInteger(id) || id<1) throw Error('Worker job identity');\n   workerData.request=job; workerData.result=null;\n   bridge(); product=workerData.result;\n   if(!(product instanceof Int32Array)) throw Error('Worker bridge output');\n   port.postMessage({id,identity,result:product},[product.buffer]);\n  } catch(error) { try { sendFailure(id,error); } finally {} }\n  finally {\n   // The task no longer retains owned operands before terminal publication.\n   workerData.request=null; workerData.result=null;\n   job.a=null; job.b=null; product=null;\n   Atomics.store(state,0,2); Atomics.notify(state,0);\n  }\n });\n Atomics.store(state,0,1); Atomics.notify(state,0);\n})().catch(error=>{\n try { sendFailure(0,error); } finally { Atomics.store(state,0,3); Atomics.notify(state,0); }\n});\n";

// No Node builtin discovery until caller has proven real Node capability.
function acquire539(payloadString) {
 const p=globalThis.process;
 if(!p || typeof p.getBuiltinModule!=='function') return null;
 const wt=p.getBuiltinModule('worker_threads');
 const os=p.getBuiltinModule('os');
 if(!wt || !os || typeof SharedArrayBuffer!=='function') return null;
 const cpus=typeof os.availableParallelism==='function'?os.availableParallelism():os.cpus().length;
 if(cpus<=2 || (wt.workerData && wt.workerData.krogu539Role===PROTOCOL)) return null;
 let owner=p[KEY];
 if(owner) {
  if(owner.protocol!==PROTOCOL || owner.payload!==payloadString || owner.state!=='IDLE') return null;
 } else {
  // No consumer entry URLs or sidecar assumptions. A complete own compiler
  // closure travels inside the KLIB and is materialized in an owned directory.
  const fs=p.getBuiltinModule('fs'), path=p.getBuiltinModule('path'), url=p.getBuiltinModule('url'), crypto=p.getBuiltinModule('crypto');
  if(!fs || !path || !url || !crypto) return null;
  const payload=JSON.parse(payloadString);
  if(payload.protocol!=='krogu539.v1' || !['js','wasm'].includes(payload.kind) || !/^[a-f0-9]{64}$/.test(payload.identity)) throw Error('Payload identity');
  if(!Array.isArray(payload.files) || payload.files.length<1 || payload.files.length>32) throw Error('Payload file inventory');
  const names=new Set(); let bytes=0;
  for(const file of payload.files) {
   if(!/^[A-Za-z0-9_.-]+$/.test(file.name) || file.name==='.' || file.name==='..' || names.has(file.name) || !/^[a-f0-9]{64}$/.test(file.sha256) || typeof file.base64!=='string') throw Error('Payload path/hash');
   names.add(file.name); bytes+=file.base64.length;
  }
  if(bytes>6*1024*1024 || !names.has(payload.entry)) throw Error('Payload closure bound');
  owner={protocol:PROTOCOL,payload:payloadString,identity:payload.identity,state:'STARTING',slots:[null,null],timer:null,sequence:0,directory:null,files:[],retirement:null,retirementTimer:null,beforeExit:null,cleanupFailed:false};
  Object.defineProperty(p,KEY,{value:owner,configurable:true});
  owner.cleanFiles=function() {
   for(let i=owner.files.length-1;i>=0;i--) {
    const file=owner.files[i];
    try {fs.unlinkSync(file);} catch(error) {if(error.code!=='ENOENT') throw error;}
   }
   if(owner.directory!==null) fs.rmdirSync(owner.directory);
   owner.files.length=0; owner.directory=null;
  };
  owner.quarantine=function() {
   try {
   if(owner.state==='CLOSING' || owner.state==='BROKEN') return owner.retirement;
   owner.state='CLOSING';
   if(owner.timer!==null) { clearTimeout(owner.timer); owner.timer=null; }
   // Never replace a generation before every asynchronous termination settles.
   const endings=[];
   for(const slot of owner.slots) if(slot!==null) {
    try { if(slot.port) slot.port.close(); if(slot.childPort) slot.childPort.close(); } catch(_) {}
    if(slot.worker) {
     try { endings.push(slot.worker.terminate()); } catch(_) { owner.state='BROKEN'; }
    }
   }
   owner.retirement=Promise.allSettled(endings).then(results=>{
    if(owner.state==='BROKEN' || results.some(x=>x.status==='rejected')) { owner.state='BROKEN';owner.cleanupFailed=true;return; }
    for(const slot of owner.slots) if(slot) { slot.worker=null; slot.port=null; slot.childPort=null; }
    try {owner.cleanFiles();} catch(_) {owner.state='BROKEN';owner.cleanupFailed=true;return;}
    if(owner.beforeExit) p.removeListener('beforeExit',owner.beforeExit);
    if(owner.retirementTimer!==null) {clearTimeout(owner.retirementTimer);owner.retirementTimer=null;}
    if(p[KEY]===owner) delete p[KEY];
   }).catch(()=>{owner.state='BROKEN';owner.cleanupFailed=true;});
   } catch(_) {owner.state='BROKEN';owner.cleanupFailed=true;}
   finally {
    for(const slot of owner.slots) if(slot) {
     try {if(slot.worker) slot.worker.unref();if(slot.port) slot.port.unref();} catch(_) {}
    }
   }
   return owner.retirement;
  };
  owner.beforeExit=function() {
   if(p[KEY]!==owner) return;
   // beforeExit may perform asynchronous work. One referenced timer keeps only
   // this owned retirement alive; the ordinary idle timer remains unref'ed.
   owner.retirementTimer=setTimeout(()=>{
    owner.retirementTimer=null;owner.state='BROKEN';owner.cleanupFailed=true;
    p.removeListener('beforeExit',owner.beforeExit);
    if(!p.exitCode) p.exitCode=1;
    try {p.stderr.write('Krogu Node worker retirement deadline\n');} catch(_) {}
   },20000);
   const completion=owner.quarantine();
   if(completion) completion.then(()=>{
    if(owner.retirementTimer!==null) {clearTimeout(owner.retirementTimer);owner.retirementTimer=null;}
    p.removeListener('beforeExit',owner.beforeExit);
    if(owner.cleanupFailed || p[KEY]===owner) {
     if(!p.exitCode) p.exitCode=1;
     try {p.stderr.write('Krogu Node worker retirement failed\n');} catch(_) {}
    }
   });
  };
  try {
   p.once('beforeExit',owner.beforeExit);
   owner.directory=fs.mkdtempSync(path.join(os.tmpdir(),'krogu-node539-'));
   fs.chmodSync(owner.directory,448);
   const digest=crypto.createHash('sha256');
   for(const file of payload.files) {
    const decoded=Buffer.from(file.base64,'base64');
    if(decoded.toString('base64')!==file.base64 || crypto.createHash('sha256').update(decoded).digest('hex')!==file.sha256) throw Error('Payload content hash');
    digest.update(decoded);
    const destination=path.join(owner.directory,file.name);
    owner.files.push(destination); // Owned path recorded BEFORE a partial write.
    fs.writeFileSync(destination,decoded,{flag:'wx',mode:384});
   }
   if(digest.digest('hex')!==payload.identity) throw Error('Payload generation hash');
   const entryURL=url.pathToFileURL(path.join(owner.directory,payload.entry)).href;
   const identity=owner.identity;
   for(let i=0;i<2;i++) {
    const slot={worker:null,port:null,childPort:null,status:null,pending:0};
    owner.slots[i]=slot;
    const channel=new wt.MessageChannel();
    slot.port=channel.port1; slot.childPort=channel.port2;
    const shared=new SharedArrayBuffer(4); slot.status=new Int32Array(shared);
    slot.worker=new wt.Worker(CHILD,{eval:true,execArgv:[],
     env:{},workerData:{port:slot.childPort,status:shared,entryURL,identity,krogu539Role:PROTOCOL},
     transferList:[slot.childPort]});
    slot.worker.on('error',()=>{owner.quarantine();});
    slot.worker.on('exit',()=>{if(owner.state!=='CLOSING' && owner.state!=='BROKEN') owner.quarantine();});
   }
   for(const slot of owner.slots) {
    const end=Date.now()+DEADLINE_MS;
    while(Atomics.load(slot.status,0)===0) {
     if(Date.now()>=end) throw Error('Worker initialization deadline');
     Atomics.wait(slot.status,0,0,10);
    }
    if(Atomics.load(slot.status,0)!==1) throw Error('Worker initialization failed');
   }
   owner.state='IDLE';
  } catch(error) {
   owner.quarantine();
   // Only known filesystem/Node permission capability failures may decline
   // before arithmetic acceptance. OOM and unknown errors remain failures.
   if(['EACCES','EPERM','EROFS','ERR_ACCESS_DENIED'].includes(error && error.code)) return null;
   throw error;
  }
 }
 if(owner.timer!==null) { clearTimeout(owner.timer); owner.timer=null; }
 owner.state='BUSY'; // Before any fallible ref/lease construction.
 try {
  for(const slot of owner.slots) { slot.worker.ref(); slot.port.ref(); }
  let released=false;
  return {
   submit(index,a,b,same,k,s) {
    if(released || owner.state!=='BUSY' || (index!==0 && index!==1)) throw Error('Invalid owner slot');
    const slot=owner.slots[index];
    if(slot.pending!==0 || Atomics.load(slot.status,0)!==1) throw Error('Worker slot occupied');
    if(!(a instanceof Int32Array) || !(b instanceof Int32Array) || (same && a!==b)) throw Error('Owned input transport');
    if(!Number.isSafeInteger(owner.sequence+1)) throw Error('Worker job counter exhausted');
    const id=++owner.sequence; const identity=owner.identity;
    // Transfer only copied input arrays, never the public magnitude backing.
    const transfer=a===b?[a.buffer]:[a.buffer,b.buffer];
    slot.port.postMessage({id,identity,a,b,same,k,s},transfer);
    slot.pending=id; // Nonallocating ownership store directly after accepted postMessage.
   },
   join(index) {
    const slot=owner.slots[index];
    const id=slot.pending;
    if(id===0) throw Error('No accepted worker task');
    try {
     const end=Date.now()+DEADLINE_MS;
     while(Atomics.load(slot.status,0)!==2) {
      if(owner.state==='CLOSING'||owner.state==='BROKEN') throw Error('Worker generation quarantined');
      if(Date.now()>=end) throw Error('Worker completion deadline');
      Atomics.wait(slot.status,0,1,10);
     }
     let message;
     while(true) {
      const received=wt.receiveMessageOnPort(slot.port);
      message=received?received.message:null;
      if(message) break;
      if(owner.state==='CLOSING'||owner.state==='BROKEN') throw Error('Worker generation quarantined');
      if(Date.now()>=end) throw Error('Worker transport deadline');
      Atomics.wait(slot.status,0,2,1);
     }
     slot.pending=0;
     Atomics.store(slot.status,0,1);
     if(message.id!==id || message.identity!==owner.identity) throw Error('Worker terminal identity mismatch');
     if(message.error!==undefined) throw Error('Parallel worker failed: '+message.error);
     if(!(message.result instanceof Int32Array)) throw Error('Worker terminal result missing');
     return message.result;
    } catch(error) { owner.quarantine(); throw error; }
   },
   release(failed) {
    if(released) return;
    released=true;
    if(failed || owner.slots.some(x=>x.pending!==0) || owner.state!=='BUSY') {owner.quarantine();return;}
    try {
     for(const slot of owner.slots) {slot.worker.unref();slot.port.unref();}
     owner.state='IDLE';
     owner.timer=setTimeout(()=>{if(owner.state==='IDLE') owner.quarantine();},IDLE_MS);
     owner.timer.unref();
    } catch(error) {owner.quarantine();throw error;}
   }
  };
 } catch(error) {owner.quarantine();throw error;}
};

return acquire539(payloadString);
})""")
