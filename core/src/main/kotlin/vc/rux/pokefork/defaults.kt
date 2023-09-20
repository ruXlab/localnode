package vc.rux.pokefork

import com.github.dockerjava.core.DockerClientBuilder

internal val defaultDockerClient by lazy {
    DockerClientBuilder.getInstance().build()
}