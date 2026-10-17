package preparation598

import java.io.File

/** Correctness ONLY, no benchmark. UNCOMPILED/UNRUN preparation. */
fun main(args: Array<String>) {
    require(System.getProperty("java.vendor").contains("BellSoft", ignoreCase = true))
    require(System.getProperty("java.runtime.version") == "25.0.4+9-LTS")
    val output = File(args.single())
    check(output.parentFile.isDirectory && output.createNewFile()) { "Output must be new" }
    output.bufferedWriter().use { writer ->
        writer.appendLine("# UNTIMED correctness; no performance or full598 acceptance")
        writer.appendLine("# runtime=${System.getProperty("java.runtime.version")} vendor=${System.getProperty("java.vendor")}")
        fun <I : Any, D : Any> peer(adapter: PeerAdapter<I, D>) {
            val report = verifyUntimed(adapter) // Any mismatch throws; partial file remains without COMPLETE.
            for (o in report.observations) writer.appendLine(listOf("CHECK", o.peer, o.id,
                o.operandRelation, o.outputText, o.jdkText, o.valueParity, o.textParity).joinToString("\t"))
            for ((id, reason) in report.unavailable) writer.appendLine("NA\t${adapter.id}\t$id\t$reason")
            verifyTextWorkloads598(adapter)
            writer.flush()
        }
        peer(JdkAdapter); peer(KroguAdapter); peer(IonsPinAdapter); peer(KBigNumAdapter)
        writer.appendLine("COMPLETE_UNTIMED_ONLY")
    }
}
