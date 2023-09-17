package vc.rux.pokefork.web3j

import okhttp3.internal.toHexString
import org.slf4j.LoggerFactory
import org.web3j.protocol.Web3j
import org.web3j.protocol.core.JsonRpc2_0Web3j
import org.web3j.protocol.core.Request
import org.web3j.protocol.core.Response
import org.web3j.protocol.http.HttpService
import org.web3j.utils.Numeric
import vc.rux.pokefork.IPokeForkAPI
import vc.rux.pokefork.hardhat.HardhatFork
import vc.rux.pokefork.web3j.utils.toHexStringPrefixed

class Web3jFork(
    private val hardhatFork: HardhatFork,
    private val jsonRpc20: JsonRpc2_0Web3j,
    private val httpService: HttpService,
) : IPokeForkAPI, Web3j by jsonRpc20 {

    fun mine(blocks: Int) {
        log.info("mine: Mining $blocks blocks")
        val resp = httpService.send(
            Request(
                "hardhat_mine",
                listOf(blocks.toHexStringPrefixed()),
                httpService,
                HardhatMineResponse::class.java
            ),
            HardhatMineResponse::class.java
        )

        resp.throwIfErrored()
    }

    class HardhatMineResponse : Response<String>() {

    }

    companion object {
        private val log = LoggerFactory.getLogger(Web3jFork::class.java)

        fun from(hardhatFork: HardhatFork): Web3jFork {
            val http = HttpService(hardhatFork.localRpcNodeUrl)
            return Web3jFork(hardhatFork, JsonRpc2_0Web3j(http), http)
        }
    }

}