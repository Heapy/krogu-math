package io.heapy.krogu.math

// Input-selected compatibility policy for nonconforming callback observations.
// Never infer membership from the returned value. See the finite proof.
internal object FiniteZeroPolicy567 {
    val roundKeys: Set<List<String>> = buildSet {
        for (behavior in listOf("super", "sequence")) for (mode in RoundingMode.entries) {
            for (report in listOf(-1, 0, 3, 9)) {
                val precisions = when (report) { -1, 0 -> listOf(0, 1, 7); 3 -> listOf(0, 7); else -> listOf(0, 1) }
                for (precision in precisions)
                    add(listOf("S", behavior, "0.00", "$report", "$precision", mode.name))
            }
            if (mode in listOf(RoundingMode.DOWN, RoundingMode.CEILING, RoundingMode.HALF_UP, RoundingMode.HALF_DOWN, RoundingMode.HALF_EVEN))
                add(listOf("S", behavior, "-123.456", "9", "1", mode.name))
            if (mode in listOf(RoundingMode.DOWN, RoundingMode.FLOOR, RoundingMode.HALF_UP, RoundingMode.HALF_DOWN, RoundingMode.HALF_EVEN))
                add(listOf("S", behavior, "999.99", "9", "1", mode.name))
        }
    }
    val directUnaryKeys: Set<List<String>> = buildSet {
        for (sign in listOf(-1, 1)) for (precision in listOf(0, 1, 7)) for (mode in listOf("HALF_EVEN", "UNNECESSARY"))
            add(listOf("C", "plusContext:$sign", "0.00", "0", "$precision", mode))
    }
    val absKeys: Set<List<String>> = buildSet {
        for (sign in listOf(0, 1)) for (precision in listOf(0, 1, 7)) for (mode in listOf("HALF_EVEN", "UNNECESSARY"))
            add(listOf("C", "absContext:$sign", "0.00", "0", "$precision", mode))
    }
    val nullAbsKeys: Set<List<String>> = buildSet {
        for (behavior in listOf("return", "self", "throw", "super")) for (sign in listOf(0, 1))
            add(listOf("C", "nullAbs:$behavior", "0.00", "$sign", "0", "HALF_EVEN"))
    }
    val keys = roundKeys + directUnaryKeys + absKeys + nullAbsKeys

    fun required(key: List<String>, host: String): String? = when (key) {
        in roundKeys, in directUnaryKeys -> "0|false|true|${host.substringAfterLast('|')}"
        in absKeys -> {
            kotlin.test.assertEquals("0.00|true|false|sign;plusContext;", host, "complete retained abs observation $key")
            "0.00|true|false|sign;"
        }
        in nullAbsKeys -> "!NullPointerException|sign;"
        else -> null
    }
}
