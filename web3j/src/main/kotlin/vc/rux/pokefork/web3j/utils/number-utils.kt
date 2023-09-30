package vc.rux.pokefork.web3j.utils

import org.web3j.utils.Numeric
import java.math.BigInteger


internal fun Int.toHexStringPrefixed(): String = "0x" + this.toString(16)
internal fun Long.toHexStringPrefixed(): String = "0x" + this.toString(16)

internal fun BigInteger.toHexStringPrefixed(): String = "0x" + this.toString(16)


internal fun BigInteger.toHexStringSuffixed(bytesWidth: Int): String =
    this.toString(16)
        .let { if (it.length % 2 == 0) it else "0$it" }
        .let { it + "00".repeat(bytesWidth) }
        .take(bytesWidth * 2)
        .let { "0x$it" }

