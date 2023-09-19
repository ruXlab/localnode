package vc.rux.pokefork.web3j

import assertk.all
import assertk.assertThat
import assertk.assertions.*
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.web3j.protocol.core.DefaultBlockParameterName
import vc.rux.pokefork.errors.PokeForkError
import vc.rux.pokefork.hardhat.HardHatNodeConfig
import vc.rux.pokefork.hardhat.HardhatNode
import vc.rux.pokefork.hardhat.NodeMode

class Web3JNodeTest {
    private lateinit var fork: HardhatNode
    
    private val config = HardHatNodeConfig.fork("https://rpc.ankr.com/eth", 1)

    @AfterEach
    fun afterEach() {
        if (::fork.isInitialized)
            fork.stop()
    }

    @Test
    fun `chainId is set to the required one `() {
        // given
        fork = HardhatNode.fork(config.copy(NodeMode.Fork("https://rpc.ankr.com/eth", 42)))

        // when
        val web3 = Web3jNode.from(fork)

        // then
        val chainId = web3.netVersion().send().netVersion
        assertThat(chainId).isEqualTo("42")
    }

    @Test
    fun `when forked, the block number must be greater than 0`() {
        // given
        fork = HardhatNode.fork(config)
        val web3 = Web3jNode.from(fork)

        // when and then
        assertThat(web3.ethBlockNumber().send().blockNumber.toLong())
            .isGreaterThan(0)
    }
    
    @Test
    fun `can mine blocks`() {
        // given
        fork = HardhatNode.fork(config)
        val web3 = Web3jNode.from(fork)
        val bnBeforeMine = web3.ethBlockNumber().send().blockNumber.toLong()
        println(bnBeforeMine)

        // when
        web3.mine(42)

        // then
        val bnAfterMine = web3.ethBlockNumber().send().blockNumber.toLong()
        assertThat(bnAfterMine).isEqualTo(bnBeforeMine + 42)
    }

    @Test
    fun `setBalance can set balance in forked network`() {
        // given
        fork = HardhatNode.fork(config)
        val web3 = Web3jNode.from(fork)
        val balanceBefore = web3.ethGetBalance(VITALIK_WALLET, DefaultBlockParameterName.LATEST).send().balance

        // when
        web3.setBalance(VITALIK_WALLET, 42.toBigInteger())
        web3.mine(1)

        // then
        val balanceAfter = web3.ethGetBalance(VITALIK_WALLET, DefaultBlockParameterName.LATEST).send().balance
        assertThat(balanceAfter).all {
            println(balanceBefore)
            println(balanceAfter)
            isNotEqualTo(balanceBefore)
            isEqualTo(42.toBigInteger())
        }
    }

    @Test
    fun `setBalance throws exception if bad params passed`() {
        // given
        fork = HardhatNode.fork(config)
        val web3 = Web3jNode.from(fork)

        // when and then
        val error = assertThrows<PokeForkError> {
            web3.setBalance("0xPoKeForK", 42.toBigInteger())
        }
        assertThat(error)
            .isInstanceOf<PokeForkRpcCallError>()
            .transform { it.error.message }
            .contains("invalid value", ignoreCase = true)
    }


    companion object {
        const val VITALIK_WALLET = "0xd8dA6BF26964aF9D7eEd9e03E53415D37aA96045";
    }
}