package vc.rux.localnode

import com.github.dockerjava.core.DockerClientBuilder

internal val defaultDockerClient by lazy {
    DockerClientBuilder.getInstance().build()
}