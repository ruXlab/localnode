package vc.rux.pokefork.web3j

import org.web3j.crypto.Credentials

val randomCredentials = Credentials.create(
    "0x" + "deadbeef".repeat(8)
)