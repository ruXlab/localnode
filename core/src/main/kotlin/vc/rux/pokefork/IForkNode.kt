package vc.rux.pokefork

import vc.rux.pokefork.ILocalNode

interface IForkNode : ILocalNode {
    fun forkBlock(blockNumber: Long) 
}

