package vc.rux.localnode

interface IForkNode : ILocalNode {
    fun forkBlock(blockNumber: Long) 
}

