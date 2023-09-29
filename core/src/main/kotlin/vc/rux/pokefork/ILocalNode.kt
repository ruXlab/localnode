package vc.rux.pokefork

import java.math.BigInteger

interface ILocalNode {
    /**
     * Sets the base fee per gas for the next block.
     * @param baseFeePerGas base fee per gas in wei defined in EIP-1559
     * @see https://eips.ethereum.org/EIPS/eip-1559
     */
    fun setNextBlockBaseFeePerGas(baseFeePerGas: BigInteger)

    /**
     * Mines the specified number of blocks
     * @param blocks number of blocks to mine
     */
    fun mine(blocks: Int)

    /**
     * Sets the balance of the specified account
     * @param destination account address
     * @param balance balance in wei
     */
    fun setBalance(destination: String, balance: BigInteger)
}