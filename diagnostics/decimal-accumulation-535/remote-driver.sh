set -eu
umask 077
art=/home/kokoko/.local/share/krogu-math/perf/artifacts/535-1
run=/home/kokoko/.local/share/krogu-math/perf/runs/535-2
jdk=/home/kokoko/.sdkman/candidates/java/25.0.4-librca
cd "$art"
sha256sum -c transfer.sha256
test ! -e "$run"
mkdir -m 700 "$run"
cp transfer.sha256 "$run/transfer.sha256"
"$jdk/bin/java" -version > "$run/java-version.txt" 2>&1
sha256sum "$jdk/release" "$jdk/bin/java" "$jdk/lib/server/libjvm.so" > "$run/jdk.sha256"
printf '%s\n' 'env -i JAVA_HOME=pinned PATH=/usr/bin:/bin TZ=UTC LANG=C LC_ALL=C' '-Xms256m -Xmx1g -XX:+UseG1GC -cp runner.jar:kotlin-stdlib.jar Accumulator535Runner candidate.jar output.csv order candidateSHA' 'deadline=300s;kill-after=5s;3 serial fresh processes; orders forward/reverse/forward; same artifact' > "$run/invocation-policy.txt"
for fork in 0 1 2; do
 order=forward
 if test "$fork" = 1; then order=reverse; fi
 cat /proc/stat > "$run/$fork-before.proc-stat.txt"
 cat /proc/loadavg > "$run/$fork-before.loadavg.txt"
 cat /proc/meminfo > "$run/$fork-before.meminfo.txt"
 ps -eo pid,ppid,pcpu,comm --sort=-pcpu | head -12 > "$run/$fork-before.cpu-executables.txt"
 date -u +%Y-%m-%dT%H:%M:%SZ > "$run/$fork-start.txt"
 started=$SECONDS
 /usr/bin/timeout --signal=TERM --kill-after=5s 300s /usr/bin/env -i JAVA_HOME="$jdk" PATH=/usr/bin:/bin TZ=UTC LANG=C LC_ALL=C "$jdk/bin/java" -Xms256m -Xmx1g -XX:+UseG1GC -cp runner.jar:kotlin-stdlib.jar Accumulator535Runner candidate.jar "$run/$fork.csv" "$order" 1f730223b9b5fb809c1101fd5a7f581574550fde74192369251234582e470cb6 > "$run/$fork.stdout.txt" 2> "$run/$fork.stderr.txt"
 printf "exit=0\nelapsed_whole_seconds=%s\npeak_process_rss=NA_external_time_not_installed\n" "$((SECONDS-started))" > "$run/$fork.process.txt"
 date -u +%Y-%m-%dT%H:%M:%SZ > "$run/$fork-end.txt"
 cat /proc/stat > "$run/$fork-after.proc-stat.txt"
 cat /proc/loadavg > "$run/$fork-after.loadavg.txt"
 cat /proc/meminfo > "$run/$fork-after.meminfo.txt"
 printf 'fork %s PASS\n' "$fork"
done
sha256sum -c transfer.sha256
printf 'PASS three fresh535 processes\n' > "$run/COMPLETE.txt"
printf 'COMPLETE535\n'
