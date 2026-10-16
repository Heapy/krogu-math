# Kotlin lowestSetBit migration

Kotlin callers of Java `BigInteger.getLowestSetBit()` can use Java's synthetic
property spelling. When migrating that expression to krogu, explicitly import
the own property as well as the type:

```kotlin
import io.heapy.krogu.math.BigInteger
import io.heapy.krogu.math.lowestSetBit

val position: Int = BigInteger("8").lowestSetBit // 3
```

Importing `BigInteger` alone does not import this extension. The method spelling
`value.getLowestSetBit()` continues to work without a property import. An alias
is also available through ordinary Kotlin import syntax:

```kotlin
import io.heapy.krogu.math.BigInteger
import io.heapy.krogu.math.lowestSetBit as lowBit

val position: Int = BigInteger("-8").lowBit // 3
```

The read-only property calls the receiver's public virtual `getLowestSetBit()`.
It preserves that call's result or directly thrown exception; it has no backing
field and does not recompute the number. Zero returns -1 and nonzero values
return the index of their lowest set bit. Caller-owned members still take
precedence over the imported extension under ordinary Kotlin resolution.

This source addition and its paired/common migration tests are **UNCOMPILED and
UNRUN** at preparation. Ordinary property access is the promised form; this
document makes no experimental Java synthetic-property-reference promise.
It does not close Number525/557, the separate operator collision595, full525/553,
matching hosted-platform gates, performance or official JCK conformance.
