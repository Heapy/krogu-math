package preparation598

/** Public text APIs only. No normalization/peer arithmetic inside measured closures. */
internal data class TextPrepared598(
    val input: String, val operation: () -> Any,
    val checkResult: (Any) -> String, val checkInput: () -> Unit,
)
internal fun textInput598(family: String, name: String): String = when ("$family/$name") {
    "int/zero" -> "0"
    "int/signed" -> "-7"
    "int/carry30" -> "1073741823"
    "int/wide10" -> "100000000000000000000"
    "decimal/zero" -> "0"
    "decimal/scale" -> "1.2"
    "decimal/signed" -> "-1.25"
    "decimal/wide10" -> "100000000000000000001.25"
    else -> error("Unknown text fixture")
}
internal fun <I: Any,D: Any> textCase598(adapter: PeerAdapter<I,D>, id: String): TextPrepared598 {
    val p=id.split('/');require(p.size==3 && p[2] in listOf("PARSE","FORMAT"))
    val input=textInput598(p[0],p[1])
    fun <V:Any> typed(parse:(String)->V, text:(V)->String):TextPrepared598 {
        // FORMAT input is parsed once, never formatted before FIRST. PARSE has no prepared native input.
        val value=if(p[2]=="FORMAT") parse(input) else null
        return if(p[2]=="PARSE") TextPrepared598(input,{parse(input)},
            {result -> @Suppress("UNCHECKED_CAST") val observed=text(result as V)
                // Text fixtures deliberately use unique canonical expanded spellings.
                check(observed==input) { "${adapter.id}:$id non-equivalent parse observation: $observed vs $input" }; observed },
            {check(input==textInput598(p[0],p[1]))})
        else TextPrepared598(input,{text(checkNotNull(value))},
            {result -> check(result is String && result==input) { "${adapter.id}:$id non-equivalent format:$result vs $input" }; result },
            {check(text(checkNotNull(value))==input) { "Format input changed/non-equivalent" }})
    }
    return when(p[0]) {
        "int" -> typed(adapter::integer,adapter::integerText)
        "decimal" -> typed(adapter::decimal,adapter::decimalText)
        else -> error("Unknown text family")
    }
}

/** Additive full correctness gate. Existing arithmetic/capability gates remain unchanged. */
internal fun <I:Any,D:Any> verifyTextWorkloads598(adapter:PeerAdapter<I,D>):Int {
    var count=0
    val retained=mutableListOf<Pair<TextPrepared598,Any>>()
    for((family,names) in listOf("int" to listOf("zero","signed","carry30","wide10"),"decimal" to listOf("zero","scale","signed","wide10")))
        for(name in names) for(op in listOf("PARSE","FORMAT")) {
            val id="$family/$name/$op";val case=textCase598(adapter,id)
            repeat(2) { repetition ->
                val result=case.operation();val observed=case.checkResult(result);case.checkInput()
                retained.forEach { (old,value) -> check(old.checkResult(value)==old.input);old.checkInput() }
                retained+=case to result
                println("CHECK_TEXT598\t${adapter.id}\t$id\t$repetition\t${case.input}\t$observed")
                count++
            }
        }
    check(count==32)
    println("TEXT_GATE598_COUNT\t${adapter.id}\t$count")
    return count
}
