package preparation598

import java.io.File

/** Only new text API semantics; no Krogu, timers, benchmark or full598 acceptance. */
fun main(args:Array<String>) {
    check(System.getProperty("java.vendor").contains("BellSoft",true))
    check(System.getProperty("java.runtime.version")=="25.0.4+9-LTS")
    val out=File(args.single());check(out.parentFile.isDirectory && out.createNewFile())
    var checks=0;var failures=0
    out.bufferedWriter().use { w ->
        w.appendLine("# PREPARATION_ONLY_TEXT_APIS_NO_KROGU_NO_TIMING")
        fun <I:Any,D:Any> peer(a:PeerAdapter<I,D>) {
            for((family,names) in listOf("int" to listOf("zero","signed","carry30","wide10"),"decimal" to listOf("zero","scale","signed","wide10")))
                for(name in names) for(op in listOf("PARSE","FORMAT")) {
                    val id="$family/$name/$op";val case=textCase598(a,id)
                    val retained=mutableListOf<Any>()
                    repeat(2) { repetition ->
                        val result=case.operation();retained+=result
                        try {
                            val observed=case.checkResult(result);case.checkInput()
                            retained.forEach { check(case.checkResult(it)==case.input) }
                            w.appendLine("CHECK_TEXT598\t${a.id}\t$id\t$repetition\t${case.input}\t$observed")
                            checks++
                        } catch(e:IllegalStateException) {
                            failures++;w.appendLine("FAIL_TEXT598\t${a.id}\t$id\t$repetition\t${e.message}")
                        }
                    }
                }
        }
        peer(JdkAdapter);peer(IonsPinAdapter);peer(KBigNumAdapter)
        w.appendLine("TEXT598_COUNTS\t$checks\t$failures")
        if(failures==0)w.appendLine("COMPLETE_TEXT598_PEER_ONLY")
    }
    check(checks==96 && failures==0) { "Text semantics mismatch: $checks checks / $failures failures" }
}
