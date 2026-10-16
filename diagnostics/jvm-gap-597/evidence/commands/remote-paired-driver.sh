set -eu
umask 077
phase=paired
art=/home/kokoko/.local/share/krogu-math/perf/artifacts/597-paired-1
run=/home/kokoko/.local/share/krogu-math/perf/runs/597-paired-1
jdk=/home/kokoko/.sdkman/candidates/java/25.0.4-librca
cd "$art"
test -x /usr/bin/python3
test -x /usr/bin/timeout
sha256sum -c transfer.sha256
test ! -e "$run"
mkdir -m 700 "$run"
cp transfer.sha256 "$run/transfer.sha256"
"$jdk/bin/java" -version > "$run/java-version.txt" 2>&1
sha256sum "$jdk/release" "$jdk/bin/java" "$jdk/lib/server/libjvm.so" > "$run/jdk.sha256"
before_hash=$(sha256sum before597.jar | cut -d ' ' -f 1)
after_hash=$(sha256sum after597.jar | cut -d ' ' -f 1)
probe_hash=$(sha256sum probe.jar | cut -d ' ' -f 1)
test "$before_hash" = 1f730223b9b5fb809c1101fd5a7f581574550fde74192369251234582e470cb6
test "$after_hash" = 47ec988d79aca123f1d8070863519776b442f7cb20ecb16860c0d0f972513203
test "$probe_hash" = d30394216cd721dcb02b2fa73ce2b3d2474dd19d78c3371a8bdd9096696f8559
printf '%s\n' 'env-i; BellSoft25.0.4+9; -Xms256m -Xmx1g -XX:+UseG1GC; exact20timingcases;3eras-before-after-JDK;900rows; current-thread CPU/allocation; sampledJFR estimates; wholeprocessVmHWM aftersetup/JFR; externalRSSNA' > "$run/policy.txt"
execute() {
 era=$1; fork=$2; mode=$3; id=$4
 case "$era" in before597) jar=before597.jar; hash=$before_hash;; after597|jdk) jar=after597.jar; hash=$after_hash;; *) exit 2;; esac
 impl=krogu
 if test "$era" = jdk; then impl=jdk; fi
 order=forward
 if test "$fork" = 1; then order=reverse; fi
 name="$era-$fork-$mode-$(printf %s "$id" | tr / _)"
 destination="$run/$name"
 sha256sum -c transfer.sha256 > "$run/$name-before-binding.txt"
 cat /proc/stat > "$run/$name-before.proc-stat.txt"
 cat /proc/loadavg > "$run/$name-before.loadavg.txt"
 cat /proc/meminfo > "$run/$name-before.meminfo.txt"
 ps -eo pid,ppid,pcpu,comm --sort=-pcpu | head -12 > "$run/$name-before.cpu-executables.txt"
 deadline=120
 if test "$mode" = timing; then deadline=300; fi
 printf '%s\n' "$jdk/bin/java" '-Xms256m' '-Xmx1g' '-XX:+UseG1GC' '-cp' "$jar:probe.jar:kotlin-stdlib.jar:kotlin-stdlib-jdk7.jar:kotlin-stdlib-jdk8.jar" 'bench.Gap597Probe' "$mode" "$impl" "$art/$jar" "$hash" "$art/probe.jar" "$probe_hash" "$fork" "$order" "$id" "$destination" > "$run/$name.argv.txt"
 started=$SECONDS
 /usr/bin/timeout --signal=TERM --kill-after=5s "$deadline" /usr/bin/env -i JAVA_HOME="$jdk" PATH=/usr/bin:/bin TZ=UTC LANG=C LC_ALL=C "$jdk/bin/java" -Xms256m -Xmx1g -XX:+UseG1GC -cp "$jar:probe.jar:kotlin-stdlib.jar:kotlin-stdlib-jdk7.jar:kotlin-stdlib-jdk8.jar" bench.Gap597Probe "$mode" "$impl" "$art/$jar" "$hash" "$art/probe.jar" "$probe_hash" "$fork" "$order" "$id" "$destination" > "$run/$name.stdout.txt" 2> "$run/$name.stderr.txt"
 printf 'exit=0\nelapsed_whole_seconds=%s\nexternal_process_peak_rss=NA_not_installed\n' "$((SECONDS-started))" > "$run/$name.process.txt"
 test -f "$destination/COMPLETE.txt"
 /usr/bin/python3 - "$destination" "$mode" "$impl" "$id" <<'CHECK597'
import csv,sys,pathlib
p=pathlib.Path(sys.argv[1]);mode,impl,case=sys.argv[2:]
labels=['small','wide','negative-scale','large-scale']
priority={f'decimal/{label}/{op}' for label in labels for op in ['divide','precision']}
expected=priority|{f'precision-first/{label}/{form}' for label in labels for form in ['string','coefficient']}|{f'precision-construction/{label}/string' for label in labels}
wanted=expected if mode=='timing' else {case}
rows=list(csv.reader((p/'samples.csv').open()))[1:]
assert len(rows)==len(wanted)*(5 if mode=='timing' else 1)
assert {r[1] for r in rows}==wanted
assert len({(r[1],r[2]) for r in rows})==len(rows)
for caseid in wanted:
 assert {int(r[2]) for r in rows if r[1]==caseid}==(set(range(5)) if mode=='timing' else {0})
for r in rows:
 assert len(r)==10 and r[0]==impl
 n,elapsed,cpu,allocated,offset,mask=int(r[3]),int(r[4]),int(r[5]),int(r[6]),int(r[8]),int(r[9])
 assert n>0 and elapsed>0 and cpu>=0 and allocated>=0 and 0<=offset<16
 expectedmask=65535 if n>=16 else sum(1<<((offset+i)&15) for i in range(n))
 assert mask==expectedmask
if mode=='profile':assert (p/'own-profile.jfr').is_file() and (p/'attribution.csv').is_file()
(p/'remote-inventory-verified.txt').write_text(f'PASS {len(rows)} exact rows; mode={mode}\n')
CHECK597
 cat /proc/stat > "$run/$name-after.proc-stat.txt"
 cat /proc/loadavg > "$run/$name-after.loadavg.txt"
 cat /proc/meminfo > "$run/$name-after.meminfo.txt"
 sha256sum -c transfer.sha256 > "$run/$name-after-binding.txt"
 printf 'PASS %s\n' "$name"
}
for fork in 0 1 2; do
 eras='before597 after597 jdk'
 if test "$fork" = 1; then eras='jdk after597 before597'; fi
 for era in $eras; do execute "$era" "$fork" timing all; done
done
sha256sum -c "$run/jdk.sha256" > "$run/jdk-after-verified.txt"
printf 'PASS900rows ninefreshprocesses;independentpairedanalysispending\n' > "$run/COMPLETE.txt"
