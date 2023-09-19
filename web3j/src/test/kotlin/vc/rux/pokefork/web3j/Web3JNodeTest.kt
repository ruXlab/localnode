package vc.rux.pokefork.web3j

import assertk.all
import assertk.assertThat
import assertk.assertions.*
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.CsvSource
import org.web3j.protocol.core.DefaultBlockParameterName
import vc.rux.pokefork.errors.PokeForkError
import vc.rux.pokefork.hardhat.HardHatNodeConfig
import vc.rux.pokefork.hardhat.HardhatNode
import vc.rux.pokefork.hardhat.NodeMode
import java.math.BigDecimal
import java.math.BigDecimal.TEN

class Web3JNodeTest {
    private lateinit var fork: HardhatNode
    
    private val config = HardHatNodeConfig.fork("https://rpc.ankr.com/eth", 1)

    @AfterEach
    fun afterEach() {
        if (::fork.isInitialized)
            fork.stop()
    }


    @CsvSource(value = ["18100000,0.208933821146944046", "15000000,321495.8128334608039745"])
    @ParameterizedTest(name = "forkBlock changes blocks - blockNumber: {0}, expectedBalance: {1}")
    fun `forkBlock changes blocks`(blockNumber: Long, expectedBalance: BigDecimal) {
        // given
        fork = HardhatNode.fork(config)
        val web3 = LocalWeb3jNode.from(fork)

        // when
        web3.forkBlock(blockNumber)

        // then
        assertThat(web3.ethGetBalance(FTX_WALLET, DefaultBlockParameterName.LATEST).send().balance)
            .isEqualTo((expectedBalance * TEN.pow(18)).toBigInteger())
    }

    @Test
    fun `chainId is set to the required one `() {
        // given
        fork = HardhatNode.fork(config.copy(NodeMode.Fork("https://rpc.ankr.com/eth", 42)))

        // when
        val web3 = LocalWeb3jNode.from(fork)

        // then
        val chainId = web3.netVersion().send().netVersion
        assertThat(chainId).isEqualTo("42")
    }

    @Test
    fun `when forked, the block number must be greater than 0`() {
        // given
        fork = HardhatNode.fork(config)
        val web3 = LocalWeb3jNode.from(fork)

        // when and then
        assertThat(web3.ethBlockNumber().send().blockNumber.toLong())
            .isGreaterThan(0)
    }
    
    @Test
    fun `can mine blocks`() {
        // given
        fork = HardhatNode.fork(config)
        val web3 = LocalWeb3jNode.from(fork)
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
        val web3 = LocalWeb3jNode.from(fork)
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
        val web3 = LocalWeb3jNode.from(fork)

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
        const val VITALIK_WALLET = "0xd8dA6BF26964aF9D7eEd9e03E53415D37aA96045"
        const val FTX_WALLET = "0x2FAF487A4414Fe77e2327F0bf4AE2a264a776AD2"
    }
}