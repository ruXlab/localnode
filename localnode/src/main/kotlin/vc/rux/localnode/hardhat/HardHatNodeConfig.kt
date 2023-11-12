package vc.rux.localnode.hardhat

import vc.rux.localnode.NodeMode

data class HardHatNodeConfig(
    val nodeMode: NodeMode,
    val blockNumber: Long? = null,
    val imageName: String = "localnode",
    val imageTag: String? = null,
    val hardhatVersion: String = "2.17.3"
) {
    companion object {
        /**
         * Shortcut factory method - create a fork of the existing EVM network from provided node RPC URL.
         *
         * @param realNodeRpc The RPC URL of the real Ethereum node to fork.
         * @param chainId The chain ID for the forked network (optional). If not set the implementation (hardhat or anvil) will choose it.
         */
        fun fork(realNodeRpc: String, chainId: Long? = null): HardHatNodeConfig =
            HardHatNodeConfig(
                nodeMode = NodeMode.Fork(realNodeRpc, chainId)
            )

        /**
         * Shortcut factory method - create a local EVM network from scratch.
         *
         * @param chainId The chain ID (optional).
         */
        fun local(chainId: Long? = null): HardHatNodeConfig =
            HardHatNodeConfig(
                nodeMode = NodeMode.Local(chainId)
            )
    }
}
