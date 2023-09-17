package vc.rux.pokefork.hardhat

import com.github.dockerjava.api.DockerClient
import com.github.dockerjava.api.async.ResultCallback
import com.github.dockerjava.api.command.BuildImageResultCallback
import com.github.dockerjava.api.command.CreateContainerResponse
import com.github.dockerjava.api.model.BuildResponseItem
import com.github.dockerjava.api.model.HostConfig
import org.slf4j.LoggerFactory
import vc.rux.pokefork.defaultDockerClient
import vc.rux.pokefork.hardhat.internal.HardHatConfigJs
import vc.rux.pokefork.hardhat.internal.HardHatDockerfile
import java.net.HttpURLConnection
import java.net.URI
import java.net.URL
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.nio.file.Files
import kotlin.time.Duration.Companion.seconds
import kotlin.time.toJavaDuration


class HardhatFork private constructor(
    private val config: HardHatForkConfig,
    private val dockerClient: DockerClient = defaultDockerClient
){
    private val networkId = config.networkId ?: 31337
    private val imageName: String by config::imageName
    private val imageTag = config.imageTag ?: "hardhat-${config.hardhatVersion}-chainId-${networkId}"
    private val fullImage = "$imageName:$imageTag"

    private lateinit var containerId: String

    lateinit var localRpcNodeUrl: String

    private fun run() {
        if (!checkIfImageExists())
            buildDockerImage(dockerClient, imageName, imageTag)
        
        val containerConfig = dockerClient.createContainerCmd(fullImage)
            .withHostConfig(HostConfig.newHostConfig().withPublishAllPorts(true))

        // Create the container
        val containerResponse: CreateContainerResponse = containerConfig.exec()
        containerId = containerResponse.id

        log.info("run: created container $containerId")

        // Start the container
        dockerClient.startContainerCmd(containerId).exec()

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
        
        // Stop and remove the container when done
        dockerClient.stopContainerCmd(containerId).exec()
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

        val dockerfileContent = HardHatDockerfile(
            jsConfigJs = HardHatConfigJs(
                chainId = networkId,
                blockNumber = config.blockNumber
            ),
            hardhatVersion = config.hardhatVersion
        ).toDockerfileContent()
        
        val tmpDir = Files.createTempDirectory(this.javaClass.simpleName)
        val tmpFile = Files.writeString(tmpDir.resolve("Dockerfile"), dockerfileContent).toFile().also {
            it.deleteOnExit()
        }

        val callback = BuildImageResultCallback()
        // Build the Docker image from a Dockerfile
        dockerClient.buildImageCmd()
            .withBuildArg("HARDHAT_VERSION", config.hardhatVersion)
            .withBuildArg("NETWORK_ID", config.networkId?.toString() ?: "31337")
            .withDockerfile(tmpFile)
            .withTags(setOf("$imageName:$imageTag"))
            .exec<ResultCallback<BuildResponseItem>>(callback)

        val imageId = callback.awaitImageId()

        tmpFile.delete()
        log.info("buildDockerImage: finished building $fullImage, imageId: $imageId")
    }

    companion object {
        private val log = LoggerFactory.getLogger(HardhatFork::class.java)
        private val MAX_WAIT_BOOT_TIME = 60.seconds

        @JvmStatic
        fun fork(config: HardHatForkConfig): HardhatFork {
            return HardhatFork(config).also { it.run() }
        }
    }
}