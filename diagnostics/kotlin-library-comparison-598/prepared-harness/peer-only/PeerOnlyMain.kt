package preparation598

import java.io.File

/** Dependency/API and correctness preparation only. No Krogu, timing or final comparison. */
fun main(args: Array<String>) {
    require(System.getProperty("java.vendor").contains("BellSoft", ignoreCase=true))
    require(System.getProperty("java.runtime.version")=="25.0.4+9-LTS")
    val out=File(args.single())
    check(out.parentFile.isDirectory && out.createNewFile()) { "Output must be new" }
    out.bufferedWriter().use { writer ->
        writer.appendLine("# PREPARATION_ONLY_NO_KROGU_NO_TIMING_NO_FINAL598_ACCEPTANCE")
        writer.appendLine("# runtime=${System.getProperty("java.runtime.version")} vendor=${System.getProperty("java.vendor")}")
        fun <I:Any,D:Any> peer(adapter:PeerAdapter<I,D>) {
            val report=verifyUntimed(adapter)
            for(o in report.observations) writer.appendLine(listOf("CHECK",o.peer,o.id,o.operandRelation,o.outputText,o.jdkText,o.valueParity,o.textParity).joinToString("\t"))
            for((id,reason) in report.unavailable) writer.appendLine("NA\t${adapter.id}\t$id\t$reason")
            writer.flush()
        }
        peer(JdkAdapter)
        peer(IonsPinAdapter)
        peer(KBigNumAdapter)
        writer.appendLine("COMPLETE_PEER_ONLY_UNTIMED_PREPARATION")
    }
}
