package vc.rux.localnode.hardhat

import vc.rux.localnode.NodeMode

interface IEthereumLikeNode {
    val localRpcNodeUrl: String

    val nodeMode: NodeMode
    fun stop()
}