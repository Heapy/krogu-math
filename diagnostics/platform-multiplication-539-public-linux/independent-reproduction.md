# Reproduce the independent public Linux raw audit

Read-only after immutable artifact extraction. Run from the project root. This verifies archive/API/closure, source epoch context, all raw/checksums/statistics/CSV/CPU observations; it prints recomputed JSON and does not overwrite accepted audits or execute a numerical benchmark. The separate source/tooling supplement binds source TSV records to exact Git blobs. No private payload is used.

```sh
python3 - <<'PY'
from pathlib import Path
import json,hashlib,zipfile,statistics,re,csv,math
p=Path('.codex-work/539');g=p/'public-linux-guard-actual-39009038930/guard';r=g/'run';sha=lambda q:hashlib.sha256(Path(q).read_bytes()).hexdigest();summary=json.loads((g/'paired-summary.json').read_text());context=dict(l.split('=',1) for l in (g/'context.txt').read_text().splitlines());head='67acda4549e38f235bb842aedeb570f8e1220cea'
archive=p/'public-linux-guard-67acda4-artifact.zip';assert archive.stat().st_size==18585761 and sha(archive)=='b702abbe93502159afb1a1892acc360b9eaaf3f60cc7b4925ce86981a20943c0';api=p/'public-linux-guard-67acda4-artifacts.json';artifact=json.loads(api.read_text())['artifacts'][0];assert artifact['workflow_run']['id']==39009038930 and artifact['workflow_run']['head_sha']==head and artifact['digest']=='sha256:'+sha(archive)
status=p/'public-linux-guard-67acda4-status.json';st=json.loads(status.read_text());assert st['conclusion']=='success' and st['headSha']==head and st['jobs'][0]['conclusion']=='success'
with zipfile.ZipFile(archive) as z:
 for zi in z.infolist():
  q=g.parent/zi.filename;assert q.is_file() and q.stat().st_size==zi.file_size and hashlib.sha256(z.read(zi)).hexdigest()==sha(q)
assert context['baseline_ref']=='07910edf6d5687f1d89dbe3369c4546f648feed2' and context['current_ref']=='975182f9bca911be86ef1bc4efedc7039cc8fe36' and context['tooling_ref']==head
assert sha(g/'current-source.tsv')==context['source_manifest_sha256']==summary['source_manifest_sha256'];assert summary['baseline_artifact_manifest_sha256']==context['baseline_manifest'] and summary['current_artifact_manifest_sha256']==context['current_manifest']
closure=[]
for label in ['baseline','current']:
 root=g/'payload'/label;rows=[l.split('\t') for l in (root/'manifest.sha256').read_text().splitlines()];names=[n for h,n in rows];assert len(names)==len(set(names))==1089
 actual={q.relative_to(root).as_posix() for q in root.rglob('*') if q.is_file()};assert actual==set(names)|{'manifest.sha256'} and sha(root/'manifest.sha256')==context[label+'_manifest']
 for h,n in rows:assert sha(root/n)==h and not (root/n).is_symlink()
 hidden=[n for n in names if any(c.startswith('.') for c in Path(n).parts)];assert len(hidden)==9;closure.append({'label':label,'listed_files':1089,'total_with_manifest':1090,'hidden_files_retained':hidden,'manifest_sha256':sha(root/'manifest.sha256')})
assert sha(g/'payload/check-public-paired-guard.py')=='cbde6eeb0920c65952817ca374d5b2562139daebe249b8c166c13766b20db32d';assert sha(g/'payload/preflight-adoption-protocol.json')=='eb49f03d317e1a8c60c9ce272fd9541d333ae0635f9b7e31e8948a68d1e26fc4'
with zipfile.ZipFile(p/'public-linux-guard-67acda4-logs.zip') as z:
 build=z.read('diagnostic/8_Build two clean public source epochs sequentially.txt').decode();assert build.count('BUILD SUCCESSFUL')==2 and build.count('PINNED_NODE539 project=: type=org.jetbrains.kotlin.gradle.targets.js.nodejs.NodeJsPlugin version=24.16.0 download=false')==2
 install=z.read('diagnostic/7_Pin an owned official Linux Node runtime and external build configuration.txt').decode();assert 'node.tar.xz: OK' in install
bits=[1,30,63,64,127,511,1023];order=[f'{api}/{b}/{shape}' for b in bits for shape in ['distinct','square'] for api in ['public','parallel-public']];assert len(order)==28
values={}
for b in bits:
 a=1<<(b-1);a+=b!=1
 for shape,v in [('distinct',a*(a+2)),('square',a*a)]:
  for api in ['public','parallel-public']:values[f'{api}/{b}/{shape}']=v
def signed32(n):return (n+(1<<31))%(1<<32)-(1<<31)
def hash_integer(v):
 words=[]
 while v:words.append(v&0xffffffff);v>>=32
 h=0
 for w in words[::-1]:h=signed32(31*h+w)
 return h
def ring(v):
 h=1;hv=hash_integer(v)
 for i in range(64):h=signed32(31*h+hv)
 return h
events=[json.loads(l) for l in (r/'events.jsonl').read_text().splitlines()];assert sum(e['type']=='CHILD_START' for e in events)==sum(e['type']=='CHILD_TERMINAL' for e in events)==6 and sum(e['type']=='PAIR_START' for e in events)==sum(e['type']=='PAIR_END' for e in events)==3
starts=[e for e in events if e['type']=='CHILD_START'];assert [(e['fork'],e['label']) for e in starts]==[(0,'baseline'),(0,'current'),(1,'current'),(1,'baseline'),(2,'baseline'),(2,'current')]
forks={};raw=[];pids=set();total=0
for f in range(3):
 for label in ['baseline','current']:
  q=r/f'fork{f}-{label}.log';lines=q.read_text().splitlines();binding=dict(l.split('=',1) for l in Path(str(q)+'.binding').read_text().splitlines());own=[l for l in lines if l.startswith('# OWN539 ')];assert len(own)==1;pid=int(re.search(r'pid=(\d+)',own[0]).group(1));start=int(re.search(r'start_epoch_ms=(\d+)',own[0]).group(1));assert pid not in pids;pids.add(pid);assert binding['pid']==str(pid) and binding['start_epoch_ms']==str(start) and binding['log_sha256']==sha(q) and binding['source_manifest_sha256']==context['source_manifest_sha256'] and binding['artifact_manifest_sha256']==context[label+'_manifest'];assert Path(str(q)+'.exit').read_text().strip()=='0'
  assert 'node=v24.16.0 kind=JS flags=["--expose-gc"]' in own[0];assert sum(l.startswith('# REGISTERED539 selected=1 ') for l in lines)==1;assert '# SELECTED539 tests=1 passes=1 failures=0 pending=0' in lines
  expected=order[::-1] if f==1 else order;rep=[l.split(',') for l in lines if l.startswith('REP539,')];checks=[l.split(',') for l in lines if l.startswith('CHECK539,')];times=[l.split(',') for l in lines if l.startswith('TIME539,')];assert len(rep)==len(checks)==28 and len(times)==196;assert [x[1] for x in checks]==expected and all(len(x)==3 and x[2]=='PASS' for x in checks);assert [x[1] for x in rep]==expected and all(len(x)==3 and int(x[2])==values[x[1]] for x in rep)
  grouped={}
  for case in expected:
   rows=[x for x in times if x[1]==case];assert len(rows)==7 and [int(x[2]) for x in rows]==list(range(7))
   # TIME schema has nine comma-separated fields: marker,id,sample,count,wall,cpu,allocation,heap,digest.
   assert all(len(x)==9 and int(x[3])>=64 and int(x[4])>0 and int(x[5])>=0 and x[6]=='NA' and int(x[7])>=0 and int(x[8])==ring(values[case]) for x in rows)
   assert len({x[3] for x in rows})==1
   wall=statistics.median(int(x[4])/int(x[3]) for x in rows);cpu=statistics.median(int(x[5])/int(x[3]) for x in rows);owner=summary['forks'][f'{f}/{label}']['cases'][case];assert wall==owner['wall_ns'] and cpu==owner['cpu_ns'] and owner['caller_allocated_bytes'] is None and owner['cpu_available_samples']==7 and owner['allocation_available_samples']==0;grouped[case]={'wall':wall,'cpu':cpu}
  forks[f,label]=grouped;total+=len(times)
  e=next(x for x in starts if x['fork']==f and x['label']==label);assert e['pid']==pid;end=next(x for x in events if x['type']=='CHILD_TERMINAL' and x['log']==q.name);assert end['exit']==0 and end['pid']==pid and end['start_epoch_ms']==start
  raw.append({'fork':f,'label':label,'pid':pid,'start_epoch_ms':start,'log_sha256':sha(q),'binding_sha256':sha(Path(str(q)+'.binding')),'start_ticks':e['start_ticks']})
assert total==1176 and summary['TIME']==1176 and summary['REP']==summary['CHECK']==168
cases=[];flags=[]
csvrows={x['case']:x for x in csv.DictReader((g/'paired-summary.csv').open())}
for name in sorted(order):
 b=[forks[f,'baseline'][name]['wall'] for f in range(3)];c=[forks[f,'current'][name]['wall'] for f in range(3)];ratios=[y/x for x,y in zip(b,c)];deltas=[y-x for x,y in zip(b,c)];mr=statistics.median(ratios);md=statistics.median(deltas);flag=mr>1.15 and md>5;owner=next(x for x in summary['cases'] if x['case']==name);assert owner['baseline_fork_wall_ns']==b and owner['current_fork_wall_ns']==c and owner['paired_ratios']==ratios and owner['paired_deltas_ns']==deltas and owner['median_paired_ratio']==mr and owner['median_paired_delta_ns']==md and owner['investigate_15percent_AND_5ns']==flag
 cr=csvrows[name];assert float(cr['median_paired_ratio'])==mr and float(cr['median_paired_delta_ns'])==md and (cr['investigate_15percent_AND_5ns']=='True')==flag
 for f in range(3):assert float(cr[f'pair{f}_ratio'])==ratios[f] and float(cr[f'pair{f}_delta_ns'])==deltas[f]
 cpu_ratios=[forks[f,'current'][name]['cpu']/forks[f,'baseline'][name]['cpu'] for f in range(3)]
 cases.append({'case':name,'paired_wall_ratios':ratios,'paired_wall_deltas_ns':deltas,'median_ratio':mr,'median_delta_ns':md,'paired_cpu_ratios':cpu_ratios,'all3_slower':all(x>1 for x in ratios),'flag':flag})
 if flag:flags.append(name)
assert flags==summary['guard_flags']==[]
host=json.loads((r/'host.jsonl').read_text());assert host['node_sha256']==context['node_sha256']=='b2959781cc5a74c357ffa02367efa8a0330cbb1c9cb347732fdfaaaca381cbcd' and host['node']=='v24.16.0' and host['arch']=='x64';hz=host['CLK_TCK'];assert hz==100
samples=[json.loads(l) for l in (r/'cpu-samples.jsonl').read_text().splitlines()];processes=[json.loads(l) for l in (r/'cpu-processes.jsonl').read_text().splitlines()];assert len(samples)==len(processes)==111
intervals=[];ext=[];busy=[];churn=0;reset_total=0;residual=0;prev=None;rss=[]
for i,(s,pr) in enumerate(zip(samples,processes)):
 assert s['sample']==pr['sample']==i
 records={int(x['pid']):x for x in pr['records']};assert len(records)==len(pr['records']) and records[host['driver_pid']]['start']==host['driver_start_ticks'];owned={host['driver_pid']}
 while True:
  new=owned|{pid for pid,x in records.items() if x['ppid'] in owned}
  if new==owned:break
  owned=new
 assert all(x['owned_ancestry']==(pid in owned) for pid,x in records.items())
 if prev:
  old,old_owned,old_s=prev;dt=int(s['mono_end_ns'])-int(old_s['mono_end_ns']);assert dt==int(s['interval_ns']);intervals.append(dt/1e9);external=observed=missing=reset=0
  for pid,x in records.items():
   ox=old.get(pid)
   if ox and ox['start']==x['start'] and int(x['cpu'])>=int(ox['cpu']):
    delta=int(x['cpu'])-int(ox['cpu'])
    if pid in owned:observed+=delta
    else:external+=delta
   else:
    if ox and ox['start']==x['start']:reset+=1
    if pid not in owned:missing+=1
  missing+=sum(pid not in records and pid not in old_owned for pid in old);assert str(external)==s['external_matched_ticks'] and str(observed)==s['owned_matched_ticks'] and reset==s['counter_reset_count'];assert s['counter_incomplete_count']>=missing;residual+=s['counter_incomplete_count']-missing;churn+=missing;reset_total+=reset
  cores=external*1e9/hz/dt;assert math.isclose(cores,s['external_observed_cores'],rel_tol=1e-13,abs_tol=1e-13);ext.append(cores)
  ds=[int(x)-int(y) for x,y in zip(s['system_ticks_first8'],old_s['system_ticks_first8'])];assert all(v>=0 for v in ds);fraction=(sum(ds)-ds[3]-ds[4])/sum(ds);assert math.isclose(fraction,s['system_busy_fraction'],rel_tol=1e-13,abs_tol=1e-13);busy.append(fraction)
 resource=s['owned_child_resource']
 if resource:
  assert resource['pid'] in pids and records[resource['pid']]['start']==resource['start_ticks'];rss.append(resource['VmHWM_bytes'])
 prev=records,owned,s
pre=[e for e in events if e['type'].startswith('PRE_OBSERVATION_')];assert len(pre)==2;preduration=(int(pre[1]['mono_ns'])-int(pre[0]['mono_ns']))/1e9;assert 15<=preduration<16 and max(intervals)<=5
old=json.loads((p/'actual-public-node-js-paired-small-5-summary.json').read_text());oldcomparison=[]
for name in old['guard_flags']:
 oc=next(c for c in old['cases'] if c['case']==name);nc=next(c for c in cases if c['case']==name);oldcomparison.append({'case':name,'old_macos_guard5_ratios':oc['paired_ratios'],'old_macos_guard5_deltas_ns':oc['paired_deltas_ns'],'new_linux_ratios':nc['paired_wall_ratios'],'new_linux_deltas_ns':nc['paired_wall_deltas_ns'],'new_linux_cpu_ratios':nc['paired_cpu_ratios'],'new_linux_trigger':nc['flag']})
out={'status':'PASS_ACTUAL_PUBLIC_SOURCE_BUILT_LINUX_PAIRED28_DATA_AND_SCOPE','run_id':39009038930,'tooling_head':head,'archive_sha256':sha(archive),'artifact_id':artifact['id'],'context':context,'closure':closure,'counts':{'TIME':1176,'REP':168,'CHECK':168,'cases':28,'fresh_children':6},'raw_bindings':raw,'cases':cases,'guard_flags':flags,'all3_adverse':[c for c in cases if c['all3_slower']],'cpu_timeline':{'samples':111,'max_interval_seconds':max(intervals),'preobservation_seconds':preduration,'external_observed_cores_range':[min(ext),max(ext)],'system_busy_fraction_range':[min(busy),max(busy)],'recorded_churn_lower_bound':churn,'counter_resets':reset_total,'inaccessible_residual_count':residual,'sampled_child_VmHWM_bytes_range':[min(rss),max(rss)],'scope':'Matched process counter lower bounds, continuous descriptive sampling; churn/gaps disclosed, no isolation/causality/admission claim. Sampled processVmHWM not completed lifetime or peroperation/owned retained memory.'},'old_mac_six_flag_comparison':oldcomparison,'summary_sha256':sha(g/'paired-summary.json'),'cpu_samples_sha256':sha(r/'cpu-samples.jsonl'),'cpu_processes_sha256':sha(r/'cpu-processes.jsonl'),'events_sha256':sha(r/'events.jsonl'),'limits':['Linux rebuiltsource07910/975 uses new artifacts/manifests and x64Node24.16, not aa9mac/claws binary identity.','Both all3adverse rows retained under frozen15percentAND5ns rule; no criterion rewrite or automatic zero-regression inference.','Old6macflags, local99668NO_NUMERIC and pre-numeric39008435845 failure remain their original outcomes; new result is not causal recovery proof.','Reviewer only read/hashes/recomputed statistics; no benchmark, build or private payload export.']}
print(json.dumps(out,indent=2))
PY
```

