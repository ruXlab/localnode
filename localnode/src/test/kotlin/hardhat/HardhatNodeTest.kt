package hardhat

import assertk.assertThat
import assertk.assertions.isEqualTo
import assertk.assertions.isNotNull
import assertk.assertions.isNull
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.ValueSource
import vc.rux.localnode.defaultDockerClient
import vc.rux.localnode.hardhat.HardHatNodeConfig
import vc.rux.localnode.hardhat.HardhatNode

class HardhatTest {

    @ValueSource(strings = ["local", "fork"])
    @ParameterizedTest(name = "{0} mode: container must be shut down after stop called")
    fun `fork mode - container must be shut down after stop called`(nodeMode: String) {
        // given
        val config = when(nodeMode) {
            "fork" -> HardHatNodeConfig.fork("https://rpc.ankr.com/eth", 31337)
            "local" -> HardHatNodeConfig.local()
            else -> throw IllegalArgumentException("Unsupported new nodeMode: $nodeMode, please update the test")
        }
        val connection = HardhatNode.start(config)

        // given precondition: given container is running
        var container = defaultDockerClient.listContainersCmd().exec()
            .singleOrNull { it.id == connection.containerId }
        assertThat(container)
            .isNotNull()
            .transform { it.state }
            .isEqualTo("running")


        // when
        connection.stop()

        // then
        container = defaultDockerClient.listContainersCmd().exec()
            .singleOrNull { it.id == connection.containerId }
        assertThat(container).isNull()
    }
}