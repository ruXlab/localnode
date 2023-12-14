package vc.rux.localnode.anvil

import com.github.dockerjava.api.DockerClient
import com.github.dockerjava.api.command.CreateContainerResponse
import com.github.dockerjava.api.command.PullImageResultCallback
import com.github.dockerjava.api.model.ExposedPort
import com.github.dockerjava.api.model.HostConfig
import com.github.dockerjava.api.model.PullResponseItem
import org.slf4j.LoggerFactory
import vc.rux.localnode.NodeMode
import vc.rux.localnode.common.idPrefix
import vc.rux.localnode.common.waitForRpcToBoot
import vc.rux.localnode.defaultDockerClient
import vc.rux.localnode.hardhat.HardhatNode
import vc.rux.localnode.hardhat.IEthereumLikeNode
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.Duration.Companion.seconds

class AnvilNode private constructor(
    val config: AnvilNodeConfig,
    private val dockerClient: DockerClient = defaultDockerClient
): IEthereumLikeNode {
    private val chainId = config.nodeMode.chainId ?: 31337
    private val imageName: String by config::imageName
    private val imageTag = config.imageTag ?: mkDefaultImageTag()
    private val fullImage = "$imageName:$imageTag"

    lateinit var containerId: String

    override lateinit var localRpcNodeUrl: String
    override val nodeMode: NodeMode by config::nodeMode


    private fun mkDefaultImageTag(): String =
        "anvil-${config.nodeMode.idPrefix}$chainId"
    private fun run() {
        ensureImagePulled()

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

        waitForRpcToBoot(localRpcNodeUrl, fullImage, MAX_WAIT_BOOT_TIME, READINESS_CHECK_INTERVAL)
    }

    private fun ensureImagePulled() {
        val callback = object: PullImageResultCallback() {
            override fun onNext(item: PullResponseItem?) {
                super.onNext(item)
                log.info("Pulling image ${config.foundryImage}: {}", item)
            }

            override fun onComplete() {
                super.onComplete()
                log.info("Pulled image ${config.foundryImage}")
            }
        }
        dockerClient.pullImageCmd(config.foundryImage)
            .exec(callback)

        callback.awaitCompletion()
    }


    override fun stop() {
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
        private val READINESS_CHECK_INTERVAL = 100.milliseconds

        @JvmStatic
        fun start(config: AnvilNodeConfig): AnvilNode {
            return AnvilNode(config).also { it.run() }
        }
    }
}