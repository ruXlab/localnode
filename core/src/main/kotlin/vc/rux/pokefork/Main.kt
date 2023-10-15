package vc.rux.pokefork

import vc.rux.pokefork.hardhat.HardHatNodeConfig
import vc.rux.pokefork.hardhat.HardhatNode

fun main() {
    val hhFork = HardhatNode.start(
        HardHatNodeConfig.fork(
            "https://rpc.ankr.com/eth",
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
