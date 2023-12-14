package vc.rux.localnode.web3j

import org.slf4j.LoggerFactory
import org.web3j.protocol.Web3j
import org.web3j.protocol.core.JsonRpc2_0Web3j
import org.web3j.protocol.core.Request
import org.web3j.protocol.core.Response
import org.web3j.protocol.http.HttpService
import vc.rux.localnode.IForkNode
import vc.rux.localnode.ILocalNode
import vc.rux.localnode.NodeMode
import vc.rux.localnode.hardhat.IEthereumLikeNode
import vc.rux.localnode.web3j.utils.toHexStringPrefixed
import vc.rux.localnode.web3j.utils.toHexStringSuffixed
import java.lang.System.currentTimeMillis
import java.math.BigInteger

// hm, looks like it can be a separate project
class LocalWeb3jNode(
    private val hardhatNode: IEthereumLikeNode,
    private val jsonRpc20: JsonRpc2_0Web3j,
    private val httpService: HttpService,
) : ILocalNode, IForkNode, Web3j by jsonRpc20 {
    override fun forkBlock(blockNumber: Long) {
        log.debug("forkBlock: forking from $blockNumber")
        val startedAt = currentTimeMillis()
        val realRpcUrl = (hardhatNode.nodeMode as? NodeMode.Fork)?.realNodeRpc
            ?: throw IllegalArgumentException("Node is not running in fork mode, used configuration: ${hardhatNode.nodeMode.javaClass.simpleName}")

        sendRpcCallAndCheckResponse("hardhat_reset", listOf(
            mapOf("forking" to mapOf(
                "jsonRpcUrl" to realRpcUrl,
                "blockNumber" to blockNumber
            )),
        ))

        log.debug("forkBlock: forked from $blockNumber, fork took: ${currentTimeMillis() - startedAt}ms")
    }

    override fun setNextBlockBaseFeePerGas(baseFeePerGas: BigInteger) {
        log.debug("setNextBlockBaseFeePerGas: setting baseFeePerGas to {}", baseFeePerGas)
        sendRpcCallAndCheckResponse("hardhat_setNextBlockBaseFeePerGas", listOf(baseFeePerGas.toHexStringPrefixed()))
    }

    override fun mine(blocks: Int) {
        log.debug("mine: Mining $blocks blocks")
        sendRpcCallAndCheckResponse("hardhat_mine", listOf(blocks.toHexStringPrefixed()))
    }

    override fun setBalance(destination: String, balance: BigInteger) {
        log.info("mine: set balance of {} to {}", destination, balance)

        sendRpcCallAndCheckResponse("hardhat_setBalance",
            listOf(destination, balance.toHexStringPrefixed())
        )
    }

    override fun setStorageAt(destination: String, offset: BigInteger, value: BigInteger) {
        if (offset.bitLength() > 256)
            throw IllegalArgumentException("Offset is greater than U256")
        if (offset < BigInteger.ZERO)
            throw IllegalArgumentException("Offset can't be negative")
        if (value.bitLength() > 256)
            throw IllegalArgumentException("Value is greater than U256")
        if (value < BigInteger.ZERO)
            throw IllegalArgumentException("Value can't be negative")

        val (hexOffset, hexValue) = (offset.toHexStringPrefixed() to value.toHexStringSuffixed(32))

        log.info("setStorageAt: set storage of {} at {} to {}", destination, hexOffset, hexValue)
        sendRpcCallAndCheckResponse("hardhat_setStorageAt",
            listOf(destination, hexOffset, hexValue)
        )
    }

    override fun impersonateAccount(address: String) {
        log.info("impersonateAccount: {}", address)
        sendRpcCallAndCheckResponse("hardhat_impersonateAccount", listOf(address))
    }

    override fun stopImpersonatingAccount(address: String) {
        log.info("stopImpersonatingAccount: {}", address)
        sendRpcCallAndCheckResponse("hardhat_stopImpersonatingAccount", listOf(address))
    }

    override fun chainSnapshot(): String {
        log.info("chainSnapshot")
        val resp = httpService.send(
            Request("evm_snapshot", emptyList<Any>(), httpService, HardhatStringResponse::class.java),
            HardhatStringResponse::class.java
        )

        return resp.throwIfErrored().result
    }

    override fun chainRevert(snapshotId: String) {
        log.info("chainRevert: rolling back blockchain state to snapshot $snapshotId")
        sendRpcCallAndCheckResponse("evm_revert", listOf(snapshotId))
    }

    /**
     * Helper method; reduces boilerplate
     */
    private fun sendRpcCallAndCheckResponse(
        method: String,
        params: List<Any>,
    ) {
        httpService.send(
            Request(method, params, httpService, HardhatStringResponse::class.java),
            HardhatStringResponse::class.java
        ).also { resp ->
            resp.throwIfErroredOrResultIsNotTrue()
        }
    }

    internal class HardhatStringResponse : Response<String>()
    
    companion object {
        private val log = LoggerFactory.getLogger(LocalWeb3jNode::class.java)

        fun from(hardhatNode: IEthereumLikeNode): LocalWeb3jNode {
            val http = HttpService(hardhatNode.localRpcNodeUrl)
            return LocalWeb3jNode(hardhatNode, JsonRpc2_0Web3j(http), http)
        }
    }

}