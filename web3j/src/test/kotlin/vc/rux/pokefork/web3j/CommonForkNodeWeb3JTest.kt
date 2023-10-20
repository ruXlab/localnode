package vc.rux.pokefork.web3j

import assertk.all
import assertk.assertThat
import assertk.assertions.*
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.CsvSource
import org.web3j.contracts.eip20.generated.ERC20
import org.web3j.protocol.core.DefaultBlockParameterName.LATEST
import org.web3j.protocol.core.Ethereum
import org.web3j.protocol.core.methods.request.Transaction
import org.web3j.tx.gas.DefaultGasProvider
import vc.rux.pokefork.NodeMode
import vc.rux.pokefork.errors.PokeForkError
import vc.rux.pokefork.hardhat.IEthereumLikeNode
import vc.rux.pokefork.web3j.utils.toHexStringSuffixed
import java.math.BigDecimal
import java.math.BigDecimal.TEN
import java.math.BigInteger
import java.math.BigInteger.TWO
import kotlin.text.RegexOption.IGNORE_CASE

abstract class CommonForkNodeWeb3JTest {
    protected lateinit var fork: IEthereumLikeNode
    abstract fun forkImplementationFactory(mode: NodeMode): IEthereumLikeNode

    fun defaultMainnetFork(): IEthereumLikeNode =
        forkImplementationFactory(NodeMode.Fork(MAINNET_RPC, 1))

    @AfterEach
    fun afterEach() {
        if (::fork.isInitialized)
            fork.stop()
    }

    @CsvSource(value = ["18100000,0.208933821146944046", "15000000,321495.8128334608039745"])
    @ParameterizedTest(name = "forkBlock changes blocks - blockNumber: {0}, expectedBalance: {1}")
    fun `forkBlock changes blocks`(blockNumber: Long, expectedBalance: BigDecimal) {
        // given
        fork = defaultMainnetFork()
        val web3 = LocalWeb3jNode.from(fork)

        // when
        web3.forkBlock(blockNumber)

        // then
        assertThat(web3.ethGetBalance(FTX_WALLET, LATEST).send().balance)
            .isEqualTo((expectedBalance * TEN.pow(18)).toBigInteger())
    }

    @Test
    fun `chainId is set to the required one `() {
        // given
        fork = forkImplementationFactory(NodeMode.Fork(MAINNET_RPC, 42))

        // when
        val web3 = LocalWeb3jNode.from(fork)

        // then
        val chainId = web3.netVersion().send().netVersion
        assertThat(chainId).isEqualTo("42")
    }

    @Test
    fun `when forked, the block number must be greater than 0`() {
        // given
        fork = defaultMainnetFork()
        val web3 = LocalWeb3jNode.from(fork)

        // when and then
        assertThat(web3.ethBlockNumber().send().blockNumber.toLong())
            .isGreaterThan(0)
    }
    
    @Test
    fun `can mine blocks`() {
        // given
        fork = defaultMainnetFork()
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
        fork = defaultMainnetFork()
        val web3 = LocalWeb3jNode.from(fork)
        val balanceBefore = web3.ethGetBalance(VITALIK_WALLET, LATEST).send().balance

        // when
        web3.setBalance(VITALIK_WALLET, 42.toBigInteger())
        web3.mine(1)

        // then
        val balanceAfter = web3.ethGetBalance(VITALIK_WALLET, LATEST).send().balance
        assertThat(balanceAfter).all {
            println(balanceBefore)
            println(balanceAfter)
            isNotEqualTo(balanceBefore)
            isEqualTo(42.toBigInteger())
        }
    }


    @Test
    fun `setStorageAt can change the token name`() {
        // given
        fork = defaultMainnetFork()
        val web3 = LocalWeb3jNode.from(fork)

        val ust = ERC20.load(UST_TOKEN, web3, randomCredentials, DefaultGasProvider());
        val newName = "UnSTablecoin"
        val newNameInMemoryLayout = run { // let's play with low level string formation here
            val nameAsHexAlignedLeft = BigInteger(newName.toByteArray()).toHexStringSuffixed(32).drop(2)
            BigInteger(nameAsHexAlignedLeft, 16).or(newName.length.toBigInteger() * TWO)
        }
        // precondition
        assertThat(ust.symbol().send()).isEqualTo("UST")

        // when
        // utf8 string to BigInteger
        web3.setStorageAt(UST_TOKEN, 0x4.toBigInteger(), newNameInMemoryLayout)

        // then
        assertThat(ust.symbol().send()).isEqualTo(newName)
    }

