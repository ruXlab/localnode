package vc.rux.pokefork.web3j

import org.slf4j.LoggerFactory
import org.web3j.protocol.Web3j
import org.web3j.protocol.core.JsonRpc2_0Web3j
import org.web3j.protocol.core.Request
import org.web3j.protocol.core.Response
import org.web3j.protocol.http.HttpService
import vc.rux.pokefork.IPokeForkAPI
import vc.rux.pokefork.hardhat.HardhatNode
import vc.rux.pokefork.web3j.utils.toHexStringPrefixed
import java.math.BigInteger

// hm, looks like it can be a separate project
class Web3jNode(
    private val hardhatNode: HardhatNode,
    private val jsonRpc20: JsonRpc2_0Web3j,
    private val httpService: HttpService,
) : IPokeForkAPI, Web3j by jsonRpc20 {

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


    internal class HardhatMineResponse : Response<String>()
    internal class HardhatSetBalanceResponse : Response<String>()

    companion object {
        private val log = LoggerFactory.getLogger(Web3jNode::class.java)

        fun from(hardhatNode: HardhatNode): Web3jNode {
            val http = HttpService(hardhatNode.localRpcNodeUrl)
            return Web3jNode(hardhatNode, JsonRpc2_0Web3j(http), http)
        }
    }

}