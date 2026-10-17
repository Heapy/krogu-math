// Shared controller/checker metadata only. No peer code is loaded here.
data class Work598(val peer:String,val id:String)
fun catalog598(platform:String):List<Work598> {
    require(platform in listOf("JVM","JS","Wasm","Native"))
    val peers=if(platform=="JVM") listOf("jdk-bellsoft-25.0.4+9","krogu-final539-PENDING","ionspin-bignum-0.3.10","kbignum-0.0.20-desktop")
        else listOf("krogu-final539-PENDING","ionspin-bignum-0.3.10")
    val ids=listOf("int" to listOf("zero","signed","carry30","wide10"),"decimal" to listOf("zero","scale","signed","wide10"))
        .flatMap { (kind,names)->names.flatMap { n->listOf("ADD","SUBTRACT","MULTIPLY","self","PARSE","FORMAT").map { "$kind/$n/$it" } } }.sorted()
    val core=ids.flatMap { id->peers.map { Work598(it,id) } }
    val fixed=if(platform=="JVM") listOf("positive-tie","negative-tie","negative-divisor","both-negative","nonterminating","carry","zero","exact")
        .flatMap { id->peers.filter { it!="ionspin-bignum-0.3.10" }.map { Work598(it,"fixed-scale/$id") } } else emptyList()
    return (core+fixed).sortedWith(compareBy<Work598>{it.id}.thenBy{peers.indexOf(it.peer)}).also { check(it.size==if(platform=="JVM")216 else 96) }
}

// Independent fixture tuples and checksums, not calls into a numerical peer.
data class Expected598(val left:String,val right:String,val expected:String,val canonical:String,val family:String,val self:Boolean)
fun canonical598(text:String):String {
    require(text.length in 1..10000)
    val m=Regex("([+-]?)([0-9]+)(?:\\.([0-9]+))?(?:[eE]([+-]?[0-9]+))?").matchEntire(text)?:error("Invalid decimal observation")
    val fraction=m.groupValues[3];val exponent=m.groupValues[4].ifEmpty{"0"}.toLong().also{require(it in -1000000L..1000000L)}
    val coefficient=(m.groupValues[2]+fraction).trimStart('0');if(coefficient.isEmpty())return "0@0"
    val digits=coefficient.trimEnd('0');val scale=fraction.length.toLong()-exponent-(coefficient.length-digits.length)
    return (if(m.groupValues[1]=="-")"-" else "")+digits+"@"+scale
}
fun checksum598(text:String)=text.fold(0){h,c->31*h+c.code}
fun expected598(id:String):Expected598 {
    val p=id.split('/')
    if(p[0]=="fixed-scale") {
        require(p.size==2)
        val f=when(p[1]) {
            "positive-tie"->listOf("1","8","0.13")
            "negative-tie"->listOf("-1","8","-0.13")
            "negative-divisor"->listOf("1","-8","-0.13")
            "both-negative"->listOf("-1","-8","0.13")
            "nonterminating"->listOf("1","3","0.33333333333333333333")
            "carry"->listOf("9.995","1","10.00")
            "zero"->listOf("0.000","7","0.0000")
            "exact"->listOf("12.00","3","4.00")
            else->error("Unknown fixed fixture")
        }
        return Expected598(f[0],f[1],f[2],canonical598(f[2]),"decimal",false)
    }
    require(p.size==3)
    if(p[2] in listOf("PARSE","FORMAT")) {
        val text=when("${p[0]}/${p[1]}") {
            "int/zero"->"0"; "int/signed"->"-7"; "int/carry30"->"1073741823"; "int/wide10"->"100000000000000000000"
            "decimal/zero"->"0"; "decimal/scale"->"1.2"; "decimal/signed"->"-1.25"; "decimal/wide10"->"100000000000000000001.25"
            else->error("Unknown text fixture")
        }
        return Expected598(text,text,text,text,"text",false)
    }

    val f=when("${p[0]}/${p[1]}") {
        "int/zero"->listOf("0","0","0","0","0","0")
        "int/signed"->listOf("-7","3","-4","-10","-21","49")
        "int/carry30"->listOf("1073741823","1","1073741824","1073741822","1073741823","1152921502459363329")
        "int/wide10"->listOf("100000000000000000000","-10","99999999999999999990","100000000000000000010","-1000000000000000000000","1"+"0".repeat(40))
        "decimal/zero"->listOf("0.00","-0.000","0.000","0.000","0.00000","0.0000")
        "decimal/scale"->listOf("1.20","3.400","4.600","-2.200","4.08000","1.4400")
        "decimal/signed"->listOf("-1.25","0.05","-1.20","-1.30","-0.0625","1.5625")
        "decimal/wide10"->listOf("100000000000000000000.0","-10.00","99999999999999999990.00","100000000000000000010.00","-1000000000000000000000.000","1"+"0".repeat(40)+".00")
        else->error("Unknown exact fixture")
    }
    val expected=f[when(p[2]){"ADD"->2;"SUBTRACT"->3;"MULTIPLY"->4;"self"->5;else->error("Unknown operation")}]
    return Expected598(f[0],if(p[2]=="self")f[0] else f[1],expected,if(p[0]=="int")expected else canonical598(expected),p[0],p[2]=="self")
}
fun ringChecksum598(value:String,count:Int):Int {var h=0;repeat(minOf(count,64)){h=31*h+checksum598(value)};return h}
