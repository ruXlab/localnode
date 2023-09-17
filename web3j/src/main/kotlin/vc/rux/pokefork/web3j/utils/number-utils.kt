package vc.rux.pokefork.web3j.utils


internal fun Int.toHexStringPrefixed(): String = "0x" + this.toString(16)
internal fun Long.toHexStringPrefixed(): String = "0x" + this.toString(16)
