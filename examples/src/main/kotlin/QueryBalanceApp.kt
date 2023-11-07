import misc.addresses
import misc.archiveNodeUrl
import org.web3j.protocol.core.DefaultBlockParameter
import org.web3j.utils.Convert.Unit
import org.web3j.utils.Convert.fromWei
import vc.rux.pokefork.anvil.AnvilNode
import vc.rux.pokefork.anvil.AnvilNodeConfig
import vc.rux.pokefork.web3j.LocalWeb3jNode

object QueryBalanceApp {
    @JvmStatic
    fun main(vararg args: String) {
        // Start the forked local node
        val pokefork = AnvilNode.start(AnvilNodeConfig.fork(archiveNodeUrl, chainId = 1))
        val web3j = LocalWeb3jNode.from(pokefork)

        println("Checking the amount of ETH locked in Arbitrum Native bridge (portal) at ${addresses.arbitrumEthBridge}")
        println()
        println("block number\t | balance")
        // Check the balances
        for (block in 15500000..18600000 step 300000) {
            val balance = web3j.ethGetBalance(
                addresses.arbitrumEthBridge, DefaultBlockParameter.valueOf(block.toBigInteger())
            ).send().balance

            println("$block \t\t | ${fromWei(balance.toString(), Unit.ETHER)} ETH")
        }
    }

}