package vc.rux.pokefork.anvil

import com.github.dockerjava.api.DockerClient
import com.github.dockerjava.api.command.CreateContainerResponse
import com.github.dockerjava.api.model.ExposedPort
import com.github.dockerjava.api.model.HostConfig
import org.slf4j.LoggerFactory
import vc.rux.pokefork.NodeMode
import vc.rux.pokefork.common.idPrefix
import vc.rux.pokefork.defaultDockerClient
import vc.rux.pokefork.hardhat.HardhatNode
import java.net.HttpURLConnection
import java.net.URL
import kotlin.time.Duration.Companion.seconds

class AnvilNode private constructor(
    val config: AnvilNodeConfig,
    private val dockerClient: DockerClient = defaultDockerClient
) {
    private val chainId = config.nodeMode.chainId ?: 31337
    private val imageName: String by config::imageName
    private val imageTag = config.imageTag ?: mkImageTag()
    private val fullImage = "$imageName:$imageTag"

    lateinit var containerId: String

    lateinit var localRpcNodeUrl: String

    private fun mkImageTag(): String =
        "anvil-${config.nodeMode.idPrefix}$chainId"
    private fun run() {
        val containerConfig = dockerClient.createContainerCmd(config.foundryImage)
            .withEntrypoint("anvil", *mkAnvilParams().toTypedArray())
            .withExposedPorts(ExposedPort.tcp(8545))
            .withHostConfig(HostConfig.newHostConfig().withPublishAllPorts(true))
        
        // Create the container
        val containerResponse: CreateContainerResponse = containerConfig.exec()
        containerId = containerResponse.id

        log.info("run: created foundry(anvil) container $containerId")

        // Start the container
        dockerClient.startContainerCmd(containerId)
            .exec()

        val mappedRpcPort = dockerClient.inspectContainerCmd(containerId).exec().networkSettings.ports.bindings
            .asSequence()
            .singleOrNull { it.key.port == 8545 }
            ?.let { it.key to it.value.first().hostPortSpec }
            ?: throw IllegalStateException("The container $fullImage started but the port 8545 is not exposed")

        localRpcNodeUrl = "http://localhost:${mappedRpcPort.second}"

        log.warn("Container's port {} is bound to {}, the local node should be available at {}",
            mappedRpcPort.first, mappedRpcPort.second, localRpcNodeUrl)

        waitForRpcToBoot()
    }

    // A very simple RPC client without extra dependencies
    private fun waitForRpcToBoot() {
        val startedAt = System.currentTimeMillis()
        val requestBody = """{"jsonrpc":"2.0","method":"net_version","params":[],"id":67}"""
        while(System.currentTimeMillis() - startedAt < MAX_WAIT_BOOT_TIME.inWholeMilliseconds) {
            try {
                val conn = URL(localRpcNodeUrl).openConnection() as HttpURLConnection
                conn.doOutput = true
                conn.requestMethod = "POST"
                conn.setRequestProperty("Content-Type", "application/json")
                conn.outputStream.use { it.write(requestBody.toByteArray()) }

                val code = conn.responseCode
                if (code == 200) {
                    log.debug("The local RPC node became available after ${System.currentTimeMillis() - startedAt}ms")
                    return
                }
                throw IllegalStateException("The local RPC node is not available yet, got ${code} response")
            } catch (e: Exception) {
                log.debug("Failed to communicate with the local RPC: $e")
            }

            Thread.sleep(300)
        }

        throw IllegalStateException("The container $fullImage started but the RPC is not available after $MAX_WAIT_BOOT_TIME")
    }

    fun stop() {
        if (!::containerId.isInitialized)
            throw IllegalStateException("The container is not running, cannot stop it")

        log.info("Stopping container $containerId")

        dockerClient.killContainerCmd(containerId).exec()
        dockerClient.removeContainerCmd(containerId).exec()
    }
    
    private fun mkAnvilParams(): List<String> {
        return listOfNotNull(
            when (val nodeMode = config.nodeMode) {
                is NodeMode.Fork -> listOf("--fork-url", nodeMode.realNodeRpc)
                is NodeMode.Local -> emptyList()
            },
            listOf("--chain-id", chainId.toString(), "--host", "0.0.0.0"),
            config.blockNumber?.let { listOf("--fork-block-number", it.toString()) },
        ).flatten()
    }
    
    companion object {
        private val log = LoggerFactory.getLogger(HardhatNode::class.java)
        private val MAX_WAIT_BOOT_TIME = 60.seconds

        @JvmStatic
        fun start(config: AnvilNodeConfig): AnvilNode {
            return AnvilNode(config).also { it.run() }
        }
    }
}