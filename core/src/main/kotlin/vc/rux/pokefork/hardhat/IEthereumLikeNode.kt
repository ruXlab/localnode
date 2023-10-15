package vc.rux.pokefork.hardhat

import vc.rux.pokefork.NodeMode

interface IEthereumLikeNode {
    val localRpcNodeUrl: String

    val nodeMode: NodeMode
    fun stop()
}