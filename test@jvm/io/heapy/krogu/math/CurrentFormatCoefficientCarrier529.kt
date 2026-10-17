package io.heapy.krogu.math

/**
 * A current-format object-resolution fixture needs an actual serialized coefficient
 * object. Compact decimals intentionally have none. A public wide carrier at the
 * same scale lets ObjectInputStream resolve that coefficient to the fixture's
 * existing truthful subtype. Numerical/callback/identity assertions are unchanged;
 * this is neither a legacy format nor a constructor callback bypass in production.
 */
internal fun currentFormatCoefficientCarrier529(value: BigDecimal): BigDecimal =
    if (value.hasCompactStorage529()) BigDecimal(BigInteger("18446744073709551616"), value.scale()) else value
