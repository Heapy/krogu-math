#!/usr/bin/env kotlinr
@file:DependsOn("org.jetbrains.kotlinx:kotlinx-serialization-json-jvm:1.11.0")
import java.io.File
import kotlinx.serialization.json.*

require(args.size == 1) { "Usage: check-results.main.kts <standard-report-directory>" }
val root = File(args.single())
val metadata = Json.parseToJsonElement(root.resolve("run.json").readText()).jsonObject
val target = metadata.getValue("platform").jsonPrimitive.content
check(target in listOf("jvm", "js", "wasmJs", "macosArm64", "linuxX64", "linuxArm64", "mingwX64"))
val profile = metadata.getValue("profile").jsonPrimitive.content
check(profile in listOf("main", "smoke"))
val workloads = buildSet {
    for (digits in listOf(9, 77, 309, 1234)) for (op in listOf("parse", "format", "compare", "multiply", "square", "divide")) add("integer/$digits/$op")
    for (shape in listOf("small", "wide", "negative-scale", "large-scale")) for (op in listOf("parse", "format", "precision", "compare", "multiply", "square", "divide", "round", "setScale", "chain")) add("decimal/$shape/$op")
}
val module = metadata.getValue("module").jsonPrimitive.content
val representative = "-smoke-" in module
val fixedOnly = module == "fixed-smoke-jvm"
val peerSuite = module.startsWith("peer-") || fixedOnly
val completeInventory: Set<Pair<String, Map<String,String>>> = if (!peerSuite) {
    val classes = if (target == "jvm") listOf("ArithmeticBenchmark", "JdkArithmeticBenchmark") else listOf("ArithmeticBenchmark")
    classes.flatMap { name -> workloads.map { "bench.$name.operation" to mapOf("workload" to it) } }.toSet()
} else buildSet {
    val ids = listOf("int" to listOf("zero", "signed", "carry30", "wide10"), "decimal" to listOf("zero", "scale", "signed", "wide10"))
        .flatMap { (family, names) -> names.flatMap { name -> listOf("ADD", "SUBTRACT", "MULTIPLY", "self", "PARSE", "FORMAT").map { "$family/$name/$it" } } }
    for (id in ids) {
        for (peer in listOf("krogu", "ions")) add("preparation598.PeerBenchmark.operation" to mapOf("workload" to id, "peer" to peer))
        if (target == "jvm") for (peer in listOf("jdk", "kbignum")) add("preparation598.JvmPeerBenchmark.operation" to mapOf("workload" to id, "peer" to peer))
    }
    if (target == "jvm") for (id in listOf("positive-tie", "negative-tie", "negative-divisor", "both-negative", "nonterminating", "carry", "zero", "exact"))
        for (peer in listOf("krogu", "jdk", "kbignum")) add("preparation598.FixedScaleBenchmark.operation" to mapOf("workload" to "fixed-scale/$id", "peer" to peer))
}
val selectedWorkloads = when {
    fixedOnly -> setOf("fixed-scale/positive-tie")
    !representative -> null
    peerSuite -> setOf("int/carry30/MULTIPLY", "int/wide10/PARSE", "decimal/wide10/FORMAT", "decimal/scale/ADD")
    else -> setOf("integer/9/parse", "integer/77/format", "decimal/wide/multiply")
}
val expected = completeInventory.filter { (name, parameters) ->
    (selectedWorkloads == null || parameters.getValue("workload") in selectedWorkloads) &&
        (!representative || fixedOnly || !name.contains("FixedScaleBenchmark"))
}.toSet()
val rows = Json.parseToJsonElement(root.resolve("results.json").readText()).jsonArray
val actual = rows.map { row ->
    val value = row.jsonObject
    val params = value.getValue("params").jsonObject.mapValues { it.value.jsonPrimitive.content }
    check(params.keys == if (peerSuite) setOf("workload", "peer") else setOf("workload"))
    value.getValue("benchmark").jsonPrimitive.content to params
}
check(actual.size == expected.size && actual.toSet() == expected) { "Missing, duplicate or unexpected benchmark/parameter rows" }
for (row in rows) {
    val value = row.jsonObject
    check(value.getValue("warmupIterations").jsonPrimitive.int == if (profile == "smoke") 1 else 5)
    check(value.getValue("measurementIterations").jsonPrimitive.int == if (profile == "smoke") 1 else 7)
    val metric = value.getValue("primaryMetric").jsonObject
    check(metric.getValue("scoreUnit").jsonPrimitive.content == "ns/op")
    check(metric.getValue("score").jsonPrimitive.double.let { it.isFinite() && it >= 0 })
    val samples = metric.getValue("rawData").jsonArray
    check(samples.size == if (target == "jvm" && !representative) 3 else 1) { "Unexpected raw fork count" }
    check(samples.all { it.jsonArray.size == if (profile == "smoke") 1 else 7 }) { "Incomplete raw measurement samples" }
    check(samples.all { fork -> fork.jsonArray.isNotEmpty() && fork.jsonArray.all { it.jsonPrimitive.double.let { x -> x.isFinite() && x >= 0 } } })
}
println("PASS standard inventory: ${rows.size} results; target=$target; profile=$profile. No latency threshold or stability claim.")
