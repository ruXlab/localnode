package vc.rux.pokefork.web3j

import assertk.assertThat
import assertk.assertions.isEqualTo
import org.junit.jupiter.api.Test
import vc.rux.pokefork.hardhat.HardHatForkConfig
import vc.rux.pokefork.hardhat.HardhatFork

class Web3jForTest {
    private val config = HardHatForkConfig(
        rpcUrl = "https://rpc.ankr.com/eth", networkId = 31337,
    )

    @Test
    fun `chainId is set to the required one `() {
        // given
        val connection = HardhatFork.fork(config.copy(networkId = 42))

        // when
        val fork = Web3jFork.from(connection)

        // then
        val chainId = fork.netVersion().send().netVersion
        assertThat(chainId).isEqualTo("42")
    }
    
    @Test
    fun `can mine blocks`() {
        // given
        val fork = Web3jFork.from(HardhatFork.fork(config))
        val bnBeforeMine = fork.ethBlockNumber().send().blockNumber.toLong()

        // when
        fork.mine(42)

        // then
        val bnAfterMine = fork.ethBlockNumber().send().blockNumber.toLong()
        assertThat(bnAfterMine).isEqualTo(bnBeforeMine + 42)
    }
}