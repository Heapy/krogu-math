#!/usr/bin/env kotlinr
// SPDX-License-Identifier: Apache-2.0
// Metadata-only aggregation. Requires jq; records its version. Never executes math.
import java.io.File

val work = File("build/math-tck").canonicalFile
require(args.isNotEmpty()) { "Supply completed result directory IDs" }
val inputs = args.map { id ->
    require(id.matches(Regex("[0-9]+")))
    File(work, "results/$id/results.json").also { require(it.isFile) }
}
val directory = File(work, "summary-${args.joinToString("-")}").apply { mkdirs() }
val version = ProcessBuilder("jq", "--version").redirectErrorStream(true).start()
File(directory, "jq-version.txt").writeText(version.inputStream.bufferedReader().readText())
check(version.waitFor() == 0)
val program = """
  if any(.[]; .complete != true) then error("Incomplete input campaign") else . end |
  if ([.[].portJarSha256] | unique | length) != 1 then error("Mixed port jars") else . end |
  if ([.[].upstreamCommit] | unique | length) != 1 then error("Mixed upstream revisions") else . end |
  if ([.[].seed] | unique | length) != 1 then error("Mixed seed configurations") else . end |
  . as ${'$'}campaigns |
  [ .[].results[] ] |
  group_by([.scope, .phase, .path, (.directive // .method // .id // "")]) |
  map(
    . as ${'$'}evidence |
    sort_by(
      if .execution != null and .status == "FAIL" then 1000
      elif .execution != null and .status == "PASS" then 900
      elif .execution != null then 850
      elif .freshMemoryGateRequired == true then 800
      elif ((.reason // "") | startswith("Profile ") or startswith("Filtered ")) then 100
      else 700 end
    ) | last | . + {evidence: ${'$'}evidence}
  ) as ${'$'}results |
  {
    schema: 1,
    note: "Completed campaigns merged by exact phase/path/directive; actual FAIL dominates PASS, profile-filtered SKIP cannot replace an attempt. Scope counts are not assertions or certification.",
    portJarSha256: ${'$'}campaigns[0].portJarSha256,
    upstreamCommit: ${'$'}campaigns[0].upstreamCommit,
    seed: ${'$'}campaigns[0].seed,
    campaigns: [${'$'}campaigns[] | {phase, profiles, testsExecuted, counts, runtimeRelease, runnerSha256, ownedToolSources}],
    publicCounts: (${'$'}results | map(select(.scope == "public-tests")) | group_by(.status) | map({status: .[0].status, count: length})),
    publicByPhase: (${'$'}results | map(select(.scope == "public-tests")) | group_by([.phase,.status]) | map({phase: .[0].phase, status: .[0].status, count: length})),
    scopeCounts: (${'$'}results | group_by([.scope,.status]) | map({scope: .[0].scope, status: .[0].status, count: length})),
    results: ${'$'}results
  }
""".trimIndent()
val process = ProcessBuilder(listOf("jq", "-s", program) + inputs.map { it.path })
    .redirectOutput(File(directory, "summary.json")).redirectError(File(directory, "aggregation.stderr.log")).start()
check(process.waitFor() == 0) { File(directory, "aggregation.stderr.log").readText() }
println("Summary: ${File(directory, "summary.json").path}")
