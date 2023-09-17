package vc.rux.pokefork.hardhat

data class HardHatForkConfig(
    val rpcUrl: String,
    val blockNumber: Long? = null,
    val networkId: Int? = null,
    val imageName: String = "pokefork",
    val imageTag: String? = null,
    val hardhatVersion: String = "2.17.3"
)
