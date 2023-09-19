package vc.rux.pokefork

import java.math.BigInteger

interface IPokeForkAPI {
    fun mine(blocks: Int)

    fun setBalance(destination: String, balance: BigInteger)
}