# Independent timing audit reproduction evidence

Python below was executed only for local read-only metadata/raw recomputation. These are preserved transcript bytes, not new standalone automation entrypoints. No numerical peer execution.

## independent-jvm-timing-audit.py

Executed bytes SHA256 `dd552ac8f99cd8f831fd5dfad1e01b9b7214da9fcacc7aad5bddb111915ff43f`; exact history copy `history/independent-audit-python-executed/independent-jvm-timing-audit.py.txt`.

```python
from pathlib import Path
import json,hashlib,csv,statistics as st,math,re,collections
B=Path(__file__).resolve().parent
H=lambda p:hashlib.sha256(p.read_bytes()).hexdigest()
receipt=B/'timing-jvm-1-actual.json'; A=json.loads(receipt.read_text());assert H(receipt)=='eae5e98b9b5a76cf945c6d27c04dbbbb7564602dce0fe8cd05ead2dd58f861cf'
for e in A['files']:
 p=Path(e['path']);assert p.stat().st_size==e['bytes'] and H(p)==e['sha256'],p
root=B/'timing-jvm-1'; summary=B/'timing-jvm-1-summary';art=Path(A['artifact_root']);assert H(art/'manifest.sha256')==A['artifact_manifest_sha256'] and H(Path(A['runtime_exe']))==A['runtime_sha256']
manifest={}
for l in (art/'manifest.sha256').read_text().splitlines():
 d,n=l.split('\t');assert H(art/n)==d;manifest[n]=d
assert {p.relative_to(art).as_posix() for p in art.rglob('*') if p.is_file()}==set(manifest)|{'manifest.sha256'}
snapshot=json.loads((B/A['source_snapshot']).read_text());assert len(snapshot['entries'])==20
for e in snapshot['entries']:
 p=Path(e['snapshot_path']);assert H(p)==e['sha256'] and p.stat().st_size==e['bytes']

def prop(p):return dict(l.split('=',1) for l in p.read_text().splitlines() if '=' in l)
bind=prop(root/'binding.txt');assert bind['krogu_commit']==A['accepted_main']=='829ce21154c7e75f5206eeb173351c391ed21597';assert bind['publication_ledger_sha256']=='4a424029142eb791357a8bb222fca52f836f67956dee6c36289d448a580e8d23'
peers=['jdk-bellsoft-25.0.4+9','krogu-final539-PENDING','ionspin-bignum-0.3.10','kbignum-0.0.20-desktop'];K=peers[1]
fixtures={'int':{'zero':['0','0','0','0','0','0'],'signed':['-7','3','-4','-10','-21','49'],'carry30':['1073741823','1','1073741824','1073741822','1073741823','1152921502459363329'],'wide10':['100000000000000000000','-10','99999999999999999990','100000000000000000010','-1000000000000000000000','1'+'0'*40]},'decimal':{'zero':['0.00','-0.000','0.000','0.000','0.00000','0.0000'],'scale':['1.20','3.400','4.600','-2.200','4.08000','1.4400'],'signed':['-1.25','0.05','-1.20','-1.30','-0.0625','1.5625'],'wide10':['100000000000000000000.0','-10.00','99999999999999999990.00','100000000000000000010.00','-1000000000000000000000.000','1'+'0'*40+'.00']}}
textvals={'int/zero':'0','int/signed':'-7','int/carry30':'1073741823','int/wide10':'100000000000000000000','decimal/zero':'0','decimal/scale':'1.2','decimal/signed':'-1.25','decimal/wide10':'100000000000000000001.25'}
fixed={'positive-tie':['1','8','0.13'],'negative-tie':['-1','8','-0.13'],'negative-divisor':['1','-8','-0.13'],'both-negative':['-1','-8','0.13'],'nonterminating':['1','3','0.33333333333333333333'],'carry':['9.995','1','10.00'],'zero':['0.000','7','0.0000'],'exact':['12.00','3','4.00']}
def canon(v):
 m=re.fullmatch(r'([+-]?)([0-9]+)(?:\.([0-9]+))?(?:[eE]([+-]?[0-9]+))?',v);assert m
 sg,whole,frac,ex=m.groups();frac=frac or '';c=(whole+frac).lstrip('0')
 if not c:return '0@0'
 d=c.rstrip('0');return ('-' if sg=='-' else '')+d+'@'+str(len(frac)-int(ex or 0)-(len(c)-len(d)))
def expected(id):
 p=id.split('/')
 if p[0]=='fixed-scale':
  a,b,v=fixed[p[1]];return a,b,v,canon(v),'decimal',False
 family,name,op=p
 if op in ['PARSE','FORMAT']:
  v=textvals[family+'/'+name];return v,v,v,v,'text',False
 f=fixtures[family][name];v=f[{'ADD':2,'SUBTRACT':3,'MULTIPLY':4,'self':5}[op]];return f[0],f[0] if op=='self' else f[1],v,canon(v) if family=='decimal' else v,family,op=='self'
ids=[f'{f}/{n}/{o}' for f,fs in fixtures.items() for n in fs for o in ['ADD','SUBTRACT','MULTIPLY','self','PARSE','FORMAT']]
catalog=sorted([(p,id) for id in ids for p in peers]+[(p,'fixed-scale/'+n) for n in fixed for p in peers if p!=peers[2]],key=lambda x:(x[1],peers.index(x[0])))
assert len(catalog)==216 and [tuple(l.split('\t')) for l in (root/'catalog.tsv').read_text().splitlines()]==catalog

def int32(n):return (n+2**31)%2**32-2**31
def checksum(v):
 h=0
 for c in v:h=int32(31*h+ord(c))
 return h
def ring(v,n):
 h=0
 for _ in range(min(n,64)):h=int32(31*h+checksum(v))
 return h
def close(x,y):assert math.isclose(float(x),float(y),rel_tol=1e-12,abs_tol=1e-9),(x,y)
def metric(s):return None if s=='NA' else int(s)
records={};identities=set();caps=0;durations=[];rss=[];sampleSpread=[]
for fork in range(3):
 order='reverse' if fork==1 else 'forward'
 for position,(peer,id) in enumerate(catalog[::-1] if fork==1 else catalog):
  file=root/f'fork{fork}-case{position}.log';lines=file.read_text().splitlines();meta=prop(Path(str(file)+'.binding'));identity=prop(Path(str(file)+'.identity'))
  assert meta=={'peer':peer,'id':id,'fork':str(fork),'order':order,'log_sha256':H(file),'exit':'0'}
  assert int(identity['supervisor_pid'])>0 and identity['supervisor_start'];key=(identity['supervisor_pid'],identity['supervisor_start']);assert key not in identities;identities.add(key)
  argv=Path(str(file)+'.argv.txt').read_text().splitlines();assert argv[:9]==['/usr/bin/time','-l',A['runtime_exe'],'-Xms512m','-Xmx512m','-XX:+UseSerialGC','-XX:ActiveProcessorCount=1','-XX:-OmitStackTraceInFastThrow','-cp'];assert argv[-5:]==['preparation598.Timing598Kt',peer,id,str(fork),order];assert argv[9].split(':')==[str(art/p) for p in (art/'classpath.txt').read_text().splitlines()]
  def single(tag):
   rs=[l.split('\t') for l in lines if l.startswith(tag+'\t')];assert len(rs)==1,(tag,file);return rs[0]
  assert single('IDENTITY598')==['IDENTITY598','JVM/BellSoft25.0.4+9-LTS',peer,id,str(fork),order]
  inp=single('INPUT598');a,b,v,canonical,family,selfalias=expected(id);assert inp[1:3]==[a,b] and inp[4]==v
  if family=='text':assert inp[3]=='unary-text'
  elif selfalias:assert inp[3]=='self-alias'
  else:
   assert inp[3] in ['binary-distinct','binary-native-alias']
   if inp[3]=='binary-native-alias':assert canon(a)==canon(b)
  first=single('FIRST598');gate=single('GATE598_PASS');warm=single('WARM598');done=single('COMPLETE598_TIMING_CHILD');assert gate==['GATE598_PASS',peer,id,str(fork)] and done==['COMPLETE598_TIMING_CHILD',peer,id,str(fork)]
  assert lines.index('\t'.join(first))<lines.index('\t'.join(gate))<lines.index('\t'.join(warm))
  assert len(first)==12 and first[1:5]==[peer,id,str(fork),'1'] and int(first[5])>0 and int(first[11])==checksum(canonical)
  assert len(warm)==8 and warm[1:4]==[peer,id,str(fork)] and warm[7]=='TARGET200ms_CAP1048576ops_TARGET20msbatch';ops,dur,count=map(int,warm[4:7]);assert 1<=ops<=1048576 and 1<=count<=1048576 and dur>0;caps+=ops==1048576;durations.append(dur)
  gates=[l.split('\t') for l in lines if l.startswith('GATE_COUNT598\t')];assert gates==[['GATE_COUNT598',p,str(n),str(na)] for p,n,na in zip(peers,[168,168,159,167],[1,1,3,2])]
  textrows=[l.split('\t') for l in lines if l.startswith('CHECK_TEXT598\t')];assert len(textrows)==128 and {tuple(r[1:]) for r in textrows}=={(p,k+'/'+o,str(r),v,v) for p in peers for k,v in textvals.items() for o in ['PARSE','FORMAT'] for r in range(2)}
  assert [l for l in lines if l.startswith('TEXT_GATE598_COUNT\t')]==['TEXT_GATE598_COUNT\t'+p+'\t32' for p in peers]
  rows=[l.split('\t') for l in lines if l.startswith('TIME598\t')];assert len(rows)==7
  for i,row in enumerate(rows):
   assert len(row)==13 and row[1:6]==[peer,id,str(fork),str(i),str(count)] and int(row[6])>0 and int(row[12])==ring(canonical,count);assert lines.index('\t'.join(row))>lines.index('\t'.join(warm));assert all(s=='NA' or int(s)>=0 for s in row[7:12])
  wall=[int(r[6])/count for r in rows]
  def counter(col):
   vals=[metric(r[col]) for r in rows];assert all(v is None for v in vals) or all(v is not None for v in vals);return None if vals[0] is None else st.median([v/count for v in vals])
  record={'wall':st.median(wall),'min':min(wall),'max':max(wall),'cpu':counter(7),'alloc':counter(8),'relation':inp[3],'FIRST':int(first[5])};records[(peer,id,fork)]=record;sampleSpread.append(max(wall)/min(wall))
  peaks=[int(m.group(1)) for l in lines if (m:=re.fullmatch(r'\s*([0-9]+)\s+maximum resident set size',l))];assert len(peaks)==1 and peaks[0]>0;rss+=peaks
assert caps==565 and len(records)==648

def tsv(name):
 with (summary/name).open() as f:return list(csv.DictReader(f,delimiter='\t'))
per=tsv('per-fork.tsv');assert len(per)==648
for r in per:
 d=records[(r['peer'],r['id'],int(r['fork']))];assert d['relation']==r['relation']
 for col,key in [('ns_per_op_median','wall'),('ns_per_op_min','min'),('ns_per_op_max','max')]:close(r[col],d[key])
 close(r['derived_ops_per_second'],1e9/d['wall'])
 for col,key in [('cpu_ns_per_op_median','cpu'),('allocation_B_per_op_median','alloc')]:
  if d[key] is None:assert r[col]=='NA'
  else:close(r[col],d[key])
pairs=tsv('paired.tsv');fpairs=tsv('first-paired.tsv');assert len(pairs)==len(fpairs)==480
for rows,firstmode in [(pairs,False),(fpairs,True)]:
 for r in rows:
  d=records[(r['peer'],r['id'],int(r['fork']))];k=records[(K,r['id'],int(r['fork']))];value='FIRST' if firstmode else 'wall';ratio='first_peer_over_krogu' if firstmode else 'peer_ns_over_krogu_ns';delta='first_peer_minus_krogu_ns' if firstmode else 'peer_minus_krogu_ns';close(r[ratio],d[value]/k[value]);close(r[delta],d[value]-k[value]);assert r['same_observed_relation']==str(d['relation']==k['relation']).lower()
# Save every comparative case with all three cohorts and resource ratios; no ranking filter.
comparison=[]
for peer,id in catalog:
 if peer==K:continue
 ds=[records[peer,id,f] for f in range(3)];ks=[records[K,id,f] for f in range(3)];ratios=[d['wall']/k['wall'] for d,k in zip(ds,ks)];firsts=[d['FIRST']/k['FIRST'] for d,k in zip(ds,ks)]
 comparison.append({'peer':peer,'id':id,'wall_ratios_F_R_F':ratios,'wall_deltas_ns':[d['wall']-k['wall'] for d,k in zip(ds,ks)],'wall_ratio_median':st.median(ratios),'ratio_range_max_over_min':max(ratios)/min(ratios),'direction':'peer_slower_all3' if min(ratios)>1 else 'peer_faster_all3' if max(ratios)<1 else 'mixed','FIRST_ratios_F_R_F':firsts,'cpu_ratios_F_R_F':[None if d['cpu'] is None or k['cpu'] in [None,0] else d['cpu']/k['cpu'] for d,k in zip(ds,ks)],'alloc_bytes_per_op_peer_F_R_F':[d['alloc'] for d in ds],'alloc_bytes_per_op_Krogu_F_R_F':[k['alloc'] for k in ks],'same_relation_F_R_F':[d['relation']==k['relation'] for d,k in zip(ds,ks)]})
report=B/'independent-jvm-timing-1-comparisons.json';assert not report.exists();report.write_text(json.dumps(comparison,indent=2)+'\n')
byPeer={}
for peer in peers:
 if peer==K:continue
 rs=[r for r in comparison if r['peer']==peer];byPeer[peer]={'cases':len(rs),'directions':dict(collections.Counter(r['direction'] for r in rs)),'wall_ratio_medians_range':[min(r['wall_ratio_median'] for r in rs),max(r['wall_ratio_median'] for r in rs)],'max_three_cohort_ratio_spread':max(r['ratio_range_max_over_min'] for r in rs),'mixed_relation_cases':sum(not all(r['same_relation_F_R_F']) for r in rs)}
audit={'verdict':'ACTUAL_JVM598_TIMING_DATA_AND_RECOMPUTED_COMPARISONS_PASS_QUALIFIED_NOT_FULL598','receipt_sha256':H(receipt),'counts':{'cases':216,'fresh_supervisor_identities':648,'FIRST':648,'TIME':4536,'paired_rows_each_FIRST_warm':480},'bindings':'All receipt files, full artifact manifest/exact tree, runtime executable and20 own-source snapshots hashed; accepted829/GAV ledger4a424029 and exact six-loop command variants checked. No foreign implementation read.','source_runtime_scope':'JVM512MiB SerialGC/ActiveProcessorCount1; runtime asserts BellSoft25.0.4+9-LTS. Observed identities are usr/bin/time supervisors, not independently sampled arithmetic runtime PID.','correctness':'Every child INPUT matched independent fixed tuples; FIRST and seven ring digests independently recomputed; four arithmetic gate counts and128 exact text rows rechecked. All7 count/order bindings strict. Earlier-result/input preservation executes reviewed gates; last64 timed result checking remains partial ring scope.','training':{'capped_children':caps,'duration_ns_min':min(durations),'duration_ns_max':max(durations),'limitation':'565 children stop at operation cap; 200ms target/stationarity not established. Warm measured after observed correctness gate; observed/cached FORMAT explicit.'},'comparisons':{'file':str(report.relative_to(B)),'sha256':H(report),'cases':len(comparison),'by_peer':byPeer,'method':'Seven-sample wall/CPU/alloc medians per fresh child; matched same workload/cohort peer over Krogu. FIRST independently recalculated. All3 ratios/deltas/ranges retained; descriptive, no significance/independence/global ranking or539 threshold.'},'metrology':{'wall':'Monotonic ns / operation count; includes adapter/native operation/ring escape.','cpu':'Current-thread ns when supported; excludes other-thread CPU.','allocation':'Current-thread bytes/operation when available; not retained memory.','memory':'Heap coarse snapshots and whole-child time-l maxRSS including gate/startup/exit; not owned/per-operation peak.','RSS_bytes_range':[min(rss),max(rss)],'within_child_max_sample_ratio':max(sampleSpread)},'NA':'Ions fixed-scale and significant HALF_EVEN remain outside equivalent measured subset; raw unavailable counters remain NA.','limitations':['FIRST first explicit selected public operation after preparation; PARSE String-only, FORMAT parsed unobserved, arithmetic parsed operands. Module initialization/internal parse work may predate FIRST.','Warm repeated FORMAT may use native caches after observations; not fresh-format-value pool.','Small fixed corpus; portable targets, integrated report and remaining task requirements separate.','No new numerical run or automatic favorable repetition; all adverse and mixed aliases retained.'],'blocking_findings':[]}
p=B/'independent-jvm-timing-1-actual-audit.json';assert not p.exists();p.write_text(json.dumps(audit,indent=2)+'\n');print(p,H(p));print(json.dumps(byPeer));print('training',caps,min(durations),max(durations))
```

