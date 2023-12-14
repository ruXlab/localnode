package vc.rux.localnode

/**
 * NodeMode is a sealed interface with two implementations:
 *
 * Local: Operates as an independent, self-contained Ethereum blockchain locally on your machine,
 * ideal for fast and deterministic development and testing.
 *
 * Fork: Allows you to replicate the state of a real Ethereum network at a specific block height,
 * enabling interaction with smart contracts and testing with real-world data using the provided realNodeRpc URL.
 */
sealed interface NodeMode {
    val chainId: Long?

    /**
     * Local chain only.
     * In this mode, it operates as an independent, self-contained Ethereum blockchain
     * running locally on your development machine. It doesn't rely on an external Ethereum node
     * and provides a fast and deterministic environment for the development and testing of Ethereum
     * smart contracts and DApps.
     */
    data class Local(override val chainId: Long?): NodeMode

    /**
     * Forked node.
     * It allows you to fork the state of the Ethereum mainnet or another Ethereum network at a specific block.
     * This means you can replicate the state of a real Ethereum network at a particular block height, enabling
     * you to interact with smart contracts and test your DApps with real-world data.
     */
    data class Fork(val realNodeRpc: String, override val chainId: Long?): NodeMode
}