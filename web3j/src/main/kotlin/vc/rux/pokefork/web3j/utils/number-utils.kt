package vc.rux.pokefork.web3j.utils

import java.math.BigInteger


internal fun Int.toHexStringPrefixed(): String = "0x" + this.toString(16)
internal fun Long.toHexStringPrefixed(): String = "0x" + this.toString(16)

internal fun BigInteger.toHexStringPrefixed(): String = "0x" + this.toString(16)