## independent-jvm-timing-v2-audit.py

Executed bytes SHA256 `0f3835c854a495a7a74ac6a75ffcae2108461d8845899de4a1ffd49f7760a7b2`; exact history copy `history/independent-audit-python-executed/independent-jvm-timing-v2-audit.py.txt`.

```python
from pathlib import Path
import json,hashlib,csv,statistics as st,math,re,collections
B=Path(__file__).resolve().parent
H=lambda p:hashlib.sha256(p.read_bytes()).hexdigest()
receipt=B/'timing-jvm-v2-actual.json'; A=json.loads(receipt.read_text());assert H(receipt)=='8333999cd132ec2d194e0f2de9852da385f4291bd0b8af51296b2b2023c0fae1'
for e in A['files']:
 p=Path(e['path']);assert p.stat().st_size==e['bytes'] and H(p)==e['sha256'],p
A['runtime_exe']='/Users/yoda/.sdkman/candidates/java/25.0.4-librca/bin/java';A['runtime_sha256']='b7a71fa440912190cce262a062a30629f1668c7e14c0d4c021c6f37d3f734c29';A['source_snapshot']='artifacts/timing-jvm-829ce211-v2-sources/source-map.json'
root=B/'timing-jvm-v2'; summary=B/'timing-jvm-v2-summary';art=Path(A['artifact_root']);assert H(art/'manifest.sha256')==A['artifact_manifest_sha256'] and H(Path(A['runtime_exe']))==A['runtime_sha256']
manifest={}
for l in (art/'manifest.sha256').read_text().splitlines():
 d,n=l.split('\t');assert H(art/n)==d;manifest[n]=d
assert {p.relative_to(art).as_posix() for p in art.rglob('*') if p.is_file()}==set(manifest)|{'manifest.sha256'}
snapshot=json.loads((B/A['source_snapshot']).read_text());assert len(snapshot['entries'])==22
for e in snapshot['entries']:
 p=Path(e['snapshot_path']);assert H(p)==e['sha256'] and p.stat().st_size==e['bytes']

def prop(p):return dict(l.split('=',1) for l in p.read_text().splitlines() if '=' in l)
bind=prop(root/'binding.txt');assert bind['krogu_commit']==A['accepted_main']=='829ce21154c7e75f5206eeb173351c391ed21597';assert bind['publication_ledger_sha256']=='4a424029142eb791357a8bb222fca52f836f67956dee6c36289d448a580e8d23'
peers=['jdk-bellsoft-25.0.4+9','krogu-final539-PENDING','ionspin-bignum-0.3.10','kbignum-0.0.20-desktop'];K=peers[1]
fixtures={'int':{'zero':['0','0','0','0','0','0'],'signed':['-7','3','-4','-10','-21','49'],'carry30':['1073741823','1','1073741824','1073741822','1073741823','1152921502459363329'],'wide10':['100000000000000000000','-10','99999999999999999990','100000000000000000010','-1000000000000000000000','1'+'0'*40]},'decimal':{'zero':['0.00','-0.000','0.000','0.000','0.00000','0.0000'],'scale':['1.20','3.400','4.600','-2.200','4.08000','1.4400'],'signed':['-1.25','0.05','-1.20','-1.30','-0.0625','1.5625'],'wide10':['100000000000000000000.0','-10.00','99999999999999999990.00','100000000000000000010.00','-1000000000000000000000.000','1'+'0'*40+'.00']}}
textvals={'int/zero':'0','int/signed':'-7','int/carry30':'1073741823','int/wide10':'100000000000000000000','decimal/zero':'0','decimal/scale':'1.2','decimal/signed':'-1.25','decimal/wide10':'100000000000000000001.25'}
fixed={'positive-tie':['1','8','0.13'],'negative-tie':['-1','8','-0.13'],'negative-divisor':['1','-8','-0.13'],'both-negative':['-1','-8','0.13'],'nonterminating':['1','3','0.33333333333333333333'],'carry':['9.995','1','10.00'],'zero':['0.000','7','0.0000'],'exact':['12.00','3','4.00']}
def canon(v):
 m=re.fullmatch(r'([+-]?)([0-9]+)(?:\.([0-9]+))?(?:[eE]([+-]?[0-9]+))?',v);assert m
 sg,whole,frac,ex=m.groups();frac=frac or '';c=(whole+frac).lstrip('0')
 if not c:return '0@0'
 d=c.rstrip('0');return ('-' if sg=='-' else '')+d+'@'+str(len(frac)-int(ex or 0)-(len(c)-len(d)))
def expected(id):
 p=id.split('/')
 if p[0]=='fixed-scale':
  a,b,v=fixed[p[1]];return a,b,v,canon(v),'decimal',False
 family,name,op=p
 if op in ['PARSE','FORMAT']:
  v=textvals[family+'/'+name];return v,v,v,v,'text',False
 f=fixtures[family][name];v=f[{'ADD':2,'SUBTRACT':3,'MULTIPLY':4,'self':5}[op]];return f[0],f[0] if op=='self' else f[1],v,canon(v) if family=='decimal' else v,family,op=='self'
ids=[f'{f}/{n}/{o}' for f,fs in fixtures.items() for n in fs for o in ['ADD','SUBTRACT','MULTIPLY','self','PARSE','FORMAT']]
catalog=sorted([(p,id) for id in ids for p in peers]+[(p,'fixed-scale/'+n) for n in fixed for p in peers if p!=peers[2]],key=lambda x:(x[1],peers.index(x[0])))
assert len(catalog)==216 and [tuple(l.split('\t')) for l in (root/'catalog.tsv').read_text().splitlines()]==catalog

def int32(n):return (n+2**31)%2**32-2**31
def checksum(v):
 h=0
 for c in v:h=int32(31*h+ord(c))
 return h
def ring(v,n):
 h=0
 for _ in range(min(n,64)):h=int32(31*h+checksum(v))
 return h
def close(x,y):assert math.isclose(float(x),float(y),rel_tol=1e-12,abs_tol=1e-9),(x,y)
def metric(s):return None if s=='NA' else int(s)
records={};identities=set();caps=0;durations=[];rss=[];sampleSpread=[]
for fork in range(3):
 order='reverse' if fork==1 else 'forward'
 for position,(peer,id) in enumerate(catalog[::-1] if fork==1 else catalog):
  file=root/f'fork{fork}-case{position}.log';lines=file.read_text().splitlines();meta=prop(Path(str(file)+'.binding'));identity=prop(Path(str(file)+'.identity'))
  assert meta=={'peer':peer,'id':id,'fork':str(fork),'order':order,'log_sha256':H(file),'exit':'0'}
  assert int(identity['supervisor_pid'])>0 and identity['supervisor_start'];key=(identity['supervisor_pid'],identity['supervisor_start']);assert key not in identities;identities.add(key)
  argv=Path(str(file)+'.argv.txt').read_text().splitlines();assert argv[:9]==['/usr/bin/time','-l',A['runtime_exe'],'-Xms512m','-Xmx512m','-XX:+UseSerialGC','-XX:ActiveProcessorCount=1','-XX:-OmitStackTraceInFastThrow','-cp'];assert argv[-5:]==['preparation598.Timing598Kt',peer,id,str(fork),order];assert argv[9].split(':')==[str(art/p) for p in (art/'classpath.txt').read_text().splitlines()]
  def single(tag):
   rs=[l.split('\t') for l in lines if l.startswith(tag+'\t')];assert len(rs)==1,(tag,file);return rs[0]
  assert single('IDENTITY598')==['IDENTITY598','JVM/BellSoft25.0.4+9-LTS',peer,id,str(fork),order]
  inp=single('INPUT598');a,b,v,canonical,family,selfalias=expected(id);assert inp[1:3]==[a,b] and inp[4]==v
  if family=='text':assert inp[3]=='unary-text'
  elif selfalias:assert inp[3]=='self-alias'
  else:
   assert inp[3] in ['binary-distinct','binary-native-alias']
   if inp[3]=='binary-native-alias':assert canon(a)==canon(b)
  first=single('FIRST598');gate=single('GATE598_PASS');warm=single('WARM598');done=single('COMPLETE598_TIMING_CHILD');assert gate==['GATE598_PASS',peer,id,str(fork)] and done==['COMPLETE598_TIMING_CHILD',peer,id,str(fork)]
  assert lines.index('\t'.join(first))<lines.index('\t'.join(gate))<lines.index('\t'.join(warm))
  assert len(first)==12 and first[1:5]==[peer,id,str(fork),'1'] and int(first[5])>0 and int(first[11])==checksum(canonical)
  assert len(warm)==8 and warm[1:4]==[peer,id,str(fork)] and warm[7]=='MINIMUM200ms_BATCHCAP1048576_TARGET20msbatch';ops,dur,count=map(int,warm[4:7]);assert ops>0 and 1<=count<=1048576 and dur>=200000000;caps+=ops==1048576;durations.append(dur)
  gates=[l.split('\t') for l in lines if l.startswith('GATE_COUNT598\t')];assert gates==[['GATE_COUNT598',p,str(n),str(na)] for p,n,na in zip(peers,[168,168,159,167],[1,1,3,2])]
  textrows=[l.split('\t') for l in lines if l.startswith('CHECK_TEXT598\t')];assert len(textrows)==128 and {tuple(r[1:]) for r in textrows}=={(p,k+'/'+o,str(r),v,v) for p in peers for k,v in textvals.items() for o in ['PARSE','FORMAT'] for r in range(2)}
  assert [l for l in lines if l.startswith('TEXT_GATE598_COUNT\t')]==['TEXT_GATE598_COUNT\t'+p+'\t32' for p in peers]
  rows=[l.split('\t') for l in lines if l.startswith('TIME598\t')];assert len(rows)==7
  for i,row in enumerate(rows):
   assert len(row)==13 and row[1:6]==[peer,id,str(fork),str(i),str(count)] and int(row[6])>0 and int(row[12])==ring(canonical,count);assert lines.index('\t'.join(row))>lines.index('\t'.join(warm));assert all(s=='NA' or int(s)>=0 for s in row[7:12])
  wall=[int(r[6])/count for r in rows]
  def counter(col):
   vals=[metric(r[col]) for r in rows];assert all(v is None for v in vals) or all(v is not None for v in vals);return None if vals[0] is None else st.median([v/count for v in vals])
  record={'wall':st.median(wall),'min':min(wall),'max':max(wall),'cpu':counter(7),'alloc':counter(8),'relation':inp[3],'FIRST':int(first[5])};records[(peer,id,fork)]=record;sampleSpread.append(max(wall)/min(wall))
  peaks=[int(m.group(1)) for l in lines if (m:=re.fullmatch(r'\s*([0-9]+)\s+maximum resident set size',l))];assert len(peaks)==1 and peaks[0]>0;rss+=peaks
assert min(durations)>=200000000 and len(records)==648

def tsv(name):
 with (summary/name).open() as f:return list(csv.DictReader(f,delimiter='\t'))
per=tsv('per-fork.tsv');assert len(per)==648
for r in per:
 d=records[(r['peer'],r['id'],int(r['fork']))];assert d['relation']==r['relation']
 for col,key in [('ns_per_op_median','wall'),('ns_per_op_min','min'),('ns_per_op_max','max')]:close(r[col],d[key])
 close(r['derived_ops_per_second'],1e9/d['wall'])
 for col,key in [('cpu_ns_per_op_median','cpu'),('allocation_B_per_op_median','alloc')]:
  if d[key] is None:assert r[col]=='NA'
  else:close(r[col],d[key])
pairs=tsv('paired.tsv');fpairs=tsv('first-paired.tsv');assert len(pairs)==len(fpairs)==480
for rows,firstmode in [(pairs,False),(fpairs,True)]:
 for r in rows:
  d=records[(r['peer'],r['id'],int(r['fork']))];k=records[(K,r['id'],int(r['fork']))];value='FIRST' if firstmode else 'wall';ratio='first_peer_over_krogu' if firstmode else 'peer_ns_over_krogu_ns';delta='first_peer_minus_krogu_ns' if firstmode else 'peer_minus_krogu_ns';close(r[ratio],d[value]/k[value]);close(r[delta],d[value]-k[value]);assert r['same_observed_relation']==str(d['relation']==k['relation']).lower()
# Save every comparative case with all three cohorts and resource ratios; no ranking filter.
comparison=[]
for peer,id in catalog:
 if peer==K:continue
 ds=[records[peer,id,f] for f in range(3)];ks=[records[K,id,f] for f in range(3)];ratios=[d['wall']/k['wall'] for d,k in zip(ds,ks)];firsts=[d['FIRST']/k['FIRST'] for d,k in zip(ds,ks)]
 comparison.append({'peer':peer,'id':id,'wall_ratios_F_R_F':ratios,'wall_deltas_ns':[d['wall']-k['wall'] for d,k in zip(ds,ks)],'wall_ratio_median':st.median(ratios),'ratio_range_max_over_min':max(ratios)/min(ratios),'direction':'peer_slower_all3' if min(ratios)>1 else 'peer_faster_all3' if max(ratios)<1 else 'mixed','FIRST_ratios_F_R_F':firsts,'cpu_ratios_F_R_F':[None if d['cpu'] is None or k['cpu'] in [None,0] else d['cpu']/k['cpu'] for d,k in zip(ds,ks)],'alloc_bytes_per_op_peer_F_R_F':[d['alloc'] for d in ds],'alloc_bytes_per_op_Krogu_F_R_F':[k['alloc'] for k in ks],'same_relation_F_R_F':[d['relation']==k['relation'] for d,k in zip(ds,ks)]})
report=B/'independent-jvm-timing-v2-comparisons.json';assert not report.exists();report.write_text(json.dumps(comparison,indent=2)+'\n')
byPeer={}
for peer in peers:
 if peer==K:continue
 rs=[r for r in comparison if r['peer']==peer];byPeer[peer]={'cases':len(rs),'directions':dict(collections.Counter(r['direction'] for r in rs)),'wall_ratio_medians_range':[min(r['wall_ratio_median'] for r in rs),max(r['wall_ratio_median'] for r in rs)],'max_three_cohort_ratio_spread':max(r['ratio_range_max_over_min'] for r in rs),'mixed_relation_cases':sum(not all(r['same_relation_F_R_F']) for r in rs)}
audit={'verdict':'ACTUAL_JVM598_V2_DATA_AND_RECOMPUTED_COMPARISONS_PASS_TRENDS_DISPOSITION_SEPARATE','receipt_sha256':H(receipt),'counts':{'cases':216,'fresh_supervisor_identities':648,'FIRST':648,'TIME':4536,'paired_rows_each_FIRST_warm':480},'bindings':'All receipt files, full artifact manifest/exact tree, runtime executable and22 source/descriptor snapshots hashed; accepted829/GAV ledger4a424029 and exact six-loop command variants checked. No foreign implementation read.','source_runtime_scope':'JVM512MiB SerialGC/ActiveProcessorCount1; runtime asserts BellSoft25.0.4+9-LTS. Observed identities are usr/bin/time supervisors, not independently sampled arithmetic runtime PID.','correctness':'Every child INPUT matched independent fixed tuples; FIRST and seven ring digests independently recomputed; four arithmetic gate counts and128 exact text rows rechecked. All7 count/order bindings strict. Earlier-result/input preservation executes reviewed gates; last64 timed result checking remains partial ring scope.','training':{'duration_ns_min':min(durations),'duration_ns_max':max(durations),'limitation':'All648 elapsed training phases reach200ms; stationarity not inferred. Warm measured after observed correctness gate; observed/cached FORMAT explicit.'},'comparisons':{'file':str(report.relative_to(B)),'sha256':H(report),'cases':len(comparison),'by_peer':byPeer,'method':'Seven-sample wall/CPU/alloc medians per fresh child; matched same workload/cohort peer over Krogu. FIRST independently recalculated. All3 ratios/deltas/ranges retained; descriptive, no significance/independence/global ranking or539 threshold.'},'metrology':{'wall':'Monotonic ns / operation count; includes adapter/native operation/ring escape.','cpu':'Current-thread ns when supported; excludes other-thread CPU.','allocation':'Current-thread bytes/operation when available; not retained memory.','memory':'Heap coarse snapshots and whole-child time-l maxRSS including gate/startup/exit; not owned/per-operation peak.','RSS_bytes_range':[min(rss),max(rss)],'within_child_max_sample_ratio':max(sampleSpread)},'NA':'Ions fixed-scale and significant HALF_EVEN remain outside equivalent measured subset; raw unavailable counters remain NA.','limitations':['FIRST first explicit selected public operation after preparation; PARSE String-only, FORMAT parsed unobserved, arithmetic parsed operands. Module initialization/internal parse work may predate FIRST.','Warm repeated FORMAT may use native caches after observations; not fresh-format-value pool.','Small fixed corpus; portable targets, integrated report and remaining task requirements separate.','No new numerical run or automatic favorable repetition; all adverse and mixed aliases retained.'],'blocking_findings':[]}
p=B/'independent-jvm-timing-v2-actual-audit.json';assert not p.exists();p.write_text(json.dumps(audit,indent=2)+'\n');print(p,H(p));print(json.dumps(byPeer));print('training',caps,min(durations),max(durations))
```
