package vc.rux.pokefork.web3j

import org.slf4j.LoggerFactory
import org.web3j.protocol.Web3j
import org.web3j.protocol.core.JsonRpc2_0Web3j
import org.web3j.protocol.core.Request
import org.web3j.protocol.core.Response
import org.web3j.protocol.http.HttpService
import vc.rux.pokefork.IForkNode
import vc.rux.pokefork.ILocalNode
import vc.rux.pokefork.hardhat.HardhatNode
import vc.rux.pokefork.hardhat.NodeMode
import vc.rux.pokefork.web3j.utils.toHexStringPrefixed
import java.lang.System.currentTimeMillis
import java.math.BigInteger

// hm, looks like it can be a separate project
class LocalWeb3jNode(
    private val hardhatNode: HardhatNode,
    private val jsonRpc20: JsonRpc2_0Web3j,
    private val httpService: HttpService,
) : ILocalNode, IForkNode, Web3j by jsonRpc20 {
    override fun forkBlock(blockNumber: Long) {
        log.debug("forkBlock: forking from $blockNumber")
        val startedAt = currentTimeMillis()
        val realRpcUrl = (hardhatNode.config.nodeMode as? NodeMode.Fork)?.realNodeRpc
            ?: throw IllegalArgumentException("Node is not running in fork mode, used configuration: ${hardhatNode.config.nodeMode.javaClass.simpleName}")

        val resp = httpService.send(
            Request(
                "hardhat_reset",
                listOf(
                    mapOf("forking" to mapOf(
                        "jsonRpcUrl" to realRpcUrl,
                        "blockNumber" to blockNumber
                    )),
                ),
                httpService,
                HardhatStringResponse::class.java
            ),
            HardhatStringResponse::class.java
        )

        resp.throwIfErroredOrResultIsNotTrue()

        log.debug("forkBlock: forked from $blockNumber, fork took: ${currentTimeMillis() - startedAt}ms")
    }

    override fun setNextBlockBaseFeePerGas(baseFeePerGas: BigInteger) {
        log.debug("setNextBlockBaseFeePerGas: setting baseFeePerGas to {}", baseFeePerGas)
        val resp = httpService.send(
            Request(
                "hardhat_setNextBlockBaseFeePerGas",
                listOf(baseFeePerGas.toHexStringPrefixed()),
                httpService,
                HardhatStringResponse::class.java
            ),
            HardhatStringResponse::class.java
        )

        resp.throwIfErroredOrResultIsNotTrue()
    }

    override fun mine(blocks: Int) {
        log.debug("mine: Mining $blocks blocks")
        val resp = httpService.send(
            Request(
                "hardhat_mine",
                listOf(blocks.toHexStringPrefixed()),
                httpService,
                HardhatMineResponse::class.java
            ),
            HardhatMineResponse::class.java
        )

        resp.throwIfErroredOrResultIsNotTrue()
    }

    override fun setBalance(destination: String, balance: BigInteger) {
        log.info("mine: set balance of {} to {}", destination, balance)
        val resp = httpService.send(
            Request(
                "hardhat_setBalance",
                listOf(destination, balance.toHexStringPrefixed()),
                httpService,
                HardhatSetBalanceResponse::class.java
            ),
            HardhatSetBalanceResponse::class.java
        )

        resp.throwIfErroredOrResultIsNotTrue()
    }


    internal class HardhatStringResponse : Response<String>()

    internal class HardhatMineResponse : Response<String>()
    internal class HardhatSetBalanceResponse : Response<String>()

    companion object {
        private val log = LoggerFactory.getLogger(LocalWeb3jNode::class.java)

        fun from(hardhatNode: HardhatNode): LocalWeb3jNode {
            val http = HttpService(hardhatNode.localRpcNodeUrl)
            return LocalWeb3jNode(hardhatNode, JsonRpc2_0Web3j(http), http)
        }
    }

}