package vc.rux.pokefork.anvil

import vc.rux.pokefork.NodeMode

data class AnvilNodeConfig(
    val nodeMode: NodeMode,
    val blockNumber: Long? = null,
    val imageName: String = "pokefork",
    val imageTag: String? = null,
    val foundryImage: String = "ghcr.io/foundry-rs/foundry:latest"
) {
    companion object {
        /**
         * Shortcut factory method - create a fork of the existing EVM network from provided node RPC URL.
         *
         * @param realNodeRpc The RPC URL of the real Ethereum node to fork.
         * @param chainId The chain ID for the forked network (optional). If not set the implementation (hardhat or anvil) will choose it.
         */
        fun fork(realNodeRpc: String, chainId: Long? = null): AnvilNodeConfig =
            AnvilNodeConfig(
                nodeMode = NodeMode.Fork(realNodeRpc, chainId)
            )

        /**
         * Shortcut factory method - create a local EVM network from scratch.
         *
         * @param chainId The chain ID (optional).
         */
        fun local(chainId: Long? = null): AnvilNodeConfig =
            AnvilNodeConfig(
                nodeMode = NodeMode.Local(chainId)
            )
    }

}
