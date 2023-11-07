package misc

val archiveNodeUrl by lazy {
    System.getenv("ARCHIVE_NODE_URL_ETHEREUM") ?: "https://rpc.ankr.com/eth"
}

object addresses {
    const val optimismPortal = "0xbEb5Fc579115071764c7423A4f12eDde41f106Ed"
    const val arbitrumEthBridge = "0x8315177ab297ba92a06054ce80a67ed4dbd7ed3a"
}
