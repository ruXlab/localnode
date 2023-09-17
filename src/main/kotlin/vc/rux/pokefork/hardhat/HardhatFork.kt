package vc.rux.pokefork.hardhat

import com.github.dockerjava.api.DockerClient
import com.github.dockerjava.api.async.ResultCallback
import com.github.dockerjava.api.command.BuildImageResultCallback
import com.github.dockerjava.api.command.CreateContainerResponse
import com.github.dockerjava.api.model.BuildResponseItem
import com.github.dockerjava.api.model.HostConfig
import org.slf4j.LoggerFactory
import vc.rux.pokefork.defaultDockerClient
import java.io.BufferedReader
import java.nio.file.Files
import java.nio.file.Path


class HardhatFork private constructor(
    private val config: HardHatForkConfig,
    private val dockerClient: DockerClient = defaultDockerClient
){
    private val imageName: String by config::imageName
    private val imageTag = config.imageTag ?: "hardhat-${config.hardhatVersion}-chainId-${config.networkId}"
    private val fullImage = "$imageName:$imageTag"

    private lateinit var containerId: String
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

        log.info("Container's port ${mappedRpcPort.first} is bound to ${mappedRpcPort.second}")
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

        val dockerfileContent = this.javaClass.classLoader
            .getResourceAsStream("Dockerfile.hardhat").bufferedReader().use(BufferedReader::readText)
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
        @JvmStatic
        fun fork(config: HardHatForkConfig): HardhatFork {
            return HardhatFork(config).also { it.run() }
        }
    }
}