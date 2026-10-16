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