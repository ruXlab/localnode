package vc.rux.pokefork

import com.github.dockerjava.api.DockerClient
import com.github.dockerjava.api.command.CreateContainerResponse
import com.github.dockerjava.api.model.HostConfig
import com.github.dockerjava.api.model.PortBinding
import com.github.dockerjava.core.DockerClientBuilder
import com.github.dockerjava.core.command.PullImageResultCallback
import com.github.dockerjava.api.command.CreateContainerCmd
import vc.rux.pokefork.hardhat.HardHatForkConfig
import vc.rux.pokefork.hardhat.HardhatFork

fun main() {
    val hhFork = HardhatFork.fork(
        HardHatForkConfig(
            rpcUrl = "https://rpc.ankr.com/eth",
            blockNumber = null,
        )
    )
    hhFork.stop()
    // Initialize the Docker client
//    val dockerClient: DockerClient = DockerClientBuilder.getInstance().build()
//
//    // Pull the Docker image (if not already available locally)
//    dockerClient.pullImageCmd("nginx:latest")
//        .exec(PullImageResultCallback())
//        .awaitSuccess()
//
//    // Create a container configuration
//    val containerConfig: CreateContainerCmd = dockerClient.createContainerCmd("nginx:latest")
//        .withHostConfig(HostConfig.newHostConfig().withPortBindings(PortBinding.parse("80:80")))
//
//    // Create the container
//    val containerResponse: CreateContainerResponse = containerConfig.exec()
//    val containerId: String = containerResponse.id
//
//    // Start the container
//    dockerClient.startContainerCmd(containerId).exec()
//
//    // Stop and remove the container when done
//    dockerClient.stopContainerCmd(containerId).exec()
//    dockerClient.removeContainerCmd(containerId).exec()
}
