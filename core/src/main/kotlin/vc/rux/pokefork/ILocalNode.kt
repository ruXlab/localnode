package vc.rux.pokefork

import java.math.BigInteger

interface ILocalNode {
    fun mine(blocks: Int)

    fun setBalance(destination: String, balance: BigInteger)
}