package vc.rux.pokefork.hardhat

import com.github.dockerjava.api.DockerClient
import com.github.dockerjava.api.async.ResultCallback
import com.github.dockerjava.api.command.BuildImageResultCallback
import com.github.dockerjava.api.command.CreateContainerResponse
import com.github.dockerjava.api.model.BuildResponseItem
import com.github.dockerjava.api.model.HostConfig
import org.slf4j.LoggerFactory
import vc.rux.pokefork.NodeMode
import vc.rux.pokefork.common.idPrefix
import vc.rux.pokefork.common.waitForRpcToBoot
import vc.rux.pokefork.defaultDockerClient
import vc.rux.pokefork.hardhat.internal.HardHatConfigJs
import vc.rux.pokefork.hardhat.internal.HardHatDockerfile
import java.nio.file.Files
import kotlin.time.Duration.Companion.seconds

class HardhatNode private constructor(
    val config: HardHatNodeConfig,
    private val dockerClient: DockerClient = defaultDockerClient
) : IEthereumLikeNode {
    private val chainId = config.nodeMode.chainId ?: 31337L
    private val imageName: String by config::imageName
    private val imageTag = config.imageTag ?: mkDefaultImageTag()
    private val fullImage = "$imageName:$imageTag"

    lateinit var containerId: String

    override lateinit var localRpcNodeUrl: String
    override val nodeMode: NodeMode by config::nodeMode

    private fun mkDefaultImageTag(): String =
        "hardhat-${config.hardhatVersion}-${config.nodeMode.idPrefix}$chainId"

    private fun run() {
        if (!checkIfImageExists())
            buildDockerImage(dockerClient, imageName, imageTag)

        val containerConfig = dockerClient.createContainerCmd(fullImage)
            .withHostConfig(HostConfig.newHostConfig().withPublishAllPorts(true))

        // Create the container
        val containerResponse: CreateContainerResponse = containerConfig.exec()
        containerId = containerResponse.id

        log.info("run: created hardhat container $containerId")

        // Start the container
        dockerClient.startContainerCmd(containerId)
            .exec()

        val mappedRpcPort = dockerClient.inspectContainerCmd(containerId).exec().networkSettings.ports.bindings
            .asSequence()
            .singleOrNull { it.key.port == 8545 }
            ?.let { it.key to it.value.first().hostPortSpec }
            ?: throw IllegalStateException("The container $fullImage started but the port 8545 is not exposed")

        localRpcNodeUrl = "http://localhost:${mappedRpcPort.second}"

        log.warn(
            "Container's port {} is bound to {}, the local node should be available at {}",
            mappedRpcPort.first, mappedRpcPort.second, localRpcNodeUrl
        )

        waitForRpcToBoot(localRpcNodeUrl, imageName, MAX_WAIT_BOOT_TIME)
    }

    override fun stop() {
        if (!::containerId.isInitialized)
            throw IllegalStateException("The container is not running, cannot stop it")

        log.info("Stopping container $containerId")

        dockerClient.killContainerCmd(containerId).exec()
        dockerClient.removeContainerCmd(containerId).exec()
    }

    private fun checkIfImageExists(): Boolean {
        dockerClient.listImagesCmd().exec().forEach { image ->
            if (image.repoTags.any { it == fullImage }) return true
        }
        return false
    }

    private fun buildDockerImage(dockerClient: DockerClient, imageName: String, imageTag: String) {
        log.info("buildDockerImage: Building Docker image $imageName:$imageTag")

        val hardhatNodeParams = when (val nodeMode = config.nodeMode) {
            is NodeMode.Fork -> listOf("--fork", nodeMode.realNodeRpc)
            is NodeMode.Local -> emptyList()
        }

        val dockerfileContent = HardHatDockerfile(
            jsConfigJs = HardHatConfigJs(
                chainId = chainId,
                blockNumber = config.blockNumber
            ),
            hardhatVersion = config.hardhatVersion,
            commandLineParams = listOf("npx", "hardhat", "node") + hardhatNodeParams
        ).toDockerfileContent()

        val tmpDir = Files.createTempDirectory(this.javaClass.simpleName)
        val tmpFile = Files.writeString(tmpDir.resolve("Dockerfile"), dockerfileContent).toFile().also {
            it.deleteOnExit()
        }

        val callback = BuildImageResultCallback()

        // Build the Docker image from the just created Dockerfile
        dockerClient.buildImageCmd()
            .withDockerfile(tmpFile)
            .withTags(setOf("$imageName:$imageTag"))
            .exec<ResultCallback<BuildResponseItem>>(callback)

        val imageId = callback.awaitImageId()

        tmpFile.delete()
        log.info("buildDockerImage: finished building $fullImage, imageId: $imageId")
    }

    companion object {
        private val log = LoggerFactory.getLogger(HardhatNode::class.java)
        private val MAX_WAIT_BOOT_TIME = 60.seconds

        @JvmStatic
        fun start(config: HardHatNodeConfig): HardhatNode {
            return HardhatNode(config).also { it.run() }
        }
    }
}