    @Test
    fun `setNextBlockBaseFeePerGas can set base fee per gas`() {
        // given
        fork = defaultMainnetFork()
        val web3 = LocalWeb3jNode.from(fork)

        // when
        web3.setNextBlockBaseFeePerGas(42.toBigInteger())
        web3.mine(1)

        // then
        val block = web3.ethGetBlockByNumber(LATEST, false).send().block
        assertThat(block.baseFeePerGas).isEqualTo(42.toBigInteger())
    }

    @Test
    fun `setBalance throws exception if bad params passed`() {
        // given
        fork = defaultMainnetFork()
        val web3 = LocalWeb3jNode.from(fork)

        // when and then
        val error = assertThrows<PokeForkError> {
            web3.setBalance("0xPoKeForK", 42.toBigInteger())
        }
        assertThat(error)
            .isInstanceOf<PokeForkRpcCallError>()
            .transform { it.error.message }
            .containsMatch("(invalid value)|(invalid length)".toRegex(IGNORE_CASE)) // hardhat returns '..invalid value..', anvil - '..invalid length..'
    }

    @Test
    fun `impersonateAccount works`() {
        // given
        fork = defaultMainnetFork()
        val web3 = LocalWeb3jNode.from(fork)
        val sendAllEthTx = formSendEthTx(web3)

        // when
        web3.impersonateAccount(VITALIK_WALLET)
        web3.ethSendTransaction(sendAllEthTx).send().throwIfErrored()

        // then
        val newBalance = web3.ethGetBalance(VITALIK_WALLET, LATEST).send().balance
        assertThat(newBalance).isZero()
    }

    @Test
    fun `stopImpersonatingAccount works`() {
        // given
        fork = defaultMainnetFork()
        val web3 = LocalWeb3jNode.from(fork)
        web3.impersonateAccount(VITALIK_WALLET)

        // when
        web3.stopImpersonatingAccount(VITALIK_WALLET)
        val error = assertThrows<PokeForkError> {
            web3.ethSendTransaction(formSendEthTx(web3)).send().throwIfErrored()
        }

        // then
        assertThat(error)
            .isInstanceOf<PokeForkRpcCallError>()
            .transform { it.error.message }
            .containsMatch("(unknown account)|(sender account not recognized)|(no signer available)".toRegex(IGNORE_CASE))
    }

    @Test
    fun `can take snapshot and restore it`() {
        // given
        fork = defaultMainnetFork()
        val web3 = LocalWeb3jNode.from(fork)
        web3.impersonateAccount(A_ADDRESS)
        web3.setBalance(A_ADDRESS, ONE_ETH)
        val balanceBeforeFirstCheckpoint = web3.ethBalanceOf(A_ADDRESS)

        // when
        val snapshotId = web3.chainSnapshot()
        web3.ethSendTransaction(
            formSendEthTx(web3, from = A_ADDRESS, amount =  balanceBeforeFirstCheckpoint  / TWO)
        ).send().throwIfErrored()
        assertThat(web3.ethBalanceOf(A_ADDRESS)).isLessThan(balanceBeforeFirstCheckpoint)

        // and when
        web3.chainRevert(snapshotId)

        // then
        assertThat(web3.ethBalanceOf(A_ADDRESS)).isEqualTo(balanceBeforeFirstCheckpoint)
    }


    private fun formSendEthTx(
        web3: LocalWeb3jNode, from: String = VITALIK_WALLET, destination: String = ZERO_ADDRESS, amount: BigInteger? = null
    ): Transaction {
        val nonce = web3.ethGetTransactionCount(from, LATEST).send().transactionCount
        val ethBalance = web3.ethBalanceOf(from)
        val gasPrice = web3.ethGasPrice().send().gasPrice
        return Transaction.createEtherTransaction(
            from, nonce, gasPrice, 21_000.toBigInteger(), destination,
            amount ?: (ethBalance - gasPrice * 21_000.toBigInteger())
        )
    }

    private fun Ethereum.ethBalanceOf(address: String): BigInteger =
        this.ethGetBalance(address, LATEST).send().balance
    
    companion object {
        const val VITALIK_WALLET = "0xd8dA6BF26964aF9D7eEd9e03E53415D37aA96045"
        const val FTX_WALLET = "0x2FAF487A4414Fe77e2327F0bf4AE2a264a776AD2"
        const val UST_TOKEN = "0xa47c8bf37f92abed4a126bda807a7b7498661acd"
        const val ZERO_ADDRESS = "0x0000000000000000000000000000000000000000"
        const val A_ADDRESS = "0xAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA"
    }
}